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

package com.movtery.zalithlauncher.game.version.installed

import com.movtery.zalithlauncher.game.launch.LogName
import com.movtery.zalithlauncher.game.path.getVersionsHome
import com.movtery.zalithlauncher.game.version.installed.utils.parseJsonToVersionInfo
import com.movtery.zalithlauncher.info.InfoDistributor
import com.movtery.zalithlauncher.utils.logging.Logger.lDebug
import com.movtery.zalithlauncher.utils.logging.Logger.lError
import com.movtery.zalithlauncher.utils.logging.Logger.lInfo
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.commons.io.FileUtils
import java.io.File

object VersionsManager {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()
    private val listeners: MutableList<suspend (List<Version>) -> Unit> = mutableListOf()

    /**
     * 注册版本列表刷新监听器
     */
    fun registerListener(listener: suspend (List<Version>) -> Unit) {
        listeners.add(listener)
    }

    /**
     * 移除版本列表刷新监听器
     */
    fun unregisterListener(listener: suspend (List<Version>) -> Unit) {
        listeners.remove(listener)
    }

    /**
     * 当前所有的游戏版本
     */
    var versions: List<Version> = emptyList()
        private set

    /**
     * 当前的游戏信息
     */
    var gameInfo: CurrentGameInfo? = null
        private set

    private val _currentVersion = MutableStateFlow<Version?>(null)
    val currentVersion = _currentVersion.asStateFlow()

    private var currentJob: Job? = null

    private val _isRefreshing = MutableStateFlow(false)
    /** 是否正在刷新版本 */
    val isRefreshing = _isRefreshing.asStateFlow()

    /**
     * 检查版本是否已经存在
     */
    fun isVersionExists(versionName: String, checkJson: Boolean = false): Boolean {
        val folder = File(getVersionsHome(), versionName)
        //保证版本文件夹存在的同时，也应保证其版本json文件存在
        return if (checkJson) File(folder, "${folder.name}.json").exists()
        else folder.exists()
    }

    /**
     * 刷新所有版本
     * @param tag 是由谁发起的刷新，输出到日志方便定位
     * @param trySetVersion 在刷新完成后尝试设置当前版本
     */
    fun refresh(tag: String, trySetVersion: String? = null) {
        currentJob?.cancel()
        currentJob = scope.launch {
            mutex.withLock {
                _isRefreshing.update { true }
                lDebug("Initiated by $tag: starting to refresh the version list.")

                if (trySetVersion != null) {
                    saveCurrentVersion(trySetVersion, refresh = false)
                    lDebug("Has attempted to save the current version: $trySetVersion")
                }

                versions = emptyList()

                val newVersions = mutableListOf<Version>()
                File(getVersionsHome()).listFiles()?.forEach { versionFile ->
                    runCatching {
                        processVersionFile(versionFile)
                    }.getOrNull()?.let {
                        newVersions.add(it)
                    }
                }

                versions = newVersions.sortedWith(VersionComparator)

                gameInfo = refreshCurrentInfo()
                lDebug("Version list refreshed, refreshing the current version now.")
                refreshCurrentVersion()

                listeners.forEach { it.invoke(versions) }

                _isRefreshing.update { false }
            }
        }
    }

    /**
     * 执行在版本列表刷新完成后可执行的任务
     */
    suspend fun waitForRefresh() {
        mutex.withLock {}
    }

    private fun processVersionFile(versionFile: File): Version? {
        if (versionFile.exists() && versionFile.isDirectory) {
            var isVersion = false

            //通过判断是否存在版本的.json文件，来确定其是否为一个版本
            val jsonFile = File(versionFile, "${versionFile.name}.json")
            val versionInfo = if (jsonFile.exists() && jsonFile.isFile) {
                parseJsonToVersionInfo(jsonFile)?.also {
                    //如果解析失败了，可能不是标准版本
                    //保险起见，只有解析成功了的版本，才会被判定为有效版本
                    isVersion = true
                }
            } else {
                null
            }

            val versionConfig = VersionConfig.parseConfig(versionFile)

            val version = Version(
                versionFile.name,
                versionConfig,
                versionInfo,
                isVersion,
                versionInfo.getVersionType()
            )

            lInfo(
                "Identified and added version: ${version.getVersionName()}, " +
                        "Path: (${version.getVersionPath()}), " +
                        "Info: ${version.getVersionInfo()?.getInfoString()}"
            )

            return version
        }
        return null
    }

    private fun refreshCurrentVersion() {
        val version = run {
            if (versions.isEmpty()) return@run null

            fun getVersionByFirst(): Version? {
                return versions.find { it.isValid() }?.apply {
                    //确保版本有效
                    saveCurrentVersion(getVersionName(), refresh = false)
                }
            }

            runCatching {
                val versionString = gameInfo!!.version
                getVersion(versionString) ?: run {
                    lDebug("Stored version $versionString not found, using the first available version instead.")
                    getVersionByFirst()
                }
            }.onFailure { e ->
                lWarning("The current version information has not been initialized yet.", e)
            }.getOrElse {
                getVersionByFirst()
            }
        }.also { version ->
            lDebug("The current version is: ${version?.getVersionName()}")
        }

        _currentVersion.update { version }
    }

    fun getVersion(name: String?): Version? {
        name?.let { versionName ->
            return versions.find { it.getVersionName() == versionName }?.takeIf { it.isValid() }
        }
        return null
    }

    /**
     * @return 通过版本名，判断其版本是否存在
     */
    fun checkVersionExistsByName(versionName: String?) =
        versionName?.let { name -> versions.any { it.getVersionName() == name } } ?: false

    /**
     * @return 获取 Zalith 启动器版本标识文件夹
     */
    fun getZalithVersionPath(version: Version) = File(version.getVersionPath(), InfoDistributor.LAUNCHER_IDENTIFIER)

    /**
     * @return 通过目录获取 Zalith 启动器版本标识文件夹
     */
    fun getZalithVersionPath(folder: File) = File(folder, InfoDistributor.LAUNCHER_IDENTIFIER)

    /**
     * @return 通过名称获取 Zalith 启动器版本标识文件夹
     */
    fun getZalithVersionPath(name: String) = File(getVersionPath(name), InfoDistributor.LAUNCHER_IDENTIFIER)

    /**
     * @return 游戏的上一次运行日志
     */
    fun getLatestLog(version: Version) = File(getZalithVersionPath(version), LogName.GAME.fileName)

    /**
     * @return 获取当前版本设置的图标
     */
    fun getVersionIconFile(version: Version) = File(getZalithVersionPath(version), "VersionIcon.png")

    /**
     * @return 通过目录获取 Zalith 启动器版本标识文件夹
     */
    fun getVersionIconFile(folder: File) = File(getZalithVersionPath(folder), "VersionIcon.png")

    /**
     * @return 通过名称获取当前版本设置的图标
     */
    fun getVersionIconFile(name: String) = File(getZalithVersionPath(name), "VersionIcon.png")

    /**
     * @return 通过名称获取版本的文件夹路径
     */
    fun getVersionPath(name: String) = File(getVersionsHome(), name)

    /**
     * 保存当前选择的版本
     * @return 是否执行保存
     */
    fun saveVersion(version: Version, refresh: Boolean = true): Boolean {
        if (!version.isValid()) return false
        saveCurrentVersion(version.getVersionName(), refresh)
        return true
    }

    /**
     * 保存当前选择的版本
     */
    fun saveCurrentVersion(versionName: String, refresh: Boolean = true) {
        runCatching {
            gameInfo!!.apply {
                version = versionName
                saveCurrentInfo()
            }
            if (refresh) {
                lDebug("Current game info file saved, refreshing the current version now.")
                refreshCurrentVersion()
            }
        }.onFailure { e ->
            lError("An exception occurred while saving the currently selected version information.", e)
        }
    }

    /**
     * 重命名当前版本，但并不会在这里对即将重命名的名称，进行非法性判断
     */
    fun renameVersion(version: Version, name: String) {
        val currentVersionName = _currentVersion.value?.getVersionName()
        //如果当前的版本是即将被重命名的版本，那么就把将要重命名的名字设置为当前版本
        val saveToCurrent = version.getVersionName() == currentVersionName

        val versionFolder = version.getVersionPath()
        val renameFolder = File(getVersionsHome(), name)

        //不管重命名之后的文件夹是什么，只要这个文件夹存在，那么就必须删除
        //否则将出现问题
        FileUtils.deleteQuietly(renameFolder)

        val originalName = versionFolder.name

        versionFolder.renameTo(renameFolder)

        val versionJsonFile = File(renameFolder, "$originalName.json")
        val versionJarFile = File(renameFolder, "$originalName.jar")
        val renameJsonFile = File(renameFolder, "$name.json")
        val renameJarFile = File(renameFolder, "$name.jar")

        versionJsonFile.renameTo(renameJsonFile)
        versionJarFile.renameTo(renameJarFile)

        FileUtils.deleteQuietly(versionFolder)

        if (saveToCurrent) {
            //设置并刷新当前版本
            saveCurrentVersion(name, refresh = false)
        }

        refresh("VersionsManager.renameVersion")
    }

    /**
     * 将选中的版本复制为一个新的版本
     * @param version 选中的版本
     * @param name 新的版本的名称
     * @param copyAllFile 是否复制全部文件
     */
    fun copyVersion(version: Version, name: String, copyAllFile: Boolean) {
        val versionsFolder = version.getVersionsFolder()
        val newVersion = File(versionsFolder, name)

        val originalName = version.getVersionName()

        //新版本的json与jar文件
        val newJsonFile = File(newVersion, "$name.json")
        val newJarFile = File(newVersion, "$name.jar")

        val originalVersionFolder = version.getVersionPath()
        if (copyAllFile) {
            //启用复制所有文件时，直接将原文件夹整体复制到新版本
            FileUtils.copyDirectory(originalVersionFolder, newVersion)
            //重命名json、jar文件
            val jsonFile = File(newVersion, "$originalName.json")
            val jarFile = File(newVersion, "$originalName.jar")
            if (jsonFile.exists()) jsonFile.renameTo(newJsonFile)
            if (jarFile.exists()) jarFile.renameTo(newJarFile)
        } else {
            //不复制所有文件时，仅复制并重命名json、jar文件
            val originalJsonFile = File(originalVersionFolder, "$originalName.json")
            val originalJarFile = File(originalVersionFolder, "$originalName.jar")
            newVersion.mkdirs()
            // versions/1.21.3/1.21.3.json -> versions/name/name.json
            if (originalJsonFile.exists()) originalJsonFile.copyTo(newJsonFile)
            // versions/1.21.3/1.21.3.jar -> versions/name/name.jar
            if (originalJarFile.exists()) originalJarFile.copyTo(newJarFile)
        }

        //保存版本配置文件
        version.getVersionConfig().copy().let { config ->
            config.setVersionPath(newVersion)
            config.isolationType = SettingState.ENABLE
            config.saveWithThrowable()
        }

        refresh("VersionsManager.copyVersion")
    }

    /**
     * 删除版本
     */
    fun deleteVersion(version: Version) {
        FileUtils.deleteQuietly(version.getVersionPath())
        refresh("VersionsManager.deleteVersion")
    }
}