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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionViewModelStores
import com.movtery.zalithlauncher.game.version.installed.VersionsManager

/**
 * 以版本作用域的 [ViewModelStore] 覆盖 [LocalViewModelStoreOwner]
 * 作用域内创建的 ViewModel 生命周期跟随 [version] 对象
 * 退出承载屏幕后保留，版本列表刷新、版本对象失效后销毁
 */
@Composable
fun VersionViewModelStoreScope(
    version: Version,
    content: @Composable () -> Unit
) {
    val host = checkNotNull(LocalViewModelStoreOwner.current)
    val storeOwner = remember(version, host) {
        VersionViewModelStores.acquire(version)
        VersionScopedViewModelStoreOwner(version, host)
    }

    DisposableEffect(version) {
        onDispose {
            VersionViewModelStores.releaseAndClearIfStale(
                version = version,
                currentVersions = VersionsManager.versions.value
            )
        }
    }

    CompositionLocalProvider(
        LocalViewModelStoreOwner provides storeOwner,
        content = content
    )
}

private class VersionScopedViewModelStoreOwner(
    private val version: Version,
    private val host: ViewModelStoreOwner
) : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
    private val hasDefault = host as? HasDefaultViewModelProviderFactory

    override val viewModelStore: ViewModelStore
        get() = VersionViewModelStores.storeOf(version)

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = checkNotNull(hasDefault) {
            "Host ViewModelStoreOwner must implement HasDefaultViewModelProviderFactory"
        }.defaultViewModelProviderFactory

    override val defaultViewModelCreationExtras: CreationExtras
        get() = hasDefault?.defaultViewModelCreationExtras ?: CreationExtras.Empty
}