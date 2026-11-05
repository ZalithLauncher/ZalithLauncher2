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

package com.movtery.zalithlauncher.components.jre

import android.content.Context
import com.movtery.zalithlauncher.components.AssetManifest
import com.movtery.zalithlauncher.components.ComponentFile
import com.movtery.zalithlauncher.components.UnpackSingleTask
import com.movtery.zalithlauncher.components.toComponentFile
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.device.Architecture

class UnpackJnaTask(context: Context) : UnpackSingleTask(
    context = context,
    rootDir = PathManager.DIR_JNA,
    assetsDirName = "runtimes/jna",
    fileDirName = "jna"
) {
    override fun selectFiles(manifest: AssetManifest): List<ComponentFile> {
        val abi = Architecture.archAsStringAndroid(Architecture.getDeviceArchitecture())
        val versions = manifest.files.mapTo(HashSet()) { it.path.substringBefore('/') }
        val files = manifest.files.mapNotNull { file ->
            val parts = file.path.split('/')
            require(parts.size == 4 && parts[1] == "natives" && parts[3] == "libjnidispatch.so") {
                "Unsupported JNA asset path: ${file.path}"
            }
            if (parts[2] != abi) return@mapNotNull null
            file.toComponentFile("${parts[0]}/libjnidispatch.so")
        }
        require(files.mapTo(HashSet()) { it.outputPath.substringBefore('/') } == versions) {
            "JNA assets are incomplete for ABI: $abi"
        }
        return files
    }
}