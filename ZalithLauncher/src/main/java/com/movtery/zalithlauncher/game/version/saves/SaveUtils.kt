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

import com.github.steveice10.opennbt.NBTIO
import com.github.steveice10.opennbt.tag.builtin.CompoundTag
import com.movtery.zalithlauncher.game.version.installed.utils.isBiggerOrEqualVer
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.nbt.asBooleanNotNull
import com.movtery.zalithlauncher.utils.nbt.asCompoundTag
import com.movtery.zalithlauncher.utils.nbt.asInt
import com.movtery.zalithlauncher.utils.nbt.asLong
import com.movtery.zalithlauncher.utils.nbt.asString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "SaveUtils"

/**
 * 判断这个存档是否与指定的版本兼容
 * @param minecraftVersion 当前 MC 的版本，用于比较版本兼容性
 */
fun SaveData.isCompatible(minecraftVersion: String) =
    isValid && levelMCVersion != null && minecraftVersion.isBiggerOrEqualVer(levelMCVersion)

/**
 * 从 level.dat 文件中解析出必要的信息，构建 SaveData
 * [参考 Minecraft Wiki](https://zh.minecraft.wiki/w/%E5%AD%98%E6%A1%A3%E5%9F%BA%E7%A1%80%E6%95%B0%E6%8D%AE%E5%AD%98%E5%82%A8%E6%A0%BC%E5%BC%8F#%E5%AD%98%E5%82%A8%E6%A0%BC%E5%BC%8F)
 * @param saveFile 存档的文件夹
 * @param levelDatFile level.dat 文件
 * @param worldGenDatFile 26.1+ 新存档格式，将世界生成设置迁移到了 /data/minecraft/world_gen_settings.dat
 */
suspend fun parseLevelDatFile(
    saveFile: File,
    levelDatFile: File,
    worldGenDatFile: File? = null
): SaveData = withContext(Dispatchers.IO) {
//    val fileSize = FileUtils.sizeOf(saveFile)
    runCatching {
        if (!levelDatFile.exists()) error("The ${levelDatFile.absolutePath} file does not exist!")

        val compound = NBTIO.readFile(levelDatFile)
            ?: error("Failed to read the level.dat file as a CompoundTag.")
        val data: CompoundTag = compound.asCompoundTag("Data")
            ?: error("{level.dat} Data entry not found in the NBT structure tree.")

        //存档名称，不存在则为空
        val levelName = data.asString("LevelName", "")
        //存档的游戏版本
        val levelMCVersion = data.asCompoundTag("Version")?.asString("Name", null)
        //上次保存此存档的时间戳
        val lastPlayed = data.asLong("LastPlayed", 0) ?: 0 //0则代表不存在
        //游戏时长
        val playTime = data.asLong("Time", 0) ?: 0 //0则代表不存在
        //存档的游戏模式
        val gameMode = data.asInt("GameType", 0) //默认为生存模式
            ?.let { levelCode -> GameMode.entries.find { it.levelCode == levelCode } }
        //游戏难度
        val difficulty = data.asInt("Difficulty", 2) //默认为普通
            ?.let { levelCode -> Difficulty.entries.find { it.levelCode == levelCode } }
        //是否锁定了游戏难度
        val difficultyLocked = data.asBooleanNotNull("DifficultyLocked", false)
        //是否为极限模式
        val hardcoreMode = data.asBooleanNotNull("hardcore", false)
        //是否开启了命令（作弊）
        val allowCommands = if (data.contains("allowCommands")) {
            data.asBooleanNotNull("allowCommands", false)
        } else {
            //如果不存在 allowCommands，则通过游戏模式判断
            gameMode == GameMode.CREATIVE
        }
        //世界种子
        val worldSeed = if (worldGenDatFile != null && worldGenDatFile.isFile && worldGenDatFile.exists()) {
            //26.1+
            runCatching {
                val worldGenCompound = NBTIO.readFile(worldGenDatFile)
                    ?: error("Failed to read the world_gen_settings.dat file as a CompoundTag.")
                val worldData = worldGenCompound.asCompoundTag("data")
                    ?: error("{world_gen_settings.dat} data entry not found in the NBT structure tree.")

                worldData.asLong("seed", null)
            }.onFailure {
                Logger.warning(TAG, "An exception occurred while reading and parsing the world_gen_settings.dat file (${worldGenDatFile.absolutePath}).", it)
            }.getOrNull()
        } else {
            data.asCompoundTag("WorldGenSettings")
                ?.asLong("seed", null)
            //如果不存在，则尝试获取 RandomSeed
                ?: data.asLong("RandomSeed", null)
        }

        SaveData(
            saveFile = saveFile,
//            saveSize = fileSize,
            isValid = true,
            levelName = levelName,
            levelMCVersion = levelMCVersion,
            lastPlayed = lastPlayed.takeIf { it != 0L },
            playTime = playTime.takeIf { it != 0L },
            gameMode = gameMode,
            //关于极限模式：极限模式开启后，难度会被锁定为困难（尽管 level.dat 文件内并不会这样存储）
            //https://zh.minecraft.wiki/w/%E6%9E%81%E9%99%90%E6%A8%A1%E5%BC%8F#%E5%88%9B%E5%BB%BA%E6%96%B0%E7%9A%84%E4%B8%96%E7%95%8C
            difficulty = if (hardcoreMode) Difficulty.HARD else difficulty,
            difficultyLocked = difficultyLocked,
            hardcoreMode = hardcoreMode,
            allowCommands = allowCommands,
            worldSeed = worldSeed
        )
    }.onFailure {
        Logger.warning(TAG, "An exception occurred while reading and parsing the level.dat file (${levelDatFile.absolutePath}).", it)
    }.getOrElse {
        //读取出现异常，返回一个无效数据
        SaveData(
            saveFile = saveFile,
//            saveSize = fileSize,
            isValid = false
        )
    }
}
