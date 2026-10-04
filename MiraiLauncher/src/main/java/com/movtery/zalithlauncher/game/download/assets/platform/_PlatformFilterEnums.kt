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

package com.movtery.zalithlauncher.game.download.assets.platform

import android.os.Parcelable
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeClassID
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ProjectTypeFacet
import com.movtery.zalithlauncher.game.version.installed.VersionFolders

interface PlatformFilterCode {
    fun getDisplayName(): Int
    fun index(): Int
}

interface PlatformDisplayLabel {
    fun getDisplayName(): String
    fun index(): Int
}

interface ModLoaderDisplayLabel: Parcelable, PlatformDisplayLabel

enum class PlatformClasses(
    val curseforge: CurseForgeClassID,
    val modrinth: ProjectTypeFacet?,
    val versionFolder: VersionFolders
) {
    MOD(
        curseforge = CurseForgeClassID.MOD,
        modrinth = ProjectTypeFacet.MOD,
        versionFolder = VersionFolders.MOD
    ),
    MOD_PACK(
        curseforge = CurseForgeClassID.MOD_PACK,
        modrinth = ProjectTypeFacet.MODPACK,
        versionFolder = VersionFolders.NONE
    ),
    RESOURCE_PACK(
        curseforge = CurseForgeClassID.RESOURCE_PACK,
        modrinth = ProjectTypeFacet.RESOURCE_PACK,
        versionFolder = VersionFolders.RESOURCE_PACK
    ),
    SAVES(
        curseforge = CurseForgeClassID.SAVES,
        modrinth = null,
        versionFolder = VersionFolders.SAVES
    ),
    SHADERS(
        curseforge = CurseForgeClassID.SHADERS,
        modrinth = ProjectTypeFacet.SHADER,
        versionFolder = VersionFolders.SHADERS
    )
}