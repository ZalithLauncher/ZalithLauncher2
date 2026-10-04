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

package com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.ModLoaderDisplayLabel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = CurseForgeModLoader.Serializer::class)
@Parcelize
enum class CurseForgeModLoader(val code: Int) : ModLoaderDisplayLabel {
    ANY(0) {
        override fun getDisplayName(): String = ""
    },
    FORGE(1) {
        override fun getDisplayName(): String = ModLoader.FORGE.displayName
    },
    CAULDRON(2) {
        override fun getDisplayName(): String = "Cauldron"
    },
    LITE_LOADER(3) {
        override fun getDisplayName(): String = ModLoader.LITE_LOADER.displayName
    },
    FABRIC(4) {
        override fun getDisplayName(): String = ModLoader.FABRIC.displayName
    },
    QUILT(5) {
        override fun getDisplayName(): String = ModLoader.QUILT.displayName
    },
    NEOFORGE(6) {
        override fun getDisplayName(): String = ModLoader.NEOFORGE.displayName
    };

    override fun index(): Int = this.ordinal

    companion object {
        private val map = entries.associateBy { it.code }
        fun fromCode(code: Int): CurseForgeModLoader = map[code] ?: error("Unknown mod loader code: $code")
    }

    object Serializer : KSerializer<CurseForgeModLoader> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("CurseForgeModLoader", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): CurseForgeModLoader {
            val code = decoder.decodeInt()
            return CurseForgeModLoader.fromCode(code)
        }

        override fun serialize(encoder: Encoder, value: CurseForgeModLoader) {
            encoder.encodeInt(value.code)
        }
    }
}

/**
 * 可视化筛选器支持的模组加载器
 */
val curseForgeModLoaderFilters = listOf(
    CurseForgeModLoader.FORGE,
    CurseForgeModLoader.FABRIC,
    CurseForgeModLoader.NEOFORGE,
    CurseForgeModLoader.QUILT
)