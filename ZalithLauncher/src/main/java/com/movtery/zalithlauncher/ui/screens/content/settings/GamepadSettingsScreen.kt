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

package com.movtery.zalithlauncher.ui.screens.content.settings

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.unit.floatRange
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedLazyColumn
import com.movtery.zalithlauncher.ui.components.CheckChip
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.control.GamepadBindingKeyboard
import com.movtery.zalithlauncher.ui.control.gamepad.GamepadMap
import com.movtery.zalithlauncher.ui.control.gamepad.JoystickMode
import com.movtery.zalithlauncher.ui.control.gamepad.getNameByGamepadEvent
import com.movtery.zalithlauncher.ui.control.gamepad.remapperMMKV
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.IntSliderSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.ListSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank
import com.movtery.zalithlauncher.viewmodel.GAMEPAD_CONFIG_NAME_LENGTH
import com.movtery.zalithlauncher.viewmodel.GamepadViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface BindKeyOperation {
    data object None : BindKeyOperation
    /** 展示键值绑定对话框，开始绑定键值 */
    data class OnBind(val map: GamepadMap) : BindKeyOperation
}

private sealed interface CreateNewConfigOperation {
    data object None : CreateNewConfigOperation
    /** 创建新的映射配置 */
    data object Create : CreateNewConfigOperation
    /** 删除当前映射配置 */
    data object Delete : CreateNewConfigOperation
}

@Composable
fun GamepadSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?
) {
    val viewModel: GamepadViewModel = viewModel()

    var bindKeyOperation by remember { mutableStateOf<BindKeyOperation>(BindKeyOperation.None) }
    var createConfigOperation by remember {
        mutableStateOf<CreateNewConfigOperation>(CreateNewConfigOperation.None)
    }

    /**
     * 编辑手柄按键绑定：true为游戏内，false为菜单内
     */
    var editKeyInGame by remember { mutableStateOf(true) }

    /**
     * 用于更新列表
     */
    var refreshed by remember { mutableStateOf(false) }

    val currentMapping = remember(refreshed) {
        viewModel.currentMapping
    }
    /**
     * 根据当前是否拥有配置，决定是否可以展示绑定页
     */
    val canShowBind = remember(currentMapping) {
        currentMapping != null
    }

    BindKeyOperation(
        operation = bindKeyOperation,
        changeOperation = { bindKeyOperation = it },
        viewModel = viewModel,
        editKeyInGame = editKeyInGame,
        onRefresh = { refreshed = refreshed.not() }
    )

    CreateNewConfigOperation(
        operation = createConfigOperation,
        onChange = { createConfigOperation = it },
        checkContains = { name ->
            viewModel.containsConfig(name)
        },
        onCreateConfig = { name ->
            viewModel.createNewConfig(
                name = name,
                onContainsConfig = {
                    lWarning("There is already a configuration with the same name")
                },
                onFinished = {
                    refreshed = refreshed.not()
                }
            )
        },
        onDeleteCurrent = {
            if (currentMapping != null) {
                viewModel.deleteConfig(
                    name = currentMapping.name,
                    onFinished = {
                        refreshed = refreshed.not()
                    }
                )
            }
        }
    )

    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.Gamepad, settingsScreenKey, false)
    ) { isVisible ->
        AnimatedLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            isVisible = isVisible
        ) { scope ->
            animatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    val scope = rememberCoroutineScope()
                    val context = LocalContext.current

                    val resetFinishedText = stringResource(R.string.settings_gamepad_remapping_reset_finished)
                    SettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        title = stringResource(R.string.settings_gamepad_remapping_reset_title),
                        summary = stringResource(R.string.settings_gamepad_remapping_reset_summary),
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                val mmkv = remapperMMKV()
                                mmkv.clearAll()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        resetFinishedText,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.gamepadControl,
                        title = stringResource(R.string.settings_gamepad_title),
                        summary = stringResource(R.string.settings_gamepad_summary)
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.gamepadDeadZoneScale,
                        title = stringResource(R.string.settings_gamepad_deadzone_title),
                        summary = stringResource(R.string.settings_gamepad_deadzone_summary),
                        valueRange = AllSettings.gamepadDeadZoneScale.floatRange,
                        suffix = "%",
                        enabled = AllSettings.gamepadControl.state,
                        fineTuningControl = true
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.gamepadCursorSensitivity,
                        title = stringResource(R.string.settings_gamepad_cursor_sensitivity_title),
                        summary = stringResource(R.string.settings_gamepad_cursor_sensitivity_summary),
                        valueRange = AllSettings.gamepadCursorSensitivity.floatRange,
                        suffix = "%",
                        enabled = AllSettings.gamepadControl.state,
                        fineTuningControl = true
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.gamepadCameraSensitivity,
                        title = stringResource(R.string.settings_gamepad_camera_sensitivity_title),
                        summary = stringResource(R.string.settings_gamepad_camera_sensitivity_summary),
                        valueRange = AllSettings.gamepadCameraSensitivity.floatRange,
                        suffix = "%",
                        enabled = AllSettings.gamepadControl.state,
                        fineTuningControl = true
                    )

                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.joystickControlMode,
                        items = JoystickMode.entries,
                        title = stringResource(R.string.settings_gamepad_joystick_mode_title),
                        summary = stringResource(R.string.settings_gamepad_joystick_mode_summary),
                        getItemText = { mode ->
                            stringResource(mode.titleRes)
                        },
                        getItemSummary = { mode ->
                            Text(
                                modifier = Modifier.alpha(0.7f),
                                text = stringResource(mode.summaryRes),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        enabled = AllSettings.gamepadControl.state
                    )
                }
            }

            animatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    val list = remember(refreshed) {
                        viewModel.getAllConfigKeys()
                    }
                    val isCreateOnly = remember(list, currentMapping) {
                        list.isEmpty() && currentMapping == null
                    }

                    if (list.isNotEmpty()) {
                        ListSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Top,
                            unit = AllSettings.gamepadMappingConfig,
                            items = list,
                            getItemText = { it },
                            getItemId = { it },
                            title = stringResource(R.string.settings_gamepad_config_title),
                            summary = stringResource(R.string.settings_gamepad_config_summary),
                            enabled = AllSettings.gamepadControl.state,
                            onValueChange = {
                                viewModel.reloadAllMappings()
                                refreshed = refreshed.not()
                            }
                        )
                    }

                    if (currentMapping != null) {
                        SettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            title = stringResource(R.string.settings_gamepad_config_delete),
                            innerPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                            onClick = {
                                createConfigOperation = CreateNewConfigOperation.Delete
                            }
                        )
                    }

                    SettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = if (isCreateOnly) CardPosition.Single else CardPosition.Bottom,
                        title = stringResource(R.string.settings_gamepad_config_create),
                        innerPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                        onClick = {
                            createConfigOperation = CreateNewConfigOperation.Create
                        }
                    )
                }
            }

            if (canShowBind) {
                animatedItem(scope) { yOffset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        //游戏内
                        CheckChip(
                            selected = editKeyInGame,
                            label = {
                                Text(text = stringResource(R.string.settings_gamepad_mapping_in_game))
                            },
                            onClick = {
                                editKeyInGame = true
                            }
                        )

                        //菜单内
                        CheckChip(
                            selected = editKeyInGame.not(),
                            label = {
                                Text(text = stringResource(R.string.settings_gamepad_mapping_in_menu))
                            },
                            onClick = {
                                editKeyInGame = false
                            }
                        )
                    }
                }

                animatedItems(
                    lazyListScope = scope,
                    items = GamepadMap.entries,
                    key = { it.identifier }
                ) { _, item, yOffset ->
                    SettingsCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                        position = CardPosition.Single,
                        onClick = {
                            bindKeyOperation = BindKeyOperation.OnBind(item)
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(all = 16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Image(
                                modifier = Modifier
                                    .padding(all = 6.dp)
                                    .size(32.dp),
                                painter = painterResource(item.getIconRes()),
                                contentDescription = null,
                                contentScale = ContentScale.Fit
                            )

                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val codes = remember(item, editKeyInGame, refreshed) {
                                    viewModel.currentMapping?.findByMap(item, inGame = editKeyInGame)?.toList() ?: emptyList()
                                }

                                Text(
                                    text = if (codes.isNotEmpty()) {
                                        stringResource(R.string.settings_gamepad_mapping_bound)
                                    } else {
                                        stringResource(R.string.settings_gamepad_mapping_unbound)
                                    },
                                    style = MaterialTheme.typography.titleSmall
                                )

                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .basicMarquee(Int.MAX_VALUE),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    codes.forEach { code ->
                                        LittleTextLabel(
                                            text = getNameByGamepadEvent(code)
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        viewModel.currentMapping?.resetMapping(item, editKeyInGame)
                                        refreshed = refreshed.not()
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_restart_alt),
                                        contentDescription = stringResource(R.string.generic_reset)
                                    )
                                }

                                Icon(
                                    modifier = Modifier.size(28.dp),
                                    painter = painterResource(R.drawable.ic_arrow_right_rounded),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BindKeyOperation(
    operation: BindKeyOperation,
    changeOperation: (BindKeyOperation) -> Unit,
    viewModel: GamepadViewModel,
    editKeyInGame: Boolean,
    onRefresh: () -> Unit
) {
    when (operation) {
        is BindKeyOperation.None -> {}
        is BindKeyOperation.OnBind -> {
            val gamepad = operation.map

            val selectedKeys = remember(gamepad, editKeyInGame) {
                val mapList = viewModel.currentMapping?.findByMap(gamepad, inGame = editKeyInGame)?.toList() ?: emptyList()
                mapList.toMutableList()
            }

            GamepadBindingKeyboard(
                selectedKeys = selectedKeys,
                onKeyAdd = { key ->
                    selectedKeys.add(key)
                    viewModel.currentMapping?.saveMapping(gamepad, selectedKeys.toSet(), editKeyInGame)
                    onRefresh()
                },
                onKeyRemove = { key ->
                    selectedKeys.remove(key)
                    viewModel.currentMapping?.saveMapping(gamepad, selectedKeys.toSet(), editKeyInGame)
                    onRefresh()
                },
                onDismissRequest = {
                    changeOperation(BindKeyOperation.None)
                }
            )
        }
    }
}

@Composable
private fun CreateNewConfigOperation(
    operation: CreateNewConfigOperation,
    onChange: (CreateNewConfigOperation) -> Unit,
    checkContains: (name: String) -> Boolean,
    onCreateConfig: (name: String) -> Unit,
    onDeleteCurrent: () -> Unit
) {
    when (operation) {
        is CreateNewConfigOperation.None -> {}
        is CreateNewConfigOperation.Create -> {
            var name by remember { mutableStateOf("") }
            val isContains = remember(name) {
                if (!name.isEmptyOrBlank()) {
                    checkContains(name)
                } else {
                    false
                }
            }
            val isNameEmpty = remember(name) {
                name.isEmptyOrBlank()
            }

            SimpleEditDialog(
                title = stringResource(R.string.settings_gamepad_config_create),
                value = name,
                onValueChange = { name = it.take(GAMEPAD_CONFIG_NAME_LENGTH) },
                label = {
                    val text = stringResource(R.string.settings_gamepad_config_create_name)
                    Text(
                        text = "$text (${name.length}/$GAMEPAD_CONFIG_NAME_LENGTH)"
                    )
                },
                isError = isContains || isNameEmpty,
                supportingText = {
                    if (isContains) {
                        Text(stringResource(R.string.settings_gamepad_config_create_contains))
                    } else if (isNameEmpty) {
                        Text(stringResource(R.string.generic_cannot_empty))
                    }
                },
                singleLine = true,
                onConfirm = {
                    if (!isContains && !isNameEmpty) {
                        onCreateConfig(name)
                        onChange(CreateNewConfigOperation.None)
                    }
                },
                onDismissRequest = {
                    onChange(CreateNewConfigOperation.None)
                }
            )
        }
        is CreateNewConfigOperation.Delete -> {
            SimpleAlertDialog(
                title = stringResource(R.string.settings_gamepad_config_delete),
                text = stringResource(R.string.settings_gamepad_config_delete_message),
                onDismiss = {
                    onChange(CreateNewConfigOperation.None)
                },
                onConfirm = {
                    onDeleteCurrent()
                    onChange(CreateNewConfigOperation.None)
                }
            )
        }
    }
}