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

package com.movtery.zalithlauncher.terracotta

import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.movtery.zalithlauncher.coroutine.MutableTransitionStateFlow
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.burningtnt.terracotta.TerracottaAndroidAPI
import java.io.IOException
import java.io.StringWriter
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "Terracotta"

/**
 * [Reference FCL](https://github.com/FCL-Team/FoldCraftLauncher/blob/52f0542/FCL/src/main/java/com/tungsten/fcl/terracotta/Terracotta.java)
 */
object Terracotta {
    const val TERRACOTTA_USER_NOTICE_VERSION = 1

    enum class Mode {
        /** 房主模式 */
        Host,
        /** 房客模式 */
        Guest
    }

    var initialized = false
        private set

    private var eventViewModel: EventViewModel? = null

    private var notificationJob: Job? = null
    private var metadata: TerracottaAndroidAPI.Metadata? = null

    var mode: Mode? = null
        private set

    private val _state = MutableTransitionStateFlow<TerracottaState.Ready?>(null)
    val state = _state.stateFlow
    val stateChanges = _state.changes

    private val stateRef = AtomicReference<TerracottaState.Ready?>(null)

    fun initialize(
        lifecycleScope: CoroutineScope,
        eventViewModel: EventViewModel
    ) {
        if (initialized) return
        this.eventViewModel = eventViewModel

        metadata = TerracottaAndroidAPI.initialize(
            PathManager.DIR_TERRACOTTA.absolutePath,
            PathManager.FILE_TERRACOTTA_LOG.absolutePath
        ) {
            eventViewModel.sendEvent(EventViewModel.Event.Terracotta.RequestVPN)
            lifecycleScope.startNotificationJob()
        }

        lifecycleScope.launch(Dispatchers.Default) {
            while (true) {
                val current = stateRef.get()
                val index = current?.index ?: -1

                val stateJson = TerracottaAndroidAPI.getState()
                val obj = TerracottaState.TerracottaStateGson.fromJson<TerracottaState.Ready>(
                    stateJson,
                    object : TypeToken<TerracottaState.Ready>() {}.type
                ) ?: throw JsonParseException("Json object cannot be null.")

                if (obj.index > index) {
                    compareAndSet(current, obj)
                }

                delay(1L.milliseconds)
            }
        }

        initialized = true
    }

    fun setWaiting(manual: Boolean) {
        if (!initialized) return

        stopNotificationJob()
        if (manual) this.eventViewModel?.sendEvent(EventViewModel.Event.Terracotta.StopVPN)
        TerracottaAndroidAPI.setWaiting()
    }

    fun setScanning(room: String?, player: String?, extraNodes: List<String>?) {
        checkInitialized()
        if (_state.value !is TerracottaState.Waiting)
            throw Exception("reset state to waiting first!")

        mode = Mode.Host
        TerracottaAndroidAPI.setScanning(room, player, extraNodes)
    }

    fun setGuesting(room: String, player: String?, extraNodes: List<String>?): Boolean {
        checkInitialized()
        if (_state.value !is TerracottaState.Waiting)
            throw Exception("reset state to waiting first!")

        mode = Mode.Guest
        return TerracottaAndroidAPI.setGuesting(room, player, extraNodes)
    }

    fun parseRoomCode(room: String?): TerracottaAndroidAPI.RoomType? {
        if (!initialized || room == null) return null
        return TerracottaAndroidAPI.parseRoomCode(room)
    }

    fun getMetadata(): TerracottaAndroidAPI.Metadata =
        metadata ?: TerracottaAndroidAPI.Metadata("unknown", 0, "unknown")

    fun collectLogs(): String? {
        if (!initialized) return null
        return try {
            TerracottaAndroidAPI.collectLogs().use { reader ->
                val writer = StringWriter()
                val buf = CharArray(4096)
                var n: Int
                while (reader.read(buf).also { n = it } != -1) {
                    writer.write(buf, 0, n)
                }
                writer.toString()
            }
        } catch (e: IOException) {
            e.message?.let { Logger.warning(TAG, it) }
            "Failed to collect logs: ${e.message}"
        }
    }

    @Deprecated("This API is exposed for debug purpose.")
    fun testNativePanic() {
        if (!initialized) return
        TerracottaAndroidAPI.panic()
    }

    private fun checkInitialized() {
        if (!initialized) throw Exception("initialize Terracotta first!")
    }

    private fun compareAndSet(previous: TerracottaState.Ready?, next: TerracottaState.Ready) {
        if (stateRef.compareAndSet(previous, next)) {
            _state.set(next)
        }
    }

    private fun CoroutineScope.startNotificationJob() {
        notificationJob?.cancel()
        notificationJob = launch(Dispatchers.Default) {
            state.collect { state ->
                if (state != null && state !is TerracottaState.Waiting) {
                    val stringRes = state.localStringRes()
                    eventViewModel?.sendEvent(EventViewModel.Event.Terracotta.VPNUpdateState(stringRes))
                }
            }
        }
    }

    private fun stopNotificationJob() {
        notificationJob?.cancel()
        notificationJob = null
    }
}
