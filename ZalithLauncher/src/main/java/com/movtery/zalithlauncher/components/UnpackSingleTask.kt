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

package com.movtery.zalithlauncher.components

import android.content.Context
import android.content.res.AssetManager
import com.movtery.zalithlauncher.context.copyAssetFile
import com.movtery.zalithlauncher.utils.file.readString
import com.movtery.zalithlauncher.utils.logging.Logger
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

private const val TAG = "UnpackSingleTask"

abstract class UnpackSingleTask(
    val context: Context,
    val rootDir: File,
    val assetsDirName: String,
    val fileDirName: String,
) : AbstractUnpackTask() {
    private lateinit var am: AssetManager
    private lateinit var versionFile: File
    private lateinit var input: InputStream
    private var isCheckFailed: Boolean = false

    init {
        runCatching {
            am = context.assets
            versionFile = File("$rootDir/$fileDirName/version")
            input = am.open("$assetsDirName/version")
        }.onFailure { e ->
            Logger.warning(TAG, "Failed to init asset version. assetsPath=$assetsDirName/version", e)
            isCheckFailed = true
        }
    }

    fun isCheckFailed() = isCheckFailed

    override fun checkState(): InstallableItem.State {
        if (isCheckFailed) return InstallableItem.State.NOT_EXISTS

        return if (!versionFile.exists()) {
            requestEmptyParentDir(versionFile)
            Logger.info(TAG, "$fileDirName: Pack was installed manually, or does not exist...")
            InstallableItem.State.NOT_STARTED
        } else {
            runCatching {
                val fis = FileInputStream(versionFile)
                val release1 = input.readString()
                val release2 = fis.readString()
                if (release1 != release2) {
                    requestEmptyParentDir(versionFile)
                    InstallableItem.State.PENDING
                } else {
                    Logger.info(TAG, "$fileDirName: Pack is up-to-date with the launcher, continuing...")
                    InstallableItem.State.FINISHED
                }
            }.onFailure { e ->
                Logger.error("CheckComponent", "An exception occurred while detecting the assets resource.", e)
            }.getOrElse {
                //检查失败，要求重新进行安装
                InstallableItem.State.NOT_STARTED
            }
        }
    }

    override suspend fun run() {
        val dir = File(rootDir, fileDirName)
        FileUtils.deleteDirectory(dir)

        copyAssetDirectory(assetsDirName, dir)

        context.copyAssetFile(
            fileName = "$assetsDirName/version",
            output = File(dir, "version"),
            overwrite = true
        )
        val fileCount = dir.walkTopDown().count { it.isFile }
        Logger.info(TAG, "$fileDirName: unpacked $fileCount files")
    }

    /**
     * 递归复制 assets 目录下的所有文件。
     * 组件资产里可能存在子目录（如 lwjgl3/<ver>/natives/<abi>/），
     * 而 copyAssetFile 仅支持文件，这里对子目录做递归处理。
     */
    private suspend fun copyAssetDirectory(assetPath: String, outputDir: File) {
        outputDir.mkdirs()
        val children = am.list(assetPath) ?: emptyArray()
        Logger.debug(TAG, "copyAssetDirectory: $assetPath (${children.size} entries)")
        children.forEach { child ->
            val childAssetPath = "$assetPath/$child"
            val childOutput = File(outputDir, child)
            if (isAssetDirectory(childAssetPath)) {
                copyAssetDirectory(childAssetPath, childOutput)
            } else {
                context.copyAssetFile(childAssetPath, childOutput, overwrite = true)
                moreProgress(childOutput)
            }
        }
    }

    private fun isAssetDirectory(assetPath: String): Boolean {
        // 目录无法用 open() 打开，而 list() 能列出其子项；空目录会误判为文件，但组件内不存在空目录
        return try {
            am.open(assetPath).close()
            false
        } catch (_: IOException) {
            true
        }
    }

    /**
     * 执行更多操作
     */
    open suspend fun moreProgress(file: File) {}

    private fun requestEmptyParentDir(file: File) {
        file.parentFile!!.apply {
            if (exists() and isDirectory) {
                FileUtils.deleteDirectory(this)
            }
            mkdirs()
        }
    }
}