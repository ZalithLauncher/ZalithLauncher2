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

package com.movtery.zalithlauncher.game.version.export.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 可选择的文件树
 * @param alias 当前节点的别称，安卓字符串资源
 * @param child 如果有子节点（不为null），则当前节点为文件夹目录
 *              如果没有子节点（null），则当前节点为文件
 */
class FileSelectionData(
    val file: File,
    val alias: Int? = null,
    val child: List<FileSelectionData>? = null
): Comparable<FileSelectionData> {
    private val _selected = MutableStateFlow(Selected.Unselected)
    /** 当前节点的选中状态 */
    val selected = _selected.asStateFlow()

    private val _expand = MutableStateFlow(false)
    /** 当前节点的展开状态（文件夹节点） */
    val expand = _expand.asStateFlow()

    private var _cachedSelected: Int = 0
    private var _cachedTotal: Int = 0

    /**
     * 更新这个节点的选中状态
     */
    fun updateSelectState(new: Selected) {
        if (new == Selected.Indeterminate) {
            error("File node cannot be set to \"Indeterminate\" selection state")
        }

        if (child != null && child.isEmpty()) {
            //子节点为空时，不允许选择
            iterativeSelect(Selected.Unselected)
        } else {
            iterativeSelect(new)
        }
    }

    private fun iterativeSelect(new: Selected) {
        val stack = ArrayDeque<FileSelectionData>()
        stack.add(this)

        while (stack.isNotEmpty()) {
            val node = stack.removeLast()
            node._selected.update { new }

            val children = node.child ?: continue
            //更新子节点的选中状态
            for (childNode in children) {
                if (childNode.child != null && childNode.child.isEmpty()) {
                    //子节点为空时，不允许选择
                    childNode._selected.update { Selected.Unselected }
                } else {
                    stack.add(childNode)
                }
            }
        }
    }

    /**
     * 展开/收起当前节点，收起时同时应用到子节点
     */
    fun expandDirs(state: Boolean) {
        //更新当前节点
        _expand.update { state }

        if (!state && child != null) {
            val stack = ArrayDeque<FileSelectionData>()
            child.let { stack.addAll(it) }

            while (stack.isNotEmpty()) {
                val node = stack.removeLast()

                node._expand.update { false }
                node.child?.let { children ->
                    stack.addAll(children)
                }
            }
        }
    }

    override fun compareTo(other: FileSelectionData): Int {
        val thisIsFile = isFile()
        val otherIsFile = other.isFile()

        return when {
            thisIsFile != otherIsFile -> {
                if (!thisIsFile) -1 else 1
            }
            else -> {
                val nameCompare = file.name.compareTo(other.file.name)
                if (nameCompare != 0) {
                    nameCompare
                } else {
                    //如果文件名相同，用绝对路径作为最终依据
                    file.absolutePath.compareTo(other.file.absolutePath)
                }
            }
        }
    }

    companion object {
        /**
         * 刷新文件夹根节点的选中状态
         * @return 选中了多少个文件
         */
        suspend fun refreshTreeSelect(
            list: List<FileSelectionData>
        ): Int {
            return withContext(Dispatchers.Default) {
                ensureActive()

                var selectedFiles = 0
                val stack = ArrayDeque<Pair<FileSelectionData, Boolean>>()

                list.forEach { stack.add(it to false) }

                while (stack.isNotEmpty()) {
                    ensureActive()
                    val (node, visited) = stack.removeLast()

                    if (!visited) {
                        stack.add(node to true)
                        node.child?.forEach {
                            stack.add(it to false)
                        }
                    } else {
                        val children = node.child

                        if (children == null) {
                            //文件节点
                            if (node._selected.value == Selected.Selected) {
                                selectedFiles++
                                node._cachedSelected = 1
                            } else {
                                node._cachedSelected = 0
                            }
                            node._cachedTotal = 1
                        } else {
                            var total = 0
                            var selected = 0

                            for (child in children) {
                                total += child._cachedTotal
                                selected += child._cachedSelected
                            }

                            node._cachedTotal = total
                            node._cachedSelected = selected

                            node._selected.update {
                                when {
                                    total == 0 -> Selected.Unselected
                                    selected <= 0 -> Selected.Unselected
                                    selected < total -> Selected.Indeterminate
                                    else -> Selected.Selected
                                }
                            }

                            selectedFiles += selected
                        }
                    }
                }

                selectedFiles
            }
        }
    }
}

fun FileSelectionData.isFile(): Boolean = child == null

/**
 * 以递归的方式，获取所有节点所有选中的文件
 */
fun List<FileSelectionData>.getSelectedFiles(): List<File> {
    return asSequence()
        .flatMap { node ->
            when {
                node.child == null && node.selected.value == Selected.Selected -> sequenceOf(node.file)
                !node.child.isNullOrEmpty() -> node.child.getSelectedFiles().asSequence()
                else -> emptySequence()
            }
        }
        .toList()
}