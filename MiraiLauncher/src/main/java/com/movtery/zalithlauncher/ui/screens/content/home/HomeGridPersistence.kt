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

package com.movtery.zalithlauncher.ui.screens.content.home

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.movtery.cardgrid.state.GridCard
import com.tencent.mmkv.MMKV

/** 单张卡片的持久化数据 */
data class HomeCardSnapshot(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("type")
    val type: String = "",
    @SerializedName("x")
    val x: Int = 0,
    @SerializedName("y")
    val y: Int = 0,
    @SerializedName("width")
    val width: Int = 0,
    @SerializedName("height")
    val height: Int = 0
)

/** 网格布局的持久化数据，[columns] 为保存时的网格列数 */
data class HomeGridSnapshot(
    @SerializedName("columns")
    val columns: Int = 0,
    @SerializedName("cards")
    val cards: List<HomeCardSnapshot> = emptyList()
)

/**
 * 主页网格布局的持久化：
 * 使用主页网格专用的 MMKV 实例存储 Gson 序列化的 JSON，与启动器设置存储分离，
 * 系统卡片不参与持久化。
 */
object HomeGridStore {
    private const val KEY_LAYOUT = "homeCardLayout"

    private val mmkv: MMKV by lazy { MMKV.mmkvWithID("home_grid") }

    private val gson = Gson()

    /** 读取布局快照，无有效数据时返回 null */
    fun load(): HomeGridSnapshot? {
        val json = mmkv.decodeString(KEY_LAYOUT, "") ?: ""
        if (json.isBlank()) return null
        return runCatching {
            gson.fromJson(json, HomeGridSnapshot::class.java)
        }.getOrNull()?.takeIf { it.cards.isNotEmpty() }
    }

    /** 保存布局快照（仅用户卡片参与持久化） */
    fun save(cards: List<GridCard>, columns: Int) {
        val snapshot = HomeGridSnapshot(
            columns = columns,
            cards = cards.map { card ->
                HomeCardSnapshot(
                    id = card.id,
                    type = card.type.typeId,
                    x = card.layout.x,
                    y = card.layout.y,
                    width = card.layout.width,
                    height = card.layout.height
                )
            }
        )
        mmkv.encode(KEY_LAYOUT, gson.toJson(snapshot))
    }
}
