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

package com.movtery.zalithlauncher.ui.vulkan_checker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.device.McVersionSpan
import com.movtery.zalithlauncher.utils.device.VulkanDependency
import com.movtery.zalithlauncher.utils.device.VulkanRequirements
import com.movtery.zalithlauncher.utils.device.profileSupport
import com.movtery.zalithlauncher.utils.device.supports

@Composable
fun VulkanChecker(
    operation: VCOperation,
    onChange: (VCOperation) -> Unit,
    startCheck: (Version) -> Unit,
    confirmResult: () -> Unit,
) {
    when (operation) {
        is VCOperation.None -> {}
        is VCOperation.Tip -> {
            SimpleAlertDialog(
                title = stringResource(R.string.game_vulkan_check_title),
                text = stringResource(R.string.game_vulkan_check_text),
                dismissByDialog = false,
                onDismiss = {
                    startCheck(operation.version)
                }
            )
        }
        is VCOperation.Result -> {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnClickOutside = false,
                    dismissOnBackPress = false,
                )
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
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
                            modifier = Modifier.padding(all = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.game_vulkan_check_title),
                                style = MaterialTheme.typography.headlineSmall
                            )

                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .fillMaxWidth()
                                    .fadeEdge(scrollState)
                                    .verticalScrollWithBar(scrollState),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CompositionLocalProvider(
                                    LocalTextStyle provides MaterialTheme.typography.labelMedium
                                ) {
                                    val data = operation.data
                                    if (data == null) {
                                        Text(text = stringResource(R.string.game_vulkan_check_failed))
                                    } else {
                                        val profiles = data.profileSupport()

                                        //总体结论
                                        val summary = when {
                                            profiles.all { it.supported } ->
                                                stringResource(R.string.game_vulkan_check_supp, profiles.first().since)
                                            profiles.none { it.supported } ->
                                                stringResource(R.string.game_vulkan_check_unsupp)
                                            else ->
                                                stringResource(R.string.game_vulkan_check_partial)
                                        }
                                        Text(text = summary)

                                        //仅在部分版本区间受支持时，展示各版本区间的支持情况
                                        if (profiles.distinctBy { it.supported }.size > 1) {
                                            TextGroup(text = stringResource(R.string.game_vulkan_check_versions)) {
                                                profiles.forEach { profile ->
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Text(
                                                            text = "Minecraft ${profile.versionRangeText}"
                                                        )
                                                        Text(
                                                            text = if (profile.supported) {
                                                                stringResource(R.string.game_vulkan_check_profile_supp)
                                                            } else {
                                                                stringResource(R.string.game_vulkan_check_profile_unsupp)
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        //版本号
                                        Text(stringResource(R.string.game_vulkan_check_version, data.versionString))
                                        //是否使用 Turnip
                                        Text(stringResource(R.string.game_vulkan_check_turnip, operation.useTurnip))

                                        //各功能/扩展的支持情况与版本依赖标注
                                        TextGroup(text = stringResource(R.string.game_vulkan_check_extensions)) {
                                            VulkanRequirements.EXTENSIONS.forEach {
                                                DependencyText(
                                                    dependency = it,
                                                    supported = data.supports(it)
                                                )
                                            }
                                        }
                                        TextGroup(text = stringResource(R.string.game_vulkan_check_features)) {
                                            VulkanRequirements.FEATURES.forEach {
                                                DependencyText(
                                                    dependency = it,
                                                    supported = data.supports(it)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                FilledTonalButton(
                                    modifier = Modifier.focusProperties { canFocus = false },
                                    onClick = {
                                        confirmResult()
                                        onChange(VCOperation.None)
                                    }
                                ) {
                                    Text(stringResource(R.string.generic_confirm))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TextGroup(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            content = content
        )
    }
}

@Composable
private fun DependencyText(
    dependency: VulkanDependency,
    supported: Boolean,
    unsupportedColor: Color = MaterialTheme.colorScheme.error,
) {
    Column {
        Row {
            val color = if (supported) LocalContentColor.current else unsupportedColor
            Text(
                text = dependency.name,
                color = color
            )
            if (!supported) {
                Text(
                    text = stringResource(R.string.game_vulkan_check_item_missing),
                    color = color
                )
            }
        }

        @Composable
        fun VersionSupp(
            text: String,
            spans: List<McVersionSpan>,
            modifier: Modifier = Modifier
        ) {
            Row(modifier) {
                Text(text = text)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    spans.forEach { span ->
                        Text(text = span.displayText)
                    }
                }
            }
        }

        if (dependency.requiredIn.isNotEmpty()) {
            VersionSupp(
                modifier = Modifier.padding(start = 12.dp),
                text = stringResource(R.string.game_vulkan_check_dep_required),
                spans = dependency.requiredIn
            )
        }
        if (dependency.optionalIn.isNotEmpty()) {
            VersionSupp(
                modifier = Modifier.padding(start = 12.dp),
                text = stringResource(R.string.game_vulkan_check_dep_optional),
                spans = dependency.optionalIn
            )
        }
    }
}