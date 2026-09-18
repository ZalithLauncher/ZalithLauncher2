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

package com.movtery.zalithlauncher.game.download.assets.platform.curseforge

import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchFilter
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeCategory
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeModLoader
import com.movtery.zalithlauncher.utils.string.isNotEmptyOrBlank

/**
 * CurseForge 平台的 API 链接
 * [CurseForge REST API](https://docs.curseforge.com/rest-api/?shell#base-url)
 */
const val CURSEFORGE_API = "https://api.curseforge.com/v1"

/**
 * MCIM 镜像：CurseForge 平台的 API 链接
 * [MCIM CurseForge API](https://github.com/mcmod-info-mirror/mcim-rust-api?tab=readme-ov-file#curseforge)
 */
const val MCIM_CURSEFORGE_API = "https://mod.mcimirror.top/curseforge/v1"

fun PlatformSearchFilter.toCurseForgeRequest(
    query: String,
    platformClasses: PlatformClasses
): CurseForgeSearchRequest {
    val curseforgeCategories = categories.map { category ->
        category as? CurseForgeCategory
    }.toTypedArray()

    return CurseForgeSearchRequest(
        classId = platformClasses.curseforge.classID,
        categories = setOfNotNull(
            *curseforgeCategories
        ),
        searchFilter = query,
        gameVersion = gameVersion.takeIf { it.isNotEmptyOrBlank() }?.trim(),
        sortField = sortField,
        modLoader = modloader as? CurseForgeModLoader,
        index = index,
        pageSize = limit
    )
}