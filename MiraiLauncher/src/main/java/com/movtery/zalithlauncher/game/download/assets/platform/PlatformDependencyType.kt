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

package com.movtery.zalithlauncher.game.download.assets.platform

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = PlatformDependencyType.Serializer::class)
enum class PlatformDependencyType(val curseforgeCode: Int) {
    @SerialName("required")
    REQUIRED(3),            //依赖
    @SerialName("optional")
    OPTIONAL(2),            //可选
    @SerialName("incompatible")
    INCOMPATIBLE(5),        //不兼容
    @SerialName("embedded")
    EMBEDDED(1),            //嵌入式

    @SerialName("tool")
    TOOL(4),                //工具 (CurseForge)
    @SerialName("include")
    INCLUDE(6);             //包括 (CurseForge)

    companion object {
        private val map = PlatformDependencyType.entries.associateBy { it.curseforgeCode }
        fun fromCurseForgeCode(code: Int): PlatformDependencyType = map[code] ?: error("Unknown dependency code: $code")
    }

    object Serializer : KSerializer<PlatformDependencyType> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("PlatformDependencyType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): PlatformDependencyType {
            return when (val input = runCatching { decoder.decodeInt() }.getOrNull()) {
                null -> {
                    val name = decoder.decodeString().lowercase()
                    when (name) {
                        "required" -> REQUIRED
                        "optional" -> OPTIONAL
                        "incompatible" -> INCOMPATIBLE
                        "embedded" -> EMBEDDED
                        else -> error("Unknown type name: $name")
                    }
                }
                else -> {
                    PlatformDependencyType.fromCurseForgeCode(input)
                }
            }
        }

        override fun serialize(encoder: Encoder, value: PlatformDependencyType) {
            val name = when (value) {
                REQUIRED -> "required"
                OPTIONAL -> "optional"
                INCOMPATIBLE -> "incompatible"
                EMBEDDED -> "embedded"
                TOOL -> "tool"
                INCLUDE -> "include"
            }
            encoder.encodeString(name)
        }
    }
}