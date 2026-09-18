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

package com.movtery.zalithlauncher.game.version.saves

import java.io.File

/**
 * 存档解析后的信息类
 */
data class SaveData(
    /** 存档文件夹 */
    val saveFile: File,
// 性能、速度考虑，不再计算存档的大小
//    /** 提前计算好的存档大小 */
//    val saveSize: Long,
    /** 该存档是否有效 */
    val isValid: Boolean,
    /** 存档真正的名字 */
    val levelName: String? = null,
    /** 游戏的版本名称 */
    val levelMCVersion: String? = null,
    /** 上次保存此存档的时间戳 */
    val lastPlayed: Long? = null,
    /** 游戏时长 */
    val playTime: Long? = null,
    /** 存档游戏模式 */
    val gameMode: GameMode? = null,
    /** 存档难度等级 */
    val difficulty: Difficulty? = null,
    /** 难度是否被锁定 */
    val difficultyLocked: Boolean? = null,
    /** 是否为极限模式 */
    val hardcoreMode: Boolean? = null,
    /** 存档是否启用命令(作弊) */
    val allowCommands: Boolean? = null,
    /** 世界种子 */
    val worldSeed: Long? = null
)
