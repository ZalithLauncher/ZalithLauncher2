package com.movtery.zalithlauncher.setting.enums

import com.movtery.zalithlauncher.R

/**
 * Tingkat peredam bising (Noise Cancelling) untuk mikrofon saat merekam layar
 */
enum class NoiseCancelLevel(val nameRes: Int) {
    /** Tanpa pemrosesan */
    OFF(R.string.record_noise_cancel_off),

    /** Peredam bising bawaan perangkat + filter suara rendah + gerbang bising ringan */
    STANDARD(R.string.record_noise_cancel_standard),

    /** Semua di atas, dengan gerbang bising yang lebih ketat agar suara sekitar tidak terdengar */
    STRONG(R.string.record_noise_cancel_strong)
}
