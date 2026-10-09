/*
 * Zalith Launcher 2
 * Copyright (C) 2026 Mirai Launcher contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.game.renderer

import com.movtery.zalithlauncher.game.renderer.renderers.LTWRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LTWRendererTest {
    @Test
    fun rendererIsRegisteredAsBuiltIn() {
        Renderers.init(reset = true)

        assertTrue(Renderers.getRenderers().contains(LTWRenderer))
    }

    @Test
    fun rendererUsesItsBundledOpenGlAndEglLibrary() {
        assertEquals("opengles3_ltw", LTWRenderer.getRendererId())
        assertEquals("libltw.so", LTWRenderer.getRendererLibrary())
        assertEquals("libltw.so", LTWRenderer.getRendererEGL())
        assertEquals("3", LTWRenderer.getRendererEnv().value["LIBGL_ES"])
    }

    @Test
    fun rendererAdvertisesMinecraftVersionsStartingAtOneEighteen() {
        assertEquals("1.18", LTWRenderer.getMinMCVersion())
        assertEquals(null, LTWRenderer.getMaxMCVersion())
    }
}
