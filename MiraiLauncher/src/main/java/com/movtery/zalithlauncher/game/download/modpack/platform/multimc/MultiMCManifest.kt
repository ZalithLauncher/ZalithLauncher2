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

package com.movtery.zalithlauncher.game.download.modpack.platform.multimc

import com.google.gson.annotations.SerializedName
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.download.modpack.platform.PackManifest

class MultiMCManifest(
    @SerializedName("formatVersion")
    val formatVersion: Int,
    @SerializedName("components")
    val components: List<Component> = emptyList()
): PackManifest {

    class CachedRequires(
        @SerializedName("equals")
        val equalsVersion: String?,
        @SerializedName("uid")
        val uid: String?,
        @SerializedName("suggests")
        val suggests: String?
    )

    class Component(
        @SerializedName("cachedName")
        val cachedName: String? = null,
        @SerializedName("cachedRequires")
        val cachedRequires: MutableList<CachedRequires>? = null,
        @SerializedName("cachedVersion")
        val cachedVersion: String? = null,
        @SerializedName("important")
        val isImportant: Boolean,
        @SerializedName("dependencyOnly")
        val isDependencyOnly: Boolean,
        @SerializedName("uid")
        val uid: String,
        @SerializedName("version")
        val version: String
    )

    /**
     * 尝试获取 Minecraft 版本
     */
    fun getMinecraftVersion(): String? =
        components.find { it.uid == UID_MINECRAFT && it.isImportant }?.version

    /**
     * 匹配模组加载器与版本
     */
    fun Component.retrieveLoader(): Pair<ModLoader, String>? {
        return when (uid) {
            UID_FORGE -> ModLoader.FORGE to version
            UID_NEOFORGE -> ModLoader.NEOFORGE to version
            UID_FABRIC -> ModLoader.FABRIC to version
            UID_QUILT -> ModLoader.QUILT to version
            else -> null
        }
    }
}

const val UID_MINECRAFT = "net.minecraft"
const val UID_FORGE = "net.minecraftforge"
const val UID_NEOFORGE = "net.neoforged"
const val UID_LITELOADER = "com.mumfrey.liteloader"
const val UID_FABRIC = "net.fabricmc.fabric-loader"
const val UID_QUILT = "org.quiltmc.quilt-loader"