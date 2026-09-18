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

package com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.ForgeBuildVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.ForgeLikeVersion

/**
 * [Reference PCL2](https://github.com/Hex-Dragon/PCL2/blob/44aea3e/Plain%20Craft%20Launcher%202/Modules/Minecraft/ModDownload.vb#L773-L807)
 */
class NeoForgeVersion(
    private val rawVersion: String,
    val isLegacyForge: Boolean
) : ForgeLikeVersion(
    loaderName = ModLoader.NEOFORGE.displayName,
    forgeBuildVersion = parseVersion(rawVersion),
    versionName = parseVersionName(rawVersion),
    inherit = parseInherit(rawVersion),
    fileExtension = "jar"
) {
    val baseUrl: String
        get() {
            val packageName = if (isLegacyForge) "forge" else "neoforge"
            return "https://maven.neoforged.net/releases/net/neoforged/$packageName/$rawVersion/$packageName-$rawVersion"
        }

    val isBeta: Boolean
        get() = rawVersion.contains("beta")
}

private fun parseVersion(rawVersion: String): ForgeBuildVersion {
    return when {
        rawVersion.contains("1.20.1") -> {
            val versionPart = rawVersion.replace("1.20.1-", "")
            ForgeBuildVersion.parse("19.$versionPart")
        }
        else -> ForgeBuildVersion.parse(rawVersion.substringBefore("-"))
    }
}

private fun parseVersionName(rawVersion: String): String {
    return if (rawVersion.contains("1.20.1")) {
        rawVersion.replace("1.20.1-", "")
    } else {
        rawVersion
    }
}

private fun parseInherit(rawVersion: String): String {
    return when {
        rawVersion.contains("1.20.1") -> "1.20.1"
        //暂时认为0开头代表特殊版本
        rawVersion.startsWith("0.") -> {
            //特殊版本
            val versionPart = rawVersion.replace("0.", "").substringBefore("-")
            //"25w14craftmine.3" -> "25w14craftmine"
            val version = versionPart.substringBeforeLast(".")
            version
        }
        else -> {
            val version = parseVersion(rawVersion)
            //优先解析26.1+新版本格式
            parseNewInherit(version) ?: run {
                buildString {
                    append("1.").append(version.major)
                    if (version.minor != 0) append(".").append(version.minor)
                }
            }
        }
    }
}

private fun parseNewInherit(
    buildVersion: ForgeBuildVersion
): String? {
    //26.1.0.0
    if (buildVersion.major < 26) return null
    return buildString {
        append(buildVersion.major)
        append(".")
        append(buildVersion.minor)
        buildVersion.build
            .takeIf { it > 0 }
            ?.let { part ->
                append(".")
                append(part)
            }
    }
}