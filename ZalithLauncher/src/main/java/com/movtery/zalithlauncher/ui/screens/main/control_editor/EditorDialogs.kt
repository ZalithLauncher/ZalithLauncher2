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

import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableJoystickStyle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.control.CabinetStyleItem
import com.movtery.zalithlauncher.ui.components.ProgressDialog
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_joystick.EditJoystickStyleDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_joystick.JoystickStyleListDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_layer.EditControlLayerDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_layer.EditSwitchLayersVisibilityDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_style.EditButtonStyleDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_style.StyleListDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_translatable.EditTranslatableTextDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget.SelectLayers
import com.movtery.zalithlauncher.ui.screens.main.control_editor.style_cabinet.OpenCabinetSource
import com.movtery.zalithlauncher.ui.screens.main.control_editor.style_cabinet.StyleCabinetDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.style_cabinet.StyleConflictDialog
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.EditorViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlin.time.Duration.Companion.milliseconds

/**
 * 编辑器全部对话框的统一渲染入口
 *
 * 真 Dialog 由三个状态机渲染器按当前状态就地组合；
 * 假 Dialog（样式编辑器、样式收藏柜）常驻组合，以 visible 参数控制显隐以保证过渡动画连续。
 * 所有状态转移均通过 [EditorViewModel] 的 intent 函数完成，此处只读状态。
 */
@Composable
fun EditorDialogHost(viewModel: EditorViewModel) {
    val layers by viewModel.observableLayout.layers.collectAsStateWithLifecycle()
    val styles by viewModel.observableLayout.styles.collectAsStateWithLifecycle()
    val joystickStyles by viewModel.observableLayout.joystickStyles.collectAsStateWithLifecycle()
    val cabinetItems by viewModel.styleCabinet.items.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val editorToast: (Int) -> Unit = { resId ->
        Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show()
    }
    val favoriteResultToast: (Boolean) -> Unit = { ok ->
        editorToast(
            if (ok) R.string.control_editor_style_cabinet_favorited
            else R.string.control_manage_failed_to_save
        )
    }

    EditorOperationDialogs(
        viewModel = viewModel,
        styles = styles,
        joystickStyles = joystickStyles,
        favoriteResultToast = favoriteResultToast,
        editorToast = editorToast
    )

    EditorWidgetOperationDialogs(
        viewModel = viewModel,
        controlLayers = layers
    )

    EditorWarningOperationDialogs(viewModel)

    EditorStyleEditDialogs(viewModel)

    //样式收藏柜（假Dialog，常驻组合）
    StyleCabinetDialog(
        visible = viewModel.editorOperation is EditorOperation.OpenStyleCabinet,
        items = cabinetItems,
        onClose = viewModel::closeStyleCabinet,
        onEditItem = viewModel::editCabinetItem,
        onAddToLayout = { item ->
            viewModel.addCabinetStyleToLayout(item) {
                editorToast(R.string.control_editor_style_cabinet_added_to_layout)
            }
        },
        onDeleteItem = viewModel::deleteCabinetItem
    )
}

/**
 * 主操作状态机的真 Dialog 渲染
 */
@Composable
private fun EditorOperationDialogs(
    viewModel: EditorViewModel,
    styles: List<ObservableButtonStyle>,
    joystickStyles: List<ObservableJoystickStyle>,
    favoriteResultToast: (Boolean) -> Unit,
    editorToast: (Int) -> Unit
) {
    /** 复制控件层时新层的默认名称 */
    val defaultLayerName = stringResource(R.string.control_editor_edit_layer_default)

    when (val operation = viewModel.editorOperation) {
        is EditorOperation.None,
        is EditorOperation.SelectButton,
        is EditorOperation.EditButtonStyle,
        is EditorOperation.EditJoystickStyle,
        is EditorOperation.OpenStyleCabinet,
        is EditorOperation.EditCabinetButtonStyle,
        is EditorOperation.EditCabinetJoystickStyle -> {}

        is EditorOperation.EditLayer -> {
            val layer = operation.layer
            EditControlLayerDialog(
                layer = layer,
                onDismissRequest = viewModel::dismissOperation,
                onDelete = {
                    viewModel.requestDeleteLayer(layer)
                },
                onMergeDownward = {
                    viewModel.mergeLayerDownward(layer)
                },
                onCopy = {
                    viewModel.copyLayer(layer, defaultLayerName)
                },
                onHideChange = { value ->
                    viewModel.onLayerHideChanged(value, layer)
                },
            )
        }
        is EditorOperation.DeleteLayer -> {
            val layer = operation.layer
            SimpleAlertDialog(
                title = stringResource(R.string.generic_delete),
                text = stringResource(
                    R.string.control_editor_layers_delete,
                    layer.name.orUnspecified()
                ),
                onDismiss = viewModel::dismissOperation,
                onConfirm = {
                    viewModel.confirmDeleteLayer(layer)
                }
            )
        }
        is EditorOperation.OpenStyleList -> {
            StyleListDialog(
                styles = styles,
                onEditStyle = viewModel::editStyle,
                onCreate = viewModel::startCreateStyle,
                onClone = viewModel::cloneStyle,
                onDelete = viewModel::requestDeleteStyle,
                onFavorite = { style ->
                    viewModel.favoriteStyle(style, onResult = favoriteResultToast)
                },
                onOpenCabinet = {
                    viewModel.openStyleCabinet(OpenCabinetSource.ControlStyle)
                },
                onClose = viewModel::dismissOperation
            )
        }
        is EditorOperation.CreateStyle -> {
            var name by remember { mutableStateOf("") }
            SimpleEditDialog(
                title = stringResource(R.string.control_editor_edit_style_config_name),
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                onDismissRequest = viewModel::dismissOperation,
                onConfirm = {
                    viewModel.confirmCreateStyle(name)
                }
            )
        }
        is EditorOperation.DeleteButtonStyle -> {
            val style = operation.style
            SimpleAlertDialog(
                title = stringResource(R.string.generic_delete),
                text = stringResource(
                    R.string.control_editor_edit_style_config_delete,
                    style.name.orUnspecified()
                ),
                onDismiss = viewModel::dismissOperation,
                onConfirm = {
                    viewModel.confirmDeleteStyle(style)
                }
            )
        }
        is EditorOperation.DeleteJoystickStyle -> {
            val style = operation.style
            SimpleAlertDialog(
                title = stringResource(R.string.control_editor_special_joystick_style_delete_title),
                text = stringResource(
                    R.string.control_editor_edit_joystick_style_list_delete,
                    style.name.orUnspecified()
                ),
                confirmText = stringResource(R.string.generic_delete),
                onConfirm = {
                    viewModel.confirmDeleteJoystickStyle(style)
                },
                onDismiss = viewModel::dismissOperation
            )
        }
        is EditorOperation.OpenJoystickStyleList -> {
            JoystickStyleListDialog(
                styles = joystickStyles,
                onEditStyle = viewModel::editJoystickStyle,
                onCreate = viewModel::startCreateJoystickStyle,
                onClone = viewModel::cloneJoystickStyle,
                onDelete = viewModel::requestDeleteJoystickStyle,
                onFavorite = { style ->
                    viewModel.favoriteJoystickStyle(style, onResult = favoriteResultToast)
                },
                onOpenCabinet = {
                    viewModel.openStyleCabinet(OpenCabinetSource.JoystickStyle)
                },
                onClose = viewModel::dismissOperation
            )
        }
        is EditorOperation.CreateJoystickStyle -> {
            var name by remember { mutableStateOf("") }
            SimpleEditDialog(
                title = stringResource(R.string.control_editor_edit_joystick_style_list_name),
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                onDismissRequest = viewModel::dismissOperation,
                onConfirm = {
                    viewModel.confirmCreateJoystickStyle(name)
                }
            )
        }
        is EditorOperation.FavoriteButtonStyleConflict,
        is EditorOperation.FavoriteJoystickStyleConflict -> {
            StyleConflictDialog(
                title = stringResource(R.string.control_editor_style_cabinet_conflict_title),
                message = stringResource(R.string.control_editor_style_cabinet_conflict_message),
                primaryText = stringResource(R.string.control_editor_style_cabinet_conflict_overwrite),
                secondaryText = stringResource(R.string.control_editor_style_cabinet_conflict_independent),
                onPrimary = {
                    viewModel.resolveFavoriteConflict(independent = false, onResult = favoriteResultToast)
                },
                onSecondary = {
                    viewModel.resolveFavoriteConflict(independent = true, onResult = favoriteResultToast)
                },
                onCancel = viewModel::cancelFavoriteConflict
            )
        }
        is EditorOperation.AddToLayoutButtonStyleConflict,
        is EditorOperation.AddToLayoutJoystickStyleConflict -> {
            StyleConflictDialog(
                title = stringResource(R.string.control_editor_style_cabinet_add_conflict_title),
                message = stringResource(R.string.control_editor_style_cabinet_add_conflict_message),
                primaryText = stringResource(R.string.control_editor_style_cabinet_conflict_overwrite),
                secondaryText = stringResource(R.string.control_editor_style_cabinet_add_conflict_independent),
                onPrimary = {
                    viewModel.resolveAddToLayoutConflict(overwrite = true)
                    editorToast(R.string.control_editor_style_cabinet_added_to_layout)
                },
                onSecondary = {
                    viewModel.resolveAddToLayoutConflict(overwrite = false)
                    editorToast(R.string.control_editor_style_cabinet_added_to_layout)
                },
                onCancel = viewModel::cancelAddToLayoutConflict
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
                viewModel.dismissOperation()
            }
        }
    }
}

/**
 * 控件操作状态机的 Dialog 渲染
 */
@Composable
private fun EditorWidgetOperationDialogs(
    viewModel: EditorViewModel,
    controlLayers: List<ObservableControlLayer>
) {
    when (val operation = viewModel.editorWidgetOperation) {
        is EditorWidgetOperation.None -> {}
        is EditorWidgetOperation.CloneButton -> {
            val data = operation.data
            val layer = operation.layer
            SelectLayers(
                layers = controlLayers,
                initLayer = layer,
                onDismissRequest = viewModel::dismissWidgetOperation,
                title = stringResource(R.string.control_editor_edit_dialog_clone_widget_title),
                confirmText = stringResource(R.string.control_editor_edit_dialog_clone_widget),
                onConfirm = { layers ->
                    viewModel.confirmCloneWidgets(data, layers)
                }
            )
        }
        is EditorWidgetOperation.DeleteButton -> {
            val data = operation.data
            val layer = operation.layer
            SimpleAlertDialog(
                title = stringResource(R.string.generic_delete),
                text = stringResource(R.string.control_editor_edit_dialog_delete_widget),
                onDismiss = viewModel::dismissWidgetOperation,
                onConfirm = {
                    viewModel.confirmDeleteWidget(data, layer)
                }
            )
        }
        is EditorWidgetOperation.EditWidgetText -> {
            EditTranslatableTextDialog(
                text = operation.string,
                singleLine = false,
                onClose = viewModel::dismissWidgetOperation
            )
        }
        is EditorWidgetOperation.SendText -> {
            val data = operation.data
            //文本内容
            var value by remember {
                mutableStateOf(data.clickEvents.find { it.type == ClickEvent.Type.SendText }?.key ?: "")
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
                    viewModel.confirmSendText(data, value)
                }
            )
        }
        is EditorWidgetOperation.SwitchLayersVisibility -> {
            EditSwitchLayersVisibilityDialog(
                data = operation.data,
                layers = controlLayers,
                type = operation.type,
                onDismissRequest = viewModel::dismissWidgetOperation
            )
        }
    }
}

/**
 * 警告状态机的 Dialog 渲染
 */
@Composable
private fun EditorWarningOperationDialogs(viewModel: EditorViewModel) {
    when (viewModel.editorWarningOperation) {
        is EditorWarningOperation.None -> {}

        is EditorWarningOperation.WarningNoLayers -> {
            SimpleAlertDialog(
                title = stringResource(R.string.control_editor_menu_no_layers_title),
                text = stringResource(R.string.control_editor_menu_no_layers_message)
            ) {
                viewModel.dismissWarning()
            }
        }
        is EditorWarningOperation.WarningNoSelectLayer -> {
            SimpleAlertDialog(
                title = stringResource(R.string.control_editor_menu_no_selected_layer_title),
                text = stringResource(R.string.control_editor_menu_no_selected_layer_message)
            ) {
                viewModel.dismissWarning()
            }
        }
    }
}

/**
 * 样式编辑器渲染
 */
@Composable
private fun EditorStyleEditDialogs(viewModel: EditorViewModel) {
    val buttonEditTarget: ObservableButtonStyle? = when (val operation = viewModel.editorOperation) {
        is EditorOperation.EditButtonStyle -> operation.style
        is EditorOperation.EditCabinetButtonStyle -> operation.item.style
        else -> null
    }
    var lastButtonEditTarget by remember { mutableStateOf<ObservableButtonStyle?>(null) }
    LaunchedEffect(buttonEditTarget) {
        if (buttonEditTarget != null) lastButtonEditTarget = buttonEditTarget
    }

    EditButtonStyleDialog(
        visible = buttonEditTarget != null,
        style = buttonEditTarget ?: lastButtonEditTarget,
        onClose = viewModel::closeButtonStyleEdit
    )

    val joystickEditTarget: ObservableJoystickStyle? = when (val operation = viewModel.editorOperation) {
        is EditorOperation.EditJoystickStyle -> operation.style
        is EditorOperation.EditCabinetJoystickStyle -> operation.item.style
        else -> null
    }
    var lastJoystickEditTarget by remember { mutableStateOf<ObservableJoystickStyle?>(null) }
    LaunchedEffect(joystickEditTarget) {
        if (joystickEditTarget != null) lastJoystickEditTarget = joystickEditTarget
    }

    EditJoystickStyleDialog(
        visible = joystickEditTarget != null,
        style = joystickEditTarget ?: lastJoystickEditTarget,
        onClose = viewModel::closeJoystickStyleEdit,
    )

    //柜内样式编辑防抖保存，避免滑动条等高频修改触发频繁落盘
    (viewModel.editorOperation as? EditorOperation.EditCabinetButtonStyle)?.let { edit ->
        CabinetEditSaveEffect(item = edit.item) {
            viewModel.saveCabinetItem(edit.item)
        }
    }
    (viewModel.editorOperation as? EditorOperation.EditCabinetJoystickStyle)?.let { edit ->
        CabinetEditSaveEffect(item = edit.item) {
            viewModel.saveCabinetItem(edit.item)
        }
    }
}

/**
 * 柜内样式编辑会话的防抖保存
 */
@OptIn(FlowPreview::class)
@Composable
private fun CabinetEditSaveEffect(
    item: CabinetStyleItem,
    save: () -> Unit
) {
    LaunchedEffect(item) {
        snapshotFlow {
            when (item) {
                is CabinetStyleItem.Button -> item.style.pack()
                is CabinetStyleItem.Joystick -> item.style.pack()
            }
        }.drop(1)
            .debounce(500.milliseconds)
            .collectLatest { save() }
    }
}
