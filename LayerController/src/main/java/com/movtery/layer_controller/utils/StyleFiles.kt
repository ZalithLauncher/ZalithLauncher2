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

package com.movtery.layer_controller.utils

import com.movtery.layer_controller.data.ButtonStyle
import com.movtery.layer_controller.data.JoystickStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 将控件外观样式保存到目标文件夹，文件名为样式 uuid
 * @return 保存的文件
 */
suspend fun ButtonStyle.saveToStyleFile(dir: File): File {
    val style = this
    return writeStyleFile(dir, uuid) {
        layoutJson.encodeToString(ButtonStyle.serializer(), style)
    }
}

/**
 * 将摇杆样式保存到目标文件夹，文件名为样式 uuid
 * @return 保存的文件
 */
suspend fun JoystickStyle.saveToStyleFile(dir: File): File {
    val style = this
    return writeStyleFile(dir, uuid) {
        layoutJson.encodeToString(JoystickStyle.serializer(), style)
    }
}

/**
 * 从文件加载控件外观样式
 */
fun loadButtonStyle(file: File): ButtonStyle {
    return layoutJson.decodeFromString(ButtonStyle.serializer(), file.readText())
}

/**
 * 从文件加载摇杆样式
 */
fun loadJoystickStyle(file: File): JoystickStyle {
    return layoutJson.decodeFromString(JoystickStyle.serializer(), file.readText())
}

private suspend fun writeStyleFile(
    dir: File,
    uuid: String,
    encode: () -> String
): File = withContext(Dispatchers.IO) {
    val file = File(dir, "$uuid.json")
    val parentDir = file.parentFile ?: File(".")
    parentDir.mkdirs()

    // 先写入临时文件再替换目标文件
    val tempFile = File(parentDir, "${file.name}.tmp")
    try {
        tempFile.writeText(encode())
        if (!tempFile.renameTo(file)) {
            tempFile.copyTo(file, overwrite = true)
            tempFile.delete()
        }
    } catch (e: Exception) {
        tempFile.delete()
        throw e
    }
    file
}
