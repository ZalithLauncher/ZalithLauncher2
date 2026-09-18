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

package com.movtery.zalithlauncher.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InstallableItem(
    val name: String,
    val summary: String?,
    val task: AbstractUnpackTask
) : Comparable<InstallableItem> {
    private val _state = MutableStateFlow(State.NOT_STARTED)
    /** 当前项的安装状态 */
    val state = _state.asStateFlow()

    fun updateState(state: State) {
        this._state.update { state }
    }

    override fun compareTo(other: InstallableItem): Int {
        return name.compareTo(other.name, ignoreCase = true)
    }

    enum class State {
        /** 未安装 */
        NOT_STARTED,
        /** 需要更新 */
        PENDING,
        /** 安装中 */
        RUNNING,
        /** 已完成/已安装 */
        FINISHED,
        /** 资源不存在，不可安装 */
        NOT_EXISTS
    }
}