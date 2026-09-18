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

package com.movtery.zalithlauncher.ui.screens.content.versions.elements

import com.movtery.zalithlauncher.game.version.resource_pack.ResourcePackInfo

/** 资源包操作状态 */
sealed interface ResourcePackOperation {
    data object None : ResourcePackOperation
    /** 执行任务中 */
    data object Progress : ResourcePackOperation
    /** 重命名资源包输入对话框 */
    data class RenamePack(val packInfo: ResourcePackInfo) : ResourcePackOperation
    /** 删除资源包输入对话框 */
    data class DeletePack(val packInfo: ResourcePackInfo) : ResourcePackOperation
}

/**
 * 简易的资源包过滤器
 */
data class ResourcePackFilter(
    val onlyShowValid: Boolean,
    val filterName: String
)

/**
 * 简易过滤器，过滤指定名称的资源包
 */
fun List<ResourcePackInfo>.filterPacks(filter: ResourcePackFilter) = this.filter {
    val valid = !filter.onlyShowValid || it.isValid
    val nameMatched = filter.filterName.isEmpty() ||
            //用清除了格式化代码的名称进行判断
            it.rawName.contains(filter.filterName, true)
    valid && nameMatched
}
