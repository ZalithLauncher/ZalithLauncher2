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

package com.movtery.zalithlauncher.game.download.game.optifine

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.addons.mirror.MirrorSource
import com.movtery.zalithlauncher.game.addons.mirror.SourceType
import com.movtery.zalithlauncher.game.addons.mirror.orderedByGameSourcePreference
import com.movtery.zalithlauncher.game.addons.mirror.runMirrorable
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.addons.modloader.optifine.OptiFineVersion
import com.movtery.zalithlauncher.game.addons.modloader.optifine.OptiFineVersions
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.isChinaMainland
import com.movtery.zalithlauncher.utils.network.downloadFile
import com.movtery.zalithlauncher.utils.network.withSpeedReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

const val OPTIFINE_DOWNLOAD_ID = "Download.OptiFine"

fun targetTempOptiFineInstaller(tempGameDir: File, tempMinecraftDir: File, fileName: String, isNewVersion: Boolean): File {
    return if (isNewVersion) File(tempGameDir, ".temp/OptiFine.jar")
    else {
        val nameFileCleaned = fileName
            .replace("OptiFine_", "")
            .replace(".jar", "")
            .replace("preview_", "")
        val nameFileFormatted = fileName
            .replace("OptiFine_", "OptiFine-")
            .replace("preview_", "")
        File(tempMinecraftDir, "libraries/optifine/OptiFine/$nameFileCleaned/$nameFileFormatted")
    }
}

fun getOptiFineDownloadTask(
    targetTempInstaller: File,
    optifine: OptiFineVersion
): Task {
    return Task.runTask(
        id = OPTIFINE_DOWNLOAD_ID,
        dispatcher = Dispatchers.IO,
        task = { task ->
            task.updateProgress(-1f)
            task.updateMessage(androidText(
                R.string.download_game_install_optifine_fetch_download_url, optifine.realVersion
            ))
            val optifineUrl = getOFUrlMirrorable(optifine)

            task.updateProgress(-1f)
            task.updateMessage(androidText(
                R.string.download_game_install_base_download_file, ModLoader.OPTIFINE.displayName, optifine.realVersion
            ))
            withSpeedReport(
                onSpeedReport = { bytes ->
                    task.updateSpeed(bytes)
                },
                onClear = {
                    task.clearSpeed()
                }
            ) {
                downloadFile(
                    url = optifineUrl,
                    outputFile = targetTempInstaller
                )
            }
        }
    )
}

fun getOptiFineModsDownloadTask(
    optifine: OptiFineVersion,
    tempModsDir: File
): Task {
    return Task.runTask(
        id = OPTIFINE_DOWNLOAD_ID,
        dispatcher = Dispatchers.IO,
        task = { task ->
            task.updateProgress(-1f)
            task.updateMessage(androidText(
                R.string.download_game_install_optifine_fetch_download_url, optifine.realVersion
            ))
            val optifineUrl = getOFUrlMirrorable(optifine)

            //开始下载为 Mod
            task.updateProgress(-1f)
            task.updateMessage(androidText(
                R.string.download_game_install_base_download_file, ModLoader.OPTIFINE.displayName, optifine.realVersion
            ))
            withSpeedReport(
                onSpeedReport = { bytes ->
                    task.updateSpeed(bytes)
                },
                onClear = {
                    task.clearSpeed()
                }
            ) { report ->
                downloadFile(
                    url = optifineUrl,
                    outputFile = File(tempModsDir, optifine.fileName),
                    sizeCallback = report
                )
            }
        }
    )
}

private suspend fun getOFUrlMirrorable(
    optifine: OptiFineVersion
): String {
    return if (isChinaMainland()) {
        runMirrorable(
            listOf(
                fetchOfficialOptiFineUrlSource(optifine),
                fetchBMCLOptiFineUrlSource(optifine)
            ).orderedByGameSourcePreference()
        )!!
    } else {
        fetchOptiFineDownloadUrl(optifine)
    }
}

/**
 * 从官方源获取 OptiFine 主文件下载链接
 */
private fun fetchOfficialOptiFineUrlSource(optifine: OptiFineVersion): MirrorSource<String> =
    MirrorSource(SourceType.OFFICIAL) { fetchOptiFineDownloadUrl(optifine) }

private fun fetchBMCLOptiFineUrlSource(optifine: OptiFineVersion): MirrorSource<String> =
    MirrorSource(SourceType.BMCLAPI) { getDownloadUrlWithBMCLAPI(optifine) }

private suspend fun fetchOptiFineDownloadUrl(
    optifine: OptiFineVersion
) = withContext(Dispatchers.IO) {
    OptiFineVersions.fetchOptiFineDownloadUrl(optifine.fileName) ?: throw CantFetchingOptiFineUrlException()
}

private fun getDownloadUrlWithBMCLAPI(optifine: OptiFineVersion): String {
    val inherit = if (optifine.inherit == "1.8" || optifine.inherit == "1.9") "${optifine.inherit}.0" else optifine.inherit
    val displayNameStripped = optifine.displayName.removePrefix("${optifine.inherit} ")

    val suffix = if (optifine.isPreview) {
        "HD_U_${displayNameStripped.replace(" ", "/")}"
    } else {
        "HD_U/$displayNameStripped"
    }

    return "https://bmclapi2.bangbang93.com/optifine/$inherit/$suffix"
}