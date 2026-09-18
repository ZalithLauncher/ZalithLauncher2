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

package com.movtery.zalithlauncher.game.addons.mirror

import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.io.IOException

private const val TAG = "SourceUtils"

data class MirrorSource<T>(
    val type: SourceType,
    val block: suspend () -> T?
)

/**
 * 按顺序尝试各源，首个成功者胜出
 * 全部失败时抛出最后一次异常
 */
suspend fun <T> runMirrorable(
    sources: List<MirrorSource<T>>
): T? = withContext(Dispatchers.IO) {
    var result: T? = null
    var succeed = false
    var lastException: Throwable? = null

    loop@ for (source in sources) {
        ensureActive()

        runCatching {
            val res = source.block()
            result = res
            succeed = true
            break@loop
        }.onFailure {
            Logger.debug(TAG, "Source ${source.type.displayName} failed!", it)
            lastException = it
        }
    }

    if (!succeed) throw lastException ?: IOException("Failed to retrieve information from the source!")

    result
}

/**
 * 把官方在前、镜像在后的规范源列表按“游戏内容镜像源”设置重排；
 * 自动档下大陆用户镜像优先
 */
fun <T> List<MirrorSource<T>>.orderedByGameSourcePreference(): List<MirrorSource<T>> =
    when (resolveMirrorPriority(AllSettings.gameDownloadSource.getValue(), mainland = true)) {
        MirrorPriority.OFFICIAL_FIRST -> this
        MirrorPriority.MIRROR_FIRST -> reversed()
    }
