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

package com.movtery.zalithlauncher.game.version.multiplayer.description

import com.movtery.zalithlauncher.game.text.TextColor
import kotlinx.serialization.Serializable

private fun flattenComponents(
    description: ComponentDescription,
    out: StringBuilder
) {
    if (description.text.isNotEmpty()) {
        out.append(description.text)
    }

    description.extra.forEach { child ->
        flattenComponents(child, out)
    }
}

/**
 * 文本组件列表式服务器描述
 */
@Serializable
data class ComponentDescriptionRoot(
    val values: List<ComponentDescription>
): ServerDescription {
    override fun toString(): String {
        val content = buildString {
            values.forEach {
                flattenComponents(it, this@buildString)
            }
        }
        return "ComponentDescriptionRoot:\n$content"
    }
}

/**
 * 文本组件式服务器描述
 * [参考实现 WIKI](https://zh.minecraft.wiki/w/%E6%96%87%E6%9C%AC%E7%BB%84%E4%BB%B6#%E5%9F%BA%E7%A1%80%E7%BB%93%E6%9E%84)（仅部分实现）
 * @param text 这个组件的实际文本
 * @param color 控制这个文本组件的颜色，为 null 代表使用系统默认颜色
 * @param bold 控制这个文本组件是否为粗体
 * @param italic 控制这个文本组件是否为斜体
 * @param underlined 控制这个文本组件是否带有下划线
 * @param strikethrough 控制这个文本组件是否带有删除线
 * @param obfuscated 控制这个文本组件是否渲染为随机字符
 * @param extra 此文本组件的子组件，子组件自动继承父组件的文本属性（仅子组件未定义的属性）
 */
@Serializable
data class ComponentDescription(
    val text: String,
    val color: TextColor? = null,
    val bold: Boolean? = null,
    val italic: Boolean? = null,
    val underlined: Boolean? = null,
    val strikethrough: Boolean? = null,
    val obfuscated: Boolean? = null,
    val extra: List<ComponentDescription> = emptyList()
): ServerDescription {
    override fun toString(): String {
        val content = buildString {
            flattenComponents(this@ComponentDescription, this@buildString)
        }
        return "ComponentDescription:\n$content"
    }
}