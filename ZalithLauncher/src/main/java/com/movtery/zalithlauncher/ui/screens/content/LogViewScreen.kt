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

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavBackStack
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.code_editor.EditorState
import com.movtery.zalithlauncher.ui.code_editor.SoraEditor
import com.movtery.zalithlauncher.ui.code_editor.TextMateRegistry
import com.movtery.zalithlauncher.ui.code_editor.scheme.SchemeIDEADark
import com.movtery.zalithlauncher.ui.code_editor.scheme.SchemeIDEALight
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile

/**
 * 导航到日志查看器
 */
fun NavBackStack<TitledNavKey>.navigateToLogView(
    logPath: String,
) = this.navigateTo(
    screenKey = NormalNavKey.LogView(logPath = logPath),
    useClassEquality = true
)

/** 日志查看器加载的日志体积上限，超过则只读取末尾部分 */
private const val MAX_LOG_VIEW_SIZE: Long = 8L * 1024 * 1024

private fun readLog(file: File): String {
    val size = file.length()
    if (size <= MAX_LOG_VIEW_SIZE) return file.readText()
    RandomAccessFile(file, "r").use { raf ->
        raf.seek(size - MAX_LOG_VIEW_SIZE)
        val bytes = ByteArray(MAX_LOG_VIEW_SIZE.toInt())
        raf.readFully(bytes)
        val text = String(bytes, Charsets.UTF_8)
            // 截断位置可能留下不完整的字符，解码成替换字符后去掉
            .trimStart('\uFFFD')

        // 从首个换行处开始，避免截断处留下半个字符导致首行乱码
        // 窗口内没有换行时（超长单行）则不截断，保留内容
        val firstNewline = text.indexOf('\n')
        return if (firstNewline >= 0 && firstNewline < text.length - 1) {
            text.substring(firstNewline + 1)
        } else {
            text
        }
    }
}

@Composable
fun LogViewScreen(
    key: NormalNavKey.LogView,
    backStackViewModel: ScreenBackStackViewModel
) {
    val isDark = isLauncherInDarkTheme()
    val context = LocalContext.current

    var editorState by remember { mutableStateOf<EditorState>(EditorState.Loading) }

    LaunchedEffect(key) {
        editorState = EditorState.Loading
        val content = withContext(Dispatchers.IO) {
            runCatching {
                readLog(File(key.logPath))
            }.getOrElse { e ->
                Logger.warning("ViewLog", "Unable to read log file!", e)
                e.message
            }
        }
        editorState = EditorState.Success(Content(content))
    }

    BaseScreen(
        screenKey = key,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val fallbackScheme = remember(isDark) {
                if (isDark) SchemeIDEADark() else SchemeIDEALight()
            }
            var language by remember { mutableStateOf<Language?>(null) }
            var scheme by remember { mutableStateOf<EditorColorScheme?>(null) }
            LaunchedEffect(isDark) {
                language = TextMateRegistry.languageFor("text.log", context)
                scheme = TextMateRegistry.colorScheme(isDark, context)
            }

            SoraEditor(
                state = editorState,
                scheme = scheme ?: fallbackScheme,
                language = language,
                isReadOnly = true,
                onSaveClick = {}
            )
        }
    }
}