/*
 * Zalith Launcher 2
 * Copyright (C) 2026 Aerix Launcher contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.game.renderer

import com.movtery.zalithlauncher.game.renderer.renderers.LTWLegacyRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LTWLegacyRendererTest {
    @Test
    fun rendererIsRegisteredAsBuiltIn() {
        Renderers.init(reset = true)

        assertTrue(Renderers.getRenderers().contains(LTWLegacyRenderer))
    }

    /**
     * The native bridge sends anything starting with `opengles` down the GLES-backed context
     * path. The exact string `opengles3` is taken by Krypton Wrapper and also advertises
     * OpenGL 4.0 to the game, so LTW Legacy must not claim it.
     */
    @Test
    fun rendererIdRoutesToTheGlesBridgeWithoutClaimingKrypton() {
        val id = LTWLegacyRenderer.getRendererId()

        assertTrue(id.startsWith("opengles"))
        assertNotEquals("opengles3", id)
        assertFalse(id.contains("desktopgl"))
    }

    @Test
    fun rendererUsesItsOwnLibraryAndLeavesEglToThePlatform() {
        assertEquals("libltwlegacy.so", LTWLegacyRenderer.getRendererLibrary())
        // No EGL override: unlike LTW, this library is a GL translation layer only. Returning
        // a name here would point POJAVEXEC_EGL and SDL_EGL_LIBRARY at it as well.
        assertEquals(null, LTWLegacyRenderer.getRendererEGL())
    }

    /**
     * Identifiers are how the launcher stores and reloads a chosen renderer. A duplicate
     * would make [Renderers.addRenderer] reject one of them, so every built-in has to be
     * individually addressable.
     */
    @Test
    fun rendererIdentifiersAreUniqueAcrossEveryBuiltIn() {
        val identifiers = Renderers.BUILT_IN.map { it.getUniqueIdentifier() }

        assertEquals(1, identifiers.count { it == LTWLegacyRenderer.getUniqueIdentifier() })
        assertEquals(identifiers.size, identifiers.toSet().size)
    }

    @Test
    fun rendererCoversTheLegacyOpenGlEra() {
        assertEquals("1.8", LTWLegacyRenderer.getMinMCVersion())
        // 1.16.5 is still the legacy OpenGL pipeline; 1.17 is where the core profile starts.
        assertEquals("1.16.5", LTWLegacyRenderer.getMaxMCVersion())
    }

    @Test
    fun rendererPinsTheBackendAndVersionItReports() {
        val env = LTWLegacyRenderer.getRendererEnv().value

        assertEquals("2", env["LIBGL_ES"])
        assertEquals("21", env["LIBGL_GL"])
        assertEquals("1", env["LIBGL_NOBANNER"])
    }
}
