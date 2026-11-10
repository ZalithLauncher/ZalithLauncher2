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

package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.movtery.zalithlauncher.ui.theme.NevNumberedTab
import com.movtery.zalithlauncher.ui.theme.NevPillButton
import com.movtery.zalithlauncher.ui.theme.NevPillStyle
import com.movtery.zalithlauncher.ui.theme.NevRed
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableClickEventsProvider
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableJoystickData
import com.movtery.layer_controller.observable.ObservableJoystickStyle
import com.movtery.layer_controller.observable.ObservableNormalData
import com.movtery.layer_controller.observable.ObservableTranslatableString
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.EdgeDirection
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.clearWith
import com.movtery.zalithlauncher.ui.screens.content.elements.CategoryItem
import com.movtery.zalithlauncher.ui.screens.rememberSwapTween
import com.movtery.zalithlauncher.ui.screens.rememberTitledNavBackStack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec

private enum class EditWidgetDialogState(val alpha: Float, val buttonText: Int) {
    /** 完全不透明 */
    OPAQUE(1.0f, R.string.control_editor_edit_dialog_open_preview) {
        override fun nextByUser(): EditWidgetDialogState = SEMI_TRANSPARENT_USER
    },
    /** 半透明 */
    SEMI_TRANSPARENT(0.3f, R.string.control_editor_edit_dialog_close_preview) {
        override fun nextByUser(): EditWidgetDialogState = OPAQUE
    },
    /** 半透明（用户主动选择） */
    SEMI_TRANSPARENT_USER(0.3f, R.string.control_editor_edit_dialog_close_preview){
        override fun nextByUser(): EditWidgetDialogState = OPAQUE
    };

    abstract fun nextByUser(): EditWidgetDialogState
}

/**
 * 控件编辑对话框
 * **不再真正使用Dialog，真的会有性能问题！**
 */
@Composable
fun EditWidgetDialog(
    visible: Boolean,
    data: SelectedWidgetData?,
    styles: List<ObservableButtonStyle>,
    joystickStyles: List<ObservableJoystickStyle>,
    onDismissRequest: () -> Unit,
    onDelete: (ObservableWidget, ObservableControlLayer) -> Unit,
    onClone: (ObservableWidget, ObservableControlLayer) -> Unit,
    onEditWidgetText: (ObservableTranslatableString) -> Unit,
    switchControlLayers: (ObservableClickEventsProvider, ClickEvent.Type) -> Unit,
    sendText: (ObservableClickEventsProvider) -> Unit,
    openStyleList: () -> Unit,
    openJoystickStyleList: () -> Unit,
    onEditJoystickEvents: () -> Unit,
) {
    val tween = rememberSwapTween()

    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = visible,
        enter = fadeIn(animationSpec = tween),
        exit = fadeOut(animationSpec = tween)
    ) {
        val backStack = rememberTitledNavBackStack(EditWidgetCategory.Info)
        var dialogTransparent by remember { mutableStateOf(EditWidgetDialogState.OPAQUE) }

        val alpha by animateFloatAsState(
            dialogTransparent.alpha
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha),
            contentAlignment = Alignment.Center
        ) {
            //防止底下的控件被点击
            if (visible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onDismissRequest
                        )
                )
            }

            if (data != null) {
                val categories = remember(data) {
                    when (data.data) {
                        is ObservableNormalData -> editWidgetCategories
                        is ObservableJoystickData -> editJoystickCategories
                        else -> editWidgetCategories.filterNot { it.key == EditWidgetCategory.ClickEvent }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight()
                        .padding(all = 12.dp),
                    shadowElevation = 3.dp,
                    color = Color.Transparent,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(listOf(NevRed.Outline, NevRed.OutlineBright))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NevRed.PanelBrush)
                            .padding(horizontal = 8.dp)
                    ) {
                        EditWidgetHeader()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            EditWidgetTabLayout(
                                modifier = Modifier.fillMaxHeight(),
                                items = categories,
                                currentKey = backStack.lastOrNull(),
                                navigateTo = { key ->
                                    backStack.clearWith(key)
                                }
                            )

                            EditWidgetNavigation(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                backStack = backStack,
                                data = data.data,
                                styles = styles,
                                joystickStyles = joystickStyles,
                                switchControlLayers = switchControlLayers,
                                sendText = sendText,
                                openStyleList = openStyleList,
                                openJoystickStyleList = openJoystickStyleList,
                                onEditWidgetText = onEditWidgetText,
                                onPreviewRequested = {
                                    if (dialogTransparent == EditWidgetDialogState.SEMI_TRANSPARENT_USER) return@EditWidgetNavigation
                                    dialogTransparent = EditWidgetDialogState.SEMI_TRANSPARENT
                                },
                                onDismissRequested = {
                                    if (dialogTransparent == EditWidgetDialogState.SEMI_TRANSPARENT_USER) return@EditWidgetNavigation
                                    dialogTransparent = EditWidgetDialogState.OPAQUE
                                },
                                onEditJoystickEvents = onEditJoystickEvents,
                            )
                        }
                        //底部操作栏
                        Row(
                            modifier = Modifier
                                .padding(all = 8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (dialogTransparent != EditWidgetDialogState.SEMI_TRANSPARENT) {
                                NevPillButton(
                                    text = stringResource(dialogTransparent.buttonText),
                                    painter = painterResource(R.drawable.ic_image_filled),
                                    style = NevPillStyle.Gradient,
                                    onClick = {
                                        dialogTransparent = dialogTransparent.nextByUser()
                                    }
                                )
                                Spacer(Modifier.width(16.dp))
                            } else {
                                //占位用，防止右侧按钮向左靠齐
                                Spacer(Modifier)
                            }

                            val scrollState = rememberScrollState()
                            LaunchedEffect(Unit) {
                                scrollState.scrollTo(scrollState.maxValue)
                            }
                            Row(
                                modifier = Modifier
                                    .border(
                                        BorderStroke(1.dp, NevRed.Outline),
                                        RoundedCornerShape(50)
                                    )
                                    .padding(4.dp)
                                    .fadeEdge(
                                        state = scrollState,
                                        direction = EdgeDirection.Horizontal
                                    )
                                    .horizontalScroll(state = scrollState),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                NevPillButton(
                                    text = stringResource(R.string.generic_delete),
                                    painter = painterResource(R.drawable.ic_delete_filled),
                                    style = NevPillStyle.Danger,
                                    onClick = {
                                        onDelete(data.data, data.layer)
                                    }
                                )

                                NevPillButton(
                                    text = stringResource(R.string.control_editor_edit_dialog_clone_widget),
                                    painter = painterResource(R.drawable.ic_file_copy_filled),
                                    style = NevPillStyle.Neutral,
                                    onClick = {
                                        onClone(data.data, data.layer)
                                    }
                                )

                                NevPillButton(
                                    text = stringResource(R.string.generic_close),
                                    painter = painterResource(R.drawable.ic_close),
                                    style = NevPillStyle.Gradient,
                                    onClick = onDismissRequest
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
private fun EditWidgetHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(NevRed.GradientDiagonal, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(26.dp),
                painter = painterResource(R.drawable.ic_settings_filled),
                contentDescription = null,
                tint = NevRed.OnGradient
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.control_editor_inspector_tag),
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                color = NevRed.OutlineBright
            )
            Text(
                text = stringResource(R.string.control_editor_inspector_subtitle),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        //装饰性的进度短线
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(6) { index ->
                Box(
                    modifier = Modifier
                        .size(width = 18.dp, height = 5.dp)
                        .background(
                            if (index < 4) NevRed.Gradient
                            else Brush.horizontalGradient(listOf(NevRed.Outline, NevRed.Outline)),
                            RoundedCornerShape(50)
                        )
                )
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, NevRed.Outline, Color.Transparent)
                )
            )
    )
}

@Composable
private fun EditWidgetTabLayout(
    modifier: Modifier = Modifier,
    items: List<CategoryItem>,
    currentKey: TitledNavKey?,
    navigateTo: (TitledNavKey) -> Unit
) {
    Column(
        modifier = modifier
            .width(190.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        items.forEachIndexed { index, item ->
            NevNumberedTab(
                modifier = Modifier.fillMaxWidth(),
                number = index + 1,
                selected = currentKey == item.key,
                onClick = {
                    navigateTo(item.key)
                }
            ) {
                item.icon()
                Text(
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    text = stringResource(item.textRes),
                    maxLines = 1,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EditWidgetNavigation(
    modifier: Modifier = Modifier,
    backStack: NavBackStack<TitledNavKey>,
    data: ObservableWidget,
    styles: List<ObservableButtonStyle>,
    joystickStyles: List<ObservableJoystickStyle>,
    onEditWidgetText: (ObservableTranslatableString) -> Unit,
    switchControlLayers: (ObservableClickEventsProvider, ClickEvent.Type) -> Unit,
    sendText: (ObservableClickEventsProvider) -> Unit,
    openStyleList: () -> Unit,
    openJoystickStyleList: () -> Unit,
    onPreviewRequested: () -> Unit,
    onDismissRequested: () -> Unit,
    onEditJoystickEvents: () -> Unit,
) {
    val currentKey = backStack.lastOrNull()

    if (backStack.isNotEmpty()) {
        NavDisplay(
            modifier = modifier,
            backStack = backStack,
            onBack = { /* 忽略 */ },
            transitionSpec = rememberTransitionSpec(),
            popTransitionSpec = rememberTransitionSpec(),
            entryProvider = entryProvider {
                entry<EditWidgetCategory.Info> { key ->
                    EditWidgetInfo(
                        screenKey = key,
                        currentKey = currentKey,
                        data = data,
                        onPreviewRequested = onPreviewRequested,
                        onDismissRequested = onDismissRequested
                    )
                }
                entry<EditWidgetCategory.TextStyle> { key ->
                    EditTextStyle(
                        screenKey = key,
                        currentKey = currentKey,
                        data = data,
                        onEditWidgetText = onEditWidgetText
                    )
                }
                entry<EditWidgetCategory.ClickEvent> { key ->
                    EditWidgetClickEvent(
                        screenKey = key,
                        currentKey = currentKey,
                        data = data as ObservableNormalData,
                        switchControlLayers = switchControlLayers,
                        sendText = sendText
                    )
                }
                entry<EditWidgetCategory.Style> { key ->
                    EditWidgetStyle(
                        screenKey = key,
                        currentKey = currentKey,
                        data = data,
                        styles = styles,
                        openStyleList = openStyleList
                    )
                }
                entry<EditWidgetCategory.JoystickConfig> { key ->
                    EditJoystickConfig(
                        screenKey = key,
                        currentKey = currentKey,
                        data = data as ObservableJoystickData
                    )
                }
                entry<EditWidgetCategory.DirectionEvents> {
                    LaunchedEffect(Unit) {
                        onEditJoystickEvents()
                    }
                    EditJoystickEvents(
                        data = data as ObservableJoystickData,
                        switchControlLayers = switchControlLayers,
                        sendText = sendText,
                    )
                }
                entry<EditWidgetCategory.JoystickStyle> { key ->
                    EditJoystickStyle(
                        screenKey = key,
                        currentKey = currentKey,
                        data = data as ObservableJoystickData,
                        joystickStyles = joystickStyles,
                        openJoystickStyleList = openJoystickStyleList,
                    )
                }
            }
        )
    }
}