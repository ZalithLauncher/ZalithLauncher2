/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 * Copyright (C) 2026 Mirai Launcher contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.renderer

import com.movtery.zalithlauncher.game.renderer.renderers.GL4ESRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.KopperZinkRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.LTWLegacyRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.LTWRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.VirGLRenderer
import com.movtery.zalithlauncher.game.version.installed.utils.isBiggerVer
import com.movtery.zalithlauncher.game.version.installed.utils.isLowerVer

/**
 * Chooses which wrapper an instance should launch with.
 *
 * A renderer is only ever offered for a Minecraft version it declares itself compatible with,
 * through [RendererInterface.getMinMCVersion] / [RendererInterface.getMaxMCVersion]. Nothing
 * in this file hardcodes a renderer's compatibility window, so adding a renderer to
 * [Renderers.BUILT_IN] is enough to make it participate in automatic selection.
 *
 * Minecraft's renderer boundary is the OpenGL profile, not the version number nobody can
 * agree on:
 *
 *  - **1.8 – 1.16.5** drive OpenGL 1.x/2.1 with fixed-function state and the legacy
 *    client-array draw path, so they need [LTWLegacyRenderer] (or GL4ES).
 *  - **1.17 and newer** require the OpenGL 3.2 core profile, which is what [LTWRenderer]
 *    implements.
 *
 * An explicit per-instance choice always wins, but only while it stays compatible with the
 * version being launched; otherwise the launcher would refuse to start the game a moment later
 * in its own compatibility check.
 */
object RendererPicker {
    // Derived from the renderers themselves so an identifier is never written down twice.
    val GL4ES: String = GL4ESRenderer.getUniqueIdentifier()
    val VIRGL: String = VirGLRenderer.getUniqueIdentifier()
    val LTW: String = LTWRenderer.getUniqueIdentifier()
    val ZINK: String = KopperZinkRenderer.getUniqueIdentifier()
    val LTW_LEGACY: String = LTWLegacyRenderer.getUniqueIdentifier()

    /** The Minecraft release that moved the game onto the OpenGL 3.2 core profile. */
    private const val CORE_PROFILE_VERSION = "1.17"

    /** Preferred legacy wrappers, best first. */
    private val LEGACY_ORDER = listOf(LTW_LEGACY, GL4ES, VIRGL)

    /** Preferred core-profile wrappers, best first. */
    private val MODERN_ORDER = listOf(LTW, ZINK)

    data class Choice(
        val identifier: String,
        val reason: String,
        val automatic: Boolean,
    )

    /**
     * Resolve the identifier to launch [mcVersion] with, or `null` when the picker cannot
     * make a better decision than the caller already would.
     *
     * The launcher calls this on the launch path. It returns `null` for an unknown version so
     * that a missing version string keeps behaving exactly as it did before the picker
     * existed, rather than silently changing which wrapper starts.
     */
    fun resolve(mcVersion: String, manualIdentifier: String): String? {
        if (mcVersion.isBlank()) return null
        val available = Renderers.getRenderers().map { it.getUniqueIdentifier() }.toSet()
        if (available.isEmpty()) return null
        return pick(mcVersion, manualIdentifier, available).identifier.ifEmpty { null }
    }

    /**
     * @param mcVersion the Minecraft version being launched; blank means unknown.
     * @param manualIdentifier the instance's configured renderer, blank when it has none.
     * @param available identifiers of the renderers that are actually loaded right now.
     */
    fun pick(
        mcVersion: String,
        manualIdentifier: String,
        available: Set<String>,
    ): Choice {
        val manual = manualIdentifier.trim()
        if (manual.isNotEmpty()) {
            if (manual in available && supports(manual, mcVersion)) {
                return Choice(manual, "instance override", automatic = false)
            }
            val fallback = automatic(mcVersion, available)
            val reason = if (manual !in available) {
                "instance override missing, ${fallback.reason}"
            } else {
                "instance override unsupported on $mcVersion, ${fallback.reason}"
            }
            return fallback.copy(reason = reason)
        }
        return automatic(mcVersion, available)
    }

    private fun automatic(mcVersion: String, available: Set<String>): Choice {
        val order = if (usesCoreProfile(mcVersion)) MODERN_ORDER else LEGACY_ORDER

        val compatible = order.filter { it in available && supports(it, mcVersion) }
        compatible.firstOrNull()?.let { picked ->
            val reason = when {
                picked != order.first() ->
                    "preferred wrapper unavailable for ${describe(mcVersion)}, using ${nameOf(picked)}"
                usesCoreProfile(mcVersion) ->
                    "${describe(mcVersion)} needs the core profile, using ${nameOf(picked)}"
                else ->
                    "${describe(mcVersion)} uses the legacy pipeline, using ${nameOf(picked)}"
            }
            return Choice(picked, reason, automatic = true)
        }

        // Nothing declares support for this version. Offering something is better than
        // refusing to launch, but say so plainly instead of pretending it is a good match.
        val anyAvailable = available.firstOrNull()
            ?: return Choice("", "no renderer available", automatic = true)
        return Choice(
            anyAvailable,
            "no renderer declares support for ${describe(mcVersion)}, using ${nameOf(anyAvailable)}",
            automatic = true
        )
    }

    private fun usesCoreProfile(mcVersion: String): Boolean =
        mcVersion.isNotBlank() && !mcVersion.isLowerVer(CORE_PROFILE_VERSION)

    /**
     * Whether [identifier] declares support for [mcVersion].
     *
     * An identifier with no built-in metadata belongs to a renderer plugin, which may declare
     * any version range it likes; it is treated as compatible rather than blocked.
     */
    private fun supports(identifier: String, mcVersion: String): Boolean {
        if (mcVersion.isBlank()) return true
        val renderer = Renderers.BUILT_IN.firstOrNull { it.getUniqueIdentifier() == identifier }
            ?: return true
        renderer.getMinMCVersion()?.let { min -> if (mcVersion.isLowerVer(min)) return false }
        renderer.getMaxMCVersion()?.let { max -> if (mcVersion.isBiggerVer(max)) return false }
        return true
    }

    private fun nameOf(identifier: String): String =
        Renderers.BUILT_IN.firstOrNull { it.getUniqueIdentifier() == identifier }
            ?.getRendererName()
            ?: identifier

    private fun describe(mcVersion: String): String =
        if (mcVersion.isBlank()) "an unknown Minecraft version" else "Minecraft $mcVersion"
}
