/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.game.launch

import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest

/**
 * 依赖库加载顺序修复 & 依赖库检查
 * 为以后方便扩展考虑诞生的东西（
 */
class LibSortFix {
    /**
     * 检查并插入依赖库
     */
    fun LinkedHashMap<GameManifest.Library, String>.insertLib(
        libItem: GameManifest.Library,
        path: String
    ) {
        val name = libItem.name
        if (name.startsWith("org.ow2.asm:asm:")) {
            removeIf { (key, _) ->
                key.name.startsWith("org.ow2.asm:asm-all:")
            }
        }

        this[libItem] = path
    }

    private fun <K, V> LinkedHashMap<K, V>.removeIf(predicate: (Map.Entry<K, V>) -> Boolean) {
        val iterator = this.entries.iterator()
        while (iterator.hasNext()) {
            if (predicate(iterator.next())) {
                iterator.remove()
            }
        }
    }
}
