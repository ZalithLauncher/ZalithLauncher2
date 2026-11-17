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

package com.movtery.zalithlauncher.ui.screens

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.SaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay

/**
 * 按嵌套 key 实例分桶的 [SaveableStateHolder] 集合，由 [NavStateHost] 创建并维护
 */
class NavKeyStateHolders internal constructor() {
    internal val holders = HashMap<Any, SaveableStateHolder>()
}

/**
 * 当前活跃的 [NavKeyStateHolders]；未处于 [NavStateHost] 内时为 null
 */
val LocalNavKeyStateHolders = compositionLocalOf<NavKeyStateHolders?> { null }

/**
 * 嵌套导航的状态宿主
 *
 * 为根 key 及其返回栈可达的所有 [BackStackNavKey] 实例各持有一个 [SaveableStateHolder]，
 * holder 的生命周期与实例在返回栈中的可达性严格一致：实例离开返回栈后，其 holder 随锚点离开组合而被回收。
 *
 * 配合 [NestedNavDisplay] 使用，嵌套页面在外层导航切换后仍能恢复状态
 */
@Composable
fun NavStateHost(
    rootKey: BackStackNavKey<*>,
    content: @Composable () -> Unit
) {
    val holders = remember { NavKeyStateHolders() }

    AnchorNavKeys(
        holders = holders,
        navKey = rootKey,
        visited = HashSet(),
        pinChildren = false
    )

    CompositionLocalProvider(LocalNavKeyStateHolders provides holders, content = content)
}

@Composable
private fun AnchorNavKeys(
    holders: NavKeyStateHolders,
    navKey: BackStackNavKey<*>,
    visited: MutableSet<BackStackNavKey<*>>,
    pinChildren: Boolean
) {
    if (!visited.add(navKey)) return
    key(navKey) {
        val holder = rememberSaveableStateHolder()

        // 组合期登记
        // 锚点先于内容组合，保证同一次组合中 NestedNavDisplay 能读到
        holders.holders[navKey] = holder
        DisposableEffect(navKey) {
            onDispose {
                holders.holders.remove(navKey)
            }
        }

        if (pinChildren) {
            // 钉住出现过的子 key，即使离开本实例的子栈，锚点也保留到本锚点销毁为止
            val pinned = remember { mutableStateListOf<BackStackNavKey<*>>() }
            navKey.backStack.forEach { subKey ->
                if (subKey is BackStackNavKey<*> && !pinned.contains(subKey)) {
                    pinned.add(subKey)
                }
            }
            pinned.forEach { subKey ->
                AnchorNavKeys(
                    holders = holders,
                    navKey = subKey,
                    visited = visited,
                    pinChildren = true
                )
            }
        } else {
            navKey.backStack.forEach { subKey ->
                if (subKey is BackStackNavKey<*>) {
                    AnchorNavKeys(
                        holders = holders,
                        navKey = subKey,
                        visited = visited,
                        pinChildren = true
                    )
                }
            }
        }
    }
}

/**
 * 获取 [ownerKey] 实例对应的 [SaveableStateHolder]
 *
 * 未处于 [NavStateHost] 内时回退为本地 holder，行为与 NavDisplay 默认装饰器一致
 */
@Composable
fun rememberNavKeyStateHolder(ownerKey: Any): SaveableStateHolder {
    val fallback = rememberSaveableStateHolder()
    val hoisted = LocalNavKeyStateHolders.current?.holders?.get(ownerKey)
    // 首帧锚点已先于本组合登记，直接固化结果
    // 锚点销毁后保持既有结果，避免退出中的内容回退到空 holder
    val frozen = remember { mutableStateOf(hoisted) }
    return frozen.value ?: fallback
}

/**
 * 嵌套屏幕专用的 NavDisplay
 *
 * 通过 [NavStateHost] 按实例持有的 [SaveableStateHolder] 保存各个 entry 的 UI 状态，
 * 使嵌套页面在外层导航切换并返回后仍能恢复状态
 *
 * @param ownerKey 该导航所属的嵌套 key 实例
 * @param extraDecorators 额外的装饰器，附加在状态保存装饰器之后
 */
@Composable
fun <E : TitledNavKey> NestedNavDisplay(
    ownerKey: Any,
    backStack: NavBackStack<E>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { backStack.removeLastOrNull() },
    extraDecorators: List<NavEntryDecorator<E>> = emptyList(),
    transitionSpec: AnimatedContentTransitionScope<Scene<E>>.() -> ContentTransform,
    popTransitionSpec: AnimatedContentTransitionScope<Scene<E>>.() -> ContentTransform,
    entryProvider: (key: E) -> NavEntry<E>
) {
    val stateHolder = rememberNavKeyStateHolder(ownerKey)
    val saveableDecorator = remember(stateHolder) {
        SaveableStateHolderNavEntryDecorator<E>(stateHolder)
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = onBack,
        entryDecorators = listOf(saveableDecorator) + extraDecorators,
        transitionSpec = transitionSpec,
        popTransitionSpec = popTransitionSpec,
        entryProvider = entryProvider
    )
}
