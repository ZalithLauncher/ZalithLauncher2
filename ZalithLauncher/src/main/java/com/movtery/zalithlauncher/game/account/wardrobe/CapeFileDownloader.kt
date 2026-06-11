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

import java.io.File

class CapeFileDownloader: WardrobeDownloader() {
    /**
     * 尝试下载yggdrasil皮肤
     */
    @Throws(Exception::class)
    suspend fun download(
        url: String,
        capeFile: File,
        uuid: String,
    ) {
        try {
            val valueObject = yggdrasil(url, uuid)
            
            // Null check cho textures và cape object
            val textures = valueObject.get("textures")?.asJsonObject
            if (textures == null) {
                // Không có textures, xóa file cũ nếu tồn tại
                if (capeFile.exists()) {
                    capeFile.delete()
                }
                return
            }
            
            val capeObject = textures.get("CAPE")?.asJsonObject
            if (capeObject == null) {
                // Không có cape, xóa file cũ nếu tồn tại
                if (capeFile.exists()) {
                    capeFile.delete()
                }
                return
            }
            
            val capeUrl = capeObject.get("url")?.asString
            if (capeUrl.isNullOrEmpty()) {
                // URL cape trống hoặc null, xóa file cũ
                if (capeFile.exists()) {
                    capeFile.delete()
                }
                return
            }
            
            // Tải cape xuống
            download(capeUrl, capeFile)
            
        } catch (e: Exception) {
            // Log lỗi và throw ra ngoài để caller xử lý
            android.util.Log.e("CapeFileDownloader", "Failed to download cape for uuid: $uuid", e)
            throw e
        }
    }
}
