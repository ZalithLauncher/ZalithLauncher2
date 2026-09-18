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

package com.movtery.zalithlauncher.game.download.modpack.install

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.fabric.FabricVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.quilt.QuiltVersions
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.forge.ForgeVersions
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.NeoForgeVersions
import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo

/**
 * 整合包信息
 * @param name 整合包名称
 * @param summary 整合包的简介（可用到版本描述上）
 * @param ram 整合包推荐分配的内存
 * @param files 整合包所有需要下载的模组
 * @param loaders 整合包需要安装的模组加载器
 * @param gameVersion 整合包需要的游戏版本
 */
data class ModPackInfo(
    val name: String,
    val summary: String? = null,
    val ram: Int? = null,
    val files: List<ModFile>,
    val loaders: List<Pair<ModLoader, String>>,
    val gameVersion: String
)

/**
 * 模组加载器解析匹配任务
 * @return 构建好的游戏下载安装信息
 */
suspend fun ModPackInfo.retrieveLoaderTask(
    targetVersionName: String
): GameDownloadInfo {
    var gameInfo = GameDownloadInfo(
        gameVersion = gameVersion,
        customVersionName = targetVersionName
    )

    //匹配目标加载器版本，获取详细版本信息
    loaders.forEach { pair ->
        pair.retrieveLoader(
            gameVersion = gameVersion,
            gameInfo = gameInfo,
            pasteGameInfo = { newInfo ->
                gameInfo = newInfo
            }
        )
    }

    return gameInfo
}

/**
 * 模组加载器解析匹配，并粘贴游戏下载信息
 * @param gameVersion 当前游戏版本
 * @param pasteGameInfo 将识别到的模组加载器版本贴回信息类
 */
suspend fun Pair<ModLoader, String>.retrieveLoader(
    gameVersion: String,
    gameInfo: GameDownloadInfo,
    pasteGameInfo: (GameDownloadInfo) -> Unit
) {
    val (loader, version) = this
    when (loader) {
        ModLoader.FORGE -> {
            ForgeVersions.fetchForgeList(gameVersion)?.find {
                it.versionName == version
            }?.let { forgeVersion ->
                pasteGameInfo(gameInfo.copy(forge = forgeVersion))
            }
        }
        ModLoader.NEOFORGE -> {
            NeoForgeVersions.fetchNeoForgeList(gameVersion = gameVersion)?.find {
                it.versionName == version
            }?.let { neoforgeVersion ->
                pasteGameInfo(gameInfo.copy(neoforge = neoforgeVersion))
            }
        }
        ModLoader.FABRIC -> {
            FabricVersions.fetchFabricLoaderList(gameVersion)?.find {
                it.version == version
            }?.let { fabricVersion ->
                pasteGameInfo(gameInfo.copy(fabric = fabricVersion))
            }
        }
        ModLoader.QUILT -> {
            QuiltVersions.fetchQuiltLoaderList(gameVersion)?.find {
                it.version == version
            }?.let { quiltVersion ->
                pasteGameInfo(gameInfo.copy(quilt = quiltVersion))
            }
        }
        else -> {
            //不支持
        }
    }
}