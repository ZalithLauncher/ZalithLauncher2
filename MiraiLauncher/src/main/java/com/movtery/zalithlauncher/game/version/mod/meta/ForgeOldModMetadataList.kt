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

package com.movtery.zalithlauncher.game.version.mod.meta

import com.google.gson.annotations.SerializedName

// from https://github.com/HMCL-dev/HMCL/commit/2c428faa9540e8666ce3cec07ff284cb53eab34a
class ForgeOldModMetadataList(
    @SerializedName("modListVersion")
    val modListVersion: Int?,
    @SerializedName("modList")
    val modList: List<ForgeOldModMetadata>?
)