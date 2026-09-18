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

package com.movtery.zalithlauncher.game.addons.modloader.modlike

import com.movtery.zalithlauncher.game.addons.modloader.AddonVersion
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthFile
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthVersion

/**
 * 模组版本，因 Modrinth 访问优势，暂只支持 Modrinth
 */
open class ModVersion(
    /** Minecraft 版本 */
    inherit: String,
    /** 显示名称 */
    val displayName: String,
    /** 版本详细信息类 */
    val version: ModrinthVersion,
    /** 可下载的主文件 */
    val file: ModrinthFile
) : AddonVersion(
    inherit = inherit
) {

    override fun getAddonVersion(): String = this.version.versionNumber

    override fun isVersion(versionString: String): Boolean {
        return this.version.versionNumber == versionString
    }
}