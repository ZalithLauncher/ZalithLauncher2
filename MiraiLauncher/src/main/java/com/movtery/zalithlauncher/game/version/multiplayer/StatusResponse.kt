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

package com.movtery.zalithlauncher.game.version.multiplayer

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.movtery.zalithlauncher.game.text.TextColor
import com.movtery.zalithlauncher.game.text.parseColorFromIdentifier
import com.movtery.zalithlauncher.game.text.shadowColor
import com.movtery.zalithlauncher.game.version.multiplayer.description.ComponentDescription
import com.movtery.zalithlauncher.game.version.multiplayer.description.ComponentDescriptionRoot
import com.movtery.zalithlauncher.game.version.multiplayer.description.ServerDescription
import com.movtery.zalithlauncher.game.version.multiplayer.description.StringDescription
import com.movtery.zalithlauncher.path.GLOBAL_JSON
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.charset.StandardCharsets
import java.util.Base64

@Serializable
data class StatusResponse(
    val description: ServerDescription? = null,
    val players: Players,
    val version: Version,
    val favicon: Favicon? = null,
    val enforcesSecureChat: Boolean = false
) {
    companion object {
        fun parse(raw: String): StatusResponse {
            return GLOBAL_JSON.decodeFromString(StatusResponseSerializer, raw)
        }
    }
}

/**
 * 服务器的玩家信息
 * @param max 最多可允许多少玩家加入
 * @param online 当前有多少玩家在线
 * @param sample 简要描述当前在线的玩家
 */
@Serializable
data class Players(
    val max: Int,
    val online: Int,
    val sample: List<PlayerSample> = emptyList()
)

@Serializable
data class PlayerSample(
    val name: String,
    val id: String
)

@Serializable
data class Version(
    val name: String,
    val protocol: Int
)

private const val ICON_BYTE_PREFIX = "data:image/png;base64,"

@Serializable(with = Favicon.FaviconSerializer::class)
class Favicon(
    val icon: ByteArray
) {
    object FaviconSerializer : KSerializer<Favicon> {
        private val delegateSerializer = serializer()

        override val descriptor: SerialDescriptor = delegateSerializer.descriptor

        override fun serialize(encoder: Encoder, value: Favicon) {
            val base64 = Base64.getEncoder().encode(value.icon)
            val base64String = String(base64, StandardCharsets.UTF_8)
            encoder.encodeString(ICON_BYTE_PREFIX + base64String)
        }

        override fun deserialize(decoder: Decoder): Favicon {
            val string = decoder.decodeString()
            //Minecraft 要求必须以这个格式进行解析
            require(string.startsWith(ICON_BYTE_PREFIX))
            val base64 = string.substring(ICON_BYTE_PREFIX.length).replace("\n", "")
            val icon = Base64.getDecoder().decode(base64.toByteArray())
            return Favicon(icon)
        }
    }
}

object StatusResponseSerializer : KSerializer<StatusResponse> {
    private val delegateSerializer = StatusResponse.serializer()

    override val descriptor: SerialDescriptor = delegateSerializer.descriptor

    override fun deserialize(decoder: Decoder): StatusResponse {
        val jsonDecoder = decoder as? JsonDecoder ?: throw SerializationException("This serializer can only be used with Json")
        val jsonElement = jsonDecoder.decodeJsonElement()

        val jsonObject = jsonElement.jsonObject

        val description = jsonObject["description"]?.let { parseDescriptionFromJson(it) }
        val players = GLOBAL_JSON.decodeFromJsonElement(Players.serializer(), jsonObject["players"]!!)
        val version = GLOBAL_JSON.decodeFromJsonElement(Version.serializer(), jsonObject["version"]!!)
        val favicon = jsonObject["favicon"]?.let { GLOBAL_JSON.decodeFromJsonElement(Favicon.serializer(), it) }
        val enforcesSecureChat = jsonObject["enforcesSecureChat"]?.jsonPrimitive?.booleanOrNull ?: false

        return StatusResponse(
            description = description,
            players = players,
            version = version,
            favicon = favicon,
            enforcesSecureChat = enforcesSecureChat
        )
    }

    override fun serialize(encoder: Encoder, value: StatusResponse) {
        delegateSerializer.serialize(encoder, value)
    }
}

fun parseDescriptionFromJson(element: JsonElement): ServerDescription? {
    return when (element) {
        is JsonArray -> {
            var root: ComponentDescription? = null

            val values = element.mapIndexedNotNull { index, element0 ->
                val data = parseComponentRecursive(element0, root)
                if (index == 0) {
                    root = data
                }
                data
            }

            ComponentDescriptionRoot(values)
        }
        is JsonObject -> {
            parseComponentRecursive(element, null)
        }
        is JsonPrimitive -> {
            StringDescription(value = element.contentOrNull ?: "")
        }
    }
}

private fun parseComponentRecursive(
    element: JsonElement,
    parent: ComponentDescription?
): ComponentDescription? {
    return when (element) {
        is JsonObject -> {
            val currentComponent = element.tryParseText(parent)

            val extra = mutableListOf<ComponentDescription>()
            val extraElement = element["extra"]

            if (extraElement is JsonArray) {
                extraElement.forEach { childElement ->
                    parseComponentRecursive(
                        element = childElement,
                        parent = currentComponent
                    )?.let { child ->
                        extra.add(child)
                    }
                }
            }

            currentComponent?.copy(
                extra = extra
            ) ?: ComponentDescription(
                text = "",
                extra = extra
            )
        }
        is JsonPrimitive -> {
            ComponentDescription(
                text = element.contentOrNull ?: "",
                color = parent?.color,
                bold = parent?.bold,
                italic = parent?.italic,
                underlined = parent?.underlined,
                strikethrough = parent?.strikethrough,
                obfuscated = parent?.obfuscated
            )
        }
        else -> null
    }
}

private fun JsonObject.tryParseText(
    parent: ComponentDescription?
): ComponentDescription? {
    //判断类型，如果类型不是text，则忽略
    //如果未填类型，则默认为text
    val type = get("type")?.jsonPrimitive?.contentOrNull ?: "text"
    val isText = type == "text"
    if (isText) {
        val text = get("text")?.jsonPrimitive?.contentOrNull ?: ""

        val color = get("color")?.jsonPrimitive?.contentOrNull?.let { content ->
            if (content.startsWith('#')) {
                runCatching {
                    val color = Color(content.toColorInt())
                    //动态生成阴影色
                    val background = shadowColor(color)
                    TextColor(color, background)
                }.getOrNull()
            } else {
                //用颜色代码表示的颜色
                parseColorFromIdentifier(content)
            }
        } ?: parent?.color

        val bold = get("bold")?.jsonPrimitive?.booleanOrNull ?: parent?.bold
        val italic = get("italic")?.jsonPrimitive?.booleanOrNull ?: parent?.italic
        val underlined = get("underlined")?.jsonPrimitive?.booleanOrNull ?: parent?.underlined
        val strikethrough = get("strikethrough")?.jsonPrimitive?.booleanOrNull ?: parent?.strikethrough
        val obfuscated = get("obfuscated")?.jsonPrimitive?.booleanOrNull ?: parent?.obfuscated

        return ComponentDescription(
            text = text,
            color = color,
            bold = bold,
            italic = italic,
            underlined = underlined,
            strikethrough = strikethrough,
            obfuscated = obfuscated
        )
    } else {
        return null
    }
}