package com.movtery.zalithlauncher.game.renderer

import org.junit.Assert.assertEquals
import org.junit.Test

class RendererPickerTest {
    private val all = setOf(
        RendererPicker.GL4ES,
        RendererPicker.VIRGL,
        RendererPicker.LTW,
        RendererPicker.ZINK,
    )

    @Test
    fun legacyVersionsPreferGl4es() {
        val choice = RendererPicker.pick("1.12.2", "", all)
        assertEquals(RendererPicker.GL4ES, choice.identifier)
        assertEquals(true, choice.automatic)
    }

    @Test
    fun legacyFallsBackToVirgl() {
        val choice = RendererPicker.pick("1.16.4", "", setOf(RendererPicker.VIRGL))
        assertEquals(RendererPicker.VIRGL, choice.identifier)
    }

    @Test
    fun modernVersionsPreferLtw() {
        val choice = RendererPicker.pick("1.21.1", "", all)
        assertEquals(RendererPicker.LTW, choice.identifier)
    }

    @Test
    fun modernFallsBackToZink() {
        val choice = RendererPicker.pick("1.17.1", "", setOf(RendererPicker.ZINK))
        assertEquals(RendererPicker.ZINK, choice.identifier)
    }

    @Test
    fun instanceOverrideWins() {
        val choice = RendererPicker.pick("1.21", RendererPicker.GL4ES, all)
        assertEquals(RendererPicker.GL4ES, choice.identifier)
        assertEquals(false, choice.automatic)
    }

    @Test
    fun missingOverrideFallsBack() {
        val choice = RendererPicker.pick("1.8.9", "missing", all)
        assertEquals(RendererPicker.GL4ES, choice.identifier)
    }
}
