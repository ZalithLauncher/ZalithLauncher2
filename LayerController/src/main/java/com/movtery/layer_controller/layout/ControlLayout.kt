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

package com.movtery.layer_controller.layout

import com.movtery.layer_controller.EDITOR_VERSION
import com.movtery.layer_controller.data.ButtonStyle
import com.movtery.layer_controller.data.lang.EmptyTranslatableString
import com.movtery.layer_controller.data.lang.TranslatableString
import com.movtery.layer_controller.layout.ControlLayout.Info
import com.movtery.layer_controller.observable.Modifiable
import com.movtery.layer_controller.observable.isModified
import com.movtery.layer_controller.updateLayoutToNew
import com.movtery.layer_controller.utils.layoutJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

/**
 * 描述一个控制布局结构
 * @param info 控制布局基本信息
 * @param layers 控制层级列表
 * @param special 可以变更启动器层的部分组件的扩展设定
 * @param editorVersion 使用编辑器版本
 */
@Serializable
data class ControlLayout(
    @SerialName("info")
    val info: Info,
    @SerialName("layers")
    val layers: List<ControlLayer> = emptyList(),
    @SerialName("styles")
    val styles: List<ButtonStyle> = emptyList(),
    @SerialName("special")
    val special: Special = Special(),
    @SerialName("editorVersion")
    val editorVersion: Int
) : Modifiable<ControlLayout> {
    @Serializable
    data class Info(
        @SerialName("name")
        val name: TranslatableString,
        @SerialName("author")
        val author: TranslatableString,
        @SerialName("description")
        val description: TranslatableString,
        @SerialName("versionCode")
        val versionCode: Int,
        @SerialName("versionName")
        val versionName: String
    ) : Modifiable<Info> {
        override fun isModified(other: Info): Boolean {
            return other.name.isModified(this.name) ||
                    other.author.isModified(this.author) ||
                    other.description.isModified(this.description) ||
                    this.versionCode != other.versionCode ||
                    this.versionName != other.versionName
        }
    }

    /**
     * 特殊设定，比如由控制布局改变启动器的部分组件属性之类的
     */
    @Serializable
    data class Special(
        val _dummy: Int = 0
        //将来可扩展更多设定
    ) : Modifiable<Special> {
        override fun isModified(other: Special): Boolean {
            return false
        }
    }

    override fun isModified(other: ControlLayout): Boolean {
        return this.info.isModified(other.info) ||
                this.layers.isModified(other.layers) ||
                this.styles.isModified(other.styles) ||
                this.special.isModified(other.special) ||
                this.editorVersion != other.editorVersion
    }
}

val EmptyLayoutInfo = Info(
    name = EmptyTranslatableString,
    author = EmptyTranslatableString,
    description = EmptyTranslatableString,
    versionCode = 0,
    versionName = ""
)

val EmptyControlLayout = ControlLayout(
    editorVersion = EDITOR_VERSION,
    info = EmptyLayoutInfo,
    layers = emptyList(),
    styles = emptyList()
)

/**
 * 从文件加载控制布局配置（检查版本号，大于编辑器版本则抛出`IllegalArgumentException`）
 */
fun loadLayoutFromFile(file: File): ControlLayout {
    val jsonString = file.readText()
    return loadLayoutFromString(jsonString)
}

fun loadLayoutFromString(jsonString: String): ControlLayout {
    val jsonObject = layoutJson.decodeFromString<JsonObject>(jsonString)
    if (jsonObject["editorVersion"] == null) throw SerializationException("The file does not contain the key \"editorVersion\".")
    val version = jsonObject["editorVersion"]!!.jsonPrimitive.int
    if (version <= EDITOR_VERSION) {
        var layout = layoutJson.decodeFromString<ControlLayout>(jsonString)
        if (version < EDITOR_VERSION) layout = updateLayoutToNew(layout)
        return layout
    } else {
        throw IllegalArgumentException("Control layout versions are not supported!")
    }
}

/**
 * 从文件加载控制布局配置（不检查版本号）
 */
fun loadLayoutFromFileUncheck(file: File): ControlLayout {
    val jsonString = file.readText()
    return layoutJson.decodeFromString<ControlLayout>(jsonString)
}