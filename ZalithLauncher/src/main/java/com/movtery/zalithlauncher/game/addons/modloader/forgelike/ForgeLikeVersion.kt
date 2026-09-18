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

package com.movtery.zalithlauncher.game.addons.modloader.forgelike

import com.movtery.zalithlauncher.game.addons.modloader.AddonVersion

/**
 * [Reference PCL2](https://github.com/Hex-Dragon/PCL2/blob/44aea3e/Plain%20Craft%20Launcher%202/Modules/Minecraft/ModDownload.vb#L512-L563)
 */
abstract class ForgeLikeVersion(
    /** 加载器展示名称 */
    val loaderName: String,
    /** 标准化后的版本号，仅可用于比较与排序 */
    val forgeBuildVersion: ForgeBuildVersion,
    /** 可对玩家显示的非格式化版本名 */
    val versionName: String,
    /** 对应的 Minecraft 版本 */
    inherit: String,
    /** 文件扩展名 */
    val fileExtension: String
) : AddonVersion(
    inherit = inherit
) {
    /**
     * Forge：MC 版本是否小于 1.13。（1.13+ 的版本号首位都大于 20）
     * NeoForge：MC 版本是否为 1.20.1。（1.20.1 的版本号首位人为规定为 19 开头）
     */
    val isLegacy: Boolean get() = forgeBuildVersion.major < 20

    override fun getAddonVersion(): String = forgeBuildVersion.toString()

    override fun isVersion(versionString: String): Boolean {
        val target = ForgeBuildVersion.parse(versionString).toString()
        return target == forgeBuildVersion.toString()
    }
}