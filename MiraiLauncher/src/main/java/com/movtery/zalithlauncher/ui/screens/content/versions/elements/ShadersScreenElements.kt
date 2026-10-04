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

package com.movtery.zalithlauncher.ui.screens.content.versions.elements

import java.io.File

sealed interface ShaderOperation {
    data object None : ShaderOperation
    /** 执行任务中 */
    data object Progress : ShaderOperation
    /** 重命名光影包输入对话框 */
    data class Rename(val info: ShaderPackInfo) : ShaderOperation
    /** 删除光影包对话框 */
    data class Delete(val info: ShaderPackInfo) : ShaderOperation
}

/**
 * 光影包信息
 */
data class ShaderPackInfo(
    val file: File,
    val fileSize: Long
)

/**
 * 简易过滤器，过滤特定的光影包
 */
fun List<ShaderPackInfo>.filterShaders(
    nameFilter: String
) = this.filter {
    nameFilter.isEmpty() || it.file.name.contains(nameFilter, true)
}