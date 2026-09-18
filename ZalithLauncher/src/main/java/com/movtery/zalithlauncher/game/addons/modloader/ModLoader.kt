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

package com.movtery.zalithlauncher.game.addons.modloader

/**
 * 模组加载器/模组类别枚举
 * @param isLoader 该类别是一个模组加载器
 * @param isApiMod 该类别是一个 API 模组
 * @param autoDownloadable 该模组加载器是启动器支持自动安装的加载器
 */
enum class ModLoader(
    val displayName: String,
    val isLoader: Boolean = true,
    val isApiMod: Boolean = false,
    val autoDownloadable: Boolean = true
) {
    UNKNOWN(displayName = "", isLoader = false, autoDownloadable = false),
    OPTIFINE(displayName = "OptiFine", isLoader = false, autoDownloadable = false),
    FORGE(displayName = "Forge"),
    NEOFORGE(displayName = "NeoForge"),

    FABRIC(displayName = "Fabric"),
    FABRIC_API(displayName = "Fabric API", isLoader = false, isApiMod = true, autoDownloadable = false),

    LEGACY_FABRIC(displayName = "Legacy Fabric"),
    LEGACY_FABRIC_API(displayName = "Legacy Fabric API", isLoader = false, isApiMod = true, autoDownloadable = false),

    BABRIC(displayName = "Babric", autoDownloadable = false),
    BABRIC_API(displayName = "Babric API", isLoader = false, isApiMod = true, autoDownloadable = false),

    QUILT(displayName = "Quilt"),
    QUILT_API(displayName = "Quilted Fabric API", isLoader = false, isApiMod = true, autoDownloadable = false),

    LITE_LOADER(displayName = "LiteLoader", autoDownloadable = false),
    CLEANROOM(displayName = "Cleanroom"),

    PACK(displayName = "Pack", isLoader = false, autoDownloadable = false)
}