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

package com.movtery.zalithlauncher.utils.json

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser

fun JsonObject.merge(other: JsonObject) {
    other.entrySet().forEach { (key, otherValue) ->
        when (val currentValue = this.get(key)) {
            //当前存在同名对象属性：递归合并
            is JsonObject -> if (otherValue is JsonObject) {
                currentValue.merge(otherValue)
            } else {
                // 类型不同时直接覆盖
                this.add(key, otherValue.deepCopy())
            }

            //当前存在同名数组：追加元素
            is JsonArray -> if (otherValue is JsonArray) {
                otherValue.forEach { element ->
                    currentValue.add(element.deepCopy())
                }
            } else {
                this.add(key, otherValue.deepCopy())
            }

            //当前属性不存在或为简单类型：直接覆盖
            else -> this.add(key, otherValue.deepCopy())
        }
    }
}

fun JsonObject.safeGetMember(memberName: String): String {
    return this.get(memberName)?.takeIf { it.isJsonPrimitive }?.asString ?: ""
}

/**
 * 快速解析为JsonObject
 */
fun String.parseToJson(): JsonObject = JsonParser.parseString(this).asJsonObject