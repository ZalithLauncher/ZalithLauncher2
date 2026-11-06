package com.movtery.zalithlauncher.game.record

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.media.MediaScannerConnection
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.Display
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.notification.NOTIFICATION_ID_RECORD_RESULT
import com.movtery.zalithlauncher.notification.NOTIFICATION_ID_RECORD_SERVICE
import com.movtery.zalithlauncher.notification.NotificationChannelData
import com.movtery.zalithlauncher.notification.NotificationManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.min

private const val TAG = "RecordingService"
private const val GALLERY_FOLDER = "NevLauncher"

/**
 * Layanan latar depan untuk merekam layar. Berjalan di proses game, sama seperti layar game.
 *
 * Alur: UI meminta izin MediaProjection -> [RecordingService.start] -> layanan merekam ->
 * jeda / lanjut / simpan lewat tombol di Menu Game atau tombol pada notifikasi.
 */
class RecordingService : Service() {
    private val worker = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private var projection: MediaProjection? = null
    private var recorder: ScreenRecorder? = null
    private var tempFile: File? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationManager.createNotificationChannel(this, NotificationChannelData.RECORD_SERVICE_CHANNEL)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart(intent)
            ACTION_PAUSE -> handlePause()
            ACTION_RESUME -> handleResume()
            ACTION_STOP -> handleStop()
            else -> if (!RecordingState.snapshot.value.isActive) stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        //Layanan dihentikan paksa saat masih merekam: tutup berkas sebisanya
        val rec = recorder
        if (rec != null) {
            try {
                rec.stop()
            } catch (_: Exception) {
            }
            recorder = null
        }
        try {
            projection?.stop()
        } catch (_: Exception) {
        }
        projection = null
        worker.shutdown()
        RecordingState.update { RecordSnapshot() }
    }

    //region kontrol

    private fun handleStart(intent: Intent) {
        if (RecordingState.snapshot.value.isActive) return

        RecordingState.update { RecordSnapshot(phase = RecordPhase.STARTING) }
        enterForeground(buildNotification(RecordPhase.STARTING, 0L))

        @Suppress("DEPRECATION")
        val data: Intent? = intent.getParcelableExtra(EXTRA_DATA)
        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
        if (data == null) {
            failStart(getString(R.string.record_error_no_permission))
            return
        }

        worker.execute {
            try {
                beginRecording(resultCode, data)
            } catch (e: Throwable) {
                Logger.error(TAG, "Failed to start recording", e)
                mainHandler.post { failStart(e.message ?: e.javaClass.simpleName) }
            }
        }
    }

    private fun beginRecording(resultCode: Int, data: Intent) {
        val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val proj = manager.getMediaProjection(resultCode, data)
            ?: throw IllegalStateException(getString(R.string.record_error_no_permission))
        projection = proj

        val file = createTempFile()
        tempFile = file

        val rec = ScreenRecorder(
            context = this,
            projection = proj,
            config = buildConfig(),
            outputFile = file,
            onFatalError = { error ->
                Logger.error(TAG, "Recording stopped by an error", error)
                mainHandler.post {
                    RecordingState.emit(RecordEvent(R.string.record_event_stopped_error))
                    handleStop()
                }
            },
            onProjectionStopped = {
                //Pengguna menghentikan berbagi layar dari sistem
                mainHandler.post { handleStop() }
            }
        )
        rec.start()
        recorder = rec

        mainHandler.post {
            val now = SystemClock.elapsedRealtime()
            RecordingState.update {
                RecordSnapshot(phase = RecordPhase.RECORDING, accumulatedMs = 0L, segmentStartRealtime = now)
            }
            refreshNotification()
            rec.warnings.forEach { warning ->
                RecordingState.emit(
                    RecordEvent(
                        when (warning) {
                            RecordWarning.GAME_AUDIO_UNAVAILABLE -> R.string.record_warning_game_audio
                            RecordWarning.MIC_UNAVAILABLE -> R.string.record_warning_mic
                        }
                    )
                )
            }
        }
    }

    private fun handlePause() {
        val rec = recorder ?: return
        val state = RecordingState.snapshot.value
        if (state.phase != RecordPhase.RECORDING) return
        rec.pause()
        val now = SystemClock.elapsedRealtime()
        RecordingState.update {
            it.copy(phase = RecordPhase.PAUSED, accumulatedMs = it.elapsedMs(now), segmentStartRealtime = 0L)
        }
        refreshNotification()
    }

    private fun handleResume() {
        val rec = recorder ?: return
        if (RecordingState.snapshot.value.phase != RecordPhase.PAUSED) return
        rec.resume()
        val now = SystemClock.elapsedRealtime()
        RecordingState.update { it.copy(phase = RecordPhase.RECORDING, segmentStartRealtime = now) }
        refreshNotification()
    }

    private fun handleStop() {
        val rec = recorder
        val phase = RecordingState.snapshot.value.phase
        if (rec == null || (phase != RecordPhase.RECORDING && phase != RecordPhase.PAUSED)) {
            //Belum sempat mulai atau sudah selesai
            if (phase == RecordPhase.STARTING) return
            if (!RecordingState.snapshot.value.isActive) stopSelf()
            return
        }

        val now = SystemClock.elapsedRealtime()
        RecordingState.update {
            it.copy(phase = RecordPhase.SAVING, accumulatedMs = it.elapsedMs(now), segmentStartRealtime = 0L)
        }
        refreshNotification()

        val file = tempFile
        val proj = projection
        recorder = null
        projection = null

        worker.execute {
            val ok = try {
                rec.stop()
            } catch (e: Throwable) {
                Logger.error(TAG, "Failed to stop recording", e)
                false
            }
            try {
                proj?.stop()
            } catch (_: Exception) {
            }

            var event: RecordEvent
            if (!ok || file == null || !file.exists()) {
                event = RecordEvent(R.string.record_event_failed_empty)
            } else {
                val exported = try {
                    exportToGallery(this, file)
                } catch (e: Throwable) {
                    Logger.error(TAG, "Failed to export recording", e)
                    null
                }
                event = if (exported != null) {
                    file.delete()
                    RecordEvent(R.string.record_event_saved, exported)
                } else {
                    RecordEvent(R.string.record_event_failed_export, file.absolutePath)
                }
            }

            mainHandler.post {
                RecordingState.update { RecordSnapshot() }
                RecordingState.emit(event)
                stopForegroundCompat()
                postResultNotification(event)
                stopSelf()
            }
        }
    }

    private fun failStart(message: String) {
        releaseAfterFailure()
        RecordingState.update { RecordSnapshot() }
        RecordingState.emit(RecordEvent(R.string.record_event_failed_start, message))
        stopForegroundCompat()
        stopSelf()
    }

    private fun releaseAfterFailure() {
        try {
            recorder?.stop()
        } catch (_: Exception) {
        }
        recorder = null
        try {
            projection?.stop()
        } catch (_: Exception) {
        }
        projection = null
        tempFile?.delete()
        tempFile = null
    }

    //endregion

    //region pengaturan

    private fun buildConfig(): RecordConfig {
        val quality = AllSettings.recordQuality.getValue()

        val metrics = DisplayMetrics()
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        @Suppress("DEPRECATION")
        display.getRealMetrics(metrics)

        val shortSide = min(metrics.widthPixels, metrics.heightPixels).coerceAtLeast(1)
        val scale = min(1f, quality.maxShortSide.toFloat() / shortSide)
        val width = (metrics.widthPixels * scale).toInt()
        val height = (metrics.heightPixels * scale).toInt()

        return RecordConfig(
            width = width,
            height = height,
            densityDpi = metrics.densityDpi,
            videoBitrate = quality.bitrate,
            recordGameAudio = AllSettings.recordGameAudio.getValue(),
            recordMic = AllSettings.recordMic.getValue(),
            gameVolume = AllSettings.recordGameVolume.getValue() / 100f,
            micVolume = AllSettings.recordMicVolume.getValue() / 100f,
            noiseCancel = AllSettings.recordNoiseCancel.getValue()
        )
    }

    private fun createTempFile(): File {
        val base = getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: cacheDir
        val dir = File(base, "records")
        dir.mkdirs()
        val name = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        return File(dir, "NevLauncher_$name.mp4")
    }

    //endregion

    //region notifikasi

    private fun refreshNotification() {
        val state = RecordingState.snapshot.value
        val notification = buildNotification(state.phase, state.elapsedMs())
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        try {
            manager.notify(NOTIFICATION_ID_RECORD_SERVICE, notification)
        } catch (e: Exception) {
            Logger.warning(TAG, "Failed to update notification", e)
        }
    }

    private fun buildNotification(phase: RecordPhase, elapsedMs: Long): Notification {
        val channel = NotificationChannelData.RECORD_SERVICE_CHANNEL
        val builder = NotificationCompat.Builder(this, channel.channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        when (phase) {
            RecordPhase.STARTING -> {
                builder.setContentTitle(getString(R.string.record_notification_starting))
            }

            RecordPhase.RECORDING -> {
                builder.setContentTitle(getString(R.string.record_notification_recording))
                    .setUsesChronometer(true)
                    .setWhen(System.currentTimeMillis() - elapsedMs)
                    .addAction(0, getString(R.string.record_action_pause), actionIntent(ACTION_PAUSE, 1))
                    .addAction(0, getString(R.string.record_action_save), actionIntent(ACTION_STOP, 2))
            }

            RecordPhase.PAUSED -> {
                builder.setContentTitle(getString(R.string.record_notification_paused))
                    .setContentText(formatDuration(elapsedMs))
                    .addAction(0, getString(R.string.record_action_resume), actionIntent(ACTION_RESUME, 3))
                    .addAction(0, getString(R.string.record_action_save), actionIntent(ACTION_STOP, 2))
            }

            RecordPhase.SAVING -> {
                builder.setContentTitle(getString(R.string.record_notification_saving))
            }

            RecordPhase.IDLE -> {}
        }
        return builder.build()
    }

    private fun actionIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, RecordingService::class.java).setAction(action)
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun postResultNotification(event: RecordEvent) {
        val text = if (event.arg != null) getString(event.messageRes, event.arg) else getString(event.messageRes)
        val notification = NotificationCompat.Builder(this, NotificationChannelData.RECORD_SERVICE_CHANNEL.channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.record_notification_channel_name))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        try {
            val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.notify(NOTIFICATION_ID_RECORD_RESULT, notification)
        } catch (e: Exception) {
            Logger.warning(TAG, "Failed to post result notification", e)
        }
    }

    private fun enterForeground(notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                var type = ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                val micGranted = ContextCompat.checkSelfPermission(
                    this, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && micGranted) {
                    type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                }
                startForeground(NOTIFICATION_ID_RECORD_SERVICE, notification, type)
            } else {
                startForeground(NOTIFICATION_ID_RECORD_SERVICE, notification)
            }
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to enter foreground", e)
        }
    }

    private fun stopForegroundCompat() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    //endregion

    companion object {
        const val ACTION_START = "com.movtery.zalithlauncher.record.START"
        const val ACTION_PAUSE = "com.movtery.zalithlauncher.record.PAUSE"
        const val ACTION_RESUME = "com.movtery.zalithlauncher.record.RESUME"
        const val ACTION_STOP = "com.movtery.zalithlauncher.record.STOP"

        private const val EXTRA_RESULT_CODE = "result_code"
        private const val EXTRA_DATA = "projection_data"

        /**
         * Mulai merekam, setelah pengguna menyetujui izin berbagi layar
         */
        fun start(context: Context, resultCode: Int, data: Intent) {
            val intent = Intent(context, RecordingService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_RESULT_CODE, resultCode)
                .putExtra(EXTRA_DATA, data)
            ContextCompat.startForegroundService(context, intent)
        }

        fun pause(context: Context) = sendAction(context, ACTION_PAUSE)
        fun resume(context: Context) = sendAction(context, ACTION_RESUME)
        fun stopAndSave(context: Context) = sendAction(context, ACTION_STOP)

        private fun sendAction(context: Context, action: String) {
            context.startService(Intent(context, RecordingService::class.java).setAction(action))
        }

        /**
         * Format durasi menjadi mm:ss atau h:mm:ss
         */
        fun formatDuration(ms: Long): String {
            val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
            val h = totalSeconds / 3600
            val m = (totalSeconds % 3600) / 60
            val s = totalSeconds % 60
            return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
            else String.format(Locale.US, "%02d:%02d", m, s)
        }

        /**
         * Salin rekaman ke galeri (Movies/NevLauncher)
         * @return lokasi yang ditampilkan ke pengguna, atau null jika gagal
         */
        private fun exportToGallery(context: Context, file: File): String? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/$GALLERY_FOLDER")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
                val uri: Uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return null
                try {
                    val out = resolver.openOutputStream(uri) ?: throw IllegalStateException("No output stream")
                    out.use { stream -> file.inputStream().use { it.copyTo(stream) } }
                    val done = ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }
                    resolver.update(uri, done, null, null)
                    "${Environment.DIRECTORY_MOVIES}/$GALLERY_FOLDER/${file.name}"
                } catch (e: Exception) {
                    resolver.delete(uri, null, null)
                    throw e
                }
            } else {
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    GALLERY_FOLDER
                )
                if (!dir.exists() && !dir.mkdirs()) return null
                val dest = File(dir, file.name)
                file.copyTo(dest, overwrite = true)
                MediaScannerConnection.scanFile(context, arrayOf(dest.absolutePath), arrayOf("video/mp4"), null)
                "${Environment.DIRECTORY_MOVIES}/$GALLERY_FOLDER/${file.name}"
            }
        }
    }
}
