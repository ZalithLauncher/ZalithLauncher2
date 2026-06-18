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

package com.movtery.zalithlauncher.ui.screens.main.control_editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.layer_controller.ControlEditorLayer
import com.movtery.layer_controller.data.ButtonSize
import com.movtery.layer_controller.data.CenterPosition
import com.movtery.layer_controller.data.DefaultJoystickData
import com.movtery.layer_controller.data.NormalData
import com.movtery.layer_controller.data.TextData
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.data.createAdaptiveButtonSize
import com.movtery.layer_controller.data.createWidgetWithUUID
import com.movtery.layer_controller.data.lang.createTranslatable
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.layout.createNewLayer
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableJoystickData
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.CURSOR_DISABLED
import com.movtery.zalithlauncher.bridge.ZLBridgeStates
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme
import com.movtery.zalithlauncher.ui.components.MenuState
import com.movtery.zalithlauncher.ui.components.ProgressDialog
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.components.rememberBoxSize
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_layer.EditControlLayerDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_layer.EditSwitchLayersVisibilityDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_style.EditButtonStyleDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_style.StyleListDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_translatable.EditTranslatableTextDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget.EditWidgetDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget.SelectLayers
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget.SelectedWidgetData
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.EditorViewModel
import com.movtery.zalithlauncher.viewmodel.JoystickMovementViewModel
import java.io.File

/**
 * 控制布局编辑器主要UI，用于编辑控制布局
 * @param exit 保存后执行的退出
 * @param menuExit 通过菜单直接调用的“直接退出”
 */
@Composable
fun BoxWithConstraintsScope.ControlEditor(
    viewModel: EditorViewModel,
    targetFile: File,
    exit: () -> Unit,
    menuExit: () -> Unit
) {
    val observedLayout = viewModel.observableLayout

    val layers by observedLayout.layers.collectAsStateWithLifecycle()
    val styles by observedLayout.styles.collectAsStateWithLifecycle()

    val globalJoystick = observedLayout.globalJoystick

    /** 默认新建的控件层的名称 */
    val defaultLayerName = stringResource(R.string.control_editor_edit_layer_default)

    /** 默认新建的按键的名称 */
    val defaultButtonName = stringResource(R.string.control_editor_edit_button_default)

    /** 默认新建的文本框的名称 */
    val defaultTextName = stringResource(R.string.control_editor_edit_text_default)

    val density = LocalDensity.current
    val screenSize = rememberBoxSize()

    if (viewModel.isPreviewMode) {
        val joystickMovementViewModel: JoystickMovementViewModel = hiltViewModel()
        val cursorMode by ZLBridgeStates.cursorMode.collectAsStateWithLifecycle()
        val isGrabbing = remember(cursorMode) {
            cursorMode == CURSOR_DISABLED
        }

        LaunchedEffect(globalJoystick, isGrabbing) {
            if (globalJoystick != null && isGrabbing) {
                globalJoystick.onDirectionChanged = {
                    joystickMovementViewModel.onListen(it)
                }
            } else if (globalJoystick != null) {
                globalJoystick.onDirectionChanged = {}
            }
        }

        PreviewControlBox(
            modifier = Modifier.fillMaxSize(),
            observableLayout = observedLayout,
            previewScenario = viewModel.previewScenario,
            previewHideLayerWhen = viewModel.previewHideLayerWhen
        )
    } else {
        ControlEditorLayer(
            observedLayout = viewModel.observableLayout,
            selectedWidget = viewModel.selectedWidget?.data,
            onButtonTap = { data, layer ->
                val current = viewModel.selectedWidget?.data
                viewModel.selectedWidget = SelectedWidgetData(data, layer)
                if (current == data) {
                    //选中后再点击一次，打开编辑菜单
                    viewModel.editorOperation = EditorOperation.SelectButton
                }
            },
            onBackgroundClick = {
                //点击背景层时清除选中的控件
                viewModel.selectedWidget = null
            },
            floatingButtons = {
                //设置属性
                ActionButton(
                    painter = painterResource(R.drawable.ic_settings_filled),
                    text = stringResource(R.string.generic_setting),
                    onClick = {
                        if (viewModel.selectedWidget != null) {
                            viewModel.editorOperation = EditorOperation.SelectButton
                        }
                    }
                )
                //复制控件
                val widget = viewModel.selectedWidget
                if (widget?.layer != null && widget.data !is ObservableJoystickData) {
                    ActionButton(
                        painter = painterResource(R.drawable.ic_file_copy_filled),
                        text = stringResource(R.string.control_editor_edit_dialog_clone_widget),
                        onClick = {
                            val data = widget.data
                            val layer = widget.layer
                            viewModel.editorWidgetOperation =
                                EditorWidgetOperation.CloneButton(data, layer)
                        }
                    )
                }
                //删除
                ActionButton(
                    painter = painterResource(R.drawable.ic_delete_filled),
                    text = stringResource(R.string.generic_delete),
                    onClick = {
                        val data = widget?.data ?: return@ActionButton
                        val layer = widget.layer
                        viewModel.editorWidgetOperation =
                            EditorWidgetOperation.DeleteButton(data, layer)
                    }
                )
            },
            enableSnap = AllSettings.editorEnableWidgetSnap.state,
            snapInAllLayers = AllSettings.editorSnapInAllLayers.state,
            snapMode = AllSettings.editorWidgetSnapMode.state,
            focusedLayer = viewModel.selectedLayer?.takeIf { viewModel.isLayerFocus },
            isDark = isLauncherInDarkTheme()
        )
    }

    EditorMenu(
        state = viewModel.editorMenu,
        closeScreen = { viewModel.editorMenu = MenuState.HIDE },
        layers = layers,
        onReorder = { from, to ->
            viewModel.observableLayout.reorder(from, to)
        },
        selectedLayer = viewModel.selectedLayer,
        onLayerSelected = { layer ->
            viewModel.selectedLayer = layer
        },
        createLayer = {
            val newLayer = viewModel.observableLayout.addLayer(
                layer = createNewLayer(defaultLayerName = defaultLayerName)
            )
            viewModel.editorOperation = EditorOperation.EditLayer(newLayer)
        },
        onAttribute = { layer ->
            viewModel.editorOperation = EditorOperation.EditLayer(layer)
        },
        onHideSwitch = { layer ->
            layer.editorHide = layer.editorHide.not()
            if (layer.editorHide && viewModel.selectedWidget?.layer == layer) {
                viewModel.selectedWidget = null
            }
        },
        addNewButton = {
            viewModel.addWidget(layers) { layer ->
                layer.addNormalButton(
                    createWidgetWithUUID { uuid ->
                        NormalData(
                            text = createTranslatable(default = defaultButtonName),
                            uuid = uuid,
                            position = CenterPosition,
                            buttonSize = createAdaptiveButtonSize(
                                referenceLength = screenSize.height,
                                density = density.density
                            ),
                            visibilityType = VisibilityType.ALWAYS,
                            isSwipple = false,
                            isPenetrable = false,
                            isToggleable = false
                        )
                    }
                )
            }
        },
        addNewText = {
            viewModel.addWidget(layers) { layer ->
                layer.addTextBox(
                    createWidgetWithUUID { uuid ->
                        TextData(
                            text = createTranslatable(default = defaultTextName),
                            uuid = uuid,
                            position = CenterPosition,
                            buttonSize = createAdaptiveButtonSize(
                                referenceLength = screenSize.height,
                                density = density.density,
                                type = ButtonSize.Type.WrapContent //文本框默认使用包裹内容
                            ),
                            visibilityType = VisibilityType.ALWAYS
                        )
                    }
                )
            }
        },
        openStyleList = {
            viewModel.editorOperation = EditorOperation.OpenStyleList
        },
        hasJoystick = globalJoystick != null,
        onEditJoystickStyle = {
            if (globalJoystick == null) {
                viewModel.addWidget(layers) { layer ->
                    layer.addJoystick(DefaultJoystickData)
                }
            } else {
                val layer = layers.find { it.joysticks.value.contains(globalJoystick) }
                viewModel.selectedWidget = SelectedWidgetData(globalJoystick, layer)
                viewModel.editorOperation = EditorOperation.SelectButton
            }
        },
        isLayerFocus = viewModel.isLayerFocus,
        onLayerFocusChanged = { viewModel.isLayerFocus = it },
        isPreviewMode = viewModel.isPreviewMode,
        onPreviewChanged = { mode ->
            viewModel.applyEditorHide()
            viewModel.isPreviewMode = mode
        },
        previewScenario = viewModel.previewScenario,
        onPreviewScenarioChanged = { scenario ->
            viewModel.previewScenario = scenario
        },
        previewHideLayerWhen = viewModel.previewHideLayerWhen,
        onPreviewHideLayerChanged = { hideWhen ->
            viewModel.previewHideLayerWhen = hideWhen
        },
        onSave = {
            viewModel.save(targetFile, onSaved = {})
        },
        saveAndExit = {
            viewModel.save(targetFile, onSaved = exit)
        },
        onExit = menuExit
    )

    MenuBox(
        position = viewModel.editorBallPosition,
        onPositionChanged = { viewModel.editorBallPosition = it },
        opened = viewModel.editorMenu == MenuState.SHOW
    ) {
        viewModel.switchMenu()
    }

    EditWidgetDialog(
        data = viewModel.selectedWidget,
        visible = viewModel.editorOperation == EditorOperation.SelectButton,
        styles = styles,
        onDismissRequest = {
            viewModel.editorOperation = EditorOperation.None
        },
        onDelete = { data, layer ->
            viewModel.editorWidgetOperation = EditorWidgetOperation.DeleteButton(data, layer)
        },
        onClone = { data, layer ->
            viewModel.editorWidgetOperation = EditorWidgetOperation.CloneButton(data, layer)
        },
        onEditWidgetText = { string ->
            viewModel.editorWidgetOperation = EditorWidgetOperation.EditWidgetText(string)
        },
        switchControlLayers = { data, type ->
            viewModel.editorWidgetOperation =
                EditorWidgetOperation.SwitchLayersVisibility(data, type)
        },
        sendText = { data ->
            viewModel.editorWidgetOperation = EditorWidgetOperation.SendText(data)
        },
        openStyleList = {
            viewModel.editorOperation = EditorOperation.OpenStyleList
        }
    )

    EditButtonStyleDialog(
        visible = viewModel.editorOperation == EditorOperation.EditButtonStyle,
        style = viewModel.selectedStyle,
        onClose = {
            viewModel.editorOperation = EditorOperation.None
        }
    )

    EditorOperation(
        operation = viewModel.editorOperation,
        changeOperation = { viewModel.editorOperation = it },
        onDeleteLayer = { layer ->
            val isWidgetLayer = viewModel.selectedWidget?.layer == layer
            viewModel.removeLayer(layer)
            if (isWidgetLayer) {
                viewModel.selectedWidget = null
            }
        },
        onMergeDownward = { layer ->
            viewModel.observableLayout.mergeDownward(layer)
        },
        onCopy = { layer ->
            val baseLayer = layer.pack()
            val newLayer = viewModel.observableLayout.addLayer(
                layer = createNewLayer(
                    defaultLayerName = defaultLayerName
                ).copy(
                    hide = baseLayer.hide,
                    hideWhenMouse = baseLayer.hideWhenMouse,
                    hideWhenGamepad = baseLayer.hideWhenGamepad,
                    visibilityType = baseLayer.visibilityType,
                    normalButtons = baseLayer.normalButtons,
                    textBoxes = baseLayer.textBoxes
                )
            )
            viewModel.editorOperation = EditorOperation.EditLayer(newLayer)
        },
        onHideChange = { hide, layer ->
            if (hide && viewModel.selectedWidget?.layer == layer) {
                viewModel.selectedWidget = null
            }
        },
        onEditStyle = { style ->
            viewModel.selectedStyle = style
            viewModel.editorOperation = EditorOperation.EditButtonStyle
        },
        onCreateStyle = { name ->
            viewModel.createNewStyle(name)
        },
        onCloneStyle = { style ->
            viewModel.cloneStyle(style)
        },
        onDeleteStyle = { style ->
            viewModel.removeStyle(style)
        },
        styles = styles
    )

    EditorWidgetOperation(
        operation = viewModel.editorWidgetOperation,
        changeOperation = { viewModel.editorWidgetOperation = it },
        controlLayers = layers,
        onCloneWidgets = { widget, layers ->
            viewModel.cloneWidgetToLayers(widget, layers)
        },
        onDeleteWidget = { widget, layer ->
            viewModel.removeWidget(layer, widget)
            viewModel.selectedWidget = null
            viewModel.editorOperation = EditorOperation.None
        }
    )

    EditorWarningOperation(
        operation = viewModel.editorWarningOperation,
        changeOperation = { viewModel.editorWarningOperation = it }
    )
}

@Composable
private fun ActionButton(
    painter: Painter,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.semantics { role = Role.Button },
        shape = ButtonDefaults.shape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(all = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(20.dp),
                painter = painter,
                contentDescription = text
            )
            Text(
                modifier = Modifier.padding(end = 6.dp),
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun EditorOperation(
    operation: EditorOperation,
    changeOperation: (EditorOperation) -> Unit,
    onDeleteLayer: (ObservableControlLayer) -> Unit,
    onMergeDownward: (ObservableControlLayer) -> Unit,
    onCopy: (ObservableControlLayer) -> Unit,
    onHideChange: (Boolean, ObservableControlLayer) -> Unit,
    onEditStyle: (ObservableButtonStyle) -> Unit,
    onCreateStyle: (name: String) -> Unit,
    onCloneStyle: (ObservableButtonStyle) -> Unit,
    onDeleteStyle: (ObservableButtonStyle) -> Unit,
    styles: List<ObservableButtonStyle>
) {
    when (operation) {
        is EditorOperation.None,
        is EditorOperation.SelectButton,
        is EditorOperation.EditButtonStyle -> {}

        is EditorOperation.EditLayer -> {
            val layer = operation.layer
            EditControlLayerDialog(
                layer = layer,
                onDismissRequest = {
                    changeOperation(EditorOperation.None)
                },
                onDelete = {
                    changeOperation(EditorOperation.DeleteLayer(layer))
                },
                onMergeDownward = {
                    onMergeDownward(layer)
                },
                onCopy = {
                    onCopy(layer)
                },
                onHideChange = { value ->
                    onHideChange(value, layer)
                },
            )
        }

        is EditorOperation.DeleteLayer -> {
            val layer = operation.layer
            SimpleAlertDialog(
                title = stringResource(R.string.generic_delete),
                text = stringResource(R.string.control_editor_layers_delete, layer.name),
                onDismiss = {
                    changeOperation(EditorOperation.None)
                },
                onConfirm = {
                    onDeleteLayer(layer)
                    changeOperation(EditorOperation.None)
                }
            )
        }

        is EditorOperation.OpenStyleList -> {
            StyleListDialog(
                styles = styles,
                onEditStyle = onEditStyle,
                onCreate = {
                    changeOperation(EditorOperation.CreateStyle)
                },
                onClone = { style ->
                    onCloneStyle(style)
                },
                onDelete = { style ->
                    changeOperation(EditorOperation.DeleteButtonStyle(style))
                },
                onClose = {
                    changeOperation(EditorOperation.None)
                }
            )
        }

        is EditorOperation.CreateStyle -> {
            var name by remember { mutableStateOf("") }
            SimpleEditDialog(
                title = stringResource(R.string.control_editor_edit_style_config_name),
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                onDismissRequest = {
                    changeOperation(EditorOperation.None)
                },
                onConfirm = {
                    onCreateStyle(name)
                    changeOperation(EditorOperation.OpenStyleList)
                }
            )
        }

        is EditorOperation.DeleteButtonStyle -> {
            val style = operation.style
            SimpleAlertDialog(
                title = stringResource(R.string.generic_delete),
                text = stringResource(R.string.control_editor_edit_style_config_delete, style.name),
                onDismiss = {
                    changeOperation(EditorOperation.None)
                },
                onConfirm = {
                    onDeleteStyle(style)
                    changeOperation(EditorOperation.None)
                }
            )
        }

        is EditorOperation.Saving -> {
            ProgressDialog(
                title = stringResource(R.string.control_manage_saving)
            )
        }

        is EditorOperation.SaveFailed -> {
            SimpleAlertDialog(
                title = stringResource(R.string.control_manage_failed_to_save),
                text = operation.error.getMessageOrToString()
            ) {
                changeOperation(EditorOperation.None)
            }
        }
    }
}

@Composable
private fun EditorWidgetOperation(
    operation: EditorWidgetOperation,
    changeOperation: (EditorWidgetOperation) -> Unit,
    controlLayers: List<ObservableControlLayer>,
    onCloneWidgets: (ObservableWidget, List<ObservableControlLayer>) -> Unit,
    onDeleteWidget: (ObservableWidget, ObservableControlLayer?) -> Unit,
) {
    when (operation) {
        is EditorWidgetOperation.None -> {}
        is EditorWidgetOperation.CloneButton -> {
            val data = operation.data
            val layer = operation.layer
            SelectLayers(
                layers = controlLayers,
                initLayer = layer,
                onDismissRequest = {
                    changeOperation(EditorWidgetOperation.None)
                },
                title = stringResource(R.string.control_editor_edit_dialog_clone_widget_title),
                confirmText = stringResource(R.string.control_editor_edit_dialog_clone_widget),
                onConfirm = { layers ->
                    onCloneWidgets(data, layers)
                    changeOperation(EditorWidgetOperation.None)
                }
            )
        }

        is EditorWidgetOperation.DeleteButton -> {
            val data = operation.data
            val layer = operation.layer
            SimpleAlertDialog(
                title = stringResource(R.string.generic_delete),
                text = stringResource(R.string.control_editor_edit_dialog_delete_widget),
                onDismiss = {
                    changeOperation(EditorWidgetOperation.None)
                },
                onConfirm = {
                    onDeleteWidget(data, layer)
                    changeOperation(EditorWidgetOperation.None)
                }
            )
        }

        is EditorWidgetOperation.EditWidgetText -> {
            EditTranslatableTextDialog(
                text = operation.string,
                singleLine = false,
                onClose = {
                    changeOperation(EditorWidgetOperation.None)
                }
            )
        }

        is EditorWidgetOperation.SendText -> {
            val data = operation.data
            //文本内容
            var value by remember {
                mutableStateOf(
                    data.clickEvents.find { it.type == ClickEvent.Type.SendText }?.key ?: ""
                )
            }
            SimpleEditDialog(
                title = stringResource(R.string.control_editor_edit_event_launcher_send_text),
                value = value,
                onValueChange = { new ->
                    value = new
                },
                extraBody = {
                    Text(
                        text = stringResource(R.string.control_editor_edit_event_launcher_send_text_summary),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                label = {
                    Text(text = stringResource(R.string.control_editor_edit_event_launcher_send_text_hint))
                },
                singleLine = true,
                onConfirm = {
                    //清除所有发送文本事件，如果文本不为空则再添加
                    data.removeAllEvent(ClickEvent.Type.SendText)
                    if (value.isNotEmpty()) {
                        data.addEvent(ClickEvent(ClickEvent.Type.SendText, value))
                    }
                    changeOperation(EditorWidgetOperation.None)
                }
            )
        }

        is EditorWidgetOperation.SwitchLayersVisibility -> {
            val data = operation.data
            val type = operation.type
            EditSwitchLayersVisibilityDialog(
                data = data,
                layers = controlLayers,
                type = type,
                onDismissRequest = {
                    changeOperation(EditorWidgetOperation.None)
                }
            )
        }
    }
}

@Composable
private fun EditorWarningOperation(
    operation: EditorWarningOperation,
    changeOperation: (EditorWarningOperation) -> Unit
) {
    when (operation) {
        is EditorWarningOperation.None -> {}
        is EditorWarningOperation.WarningNoLayers -> {
            SimpleAlertDialog(
                title = stringResource(R.string.control_editor_menu_no_layers_title),
                text = stringResource(R.string.control_editor_menu_no_layers_message)
            ) {
                changeOperation(EditorWarningOperation.None)
            }
        }

        is EditorWarningOperation.WarningNoSelectLayer -> {
            SimpleAlertDialog(
                title = stringResource(R.string.control_editor_menu_no_selected_layer_title),
                text = stringResource(R.string.control_editor_menu_no_selected_layer_message)
            ) {
                changeOperation(EditorWarningOperation.None)
            }
        }
    }
}
