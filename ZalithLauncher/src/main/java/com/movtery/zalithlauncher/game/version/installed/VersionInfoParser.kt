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
import com.movtery.zalithlauncher.utils.logging.Logger.lDebug
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import java.io.File

fun getGameManifest(version: Version): GameManifest {
    return getGameManifest(version, false)
}

/**
 * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/a6f3fc0/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/Tools.java#L885-L979)
 */
fun getGameManifest(version: Version, skipInheriting: Boolean): GameManifest {
    var gameManifest = GSON.fromJson(File(version.getVersionPath(), "${version.getVersionName()}.json").readText(), GameManifest::class.java)
    if (skipInheriting || version.getVersionInfo()?.loaderInfo == null) {
        processLibraries { gameManifest.libraries }
    } else if (gameManifest.inheritsFrom != null) {
        val inheritsManifest = run {
            val inherits = gameManifest.inheritsFrom
            GSON.fromJson(File(version.getVersionsFolder()).child(inherits).child("${inherits}.json").readText(), GameManifest::class.java)
        }
        insertSafety(
            target = inheritsManifest,
            from = gameManifest,
            "assetIndex", "assets", "id", "mainClass", "minecraftArguments", "releaseTime", "time", "type"
        )

        // Go through the libraries, remove the ones overridden by the custom version
        val inheritLibraryList: MutableList<Library> = ArrayList(inheritsManifest.libraries)
        outer_loop@ for (library in gameManifest.libraries) {
            // Clean libraries overridden by the custom version
            val libName: String = library.name.substring(0, library.name.lastIndexOf(":"))

            for (inheritLibrary in inheritLibraryList) {
                val inheritLibName: String =
                    inheritLibrary.name.substring(0, inheritLibrary.name.lastIndexOf(":"))

                if (libName == inheritLibName) {
                    lDebug(
                        "Library " + libName + ": Replaced version " +
                                libName.substring(libName.lastIndexOf(":") + 1) + " with " +
                                inheritLibName.substring(inheritLibName.lastIndexOf(":") + 1)
                    )

                    // Remove the library , superseded by the overriding libs
                    inheritLibraryList.remove(inheritLibrary)
                    continue@outer_loop
                }
            }
        }


        // Fuse libraries
        inheritLibraryList += gameManifest.libraries
        inheritsManifest.libraries = inheritLibraryList
        processLibraries { inheritsManifest.libraries }

        // Inheriting Minecraft 1.13+ with append custom args
        if (inheritsManifest.arguments != null && gameManifest.arguments != null) {
            val totalArgList: MutableList<Any?> = ArrayList(inheritsManifest.arguments.game)

            var nskip = 0
            for (i in 0..<gameManifest.arguments.game.size) {
                if (nskip > 0) {
                    nskip--
                    continue
                }

                var perCustomArg: Any = gameManifest.arguments.game[i]
                if (perCustomArg is String) {
                    var perCustomArgStr = perCustomArg
                    // Check if there is a duplicate argument on combine
                    if (perCustomArgStr.startsWith("--") && totalArgList.contains(
                            perCustomArgStr
                        )
                    ) {
                        perCustomArg = gameManifest.arguments.game[i + 1]
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

        gameManifest = inheritsManifest
    }

    if (gameManifest.javaVersion?.majorVersion == 0) {
        gameManifest.javaVersion.majorVersion = gameManifest.javaVersion.version
    }

    return gameManifest
}

/**
 * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/a6f3fc0/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/Tools.java#L982-L996)
 */
// Prevent NullPointerException
private fun insertSafety(
    target: GameManifest,
    from: GameManifest,
    vararg keyArr: String
) {
    keyArr.forEach { key ->
        var value: Any? = null
        runCatching {
            val fieldA = from.javaClass.getDeclaredField(key).apply { isAccessible = true }
            value = fieldA.get(from)
            if (((value is String) && value.isNotEmpty()) || value != null) {
                val fieldB = target.javaClass.getDeclaredField(key).apply { isAccessible = true }
                fieldB.set(target, value)
            }
        }.onFailure {
            lWarning("Unable to insert $key = $value", it)
        }
    }
}
