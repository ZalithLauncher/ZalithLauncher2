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

package com.movtery.zalithlauncher.game.addons.modloader.cleanroom

import com.movtery.zalithlauncher.game.addons.modloader.AddonVersion
import java.time.Instant

/**
 * Cleanroom 版本
 * @param version 版本名称（可直接向用户展示）
 * @param createdAt 创建时间
 */
class CleanroomVersion(
    val version: String,
    val createdAt: Instant
): AddonVersion(
    //固定写死 1.12.2
    inherit = "1.12.2"
) {
    /**
     * Cleanroom 安装器下载链接
     */
    val installerUrl: String
        get() = "https://hmcl-dev.github.io/metadata/cleanroom/files/cleanroom-$version-installer.jar"

    override fun getAddonVersion(): String = this.version

    override fun isVersion(versionString: String): Boolean {
        return this.version == versionString
    }
}