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

package com.movtery.zalithlauncher.game.version.mod

import android.os.Parcelable
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeFile
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthVersion
import kotlinx.parcelize.Parcelize

/**
 * 本地模组文件在平台上匹配到的安装信息
 * @param platform 所属平台
 * @param projectId 平台项目ID
 * @param versionId 平台版本（文件）ID
 * @param versionName 平台版本号
 * @param notFound 平台上是否存在该项目；作为指纹未命中时的负缓存标记
 */
@Parcelize
data class InstalledMod(
    val platform: Platform,
    val projectId: String,
    val versionId: String,
    val versionName: String,
    val notFound: Boolean = false
) : Parcelable

/**
 * 指纹匹配到的平台版本转换为本地安装信息
 */
fun PlatformVersion.toInstalledMod(): InstalledMod = when (this) {
    is ModrinthVersion -> InstalledMod(
        platform = Platform.MODRINTH,
        projectId = projectId,
        versionId = id,
        versionName = versionNumber
    )
    is CurseForgeFile -> InstalledMod(
        platform = Platform.CURSEFORGE,
        projectId = modId.toString(),
        versionId = id.toString(),
        versionName = displayName
    )
    else -> error("Unknown version type: $this")
}
