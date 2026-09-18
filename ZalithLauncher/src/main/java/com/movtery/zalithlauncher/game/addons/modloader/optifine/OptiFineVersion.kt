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

package com.movtery.zalithlauncher.game.addons.modloader.optifine

import com.movtery.zalithlauncher.game.addons.modloader.AddonVersion

class OptiFineVersion(
    /** 显示名称 */
    val displayName: String,
    /** 文件名称 */
    val fileName: String,
    /** 版本名称 */
    val version: String,
    /** Minecraft 版本 */
    inherit: String,
    /** 发布时间，格式为“yyyy/mm/dd” */
    val releaseDate: String,
    /** 最低需求 Forge 版本：null 为不兼容，空字符串为无限制 */
    val forgeVersion: String?,
    /** 是否为预览版本 */
    val isPreview: Boolean
) : AddonVersion(
    inherit = inherit
) {
    /**
     * OptiFine_1.10.2_HD_U_C1 -> C1
     * 1.20.6 J1 pre18 -> J1 pre18
     */
    val realVersion: String
        get() = displayName.removePrefix(inherit).trim()

    override fun getAddonVersion(): String = this.version

    override fun isVersion(versionString: String): Boolean {
        return this.version == versionString
    }
}