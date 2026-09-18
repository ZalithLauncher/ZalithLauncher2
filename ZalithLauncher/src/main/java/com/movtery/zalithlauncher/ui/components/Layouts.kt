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

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween

@Composable
fun ScalingLabel(
    modifier: Modifier = Modifier,
    text: String,
    influencedByBackground: Boolean = true,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    color: Color = itemColor(influencedByBackground),
    contentColor: Color = onItemColor(),
    blur: Int = AllSettings.backgroundBlur.state,
) {
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }
    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        shape = shape,
        color = color,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .backgroundGlass(blur, color, influencedByBackground)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text = text)
        }
    }
}

@Composable
fun ScalingLabel(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    influencedByBackground: Boolean = true,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    color: Color = itemColor(influencedByBackground = influencedByBackground),
    contentColor: Color = onItemColor(),
) {
    ScalingLabel(
        modifier = modifier,
        onClick = onClick,
        text = {
            Text(text = text)
        },
        influencedByBackground = influencedByBackground,
        shape = shape,
        color = color,
        contentColor = contentColor,
    )
}

@Composable
fun ScalingLabel(
    modifier: Modifier = Modifier,
    text: @Composable RowScope.() -> Unit,
    onClick: () -> Unit,
    influencedByBackground: Boolean = true,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    color: Color = itemColor(influencedByBackground = influencedByBackground),
    contentColor: Color = onItemColor(),
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
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            content = text,
        )
    }
}

@Composable
fun LittleTextLabel(
    modifier: Modifier = Modifier,
    text: String,
    singleLine: Boolean = true,
    color: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = MaterialTheme.colorScheme.onTertiary,
    shape: Shape = MaterialTheme.shapes.large,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = shape
    ) {
        Text(
            modifier = Modifier
                .then(
                    if (singleLine) Modifier.basicMarquee(Int.MAX_VALUE)
                    else Modifier
                )
                .padding(horizontal = 4.dp, vertical = 2.dp),
            text = text,
            style = textStyle,
            maxLines = if (singleLine) 1 else Int.MAX_VALUE
        )
    }
}

@Composable
fun SimpleListItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    itemName: String,
    summary: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.large)
            .clickable(enabled = enabled, onClick = onClick),
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
fun SimpleListItem(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    itemName: String,
    summary: (@Composable () -> Unit)? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.large)
            .clickable(enabled = enabled) {
                onCheckedChange(!checked)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
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

data class IDItem(val id: String, val title: String)

@Composable
fun SliderValueEditDialog(
    onDismissRequest: () -> Unit,
    title: String,
    valueRange: ClosedFloatingPointRange<Float>,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit = {},
    intCheck: Boolean = false
) {
    var inputValue by remember { mutableStateOf(value.let { if (intCheck) it.toInt() else it }.toString()) }
    var errorText by remember { mutableStateOf("") }
    val numberFormatError = stringResource(R.string.generic_input_failed_to_number)
    val numberTooSmallError = stringResource(R.string.generic_input_too_small, valueRange.start.toInt())
    val numberTooLargeError = stringResource(R.string.generic_input_too_large, valueRange.endInclusive.toInt())

    SimpleEditDialog(
        title = title,
        value = inputValue,
        onValueChange = { newInput ->
            inputValue = newInput

            val result = if (intCheck) inputValue.toIntOrNull()?.toFloat() else inputValue.toFloatOrNull()
            errorText = when {
                result == null -> numberFormatError
                result < valueRange.start -> numberTooSmallError
                result > valueRange.endInclusive -> numberTooLargeError
                else -> ""
            }
        },
        isError = errorText.isNotEmpty(),
        supportingText = {
            if (errorText.isNotEmpty()) Text(text = errorText)
        },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        onConfirm = {
            if (errorText.isEmpty()) {
                val newValue = inputValue.toFloatOrNull() ?: value
                onValueChange(newValue)
                onValueChangeFinished()
                onDismissRequest()
            }
        },
        onDismissRequest = onDismissRequest
    )
}

@Composable
fun TitleAndSummary(
    modifier: Modifier = Modifier,
    title: String,
    summary: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
    summaryStyle: TextStyle = MaterialTheme.typography.labelSmall
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = titleStyle
        )
        summary?.let { text ->
            Text(
                modifier = Modifier.alpha(0.7f),
                text = text,
                style = summaryStyle
            )
        }
    }
}

@Composable
fun FocusableBox(
    modifier: Modifier = Modifier,
    requestKey: Any? = null,
    canRequestFocus: () -> Boolean = { true }
) {
    val focusRequester = remember { FocusRequester() }
    val currentCanRequestFocus by rememberUpdatedState(canRequestFocus)

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable(enabled = true)
    )

    LaunchedEffect(requestKey) {
        if (currentCanRequestFocus()) {
            focusRequester.requestFocus()
        }
    }
}