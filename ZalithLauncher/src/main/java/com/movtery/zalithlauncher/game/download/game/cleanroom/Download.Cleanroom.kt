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

package com.movtery.zalithlauncher.game.download.game.cleanroom

import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.addons.modloader.cleanroom.CleanroomVersion
import com.movtery.zalithlauncher.utils.network.downloadFile
import com.movtery.zalithlauncher.utils.network.withSpeedReport
import java.io.File

const val CLEANROOM_DOWNLOAD_ID = "Download.Cleanroom"

fun targetTempCleanroomInstaller(tempGameDir: File): File = File(tempGameDir, ".temp/cleanroom_installer.jar")

fun getCleanroomDownloadTask(
    targetTempInstaller: File,
    cleanroomVersion: CleanroomVersion
): Task {
    return Task.runTask(
        id = CLEANROOM_DOWNLOAD_ID,
        task = { task ->
            withSpeedReport(
                onSpeedReport = { bytes ->
                    task.updateSpeed(bytes)
                },
                onClear = {
                    task.clearSpeed()
                }
            ) { report ->
                downloadFile(
                    url = cleanroomVersion.installerUrl,
                    outputFile = targetTempInstaller,
                    sizeCallback = report
                )
            }
        }
    )
}