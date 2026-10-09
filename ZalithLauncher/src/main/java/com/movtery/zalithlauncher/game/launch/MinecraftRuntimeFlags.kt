/*
 * Zalith Launcher 2
 * Copyright (C) 2026 Aerix Launcher contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.game.launch

import com.movtery.zalithlauncher.game.version.installed.utils.isLowerVer

/** Java module and LWJGL compatibility flags for modern Minecraft launch profiles. */
internal object MinecraftRuntimeFlags {
    private const val MIN_MINECRAFT_VERSION = "1.20"

    private val MODULE_OPEN_FLAGS = listOf(
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens=java.base/java.net=ALL-UNNAMED",
        "--add-opens=java.base/java.nio=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED",
        "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
        "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.font=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.java2d=ALL-UNNAMED",
    )

    /**
     * Java 17+ module access and native LWJGL settings for Minecraft 1.20 through current 26.x.
     * Older Java/game pairs receive no new flags. The exact same cache path is used for native
     * extraction across ABI-specific LWJGL component sets.
     */
    fun forMinecraft(
        javaMajor: Int,
        minecraftVersion: String,
        sharedLibraryExtractPath: String,
    ): List<String> {
        if (javaMajor < 17 || minecraftVersion.isBlank() || minecraftVersion.isLowerVer(MIN_MINECRAFT_VERSION)) {
            return emptyList()
        }

        return buildList {
            add("--enable-native-access=ALL-UNNAMED")
            addAll(MODULE_OPEN_FLAGS)
            add("-Dorg.lwjgl.system.allocator=system")
            add("-Dorg.lwjgl.system.SharedLibraryExtractPath=$sharedLibraryExtractPath")
        }
    }
}
