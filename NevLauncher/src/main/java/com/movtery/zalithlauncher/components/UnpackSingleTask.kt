/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.components

import android.content.Context
import com.movtery.zalithlauncher.context.copyAssetFile
import com.movtery.zalithlauncher.utils.file.ensureDirectory
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import kotlinx.coroutines.CancellationException
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

private const val TAG = "UnpackSingleTask"

abstract class UnpackSingleTask(
    val context: Context,
    val rootDir: File,
    val assetsDirName: String,
    val fileDirName: String,
) : AbstractUnpackTask() {
    open fun selectFiles(manifest: AssetManifest): List<ComponentFile> {
        return manifest.files.map { it.toComponentFile() }
    }

    private fun loadComponentFiles(): List<ComponentFile> {
        val manifest = context.assets.open("$assetsDirName/$ASSET_MANIFEST_FILE").use { input ->
            val json = input.bufferedReader().use { it.readText() }
            parseAssetManifest(json, assetsDirName)
        }
        return selectFiles(manifest).also { validateComponentFiles(it) }
    }

    override suspend fun checkState(): InstallableItem.State {
        val files = try {
            loadComponentFiles()
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to load asset manifest: $assetsDirName", e)
            updateMessage(e.getMessageOrToString())
            return InstallableItem.State.FAILED
        }

        val dir = File(rootDir, fileDirName)
        if (!dir.exists()) return InstallableItem.State.NOT_STARTED
        if (!dir.isDirectory) return InstallableItem.State.PENDING

        return try {
            val ignoredMetadata = setOf(ASSET_MANIFEST_FILE, LEGACY_VERSION_FILE)
            if (verifyComponentDirectory(dir, files, ignoredMetadata)) {
                Logger.info(TAG, "$fileDirName: Pack passed SHA-1 verification, continuing...")
                InstallableItem.State.FINISHED
            } else {
                Logger.info(TAG, "$fileDirName: Pack failed SHA-1 verification, reinstalling...")
                InstallableItem.State.PENDING
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.warning(TAG, "Failed to verify component: $fileDirName", e)
            InstallableItem.State.PENDING
        }
    }

    override suspend fun run() {
        updateMessage(null)
        val files = loadComponentFiles()
        val target = File(rootDir, fileDirName)
        val parent = target.parentFile ?: throw IOException("Target has no parent: $target")
        parent.ensureDirectory()

        val staging = File(parent, ".${target.name}.installing")
        val backup = File(parent, ".${target.name}.backup")
        recoverInterruptedCommit(target, staging, backup)
        installComponent(target, staging, backup, files)
    }

    private fun recoverInterruptedCommit(target: File, staging: File, backup: File) {
        if (!target.exists() && backup.exists()) {
            movePath(backup, target)
        }
        if (target.exists() && backup.exists()) {
            deletePath(backup)
        }
        if (staging.exists()) {
            deletePath(staging)
        }
    }

    private suspend fun installComponent(
        target: File,
        staging: File,
        backup: File,
        files: List<ComponentFile>
    ) {
        try {
            if (!staging.mkdir()) throw IOException("Failed to create staging directory: $staging")
            files.forEach { componentFile ->
                val output = File(staging, componentFile.outputPath)
                context.copyAssetFile(
                    fileName = "$assetsDirName/${componentFile.assetPath}",
                    output = output,
                    overwrite = false
                )
                moreProgress(output)
            }

            if (!verifyComponentDirectory(staging, files)) {
                throw IOException("Staged component failed SHA-1 verification: $fileDirName")
            }
            commitComponent(target, staging, backup)
            Logger.info(TAG, "$fileDirName: unpacked and verified ${files.size} files")
        } finally {
            if (staging.exists()) {
                runCatching {
                    deletePath(staging)
                }.onFailure { e ->
                    Logger.warning(TAG, "Failed to clean staging directory: $staging", e)
                }
            }
        }
    }

    private fun commitComponent(target: File, staging: File, backup: File) {
        var backupCreated = false
        var committed = false
        try {
            if (target.exists()) {
                movePath(target, backup)
                backupCreated = true
            }
            movePath(staging, target)
            committed = true
        } catch (e: Exception) {
            if (!target.exists() && backupCreated && backup.exists()) {
                runCatching {
                    movePath(backup, target)
                }.onFailure { rollback ->
                    e.addSuppressed(rollback)
                }
            }
            throw e
        } finally {
            if (committed && backup.exists()) {
                runCatching {
                    deletePath(backup)
                }.onFailure { e ->
                    Logger.warning(TAG, "Failed to clean component backup: $backup", e)
                }
            }
        }
    }

    private fun movePath(source: File, target: File) {
        try {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source.toPath(), target.toPath())
        } catch (_: UnsupportedOperationException) {
            Files.move(source.toPath(), target.toPath())
        }
    }

    private fun deletePath(file: File) {
        if (file.isDirectory) {
            FileUtils.deleteDirectory(file)
        } else if (file.exists() && !file.delete()) {
            throw IOException("Failed to delete: $file")
        }
    }

    open suspend fun moreProgress(file: File) {}
}
