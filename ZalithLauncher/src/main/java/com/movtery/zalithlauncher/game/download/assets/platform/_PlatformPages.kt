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

import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsPage

fun previousPage(
    pageNumber: Int,
    pages: List<AssetsPage?>,
    index: Int,
    limit: Int,
    onSuccess: (AssetsPage) -> Unit = {},
    onSearch: (index: Int) -> Unit = {}
) {
    val targetIndex = pageNumber - 2 //上一页在缓存中的索引
    val previousPage = pages.getOrNull(targetIndex)
    if (previousPage != null) {
        onSuccess(previousPage)
    } else {
        //重新搜索
        onSearch((index - limit).coerceAtLeast(0))
    }
}

fun nextPage(
    pageNumber: Int,
    isLastPage: Boolean,
    pages: List<AssetsPage?>,
    index: Int,
    limit: Int,
    onSuccess: (AssetsPage) -> Unit = {},
    onSearch: (index: Int) -> Unit = {}
) {
    if (!isLastPage) {
        val nextIndex = pageNumber
        //判断是否已缓存下一页
        val nextPage = pages.getOrNull(nextIndex)
        if (nextPage != null) {
            onSuccess(nextPage)
        } else {
            //搜索下一页
            onSearch(index + limit)
        }
    }
}

fun navigatePage(
    pageNumber: Int,
    pages: List<AssetsPage?>,
    limit: Int,
    onSuccess: (AssetsPage) -> Unit = {},
    onSearch: (index: Int) -> Unit = {}
) {
    val targetNumber = pageNumber - 1
    //判断是否已缓存目标页
    val targetPage = pages.getOrNull(targetNumber)
    if (targetPage != null) {
        onSuccess(targetPage)
    } else {
        //搜索目标页
        onSearch(targetNumber * limit)
    }
}