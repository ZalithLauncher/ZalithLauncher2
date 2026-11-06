package com.movtery.zalithlauncher.setting.enums

import com.movtery.zalithlauncher.R

/**
 * Kualitas video hasil rekam layar
 *
 * @param maxShortSide sisi terpendek maksimum hasil rekaman (piksel)
 * @param bitrate bitrate video (bit per detik)
 */
enum class RecordQuality(
    val nameRes: Int,
    val maxShortSide: Int,
    val bitrate: Int
) {
    LOW(R.string.record_quality_low, 720, 4_000_000),
    MEDIUM(R.string.record_quality_medium, 1080, 8_000_000),
    HIGH(R.string.record_quality_high, 1440, 14_000_000)
}
