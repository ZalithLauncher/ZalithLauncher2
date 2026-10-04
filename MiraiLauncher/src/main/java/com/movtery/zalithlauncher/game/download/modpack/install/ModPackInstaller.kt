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

package com.movtery.zalithlauncher.game.download.modpack.install

import android.content.Context
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskFlowExecutor
import com.movtery.zalithlauncher.coroutine.TaskLogOutput
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.coroutine.addTask
import com.movtery.zalithlauncher.coroutine.buildPhase
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.assets.platform.mcim.mapMCIMMirrorUrls
import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo
import com.movtery.zalithlauncher.game.download.game.GameInstaller
import com.movtery.zalithlauncher.game.version.installed.VersionConfig
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.installed.getVersionIconFile
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.file.copyDirectoryContents
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.downloadFile
import com.movtery.zalithlauncher.utils.network.downloadFileFromSources
import com.movtery.zalithlauncher.utils.network.isUsingMobileData
import com.movtery.zalithlauncher.utils.network.withSpeedReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

private const val TAG = "ModPackInstaller"

/**
 * 在线下载的整合包安装器，仅支持 CurseForge、Modrinth
 * @param version 选中的整合包的版本信息
 * @param iconUrl 整合包的图标链接
 * @param scope 在有生命周期管理的scope中执行安装任务
 * @param waitForVersionName 等待用户输入版本名称
 * @param waitForConfirmMobileData 等待用户确认使用移动网络
 */
class ModPackInstaller(
    private val context: Context,
    private val version: PlatformVersion,
    private val iconUrl: String?,
    private val scope: CoroutineScope,
    private val waitForVersionName: suspend (ModPackInfo) -> String,
    private val waitForConfirmMobileData: suspend () -> Boolean,
) {
    private val taskExecutor = TaskFlowExecutor(scope)
    val tasksFlow: StateFlow<List<TitledTask>> = taskExecutor.tasksFlow

    private val _logOutput = MutableStateFlow<TaskLogOutput?>(null)
    /** 安装 JVM 实时日志输出 */
    val logOutput: StateFlow<TaskLogOutput?> = _logOutput.asStateFlow()

    /**
     * 整合包文件解析出的信息
     */
    private lateinit var modpackInfo: ModPackInfo

    /**
     * 用户指定的预安装版本名称
     */
    private lateinit var targetVersionName: String

    /**
     * 即将下载的游戏版本的信息
     */
    private lateinit var gameDownloadInfo: GameDownloadInfo

    /**
     * 开始安装整合包
     * @param isRunning 正在运行中，拒绝这次安装时
     * @param onInstalled 完成安装时
     * @param onCancelled 内部取消时
     * @param onError 安装时遇到异常
     */
    fun installModPack(
        isRunning: () -> Unit = {},
        onInstalled: (version: String) -> Unit,
        onCancelled: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (taskExecutor.isRunning()) {
            //正在安装中，阻止这次安装请求
            isRunning()
            return
        }

        taskExecutor.executePhasesAsync(
            onStart = {
                val tasks = getTaskPhase()
                taskExecutor.addPhases(tasks)
            },
            onComplete = {
                onInstalled(targetVersionName)
            },
            onError = { e ->
                if (e is UsingMobileDataException) {
                    //用户不希望使用移动网络
                    onCancelled()
                    return@executePhasesAsync
                }
                onError(e)
            }
        )
    }

    private suspend fun getTaskPhase() = withContext(Dispatchers.IO) {
        //临时游戏环境目录
        val tempModPackDir = PathManager.DIR_CACHE_MODPACK_DOWNLOADER
        val tempVersionsDir = File(tempModPackDir, "fkVersion")
        //整合包安装包文件
        val installerFile = File(tempModPackDir, "installer.zip")
        //icon临时文件
        val tempIconFile = File(tempModPackDir, "icon.png")

        listOf(
            buildPhase {
                //清除上一次安装的缓存（如果有的话，可能会影响这次的安装结果）
                addTask(
                    id = "Download.ModPack.ClearTemp",
                    title = androidText(R.string.download_install_clear_temp),
                    icon = R.drawable.ic_auto_delete_outlined
                ) { _ ->
                    clearTempModPackDir()
                    //清理完成缓存目录后，创建新的缓存目录
                    tempModPackDir.createDirAndLog()
                    tempVersionsDir.createDirAndLog()
                    VersionFolders.MOD.getDir(tempVersionsDir).createDirAndLog() //创建临时模组目录

                    //在这个阶段开始检查是否使用移动网络
                    if (isUsingMobileData(context)) {
                        val use = waitForConfirmMobileData()
                        if (!use) {
                            //用户不决定使用移动网络安装，取消导入
                            throw UsingMobileDataException()
                        }
                    }
                }

                //下载整合包安装包
                addTask(
                    id = "Download.ModPack.Installer",
                    title = androidText(R.string.download_game_install_base_download_file2, version.platformDisplayName())
                ) { task ->
                    val totalFileSize = version.platformFileSize().toDouble()
                    var downloadedSize = 0L
                    fun updateProgress() {
                        task.updateProgress((downloadedSize.toDouble() / totalFileSize).toFloat())
                    }
                    withSpeedReport(
                        onSpeedReport = { bytes ->
                            task.updateSpeed(bytes)
                        },
                        onClear = {
                            task.clearSpeed()
                        }
                    ) { report ->
                        downloadFileFromSources(
                            urls = version
                                .platformDownloadUrl()
                                .mapMCIMMirrorUrls(),
                            sha1 = version.platformSha1(),
                            outputFile = installerFile,
                            sizeCallback = { size ->
                                downloadedSize += size
                                updateProgress()
                                report(size)
                            }
                        )
                    }
                    //下载icon图片
                    task.updateProgress(-1f)
                    task.updateMessage(null)
                    iconUrl?.let { iconUrl ->
                        downloadFile(
                            url = iconUrl,
                            outputFile = tempIconFile
                        )
                    }
                }

                //解析整合包、解压整合包
                addTask(
                    id = "Parse.ModPack",
                    title = androidText(R.string.download_modpack_install_parse),
                    icon = R.drawable.ic_build_outlined
                ) { task ->
                    modpackInfo = parserModPack(
                        file = installerFile,
                        platform = version.platform(),
                        targetFolder = tempVersionsDir,
                        task = task
                    )
                }

                //等待用户输入预安装版本名称
                addTask(
                    id = "Download.ModPack.WaitUserForVersionName",
                    title = androidText(R.string.download_install_input_version_name),
                    icon = R.drawable.ic_edit_outlined
                ) { task ->
                    task.updateProgress(-1f)
                    targetVersionName = waitForVersionName(modpackInfo)
                }

                //下载整合包模组文件
                addTask(
                    id = "Download.ModPack.Mods",
                    dispatcher = Dispatchers.IO,
                    title = androidText(R.string.download_modpack_download)
                ) { task ->
                    val downloadTask = ModDownloader(modpackInfo.files)
                    downloadTask.startDownload(task)
                }

                //分析并匹配模组加载器信息，并构造出游戏安装信息
                addTask(
                    id = "ModPack.Retrieve.Loader",
                    title = androidText(R.string.download_modpack_get_loaders),
                    icon = R.drawable.ic_build_outlined
                ) { _ ->
                    //构建游戏安装信息
                    gameDownloadInfo = modpackInfo.retrieveLoaderTask(
                        targetVersionName = targetVersionName
                    )

                    //开始安装游戏！切换到下一阶段！
                    val gameInstaller = GameInstaller(
                        context = context,
                        info = gameDownloadInfo,
                        scope = scope,
                        logOutputHolder = _logOutput
                    )
                    taskExecutor.addPhases(
                        phases = gameInstaller.getTaskPhase(
                            createIsolation = false,
                            onInstalled = { targetClientDir ->
                                //已经完成游戏安装，开始最终任务
                                //整合包临时文件安装任务
                                val finalTask = TitledTask(
                                    title = androidText(R.string.download_modpack_final_move),
                                    runningIcon = R.drawable.ic_build_outlined,
                                    task = createFinalInstallTask(
                                        targetClientDir = targetClientDir,
                                        tempVersionsDir = tempVersionsDir,
                                        tempIconFile = tempIconFile
                                    )
                                )
                                //切换到安装阶段
                                taskExecutor.addPhase(
                                    buildPhase { add(finalTask) }
                                )
                            }
                        )
                    )
                }
            }
        )
    }

    /**
     * 取消安装
     */
    fun cancelInstall() {
        taskExecutor.cancel()
    }

    /**
     * 清理临时整合包版本目录
     */
    private suspend fun clearTempModPackDir() = withContext(Dispatchers.IO) {
        PathManager.DIR_CACHE_MODPACK_DOWNLOADER.takeIf { it.exists() }?.let { folder ->
            FileUtils.deleteQuietly(folder)
            Logger.info(TAG, "Temporary modpack directory cleared.")
        }
    }

    /**
     * 创建最终安装任务
     */
    private fun createFinalInstallTask(
        targetClientDir: File,
        tempVersionsDir: File,
        tempIconFile: File
    ) = Task.runTask(
        id = "ModPack.Final.Install",
        dispatcher = Dispatchers.IO,
        task = { task ->
            task.updateProgress(-1f)
            //复制文件
            copyDirectoryContents(
                tempVersionsDir,
                targetClientDir
            ) { percentage ->
                task.updateProgress(percentage = percentage)
            }

            //复制整合包icon
            if (tempIconFile.exists() && tempIconFile.isFile) {
                val iconFile = getVersionIconFile(targetClientDir)
                if (iconFile.exists()) FileUtils.deleteQuietly(iconFile)
                tempIconFile.copyTo(iconFile)
            }

            //创建版本信息
            VersionConfig.createIsolation(targetClientDir).apply {
                this.versionSummary = modpackInfo.summary ?: "" //整合包描述
                this.ramAllocation = modpackInfo.ram ?: -1
            }.save()

            //清理临时整合包目录
            task.updateProgress(-1f)
            task.updateMessage(androidText(R.string.download_install_clear_temp))
            clearTempModPackDir()
        }
    )

    private fun File.createDirAndLog(): File {
        this.mkdirs()
        Logger.debug(TAG, "Created directory: $this")
        return this
    }
}