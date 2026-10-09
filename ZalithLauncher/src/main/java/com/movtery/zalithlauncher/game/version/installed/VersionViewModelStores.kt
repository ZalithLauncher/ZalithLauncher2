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

package com.movtery.zalithlauncher.game.version.installed

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModelStore

/**
 * 版本作用域的 ViewModelStore 注册表
 *
 * 以 [Version] 对象身份（引用相等）为粒度管理 ViewModelStore：
 * ViewModel 的生命周期跟随 Version 对象，而非承载它的屏幕；
 * 版本列表刷新后，随旧 Version 创建的 store 会被统一销毁。
 */
object VersionViewModelStores {
    private val lock = Any()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val stores = HashMap<Version, ViewModelStore>()

    /** 各版本正被承载屏幕持有的计数，计数归零后才允许清理 */
    private val holderCounts = HashMap<Version, Int>()

    /**
     * 标记一个版本开始被承载屏幕持有
     */
    fun acquire(version: Version) = synchronized(lock) {
        holderCounts.merge(version, 1, Int::plus)
    }

    /**
     * 获取指定版本的 ViewModelStore，不存在则创建
     */
    fun storeOf(version: Version): ViewModelStore = synchronized(lock) {
        stores.getOrPut(version) { ViewModelStore() }
    }

    /**
     * 解除一次持有；版本已不被任何屏幕持有、且不在当前版本列表中时，销毁其 store
     */
    fun releaseAndClearIfStale(version: Version, currentVersions: Collection<Version>) = synchronized(lock) {
        val remaining = holderCounts[version]?.dec() ?: 0
        if (remaining <= 0) {
            holderCounts.remove(version)
            if (currentVersions.none { it === version }) {
                //由承载屏幕的组合销毁触发，必然处于主线程，可直接清理
                stores.remove(version)?.clear()
            }
        } else {
            holderCounts[version] = remaining
        }
    }

    /**
     * 清理所有已不在当前版本列表、且没有任何屏幕持有的 store
     */
    fun clearStale(currentVersions: Collection<Version>) = synchronized(lock) {
        val staleStores = stores.keys.filter { key ->
            key !in holderCounts && currentVersions.none { it === key }
        }.mapNotNull { version ->
            stores.remove(version)
        }
        if (staleStores.isNotEmpty()) {
            mainHandler.post {
                staleStores.forEach { it.clear() }
            }
        }
    }
}
