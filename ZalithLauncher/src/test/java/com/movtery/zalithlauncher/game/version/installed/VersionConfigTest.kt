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

package com.movtery.zalithlauncher.game.version.installed

import com.movtery.zalithlauncher.utils.GSON
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.io.File

class VersionConfigTest {
    @Test
    fun testSfpewSettingsSurviveCopyAndSerialization() {
        val config = VersionConfig(
            filePath = File("test"),
            sfpewPluginLayer = SettingState.ENABLE,
            sfpewRelaxedContext = SettingState.DISABLE
        )

        val copy = config.copy()
        assertEquals(SettingState.ENABLE, copy.sfpewPluginLayer)
        assertEquals(SettingState.DISABLE, copy.sfpewRelaxedContext)

        val restored = GSON.fromJson(GSON.toJson(config), VersionConfig::class.java)
        assertEquals(SettingState.ENABLE, restored.sfpewPluginLayer)
        assertEquals(SettingState.DISABLE, restored.sfpewRelaxedContext)
    }

    @Test
    fun testLegacyConfigFollowsGlobalSfpewSettings() {
        val config = GSON.fromJson("{}", VersionConfig::class.java)

        assertEquals(SettingState.FOLLOW_GLOBAL, config.sfpewPluginLayer)
        assertEquals(SettingState.FOLLOW_GLOBAL, config.sfpewRelaxedContext)
    }
}
