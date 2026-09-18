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

package com.movtery.zalithlauncher.game.versioninfo.models

import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersion
import com.movtery.zalithlauncher.game.versioninfo.allAprilFools

/**
 * 简易的版本类型过滤器，过滤版本：正式版、快照版、远古版
 * @param release 是否保留正式版
 * @param snapshot 是否保留快照版
 * @param old 是否保留远古版
 */
fun List<VersionManifest.Version>.filterType(
    release: Boolean,
    snapshot: Boolean,
    old: Boolean
) = this.filter { version ->
    version.isType(release, snapshot, old)
}

/**
 * 将 [VersionManifest.Version] 列表映射为 [MinecraftVersion] 列表
 */
fun List<VersionManifest.Version>.mapVersion(): List<MinecraftVersion> {
    return this.map { version ->
        //检查其是否为愚人节版
        val aprilFoolsVersion = allAprilFools.find { it.version.equals(version.id, ignoreCase = true) }

        MinecraftVersion(
            version = version,
            type = if (aprilFoolsVersion != null) {
                //确认为愚人节版本
                MinecraftVersion.Type.AprilFools
            } else {
                when (version.type) {
                    "release" -> MinecraftVersion.Type.Release
                    "snapshot", "pending", "unobfuscated" -> MinecraftVersion.Type.Snapshot
                    "old_beta" -> MinecraftVersion.Type.OldBeta
                    "old_alpha" -> MinecraftVersion.Type.OldAlpha
                    else -> MinecraftVersion.Type.Unknown
                }
            },
            summary = aprilFoolsVersion?.type?.summary, //暂时仅为愚人节版提供描述
            urlSuffix = aprilFoolsVersion?.type?.urlSuffix
        )
    }
}

/**
 * 检查版本类型是否匹配给定的类型
 * @param release 如果该版本为正式版，则返回它的值
 * @param snapshot 如果该版本为快照版，则返回它的值
 * @param aprilFools 如果该版本为愚人节版，则返回它的值
 * @param old 如果该版本为远古版，则返回它的值
 */
fun MinecraftVersion.isType(
    release: Boolean,
    snapshot: Boolean,
    aprilFools: Boolean,
    old: Boolean
) = when (type) {
    MinecraftVersion.Type.Release -> release
    MinecraftVersion.Type.Snapshot -> snapshot
    MinecraftVersion.Type.OldBeta -> old
    MinecraftVersion.Type.OldAlpha -> old
    MinecraftVersion.Type.AprilFools -> aprilFools
    MinecraftVersion.Type.Unknown -> old //未知版本，默认归类到远古版
}

/**
 * 检查版本类型是否匹配给定的类型
 * @param release 如果该版本为正式版，则返回它的值
 * @param snapshot 如果该版本为快照版，则返回它的值
 * @param old 如果该版本为远古版，则返回它的值
 */
fun VersionManifest.Version.isType(
    release: Boolean,
    snapshot: Boolean,
    old: Boolean
) = when (type) {
    "release" -> release
    "snapshot", "pending" -> snapshot
    else -> old && type.startsWith("old")
}