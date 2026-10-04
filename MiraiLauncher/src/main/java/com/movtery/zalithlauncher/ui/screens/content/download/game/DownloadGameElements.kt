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

package com.movtery.zalithlauncher.ui.screens.content.download.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.addons.modloader.cleanroom.CleanroomVersion
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.FabricLikeVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.forge.ForgeVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.NeoForgeVersion
import com.movtery.zalithlauncher.game.addons.modloader.modlike.ModVersion
import com.movtery.zalithlauncher.game.addons.modloader.optifine.OptiFineVersion
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.components.influencedByBackgroundColor
import com.movtery.zalithlauncher.ui.components.rememberMaxHeight
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.getTimeAgo

/** Addon 加载状态 */
sealed interface AddonState {
    /** 已完成加载 */
    data object None : AddonState
    /** 加载中 */
    data object Loading : AddonState
    /**
     * 加载出现异常
     * @param message 异常消息代理对象
     */
    data class Error(val message: AndroidStringText): AddonState
}

/**
 * 简易 Addon 文本占位
 */
@Composable
private fun AddonTextLayout(
    modifier: Modifier = Modifier,
    title: String,
    summary: String
) {
    AddonTextLayout(
        modifier = modifier,
        title = title,
        summary = {
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    )
}

/**
 * 简易 Addon 文本占位
 */
@Composable
private fun AddonTextLayout(
    modifier: Modifier = Modifier,
    title: String,
    summary: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.padding(all = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall
        )
        summary()
    }
}

/**
 * Addon 版本列表
 * @param state Addon 当前的加载状态
 * @param items Addon 版本列表
 * @param current 当前选择的 Addon 版本
 * @param incompatibleSet 当前 Addon 的不兼容列表
 * @param checkIncompatible 检查当前的 Addon 的不兼容情况
 * @param triggerCheckIncompatible 手动触发检查不兼容情况
 * @param error 设置错误名称，让该列表不可用
 * @param iconPainter Addon 的图标
 * @param maxListHeight 列表展开最高显示高度
 * @param autoCollapse 选择版本后是否自动收起
 */
@Composable
fun <E> AddonListLayout(
    modifier: Modifier = Modifier,
    state: AddonState,
    items: List<E>?,
    current: E?,
    incompatibleSet: Set<ModLoader>,
    checkIncompatible: () -> Unit = {},
    triggerCheckIncompatible: Array<Any> = arrayOf(Unit),
    error: String? = null,
    iconPainter: Painter,
    title: String,
    getItemText: @Composable (E) -> String,
    summary: (@Composable (E) -> Unit)? = null,
    maxListHeight: Dp = rememberMaxHeight(),
    autoCollapse: Boolean = true,
    onValueChange: (E?) -> Unit = {},
    onReload: () -> Unit = {},
    shape: Shape = MaterialTheme.shapes.large,
    influencedByBackground: Boolean = true,
    color: Color = cardColor(influencedByBackground),
    contentColor: Color = onCardColor(),
    blur: Int = AllSettings.backgroundBlur.state,
) {
    var selectedItem by remember { mutableStateOf<E?>(null) }

    LaunchedEffect(state, selectedItem, *triggerCheckIncompatible) {
        //加载状态 || 选择项变更 || 手动触发，检查一次不兼容情况
        checkIncompatible()
    }

    LaunchedEffect(state, items, current) {
        selectedItem = items?.firstOrNull { it == current }
    }
    var expanded by remember { mutableStateOf(false) }

    fun clear() {
        onValueChange(null)
        selectedItem = null
        if (autoCollapse) expanded = false
    }

    //不兼容列表不为空，清除当前Addon的选择
    LaunchedEffect(incompatibleSet) {
        if (incompatibleSet.isNotEmpty()) clear()
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .backgroundGlass(blur, color, influencedByBackground)
        ) {
            AddonListHeader(
                modifier = Modifier.fillMaxWidth(),
                state = state,
                iconPainter = iconPainter,
                title = title,
                items = items,
                selectedItem = selectedItem,
                getItemText = getItemText,
                incompatibleSet = incompatibleSet,
                error = error,
                expanded = expanded,
                //加载已完成 && 版本列表不为空 && 不兼容列表为空 && 错误名称为 null -> 可展开
                enabled = state == AddonState.None && !items.isNullOrEmpty() && incompatibleSet.isEmpty() && error == null,
                onClick = { expanded = !expanded },
                onClear = ::clear,
                onReload = onReload
            )

            if (state == AddonState.None && !items.isNullOrEmpty() && error == null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(animationSpec = getAnimateTween()),
                        exit = shrinkVertically(animationSpec = getAnimateTween()) + fadeOut(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = maxListHeight)
                                .padding(vertical = 4.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(items) { item ->
                                AddonListItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 4.dp),
                                    selected = selectedItem == item,
                                    itemName = getItemText(item),
                                    summary = summary?.let {
                                        { it.invoke(item) }
                                    },
                                    onClick = {
                                        if (expanded && selectedItem != item) {
                                            onValueChange(item)
                                            selectedItem = item
                                            if (autoCollapse) expanded = false
                                        }
                                    }
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
private fun <E> AddonListHeader(
    modifier: Modifier = Modifier,
    state: AddonState,
    iconPainter: Painter,
    title: String,
    items: List<E>?,
    selectedItem: E?,
    getItemText: @Composable (E) -> String,
    incompatibleSet: Set<ModLoader>,
    error: String? = null,
    expanded: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    onClear: () -> Unit = {},
    onReload: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Image(
            modifier = Modifier.size(34.dp),
            painter = iconPainter,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))

        when(state) {
            is AddonState.None -> {
                Column(modifier = Modifier.weight(1f)) {
                    val summary: String = when {
                        error != null -> error
                        incompatibleSet.isNotEmpty() -> {
                            stringResource(R.string.download_game_addon_incompatible_with, incompatibleSet.joinToString(", ") { it.displayName })
                        }
                        items.isNullOrEmpty() -> stringResource(R.string.download_game_addon_unavailable)
                        selectedItem == null -> stringResource(R.string.download_game_addon_available)
                        else -> stringResource(R.string.settings_element_selected, getItemText(selectedItem))
                    }
                    AddonTextLayout(
                        modifier = Modifier.fillMaxWidth(),
                        title = title,
                        summary = summary,
                    )
                }
                if (!items.isNullOrEmpty() && incompatibleSet.isEmpty() && error == null) {
                    val rotation by animateFloatAsState(
                        targetValue = if (expanded) -180f else 0f,
                        animationSpec = getAnimateTween()
                    )
                    Icon(
                        modifier = Modifier
                            .size(34.dp)
                            .rotate(rotation),
                        painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                        contentDescription = stringResource(if (expanded) R.string.generic_expand else R.string.generic_collapse)
                    )
                    if (selectedItem != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            modifier = Modifier
                                .size(34.dp),
                            onClick = onClear
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(R.drawable.ic_deselect),
                                contentDescription = stringResource(R.string.generic_clear)
                            )
                        }
                    }
                }
            }
            is AddonState.Loading -> {
                AddonTextLayout(
                    modifier = Modifier.weight(1f),
                    title = title,
                    summary = stringResource(R.string.generic_loading),
                )
            }
            is AddonState.Error -> {
                AddonTextLayout(
                    modifier = Modifier.weight(1f),
                    title = title,
                    summary = {
                        AndroidStringText(
                            text = androidText(
                                R.string.download_game_addon_list_load_error,
                                state.message
                            )
                        )
                    },
                )
                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(34.dp),
                    onClick = onReload
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_refresh),
                        contentDescription = stringResource(R.string.generic_refresh)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun AddonListItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    itemName: String,
    summary: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = itemName,
                style = MaterialTheme.typography.labelMedium
            )
            summary?.invoke()
        }
    }
}

@Composable
fun AddonWarningItem(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = influencedByBackgroundColor(
        color = MaterialTheme.colorScheme.errorContainer,
        enabled = true
    ),
    contentColor: Color = MaterialTheme.colorScheme.onErrorContainer,
    blur: Int = AllSettings.backgroundBlur.state,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = color,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .backgroundGlass(blur, color)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = 4.dp),
                    painter = painterResource(R.drawable.ic_warning_filled),
                    contentDescription = null
                )
            }

            AddonTextLayout(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.generic_warning),
                summary = text
            )
        }
    }
}

@Composable
fun OptiFineVersionSummary(
    optifine: OptiFineVersion,
    iconSize: Dp = 14.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall
) {
    val typeText = if (optifine.isPreview) {
        stringResource(R.string.download_game_addon_preview)
    } else {
        stringResource(R.string.download_game_addon_release)
    }

    Row(
        modifier = Modifier.alpha(alpha = 0.7f),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        //版本状态
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(R.drawable.ic_package_2_outlined),
                contentDescription = null
            )
            Text(text = typeText, style = textStyle)
        }
        //发布时间
        optifine.releaseDate.takeIf { it.isNotEmpty() }?.let { releaseDate ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(iconSize),
                    painter = painterResource(R.drawable.ic_autorenew),
                    contentDescription = null
                )
                Text(text = releaseDate, style = textStyle)
            }
        }

        //兼容状态
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                optifine.forgeVersion == null -> {
                    Icon(
                        modifier = Modifier.size(iconSize),
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(R.string.download_game_addon_incompatible_with, ModLoader.FORGE.displayName),
                        style = textStyle
                    )
                }
                optifine.forgeVersion.isNotEmpty() -> {
                    Icon(
                        modifier = Modifier.size(iconSize),
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(R.string.download_game_addon_compatible_with, "${ModLoader.FORGE.displayName} ${optifine.forgeVersion}"),
                        style = textStyle
                    )
                }
            }
        }
    }
}

@Composable
fun ForgeVersionSummary(
    forgeVersion: ForgeVersion,
    iconSize: Dp = 14.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall
) {
    Row(
        modifier = Modifier.alpha(alpha = 0.7f),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (forgeVersion.isRecommended) {
            //Forge 官方推荐
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(iconSize),
                    painter = painterResource(R.drawable.ic_star_filled),
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.download_game_addon_recommended, ModLoader.FORGE.displayName),
                    style = textStyle
                )
            }
        }
        //发布时间
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(R.drawable.ic_autorenew),
                contentDescription = null
            )
            Text(text = forgeVersion.releaseTime, style = textStyle)
        }
    }
}

@Composable
fun NeoForgeSummary(
    neoforgeVersion: NeoForgeVersion,
    iconSize: Dp = 14.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall
) {
    val typeText = if (neoforgeVersion.isBeta) {
        stringResource(R.string.download_game_addon_debug)
    } else {
        stringResource(R.string.download_game_addon_release)
    }

    Row(
        modifier = Modifier.alpha(alpha = 0.7f),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        //版本状态
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(R.drawable.ic_package_2_outlined),
                contentDescription = null
            )
            Text(text = typeText, style = textStyle)
        }
    }
}

@Composable
fun FabricLikeSummary(
    fabricLikeVersion: FabricLikeVersion,
    iconSize: Dp = 14.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall
) {
    val typeText = if (fabricLikeVersion.stable) {
        stringResource(R.string.download_game_addon_stable)
    } else {
        stringResource(R.string.download_game_addon_debug)
    }

    Row(
        modifier = Modifier.alpha(alpha = 0.7f),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        //版本状态
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(R.drawable.ic_package_2_outlined),
                contentDescription = null
            )
            Text(text = typeText, style = textStyle)
        }
    }
}

@Composable
fun CleanroomSummary(
    version: CleanroomVersion,
    iconSize: Dp = 14.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall
) {
    //更新时间
    Row(
        modifier = Modifier.alpha(alpha = 0.7f),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(iconSize),
            painter = painterResource(R.drawable.ic_autorenew),
            contentDescription = null
        )
        Text(
            text = getTimeAgo(
                context = LocalContext.current,
                pastInstant = version.createdAt
            ),
            style = textStyle
        )
    }
}

@Composable
fun ModSummary(
    modVersion: ModVersion,
    iconSize: Dp = 14.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall
) {
    val version = modVersion.version

    Row(
        modifier = Modifier.alpha(alpha = 0.7f),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        //版本状态
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(R.drawable.ic_package_2_outlined),
                contentDescription = null
            )
            Text(
                text = stringResource(version.versionType.textRes),
                style = textStyle
            )
        }
        //更新时间
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(R.drawable.ic_autorenew),
                contentDescription = null
            )
            Text(
                text = getTimeAgo(
                    context = LocalContext.current,
                    dateString = version.datePublished
                ),
                style = textStyle
            )
        }
    }
}