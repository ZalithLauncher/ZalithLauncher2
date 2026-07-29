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

import android.content.Context
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.utils.logging.Logger
import com.vpl.verifiedpluginload.api.VerifiedPluginLoad
import com.vpl.verifiedpluginload.api.VerifiedPluginLoadBlocking
import com.vpl.verifiedpluginload.api.VerifiedPluginLoadConfig
import com.vpl.verifiedpluginload.api.VerifiedPluginLoadRegistry
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

object PluginTrustListSync {
    private const val TAG = "VerifiedPluginLoad"

    private val lock = Any()
    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "VerifiedPluginLoad-startup-refresh").apply { isDaemon = true }
    }

    @Volatile
    private var startupRefresh: CompletableDeferred<Unit>? = null

    fun start(context: Context) {
        synchronized(lock) {
            if (startupRefresh != null) return
            val vpl = configure(context.applicationContext)
            val deferred = CompletableDeferred<Unit>()
            startupRefresh = deferred
            executor.execute {
                try {
                    VerifiedPluginLoadBlocking.refreshTrustList(vpl)
                    Logger.info(TAG, "Startup trust-list refresh completed")
                    deferred.complete(Unit)
                } catch (e: Exception) {
                    Logger.warning(TAG, "Startup trust-list refresh failed", e)
                    deferred.complete(Unit)
                }
            }
        }
    }

    suspend fun awaitStartupRefresh(): Unit = withContext(Dispatchers.IO) {
        val deferred = synchronized(lock) { startupRefresh }
        deferred?.await()
    }

    private fun configure(context: Context): VerifiedPluginLoad {
        val prefixes = BuildConfig.VPL_TRUST_LIST_URL_PREFIXES
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val jsonSuffix = BuildConfig.VPL_TRUST_LIST_JSON_SUFFIX
            .takeIf { it.isNotEmpty() }

        val signatureSuffix = BuildConfig.VPL_TRUST_LIST_SIGNATURE_SUFFIX
            .takeIf { it.isNotEmpty() }

        val remoteConfigured = prefixes.isNotEmpty() && jsonSuffix != null && signatureSuffix != null

        return VerifiedPluginLoadRegistry.configure(
            context,
            VerifiedPluginLoadConfig(
                storageDirectory = File(context.filesDir, "verified-plugin-load"),
                trustListUrlPrefixes = if (remoteConfigured) prefixes else emptyList(),
                trustListJsonSuffix = if (remoteConfigured) jsonSuffix else null,
                trustListSignatureSuffix = if (remoteConfigured) signatureSuffix else null,
                networkTimeoutMillis = 5_000,
                maxTrustListBytes = 1_048_576
            )
        )
    }
}
