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

package com.movtery.zalithlauncher.game.download.assets.favorites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchData
import com.movtery.zalithlauncher.game.download.assets.platform.getProjectByVersion
import com.movtery.zalithlauncher.utils.logging.Logger
import io.ktor.client.plugins.ClientRequestException
import io.ktor.server.plugins.NotFoundException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/**
 * 收藏键，平台与项目Id唯一确定一个收藏项
 */
data class FavoriteKey(
    val platform: Platform,
    val projectId: String
)

/**
 * 收藏项目条目，本地缓存数据与已加载的远端项目数据
 */
data class FavoriteEntry(
    val platform: Platform,
    val project: FavoriteProject,
    val remote: PlatformProject? = null,
    val invalid: Boolean = false
)

/**
 * 收藏项目仓库
 */
object FavoriteProjectsRepository {
    private const val TAG = "FavoriteProjectsRepository"
    private const val REFRESH_CONCURRENCY = 8

    /** 所有收藏项目，键为 [FavoriteKey] */
    val projects = mutableStateMapOf<FavoriteKey, FavoriteEntry>()

    /** 收藏数据是否已完成装载 */
    var initialized by mutableStateOf(false)
        private set

    //仓库内部协程作用域，承载非挂起入口的异步任务
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    //串行化数据装载与读写，避免并发修改导致的状态错乱
    private val mutex = Mutex()

    /**
     * 仅读取内存数据池；数据未装载时触发一次异步装载
     */
    fun isFavorite(platform: Platform, projectId: String): Boolean {
        if (!initialized) scope.launch { ensureLoaded() }
        return projects.containsKey(FavoriteKey(platform, projectId))
    }

    /**
     * 收藏一个搜索结果项目
     */
    fun favorite(data: PlatformSearchData, classes: PlatformClasses) {
        scope.launch { saveFavorite(data.platform(), data.toFavoriteProject(classes)) }
    }

    /**
     * 收藏一个远端项目
     */
    fun favorite(project: PlatformProject, defaultClasses: PlatformClasses) {
        scope.launch { saveFavorite(project.platform(), project.toFavoriteProject(defaultClasses)) }
    }

    fun unfavorite(platform: Platform, projectId: String) {
        scope.launch { removeFavorite(platform, projectId) }
    }

    /**
     * 切换搜索结果项目的收藏状态
     */
    fun toggle(data: PlatformSearchData, classes: PlatformClasses) {
        scope.launch {
            val platform = data.platform()
            val projectId = data.platformId()
            if (checkFavorite(platform, projectId)) {
                removeFavorite(platform, projectId)
            } else {
                saveFavorite(platform, data.toFavoriteProject(classes))
            }
        }
    }

    /**
     * 切换远端项目的收藏状态
     */
    fun toggle(project: PlatformProject, defaultClasses: PlatformClasses) {
        scope.launch {
            val platform = project.platform()
            val projectId = project.platformId()
            if (checkFavorite(platform, projectId)) {
                removeFavorite(platform, projectId)
            } else {
                saveFavorite(platform, project.toFavoriteProject(defaultClasses))
            }
        }
    }

    /**
     * 确保收藏数据已装载
     */
    suspend fun ensureLoaded() {
        if (initialized) return
        mutex.withLock {
            if (!initialized) reloadLocked()
        }
    }

    /**
     * 从 MMKV 重新加载收藏数据
     */
    suspend fun reload() = mutex.withLock {
        reloadLocked()
    }

    /**
     * 并发刷新所有收藏项目的远端数据，逐条更新内存与本地缓存
     */
    suspend fun refreshRemote() = coroutineScope {
        val pending = projects.values.toList()
        if (pending.isEmpty()) return@coroutineScope

        val semaphore = Semaphore(REFRESH_CONCURRENCY)
        pending.map { entry ->
            async {
                semaphore.withPermit { refreshEntry(entry) }
            }
        }.awaitAll()
    }

    /**
     * 装载数据，须持有互斥锁调用
     */
    private suspend fun reloadLocked() {
        val latest = withContext(Dispatchers.IO) { readAll() }
        if (!initialized) {
            initialized = true
            projects.clear()
            projects.putAll(latest)
        } else {
            latest.forEach { (key, entry) ->
                if (!projects.containsKey(key)) projects[key] = entry
            }
            (projects.keys - latest.keys).forEach(projects::remove)
        }
    }

    private suspend fun checkFavorite(platform: Platform, projectId: String): Boolean {
        ensureLoaded()
        return projects.containsKey(FavoriteKey(platform, projectId))
    }

    private suspend fun saveFavorite(platform: Platform, project: FavoriteProject) {
        mutex.withLock {
            if (!initialized) reloadLocked()
            withContext(Dispatchers.IO) {
                favoritesMMKV(platform).encode(project.projectId, project)
            }
            projects[FavoriteKey(platform, project.projectId)] = FavoriteEntry(platform, project)
        }
    }

    private suspend fun removeFavorite(platform: Platform, projectId: String) {
        mutex.withLock {
            if (!initialized) reloadLocked()
            withContext(Dispatchers.IO) {
                favoritesMMKV(platform).remove(projectId)
            }
            projects.remove(FavoriteKey(platform, projectId))
        }
    }

    private suspend fun refreshEntry(entry: FavoriteEntry) {
        val key = FavoriteKey(entry.platform, entry.project.projectId)
        try {
            val remote = getProjectByVersion(
                projectId = entry.project.projectId,
                platform = entry.platform,
                printLog = false
            )
            //条目可能在刷新过程中被移除，仅更新仍然存在的条目
            mutex.withLock {
                projects[key]?.let { current ->
                    if (!remote.platformAvailable()) {
                        //项目被平台标记为不可见（如已删除），标记条目失效
                        markInvalid(key, current)
                    } else {
                        val merged = mergeCache(current.project, remote)
                        projects[key] = current.copy(project = merged, remote = remote, invalid = false)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            if (e.isProjectNotFound()) {
                //远端项目已不可访问，标记条目失效
                mutex.withLock {
                    projects[key]?.let { current -> markInvalid(key, current) }
                }
            } else {
                Logger.warning(TAG, "Failed to refresh favorite project: ${key.platform}/${key.projectId}", e)
            }
        }
    }

    private fun markInvalid(key: FavoriteKey, current: FavoriteEntry) {
        if (current.invalid) return
        projects[key] = current.copy(invalid = true)
    }

    /**
     * 平台接口是否返回了项目未找到
     */
    private fun Throwable.isProjectNotFound(): Boolean =
        this is NotFoundException || (this is ClientRequestException && response.status.value == 404)

    /**
     * 以远端数据校正本地缓存，数据有变化时回写 MMKV，收藏时间保持不变
     */
    private suspend fun mergeCache(cached: FavoriteProject, remote: PlatformProject): FavoriteProject {
        val classes = remote.platformClasses(cached.classes)
        val iconUrl = remote.platformIconUrl()
        val title = remote.platformTitle()
        val description = remote.platformSummary() ?: ""
        val authors = remote.platformAuthors()

        val unchanged = classes == cached.classes &&
                iconUrl == cached.iconUrl &&
                title == cached.title &&
                description == cached.description &&
                authors == cached.authors
        if (unchanged) return cached

        val merged = FavoriteProject(
            projectId = cached.projectId,
            iconUrl = iconUrl,
            title = title,
            description = description,
            authors = authors,
            classes = classes,
            followTime = cached.followTime
        )
        withContext(Dispatchers.IO) {
            favoritesMMKV(remote.platform()).encode(merged.projectId, merged)
        }
        return merged
    }

    private fun readAll(): Map<FavoriteKey, FavoriteEntry> {
        val result = mutableMapOf<FavoriteKey, FavoriteEntry>()
        Platform.entries.forEach { platform ->
            val mmkv = favoritesMMKV(platform)
            mmkv.allKeys()?.forEach { key ->
                runCatching {
                    mmkv.decodeParcelable(key, FavoriteProject::class.java)
                }.getOrNull()?.let { project ->
                    result[FavoriteKey(platform, project.projectId)] = FavoriteEntry(platform, project)
                }
            }
        }
        return result
    }
}

/**
 * 从搜索结果数据生成收藏缓存
 */
fun PlatformSearchData.toFavoriteProject(classes: PlatformClasses): FavoriteProject = FavoriteProject(
    projectId = platformId(),
    iconUrl = platformIconUrl(),
    title = platformTitle(),
    description = platformDescription(),
    authors = platformAuthors(),
    classes = classes,
    followTime = System.currentTimeMillis()
)

/**
 * 从远端项目数据生成收藏缓存
 */
fun PlatformProject.toFavoriteProject(defaultClasses: PlatformClasses): FavoriteProject = FavoriteProject(
    projectId = platformId(),
    iconUrl = platformIconUrl(),
    title = platformTitle(),
    description = platformSummary() ?: "",
    authors = platformAuthors(),
    classes = platformClasses(defaultClasses),
    followTime = System.currentTimeMillis()
)
