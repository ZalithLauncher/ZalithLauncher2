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

package com.movtery.zalithlauncher.ui.screens.content.versions.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.movtery.zalithlauncher.game.version.export.data.FileSelectionData

sealed interface VisibleNode {
    data class FileNode(
        val data: FileSelectionData,
        private val indentation0: Int
    ) : VisibleNode {
        override val indentation: Int get() = indentation0
        override val key: String get() = data.file.absolutePath
    }

    /** 仅展示空目录提示文本 */
    data class EmptyNode(
        private val key0: String,
        private val indentation0: Int,
    ) : VisibleNode {
        override val indentation: Int get() = indentation0
        override val key: String get() = key0
    }

    val indentation: Int
    val key: String
}

private data class StackItem(
    val node: FileSelectionData,
    val indentation: Int
)

@Composable
fun rememberVisibleNodes(
    list: List<FileSelectionData>,
    refreshExpand: Any? = null
): List<VisibleNode> {
    return remember(list, refreshExpand) {
        val result = ArrayList<VisibleNode>(list.size)
        val stack = ArrayDeque<StackItem>()

        for (i in list.indices.reversed()) {
            stack.addLast(
                StackItem(list[i], 0)
            )
        }

        while (stack.isNotEmpty()) {
            val (node, indentation) = stack.removeLast()

            result.add(
                VisibleNode.FileNode(node, indentation)
            )

            val child = node.child
            if (child != null && node.expand.value) {
                val indentation0 = indentation + 1

                if (child.isEmpty()) {
                    val key = "parent:" + node.file.absolutePath + ",indentation=" + indentation.toString()
                    result.add(
                        VisibleNode.EmptyNode(key, indentation0)
                    )
                } else {
                    for (i in child.indices.reversed()) {
                        stack.addLast(
                            StackItem(child[i], indentation0)
                        )
                    }
                }
            }
        }

        result
    }
}