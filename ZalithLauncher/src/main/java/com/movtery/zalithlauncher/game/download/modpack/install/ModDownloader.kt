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

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.download.engine.findHttpCode
import com.movtery.zalithlauncher.game.version.download.DownloadFailedException
import com.movtery.zalithlauncher.game.version.download.DownloadTask
import com.movtery.zalithlauncher.game.version.download.runBatchDownloads
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.isInterruptedIOException
import io.ktor.server.plugins.NotFoundException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.FileNotFoundException
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "ModDownloader"

/**
 * 整合包模组下载器
 * 先并发把延迟解析的模组链接解析出来，再交给批量下载引擎执行
 */
class ModDownloader(
    val mods: List<ModFile>,
    private val maxDownloadThreads: Int = 64
) {
    suspend fun startDownload(task: Task) {
        task.updateMessage(null)

        val resolvedFailures = AtomicInteger(0)
        val missingMods = AtomicInteger(0)
        val tasks = prepareAll(resolvedFailures, missingMods)

        try {
            task.runBatchDownloads(
                tasks = tasks,
                maxConnections = maxDownloadThreads,
                retryRounds = 1,
                onSnapshot = { snapshot ->
                    task.updateSpeed(snapshot.speedBytesPerSec)
                    task.updateMessage(
                        androidText(
                            R.string.download_modpack_download_mods,
                            snapshot.downloadedFiles + missingMods.get(),
                            snapshot.totalFiles + missingMods.get(),
                            formatFileSize(snapshot.downloadedBytes)
                        )
                    )
                },
                acceptFailure = { _, error ->
                    val code = error.findHttpCode()
                    val skipped = error is FileNotFoundException || error is NotFoundException || code == 404
                    if (skipped) missingMods.incrementAndGet()
                    skipped
                }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.error(TAG, "Some mods failed to download", e)
        }

        if (resolvedFailures.get() > 0) {
            throw DownloadFailedException("${resolvedFailures.get()} mods failed to resolve download links")
        }

        task.updateProgress(1f)
        task.updateMessage(null)
    }

    /** 并发解析全部模组的下载信息，解析失败的记入失败计数 */
    private suspend fun prepareAll(resolvedFailures: AtomicInteger, missingMods: AtomicInteger): List<DownloadTask> =
        coroutineScope {
            val semaphore = Semaphore(maxDownloadThreads)
            mods.map { mod ->
                async(Dispatchers.IO) {
                    semaphore.withPermit {
                        try {
                            prepare(mod)
                        } catch (_: CancellationException) {
                            throw CancellationException("mod link resolving cancelled")
                        } catch (_: FileNotFoundException) {
                            //已在源端下架（404）的模组会被跳过而不是终止安装
                            missingMods.incrementAndGet()
                            null
                        } catch (_: NotFoundException) {
                            //已在源端下架（404）的模组会被跳过而不是终止安装
                            missingMods.incrementAndGet()
                            null
                        } catch (e: Exception) {
                            if (e.isInterruptedIOException()) throw CancellationException("cancelled", e)
                            Logger.error(TAG, "Failed to resolve mod links: ${mod.outputFile?.name}", e)
                            resolvedFailures.incrementAndGet()
                            null
                        }
                    }
                }
            }.awaitAll().filterNotNull()
        }

    /**
     * 解析单个模组的下载信息（CurseForge 包常只有 projectID/fileID，必须现场请求）
     * 只有已经拿到直链的 ModFile 的 getFile 才是 null。
     */
    private suspend fun prepare(mod: ModFile): DownloadTask {
        val file = mod.getFile?.invoke() ?: mod
        val urls = file.downloadUrls ?: throw FileNotFoundException("No download url for ${file.outputFile}")
        val output = file.outputFile ?: throw FileNotFoundException("No target file for mod")

        return DownloadTask(
            urls = urls,
            verifyIntegrity = true,
            targetFile = output,
            sha1 = file.sha1
        )
    }
}
