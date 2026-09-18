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

package com.movtery.zalithlauncher.game.path

import android.content.Context
import com.movtery.zalithlauncher.database.AppDatabase
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings.currentGamePathId
import com.movtery.zalithlauncher.utils.canHandlePermission
import com.movtery.zalithlauncher.utils.hasStoragePermission
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID
import kotlin.io.path.exists

private const val TAG = "GamePathManager"

/**
 * 游戏目录管理，为支持将游戏文件保存至不同的路径
 */
object GamePathManager {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()
    private val defaultGamePath = File(PathManager.DIR_FILES_EXTERNAL, ".minecraft").absolutePath
    /**
     * 默认游戏目录的ID
     */
    const val DEFAULT_ID = "default"

    private val _gamePathData = MutableStateFlow<List<GamePath>>(listOf())
    val gamePathData = _gamePathData.asStateFlow()

    private val _currentPath = MutableStateFlow(defaultGamePath)
    /** 当前选择的路径 */
    val currentPath = _currentPath.asStateFlow()

    /**
     * 当前游戏路径
     */
    fun getCurrentPath(): String = File(_currentPath.value).absolutePath

    /**
     * 当前用户路径
     */
    fun getUserPath(): String = File(_currentPath.value).parentFile!!.absolutePath

    private lateinit var database: AppDatabase
    private lateinit var gamePathDao: GamePathDao

    fun initialize(context: Context) {
        database = AppDatabase.getInstance(context)
        gamePathDao = database.gamePathDao()
    }

    fun reloadPath() {
        scope.launch {
            mutex.withLock {
                _gamePathData.update { emptyList() }

                val newValue = mutableListOf<GamePath>()
                //添加默认游戏目录
                newValue.add(0, GamePath(DEFAULT_ID, "", defaultGamePath))

                run parseConfig@{
                    //从数据库中加载游戏目录
                    val paths = gamePathDao.getAllPaths()
                    newValue.addAll(paths.sortedBy { it.title })
                }

                _gamePathData.update { newValue }

                if (canHandlePermission &&  !hasStoragePermission) {
                    _currentPath.update { defaultGamePath }
                    saveDefaultPath(false)
                } else {
                    refreshCurrentPath(false)
                }

                VersionsManager.refresh("GamePathManager.reloadPath")
                Logger.info(TAG, "Loaded ${_gamePathData.value.size} game paths")
            }
        }
    }

    /**
     * 执行在路径列表刷新完成后可执行的任务
     */
    suspend fun waitForRefresh() {
        mutex.withLock {}
    }

    private fun String.createNoMediaFile() {
        runCatching {
            val parent = Paths.get(this)
            Files.createDirectories(parent)
            val noMediaFile = parent.resolve(".nomedia")
            if (!noMediaFile.exists()) Files.createFile(noMediaFile)
        }.onFailure { e ->
            Logger.error(TAG, "Failed to create .nomedia file in $this", e)
        }
    }

    /**
     * 查找是否存在指定id的项
     */
    fun containsId(id: String): Boolean = _gamePathData.value.any { it.id == id }

    /**
     * 查找是否存在指定path的项
     */
    fun containsPath(path: String): Boolean = _gamePathData.value.any { it.path == path }

    /**
     * 修改并保存指定目录的标题
     * @throws IllegalArgumentException 未找到匹配项
     */
    fun modifyTitle(path: GamePath, modifiedTitle: String) {
        if (!containsId(path.id)) throw IllegalArgumentException("Item with ID ${path.id} not found, unable to rename.")
        path.title = modifiedTitle
        savePath(path)
    }

    /**
     * 添加新的路径并保存
     * @throws IllegalArgumentException 当前添加的路径与现有项冲突
     */
    fun addNewPath(title: String, path: String) {
        if (containsPath(path)) throw IllegalArgumentException("The path conflicts with an existing item!")
        savePath(
            GamePath(id = generateUUID(), title = title, path = path)
        )
    }

    /**
     * 删除路径并保存
     */
    fun removePath(path: GamePath) {
        if (!containsId(path.id)) return
        deletePath(path)
    }

    /**
     * 保存为默认的游戏目录
     */
    fun saveDefaultPath(reloadVersions: Boolean = true) {
        saveCurrentPathUncheck(DEFAULT_ID, reloadVersions)
    }

    /**
     * 保存当前选择的路径
     * @throws IllegalStateException 未授予存储/管理所有文件权限
     * @throws IllegalArgumentException 未找到匹配项
     */
    fun saveCurrentPath(id: String, reloadVersions: Boolean = true) {
        if (canHandlePermission && !hasStoragePermission) throw IllegalStateException("Storage permissions are not granted")
        if (!containsId(id)) throw IllegalArgumentException("No match found!")
        saveCurrentPathUncheck(id, reloadVersions)
    }

    private fun saveCurrentPathUncheck(id: String, reloadVersions: Boolean) {
        if (currentGamePathId.getValue() == id) return
        currentGamePathId.save(id)
        refreshCurrentPath(reloadVersions)
    }

    private fun refreshCurrentPath(reloadVersions: Boolean) {
        val id = currentGamePathId.getValue()
        _gamePathData.value.find { it.id == id }?.let { item ->
            if (_currentPath.value == item.path) return //避免重复刷新
            val path = item.path
            _currentPath.update { path }
            path.createNoMediaFile()
            if (reloadVersions) {
                VersionsManager.refresh("GamePathManager.refreshCurrentPath")
            }
        } ?: saveCurrentPath(DEFAULT_ID, reloadVersions)
    }

    private fun generateUUID(): String {
        val uuid = UUID.randomUUID().toString()
        return if (containsId(uuid)) generateUUID()
        else uuid
    }

    private fun savePath(path: GamePath) {
        scope.launch {
            runCatching {
                gamePathDao.savePath(path)
                Logger.info(TAG, "Saved game path: ${path.path}")
            }.onFailure { e ->
                Logger.error(TAG, "Failed to save game path config!", e)
            }
            reloadPath()
        }
    }

    private fun deletePath(path: GamePath) {
        scope.launch {
            gamePathDao.deletePath(path)
            reloadPath()
        }
    }
}