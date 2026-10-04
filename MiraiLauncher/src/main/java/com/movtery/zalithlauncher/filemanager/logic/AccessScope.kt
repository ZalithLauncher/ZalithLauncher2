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

package com.movtery.zalithlauncher.filemanager.logic

import com.movtery.zalithlauncher.filemanager.os.FmLog
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

private const val TAG = "FileAccessScope"

/** 访问范围控制器 */
class AccessScope(val root: Path) {
    init {
        require(Files.isDirectory(root)) {
            "AccessScope root must be an existing directory: $root"
        }
    }

    /**
     * 归一化并校验 [target] 位于 [root] 之内（相对路径以 [root] 为基础解析），返回归一化后的绝对路径
     * @throws OutOfScopeException 越界时抛出
     */
    fun guard(target: Path): Path {
        val resolved = resolveAgainstRoot(target)
        val normalized = resolved.normalize().toAbsolutePath()
        requireInside(normalized)
        return normalized
    }

    /**
     * 校验绝对路径 [target] 位于 [root] 之内，返回归一化后的绝对路径
     * @throws OutOfScopeException 越界时抛出
     */
    fun guardAbsolute(target: Path): Path {
        val normalized = target.normalize().toAbsolutePath()
        requireInside(normalized)
        return normalized
    }

    /** 判断 [child] 是否是 [root] 的子项 */
    fun isUnder(child: Path): Boolean {
        val normalized = child.normalize().toAbsolutePath()
        return normalized == rootAbs || normalized.startsWith(rootAbs)
    }

    val rootAbs: Path = root.normalize().toAbsolutePath()

    private fun resolveAgainstRoot(target: Path): Path {
        return if (target.isAbsolute) {
            target
        } else {
            rootAbs.resolve(target)
        }
    }

    private fun requireInside(normalized: Path) {
        if (!normalized.startsWith(rootAbs)) {
            FmLog.warn(TAG, "Denied out-of-scope access.")
            throw OutOfScopeException("Target path is outside the accessible scope.")
        }
    }

    companion object {
        /**
         * 通过字符串路径构建 [AccessScope]
         * @throws IllegalArgumentException 根目录不存在时抛出
         */
        fun ofRoot(rootPath: String): AccessScope {
            val root = Paths.get(rootPath).normalize().toAbsolutePath()
            if (!Files.isDirectory(root)) {
                throw IllegalArgumentException("Root directory does not exist or is not a directory")
            }
            return AccessScope(root)
        }
    }
}

/** 超出可访问范围时抛出的异常，不携带越界路径细节 */
class OutOfScopeException(message: String) : SecurityException(message)