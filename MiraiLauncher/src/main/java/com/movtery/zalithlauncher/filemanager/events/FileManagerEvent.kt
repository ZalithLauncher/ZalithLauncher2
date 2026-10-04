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

package com.movtery.zalithlauncher.filemanager.events

import android.os.Bundle
import android.os.Message
import android.os.Messenger
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

/**
 * 文件管理器文件变更事件
 * @param type 事件类型
 * @param changedDirs 发生变更的目录绝对路径列表
 * @param id 事件唯一 ID
 */
@Parcelize
data class FileManagerEvent(
    val type: Type,
    val changedDirs: List<String>,
    val id: String = UUID.randomUUID().toString()
) : Parcelable {
    enum class Type {
        /** 新建文件/文件夹 */
        CREATE,
        /** 条目重命名 */
        RENAME,
        /** 删除（移入回收站） */
        DELETE,
        /** 复制/粘贴 */
        COPY_PASTE,
        /** 压缩 */
        ARCHIVE,
        /** 解压 */
        EXTRACT,
        /** 从 SAF 导入完成 */
        IMPORT,
        /** 回收站恢复 */
        TRASH_RESTORE,
        /** 回收站彻底删除 */
        TRASH_PURGE,
        /** 回收站清空 */
        TRASH_CLEAR
    }

    companion object {
        const val KEY_EVENT = "fm_event"

        /**
         * 将事件封装为可经 Messenger 发送的 [Message]
         */
        fun toMessage(event: FileManagerEvent): Message = Message.obtain().apply {
            what = MSG_EVENT
            data = Bundle().apply {
                putParcelable(KEY_EVENT, event)
            }
        }

        /**
         * 从 [Message] 中解析事件；非法消息返回 null
         */
        fun fromMessage(message: Message): FileManagerEvent? {
            if (message.what != MSG_EVENT) return null
            val data = message.data ?: return null
            data.classLoader = FileManagerEvent::class.java.classLoader
            return data.getParcelable(KEY_EVENT)
        }

        const val MSG_REGISTER_CLIENT = 1
        const val MSG_UNREGISTER_CLIENT = 2
        const val MSG_EVENT = 100
    }
}

fun Messenger.sendEvent(event: FileManagerEvent) {
    runCatching {
        send(FileManagerEvent.toMessage(event))
    }
}
