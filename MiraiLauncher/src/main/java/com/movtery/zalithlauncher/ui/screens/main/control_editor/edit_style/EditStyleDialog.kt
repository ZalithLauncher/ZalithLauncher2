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

package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_style

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.movtery.layer_controller.data.BORDER_WIDTH
import com.movtery.layer_controller.data.ButtonShape
import com.movtery.layer_controller.data.DEFAULT_FONT_SIZE
import com.movtery.layer_controller.data.FONT_SIZE_RANGE
import com.movtery.layer_controller.data.SHAPE_RANGE
import com.movtery.layer_controller.layout.RendererStyleBox
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableStyleConfig
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.SingleLineTextCheck
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutColorItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSliderItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSwitchItem
import com.movtery.zalithlauncher.ui.screens.rememberSwapTween
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.ui.theme.onItemColor

private data class TabItem(val titleRes: Int)

/**
 * 控件样式编辑对话框
 * **不再真正使用Dialog，真的会有性能问题！**
 */
@Composable
fun EditButtonStyleDialog(
    visible: Boolean,
    style: ObservableButtonStyle?,
    onClose: () -> Unit
) {
    val tween = rememberSwapTween()

    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = visible,
        enter = fadeIn(animationSpec = tween),
        exit = fadeOut(animationSpec = tween)
    ) {
        val tabs = remember {
            listOf(
                TabItem(R.string.control_editor_edit_style_config_light),
                TabItem(R.string.control_editor_edit_style_config_dark)
            )
        }

        val pagerState = rememberPagerState(pageCount = { tabs.size })
        var selectedTabIndex by remember { mutableIntStateOf(0) }

        LaunchedEffect(selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            //作为背景层，被点击时关闭Dialog
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onClose
                    )
            )

            if (style != null) {
                ConstraintLayout(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val (preview, config) = createRefs()

                    RendererBox(
                        style = style,
                        isDarkTheme = !style.commonStyle && selectedTabIndex == 1,
                        modifier = Modifier.constrainAs(preview) {
                            end.linkTo(config.start)

                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                    )

                    Surface(
                        modifier = Modifier
                            .padding(16.dp)
                            .constrainAs(config) {
                                start.linkTo(preview.end)
                                end.linkTo(parent.end)

                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)

                                width = Dimension.percent(0.8f)
                                height = Dimension.fillToConstraints
                            },
                        shadowElevation = 3.dp,
                        color = cardColor(false),
                        contentColor = onCardColor(),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Row(
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(all = 12.dp)
                                    .weight(0.4f)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SingleLineTextCheck(
                                    text = style.name,
                                    onSingleLined = { style.name = it }
                                )

                                //控件外观名称
                                OwnOutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = style.name,
                                    onValueChange = {
                                        style.name = it
                                    },
                                    singleLine = true,
                                    label = {
                                        Text(text = stringResource(R.string.control_editor_edit_style_config_name))
                                    },
                                    shape = MaterialTheme.shapes.large
                                )
                                //启用动画过渡
                                InfoLayoutSwitchItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = stringResource(R.string.control_editor_edit_style_config_animate_swap),
                                    value = style.animateSwap,
                                    onValueChange = { style.animateSwap = it }
                                )
                                //不区分系统主题
                                InfoLayoutSwitchItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = stringResource(R.string.control_editor_edit_style_config_common_style),
                                    value = style.commonStyle,
                                    onValueChange = { style.commonStyle = it }
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(0.6f)
                                    .fillMaxHeight()
                            ) {
                                if (style.commonStyle) {
                                    //仅编辑亮色外观（共用）
                                    StyleConfigEditor(
                                        modifier = Modifier.fillMaxSize(),
                                        styleConfig = style.lightStyle
                                    )
                                } else {
                                    //顶贴标签栏
                                    SecondaryTabRow(
                                        selectedTabIndex = selectedTabIndex,
                                        containerColor = cardColor(false)
                                    ) {
                                        tabs.forEachIndexed { index, item ->
                                            Tab(
                                                selected = index == selectedTabIndex,
                                                onClick = {
                                                    selectedTabIndex = index
                                                },
                                                text = {
                                                    MarqueeText(text = stringResource(item.titleRes))
                                                }
                                            )
                                        }
                                    }

                                    HorizontalPager(
                                        state = pagerState,
                                        userScrollEnabled = false,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                    ) { page ->
                                        when (page) {
                                            0 -> {
                                                StyleConfigEditor(
                                                    modifier = Modifier.fillMaxSize(),
                                                    styleConfig = style.lightStyle
                                                )
                                            }
                                            1 -> {
                                                StyleConfigEditor(
                                                    modifier = Modifier.fillMaxSize(),
                                                    styleConfig = style.darkStyle
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    createHorizontalChain(
                        preview, config,
                        chainStyle = ChainStyle.Packed
                    )
                }
            }
        }
    }
}

/**
 * 渲染样式在不同状态下的外观
 */
@Composable
private fun RendererBox(
    style: ObservableButtonStyle,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
    borderColor: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = MaterialTheme.shapes.large
) {
    Surface(
        modifier = modifier.border(
            width = 4.dp,
            color = borderColor,
            shape = shape
        ),
        color = color,
        contentColor = contentColor,
        shape = shape,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            val boxModifier = Modifier.size(50.dp)

            //普通状态
            RendererStyleBox(
                style = style,
                isDark = isDarkTheme,
                isPressed = false,
                text = "abc",
                modifier = boxModifier
            )

            //按下状态
            RendererStyleBox(
                style = style,
                isDark = isDarkTheme,
                isPressed = true,
                text = "abc",
                modifier = boxModifier
            )
        }
    }
}

@Composable
private fun StyleConfigEditor(
    modifier: Modifier = Modifier,
    styleConfig: ObservableStyleConfig
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val itemModifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 12.dp)

        //普通
        item(key = "normal_title") {
            Text(
                modifier = itemModifier,
                text = stringResource(R.string.control_editor_edit_style_config_normal)
            )
        }

        commonStyleConfig(
            tag = "normal",
            itemModifier = itemModifier,
            alpha = styleConfig.alpha,
            onAlphaChange = { styleConfig.alpha = it },
            backgroundColor = styleConfig.backgroundColor,
            onBackgroundColorChange = { styleConfig.backgroundColor = it },
            contentColor = styleConfig.contentColor,
            onContentColorChange = { styleConfig.contentColor = it },
            textSize = styleConfig.fontSize,
            onTextSizeChanged = { styleConfig.fontSize = it },
            borderWidth = styleConfig.borderWidth,
            onBorderWidthChange = { styleConfig.borderWidth = it },
            borderColor = styleConfig.borderColor,
            onBorderColorChange = { styleConfig.borderColor = it },
            borderRadius = styleConfig.borderRadius,
            onBorderRadiusChange = { styleConfig.borderRadius = it }
        )

        item(key = "divider") {
            HorizontalDivider(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .padding(vertical = 6.dp)
                    .fillMaxWidth()
            )
        }

        //按下
        item(key = "pressed_title") {
            Text(
                text = stringResource(R.string.control_editor_edit_style_config_pressed)
            )
        }

        commonStyleConfig(
            tag = "pressed",
            itemModifier = itemModifier,
            alpha = styleConfig.pressedAlpha,
            onAlphaChange = { styleConfig.pressedAlpha = it },
            backgroundColor = styleConfig.pressedBackgroundColor,
            onBackgroundColorChange = { styleConfig.pressedBackgroundColor = it },
            contentColor = styleConfig.pressedContentColor,
            onContentColorChange = { styleConfig.pressedContentColor = it },
            textSize = styleConfig.pressedFontSize,
            onTextSizeChanged = { styleConfig.pressedFontSize = it },
            borderWidth = styleConfig.pressedBorderWidth,
            onBorderWidthChange = { styleConfig.pressedBorderWidth = it },
            borderColor = styleConfig.pressedBorderColor,
            onBorderColorChange = { styleConfig.pressedBorderColor = it },
            borderRadius = styleConfig.pressedBorderRadius,
            onBorderRadiusChange = { styleConfig.pressedBorderRadius = it }
        )
    }
}

private fun LazyListScope.commonStyleConfig(
    tag: String,
    itemModifier: Modifier,
    alpha: Float,
    onAlphaChange: (Float) -> Unit,
    backgroundColor: Color,
    onBackgroundColorChange: (Color) -> Unit,
    contentColor: Color,
    onContentColorChange: (Color) -> Unit,
    textSize: Int?,
    onTextSizeChanged: (Int?) -> Unit,
    borderWidth: Int,
    onBorderWidthChange: (Int) -> Unit,
    borderColor: Color,
    onBorderColorChange: (Color) -> Unit,
    borderRadius: ButtonShape,
    onBorderRadiusChange: (ButtonShape) -> Unit
) {
    //整体不透明度
    item(key = "opacity_$tag") {
        InfoLayoutSliderItem(
            modifier = itemModifier.animateItem(),
            title = stringResource(R.string.control_editor_edit_style_config_alpha),
            value = alpha * 100f,
            onValueChange = { onAlphaChange(it / 100f) },
            valueRange = 0f..100f,
            decimalFormat = "#0",
            suffix = "%",
            fineTuningStep = 0.1f
        )
    }

    //背景颜色
    item(key = "background_color_$tag") {
        InfoLayoutColorItem(
            modifier = itemModifier.animateItem(),
            title = stringResource(R.string.control_editor_edit_style_config_background_color),
            color = backgroundColor,
            onColorChanged = onBackgroundColorChange
        )
    }

    //内容颜色
    item(key = "content_color_$tag") {
        InfoLayoutColorItem(
            modifier = itemModifier.animateItem(),
            title = stringResource(R.string.control_editor_edit_style_config_content_color),
            color = contentColor,
            onColorChanged = onContentColorChange
        )
    }

    //自定义文本大小
    item(key = "custom_text_size_$tag") {
        InfoLayoutSwitchItem(
            modifier = itemModifier.animateItem(),
            title = stringResource(R.string.control_editor_edit_text_size_custom),
            value = textSize != null,
            onValueChange = { value ->
                if (value) {
                    onTextSizeChanged(DEFAULT_FONT_SIZE)
                } else {
                    onTextSizeChanged(null)
                }
            }
        )
    }

    if (textSize != null) {
        //文本大小
        item(key = "text_size_$tag") {
            InfoLayoutSliderItem(
                modifier = itemModifier.animateItem(),
                title = stringResource(R.string.control_editor_edit_text_size),
                value = textSize.toFloat(),
                onValueChange = { value ->
                    onTextSizeChanged(value.toInt())
                },
                valueRange = FONT_SIZE_RANGE,
                decimalFormat = "#0",
                suffix = "sp",
                fineTuningStep = 1f,
            )
        }
    }

    //边框粗细
    item(key = "border_width_$tag") {
        InfoLayoutSliderItem(
            modifier = itemModifier.animateItem(),
            title = stringResource(R.string.control_editor_edit_style_config_border_width),
            value = borderWidth.toFloat(),
            onValueChange = { onBorderWidthChange(it.toInt()) },
            valueRange = BORDER_WIDTH,
            decimalFormat = "#0",
            suffix = "dp",
            fineTuningStep = 1f,
        )
    }

    //边框颜色
    item(key = "border_color_$tag") {
        InfoLayoutColorItem(
            modifier = itemModifier.animateItem(),
            title = stringResource(R.string.control_editor_edit_style_config_border_color),
            color = borderColor,
            onColorChanged = onBorderColorChange
        )
    }

    item(key = "corner_radius_text_$tag") {
        Text(
            modifier = Modifier.animateItem(),
            text = stringResource(R.string.control_editor_edit_style_config_widget_radius)
        )
    }

    //控件圆角
    item(key = "corner_radius_$tag") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateItem(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //左上角
            InfoLayoutSliderItem(
                modifier = itemModifier,
                title = stringResource(R.string.control_editor_edit_style_config_widget_radius_left_top),
                value = borderRadius.topStart,
                onValueChange = { onBorderRadiusChange(borderRadius.copy(topStart = it)) },
                valueRange = SHAPE_RANGE,
                suffix = "dp"
            )
            //右上角
            InfoLayoutSliderItem(
                modifier = itemModifier,
                title = stringResource(R.string.control_editor_edit_style_config_widget_radius_right_top),
                value = borderRadius.topEnd,
                onValueChange = { onBorderRadiusChange(borderRadius.copy(topEnd = it)) },
                valueRange = SHAPE_RANGE,
                suffix = "dp"
            )
            //左下角
            InfoLayoutSliderItem(
                modifier = itemModifier,
                title = stringResource(R.string.control_editor_edit_style_config_widget_radius_left_bottom),
                value = borderRadius.bottomStart,
                onValueChange = { onBorderRadiusChange(borderRadius.copy(bottomStart = it)) },
                valueRange = SHAPE_RANGE,
                suffix = "dp"
            )
            //右下角
            InfoLayoutSliderItem(
                modifier = itemModifier,
                title = stringResource(R.string.control_editor_edit_style_config_widget_radius_right_bottom),
                value = borderRadius.bottomEnd,
                onValueChange = { onBorderRadiusChange(borderRadius.copy(bottomEnd = it)) },
                valueRange = SHAPE_RANGE,
                suffix = "dp"
            )
        }
    }
}

