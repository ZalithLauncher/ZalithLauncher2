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

package com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models

import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses

/**
 * CurseForge 资源搜索类别
 */
enum class CurseForgeClassID(val classID: Int, val slug: String) {
    /** 模组 */
    MOD(6, "mc-mods"),

    /** 整合包 */
    MOD_PACK(4471, "modpacks"),

    /** 资源包 */
    RESOURCE_PACK(12, "texture-packs"),

    /** 存档 */
    SAVES(17, "worlds"),

    /** 光影包 */
    SHADERS(6552, "shaders")
}

/**
 * 获取 CurseForge 资源类别信息
 */
fun CurseForgeData.getClassIdOrNull(): CurseForgeClassID? {
    return if (classId == null) {
        null
    } else {
        CurseForgeClassID.entries.find { id ->
            id.classID == classId
        }
    }
}

/**
 * 获取 CurseForge 平台类别信息
 */
fun CurseForgeData.getPlatformClassesOrNull(): PlatformClasses? {
    val classIdType = getClassIdOrNull() ?: return null
    return when (classIdType) {
        CurseForgeClassID.MOD -> PlatformClasses.MOD
        CurseForgeClassID.MOD_PACK -> PlatformClasses.MOD_PACK
        CurseForgeClassID.RESOURCE_PACK -> PlatformClasses.RESOURCE_PACK
        CurseForgeClassID.SAVES -> PlatformClasses.SAVES
        CurseForgeClassID.SHADERS -> PlatformClasses.SHADERS
    }
}