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

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import org.junit.Test

class TestStatusResponseSerializer {

    @Test
    fun testSerializer1() {
        val element = buildJsonArray {
            addJsonObject {
                put("text", JsonPrimitive("123"))
            }
            addJsonObject {
                put("type", JsonPrimitive("text"))
                put("text", JsonPrimitive("abc"))
                put("color", JsonPrimitive("yellow"))
                put("extra", buildJsonArray {
                    addJsonObject {
                        put("text", JsonPrimitive("def"))
                        put("color", JsonPrimitive("red"))
                    }
                })
            }
            addJsonObject {
                put("text", JsonPrimitive("123"))
            }
            addJsonObject {
                put("text", JsonPrimitive("456"))
                put("color", JsonPrimitive("blue"))
            }
            addJsonObject {
                put("text", JsonPrimitive("789"))
            }
        }

        println("")
        println(element)
        val data = parseDescriptionFromJson(element)

        println("======================")

        println(data)
        println("")
    }

    @Test
    fun testSerializer2() {
        val element = buildJsonArray {
            add(JsonPrimitive("123"))
            addJsonObject {
                put("text", JsonPrimitive("123"))
                put("color", JsonPrimitive("red"))
                put("extra", buildJsonArray {
                    add(JsonPrimitive("456"))
                })
            }
            add(JsonPrimitive("abc"))
        }

        println("")
        println(element)
        val data = parseDescriptionFromJson(element)

        println("======================")

        println(data)
        println("")
    }
}