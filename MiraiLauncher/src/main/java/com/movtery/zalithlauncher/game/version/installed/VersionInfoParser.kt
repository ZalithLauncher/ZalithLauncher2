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

package com.movtery.zalithlauncher.game.version.installed

import com.movtery.zalithlauncher.game.version.download.processLibraries
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest.Library
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.file.child
import java.io.File

class VersionInfoParser(private val version: Version) {
    private var gameManifest: GameManifest? = null
    private var inherit: Boolean? = null
    private var skipIfNotExists: Boolean = false

    /**
     * 设置预先加载的 [GameManifest]
     */
    fun setManifest(manifest: GameManifest): VersionInfoParser {
        this.gameManifest = manifest
        return this
    }

    /**
     * 启用版本继承
     * @param skipIfNotExists 若 [GameManifest.inheritsFrom] 对应的 JSON 文件不存在，则静默跳过继承
     */
    fun setInheriting(skipIfNotExists: Boolean = false): VersionInfoParser {
        this.inherit = true
        this.skipIfNotExists = skipIfNotExists
        return this
    }

    /**
     * 构建并返回最终合并后的 [GameManifest]
     */
    fun build(): GameManifest {
        val manifest = gameManifest ?: GSON.fromJson(
            File(version.getVersionPath(), "${version.getVersionName()}.json").readText(),
            GameManifest::class.java
        )

        val inheritsManifest = if (inherit == true && manifest.inheritsFrom != null) {
            val inherits = manifest.inheritsFrom
            File(version.getVersionsFolder()).child(inherits).child("${inherits}.json")
                .let { inheritsFile ->
                    if (skipIfNotExists && !inheritsFile.exists()) null
                    else {
                        GSON.fromJson(inheritsFile.readText(), GameManifest::class.java)
                    }
                }
        } else null

        return getGameManifest(
            gameManifest = manifest,
            inheritsManifest = inheritsManifest
        )
    }
}

/**
 * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/a6f3fc0/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/Tools.java#L885-L979)
 */
private fun getGameManifest(
    gameManifest: GameManifest,
    inheritsManifest: GameManifest?,
): GameManifest {
    var gameManifest0 = gameManifest
    if (inheritsManifest != null && gameManifest0.inheritsFrom != null) {
        mergeManifest(
            from = gameManifest0,
            target = inheritsManifest,
        )

        // Fuse libraries
        val inheritLibraryList: MutableList<Library> = ArrayList(inheritsManifest.libraries)
        inheritLibraryList += gameManifest0.libraries
        inheritsManifest.libraries = inheritLibraryList

        // Inheriting Minecraft 1.13+ with append custom args
        if (inheritsManifest.arguments != null && gameManifest0.arguments != null) {
            val totalArgList: MutableList<Any?> = ArrayList(inheritsManifest.arguments.game)

            var nskip = 0
            for (i in 0..<gameManifest0.arguments.game.size) {
                if (nskip > 0) {
                    nskip--
                    continue
                }

                var perCustomArg: Any = gameManifest0.arguments.game[i]
                if (perCustomArg is String) {
                    var perCustomArgStr = perCustomArg
                    // Check if there is a duplicate argument on combine
                    if (perCustomArgStr.startsWith("--") && totalArgList.contains(
                            perCustomArgStr
                        )
                    ) {
                        perCustomArg = gameManifest0.arguments.game[i + 1]
                        if (perCustomArg is String) {
                            perCustomArgStr = perCustomArg
                            // If the next is argument value, skip it
                            if (!perCustomArgStr.startsWith("--")) {
                                nskip++
                            }
                        }
                    } else {
                        totalArgList.add(perCustomArgStr)
                    }
                } else if (!totalArgList.contains(perCustomArg)) {
                    totalArgList.add(perCustomArg)
                }
            }

            inheritsManifest.arguments.game = totalArgList
        }

        gameManifest0 = inheritsManifest
    }

    processLibraries { gameManifest0.libraries }

    if (gameManifest0.javaVersion?.majorVersion == 0) {
        gameManifest0.javaVersion.majorVersion = gameManifest0.javaVersion.version
    }

    return uniqueLibraries(gameManifest0)
}

private inline fun <T> mergeField(
    getter: () -> T?,
    setter: (T) -> Unit
) {
    when (val value = getter()) {
        null -> return

        is String -> {
            if (value.isNotEmpty()) {
                setter(value)
            }
        }

        else -> setter(value)
    }
}

private fun mergeManifest(
    from: GameManifest,
    target: GameManifest,
) {
    mergeField(from::getRawAssetIndex, target::setAssetIndex)
    mergeField(from::getAssets, target::setAssets)
    mergeField(from::getId, target::setId)
    mergeField(from::getMainClass, target::setMainClass)
    mergeField(from::getMinecraftArguments, target::setMinecraftArguments)
    mergeField(from::getReleaseTime, target::setReleaseTime)
    mergeField(from::getTime, target::setTime)
    mergeField(from::getType, target::setType)
}
