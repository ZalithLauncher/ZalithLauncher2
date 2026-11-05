/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.ui.screens.content.versions

import android.os.Build
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.control.ControlManager
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.plugin.Plugin
import com.movtery.zalithlauncher.game.plugin.driver.DriverPluginManager
import com.movtery.zalithlauncher.game.renderer.ConfigurableRenderer
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.game.support.touch_controller.VibrationHandler
import com.movtery.zalithlauncher.game.version.installed.GraphicsApi
import com.movtery.zalithlauncher.game.version.installed.SettingState
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionConfig
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.unit.floatRange
import com.movtery.zalithlauncher.setting.unit.getOrMin
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedColumn
import com.movtery.zalithlauncher.ui.components.IDItem
import com.movtery.zalithlauncher.ui.components.animated
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.MemoryPreview
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrophoneCheckOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrophoneCheckState
import com.movtery.zalithlauncher.ui.screens.content.settings.DriverSummaryLayout
import com.movtery.zalithlauncher.ui.screens.content.settings.RendererConfigDialog
import com.movtery.zalithlauncher.ui.screens.content.settings.RendererSummaryLayout
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.EnumSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.IntSliderSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.ListSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SimpleIDListCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.TextInputSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.StatefulVersionConfig
import com.movtery.zalithlauncher.ui.screens.content.versions.layouts.StatefulDropdownMenuFollowGlobal
import com.movtery.zalithlauncher.ui.screens.content.versions.layouts.ToggleableIntSliderSettingsCard
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.platform.getMaxMemoryForSettings
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.utils.string.isNotEmptyOrBlank
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "VersionConfigScreen"

/** 页面退出后的配置保存作用域 */
private val configSaveScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

@Composable
fun VersionConfigScreen(
    mainScreenKey: TitledNavKey?,
    versionsScreenKey: TitledNavKey?,
    version: Version,
    backToMainScreen: () -> Unit,
    onCheckVulkan: (Version) -> Unit,
    showToast: (AndroidStringText) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    if (!version.isValid()) {
        backToMainScreen()
        return
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.VersionSettings::class.java, mainScreenKey)
        ),
        Triple(NormalNavKey.Versions.Config, versionsScreenKey, false)
    ) { isVisible ->
        val config = version.getVersionConfig()
        val stateful = remember(version) { StatefulVersionConfig(config) }

        DisposableEffect(version) {
            onDispose {
                if (stateful.isModified()) {
                    // 页面退出时统一进行保存
                    stateful.packInto(config)
                    saveConfigAsync(config, submitError)
                }
            }
        }

        AnimatedColumn(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScrollWithBar(state = rememberScrollState())
                .padding(all = 12.dp),
            isVisible = isVisible
        ) {
            AnimatedItem { ctx ->
                VersionConfigs(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animated(ctx),
                    stateful = stateful,
                )
            }

            AnimatedItem { ctx ->
                GameConfigs(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animated(ctx),
                    stateful = stateful,
                )
            }

            AnimatedItem { ctx ->
                SupportConfigs(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animated(ctx),
                    stateful = stateful,
                    onCheckVulkan = {
                        onCheckVulkan(version)
                    },
                    showToast = showToast,
                )
            }
        }
    }
}

@Composable
private fun VersionConfigs(
    stateful: StatefulVersionConfig,
    modifier: Modifier = Modifier
) {
    SettingsCardColumn(
        modifier = modifier
    ) {
        Text(
            modifier = Modifier.padding(all = 8.dp),
            text = stringResource(R.string.versions_config_version_settings),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge
        )

        StatefulDropdownMenuFollowGlobal(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Top,
            currentValue = stateful.isolationType,
            onValueChange = { stateful.isolationType = it },
            title = stringResource(R.string.versions_config_isolation_title),
            summary = stringResource(R.string.versions_config_isolation_summary)
        )

        StatefulDropdownMenuFollowGlobal(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            currentValue = stateful.skipGameIntegrityCheck,
            onValueChange = { stateful.skipGameIntegrityCheck = it },
            title = stringResource(R.string.settings_game_skip_game_integrity_check_title),
            summary = stringResource(R.string.settings_game_skip_game_integrity_check_summary)
        )

        val renderers = Renderers.getRenderers()
        val renderersIdList = getIDList(renderers) { IDItem(it.getUniqueIdentifier(), it.getRendererName()) }
        val effectiveRendererId = stateful.renderer.ifEmpty { AllSettings.renderer.state }
        val effectiveRenderer = remember(effectiveRendererId) {
            renderers.find { it.getUniqueIdentifier() == effectiveRendererId }
        }
        val configurableRenderer = remember(stateful.renderer) {
            if (stateful.renderer.isNotEmpty()) {
                renderers.filterIsInstance<ConfigurableRenderer>()
                    .find { it.getUniqueIdentifier() == stateful.renderer }
                    ?.takeIf { it.configSpec.entries.isNotEmpty() }
            } else null
        }
        var showConfigDialog by remember { mutableStateOf(false) }

        ListSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            items = renderersIdList,
            currentId = stateful.renderer,
            defaultId = "",
            title = stringResource(R.string.versions_config_renderer),
            getItemText = { it.title },
            getItemId = { it.id },
            getItemSummary = { item ->
                renderers.find { it.getUniqueIdentifier() == item.id }?.let { renderer ->
                    RendererSummaryLayout(renderer)
                }
            },
            trailingIcon = {
                // 设置跟随全局时不能独立修改
                if (configurableRenderer != null) {
                    IconButton(onClick = { showConfigDialog = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings_filled),
                            contentDescription = stringResource(R.string.settings_renderer_config_title)
                        )
                    }
                }
            },
            onValueChange = { stateful.renderer = it.id }
        )

        if (showConfigDialog && configurableRenderer != null) {
            RendererConfigDialog(
                renderer = configurableRenderer,
                versionFolder = stateful.versionPath,
                onDismissRequest = { showConfigDialog = false }
            )
        }

        val sfpewPluginLayerEnabled = when (stateful.sfpewPluginLayer) {
            SettingState.FOLLOW_GLOBAL -> AllSettings.sfpewPluginLayer.state
            SettingState.ENABLE -> true
            SettingState.DISABLE -> false
        }
        StatefulDropdownMenuFollowGlobal(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            currentValue = stateful.sfpewPluginLayer,
            enabled = effectiveRenderer is Plugin,
            onValueChange = { stateful.sfpewPluginLayer = it },
            title = stringResource(R.string.settings_renderer_sfpew_plugin_layer_title),
            summary = stringResource(R.string.settings_renderer_sfpew_plugin_layer_summary)
        )

        StatefulDropdownMenuFollowGlobal(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            currentValue = stateful.sfpewRelaxedContext,
            enabled = effectiveRenderer?.let { renderer ->
                renderer.supportsSfpewByDefault() ||
                // 如果是插件，需要看是否给插件启用了这个玩意
                (renderer is Plugin && sfpewPluginLayerEnabled)
            } == true,
            onValueChange = { stateful.sfpewRelaxedContext = it },
            title = stringResource(R.string.renderer_config_sfpew_relaxed_context_title),
            summary = stringResource(R.string.renderer_config_sfpew_relaxed_context_desc)
        )

        val drivers = DriverPluginManager.getDriverList()
        val driversIdList = getIDList(drivers) { IDItem(it.id, it.name) }
        ListSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            items = driversIdList,
            currentId = stateful.driver,
            defaultId = "",
            title = stringResource(R.string.versions_config_vulkan_driver),
            getItemText = { it.title },
            getItemId = { it.id },
            getItemSummary = { item ->
                drivers.find { it.id == item.id }?.let { driver ->
                    DriverSummaryLayout(driver)
                }
            },
            onValueChange = { stateful.driver = it.id }
        )

        val graphicsApis = GraphicsApi.entries
        val defaultGraphicsTitle = stringResource(R.string.settings_game_graphics_api_default)
        val defaultOpenGLTitle = stringResource(R.string.settings_game_graphics_api_default_opengl)
        val graphicsApisList = getIDList(graphicsApis) {
            val title = when (it) {
                GraphicsApi.DEFAULT -> defaultGraphicsTitle
                GraphicsApi.DEFAULT_OPENGL -> defaultOpenGLTitle
                else -> it.displayName
            }
            IDItem(it.name, title)
        }
        ListSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            items = graphicsApisList,
            currentId = stateful.graphicsApi?.name ?: "",
            defaultId = "",
            title = stringResource(R.string.settings_game_graphics_api_title),
            summary = stringResource(R.string.settings_game_graphics_api_summary),
            getItemText = { it.title },
            getItemId = { it.id },
            onValueChange = { item ->
                stateful.graphicsApi = GraphicsApi.entries.find { it.name == item.id }
            }
        )

        val controls by ControlManager.dataList.collectAsStateWithLifecycle()
        val unspecifiedName = stringResource(R.string.generic_unspecified)
        val controlsIdList = getIDList(controls.filter { it.isSupport }) {
            IDItem(
                it.file.name,
                it.controlLayout.info.name.translate()
                    .takeIf { name -> name.isNotEmptyOrBlank() } ?: unspecifiedName
            )
        }
        ListSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Bottom,
            items = controlsIdList,
            currentId = stateful.control,
            defaultId = "",
            title = stringResource(R.string.versions_config_control),
            getItemText = { it.title },
            getItemId = { it.id },
            onValueChange = { stateful.control = it.id }
        )
    }
}

@Composable
private fun GameConfigs(
    stateful: StatefulVersionConfig,
    modifier: Modifier = Modifier
) {
    SettingsCardColumn(
        modifier = modifier
    ) {
        Text(
            modifier = Modifier.padding(all = 8.dp),
            text = stringResource(R.string.versions_config_game_settings),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge
        )

        SimpleIDListCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Top,
            items = getIDList(RuntimesManager.getRuntimes().filter { it.isCompatible() }) { IDItem(it.name, it.name) },
            currentId = stateful.javaRuntime,
            defaultId = "",
            title = stringResource(R.string.settings_game_java_runtime_title),
            summary = stringResource(R.string.versions_config_java_runtime_summary),
            onValueChange = { stateful.javaRuntime = it.id }
        )

        ToggleableIntSliderSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            currentValue = stateful.ramAllocation,
            valueRange = AllSettings.ramAllocation.floatRange.start..getMaxMemoryForSettings(LocalContext.current).toFloat(),
            defaultValue = AllSettings.ramAllocation.getOrMin(),
            title = stringResource(R.string.settings_game_java_memory_title),
            summary = stringResource(R.string.settings_game_java_memory_summary),
            suffix = "MB",
            onValueChange = { stateful.ramAllocation = it },
            previewContent = {
                AnimatedVisibility(
                    modifier = Modifier.fillMaxWidth(),
                    visible = stateful.ramAllocation >= 256
                ) {
                    MemoryPreview(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 2.dp, end = 8.dp),
                        preview = stateful.ramAllocation.takeIf { it >= 256 }?.toDouble(),
                        usedText = { usedMemory, totalMemory ->
                            stringResource(R.string.settings_game_java_memory_used_text, usedMemory.toInt(), totalMemory.toInt())
                        },
                        previewText = { preview ->
                            stringResource(R.string.settings_game_java_memory_allocation_text, preview.toInt())
                        }
                    )
                }
            }
        )

        TextInputSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            value = stateful.customInfo,
            onValueChange = { stateful.customInfo = it },
            title = stringResource(R.string.settings_game_version_custom_info_title),
            summary = stringResource(R.string.settings_game_version_custom_info_summary),
            label = {
                Text(text = stringResource(R.string.versions_config_follow_global_if_blank))
            }
        )

        TextInputSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            value = stateful.gameArgs,
            title = stringResource(R.string.versions_config_game_args_title),
            summary = stringResource(R.string.versions_config_game_args_summary),
            onValueChange = { stateful.gameArgs = it },
            label = {
                Text(text = stringResource(R.string.versions_config_disable_if_blank))
            }
        )

        TextInputSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            value = stateful.jvmArgs,
            title = stringResource(R.string.settings_game_jvm_args_title),
            summary = stringResource(R.string.settings_game_jvm_args_summary),
            onValueChange = { stateful.jvmArgs = it },
            label = {
                Text(text = stringResource(R.string.versions_config_follow_global_if_blank))
            }
        )

        TextInputSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Bottom,
            value = stateful.serverIp,
            title = stringResource(R.string.versions_config_auto_join_server_ip_title),
            summary = stringResource(R.string.versions_config_auto_join_server_ip_summary),
            onValueChange = { stateful.serverIp = it },
            label = {
                Text(text = stringResource(R.string.versions_config_disable_if_blank))
            }
        )
    }
}

@Composable
private fun SupportConfigs(
    stateful: StatefulVersionConfig,
    onCheckVulkan: () -> Unit,
    showToast: (AndroidStringText) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    SettingsCardColumn(modifier = modifier) {
        Text(
            modifier = Modifier.padding(all = 8.dp),
            text = stringResource(R.string.versions_config_support_settings),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge
        )

        val vibrator = remember(context) { context.getSystemService<Vibrator>() }
        val effectiveVibrateKind = stateful.touchVibrateKind ?: VibrationHandler.VibrateKind.default
        EnumSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Top,
            value = effectiveVibrateKind,
            title = stringResource(R.string.versions_config_vibrate_kind_title),
            summary = stringResource(R.string.versions_config_vibrate_kind_summary),
            entries = VibrationHandler.VibrateKind.entries,
            getRadioEnable = { enum ->
                enum == VibrationHandler.VibrateKind.ONE_SHOT || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            },
            getRadioText = { enum ->
                when (enum) {
                    VibrationHandler.VibrateKind.ONE_SHOT -> stringResource(R.string.vibrate_kind_one_shot)
                    VibrationHandler.VibrateKind.CLICK -> stringResource(R.string.vibrate_kind_click)
                    VibrationHandler.VibrateKind.DOUBLE_CLICK -> stringResource(R.string.vibrate_kind_double_click)
                    VibrationHandler.VibrateKind.HEAVY_CLICK -> stringResource(R.string.vibrate_kind_heavy_click)
                    VibrationHandler.VibrateKind.TICK -> stringResource(R.string.vibrate_kind_tick)
                }
            },
            maxItemsInEachRow = 4,
            onRadioClick = { enum ->
                stateful.touchVibrateKind = enum
                vibrator?.let { vibrator ->
                    VibrationHandler.vibrate(
                        vibrator = vibrator,
                        vibrateDuration = stateful.touchVibrateDuration,
                        vibrateKind = stateful.touchVibrateKind,
                    )
                }
            }
        )

        AnimatedVisibility(effectiveVibrateKind == VibrationHandler.VibrateKind.ONE_SHOT) {
            IntSliderSettingsCard(
                modifier = Modifier.fillMaxWidth(),
                position = CardPosition.Middle,
                value = stateful.touchVibrateDuration,
                title = stringResource(R.string.versions_config_vibrate_duration_title),
                summary = stringResource(R.string.versions_config_vibrate_duration_summary),
                valueRange = 80f..500f,
                onValueChange = { stateful.touchVibrateDuration = it },
                onValueChangeFinished = {
                    vibrator?.let { vibrator ->
                        VibrationHandler.vibrate(
                            vibrator = vibrator,
                            vibrateDuration = stateful.touchVibrateDuration,
                            vibrateKind = stateful.touchVibrateKind,
                        )
                    }
                },
                suffix = "ms",
                fineTuningControl = true
            )
        }

        SettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            title = stringResource(R.string.game_vulkan_check_title),
            summary = stringResource(R.string.game_vulkan_check_text),
            onClick = onCheckVulkan
        )

        //检查麦克风
        var microphoneState by remember { mutableStateOf<MicrophoneCheckState>(MicrophoneCheckState.None) }
        MicrophoneCheckOperation(
            state = microphoneState,
            changeState = { microphoneState = it },
            onShowToast = showToast,
        )
        SettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Bottom,
            title = stringResource(R.string.versions_config_microphone_check_title),
            summary = stringResource(R.string.versions_config_microphone_check_summary),
            onClick = {
                microphoneState = MicrophoneCheckState.Start
            }
        )
    }
}

@Composable
private fun <E> getIDList(list: List<E>, toIDItem: (E) -> IDItem): List<IDItem> {
    return list.map {
        toIDItem(it)
    }.toMutableList().apply {
        add(0, IDItem("", stringResource(R.string.generic_follow_global)))
    }
}

private fun saveConfigAsync(
    config: VersionConfig,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    configSaveScope.launch {
        runCatching {
            config.saveWithThrowable()
        }.onFailure { e ->
            Logger.error(TAG, "Failed to save version config!", e)
            submitError(
                ErrorViewModel.ThrowableMessage(
                    title = androidText(R.string.versions_config_failed_to_save),
                    message = androidText(e.getMessageOrToString())
                )
            )
        }
    }
}