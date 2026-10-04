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

package com.movtery.zalithlauncher.game.download.assets.platform

import android.util.Log
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.game.addons.mirror.orderSourceCandidates
import com.movtery.zalithlauncher.game.download.assets.mapExceptionToMessage
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.CurseForgeSearcher
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.MCIM_CURSEFORGE_API
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeFile
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.MCIM_MODRINTH_API
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.ModrinthSearcher
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthVersion
import com.movtery.zalithlauncher.game.download.assets.utils.localizedModSearchKeywords
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.MirrorSourceType
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadAssetsState
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.SearchAssetsState
import com.movtery.zalithlauncher.utils.isChinaMainland
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.isInterruptedIOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

private const val TAG = "PlatformSearch"

private val modrinthSearcher = ModrinthSearcher()
private val mirrorModrinthSearcher = ModrinthSearcher(
    api = MCIM_MODRINTH_API,
    source = "MCIM Modrinth"
)

private val curseForgeSearcher = CurseForgeSearcher()
private val mirrorCurseForgeSearcher = CurseForgeSearcher(
    api = MCIM_CURSEFORGE_API,
    source = "MCIM CurseForge"
)

/**
 * 对资源平台搜索启用镜像源机制进行操作
 */
suspend fun <E: AbstractPlatformSearcher, T> mirroredPlatformSearcher(
    searchers: List<E>,
    printLog: Boolean = true,
    block: suspend (E) -> T
): T {
    require(searchers.isNotEmpty()) { "Searcher list must not be empty." }

    val errors = mutableListOf<Exception>()
    var lastException: Exception? = null

    for (searcher in searchers) {
        try {
            if (printLog) {
                Logger.debug(TAG, "Starting to attempt to perform the operation on source: {${searcher.source}}")
            }
            return block(searcher)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Log.w("PlatformSearcher", "Failed to perform the operation on source: {${searcher.source}}", e)
            lastException = e

            if (e.isInterruptedIOException()) {
                throw e
            } else if (e is FileNotFoundException) {
                errors.add(e)
                break
            } else {
                errors.add(e)
            }
        }
    }

    if (printLog) {
        Logger.warning(TAG, 
            msg = "An error occurred during this search.",
            t = IOException("All sources have failed to attempt", lastException).apply {
                errors.forEachIndexed { i, e ->
                    addSuppressed(Exception("Mirror error #${i + 1}: ${e.message}"))
                }
            }
        )
    }
    throw lastException ?: IllegalStateException("Should not have executed to this stage.")
}

/**
 * Automatically fall back to the alternate CurseForge source when the preferred source fails.
 * An explicit Official preference continues to use only CurseForge's API.
 */
/**
 * True when the official CurseForge API can actually be used: either the build
 * ships a key, or the user pasted their own in Settings.
 */
fun hasCurseForgeApiKey(): Boolean =
    AllSettings.curseForgeApiKey.getValue().isNotBlank() ||
            BuildKeys.CURSEFORGE_API.isNotBlank()

fun mirroredCurseForgeSource(
    enabledMirror: Boolean = isChinaMainland()
): List<CurseForgeSearcher> {
    val preference = AllSettings.assetPlatformSource.getValue()
    if (!hasCurseForgeApiKey()) {
        //Without a key the official API answers 403 to every request: never send it.
        if (preference == MirrorSourceType.OFFICIAL) {
            throw IOException("CurseForge official API requires an API key. Paste one in Settings, or switch the asset source to Auto.")
        }
        //Official unusable: mirror only.
        return listOf(mirrorCurseForgeSearcher)
    }
    val mirrorSource = mirrorCurseForgeSearcher.takeIf {
        preference != MirrorSourceType.OFFICIAL
    }
    return orderSourceCandidates(
        official = curseForgeSearcher,
        mirror = mirrorSource,
        preference = preference,
        mainland = enabledMirror
    )
}

/**
 * 自动模式按地区调整 Modrinth 与 MCIM 的尝试顺序；显式源偏好始终生效。
 */
fun mirroredModrinthSource(
    enabledMirror: Boolean = isChinaMainland()
): List<ModrinthSearcher> {
    val preference = AllSettings.assetPlatformSource.getValue()
    val mirrorSource = mirrorModrinthSearcher.takeIf {
        preference != MirrorSourceType.OFFICIAL
    }
    return orderSourceCandidates(
        official = modrinthSearcher,
        mirror = mirrorSource,
        preference = preference,
        mainland = enabledMirror
    )
}

/** Overall wall-clock budget for one interactive search across all queries. */
private const val SEARCH_OVERALL_TIMEOUT_MS = 30_000L

/** Interactive search responses are tiny; keep the most recent pages in memory. */
private const val SEARCH_CACHE_MAX_ENTRIES = 16

/** Search cache TTL: mod listings barely change within a minute and a half. */
private const val SEARCH_CACHE_TTL_MS = 90_000L

private data class CachedSearchResult(
    val result: PlatformSearchResult,
    val timestampMs: Long
)

private val searchCacheLock = Any()
private val searchCache = object : LinkedHashMap<String, CachedSearchResult>(16, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CachedSearchResult>): Boolean {
        return size > SEARCH_CACHE_MAX_ENTRIES
    }
}

private fun searchCacheKey(
    platform: Platform,
    classes: PlatformClasses,
    query: String,
    filter: PlatformSearchFilter
): String = buildString {
    append(platform.name).append('|')
    append(classes.name).append('|')
    append(AllSettings.assetPlatformSource.getValue()).append('|')
    append(query).append('|')
    append(filter.copy(searchName = "").toString())
}

private fun getCachedSearchResult(key: String): PlatformSearchResult? = synchronized(searchCacheLock) {
    val cached = searchCache[key] ?: return@synchronized null
    if (System.currentTimeMillis() - cached.timestampMs > SEARCH_CACHE_TTL_MS) {
        searchCache.remove(key)
        return@synchronized null
    }
    cached.result
}

private fun putCachedSearchResult(key: String, result: PlatformSearchResult) {
    synchronized(searchCacheLock) {
        searchCache[key] = CachedSearchResult(result, System.currentTimeMillis())
    }
}

/**
 * Runs [block] against every candidate source concurrently and returns the first
 * successful result, cancelling the slower sources. When every source fails, the
 * last error is thrown. A single candidate runs directly without extra coroutines.
 *
 * Unlike [mirroredPlatformSearcher] (sequential fallback), a slow-but-alive source
 * never stalls the search: the fastest healthy source wins every time.
 */
suspend fun <E : AbstractPlatformSearcher, T> fastestMirroredResult(
    searchers: List<E>,
    block: suspend (E) -> T
): T = coroutineScope {
    require(searchers.isNotEmpty()) { "Searcher list must not be empty." }
    //Single source runs directly, but off Main so response parsing never drops frames.
    if (searchers.size == 1) return@coroutineScope withContext(Dispatchers.IO) { block(searchers.first()) }

    val pending = searchers.associateWith { searcher ->
        async(Dispatchers.IO) { block(searcher) }
    }.toMutableMap()
    var lastError: Throwable? = null
    // Wait for the first source to settle; a failure only prunes that source while
    // the survivors keep racing. External cancellation rethrows immediately.
    while (pending.isNotEmpty()) {
        try {
            select<Unit> {
                pending.values.forEach { deferred -> deferred.onAwait {} }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            lastError = e
        }
        //Prune failures, naming the dead source; the survivors keep racing.
        val failures = pending.entries.filter { it.value.getCompletionExceptionOrNull() != null }
        failures.forEach { (searcher, deferred) ->
            Logger.warning(TAG, "Search source {${searcher.source}} failed (${deferred.getCompletionExceptionOrNull()?.message}), ${pending.size - failures.size} source(s) still racing.")
            pending.remove(searcher)
        }
        pending.entries.firstOrNull { it.value.isCompleted }?.let { (searcher, winner) ->
            pending.values.forEach { deferred -> if (deferred !== winner) deferred.cancel() }
            Logger.debug(TAG, "Search source {${searcher.source}} won the race.")
            return@coroutineScope winner.await()
        }
    }
    throw lastError ?: IllegalStateException("All sources failed without reporting an error.")
}

suspend fun searchAssets(
    searchPlatform: Platform,
    searchFilter: PlatformSearchFilter,
    platformClasses: PlatformClasses,
    onSuccess: suspend (PlatformSearchResult) -> Unit,
    onError: (SearchAssetsState.Error) -> Unit
) {
    runCatching {
        val (containsChinese, englishKeywords) = searchFilter.searchName.localizedModSearchKeywords(platformClasses)
        //参考源代码：[HMCL Github](https://github.com/HMCL-dev/HMCL/blob/d295e60/HMCL/src/main/java/org/jackhuang/hmcl/game/LocalizedRemoteModRepository.java#L56-L68)
        //逐个英文短语尝试搜索，取第一个有非空结果的
        val queries = englishKeywords?.takeIf { it.isNotEmpty() }?.toList()
            ?: listOf(searchFilter.searchName)
        //参考源代码：[HMCL Github](https://github.com/HMCL-dev/HMCL/blob/8767cc0e/HMCL/src/main/java/org/jackhuang/hmcl/game/LocalizedRemoteAddonRepository.java)
        //翻译出的英文短语搜索固定使用相关性排序，避免所选的排序方式将目标资源挤出结果页
        val searchFilterForQuery = englishKeywords?.takeIf { it.isNotEmpty() }
            ?.let { searchFilter.copy(sortField = PlatformSortField.RELEVANCE) }
            ?: searchFilter

        var lastResult: PlatformSearchResult? = null
        var lastException: Exception? = null
        try {
            withTimeout(SEARCH_OVERALL_TIMEOUT_MS) {
                for (query in queries) {
                    val cacheKey = searchCacheKey(
                        platform = searchPlatform,
                        classes = platformClasses,
                        query = query,
                        filter = searchFilterForQuery
                    )
                    try {
                        // Serve repeat searches (tab switches, back navigation, retries) instantly.
                        val cached = getCachedSearchResult(cacheKey)
                        val r = cached ?: when (searchPlatform) {
                            Platform.CURSEFORGE -> fastestMirroredResult(
                                searchers = mirroredCurseForgeSource()
                            ) { searcher ->
                                searcher.searchAssets(
                                    query = query,
                                    searchFilter = searchFilterForQuery,
                                    platformClasses = platformClasses
                                )
                            }
                            Platform.MODRINTH -> fastestMirroredResult(
                                searchers = mirroredModrinthSource()
                            ) { searcher ->
                                searcher.searchAssets(
                                    query = query,
                                    searchFilter = searchFilterForQuery,
                                    platformClasses = platformClasses
                                )
                            }
                        }
                        if (cached == null) putCachedSearchResult(cacheKey, r)
                        lastResult = r
                        if (r.hasResults()) break
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (e: Exception) {
                        //当前关键词搜索失败，记录异常并继续尝试下一个
                        lastException = e
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            // Convert the timeout into a visible error instead of hanging on "Searching".
            lastException = IOException("Search timed out after ${SEARCH_OVERALL_TIMEOUT_MS / 1000}s", e)
        }

        val result = lastResult ?: throw lastException ?: IOException("Failed to search for all queries")

        val displayResult = if (containsChinese) {
            withContext(Dispatchers.Default) {
                result.processChineseSearchResults(searchFilter.searchName, platformClasses)
            }
        } else {
            result
        }
        onSuccess(displayResult)
    }.onFailure { e ->
        if (e !is CancellationException) {
            Logger.error(TAG, "An exception occurred while searching for assets.", e)
            val state = SearchAssetsState.Error(mapExceptionToMessage(e))
            onError(state)
        } else {
            Logger.debug(TAG, "The search task has been cancelled.")
        }
    }
}

suspend fun getVersions(
    projectID: String,
    platform: Platform,
    pageCallback: (chunk: Int, page: Int) -> Unit = { _, _ -> },
) = when (platform) {
    Platform.CURSEFORGE -> mirroredPlatformSearcher(
        searchers = mirroredCurseForgeSource()
    ) { searcher ->
        searcher.getVersions(
            projectID = projectID,
            pageCallback = pageCallback
        )
    }
    Platform.MODRINTH -> mirroredPlatformSearcher(
        searchers = mirroredModrinthSource()
    ) { searcher ->
        searcher.getVersions(
            projectID = projectID,
            pageCallback = pageCallback
        )
    }
}

suspend fun <E> getVersions(
    projectID: String,
    platform: Platform,
    pageCallback: (chunk: Int, page: Int) -> Unit = { _, _ -> },
    onSuccess: suspend (List<PlatformVersion>) -> Unit,
    onError: (DownloadAssetsState<List<E>>) -> Unit
) {
    runCatching {
        val result = getVersions(projectID, platform, pageCallback)
        onSuccess(result)
    }.onFailure { e ->
        if (e !is CancellationException) {
            Logger.error(TAG, "An exception occurred while retrieving the project version.", e)
            val state = DownloadAssetsState.Error<List<E>>(mapExceptionToMessage(e))
            onError(state)
        } else {
            Logger.debug(TAG, "The version retrieval task has been cancelled.")
        }
    }
}

suspend fun <E> getProject(
    projectID: String,
    platform: Platform,
    onSuccess: (PlatformProject) -> Unit,
    onError: (DownloadAssetsState<E>, Throwable) -> Unit
) {
    runCatching {
        when (platform) {
            Platform.CURSEFORGE -> mirroredPlatformSearcher(
                searchers = mirroredCurseForgeSource()
            ) { searcher ->
                searcher.getProject(projectID)
            }
            Platform.MODRINTH -> mirroredPlatformSearcher(
                searchers = mirroredModrinthSource()
            ) { searcher ->
                searcher.getProject(projectID)
            }
        }
    }.fold(
        onSuccess = onSuccess,
        onFailure = { e ->
            if (e !is CancellationException) {
                Logger.error(TAG, "An exception occurred while retrieving project information.", e)
                val state = DownloadAssetsState.Error<E>(mapExceptionToMessage(e))
                onError(state, e)
            } else {
                Logger.debug(TAG, "The project retrieval task has been cancelled.")
            }
        }
    )
}

suspend fun getProjectByVersion(
    projectId: String,
    platform: Platform,
    printLog: Boolean = true
): PlatformProject = withContext(Dispatchers.IO) {
    when (platform) {
        Platform.MODRINTH -> mirroredPlatformSearcher(
            searchers = mirroredModrinthSource(),
            printLog = printLog
        ) { searcher ->
            searcher.getProject(projectId)
        }
        Platform.CURSEFORGE -> mirroredPlatformSearcher(
            searchers = mirroredCurseForgeSource(),
            printLog = printLog
        ) { searcher ->
            searcher.getProject(projectId)
        }
    }
}

/**
 * 获取指定平台上的单个版本
 * @param versionId 版本在平台上的Id
 */
suspend fun getVersionById(
    versionId: String,
    platform: Platform,
    printLog: Boolean = true
): PlatformVersion = withContext(Dispatchers.IO) {
    when (platform) {
        Platform.MODRINTH -> mirroredPlatformSearcher(
            searchers = mirroredModrinthSource(),
            printLog = printLog
        ) { searcher ->
            searcher.getVersion(versionId)
        }
        Platform.CURSEFORGE -> error("CurseForge dependencies do not carry a version id.")
    }
}

suspend fun getVersionByLocalFile(file: File, sha1: String): PlatformVersion? = withContext(Dispatchers.IO) {
    coroutineScope {
        val modrinthDeferred = async(Dispatchers.IO) {
            runCatching {
                mirroredPlatformSearcher(
                    searchers = mirroredModrinthSource(),
                    printLog = false
                ) { searcher ->
                    searcher.getVersionByLocalFile(file, sha1)
                }
            }.getOrNull()
        }

        val curseForgeDeferred = async(Dispatchers.IO) {
            runCatching {
                mirroredPlatformSearcher(
                    searchers = mirroredCurseForgeSource(),
                    printLog = false
                ) { searcher ->
                    searcher.getVersionByLocalFile(file, sha1)
                }
            }.getOrNull()
        }

        val result = select {
            modrinthDeferred.onAwait { result ->
                if (result != null) {
                    curseForgeDeferred.cancel()
                    result
                } else {
                    null
                }
            }
            curseForgeDeferred.onAwait { result ->
                if (result != null) {
                    modrinthDeferred.cancel()
                    result
                } else {
                    null
                }
            }
        }

        result ?: run {
            if (!modrinthDeferred.isCompleted) modrinthDeferred.await()
            else if (!curseForgeDeferred.isCompleted) curseForgeDeferred.await()
            else null
        }
    }
}

/** 单次批量指纹匹配的指纹数量上限，防止单次请求数据量过大 */
const val FINGERPRINT_BATCH_SIZE = 100

/**
 * 通过本地文件的 SHA-1 值批量获取 Modrinth 平台对应的版本
 * @param sha1List SHA-1 值列表，长度不应超过 [FINGERPRINT_BATCH_SIZE]
 * @return 键为 SHA-1 值，值为匹配到的版本，未命中的指纹不在结果中
 */
suspend fun getModrinthVersBySha1(
    sha1List: List<String>
): Map<String, ModrinthVersion> = mirroredPlatformSearcher(
    searchers = mirroredModrinthSource(),
    printLog = false
) { searcher ->
    searcher.getVersionFiles(sha1List = sha1List)
}

/**
 * 通过本地文件的 CurseForge 指纹批量获取 CurseForge 平台对应的文件
 * @param fingerprints 指纹列表，长度不应超过 [FINGERPRINT_BATCH_SIZE]
 * @return 键为文件指纹，值为匹配到的文件，未命中的指纹不在结果中
 */
suspend fun getCFFilesByFingerprints(
    fingerprints: List<Long>
): Map<Long, CurseForgeFile> = mirroredPlatformSearcher(
    searchers = mirroredCurseForgeSource(),
    printLog = false
) { searcher ->
    searcher.getFilesByFingerprints(fingerprints = fingerprints)
}