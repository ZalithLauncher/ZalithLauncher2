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

package com.movtery.zalithlauncher.components.jre

import android.content.Context
import com.movtery.zalithlauncher.components.UnpackSingleTask
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.file.extractFromZip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile

class UnpackJnaTask(context: Context) : UnpackSingleTask(
    context = context,
    rootDir = PathManager.DIR_JNA,
    assetsDirName = "runtimes/jna",
    fileDirName = "jna"
) {
    override suspend fun moreProgress(file: File) {
        if (file.extension == "zip") {
            withContext(Dispatchers.IO) {
                ZipFile(file).use { zip ->
                    //解压 jna 压缩包
                    zip.extractFromZip("", this@UnpackJnaTask.rootDir)
                }
                file.delete()
            }
        }
    }
}