/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.game.renderer.config

import androidx.annotation.StringRes
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

sealed interface ConfigText {
    data class Resource(@StringRes val resId: Int) : ConfigText
    data class Dynamic(val value: String) : ConfigText
}

sealed interface ConfigScalar {
    fun asString(): String
    fun toJson(): JsonPrimitive

    data class StringValue(val value: String) : ConfigScalar {
        override fun asString(): String = value
        override fun toJson(): JsonPrimitive = JsonPrimitive(value)
    }

    data class BooleanValue(val value: Boolean) : ConfigScalar {
        override fun asString(): String = value.toString()
        override fun toJson(): JsonPrimitive = JsonPrimitive(value)
    }

    sealed interface NumberValue : ConfigScalar

    data class IntValue(val value: Int) : NumberValue {
        override fun asString(): String = value.toString()
        override fun toJson(): JsonPrimitive = JsonPrimitive(value)
    }

    data class DoubleValue(val value: Double) : NumberValue {
        override fun asString(): String = value.toString()
        override fun toJson(): JsonPrimitive = JsonPrimitive(value)
    }
}

sealed interface ConfigTarget {
    data class Environment(val key: String) : ConfigTarget
    data class JsonField(val key: String) : ConfigTarget
    /** SFPEW 兼容层（仅存于启动器配置） */
    data object SfpewLayer : ConfigTarget
}

/**
 * 渲染器配置项的基础定义，描述默认值、展示文案和启动时的生效目标
 */
sealed interface ConfigEntry {
    val id: String
    val title: ConfigText?
    val description: ConfigText?
    val defaultValue: ConfigScalar
    val defaultEnabled: Boolean? get() = null
    val target: ConfigTarget

    fun coerce(value: ConfigScalar): ConfigScalar
}

data class SwitchEntry(
    override val id: String,
    override val title: ConfigText?,
    override val description: ConfigText?,
    val default: Boolean,
    val enabledValue: ConfigScalar = ConfigScalar.StringValue("1"),
    override val target: ConfigTarget
) : ConfigEntry {
    override val defaultValue: ConfigScalar = ConfigScalar.BooleanValue(default)

    override fun coerce(value: ConfigScalar): ConfigScalar = when (value) {
        is ConfigScalar.BooleanValue -> value
        else -> ConfigScalar.BooleanValue(value.asString().isNotEmpty())
    }
}

data class ChoiceOption(
    val value: ConfigScalar,
    val label: ConfigText? = null,
    val description: ConfigText? = null
)

data class SingleChoiceEntry(
    override val id: String,
    override val title: ConfigText?,
    override val description: ConfigText?,
    val options: List<ChoiceOption>,
    override val defaultValue: ConfigScalar,
    override val defaultEnabled: Boolean? = null,
    override val target: ConfigTarget
) : ConfigEntry {
    override fun coerce(value: ConfigScalar): ConfigScalar =
        options.find { it.value.asString() == value.asString() }?.value ?: defaultValue
}

data class OrderedChoiceEntry(
    override val id: String,
    override val title: ConfigText?,
    override val description: ConfigText?,
    val options: List<ChoiceOption>,
    override val defaultValue: ConfigScalar.StringValue,
    override val defaultEnabled: Boolean? = null,
    override val target: ConfigTarget,
    val inheritFrom: String? = null,
    val nativeValue: String? = null
) : ConfigEntry {
    fun defaultValue(parentValue: ConfigScalar?): ConfigScalar.StringValue {
        if (inheritFrom == null || parentValue == null) return defaultValue
        val expanded = parentValue.asString().split(',').joinToString(",") {
            if (it == "native") nativeValue ?: it else it
        }
        return coerce(ConfigScalar.StringValue(expanded))
    }

    override fun coerce(value: ConfigScalar): ConfigScalar.StringValue {
        val valuesByKey = options.associateBy { it.value.asString() }
        val ordered = value.asString().split(',').mapNotNull(valuesByKey::get)
        val defaults = defaultValue.value.split(',').mapNotNull(valuesByKey::get)
        return ConfigScalar.StringValue(
            (ordered + defaults).distinctBy { it.value.asString() }.joinToString(",") { it.value.asString() }
        )
    }
}

data class NumberEntry(
    override val id: String,
    override val title: ConfigText?,
    override val description: ConfigText?,
    override val defaultValue: ConfigScalar.NumberValue,
    val emptyValue: ConfigScalar.NumberValue? = null,
    val integerOnly: Boolean = true,
    val min: Double? = null,
    val max: Double? = null,
    override val target: ConfigTarget
) : ConfigEntry {
    override fun coerce(value: ConfigScalar): ConfigScalar = when (value) {
        is ConfigScalar.StringValue if value.value.isEmpty() -> value
        else -> parse(value.asString()) ?: defaultValue
    }

    fun parse(text: String): ConfigScalar? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ConfigScalar.StringValue("")
        val number = if (integerOnly) {
            trimmed.toIntOrNull()?.let { ConfigScalar.IntValue(it) }
        } else {
            trimmed.toDoubleOrNull()?.let { ConfigScalar.DoubleValue(it) }
        } ?: return null
        val value = when (number) {
            is ConfigScalar.IntValue -> number.value.toDouble()
            is ConfigScalar.DoubleValue -> number.value
        }
        return number.takeIf {
            value.isFinite() && (min == null || value >= min) && (max == null || value <= max)
        }
    }
}

data class TextEntry(
    override val id: String,
    override val title: ConfigText?,
    override val description: ConfigText?,
    val default: String,
    override val target: ConfigTarget
) : ConfigEntry {
    override val defaultValue: ConfigScalar = ConfigScalar.StringValue(default)

    override fun coerce(value: ConfigScalar): ConfigScalar = ConfigScalar.StringValue(value.asString())
}

/**
 * 一个渲染器的完整配置规范，包含可编辑配置项和始终注入的固定环境变量
 */
data class RendererConfigSpec(
    val entries: List<ConfigEntry>,
    val fixedEnv: Map<String, String> = emptyMap()
)

fun configScalarFromJson(json: JsonPrimitive): ConfigScalar = when {
    json.isString -> ConfigScalar.StringValue(json.content)
    json.booleanOrNull != null -> ConfigScalar.BooleanValue(json.booleanOrNull!!)
    json.intOrNull != null -> ConfigScalar.IntValue(json.intOrNull!!)
    json.doubleOrNull != null -> ConfigScalar.DoubleValue(json.doubleOrNull!!)
    else -> ConfigScalar.StringValue(json.content)
}
