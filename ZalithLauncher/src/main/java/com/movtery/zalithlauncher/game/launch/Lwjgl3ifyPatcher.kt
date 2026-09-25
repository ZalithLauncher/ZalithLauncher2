/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.launch

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.movtery.zalithlauncher.game.download.game.getLibraryPath
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.mod.isEnabled
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.file.ensureDirectory
import com.movtery.zalithlauncher.utils.file.readText
import com.movtery.zalithlauncher.utils.json.parseToJson
import com.movtery.zalithlauncher.utils.json.safeGetMember
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipFile

private const val TAG = "Lwjgl3ifyPatcher"

/**
 * GTNH / lwjgl3ify compatibility layer.
 *
 * The lwjgl3ify 3.x mod jar embeds a complete version.json (the RetroFuturaBootstrap
 * main class, Java 17+ --add-opens JVM arguments and the full Forge 1.7.10 library closure).
 * Before launching, detect lwjgl3ify in the instance's mods folder, merge the embedded
 * version.json into the instance version on disk, extract forgePatches offline and make
 * sure config/lwjgl3ify.cfg disables the desktop entry creation.
 */
object Lwjgl3ifyPatcher {
    /** RetroFuturaBootstrap 主类前缀，打补丁后的版本以此入口启动 */
    const val RFB_MAIN_CLASS_PREFIX = "com.gtnewhorizons.retrofuturabootstrap"

    private const val EMBEDDED_VERSION_JSON = "me/eigenraven/lwjgl3ify/relauncher/version.json"
    private const val EMBEDDED_FORGE_PATCHES = "me/eigenraven/lwjgl3ify/relauncher/forgePatches.zip"
    private const val BACKUP_SUFFIX = ".before-lwjgl3ify"
    private const val GTNH_MAVEN = "https://nexus.gtnewhorizons.com/repository/public/"
    private const val FORGE_MAVEN = "https://maven.minecraftforge.net/"
    private const val LIBRARIES_MAVEN = "https://libraries.minecraft.net/"

    private const val PROP_MAIN_CLASS = "mainClass"
    private const val PROP_JAVA_VERSION = "javaVersion"
    private const val PROP_ARGUMENTS = "arguments"
    private const val PROP_JVM = "jvm"
    private const val PROP_GAME = "game"
    private const val PROP_LIBRARIES = "libraries"
    private const val PROP_MINECRAFT_ARGUMENTS = "minecraftArguments"
    private const val PROP_INHERITS_FROM = "inheritsFrom"
    private const val PROP_NAME = "name"
    private const val PROP_URL = "url"
    private const val PROP_MAJOR_VERSION = "majorVersion"

    /** 内嵌 version.json 中 forgePatches 构件的库坐标 */
    private val FORGE_PATCHES_REGEX = Regex("^com\\.github\\.GTNewHorizons:lwjgl3ify:[^:]+:forgePatches$")

    private const val CFG_KEY = "B:linuxCreateAppDesktopEntry"
    private const val CFG_ENTRY = "$CFG_KEY=false"
    private const val DEFAULT_TARGET_JAVA_MAJOR = 21

    /**
     * 启动前检测 mods 目录中的 lwjgl3ify：将其内嵌的 version.json 合并进实例版本 JSON，
     * 离线解出 forgePatches 并补写 config/lwjgl3ify.cfg。
     * 任何失败都只记录日志，不阻断启动流程。
     */
    suspend fun patchIfNeeded(version: Version) {
        try {
            val versionJson = versionJsonFile(version)
            val modsDir = VersionFolders.MOD.getDir(version.getGameDir())
            val lwjgl3ifyJar = findLwjgl3ifyJar(modsDir)
            if (lwjgl3ifyJar == null) {
                //mods 中已无启用的 lwjgl3ify，还原被补丁的版本 JSON
                restoreIfNeeded(versionJson)
                return
            }
            Logger.info(TAG, "Detected enabled lwjgl3ify mod: ${lwjgl3ifyJar.name}")

            ZipFile(lwjgl3ifyJar).use { zip ->
                val embedded = readEmbeddedVersionJson(zip)
                if (embedded == null) {
                    Logger.warning(TAG, "${lwjgl3ifyJar.name} does not contain a valid lwjgl3ify relauncher version.json, skipping")
                    return
                }
                ensureForgePatches(version, embedded, zip)
                ensureCfg(version.getGameDir())

                val current = versionJson.readText().parseToJson()
                //已合并过同一版本的 lwjgl3ify，无需重复合并
                if (isPatchedWith(current, embedded)) return
                if (!isMergeable(embedded)) {
                    Logger.warning(TAG, "The embedded version.json of ${lwjgl3ifyJar.name} is not a valid RFB version, skipping")
                    return
                }
                backupOriginalVersionJson(versionJson)
                mergeVersionJson(current, embedded)
                versionJson.writeText(GSON.toJson(current))
                Logger.info(TAG, "Patched version ${version.getVersionName()} with lwjgl3ify from ${lwjgl3ifyJar.name}")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            //补丁失败不应阻断正常启动流程
            Logger.warning(TAG, "Failed to apply the lwjgl3ify patch", e)
        }
    }

    /** 版本 JSON 的主类是否为 RFB 入口（供 UI 判断该实例是否已启用 lwjgl3ify） */
    fun isLwjgl3ifyVersion(version: Version): Boolean {
        return try {
            versionJsonFile(version).readText().parseToJson()
                .safeGetMember(PROP_MAIN_CLASS).startsWith(RFB_MAIN_CLASS_PREFIX)
        } catch (e: Exception) {
            Logger.debug(TAG, "Failed to read the version json of ${version.getVersionName()}", e)
            false
        }
    }

    /**
     * 计算实际生效的 Java 大版本（只做数值计算，不加载运行时文件），
     * 选择逻辑与 GameLauncher.getRuntime() 保持一致；无法确定时返回 0
     */
    fun resolveEffectiveJavaMajor(version: Version): Int {
        val versionRuntime = version.getJavaRuntime()
        if (versionRuntime.isNotEmpty()) return RuntimesManager.loadRuntime(versionRuntime).javaVersion

        val target = readTargetJavaMajor(version)
        val picked = RuntimesManager.loadRuntime(AllSettings.javaRuntime.getValue())
        return if (AllSettings.autoPickJavaRuntime.getValue() &&
            (picked.javaVersion == 0 || picked.javaVersion < target)
        ) {
            RuntimesManager.getNearestJreName(target)
                ?.let { RuntimesManager.loadRuntime(it).javaVersion } ?: picked.javaVersion
        } else {
            picked.javaVersion
        }
    }

    private fun readTargetJavaMajor(version: Version): Int {
        return try {
            versionJsonFile(version).readText().parseToJson()
                .getAsJsonObject(PROP_JAVA_VERSION)?.get(PROP_MAJOR_VERSION)
                ?.takeIf { it.isJsonPrimitive }?.asInt
                ?: DEFAULT_TARGET_JAVA_MAJOR
        } catch (e: Exception) {
            Logger.debug(TAG, "Failed to read the target java version of ${version.getVersionName()}, fallback to $DEFAULT_TARGET_JAVA_MAJOR", e)
            DEFAULT_TARGET_JAVA_MAJOR
        }
    }

    /** 扫描 mods 目录，返回第一个内嵌 lwjgl3ify version.json 的启用 mod jar */
    private fun findLwjgl3ifyJar(modsDir: File): File? {
        if (!modsDir.isDirectory) return null
        return modsDir.listFiles()
            ?.filter { it.isFile && it.isEnabled() && it.name.endsWith(".jar", ignoreCase = true) }
            ?.firstOrNull { jar -> ZipFile(jar).use { zip -> zip.getEntry(EMBEDDED_VERSION_JSON) != null } }
    }

    private fun readEmbeddedVersionJson(zip: ZipFile): JsonObject? {
        if (zip.getEntry(EMBEDDED_VERSION_JSON) == null) return null
        return zip.readText(EMBEDDED_VERSION_JSON).parseToJson()
    }

    /** 版本是否已合并过同一 lwjgl3ify 的内嵌 version.json */
    private fun isPatchedWith(current: JsonObject, embedded: JsonObject): Boolean {
        if (!current.safeGetMember(PROP_MAIN_CLASS).startsWith(RFB_MAIN_CLASS_PREFIX)) return false
        return findForgePatchesName(current) == findForgePatchesName(embedded)
    }

    /** 在 libraries 数组中查找 forgePatches 构件的库坐标 */
    private fun findForgePatchesName(json: JsonObject): String? {
        val libraries = json.getAsJsonArray(PROP_LIBRARIES) ?: return null
        return libraries.mapNotNull { element ->
            (element as? JsonObject)?.safeGetMember(PROP_NAME)
                ?.takeIf { name -> name.matches(FORGE_PATCHES_REGEX) }
        }.firstOrNull()
    }

    /** 内嵌 version.json 是否具备合并条件：RFB 入口与完整的 jvm/game 参数 */
    private fun isMergeable(embedded: JsonObject): Boolean {
        if (!embedded.safeGetMember(PROP_MAIN_CLASS).startsWith(RFB_MAIN_CLASS_PREFIX)) return false
        val arguments = embedded.getAsJsonObject(PROP_ARGUMENTS) ?: return false
        val jvmArgs = arguments.get(PROP_JVM) as? JsonArray ?: return false
        val gameArgs = arguments.get(PROP_GAME) as? JsonArray ?: return false
        return jvmArgs.size() > 0 && gameArgs.size() > 0
    }

    /**
     * 将内嵌 version.json 的入口、参数与完整依赖库闭包合并进实例版本 JSON：
     * arguments/libraries 均为深拷贝整体替换，其余字段保持原样。
     */
    private fun mergeVersionJson(current: JsonObject, embedded: JsonObject) {
        current.remove(PROP_MINECRAFT_ARGUMENTS)
        current.remove(PROP_INHERITS_FROM)
        current.addProperty(PROP_MAIN_CLASS, embedded.safeGetMember(PROP_MAIN_CLASS))

        val arguments = embedded.getAsJsonObject(PROP_ARGUMENTS)?.deepCopy()
            ?: throw IllegalStateException("The embedded version.json has no arguments object")
        current.add(PROP_ARGUMENTS, arguments)

        val libraries = embedded.getAsJsonArray(PROP_LIBRARIES)?.deepCopy()
            ?: throw IllegalStateException("The embedded version.json has no libraries array")
        libraries.forEach { library -> fillLibraryUrl(library.asJsonObject) }
        current.add(PROP_LIBRARIES, libraries)

        //内嵌声明缺失 javaVersion 时保留实例原有的 Java 版本声明
        embedded.get(PROP_JAVA_VERSION)?.let { current.add(PROP_JAVA_VERSION, it.deepCopy()) }
    }

    /** 为缺少下载地址的库按 groupId 补默认 Maven 源（新版内嵌 JSON 已自带地址，此步为保底） */
    private fun fillLibraryUrl(library: JsonObject) {
        if (library.safeGetMember(PROP_URL).isNotEmpty()) return
        val name = library.safeGetMember(PROP_NAME)
        val base = when {
            name.startsWith("com.github.GTNewHorizons") -> GTNH_MAVEN
            name.startsWith("net.minecraftforge") ||
                    name.startsWith("org.scala-lang") ||
                    name.startsWith("com.typesafe") -> FORGE_MAVEN
            else -> LIBRARIES_MAVEN
        }
        library.addProperty(PROP_URL, base)
    }

    /**
     * 优先从 mod jar 内嵌的 forgePatches.zip 还原库文件，避免依赖 GTNH Nexus；
     * 后续的校验下载任务会按 libraries 中声明的地址补齐其余缺失的库。
     */
    private fun ensureForgePatches(version: Version, embedded: JsonObject, zip: ZipFile) {
        val name = findForgePatchesName(embedded) ?: run {
            Logger.warning(TAG, "The embedded version.json does not declare a forgePatches library")
            return
        }
        val target = File(getLibraryPath(name, version.getGameHome()))
        if (target.isFile && target.length() > 0) return
        val entry = zip.getEntry(EMBEDDED_FORGE_PATCHES) ?: run {
            Logger.warning(TAG, "$EMBEDDED_FORGE_PATCHES not found in ${zip.name}")
            return
        }
        val temp = File(target.parentFile, "${target.name}.tmp")
        try {
            target.parentFile?.ensureDirectory()
            zip.getInputStream(entry).use { input ->
                FileOutputStream(temp).use { output -> input.copyTo(output) }
            }
            //写临时文件后原子替换，中断不会留下截断的 jar
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
            Logger.info(TAG, "Extracted embedded forgePatches to ${target.absolutePath}")
        } finally {
            //清理中断残留的临时文件
            if (temp.isFile) temp.delete()
        }
    }

    /**
     * 确保实例 config/lwjgl3ify.cfg 关闭 linuxCreateAppDesktopEntry：
     * lwjgl3ify 初始化时会向 XDG_DATA_HOME（或 ~/.local/share）写入桌面快捷方式，
     * Android 上目录缺失会杀死 RFB 主线程导致游戏静默退出。
     */
    private fun ensureCfg(gameDir: File) {
        val configDir = File(gameDir, "config").ensureDirectory()
        val cfg = File(configDir, "lwjgl3ify.cfg")
        if (!cfg.isFile) {
            cfg.writeText("# Configuration file\n\nwindow {\n    $CFG_ENTRY\n}\n")
            Logger.info(TAG, "Created lwjgl3ify.cfg with linuxCreateAppDesktopEntry=false")
            return
        }
        val text = cfg.readText()
        if (containsActiveLine(text, CFG_KEY)) return
        val windowStart = Regex("(?m)^[ \t]*window\\s*\\{").find(text)
        val insertAt = windowStart?.let { match -> text.indexOf('\n', match.range.last) } ?: -1
        if (insertAt >= 0) {
            //插入到已有 window 段内
            cfg.writeText(text.substring(0, insertAt) + "\n    $CFG_ENTRY" + text.substring(insertAt))
            Logger.info(TAG, "Inserted linuxCreateAppDesktopEntry=false into the existing window section of lwjgl3ify.cfg")
        } else {
            cfg.writeText(text + "\nwindow {\n    $CFG_ENTRY\n}\n")
            Logger.info(TAG, "Appended the window section with linuxCreateAppDesktopEntry=false to lwjgl3ify.cfg")
        }
    }

    /** 跳过 # 与 // 注释行后，判断是否存在包含 key 的行 */
    private fun containsActiveLine(text: String, key: String): Boolean {
        return text.split('\n').any { line ->
            val trimmed = line.trim()
            !trimmed.startsWith("#") && !trimmed.startsWith("//") && trimmed.contains(key)
        }
    }

    /** mods 中已无启用的 lwjgl3ify 时，从备份还原被补丁过的版本 JSON */
    private fun restoreIfNeeded(versionJson: File) {
        if (!versionJson.isFile) return
        val current = versionJson.readText().parseToJson()
        if (!current.safeGetMember(PROP_MAIN_CLASS).startsWith(RFB_MAIN_CLASS_PREFIX)) return
        val backup = backupFile(versionJson)
        if (!backup.isFile) {
            //无法凭空还原，仅提示
            Logger.warning(TAG, "${versionJson.name} still uses the RFB main class, but no lwjgl3ify backup exists, skipping restore")
            return
        }
        backup.copyTo(versionJson, overwrite = true)
        if (!backup.delete()) {
            Logger.warning(TAG, "Failed to delete the lwjgl3ify backup ${backup.name}")
        }
        Logger.info(TAG, "lwjgl3ify is no longer present, restored ${versionJson.name} from ${backup.name}")
    }

    /** 首次合并前备份原版本 JSON（仅当备份不存在时创建，不覆盖已有备份） */
    private fun backupOriginalVersionJson(versionJson: File) {
        val backup = backupFile(versionJson)
        if (versionJson.isFile && !backup.exists()) {
            versionJson.copyTo(backup)
            Logger.info(TAG, "Backed up the original version json to ${backup.name}")
        }
    }

    private fun backupFile(versionJson: File): File =
        File(versionJson.parentFile, versionJson.name + BACKUP_SUFFIX)

    private fun versionJsonFile(version: Version): File =
        File(version.getVersionPath(), "${version.getVersionName()}.json")
}
