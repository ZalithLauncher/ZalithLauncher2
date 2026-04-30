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

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.context.copyLocalFile
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.image.isImageFile
import com.movtery.zalithlauncher.utils.video.isVideoFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

/**
 * 启动器背景管理
 */
class BackgroundViewModel : ViewModel() {
    val backgroundFile: File = PathManager.FILE_LAUNCHER_BACKGROUND

    /**
     * 背景文件是否有效（存在，且是有效的视频或图片）
     */
    var isValid by mutableStateOf(false)
        private set

    /**
     * 背景文件是一个视频
     */
    var isVideo by mutableStateOf(false)
        private set

    /**
     * 背景文件是一个图片
     */
    var isImage by mutableStateOf(false)
        private set

    var refreshTrigger by mutableStateOf(false)
        private set

    private suspend fun updateState() {
        val (isImage0, isVideo0) = withContext(Dispatchers.IO) {
            val isImage = backgroundFile.isImageFile()
            //如果文件是图片，则不检查是否为视频
            val isVideo = if (!isImage) backgroundFile.isVideoFile() else false
            isImage to isVideo
        }
        //更新状态
        withContext(Dispatchers.Main) {
            isImage = isImage0
            isVideo = isVideo0
            isValid = backgroundFile.exists() && (isImage0 || isVideo0)
            refreshTrigger = !refreshTrigger
        }
    }

    init {
        viewModelScope.launch(Dispatchers.Main) {
            updateState()
        }
    }

    suspend fun delete() {
        withContext(Dispatchers.IO) {
            FileUtils.deleteQuietly(backgroundFile)
            updateState()
        }
    }

    suspend fun import(context: Context, result: Uri) {
        withContext(Dispatchers.IO) {
            FileUtils.deleteQuietly(backgroundFile)
            context.copyLocalFile(result, backgroundFile)
            if (!backgroundFile.isImageFile() && !backgroundFile.isVideoFile()) {
                //不是媒体类文件
                FileUtils.deleteQuietly(backgroundFile)
                error("The selected file is not an image or a video!")
            }
            updateState()
        }
    }
}

/**
 * 本地随时可以使用的背景图片管理 ViewModel
 * 由 MainActivity 的主题提供
 */
val LocalBackgroundViewModel = compositionLocalOf<BackgroundViewModel?> {
    error("No BackgroundViewModel provided")
}

@Composable
fun <E> influencedByBackground(
    value: E,
    influenced: E,
    enabled: Boolean = true
): E {
    return if (enabled) {
        if (LocalBackgroundViewModel.current?.isValid == true) {
            influenced
        } else {
            value
        }
    } else {
        value
    }
}