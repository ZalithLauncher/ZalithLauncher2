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

import com.movtery.zalithlauncher.filemanager.logic.AccessScope
import com.movtery.zalithlauncher.filemanager.logic.FilenameValidator
import com.movtery.zalithlauncher.filemanager.logic.FmFilenameError
import com.movtery.zalithlauncher.filemanager.logic.FmFilenameException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class RenameCreateOps(private val scope: AccessScope) {

    /** 重命名条目 */
    suspend fun rename(
        target: Path,
        newName: String
    ): Path {
        FilenameValidator.check(newName)
        val safeTarget = scope.guardAbsolute(target)
        if (!Files.exists(safeTarget, LinkOption.NOFOLLOW_LINKS)) {
            throw IOException("Target does not exist")
        }
        val parent = safeTarget.parent ?: throw IOException("Target has no parent")
        if (!Files.isWritable(parent)) throw IOException("Parent is not writable")
        val dest = parent.resolve(newName).normalize().toAbsolutePath()
        scope.guardAbsolute(dest)
        if (Files.exists(dest, LinkOption.NOFOLLOW_LINKS) && dest != safeTarget) {
            throw FmFilenameException("Name conflicts with existing entry", FmFilenameError.NAME_CONFLICT)
        }
        withContext(Dispatchers.IO) {
            try {
                Files.move(safeTarget, dest, StandardCopyOption.ATOMIC_MOVE)
            } catch (_: Exception) {
                Files.move(safeTarget, dest, StandardCopyOption.REPLACE_EXISTING)
            }
        }
        return dest
    }

    /** 新建文件夹 */
    suspend fun createFolder(parent: Path, name: String): Path = createEntry(parent, name, isFolder = true)

    /** 新建空白文件 */
    suspend fun createFile(parent: Path, name: String): Path = createEntry(parent, name, isFolder = false)

    private suspend fun createEntry(
        parent: Path,
        name: String,
        isFolder: Boolean
    ): Path {
        FilenameValidator.check(name)
        val safeParent = scope.guard(parent)
        if (!Files.isDirectory(safeParent)) throw IOException("Parent is not a directory")
        if (!Files.isWritable(safeParent)) throw IOException("Parent is not writable")

        val child = safeParent.resolve(name).normalize().toAbsolutePath()
        scope.guardAbsolute(child)

        if (Files.exists(child, LinkOption.NOFOLLOW_LINKS)) {
            throw FmFilenameException("Name conflicts with existing entry", FmFilenameError.NAME_CONFLICT)
        }

        withContext(Dispatchers.IO) {
            if (isFolder) {
                Files.createDirectories(child)
            } else {
                Files.createFile(child)
            }
            FilePermissions.apply(child)
        }

        return child
    }
}