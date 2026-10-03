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

    /** The same set as [all], but with the LTW Legacy wrapper loaded as well. */
    private val allWithLtwLegacy = all + RendererPicker.LTW_LEGACY

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

    @Test
    fun legacyVersionsPreferLtwLegacy() {
        val choice = RendererPicker.pick("1.12.2", "", allWithLtwLegacy)
        assertEquals(RendererPicker.LTW_LEGACY, choice.identifier)
        assertEquals(true, choice.automatic)
    }

    /** 1.16.5 is the last release on the legacy OpenGL pipeline, so it must not move to LTW. */
    @Test
    fun lastLegacyReleaseStillPicksTheLegacyWrapper() {
        val choice = RendererPicker.pick("1.16.5", "", allWithLtwLegacy)
        assertEquals(RendererPicker.LTW_LEGACY, choice.identifier)
    }

    /** 1.17 is where Minecraft requires the OpenGL 3.2 core profile. */
    @Test
    fun coreProfileVersionsPickLtw() {
        assertEquals(
            RendererPicker.LTW,
            RendererPicker.pick("1.17", "", allWithLtwLegacy).identifier,
        )
    }

    @Test
    fun legacyStillFallsBackToGl4esWhenTheLegacyWrapperIsAbsent() {
        val choice = RendererPicker.pick("1.12.2", "", all)
        assertEquals(RendererPicker.GL4ES, choice.identifier)
    }

    /** An override for a version the wrapper supports must survive untouched. */
    @Test
    fun supportedOverrideWinsOnLegacyVersions() {
        val choice = RendererPicker.pick("1.8.9", RendererPicker.VIRGL, allWithLtwLegacy)
        assertEquals(RendererPicker.VIRGL, choice.identifier)
        assertEquals(false, choice.automatic)
    }

    /**
     * GL4ES declares 1.21.4 as its ceiling. Selecting it anyway would pass the picker and then
     * fail the launcher's own support check, so the picker has to fall back here.
     */
    @Test
    fun overrideBeyondItsVersionRangeIsRejected() {
        val choice = RendererPicker.pick("1.21.5", RendererPicker.GL4ES, all)

        assertEquals(RendererPicker.LTW, choice.identifier)
        assertEquals(true, choice.automatic)
    }

    @Test
    fun resolveLeavesUnknownVersionsToTheCaller() {
        assertEquals(null, RendererPicker.resolve("", RendererPicker.GL4ES))
    }
}
