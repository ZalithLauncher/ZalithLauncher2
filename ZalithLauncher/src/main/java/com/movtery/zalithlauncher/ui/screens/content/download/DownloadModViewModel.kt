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

package com.movtery.zalithlauncher.ui.screens.content.download

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.mod.InstalledMod
import com.movtery.zalithlauncher.game.version.mod.ModFingerprints
import com.movtery.zalithlauncher.game.version.mod.matchInstalledMods
import com.movtery.zalithlauncher.game.version.mod.scanModFingerprints
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "DownloadModViewModel"

/** 下载模组屏幕的本地模组安装信息状态 */
class DownloadModViewModel : ViewModel() {
    /** 当前匹配的目标平台 */
    var currentPlatform: Platform = AllSettings.searchModPlatform.getValue()
        private set

    /** 是否正在进行本地扫描或平台匹配 */
    var matching by mutableStateOf(false)
        private set

    /** 当前平台下本地已安装的模组项目映射，键为平台项目ID */
    var installedByProject by mutableStateOf<Map<String, InstalledMod>>(emptyMap())
        private set

    /** 当前平台下本地已安装的模组版本映射，键为平台版本ID */
    private var installedByVersion by mutableStateOf<Map<String, InstalledMod>>(emptyMap())

    private var scanJob: Job? = null

    /** 最近一次扫描对应的版本名称，用于判断扫描目标是否变化 */
    private var scannedVersionName: String? = null

    /** 最近一次扫描得到的所有本地模组文件指纹 */
    private var scannedFingerprints: List<ModFingerprints> = emptyList()

    /** 本次生命周期内已完成的平台匹配结果，避免切换平台时重复计算 */
    private val matchedResults =
        mutableMapOf<Platform, Pair<Map<String, InstalledMod>, Map<String, InstalledMod>>>()

    /**
     * 查询平台项目在本地是否已安装
     */
    fun checkProject(platform: Platform, projectId: String): InstalledMod? {
        return installedByProject[projectId]
            ?.takeIf { it.platform == platform && !it.notFound }
    }

    /**
     * 查询平台版本在本地是否已安装
     */
    fun checkVersion(version: PlatformVersion): InstalledMod? {
        return installedByVersion[version.platformId()]
            ?.takeIf { it.platform == version.platform() && !it.notFound }
    }

    /**
     * 扫描指定游戏版本的模组目录，并按当前平台匹配本地已安装的模组
     *
     * 扫描目标版本变化时立即清除旧的匹配结果；
     * 同版本重复扫描时保留旧结果直至新结果就绪，避免标注闪烁
     */
    fun scan(version: Version?) {
        scanJob?.cancel()

        val versionName = version?.getVersionName()
        if (versionName != scannedVersionName) {
            installedByProject = emptyMap()
            installedByVersion = emptyMap()
        }
        //重扫后指纹集合可能变化，已完成的平台匹配结果全部失效
        matchedResults.clear()

        scanJob = viewModelScope.launch {
            matching = true
            scannedVersionName = versionName
            scannedFingerprints = version?.let { ver ->
                runCatching {
                    scanModFingerprints(VersionFolders.MOD.getDir(ver.getGameDir()))
                }.onFailure { e ->
                    Logger.warning(TAG, "Failed to scan local mod fingerprints", e)
                }.getOrDefault(emptyList())
            } ?: emptyList()

            applyMatches(currentPlatform)
            matching = false
        }
    }

    /**
     * 搜索平台变更时，使用已扫描的指纹按新平台重新匹配
     */
    fun onPlatformChanged(platform: Platform) {
        if (currentPlatform == platform) return
        currentPlatform = platform

        // 扫描尚未结束时，扫描尾部会自动按最新平台进行匹配
        if (scanJob?.isActive == true) return

        scanJob = viewModelScope.launch {
            matching = true
            applyMatches(platform)
            matching = false
        }
    }

    private suspend fun applyMatches(platform: Platform) {
        matchedResults[platform]?.let { (byProject, byVersion) ->
            installedByProject = byProject
            installedByVersion = byVersion
            return
        }

        val fingerprints = scannedFingerprints
        if (fingerprints.isEmpty()) {
            installedByProject = emptyMap()
            installedByVersion = emptyMap()
            return
        }

        val matched = matchInstalledMods(fingerprints, platform)
        // 存在失败分块时不做会话内缓存，下次切换平台时重试（成功块已有持久缓存兜底）
        if (matched.complete) matchedResults[platform] = matched.byProject to matched.byVersion
        installedByProject = matched.byProject
        installedByVersion = matched.byVersion
    }

    override fun onCleared() {
        scanJob?.cancel()
    }
}
