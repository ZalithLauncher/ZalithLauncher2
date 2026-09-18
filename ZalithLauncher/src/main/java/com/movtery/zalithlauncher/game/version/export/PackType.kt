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

package com.movtery.zalithlauncher.game.version.export

/**
 * 打包的类型
 */
enum class PackType(val options: PackEditOptions) {
    /**
     * MCBBS 导出格式
     */
    MCBBS(
        PackEditOptions.Builder()
            .requireAuthor()
            .requireSummary()
            .requireJvmArgs()
            .requireJavaArgs()
            .requireWebsiteUrl()
            .requireMinMemory()
            .build()
    ),

    /**
     * Modrinth 标准导出格式
     */
    Modrinth(
        PackEditOptions.Builder()
            .requireSummary()
            .requirePackModrinth()
            .requirePackCurseForge()
            .build()
    ),

    CurseForge(
        PackEditOptions.Builder()
            .requireAuthor()
            .requirePackCurseForge()
            .requireMinMemory()
            .build()
    ),

    /**
     * MultiMC 导出格式
     */
    MultiMC(
        PackEditOptions.Builder()
            .requireAuthor()
            .requireSummary()
            .requireMinMemory()
            .requireMaxMemory()
            .build()
    ),
}

class PackEditOptions private constructor(
    val requireAuthor: Boolean = false,
    val requireSummary: Boolean = false,
    val requireJvmArgs: Boolean = false,
    val requireJavaArgs: Boolean = false,
    val requireWebsiteUrl: Boolean = false,
    val requireMinMemory: Boolean = false,
    val requireMaxMemory: Boolean = false,
    val requirePackModrinth: Boolean = false,
    val requirePackCurseForge: Boolean = false
) {
    class Builder {
        private var requireAuthor: Boolean = false
        private var requireSummary: Boolean = false
        private var requireJvmArgs: Boolean = false
        private var requireJavaArgs: Boolean = false
        private var requireWebsiteUrl: Boolean = false
        private var requireMinMemory: Boolean = false
        private var requireMaxMemory: Boolean = false
        private var requirePackModrinth: Boolean = false
        private var requirePackCurseForge: Boolean = false

        fun requireAuthor() = this.also { requireAuthor = true }
        fun requireSummary() = this.also { requireSummary = true }
        fun requireJvmArgs() = this.also { requireJvmArgs = true }
        fun requireJavaArgs() = this.also { requireJavaArgs = true }
        fun requireWebsiteUrl() = this.also { requireWebsiteUrl = true }
        fun requireMinMemory() = this.also { requireMinMemory = true }
        fun requireMaxMemory() = this.also { requireMaxMemory = true }
        fun requirePackModrinth() = this.also { requirePackModrinth = true }
        fun requirePackCurseForge() = this.also { requirePackCurseForge = true }

        fun build() = PackEditOptions(
            requireAuthor = requireAuthor,
            requireSummary = requireSummary,
            requireJvmArgs = requireJvmArgs,
            requireJavaArgs = requireJavaArgs,
            requireWebsiteUrl = requireWebsiteUrl,
            requireMinMemory = requireMinMemory,
            requireMaxMemory = requireMaxMemory,
            requirePackModrinth = requirePackModrinth,
            requirePackCurseForge = requirePackCurseForge
        )
    }
}