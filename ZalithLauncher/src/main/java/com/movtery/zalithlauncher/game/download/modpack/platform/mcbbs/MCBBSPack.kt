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

package com.movtery.zalithlauncher.game.download.modpack.platform.mcbbs

import android.content.Context
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskFlowExecutor
import com.movtery.zalithlauncher.coroutine.TaskLogOutput
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.coroutine.addTask
import com.movtery.zalithlauncher.coroutine.buildPhase
import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo
import com.movtery.zalithlauncher.game.download.game.GameInstaller
import com.movtery.zalithlauncher.game.download.modpack.install.retrieveLoader
import com.movtery.zalithlauncher.game.download.modpack.platform.AbstractPack
import com.movtery.zalithlauncher.game.download.modpack.platform.PackPlatform
import com.movtery.zalithlauncher.game.version.installed.VersionConfig
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.file.copyDirectoryContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

class MCBBSPack(
    private val root: File,
    private val manifest: MCBBSManifest
): AbstractPack(platform = PackPlatform.MCBBS) {

    /**
     * 用户指定的预安装版本名称
     */
    private lateinit var targetVersionName: String

    /**
     * 即将下载的游戏版本的信息
     */
    private lateinit var gameDownloadInfo: GameDownloadInfo

    override fun getFinalClientName(): String {
        return targetVersionName
    }

    override fun buildTaskPhases(
        context: Context,
        scope: CoroutineScope,
        versionFolder: File,
        waitForVersionName: suspend (name: String) -> String,
        addPhases: (List<TaskFlowExecutor.TaskPhase>) -> Unit,
        onClearTemp: suspend () -> Unit,
        logOutputHolder: MutableStateFlow<TaskLogOutput?>
    ): List<TaskFlowExecutor.TaskPhase> {
        return listOf(
            buildPhase {
                //等待用户输入预安装版本名称
                addTask(
                    id = "ImportModpack.WaitUserForVersionName",
                    title = androidText(R.string.download_install_input_version_name),
                    icon = R.drawable.ic_edit_outlined
                ) { task ->
                    task.updateProgress(-1f)
                    targetVersionName = waitForVersionName(manifest.name)
                }

                //提取整合包文件
                addTask(
                    id = "ImportModpack.ExtractFiles",
                    title = androidText(R.string.import_modpack_task_extract_files),
                    icon = R.drawable.ic_build_outlined
                ) { task ->
                    task.updateProgress(-1f)
                    copyDirectoryContents(
                        from = File(root, "overrides"),
                        to = versionFolder,
                        onProgress = { progress ->
                            task.updateProgress(progress)
                        }
                    )
                }

                //分析并匹配模组加载器信息，并构造出游戏安装信息
                addTask(
                    id = "ImportModpack.RetrieveLoader",
                    title = androidText(R.string.download_modpack_get_loaders),
                    icon = R.drawable.ic_build_outlined
                ) {
                    val gameVersion = manifest.getMinecraftVersion()!!

                    //构建游戏安装信息
                    gameDownloadInfo = GameDownloadInfo(
                        gameVersion = gameVersion,
                        customVersionName = targetVersionName
                    )

                    //构建模组加载器安装信息
                    manifest.addons.forEach { addon ->
                        with(manifest) { addon.retrieveLoader() }?.let { pair ->
                            pair.retrieveLoader(
                                gameVersion = gameVersion,
                                gameInfo = gameDownloadInfo,
                                pasteGameInfo = { info ->
                                    gameDownloadInfo = info
                                }
                            )
                        }
                    }

                    //开始安装游戏！切换到下一阶段！
                    val gameInstaller = GameInstaller(
                        context = context,
                        info = gameDownloadInfo,
                        scope = scope,
                        logOutputHolder = logOutputHolder
                    )
                    addPhases(
                        gameInstaller.getTaskPhase(
                            createIsolation = false,
                            onInstalled = { targetClientDir ->
                                //已经完成游戏安装，开始最终任务
                                //整合包临时文件安装任务
                                val finalTask = TitledTask(
                                    title = androidText(R.string.download_modpack_final_move),
                                    runningIcon = R.drawable.ic_build_outlined,
                                    task = createFinalInstallTask(
                                        targetClientDir = targetClientDir,
                                        tempVersionsDir = versionFolder,
                                        onClearTemp = onClearTemp
                                    )
                                )
                                //切换到安装阶段
                                addPhases(
                                    listOf(
                                        buildPhase { add(finalTask) }
                                    )
                                )
                            }
                        )
                    )
                }
            }
        )
    }

    /**
     * 创建最终安装任务
     */
    private fun createFinalInstallTask(
        targetClientDir: File,
        tempVersionsDir: File,
        onClearTemp: suspend () -> Unit
    ) = Task.runTask(
        id = "ImportModpack.FinalInstall",
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

            //创建版本信息
            VersionConfig.createIsolation(targetClientDir).apply {
                //游戏参数
                manifest.launchInfo.launchArguments?.joinToString(" ")?.let { arg ->
                    this.gameArgs = arg
                }
                //Jvm 参数
                manifest.launchInfo.javaArguments?.joinToString(" ")?.let { arg ->
                    this.jvmArgs = arg
                }
                //内存配置
                this.ramAllocation = manifest.launchInfo.minMemory
                this.versionSummary = manifest.description
            }.save()

            //清理临时整合包目录
            task.updateProgress(-1f)
            task.updateMessage(androidText(R.string.download_install_clear_temp))
            onClearTemp()
        }
    )
}