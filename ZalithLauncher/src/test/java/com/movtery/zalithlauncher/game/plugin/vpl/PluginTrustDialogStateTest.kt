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

package com.movtery.zalithlauncher.game.plugin.vpl

import com.movtery.zalithlauncher.ui.androidText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PluginTrustDialogStateTest {

    @After
    fun tearDown() {
        // The state is a singleton; leaving a request behind would leak into the next test.
        PluginTrustDialogState.dismissCurrent()
    }

    private fun showKeyTrust(scope: kotlinx.coroutines.CoroutineScope) {
        scope.launch {
            PluginTrustDialogState.showKeyTrust(
                title = androidText("t"),
                summary = null,
                message = null,
                sections = emptyList(),
                cooldownSeconds = 6
            )
        }
    }

    @Test
    fun aHandledConfigurationChangeRestartsTheCooldownInsteadOfCancellingTheLaunch() = runTest {
        showKeyTrust(this)
        runCurrent()

        val before = PluginTrustDialogState.currentRequest.value
        assertTrue(before is PluginTrustDialogState.DialogRequest.KeyTrust)
        val original = before as PluginTrustDialogState.DialogRequest.KeyTrust
        assertEquals(0, original.cooldownGeneration)

        PluginTrustDialogState.restartCooldown()

        val after = PluginTrustDialogState.currentRequest.value
        assertNotNull("the request must survive a rotation", after)
        val restarted = after as PluginTrustDialogState.DialogRequest.KeyTrust
        assertEquals(1, restarted.cooldownGeneration)
        // Cancelling here would abort the launch on any rotation.
        assertFalse(restarted.deferred.isCompleted)
        // The same deferred has to carry over, or the awaiting gate would never be resumed.
        assertTrue(original.deferred === restarted.deferred)

        PluginTrustDialogState.dismissCurrent()
    }

    @Test
    fun restartingTheCooldownOnNothingIsHarmless() {
        assertNull(PluginTrustDialogState.currentRequest.value)
        PluginTrustDialogState.restartCooldown()
        assertNull(PluginTrustDialogState.currentRequest.value)
    }

    @Test
    fun dismissingResumesTheAwaitingGateWithACancel() = runTest {
        var action: PluginTrustDialogState.DialogAction? = null
        launch {
            action = PluginTrustDialogState.showKeyTrust(
                title = androidText("t"),
                summary = null,
                message = null,
                sections = emptyList(),
                cooldownSeconds = 6
            )
        }
        runCurrent()

        PluginTrustDialogState.dismissCurrent()
        runCurrent()

        assertEquals(PluginTrustDialogState.DialogAction.CANCEL, action)
        assertNull(PluginTrustDialogState.currentRequest.value)
    }
}
