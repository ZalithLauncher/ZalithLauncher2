/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.game.renderer.config

import com.movtery.zalithlauncher.game.renderer.LoadedRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.MobileGluesRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * SFPEW 兼容层开关驱动的 MobileGlues 版本支持边界
 */
class MobileGluesVersionSupportTest {
    @Test
    fun sfpewEnabledKeepsNoMinVersion() {
        val config = resolve(sfpew = true)

        assertNull(MobileGluesRenderer.getMinMCVersion(config))
        assertNull(MobileGluesRenderer.getDisplayMinMCVersion(config))
    }

    @Test
    fun sfpewDisabledLimitsMinVersionTo117() {
        val config = resolve(sfpew = false)

        // 1.16.5 及以下落在该边界之下，由版本比较逻辑判定
        assertEquals("1.17", MobileGluesRenderer.getMinMCVersion(config))
        assertEquals("1.17", MobileGluesRenderer.getDisplayMinMCVersion(config))
    }

    @Test
    fun maxVersionIsNotConfigurable() {
        val config = resolve(sfpew = false)

        assertNull(MobileGluesRenderer.getMaxMCVersion(config))
        assertNull(MobileGluesRenderer.getDisplayMaxMCVersion(config))
    }

    @Test
    fun loadedRendererFollowsResolvedConfig() {
        val enabled = LoadedRenderer(MobileGluesRenderer, resolve(sfpew = true))
        val disabled = LoadedRenderer(MobileGluesRenderer, resolve(sfpew = false))

        assertEquals(true, enabled.sfpewLayerEnabled)
        assertNull(enabled.getMinMCVersion())
        assertEquals("1.17", disabled.getMinMCVersion())
    }

    private fun resolve(sfpew: Boolean): ResolvedRendererConfig {
        val store = MemoryRendererConfigStore()
        store.putValue("renderer", "sfpewLayer", ConfigScalar.BooleanValue(sfpew))
        return resolveRendererConfig(
            rendererId = "renderer",
            spec = MobileGluesRenderer.configSpec,
            globalStore = store
        )
    }

    private class MemoryRendererConfigStore : RendererConfigStore {
        private val values = mutableMapOf<Pair<String, String>, ConfigScalar>()
        private val enabled = mutableMapOf<Pair<String, String>, Boolean>()

        override fun getValue(rendererId: String, entryId: String): ConfigScalar? =
            values[rendererId to entryId]

        override fun putValue(rendererId: String, entryId: String, value: ConfigScalar) {
            values[rendererId to entryId] = value
        }

        override fun getEnabled(rendererId: String, entryId: String): Boolean? =
            enabled[rendererId to entryId]

        override fun putEnabled(rendererId: String, entryId: String, enabled: Boolean) {
            this.enabled[rendererId to entryId] = enabled
        }

        override fun removeEntry(rendererId: String, entryId: String) {
            values.remove(rendererId to entryId)
            enabled.remove(rendererId to entryId)
        }

        override fun removeRenderer(rendererId: String) {
            values.keys.removeAll { it.first == rendererId }
            enabled.keys.removeAll { it.first == rendererId }
        }

        override fun keys(rendererId: String): Set<String> =
            (values.keys + enabled.keys).filter { it.first == rendererId }.mapTo(mutableSetOf()) { it.second }
    }
}
