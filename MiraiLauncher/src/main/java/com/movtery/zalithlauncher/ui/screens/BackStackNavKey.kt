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

package com.movtery.zalithlauncher.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import com.movtery.zalithlauncher.ui.AndroidStringText
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.reflect.KClass

@Serializable
abstract class BackStackNavKey<E: TitledNavKey>(
    @Transient private val initTitle: AndroidStringText? = null
) : TitledNavKey {
    override var title by mutableStateOf(initTitle)

    /** 当前屏幕正在使用的堆栈 */
    @Contextual
    val backStack: NavBackStack<E> = NavBackStack()
    /** 当前屏幕的Key */
    var currentKey by mutableStateOf<E?>(null)

    @Suppress("unused")
    fun navigateOnce(key: E) {
        backStack.navigateOnce(key)
    }

    fun navigateTo(screenKey: E, useClassEquality: Boolean = false) {
        backStack.navigateTo(screenKey, useClassEquality)
    }

    fun removeAndNavigateTo(remove: KClass<*>, screenKey: E, useClassEquality: Boolean = false) {
        backStack.removeAndNavigateTo(remove, screenKey, useClassEquality)
    }

    fun removeAndNavigateTo(removes: List<KClass<*>>, screenKey: E, useClassEquality: Boolean = false) {
        backStack.removeAndNavigateTo(removes, screenKey, useClassEquality)
    }

    fun clearWith(navKey: E) {
        backStack.clearWith(navKey)
    }
}