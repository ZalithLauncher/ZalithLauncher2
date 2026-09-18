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

import androidx.compose.ui.graphics.Color
import com.movtery.layer_controller.layout.ControlLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.io.File
import java.util.Base64
import java.util.Locale
import java.util.UUID

internal val layoutJson = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    serializersModule = SerializersModule {
        contextual(Color::class, ColorSerializer)
    }
}

internal fun randomUUID(length: Int = 12): String =
    UUID.randomUUID()
        .toString()
        .replace("-", "")
        .take(length)

internal fun getAButtonUUID() = randomUUID(18)

/**
 * 生成一个随机的文件名
 * @param characters 字符数
 */
fun newRandomFileName(characters: Int = 8): String {
    val uuid = UUID.randomUUID()
    val bytes = ByteArray(16).apply {
        System.arraycopy(
            longToBytes(uuid.mostSignificantBits), 0, this, 0, 8
        )
        System.arraycopy(
            longToBytes(uuid.leastSignificantBits), 0, this, 8, 8
        )
    }

    val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    return encoded.take(characters)
}

private fun longToBytes(long: Long): ByteArray {
    val buffer = ByteArray(8)
    for (i in 0 until 8) {
        buffer[i] = (long shr (8 * (7 - i))).toByte()
    }
    return buffer
}

suspend fun ControlLayout.saveToFile(file: File) {
    withContext(Dispatchers.IO) {
        val jsonString = layoutJson.encodeToString(this@saveToFile)
        val parentDir = file.parentFile ?: File(".")
        parentDir.mkdirs()

        // 先写入临时文件再替换目标文件
        val tempFile = File(parentDir, "${file.name}.tmp")
        try {
            tempFile.writeText(jsonString)
            if (!tempFile.renameTo(file)) {
                tempFile.copyTo(file, overwrite = true)
                tempFile.delete()
            }
        } catch (e: Exception) {
            tempFile.delete()
            throw e
        }
    }
}

fun <E> checkMin(
    tag: String,
    value: E,
    min: E
) where E : Number, E : Comparable<E> {
    if (value < min) {
        error("$tag is below its minimum value: $min, current: $value")
    }
}

fun <E : Comparable<E>> checkInRange(
    tag: String,
    value: E,
    range: ClosedRange<E>
) {
    if (value !in range) {
        error("{$tag} exceeds the valid range of values: $range, current: $value")
    }
}

/**
 * 生成简单的语言标签
 */
fun Locale.toSimpleLangTag(): String {
    return "$language-$country"
}

internal fun Locale.compareLangTag(
    targetTag: String
): Boolean {
    return if (targetTag.contains("-")) {
        toSimpleLangTag() == targetTag
    } else {
        //仅检查语言（不包含国家）
        language == targetTag
    }
}