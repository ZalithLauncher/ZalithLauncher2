package com.movtery.zalithlauncher.game.stats

import android.os.SystemClock
import com.movtery.zalithlauncher.setting.launcherMMKV
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Statistik waktu bermain per hari.
 *
 * Disimpan di MMKV mode multi-proses, jadi bisa ditulis dari proses game (`:game`)
 * dan dibaca dari proses launcher utama.
 *
 * Waktu dihitung selama layar game berada di depan (resume sampai pause).
 * Penghitungan ditulis berkala lewat [tick], sehingga kalau proses game dimatikan
 * paksa, hanya detik terakhir sejak tick sebelumnya yang hilang.
 */
object PlayTimeStats {
    private const val KEY_PREFIX = "play_time_stats_"
    /** Simpan data paling lama 120 hari agar tidak menumpuk. */
    private const val KEEP_DAYS = 120L

    private var lastTickRealtime: Long = -1L

    /** Mulai menghitung (dipanggil saat layar game tampil). */
    @Synchronized
    fun start() {
        lastTickRealtime = SystemClock.elapsedRealtime()
    }

    /** Tambahkan waktu sejak tick terakhir ke hari ini. */
    @Synchronized
    fun tick() {
        if (lastTickRealtime < 0L) return
        val now = SystemClock.elapsedRealtime()
        val deltaSeconds = (now - lastTickRealtime) / 1000L
        if (deltaSeconds <= 0L) return
        //hanya majukan penanda sebesar detik yang sudah dihitung, sisa milidetik tidak hilang
        lastTickRealtime += deltaSeconds * 1000L
        add(LocalDate.now(), deltaSeconds)
    }

    /** Berhenti menghitung (dipanggil saat layar game tidak lagi tampil). */
    @Synchronized
    fun stop() {
        tick()
        lastTickRealtime = -1L
    }

    private fun add(date: LocalDate, seconds: Long) {
        val mmkv = launcherMMKV()
        val key = KEY_PREFIX + date
        mmkv.encode(key, mmkv.decodeLong(key, 0L) + seconds)
    }

    /** Detik bermain pada [date]. */
    fun secondsOf(date: LocalDate): Long =
        launcherMMKV().decodeLong(KEY_PREFIX + date, 0L)

    /**
     * Waktu bermain Senin sampai Minggu pada pekan yang memuat [today].
     * Selalu berisi 7 data, urut Senin (indeks 0) sampai Minggu (indeks 6).
     */
    fun weekOf(today: LocalDate = LocalDate.now()): List<DayPlayTime> {
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return (0L until 7L).map { offset ->
            val date = monday.plusDays(offset)
            DayPlayTime(
                date = date,
                seconds = secondsOf(date),
                isToday = date == today,
                isFuture = date.isAfter(today)
            )
        }
    }

    /** Hapus data lama. Aman dipanggil kapan saja. */
    fun pruneOld(today: LocalDate = LocalDate.now()) {
        val mmkv = launcherMMKV()
        val limit = today.minusDays(KEEP_DAYS)
        mmkv.allKeys()?.forEach { key ->
            if (!key.startsWith(KEY_PREFIX)) return@forEach
            val date = runCatching { LocalDate.parse(key.removePrefix(KEY_PREFIX)) }.getOrNull()
            if (date != null && date.isBefore(limit)) mmkv.removeValueForKey(key)
        }
    }
}

data class DayPlayTime(
    val date: LocalDate,
    val seconds: Long,
    val isToday: Boolean,
    val isFuture: Boolean
)
