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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.version.installed.utils

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class VersionCompareTest {

    @Test
    fun testCompareLegacyReleases() {
        assertTrue("1.8".isBiggerVer("1.7"))
        assertTrue("1.0".isBiggerOrEqualVer("1.0"))
        assertTrue("1.16".isLowerVer("1.17"))
    }

    @Test
    fun testCompareLegacySnapshots() {
        assertTrue("20w14b".isBiggerVer("someRelease"))
        assertTrue("20w14a".isLowerVer("someRelease"))
    }

    @Test
    fun testCompareNewReleases() {
        assertTrue("26.3".isBiggerOrEqualVer("26.2"))
        assertTrue("26.1".isLowerOrEqualVer("26.1"))
        assertFalse("26.1".isBiggerVer("26.2"))
    }

    @Test
    fun testCompareNewSnapshots() {
        assertTrue("25.4-snapshot-2".isBiggerVer("25.4"))
        assertTrue("25.4-snapshot-1".isLowerVer("25.4"))
    }


    @Test
    fun testNewVsLegacy() {
        assertTrue("26.1".isBiggerVer("1.21.11"))
        assertTrue("26.2-snapshot-1".isBiggerVer("1.21.11"))
        assertTrue("25.4-snapshot-1".isBiggerVer("23w40a"))
        assertFalse("20w14a".isBiggerVer("1.21.11"))
    }

    @Test
    fun testVersionEquality() {
        assertTrue("1.21.5".isBiggerOrEqualVer("1.21.5"))
        assertTrue("20w30a".isLowerOrEqualVer("1.21.5"))
    }

}