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

package com.movtery.zalithlauncher.game.version.installed.cleanup

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskFlowExecutor
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.coroutine.addTask
import com.movtery.zalithlauncher.coroutine.buildPhase
import com.movtery.zalithlauncher.game.path.getAssetsHome
import com.movtery.zalithlauncher.game.version.download.BaseMinecraftDownloader
import com.movtery.zalithlauncher.game.version.installed.VersionInfoParser
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.file.collectFiles
import com.movtery.zalithlauncher.utils.file.findRedundantFiles
import com.movtery.zalithlauncher.utils.file.formatFileSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

class GameAssetCleaner(
    scope: CoroutineScope
) {
    private val taskExecutor = TaskFlowExecutor(scope)
    val tasksFlow: StateFlow<List<TitledTask>> = taskExecutor.tasksFlow

    /**
     * 基础下载器
     */
    private val downloader = BaseMinecraftDownloader()

    /**
     * 已安装的全部的文件
     */
    private val allFiles = mutableListOf<File>()

    /**
     * 所有游戏所需的文件
     */
    private val allGameFiles = mutableListOf<File>()

    /**
     * 所有冗余文件
     */
    private lateinit var allRedundantFiles: List<File>

    /**
     * 已被清理的文件数量
     */
    private var cleanedFileCount = 0

    /**
     * 已清理的文件总大小
     */
    private var cleanedSize: Long = 0L

    /**
     * 清理失败的文件
     */
    private val failedFiles = mutableListOf<File>()

    /**
     * 开始清理
     * @param isRunning 正在运行中，阻止此次清理任务时
     * @param onEnd 清理结束时
     * @param onThrowable 清理过程中遇到错误时
     */
    fun start(
        isRunning: () -> Unit = {},
        onEnd: (count: Int, size: String) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) {
        if (taskExecutor.isRunning()) {
            //正在清理中，阻止这次清理请求
            isRunning()
            return
        }

        taskExecutor.executePhasesAsync(
            onStart = {
                val tasks = getTaskPhases()
                taskExecutor.addPhases(tasks)
            },
            onComplete = {
                onEnd(cleanedFileCount, formatFileSize(cleanedSize))
            },
            onError = onThrowable
        )
    }

    private suspend fun getTaskPhases() = withContext(Dispatchers.IO) {
        //不再清理依赖库，文件并不会太大，也有可能导致其他问题：#617
//        val libraryFolder = File(getLibrariesHome())
        val assetsFolder = File(getAssetsHome())

        allFiles.clear()
        allGameFiles.clear()
        failedFiles.clear()
        cleanedFileCount = 0
        cleanedSize = 0L

        listOf(
            buildPhase {
                //获取全部文件
                addTask(
                    id = "GameAssetCleaner.CollectFiles",
                    title = androidText(R.string.versions_manage_cleanup_collect_files),
                    icon = R.drawable.ic_article_outlined,
                ) { task ->
                    task.updateProgress(-1f)

//                    collectFiles(libraryFolder) { allFiles.add(it.alsoProgress(task)) }
                    collectFiles(assetsFolder) { allFiles.add(it.alsoProgress(task)) }
                }

                //收集所有版本所需的游戏文件
                addTask(
                    id = "GameAssetCleaner.CollectGameFiles",
                    title = androidText(R.string.versions_manage_cleanup_collect_game_files),
                    icon = R.drawable.ic_article_outlined
                ) { task ->
                    task.updateProgress(-1f)

                    val allVersions = VersionsManager.versions.value
                    allVersions.forEach { version ->
                        ensureActive()

                        task.updateMessage(androidText(
                            R.string.versions_manage_cleanup_progress_next_version, version.getVersionName()
                        ))

                        //已启动游戏时所需的依赖为准
                        val gameManifest = VersionInfoParser(version)
                            .setInheriting(skipIfNotExists = true)
                            .build()

                        val index = downloader.createAssetIndex(downloader.assetIndexTarget, gameManifest)

                        fun addGameFile(file: File) {
                            if (allGameFiles.addIfNotContains(file)) {
                                file.alsoProgress(task)
                            } else {
                                task.updateMessage(androidText(R.string.versions_manage_cleanup_progress_collected))
                            }
                        }

//                        downloader.loadLibraryDownloads(gameManifest) { _, _, targetFile, _, _ ->
//                            addGameFile(targetFile)
//                        }
                        downloader.loadAssetsDownload(index) { _, _, targetFile, _ ->
                            addGameFile(targetFile)
                        }
                    }
                }

                //对比出无用的文件
                addTask(
                    id = "GameAssetCleaner.CompareFiles",
                    title = androidText(R.string.versions_manage_cleanup_compare_files),
                    icon = R.drawable.ic_build_outlined
                ) { task ->
                    task.updateProgress(-1f)

                    allRedundantFiles = findRedundantFiles(
                        sourceFiles = allFiles,
                        targetFiles = allGameFiles,
                    ).filter { it.exists() }
                }

                //清理文件
                addTask(
                    id = "GameAssetsCleaner.Cleanup",
                    title = androidText(R.string.versions_manage_cleanup_cleanup),
                    icon = R.drawable.ic_auto_delete_outlined,
                    dispatcher = Dispatchers.IO
                ) { task ->
                    task.updateProgress(-1f)

                    val totalSize = allRedundantFiles.size
                    allRedundantFiles.forEachIndexed { index, file ->
                        ensureActive()
                        val size = FileUtils.sizeOf(file)
                        if (!FileUtils.deleteQuietly(file)) {
                            failedFiles.add(file)
                        } else {
                            cleanedFileCount++
                            cleanedSize += size
                        }
                        task.updateProgress(
                            index.toFloat() / totalSize.toFloat()
                        )
                        task.updateMessage(androidText(
                            R.string.versions_manage_cleanup_progress, file.name
                        ))
                    }

                    task.updateProgress(-1f)

                    if (failedFiles.isNotEmpty()) {
                        throw CleanFailedException(failedFiles)
                    }
                }
            }
        )
    }

    fun cancel() {
        taskExecutor.cancel()
    }

    private fun File.alsoProgress(task: Task) = this.also {
        task.updateProgress(-1f)
        task.updateMessage(androidText(
            R.string.versions_manage_cleanup_progress, it.name
        ))
    }

    /**
     * 如果集合内不存在该路径的文件，则添加
     * @return 是否添加
     */
    private fun MutableList<File>.addIfNotContains(file: File): Boolean {
        return if (!any { it.absolutePath == file.absolutePath }) {
            add(file)
            true
        } else {
            false
        }
    }
}