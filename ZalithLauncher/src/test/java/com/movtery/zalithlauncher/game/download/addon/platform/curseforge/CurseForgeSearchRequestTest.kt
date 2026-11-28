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

package com.movtery.zalithlauncher.game.download.addon.platform.curseforge

import com.movtery.zalithlauncher.game.download.addon.platform.curseforge.models.CurseForgeModLoader
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class CurseForgeSearchRequestTest {

    @Test
    fun modLoaderCanFilterWithoutGameVersion() {
        val parameters = CurseForgeSearchRequest(
            gameVersion = null,
            modLoader = CurseForgeModLoader.FABRIC
        ).toParameters()

        assertEquals("[\"Fabric\"]", parameters["modLoaderTypes"])
        assertNull(parameters["modLoaderType"])
    }

    @Test
    fun rejectsPageSizeAboveLimit() {
        assertThrows(IllegalArgumentException::class.java) {
            CurseForgeSearchRequest(pageSize = CURSEFORGE_MAX_PAGE_SIZE + 1).toParameters()
        }
    }

    @Test
    fun rejectsPaginationBeyondLimit() {
        assertThrows(IllegalArgumentException::class.java) {
            CurseForgeSearchRequest(
                index = CURSEFORGE_MAX_PAGINATION_RESULTS - 1,
                pageSize = 2
            ).toParameters()
        }
    }
}
