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

package com.movtery.zalithlauncher.game.download.game

import android.content.Context
import android.content.Intent
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskStage
import com.movtery.zalithlauncher.coroutine.TaskFlowExecutor
import com.movtery.zalithlauncher.coroutine.TaskLogOutput
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.coroutine.addTask
import com.movtery.zalithlauncher.coroutine.buildPhase
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.addons.modloader.cleanroom.CleanroomVersion
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.FabricLikeVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.ForgeLikeVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.NeoForgeVersion
import com.movtery.zalithlauncher.game.addons.modloader.modlike.ModVersion
import com.movtery.zalithlauncher.game.download.assets.platform.mcim.mapMCIMMirrorUrls
import com.movtery.zalithlauncher.game.download.game.cleanroom.getCleanroomDownloadTask
import com.movtery.zalithlauncher.game.download.game.cleanroom.targetTempCleanroomInstaller
import com.movtery.zalithlauncher.game.download.game.fabric.getFabricLikeCompleterTask
import com.movtery.zalithlauncher.game.download.game.fabric.getFabricLikeDownloadTask
import com.movtery.zalithlauncher.game.download.game.forge.getForgeLikeAnalyseTask
import com.movtery.zalithlauncher.game.download.game.forge.getForgeLikeDownloadTask
import com.movtery.zalithlauncher.game.download.game.forge.getForgeLikeInstallTask
import com.movtery.zalithlauncher.game.download.game.forge.isNeoForge
import com.movtery.zalithlauncher.game.download.game.forge.targetTempForgeLikeInstaller
import com.movtery.zalithlauncher.game.download.game.optifine.getOptiFineDownloadTask
import com.movtery.zalithlauncher.game.download.game.optifine.getOptiFineInstallTask
import com.movtery.zalithlauncher.game.download.game.optifine.getOptiFineModsDownloadTask
import com.movtery.zalithlauncher.game.download.game.optifine.isMinecraft17Plus
import com.movtery.zalithlauncher.game.download.game.optifine.targetTempOptiFineInstaller
import com.movtery.zalithlauncher.game.download.jvm_server.JVMSocketServer
import com.movtery.zalithlauncher.game.download.jvm_server.JvmService
import com.movtery.zalithlauncher.game.path.getGameHome
import com.movtery.zalithlauncher.game.path.getVersionsHome
import com.movtery.zalithlauncher.game.version.download.BaseMinecraftDownloader
import com.movtery.zalithlauncher.game.version.download.MinecraftDownloader
import com.movtery.zalithlauncher.game.version.installed.VersionConfig
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.file.copyDirectoryContents
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.downloadFileFromSources
import com.movtery.zalithlauncher.utils.network.withSpeedReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

private const val TAG = "GameInstaller"

/**
 * 在安装游戏前发现存在冲突的已安装版本，抛出这个异常
 */
private class GameAlreadyInstalledException : RuntimeException()

/**
 * 游戏安装器
 * @param context 用于获取任务描述信息
 * @param info 安装游戏所需要的信息，包括 Minecraft id、自定义版本名称、Addon 列表
 * @param scope 在有生命周期管理的scope中执行安装任务
 * @param logOutputHolder 安装 JVM 日志输出的容器
 */
class GameInstaller(
    private val context: Context,
    private val info: GameDownloadInfo,
    private val scope: CoroutineScope,
    private val logOutputHolder: MutableStateFlow<TaskLogOutput?> = MutableStateFlow(null),
    private val targetGameFolder: File = File(getGameHome())
) {
    private val taskExecutor = TaskFlowExecutor(scope)
    val tasksFlow: StateFlow<List<TitledTask>> = taskExecutor.tasksFlow

    /**
     * 安装 JVM 实时日志输出，仅在执行前台安装任务期间存在
     */
    val logOutput: StateFlow<TaskLogOutput?> = logOutputHolder.asStateFlow()

    /**
     * 基础下载器
     */
    private val downloader = BaseMinecraftDownloader(targetGameFolder.absolutePath)

    /**
     * 目标游戏客户端目录（缓存）
     * versions/<client-name>/...
     */
    private var targetClientDir: File? = null

    /**
     * 安装 Minecraft 游戏
     * @param isRunning 正在运行中，阻止此次安装时
     * @param onInstalled 游戏已完成安装
     * @param onError 游戏安装失败
     * @param onGameAlreadyInstalled 在安装游戏前发现存在冲突的已安装版本
     */
    fun installGame(
        isRunning: () -> Unit = {},
        onInstalled: (version: String) -> Unit,
        onError: (th: Throwable) -> Unit,
        onGameAlreadyInstalled: () -> Unit
    ) {
        if (taskExecutor.isRunning()) {
            //正在安装中，阻止这次安装请求
            isRunning()
            return
        }

        taskExecutor.executePhasesAsync(
            onStart = {
                val tasks = try {
                    getTaskPhase()
                } catch (_: GameAlreadyInstalledException) {
                    onGameAlreadyInstalled()
                    return@executePhasesAsync
                }
                taskExecutor.addPhases(tasks)
            },
            onComplete = {
                onInstalled(info.customVersionName)
            },
            onError = {
                onError(it)
            }
        )
    }

    /**
     * 修改已安装的版本：按当前 info 描述的 Minecraft 版本与加载器组合，原地重建该版本
     * @param isRunning 正在运行中，阻止此次修改时
     * @param onModified 版本已完成修改
     * @param onError 版本修改失败
     */
    fun modifyVersion(
        isRunning: () -> Unit = {},
        onModified: () -> Unit,
        onError: (th: Throwable) -> Unit
    ) {
        if (taskExecutor.isRunning()) {
            //正在修改中，阻止这次修改请求
            isRunning()
            return
        }

        taskExecutor.executePhasesAsync(
            onStart = {
                val tasks = getModifyTaskPhase()
                taskExecutor.addPhases(tasks)
            },
            onComplete = {
                onModified()
            },
            onError = {
                onError(it)
            }
        )
    }

    /**
     * 安装过程中所需的所有文件路径配置
     */
    private class InstallationPathConfig(
        val targetClientDir: File,
        val tempGameDir: File,
        val tempMinecraftDir: File,
        val tempGameVersionsDir: File,
        val tempClientDir: File,
        val tempModsDir: File,
        val optifineDir: File?,
        val forgeDir: File?,
        val neoforgeDir: File?,
        val fabricDir: File?,
        val legacyFabricDir: File?,
        val quiltDir: File?,
        val cleanroomDir: File?,
        val babricDir: File?
    )

    /**
     * 构建安装过程中使用的所有路径配置
     */
    private fun createPathConfig(checkVersionExists: Boolean = true): InstallationPathConfig {
        //目标版本目录
        val targetClientDir1 = File(getVersionsHome(targetGameFolder.absolutePath), info.customVersionName)
        targetClientDir = targetClientDir1
        val targetVersionJson = File(targetClientDir1, "${info.customVersionName}.json")

        //目标版本已经安装的情况，退出
        if (checkVersionExists && targetVersionJson.exists()) {
            Logger.debug(TAG, "The game has already been installed!")
            throw GameAlreadyInstalledException()
        }

        val tempGameDir = PathManager.DIR_CACHE_GAME_DOWNLOADER
        val tempMinecraftDir = File(tempGameDir, ".minecraft")
        val tempGameVersionsDir = File(tempMinecraftDir, "versions")
        val tempClientDir = File(tempGameVersionsDir, info.gameVersion)

        //ModLoader临时目录
        val optifineDir = info.optifine?.let { File(tempGameVersionsDir, it.version) }
        val forgeDir = info.forge?.let { File(tempGameVersionsDir, "forge-${it.versionName}") }
        val neoforgeDir = info.neoforge?.let { File(tempGameVersionsDir, "neoforge-${it.versionName}") }
        val fabricDir = info.fabric?.let { File(tempGameVersionsDir, "fabric-loader-${it.version}-${info.gameVersion}") }
        val legacyFabricDir = info.legacyFabric?.let { File(tempGameVersionsDir, "legacy-fabric-loader-${it.version}-${info.gameVersion}") }
        val quiltDir = info.quilt?.let { File(tempGameVersionsDir, "quilt-loader-${it.version}-${info.gameVersion}") }
        val cleanroomDir = info.cleanroom?.let { File(tempGameVersionsDir, "cleanroom-${it.version}-${info.gameVersion}") }
        val babricDir = info.babric?.let { File(tempGameVersionsDir, "babric-loader-${it.version}-${info.gameVersion}") }

        //Mods临时目录
        val tempModsDir = File(tempGameDir, ".temp_mods")

        return InstallationPathConfig(
            targetClientDir = targetClientDir1,
            tempGameDir = tempGameDir,
            tempMinecraftDir = tempMinecraftDir,
            tempGameVersionsDir = tempGameVersionsDir,
            tempClientDir = tempClientDir,
            tempModsDir = tempModsDir,
            optifineDir = optifineDir,
            forgeDir = forgeDir,
            neoforgeDir = neoforgeDir,
            fabricDir = fabricDir,
            legacyFabricDir = legacyFabricDir,
            quiltDir = quiltDir,
            cleanroomDir = cleanroomDir,
            babricDir = babricDir
        )
    }

    /**
     * 获取安装 Minecraft 游戏的任务流阶段
     * @param onInstalled 游戏已完成安装
     */
    suspend fun getTaskPhase(
        createIsolation: Boolean = true,
        onInstalled: suspend (targetClientDir: File) -> Unit = {},
    ): List<TaskFlowExecutor.TaskPhase> = withContext(Dispatchers.IO) {
        val pathConfig = createPathConfig()

        listOf(
            buildPhase {
                //开始之前，应该先清理一次临时游戏目录，否则可能会影响安装结果
                addTask(
                    id = "Download.Game.ClearTemp",
                    title = androidText(R.string.download_install_clear_temp),
                    icon = R.drawable.ic_auto_delete_outlined,
                ) {
                    clearTempGameDir()
                    //清理完成缓存目录后，创建新的缓存目录
                    pathConfig.tempClientDir.createDirAndLog()
                    pathConfig.optifineDir?.createDirAndLog()
                    pathConfig.forgeDir?.createDirAndLog()
                    pathConfig.neoforgeDir?.createDirAndLog()
                    pathConfig.fabricDir?.createDirAndLog()
                    pathConfig.legacyFabricDir?.createDirAndLog()
                    pathConfig.quiltDir?.createDirAndLog()
                    pathConfig.cleanroomDir?.createDirAndLog()
                    pathConfig.babricDir?.createDirAndLog()
                    pathConfig.tempModsDir.createDirAndLog()
                }

                //下载安装原版
                addTask(
                    title = androidText(R.string.download_game_install_vanilla, info.gameVersion),
                    task = createMinecraftDownloadTask(info.gameVersion, pathConfig.tempGameVersionsDir)
                )

                //下载加载器/模组
                addLoaderTasks(
                    tempGameDir = pathConfig.tempGameDir,
                    tempMinecraftDir = pathConfig.tempMinecraftDir,
                    forgeDir = pathConfig.forgeDir,
                    neoforgeDir = pathConfig.neoforgeDir,
                    fabricDir = pathConfig.fabricDir,
                    legacyFabricDir = pathConfig.legacyFabricDir,
                    quiltDir = pathConfig.quiltDir,
                    cleanroomDir = pathConfig.cleanroomDir,
                    babricDir = pathConfig.babricDir,
                    tempModsDir = pathConfig.tempModsDir
                )

                //最终游戏安装任务
                addTask(
                    title = androidText(R.string.download_game_install_game_files_progress),
                    icon = R.drawable.ic_build_outlined,
                    //如果有非原版以外的任务，则需要进行处理安装（合并版本Json、迁移文件等）
                    task = if (
                        pathConfig.optifineDir != null ||
                        pathConfig.forgeDir != null ||
                        pathConfig.neoforgeDir != null ||
                        pathConfig.fabricDir != null ||
                        pathConfig.legacyFabricDir != null ||
                        pathConfig.quiltDir != null ||
                        pathConfig.cleanroomDir != null ||
                        pathConfig.babricDir != null ||
                        pathConfig.tempModsDir.listFiles()?.isNotEmpty() == true
                    ) {
                        createGameInstalledTask(
                            tempMinecraftDir = pathConfig.tempMinecraftDir,
                            targetMinecraftDir = targetGameFolder,
                            targetClientDir = pathConfig.targetClientDir,
                            tempClientDir = pathConfig.tempClientDir,
                            tempModsDir = pathConfig.tempModsDir,
                            createIsolation = createIsolation,
                            optiFineFolder = pathConfig.optifineDir,
                            forgeFolder = pathConfig.forgeDir,
                            neoForgeFolder = pathConfig.neoforgeDir,
                            fabricFolder = pathConfig.fabricDir,
                            legacyFabricFolder = pathConfig.legacyFabricDir,
                            quiltFolder = pathConfig.quiltDir,
                            cleanroomFolder = pathConfig.cleanroomDir,
                            babricFolder = pathConfig.babricDir,
                            onComplete = {
                                onInstalled(pathConfig.targetClientDir)
                                targetClientDir = null
                            }
                        )
                    } else {
                        //仅仅下载了原版，只复制版本client文件
                        createVanillaFilesCopyTask(
                            tempMinecraftDir = pathConfig.tempMinecraftDir,
                            onComplete = {
                                onInstalled(pathConfig.targetClientDir)
                                targetClientDir = null
                            }
                        )
                    }
                )
            }
        )
    }

    /**
     * 获取修改版本的任务流阶段
     */
    private suspend fun getModifyTaskPhase(): List<TaskFlowExecutor.TaskPhase> = withContext(Dispatchers.IO) {
        val pathConfig = createPathConfig(checkVersionExists = false)
        val tempOutputDir = File(PathManager.DIR_CACHE_GAME_DOWNLOADER, "modified/${info.customVersionName}")

        listOf(
            buildPhase {
                //开始之前，应该先清理一次临时游戏目录，否则可能会影响安装结果
                addTask(
                    id = "ModifyVersion.ClearTemp",
                    title = androidText(R.string.download_install_clear_temp),
                    icon = R.drawable.ic_auto_delete_outlined,
                ) {
                    clearTempGameDir()
                    //清理完成缓存目录后，创建新的缓存目录
                    pathConfig.tempClientDir.createDirAndLog()
                    pathConfig.optifineDir?.createDirAndLog()
                    pathConfig.forgeDir?.createDirAndLog()
                    pathConfig.neoforgeDir?.createDirAndLog()
                    pathConfig.fabricDir?.createDirAndLog()
                    pathConfig.legacyFabricDir?.createDirAndLog()
                    pathConfig.quiltDir?.createDirAndLog()
                    pathConfig.cleanroomDir?.createDirAndLog()
                    pathConfig.babricDir?.createDirAndLog()
                    pathConfig.tempModsDir.createDirAndLog()
                }

                //下载安装 Minecraft 本体：Json/Jar 进入临时目录，libraries/assets 直接进入游戏目录
                //修改版本时也完整执行该任务，确保原版 Jar 干净可用，并补齐缺失的 libraries/assets
                addTask(
                    title = androidText(R.string.download_game_install_vanilla, info.gameVersion),
                    task = createMinecraftDownloadTask(info.gameVersion, pathConfig.tempGameVersionsDir)
                )

                //下载加载器/模组
                addLoaderTasks(
                    tempGameDir = pathConfig.tempGameDir,
                    tempMinecraftDir = pathConfig.tempMinecraftDir,
                    forgeDir = pathConfig.forgeDir,
                    neoforgeDir = pathConfig.neoforgeDir,
                    fabricDir = pathConfig.fabricDir,
                    legacyFabricDir = pathConfig.legacyFabricDir,
                    quiltDir = pathConfig.quiltDir,
                    cleanroomDir = pathConfig.cleanroomDir,
                    babricDir = pathConfig.babricDir,
                    tempModsDir = pathConfig.tempModsDir
                )

                //最终修改任务：合并版本 Json、迁移游戏文件，全部成功后才替换目标版本文件
                addTask(
                    title = androidText(R.string.download_game_install_game_files_progress),
                    icon = R.drawable.ic_build_outlined,
                    task = createVersionModifiedTask(
                        pathConfig = pathConfig,
                        tempOutputDir = tempOutputDir,
                        onComplete = {
                            targetClientDir = null
                        }
                    )
                )
            }
        )
    }

    private fun MutableList<TitledTask>.addLoaderTasks(
        tempGameDir: File,
        tempMinecraftDir: File,
        forgeDir: File?,
        neoforgeDir: File?,
        fabricDir: File?,
        legacyFabricDir: File?,
        quiltDir: File?,
        cleanroomDir: File?,
        babricDir: File?,
        tempModsDir: File
    ) {
        // OptiFine 安装
        info.optifine?.let { optifineVersion ->
            //Fabric 系加载器无法加载 OptiFine，需要搭配 OptiFabric，仅作为 Mod 下载
            val hasFabricLike = fabricDir != null || legacyFabricDir != null || quiltDir != null

            if (!hasFabricLike) {
                val targetInstaller: File = targetTempOptiFineInstaller(tempGameDir)

                //下载安装器
                addTask(
                    title = androidText(
                        R.string.download_game_install_base_download_file,
                        ModLoader.OPTIFINE.displayName,
                        info.optifine.displayName
                    ),
                    task = getOptiFineDownloadTask(
                        targetTempInstaller = targetInstaller,
                        optifine = optifineVersion
                    )
                )

                //安装 OptiFine
                addTask(
                    title = androidText(
                        R.string.download_game_install_base_install,
                        ModLoader.OPTIFINE.displayName
                    ),
                    icon = R.drawable.ic_build_outlined,
                    task = getOptiFineInstallTask(
                        downloader = downloader,
                        tempGameDir = tempGameDir,
                        tempMinecraftDir = tempMinecraftDir,
                        tempInstallerJar = targetInstaller,
                        optifineVersion = optifineVersion,
                        hasBootstrapForge = (forgeDir != null || neoforgeDir != null) && isMinecraft17Plus(info.gameVersion),
                        logOutputHolder = logOutputHolder
                    )
                )
            } else {
                //仅作为Mod进行下载
                addTask(
                    title = androidText(
                        R.string.download_game_install_base_download_file,
                        ModLoader.OPTIFINE.displayName,
                        info.optifine.displayName
                    ),
                    task = getOptiFineModsDownloadTask(
                        optifine = optifineVersion,
                        tempModsDir = tempModsDir
                    )
                )
            }
        }

        // Forge 安装
        info.forge?.let { forgeVersion ->
            createForgeLikeTask(
                forgeLikeVersion = forgeVersion,
                tempGameDir = tempGameDir,
                tempMinecraftDir = tempMinecraftDir,
                tempFolderName = forgeDir!!.name,
                addTask = { title, icon, task ->
                    addTask(title = title, icon = icon, task = task)
                }
            )
        }

        // NeoForge 安装
        info.neoforge?.let { neoforgeVersion ->
            createForgeLikeTask(
                forgeLikeVersion = neoforgeVersion,
                tempGameDir = tempGameDir,
                tempMinecraftDir = tempMinecraftDir,
                tempFolderName = neoforgeDir!!.name,
                addTask = { title, icon, task ->
                    addTask(title = title, icon = icon, task = task)
                }
            )
        }

        fun addFabricLike(
            version: FabricLikeVersion,
            dirName: String
        ) {
            createFabricLikeTask(
                fabricLikeVersion = version,
                tempMinecraftDir = tempMinecraftDir,
                tempFolderName = dirName,
                addTask = { title, icon, task ->
                    addTask(title = title, icon = icon, task = task)
                }
            )
        }

        fun addMod(
            mod: ModVersion,
            modName: String,
            modVer: String,
        ) {
            addTask(
                title = androidText(
                    R.string.download_game_install_base_download_file,
                    modName, modVer
                ),
                task = createModLikeDownloadTask(
                    tempModsDir = tempModsDir,
                    modVersion = mod
                )
            )
        }

        // Fabric 安装
        info.fabric?.let { fabricVersion ->
            addFabricLike(fabricVersion, fabricDir!!.name)
        }
        info.fabricAPI?.let { apiVersion ->
            addMod(
                mod = apiVersion,
                modName = ModLoader.FABRIC_API.displayName,
                modVer = apiVersion.displayName
            )
        }

        // Legacy Fabric 安装
        info.legacyFabric?.let { fabricVersion ->
            addFabricLike(fabricVersion, legacyFabricDir!!.name)
        }
        info.legacyFabricAPI?.let { apiVersion ->
            addMod(
                mod = apiVersion,
                modName = ModLoader.LEGACY_FABRIC_API.displayName,
                modVer = apiVersion.displayName
            )
        }

        // Quilt 安装
        info.quilt?.let { quiltVersion ->
            addFabricLike(quiltVersion, quiltDir!!.name)
        }
        info.quiltAPI?.let { apiVersion ->
            addMod(
                mod = apiVersion,
                modName = ModLoader.QUILT_API.displayName,
                modVer = apiVersion.displayName
            )
        }

        // Cleanroom 安装
        info.cleanroom?.let { cleanroomVersion ->
            createCleanroomTask(
                cleanroomVersion = cleanroomVersion,
                tempGameDir = tempGameDir,
                tempMinecraftDir = tempMinecraftDir,
                tempFolderName = cleanroomDir!!.name,
                addTask = { title, icon, task ->
                    addTask(title = title, icon = icon, task = task)
                }
            )
        }

        // Babric 安装
        info.babric?.let { babricVersion ->
            addFabricLike(babricVersion, babricDir!!.name)
        }
    }

    fun cancelInstall(
        clearTarget: Boolean = true
    ) {
        taskExecutor.cancel()

        if (clearTarget) {
            clearTargetClient()
        }

        CoroutineScope(Dispatchers.Main).launch {
            //停止Jvm服务
            val intent = Intent(GlobalContext.applicationContext, JvmService::class.java)
            GlobalContext.applicationContext.stopService(intent)
            JVMSocketServer.stop()
        }
    }

    /**
     * 清除临时游戏目录
     */
    private suspend fun clearTempGameDir() = withContext(Dispatchers.IO) {
        PathManager.DIR_CACHE_GAME_DOWNLOADER.takeIf { it.exists() }?.let { folder ->
            FileUtils.deleteQuietly(folder)
            Logger.info(TAG, "Temporary game directory cleared.")
        }
    }

    /**
     * 安装失败、取消安装时，都应该清除目标客户端版本文件夹
     */
    private fun clearTargetClient() {
        val dirToDelete = targetClientDir //临时变量
        targetClientDir = null

        CoroutineScope(Dispatchers.IO).launch {
//            clearTempGameDir() 考虑到用户可能操作快，双线程清理同一个文件夹可能导致一些问题
            dirToDelete?.let {
                //直接清除上一次安装的目标目录
                FileUtils.deleteQuietly(it)
                Logger.info(TAG, "Successfully deleted version directory: ${it.name} at path: ${it.absolutePath}")
            }
        }
    }

    /**
     * 获取下载原版 Task
     */
    private fun createMinecraftDownloadTask(
        tempClientName: String,
        tempVersionsDir: File
    ): Task {
        val mcDownloader = MinecraftDownloader(
            context = context,
            version = info.gameVersion,
            customName = info.customVersionName,
            downloader = downloader,
            onThrowable = { throw it }
        )

        return mcDownloader.getDownloadTask(tempClientName, tempVersionsDir)
    }

    /**
     * @param tempFolderName 临时ModLoader版本文件夹名称
     */
    private fun createForgeLikeTask(
        forgeLikeVersion: ForgeLikeVersion,
        loaderVersion: String = forgeLikeVersion.versionName,
        tempGameDir: File,
        tempMinecraftDir: File,
        tempFolderName: String,
        addTask: (title: AndroidStringText, icon: Int?, task: Task) -> Unit
    ) {
        //类似 1.19.3-41.2.8 格式，优先使用 Version 中要求的版本而非 Inherit（例如 1.19.3 却使用了 1.19 的 Forge）
        val (processedInherit, processedLoaderVersion) =
            if (
                !forgeLikeVersion.isNeoForge && loaderVersion.startsWith("1.") && loaderVersion.contains("-")
            ) {
                loaderVersion.substringBefore("-") to loaderVersion.substringAfter("-")
            } else {
                forgeLikeVersion.inherit to loaderVersion
            }

        val tempInstaller = targetTempForgeLikeInstaller(tempGameDir)
        //下载安装器
        addTask(
            androidText(
                R.string.download_game_install_base_download_file,
                forgeLikeVersion.loaderName,
                processedLoaderVersion
            ),
            null,
            getForgeLikeDownloadTask(tempInstaller, forgeLikeVersion)
        )
        //分析与安装
        val isNew = forgeLikeVersion is NeoForgeVersion || !forgeLikeVersion.isLegacy

        if (isNew) {
            addTask(
                androidText(
                    R.string.download_game_install_forgelike_analyse,
                    forgeLikeVersion.loaderName
                ),
                R.drawable.ic_build_outlined,
                getForgeLikeAnalyseTask(
                    downloader = downloader,
                    targetTempInstaller = tempInstaller,
                    removeFromDownload = "${forgeLikeVersion.loaderName.lowercase()}-$processedInherit-$loaderVersion",
                    tempMinecraftFolder = tempMinecraftDir,
                    sourceInherit = info.gameVersion,
                    processedInherit = processedInherit,
                )
            )
        }

        addTask(
            androidText(
                R.string.download_game_install_base_install,
                forgeLikeVersion.loaderName
            ),
            R.drawable.ic_build_outlined,
            getForgeLikeInstallTask(
                isNew = isNew,
                downloader = downloader,
                loaderName = forgeLikeVersion.loaderName,
                tempFolderName = tempFolderName,
                tempInstaller = tempInstaller,
                tempGameFolder = tempGameDir,
                tempMinecraftDir = tempMinecraftDir,
                inherit = processedInherit,
                logOutputHolder = logOutputHolder
            )
        )
    }

    private fun createFabricLikeTask(
        fabricLikeVersion: FabricLikeVersion,
        tempMinecraftDir: File,
        tempFolderName: String,
        addTask: (title: AndroidStringText, icon: Int?, task: Task) -> Unit
    ) {
        val tempVersionJson = File(tempMinecraftDir, "versions/$tempFolderName/$tempFolderName.json")

        //下载 Json
        addTask(
            androidText(
                R.string.download_game_install_base_download_file,
                fabricLikeVersion.loaderName,
                fabricLikeVersion.version
            ),
            null,
            getFabricLikeDownloadTask(
                fabricLikeVersion = fabricLikeVersion,
                tempVersionJson = tempVersionJson
            )
        )

        //补全游戏库
        addTask(
            androidText(
                R.string.download_game_install_forgelike_analyse,
                fabricLikeVersion.loaderName
            ),
            null,
            getFabricLikeCompleterTask(
                downloader = downloader,
                tempMinecraftDir = tempMinecraftDir,
                tempVersionJson = tempVersionJson
            )
        )
    }

    private fun createCleanroomTask(
        cleanroomVersion: CleanroomVersion,
        tempGameDir: File,
        tempMinecraftDir: File,
        tempFolderName: String,
        addTask: (title: AndroidStringText, icon: Int?, task: Task) -> Unit
    ) {
        val tempInstaller = targetTempCleanroomInstaller(tempGameDir)
        //下载安装器
        addTask(
            androidText(
                R.string.download_game_install_base_download_file,
                ModLoader.CLEANROOM.displayName,
                cleanroomVersion.version
            ),
            null,
            getCleanroomDownloadTask(tempInstaller, cleanroomVersion)
        )

        //以新Forge安装器的方式进行安装
        addTask(
            androidText(
                R.string.download_game_install_forgelike_analyse,
                cleanroomVersion.version
            ),
            R.drawable.ic_build_outlined,
            getForgeLikeAnalyseTask(
                downloader = downloader,
                targetTempInstaller = tempInstaller,
                removeFromDownload = "cleanroom-${cleanroomVersion.version}",
                tempMinecraftFolder = tempMinecraftDir,
                sourceInherit = info.gameVersion,
                processedInherit = "1.12.2"
            )
        )

        addTask(
            androidText(
                R.string.download_game_install_base_install,
                cleanroomVersion.version
            ),
            R.drawable.ic_build_outlined,
            getForgeLikeInstallTask(
                isNew = true,
                downloader = downloader,
                loaderName = cleanroomVersion.version,
                tempFolderName = tempFolderName,
                tempInstaller = tempInstaller,
                tempGameFolder = tempGameDir,
                tempMinecraftDir = tempMinecraftDir,
                inherit = "1.12.2",
                logOutputHolder = logOutputHolder
            )
        )
    }

    private fun createModLikeDownloadTask(
        tempModsDir: File,
        modVersion: ModVersion
    ) = Task.runTask(
        id = "Download.Mods",
        task = { task ->
            withSpeedReport(
                onSpeedReport = { bytes ->
                    task.updateSpeed(bytes)
                },
                onClear = {
                    task.clearSpeed()
                }
            ) { report ->
                downloadFileFromSources(
                    urls = modVersion.file.url.mapMCIMMirrorUrls(),
                    sha1 = modVersion.file.hashes.sha1,
                    outputFile = File(tempModsDir, modVersion.file.fileName),
                    sizeCallback = report
                )
            }
        }
    )

    /**
     * 游戏带附加内容安装完成，合并版本Json、迁移游戏文件
     * @param createIsolation 是否新创建启用版本隔离的版本配置
     */
    private fun createGameInstalledTask(
        tempMinecraftDir: File,
        targetMinecraftDir: File,
        targetClientDir: File,
        tempClientDir: File,
        tempModsDir: File,
        createIsolation: Boolean = true,
        optiFineFolder: File? = null,
        forgeFolder: File? = null,
        neoForgeFolder: File? = null,
        fabricFolder: File? = null,
        legacyFabricFolder: File? = null,
        quiltFolder: File? = null,
        cleanroomFolder: File? = null,
        babricFolder: File? = null,
        onComplete: suspend () -> Unit = {}
    ) = Task.runTask(
        id = GAME_JSON_MERGER_ID,
        dispatcher = Dispatchers.IO,
        task = { task ->
            //合并版本 Json
            task.updateProgress(0.1f)
            mergeGameJson(
                info = info,
                outputFolder = targetClientDir,
                clientFolder = tempClientDir,
                optiFineFolder = optiFineFolder,
                forgeFolder = forgeFolder,
                neoForgeFolder = neoForgeFolder,
                fabricFolder = fabricFolder,
                legacyFabricFolder = legacyFabricFolder,
                quiltFolder = quiltFolder,
                cleanroomFolder = cleanroomFolder,
                babricFolder = babricFolder
            )

            //迁移游戏文件
            copyDirectoryContents(
                File(tempMinecraftDir, "libraries"),
                File(targetMinecraftDir, "libraries"),
                onProgress = { percentage ->
                    task.updateProgress(percentage)
                }
            )

            //复制客户端文件
            copyVanillaFiles(
                sourceGameFolder = tempMinecraftDir,
                sourceVersion = info.gameVersion,
                destinationGameFolder = targetGameFolder,
                targetVersion = info.customVersionName
            )

            //复制Mods
            tempModsDir.listFiles()?.let {
                val targetModsDir = VersionFolders.MOD.getDir(targetClientDir)
                it.forEach { modFile ->
                    val targetMod = File(targetModsDir, modFile.name)
                    if (!targetMod.exists()) {
                        //如果已经安装了，那就不覆盖
                        modFile.copyTo(targetMod)
                    }
                }
                if (createIsolation) {
                    //开启版本隔离
                    VersionConfig.createIsolation(targetClientDir).save()
                }
            }

            //清除临时游戏目录
            task.updateProgress(-1f)
            task.updateMessage(androidText(R.string.download_install_clear_temp))
            clearTempGameDir()

            onComplete()
        }
    )

    /**
     * 版本修改的最终任务：合并版本 Json、迁移游戏文件，全部成功后才替换目标版本的 Json/Jar
     * @param tempOutputDir 合并结果的临时输出目录
     */
    private fun createVersionModifiedTask(
        pathConfig: InstallationPathConfig,
        tempOutputDir: File,
        onComplete: suspend () -> Unit = {}
    ) = Task.runTask(
        id = GAME_JSON_MERGER_ID,
        dispatcher = Dispatchers.IO,
        task = { task ->
            val targetClientDir = pathConfig.targetClientDir

            //合并版本 Json 到临时目录，避免直接破坏目标版本
            task.updateProgress(0.1f)
            mergeGameJson(
                info = info,
                outputFolder = tempOutputDir,
                clientFolder = pathConfig.tempClientDir,
                optiFineFolder = pathConfig.optifineDir,
                forgeFolder = pathConfig.forgeDir,
                neoForgeFolder = pathConfig.neoforgeDir,
                fabricFolder = pathConfig.fabricDir,
                legacyFabricFolder = pathConfig.legacyFabricDir,
                quiltFolder = pathConfig.quiltDir,
                cleanroomFolder = pathConfig.cleanroomDir
            )

            //迁移游戏库
            copyDirectoryContents(
                File(pathConfig.tempMinecraftDir, "libraries"),
                File(targetGameFolder, "libraries"),
                onProgress = { percentage ->
                    task.updateProgress(percentage)
                }
            )

            //复制新增的Mods（不覆盖已存在的文件）
            pathConfig.tempModsDir.listFiles()?.forEach { modFile ->
                val targetModsDir = VersionFolders.MOD.getDir(targetClientDir)
                val targetMod = File(targetModsDir, modFile.name)
                if (!targetMod.exists()) {
                    modFile.copyTo(targetMod)
                }
            }

            //替换目标版本的 Json/Jar
            task.updateProgress(-1f)
            swapVersionFiles(targetClientDir, tempOutputDir)

            //清除临时游戏目录
            task.updateMessage(androidText(R.string.download_install_clear_temp))
            clearTempGameDir()

            onComplete()
        }
    )

    /**
     * 将修改后的版本 Json/Jar 替换进目标版本目录
     * 替换前会备份原文件，替换失败时自动还原，保证目标版本不被破坏
     */
    private fun swapVersionFiles(targetClientDir: File, tempOutputDir: File) {
        val targetJson = File(targetClientDir, "${info.customVersionName}.json")
        val targetJar = File(targetClientDir, "${info.customVersionName}.jar")
        val newJson = File(tempOutputDir, "${info.customVersionName}.json")
        val newJar = File(tempOutputDir, "${info.customVersionName}.jar")
        val backupJson = File(PathManager.DIR_CACHE, "modify_${info.customVersionName}_json")
        val backupJar = File(PathManager.DIR_CACHE, "modify_${info.customVersionName}_jar")

        //备份原 Json/Jar，替换失败时用于还原
        runCatching {
            targetJson.takeIf { it.exists() }?.let {
                backupJson.delete()
                it.copyTo(backupJson)
            }
            targetJar.takeIf { it.exists() }?.let {
                backupJar.delete()
                it.copyTo(backupJar)
            }
        }.onFailure {
            FileUtils.deleteQuietly(backupJson)
            FileUtils.deleteQuietly(backupJar)
            throw it
        }

        try {
            FileUtils.deleteQuietly(targetJson)
            FileUtils.deleteQuietly(targetJar)
            FileUtils.moveFile(newJson, targetJson)
            FileUtils.moveFile(newJar, targetJar)
        } catch (e: Exception) {
            //替换失败，还原原版本文件
            runCatching {
                if (backupJson.exists()) {
                    FileUtils.deleteQuietly(targetJson)
                    FileUtils.moveFile(backupJson, targetJson)
                }
                if (backupJar.exists()) {
                    FileUtils.deleteQuietly(targetJar)
                    FileUtils.moveFile(backupJar, targetJar)
                }
            }.onFailure { restoreError ->
                Logger.error(TAG, "Failed to revert version files: ${restoreError.message}", restoreError)
            }
            throw e
        }

        //替换成功，清理备份
        FileUtils.deleteQuietly(backupJson)
        FileUtils.deleteQuietly(backupJar)
    }

    /**
     * 仅原本客户端文件复制任务 json、jar
     */
    private fun createVanillaFilesCopyTask(
        tempMinecraftDir: File,
        onComplete: suspend () -> Unit = {}
    ): Task {
        return Task.runTask(
            id = "VanillaFilesCopy",
            task = { task ->
                //复制客户端文件
                copyVanillaFiles(
                    sourceGameFolder = tempMinecraftDir,
                    sourceVersion = info.gameVersion,
                    destinationGameFolder = targetGameFolder,
                    targetVersion = info.customVersionName
                )

                //清除临时游戏目录
                task.updateProgress(-1f)
                task.updateMessage(androidText(R.string.download_install_clear_temp))
                clearTempGameDir()

                onComplete()
            }
        )
    }

    /**
     * 创建一个用于 TaskSystem 的代理任务，镜像当前安装进度
     * 用于最小化安装对话框，同时让安装在后台继续运行
     */
    fun createBackgroundTask(onCancelRequest: () -> Unit): Task {
        return Task.runTask(
            id = "game_install_${info.gameVersion}_${info.customVersionName}",
            task = { proxyTask ->
                val mirrorJob = launch {
                    while (true) {
                        kotlinx.coroutines.delay(150)
                        val titledTasks = tasksFlow.value
                        val running = titledTasks.firstOrNull { it.task.stage.value == TaskStage.RUNNING }
                            ?: titledTasks.lastOrNull()
                        running?.task?.let { t ->
                            proxyTask.updateProgress(t.progress.value)
                            t.message.value?.let { proxyTask.updateMessage(it) }
                            t.rateBytesPerSec.value?.let { proxyTask.updateSpeed(it) } ?: proxyTask.clearSpeed()
                        }
                    }
                }
                try {
                    taskExecutor.awaitCompletion()
                } catch (ce: kotlinx.coroutines.CancellationException) {
                    throw ce
                } catch (_: Exception) {
                } finally {
                    mirrorJob.cancel()
                }
            },
            onCancel = onCancelRequest
        )
    }

    private fun File.createDirAndLog(): File {
        this.mkdirs()
        Logger.debug(TAG, "Created directory: $this")
        return this
    }

}