package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.ui.screens.content.elements.DisabledAlpha

/**
 * 根据按钮在UI组中的位置，选择不同的圆角形状
 */
enum class ButtonPosition {
    Top, Middle, Bottom, Single
}

/**
 * 根据 UI 组件在组中的位置决定的按钮的形状
 */
@Composable
fun rememberButtonPosShape(
    position: ButtonPosition,
    outerShape: Dp = 28.dp,
    outerShapePressed: Dp = outerShape,
    innerShape: Dp = 4.dp,
    innerShapePressed: Dp = innerShape,
): Shape {
    return remember(position, outerShape, outerShapePressed, innerShape, innerShapePressed) {
        when (position) {
            ButtonPosition.Top -> RoundedCornerShape(
                topStart = outerShape,
                topEnd = outerShape,
                bottomStart = innerShape,
                bottomEnd = innerShape
            )
            ButtonPosition.Middle -> RoundedCornerShape(innerShape)
            ButtonPosition.Bottom -> RoundedCornerShape(
                topStart = innerShape,
                topEnd = innerShape,
                bottomStart = outerShape,
                bottomEnd = outerShape
            )
            ButtonPosition.Single -> RoundedCornerShape(outerShape)
        }
    }
}

private val PositionButtonDefaultsContentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp)

@Composable
private fun PositionButtonBase(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    position: ButtonPosition,
    containerColor: Color,
    contentColor: Color,
    border: BorderStroke?,
    shadowElevation: Dp,
    contentPadding: PaddingValues,
    content: @Composable RowScope.() -> Unit
) {
    val shape = rememberButtonPosShape(position)
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides 0.dp,
        LocalTextStyle provides MaterialTheme.typography.labelLarge
    ) {
        Surface(
            modifier = modifier.semantics { role = Role.Button },
            shape = shape,
            onClick = onClick,
            color = containerColor,
            contentColor = contentColor,
            tonalElevation = 0.dp,
            shadowElevation = shadowElevation,
            border = border
        ) {
            Row(
                modifier = Modifier
                    .defaultMinSize(minHeight = ButtonDefaults.MinHeight)
                    .padding(contentPadding)
                    .alpha(if (enabled) 1f else DisabledAlpha),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

@Composable
fun PositionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    position: ButtonPosition = ButtonPosition.Single,
    contentPadding: PaddingValues = PositionButtonDefaultsContentPadding,
    content: @Composable RowScope.() -> Unit
) = PositionButtonBase(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    position = position,
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    border = null,
    shadowElevation = 0.dp,
    contentPadding = contentPadding,
    content = content
)

@Composable
fun PositionFilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    position: ButtonPosition = ButtonPosition.Single,
    contentPadding: PaddingValues = PositionButtonDefaultsContentPadding,
    content: @Composable RowScope.() -> Unit
) = PositionButtonBase(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    position = position,
    containerColor = MaterialTheme.colorScheme.secondaryContainer,
    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    border = null,
    shadowElevation = 0.dp,
    contentPadding = contentPadding,
    content = content
)

@Composable
fun PositionOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    position: ButtonPosition = ButtonPosition.Single,
    contentPadding: PaddingValues = PositionButtonDefaultsContentPadding,
    content: @Composable RowScope.() -> Unit
) = PositionButtonBase(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    position = position,
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    shadowElevation = 0.dp,
    contentPadding = contentPadding,
    content = content
)

@Composable
fun PositionTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    position: ButtonPosition = ButtonPosition.Single,
    contentPadding: PaddingValues = PositionButtonDefaultsContentPadding,
    content: @Composable RowScope.() -> Unit
) = PositionButtonBase(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    position = position,
    containerColor = Color.Transparent,
    contentColor = MaterialTheme.colorScheme.primary,
    border = null,
    shadowElevation = 0.dp,
    contentPadding = contentPadding,
    content = content
)

@Composable
fun PositionElevatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    position: ButtonPosition = ButtonPosition.Single,
    contentPadding: PaddingValues = PositionButtonDefaultsContentPadding,
    content: @Composable RowScope.() -> Unit
) = PositionButtonBase(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    position = position,
    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor = MaterialTheme.colorScheme.primary,
    border = null,
    shadowElevation = 1.dp,
    contentPadding = contentPadding,
    content = content
)
