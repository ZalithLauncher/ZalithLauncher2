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

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ZLApplication
import com.movtery.zalithlauncher.context.getFileName
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.launch.executeJarWithUri
import com.movtery.zalithlauncher.game.multirt.Runtime
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.ImportMultipleFileButton
import com.movtery.zalithlauncher.ui.screens.content.elements.ImportSingleFileButton
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.device.Architecture
import com.movtery.zalithlauncher.utils.file.checkExtensionOrThrow
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.utils.string.throwableToString
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.sendToast
import kotlinx.coroutines.Dispatchers

private sealed interface RuntimeOperation {
    data object None: RuntimeOperation
    data class PreDelete(val runtime: Runtime): RuntimeOperation
    data class Delete(val runtime: Runtime): RuntimeOperation
}

@Composable
fun JavaManageScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current

    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.JavaManager, settingsScreenKey, false)
    ) { isVisible ->
        val yOffset by swapAnimateDpAsState(
            targetValue = (-40).dp,
            swapIn = isVisible
        )

        var runtimes by remember { mutableStateOf(getRuntimes()) }
        var runtimeOperation by remember { mutableStateOf<RuntimeOperation>(RuntimeOperation.None) }

        var showSelectRuntimeDialog by remember { mutableStateOf(false) }
        var selectedRuntimeForJar by remember { mutableStateOf<Runtime?>(null) }
        var launchJarPicker by remember { mutableStateOf<(() -> Unit)?>(null) }

        RuntimeOperation(
            runtimeOperation = runtimeOperation,
            updateOperation = { runtimeOperation = it },
            callRefresh = { runtimes = getRuntimes(true) },
            submitError = submitError
        )

        if (showSelectRuntimeDialog) {
            SelectJavaRuntimeDialog(
                runtimes = runtimes,
                onDismissRequest = {
                    showSelectRuntimeDialog = false
                    launchJarPicker = null
                },
                onSelectRuntime = { selectedRuntime ->
                    showSelectRuntimeDialog = false
                    selectedRuntimeForJar = selectedRuntime
                    launchJarPicker?.invoke()
                    launchJarPicker = null
                }
            )
        }

        SettingsCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 12.dp)
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
            position = CardPosition.Single
        ) {
            CardTitleLayout {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconTextButton(
                        onClick = { runtimes = getRuntimes(true) },
                        painter = painterResource(R.drawable.ic_refresh),
                        contentDescription = stringResource(R.string.generic_refresh),
                        text = stringResource(R.string.generic_refresh),
                    )
                    ImportMultipleFileButton(
                        extension = "xz",
                        progressUris = { uris ->
                            uris.forEach { uri ->
                                progressRuntimeUri(
                                    context = context,
                                    uri = uri,
                                    callRefresh = { runtimes = getRuntimes(true) },
                                    submitError = submitError
                                )
                            }
                        }
                    )
                    ImportSingleFileButton(
                        extension = "jar",
                        onClick = {
                            selectedRuntimeForJar = null
                        },
                        onLongClick = { launch ->
                            launchJarPicker = launch
                            showSelectRuntimeDialog = true
                        },
                        progressUris = { uris ->
                            uris.firstOrNull()?.let { uri ->
                                val customRuntime = selectedRuntimeForJar
                                selectedRuntimeForJar = null

                                if (customRuntime != null) {
                                    (context as? Activity)?.let { activity ->
                                        executeJarWithUri(activity, uri, customRuntime.name)
                                    }
                                } else {
                                    RuntimesManager.getExactJreName(8) ?: run {
                                        eventViewModel.sendToast(androidText(R.string.multirt_no_java_8), Toast.LENGTH_LONG)
                                        return@ImportSingleFileButton
                                    }
                                    (context as? Activity)?.let { activity ->
                                        val jreName = AllSettings.javaRuntime.takeIf { AllSettings.autoPickJavaRuntime.getValue() }?.getValue()
                                        executeJarWithUri(activity, uri, jreName)
                                    }
                                }
                            }
                        },
                        painter = painterResource(R.drawable.ic_terminal_outlined),
                        text = stringResource(R.string.execute_jar_title)
                    )
                }
            }

            val scrollState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nonInteractiveScrollbar(
                        state = scrollState.scrollIndicatorState!!,
                        orientation = Orientation.Vertical,
                    ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                state = scrollState,
            ) {
                items(runtimes) { runtime ->
                    JavaRuntimeItem(
                        runtime = runtime,
                        modifier = Modifier
                            .padding(vertical = 6.dp),
                        onDeleteClick = {
                            runtimeOperation = RuntimeOperation.PreDelete(runtime)
                        }
                    )
                }
            }
        }
    }
}

private fun getRuntimes(forceLoad: Boolean = false): List<Runtime> =
    RuntimesManager.getRuntimes(forceLoad = forceLoad)

@Composable
private fun RuntimeOperation(
    runtimeOperation: RuntimeOperation,
    updateOperation: (RuntimeOperation) -> Unit,
    callRefresh: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    when(runtimeOperation) {
        is RuntimeOperation.None -> {}
        is RuntimeOperation.PreDelete -> {
            val runtime = runtimeOperation.runtime
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.multirt_runtime_delete_message, runtime.name),
                onConfirm = { updateOperation(RuntimeOperation.Delete(runtime)) },
                onDismiss = { updateOperation(RuntimeOperation.None) }
            )
        }
        is RuntimeOperation.Delete -> {
            val failedMessage = stringResource(R.string.multirt_runtime_delete_failed)
            val runtime = runtimeOperation.runtime
            TaskSystem.submitTask(
                Task.runTask(
                    id = runtime.name,
                    dispatcher = Dispatchers.IO,
                    task = { task ->
                        task.updateMessage(androidText(
                            R.string.multirt_runtime_deleting, runtime.name
                        ))
                        RuntimesManager.removeRuntime(runtime.name)
                    },
                    onError = {
                        submitError(
                            ErrorViewModel.ThrowableMessage(
                                title = androidText(failedMessage),
                                message = androidText(it.getMessageOrToString())
                            )
                        )
                    },
                    onFinally = callRefresh
                )
            )
            updateOperation(RuntimeOperation.None)
        }
    }
}

private fun progressRuntimeUri(
    context: Context,
    uri: Uri,
    callRefresh: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    fun showError(
        title: AndroidStringText = androidText(R.string.multirt_runtime_import_failed),
        message: AndroidStringText
    ) {
        submitError(
            ErrorViewModel.ThrowableMessage(
                title = title,
                message = message
            )
        )
    }

    val name = context.getFileName(uri) ?: run {
        showError(message = androidText(R.string.multirt_runtime_import_failed_file_name))
        return
    }
    TaskSystem.submitTask(
        Task.runTask(
            id = name,
            dispatcher = Dispatchers.IO,
            task = { task ->
                name.checkExtensionOrThrow(listOf("xz"))

                val inputStream = context.contentResolver.openInputStream(uri) ?: run {
                    showError(message = androidText(R.string.multirt_runtime_import_failed_input_stream))
                    return@runTask
                }
                RuntimesManager.installRuntime(
                    nativeLibDir = PathManager.DIR_NATIVE_LIB,
                    inputStream = inputStream,
                    name = name,
                    updateProgress = { textRes, textArg ->
                        task.updateMessage(androidText(
                            textRes, *textArg
                        ))
                    }
                )
            },
            onError = {
                showError(message = androidText(throwableToString(it)))
            },
            onFinally = callRefresh,
            onCancel = {
                runCatching {
                    RuntimesManager.removeRuntime(name)
                    callRefresh()
                }.onFailure { t ->
                    showError(
                        title = androidText(R.string.multirt_runtime_delete_failed),
                        message = androidText(t.getMessageOrToString())
                    )
                }
            }
        )
    )
}

@Composable
private fun JavaRuntimeItem(
    runtime: Runtime,
    modifier: Modifier = Modifier,
    color: Color = itemColor(),
    contentColor: Color = onItemColor(),
    onClick: () -> Unit = {},
    onDeleteClick: (() -> Unit)? = null
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
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = runtime.name,
                    style = MaterialTheme.typography.titleSmall
                )
                //环境标签
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (runtime.isProvidedByLauncher) {
                        Text(
                            text = stringResource(R.string.multirt_runtime_provided_by_launcher),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text =  runtime.versionString?.let {
                            stringResource(R.string.multirt_runtime_version_name, it)
                        } ?: stringResource(R.string.multirt_runtime_corrupt),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (runtime.versionString != null) contentColor else MaterialTheme.colorScheme.error
                    )
                    runtime.javaVersion.takeIf { it != 0 }?.let { javaVersion ->
                        Text(
                            text = stringResource(R.string.multirt_runtime_version_code, javaVersion),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    runtime.arch?.let { arch ->
                        val compatible = ZLApplication.DEVICE_ARCHITECTURE == Architecture.archAsInt(arch)
                        Text(
                            text = arch.takeIf {
                                compatible
                            }?.let {
                                stringResource(R.string.multirt_runtime_version_arch, it)
                            } ?: stringResource(R.string.multirt_runtime_incompatible_arch, arch),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (compatible) contentColor else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            if (onDeleteClick != null) {
                IconButton(
                    //内置环境（未损坏）无法删除
                    enabled = !runtime.isProvidedByLauncher || !runtime.isCompatible(),
                    onClick = onDeleteClick
                ) {
                    Icon(
                        modifier = Modifier.padding(all = 8.dp),
                        painter = painterResource(R.drawable.ic_delete_filled),
                        contentDescription = stringResource(R.string.generic_delete)
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectJavaRuntimeDialog(
    runtimes: List<Runtime>,
    onDismissRequest: () -> Unit,
    onSelectRuntime: (Runtime) -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = rememberDialogMaxHeight())
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = cardColor(false),
            contentColor = onCardColor(),
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.execute_jar_title),
                    style = MaterialTheme.typography.titleMedium
                )

                val scrollState = rememberLazyListState()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    state = scrollState,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(runtimes) { runtime ->
                        JavaRuntimeItem(
                            runtime = runtime,
                            onClick = { onSelectRuntime(runtime) },
                            onDeleteClick = null
                        )
                    }
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismissRequest
                ) {
                    MarqueeText(text = stringResource(R.string.generic_cancel))
                }
            }
        }
    }
}
