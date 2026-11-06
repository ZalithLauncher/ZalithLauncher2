package com.movtery.zalithlauncher.game.record

import com.movtery.zalithlauncher.setting.enums.NoiseCancelLevel
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Filter perangkat lunak untuk suara mikrofon (mono, 16-bit PCM):
 * 1. Filter high-pass untuk membuang dengung/getaran bersuara rendah
 * 2. Gerbang bising adaptif: mengikuti tingkat bising latar, dan menutup saat tidak ada suara bicara
 *    sehingga suara dari sekitar tidak ikut terekam
 *
 * Dipakai bersama NoiseSuppressor / AcousticEchoCanceler bawaan perangkat.
 * Kelas ini tidak aman dipakai dari beberapa thread sekaligus.
 */
class MicNoiseFilter(
    private val sampleRate: Int,
    private val level: NoiseCancelLevel
) {
    private val enabled = level != NoiseCancelLevel.OFF
    private val strong = level == NoiseCancelLevel.STRONG

    //High-pass orde 1
    private val hpCutoffHz = if (strong) 140.0 else 90.0
    private val hpAlpha: Float = run {
        val rc = 1.0 / (2.0 * Math.PI * hpCutoffHz)
        val dt = 1.0 / sampleRate
        (rc / (rc + dt)).toFloat()
    }
    private var hpPrevIn = 0f
    private var hpPrevOut = 0f

    //Parameter gerbang bising
    private val minThreshold = if (strong) 0.012f else 0.006f
    private val thresholdRatio = if (strong) 3.5f else 2.5f
    private val closedGain = if (strong) 0.02f else 0.18f
    private val holdSamples = (sampleRate * if (strong) 0.25f else 0.35f).toInt()
    private val attackCoeff = coeffForMillis(4f)
    private val releaseCoeff = coeffForMillis(if (strong) 90f else 140f)

    //Status gerbang bising
    private var work = FloatArray(0)
    private var noiseFloor = 0.01f
    private var holdLeft = 0
    private var gain = closedGain

    private fun coeffForMillis(ms: Float): Float {
        val samples = ms / 1000f * sampleRate
        return 1f - kotlin.math.exp(-1f / max(1f, samples))
    }

    /**
     * Memproses [length] sampel pertama pada [buffer] secara langsung (in-place)
     */
    fun process(buffer: ShortArray, length: Int) {
        if (!enabled || length <= 0) return

        //Hitung RMS blok, setelah high-pass
        var sumSq = 0f
        if (work.size < length) work = FloatArray(length)
        val tmp = work
        for (i in 0 until length) {
            val x = buffer[i] / 32768f
            val y = hpAlpha * (hpPrevOut + x - hpPrevIn)
            hpPrevIn = x
            hpPrevOut = y
            tmp[i] = y
            sumSq += y * y
        }
        val rms = sqrt(sumSq / length)

        //Perkirakan lantai bising: turun cepat, naik sangat lambat
        noiseFloor = if (rms < noiseFloor) {
            noiseFloor + (rms - noiseFloor) * 0.25f
        } else {
            noiseFloor + (rms - noiseFloor) * 0.0008f
        }
        noiseFloor = noiseFloor.coerceIn(0.0005f, 0.08f)

        val threshold = max(minThreshold, noiseFloor * thresholdRatio)
        val speaking = rms > threshold
        if (speaking) {
            holdLeft = holdSamples
        } else {
            holdLeft = max(0, holdLeft - length)
        }
        val open = holdLeft > 0

        val target = if (open) 1f else closedGain
        for (i in 0 until length) {
            val coeff = if (target > gain) attackCoeff else releaseCoeff
            gain += (target - gain) * coeff
            val out = tmp[i] * gain
            buffer[i] = (out * 32767f).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
    }

    /**
     * Kembalikan filter ke keadaan awal (misalnya setelah jeda)
     */
    fun reset() {
        hpPrevIn = 0f
        hpPrevOut = 0f
        holdLeft = 0
        gain = closedGain
    }
}
