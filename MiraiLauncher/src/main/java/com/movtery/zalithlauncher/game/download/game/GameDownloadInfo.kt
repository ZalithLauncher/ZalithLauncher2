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

package com.movtery.zalithlauncher.game.download.game

import com.movtery.zalithlauncher.game.addons.modloader.cleanroom.CleanroomVersion
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.fabric.FabricVersion
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.legacyfabric.LegacyFabricVersion
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.quilt.QuiltVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.forge.ForgeVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.NeoForgeVersion
import com.movtery.zalithlauncher.game.addons.modloader.modlike.ModVersion
import com.movtery.zalithlauncher.game.addons.modloader.optifine.OptiFineVersion

data class GameDownloadInfo(
    /** Minecraft 版本 */
    val gameVersion: String,
    /** 自定义版本名称 */
    val customVersionName: String,
    /** 是否进行覆盖安装 */
    val overwrite: Boolean = false,
    val optifine: OptiFineVersion? = null,
    val forge: ForgeVersion? = null,
    val neoforge: NeoForgeVersion? = null,
    val fabric: FabricVersion? = null,
    val fabricAPI: ModVersion? = null,
    val legacyFabric: LegacyFabricVersion? = null,
    val legacyFabricAPI: ModVersion? = null,
    val quilt: QuiltVersion? = null,
    val quiltAPI: ModVersion? = null,
    val cleanroom: CleanroomVersion? = null,
)
