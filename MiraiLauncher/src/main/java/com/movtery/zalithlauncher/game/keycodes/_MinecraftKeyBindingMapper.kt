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

package com.movtery.zalithlauncher.game.keycodes

import com.movtery.inputmap.keycodes.Lwjgl2Keycode
import com.movtery.inputmap.keycodes.MinecraftKeyBindingMapper
import com.movtery.zalithlauncher.game.launch.MCOptions

/**
 * 将字符串键映射到其对应的键码
 * @return 如果找到映射则返回键码，否则返回 `null`
 */
fun mapToKeycode(bindingKey: String?, defaultValue: String): Int? {
    val binding = bindingKey?.let { MCOptions.get(it) } ?: defaultValue

    return if (binding.startsWith("key.")) {
        //新版MC键绑定映射
        MinecraftKeyBindingMapper.getGlfwKeycode(binding)?.toInt()
    } else {
        binding.toIntOrNull()?.let { lwjgl2Code ->
            //MC旧版本直接存了LWJGL2的键值
            //将旧版本LWJGL2的键码转换为GLFW
            Lwjgl2Keycode.lwjgl2ToGlfw(lwjgl2Code)
        }
    }
}

/**
 * 将字符串键映射到其对应的控制布局事件标识
 * @return 如果找到映射则返回对应的标识，否则返回 `null`
 */
fun mapToControlEvent(bindingKey: String?, defaultValue: String): String? {
    val binding = bindingKey?.let { MCOptions.get(it) } ?: defaultValue

    return if (binding.startsWith("key.")) {
        MinecraftKeyBindingMapper.getControlEvent(binding)
    } else {
        binding.toIntOrNull()?.let { lwjgl2Code ->
            //MC旧版本直接存了LWJGL2的键值
            //将旧版本LWJGL2的键码转换为控制事件标识
            Lwjgl2Keycode.lwjgl2ToControlEvent(lwjgl2Code)
        }
    }
}