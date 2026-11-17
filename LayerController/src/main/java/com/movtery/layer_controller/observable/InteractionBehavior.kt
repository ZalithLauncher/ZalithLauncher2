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

package com.movtery.layer_controller.observable

/**
 * 控件的交互行为模型
 */
sealed class InteractionBehavior {
    /**
     * 手指移出控件边界时是否自动释放
     */
    abstract val releaseOnOutOfBounds: Boolean

    /**
     * 是否允许其他控件的指针滑动进入本控件
     */
    abstract val canBeSwipedTo: Boolean

    /**
     * 当本控件处于活跃状态时，是否阻止滑动链向其他控件传播
     */
    abstract val blocksSwipeChain: Boolean



    /**
     * 普通按钮
     * 按下保持，松开释放，不参与滑动联动
     */
    data object Press : InteractionBehavior() {
        override val releaseOnOutOfBounds: Boolean get() = false
        override val canBeSwipedTo: Boolean get() = false
        override val blocksSwipeChain: Boolean get() = false
    }

    /**
     * 可滑动按钮
     * 移出边界自动释放，支持滑动联动
     */
    data object Swipable : InteractionBehavior() {
        override val releaseOnOutOfBounds: Boolean get() = true
        override val canBeSwipedTo: Boolean get() = true
        override val blocksSwipeChain: Boolean get() = false
    }

    /**
     * 可切换按钮：
     * 点击切换开/关，不可滑动联动,活跃时阻止滑动链传播
     */
    data object Toggle : InteractionBehavior() {
        override val releaseOnOutOfBounds: Boolean get() = false
        override val canBeSwipedTo: Boolean get() = false
        override val blocksSwipeChain: Boolean get() = true
    }

    companion object {
        fun from(isSwipple: Boolean, isToggleable: Boolean): InteractionBehavior = when {
            isToggleable -> Toggle
            isSwipple -> Swipable
            else -> Press
        }
    }
}
