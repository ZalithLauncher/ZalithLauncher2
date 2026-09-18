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

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.layer_controller.data.lang.createTranslatable
import com.movtery.layer_controller.layout.ControlLayout
import com.movtery.layer_controller.layout.EmptyControlLayout
import com.movtery.layer_controller.layout.EmptyLayoutInfo
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.layer_controller.observable.ObservableTranslatableString
import com.movtery.layer_controller.utils.AUTHOR_NAME_LENGTH
import com.movtery.layer_controller.utils.NAME_LENGTH
import com.movtery.layer_controller.utils.VERSION_NAME_LENGTH
import com.movtery.layer_controller.utils.newRandomFileName
import com.movtery.layer_controller.utils.saveToFile
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.control.ControlData
import com.movtery.zalithlauncher.game.control.ControlManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.activities.startEditorActivity
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedRow
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.components.EdgeDirection
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.components.SingleLineTextCheck
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.ImportMultipleFileButton
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_translatable.EditTranslatableTextDialog
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.file.shareFile
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.Locale

private sealed interface ControlOperation {
    data object None : ControlOperation
    /** 创建新布局弹窗 */
    data object CreateNew : ControlOperation
    /** 删除控制布局 */
    data class Delete(val data: ControlData) : ControlOperation
    /** 编辑普通的文本 */
    data class EditText(
        val data: ControlData,
        val string: ObservableTranslatableString,
        val type: EditTextType
    ) : ControlOperation
    /** 编辑描述 */
    data class EditDescription(val data: ControlData) : ControlOperation
    /** 编辑版本名称 */
    data class EditVersion(val data: ControlData) : ControlOperation
}

private enum class EditTextType(val length: Int, val titleRes: Int, val allowEmpty: Boolean) {
    NAME(length = NAME_LENGTH, titleRes = R.string.control_manage_create_new_name, false),
    AUTHOR(length = AUTHOR_NAME_LENGTH, titleRes = R.string.control_manage_create_new_author, true)
}

private class ControlViewModel : ViewModel() {
    var operation by mutableStateOf<ControlOperation>(ControlOperation.None)

    fun createNew(
        layout: ControlLayout,
        submitError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            saveToNew(layout, submitError)
        }
    }

    fun copyNew(
        layout: ObservableControlLayout,
        submitError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            saveToNew(layout.pack(), submitError)
        }
    }

    private suspend fun saveToNew(
        layout: ControlLayout,
        submitError: (Exception) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val file = File(PathManager.DIR_CONTROL_LAYOUTS, "${newRandomFileName()}.json")
            try {
                layout.saveToFile(file)
            } catch (e: Exception) {
                submitError(e)
                FileUtils.deleteQuietly(file)
            }
            ControlManager.refresh()
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
    }
}

@Composable
private fun rememberControlViewModel() = viewModel(
    key = NormalNavKey.Settings.ControlManager.toString()
) {
    ControlViewModel()
}

@Composable
fun ControlManageScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val viewModel = rememberControlViewModel()
    val dataList by ControlManager.dataList.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]

    ControlOperation(
        operation = viewModel.operation,
        changeOperation = { viewModel.operation = it },
        onCreate = { name, author, versionName ->
            val layout = EmptyControlLayout.copy(
                info = EmptyLayoutInfo.copy(
                    name = createTranslatable(default = name),
                    author = createTranslatable(default = author),
                    versionName = versionName
                )
            )
            viewModel.createNew(layout) { e ->
                submitError(
                    ErrorViewModel.ThrowableMessage(
                        title = androidText(R.string.control_manage_failed_to_save),
                        message = androidText(e.getMessageOrToString())
                    )
                )
            }
        },
        onDelete = { data ->
            ControlManager.deleteControl(data)
        },
        onSave = { data ->
            ControlManager.saveControl(data) { e ->
                ErrorViewModel.ThrowableMessage(
                    title = androidText(R.string.control_manage_failed_to_save),
                    message = androidText(e.getMessageOrToString())
                )
            }
        }
    )

    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.ControlManager, settingsScreenKey, false)
    ) { isVisible ->
        val selectedLayout by ControlManager.selectedLayout.collectAsStateWithLifecycle()
        val isRefreshing by ControlManager.isRefreshing.collectAsStateWithLifecycle()

        AnimatedRow(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 12.dp),
            isVisible = isVisible
        ) { scope ->
            AnimatedItem(scope) { xOffset ->
                ControlLayoutList(
                    modifier = Modifier
                        .weight(0.5f)
                        .offset { IntOffset(x = xOffset.roundToPx(), y = 0) },
                    dataList = dataList,
                    locale = locale,
                    isLoading = isRefreshing,
                    onRefresh = {
                        ControlManager.refresh()
                    },
                    onCreate = {
                        viewModel.operation = ControlOperation.CreateNew
                    },
                    onCopy = { data ->
                        viewModel.copyNew(data.controlLayout) { e ->
                            submitError(
                                ErrorViewModel.ThrowableMessage(
                                    title = androidText(R.string.control_manage_failed_to_save),
                                    message = androidText(e.getMessageOrToString())
                                )
                            )
                        }
                    },
                    onDelete = { data ->
                        viewModel.operation = ControlOperation.Delete(data)
                    },
                    eventViewModel = eventViewModel,
                )
            }

            AnimatedItem(scope) { xOffset ->
                ControlLayoutInfo(
                    modifier = Modifier
                        .weight(0.5f)
                        .offset { IntOffset(x = xOffset.roundToPx(), y = 0) },
                    isLoading = isRefreshing,
                    data = selectedLayout,
                    locale = locale,
                    onShareLayout = { data ->
                        shareFile(context, data.file)
                    },
                    onEditLayout = { data ->
                        startEditorActivity(context, data.file)
                    },
                    onEditText = { data, string, type ->
                        viewModel.operation = ControlOperation.EditText(data, string, type)
                    },
                    onEditDescription = { data ->
                        viewModel.operation = ControlOperation.EditDescription(data)
                    },
                    onEditVersion = { data ->
                        viewModel.operation = ControlOperation.EditVersion(data)
                    }
                )
            }
        }
    }
}

/**
 * 控制布局相关操作
 */
@Composable
private fun ControlOperation(
    operation: ControlOperation,
    changeOperation: (ControlOperation) -> Unit,
    onCreate: (name: String, author: String, versionName: String) -> Unit,
    onDelete: (ControlData) -> Unit,
    onSave: (ControlData) -> Unit
) {
    when (operation) {
        is ControlOperation.None -> {}
        is ControlOperation.CreateNew -> {
            CreateNewLayoutDialog(
                onDismissRequest = { changeOperation(ControlOperation.None) },
                onCreate = onCreate
            )
        }
        is ControlOperation.Delete -> {
            val data = operation.data
            val layoutName = if (data.isSupport) {
                data.controlLayout.info.name.translate()
            } else {
                data.file.name
            }
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.control_manage_delete_message, layoutName),
                onDismiss = {
                    changeOperation(ControlOperation.None)
                },
                onConfirm = {
                    onDelete(data)
                    changeOperation(ControlOperation.None)
                }
            )
        }
        is ControlOperation.EditText -> {
            EditTranslatableTextDialog(
                title = stringResource(operation.type.titleRes),
                text = operation.string,
                onDismissRequest = {
                    operation.string.reset()
                    changeOperation(ControlOperation.None)
                },
                onClose = {
                    if (!operation.type.allowEmpty) {
                        if (operation.string.default.isEmptyOrBlank()) return@EditTranslatableTextDialog
                        if (operation.string.matchQueue.any { it.value.isEmptyOrBlank() }) return@EditTranslatableTextDialog
                    }
                    onSave(operation.data)
                    changeOperation(ControlOperation.None)
                },
                allowEmpty = operation.type.allowEmpty,
                closeText = stringResource(R.string.generic_save),
                take = operation.type.length
            )
        }
        is ControlOperation.EditDescription -> {
            val string = operation.data.controlLayout.info.description
            EditTranslatableTextDialog(
                title = stringResource(R.string.control_manage_info_description),
                text = string,
                singleLine = false,
                onDismissRequest = {
                    string.reset()
                    changeOperation(ControlOperation.None)
                },
                onClose = {
                    onSave(operation.data)
                    changeOperation(ControlOperation.None)
                },
                closeText = stringResource(R.string.generic_save)
            )
        }
        is ControlOperation.EditVersion -> {
            val info = operation.data.controlLayout.info
            SimpleEditDialog(
                title = stringResource(R.string.control_manage_create_new_version_name),
                value = info.versionName,
                onValueChange = {
                    info.versionName = it.take(VERSION_NAME_LENGTH)
                },
                supportingText = {
                    Text(stringResource(R.string.generic_input_length, info.versionName.length, VERSION_NAME_LENGTH))
                },
                singleLine = true,
                onDismissRequest = {
                    info.resetVersionName()
                    changeOperation(ControlOperation.None)
                },
                onConfirm = {
                    onSave(operation.data)
                    changeOperation(ControlOperation.None)
                }
            )
        }
    }
}

/**
 * 左侧：控制布局展示列表
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ControlLayoutList(
    modifier: Modifier = Modifier,
    dataList: List<ControlData>,
    locale: Locale,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onCreate: () -> Unit,
    onCopy: (ControlData) -> Unit,
    onDelete: (ControlData) -> Unit,
    eventViewModel: EventViewModel,
) {
    BackgroundCard(
        modifier = modifier.fillMaxHeight(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        } else {
            ControlListHeader(
                modifier = Modifier.fillMaxWidth(),
                onRefresh = onRefresh,
                onCreate = onCreate,
                eventViewModel = eventViewModel,
            )

            if (dataList.isNotEmpty()) {
                val scrollState = rememberLazyListState()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .nonInteractiveScrollbar(
                            state = scrollState.scrollIndicatorState!!,
                            orientation = Orientation.Vertical,
                        ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    state = scrollState,
                ) {
                    items(dataList) { data ->
                        ControlLayoutItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            data = data,
                            locale = locale,
                            selected = data.file.name == AllSettings.controlLayout.state,
                            onSelected = { ControlManager.selectControl(data) },
                            onCopy = { onCopy(data) },
                            onDelete = { onDelete(data) }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ScalingLabel(text = stringResource(R.string.control_manage_list_empty))
                }
            }
        }
    }
}

/**
 * 左侧：控制布局列表顶部操作栏
 */
@Composable
private fun ControlListHeader(
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    onCreate: () -> Unit,
    eventViewModel: EventViewModel,
) {
    CardTitleLayout {
        val scrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .fadeEdge(
                    state = scrollState,
                    length = 32.dp,
                    direction = EdgeDirection.Horizontal
                )
                .then(
                    modifier
                        .horizontalScroll(state = scrollState)
                        .padding(all = 8.dp)
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTextButton(
                onClick = onRefresh,
                painter = painterResource(R.drawable.ic_refresh),
                contentDescription = stringResource(R.string.generic_refresh),
                text = stringResource(R.string.generic_refresh),
            )
            ImportMultipleFileButton(
                extension = "json",
                progressUris = { uris ->
                    eventViewModel.sendEvent(EventViewModel.Event.ImportControls(uris))
                }
            )
            IconTextButton(
                onClick = onCreate,
                painter = painterResource(R.drawable.ic_add_box_outlined),
                contentDescription = stringResource(R.string.control_manage_create_new),
                text = stringResource(R.string.control_manage_create_new),
            )
        }
    }
}

/**
 * 控制布局单项外观
 */
@Composable
private fun ControlLayoutItem(
    modifier: Modifier = Modifier,
    data: ControlData,
    locale: Locale,
    selected: Boolean,
    onSelected: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    color: Color = itemColor(),
    contentColor: Color = onItemColor(),
) {
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }
    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        onClick = {
            if (selected) return@Surface
            onSelected()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = MaterialTheme.shapes.large)
                .padding(all = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                enabled = data.isSupport,
                onClick = {
                    if (selected) return@RadioButton
                    onSelected()
                }
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                val info = data.controlLayout.info
                Row(
                    modifier = modifier.height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MarqueeText(
                        modifier = Modifier.weight(1f, fill = false),
                        text = if (data.isSupport) info.name.translate(locale) else data.file.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (data.isSupport) {
                    if (!info.versionName.isEmptyOrBlank()) {
                        MarqueeText(
                            text = info.versionName,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    MarqueeText(
                        text = stringResource(R.string.control_manage_info_unsupport),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            //复制
            IconButton(
                onClick = onCopy
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_copy_all_outlined),
                    contentDescription = stringResource(R.string.generic_copy)
                )
            }
            //删除
            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete_outlined),
                    contentDescription = stringResource(R.string.generic_delete)
                )
            }
        }
    }
}

/**
 * 右侧：控制布局详细信息
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ControlLayoutInfo(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    data: ControlData? = null,
    locale: Locale,
    onShareLayout: (ControlData) -> Unit,
    onEditLayout: (ControlData) -> Unit,
    onEditText: (ControlData, ObservableTranslatableString, type: EditTextType) -> Unit,
    onEditDescription: (ControlData) -> Unit,
    onEditVersion: (ControlData) -> Unit
) {
    BackgroundCard(
        modifier = modifier.fillMaxHeight(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        } else if (data == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ScalingLabel(text = stringResource(R.string.control_manage_info_empty))
            }
        } else if (!data.isSupport) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ScalingLabel(text = stringResource(R.string.control_manage_info_unsupport))
            }
        } else {
            val info = data.controlLayout.info
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(all = 12.dp)
            ) {
                item {
                    val name = info.name.translate(locale)
                    ControlInfoItem(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.control_manage_create_new_name),
                        value = if (name.isEmptyOrBlank()) stringResource(R.string.generic_unspecified) else name,
                        onEdit = {
                            onEditText(data, info.name, EditTextType.NAME)
                        }
                    )
                }

                item {
                    val author = info.author.translate(locale)
                    ControlInfoItem(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.control_manage_create_new_author),
                        value = if (author.isEmptyOrBlank()) stringResource(R.string.generic_unspecified) else author,
                        onEdit = {
                            onEditText(data, info.author, EditTextType.AUTHOR)
                        }
                    )
                }

                item {
                    val versionName = info.versionName
                    ControlInfoItem(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.control_manage_create_new_version_name),
                        value = if (versionName.isEmptyOrBlank()) stringResource(R.string.generic_unspecified) else versionName,
                        onEdit = {
                            onEditVersion(data)
                        }
                    )
                }

                val description = info.description.translate(locale)
                if (description.isEmptyOrBlank()) {
                    item {
                        ControlInfoItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(R.string.control_manage_info_description),
                            value = stringResource(R.string.control_manage_info_description_empty),
                            onEdit = {
                                onEditDescription(data)
                            }
                        )
                    }
                } else {
                    item {
                        Spacer(Modifier.height(4.dp))
                        ControlInfoItem(
                            modifier = Modifier.fillMaxWidth(),
                            onEdit = {
                                onEditDescription(data)
                            }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.control_manage_info_description),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                HorizontalDivider(
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ScalingActionButton(
                    modifier = Modifier
                        .weight(1f, fill = false),
                    onClick = { onShareLayout(data) }
                ) {
                    MarqueeText(
                        text = stringResource(R.string.generic_share)
                    )
                }

                ScalingActionButton(
                    modifier = Modifier
                        .weight(1f, fill = false),
                    onClick = { onEditLayout(data) }
                ) {
                    MarqueeText(
                        text = stringResource(R.string.control_manage_info_edit)
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlInfoItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    onEdit: () -> Unit,
    color: Color = itemColor(),
    contentColor: Color = onItemColor(),
) {
    ControlInfoItem(
        modifier = modifier,
        onEdit = onEdit,
        color = color,
        contentColor = contentColor,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        MarqueeText(
            modifier = Modifier.weight(1f),
            text = value,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ControlInfoItem(
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    color: Color = itemColor(),
    contentColor: Color = onItemColor(),
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = MaterialTheme.shapes.large)
                .padding(all = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}


/**
 * 创建新控制布局对话框
 */
@Composable
private fun CreateNewLayoutDialog(
    onDismissRequest: () -> Unit,
    onCreate: (name: String, author: String, versionName: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var versionName by remember { mutableStateOf("1.0") }

    val blankError = stringResource(R.string.control_manage_create_new_field_blank)
    val longError = stringResource(R.string.control_manage_create_new_field_long)

    var nameError by remember { mutableStateOf<String?>(null) }
    val isNameError = remember(name) {
        nameError = when {
            name.isEmptyOrBlank() -> blankError
            name.length > NAME_LENGTH -> longError
            else -> null
        }
        nameError != null
    }
    var authorNameError by remember { mutableStateOf<String?>(null) }
    val isAuthorNameError = remember(author) {
        authorNameError = when {
//            author.isEmptyOrBlank() -> blankError
            author.length > AUTHOR_NAME_LENGTH -> longError
            else -> null
        }
        authorNameError != null
    }
    var versionNameError by remember { mutableStateOf<String?>(null) }
    val isVersionNameError = remember(versionName) {
        versionNameError = when {
//            versionName.isEmptyOrBlank() -> blankError
            versionName.length > VERSION_NAME_LENGTH -> longError
            else -> null
        }
        versionNameError != null
    }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .heightIn(max = rememberDialogMaxHeight())
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 6.dp)
                    .heightIn(max = (maxHeight - 12.dp).coerceAtMost(rememberDialogMaxHeight()))
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                color = cardColor(false),
                contentColor = onCardColor(),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.control_manage_create_new_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .weight(1f, fill = false)
                            .verticalScrollWithBar(state = scrollState)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val focusManager = LocalFocusManager.current
                        val authorFocus = remember { FocusRequester() }
                        val versionNameFocus = remember { FocusRequester() }

                        SingleLineTextCheck(
                            text = name,
                            onSingleLined = { name = it }
                        )

                        //名称
                        OwnOutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = name,
                            onValueChange = {
                                name = it
                            },
                            label = {
                                Text(text = stringResource(R.string.control_manage_create_new_name))
                            },
                            isError = isNameError,
                            supportingText = {
                                nameError?.let { Text(it) } ?: run {
                                    Text(stringResource(R.string.generic_input_length, name.length, NAME_LENGTH))
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    authorFocus.requestFocus()
                                }
                            ),
                            shape = MaterialTheme.shapes.large
                        )

                        SingleLineTextCheck(
                            text = author,
                            onSingleLined = { author = it }
                        )

                        //作者
                        OwnOutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(authorFocus),
                            value = author,
                            onValueChange = {
                                author = it
                            },
                            label = {
                                Text(text = stringResource(R.string.control_manage_create_new_author))
                            },
                            isError = isAuthorNameError,
                            supportingText = {
                                authorNameError?.let { Text(it) } ?: run {
                                    Text(stringResource(R.string.generic_input_length, author.length, AUTHOR_NAME_LENGTH))
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    versionNameFocus.requestFocus()
                                }
                            ),
                            shape = MaterialTheme.shapes.large
                        )

                        SingleLineTextCheck(
                            text = versionName,
                            onSingleLined = { versionName = it }
                        )

                        //版本
                        OwnOutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(versionNameFocus),
                            value = versionName,
                            onValueChange = {
                                versionName = it
                            },
                            label = {
                                Text(text = stringResource(R.string.control_manage_create_new_version_name))
                            },
                            isError = isVersionNameError,
                            supportingText = {
                                versionNameError?.let { Text(it) } ?: run {
                                    Text(stringResource(R.string.generic_input_length, versionName.length, VERSION_NAME_LENGTH))
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus(true)
                                }
                            ),
                            shape = MaterialTheme.shapes.large
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = onDismissRequest
                        ) {
                            MarqueeText(text = stringResource(R.string.generic_cancel))
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onClick@{
                                if (isNameError || isAuthorNameError || isVersionNameError) return@onClick
                                onDismissRequest()
                                onCreate(name, author, versionName)
                            }
                        ) {
                            MarqueeText(text = stringResource(R.string.control_manage_create_new))
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun PreviewCreateNewLayoutDialog() {
    CreateNewLayoutDialog(
        onDismissRequest = {},
        onCreate = { _, _, _ -> }
    )
}