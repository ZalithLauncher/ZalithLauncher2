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

package com.movtery.zalithlauncher.ui.screens.content.home.version

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.cardgrid.model.CardInteraction
import com.movtery.cardgrid.model.CardSize
import com.movtery.cardgrid.model.CardSizeClass
import com.movtery.cardgrid.model.CardState
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage

/** 版本卡片启动回调 */
val LocalHomeCardLauncher = staticCompositionLocalOf<(Version) -> Unit> { {} }
/** 版本卡片打开版本设置屏的回调 */
val LocalHomeCardVersionSettings = staticCompositionLocalOf<(Version) -> Unit> { {} }

/**
 * 版本卡片内容
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardState.VersionCardContent(cardId: String) {
    val states by VersionCardManager.cards.collectAsStateWithLifecycle()
    val card = remember(states, cardId) {
        states.firstOrNull { it.record.cardId == cardId }
    }
    val version = (card?.status as? VersionCardStatus.Available)?.version
    val onOpenSettings = LocalHomeCardVersionSettings.current

    //点按手势不持有指针事件，交互状态与版本实例在回调触发时读取即时值
    val currentVersion by rememberUpdatedState(version)
    val currentInteraction by rememberUpdatedState(interaction)

    //文本显示门槛
    val showSummary = sizeClass.height >= CardSizeClass.LARGE
    val showDetails = sizeClass.height >= CardSizeClass.MEDIUM
    val iconSize = iconSizeFor(sizeClass)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .homeCardTap {
                if (currentInteraction == CardInteraction.Idle) {
                    currentVersion?.let(onOpenSettings)
                }
            }
            .padding(12.dp)
    ) {
        when {
            sizeClass.width >= CardSizeClass.MEDIUM &&
                    sizeClass.height >= CardSizeClass.LARGE -> HeroContent(
                card = card,
                version = version,
                iconSize = enlargedIconSize(sizeClass.width),
                showSummary = showSummary,
                showDetails = showDetails
            )
            sizeClass.width <= CardSizeClass.SMALL -> NarrowContent(
                card = card,
                version = version,
                iconSize = iconSize,
                headerIconSize = enlargedIconSize(sizeClass.height),
                showSummary = showSummary,
                showDetails = showDetails,
                tall = sizeClass.height > CardSizeClass.SMALL,
            )
            else -> WideContent(
                card = card,
                version = version,
                iconSize = iconSize,
                showSummary = showSummary,
                showDetails = showDetails,
                textButton = sizeClass.width >= CardSizeClass.LARGE
            )
        }
    }
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
 * 卡身点按手势：点按全程不消费指针事件，与网格的长按、拖动手势互不干扰；
 * 按压超过长按时长或移动超出触摸斜率均不视为点按。
 */
private fun Modifier.homeCardTap(onTap: () -> Unit): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        if (down.isConsumed) return@awaitEachGesture
        val startPosition = down.position
        val slopPx = viewConfiguration.touchSlop
        val longPressMillis = viewConfiguration.longPressTimeoutMillis
        val tapped = withTimeoutOrNull(longPressMillis) {
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: continue
                if ((change.position - startPosition).getDistance() > slopPx) {
                    return@withTimeoutOrNull false
                }
                if (!change.pressed) return@withTimeoutOrNull true
            }
            @Suppress("UNREACHABLE_CODE") false
        } ?: false
        if (tapped) onTap()
    }
}

/**
 * 大卡片
 */
@Composable
private fun HeroContent(
    card: VersionCardState?,
    version: Version?,
    iconSize: Dp,
    showSummary: Boolean,
    showDetails: Boolean
) {
    val onLaunch = LocalHomeCardLauncher.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        //头行占用按钮之外的剩余空间；按钮非加权、先行测量，任何高度下都不会被挤压
        CardHeader(
            modifier = Modifier.weight(1f),
            iconSize = iconSize,
            card = card,
            version = version,
            showSummary = showSummary,
            showDetails = showDetails,
            multiline = true
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (version != null) {
                Button(onClick = { onLaunch(version) }) {
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
    }
}

@Composable
private fun CardHeader(
    modifier: Modifier = Modifier,
    iconSize: Dp,
    card: VersionCardState?,
    version: Version?,
    showSummary: Boolean,
    showDetails: Boolean,
    multiline: Boolean
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VersionIconImage(
            modifier = Modifier.size(iconSize),
            version = version
        )
        CardTexts(
            modifier = Modifier.weight(1f),
            card = card,
            version = version,
            showSummary = showSummary,
            showDetails = showDetails,
            multiline = multiline
        )
    }
}

/**
 * 窄宽卡片
 */
@Composable
private fun NarrowContent(
    card: VersionCardState?,
    version: Version?,
    iconSize: Dp,
    headerIconSize: Dp,
    showSummary: Boolean,
    showDetails: Boolean,
    tall: Boolean,
) {
    val onLaunch = LocalHomeCardLauncher.current

    if (tall) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CardHeader(
                modifier = Modifier.weight(1f),
                iconSize = headerIconSize,
                card = card,
                version = version,
                showSummary = showSummary,
                showDetails = showDetails,
                multiline = false
            )
            if (version != null) {
                Button(
                    onClick = { onLaunch(version) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
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
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VersionIconImage(
                modifier = Modifier.size(iconSize),
                version = version
            )
            CardTexts(
                modifier = Modifier.weight(1f),
                card = card,
                version = version,
                showSummary = showSummary,
                showDetails = showDetails
            )
            if (version != null) {
                Button(
                    onClick = { onLaunch(version) },
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(14.dp),
                        painter = painterResource(R.drawable.ic_play_arrow_filled),
                        contentDescription = stringResource(R.string.main_launch_game)
                    )
                }
            }
        }
    }
}

/**
 * 宽卡片
 */
@Composable
private fun WideContent(
    card: VersionCardState?,
    version: Version?,
    iconSize: Dp,
    showSummary: Boolean,
    showDetails: Boolean,
    textButton: Boolean
) {
    val onLaunch = LocalHomeCardLauncher.current

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VersionIconImage(
            modifier = Modifier.size(iconSize),
            version = version
        )
        CardTexts(
            modifier = Modifier.weight(1f),
            card = card,
            version = version,
            showSummary = showSummary,
            showDetails = showDetails
        )
        if (version != null) {
            if (textButton) {
                Button(onClick = { onLaunch(version) }) {
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
            } else {
                Button(
                    onClick = { onLaunch(version) },
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(14.dp),
                        painter = painterResource(R.drawable.ic_play_arrow_filled),
                        contentDescription = stringResource(R.string.main_launch_game)
                    )
                }
            }
        }
    }
}

/** 卡片文本区 */
@Composable
private fun CardTexts(
    modifier: Modifier = Modifier,
    card: VersionCardState?,
    version: Version?,
    showSummary: Boolean,
    showDetails: Boolean,
    multiline: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        //版本名称
        card?.record?.versionName?.let { name ->
            Text(
                modifier = Modifier
                    .basicMarquee(iterations = Int.MAX_VALUE)
                    .then(if (version == null) Modifier.alpha(0.55f) else Modifier),
                maxLines = 1,
                text = name,
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (version != null && showSummary && version.isSummaryValid()) {
            //版本描述：多行模式放开行数并以省略号收尾，否则单行跑马灯
            Text(
                modifier = if (multiline) {
                    Modifier
                } else {
                    Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                },
                maxLines = if (multiline) 3 else 1,
                overflow = TextOverflow.Ellipsis,
                text = version.getVersionSummary(),
                style = MaterialTheme.typography.labelMedium
            )
        }

        when (val status = card?.status) {
            is VersionCardStatus.Available -> if (showDetails) {
                //版本详细信息
                val versionInfo = status.version.getVersionInfo()
                FlowRow(
                    modifier = Modifier.alpha(0.7f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = versionInfo?.minecraftVersion ?: "",
                        style = MaterialTheme.typography.labelSmall
                    )
                    versionInfo?.loaderInfo?.let { loaderInfo ->
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
            //占位状态标签不受尺寸门槛限制，任何形态都需要提示原因
            is VersionCardStatus.Deleted -> StatusPlaceholder(
                text = stringResource(R.string.home_version_card_deleted)
            )
            is VersionCardStatus.Inaccessible -> StatusPlaceholder(
                text = stringResource(R.string.home_version_card_inaccessible)
            )
            null, VersionCardStatus.Loading -> Unit
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
