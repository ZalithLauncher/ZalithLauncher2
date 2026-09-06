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

import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.logging.Logger
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "AssetFavoriteManager"

/**
 * 资源收藏 MMKV，多进程模式
 */
private fun favoritesMMKV(): MMKV = MMKV.mmkvWithID("FavoriteAssets", MMKV.MULTI_PROCESS_MODE)

/**
 * 资源收藏管理器，维护 [AssetFavorite] 列表的 StateFlow，并对外暴露收藏/取消收藏/查询能力。
 *
 * 收藏分两类，完全独立不级联：
 * - 项目收藏（[AssetFavoriteType.PROJECT]）：同一 (platform, projectId) 最多一条
 * - 版本收藏（[AssetFavoriteType.VERSION]）：同一 (platform, projectId, versionId) 唯一，可多条
 *
 * 数据通过 MMKV 持久化，每条收藏项以 [AssetFavorite.storageKey] 为键、GSON JSON 为值。
 * 所有"判定 + 写入"均在同一 Mutex 临界区内完成；写操作先乐观更新内存 StateFlow，
 * 再在 IO 线程落盘，不做全量回读。
 */
object AssetFavoriteManager {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()

    private val _favorites = MutableStateFlow<List<AssetFavorite>>(emptyList())
    /** 收藏列表，按收藏时间降序 */
    val favorites = _favorites.asStateFlow()

    private val _favoriteKeys = MutableStateFlow<Set<String>>(emptySet())
    /** 已收藏项的 [AssetFavorite.storageKey] 集合，供行级收藏状态 O(1) 查询 */
    val favoriteKeys = _favoriteKeys.asStateFlow()

    /**
     * 初始化收藏管理器（须在 MMKV.initialize 之后调用），读取全部收藏键构建内存态。
     * 注：MMKV 虽为多进程模式，但内存态不会随其他进程的写入自动刷新；
     * 目前收藏仅在主进程 UI 操作，不提供 reload，如有跨进程写入场景需再补充
     */
    fun initialize() {
        scope.launch {
            mutex.withLock {
                runCatching { loadAll() }
                    .onSuccess { list ->
                        _favorites.update { list }
                        _favoriteKeys.update { list.mapTo(HashSet()) { it.storageKey } }
                        Logger.info(TAG, "Loaded ${list.size} asset favorites")
                    }
                    .onFailure { e -> Logger.error(TAG, "Failed to load asset favorites", e) }
            }
        }
    }

    /**
     * 是否已收藏项目本身（同步查内存态）
     * @param platform 所属平台
     * @param projectId 项目在平台上的 ID
     */
    fun isProjectFavorite(platform: Platform, projectId: String): Boolean =
        assetFavoriteStorageKey(platform, projectId, AssetFavoriteType.PROJECT) in _favoriteKeys.value

    /**
     * 是否已收藏指定版本（同步查内存态）
     * @param platform 所属平台
     * @param projectId 项目在平台上的 ID
     * @param versionId 版本在平台上的 ID（CurseForge 为 fileId，Modrinth 为 versionId）
     */
    fun isVersionFavorite(platform: Platform, projectId: String, versionId: String): Boolean =
        assetFavoriteStorageKey(platform, projectId, AssetFavoriteType.VERSION, versionId) in _favoriteKeys.value

    /**
     * 取某项目下所有版本收藏记录（同步查内存态，按收藏时间降序）
     * @param platform 所属平台
     * @param projectId 项目在平台上的 ID
     */
    fun findVersions(platform: Platform, projectId: String): List<AssetFavorite> =
        _favorites.value.filter {
            it.platform == platform && it.projectId == projectId && it.type == AssetFavoriteType.VERSION
        }

    /**
     * 新增（或同 key 覆盖）一条项目收藏
     * @param favorite 收藏项，[AssetFavorite.type] 必须为 [AssetFavoriteType.PROJECT]
     */
    fun addProject(favorite: AssetFavorite) {
        require(favorite.type == AssetFavoriteType.PROJECT) { "addProject 仅接受 PROJECT 类型的收藏项" }
        put(favorite)
    }

    /**
     * 新增（或同 key 覆盖）一条版本收藏
     * @param favorite 收藏项，[AssetFavorite.type] 必须为 [AssetFavoriteType.VERSION] 且 versionId 非空
     */
    fun addVersion(favorite: AssetFavorite) {
        require(favorite.type == AssetFavoriteType.VERSION && !favorite.versionId.isNullOrBlank()) {
            "addVersion 仅接受 VERSION 类型且 versionId 非空的收藏项"
        }
        put(favorite)
    }

    /**
     * 删除项目收藏（不连带版本收藏）
     * @param platform 所属平台
     * @param projectId 项目在平台上的 ID
     */
    fun removeProject(platform: Platform, projectId: String) {
        remove(assetFavoriteStorageKey(platform, projectId, AssetFavoriteType.PROJECT))
    }

    /**
     * 删除指定版本收藏
     * @param platform 所属平台
     * @param projectId 项目在平台上的 ID
     * @param versionId 版本在平台上的 ID
     */
    fun removeVersion(platform: Platform, projectId: String, versionId: String) {
        remove(assetFavoriteStorageKey(platform, projectId, AssetFavoriteType.VERSION, versionId))
    }

    /**
     * 删除某项目下的全部收藏（项目收藏 + 所有版本收藏）。
     * 直接扫 MMKV 键按前缀匹配，不依赖内存快照。
     * @param platform 所属平台
     * @param projectId 项目在平台上的 ID
     */
    fun removeAllByProject(platform: Platform, projectId: String) {
        scope.launch {
            mutex.withLock {
                runCatching {
                    val mmkv = favoritesMMKV()
                    val prefix = assetFavoriteProjectKeyPrefix(platform, projectId)
                    val keys = mmkv.allKeys()?.filter { it.startsWith(prefix) }.orEmpty()
                    if (keys.isEmpty()) return@withLock
                    _favorites.update { list -> list.filterNot { it.storageKey in keys } }
                    _favoriteKeys.update { it - keys.toSet() }
                    keys.forEach { key -> mmkv.removeValueForKey(key) }
                    Logger.info(TAG, "Removed ${keys.size} favorites of project $projectId")
                }.onFailure { e -> Logger.error(TAG, "Failed to remove all favorites by project", e) }
            }
        }
    }

    /**
     * 按存储键删除收藏项
     * @param storageKey 收藏项的 [AssetFavorite.storageKey]
     */
    fun remove(storageKey: String) {
        scope.launch {
            mutex.withLock {
                _favorites.update { list -> list.filterNot { it.storageKey == storageKey } }
                _favoriteKeys.update { it - storageKey }
                runCatching { favoritesMMKV().removeValueForKey(storageKey) }
                    .onFailure { e -> Logger.error(TAG, "Failed to remove favorite", e) }
            }
        }
    }

    /**
     * 写入收藏项：先乐观更新内存 StateFlow，再落盘 MMKV，不做全量回读
     */
    private fun put(favorite: AssetFavorite) {
        scope.launch {
            mutex.withLock {
                val key = favorite.storageKey
                _favorites.update { list ->
                    list.filterNot { it.storageKey == key }
                        .plus(favorite)
                        .sortedByDescending { it.savedAt }
                }
                _favoriteKeys.update { it + key }
                runCatching {
                    favoritesMMKV().encode(key, GSON.toJson(favorite))
                }.onFailure { e -> Logger.error(TAG, "Failed to save favorite", e) }
            }
        }
    }

    /**
     * 全量读取：单条 JSON 解析失败仅跳过；schemaVersion 不符（旧格式/未来格式）的条目跳过
     */
    private fun loadAll(): List<AssetFavorite> {
        val mmkv = favoritesMMKV()
        val keys = mmkv.allKeys() ?: return emptyList()
        return keys.mapNotNull { key ->
            val favorite = runCatching {
                mmkv.decodeString(key)?.let { json -> GSON.fromJson(json, AssetFavorite::class.java) }
            }.getOrNull()
            favorite?.takeIf { it.schemaVersion == AssetFavorite.CURRENT_SCHEMA_VERSION }
        }.sortedByDescending { it.savedAt }
    }
}
