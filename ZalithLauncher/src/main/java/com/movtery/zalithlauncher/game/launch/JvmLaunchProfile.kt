/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.game.launch

import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.unit.getOrMin
import com.movtery.zalithlauncher.utils.string.splitPreservingQuotes

class JvmLaunchProfile(
    private val jvmLaunchInfo: JvmLaunchInfo
) : LaunchProfile {
    override fun preJvmArgs(): List<String> = emptyList()

    override fun jvmArgs(): List<String> = jvmLaunchInfo.jvmArgs.splitPreservingQuotes()

    override fun programArgs(): List<String> = emptyList()

    override fun ramAllocation(): Int {
        return if (jvmLaunchInfo.useUserJvm) {
            AllSettings.ramAllocation.getOrMin()
        } else {
            512 // MB
        }
    }

    override fun userJvmArgs(): String {
        return if (jvmLaunchInfo.useUserJvm) {
            AllSettings.jvmArgs.getValue()
        } else {
            ""
        }
    }
}