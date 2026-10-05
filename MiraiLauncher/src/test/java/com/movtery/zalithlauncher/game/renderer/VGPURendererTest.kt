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

import com.movtery.zalithlauncher.game.renderer.renderers.VGPU1368Renderer
import com.movtery.zalithlauncher.game.renderer.renderers.VGPURenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VGPURendererTest {
    @Test
    fun vgpuRenderersAreRegisteredAsBuiltIn() {
        Renderers.init(reset = true)

        assertTrue(Renderers.getRenderers().contains(VGPURenderer))
        assertTrue(Renderers.getRenderers().contains(VGPU1368Renderer))
    }

    @Test
    fun vgpuFastRendererMatchesPojavGlowWormConfiguration() {
        assertEquals("opengles2_vgpu", VGPURenderer.getRendererId())
        assertEquals("vgpu - (up to 1.16.5, fast)", VGPURenderer.getRendererName())
        assertEquals("libvgpu.so", VGPURenderer.getRendererLibrary())
        assertEquals(null, VGPURenderer.getRendererEGL())
        assertEquals("1.16.5", VGPURenderer.getMaxMCVersion())

        val env = VGPURenderer.getRendererEnv().value
        assertEquals("2", env["LIBGL_ES"])
        assertEquals("3", env["LIBGL_MIPMAP"])
        assertEquals("1", env["LIBGL_NOERROR"])
        assertEquals("1", env["LIBGL_NOINTOVLHACK"])
        assertEquals("1", env["LIBGL_NORMALIZE"])
    }

    @Test
    fun vgpu1368RendererMatchesPojavGlowWormConfiguration() {
        assertEquals("opengles2_vgpu_1", VGPU1368Renderer.getRendererId())
        assertEquals("VGPU 1.3.6β", VGPU1368Renderer.getRendererName())
        assertEquals("libvgpu_1368.so", VGPU1368Renderer.getRendererLibrary())
        assertEquals(null, VGPU1368Renderer.getRendererEGL())
        assertEquals("1.16.5", VGPU1368Renderer.getMaxMCVersion())

        val env = VGPU1368Renderer.getRendererEnv().value
        assertEquals("2", env["LIBGL_ES"])
        assertEquals("3", env["LIBGL_MIPMAP"])
        assertEquals("1", env["LIBGL_NOERROR"])
        assertEquals("1", env["LIBGL_NOINTOVLHACK"])
        assertEquals("1", env["LIBGL_NORMALIZE"])
    }
}
