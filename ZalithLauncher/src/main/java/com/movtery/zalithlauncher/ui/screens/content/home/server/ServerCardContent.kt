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

package com.movtery.zalithlauncher.ui.screens.content.home.server

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.movtery.cardgrid.model.CardSize
import com.movtery.cardgrid.model.CardSizeClass
import com.movtery.cardgrid.model.CardState
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.COPY_LABEL_SERVER_IP
import com.movtery.zalithlauncher.game.path.getGameHome
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.version.multiplayer.Players
import com.movtery.zalithlauncher.game.version.multiplayer.description.ComponentDescription
import com.movtery.zalithlauncher.game.version.multiplayer.description.ComponentDescriptionRoot
import com.movtery.zalithlauncher.game.version.multiplayer.description.ServerDescription
import com.movtery.zalithlauncher.game.version.multiplayer.description.StringDescription
import com.movtery.zalithlauncher.ui.components.ImePanContainer
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.MenuButtonLayout
import com.movtery.zalithlauncher.ui.components.MenuSwitchButton
import com.movtery.zalithlauncher.ui.components.MenuTextButton
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.ShimmerBox
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.content.elements.DisabledAlpha
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.screens.content.home.CardIconShape
import com.movtery.zalithlauncher.ui.screens.content.home.version.VersionCardDir
import com.movtery.zalithlauncher.ui.screens.content.home.version.VersionCardStatus
import com.movtery.zalithlauncher.ui.screens.content.versions.DescriptionTextRender
import com.movtery.zalithlauncher.ui.screens.content.versions.ServerSignalIcon
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.copyText
import com.movtery.zalithlauncher.utils.string.stripColorCodes

/** 服务器卡片快速启动回调；版本为 null 时由接收方解析为当前选中版本 */
val LocalServerCardQuickPlay = staticCompositionLocalOf<(Version?, String) -> Unit> { { _, _ -> } }

/** 名称与信息行之间的间距 */
private val DetailItemGap = 2.dp
/** 头行与描述区之间的间距 */
private val DetailTopGap = 4.dp

/**
 * 服务器卡片内容
 */
@Composable
fun CardState.ServerCardContent(cardId: String) {
    val states by ServerCardManager.cards.collectAsStateWithLifecycle()
    val card = remember(states, cardId) {
        states.firstOrNull { it.record.cardId == cardId }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        when {
            sizeClass.width >= CardSizeClass.MEDIUM &&
                    sizeClass.height >= CardSizeClass.LARGE -> TallContent(
                card = card,
                iconSize = enlargedIconSize(sizeClass.width),
                showMOTD = sizeClass.height >= CardSizeClass.MEDIUM,
                compactButton = false
            )
            sizeClass.width <= CardSizeClass.SMALL &&
                    sizeClass.height > CardSizeClass.SMALL -> TallContent(
                card = card,
                iconSize = enlargedIconSize(sizeClass.height),
                showMOTD = sizeClass.height >= CardSizeClass.MEDIUM,
                compactButton = true
            )
            else -> RowContent(
                card = card,
                iconSize = iconSizeFor(sizeClass),
                textButton = sizeClass.width >= CardSizeClass.LARGE,
                spacing = if (sizeClass.width <= CardSizeClass.SMALL) 6.dp else 12.dp
            )
        }
    }
}

/**
 * 服务器卡片设置对话框宿主
 */
@Composable
fun ServerCardSettingsHost(
    cardId: String,
    onDismiss: () -> Unit
) {
    val states by ServerCardManager.cards.collectAsStateWithLifecycle()
    val record = remember(states, cardId) {
        states.firstOrNull { it.record.cardId == cardId }?.record
    } ?: run {
        onDismiss()
        return
    }

    ServerCardSettingsDialog(
        record = record,
        onDismissRequest = onDismiss
    )
}

private fun iconSizeFor(sizeClass: CardSize): Dp {
    val cramped = sizeClass.height == CardSizeClass.COMPACT
    return when (sizeClass.width) {
        CardSizeClass.EXTRA_LARGE,
        CardSizeClass.LARGE -> if (cramped) 32.dp else 44.dp
        CardSizeClass.MEDIUM -> if (cramped) 28.dp else 36.dp
        else -> if (cramped) 24.dp else 28.dp
    }
}

private fun enlargedIconSize(sizeClass: CardSizeClass): Dp = when (sizeClass) {
    CardSizeClass.MEDIUM -> 48.dp
    CardSizeClass.LARGE -> 56.dp
    else -> 64.dp
}

/**
 * 纵向布局
 */
@Composable
private fun TallContent(
    card: ServerCardState?,
    iconSize: Dp,
    showMOTD: Boolean,
    compactButton: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .clipToBounds()
        ) {
            //头行高度即图标高度，扣除后即图标下方的空白
            val blank = maxHeight - iconSize - DetailTopGap
            val lineHeight = with(LocalDensity.current) {
                (MaterialTheme.typography.bodySmall.fontSize.toPx() * 1.1f).toDp()
            }
            //描述行的行数与空白高度一致
            val maxLines = (blank.value / lineHeight.value).toInt()

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                HeaderRow(
                    card = card,
                    iconSize = iconSize
                )

                MotdText(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = DetailTopGap),
                    card = card,
                    enabled = showMOTD,
                    maxLines = maxLines
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (compactButton) Arrangement.Start else Arrangement.End
        ) {
            QuickLaunchButton(
                card = card,
                compact = compactButton,
                modifier = if (compactButton) {
                    Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                } else {
                    Modifier
                }
            )
        }
    }
}

@Composable
private fun HeaderRow(card: ServerCardState?, iconSize: Dp) {
    HoverTooltip(card = card) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ServerCardIcon(
                card = card,
                iconSize = iconSize
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DetailItemGap)
            ) {
                card?.record?.name?.let { name ->
                    Text(
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        maxLines = 1,
                        text = name,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                StatusInfoRow(card = card)
            }
        }
    }
}

/**
 * 横排布局
 */
@Composable
private fun RowContent(
    card: ServerCardState?,
    iconSize: Dp,
    textButton: Boolean,
    spacing: Dp
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ServerCardIcon(
            card = card,
            iconSize = iconSize
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DetailItemGap)
        ) {
            card?.record?.name?.let { name ->
                Text(
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = 1,
                    text = name,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            StatusInfoRow(card = card)
        }
        if (card != null) {
            QuickLaunchButton(
                card = card,
                iconOnly = !textButton
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HoverTooltip(card: ServerCardState?, content: @Composable () -> Unit) {
    if (card == null) {
        content()
        return
    }
    val tooltipState = rememberTooltipState(isPersistent = true)
    val hoverInteraction = remember { MutableInteractionSource() }
    val hovered by hoverInteraction.collectIsHoveredAsState()

    LaunchedEffect(hovered) {
        if (hovered) tooltipState.show() else tooltipState.dismiss()
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Below
        ),
        tooltip = {
            tooltipText(card)?.let { tooltip ->
                PlainTooltip {
                    Text(text = tooltip)
                }
            }
        },
        state = tooltipState,
        enableUserInput = false
    ) {
        Box(modifier = Modifier.hoverable(hoverInteraction)) {
            content()
        }
    }
}

@Composable
private fun tooltipText(card: ServerCardState): String? {
    val result = (card.ping as? ServerCardPingStatus.Loaded)?.result
    return remember(result) {
        result?.status?.version?.name?.stripColorCodes()
    }
}

/** 将服务器描述组件拍平为纯文本 */
private fun ServerDescription.flattenText(): String = when (this) {
    is StringDescription -> value
    is ComponentDescriptionRoot -> values.joinToString("") { it.flattenText() }
    is ComponentDescription -> text + extra.joinToString("") { it.flattenText() }
    else -> ""
}

/** 服务器描述文本区 */
@Composable
private fun MotdText(
    modifier: Modifier = Modifier,
    card: ServerCardState?,
    enabled: Boolean,
    maxLines: Int,
) {
    if (!enabled || maxLines <= 0) return
    val description = (card?.ping as? ServerCardPingStatus.Loaded)
        ?.result?.status?.description ?: return
    DescriptionTextRender(
        modifier = modifier,
        description = description,
        fontSize = MaterialTheme.typography.bodySmall.fontSize,
        maxLines = maxLines,
        softWrap = true
    )
}

/** 延迟、在线人数与占位状态行 */
@Composable
private fun StatusInfoRow(card: ServerCardState?) {
    when (val ping = card?.ping) {
        is ServerCardPingStatus.Loaded -> {
            val undefined = stringResource(R.string.servers_list_undefined)
            val playerFull = stringResource(R.string.servers_list_players_full)
            FlowRow(
                modifier = Modifier.alpha(0.7f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ServerSignalIcon(
                        modifier = Modifier.size(16.dp),
                        signalStrength = signalStrengthOf(ping.result.pingMs)
                    )
                    Text(
                        text = "${ping.result.pingMs} ms",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(R.drawable.ic_person_outlined),
                        contentDescription = null
                    )
                    Text(
                        text = playersText(ping.result.status.players, undefined, playerFull),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        //初始刷新中：以进度条替代延迟/人数行
        ServerCardPingStatus.Loading -> LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth()
        )
        is ServerCardPingStatus.Failed -> StatusPlaceholder(
            text = stringResource(R.string.servers_list_failed_to_connect)
        )
        null -> Unit
    }
}

/** 快速启动按钮 */
@Composable
private fun QuickLaunchButton(
    card: ServerCardState?,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    iconOnly: Boolean = false
) {
    if (card?.record?.quickLaunchEnabled != true) return
    val onQuickPlay = LocalServerCardQuickPlay.current
    val record = card.record
    val launch = {
        when (record.versionMode) {
            ServerCardVersionMode.SPECIFIC -> {
                //启动前校验绑定版本是否仍然可用，失效则回退当前选中版本
                val available = card.versionStatus as? VersionCardStatus.Available
                onQuickPlay(available?.version?.takeIf { it.isValid() }, record.cardId)
            }
            ServerCardVersionMode.CURRENT -> onQuickPlay(null, record.cardId)
        }
    }
    when {
        iconOnly -> Button(
            onClick = launch,
            modifier = modifier,
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(
                modifier = Modifier.size(14.dp),
                painter = painterResource(R.drawable.ic_play_arrow_filled),
                contentDescription = stringResource(R.string.main_launch_game)
            )
        }
        compact -> Button(
            onClick = launch,
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(
                modifier = Modifier.size(14.dp),
                painter = painterResource(R.drawable.ic_play_arrow_filled),
                contentDescription = null
            )
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = stringResource(R.string.main_launch_game),
                style = MaterialTheme.typography.labelSmall
            )
        }
        else -> Button(onClick = launch, modifier = modifier) {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(R.drawable.ic_play_arrow_filled),
                contentDescription = null
            )
            Text(
                modifier = Modifier.padding(start = 6.dp),
                text = stringResource(R.string.main_launch_game)
            )
        }
    }
}

/** 服务器图标 */
@Composable
private fun ServerCardIcon(
    card: ServerCardState?,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val pxSize = with(density) { iconSize.roundToPx() }
    val icon = card?.record?.icon

    val sizeModifier = modifier
        .size(iconSize)
        .clip(CardIconShape)
    if (icon == null) {
        Image(
            modifier = sizeModifier,
            painter = painterResource(R.drawable.ic_unknown_icon),
            contentDescription = null
        )
        return
    }

    val imageRequest = remember(icon, pxSize) {
        ImageRequest.Builder(context)
            .data(icon)
            .size(pxSize)
            .crossfade(true)
            .build()
    }
    val painter = rememberAsyncImagePainter(
        model = imageRequest,
        placeholder = null,
        error = painterResource(R.drawable.ic_unknown_icon)
    )
    val state by painter.state.collectAsStateWithLifecycle()

    when (state) {
        AsyncImagePainter.State.Empty -> Box(modifier = sizeModifier)
        is AsyncImagePainter.State.Loading -> ShimmerBox(modifier = sizeModifier)
        is AsyncImagePainter.State.Error,
        is AsyncImagePainter.State.Success -> {
            Image(
                painter = painter,
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = sizeModifier
            )
        }
    }
}

@Composable
private fun StatusPlaceholder(text: String) {
    LittleTextLabel(
        text = text,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        textStyle = MaterialTheme.typography.labelSmall
    )
}

private fun signalStrengthOf(pingMs: Long): Int = when {
    pingMs < 150L -> 5
    pingMs < 300L -> 4
    pingMs < 600L -> 3
    pingMs < 1000L -> 2
    else -> 1
}

private fun playersText(players: Players, undefined: String, full: String): String {
    val online = players.online
    val max = players.max
    return when {
        online < 0 -> undefined
        max <= 0 -> online.toString()
        online in 0..max -> "$online/$max"
        else -> full
    }
}

/**
 * 服务器卡片设置对话框
 */
@Composable
private fun ServerCardSettingsDialog(
    record: ServerCardRecord,
    onDismissRequest: () -> Unit
) {
    val cardId = record.cardId
    val context = LocalContext.current

    var cardName by remember(cardId) { mutableStateOf(record.name) }
    var quickLaunch by remember(cardId) { mutableStateOf(record.quickLaunchEnabled) }
    var mode by remember(cardId) { mutableStateOf(record.versionMode) }
    var showVersionPicker by remember { mutableStateOf(false) }
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()

    CardDialogFrame(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.home_server_card_settings),
                style = MaterialTheme.typography.titleMedium
            )

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fadeEdge(state = scrollState)
                    .weight(1f, fill = false)
                    .verticalScrollWithBar(state = scrollState)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                //服务器名称
                OwnOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = cardName,
                    onValueChange = { cardName = it },
                    label = { Text(text = stringResource(R.string.servers_list_add_server_name)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large
                )

                //服务器地址
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.servers_list_add_server_ip),
                        style = MaterialTheme.typography.labelMedium
                    )
                    MenuTextButton(
                        text = record.cardId,
                        onClick = {
                            copyText(
                                label = COPY_LABEL_SERVER_IP,
                                text = record.cardId,
                                context = context
                            )
                        },
                        appendLayout = {
                            Icon(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(24.dp),
                                painter = painterResource(R.drawable.ic_copy_all_filled),
                                contentDescription = stringResource(R.string.servers_list_copy_server_address)
                            )
                        }
                    )
                }

                //手动刷新
                RefreshDialogItem(
                    modifier = Modifier.fillMaxWidth(),
                    cardId = cardId
                )

                //快速启动
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.saves_manage_quick_play),
                            style = MaterialTheme.typography.labelMedium
                        )
                        MenuSwitchButton(
                            text = stringResource(R.string.home_server_card_quick_launch),
                            switch = quickLaunch,
                            onSwitch = { quickLaunch = it }
                        )
                    }

                    CompositionLocalProvider(
                        LocalMinimumInteractiveComponentSize provides 0.dp
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            FilterChip(
                                enabled = quickLaunch,
                                selected = mode == ServerCardVersionMode.CURRENT,
                                onClick = { mode = ServerCardVersionMode.CURRENT },
                                label = {
                                    Text(text = stringResource(R.string.home_server_card_version_current))
                                }
                            )
                            FilterChip(
                                enabled = quickLaunch,
                                selected = mode == ServerCardVersionMode.SPECIFIC,
                                onClick = { mode = ServerCardVersionMode.SPECIFIC },
                                label = {
                                    Text(text = stringResource(R.string.home_server_card_version_specific))
                                }
                            )
                        }
                    }

                    //选择版本
                    AnimatedVisibility(
                        visible = mode == ServerCardVersionMode.SPECIFIC
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.download_assets_filter_game_version),
                                style = MaterialTheme.typography.labelMedium
                            )
                            MenuTextButton(
                                text = record.versionName
                                    ?: stringResource(R.string.generic_unspecified),
                                enabled = quickLaunch,
                                onClick = { showVersionPicker = true },
                                appendLayout = {
                                    Icon(
                                        modifier = Modifier
                                            .padding(end = 12.dp)
                                            .size(24.dp)
                                            .alpha(if (quickLaunch) 1f else DisabledAlpha),
                                        painter = painterResource(R.drawable.ic_settings_filled),
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = onDismissRequest
                ) {
                    MarqueeText(text = stringResource(R.string.generic_cancel))
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        ServerCardManager.setCardName(cardId, cardName)
                        ServerCardManager.setQuickLaunchEnabled(cardId, quickLaunch)
                        if (mode == ServerCardVersionMode.CURRENT) {
                            ServerCardManager.setVersionBinding(
                                cardId = cardId,
                                mode = ServerCardVersionMode.CURRENT
                            )
                        }
                        onDismissRequest()
                    }
                ) {
                    MarqueeText(text = stringResource(R.string.generic_confirm))
                }
            }
        }
    }

    if (showVersionPicker) {
        val usableVersions = remember(versions) {
            versions.filter { it.isValid() }
        }
        if (usableVersions.isEmpty()) {
            SimpleAlertDialog(
                title = stringResource(R.string.download_assets_filter_game_version),
                text = stringResource(R.string.versions_manage_no_versions),
                onDismiss = { showVersionPicker = false }
            )
        } else {
            VersionPickerDialog(
                versions = usableVersions,
                boundVersionName = record.versionName,
                onPick = { version ->
                    ServerCardManager.setVersionBinding(
                        cardId = cardId,
                        mode = ServerCardVersionMode.SPECIFIC,
                        dir = VersionCardDir.fromGameHome(getGameHome()),
                        versionName = version.getVersionName()
                    )
                    showVersionPicker = false
                },
                onDismissRequest = { showVersionPicker = false }
            )
        }
    }
}

/**
 * 版本选择对话框：列出传入的已加载版本（图标+名称），点选即绑定；
 * 版本列表为空时由调用方以普通对话框提示
 */
@Composable
private fun VersionPickerDialog(
    versions: List<Version>,
    boundVersionName: String?,
    onPick: (Version) -> Unit,
    onDismissRequest: () -> Unit
) {
    CardDialogFrame(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.download_assets_filter_game_version),
                style = MaterialTheme.typography.titleMedium
            )

            CompositionLocalProvider(
                LocalMinimumInteractiveComponentSize provides 0.dp
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(versions, key = { it.getVersionName() }) { version ->
                        MenuButtonLayout(
                            onClick = { onPick(version) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VersionIconImage(
                                modifier = Modifier
                                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
                                    .size(36.dp)
                                    .clip(CardIconShape),
                                version = version
                            )
                            MarqueeText(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(all = 12.dp),
                                text = version.getVersionName(),
                                style = MaterialTheme.typography.titleSmall
                            )
                            if (version.getVersionName() == boundVersionName) {
                                Icon(
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .size(20.dp),
                                    painter = painterResource(R.drawable.ic_check),
                                    tint = MaterialTheme.colorScheme.primary,
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

/** 手动刷新条目 */
@Composable
private fun RefreshDialogItem(
    cardId: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val inFlight by ServerCardManager.inFlightCards.collectAsStateWithLifecycle()

    MenuTextButton(
        modifier = modifier,
        text = stringResource(R.string.generic_refresh),
        onClick = {
            ServerCardManager.refresh(cardId, manual = true) { success ->
                if (!success) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.home_server_card_refresh_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        },
        appendLayout = {
            if (cardId in inFlight) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(24.dp)
                )
            } else {
                Icon(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(24.dp),
                    painter = painterResource(R.drawable.ic_refresh),
                    contentDescription = stringResource(R.string.generic_refresh)
                )
            }
        }
    )
}

@Composable
private fun CardDialogFrame(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(decorFitsSystemWindows = false)
    ) {
        ImePanContainer(
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
                content()
            }
        }
    }
}
