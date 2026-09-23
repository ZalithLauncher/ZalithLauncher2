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

package com.movtery.zalithlauncher.game.version.installed

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.logging.Logger
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "CurrentGameInfo"

/**
 * 当前游戏状态信息（支持旧配置迁移）
 * @property version 当前选择的版本名称
 * @property favoritesMap 收藏夹映射表 <收藏夹名称, 包含的版本集合>
 */
@Keep
data class CurrentGameInfo(
    @SerializedName("version")
    var version: String = "",
    @SerializedName("favoritesInfo")
    val favoritesMap: MutableMap<String, MutableSet<String>> = ConcurrentHashMap()
) {
    /**
     * 原子化保存当前状态到文件
     * @param gameHome 信息所属的游戏目录
     */
    fun saveCurrentInfo(gameHome: String) {
        val infoFile = getInfoFile(gameHome)
        runCatching {
            FileUtils.writeByteArrayToFile(infoFile, GSON.toJson(this).toByteArray(Charsets.UTF_8))
            Logger.debug(TAG, "Current version $version has been saved to the config file.")
        }.onFailure { e ->
            Logger.error(TAG, "Save failed: ${infoFile.absolutePath}", e)
        }
    }
}

private fun getInfoFile(gameHome: String) = File(gameHome, "zalith-game.cfg")

/**
 * 刷新并返回最新的游戏信息（自动处理旧配置迁移）
 * @param gameHome 信息所属的游戏目录
 */
fun refreshCurrentInfo(gameHome: String): CurrentGameInfo {
    val infoFile = getInfoFile(gameHome)

    return runCatching {
        when {
            infoFile.exists() -> loadFromJsonFile(infoFile)
            else -> createNewConfig(gameHome)
        }
    }.getOrElse { e ->
        Logger.error(TAG, "Refresh failed", e)
        createNewConfig(gameHome)
    }
}

private fun loadFromJsonFile(infoFile: File): CurrentGameInfo {
    return GSON.fromJson(infoFile.readText(), CurrentGameInfo::class.java).also { info ->
        checkNotNull(info) { "Deserialization returned null" }
    }
}

private fun createNewConfig(gameHome: String) = CurrentGameInfo().applyPostActions(gameHome)

private fun CurrentGameInfo.applyPostActions(gameHome: String): CurrentGameInfo {
    saveCurrentInfo(gameHome)
    return this
}