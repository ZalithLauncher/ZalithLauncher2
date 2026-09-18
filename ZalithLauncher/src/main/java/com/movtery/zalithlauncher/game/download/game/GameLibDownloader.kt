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

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.version.download.BaseMinecraftDownloader
import com.movtery.zalithlauncher.game.version.download.DEFAULT_DOWNLOAD_THREADS
import com.movtery.zalithlauncher.game.version.download.DownloadTask
import com.movtery.zalithlauncher.game.version.download.parseTo
import com.movtery.zalithlauncher.game.version.download.runBatchDownloads
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.file.formatFileSize
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 游戏支持库下载器
 * 装配版本 JSON 中的支持库任务并交给批量下载引擎执行
 */
class GameLibDownloader(
    private val downloader: BaseMinecraftDownloader,
    private val gameJson: String,
    private val maxDownloadThreads: Int = DEFAULT_DOWNLOAD_THREADS
) {
    private val allDownloadTasks = ConcurrentLinkedQueue<DownloadTask>()

    //判断是否已经开始下载
    private var isDownloadStarted: Boolean = false

    /**
     * 计划下载所有支持库
     */
    suspend fun schedule(
        task: Task,
        targetDir: File = downloader.librariesTarget,
        updateProgress: Boolean = true
    ) {
        val gameManifest = gameJson.parseTo(GameManifest::class.java)

        if (updateProgress) {
            task.updateProgress(-1f)
            task.updateMessage(androidText(R.string.minecraft_download_stat_download_task))
        }

        //仅加载处理支持库
        downloader.loadLibraryDownloads(gameManifest, targetDir) { urls, hash, targetFile, size, isDownloadable ->
            scheduleDownload(urls, hash, targetFile, size, isDownloadable)
        }
    }

    /**
     * 交给下载引擎执行全部支持库任务
     */
    suspend fun download(task: Task) {
        isDownloadStarted = true
        val tasks = allDownloadTasks.toList()
        if (tasks.isNotEmpty()) {
            task.runBatchDownloads(
                tasks = tasks,
                maxConnections = maxDownloadThreads,
                retryRounds = 1,
                onSnapshot = { snapshot ->
                    task.updateSpeed(snapshot.speedBytesPerSec)
                    task.updateMessage(androidText(
                        R.string.minecraft_download_downloading_game_files,
                        snapshot.downloadedFiles, snapshot.totalFiles,
                        formatFileSize(snapshot.downloadedBytes), formatFileSize(snapshot.totalBytes)
                    ))
                }
            )
        }

        //清除任务信息
        task.updateProgress(1f)
        task.updateMessage(null)
    }

    /**
     * 提交计划下载
     */
    fun scheduleDownload(urls: List<String>, sha1: String?, targetFile: File, size: Long, isDownloadable: Boolean = true) {
        if (isDownloadStarted) throw IllegalStateException("The download has already started; adding more download tasks is no longer meaningful.")

        if (allDownloadTasks.any { it.targetFile.absolutePath == targetFile.absolutePath }) return

        allDownloadTasks.add(
            DownloadTask(
                urls = urls,
                verifyIntegrity = true,
                targetFile = targetFile,
                sha1 = sha1,
                size = size,
                isDownloadable = isDownloadable
            )
        )
    }

    /**
     * 删除某一项下载任务
     * 在下载前可以使用
     */
    fun removeDownload(predicate: (DownloadTask) -> Boolean) {
        if (isDownloadStarted) throw IllegalStateException("The download has already started; removing download tasks is no longer meaningful.")
        allDownloadTasks.removeIf(predicate)
    }
}
