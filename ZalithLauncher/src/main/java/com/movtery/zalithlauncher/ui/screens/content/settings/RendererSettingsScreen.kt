/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 * Copyright (C) 2026 Mirai Launcher contributors.
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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.plugin.driver.Driver
import com.movtery.zalithlauncher.game.plugin.driver.DriverPluginManager
import com.movtery.zalithlauncher.game.plugin.renderer_v2.RendererV2Data
import com.movtery.zalithlauncher.game.renderer.RendererInterface
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.game.version.installed.GraphicsApi
import com.movtery.zalithlauncher.path.URL_CLOUD_DRIVE_DRIVER_PLUGINS
import com.movtery.zalithlauncher.path.URL_CLOUD_RENDERER_PLUGINS
import com.movtery.zalithlauncher.path.URL_GITHUB_DRIVER_PLUGINS
import com.movtery.zalithlauncher.path.URL_GITHUB_RENDERER_PLUGINS
import com.movtery.zalithlauncher.path.URL_PROJECT
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.ResolutionRule
import com.movtery.zalithlauncher.setting.unit.floatRange
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedColumn
import com.movtery.zalithlauncher.ui.components.IntInputField
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.TitleAndSummary
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.home.ModrinthMetaPill
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.IntSliderSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.ListSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.customResolutionRange
import com.movtery.zalithlauncher.utils.device.checkVulkanSupport
import com.movtery.zalithlauncher.utils.ensureCustomResolutionInitialized
import com.movtery.zalithlauncher.utils.getRealScreenSize
import com.movtery.zalithlauncher.utils.isAdrenoGPU
import com.movtery.zalithlauncher.utils.platform.getMaxMemoryForSettings
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.sendDLPlugin
import kotlin.math.roundToInt

private data class RendererStackOption(
    val keyMatch: String,
    val title: String,
    val badge: String,
    val description: String
)

private val rendererStackOptions = listOf(
    RendererStackOption(
        keyMatch = "AUTO",
        title = "Auto (Smart Picker)",
        badge = "RECOMMENDED",
        description = "Automatically selects LTW for Minecraft 1.17+ and LTW Legacy for 1.8–1.16.5."
    ),
    RendererStackOption(
        keyMatch = "LTW",
        title = "LTW (LightThatWay GLES 3.2)",
        badge = "1.17+ MODERN",
        description = "High-performance OpenGL ES 3.2 renderer for modern Minecraft 1.17+ & Sodium."
    ),
    RendererStackOption(
        keyMatch = "Legacy",
        title = "LTW Legacy (1.8–1.16.5)",
        badge = "1.8–1.16.5 CLASSIC",
        description = "Dedicated legacy pipeline for classic 1.8.9–1.16.5, Hypixel, Forge & OptiFine."
    ),
    RendererStackOption(
        keyMatch = "Zink",
        title = "Kopper Zink (Vulkan)",
        badge = "VULKAN 1.1+",
        description = "Mesa Gallium3D OpenGL-on-Vulkan translation layer for Turnip / Adreno GPUs."
    ),
    RendererStackOption(
        keyMatch = "GL4ES",
        title = "GL4ES / VirGL Fallback",
        badge = "COMPATIBILITY",
        description = "Broad OpenGL ES 2.0/3.0 compatibility fallback for older mobile GPUs."
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RendererSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    eventViewModel: EventViewModel,
) {
    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.Renderer, settingsScreenKey, false)
    ) { isVisible ->
        val context = LocalContext.current
        val allRenderers = remember { Renderers.getRenderers() }
        val currentRendererId = AllSettings.renderer.state

        AnimatedColumn(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScrollWithBar(state = rememberScrollState())
                .padding(all = 12.dp),
            isVisible = isVisible
        ) { scope ->
            // 1. Modrinth Renderer Stack & Graphics API Radio Cards (Mockup #6)
            AnimatedItem(scope) { yOffset ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1D2027),
                    border = BorderStroke(1.dp, Color(0xFF2D313C))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Renderer Stack & Graphics API",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Choose how Mirai translates desktop OpenGL calls on your Android GPU",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                            ModrinthMetaPill(text = "Smart Picker Active", highlighted = true)
                        }

                        rendererStackOptions.forEach { option ->
                            val matchedRenderer = remember(option, allRenderers) {
                                when (option.keyMatch) {
                                    "AUTO" -> null
                                    "LTW" -> allRenderers.firstOrNull {
                                        it.getUniqueIdentifier().contains("LTW", ignoreCase = true) &&
                                            !it.getUniqueIdentifier().contains("Legacy", ignoreCase = true)
                                    }
                                    "Legacy" -> allRenderers.firstOrNull {
                                        it.getUniqueIdentifier().contains("Legacy", ignoreCase = true)
                                    }
                                    "Zink" -> allRenderers.firstOrNull {
                                        it.getUniqueIdentifier().contains("Zink", ignoreCase = true)
                                    }
                                    else -> allRenderers.firstOrNull {
                                        it.getUniqueIdentifier().contains("GL4ES", ignoreCase = true) ||
                                            it.getUniqueIdentifier().contains("VirGL", ignoreCase = true)
                                    }
                                }
                            }

                            val isSelected = when (option.keyMatch) {
                                "AUTO" -> currentRendererId.isBlank() || currentRendererId.equals("auto", ignoreCase = true)
                                else -> matchedRenderer != null && currentRendererId == matchedRenderer.getUniqueIdentifier()
                            }

                            val borderColor by animateColorAsState(
                                targetValue = if (isSelected) Color(0xFF1BD96A) else Color(0xFF2E323C),
                                animationSpec = tween(200),
                                label = "rendererOptionBorder"
                            )
                            val cardBg by animateColorAsState(
                                targetValue = if (isSelected) Color(0xFF142D20) else Color(0xFF232730),
                                animationSpec = tween(200),
                                label = "rendererOptionBg"
                            )

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = cardBg,
                                border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
                                onClick = {
                                    if (option.keyMatch == "AUTO") {
                                        AllSettings.renderer.save("")
                                    } else if (matchedRenderer != null) {
                                        AllSettings.renderer.save(matchedRenderer.getUniqueIdentifier())
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            if (option.keyMatch == "AUTO") {
                                                AllSettings.renderer.save("")
                                            } else if (matchedRenderer != null) {
                                                AllSettings.renderer.save(matchedRenderer.getUniqueIdentifier())
                                            }
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFF1BD96A),
                                            unselectedColor = Color(0xFF9CA3AF)
                                        )
                                    )
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = option.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            ModrinthMetaPill(
                                                text = option.badge,
                                                highlighted = isSelected || option.keyMatch == "AUTO"
                                            )
                                        }
                                        Text(
                                            text = option.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF9CA3AF)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Detailed Renderer, Vulkan Driver & Resolution Settings
            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    val v2PluginEnvUnits = remember(currentRendererId) {
                        Renderers.getRenderers()
                            .filterIsInstance<RendererV2Data>()
                            .find { it.getUniqueIdentifier() == currentRendererId }
                            ?.env?.getConfigurableUnits()?.takeIf { it.isNotEmpty() }
                    }
                    var showV2ConfigDialog by remember { mutableStateOf(false) }

                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.renderer,
                        items = Renderers.getRenderers(),
                        title = stringResource(R.string.settings_renderer_global_renderer_title),
                        summary = stringResource(R.string.settings_renderer_global_renderer_summary),
                        getItemText = { it.getRendererName() },
                        getItemId = { it.getUniqueIdentifier() },
                        getItemSummary = {
                            RendererSummaryLayout(it)
                        },
                        trailingIcon = {
                            if (v2PluginEnvUnits != null) {
                                IconButton(
                                    onClick = { showV2ConfigDialog = true }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_settings_filled),
                                        contentDescription = stringResource(R.string.settings_renderer_config_title)
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    eventViewModel.sendDLPlugin(
                                        githubLink = URL_GITHUB_RENDERER_PLUGINS,
                                        cloudDrives = listOf(
                                            EventViewModel.Event.DownloadPlugins.CloudDrive(
                                                language = "zh",
                                                link = URL_CLOUD_RENDERER_PLUGINS
                                            )
                                        )
                                    )
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_download_2_filled),
                                    contentDescription = stringResource(R.string.generic_download)
                                )
                            }
                        }
                    )

                    if (showV2ConfigDialog && v2PluginEnvUnits != null) {
                        RendererV2ConfigDialog(
                            units = v2PluginEnvUnits,
                            onDismissRequest = { showV2ConfigDialog = false }
                        )
                    }

                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.vulkanDriver,
                        items = DriverPluginManager.getDriverList(),
                        title = stringResource(R.string.settings_renderer_global_vulkan_driver_title),
                        getItemText = { it.name },
                        getItemId = { it.id },
                        getItemSummary = {
                            DriverSummaryLayout(it)
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    eventViewModel.sendDLPlugin(
                                        githubLink = URL_GITHUB_DRIVER_PLUGINS,
                                        cloudDrives = listOf(
                                            EventViewModel.Event.DownloadPlugins.CloudDrive(
                                                language = "zh",
                                                link = URL_CLOUD_DRIVE_DRIVER_PLUGINS
                                            )
                                        )
                                    )
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_download_2_filled),
                                    contentDescription = stringResource(R.string.generic_download)
                                )
                            }
                        }
                    )

                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.graphicsApi,
                        items = GraphicsApi.entries,
                        title = stringResource(R.string.settings_game_graphics_api_title),
                        summary = stringResource(R.string.settings_game_graphics_api_summary),
                        getItemText = {
                            when (it) {
                                GraphicsApi.DEFAULT -> stringResource(R.string.settings_game_graphics_api_default)
                                GraphicsApi.DEFAULT_OPENGL -> stringResource(R.string.settings_game_graphics_api_default_opengl)
                                else -> it.displayName
                            }
                        }
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        val resolutionRule = AllSettings.resolutionRule.state

                        ListSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            unit = AllSettings.resolutionRule,
                            items = ResolutionRule.entries,
                            title = stringResource(R.string.settings_renderer_resolution_rule_title),
                            summary = stringResource(R.string.settings_renderer_resolution_rule_summary),
                            getItemText = { stringResource(it.nameRes) },
                            onValueChange = { rule ->
                                if (rule == ResolutionRule.CUSTOM) {
                                    ensureCustomResolutionInitialized(context)
                                }
                            }
                        )

                        AnimatedVisibility(
                            visible = resolutionRule == ResolutionRule.PERCENTAGE,
                            enter = fadeIn(animationSpec = getAnimateTween()) +
                                    expandVertically(animationSpec = getAnimateTween()),
                            exit = fadeOut(animationSpec = getAnimateTween()) +
                                    shrinkVertically(animationSpec = getAnimateTween())
                        ) {
                            IntSliderSettingsCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                position = CardPosition.Middle,
                                unit = AllSettings.resolutionRatio,
                                title = stringResource(R.string.settings_renderer_resolution_scale_title),
                                summary = stringResource(R.string.settings_renderer_resolution_scale_summary),
                                valueRange = AllSettings.resolutionRatio.floatRange,
                                suffix = "%",
                                fineTuningControl = true
                            )
                        }

                        AnimatedVisibility(
                            visible = resolutionRule == ResolutionRule.CUSTOM,
                            enter = fadeIn(animationSpec = getAnimateTween()) +
                                    expandVertically(animationSpec = getAnimateTween()),
                            exit = fadeOut(animationSpec = getAnimateTween()) +
                                    shrinkVertically(animationSpec = getAnimateTween())
                        ) {
                            CustomResolutionSettingsCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                position = CardPosition.Middle
                            )
                        }
                    }

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.gameFullScreen,
                        title = stringResource(R.string.settings_renderer_full_screen_title),
                        summary = stringResource(R.string.settings_renderer_full_screen_summary)
                    )
                }
            }

            // 3. Memory & Java Runtime Quick Hub (Mockup #6 middle card)
            AnimatedItem(scope) { yOffset ->
                val runtimes = remember { RuntimesManager.getRuntimes() }
                val maxAllocMb = getMaxMemoryForSettings(context).coerceAtLeast(1024)
                val totalRamMb = remember(maxAllocMb) {
                    (maxAllocMb / 0.85f).roundToInt().coerceAtLeast(2048)
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1D2027),
                    border = BorderStroke(1.dp, Color(0xFF2D313C))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Memory & Java Runtime (JRE)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Configure global JVM heap memory and default Java runtime environment",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                            ModrinthMetaPill(
                                text = "${AllSettings.ramAllocation.state} MB Allocated",
                                highlighted = true
                            )
                        }

                        IntSliderSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Single,
                            unit = AllSettings.ramAllocation,
                            title = stringResource(R.string.settings_game_java_memory_title),
                            summary = stringResource(R.string.settings_game_java_memory_summary),
                            valueRange = 256f..maxAllocMb.toFloat(),
                            suffix = " MB",
                            fineTuningControl = true
                        )

                        Text(
                            text = "Default Java Runtime (JRE 8 / 17 / 21 / 25)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val currentJre = AllSettings.javaRuntime.state
                            FilterChip(
                                selected = currentJre.isEmpty(),
                                onClick = {
                                    AllSettings.autoPickJavaRuntime.save(true)
                                    AllSettings.javaRuntime.save("")
                                },
                                label = {
                                    Text(
                                        text = "Auto (Smart JRE Picker)",
                                        fontWeight = if (currentJre.isEmpty()) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFF232730),
                                    labelColor = Color(0xFFD1D5DB),
                                    selectedContainerColor = Color(0xFF143825),
                                    selectedLabelColor = Color(0xFF1BD96A)
                                )
                            )

                            runtimes.forEach { runtime ->
                                val selected = currentJre == runtime.name
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        AllSettings.autoPickJavaRuntime.save(false)
                                        AllSettings.javaRuntime.save(runtime.name)
                                    },
                                    label = {
                                        Text(
                                            text = "${runtime.name} (Java ${runtime.javaVersion})",
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = Color(0xFF232730),
                                        labelColor = Color(0xFFD1D5DB),
                                        selectedContainerColor = Color(0xFF143825),
                                        selectedLabelColor = Color(0xFF1BD96A)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 4. GPU & Zink Tweaks
            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.sustainedPerformance,
                        title = stringResource(R.string.settings_renderer_sustained_performance_title),
                        summary = stringResource(R.string.settings_renderer_sustained_performance_summary)
                    )

                    if (checkVulkanSupport(LocalContext.current.packageManager)) {
                        var adrenoGPUAlert by remember { mutableStateOf(false) }

                        SwitchSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            unit = AllSettings.zinkPreferSystemDriver,
                            title = stringResource(R.string.settings_renderer_vulkan_driver_system_title),
                            summary = stringResource(R.string.settings_renderer_vulkan_driver_system_summary),
                            onCheckedChange = { checked ->
                                if (checked && isAdrenoGPU()) adrenoGPUAlert = true
                            }
                        )

                        if (adrenoGPUAlert) {
                            SimpleAlertDialog(
                                title = stringResource(R.string.generic_warning),
                                text = stringResource(R.string.settings_renderer_zink_driver_adreno),
                                onConfirm = {
                                    AllSettings.zinkPreferSystemDriver.save(true)
                                    adrenoGPUAlert = false
                                },
                                onDismiss = {
                                    AllSettings.zinkPreferSystemDriver.save(false)
                                    adrenoGPUAlert = false
                                }
                            )
                        }
                    }

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.vsyncInZink,
                        title = stringResource(R.string.settings_renderer_vsync_in_zink_title),
                        summary = stringResource(R.string.settings_renderer_vsync_in_zink_summary)
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.useSurfaceView,
                        title = stringResource(R.string.settings_renderer_surface_title),
                        summary = stringResource(R.string.settings_renderer_surface_summary)
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.dumpShaders,
                        title = stringResource(R.string.settings_renderer_shader_dump_title),
                        summary = stringResource(R.string.settings_renderer_shader_dump_summary)
                    )
                }
            }

            // 5. About Mirai Launcher & Updater Card (Mockup #6 bottom card: entitybrian + v2.6.1 + Check for Updates)
            AnimatedItem(scope) { yOffset ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1D2027),
                    border = BorderStroke(1.dp, Color(0xFF1BD96A).copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF143825),
                                border = BorderStroke(1.dp, Color(0xFF1BD96A).copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_mirai_mark),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(34.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Mirai Launcher v${BuildConfig.VERSION_NAME}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    ModrinthMetaPill(text = "by entitybrian", highlighted = true)
                                }
                                Text(
                                    text = "Maintained by entitybrian • GitHub Release OTA Updater",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    eventViewModel.sendEvent(EventViewModel.Event.OpenLink(URL_PROJECT))
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("GitHub")
                            }
                            Button(
                                onClick = {
                                    eventViewModel.sendEvent(EventViewModel.Event.CheckUpdate)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1BD96A),
                                    contentColor = Color(0xFF06210F)
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_update),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Check for Updates",
                                    fontWeight = FontWeight.ExtraBold
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
@OptIn(ExperimentalLayoutApi::class)
fun RendererSummaryLayout(renderer: RendererInterface) {
    FlowRow(
        modifier = Modifier.alpha(0.7f),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        with(renderer) {
            getRendererSummary()?.let { summary ->
                Text(text = summary, style = MaterialTheme.typography.labelSmall)
            }

            val minVer = getDisplayMinMCVersion()
            val maxVer = getDisplayMaxMCVersion()

            if (minVer != null || maxVer != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = stringResource(R.string.renderer_version_support), style = MaterialTheme.typography.labelSmall)

                    minVer?.let {
                        Text(text = ">= $it", style = MaterialTheme.typography.labelSmall)
                    }

                    maxVer?.let {
                        Text(text = "<= $it", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun DriverSummaryLayout(driver: Driver) {
    with(driver) {
        summary?.let { text ->
            Text(
                modifier = Modifier.alpha(0.7f),
                text = text, style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun CustomResolutionSettingsCard(
    modifier: Modifier = Modifier,
    position: CardPosition = CardPosition.Middle
) {
    val context = LocalContext.current
    val screenSize = remember(context) { getRealScreenSize(context) }

    SettingsCard(
        modifier = modifier,
        position = position
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TitleAndSummary(
                title = stringResource(R.string.settings_renderer_resolution_scale_title),
                summary = stringResource(R.string.settings_renderer_resolution_custom_summary)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IntInputField(
                    modifier = Modifier.weight(1f),
                    value = AllSettings.customResolutionWidth.state,
                    permitted = customResolutionRange(screenSize.width),
                    label = stringResource(R.string.settings_renderer_resolution_custom_width),
                    onValueChange = { AllSettings.customResolutionWidth.save(it) }
                )
                IntInputField(
                    modifier = Modifier.weight(1f),
                    value = AllSettings.customResolutionHeight.state,
                    permitted = customResolutionRange(screenSize.height),
                    label = stringResource(R.string.settings_renderer_resolution_custom_height),
                    onValueChange = { AllSettings.customResolutionHeight.save(it) }
                )
            }
        }
    }
}
