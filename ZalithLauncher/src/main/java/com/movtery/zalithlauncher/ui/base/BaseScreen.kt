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

package com.movtery.zalithlauncher.ui.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import com.movtery.zalithlauncher.ui.screens.TitledNavKey

/**
 * 单层级基础屏幕，根据 `currentKey` 判断当前屏幕是否可见
 * @param screenKey 当前屏幕的Key
 * @param currentKey 当前屏幕正在展示的Key
 * @param useClassEquality 是否使用类相等判断
 */
@Composable
fun BaseScreen(
    screenKey: TitledNavKey,
    currentKey: TitledNavKey?,
    useClassEquality: Boolean = false,
    content: @Composable (isVisible: Boolean) -> Unit,
) {
    val targetVisible = remember(currentKey, screenKey, useClassEquality) {
        isTagVisible(screenKey, currentKey, useClassEquality)
    }

    //初始不可见，用于触发首次的 false -> true 动画
    val visibleState = remember { mutableStateOf(false) }

    //仅在 composition 完成后，才允许更新可见状态
    LaunchedEffect(targetVisible) {
        visibleState.value = targetVisible
    }

    BaseScreen(
        content = content,
        visible = visibleState.value
    )
}

/**
 * 多层级基础屏幕，根据层级列表中的每个层级Key判断当前屏幕是否可见
 * @param levels 层级列表，每个层级包含（Key、当前Key、是否启用引用相等）
 */
@Composable
fun BaseScreen(
    vararg levels: Triple<TitledNavKey, TitledNavKey?, Boolean>,
    content: @Composable (isVisible: Boolean) -> Unit,
) {
    val targetVisible = remember(levels) {
        levels.all { (tag, currentKey, useReferenceEquality) ->
            isTagVisible(tag, currentKey, useReferenceEquality)
        }
    }

    //初始不可见，用于触发首次的 false -> true 动画
    val visibleState = remember { mutableStateOf(false) }

    //仅在 composition 完成后，才允许更新可见状态
    LaunchedEffect(targetVisible) {
        visibleState.value = targetVisible
    }

    BaseScreen(
        content = content,
        visible = visibleState.value
    )
}

/**
 * 多层级基础屏幕，根据层级列表中的每个层级Key判断当前屏幕是否可见
 */
@Composable
fun BaseScreen(
    levels1: List<Pair<Class<out TitledNavKey>, TitledNavKey?>>,
    vararg levels2: Triple<TitledNavKey, TitledNavKey?, Boolean>,
    content: @Composable (isVisible: Boolean) -> Unit,
) {
    val targetVisible = remember(levels1, levels2) {
        val v1 = levels1.all { (key, currentKey) ->
            isTagVisible(key, currentKey)
        }
        val v2 = levels2.all { (key, currentKey, useClassEquality) ->
            isTagVisible(key, currentKey, useClassEquality)
        }
        v1 && v2
    }

    //初始不可见，用于触发首次的 false -> true 动画
    val visibleState = remember { mutableStateOf(false) }

    //仅在 composition 完成后，才允许更新可见状态
    LaunchedEffect(targetVisible) {
        visibleState.value = targetVisible
    }

    BaseScreen(
        content = content,
        visible = visibleState.value
    )
}

@Composable
private fun BaseScreen(
    content: @Composable (isVisible: Boolean) -> Unit,
    visible: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        content(visible)

        if (!visible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    )
            )
        }
    }
}

private fun isTagVisible(key: Class<out TitledNavKey>, current: TitledNavKey?): Boolean {
    return key.isInstance(current)
}

/**
 * @param useClassEquality 是否使用类相等判断
 */
private fun isTagVisible(key: TitledNavKey, current: TitledNavKey?, useClassEquality: Boolean): Boolean {
    return when {
        current == null -> false
        useClassEquality -> key::class == current::class
        else -> key == current
    }
}