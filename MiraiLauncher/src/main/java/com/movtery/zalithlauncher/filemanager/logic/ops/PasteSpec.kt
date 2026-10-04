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

package com.movtery.zalithlauncher.filemanager.logic.ops

import java.nio.file.Path

/** 顶层冲突处理选项 */
enum class ConflictResolution {
    /** 跳过该项 */
    SKIP,
    /** 若为文件则完全覆盖，为文件夹则合并内容 */
    OVERWRITE,
    /** 为待写入条目追加后缀 */
    KEEP_BOTH
}

/** 粘贴模式。 */
enum class PasteMode {
    /** 复制 */
    COPY,
    /** 移动 */
    MOVE
}

/** 粘贴请求构建结果。 */
sealed interface PasteRequest {
    /** 不需要决策，可以直接执行 */
    data class Ready(val sources: List<Path>, val targetDir: Path, val mode: PasteMode) : PasteRequest

    /** 存在顶层冲突，需决策后才能执行 */
    data class ResolveRequest(
        val sources: List<Path>,
        val targetDir: Path,
        val mode: PasteMode,
        /** 与 [sources] 一一对应的冲突项，非空表示该项需要决策 */
        val conflicts: List<ConflictItem?>
    ) : PasteRequest
}

data class ConflictItem(val source: Path, val existing: Path)

/** 粘贴完成后的项级结果。 */
data class ItemResult(
    val source: Path,
    val target: Path?,
    val success: Boolean,
    val reason: String?
)

/** 整体粘贴结果。 */
data class PasteSummary(
    val mode: PasteMode,
    val targetDir: Path,
    val results: List<ItemResult>
)