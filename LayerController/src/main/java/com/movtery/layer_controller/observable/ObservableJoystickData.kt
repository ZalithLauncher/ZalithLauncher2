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

package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import com.movtery.layer_controller.data.ButtonPosition
import com.movtery.layer_controller.data.ButtonSize
import com.movtery.layer_controller.data.JoystickData
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.event.EventHandler
import com.movtery.layer_controller.layout.JoystickDirection
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * 可观察的JoystickData包装类
 */
class ObservableJoystickData(data: JoystickData) : ObservableWidget() {
    val uuid: String = data.uuid
    var position by mutableStateOf(data.position)
    var size by mutableStateOf(data.size)
    val style = ObservableJoystickStyle(data.style)

    var deadZoneRatio by mutableFloatStateOf(data.deadZoneRatio)
    var lockThreshold by mutableFloatStateOf(data.lockThreshold)
    var canLock by mutableStateOf(data.canLock)
    var lockSpring by mutableStateOf(data.lockSpring)

    /** 摇杆当前相对于中心的偏移量 (Px) */
    var joystickOffset by mutableStateOf(Offset.Zero)

    /** 摇杆当前方向 */
    var currentDirection by mutableStateOf(JoystickDirection.None)

    /** 摇杆当前是否可锁定 */
    var canLockState by mutableStateOf(false)

    /** 摇杆当前是否已锁定 */
    var isLockedState by mutableStateOf(false)

    var onDirectionChanged: (JoystickDirection) -> Unit = {}
    var onCanLock: (Boolean) -> Unit = {}
    var onLock: (Boolean) -> Unit = {}

    override val behavior: InteractionBehavior
        get() = InteractionBehavior.Press

    override val internalRenderPosition: ButtonPosition
        get() = position

    override fun putRenderPosition(position: ButtonPosition) {
        this.position = position
    }

    override val styleId: String?
        get() = null

    override val widgetSize: ButtonSize
        get() = size

    override fun putWidgetSize(size: ButtonSize) {
        //确保宽高一致
        this.size = size.copy(
            heightDp = size.widthDp,
            heightPercentage = size.widthPercentage,
            heightReference = size.widthReference
        )
    }

    override fun onCompositionStart(eventHandler: EventHandler?) {
    }

    override fun onCompositionDispose(eventHandler: EventHandler?) {
        currentDirection = JoystickDirection.None
        onDirectionChanged(JoystickDirection.None)
    }

    override fun onCheckVisibilityType(): VisibilityType {
        return VisibilityType.ALWAYS
    }

    override fun supportsDeepTouchDetection(): Boolean {
        return true
    }

    override fun canProcess(): Boolean {
        // 返回 true，表示一旦选中摇杆，它将独占该指针事件，不再向下透传给底层按钮
        return true
    }

    override fun onTouchEvent(
        eventHandler: EventHandler,
        allLayers: List<ObservableControlLayer>,
        change: PointerInputChange,
        activeWidgets: List<ObservableWidget>,
        addThis: () -> Unit,
        consumeEvent: (Boolean) -> Unit
    ) {
        if (!change.pressed) return

        val isAlreadyActive = this in activeWidgets
        val isFirstPress = activeWidgets.isEmpty()

        if (isAlreadyActive || isFirstPress) {
            if (isFirstPress) {
                addThis()
                if (isLockedState) {
                    isLockedState = false
                    // 如果当前处于锁定状态，按下时应立即通知外部解锁
                    onLock(false)
                }
            }

            val center = Offset(
                internalRenderOffsetPx.x + internalRenderSize.width / 2f,
                internalRenderOffsetPx.y + internalRenderSize.height / 2f
            )
            val pos = change.position
            val deadZoneRadius = (internalRenderSize.width * deadZoneRatio) / 2f
            val lockThresholdPx = internalRenderSize.width * lockThreshold

            updateState(pos, center, deadZoneRadius, lockThresholdPx)
            consumeEvent(true)
        }
    }

    private fun updateState(
        pos: Offset,
        center: Offset,
        deadZoneRadius: Float,
        lockThresholdPx: Float
    ) {
        val vector = pos - center
        val distance = sqrt(vector.x * vector.x + vector.y * vector.y)
        val maxDistance = internalRenderSize.width / 2f

        val clampedVector = if (distance > 0f) {
            if (distance > maxDistance) {
                vector * (maxDistance / distance)
            } else {
                vector
            }
        } else {
            Offset.Zero
        }

        joystickOffset = clampedVector

        val newDirection = calculateDirection(clampedVector, deadZoneRadius)
        if (currentDirection != newDirection) {
            currentDirection = newDirection
            onDirectionChanged(newDirection)
        }

        val newCanLock =
            canLock && currentDirection == JoystickDirection.North && pos.y < center.y - lockThresholdPx
        if (canLockState != newCanLock) {
            canLockState = newCanLock
            onCanLock(newCanLock)
        }
    }

    private fun calculateDirection(vector: Offset, deadZoneRadius: Float): JoystickDirection {
        val distance = sqrt(vector.x * vector.x + vector.y * vector.y)
        if (distance < deadZoneRadius) return JoystickDirection.None
        val angle = Math.toDegrees(atan2(vector.y.toDouble(), vector.x.toDouble())).toFloat()
        return when {
            angle >= -22.5f && angle < 22.5f -> JoystickDirection.East
            angle in 22.5f..<67.5f -> JoystickDirection.SouthEast
            angle in 67.5f..<112.5f -> JoystickDirection.South
            angle in 112.5f..<157.5f -> JoystickDirection.SouthWest
            angle >= 157.5f || angle < -157.5f -> JoystickDirection.West
            angle >= -157.5f && angle < -112.5f -> JoystickDirection.NorthWest
            angle >= -112.5f && angle < -67.5f -> JoystickDirection.North
            angle >= -67.5f && angle < -22.5f -> JoystickDirection.NorthEast
            else -> JoystickDirection.None
        }
    }

    override fun isReleaseOnOutOfBounds(): Boolean {
        return false
    }

    override fun onPointerBackInBounds(
        eventHandler: EventHandler,
        allLayers: List<ObservableControlLayer>
    ) {
    }

    override fun onReleaseEvent(
        eventHandler: EventHandler,
        allLayers: List<ObservableControlLayer>
    ) {
        if (canLockState) {
            isLockedState = true
            joystickOffset = Offset(0f, -internalRenderSize.height / 2f)
            currentDirection = JoystickDirection.North
            onDirectionChanged(JoystickDirection.North)
            onLock(true)
        } else {
            isLockedState = false
            joystickOffset = Offset.Zero
            currentDirection = JoystickDirection.None
            onDirectionChanged(JoystickDirection.None)
            onLock(false)
        }
    }

    fun packJoystick(): JoystickData {
        return JoystickData(
            uuid = uuid,
            position = position,
            size = size,
            style = style.pack(),
            deadZoneRatio = deadZoneRatio,
            lockThreshold = lockThreshold,
            canLock = canLock,
            lockSpring = lockSpring
        )
    }

    /**
     * 重置摇杆状态（偏移量、方向、锁定状态等）
     */
    fun resetState() {
        joystickOffset = Offset.Zero
        currentDirection = JoystickDirection.None
        onDirectionChanged(JoystickDirection.None)
        canLockState = false
        onCanLock(false)
        isLockedState = false
        onLock(false)
    }
}
