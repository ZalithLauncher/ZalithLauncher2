/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2021  huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.movtery.zalithlauncher.game.download.assets.utils

import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.utils.ModTranslations.EMPTY
import com.movtery.zalithlauncher.game.download.assets.utils.ModTranslations.MOD
import com.movtery.zalithlauncher.game.download.assets.utils.ModTranslations.MODPACK
import com.movtery.zalithlauncher.path.URL_MCMOD
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.math.max

private const val TAG = "ModTranslations"

/**
 * Parser for mod_data.txt
 *
 * @see <a href="https://www.mcmod.cn">mcmod.cn</a>
 */
enum class ModTranslations(private val resourceName: String) {
    MOD("/assets/mod_data.txt") {
        override fun getMcmodUrl(mcMod: McMod): String = "${URL_MCMOD}class/${mcMod.mcmod}.html"
    },
    MODPACK("/assets/modpack_data.txt") {
        override fun getMcmodUrl(mcMod: McMod): String = "${URL_MCMOD}modpack/${mcMod.mcmod}.html"
    },
    EMPTY("") {
        override fun getMcmodUrl(mcMod: McMod): String = ""
    };

    private var mcMods: List<McMod>? = null
    private var mcModIdMap: Map<String, McMod>? = null
    private var slugMap: Map<String, McMod>? = null
    private var keywords: List<Pair<String, McMod>>? = null
    private var maxKeywordLength = -1

    fun getModBySlugId(id: String?): McMod? {
        if (id.isNullOrBlank() || !loadSlugMap()) return null
        return slugMap!![id]
    }

    fun getModById(id: String?): McMod? {
        if (id.isNullOrBlank() || !loadModIdMap()) return null
        return mcModIdMap!![id]
    }

    abstract fun getMcmodUrl(mcMod: McMod): String

    suspend fun searchMod(query: String): List<McMod> {
        if (!loadKeywords()) return emptyList()
        val newQuery = query.filterNot(Char::isWhitespace)
        val lcs = LongestCommonSubsequence(newQuery.length, maxKeywordLength)

        return withContext(Dispatchers.Default) {
            fun <T> ensureActive(
                block: () -> T
            ): T {
                ensureActive()
                return block()
            }
            keywords!!.asSequence()
                .map { (keyword, mod) ->
                    ensureActive { lcs.calc(newQuery, keyword) to mod }
                }
                .filter { (value) ->
                    ensureActive { value >= max(1, newQuery.length - 3) }
                }
                .sortedByDescending {
                    ensureActive { it.first }
                }
                .map {
                    ensureActive { it.second }
                }
                .toList()
        }
    }

    private fun loadFromResource(): Boolean {
        if (mcMods != null) return true
        if (resourceName.isBlank()) {
            mcMods = emptyList()
            return true
        }

        return try {
            ModTranslations::class.java.getResourceAsStream(resourceName)?.use { input ->
                mcMods = input.reader().readLines()
                    .filterNot { it.startsWith("#") }
                    .map(::McMod)
            }
            true
        } catch (e: Exception) {
            Logger.warning(TAG, "Failed to load $resourceName", e)
            false
        }
    }

    private fun loadSlugMap(): Boolean {
        if (slugMap != null) return true
        if (mcMods == null && !loadFromResource()) return false

        slugMap = mcMods!!.asSequence()
            .filter { it.slug.isNotBlank() }
            .associateBy { it.slug }
        return true
    }

    private fun loadModIdMap(): Boolean {
        if (mcModIdMap != null) return true
        if (mcMods == null && !loadFromResource()) return false

        mcModIdMap = mcMods!!.flatMap { mod ->
            mod.modIds.filter { it.isNotBlank() && it != "examplemod" }.map { it to mod }
        }.toMap()
        return true
    }

    private fun loadKeywords(): Boolean {
        if (keywords != null) return true
        if (mcMods == null && !loadFromResource()) return false

        keywords = mcMods!!.flatMap { mod ->
            listOfNotNull(
                mod.name.takeIf(String::isNotBlank)?.let { it to mod },
                mod.subname.takeIf(String::isNotBlank)?.let { it to mod },
                mod.abbr.takeIf(String::isNotBlank)?.let { it to mod }
            )
        }
        maxKeywordLength = keywords!!.maxOfOrNull { it.first.length } ?: -1
        return true
    }

    class McMod(line: String) {
        val slug: String
        val mcmod: String
        val modIds: List<String>
        val name: String
        val subname: String
        val abbr: String

        init {
            val items = line.split(";", limit = 6)
            require(items.size == 6) { "Illegal mod data line, 6 items expected: $line" }
            slug = items[0]
            mcmod = items[1]
            modIds = items[2].split(",")
            name = items[3]
            subname = items[4]
            abbr = items[5]
        }

        val displayName: String
            get() = buildString {
                if (abbr.isNotBlank()) append("[").append(abbr.trim()).append("] ")
                append(name)
                if (subname.isNotBlank()) append(" (").append(subname).append(")")
            }
    }

    /**
     * Class for computing the longest common subsequence between strings.
     */
    private class LongestCommonSubsequence(maxLengthA: Int, maxLengthB: Int) {
        private val f = Array(maxLengthA + 1) { IntArray(maxLengthB + 1) }

        fun calc(a: CharSequence, b: CharSequence): Int {
            require(a.length <= f.size - 1 && b.length <= f[0].size - 1) { "Too large length" }
            for (i in 1..a.length) {
                for (j in 1..b.length) {
                    f[i][j] = if (a[i - 1] == b[j - 1]) {
                        1 + f[i - 1][j - 1]
                    } else {
                        max(f[i - 1][j], f[i][j - 1])
                    }
                }
            }
            return f[a.length][b.length]
        }
    }
}

fun PlatformClasses.getTranslations(): ModTranslations =
    when (this) {
        PlatformClasses.MOD -> MOD
        PlatformClasses.MOD_PACK -> MODPACK
        else -> EMPTY
    }