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

package com.movtery.zalithlauncher.game.download.game.fabric

import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.download.game.GameLibDownloader
import com.movtery.zalithlauncher.game.version.download.BaseMinecraftDownloader
import java.io.File

const val FABRIC_LIKE_COMPLETER_ID = "Completer.FabricLike"

fun getFabricLikeCompleterTask(
    downloader: BaseMinecraftDownloader,
    tempMinecraftDir: File,
    tempVersionJson: File,
): Task {
    return Task.runTask(
        id = FABRIC_LIKE_COMPLETER_ID,
        task = { task ->
            val libDownloader = GameLibDownloader(
                downloader = downloader,
                gameJson = tempVersionJson.readText()
            )
            //提交下载计划
            libDownloader.schedule(task, File(tempMinecraftDir, "libraries"))
            //补全游戏库
            libDownloader.download(task)
        }
    )
}