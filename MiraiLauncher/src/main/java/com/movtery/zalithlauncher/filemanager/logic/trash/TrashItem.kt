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

package com.movtery.zalithlauncher.filemanager.logic.trash

import org.json.JSONObject
import java.nio.file.Path

/**
 * 回收站条目元数据
 * @param originalPath 被删项的原始绝对路径
 * @param deletedAt 删除时间（epoch 毫秒）
 * @param name 被删项名称
 * @param isFolder 类型是否为目录
 * @param corrupted 异常标记
 */
data class TrashMeta(
    val originalPath: String,
    val deletedAt: Long,
    val name: String,
    val isFolder: Boolean,
    val corrupted: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("originalPath", originalPath)
        put("deletedAt", deletedAt)
        put("name", name)
        put("isFolder", isFolder)
        put("corrupted", corrupted)
    }

    companion object {
        fun fromJson(text: String): TrashMeta? = runCatching {
            val obj = JSONObject(text)
            TrashMeta(
                originalPath = obj.getString("originalPath"),
                deletedAt = obj.getLong("deletedAt"),
                name = obj.getString("name"),
                isFolder = obj.getBoolean("isFolder"),
                corrupted = obj.optBoolean("corrupted", false)
            )
        }.getOrNull()
    }
}

/**
 * 回收站列表项
 * @param contentDir 回收站内存储内容的路径
 * @param size 回收站内内容的总大小（字节）
 */
data class TrashItem(
    val uuid: String,
    val meta: TrashMeta,
    val contentDir: Path,
    val size: Long
) {
    val isFolder: Boolean get() = meta.isFolder
    val name: String get() = meta.name
    val deletedAt: Long get() = meta.deletedAt
}