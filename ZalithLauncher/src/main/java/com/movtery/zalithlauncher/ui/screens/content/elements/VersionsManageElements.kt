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

package com.movtery.zalithlauncher.ui.screens.content.elements

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.GifDecoder
import coil3.svg.SvgDecoder
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.path.GamePath
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.version.installed.cleanup.CleanFailedException
import com.movtery.zalithlauncher.game.version.installed.cleanup.GameAssetCleaner
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleCheckEditDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.components.SimpleTaskDialog
import com.movtery.zalithlauncher.ui.components.TextRailItem
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.logging.Logger.lError
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.utils.string.isNotEmptyOrBlank
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.Dispatchers

sealed interface GamePathOperation {
    data object None: GamePathOperation
    data object PathExists: GamePathOperation
    data class AddNewPath(val path: String): GamePathOperation
    data class RenamePath(val item: GamePath): GamePathOperation
    data class DeletePath(val item: GamePath): GamePathOperation
}

sealed interface VersionsOperation {
    data object None: VersionsOperation
    data class Rename(val version: Version): VersionsOperation
    data class Copy(val version: Version): VersionsOperation
    data class Delete(val version: Version, val text: String? = null): VersionsOperation
    data class InvalidDelete(val version: Version): VersionsOperation
    data class RunTask(val title: Int, val task: suspend () -> Unit): VersionsOperation
}

sealed interface CleanupOperation {
    data object None : CleanupOperation
    /** 清理前的提醒 */
    data object Tip : CleanupOperation
    /** 开始清理 */
    data object Clean : CleanupOperation
    /** 清理失败 */
    data class Error(val error: Throwable) : CleanupOperation
    /** 成功清除 */
    data class Success(val count: Int, val size: String) : CleanupOperation
}

enum class VersionCategory(val textRes: Int) {
    /** 全部 */
    ALL(R.string.generic_all),
    /** 原版 */
    VANILLA(R.string.versions_manage_category_vanilla),
    /** 带有模组加载器 */
    MODLOADER(R.string.versions_manage_category_modloader)
}

@Composable
fun GamePathItemLayout(
    item: GamePath,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onRename: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val notDefault = item.id != GamePathManager.DEFAULT_ID

    NavigationDrawerItem(
        modifier = modifier,
        colors = NavigationDrawerItemDefaults.colors(),
        label = {
            Column(
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            ) {
                Text(
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    text = if (notDefault) item.title else stringResource(R.string.versions_manage_game_path_default),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
                Text(
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    text = item.path,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        },
        badge = {
            var menuExpanded by remember { mutableStateOf(false) }

            Row {
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = { menuExpanded = !menuExpanded }
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ic_more_horiz),
                        contentDescription = stringResource(R.string.generic_more),
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    shape = MaterialTheme.shapes.large,
                    shadowElevation = 3.dp,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        enabled = notDefault,
                        text = { Text(text = stringResource(R.string.generic_rename)) },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(R.drawable.ic_edit_filled),
                                contentDescription = stringResource(R.string.generic_rename)
                            )
                        },
                        onClick = {
                            onRename()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        enabled = notDefault,
                        text = { Text(text = stringResource(R.string.generic_delete)) },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(R.drawable.ic_delete_filled),
                                contentDescription = stringResource(R.string.generic_delete)
                            )
                        },
                        onClick = {
                            onDelete()
                            menuExpanded = false
                        }
                    )
                }
            }
        },
        selected = selected,
        onClick = onClick
    )
}

@Composable
fun GamePathOperation(
    gamePathOperation: GamePathOperation,
    changeState: (GamePathOperation) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val errorText = stringResource(R.string.versions_manage_game_path_error_title)
    fun doRunCatching(block: () -> Unit) {
        runCatching {
            block()
        }.onFailure { e ->
            submitError(
                ErrorViewModel.ThrowableMessage(
                    title = errorText,
                    message = e.getMessageOrToString()
                )
            )
        }
    }
    when (gamePathOperation) {
        is GamePathOperation.None -> {}
        is GamePathOperation.AddNewPath -> {
            NameEditPathDialog(
                onDismissRequest = { changeState(GamePathOperation.None) },
                onConfirm = { value ->
                    if (GamePathManager.containsPath(gamePathOperation.path)) {
                        changeState(GamePathOperation.PathExists)
                    } else {
                        doRunCatching {
                            GamePathManager.addNewPath(title = value, path = gamePathOperation.path)
                        }
                        changeState(GamePathOperation.None)
                    }
                }
            )
        }
        is GamePathOperation.RenamePath -> {
            NameEditPathDialog(
                initValue = gamePathOperation.item.title,
                onDismissRequest = { changeState(GamePathOperation.None) },
                onConfirm = { value ->
                    doRunCatching {
                        GamePathManager.modifyTitle(gamePathOperation.item, value)
                    }
                    changeState(GamePathOperation.None)
                }
            )
        }
        is GamePathOperation.DeletePath -> {
            SimpleAlertDialog(
                title = stringResource(R.string.versions_manage_game_path_delete_title),
                text = stringResource(R.string.versions_manage_game_path_delete_message),
                onDismiss = { changeState(GamePathOperation.None) },
                onConfirm = {
                    doRunCatching {
                        GamePathManager.removePath(gamePathOperation.item)
                    }
                    changeState(GamePathOperation.None)
                }
            )
        }
        is GamePathOperation.PathExists -> {
            SimpleAlertDialog(
                title = stringResource(R.string.versions_manage_game_path_exists_title),
                text = stringResource(R.string.versions_manage_game_path_exists_message),
                onDismiss = { changeState(GamePathOperation.None) }
            )
        }
    }
}

@Composable
fun VersionCategoryItem(
    modifier: Modifier = Modifier,
    value: VersionCategory,
    versionsCount: Int,
    selected: Boolean,
    shape: Shape = MaterialTheme.shapes.large,
    selectedContentColor: Color = MaterialTheme.colorScheme.onSurface,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    TextRailItem(
        modifier = modifier,
        text = {
            Text(
                text = stringResource(value.textRes),
                style = style
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "($versionsCount)",
                style = style
            )
        },
        onClick = onClick,
        selected = selected,
        shape = shape,
        selectedContentColor = selectedContentColor,
        unselectedContentColor = unselectedContentColor,
        enabled = enabled
    )
}

@Composable
private fun NameEditPathDialog(
    initValue: String = "",
    onDismissRequest: () -> Unit = {},
    onConfirm: (value: String) -> Unit = {}
) {
    var value by remember { mutableStateOf(initValue) }
    SimpleEditDialog(
        title = stringResource(R.string.versions_manage_game_path_add_new),
        value = value,
        onValueChange = { value = it },
        label = { Text(text = stringResource(R.string.versions_manage_game_path_edit_title)) },
        isError = value.isEmpty(),
        supportingText = {
            if (value.isEmpty()) Text(text = stringResource(R.string.generic_cannot_empty))
        },
        singleLine = true,
        onDismissRequest = onDismissRequest,
        onConfirm = {
            if (value.isNotEmptyOrBlank()) {
                onConfirm(value.trim())
            }
        }
    )
}

@Composable
fun VersionsOperation(
    versionsOperation: VersionsOperation,
    updateVersionsOperation: (VersionsOperation) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    when(versionsOperation) {
        is VersionsOperation.None -> {}
        is VersionsOperation.Rename -> {
            RenameVersionDialog(
                version = versionsOperation.version,
                onDismissRequest = { updateVersionsOperation(VersionsOperation.None) },
                onConfirm = {
                    updateVersionsOperation(
                        VersionsOperation.RunTask(
                            title = R.string.versions_manage_rename_version,
                            task = {
                                VersionsManager.renameVersion(versionsOperation.version, it)
                            }
                        )
                    )
                }
            )
        }
        is VersionsOperation.Copy -> {
            CopyVersionDialog(
                onDismissRequest = { updateVersionsOperation(VersionsOperation.None) },
                onConfirm = { name, copyAll ->
                    updateVersionsOperation(
                        VersionsOperation.RunTask(
                            title = R.string.versions_manage_copy_version,
                            task = { VersionsManager.copyVersion(versionsOperation.version, name, copyAll) }
                        )
                    )
                }
            )
        }
        is VersionsOperation.InvalidDelete -> {
            updateVersionsOperation(
                VersionsOperation.Delete(
                    versionsOperation.version,
                    stringResource(R.string.versions_manage_delete_version_tip_invalid)
                )
            )
        }
        is VersionsOperation.Delete -> {
            val version = versionsOperation.version
            DeleteVersionDialog(
                version = version,
                message = versionsOperation.text,
                onDismissRequest = { updateVersionsOperation(VersionsOperation.None) },
                onConfirm = { title, task ->
                    updateVersionsOperation(
                        VersionsOperation.RunTask(
                            title = title,
                            task = task
                        )
                    )
                }
            )
        }
        is VersionsOperation.RunTask -> {
            val errorMessage = stringResource(R.string.versions_manage_task_error)
            SimpleTaskDialog(
                title = stringResource(versionsOperation.title),
                task = versionsOperation.task,
                context = Dispatchers.IO,
                onDismiss = { updateVersionsOperation(VersionsOperation.None) },
                onError = { e ->
                    lError("Failed to run task.", e)
                    submitError(
                        ErrorViewModel.ThrowableMessage(
                            title = errorMessage,
                            message = e.getMessageOrToString()
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun RenameVersionDialog(
    version: Version,
    onDismissRequest: () -> Unit = {},
    onConfirm: (value: String) -> Unit = {}
) {
    var name by remember { mutableStateOf(version.getVersionName()) }

    val filenameInvalidMessage = key(name) {
        isFilenameInvalid(name)
    }

    val isVersionExists = remember(name) {
        VersionsManager.isVersionExists(name, true)
    }
    val isError = name.isEmpty() || filenameInvalidMessage != null || isVersionExists

    SimpleEditDialog(
        title = stringResource(R.string.versions_manage_rename_version),
        value = name,
        onValueChange = { name = it },
        isError = isError,
        supportingText = {
            when {
                name.isEmpty() -> Text(stringResource(R.string.generic_cannot_empty))
                filenameInvalidMessage != null -> Text(filenameInvalidMessage)
                isVersionExists -> Text(stringResource(R.string.versions_manage_install_exists))
            }
        },
        singleLine = true,
        onDismissRequest = onDismissRequest,
        onConfirm = {
            if (!isError) {
                onConfirm(name)
            }
        }
    )
}

@Composable
fun CopyVersionDialog(
    onDismissRequest: () -> Unit = {},
    onConfirm: (value: String, copyAll: Boolean) -> Unit = { _, _ -> }
) {
    var copyAll by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }

    val filenameInvalidMessage = key(name) {
        isFilenameInvalid(name)
    }

    val isVersionExists = remember(name) {
        VersionsManager.isVersionExists(name, true)
    }
    val isError = name.isEmpty() || filenameInvalidMessage != null || isVersionExists

    SimpleCheckEditDialog(
        title = stringResource(R.string.versions_manage_copy_version),
        text = stringResource(R.string.versions_manage_copy_version_tip),
        checkBoxText = stringResource(R.string.versions_manage_copy_version_all),
        checked = copyAll,
        value = name,
        onCheckedChange = { copyAll = it },
        onValueChange = { name = it },
        isError = isError,
        supportingText = {
            when {
                name.isEmpty() -> Text(stringResource(R.string.generic_cannot_empty))
                filenameInvalidMessage != null -> Text(filenameInvalidMessage)
                isVersionExists -> Text(stringResource(R.string.versions_manage_install_exists))
            }
        },
        singleLine = true,
        onDismissRequest = onDismissRequest,
        onConfirm = {
            if (!isError) {
                onConfirm(name, copyAll)
            }
        }
    )
}

@Composable
fun DeleteVersionDialog(
    version: Version,
    message: String? = null,
    onDismissRequest: () -> Unit = {},
    onConfirm: (title: Int, task: suspend () -> Unit) -> Unit = { _, _ -> },
    onVersionDeleted: () -> Unit = {}
) {
    val deleteVersion = {
        onConfirm(R.string.versions_manage_delete_version) {
            VersionsManager.deleteVersion(version)
            onVersionDeleted()
        }
    }

    if (message != null) {
        SimpleAlertDialog(
            title = stringResource(R.string.versions_manage_delete_version),
            text = message,
            onDismiss = onDismissRequest,
            onConfirm = deleteVersion
        )
    } else {
        SimpleAlertDialog(
            title = stringResource(R.string.versions_manage_delete_version),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = stringResource(R.string.versions_manage_delete_version_tip_hint1, version.getVersionName()))
                    Text(text = stringResource(R.string.versions_manage_delete_version_tip_hint2))
                    Text(text = stringResource(R.string.versions_manage_delete_version_tip_hint3))
                    Text(
                        text = stringResource(R.string.versions_manage_delete_version_tip_hint4),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            onConfirm = deleteVersion,
            onCancel = onDismissRequest,
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
fun CleanupOperation(
    operation: CleanupOperation,
    changeOperation: (CleanupOperation) -> Unit,
    cleaner: GameAssetCleaner?,
    onClean: () -> Unit,
    onCancel: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    when(operation) {
        is CleanupOperation.None -> {}
        is CleanupOperation.Tip -> {
            SimpleAlertDialog(
                title = stringResource(R.string.versions_manage_cleanup),
                text = {
                    Text(stringResource(R.string.versions_manage_cleanup_tip))
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.versions_manage_cleanup_warning))
                    Text("../assets/..")
                    //不再清理依赖库，文件并不会太大，也有可能导致其他问题：#617
//                    Text("../libraries/..")
                },
                onConfirm = onClean,
                onCancel = { changeOperation(CleanupOperation.None) }
            )
        }
        is CleanupOperation.Clean -> {
            if (cleaner != null) {
                val tasks = cleaner.tasksFlow.collectAsStateWithLifecycle()
                if (tasks.value.isNotEmpty()) {
                    //清理无用游戏文件流程对话框
                    TitleTaskFlowDialog(
                        title = stringResource(R.string.versions_manage_cleanup),
                        tasks = tasks.value,
                        onCancel = {
                            onCancel()
                            changeOperation(CleanupOperation.None)
                        }
                    )
                }
            }
        }
        is CleanupOperation.Error -> {
            val error = operation.error
            if (error is CleanFailedException) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = stringResource(R.string.generic_warning),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fadeEdge(state = scrollState)
                                .verticalScroll(state = scrollState)
                        ) {
                            Text(stringResource(R.string.versions_manage_cleanup_failed_files))
                            error.files.forEach { file ->
                                Text(file.absolutePath)
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { changeOperation(CleanupOperation.None) }) {
                            MarqueeText(text = stringResource(R.string.generic_cancel))
                        }
                    }
                )
            } else {
                changeOperation(CleanupOperation.None)
                submitError(
                    ErrorViewModel.ThrowableMessage(
                        title = stringResource(R.string.versions_manage_cleanup_failed),
                        message = error.getMessageOrToString()
                    )
                )
            }
        }
        is CleanupOperation.Success -> {
            SimpleAlertDialog(
                title = stringResource(R.string.versions_manage_cleanup),
                text = stringResource(R.string.versions_manage_cleanup_success, operation.count, operation.size)
            ) {
                changeOperation(CleanupOperation.None)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VersionItemLayout(
    version: Version,
    selected: Boolean,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = itemColor(),
    contentColor: Color = onItemColor(),
    onSelected: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onRenameClick: () -> Unit = {},
    onCopyClick: () -> Unit = {},
    onExportClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onPinned: () -> Unit = {}
) {
    val context = LocalContext.current

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
                onClick = {
                    if (selected) return@RadioButton
                    onSelected()
                }
            )
            CommonVersionInfoLayout(
                modifier = Modifier.weight(1f),
                version = version
            )

            IconButton(
                onClick = {
                    val currentValue = version.pinnedState
                    runCatching {
                        version.setPinnedAndSave(!currentValue)
                    }.onFailure { e ->
                        lError("Failed to save version config!", e)
                        submitError(
                            ErrorViewModel.ThrowableMessage(
                                title = context.getString(R.string.versions_config_failed_to_save),
                                message = e.getMessageOrToString()
                            )
                        )
                    }.onSuccess {
                        onPinned()
                    }
                },
                enabled = version.isValid()
            ) {
                Crossfade(
                    targetState = version.pinnedState
                ) { pinned ->
                    val icon = if (pinned) {
                        painterResource(R.drawable.ic_pinned_filled)
                    } else {
                        painterResource(R.drawable.ic_pinned_outlined)
                    }
                    Icon(
                        modifier = Modifier.rotate(45.0f),
                        painter = icon,
                        contentDescription = stringResource(R.string.versions_manage_pin)
                    )
                }
            }

            IconButton(
                onClick = onSettingsClick,
                enabled = version.isValid()
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_settings_filled),
                    contentDescription = stringResource(R.string.versions_manage_settings)
                )
            }

            Row {
                var menuExpanded by remember { mutableStateOf(false) }

                IconButton(onClick = { menuExpanded = !menuExpanded }) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_more_horiz),
                        contentDescription = stringResource(R.string.generic_more)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    shape = MaterialTheme.shapes.large,
                    shadowElevation = 3.dp,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.generic_rename)) },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(R.drawable.ic_edit_filled),
                                contentDescription = stringResource(R.string.generic_rename)
                            )
                        },
                        onClick = {
                            onRenameClick()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.generic_copy)) },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(R.drawable.ic_file_copy_filled),
                                contentDescription = stringResource(R.string.generic_copy)
                            )
                        },
                        onClick = {
                            onCopyClick()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.versions_export)) },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(R.drawable.ic_folder_zip_filled),
                                contentDescription = stringResource(R.string.versions_export)
                            )
                        },
                        onClick = {
                            onExportClick()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.generic_delete)) },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(R.drawable.ic_delete_filled),
                                contentDescription = stringResource(R.string.generic_delete)
                            )
                        },
                        onClick = {
                            onDeleteClick()
                            menuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CommonVersionInfoLayout(
    version: Version,
    modifier: Modifier = Modifier,
    iconSize: Dp = 34.dp,
) {
    val isValid = remember(version) { version.isValid() }
    val versionName = remember(version) { version.getVersionName() }
    val isSummaryValid = remember(version) { version.isSummaryValid() }
    val versionInfo = remember(version) { version.getVersionInfo() }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VersionIconImage(
            modifier = Modifier.size(iconSize),
            version = version
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            //版本名称
            Text(
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                maxLines = 1,
                text = versionName,
                style = MaterialTheme.typography.labelLarge
            )
            //版本描述
            if (isValid && isSummaryValid) {
                val versionSummary = remember(version) {
                    version.getVersionSummary()
                }

                Text(
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = 1,
                    text = versionSummary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            //版本详细信息
            FlowRow(
                modifier = Modifier.alpha(0.7f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isValid) {
                    LittleTextLabel(
                        text = stringResource(R.string.versions_manage_invalid),
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        textStyle = MaterialTheme.typography.labelSmall
                    )
                } else {
                    versionInfo?.let { versionInfo ->
                        Text(
                            text = versionInfo.minecraftVersion,
                            style = MaterialTheme.typography.labelSmall,
                        )
                        versionInfo.loaderInfo?.let { loaderInfo ->
                            Text(
                                text = loaderInfo.loader.displayName,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = loaderInfo.version,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VersionIconImage(
    version: Version?,
    modifier: Modifier = Modifier,
    refreshKey: Any? = null
) {
    val defaultIconRes = remember(version) {
        version?.let { getLoaderIconRes(it) } ?: R.drawable.img_minecraft
    }
    val defaultIcon = painterResource(defaultIconRes)

    val context = LocalContext.current
    val loader = remember(version, refreshKey) {
        ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
                add(SvgDecoder.Factory())
            }
            .build()
    }

    val model = remember(version, refreshKey) {
        version?.let {
            val iconFile = VersionsManager.getVersionIconFile(it)
            if (iconFile.exists()) iconFile
            else null
        } ?: defaultIcon
    }

    when (model) {
        is Painter -> {
            Image(
                painter = model,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = modifier
            )
        }
        else -> {
            AsyncImage(
                model = model,
                imageLoader = loader,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = modifier
            )
        }
    }
}

private fun getLoaderIconRes(version: Version): Int {
    return when (version.getVersionInfo()?.loaderInfo?.loader) {
        ModLoader.FABRIC,
        ModLoader.BABRIC -> R.drawable.img_loader_fabric
        ModLoader.LEGACY_FABRIC -> R.drawable.img_loader_legacy_fabric

        ModLoader.FORGE -> R.drawable.img_anvil
        ModLoader.QUILT -> R.drawable.img_loader_quilt
        ModLoader.NEOFORGE -> R.drawable.img_loader_neoforge
        ModLoader.OPTIFINE -> R.drawable.img_loader_optifine
        ModLoader.LITE_LOADER -> R.drawable.img_chicken_old
        ModLoader.CLEANROOM -> R.drawable.img_loader_cleanroom
        else -> R.drawable.img_minecraft
    }
}