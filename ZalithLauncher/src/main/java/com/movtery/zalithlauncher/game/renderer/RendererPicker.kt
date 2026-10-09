/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 * Copyright (C) 2026 Aerix Launcher contributors.
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

import com.movtery.zalithlauncher.game.renderer.renderers.FreedrenoRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.GL4ESRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.KopperZinkRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.LTWRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.MobileGluesRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.NGGL4ESRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.PanfrostRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.VirGLRenderer
import com.movtery.zalithlauncher.game.version.installed.utils.isBiggerVer
import com.movtery.zalithlauncher.game.version.installed.utils.isLowerVer
import com.movtery.zalithlauncher.utils.device.VulkanRequirements
import com.movtery.zalithlauncher.utils.device.normalizeMcVersion

/**
 * Chooses which wrapper an instance should launch with.
 *
 * Renderers declare their Minecraft compatibility through [RendererInterface.getMinMCVersion]
 * / [RendererInterface.getMaxMCVersion]. The picker also applies engine-specific constraints
 * where a renderer's native contract does not match the game's backend loader.
 *
 * Minecraft's renderer boundary is the OpenGL profile, not the version number nobody can
 * agree on. Minecraft 26.2+ also uses SDL's EGL loader for its new graphics-backend path;
 * that loader requires a complete EGL library, whereas LTW only exports a partial EGL shim.
 * Prefer MobileGlues (which supplies the full EGL API) or NG-GL4ES for those versions and
 * reject LTW there rather than passing an incomplete library to SDL.
 *
 *  - **1.8 – 1.16.5** drive OpenGL 1.x/2.1 with fixed-function state and the legacy
 *    client-array draw path, so they need [GL4ESRenderer] or [VirGLRenderer].
 *  - **1.17 and newer** require a core-profile-capable OpenGL wrapper; the picker considers
 *    [LTWRenderer], [MobileGluesRenderer], and modern [NGGL4ESRenderer]. For 26.2+, the SDL
 *    backend constraint above puts MobileGlues and NG-GL4ES ahead of LTW. Legacy GLES2
 *    [GL4ESRenderer] is never used as a fallback for these versions.
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
    val MOBILEGLUES: String = MobileGluesRenderer.getUniqueIdentifier()
    val NG_GL4ES: String = NGGL4ESRenderer.getUniqueIdentifier()
    val ZINK: String = KopperZinkRenderer.getUniqueIdentifier()
    val FREEDRENO: String = FreedrenoRenderer.getUniqueIdentifier()
    val PANFROST: String = PanfrostRenderer.getUniqueIdentifier()

    /** The Minecraft release that moved the game onto the OpenGL 3.2 core profile. */
    private const val CORE_PROFILE_VERSION = "1.17"

    /** Preferred legacy wrappers, best first. */
    private val LEGACY_ORDER = listOf(GL4ES, VIRGL, FREEDRENO, PANFROST)

    /** Preferred core-profile wrappers, best first. GL4ES is intentionally absent here. */
    private val MODERN_ORDER = listOf(LTWRenderer.getUniqueIdentifier(), MOBILEGLUES, NG_GL4ES, ZINK)

    /**
     * Minecraft's SDL/EGL backend needs a full EGL shared library. LTW's egl* exports are only
     * an interception shim, so use a complete EGL implementation for Minecraft 26.2+.
     */
    private val SDL_BACKEND_ORDER = listOf(MOBILEGLUES, NG_GL4ES, ZINK)

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
    fun resolve(
        mcVersion: String,
        manualIdentifier: String,
        deviceGlesVersion: Int? = null,
        vulkanAvailable: Boolean? = null,
    ): String? {
        if (mcVersion.isBlank()) return null
        val available = Renderers.getRenderers().map { it.getUniqueIdentifier() }.toSet()
        if (available.isEmpty()) return null
        return pick(mcVersion, manualIdentifier, available, deviceGlesVersion, vulkanAvailable)
            .identifier.ifEmpty { null }
    }

    /**
     * @param mcVersion the Minecraft version being launched; blank means unknown.
     * @param manualIdentifier the instance's configured renderer, blank when it has none.
     * @param available identifiers of the renderers that are actually loaded right now.
     * @param deviceGlesVersion detected GLES major version; null/zero means unavailable.
     * @param vulkanAvailable whether a native Vulkan implementation was detected; null means unknown.
     */
    fun pick(
        mcVersion: String,
        manualIdentifier: String,
        available: Set<String>,
        deviceGlesVersion: Int? = null,
        vulkanAvailable: Boolean? = null,
    ): Choice {
        val manual = manualIdentifier.trim()
        if (manual.isNotEmpty()) {
            if (manual in available &&
                supports(manual, mcVersion) &&
                supportsDevice(manual, deviceGlesVersion) &&
                supportsVulkan(manual, vulkanAvailable, allowUnknown = true)
            ) {
                return Choice(manual, "instance override", automatic = false)
            }
            val fallback = automatic(mcVersion, available, deviceGlesVersion, vulkanAvailable)
            val reason = when {
                manual !in available -> "instance override missing, ${fallback.reason}"
                !supports(manual, mcVersion) -> "instance override unsupported on $mcVersion, ${fallback.reason}"
                !supportsVulkan(manual, vulkanAvailable, allowUnknown = true) -> "instance override requires Vulkan, but Vulkan is unavailable, ${fallback.reason}"
                else -> {
                    val required = minimumGlesVersion(manual)
                    "instance override requires GLES $required, detected GLES $deviceGlesVersion, ${fallback.reason}"
                }
            }
            return fallback.copy(reason = reason)
        }
        return automatic(mcVersion, available, deviceGlesVersion, vulkanAvailable)
    }

    private fun automatic(
        mcVersion: String,
        available: Set<String>,
        deviceGlesVersion: Int?,
        vulkanAvailable: Boolean?,
    ): Choice {
        val order = when {
            usesSdlGraphicsBackend(mcVersion) -> SDL_BACKEND_ORDER
            usesCoreProfile(mcVersion) -> MODERN_ORDER
            else -> LEGACY_ORDER
        }

        val compatible = order.filter {
            it in available && supports(it, mcVersion) &&
                    supportsDevice(it, deviceGlesVersion) && supportsVulkan(it, vulkanAvailable)
        }
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

        // A renderer outside the preferred order (for example, a plugin) may still be a
        // declared version/device-compatible choice. Never fall through to an incompatible
        // renderer such as legacy GLES2 GL4ES on a core-profile Minecraft version.
        val anyCompatible = available.firstOrNull {
            supports(it, mcVersion) && supportsDevice(it, deviceGlesVersion) &&
                    supportsVulkan(it, vulkanAvailable)
        } ?: return Choice(
            "",
            "no renderer compatible with ${describe(mcVersion)} is available for this device",
            automatic = true
        )

        return Choice(
            anyCompatible,
            "no preferred wrapper is compatible with ${describe(mcVersion)}, using ${nameOf(anyCompatible)}",
            automatic = true
        )
    }

    private fun usesCoreProfile(mcVersion: String): Boolean =
        mcVersion.isNotBlank() && !mcVersion.isLowerVer(CORE_PROFILE_VERSION)

    private fun usesSdlGraphicsBackend(mcVersion: String): Boolean =
        mcVersion.isNotBlank() &&
                !normalizeMcVersion(mcVersion).isLowerVer(VulkanRequirements.MIN_MC_VERSION)

    private fun minimumGlesVersion(identifier: String): Int? {
        return when (identifier) {
            GL4ES -> 2
            MOBILEGLUES, NG_GL4ES, LTW, VIRGL, ZINK, FREEDRENO, PANFROST -> 3
            else -> null
        }
    }

    private fun supportsDevice(identifier: String, deviceGlesVersion: Int?): Boolean {
        if (deviceGlesVersion == null || deviceGlesVersion <= 0) return true
        val minimum = minimumGlesVersion(identifier) ?: return true
        return deviceGlesVersion >= minimum
    }

    private fun supportsVulkan(
        identifier: String,
        vulkanAvailable: Boolean?,
        allowUnknown: Boolean = false,
    ): Boolean {
        val renderer = Renderers.getRenderers().firstOrNull { it.getUniqueIdentifier() == identifier }
        val requiresVulkan = renderer?.getRendererId()?.contains("vulkan") == true ||
                renderer?.getRendererId()?.contains("zink") == true
        if (!requiresVulkan) return true
        return when (vulkanAvailable) {
            true -> true
            false -> false
            null -> allowUnknown
        }
    }

    /**
     * Whether [identifier] declares support for [mcVersion].
     *
     * An identifier with no built-in metadata belongs to a renderer plugin, which may declare
     * any version range it likes; it is treated as compatible rather than blocked.
     */
    private fun supports(identifier: String, mcVersion: String): Boolean {
        if (mcVersion.isBlank()) return true
        // LTW's library exports only EGL interception hooks, not the full set of EGL symbols
        // SDL loads on the Minecraft 26.2+ graphics path. This also upgrades an older persisted
        // LTW selection to a real SDL-compatible OpenGL renderer for affected instances.
        if (identifier == LTW && usesSdlGraphicsBackend(mcVersion)) return false
        val renderer = Renderers.getRenderers().firstOrNull { it.getUniqueIdentifier() == identifier }
            ?: return true
        renderer.getMinMCVersion()?.let { min -> if (mcVersion.isLowerVer(min)) return false }
        renderer.getMaxMCVersion()?.let { max -> if (mcVersion.isBiggerVer(max)) return false }
        return true
    }

    private fun nameOf(identifier: String): String =
        Renderers.getRenderers().firstOrNull { it.getUniqueIdentifier() == identifier }
            ?.getRendererName()
            ?: identifier

    private fun describe(mcVersion: String): String =
        if (mcVersion.isBlank()) "an unknown Minecraft version" else "Minecraft $mcVersion"
}
