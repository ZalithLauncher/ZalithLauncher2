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

import com.movtery.zalithlauncher.ui.AndroidStringText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PluginTrustDialogState {

    enum class Severity { INFO, WARNING, ERROR }

    enum class DialogAction { TRUST, CANCEL }

    sealed class DialogRequest {
        abstract val title: AndroidStringText
        abstract val summary: AndroidStringText?
        abstract val message: AndroidStringText?

        /**
         * Labelled rows rather than one text blob. The launcher owns every heading and every value
         * is bounded, so no plugin-supplied string can grow the dialog or forge its structure.
         */
        abstract val sections: List<TrustSection>
        abstract val deferred: CompletableDeferred<DialogAction>

        data class AuthorTrust(
            override val title: AndroidStringText,
            override val summary: AndroidStringText?,
            override val message: AndroidStringText?,
            override val sections: List<TrustSection>,
            val severity: Severity,
            override val deferred: CompletableDeferred<DialogAction> = CompletableDeferred()
        ) : DialogRequest()

        data class KeyTrust(
            override val title: AndroidStringText,
            override val summary: AndroidStringText?,
            override val message: AndroidStringText?,
            override val sections: List<TrustSection>,
            val cooldownSeconds: Int,
            /**
             * Bumped on every handled configuration change so the countdown restarts.  A rotation
             * must not be a way to reach the enabled action button without reading the notice.
             */
            val cooldownGeneration: Int = 0,
            override val deferred: CompletableDeferred<DialogAction> = CompletableDeferred()
        ) : DialogRequest()

        data class Error(
            override val title: AndroidStringText,
            override val summary: AndroidStringText?,
            override val message: AndroidStringText?,
            override val sections: List<TrustSection>,
            override val deferred: CompletableDeferred<DialogAction> = CompletableDeferred()
        ) : DialogRequest()
    }

    private val _currentRequest = MutableStateFlow<DialogRequest?>(null)
    val currentRequest: StateFlow<DialogRequest?> = _currentRequest.asStateFlow()

    private fun setCurrentRequest(request: DialogRequest?) {
        _currentRequest.value = request
    }

    suspend fun showAuthorTrust(
        title: AndroidStringText,
        summary: AndroidStringText?,
        message: AndroidStringText?,
        sections: List<TrustSection>,
        severity: Severity
    ): DialogAction {
        val request = DialogRequest.AuthorTrust(
            title = title,
            summary = summary,
            message = message,
            sections = sections,
            severity = severity
        )
        setCurrentRequest(request)
        return try {
            request.deferred.await()
        } finally {
            setCurrentRequest(null)
        }
    }

    suspend fun showKeyTrust(
        title: AndroidStringText,
        summary: AndroidStringText?,
        message: AndroidStringText?,
        sections: List<TrustSection>,
        cooldownSeconds: Int
    ): DialogAction {
        val request = DialogRequest.KeyTrust(
            title = title,
            summary = summary,
            message = message,
            sections = sections,
            cooldownSeconds = cooldownSeconds
        )
        setCurrentRequest(request)
        return try {
            request.deferred.await()
        } finally {
            setCurrentRequest(null)
        }
    }

    suspend fun showError(
        title: AndroidStringText,
        summary: AndroidStringText?,
        message: AndroidStringText?,
        sections: List<TrustSection>
    ): DialogAction {
        val request = DialogRequest.Error(
            title = title,
            summary = summary,
            message = message,
            sections = sections
        )
        setCurrentRequest(request)
        return try {
            request.deferred.await()
        } finally {
            setCurrentRequest(null)
        }
    }

    fun dismissCurrent() {
        _currentRequest.value?.let { req ->
            if (!req.deferred.isCompleted) {
                req.deferred.complete(DialogAction.CANCEL)
            }
        }
        setCurrentRequest(null)
    }

    /**
     * Restarts the unknown-plugin countdown after a handled configuration change.
     *
     * Only the cooldown is reset; the pending request itself is left alone. Cancelling it here would
     * turn any rotation into an aborted launch, and re-showing it would let a rotation land on an
     * already-enabled action button.
     */
    fun restartCooldown() {
        val request = _currentRequest.value
        if (request is DialogRequest.KeyTrust) {
            _currentRequest.value = request.copy(
                cooldownGeneration = request.cooldownGeneration + 1
            )
        }
    }
}
