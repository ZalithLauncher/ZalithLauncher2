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

package com.movtery.zalithlauncher.utils.video

import android.media.MediaMetadataRetriever
import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File

private const val TAG = "VideoUtils"

/**
 * 尝试判断文件是否为一则视频
 */
fun File.isVideoFile(): Boolean {
    if (!exists()) return false
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(absolutePath)
        val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) != null &&
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) != null
        hasVideo
    } catch (e: Exception) {
        Logger.warning(TAG, "An exception occurred while trying to determine if $absolutePath is a video.", e)
        false
    } finally {
        retriever.release()
    }
}