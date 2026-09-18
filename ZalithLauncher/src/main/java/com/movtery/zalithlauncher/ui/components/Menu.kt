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

package com.movtery.zalithlauncher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.content.elements.DisabledAlpha
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.backgroundColor
import com.movtery.zalithlauncher.ui.theme.cardTitleColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onBackgroundColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.getAnimateTweenJellyBounce
import java.text.DecimalFormat

/**
 * 菜单状态
 */
enum class MenuState {
    /**
     * 初始化 (刚加载时)
     */
    NONE {
        override fun next() = SHOW
    },

    /**
     * 展示中
     */
    SHOW {
        override fun next() = HIDE
    },

    /**
     * 隐藏中
     */
    HIDE {
        override fun next() = SHOW
    };

    abstract fun next(): MenuState
}

@Composable
fun MenuSubscreen(
    state: MenuState,
    closeScreen: () -> Unit,
    shape: Shape = RoundedCornerShape(21.0.dp),
    backgroundColor: Color = Color.Black.copy(alpha = 0.25f),
    content: @Composable ColumnScope.() -> Unit
) {
    val visible = state == MenuState.SHOW
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            //背景阴影层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = backgroundColor)
                    .clickable(
                        indication = null, //禁用水波纹点击效果
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = closeScreen
                    )
            )
        }

        //Menu
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(fraction = 1f / 3f)
                .fillMaxHeight()
                .padding(top = 12.dp, end = 12.dp, bottom = 12.dp)
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInHorizontally(
                    animationSpec = getAnimateTweenJellyBounce()
                ) {
                    if (isRtl) -40 else 40
                  },
                exit = fadeOut() + slideOutHorizontally {
                    if (isRtl) -40 else 40
                }
            ) {
                BackgroundCard(
                    shape = shape,
                    influencedByBackground = false,
                    modifier = Modifier.fillMaxSize(),
                    content = content
                )
            }
        }
    }
}

@Composable
fun DualMenuSubscreen(
    state: MenuState,
    closeScreen: () -> Unit,
    shape: Shape = RoundedCornerShape(21.0.dp),
    backgroundColor: Color = Color.Black.copy(alpha = 0.25f),
    titleHeight: Dp = 48.dp,
    leftMenuTitle: (@Composable BoxScope.() -> Unit)? = null,
    leftMenuContent: @Composable ColumnScope.() -> Unit = {},
    rightMenuTitle: (@Composable BoxScope.() -> Unit)? = null,
    rightMenuContent: @Composable ColumnScope.() -> Unit = {}
) {
    val visible = state == MenuState.SHOW
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            //背景阴影层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = backgroundColor)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = closeScreen
                    )
            )
        }

        //左侧菜单
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(fraction = 1f / 3f)
                .fillMaxHeight()
                .padding(top = 12.dp, start = 12.dp, bottom = 12.dp)
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInHorizontally(
                    animationSpec = getAnimateTweenJellyBounce()
                ) {
                    if (isRtl) 40 else -40
                  },
                exit = fadeOut() + slideOutHorizontally {
                    if (isRtl) 40 else -40
                }
            ) {
                BackgroundCard(
                    shape = shape,
                    influencedByBackground = false,
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundColor(),
                        contentColor = onBackgroundColor()
                    ),
                    content = {
                        leftMenuTitle?.let { titleLayout ->
                            MenuTitleLayout(titleLayout, titleHeight)
                        }
                        leftMenuContent()
                    }
                )
            }
        }

        //右侧菜单
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(fraction = 1f / 3f)
                .fillMaxHeight()
                .padding(top = 12.dp, end = 12.dp, bottom = 12.dp)
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInHorizontally(
                    animationSpec = getAnimateTweenJellyBounce()
                ) {
                    if (isRtl) -40 else 40
                  },
                exit = fadeOut() + slideOutHorizontally {
                    if (isRtl) -40 else 40
                }
            ) {
                BackgroundCard(
                    shape = shape,
                    influencedByBackground = false,
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundColor(),
                        contentColor = onBackgroundColor()
                    ),
                    content = {
                        rightMenuTitle?.let { titleLayout ->
                            MenuTitleLayout(titleLayout, titleHeight)
                        }
                        rightMenuContent()
                    }
                )
            }
        }
    }
}

@Composable
private fun MenuTitleLayout(
    titleLayout: @Composable BoxScope.() -> Unit,
    height: Dp = 48.dp
) {
    Surface(
        modifier = Modifier
            .height(height)
            .fillMaxWidth(),
        color = cardTitleColor(),
        contentColor = onCardColor()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = titleLayout
        )
    }
    HorizontalDivider(modifier = Modifier.fillMaxWidth())
}

@Composable
fun MenuTextButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    influencedByBackground: Boolean = false,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemColor(influencedByBackground),
    contentColor: Color = onItemColor(),
    appendLayout: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    MenuButtonLayout(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        enabled = enabled,
        onClick = onClick,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MarqueeText(
            modifier = Modifier
                .padding(all = 16.dp)
                .alpha(if (enabled) 1f else DisabledAlpha)
                .weight(1f),
            text = text,
            style = MaterialTheme.typography.titleSmall
        )

        appendLayout?.invoke()
    }
}

@Composable
fun MenuSwitchButton(
    modifier: Modifier = Modifier,
    text: String,
    switch: Boolean,
    onSwitch: (Boolean) -> Unit,
    enabled: Boolean = true,
    influencedByBackground: Boolean = false,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemColor(influencedByBackground),
    contentColor: Color = onItemColor(),
) {
    MenuButtonLayout(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        enabled = enabled,
        onClick = { onSwitch(!switch) }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarqueeText(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .weight(1f)
                    .alpha(if (enabled) 1f else DisabledAlpha),
                text = text,
                style = MaterialTheme.typography.titleSmall
            )
            DefaultSwitch(
                checked = switch,
                onCheckedChange = onSwitch,
                enabled = enabled
            )
        }
    }
}

@Composable
fun <E> MenuListLayout(
    modifier: Modifier = Modifier,
    title: String,
    items: List<E>,
    currentItem: E,
    onItemChange: (E) -> Unit,
    getItemText: @Composable (E) -> String,
    selectedItemLayout: @Composable (ColumnScope.(E) -> Unit) = { item ->
        LittleTextLabel(
            text = getItemText(item)
        )
    },
    enabled: Boolean = true,
    maxListHeight: Dp = 200.dp,
    influencedByBackground: Boolean = false,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemColor(influencedByBackground),
    contentColor: Color = onItemColor(),
) {
    var expanded by remember { mutableStateOf(false) }

    MenuButtonLayout(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        enabled = enabled,
        onClick = {}
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MenuListHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (enabled) 1f else DisabledAlpha),
                items = items,
                title = title,
                selectedItemLayout = {
                    selectedItemLayout(currentItem)
                },
                expanded = expanded,
                enable = enabled,
                onClick = {
                    expanded = !expanded
                }
            )

            if (enabled && items.isNotEmpty()) {
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
                                MenuListItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 4.dp),
                                    text = getItemText(item),
                                    selected = currentItem == item,
                                    onClick = {
                                        if (expanded && currentItem != item) {
                                            onItemChange(item)
                                            expanded = false
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
private fun <E> MenuListHeader(
    modifier: Modifier = Modifier,
    items: List<E>,
    title: String,
    selectedItemLayout: @Composable ColumnScope.() -> Unit,
    expanded: Boolean,
    enable: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick, enabled = enable)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MarqueeText(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            selectedItemLayout()
        }

        if (!items.isEmpty()) {
            Row(
                modifier = Modifier.padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val rotation by animateFloatAsState(
                    targetValue = if (expanded) -180f else 0f,
                    animationSpec = getAnimateTween()
                )
                Icon(
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation),
                    painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                    contentDescription = stringResource(if (expanded) R.string.generic_expand else R.string.generic_collapse)
                )
            }
        }
    }
}

@Composable
private fun MenuListItem(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
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
            MarqueeText(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun MenuSliderLayout(
    modifier: Modifier = Modifier,
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean = true,
    onValueChangeFinished: (Int) -> Unit = {},
    suffix: String? = null,
    influencedByBackground: Boolean = false,
    colors: SliderColors = SliderDefaults.colors(),
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemColor(influencedByBackground),
    contentColor: Color = onItemColor(),
) {
    val interactionSource = remember { MutableInteractionSource() }
    var showInputDialog by remember { mutableStateOf(false) }

    MenuButtonLayout(
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        color = color,
        contentColor = contentColor,
        onClick = {
            showInputDialog = true
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .alpha(if (enabled) 1f else DisabledAlpha),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MarqueeText(
                    modifier = Modifier.weight(1f),
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    modifier = Modifier.clickable(enabled = enabled) {
                        showInputDialog = true
                    },
                    text = "$value${suffix ?: ""}",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            IndicatorSlider(
                modifier = Modifier.fillMaxWidth(),
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                onValueChangeFinished = { onValueChangeFinished(value) },
                interactionSource = interactionSource,
                valueRange = valueRange,
                colors = colors,
                enabled = enabled
            )
        }
    }

    if (showInputDialog) {
        SliderValueEditDialog(
            onDismissRequest = { showInputDialog = false },
            title = title,
            valueRange = valueRange,
            value = value.toFloat(),
            onValueChange = { onValueChangeFinished(it.toInt()) },
            intCheck = true
        )
    }
}

@Composable
fun MenuSliderLayout(
    modifier: Modifier = Modifier,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean = true,
    onValueChangeFinished: (Float) -> Unit = {},
    suffix: String? = null,
    decimalFormat: String = "#0.00",
    influencedByBackground: Boolean = false,
    colors: SliderColors = SliderDefaults.colors(),
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemColor(influencedByBackground),
    contentColor: Color = onItemColor(),
) {
    val formatter = DecimalFormat(decimalFormat)
    fun getTextString(value: Float) = formatter.format(value) + (suffix ?: "")

    val interactionSource = remember { MutableInteractionSource() }
    var showInputDialog by remember { mutableStateOf(false) }

    MenuButtonLayout(
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        color = color,
        contentColor = contentColor,
        onClick = {
            showInputDialog = true
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .alpha(if (enabled) 1f else DisabledAlpha),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MarqueeText(
                    modifier = Modifier.weight(1f),
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    modifier = Modifier.clickable(enabled = enabled) {
                        showInputDialog = true
                    },
                    text = getTextString(value),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            IndicatorSlider(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = { onValueChange(it) },
                onValueChangeFinished = { onValueChangeFinished(value) },
                interactionSource = interactionSource,
                valueRange = valueRange,
                colors = colors,
                enabled = enabled
            )
        }
    }

    if (showInputDialog) {
        SliderValueEditDialog(
            onDismissRequest = { showInputDialog = false },
            title = title,
            valueRange = valueRange,
            value = value,
            onValueChange = { onValueChangeFinished(it) },
        )
    }
}

@Composable
fun MenuButtonLayout(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    influencedByBackground: Boolean = false,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemColor(influencedByBackground),
    contentColor: Color = onItemColor(),
    blur: Int = AllSettings.backgroundBlur.state,
    onClick: () -> Unit = {},
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit
) {
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }

    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        shape = shape,
        color = color,
        contentColor = contentColor,
        enabled = enabled,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .backgroundGlass(blur, color, influencedByBackground),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            content = content
        )
    }
}