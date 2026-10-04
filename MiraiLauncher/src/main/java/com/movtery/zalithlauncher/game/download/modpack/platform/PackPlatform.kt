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

package com.movtery.zalithlauncher.game.download.modpack.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.Platform

/**
 * 启动器支持的整合包格式
 * @param identifier 内部使用的标识名称
 */
enum class PackPlatform(val identifier: String) {
    CurseForge(Platform.CURSEFORGE.displayName) {
        @Composable
        override fun getIcon(): Painter {
            return painterResource(R.drawable.img_platform_curseforge)
        }
    },
    Modrinth(Platform.MODRINTH.displayName) {
        @Composable
        override fun getIcon(): Painter {
            return painterResource(R.drawable.img_platform_modrinth)
        }
    },
    MultiMC("MultiMC") {
        @Composable
        override fun getIcon(): Painter {
            return painterResource(R.drawable.img_platform_multimc)
        }
    },
    MCBBS("MCBBS") {
        @Composable
        override fun getIcon(): Painter {
            return painterResource(R.drawable.img_chest)
        }
    };

    /**
     * 获取 UI 层使用的格式名称
     */
    @Composable
    open fun getText(): String = this.identifier

    /**
     * 获取 UI 层使用的格式图标
     */
    @Composable
    abstract fun getIcon(): Painter
}