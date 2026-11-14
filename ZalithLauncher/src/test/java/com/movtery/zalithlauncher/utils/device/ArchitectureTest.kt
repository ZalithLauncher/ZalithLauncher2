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

package com.movtery.zalithlauncher.utils.device

import com.movtery.zalithlauncher.utils.device.Architecture.ARCH_ARM
import com.movtery.zalithlauncher.utils.device.Architecture.ARCH_ARM64
import com.movtery.zalithlauncher.utils.device.Architecture.ARCH_X86
import com.movtery.zalithlauncher.utils.device.Architecture.ARCH_X86_64
import com.movtery.zalithlauncher.utils.device.Architecture.UNSUPPORTED_ARCH
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ArchitectureTest {

    @Test
    fun testUniversalBuildAlwaysCompatible() {
        assertTrue(Architecture.isBuildArchCompatible("all", ARCH_ARM64))
        assertTrue(Architecture.isBuildArchCompatible("all", ARCH_ARM))
        assertTrue(Architecture.isBuildArchCompatible("all", ARCH_X86))
        assertTrue(Architecture.isBuildArchCompatible("all", ARCH_X86_64))
        assertTrue(Architecture.isBuildArchCompatible("all", UNSUPPORTED_ARCH))
    }

    @Test
    fun testMatchingArchitectures() {
        assertTrue(Architecture.isBuildArchCompatible("arm64", ARCH_ARM64))
        assertTrue(Architecture.isBuildArchCompatible("arm", ARCH_ARM))
        assertTrue(Architecture.isBuildArchCompatible("x86", ARCH_X86))
        assertTrue(Architecture.isBuildArchCompatible("x86_64", ARCH_X86_64))
    }

    @Test
    fun testMismatchedArchitectures() {
        //arm64 设备装 arm 包等跨架构场景
        assertFalse(Architecture.isBuildArchCompatible("arm64", ARCH_ARM))
        assertFalse(Architecture.isBuildArchCompatible("arm", ARCH_ARM64))
        assertFalse(Architecture.isBuildArchCompatible("x86", ARCH_X86_64))
        assertFalse(Architecture.isBuildArchCompatible("x86_64", ARCH_ARM64))
    }

    @Test
    fun testUnsupportedDeviceNeverMatches() {
        assertFalse(Architecture.isBuildArchCompatible("arm64", UNSUPPORTED_ARCH))
        assertFalse(Architecture.isBuildArchCompatible("arm", UNSUPPORTED_ARCH))
        assertFalse(Architecture.isBuildArchCompatible("x86", UNSUPPORTED_ARCH))
        assertFalse(Architecture.isBuildArchCompatible("x86_64", UNSUPPORTED_ARCH))
    }
}
