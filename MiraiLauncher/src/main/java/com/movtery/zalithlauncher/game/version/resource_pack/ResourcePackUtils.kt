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

package com.movtery.zalithlauncher.game.version.resource_pack

import com.movtery.zalithlauncher.game.version.mod.meta.PackMcMeta
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.zip.ZipFile

private const val TAG = "ResourcePackUtils"

/**
 * 解析资源包文件，游戏内仅支持加载文件夹、文件后缀为zip的资源包
 * @param file 资源包文件
 */
suspend fun parseResourcePack(file: File): ResourcePackInfo? = withContext(Dispatchers.IO) {
    runCatching {
        var isValid = false
        var metaContent: String? = null
        var iconBytes: ByteArray? = null
        var fileSize: Long? = null

        if (file.isDirectory) { //文件夹形式的资源包
            //资源包元数据
            File(file, "pack.mcmeta").takeIf { it.exists() }?.let { metaFile ->
                metaContent = metaFile.readText()
            }
            //尝试读取资源包的图标
            File(file, "pack.png").takeIf { it.exists() }?.let { iconFile ->
                iconBytes = iconFile.readBytes()
            }
        } else if (file.extension == "zip") { //压缩包形式的资源包
            //性能、速度考虑，仅压缩包形式的资源包可以计算文件大小
            fileSize = FileUtils.sizeOf(file)

            ZipFile(file).use { zip ->
                //资源包元数据
                zip.getEntry("pack.mcmeta")?.let { metaEntry ->
                    metaContent = zip.getInputStream(metaEntry).bufferedReader().readText()
                }
                //尝试读取资源包的图标
                zip.getEntry("pack.png")?.let { iconEntry ->
                    iconBytes = zip.getInputStream(iconEntry).readBytes()
                }
            }
        }

        val meta = metaContent?.let { content ->
            runCatching {
                GSON.fromJson(content, PackMcMeta::class.java)
            }.onFailure {
                Logger.warning(TAG, "Failed to parse the resource package metadata: ${file.absolutePath}", it)
            }.getOrNull()
        }?.also {
            //解析成功，则代表其是一个有效的格式
            isValid = true
        }

        ResourcePackInfo(
            file = file,
            fileSize = fileSize,
            isValid = isValid,
            description = meta?.pack?.description?.toPlainText(),
            packFormat = meta?.pack?.packFormat,
            icon = iconBytes
        )
    }.onFailure {
        Logger.warning(TAG, "Failed to parse the resource package: ${file.absolutePath}", it)
    }.getOrNull()
}