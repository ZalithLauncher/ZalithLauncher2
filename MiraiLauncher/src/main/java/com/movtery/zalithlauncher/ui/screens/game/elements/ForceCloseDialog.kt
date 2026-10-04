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

package com.movtery.zalithlauncher.ui.screens.game.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog

sealed interface ForceCloseOperation {
    data object None : ForceCloseOperation
    /** 显示强制关闭对话框 */
    data object Show : ForceCloseOperation
}

@Composable
fun ForceCloseOperation(
    operation: ForceCloseOperation,
    onChange: (ForceCloseOperation) -> Unit,
    onForceClose: () -> Unit,
    text: String
) {
    when (operation) {
        ForceCloseOperation.None -> {}
        ForceCloseOperation.Show -> {
            SimpleAlertDialog(
                title = stringResource(R.string.game_button_force_close),
                text = text,
                onConfirm = onForceClose,
                onDismiss = {
                    onChange(ForceCloseOperation.None)
                }
            )
        }
    }
}