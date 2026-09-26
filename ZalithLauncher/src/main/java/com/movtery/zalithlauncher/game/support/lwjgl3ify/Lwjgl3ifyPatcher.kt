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

package com.movtery.zalithlauncher.game.support.lwjgl3ify

import com.movtery.zalithlauncher.game.path.getLibrariesHome
import com.movtery.zalithlauncher.game.version.download.artifactToPath
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionInfoParser
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.isEnabled
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.string.isBiggerTo
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipFile

private const val TAG = "Lwjgl3ifyPatcher"

/*
 * GTNH(GT New Horizons) / lwjgl3ify 兼容层
 *
 * lwjgl3ify 3.x 的 mod jar 内嵌一份完整的启动清单（RetroFuturaBootstrap 主类、
 * Java 17+ 的 --add-opens JVM 参数以及 Forge 1.7.10 全量依赖库）。
 *
 * 参考 FCL 的 Lwjgl3ifyPatcher（https://github.com/FCL-Team/FoldCraftLauncher/blob/main/FCL/src/main/java/com/tungsten/fcl/game/Lwjgl3ifyPatcher.java）
 */

private const val GTNH_MAVEN = "https://nexus.gtnewhorizons.com/repository/public/"
private const val FORGE_MAVEN = "https://maven.minecraftforge.net/"
private const val EMBEDDED_VERSION_JSON = "me/eigenraven/lwjgl3ify/relauncher/version.json"
private const val EMBEDDED_FORGE_PATCHES = "me/eigenraven/lwjgl3ify/relauncher/forgePatches.zip"

/**
 * 检测 mods 中已启用的 lwjgl3ify，把内嵌启动清单合并进当前版本后返回
 * @return 补丁后的版本清单；未检测到 lwjgl3ify 或清单不可用时为 null
 */
fun patchLwjgl3ifyIfNeeded(version: Version, mods: List<LocalMod>): GameManifest? {
    return runCatching {
        val lwjgl3ify = findLwjgl3ifyMod(mods) ?: return null
        Logger.info(TAG, "Detected lwjgl3ify ${lwjgl3ify.version.orEmpty()} in mods folder")

        val current = VersionInfoParser(version).setInheriting().build()

        val embedded = ZipFile(lwjgl3ify.file).use { zip ->
            readEmbeddedManifest(zip)?.also { manifest ->
                ensureForgePatchesFile(version, manifest, zip)
            }
        } ?: return null
        ensureLwjgl3ifyConfig(File(version.getGameDir(), "config"))

        if (isLwjgl3ifyManifest(current) &&
            findForgePatchesLibrary(current)?.name == findForgePatchesLibrary(embedded)?.name
        ) {
            //磁盘清单已是同版本的补丁产物（可能由其他启动器写入），直接可用
            return current
        }

        val patched = patchManifest(current, embedded) ?: run {
            Logger.warning(TAG, "${lwjgl3ify.file.name} does not contain a usable lwjgl3ify relauncher manifest, skipped")
            return null
        }
        Logger.info(
            TAG, "Patched version ${version.getVersionName()} with lwjgl3ify from ${lwjgl3ify.file.name}, " +
                    "mainClass=${patched.mainClass}, java=${patched.javaVersion?.majorVersion}"
        )
        patched
    }.onFailure {
        Logger.warning(TAG, "Failed to apply the lwjgl3ify patch", it)
    }.getOrNull()
}

/** 清单是否已经以 RetroFuturaBootstrap 为入口 */
private fun isLwjgl3ifyManifest(manifest: GameManifest): Boolean =
    manifest.mainClass?.startsWith("com.gtnewhorizons.retrofuturabootstrap") == true

/**
 * 从 mod 列表中取已启用且版本最高的 lwjgl3ify
 */
private fun findLwjgl3ifyMod(mods: List<LocalMod>): LocalMod? {
    return mods.asSequence()
        .filter { !it.notMod && it.id == "lwjgl3ify" && it.file.isEnabled() }
        .maxWithOrNull { a, b ->
            when {
                a.version.orEmpty().isBiggerTo(b.version.orEmpty()) -> 1
                b.version.orEmpty().isBiggerTo(a.version.orEmpty()) -> -1
                else -> 0
            }
        }
}

/**
 * 合并内嵌清单的 RFB 入口、JVM 参数、Java 版本与完整依赖闭包；
 * 仅处理 lwjgl3ify 2.x/3.x 的现代格式清单
 */
private fun patchManifest(current: GameManifest, embedded: GameManifest): GameManifest? {
    val mainClass = embedded.mainClass ?: return null
    val arguments = embedded.arguments ?: return null
    if (arguments.jvm.isNullOrEmpty()) return null

    current.mainClass = mainClass
    current.minecraftArguments = null
    current.arguments = arguments
    embedded.javaVersion?.let { current.javaVersion = it }
    current.libraries = patchLibraryUrls(embedded.libraries ?: return null)
    return current
}

/**
 * 为缺少下载地址的库（旧版 lwjgl3ify）按 groupId 重写 Maven 源；
 * 3.x 的内嵌清单已自带正确地址，此分支基本不触发
 */
private fun patchLibraryUrls(libraries: List<GameManifest.Library>): List<GameManifest.Library> {
    return libraries.onEach { library ->
        if (!library.downloads?.artifact?.url.isNullOrEmpty()) return@onEach
        library.url = resolveLibraryBase(library) ?: return@onEach
    }
}

private fun resolveLibraryBase(library: GameManifest.Library): String? {
    val groupId = library.name?.split(":")?.firstOrNull() ?: return null
    return when {
        groupId.startsWith("com.github.GTNewHorizons") -> GTNH_MAVEN
        //1.7.10 Forge 的传递依赖（Forge/Scala/Akka）托管在 Forge Maven
        groupId.startsWith("net.minecraftforge") ||
                groupId.startsWith("org.scala-lang") ||
                groupId.startsWith("com.typesafe") -> FORGE_MAVEN
        else -> null //其余库均可从 libraries.minecraft.net 命中
    }
}

private fun readEmbeddedManifest(zip: ZipFile): GameManifest? {
    val entry = zip.getEntry(EMBEDDED_VERSION_JSON) ?: return null
    return runCatching {
        zip.getInputStream(entry).use { input ->
            InputStreamReader(input, Charsets.UTF_8).use { reader ->
                GSON.fromJson(reader, GameManifest::class.java)
            }
        }
    }.onFailure {
        Logger.warning(TAG, "Cannot read the lwjgl3ify relauncher manifest from ${zip.name}", it)
    }.getOrNull()?.takeIf {
        isLwjgl3ifyManifest(it)
    } ?: run {
        Logger.warning(TAG, "${zip.name} does not contain a valid lwjgl3ify relauncher manifest, skipped")
        null
    }
}

/**
 * 从内嵌的 forgePatches.zip 离线还原该构件（GTNH Maven 无国内镜像，解压使其不依赖网络）；
 * 写临时文件后原子替换，中断不会留下截断的文件遮蔽重试
 */
private fun ensureForgePatchesFile(version: Version, embedded: GameManifest, zip: ZipFile) {
    val library = findForgePatchesLibrary(embedded) ?: return
    val target = File(getLibrariesHome(version.getGameHome()), artifactToPath(library) ?: return)
    if (target.isFile) return

    val entry = zip.getEntry(EMBEDDED_FORGE_PATCHES) ?: run {
        Logger.warning(TAG, "$EMBEDDED_FORGE_PATCHES not found in ${zip.name}")
        return
    }
    runCatching {
        target.parentFile?.mkdirs()
        val temp = File.createTempFile(target.name, ".tmp", target.parentFile)
        try {
            zip.getInputStream(entry).use { input ->
                temp.outputStream().use { output -> input.copyTo(output) }
            }
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            temp.delete()
            throw e
        }
        Logger.info(TAG, "Extracted the embedded forgePatches to ${target.absolutePath}")
    }.onFailure {
        Logger.warning(TAG, "Failed to extract the embedded forgePatches, will fall back to downloading from GTNH Maven", it)
    }
}

private fun findForgePatchesLibrary(manifest: GameManifest): GameManifest.Library? {
    return manifest.libraries?.firstOrNull { library ->
        val coords = library.name?.split(":") ?: return@firstOrNull false
        coords.size > 3 &&
                coords[0] == "com.github.GTNewHorizons" &&
                coords[1] == "lwjgl3ify" &&
                coords[3] == "forgePatches"
    }
}

/**
 * 确保实例 config/lwjgl3ify.cfg 关闭 linuxCreateAppDesktopEntry：
 * lwjgl3ify 初始化时会向 XDG_DATA_HOME（或 ~/.local/share）写桌面快捷方式，
 * Android 上不存在该目录，异常会杀死 RFB 主线程导致游戏静默退出。
 * 已有配置时跳过注释行做行级检测，优先插入已有 window 段内，无该段才在文件尾追加
 */
private fun ensureLwjgl3ifyConfig(configDir: File) {
    runCatching {
        if (!configDir.isDirectory && !configDir.mkdirs()) return
        val cfg = File(configDir, "lwjgl3ify.cfg")
        val entry = "B:linuxCreateAppDesktopEntry=false"
        if (!cfg.isFile) {
            cfg.writeText("# Configuration file\n\nwindow {\n    $entry\n}\n")
            return
        }

        val text = cfg.readText()
        val hasEntry = text.lines().any { line ->
            val trimmed = line.trim()
            !trimmed.startsWith("#") && !trimmed.startsWith("//") && trimmed.contains("linuxCreateAppDesktopEntry")
        }
        if (hasEntry) return

        val window = Regex("^[ \t]*window\\s*\\{", RegexOption.MULTILINE).find(text)
        val insertAt = window?.let { text.indexOf('\n', it.range.last) }
        if (insertAt != null && insertAt >= 0) {
            cfg.writeText(text.substring(0, insertAt) + "\n    $entry" + text.substring(insertAt))
        } else {
            cfg.writeText("$text\nwindow {\n    $entry\n}\n")
        }
    }.onFailure {
        Logger.warning(TAG, "Failed to ensure lwjgl3ify.cfg", it)
    }
}
