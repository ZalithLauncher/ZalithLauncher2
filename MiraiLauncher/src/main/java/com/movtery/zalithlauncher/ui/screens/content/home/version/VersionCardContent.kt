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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
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

/** 版本描述移入头行下方空白区的高度跨度 */
private const val DESCRIPTION_START_SPAN = 9
/** 头行与图标下方描述文本之间的间距 */
private val DetailTopGap = 4.dp
/** 详情文本行之间的间距 */
private val DetailItemGap = 2.dp

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

    //版本详细信息的显示门槛
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
                    sizeClass.height >= CardSizeClass.LARGE -> TallContent(
                card = card,
                version = version,
                iconSize = enlargedIconSize(sizeClass.width),
                spanHeight = spanHeight,
                showDetails = showDetails
            ) {
                if (version != null) TextLaunchButton(version)
            }
            sizeClass.width <= CardSizeClass.SMALL &&
                    sizeClass.height > CardSizeClass.SMALL -> TallContent(
                card = card,
                version = version,
                iconSize = enlargedIconSize(sizeClass.height),
                spanHeight = spanHeight,
                showDetails = showDetails
            ) {
                if (version != null) {
                    CompactLaunchButton(
                        version = version,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                    )
                }
            }
            else -> RowContent(
                card = card,
                version = version,
                iconSize = iconSize,
                showDetails = showDetails,
                textButton = sizeClass.width >= CardSizeClass.LARGE,
                spacing = if (sizeClass.width <= CardSizeClass.SMALL) 6.dp else 12.dp
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

@Composable
private fun lineHeightDp(style: TextStyle): Dp = with(LocalDensity.current) {
    style.lineHeight.takeOrElse { style.fontSize }.toDp()
}

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
 * 纵向布局
 */
@Composable
private fun TallContent(
    card: VersionCardState?,
    version: Version?,
    iconSize: Dp,
    spanHeight: Int,
    showDetails: Boolean,
    button: @Composable () -> Unit
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
            //头行高度即图标高度（名称、信息行与状态标签不会超过图标），扣除后即图标下方的空白
            val blank = maxHeight - iconSize - DetailTopGap
            val summaryLine = lineHeightDp(MaterialTheme.typography.labelMedium)
            val summaryValid = version?.isSummaryValid() == true
            //空白区文本：版本描述优先，缺失时由版本详细信息充当；达到高度门槛且放得下一行时显示
            val blankText = version
                ?.takeIf {
                    spanHeight >= DESCRIPTION_START_SPAN &&
                            blank >= summaryLine &&
                            (summaryValid || it.getVersionInfo() != null)
                }
                ?.getVersionSummary()
            //描述行的行数与空白高度一致，未容纳的文本以省略号收尾
            val blankMaxLines = (blank.value / summaryLine.value).toInt()

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VersionIconImage(
                        modifier = Modifier.size(iconSize),
                        version = version
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(DetailItemGap)
                    ) {
                        CardNameText(card = card, version = version)
                        //信息位于名称下方；版本缺失描述时，信息自门槛起下移充当描述行，不再显示于此
                        if (showDetails && version != null && (summaryValid || blankText == null)) {
                            InfoRow(version = version)
                        }
                        when (card?.status) {
                            is VersionCardStatus.Deleted -> StatusPlaceholder(
                                text = stringResource(R.string.home_version_card_deleted)
                            )
                            is VersionCardStatus.Inaccessible -> StatusPlaceholder(
                                text = stringResource(R.string.home_version_card_inaccessible)
                            )
                            else -> Unit
                        }
                    }
                }
                if (blankText != null) {
                    //描述行以高度权重填满头行下方的剩余空白，超出部分以省略号收尾
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = DetailTopGap),
                        maxLines = blankMaxLines,
                        overflow = TextOverflow.Ellipsis,
                        text = blankText,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        //按钮区占用固有高度，上方文本区加权，任何高度下都不会被挤压
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            button()
        }
    }
}

/** 版本信息 */
@Composable
private fun InfoRow(version: Version) {
    val versionInfo = version.getVersionInfo()
    Row(
        modifier = Modifier
            .alpha(0.7f)
            .basicMarquee(iterations = Int.MAX_VALUE),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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

/** 版本名称 */
@Composable
private fun CardNameText(
    modifier: Modifier = Modifier,
    card: VersionCardState?,
    version: Version?
) {
    card?.record?.versionName?.let { name ->
        Text(
            modifier = modifier
                .basicMarquee(iterations = Int.MAX_VALUE)
                .then(if (version == null) Modifier.alpha(0.55f) else Modifier),
            maxLines = 1,
            text = name,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * 横排布局
 */
@Composable
private fun RowContent(
    card: VersionCardState?,
    version: Version?,
    iconSize: Dp,
    showDetails: Boolean,
    textButton: Boolean,
    spacing: Dp
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
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
            showDetails = showDetails
        )
        if (version != null) {
            if (textButton) {
                TextLaunchButton(version)
            } else {
                IconLaunchButton(version)
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
    showDetails: Boolean
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DetailItemGap)
    ) {
        CardNameText(card = card, version = version)

        when (val status = card?.status) {
            is VersionCardStatus.Available -> if (showDetails) {
                //版本详细信息
                val versionInfo = status.version.getVersionInfo()
                FlowRow(
                    modifier = Modifier.alpha(0.7f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(DetailItemGap)
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

/** 带文字的启动按钮 */
@Composable
private fun TextLaunchButton(version: Version, modifier: Modifier = Modifier) {
    val onLaunch = LocalHomeCardLauncher.current
    Button(onClick = { onLaunch(version) }, modifier = modifier) {
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

/** 通栏紧凑启动按钮 */
@Composable
private fun CompactLaunchButton(version: Version, modifier: Modifier = Modifier) {
    val onLaunch = LocalHomeCardLauncher.current
    Button(
        onClick = { onLaunch(version) },
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
}

/** 仅图标的圆形启动按钮 */
@Composable
private fun IconLaunchButton(version: Version, modifier: Modifier = Modifier) {
    val onLaunch = LocalHomeCardLauncher.current
    Button(
        onClick = { onLaunch(version) },
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
