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

package com.movtery.zalithlauncher.game.addons.modloader.optifine

import com.movtery.zalithlauncher.game.addons.mirror.MirrorSource
import com.movtery.zalithlauncher.game.addons.mirror.SourceType
import com.movtery.zalithlauncher.game.addons.mirror.orderedByGameSourcePreference
import com.movtery.zalithlauncher.game.addons.mirror.runMirrorable
import com.movtery.zalithlauncher.game.addons.modloader.ResponseTooShortException
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.utils.isChinaMainland
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.safeBodyAsJson
import com.movtery.zalithlauncher.utils.network.safeBodyAsText
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

private const val TAG = "OptiFineVersions"

object OptiFineVersions {
    private const val OPTIFINE_URL = "https://optifine.net"
    private const val OPTIFINE_DOWNLOAD_URL = "$OPTIFINE_URL/downloads"
    private const val OPTIFINE_ADLOADX_URL = "$OPTIFINE_URL/adloadx"

    private var cacheResult: List<OptiFineVersion>? = null

    /**
     * 获取 OptiFine 版本列表
     */
    suspend fun fetchOptiFineList(
        force: Boolean = false,
        gameVersion: String
    ): List<OptiFineVersion>? = withContext(Dispatchers.IO) {
        if (isChinaMainland()) {
            runMirrorable(
                listOf(fetchOfficialSource(force), fetchBMCLAPISource(force)).orderedByGameSourcePreference()
            )
        } else {
            fetchOfficialVersions(force)
        }?.filter {
            it.inherit == gameVersion
        }
    }

    private fun fetchOfficialSource(force: Boolean): MirrorSource<List<OptiFineVersion>?> =
        MirrorSource(SourceType.OFFICIAL) { fetchOfficialVersions(force) }

    private fun fetchBMCLAPISource(force: Boolean): MirrorSource<List<OptiFineVersion>?> =
        MirrorSource(SourceType.BMCLAPI) { fetchListWithBMCLAPI(force) }

    private suspend fun fetchOfficialVersions(force: Boolean) = withContext(Dispatchers.Default) {
        if (!force) cacheResult?.let { return@withContext it }

        try {
            val response: HttpResponse = withContext(Dispatchers.IO) {
                GLOBAL_CLIENT.get(OPTIFINE_DOWNLOAD_URL)
            }

            val html = response.safeBodyAsText()
            if (html.length < 100) {
                throw ResponseTooShortException("Response too short")
            }
            val namePattern = Regex("<td class=['\"]colFile['\"]>([^<]+)</td>")
            val datePattern = Regex("<td class=['\"]colDate['\"]>([^<]+)</td>")
            val forgePattern = Regex("<td class=['\"]colForge['\"]>([^<]+)</td>")
            val jarPattern = Regex("adfoc\\.us[^>]+f=([^&]+\\.jar)")

            //提取所有匹配项
            val names = namePattern.findAll(html).map { it.groupValues[1].trim() }.toList()
            val dates = datePattern.findAll(html).map { it.groupValues[1].trim() }.toList()
            val forges = forgePattern.findAll(html).map { it.groupValues[1].trim() }.toList()
            val jars  = jarPattern.findAll(html).map { it.groupValues[1].trim() }.toList()

            if (names.size != dates.size || names.size != forges.size || names.size != jars.size) {
                Logger.warning(TAG, "The number of parsed fields is inconsistent.")
                return@withContext emptyList()
            }

            val versions = mutableListOf<OptiFineVersion>()
            for (i in names.indices) {
                ensureActive()

                val rawName = jars[i].removeSuffix(".jar").removePrefix("preview_")
                val rawNameSpaced = rawName.replace("_", " ")

                val isPreview = jars[i].startsWith("preview_")

                val displayName = rawNameSpaced
                    .replace("OptiFine ", "")
                    .replace("HD U ", "")
                    .replace(".0 ", " ")

                val inherit = displayName.split(" ")[0]
                val fileName = (if (isPreview) "preview_" else "") + "$rawName.jar"

                val versionName = if (rawName.contains("$inherit.0_")) {
                    //OptiFine_1.9.0_HD_U_E7 -> 1.9-OptiFine_HD_U_E7
                    "${inherit}-${rawName.replace("$inherit.0_", "")}"
                } else {
                    //OptiFine_1.10.2_HD_U_C1 -> 1.10.2-OptiFine_HD_U_C1
                    "${inherit}-${rawName.replace("${inherit}_", "")}"
                }

                val rawDate = dates[i]
                val parts = rawDate.split('.')
                val formattedDate = if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else rawDate

                //提取Forge版本
                val forgeVersion = forges[i]
                    .takeIf { !it.contains("N/A") }
                    ?.removePrefix("Forge ")
                    ?.replace("#", "")
                    ?.trim()

                versions.add(
                    OptiFineVersion(
                        displayName = displayName,
                        fileName = fileName,
                        version = versionName,
                        inherit = inherit,
                        releaseDate = formattedDate,
                        forgeVersion = forgeVersion,
                        isPreview = isPreview
                    )
                )
            }
            cacheResult = versions
            versions
        } catch(_: CancellationException) {
            Logger.debug(TAG, "Client cancelled.")
            null
        } catch (e: Exception) {
            Logger.warning(TAG, "Failed to fetch OptiFine list!", e)
            throw e
        }
    }

    private suspend fun fetchListWithBMCLAPI(force: Boolean): List<OptiFineVersion>? = withContext(Dispatchers.Default) {
        if (!force) cacheResult?.let { return@withContext it }

            try {
                val tokens: List<OptiFineVersionToken> = withContext(Dispatchers.IO) {
                    GLOBAL_CLIENT.get("https://bmclapi2.bangbang93.com/optifine/versionList").safeBodyAsJson()
                }

                tokens.map { token ->
                    val nameDisplay = (token.mcVersion + token.type.replace("HD_U", "").replace("_", " ") + " " + token.patch)
                        .replace(".0 ", " ")
                    val requiredForgeVersion = token.forge
                        ?.replace("Forge ", "")
                        ?.replace("#", "")
                        ?.takeUnless { it.contains("N/A", ignoreCase = true) }

                    val versionName = token.mcVersion + "-OptiFine_" +
                            (token.type + " " + token.patch)
                                .replace(".0 ", " ")
                                .replace(" ", "_")
                                .replace(token.mcVersion + "_", "")

                    OptiFineVersion(
                        displayName = nameDisplay,
                        fileName = token.fileName,
                        version = versionName,
                        inherit = token.mcVersion,
                        releaseDate = "",
                        forgeVersion = requiredForgeVersion,
                        isPreview = token.patch.contains("pre", ignoreCase = true)
                    )
                }.also {
                    cacheResult = it
                }
            } catch(_: CancellationException) {
                Logger.debug(TAG, "Client cancelled.")
                null
            } catch (e: Exception) {
                Logger.warning(TAG, "Failed to fetch OptiFine list!", e)
                throw e
            }
    }

    /**
     * 获取 OF 对应文件下载链接
     */
    suspend fun fetchOptiFineDownloadUrl(fileName: String): String? = withContext(Dispatchers.Default) {
        try {
            val response: HttpResponse = withContext(Dispatchers.IO) {
                GLOBAL_CLIENT.get("${OPTIFINE_ADLOADX_URL}?f=$fileName") {
                    contentType(ContentType.Text.Html)
                }
            }

            val html = response.safeBodyAsText()

            val match = Regex("""downloadx\?f=[^"'<>]+""").find(html)
            val downloadPath = match?.value

            if (downloadPath != null) {
                val finalUrl = "$OPTIFINE_URL/$downloadPath"
                return@withContext finalUrl
            } else {
                return@withContext null
            }

        } catch (e: Exception) {
            Logger.warning(TAG, "Failed to fetch $fileName download url!", e)
            return@withContext null
        }
    }
}