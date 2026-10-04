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

package com.movtery.zalithlauncher.game.download.assets.platform

import com.movtery.zalithlauncher.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PlatformSortField(
    val curseforge: String,
    val modrinth: String
): PlatformFilterCode {
    /** 相关 */
    @SerialName("RELEVANCE")
    RELEVANCE("1", "relevance") {
        override fun getDisplayName(): Int = R.string.download_assets_filter_sort_by_relevant
    },

    /** 下载量 */
    @SerialName("DOWNLOADS")
    DOWNLOADS("6", "downloads") {
        override fun getDisplayName(): Int = R.string.download_assets_filter_sort_by_total_downloads
    },

    /** 人气 */
    @SerialName("POPULARITY")
    POPULARITY("2", "follows") {
        override fun getDisplayName(): Int = R.string.download_assets_filter_sort_by_popularity
    },

    /** 新创建 */
    @SerialName("NEWEST")
    NEWEST("11", "newest") {
        override fun getDisplayName(): Int = R.string.download_assets_filter_sort_by_recently_created
    },

    /** 最近更新 */
    @SerialName("UPDATED")
    UPDATED("3", "updated") {
        override fun getDisplayName(): Int = R.string.download_assets_filter_sort_by_recently_updated
    };

    override fun index(): Int = this.ordinal
}