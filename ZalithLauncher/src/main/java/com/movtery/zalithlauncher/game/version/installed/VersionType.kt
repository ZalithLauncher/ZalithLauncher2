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

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.version.installed.VersionType.MODLOADERS
import com.movtery.zalithlauncher.game.version.installed.VersionType.UNKNOWN
import com.movtery.zalithlauncher.game.version.installed.VersionType.VANILLA

/**
 * 版本类型，区分原版、模组加载器
 */
enum class VersionType {
    /**
     * 原版
     */
    VANILLA,

    /**
     * 带有模组加载器
     */
    MODLOADERS,

    /**
     * 不清楚，无法判断
     */
    UNKNOWN
}

private val loaders = ModLoader.entries.filter { it.isLoader }

/**
 * 通过版本信息，尝试识别版本类型
 */
fun VersionInfo?.getVersionType(): VersionType {
    return when {
        this != null -> {
            when {
                loaderInfo == null || loaderInfo.loader == ModLoader.OPTIFINE -> VANILLA
                loaderInfo.loader in loaders -> MODLOADERS
                else -> UNKNOWN
            }
        }
        else -> UNKNOWN
    }
}

