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

package com.movtery.zalithlauncher.filemanager.ui

import androidx.navigation3.runtime.NavBackStack
import java.nio.file.Path

/**
 * 进入回收站页
 */
fun NavBackStack<FmNavKey>.openTrash() {
    if (lastOrNull() == FmNavKey.Trash) return
    add(FmNavKey.Trash)
}

/**
 * 退出回收站页返回主页面
 */
fun NavBackStack<FmNavKey>.closeTrash() {
    if (lastOrNull() == FmNavKey.Trash) {
        removeLastOrNull()
    }
}

/**
 * 进入文本编辑器页
 * @param path 待编辑文件的绝对路径
 */
fun NavBackStack<FmNavKey>.openEditor(path: Path) {
    if (lastOrNull() is FmNavKey.Editor) return
    add(FmNavKey.Editor(path.toString()))
}

/**
 * 退出文本编辑器页返回主页面
 */
fun NavBackStack<FmNavKey>.closeEditor() {
    if (lastOrNull() is FmNavKey.Editor) {
        removeLastOrNull()
    }
}
