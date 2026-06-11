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

package com.movtery.zalithlauncher.game.addons.modloader.fabriclike

import com.movtery.zalithlauncher.game.addons.mirror.MirrorSource
import com.movtery.zalithlauncher.game.addons.mirror.SourceType
import com.movtery.zalithlauncher.game.addons.mirror.runMirrorable
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.models.FabricLikeLoader
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.models.FabricLikeVersionsJson
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.MirrorSourceType
import com.movtery.zalithlauncher.utils.isChinaMainland
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.safeBodyAsJson
import com.movtery.zalithlauncher.utils.network.withRetry
import io.ktor.client.request.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "FabricLikeVersions"

abstract class FabricLikeVersions(
    val officialUrl: String,
    val mirrorUrl: String? = null
) {
    private var cacheVersions: FabricLikeVersionsJson? = null

    /**
     * 通用 Loader 列表获取
     * [Reference PCL2](https://github.com/Meloong-Git/PCL/blob/28ef67e/Plain%20Craft%20Launcher%202/Modules/Minecraft/ModDownload.vb#L1035-L1054)
     */
    protected suspend fun fetchLoaderList(
        force: Boolean,
        tag: String,
        mcVersion: String
    ): List<FabricLikeLoader>? = withContext(Dispatchers.Default) {
        mirrorUrl?.takeIf { isChinaMainland() }?.let {
            runMirrorable(
                when (AllSettings.fetchModLoaderSource.getValue()) {
                    MirrorSourceType.OFFICIAL_FIRST -> listOf(
                        fetchListWithOfficial(force, tag, mcVersion, 5),
                        fetchListWithBMCLAPI(force, tag, mcVersion, 5 + 30)
                    )
                    MirrorSourceType.MIRROR_FIRST -> listOf(
                        fetchListWithBMCLAPI(force, tag, mcVersion, 30),
                        fetchListWithOfficial(force, tag, mcVersion, 30 + 60)
                    )
                }
            )
        } ?: run {
            //不支持镜像源，只使用官方源
            fetchListWithSource(force, tag, mcVersion, officialUrl)
        }
    }

    /**
     * 在官方源获取版本列表
     */
    private fun fetchListWithOfficial(
        force: Boolean,
        tag: String,
        mcVersion: String,
        delayMillis: Long
    ): MirrorSource<List<FabricLikeLoader>?> = MirrorSource(
        delayMillis = delayMillis,
        type = SourceType.OFFICIAL
    ) {
        fetchListWithSource(force, tag, mcVersion, officialUrl)
    }

    /**
     * 在BMCL API源获取版本列表
     */
    private fun fetchListWithBMCLAPI(
        force: Boolean,
        tag: String,
        mcVersion: String,
        delayMillis: Long
    ): MirrorSource<List<FabricLikeLoader>?> = MirrorSource(
        delayMillis = delayMillis,
        type = SourceType.BMCLAPI
    ) {
        fetchListWithSource(force, tag, mcVersion, mirrorUrl ?: officialUrl)
    }

    /**
     * 指定特定源获取版本列表
     */
    private suspend fun fetchListWithSource(
        force: Boolean,
        tag: String,
        mcVersion: String,
        sourceUrl: String
    ): List<FabricLikeLoader>? = withContext(Dispatchers.IO) {
        val url = "$sourceUrl/versions"
        try {
            val versions: FabricLikeVersionsJson = run {
                if (!force && cacheVersions != null) return@run cacheVersions!!
                withContext(Dispatchers.IO) {
                    withRetry(tag, maxRetries = 2) { GLOBAL_CLIENT.get(url).safeBodyAsJson() }
                }
            }.also {
                cacheVersions = it
            }

            if (!versions.game.any { it.version == mcVersion }) {
                Logger.warning(TAG, "The version $mcVersion does not have a corresponding loader.")
                return@withContext null
            }

            versions.loader
        } catch (_: CancellationException) {
            Logger.debug(TAG, "Client cancelled.")
            null
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch fabriclike loader list! url: $url", e)
        }
    }
}