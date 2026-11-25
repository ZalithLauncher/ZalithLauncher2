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

package com.movtery.zalithlauncher.game.download.addon.platform.modrinth

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class ModrinthSearcherTest {

    @Test
    fun versionsParametersUseDocumentedEncoding() {
        val parameters = modrinthVersionsParameters(
            loaders = listOf("fabric", "quilt"),
            gameVersions = listOf("1.20.1", "1.21.1"),
            featured = true
        )

        assertEquals("[\"fabric\",\"quilt\"]", parameters["loaders"])
        assertEquals("[\"1.20.1\",\"1.21.1\"]", parameters["game_versions"])
        assertEquals("true", parameters["featured"])
        assertEquals("false", parameters["include_changelog"])
    }

    @Test
    fun versionsParametersOmitEmptyFilters() {
        val parameters = modrinthVersionsParameters(
            loaders = emptyList(),
            gameVersions = emptyList(),
            includeChangelog = true
        )

        assertNull(parameters["loaders"])
        assertNull(parameters["game_versions"])
        assertNull(parameters["featured"])
        assertEquals("true", parameters["include_changelog"])
    }
}
