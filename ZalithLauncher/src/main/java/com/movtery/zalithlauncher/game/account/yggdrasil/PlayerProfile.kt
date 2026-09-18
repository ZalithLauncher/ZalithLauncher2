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

package com.movtery.zalithlauncher.game.account.yggdrasil

import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.io.File

@Serializable
data class PlayerProfile(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("skins")
    val skins: List<Skin>,
    @SerialName("capes")
    val capes: List<Cape>,
    @SerialName("profileActions")
    val profileActions: JsonElement? = null
) {
    @Serializable
    data class Skin(
        @SerialName("id")
        val id: String,
        @SerialName("state")
        val state: String,
        @SerialName("url")
        val url: String,
        @SerialName("textureKey")
        val textureKey: String,
        @SerialName("variant")
        val variant: String
    )

    @Serializable
    data class Cape(
        @SerialName("id")
        val id: String,
        @SerialName("state")
        val state: String,
        @SerialName("url")
        val url: String,
        @SerialName("alias")
        val alias: String
    )
}

/**
 * 该皮肤是否正在使用中
 */
fun PlayerProfile.Skin.isUsing(): Boolean = this.state == "ACTIVE"

/**
 * 该披风是否正在使用中
 */
fun PlayerProfile.Cape.isUsing(): Boolean = this.state == "ACTIVE"

/**
 * 查找玩家当前正在使用的皮肤
 */
fun List<PlayerProfile.Skin>.findUsing(): PlayerProfile.Skin? = this.find { it.isUsing() }

/**
 * 查找玩家当前正在使用的披风
 */
fun List<PlayerProfile.Cape>.findUsing(): PlayerProfile.Cape? = this.find { it.isUsing() }

/**
 * 获取玩家皮肤模型类型
 */
fun PlayerProfile.Skin.getSkinModel(): SkinModelType {
    return when (variant) {
        "CLASSIC" -> SkinModelType.STEVE
        "SLIM" -> SkinModelType.ALEX
        else -> SkinModelType.NONE
    }
}

/**
 * 获取披风在本地的目标文件
 */
fun PlayerProfile.Cape.getFile(path: File): File {
    return File(path, "$id.png")
}