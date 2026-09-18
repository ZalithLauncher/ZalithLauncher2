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

import com.movtery.zalithlauncher.game.addons.mirror.MirrorPriority
import com.movtery.zalithlauncher.game.addons.mirror.orderCandidates
import com.movtery.zalithlauncher.game.addons.mirror.resolveMirrorPriority
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.isChinaMainland

private const val ROOT = "https://mod.mcimirror.top"

private val REPLACE_MIRROR_HOLDERS = listOf(
    //CurseForge
    "https://edge.forgecdn.net",
    //Modrinth
    "https://cdn.modrinth.com"
)

private fun assetPlatformPriority(): MirrorPriority =
    resolveMirrorPriority(AllSettings.assetPlatformSource.getValue(), mainland = true)

/**
 * 按是否中国大陆决定是否启用 MCIM 镜像，并按偏好生成有序候选链接
 */
fun String.mapMCIMMirrorUrls(): List<String> {
    if (!isChinaMainland()) return listOf(this)

    val mirroredUrl = REPLACE_MIRROR_HOLDERS.find { key ->
        startsWith(key)
    }?.let { origin ->
        replaceFirst(origin, ROOT)
    }

    return orderCandidates(official = this, mirror = mirroredUrl, priority = assetPlatformPriority())
}

/**
 * 多链接形式：把数组内可被镜像替换的链接生成镜像版本，按偏好穿插到原列表前/后
 */
fun Array<String>.mapMCIMMirrorUrls(): List<String> {
    if (!isChinaMainland()) return toList()

    val sources = mapNotNull { url ->
        REPLACE_MIRROR_HOLDERS.find { key ->
            url.startsWith(key)
        }?.let { origin ->
            url.replaceFirst(origin, ROOT)
        }
    }
    if (sources.isEmpty()) return toList()

    return when (assetPlatformPriority()) {
        MirrorPriority.OFFICIAL_FIRST -> this.toList() + sources
        MirrorPriority.MIRROR_FIRST -> sources + this.toList()
    }
}
