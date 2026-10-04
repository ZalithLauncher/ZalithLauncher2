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

package com.movtery.zalithlauncher.game.download.assets.utils

import android.content.Context
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeProject
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthSingleProject
import com.movtery.zalithlauncher.utils.isChinese
import com.movtery.zalithlauncher.utils.string.containsChinese
import com.movtery.zalithlauncher.utils.string.tokenize

/**
 * 根据平台获取模组翻译信息
 */
fun PlatformProject.getMcMod(
    classes: PlatformClasses
): ModTranslations.McMod? {
    val translations = classes.getTranslations()
    return when (this) {
        is ModrinthSingleProject -> translations.getModBySlugId(slug)
        is CurseForgeProject -> translations.getModBySlugId(data.slug)
        else -> error("Unknown project type: $this")
    }
}

/**
 * 根据平台获取模组翻译信息
 */
fun PlatformProject.getMcMod(
    translations: ModTranslations
): ModTranslations.McMod? {
    return when (this) {
        is ModrinthSingleProject -> translations.getModBySlugId(slug)
        is CurseForgeProject -> translations.getModBySlugId(data.slug)
        else -> error("Unknown project type: $this")
    }
}

/**
 * 获取 mcmod 模组翻译标题，若当前环境非中文环境，则返回原始模组名称
 */
fun ModTranslations.McMod?.getMcmodTitle(originTitle: String, context: Context? = null): String {
    return this?.displayName?.takeIf { isChinese(context) } ?: originTitle
}

/**
 * 修改自源代码：[HMCL Github](https://github.com/HMCL-dev/HMCL/blob/d295e60/HMCL/src/main/java/org/jackhuang/hmcl/game/LocalizedRemoteModRepository.java#L45-L64)
 * 原项目版权归原作者所有，遵循GPL v3协议
 * @return `Boolean` 是否包含中文, `String` 英文混合关键词 (不包含中文时，原样返回)
 */
suspend fun String.localizedModSearchKeywords(
    classes: PlatformClasses
): Pair<Boolean, Set<String>?> {
    val mcMods = this.searchMcMods(classes) ?: return false to null
    val englishSearchFiltersSet: MutableSet<String> = LinkedHashSet(16)

    for ((count, mod) in mcMods.withIndex()) {
        val englishSearchFilter = tokenize(mod.subname.ifBlank { mod.name }).joinToString(" ")
        if (englishSearchFilter.isNotBlank()) {
            englishSearchFiltersSet.add(englishSearchFilter)
        }
        if (count >= 3) break
    }

    return true to englishSearchFiltersSet
}

/**
 * 如果搜索内容包含中文，返回搜索到的 MCMod 项目
 */
suspend fun String.searchMcMods(
    classes: PlatformClasses
): List<ModTranslations.McMod>? {
    if (!this.containsChinese()) return null
    return classes.getTranslations().searchMod(this)
}