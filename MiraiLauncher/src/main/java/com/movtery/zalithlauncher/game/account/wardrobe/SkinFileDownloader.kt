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

import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File

private const val TAG = "SkinFileDownloader"

class SkinFileDownloader: WardrobeDownloader() {
    /**
     * 尝试下载yggdrasil皮肤
     */
    @Throws(Exception::class)
    suspend fun download(
        url: String,
        skinFile: File,
        uuid: String,
        changeSkinModel: (SkinModelType) -> Unit
    ) {
        val valueObject = yggdrasil(url, uuid)
        val skinObject = valueObject.get("textures").asJsonObject.get("SKIN").asJsonObject
        val skinUrl = skinObject.get("url").asString

        val skinModelType = runCatching {
            skinObject.takeIf {
                it.has("metadata")
            }?.get("metadata")?.let {
                //仅在玩家模型为细臂时，才会存在metadata字段，否则为粗臂
                //Wiki：https://zh.minecraft.wiki/w/Mojang_API#%E8%8E%B7%E5%8F%96%E7%8E%A9%E5%AE%B6%E7%9A%84%E7%9A%AE%E8%82%A4%E5%92%8C%E6%8A%AB%E9%A3%8E
                SkinModelType.ALEX
            } ?: SkinModelType.STEVE
        }.getOrElse {
            Logger.warning(TAG, "Can not get skin model type.")
            SkinModelType.NONE
        }

        download(skinUrl, skinFile)
        changeSkinModel(skinModelType)
    }
}