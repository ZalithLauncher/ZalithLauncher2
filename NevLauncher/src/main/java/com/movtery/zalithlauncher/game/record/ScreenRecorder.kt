package com.movtery.zalithlauncher.game.record

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AudioEffect
import android.media.audiofx.NoiseSuppressor
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import androidx.core.content.ContextCompat
import com.movtery.zalithlauncher.setting.enums.NoiseCancelLevel
import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs
import kotlin.math.max

private const val TAG = "ScreenRecorder"

private const val SAMPLE_RATE = 48_000
private const val AUDIO_FRAMES_PER_CHUNK = 1024
private const val AUDIO_BITRATE = 128_000
private const val TIMEOUT_US = 10_000L
private const val STOP_WAIT_MS = 6_000L
private const val MAX_PENDING_BYTES = 16 * 1024 * 1024

/**
 * Pengaturan satu sesi rekaman
 */
data class RecordConfig(
    val width: Int,
    val height: Int,
    val densityDpi: Int,
    val videoBitrate: Int,
    val fps: Int = 30,
    val recordGameAudio: Boolean,
    val recordMic: Boolean,
    /** 0f..2f */
    val gameVolume: Float,
    /** 0f..2f */
    val micVolume: Float,
    val noiseCancel: NoiseCancelLevel
)

/**
 * Peringatan yang tidak menghentikan rekaman, tetapi perlu diberi tahu ke pengguna
 */
enum class RecordWarning {
    /** Suara game tidak bisa direkam (Android di bawah 10 atau perangkat menolak) */
    GAME_AUDIO_UNAVAILABLE,

    /** Mikrofon tidak bisa dipakai (tidak ada izin atau sedang dipakai aplikasi lain) */
    MIC_UNAVAILABLE
}

/**
 * Perekam layar: video (H.264) + suara game (AudioPlaybackCapture) + mikrofon (dengan Noise Cancelling),
 * dicampur menjadi satu trek AAC lalu ditulis ke berkas MP4.
 *
 * Mendukung jeda dan lanjut. Bagian yang dijeda tidak ikut terekam (tanpa jeda kosong di hasil akhir).
 *
 * Penggunaan: [start] -> ([pause] / [resume])* -> [stop]. [stop] bersifat memblokir, panggil dari thread latar.
 */
class ScreenRecorder(
    private val context: Context,
    private val projection: MediaProjection,
    private val config: RecordConfig,
    private val outputFile: File,
    private val onFatalError: (Throwable) -> Unit,
    private val onProjectionStopped: () -> Unit
) {
    private class PendingSample(val track: Int, val data: ByteArray, val ptsUs: Long, val flags: Int)

    private val lock = Any()

    //Muxer
    private var muxer: MediaMuxer? = null
    private var muxerStarted = false
    private var expectedTracks = 1
    private var addedTracks = 0
    private var videoTrack = -1
    private var audioTrack = -1
    private val pending = ArrayList<PendingSample>()
    private var pendingBytes = 0
    private val videoSamplesWritten = AtomicInteger(0)

    //Video
    private var videoEncoder: MediaCodec? = null
    private var inputSurface: Surface? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var videoThread: Thread? = null
    private var lastVideoPtsUs = -1L
    private var videoClockOffsetUs: Long? = null

    //Audio
    private var audioEncoder: MediaCodec? = null
    private var gameRecord: AudioRecord? = null
    private var micRecord: AudioRecord? = null
    private val effects = ArrayList<AudioEffect>()
    private var audioThread: Thread? = null
    private var micFilter: MicNoiseFilter? = null

    private var callbackThread: HandlerThread? = null
    private var projectionCallback: MediaProjection.Callback? = null

    //Status waktu & kontrol
    @Volatile private var running = false
    @Volatile private var paused = false
    @Volatile private var waitingKeyframe = false
    @Volatile private var resumeBoundaryUs = 0L
    @Volatile private var pausedTotalUs = 0L
    @Volatile private var audioNeedRebase = true
    private var pauseStartUs = 0L
    private var startUs = 0L
    private var fatalReported = false

    /** Peringatan yang muncul saat [start] */
    val warnings: List<RecordWarning> get() = _warnings
    private val _warnings = ArrayList<RecordWarning>()

    /** Ukuran video akhir yang dipakai (bisa lebih kecil dari yang diminta bila perangkat tidak mendukung) */
    var videoWidth = config.width
        private set
    var videoHeight = config.height
        private set

    val isPaused: Boolean get() = paused

    private fun nowUs() = System.nanoTime() / 1000L

    @Throws(Exception::class)
    fun start() {
        outputFile.parentFile?.mkdirs()
        if (outputFile.exists()) outputFile.delete()

        try {
            startUs = nowUs()
            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            prepareAudio()
            prepareVideo()

            expectedTracks = 1 + (if (audioEncoder != null) 1 else 0)

            val thread = HandlerThread("NevRecorderCallback").also { it.start() }
            callbackThread = thread
            val handler = Handler(thread.looper)

            //Android 14+ mewajibkan callback didaftarkan sebelum membuat VirtualDisplay
            val callback = object : MediaProjection.Callback() {
                override fun onStop() {
                    onProjectionStopped()
                }
            }
            projectionCallback = callback
            projection.registerCallback(callback, handler)

            virtualDisplay = projection.createVirtualDisplay(
                "NevLauncherRecorder",
                videoWidth,
                videoHeight,
                config.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                inputSurface,
                null,
                handler
            )

            running = true
            videoThread = Thread({ runSafely { drainVideoLoop() } }, "NevRecorder-Video").also { it.start() }
            if (audioEncoder != null) {
                audioThread = Thread({ runSafely { audioLoop() } }, "NevRecorder-Audio").also { it.start() }
            }
        } catch (e: Throwable) {
            running = false
            releaseAll()
            outputFile.delete()
            throw e
        }
    }

    fun pause() {
        if (!running || paused) return
        pauseStartUs = nowUs()
        paused = true
    }

    fun resume() {
        if (!running || !paused) return
        val now = nowUs()
        pausedTotalUs += now - pauseStartUs
        resumeBoundaryUs = now
        waitingKeyframe = true
        audioNeedRebase = true
        micFilter?.reset()
        //Minta keyframe baru agar video bisa langsung diputar dari titik lanjut
        try {
            videoEncoder?.setParameters(Bundle().apply {
                putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)
            })
        } catch (e: Exception) {
            Logger.warning(TAG, "Failed to request sync frame", e)
        }
        paused = false
    }

    /**
     * Hentikan rekaman dan tutup berkas MP4. Memblokir sampai selesai.
     * @return true jika berkas berisi video yang valid
     */
    fun stop(): Boolean {
        if (!running) {
            releaseAll()
            return false
        }
        running = false
        if (paused) {
            //Anggap jeda berakhir agar waktu tidak dihitung lebih
            paused = false
        }

        try {
            virtualDisplay?.release()
        } catch (_: Exception) {
        }
        virtualDisplay = null

        try {
            videoEncoder?.signalEndOfInputStream()
        } catch (e: Exception) {
            Logger.warning(TAG, "signalEndOfInputStream failed", e)
        }

        videoThread?.joinQuietly(STOP_WAIT_MS)
        audioThread?.joinQuietly(STOP_WAIT_MS)

        val ok = synchronized(lock) { muxerStarted && videoSamplesWritten.get() > 0 }
        releaseAll()
        if (!ok) outputFile.delete()
        return ok
    }

    //region persiapan

    private fun prepareVideo() {
        var w = alignDown16(config.width)
        var h = alignDown16(config.height)
        var codecName: String? = null
        var format: MediaFormat? = null

        for (attempt in 0 until 4) {
            val f = buildVideoFormat(w, h)
            codecName = MediaCodecList(MediaCodecList.REGULAR_CODECS).findEncoderForFormat(f)
            if (codecName != null) {
                format = f
                break
            }
            w = alignDown16(w * 3 / 4)
            h = alignDown16(h * 3 / 4)
        }
        if (codecName == null || format == null) {
            throw IllegalStateException("No H.264 encoder supports this screen size")
        }

        videoWidth = w
        videoHeight = h

        val encoder = MediaCodec.createByCodecName(codecName)
        videoEncoder = encoder
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        inputSurface = encoder.createInputSurface()
        encoder.start()
    }

    private fun buildVideoFormat(w: Int, h: Int): MediaFormat =
        MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, w, h).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, config.videoBitrate)
            setInteger(MediaFormat.KEY_FRAME_RATE, config.fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        }

    @SuppressLint("MissingPermission")
    private fun prepareAudio() {
        val wantGame = config.recordGameAudio
        val wantMic = config.recordMic
        if (!wantGame && !wantMic) return

        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            if (wantGame) _warnings.add(RecordWarning.GAME_AUDIO_UNAVAILABLE)
            if (wantMic) _warnings.add(RecordWarning.MIC_UNAVAILABLE)
            return
        }

        if (wantGame) {
            gameRecord = createGameRecord()
            if (gameRecord == null) _warnings.add(RecordWarning.GAME_AUDIO_UNAVAILABLE)
        }
        if (wantMic) {
            micRecord = createMicRecord()
            if (micRecord == null) _warnings.add(RecordWarning.MIC_UNAVAILABLE)
        }
        if (gameRecord == null && micRecord == null) return

        if (micRecord != null) {
            micFilter = MicNoiseFilter(SAMPLE_RATE, config.noiseCancel)
        }

        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, SAMPLE_RATE, 2).apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BITRATE)
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16 * 1024)
        }
        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        audioEncoder = encoder
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()
    }

    @SuppressLint("MissingPermission")
    private fun createGameRecord(): AudioRecord? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return try {
            val captureConfig = AudioPlaybackCaptureConfiguration.Builder(projection)
                .addMatchingUsage(AudioAttributes.USAGE_GAME)
                .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                .build()
            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
                .build()
            val minBuf = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT
            )
            val bufSize = max(minBuf * 2, AUDIO_FRAMES_PER_CHUNK * 4 * 4)
            val record = AudioRecord.Builder()
                .setAudioPlaybackCaptureConfig(captureConfig)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(bufSize)
                .build()
            if (record.state != AudioRecord.STATE_INITIALIZED) {
                record.release()
                null
            } else record
        } catch (e: Exception) {
            Logger.warning(TAG, "Game audio capture is unavailable", e)
            null
        }
    }

    @SuppressLint("MissingPermission")
    private fun createMicRecord(): AudioRecord? {
        return try {
            //Mode STRONG memakai sumber VOICE_COMMUNICATION yang meredam bising & gema lebih kuat
            val source = if (config.noiseCancel == NoiseCancelLevel.STRONG) {
                MediaRecorder.AudioSource.VOICE_COMMUNICATION
            } else {
                MediaRecorder.AudioSource.MIC
            }
            val minBuf = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            val bufSize = max(minBuf * 2, AUDIO_FRAMES_PER_CHUNK * 2 * 4)
            val record = AudioRecord(
                source,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufSize
            )
            if (record.state != AudioRecord.STATE_INITIALIZED) {
                record.release()
                return null
            }
            if (config.noiseCancel != NoiseCancelLevel.OFF) {
                attachEffects(record.audioSessionId)
            }
            record
        } catch (e: Exception) {
            Logger.warning(TAG, "Microphone is unavailable", e)
            null
        }
    }

    /**
     * Aktifkan peredam bising dan peredam gema bawaan perangkat bila tersedia
     */
    private fun attachEffects(sessionId: Int) {
        try {
            if (NoiseSuppressor.isAvailable()) {
                NoiseSuppressor.create(sessionId)?.also {
                    it.enabled = true
                    effects.add(it)
                }
            }
        } catch (e: Exception) {
            Logger.warning(TAG, "NoiseSuppressor failed", e)
        }
        try {
            if (AcousticEchoCanceler.isAvailable()) {
                AcousticEchoCanceler.create(sessionId)?.also {
                    it.enabled = true
                    effects.add(it)
                }
            }
        } catch (e: Exception) {
            Logger.warning(TAG, "AcousticEchoCanceler failed", e)
        }
    }

    //endregion

    //region video

    private fun drainVideoLoop() {
        val encoder = videoEncoder ?: return
        val info = MediaCodec.BufferInfo()
        var stopRequestedAt = 0L

        while (true) {
            if (!running && stopRequestedAt == 0L) stopRequestedAt = System.currentTimeMillis()
            if (stopRequestedAt != 0L && System.currentTimeMillis() - stopRequestedAt > STOP_WAIT_MS) break

            val index = try {
                encoder.dequeueOutputBuffer(info, TIMEOUT_US)
            } catch (_: IllegalStateException) {
                break
            }

            when {
                index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    synchronized(lock) {
                        videoTrack = muxer!!.addTrack(encoder.outputFormat)
                        onTrackAdded()
                    }
                }

                index >= 0 -> {
                    val buffer = encoder.getOutputBuffer(index)
                    val isConfig = info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0
                    val isEos = info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                    if (buffer != null && info.size > 0 && !isConfig) {
                        handleVideoSample(buffer, info)
                    }
                    encoder.releaseOutputBuffer(index, false)
                    if (isEos) break
                }
            }
        }
    }

    private fun handleVideoSample(buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        if (paused) return

        //Samakan jam: biasanya stempel waktu frame memakai jam yang sama dengan System.nanoTime()
        var frameUs = info.presentationTimeUs
        val offset = videoClockOffsetUs ?: run {
            val diff = frameUs - nowUs()
            val value = if (abs(diff) < 2_000_000L) 0L else diff
            videoClockOffsetUs = value
            value
        }
        frameUs -= offset

        if (frameUs < resumeBoundaryUs) return

        val isKey = info.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME != 0
        if (waitingKeyframe) {
            if (!isKey) return
            waitingKeyframe = false
        }

        var pts = frameUs - startUs - pausedTotalUs
        if (pts < 0) pts = 0
        if (pts <= lastVideoPtsUs) pts = lastVideoPtsUs + 1
        lastVideoPtsUs = pts
        info.presentationTimeUs = pts

        if (writeSample(videoTrack, buffer, info)) {
            videoSamplesWritten.incrementAndGet()
        }
    }

    //endregion

    //region audio

    private fun audioLoop() {
        val encoder = audioEncoder ?: return
        val frames = AUDIO_FRAMES_PER_CHUNK
        val chunkUs = frames * 1_000_000L / SAMPLE_RATE

        var game = gameRecord
        var mic = micRecord
        val gameBuf = ShortArray(frames * 2)
        val micBuf = ShortArray(frames)
        val pcm = ByteBuffer.allocate(frames * 4).order(ByteOrder.LITTLE_ENDIAN)
        val info = MediaCodec.BufferInfo()

        var gameFailures = 0
        var micFailures = 0
        var audioBaseUs = 0L
        var chunkCount = 0L
        var lastEndUs = 0L

        try {
            game?.startRecording()
            mic?.startRecording()
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to start audio capture", e)
        }

        while (running) {
            var gameOk = false
            var micOk = false

            if (game != null) {
                gameOk = readFully(game, gameBuf, frames * 2)
                if (!gameOk && ++gameFailures > 20) {
                    Logger.warning(TAG, "Game audio capture stopped responding; continuing without it")
                    game = null
                }
            }
            if (mic != null) {
                micOk = readFully(mic, micBuf, frames)
                if (!micOk && ++micFailures > 20) {
                    Logger.warning(TAG, "Microphone stopped responding; continuing without it")
                    mic = null
                }
            }
            if (game == null && mic == null) {
                dropAudioTrack()
                break
            }

            if (paused) {
                //Tetap kuras data agar tidak menumpuk, tapi jangan direkam
                audioNeedRebase = true
                drainAudioOutput(encoder, info)
                continue
            }

            if (audioNeedRebase) {
                val nowRel = nowUs() - startUs - pausedTotalUs
                audioBaseUs = max(lastEndUs, nowRel - chunkUs)
                chunkCount = 0
                audioNeedRebase = false
            }

            if (micOk) micFilter?.process(micBuf, frames)

            //Campur: suara game (stereo) + mikrofon (mono ke kedua sisi)
            val gv = config.gameVolume
            val mv = config.micVolume
            pcm.clear()
            for (i in 0 until frames) {
                val m = if (micOk) micBuf[i] * mv else 0f
                val l = (if (gameOk) gameBuf[2 * i] * gv else 0f) + m
                val r = (if (gameOk) gameBuf[2 * i + 1] * gv else 0f) + m
                pcm.putShort(limit(l))
                pcm.putShort(limit(r))
            }
            pcm.flip()

            val ptsUs = audioBaseUs + chunkCount * frames * 1_000_000L / SAMPLE_RATE
            chunkCount++
            lastEndUs = audioBaseUs + chunkCount * frames * 1_000_000L / SAMPLE_RATE

            feedAudio(encoder, info, pcm, ptsUs)
            drainAudioOutput(encoder, info)
        }

        //Kirim tanda akhir aliran lalu kuras semua sisa data
        try {
            var sent = false
            for (i in 0 until 20) {
                val index = encoder.dequeueInputBuffer(TIMEOUT_US * 5)
                if (index >= 0) {
                    encoder.queueInputBuffer(index, 0, 0, lastEndUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    sent = true
                    break
                }
                drainAudioOutput(encoder, info)
            }
            if (sent) {
                val deadline = System.currentTimeMillis() + STOP_WAIT_MS
                while (System.currentTimeMillis() < deadline) {
                    if (drainAudioOutput(encoder, info)) break
                }
            }
        } catch (e: Exception) {
            Logger.warning(TAG, "Failed to finish audio stream", e)
        }

        try {
            game?.stop()
            mic?.stop()
        } catch (_: Exception) {
        }
    }

    private fun feedAudio(encoder: MediaCodec, info: MediaCodec.BufferInfo, pcm: ByteBuffer, ptsUs: Long) {
        repeat(5) {
            val index = encoder.dequeueInputBuffer(TIMEOUT_US)
            if (index >= 0) {
                val input = encoder.getInputBuffer(index) ?: return
                input.clear()
                input.put(pcm)
                encoder.queueInputBuffer(index, 0, input.position(), ptsUs, 0)
                return
            }
            drainAudioOutput(encoder, info)
        }
        Logger.warning(TAG, "Audio encoder is busy, a chunk was dropped")
    }

    /**
     * @return true jika sinyal akhir aliran (EOS) sudah diterima
     */
    private fun drainAudioOutput(encoder: MediaCodec, info: MediaCodec.BufferInfo): Boolean {
        while (true) {
            val index = encoder.dequeueOutputBuffer(info, 0)
            when {
                index == MediaCodec.INFO_TRY_AGAIN_LATER -> return false

                index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    synchronized(lock) {
                        audioTrack = muxer!!.addTrack(encoder.outputFormat)
                        onTrackAdded()
                    }
                }

                index >= 0 -> {
                    val buffer = encoder.getOutputBuffer(index)
                    val isConfig = info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0
                    val isEos = info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                    if (buffer != null && info.size > 0 && !isConfig) {
                        writeSample(audioTrack, buffer, info)
                    }
                    encoder.releaseOutputBuffer(index, false)
                    if (isEos) return true
                }
            }
        }
    }

    private fun readFully(record: AudioRecord, buffer: ShortArray, size: Int): Boolean {
        var offset = 0
        while (offset < size && running) {
            val n = record.read(buffer, offset, size - offset)
            if (n < 0) return false
            if (n == 0) Thread.sleep(2)
            offset += n
        }
        return offset >= size
    }

    /**
     * Pembatas lembut agar suara yang terlalu keras tidak pecah
     */
    private fun limit(x: Float): Short {
        val a = abs(x)
        val y = if (a <= 24000f) a else 24000f + (a - 24000f) / (1f + (a - 24000f) / 8767f)
        val signed = if (x < 0f) -y else y
        return signed.toInt().coerceIn(-32768, 32767).toShort()
    }

    //endregion

    //region muxer

    /**
     * Dipanggil (di dalam lock) setiap kali satu trek baru ditambahkan
     */
    private fun onTrackAdded() {
        addedTracks++
        tryStartMuxer()
    }

    /**
     * Trek audio tidak akan pernah ada (mis. semua sumber suara gagal), jangan tunggu lagi
     */
    private fun dropAudioTrack() {
        synchronized(lock) {
            if (audioTrack >= 0) return
            expectedTracks = max(1, expectedTracks - 1)
            tryStartMuxer()
        }
    }

    private fun tryStartMuxer() {
        if (muxerStarted || addedTracks == 0 || addedTracks < expectedTracks) return
        val m = muxer ?: return
        m.start()
        muxerStarted = true

        //Tulis semua sampel yang tertahan, berurutan menurut waktu
        pending.sortBy { it.ptsUs }
        val info = MediaCodec.BufferInfo()
        for (sample in pending) {
            info.set(0, sample.data.size, sample.ptsUs, sample.flags)
            try {
                m.writeSampleData(sample.track, ByteBuffer.wrap(sample.data), info)
                if (sample.track == videoTrack) videoSamplesWritten.incrementAndGet()
            } catch (e: Exception) {
                Logger.warning(TAG, "Failed to write pending sample", e)
            }
        }
        pending.clear()
        pendingBytes = 0
    }

    /**
     * @return true jika sampel langsung ditulis (bukan ditahan atau dibuang)
     */
    private fun writeSample(track: Int, buffer: ByteBuffer, info: MediaCodec.BufferInfo): Boolean {
        if (track < 0) return false
        synchronized(lock) {
            val m = muxer ?: return false
            buffer.position(info.offset)
            buffer.limit(info.offset + info.size)

            if (!muxerStarted) {
                if (pendingBytes + info.size > MAX_PENDING_BYTES) return false
                val copy = ByteArray(info.size)
                buffer.get(copy)
                pending.add(PendingSample(track, copy, info.presentationTimeUs, info.flags))
                pendingBytes += info.size
                return false
            }

            return try {
                m.writeSampleData(track, buffer, info)
                true
            } catch (e: Exception) {
                Logger.warning(TAG, "Failed to write sample", e)
                false
            }
        }
    }

    //endregion

    //region pelepasan sumber daya

    private inline fun runSafely(block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            Logger.error(TAG, "Recording thread crashed", e)
            val first = synchronized(lock) {
                val was = fatalReported
                fatalReported = true
                !was
            }
            if (first && running) onFatalError(e)
        }
    }

    private fun Thread.joinQuietly(timeoutMs: Long) {
        try {
            join(timeoutMs)
        } catch (_: InterruptedException) {
        }
    }

    private fun releaseAll() {
        try { virtualDisplay?.release() } catch (_: Exception) { }
        virtualDisplay = null

        projectionCallback?.let {
            try { projection.unregisterCallback(it) } catch (_: Exception) { }
        }
        projectionCallback = null
        callbackThread?.quitSafely()
        callbackThread = null

        effects.forEach { try { it.release() } catch (_: Exception) { } }
        effects.clear()

        try { gameRecord?.release() } catch (_: Exception) { }
        try { micRecord?.release() } catch (_: Exception) { }
        gameRecord = null
        micRecord = null

        try { videoEncoder?.stop() } catch (_: Exception) { }
        try { videoEncoder?.release() } catch (_: Exception) { }
        videoEncoder = null
        try { audioEncoder?.stop() } catch (_: Exception) { }
        try { audioEncoder?.release() } catch (_: Exception) { }
        audioEncoder = null

        try { inputSurface?.release() } catch (_: Exception) { }
        inputSurface = null

        synchronized(lock) {
            val m = muxer
            if (m != null) {
                try { if (muxerStarted) m.stop() } catch (_: Exception) { }
                try { m.release() } catch (_: Exception) { }
            }
            muxer = null
            pending.clear()
            pendingBytes = 0
        }
    }

    //endregion

    private fun alignDown16(value: Int): Int = max(16, value / 16 * 16)
}
