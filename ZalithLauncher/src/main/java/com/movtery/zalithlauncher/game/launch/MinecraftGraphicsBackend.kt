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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.launch

import com.movtery.zalithlauncher.game.version.installed.GraphicsApi

/**
 * Resolve the option written to Minecraft's `preferredGraphicsBackend` setting.
 *
 * `DEFAULT_OPENGL` normally respects a backend already saved by the game. Once the client
 * actually ships its Vulkan backend, however, that saved value can be a stale automatic Vulkan
 * choice from a previous failed launch. In that case the launcher's OpenGL default must win;
 * users who want to force Vulkan can still choose [GraphicsApi.VULKAN] explicitly.
 *
 * A null result means to preserve the existing option unchanged.
 */
internal fun resolvePreferredGraphicsBackendOption(
    graphicsApi: GraphicsApi,
    hasVulkanBackend: Boolean,
    existingOption: String?,
): String? = when (graphicsApi) {
    GraphicsApi.DEFAULT -> existingOption ?: graphicsApi.option
    GraphicsApi.DEFAULT_OPENGL -> {
        if (hasVulkanBackend) GraphicsApi.OPENGL.option
        else existingOption ?: graphicsApi.option
    }
    else -> graphicsApi.option
}
