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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.control.ControlData
import com.movtery.zalithlauncher.game.control.ControlManager
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleListDialog
import com.movtery.zalithlauncher.ui.components.SimpleListItem
import java.io.File

sealed interface ReplacementControlState {
    data object None : ReplacementControlState
    /** 显示更换控制布局的对话框 */
    data object Show : ReplacementControlState
}

@Composable
fun ReplacementControlOperation(
    operation: ReplacementControlState,
    onChange: (ReplacementControlState) -> Unit,
    currentLayout: File?,
    replacementControl: (File) -> Unit
) {
    when (operation) {
        ReplacementControlState.None -> {}
        ReplacementControlState.Show -> {
            ReplacementControlDialog(
                currentLayout = currentLayout,
                onLayoutSelected = { data ->
                    replacementControl(data.file)
                },
                onDismissRequest = {
                    onChange(ReplacementControlState.None)
                }
            )
        }
    }
}

@Composable
private fun ReplacementControlDialog(
    currentLayout: File?,
    onLayoutSelected: (ControlData) -> Unit,
    onDismissRequest: (selected: Boolean) -> Unit
) {
    val dataList by ControlManager.dataList.collectAsStateWithLifecycle()
    val controls = remember(dataList) { dataList.filter { it.isSupport } }

    if (controls.isNotEmpty()) {
        val locale = LocalConfiguration.current.locales[0]

        val current = remember(controls) {
            controls.find { currentLayout?.name == it.file.name }
        }

        SimpleListDialog(
            title = stringResource(R.string.game_menu_option_replacement_control),
            items = controls,
            current = current,
            onItemSelected = onLayoutSelected,
            onDismissRequest = onDismissRequest,
            itemLayout = { item, isCurrent, onClick ->
                SimpleListItem(
                    selected = isCurrent,
                    itemName = item.controlLayout.info.name.translate(locale),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onClick
                )
            }
        )
    } else {
        SimpleAlertDialog(
            title = stringResource(R.string.game_menu_option_replacement_control),
            text = stringResource(R.string.control_manage_list_empty),
            onConfirm = {
                onDismissRequest(false)
            },
            dismissText = stringResource(R.string.generic_refresh),
            onDismiss = {
                ControlManager.refresh()
            }
        )
    }
}