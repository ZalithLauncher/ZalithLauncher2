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

import androidx.compose.ui.graphics.Color
import com.movtery.zalithlauncher.R
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = PlatformReleaseType.Serializer::class)
enum class PlatformReleaseType(val curseforgeCode: Int, val textRes: Int, val color: Color) {
    @SerialName("release")
    RELEASE(1, R.string.download_assets_release_type_release, Color(0xFF00AE5C)),

    @SerialName("beta")
    BETA(2, R.string.download_assets_release_type_beta, Color(0xFFDF8225)),

    @SerialName("alpha")
    ALPHA(3, R.string.download_assets_release_type_alpha, Color(0xFFCA2245));

    companion object {
        private val map = PlatformReleaseType.entries.associateBy { it.curseforgeCode }
        fun fromCurseForgeCode(code: Int): PlatformReleaseType = map[code] ?: error("Unknown release code: $code")
    }

    object Serializer : KSerializer<PlatformReleaseType> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("PlatformReleaseType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): PlatformReleaseType {
            return when (val input = runCatching { decoder.decodeInt() }.getOrNull()) {
                null -> {
                    val name = decoder.decodeString().lowercase()
                    when (name) {
                        "release" -> RELEASE
                        "beta" -> BETA
                        "alpha" -> ALPHA
                        else -> error("Unknown type name: $name")
                    }
                }
                else -> {
                    PlatformReleaseType.fromCurseForgeCode(input)
                }
            }
        }

        override fun serialize(encoder: Encoder, value: PlatformReleaseType) {
            val name = when (value) {
                RELEASE -> "release"
                BETA -> "beta"
                ALPHA -> "alpha"
            }
            encoder.encodeString(name)
        }
    }
}