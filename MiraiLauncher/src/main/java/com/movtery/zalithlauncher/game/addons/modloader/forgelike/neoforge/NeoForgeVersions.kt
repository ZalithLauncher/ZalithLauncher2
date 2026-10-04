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

package com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge

import com.movtery.zalithlauncher.game.addons.mirror.MirrorSource
import com.movtery.zalithlauncher.game.addons.mirror.SourceType
import com.movtery.zalithlauncher.game.addons.mirror.orderedByGameSourcePreference
import com.movtery.zalithlauncher.game.addons.mirror.runMirrorable
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.models.BMCLAPIMaven
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.models.NeoForgedMaven
import com.movtery.zalithlauncher.utils.isChinaMainland
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.httpGetJson
import com.movtery.zalithlauncher.utils.network.withRetry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NeoForgeVersions {
    private const val TAG = "NeoForgeVersions"
    private var cacheResult: List<NeoForgeVersion>? = null

    /**
     * 获取 NeoForge 版本列表
     */
    suspend fun fetchNeoForgeList(
        force: Boolean = false,
        gameVersion: String
    ): List<NeoForgeVersion>? = withContext(Dispatchers.Default) {
        if (!force) cacheResult?.let {
            return@withContext it.outputVersionList(gameVersion)
        }

        if (isChinaMainland()) {
            runMirrorable(
                listOf(fetchListWithOfficial(), fetchListWithBMCLAPI()).orderedByGameSourcePreference()
            )
        } else {
            fetchOfficialVersions()
        }?.also {
            cacheResult = it
        }?.outputVersionList(gameVersion)
    }

    private fun List<NeoForgeVersion>.outputVersionList(
        gameVersion: String
    ): List<NeoForgeVersion> {
        return this.filter {
            it.inherit == gameVersion
        }.sortedWith { o1, o2 ->
            o2.forgeBuildVersion.compareTo(o1.forgeBuildVersion)
        }
    }

    /**
     * 在官方源获取版本列表
     */
    private fun fetchListWithOfficial(): MirrorSource<List<NeoForgeVersion>?> = MirrorSource(
        type = SourceType.OFFICIAL
    ) {
        fetchOfficialVersions()
    }

    private suspend fun fetchOfficialVersions() = withContext(Dispatchers.IO) {
        processVersionList(SourceType.OFFICIAL) {
            val neoforge = withRetry(TAG, maxRetries = 2) {
                httpGetJson<NeoForgedMaven>(url = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge")
            }
            val legacyForge = withRetry(TAG, maxRetries = 2) {
                httpGetJson<NeoForgedMaven>(url = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/forge")
            }.copy(isLegacy = true)

            neoforge + legacyForge
        }
    }

    /**
     * 在BMCL API源获取版本列表
     */
    private fun fetchListWithBMCLAPI(): MirrorSource<List<NeoForgeVersion>?> = MirrorSource(
        type = SourceType.BMCLAPI
    ) {
        processVersionList(SourceType.BMCLAPI) {
            val neoforge = withRetry(TAG, maxRetries = 2) {
                httpGetJson<BMCLAPIMaven>(url = "https://bmclapi2.bangbang93.com/neoforge/meta/api/maven/details/releases/net/neoforged/neoforge")
            }
            val legacyForge = withRetry(TAG, maxRetries = 2) {
                httpGetJson<BMCLAPIMaven>(url = "https://bmclapi2.bangbang93.com/neoforge/meta/api/maven/details/releases/net/neoforged/forge")
            }.copy(isLegacy = true)

            neoforge + legacyForge
        }
    }

    /**
     * 统一处理任务，处理异常、排序
     */
    private suspend fun processVersionList(
        sourceType: SourceType,
        block: suspend () -> List<NeoForgeVersion>
    ): List<NeoForgeVersion>? = withContext(Dispatchers.IO) {
        try {
            block()
                .sortedByDescending { it.forgeBuildVersion }
                .toList()
        } catch (_: CancellationException) {
            Logger.debug(TAG, "Client cancelled.")
            null
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch neoforge list! source: ${sourceType.displayName}", e)
        }
    }

    /**
     * 获取 NeoForge 对应版本的下载链接
     */
    fun getDownloadUrl(version: NeoForgeVersion) = "${version.baseUrl}-installer.jar"
}