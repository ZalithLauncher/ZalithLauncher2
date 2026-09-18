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
import com.movtery.layer_controller.observable.Modifiable
import com.movtery.layer_controller.utils.checkInRange
import com.movtery.layer_controller.utils.randomUUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 圆角比例取值范围
 */
val SHAPE_PERCENT_RANGE: IntRange = 0..50

/**
 * 大小比例计算取值范围
 */
val SIZE_PERCENT_RANGE: ClosedFloatingPointRange<Float> = 0.0f..1.0f

/**
 * 边框比例计算取值范围
 */
val BORDER_RADIO_RANGE: IntRange = 0..50

/**
 * @param name 样式显示名称
 * @param commonStyle 共用亮色主题
 * @param lightStyle 亮色模式样式
 * @param darkStyle 暗色模式样式
 */
@Serializable
data class JoystickStyle(
    @SerialName("name")
    val name: String,
    @SerialName("uuid")
    val uuid: String,
    @SerialName("commonStyle")
    val commonStyle: Boolean = true,
    @SerialName("lightStyle")
    val lightStyle: StyleConfig,
    @SerialName("darkStyle")
    val darkStyle: StyleConfig
): Modifiable<JoystickStyle> {
    /**
     * @param alpha 整体不透明度
     * @param backgroundColor 背景层的颜色
     * @param joystickColor 摇杆的颜色
     * @param joystickCanLockColor 摇杆移动到可以锁定的位置时，摇杆的颜色
     * @param joystickLockedColor 摇杆锁定时的颜色
     * @param lockMarkColor 锁定标记的颜色
     * @param borderWidthRatio 边框粗细
     * @param borderColor 边框颜色
     * @param backgroundShape 背景层的形状：圆角百分比值 0.0f~50.0f
     * @param joystickShape 摇杆的形状：圆角百分比值 0.0f~50.0f
     * @param joystickSize 摇杆的大小：根据背景层的大小进行缩放 0.0f~1.0f
     */
    @Serializable
    data class StyleConfig(
        @SerialName("alpha")
        val alpha: Float,
        @SerialName("backgroundColor")
        @Contextual val backgroundColor: Color,
        @SerialName("joystickColor")
        @Contextual val joystickColor: Color,
        @SerialName("joystickCanLockColor")
        @Contextual val joystickCanLockColor: Color,
        @SerialName("joystickLockedColor")
        @Contextual val joystickLockedColor: Color,
        @SerialName("lockMarkColor")
        @Contextual val lockMarkColor: Color,
        @SerialName("borderWidthRatio")
        val borderWidthRatio: Int = 0,
        @SerialName("borderColor")
        @Contextual val borderColor: Color,
        @SerialName("backgroundShape")
        val backgroundShape: Int,
        @SerialName("joystickShape")
        val joystickShape: Int,
        @SerialName("joystickSize")
        val joystickSize: Float
    ): Modifiable<StyleConfig> {
        init {
            checkInRange("alpha", alpha, ALPHA_RANGE)
            checkInRange("backgroundShape", backgroundShape, SHAPE_PERCENT_RANGE)
            checkInRange("joystickShape", joystickShape, SHAPE_PERCENT_RANGE)
            checkInRange("joystickSize", joystickSize, SIZE_PERCENT_RANGE)
            checkInRange("borderWidthRatio", borderWidthRatio, BORDER_RADIO_RANGE)
        }

        override fun isModified(other: StyleConfig): Boolean {
            return this.alpha != other.alpha ||
                    this.backgroundColor != other.backgroundColor ||
                    this.joystickColor != other.joystickColor ||
                    this.joystickCanLockColor != other.joystickCanLockColor ||
                    this.joystickLockedColor != other.joystickLockedColor ||
                    this.lockMarkColor != other.lockMarkColor ||
                    this.borderWidthRatio != other.borderWidthRatio ||
                    this.borderColor != other.borderColor ||
                    this.backgroundShape != other.backgroundShape ||
                    this.joystickShape != other.joystickShape ||
                    this.joystickSize != other.joystickSize
        }
    }

    override fun isModified(other: JoystickStyle): Boolean {
        return this.uuid != other.uuid ||
                this.lightStyle.isModified(other.lightStyle) ||
                this.darkStyle.isModified(other.darkStyle)
    }
}

val DefaultJoystickStyleConfig = JoystickStyle.StyleConfig(
    alpha = 1.0f,
    backgroundColor = Color.Black.copy(0.5f),
    joystickColor = Color.White.copy(alpha = 0.5f),
    joystickCanLockColor = Color.Yellow.copy(alpha = 0.5f),
    joystickLockedColor = Color.Green.copy(alpha = 0.5f),
    lockMarkColor = Color.White,
    borderWidthRatio = 0,
    borderColor = Color.White,
    backgroundShape = 50,
    joystickShape = 50,
    joystickSize = 0.5f
)

val DefaultJoystickStyle = JoystickStyle(
    name = "Default",
    uuid = randomUUID(),
    lightStyle = DefaultJoystickStyleConfig,
    darkStyle = DefaultJoystickStyleConfig
)

fun JoystickStyle.cloneNew(): JoystickStyle {
    return JoystickStyle(
        name = name,
        uuid = randomUUID(),
        lightStyle = lightStyle,
        darkStyle = darkStyle,
    )
}

fun createNewJoystickStyle(name: String): JoystickStyle =
    DefaultJoystickStyle.copy(
        name = name,
        uuid = randomUUID()
    )