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

package com.movtery.zalithlauncher.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.crashlogs.AbstractAPI
import com.movtery.zalithlauncher.crashlogs.LinkNotFoundException
import com.movtery.zalithlauncher.crashlogs.MCLogsResponse
import com.movtery.zalithlauncher.crashlogs.platform.MCLogsAPI
import com.movtery.zalithlauncher.crashlogs.platform.MirroredAPI
import com.movtery.zalithlauncher.ui.screens.main.crashlogs.ShareLinkOperation
import com.movtery.zalithlauncher.utils.isChinaMainland
import com.movtery.zalithlauncher.utils.network.isInterruptedIOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * 游戏日志上传逻辑 ViewModel
 */
class LogsUploadViewModel: ViewModel() {
    private var uploadJob: Job? = null
    private var checkJob: Job? = null

    /**
     * 日志上传操作流程
     */
    var operation by mutableStateOf<ShareLinkOperation>(ShareLinkOperation.None)

    /**
     * 日志文件是否适合上传分享
     */
    var canUpload by mutableStateOf(false)
        private set
    
    /**
     * 检查日志文件是否适合上传
     */
    fun check(logFile: File) {
        checkJob?.cancel()
        checkJob = viewModelScope.launch(Dispatchers.IO) {
            //2MB 已经不适合上传，容易超时
            //这种日志可能是缺某种库，狂刷 Y/N 类提醒导致的
            val canUpload0 = !logFile.exceeds2MB()
            withContext(Dispatchers.Main) {
                canUpload = canUpload0
            }
        }
    }

    /**
     * 检查文件是否超过 2MB
     */
    private fun File.exceeds2MB(): Boolean {
        return !exists() && !isFile && length() > 2 * 1024 * 1024
    }

    private suspend fun <E: AbstractAPI> mirroredAPI(
        list: List<E>,
        content: String
    ): MCLogsResponse {
        require(list.isNotEmpty()) { "API list must not be empty." }

        val errors = mutableListOf<Exception>()
        var lastException: Exception? = null

        for (api in list) {
            try {
                withContext(Dispatchers.Main) {
                    operation = ShareLinkOperation.Uploading(api.root)
                }

                return api.onUpload(content)
            } catch (e: Exception) {
                lastException = e

                if (e.isInterruptedIOException()) {
                    throw e
                } else {
                    errors.add(e)
                }
            }
        }

        throw IOException("All sources have failed to attempt", lastException).apply {
            errors.forEachIndexed { i, e ->
                addSuppressed(Exception("Mirror error #${i + 1}: ${e.message}"))
            }
        }
    }

    /**
     * 开始上传日志
     */
    fun upload(
        logFile: File,
        onSuccess: (link: String) -> Unit
    ) {
        uploadJob?.cancel()
        uploadJob = viewModelScope.launch(Dispatchers.IO) {
            if (logFile.exceeds2MB()) return@launch

            //读取内容并尝试上传
            val content = logFile.readText()

            val apiList = if (isChinaMainland()) {
                listOf(MirroredAPI, MCLogsAPI)
            } else {
                listOf(MCLogsAPI)
            }

            try {
                val response = mirroredAPI(
                    list = apiList,
                    content = content
                )
                withContext(Dispatchers.Main) {
                    val link = response.url
                    if (link == null) {
                        //远端数据没有可用的链接
                        operation = ShareLinkOperation.Error(
                            LinkNotFoundException()
                        )
                    } else {
                        onSuccess(link.replace("\\/", "/"))
                        operation = ShareLinkOperation.None
                    }
                }
            } catch (e: Exception) {
                if (e.isInterruptedIOException()) {
                    return@launch
                } else {
                    withContext(Dispatchers.Main) {
                        operation = ShareLinkOperation.Error(e)
                    }
                }
            }
        }
    }

    /**
     * 取消上传日志
     */
    fun cancel() {
        uploadJob?.cancel()
        uploadJob = null
        operation = ShareLinkOperation.None
    }

    override fun onCleared() {
        checkJob?.cancel()
        checkJob = null
        cancel()
    }
}