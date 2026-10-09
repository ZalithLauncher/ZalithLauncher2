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

package com.movtery.zalithlauncher.game.download.assets.platform.mcim

import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.MirrorSourceType
import com.movtery.zalithlauncher.utils.isChinaMainland

private const val ROOT = "https://mod.mcimirror.top"

private val REPLACE_MIRROR_HOLDERS = listOf(
    //CurseForge
    "https://edge.forgecdn.net",
    //Modrinth
    "https://cdn.modrinth.com"
)

private fun orderWithMirrorFallback(
    official: List<String>,
    mirrored: List<String>
): List<String> {
    val preference = AllSettings.assetPlatformSource.getValue()
    return when {
        preference == MirrorSourceType.OFFICIAL -> official
        preference == MirrorSourceType.MIRROR || isChinaMainland() -> mirrored + official
        else -> official + mirrored
    }
}

/**
 * Add the MCIM download URL as a fallback in Auto mode, or try it first when selected.
 */
fun String.mapMCIMMirrorUrls(): List<String> {
    val mirroredUrl = REPLACE_MIRROR_HOLDERS.find { key ->
        startsWith(key)
    }?.let { origin ->
        replaceFirst(origin, ROOT)
    } ?: return listOf(this)

    return orderWithMirrorFallback(
        official = listOf(this),
        mirrored = listOf(mirroredUrl)
    )
}

/**
 * Add mirror alternatives for all supplied download URLs, preserving the selected source order.
 */
fun Array<String>.mapMCIMMirrorUrls(): List<String> {
    val officialUrls = toList()
    val mirroredUrls = mapNotNull { url ->
        REPLACE_MIRROR_HOLDERS.find { key ->
            url.startsWith(key)
        }?.let { origin ->
            url.replaceFirst(origin, ROOT)
        }
    }
    if (mirroredUrls.isEmpty()) return officialUrls

    return orderWithMirrorFallback(
        official = officialUrls,
        mirrored = mirroredUrls
    )
}
