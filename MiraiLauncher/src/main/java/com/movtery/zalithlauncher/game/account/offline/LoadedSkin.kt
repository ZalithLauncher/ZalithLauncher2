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

package com.movtery.zalithlauncher.game.account.offline

import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 玩家皮肤和披风的完整信息
 * @param skinHash 皮肤文件的哈希值（SHA-256）
 * @param skinBytes 皮肤文件内容
 * @param capeHash 披风文件的哈希值（SHA-256）
 * @param capeBytes 披风文件内容
 * @param model 皮肤模型类型
 */
@Serializable
data class LoadedSkin(
    @SerialName("skinHash")
    val skinHash: String? = null,
    @SerialName("skinBytes")
    val skinBytes: ByteArray? = null,
    @SerialName("capeHash")
    val capeHash: String? = null,
    @SerialName("capeBytes")
    val capeBytes: ByteArray? = null,
    @SerialName("model")
    val model: SkinModelType = SkinModelType.NONE
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoadedSkin

        if (skinHash != other.skinHash) return false
        if (!skinBytes.contentEquals(other.skinBytes)) return false
        if (capeHash != other.capeHash) return false
        if (!capeBytes.contentEquals(other.capeBytes)) return false
        if (model != other.model) return false

        return true
    }

    override fun hashCode(): Int {
        var result = skinHash?.hashCode() ?: 0
        result = 31 * result + (skinBytes?.contentHashCode() ?: 0)
        result = 31 * result + (capeHash?.hashCode() ?: 0)
        result = 31 * result + (capeBytes?.contentHashCode() ?: 0)
        result = 31 * result + model.hashCode()
        return result
    }
}