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

package com.movtery.zalithlauncher.ui.screens.content.versions.elements

import com.movtery.zalithlauncher.game.version.saves.SaveData
import com.movtery.zalithlauncher.game.version.saves.isCompatible
import com.movtery.zalithlauncher.utils.string.stripColorCodes

sealed interface SavesOperation {
    data object None : SavesOperation
    /** 执行任务中 */
    data object Progress : SavesOperation
    /** 快速启动 */
    data class QuickPlay(val saveData: SaveData) : SavesOperation
    /** 重命名存档输入对话框 */
    data class RenameSave(val saveData: SaveData) : SavesOperation
    /** 备份存档输入对话框 */
    data class BackupSave(val saveData: SaveData) : SavesOperation
    /** 删除存档对话框 */
    data class DeleteSave(val saveData: SaveData) : SavesOperation
}

/**
 * 存档过滤器
 */
data class SavesFilter(val onlyShowCompatible: Boolean, val saveName: String = "")

/**
 * 简易过滤器，过滤特定的存档
 * @param minecraftVersion 当前 MC 的版本，用于比较版本兼容性
 * @param savesFilter 存档过滤器
 */
fun List<SaveData>.filterSaves(
    minecraftVersion: String,
    savesFilter: SavesFilter
) = this.filter {
    val isCompatible = !savesFilter.onlyShowCompatible || it.isCompatible(minecraftVersion)

    val nameMatches = savesFilter.saveName.isEmpty() ||
            //存档名、存档文件夹名均可参与搜索
            //自动过滤掉颜色占位符
            it.levelName?.stripColorCodes()?.contains(savesFilter.saveName, true) == true ||
            it.saveFile.name.stripColorCodes().contains(savesFilter.saveName, true)

    isCompatible && nameMatches
}