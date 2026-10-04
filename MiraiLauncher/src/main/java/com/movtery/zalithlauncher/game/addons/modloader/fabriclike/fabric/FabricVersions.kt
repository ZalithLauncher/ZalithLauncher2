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

package com.movtery.zalithlauncher.game.addons.modloader.fabriclike.fabric

import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.FabricLikeVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.models.FabricLikeLoader

object FabricVersions : FabricLikeVersions(
    officialUrl = "https://meta.fabricmc.net/v2",
    mirrorUrl = "https://bmclapi2.bangbang93.com/fabric-meta/v2"
) {

    /**
     * 获取 Fabric 列表
     */
    suspend fun fetchFabricLoaderList(mcVersion: String, force: Boolean = false): List<FabricVersion>? {
        val list: List<FabricLikeLoader> = fetchLoaderList(force, "FabricVersions", mcVersion) ?: return null

        return list.map { loader ->
            FabricVersion(
                inherit = mcVersion,
                version = loader.version,
                stable = loader.stable
            )
        }
    }
}