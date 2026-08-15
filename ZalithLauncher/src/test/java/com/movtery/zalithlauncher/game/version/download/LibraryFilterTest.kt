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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.movtery.zalithlauncher.game.version.download

import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class LibraryFilterTest {

    @Test
    fun lwjglCoreAndSdlClassesAreKeptForSdlVersions() {
        assertFalse(library("org.lwjgl:lwjgl:3.4.2").filterLibrary(allowLwjglSdlClasses = true))
        assertFalse(library("org.lwjgl:lwjgl-sdl:3.4.2").filterLibrary(allowLwjglSdlClasses = true))
    }

    @Test
    fun otherLwjglModulesRemainFilteredForSdlVersions() {
        assertTrue(library("org.lwjgl:lwjgl-opengl:3.4.2").filterLibrary(allowLwjglSdlClasses = true))
        assertTrue(library("org.lwjgl:lwjgl-openal:3.4.2").filterLibrary(allowLwjglSdlClasses = true))
    }

    @Test
    fun lwjglDesktopNativesRemainFiltered() {
        assertTrue(library("org.lwjgl:lwjgl-sdl:3.4.2:natives-linux").filterLibrary(allowLwjglSdlClasses = true))
        assertTrue(library("org.lwjgl:lwjgl-sdl:3.4.2:natives-windows-arm64").filterLibrary(allowLwjglSdlClasses = true))
    }

    @Test
    fun bundledLwjglRemainsTheDefault() {
        assertTrue(library("org.lwjgl:lwjgl:3.4.2").filterLibrary())
        assertTrue(library("org.lwjgl:lwjgl-opengl:3.4.2").filterLibrary())
    }

    @Test
    fun unrelatedLibrariesAreKept() {
        assertFalse(library("com.example:org.lwjgl.helper:1.0.0").filterLibrary())
    }

    @Test
    fun sdlBindingSelectsCurrentLwjglClasses() {
        assertTrue(
            listOf(
                library("org.lwjgl:lwjgl:3.4.2"),
                library("org.lwjgl:lwjgl-sdl:3.4.2")
            ).usesLwjglSdl()
        )
        assertFalse(listOf(library("org.lwjgl:lwjgl-glfw:3.3.6")).usesLwjglSdl())
    }

    private fun library(coordinates: String) = GameManifest.Library().apply {
        name = coordinates
    }
}
