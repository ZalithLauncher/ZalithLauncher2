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

package com.movtery.zalithlauncher.game.version.mod.meta

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.movtery.zalithlauncher.game.version.mod.meta.PackMcMeta.DescriptionContent

data class PackMcMeta(
    @SerializedName("pack")
    val pack: PackInfo
) {
    data class PackInfo(
        @SerializedName("pack_format")
        val packFormat: Int,

        @JsonAdapter(DescriptionContentAdapter::class)
        @SerializedName("description")
        val description: DescriptionContent
    )

    sealed class DescriptionContent {
        /**
         * 纯文本描述
         */
        data class Text(val text: String) : DescriptionContent()
        
        /**
         * 带格式的描述
         */
        data class Formatted(val parts: List<Part>) : DescriptionContent()
        
        /**
         * 描述部分
         */
        data class Part(
            val text: String,
            val color: String? = null
        )

        fun toPlainText(): String {
            return when (this) {
                is Text -> text
                is Formatted -> parts.joinToString("") { it.text }
            }
        }
    }
}

private class DescriptionContentAdapter : JsonDeserializer<DescriptionContent> {
    override fun deserialize(
        json: JsonElement,
        type: java.lang.reflect.Type,
        context: JsonDeserializationContext
    ): DescriptionContent {
        return when {
            json.isJsonPrimitive -> DescriptionContent.Text(json.asString)
            json.isJsonObject -> {
                val obj = json.asJsonObject
                val str = (obj.get("text")?.asString ?: obj.get("fallback")?.asString) ?: ""
                DescriptionContent.Text(str)
            }
            json.isJsonArray -> {
                val parts = mutableListOf<DescriptionContent.Part>()
                for (element in json.asJsonArray) {
                    when {
                        element.isJsonPrimitive -> parts.add(
                            DescriptionContent.Part(element.asString)
                        )
                        element.isJsonObject -> {
                            val partObj = element.asJsonObject
                            parts.add(
                                DescriptionContent.Part(
                                    text = partObj.get("text")?.asString ?: "",
                                    color = partObj.get("color")?.asString
                                )
                            )
                        }
                    }
                }
                DescriptionContent.Formatted(parts)
            }
            else -> DescriptionContent.Text("")
        }
    }
}