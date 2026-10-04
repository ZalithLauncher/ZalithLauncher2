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

package com.movtery.zalithlauncher.filemanager.logic.task

/** 任务种类，同一时间仅允许一个任务运行 */
enum class TaskKind {
    LIST,
    SEARCH,
    COPY,
    MOVE,
    DELETE,
    COMPRESS,
    EXTRACT,
    IMPORT,
    TRASH_RESTORE,
    TRASH_PURGE,
    TRASH_CLEAR;

    /** 是否应弹出任务对话框 */
    val shouldShowProgressDialog: Boolean
        get() = this != LIST && this != SEARCH
}