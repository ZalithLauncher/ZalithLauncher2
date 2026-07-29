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

package com.movtery.zalithlauncher.game.plugin.vpl

import com.movtery.zalithlauncher.game.plugin.driver.DriverPluginManager
import com.movtery.zalithlauncher.game.plugin.ffmpeg.FFmpegPluginManager
import com.movtery.zalithlauncher.game.plugin.natives.NativePluginManager
import com.movtery.zalithlauncher.game.plugin.renderer.RendererPluginManager

/**
 * Single source of truth for APK plugins inspected by VerifiedPluginLoad.
 *
 * Mirrors FCL's PluginCandidateRepository; separates launch-time candidates
 * (selected plugins only) from all-installed candidates (for the trust management page).
 */
object PluginCandidateRepository {

    data class PluginCandidate(
        val packageName: String,
        val type: PluginType
    )

    /** The plugins that would be verified for the current game launch configuration. */
    fun forLaunch(): List<PluginCandidate> {
        val candidates = LinkedHashMap<String, PluginCandidate>()

        RendererPluginManager.selectedRendererPlugin?.let { plugin ->
            add(candidates, plugin.packageName, PluginType.Renderer)
        }

        val driver = DriverPluginManager.getDriver()
        if (driver.packageName.isNotEmpty() && !driver.isLauncher) {
            add(candidates, driver.packageName, PluginType.VulkanDriver)
        }

        for (plugin in NativePluginManager.getCheckedPlugins()) {
            add(candidates, plugin.packageName, PluginType.NativeLib)
        }

        if (FFmpegPluginManager.isAvailable) {
            add(candidates, "net.kdt.pojavlaunch.ffmpeg", PluginType.FFmpeg)
        }

        return candidates.values.toList()
    }

    /** All installed plugin APKs, for the trust management page. */
    fun allInstalled(): List<PluginCandidate> {
        val candidates = LinkedHashMap<String, PluginCandidate>()

        for (plugin in RendererPluginManager.getRendererList()) {
            add(candidates, plugin.packageName, PluginType.Renderer)
        }

        for (driver in DriverPluginManager.getDriverList()) {
            if (driver.packageName.isNotEmpty() && !driver.isLauncher) {
                add(candidates, driver.packageName, PluginType.VulkanDriver)
            }
        }

        for (plugin in NativePluginManager.getPlugins()) {
            add(candidates, plugin.packageName, PluginType.NativeLib)
        }

        if (FFmpegPluginManager.isAvailable) {
            add(candidates, "net.kdt.pojavlaunch.ffmpeg", PluginType.FFmpeg)
        }

        return candidates.values.toList()
    }

    private fun add(
        candidates: LinkedHashMap<String, PluginCandidate>,
        packageName: String,
        type: PluginType
    ) {
        if (packageName.isBlank()) return
        candidates.putIfAbsent(packageName, PluginCandidate(packageName, type))
    }
}
