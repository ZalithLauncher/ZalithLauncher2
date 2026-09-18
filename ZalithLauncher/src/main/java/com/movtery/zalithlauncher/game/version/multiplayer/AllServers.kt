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

package com.movtery.zalithlauncher.game.version.multiplayer

import com.github.steveice10.opennbt.NBTIO
import com.github.steveice10.opennbt.tag.builtin.ByteTag
import com.github.steveice10.opennbt.tag.builtin.CompoundTag
import com.github.steveice10.opennbt.tag.builtin.ListTag
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.nbt.asBoolean
import com.movtery.zalithlauncher.utils.nbt.asBooleanNotNull
import com.movtery.zalithlauncher.utils.nbt.asList
import com.movtery.zalithlauncher.utils.nbt.asString
import com.movtery.zalithlauncher.utils.nbt.asStringNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Files
import java.util.Base64

private const val TAG = "AllServers"

class AllServers {
    private val _serverList = mutableListOf<ServerData>()
    val serverList: List<ServerData>
        get() = _serverList

    private val hiddenServerList = mutableListOf<ServerData>()

    /**
     * 尝试读取 servers.dat 文件中存储的服务器信息
     */
    suspend fun loadServers(dataFile: File) {
        withContext(Dispatchers.IO) {
            runCatching {
                //清除当前所有服务器
                _serverList.clear()
                hiddenServerList.clear()

                if (!dataFile.exists()) return@withContext

                val compound = NBTIO.readFile(dataFile, false, false)
                    ?: error("Failed to read the server.dat file as a CompoundTag.")
                val servers = compound.asList("servers", null)
                    ?: error("servers entry not found in the NBT structure tree.")

                servers.forEach { tag ->
                    val serverTag = tag as? CompoundTag ?: return@forEach
                    val data = serverTag.parseServerData()
                    val isHidden = serverTag.asBooleanNotNull("hidden", false)
                    if (!isHidden) {
                        _serverList.add(data)
                    } else {
                        hiddenServerList.add(data)
                    }
                }
            }.onFailure {
                Logger.warning(TAG, "An exception occurred while reading and parsing the servers.dat file (${dataFile.absolutePath}).", it)
            }
        }
    }

    fun addServer(
        server: ServerData,
        isHidden: Boolean = false
    ) {
        if (isHidden) {
            hiddenServerList.add(server)
            //Minecraft内有隐藏服务器自动清理机制
            //如果隐藏的服务器数量大于16，则会开始清除前面的服务器
            while (this.hiddenServerList.size > 16) {
                this.hiddenServerList.removeAt(this.hiddenServerList.size - 1)
            }
        } else {
            _serverList.add(server)
        }
    }

    fun removeServer(data: ServerData) {
        if (!_serverList.remove(data)) {
            hiddenServerList.remove(data)
        }
    }

    /**
     * 保存服务器列表
     * @param savePath 生成的 servers.dat 文件会保存在什么目录下
     */
    suspend fun save(savePath: File) {
        withContext(Dispatchers.IO) {
            runCatching {
                val tag = CompoundTag("")
                val serverTags = ListTag("servers")

                serverTags.addServers(_serverList, false)
                serverTags.addServers(hiddenServerList, true)

                tag.put(serverTags)

                //开始将数据写入文件，和原版 Minecraft 差不多的逻辑
                val currentPath = savePath.toPath()
                val newFile = Files.createTempFile(currentPath, "servers", ".dat").toFile()
                NBTIO.writeFile(tag, newFile, false, false)

                val currentDataFile = currentPath.resolve("servers.dat").toFile()
                val oldDataFile = currentPath.resolve("servers.dat_old").toFile()

                //先尝试存档当前的服务器数据文件
                FileUtils.deleteQuietly(oldDataFile)
                if (currentDataFile.exists()) {
                    currentDataFile.copyTo(oldDataFile, true)
                    FileUtils.deleteQuietly(currentDataFile)
                }

                //然后复制缓存的新的数据文件
                newFile.copyTo(currentDataFile, true)
                FileUtils.deleteQuietly(newFile)
            }.onFailure {
                Logger.warning(TAG, "Couldn't save server list", it)
            }
        }
    }

    private fun ListTag.addServers(
        list: List<ServerData>,
        hidden: Boolean
    ) {
        list.forEach { data ->
            val serverTag = data.save()
            serverTag.put(ByteTag("hidden", (if (hidden) 1 else 0).toByte()))
            add(serverTag)
        }
    }

    /**
     * [参考 WIKI](https://zh.minecraft.wiki/w/%E6%9C%8D%E5%8A%A1%E5%99%A8%E5%88%97%E8%A1%A8%E5%AD%98%E5%82%A8%E6%A0%BC%E5%BC%8F#%E5%AD%98%E5%82%A8%E6%A0%BC%E5%BC%8F)
     */
    private fun CompoundTag.parseServerData(): ServerData {
        val name = asStringNotNull("name", "")
        val origin = asStringNotNull("ip", "")

        //尝试解析icon作为placeholder
        val icon = asString("icon", null)?.let { base64 ->
            runCatching {
                Base64.getDecoder().decode(base64)
            }.onFailure {
                Logger.warning(TAG, "Unable to recognize server $name's icon as a valid icon array")
            }.getOrNull()
        }

        val texturesStatus = asBoolean("acceptTextures", null).let { code ->
            when (code) {
                true -> ServerData.TexturePackStatus.ENABLED
                false -> ServerData.TexturePackStatus.DISABLED
                null -> ServerData.TexturePackStatus.PROMPT
            }
        }

        val acceptedCodeOfConduct = asBoolean("acceptedCodeOfConduct", null)

        return ServerData(
            name = name,
            originIp = origin,
            texturePackStatus = texturesStatus,
            acceptedCodeOfConduct = acceptedCodeOfConduct,
            icon = icon
        )
    }
}