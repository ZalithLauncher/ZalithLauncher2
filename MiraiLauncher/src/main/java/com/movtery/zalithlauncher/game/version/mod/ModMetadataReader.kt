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

import com.movtery.zalithlauncher.game.version.mod.reader.FabricModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.reader.ForgeNewModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.reader.ForgeOldModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.reader.LiteModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.reader.OptiFineModReader
import com.movtery.zalithlauncher.game.version.mod.reader.PackMcMetadataReader
import com.movtery.zalithlauncher.game.version.mod.reader.QuiltModMetadataReader
import java.io.File

interface ModMetadataReader {
    suspend fun fromLocal(modFile: File): LocalMod
}

private val NORMAL_READERS = arrayOf(
    OptiFineModReader,
    ForgeOldModMetadataReader,
    ForgeNewModMetadataReader,
    FabricModMetadataReader,
    QuiltModMetadataReader,
    PackMcMetadataReader
)

val MOD_READERS = mapOf<String, Array<ModMetadataReader>>(
    "zip" to NORMAL_READERS,
    "jar" to NORMAL_READERS,
    "litemod" to arrayOf(
        LiteModMetadataReader
    )
)