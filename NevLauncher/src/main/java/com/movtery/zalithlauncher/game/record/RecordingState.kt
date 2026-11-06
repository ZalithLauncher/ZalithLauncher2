package com.movtery.zalithlauncher.game.record

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class RecordPhase {
    /** Tidak sedang merekam */
    IDLE,

    /** Sedang menyiapkan perekam */
    STARTING,

    /** Sedang merekam */
    RECORDING,

    /** Rekaman dijeda */
    PAUSED,

    /** Sedang menutup berkas dan menyimpan ke galeri */
    SAVING
}

/**
 * @property accumulatedMs lama rekaman yang sudah terkumpul (tidak termasuk bagian yang dijeda)
 * @property segmentStartRealtime waktu [SystemClock.elapsedRealtime] saat segmen rekaman yang sedang berjalan dimulai
 */
data class RecordSnapshot(
    val phase: RecordPhase = RecordPhase.IDLE,
    val accumulatedMs: Long = 0L,
    val segmentStartRealtime: Long = 0L
) {
    val isActive: Boolean
        get() = phase != RecordPhase.IDLE

    /**
     * Lama rekaman saat ini (milidetik)
     */
    fun elapsedMs(now: Long = SystemClock.elapsedRealtime()): Long {
        return if (phase == RecordPhase.RECORDING) {
            accumulatedMs + (now - segmentStartRealtime).coerceAtLeast(0L)
        } else accumulatedMs
    }
}

/**
 * Pesan singkat yang ditampilkan ke pengguna (toast) setelah suatu kejadian
 *
 * @property messageRes resource string, boleh memakai satu argumen %s
 */
data class RecordEvent(
    val messageRes: Int,
    val arg: String? = null
)

/**
 * Status rekaman layar yang dipakai bersama oleh layanan dan UI (berada di proses game)
 */
object RecordingState {
    private val _snapshot = MutableStateFlow(RecordSnapshot())
    val snapshot: StateFlow<RecordSnapshot> = _snapshot.asStateFlow()

    private val _events = MutableSharedFlow<RecordEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<RecordEvent> = _events.asSharedFlow()

    internal fun update(block: (RecordSnapshot) -> RecordSnapshot) {
        _snapshot.update(block)
    }

    internal fun emit(event: RecordEvent) {
        _events.tryEmit(event)
    }
}
