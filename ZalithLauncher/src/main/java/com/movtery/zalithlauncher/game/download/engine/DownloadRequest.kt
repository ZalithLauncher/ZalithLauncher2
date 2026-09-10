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

package com.movtery.zalithlauncher.game.download.engine

import java.io.File
import java.io.IOException

/**
 * 一个待下载文件的任务规格。urls 为按优先级排列的候选源列表，
 * 引擎会在失败或受限时自动沿列表换源。
 */
class DownloadRequest(
    val urls: List<String>,
    val targetFile: File,
    val sha1: String? = null,
    /** 已知的文件大小，未知时传 -1；仅用于预分配与进度统计，最终以实际响应为准 */
    val expectedSize: Long = -1L,
    /** 调用方附带的上下文对象，在进度与成功回调中原样带回 */
    val tag: Any? = null
) {
    init {
        require(urls.isNotEmpty()) { "Download requires at least one url" }
    }

    override fun toString(): String = targetFile.name
}

/** 沿因果链查找第一个 HTTP 状态异常，用于调用方识别 404 等语义 */
fun Throwable.findHttpCode(): Int? =
    generateSequence(this) { it.cause }
        .filterIsInstance<HttpResultException>()
        .firstOrNull()
        ?.code

/** 全部源尝试完毕仍然失败时抛出，message 内含每个源的失败原因 */
class AllSourcesFailedException(
    summary: String,
    cause: Throwable?
) : IOException(summary, cause)

class HttpResultException(
    val code: Int,
    message: String
) : IOException(message)
