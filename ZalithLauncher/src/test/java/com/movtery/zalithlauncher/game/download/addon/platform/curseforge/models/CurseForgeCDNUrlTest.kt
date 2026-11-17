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

package com.movtery.zalithlauncher.game.download.addon.platform.curseforge.models

import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.net.URLDecoder

class CurseForgeCDNUrlTest {

    @Test
    fun testEncodeSpaces() {
        assertEquals(
            "https://edge.forgecdn.net/files/9097/219/Mechanical%20Horizons%202026.1.0.zip",
            curseForgeCDNUrl(fileId = 9097219, fileName = "Mechanical Horizons 2026.1.0.zip")
        )
    }

    @Test
    fun testSpecialCharactersRoundTrip() {
        val fileName = "模组 v1.0 + 补丁 (50%off) [测试].jar"
        val url = curseForgeCDNUrl(fileId = 1, fileName = fileName)
        assertEquals(-1, url.indexOf(' '))
        assertEquals(
            fileName,
            URLDecoder.decode(url.substringAfterLast('/'), "UTF-8")
        )
    }
}
