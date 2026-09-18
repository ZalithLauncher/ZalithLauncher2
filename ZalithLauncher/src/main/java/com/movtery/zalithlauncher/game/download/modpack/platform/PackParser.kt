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

import com.movtery.zalithlauncher.game.download.modpack.platform.curseforge.CurseForgePackParser
import com.movtery.zalithlauncher.game.download.modpack.platform.mcbbs.MCBBSPackMetaParser
import com.movtery.zalithlauncher.game.download.modpack.platform.modrinth.ModrinthPackParser
import com.movtery.zalithlauncher.game.download.modpack.platform.multimc.MultiMCPackParser
import java.io.File

/**
 * 整合包解析通用接口，用于尝试解析已解压的整合包的格式
 */
interface PackParser {
    /**
     * 尝试解析已经解压的整合包的格式
     * @param packFolder 解压后的整合包文件夹
     */
    suspend fun parse(packFolder: File): AbstractPack?

    /**
     * 获取该解析器的标识
     */
    fun getIdentifier(): String
}

/**
 * 启动器所有支持的整合包格式解析器
 */
val ALL_PACK_PARSER = listOf(
    CurseForgePackParser,
    ModrinthPackParser,
    MultiMCPackParser,
    MCBBSPackMetaParser
)