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

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.androidText
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 嵌套NavDisplay的屏幕
 */
sealed interface NestedNavKey {
    /** 启动屏幕 */
    @Serializable class Splash : BackStackNavKey<TitledNavKey>() {
        init {
            backStack.addIfEmpty(NormalNavKey.UnpackDeps)
        }
    }
    /** 主屏幕 */
    @Serializable class Main : BackStackNavKey<TitledNavKey>() {
        init {
            backStack.addIfEmpty(NormalNavKey.LauncherMain)
        }
    }
    /** 设置屏幕 */
    @Serializable class Settings : BackStackNavKey<TitledNavKey>(androidText(R.string.generic_setting)) {
        init {
            backStack.addIfEmpty(NormalNavKey.Settings.Renderer)
        }
    }
    /** 版本详细设置屏幕 */
    @Serializable
    class VersionSettings(@Contextual val version: Version) : BackStackNavKey<TitledNavKey>(
        androidText(R.string.page_title_version_manage)
    ) {
        init {
            backStack.addIfEmpty(NormalNavKey.Versions.OverView)
        }
    }
    /** 导出整合包屏幕 */
    @Serializable
    class VersionExport(@Contextual val version: Version) : BackStackNavKey<TitledNavKey>(
        androidText(R.string.versions_export)
    ) {
        init {
            backStack.addIfEmpty(NormalNavKey.VersionExports.SelectType)
        }
    }
    /** 下载屏幕 */
    @Serializable class Download : BackStackNavKey<TitledNavKey>(
        androidText(R.string.generic_download)
    )

    //下载嵌套子屏幕
    /** 下载游戏屏幕 */
    @Serializable class DownloadGame : BackStackNavKey<TitledNavKey>(
        androidText(R.string.download_category_game)
    ) {
        init {
            backStack.addIfEmpty(NormalNavKey.DownloadGame.SelectGameVersion)
        }
    }
    /** 下载整合包屏幕 */
    @Serializable class DownloadModPack : BackStackNavKey<TitledNavKey>(
        androidText(R.string.download_category_modpack)
    ) {
        init {
            backStack.addIfEmpty(NormalNavKey.SearchModPack)
        }
    }
    /** 下载模组屏幕 */
    @Serializable class DownloadMod : BackStackNavKey<TitledNavKey>(
        androidText(R.string.download_category_mod)
    ) {
        init {
            backStack.addIfEmpty(NormalNavKey.SearchMod)
        }
    }
    /** 下载资源包屏幕 */
    @Serializable class DownloadResourcePack : BackStackNavKey<TitledNavKey>(
        androidText(R.string.download_category_resource_pack)
    ) {
        init {
            backStack.addIfEmpty(NormalNavKey.SearchResourcePack)
        }
    }
    /** 下载存档屏幕 */
    @Serializable class DownloadSaves : BackStackNavKey<TitledNavKey>(
        androidText(R.string.download_category_saves)
    ) {
        init {
            backStack.addIfEmpty(NormalNavKey.SearchSaves)
        }
    }
    /** 下载光影包屏幕 */
    @Serializable class DownloadShaders : BackStackNavKey<TitledNavKey>(
        androidText(R.string.download_category_shaders)
    ) {
        init {
            backStack.addIfEmpty(NormalNavKey.SearchShaders)
        }
    }
    /** 查看Addons资源信息屏幕 */
    @Serializable
    class AssetInfo(
        val platform: Platform,
        val projectId: String,
        val classes: PlatformClasses
    ) : BackStackNavKey<TitledNavKey>(
        androidText(R.string.generic_download)
    ) {
        init {
            backStack.addIfEmpty(
                NormalNavKey.DownloadAssets(platform, projectId, classes)
            )
        }
    }
    /** 资源收藏屏幕 */
    @Serializable class DownloadFavorites : BackStackNavKey<TitledNavKey>(
        androidText(R.string.download_favorites_title)
    ) {
        init {
            backStack.addIfEmpty(NormalNavKey.FavoritesList)
        }
    }
}
