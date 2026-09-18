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

package com.movtery.layer_controller.data

import androidx.compose.ui.graphics.Color
import com.movtery.layer_controller.data.ButtonStyle.StyleConfig
import com.movtery.layer_controller.observable.Modifiable
import com.movtery.layer_controller.utils.checkInRange
import com.movtery.layer_controller.utils.randomUUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 不透明度取值范围
 */
val ALPHA_RANGE: ClosedFloatingPointRange<Float> = 0.0f..1.0f

/**
 * 文本大小取值范围
 */
val FONT_SIZE_RANGE: ClosedFloatingPointRange<Float> = 2.0f..30.0f

/**
 * 默认文本大小（自定义时）
 */
const val DEFAULT_FONT_SIZE = 14

/**
 * 边框宽度取值范围
 */
val BORDER_WIDTH: ClosedFloatingPointRange<Float> = 0.0f..50.0f

/**
 * @param name 样式显示名称
 * @param animateSwap 在切换状态时，是否启用动画过渡
 * @param commonStyle 共用亮色主题
 * @param lightStyle 亮色模式样式
 * @param darkStyle 暗色模式样式
 */
@Serializable
data class ButtonStyle(
    @SerialName("name")
    val name: String,
    @SerialName("uuid")
    val uuid: String,
    @SerialName("animateSwap")
    val animateSwap: Boolean,
    @SerialName("commonStyle")
    val commonStyle: Boolean = true,
    @SerialName("lightStyle")
    val lightStyle: StyleConfig,
    @SerialName("darkStyle")
    val darkStyle: StyleConfig
): Modifiable<ButtonStyle> {
    /**
     * @param alpha 整体不透明度
     * @param backgroundColor 背景颜色
     * @param contentColor 内容颜色
     * @param fontSize 文字大小，为null时则默认视为TextUnit.Unspecified
     * @param borderWidth 边框粗细
     * @param borderColor 边框颜色
     * @param borderRadius 圆角尺寸
     * @param pressedAlpha 按下时，整体不透明度
     * @param pressedBackgroundColor 按下时，背景颜色
     * @param pressedContentColor 按下时，内容颜色
     * @param pressedFontSize 按下时，文字大小，为null时则默认视为TextUnit.Unspecified
     * @param pressedBorderWidth 按下时，边框粗细
     * @param pressedBorderColor 按下时，边框颜色
     * @param pressedBorderRadius 按下时，圆角尺寸
     */
    @Serializable
    data class StyleConfig(
        @SerialName("alpha")
        val alpha: Float,
        @SerialName("pressedAlpha")
        val pressedAlpha: Float,
        @SerialName("backgroundColor")
        @Contextual val backgroundColor: Color,
        @SerialName("pressedBackgroundColor")
        @Contextual val pressedBackgroundColor: Color,
        @SerialName("contentColor")
        @Contextual val contentColor: Color,
        @SerialName("pressedContentColor")
        @Contextual val pressedContentColor: Color,
        @SerialName("fontSize")
        val fontSize: Int? = null,
        @SerialName("pressedFontSize")
        val pressedFontSize: Int? = null,
        @SerialName("borderWidth")
        val borderWidth: Int,
        @SerialName("pressedBorderWidth")
        val pressedBorderWidth: Int,
        @SerialName("borderColor")
        @Contextual val borderColor: Color,
        @SerialName("pressedBorderColor")
        @Contextual val pressedBorderColor: Color,
        @SerialName("borderRadius")
        val borderRadius: ButtonShape,
        @SerialName("pressedBorderRadius")
        val pressedBorderRadius: ButtonShape
    ): Modifiable<StyleConfig> {
        init {
            checkInRange("alpha", alpha, ALPHA_RANGE)
            checkInRange("pressedAlpha", pressedAlpha, ALPHA_RANGE)
            if (fontSize != null) {
                checkInRange("fontSize", fontSize.toFloat(), FONT_SIZE_RANGE)
            }
            if (pressedFontSize != null) {
                checkInRange("pressedFontSize", pressedFontSize.toFloat(), FONT_SIZE_RANGE)
            }
            checkInRange("borderWidth", borderWidth.toFloat(), BORDER_WIDTH)
            checkInRange("pressedBorderWidth", pressedBorderWidth.toFloat(), BORDER_WIDTH)
        }

        override fun isModified(other: StyleConfig): Boolean {
            return this.alpha != other.alpha ||
                    this.pressedAlpha != other.pressedAlpha ||
                    this.backgroundColor != other.backgroundColor ||
                    this.pressedBackgroundColor != other.pressedBackgroundColor ||
                    this.contentColor != other.contentColor ||
                    this.pressedContentColor != other.pressedContentColor ||
                    this.borderWidth != other.borderWidth ||
                    this.pressedBorderWidth != other.pressedBorderWidth ||
                    this.borderColor != other.borderColor ||
                    this.pressedBorderColor != other.pressedBorderColor ||
                    this.borderRadius.isModified(other.borderRadius) ||
                    this.pressedBorderRadius.isModified(other.pressedBorderRadius)
        }
    }

    override fun isModified(other: ButtonStyle): Boolean {
        return this.name != other.name ||
                this.uuid != other.uuid ||
                this.animateSwap != other.animateSwap ||
                this.commonStyle != other.commonStyle ||
                this.lightStyle.isModified(other.lightStyle) ||
                this.darkStyle.isModified(other.darkStyle)
    }
}

val DefaultButtonStyleConfig = StyleConfig(
    alpha = 1f,
    backgroundColor = Color.Black.copy(alpha = 0.5f),
    contentColor = Color.White,
    fontSize = null,
    borderWidth = 0,
    borderColor = Color.White,
    borderRadius = ButtonShape(0f),
    pressedAlpha = 1f,
    pressedBackgroundColor = Color.Gray.copy(alpha = 0.7f),
    pressedContentColor = Color.White,
    pressedFontSize = null,
    pressedBorderWidth = 0,
    pressedBorderColor = Color.White,
    pressedBorderRadius = ButtonShape(0f)
)

val DefaultButtonStyle = ButtonStyle(
    name = "Default",
    uuid = randomUUID(),
    animateSwap = false,
    commonStyle = true,
    lightStyle = DefaultButtonStyleConfig,
    darkStyle = DefaultButtonStyleConfig
)

fun createNewButtonStyle(name: String): ButtonStyle = DefaultButtonStyle.copy(name = name, uuid = randomUUID())

/**
 * 克隆一个新的ButtonStyle对象（UUID不同）
 */
fun ButtonStyle.cloneNew(): ButtonStyle = ButtonStyle(
    name = name,
    uuid = randomUUID(),
    animateSwap = animateSwap,
    commonStyle = commonStyle,
    lightStyle = lightStyle,
    darkStyle = darkStyle
)