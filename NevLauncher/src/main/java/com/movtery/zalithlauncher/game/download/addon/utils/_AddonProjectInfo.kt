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

package com.movtery.zalithlauncher.game.download.addon.utils

import android.content.Context
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.addon.platform.curseforge.models.CurseForgeProject
import com.movtery.zalithlauncher.game.download.addon.platform.modrinth.models.ModrinthSingleProject
import com.movtery.zalithlauncher.utils.isChinese
import com.movtery.zalithlauncher.utils.string.containsChinese
import com.movtery.zalithlauncher.utils.string.tokenize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.jackhuang.hmcl.addon.AddonProjectInfo
import org.jackhuang.hmcl.addon.AddonProjectInfoRegistry

/**
 * 根据平台类别获取对应的 Addon 项目信息注册表，不支持的类别返回 `null`
 */
fun PlatformClasses.getTranslations(): AddonProjectInfoRegistry<*>? =
    when (this) {
        PlatformClasses.MOD -> AddonProjectInfoRegistry.MOD
        PlatformClasses.MOD_PACK -> AddonProjectInfoRegistry.MODPACK
        else -> null
    }

/**
 * 根据平台获取模组翻译信息
 */
fun PlatformProject.getMcMod(
    classes: PlatformClasses
): AddonProjectInfo? {
    val translations = classes.getTranslations() ?: return null
    return when (this) {
        is ModrinthSingleProject -> translations.getInfoByCurseForgeId(slug)
        is CurseForgeProject -> translations.getInfoByCurseForgeId(data.slug)
        else -> error("Unknown project type: $this")
    }
}

/**
 * 获取 mcmod 模组翻译标题，若当前环境非中文环境，则返回原始模组名称
 */
fun AddonProjectInfo?.getMcmodTitle(originTitle: String, context: Context? = null): String {
    return this?.displayName()?.takeIf { isChinese(context) } ?: originTitle
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
        val englishSearchFilter = tokenize(mod.subName().ifBlank { mod.mainName() }).joinToString(" ")
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
): List<AddonProjectInfo>? {
    if (!this.containsChinese()) return null
    val translations = classes.getTranslations() ?: return null
    return withContext(Dispatchers.Default) {
        ensureActive()
        translations.search(this@searchMcMods)
    }
}
