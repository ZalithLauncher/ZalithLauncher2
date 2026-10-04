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

package com.movtery.zalithlauncher.game.addons.modloader.modlike

import com.movtery.zalithlauncher.game.download.assets.platform.mirroredModrinthSource
import com.movtery.zalithlauncher.game.download.assets.platform.mirroredPlatformSearcher
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthVersion
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.getPrimary
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.initAllGeneric
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException

private const val TAG = "ModVersions"

/**
 * 模组版本管理类
 * @param modrinthID Modrinth 平台模组对应的 ID
 */
abstract class ModVersions(
    private val modrinthID: String
) {
    private var cacheVersions: List<ModrinthVersion>? = null

    /**
     * 获取特定版本的模组列表
     */
    suspend fun fetchVersionList(
        mcVersion: String,
        force: Boolean = false
    ): List<ModVersion>? {
        try {
            val versions = run {
                if (!force && cacheVersions != null) return@run cacheVersions!!
                mirroredPlatformSearcher(
                    searchers = mirroredModrinthSource()
                ) { searcher ->
                    searcher.getVersions(
                        projectID = modrinthID
                    ).initAllGeneric(
                        currentProjectId = modrinthID
                    )
                }.also {
                    cacheVersions = it
                }
            }

            return versions.mapNotNull { version ->
                //仅保留版本号匹配的模组版本
                if (!version.gameVersions.contains(mcVersion)) return@mapNotNull null
                //仅保留主文件
                val file = version.files.getPrimary() ?: run {
                    Logger.warning(TAG, "No file list available, skipping -> ${version.name}")
                    return@mapNotNull null
                }
                ModVersion(
                    inherit = mcVersion,
                    displayName = version.versionNumber,
                    version = version,
                    file = file
                )
            }
        } catch (_: CancellationException) {
            Logger.debug(TAG, "Client cancelled.")
            return null
        } catch (e: Exception) {
            Logger.debug(TAG, "Failed to fetch mod list! {mod id = $modrinthID}", e)
            throw e
        }
    }
}