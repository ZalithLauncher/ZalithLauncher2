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

package com.movtery.zalithlauncher.game.version.download

import android.content.Context
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.game.versioninfo.models.VersionManifest
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.utils.logging.Logger.lError
import com.movtery.zalithlauncher.utils.logging.Logger.lInfo
import com.movtery.zalithlauncher.utils.network.withSpeedReport
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong

/**
 * Minecraft 安装器
 * @param version 要安装的原版版本号
 * @param customName 自定义目标版本名称，将安装到该名称的文件夹内
 * @param verifyIntegrity 是否验证完整性
 * @param onCompletion 完成安装
 * @param onError 安装出现异常，将错误反馈给用户
 * @param onThrowable 安装出现异常，需直接处理异常时 将覆盖 onError
 * @param maxDownloadThreads 最大下载线程数
 */
class MinecraftDownloader(
    private val context: Context,
    private val version: String,
    private val customName: String = version,
    private val verifyIntegrity: Boolean,
    private val downloader: BaseMinecraftDownloader = BaseMinecraftDownloader(verifyIntegrity = verifyIntegrity),
    private val mode: DownloadMode = DownloadMode.DOWNLOAD,
    private val onCompletion: suspend (Task) -> Unit = {},
    private val onError: (message: String) -> Unit = {},
    private val onThrowable: ((throwable: Throwable) -> Unit)? = null,
    private val maxDownloadThreads: Int = 64
) {
    //已下载文件计数器
    private var downloadedFileSize: AtomicLong = AtomicLong(0)
    private var downloadedFileCount: AtomicLong = AtomicLong(0)
    private var totalFileSize: AtomicLong = AtomicLong(0)
    private var totalFileCount: AtomicLong = AtomicLong(0)

    private var allDownloadTasks = mutableListOf<DownloadTask>()
    private var downloadFailedTasks = mutableListOf<DownloadTask>()

    /** 用于速率监测的已写入大小记录 */
    private val mSpeedReport = AtomicLong(0L)

    private fun getTaskMessage(download: Int, verify: Int): Int =
        when (mode) {
            DownloadMode.DOWNLOAD -> download
            DownloadMode.VERIFY_AND_REPAIR -> verify
        }

    /**
     * 自定义 client 目录 ->client<-/versions/..
     */
    fun getDownloadTask(
        clientName: String = this.customName,
        clientVersionsDir: File = downloader.versionsTarget
    ): Task {
        return Task.runTask(
            id = DOWNLOADER_TAG,
            dispatcher = Dispatchers.Default,
            task = { task ->
                task.updateProgress(-1f, getTaskMessage(R.string.minecraft_download_stat_download_task, R.string.minecraft_download_stat_verify_task))
                if (mode == DownloadMode.DOWNLOAD) {
                    progressNewDownloadTasks(clientName, clientVersionsDir)
                } else {
                    val jsonFile = downloader.getVersionJsonPath(customName).takeIf { it.canRead() } ?: throw IOException("Version $customName JSON file is unreadable.")
                    val jsonText = jsonFile.readText()
                    val gameManifest = jsonText.parseTo(GameManifest::class.java)
                    progressDownloadTasks(gameManifest, clientName)
                }

                if (allDownloadTasks.isNotEmpty()) {
                    downloadAll(task, allDownloadTasks, getTaskMessage(R.string.minecraft_download_downloading_game_files, R.string.minecraft_download_verifying_and_repairing_files))
                    if (downloadFailedTasks.isNotEmpty()) {
                        downloadedFileCount.set(0)
                        totalFileCount.set(downloadFailedTasks.size.toLong())
                        downloadAll(task, downloadFailedTasks.toList(), getTaskMessage(R.string.minecraft_download_progress_retry_downloading_files, R.string.minecraft_download_progress_retry_verifying_files))
                    }
                    if (downloadFailedTasks.isNotEmpty()) throw DownloadFailedException()
                }
                //清除任务信息
                task.updateProgress(1f, null)

                onCompletion(task)
            },
            onError = { e ->
                lError("Failed to download Minecraft!", e)
                if (onThrowable != null) {
                    onThrowable(e)
                } else {
                    val message = when(e) {
                        is CancellationException -> return@runTask
                        is FileNotFoundException -> context.getString(R.string.minecraft_download_failed_notfound)
                        is DownloadFailedException -> {
                            val failedUrls = downloadFailedTasks.map { it.urls.joinToString(", ") }
                            "${ context.getString(R.string.minecraft_download_failed_retried) }\r\n${ failedUrls.joinToString("\r\n") }"
                        }
                        else -> e.getMessageOrToString()
                    }
                    onError(message)
                }
            }
        )
    }

    private suspend fun downloadAll(
        task: Task,
        tasks: List<DownloadTask>,
        taskMessageRes: Int
    ) = withContext(Dispatchers.IO) {
        coroutineScope {
            downloadFailedTasks.clear()

            val semaphore = Semaphore(maxDownloadThreads)

            val downloadJobs = tasks.map { downloadTask ->
                launch {
                    semaphore.withPermit {
                        downloadTask.download()
                    }
                }
            }

            val progressJob = launch(Dispatchers.Main) {
                while (isActive) {
                    ensureActive()
                    val currentFileSize = downloadedFileSize.get()
                    val totalFileSize = totalFileSize.get().run { if (this < currentFileSize) currentFileSize else this }
                    task.updateProgress(
                        (currentFileSize.toFloat() / totalFileSize.toFloat()).coerceIn(0f, 1f),
                        taskMessageRes,
                        downloadedFileCount.get(), totalFileCount.get(), //文件个数
                        formatFileSize(currentFileSize), formatFileSize(totalFileSize) //文件大小
                    )
                    delay(100)
                }
            }

            try {
                withSpeedReport(
                    onTimeReport = {
                        val currentBytes = mSpeedReport.getAndSet(0L)
                        task.updateSpeed(currentBytes)
                    },
                    onClear = {
                        mSpeedReport.set(0L)
                        task.clearSpeed()
                    }
                ) {
                    downloadJobs.joinAll()
                }
            } catch (e: CancellationException) {
                downloadJobs.forEach { it.cancel("Parent cancelled", e) }
                throw e
            } finally {
                progressJob.cancel()
            }
        }
    }

    /**
     * 仅将 Jar、Json 文件安装到自定义版本目录中
     */
    private suspend fun progressNewDownloadTasks(
        clientName: String,
        clientVersionsDir: File
    ) {
        val gameManifest = downloader.findVersion(this.version)?.let {
            downloader.createVersionJson(it, clientName, clientVersionsDir)
        } ?: throw IllegalArgumentException("Version not found: $version")

        commonScheduleDownloads(gameManifest, null, clientName, clientVersionsDir)
    }

    private suspend fun progressDownloadTasks(
        gameManifest: GameManifest,
        clientName: String,
        clientVersionsDir: File = downloader.versionsTarget
    ) {
        val inheritsFrom = downloader.takeIf {
            gameManifest.inheritsFrom != null
        }?.findVersion(gameManifest.inheritsFrom)

        //优先尝试解析原版
        inheritsFrom?.let {
            downloader.createVersionJson(it)
        }?.let { gameManifest1 ->
            progressDownloadTasks(gameManifest1, gameManifest.inheritsFrom)
        }

        commonScheduleDownloads(
            gameManifest = gameManifest,
            inheritsFrom = inheritsFrom,
            clientName = clientName,
            clientVersionsDir = clientVersionsDir
        )
    }

    private suspend fun commonScheduleDownloads(
        gameManifest: GameManifest,
        inheritsFrom: VersionManifest.Version? = null,
        clientName: String,
        clientVersionsDir: File
    ) {
        val assetsIndex = downloader.createAssetIndex(downloader.assetIndexTarget, gameManifest)

        downloader.loadClientJarDownload(
            gameManifest = gameManifest,
            clientName = clientName,
            mcFolder = clientVersionsDir,
            scheduleDownload = { urls, hash, targetFile, size ->
                scheduleDownload(urls, hash, targetFile, size)
            },
            scheduleCopy = { targetFile ->
                inheritsFrom?.let { inheritsFrom ->
                    val inheritsJar = downloader.getVersionJarPath(inheritsFrom.id)

                    allDownloadTasks.find {
                        it.targetFile.absolutePath == inheritsJar.absolutePath
                    }?.let { task ->
                        task.fileDownloadedTask = {
                            if (!targetFile.exists() && inheritsJar.exists()) {
                                inheritsJar.copyTo(targetFile, overwrite = true)
                                lInfo("Copied ${inheritsJar.absolutePath} to ${targetFile.absolutePath}")
                            }
                        }
                    }
                }
            }
        )
        downloader.loadAssetsDownload(assetsIndex) { urls, hash, targetFile, size ->
            scheduleDownload(urls, hash, targetFile, size)
        }
        downloader.loadLibraryDownloads(gameManifest) { urls, hash, targetFile, size, isDownloadable ->
            scheduleDownload(urls, hash, targetFile, size, isDownloadable)
        }
    }

    /**
     * 提交计划下载
     */
    private fun scheduleDownload(urls: List<String>, sha1: String?, targetFile: File, size: Long, isDownloadable: Boolean = true) {
        totalFileCount.incrementAndGet()
        totalFileSize.addAndGet(size)
        allDownloadTasks.add(
            DownloadTask(
                urls = urls,
                verifyIntegrity = verifyIntegrity,
                targetFile = targetFile,
                sha1 = sha1,
                isDownloadable = isDownloadable,
                onDownloadFailed = { task ->
                    downloadFailedTasks.add(task)
                },
                onFileDownloadedSize = { downloadedSize ->
                    downloadedFileSize.addAndGet(downloadedSize)
                    mSpeedReport.addAndGet(downloadedSize)
                },
                onFileDownloaded = {
                    downloadedFileCount.incrementAndGet()
                }
            )
        )
    }
}