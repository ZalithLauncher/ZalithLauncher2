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

package com.movtery.zalithlauncher.game.version.mod.update

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.assets.platform.getVersions
import com.movtery.zalithlauncher.game.download.assets.utils.ModTranslations
import com.movtery.zalithlauncher.game.version.mod.ModFile
import com.movtery.zalithlauncher.game.version.mod.ModProject
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.initAll
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.string.parseInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "ModData"

/**
 * 需要更新的模组的数据类，记录模组文件和模组所属的项目
 * @param modFile 模组在模组平台上对应的文件
 * @param project 模组在模组平台上所属的项目
 * @param mcMod 模组翻译信息
 */
data class ModData(
    val file: File,
    val modFile: ModFile,
    val project: ModProject,
    val mcMod: ModTranslations.McMod?
) {
    /**
     * 当前模组的版本号，用于新旧对比
     */
    var currentVersion: String? = null
        private set

    /**
     * 检查模组更新
     * @param minecraftVer MC版本，用于筛选版本
     * @param modLoader 模组加载器信息，用于筛选版本
     */
    suspend fun checkUpdate(
        minecraftVer: String,
        modLoader: ModLoader
    ): PlatformVersion? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val datePublished = parseInstant(modFile.datePublished)
                val projectId = project.id
                val currentLoaderName = modLoader.displayName.lowercase()
                val currentFileLoaders = modFile.loaders
                    .map { it.getDisplayName().lowercase() }
                    .toSet()
                val targetLoaders = when {
                    currentLoaderName in currentFileLoaders -> {
                        // 当前模组文件支持当前游戏加载器：仅检查当前加载器通道的更新
                        setOf(currentLoaderName)
                    }

                    currentFileLoaders.isNotEmpty() -> {
                        // 当前模组文件不支持当前游戏加载器（例如 信雅互联 场景）：
                        // 优先沿用该模组文件自身支持的加载器通道来检查更新
                        currentFileLoaders
                    }

                    else -> {
                        // 无法识别当前文件加载器信息时，回退到当前游戏加载器
                        setOf(currentLoaderName)
                    }
                }

                // 获取所有版本并初始化
                val versions = getVersions(
                    projectId,
                    project.platform
                ).initAll(projectId)
                    .filter { version ->
                        if (version.platformId() == modFile.id) {
                            // 当前版本，设置版本号
                            currentVersion = version.platformVersion()
                        }
                        val loaderNames = version.platformLoaders()
                            .map { it.getDisplayName().lowercase() }
                            .toSet()
                        // 是否支持当前MC版本
                        minecraftVer in version.platformGameVersion() &&
                        // 是否匹配目标加载器（当前加载器，或当前文件自身的加载器）
                        loaderNames.any { it in targetLoaders } &&
                        // 是否比当前版本更新
                        version.platformDatePublished() > datePublished
                    }

                // 获取最新的版本
                versions.firstOrNull()?.also { version ->
                    Logger.info(TAG, "Detected update for mod ${file.name}: $currentVersion -> ${version.platformVersion()}")
                }
            }.onFailure { th ->
                Logger.warning(TAG, "An error occurred while fetching all versions of the mod.", th)
            }.getOrNull()
        }
    }
}
