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
import com.movtery.zalithlauncher.game.download.assets.mapExceptionToMessage
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.CurseForgeSearcher
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.MCIM_CURSEFORGE_API
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.MCIM_MODRINTH_API
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.ModrinthSearcher
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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
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
 * 镜像源只能在中国地区使用
 */
fun mirroredCurseForgeSource(
    enabledMirror: Boolean = isChinaMainland()
): List<CurseForgeSearcher> {
    val source = AllSettings.assetSearchSource.getValue()
    val mirrorSource = mirrorCurseForgeSearcher.takeIf { enabledMirror }
    return when (source) {
        MirrorSourceType.OFFICIAL_FIRST ->
            listOfNotNull(curseForgeSearcher, mirrorSource)
        MirrorSourceType.MIRROR_FIRST ->
            listOfNotNull(mirrorSource, curseForgeSearcher)
    }
}

/**
 * 镜像源只能在中国地区使用
 */
fun mirroredModrinthSource(
    enabledMirror: Boolean = isChinaMainland()
): List<ModrinthSearcher> {
    val source = AllSettings.assetSearchSource.getValue()
    val mirrorSource = mirrorModrinthSearcher.takeIf { enabledMirror }
    return when (source) {
        MirrorSourceType.OFFICIAL_FIRST ->
            listOfNotNull(modrinthSearcher, mirrorSource)
        MirrorSourceType.MIRROR_FIRST ->
            listOfNotNull(mirrorSource, modrinthSearcher)
    }
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
        val queries = if (!englishKeywords.isNullOrEmpty()) {
            englishKeywords.toList()
        } else {
            listOf(searchFilter.searchName)
        }

        var lastResult: PlatformSearchResult? = null
        var lastException: Exception? = null
        for (query in queries) {
            try {
                val r = when (searchPlatform) {
                    Platform.CURSEFORGE -> mirroredPlatformSearcher(
                        searchers = mirroredCurseForgeSource(),
                        printLog = false
                    ) { searcher ->
                        searcher.searchAssets(
                            query = query,
                            searchFilter = searchFilter,
                            platformClasses = platformClasses
                        )
                    }
                    Platform.MODRINTH -> mirroredPlatformSearcher(
                        searchers = mirroredModrinthSource(),
                        printLog = false
                    ) { searcher ->
                        searcher.searchAssets(
                            query = query,
                            searchFilter = searchFilter,
                            platformClasses = platformClasses
                        )
                    }
                }
                lastResult = r
                if (r.getAssetsPage(platformClasses).data.isNotEmpty()) break
            } catch (e: Exception) {
                //当前关键词搜索失败，记录异常并继续尝试下一个
                lastException = e
            }
        }

        val result = lastResult ?: throw lastException ?: IOException("Failed to search for all queries")

        onSuccess(
            if (containsChinese) result.processChineseSearchResults(searchFilter.searchName, platformClasses)
            else result
        )
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

suspend fun getVersionByLocalFile(file: File, sha1: String): PlatformVersion? = coroutineScope {
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