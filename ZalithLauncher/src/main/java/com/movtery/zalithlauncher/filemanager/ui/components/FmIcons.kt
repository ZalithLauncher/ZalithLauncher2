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

package com.movtery.zalithlauncher.filemanager.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.filemanager.logic.entry.ArchiveType

/**
 * 条目类型对应的图标与配色，配色取自固定预设色板
 * 亮色主题下圆底使用 tone90、图标使用 tone30，暗色主题下互换
 */
private enum class FmFileIcon(
    @DrawableRes val iconRes: Int,
    val tone90: Color,
    val tone30: Color
) {
    DIRECTORY(R.drawable.ic_folder_filled, Color(0xFFD3E3FD), Color(0xFF0842A0)),
    ARCHIVE(R.drawable.ic_folder_zip_filled, Color(0xFFFFDCC2), Color(0xFF7A3E00)),
    JAR(R.drawable.ic_coffee_filled, Color(0xFFEAD8BC), Color(0xFF4A2E10)),
    APK(R.drawable.ic_android_filled, Color(0xFFE6F2A8), Color(0xFF3F4A09)),
    CODE(R.drawable.ic_code, Color(0xFFDFE2FF), Color(0xFF26316E)),
    TEXT(R.drawable.ic_text_snippet_filled, Color(0xFFB4EBE5), Color(0xFF004F49)),
    IMAGE(R.drawable.ic_image_filled, Color(0xFFC4EED0), Color(0xFF0F5223)),
    AUDIO(R.drawable.ic_audio_file_filled, Color(0xFFEADDFF), Color(0xFF4F378B)),
    VIDEO(R.drawable.ic_movie_filled, Color(0xFFFFD9E2), Color(0xFF5C1240)),
    UNKNOWN(R.drawable.ic_draft_filled, Color(0xFFE3E2E6), Color(0xFF46464F));

    companion object {
        fun of(name: String): FmFileIcon {
            if (ArchiveType.of(name) != null) return ARCHIVE

            return when (name.substringAfterLast('.', "").lowercase()) {
                "jar" -> JAR
                "apk" -> APK
                "java", "kt", "kts", "c", "cpp", "h", "js", "ts", "py", "sh", "xml", "html", "css",
                "json", "yaml", "yml", "toml", "ini", "cfg" -> CODE
                "txt", "log", "md" -> TEXT
                "png", "jpg", "jpeg", "gif", "bmp", "webp", "svg" -> IMAGE
                "mp3", "wav", "ogg", "flac", "m4a", "aac" -> AUDIO
                "mp4", "mkv", "avi", "mov", "webm" -> VIDEO
                else -> UNKNOWN
            }
        }
    }
}

object FmIcons {
    /** 根据条目类型与扩展名显示对应图标 */
    @Composable
    fun IconFor(
        name: String,
        isDirectory: Boolean,
        modifier: Modifier = Modifier,
    ) {
        val style = if (isDirectory) FmFileIcon.DIRECTORY else FmFileIcon.of(name)
        val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
        Box(
            modifier = modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (darkTheme) style.tone30 else style.tone90),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(style.iconRes),
                contentDescription = null,
                tint = if (darkTheme) style.tone90 else style.tone30
            )
        }
    }
}