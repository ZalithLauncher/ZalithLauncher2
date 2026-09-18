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
import com.movtery.zalithlauncher.context.readRawContent
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.code_editor.EditorState
import com.movtery.zalithlauncher.ui.code_editor.SoraEditor
import com.movtery.zalithlauncher.ui.code_editor.scheme.SchemeIDEADark
import com.movtery.zalithlauncher.ui.code_editor.scheme.SchemeIDEALight
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import io.github.rosemoe.sora.text.Content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LicenseScreen(
    key: NormalNavKey.License,
    backStackViewModel: ScreenBackStackViewModel
) {
    val context = LocalContext.current
    val isDark = isLauncherInDarkTheme()

    var editorState by remember { mutableStateOf<EditorState>(EditorState.Loading) }

    LaunchedEffect(key) {
        editorState = EditorState.Loading
        val content = withContext(Dispatchers.IO) {
            runCatching {
                context.readRawContent(key.raw)
            }.getOrElse { e ->
                Logger.warning("ViewLicense", "Unable to read R.raw license", e)
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
            val scheme = remember(isDark) {
                if (isDark) SchemeIDEADark() else SchemeIDEALight()
            }

            SoraEditor(
                state = editorState,
                scheme = scheme,
                isReadOnly = true,
                onSaveClick = {}
            )
        }
    }
}