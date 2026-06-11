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

package com.movtery.zalithlauncher.game.addons.modloader.forgelike.forge

import com.movtery.zalithlauncher.game.addons.mirror.MirrorSource
import com.movtery.zalithlauncher.game.addons.mirror.SourceType
import com.movtery.zalithlauncher.game.addons.mirror.runMirrorable
import com.movtery.zalithlauncher.game.addons.modloader.ResponseTooShortException
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.path.URL_USER_AGENT
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.MirrorSourceType
import com.movtery.zalithlauncher.utils.isChinaMainland
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.safeBodyAsJson
import com.movtery.zalithlauncher.utils.network.safeBodyAsText
import com.movtery.zalithlauncher.utils.network.withRetry
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * [Reference PCL2](https://github.com/Hex-Dragon/PCL2/blob/44aea3e/Plain%20Craft%20Launcher%202/Modules/Minecraft/ModDownload.vb#L626-L697)
 */
object ForgeVersions {
    private const val TAG = "ForgeVersions"
    private const val FORGE_FILE_URL = "https://files.minecraftforge.net/maven/net/minecraftforge/forge"

    /**
     * 获取 Forge 版本列表
     */
    suspend fun fetchForgeList(mcVersion: String): List<ForgeVersion>? = withContext(Dispatchers.Default) {
        if (isChinaMainland()) {
            runMirrorable(
                when (AllSettings.fetchModLoaderSource.getValue()) {
                    MirrorSourceType.OFFICIAL_FIRST -> listOf(
                        fetchListWithOfficial(mcVersion, 5),
                        fetchListWithBMCLAPI(mcVersion, 5 + 30)
                    )
                    MirrorSourceType.MIRROR_FIRST -> listOf(
                        fetchListWithBMCLAPI(mcVersion, 30),
                        fetchListWithOfficial(mcVersion, 30 + 60)
                    )
                }
            )
        } else {
            fetchListWithOfficial(mcVersion)
        }?.sortedWith { o1, o2 ->
            o2.forgeBuildVersion.compareTo(o1.forgeBuildVersion)
        }
    }

    /**
     * 从官方源获取版本列表
     */
    private fun fetchListWithOfficial(mcVersion: String, delayMillis: Long): MirrorSource<List<ForgeVersion>?> = MirrorSource(
        delayMillis = delayMillis,
        type = SourceType.OFFICIAL
    ) {
        fetchListWithOfficial(mcVersion)
    }

    private suspend fun fetchListWithOfficial(mcVersion: String) = withContext(Dispatchers.IO) {
        val url = "https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_${
            mcVersion.replace("-", "_")
        }.html"

        try {
            val html = withContext(Dispatchers.IO) {
                withRetry(TAG, maxRetries = 2) {
                    GLOBAL_CLIENT.get(url) {
                        headers {
                            append(HttpHeaders.UserAgent, "Mozilla/5.0/$URL_USER_AGENT")
                        }
                    }.safeBodyAsText()
                }
            }

            if (html.length < 100) throw ResponseTooShortException("Response too short")

            html.split("<td class=\"download-version")
                .drop(1)
                .mapNotNull { parseVersionFromHtml(it, mcVersion) }
        } catch (e: ClientRequestException) {
            val statusCode = e.response.status
            if (statusCode == HttpStatusCode.NotFound) {
                Logger.debug(TAG, "Not found.")
                null
            } else {
                throw e
            }
        } catch (_: CancellationException) {
            Logger.debug(TAG, "Client cancelled.")
            null
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch forge list! url: $url", e)
        }
    }

    /**
     * 从镜像源获取版本列表
     * [Reference PCL2](https://github.com/Meloong-Git/PCL/blob/28ef67e/Plain%20Craft%20Launcher%202/Modules/Minecraft/ModDownload.vb#L702-L751)
     */
    private fun fetchListWithBMCLAPI(mcVersion: String, delayMillis: Long): MirrorSource<List<ForgeVersion>?> = MirrorSource(
        delayMillis = delayMillis,
        type = SourceType.BMCLAPI
    ) {
        val url = "https://bmclapi2.bangbang93.com/forge/minecraft/${mcVersion.replace("-", "_")}" //兼容 Forge 1.7.10-pre4

        try {
            val tokens: List<ForgeVersionToken> = withContext(Dispatchers.IO) {
                withRetry(TAG, maxRetries = 2) {
                    GLOBAL_CLIENT.get(url).safeBodyAsJson()
                }
            }

            tokens.map { token ->
                val (hash, category) = determinePreferredFile(token.files)
                val formattedDate = ZonedDateTime.parse(token.modified, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                    .withZoneSameInstant(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))

                ForgeVersion(
                    versionName = token.version,
                    branch = token.branch,
                    inherit = mcVersion,
                    releaseTime = formattedDate,
                    hash = hash,
                    isRecommended = false,
                    category = category,
                    fileVersion = token.branch?.let { "${token.version}-$it" } ?: token.version
                )
            }
        } catch (e: ClientRequestException) {
            val statusCode = e.response.status
            if (statusCode == HttpStatusCode.NotFound) {
                Logger.debug(TAG, "Not found.")
                null
            } else {
                throw e
            }
        } catch (_: CancellationException) {
            Logger.debug(TAG, "Client cancelled.")
            null
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch forge list! url: $url", e)
        }
    }

    //选择首选文件
    private fun determinePreferredFile(files: List<ForgeVersionToken.ForgeFile>): Pair<String?, String> {
        var hash: String? = null
        var category = "unknown"
        var priority = -1

        fun updateSelection(file: ForgeVersionToken.ForgeFile, newCategory: String, newPriority: Int) {
            hash = file.hash
            category = newCategory
            priority = newPriority
        }

        files.forEach { file ->
            when {
                file.isInstallerJar() && priority <= 2 -> updateSelection(file, "installer", 2)
                file.isUniversalZip() && priority <= 1 -> updateSelection(file, "universal", 1)
                file.isClientZip() && priority <= 0 -> updateSelection(file, "client", 0)
            }
        }
        return hash to category
    }

    /**
     * 获取 Forge 对应版本的下载链接
     */
    fun getDownloadUrl(version: ForgeVersion) =
        "$FORGE_FILE_URL/${version.inherit}-${version.fileVersion}/forge-${version.inherit}-${version.fileVersion}-${version.category}.${version.fileExtension}"

    private fun parseVersionFromHtml(html: String, mcVersion: String): ForgeVersion? {
        return try {
            val name = Regex("""(?<=\D)\d+(\.\d+)+""").find(html)?.value ?: return null
            //                      <i class="promo-latest  fa" aria-hidden="true"></i>     <i class="promo-recommended  fa" aria-hidden="true"></i>
            val isRecommended = html.contains("promo-latest  fa" /* 最新推荐标签 */) || html.contains("promo-recommended  fa" /* 推荐标签 */)
            val branch = Regex("""(?<=-$name-)[^-"]+(?=-[a-z]+\.[a-z]{3})""").find(html)?.value
            val timeStr = Regex("""(?<="download-time" title=")[^"]+""").find(html)?.value ?: return null
            
            val dateTime = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
            
            val (category, hash) = when {
                html.contains("classifier-installer") -> parseInstaller(html)
                html.contains("classifier-universal") -> parseUniversal(html)
                html.contains("client.zip") -> parseClient(html)
                else -> return null
            } ?: return null

            ForgeVersion(
                versionName = name,
                branch = branch,
                inherit = mcVersion,
                releaseTime = dateTime,
                hash = hash,
                isRecommended = isRecommended,
                category = category,
                fileVersion = "$name${branch?.let { "-$it" } ?: ""}"
            )
        } catch (_: Exception) {
            null
        }
    }

    //类型为 installer.jar，支持范围 ~753 (~ 1.6.1 部分), 738~684 (1.5.2 全部)
    private fun parseInstaller(html: String): Pair<String, String>? {
        val section = html.substringAfter("installer.jar")
        val hash = Regex("""(?<=MD5:</strong> )[^<]+""").find(section)?.value?.trim()
        return hash?.let { "installer" to it }
    }

    //类型为 universal.zip，支持范围 751~449 (1.6.1 部分), 682~183 (1.5.1 ~ 1.3.2 部分)
    private fun parseUniversal(html: String): Pair<String, String>? {
        val section = html.substringAfter("universal.zip")
        val hash = Regex("""(?<=MD5:</strong> )[^<]+""").find(section)?.value?.trim()
        return hash?.let { "universal" to it }
    }

    //类型为 client.zip，支持范围 182~ (1.3.2 部分 ~)
    private fun parseClient(html: String): Pair<String, String>? {
        val section = html.substringAfter("client.zip")
        val hash = Regex("""(?<=MD5:</strong> )[^<]+""").find(section)?.value?.trim()
        return hash?.let { "client" to it }
    }
}