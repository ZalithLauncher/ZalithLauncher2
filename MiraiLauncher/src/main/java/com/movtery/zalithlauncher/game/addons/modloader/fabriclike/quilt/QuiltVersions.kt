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

package com.movtery.zalithlauncher.game.addons.modloader.fabriclike.quilt

import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.FabricLikeVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.models.FabricLikeLoader

object QuiltVersions : FabricLikeVersions("https://meta.quiltmc.org/v3") {

    /**
     * 获取 Quilt 列表
     */
    suspend fun fetchQuiltLoaderList(mcVersion: String, force: Boolean = false): List<QuiltVersion>? {
        val list: List<FabricLikeLoader> = fetchLoaderList(force, "QuiltVersions", mcVersion) ?: return null

        return list.map { loader ->
            QuiltVersion(
                inherit = mcVersion,
                version = loader.version
            )
        }
    }
}