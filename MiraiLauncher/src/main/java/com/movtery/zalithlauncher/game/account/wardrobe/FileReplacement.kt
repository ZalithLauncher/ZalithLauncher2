/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 * Copyright (C) 2026 Aerix Launcher contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.account.wardrobe

import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID

/**
 * Replaces [targetFile] from a staged file without discarding an existing target when replacement fails.
 * The staged file should be in the target directory so the preferred move can be atomic.
 */
internal fun replaceFileKeepingOriginal(stagedFile: File, targetFile: File) {
    val targetPath = targetFile.toPath()
    val stagedPath = stagedFile.toPath()
    val targetDirectory = targetFile.absoluteFile.parentFile
        ?: throw IllegalArgumentException("Target file must have a parent directory")
    Files.createDirectories(targetDirectory.toPath())

    val hadOriginal = Files.exists(targetPath)
    val backupFile = File(targetDirectory, ".${targetFile.name}.${UUID.randomUUID()}.backup")
    var backupCreated = false
    var keepBackup = false

    try {
        if (hadOriginal) {
            Files.copy(targetPath, backupFile.toPath())
            backupCreated = true
        }

        try {
            Files.move(
                stagedPath,
                targetPath,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(stagedPath, targetPath, StandardCopyOption.REPLACE_EXISTING)
        }
    } catch (failure: Exception) {
        if (backupCreated) {
            try {
                Files.move(backupFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING)
            } catch (restoreFailure: Exception) {
                failure.addSuppressed(restoreFailure)
                keepBackup = true
            }
        } else if (!hadOriginal) {
            runCatching { Files.deleteIfExists(targetPath) }
                .onFailure(failure::addSuppressed)
        }
        throw failure
    } finally {
        if (!keepBackup) {
            runCatching { Files.deleteIfExists(backupFile.toPath()) }
        }
    }
}
