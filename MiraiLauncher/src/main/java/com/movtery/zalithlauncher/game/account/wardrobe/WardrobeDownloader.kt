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

package com.movtery.zalithlauncher.game.account.wardrobe

import com.google.gson.JsonObject
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.downloadFile
import com.movtery.zalithlauncher.utils.network.fetchStringFromUrl
import com.movtery.zalithlauncher.utils.string.decodeBase64
import kotlinx.coroutines.CancellationException
import java.io.File

private const val TAG = "WardrobeDownloader"

abstract class WardrobeDownloader {
    protected suspend fun yggdrasil(
        url: String,
        uuid: String
    ): JsonObject {
        val profileJson = fetchStringFromUrl("${url.removeSuffix("/")}/session/minecraft/profile/$uuid")
        val profileObject = GSON.fromJson(profileJson, JsonObject::class.java)
        val properties = profileObject.get("properties").asJsonArray
        val rawValue = properties.get(0).asJsonObject.get("value").asString

        val value = decodeBase64(rawValue)

        return GSON.fromJson(value, JsonObject::class.java)
    }

    protected suspend fun download(url: String, file: File) {
        file.parentFile?.apply {
            if (!exists()) mkdirs()
        }

        try {
            downloadFile(url = url, outputFile = file)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            //皮肤获取失败并不致命，保持既有语义：记录日志而不上抛
            Logger.error(TAG, "Failed to download skin file", e)
        }
    }
}