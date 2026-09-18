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

import com.movtery.zalithlauncher.filemanager.os.FmLog
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions

private const val TAG = "FmFilePerms"

object FilePermissions {
    private val FILE_PERMS: Set<PosixFilePermission> = PosixFilePermissions.fromString("rw-rw-r--")
    private val DIR_PERMS: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwxrwxr-x")

    fun apply(path: Path) {
        try {
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) return

            val isDir = Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
            val permissions = if (isDir) DIR_PERMS else FILE_PERMS
            Files.setPosixFilePermissions(path, permissions)
        } catch (_: UnsupportedOperationException) {
            //文件系统不支持 POSIX 权限，忽略
        } catch (e: Exception) {
            FmLog.warn(TAG, "Failed to set permissions on $path", e)
        }
    }
}
