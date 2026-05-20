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

import androidx.collection.ArrayMap
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.bridge.LoggerBridge
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.isAuthServerAccount
import com.movtery.zalithlauncher.game.account.isLocalAccount
import com.movtery.zalithlauncher.game.account.offline.OfflineYggdrasilServer
import com.movtery.zalithlauncher.game.multirt.Runtime
import com.movtery.zalithlauncher.game.path.getAssetsHome
import com.movtery.zalithlauncher.game.path.getLibrariesHome
import com.movtery.zalithlauncher.game.plugin.natives.NativePluginManager
import com.movtery.zalithlauncher.game.version.download.artifactToPath
import com.movtery.zalithlauncher.game.version.download.filterLibrary
import com.movtery.zalithlauncher.game.version.download.getLibraryReplacement
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionInfo
import com.movtery.zalithlauncher.game.version.installed.getGameManifest
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.info.InfoDistributor
import com.movtery.zalithlauncher.path.LibPath
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.screens.content.elements.QuickPlay
import com.movtery.zalithlauncher.utils.file.child
import com.movtery.zalithlauncher.utils.logging.Logger.lDebug
import com.movtery.zalithlauncher.utils.logging.Logger.lInfo
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import com.movtery.zalithlauncher.utils.network.ServerAddress
import com.movtery.zalithlauncher.utils.string.compareVersion
import com.movtery.zalithlauncher.utils.string.insertJSONValueList
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank
import com.movtery.zalithlauncher.utils.string.isLowerTo
import com.movtery.zalithlauncher.utils.string.isNotEmptyOrBlank
import com.movtery.zalithlauncher.utils.string.toUnicodeEscaped
import java.io.File

class LaunchArgs(
    private val runtimeLibraryPath: String,
    private val account: Account,
    private val offlineServer: OfflineYggdrasilServer,
    private val gameDirPath: File,
    private val version: Version,
    private val clientJar: File,
    private val gameManifest: GameManifest,
    private val runtime: Runtime,
    private val readAssetsFile: (path: String) -> String,
    private val getCacioJavaArgs: (isJava8: Boolean) -> List<String>
) {
    fun getAllArgs(): List<String> {
        val argsList: MutableList<String> = ArrayList()

        argsList.addAll(getJavaArgs())
        argsList.addAll(getMinecraftJVMArgs())
        argsList.addAll(NativePluginManager.getJVMEnv())

        if (runtime.javaVersion > 8) {
            argsList.add("--add-exports")
            val pkg: String = gameManifest.mainClass.substring(0, gameManifest.mainClass.lastIndexOf("."))
            argsList.add("$pkg/$pkg=ALL-UNNAMED")
        }

        argsList.add(gameManifest.mainClass)
        argsList.addAll(getMinecraftClientArgs())

        version.getVersionInfo()?.let { info ->
            val quickPlay = version.quickPlaySingle
            if (quickPlay != null) {
                when (quickPlay) {
                    is QuickPlay.Save -> {
                        if (quickPlay.saveName.isEmptyOrBlank()) return@let

                        if (info.quickPlay.isQuickPlaySingleplayer) {
                            //将不受支持的字符转换为Unicode
                            val saveName = quickPlay.saveName.toUnicodeEscaped()
                            argsList.apply {
                                add("--quickPlaySingleplayer")
                                add(saveName)
                            }
                        } else {
                            val msg = "Quick Play for singleplayer is not supported and has been skipped."
                            LoggerBridge.append(msg)
                            lWarning(msg)
                        }
                    }
                    is QuickPlay.Server -> {
                        argsList.addQuickPlayServer(
                            address = quickPlay.serverAddress,
                            quickPlay = info.quickPlay
                        )
                    }
                }
            } else {
                version.getServerIp()?.let { address ->
                    argsList.addQuickPlayServer(
                        address = address,
                        quickPlay = info.quickPlay
                    )
                }
            }
        }

        return argsList
    }

    private fun MutableList<String>.addQuickPlayServer(
        address: String,
        quickPlay: VersionInfo.QuickPlay
    ) {
        runCatching {
            ServerAddress.parse(address)
        }.onFailure {
            val msg = "Unable to resolve the server address: $address. The automatic server join feature is unavailable."
            LoggerBridge.append(msg)
            lWarning(msg, it)
        }.getOrNull()?.let { parsed ->
            val args = if (quickPlay.isQuickPlayMultiplayer) {
                val port = if (parsed.port < 0) {
                    ServerAddress.DEFAULT_PORT
                } else {
                    parsed.port
                }

                listOf(
                    "--quickPlayMultiplayer",
                    "${parsed.getASCIIHost()}:$port"
                )
            } else {
                val port = parsed.port.takeIf { it >= 0 } ?: ServerAddress.DEFAULT_PORT
                listOf("--server", parsed.getASCIIHost(), "--port", port.toString())
            }

            addAll(args)
        }
    }

    private fun getLWJGL3ClassPath(): String =
        File(PathManager.DIR_COMPONENTS, "lwjgl3")
            .listFiles { file -> file.name.endsWith(".jar") }
            ?.joinToString(":") { it.absolutePath }
            ?: ""

    private fun getJavaArgs(): List<String> {
        val argsList: MutableList<String> = ArrayList()

        if (account.isLocalAccount()) {
            if (account.hasSkinFile) {
                //该离线账号拥有本地皮肤，启用离线yggdrasil服务器
                offlineServer.start()
                offlineServer.addCharacter(account)
                offlineServer.getPort()?.let { port ->
                    val msg = "Using offline Yggdrasil server on port $port"
                    LoggerBridge.append(msg)
                    lInfo(msg)
                    argsList.add("-javaagent:${LibPath.AUTHLIB_INJECTOR.absolutePath}=http://localhost:$port")
                    argsList.add("-Dauthlibinjector.side=client")
                } ?: run {
                    //无法获取端口号，说明服务器未成功启动
                    val msg = "Failed to start offline Yggdrasil server!"
                    LoggerBridge.append(msg)
                    lWarning(msg)
                    //本次启动将被忽略，为避免浪费性能，关停服务器
                    offlineServer.stop()
                }
            }
        } else if (account.isAuthServerAccount()) {
            if (account.otherBaseUrl!!.contains("auth.mc-user.com")) {
                argsList.add("-javaagent:${LibPath.NIDE_8_AUTH.absolutePath}=${account.otherBaseUrl!!.replace("https://auth.mc-user.com:233/", "")}")
                argsList.add("-Dnide8auth.client=true")
            } else {
                argsList.add("-javaagent:${LibPath.AUTHLIB_INJECTOR.absolutePath}=${account.otherBaseUrl}")
                argsList.add("-Dauthlibinjector.side=client")
            }
        }

        argsList.addAll(getCacioJavaArgs(runtime.javaVersion == 8))

        val configFilePath = version.getVersionPath().child("log4j2.xml")
        if (!configFilePath.exists()) {
            val is7 = (version.getVersionInfo()?.minecraftVersion ?: "0.0").isLowerTo("1.12")
            runCatching {
                val content = if (is7) {
                    readAssetsFile("components/log4j-1.7.xml")
                } else {
                    readAssetsFile("components/log4j-1.12.xml")
                }
                configFilePath.writeText(content)
            }.onFailure {
                lWarning("Failed to write fallback Log4j configuration autonomously!", it)
            }
        }
        argsList.add("-Dlog4j.configurationFile=${configFilePath.absolutePath}")
        argsList.add("-Dminecraft.client.jar=${clientJar.absolutePath}")
        argsList.add("-Dminecraft.launcher.brand=${InfoDistributor.LAUNCHER_NAME}")
        argsList.add("-Dminecraft.launcher.version=${BuildConfig.VERSION_NAME}")

        return argsList
    }

    private fun getMinecraftJVMArgs(): Array<String> {
        val gameManifest1 = getGameManifest(version, skipInheriting = true)

//        // Parse Forge 1.17+ additional JVM Arguments
//        if (versionInfo.inheritsFrom == null || versionInfo.arguments == null || versionInfo.arguments.jvm == null) {
//            return emptyArray()
//        }

        val varArgMap: MutableMap<String, String> = android.util.ArrayMap()
        val launchClassPath = "${getLWJGL3ClassPath()}:${generateLaunchClassPath(gameManifest)}"
        var hasClasspath = false //是否已经在jvm参数中包含 ${classpath} 配置

        varArgMap["classpath_separator"] = ":"
        varArgMap["library_directory"] = getLibrariesHome()
        varArgMap["version_name"] = gameManifest1.id
        varArgMap["natives_directory"] = runtimeLibraryPath
        setLauncherInfo(varArgMap)

        fun Any.processJvmArg(): String? = (this as? String)?.let { argument ->
            if (argument.startsWith("-Djava.library.path=")) {
                //26.2+ Mojang 更改到了具体的路径，需要手动重定向
                return@let $$"-Djava.library.path=${natives_directory}"
            }
            when {
                argument.startsWith("-DignoreList=") -> {
                    "$argument,${version.getVersionName()}.jar"
                }
                argument.contains("-Dio.netty.native.workdir") ||
                argument.contains("-Djna.tmpdir") ||
                argument.contains("-Dorg.lwjgl.system.SharedLibraryExtractPath") -> {
                    //使用一个可读的目录
                    argument.replace($$"${natives_directory}", PathManager.DIR_CACHE.absolutePath)
                }
                argument == $$"${classpath}" -> {
                    hasClasspath = true
                    launchClassPath
                }
                else -> argument
            }
        }

        val jvmArgs = gameManifest1.arguments?.jvm
            ?.mapNotNull { it.processJvmArg() }
            ?.toTypedArray()
            ?: emptyArray()

        val replacedArgs = insertJSONValueList(jvmArgs, varArgMap)
        return if (hasClasspath) {
            replacedArgs
        } else {
            //不包含 ${classpath} 配置，则需要手动添加
            replacedArgs + arrayOf("-cp", launchClassPath)
        }
    }

    /**
     * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/a6f3fc0/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/Tools.java#L572-L592)
     */
    private fun generateLaunchClassPath(gameManifest: GameManifest): String {
        val classpathList = mutableListOf<String>()
        val classpath: Array<String> = generateLibClasspath(gameManifest)

        for (jarFile in classpath) {
            val jarFileObj = File(jarFile)
            if (!jarFileObj.exists()) {
                lDebug("Ignored non-exists file: $jarFile")
                continue
            }
            classpathList.add(jarFile)
        }
        if (clientJar.exists()) {
            classpathList.add(clientJar.absolutePath)
        }

        return classpathList.joinToString(":")
    }

    /**
     * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/a6f3fc0/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/Tools.java#L871-L882)
     */
    private fun generateLibClasspath(gameManifest: GameManifest): Array<String> {
        val libSortFix = LibSortFix(version.getVersionInfo())
        val libs = LinkedHashMap<GameManifest.Library, String>()

        for (libItem in gameManifest.libraries) {
            if (!GameManifest.Rule.checkRules(libItem.rules)) {
                lDebug("Library ignored due to unmatched rules: ${libItem.name}")
                continue
            }
            if (libItem.isNative) {
                lDebug("Library ignored because it is a native library: ${libItem.name}")
                continue
            }
            val path = libItem.progressLibrary() ?: run {
                lDebug("Library ignored due to library filtering: ${libItem.name}")
                continue
            }
            with(libSortFix) {
                libs.insertLib(libItem, getLibrariesHome() + "/" + path)
            }
        }

        //最后进行去重
        val deduplicated = LinkedHashMap<GameManifest.Library, String>()
        val bestVersionMap = mutableMapOf<String, Pair<GameManifest.Library, String>>()

        for ((lib, path) in libs) {
            val nameParts = lib.name.split(":")
            if (nameParts.size < 3) {
                deduplicated[lib] = path
                continue
            }
            val groupArtifact = "${nameParts[0]}:${nameParts[1]}"
            val version = nameParts[2]

            val existing = bestVersionMap[groupArtifact]
            if (existing == null) {
                bestVersionMap[groupArtifact] = lib to path
                deduplicated[lib] = path
            } else {
                val existingVersion = existing.first.name.split(":")[2]
                val cmp = version.compareVersion(existingVersion)
                if (cmp > 0) {
                    //重复库，仅保留高版本
                    lInfo("Duplicate library detected: $groupArtifact, replacing version $existingVersion with higher version $version")
                    deduplicated.remove(existing.first)
                    bestVersionMap[groupArtifact] = lib to path
                    deduplicated[lib] = path
                } else if (cmp < 0) {
                    lDebug("Duplicate library detected: $groupArtifact, ignoring lower version $version (keeping $existingVersion)")
                } else {
                    //版本重复，仅保留一个
                    lDebug("Duplicate library detected: $groupArtifact, ignoring duplicate version $version (keeping first occurrence)")
                }
            }
        }

        return deduplicated.values.toTypedArray<String>()
    }


    /**
     * @return 库相对路径
     */
    private fun GameManifest.Library.progressLibrary(): String? {
        if (filterLibrary()) return null

        var path = artifactToPath(this)

        val versionSegment = name.split(":").getOrNull(2) ?: return path
        val versionParts = versionSegment.split(".")

        getLibraryReplacement(name, versionParts)?.let { replacement ->
            lDebug("Library ${this.name} has been changed to version ${replacement.newName.split(":").last()}")
            path = replacement.newPath
        }

        return path
    }

    private fun getMinecraftClientArgs(): Array<String> {
        val varArgMap: MutableMap<String, String> = ArrayMap()
        varArgMap["auth_session"] = account.accessToken
        varArgMap["auth_access_token"] = account.accessToken
        varArgMap["auth_player_name"] = account.username
        varArgMap["auth_uuid"] = account.profileId.replace("-", "")
        varArgMap["auth_xuid"] = account.xUid ?: ""
        varArgMap["assets_root"] = getAssetsHome()
        varArgMap["assets_index_name"] = gameManifest.assetIndex.id
        varArgMap["game_assets"] = getAssetsHome()
        varArgMap["game_directory"] = gameDirPath.absolutePath
        varArgMap["user_properties"] = "{}"
        varArgMap["user_type"] = "msa"
        varArgMap["version_name"] = gameManifest.id

        setLauncherInfo(varArgMap)

        val minecraftArgs: MutableList<String> = ArrayList()
        gameManifest.arguments?.apply {
            // Support Minecraft 1.13+
            game.forEach { if (it is String) minecraftArgs.add(it) }
        }

        return insertJSONValueList(
            splitAndFilterEmpty(
                gameManifest.minecraftArguments ?:
                minecraftArgs.toTypedArray().joinToString(" ")
            ), varArgMap
        )
    }

    private fun setLauncherInfo(verArgMap: MutableMap<String, String>) {
        verArgMap["launcher_name"] = InfoDistributor.LAUNCHER_NAME
        verArgMap["launcher_version"] = BuildConfig.VERSION_NAME
        verArgMap["version_type"] = version.getCustomInfo()
            .takeIf { it.isNotEmptyOrBlank() }
            ?: gameManifest.type
    }

    private fun splitAndFilterEmpty(arg: String): Array<String> {
        val list: MutableList<String> = ArrayList()
        arg.split(" ").forEach {
            if (it.isNotEmpty()) list.add(it)
        }
        return list.toTypedArray()
    }
}