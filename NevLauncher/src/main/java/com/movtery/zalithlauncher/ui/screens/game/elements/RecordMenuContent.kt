package com.movtery.zalithlauncher.ui.screens.game.elements

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.record.RecordEvent
import com.movtery.zalithlauncher.game.record.RecordPhase
import com.movtery.zalithlauncher.game.record.RecordingService
import com.movtery.zalithlauncher.game.record.RecordingState
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.NoiseCancelLevel
import com.movtery.zalithlauncher.setting.enums.RecordQuality
import com.movtery.zalithlauncher.setting.unit.floatRange
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MenuListLayout
import com.movtery.zalithlauncher.ui.components.MenuSliderLayout
import com.movtery.zalithlauncher.ui.components.MenuSwitchButton
import com.movtery.zalithlauncher.ui.components.MenuTextButton
import com.movtery.zalithlauncher.ui.components.lazyScrollWithBar
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import kotlinx.coroutines.delay

/**
 * Isi tab "Rekam" pada Menu Game: mulai, jeda, lanjut, simpan, serta pengaturan suara dan Noise Cancelling
 */
@Composable
fun RecordMenuContent(
    modifier: Modifier = Modifier,
    color: Color = cardColor(false),
    contentColor: Color = onCardColor(),
) {
    val context = LocalContext.current
    val snapshot by RecordingState.snapshot.collectAsState()
    val phase = snapshot.phase
    val active = snapshot.isActive

    //Penghitung waktu: diperbarui tiap setengah detik saat sedang merekam
    var tickNow by remember { mutableLongStateOf(android.os.SystemClock.elapsedRealtime()) }
    LaunchedEffect(phase) {
        while (phase == RecordPhase.RECORDING) {
            tickNow = android.os.SystemClock.elapsedRealtime()
            delay(500L)
        }
        tickNow = android.os.SystemClock.elapsedRealtime()
    }
    val elapsedText = RecordingService.formatDuration(snapshot.elapsedMs(tickNow))

    //Izin berbagi layar (MediaProjection)
    val projectionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            RecordingService.start(context, result.resultCode, data)
        } else {
            RecordingState.emit(RecordEvent(R.string.record_error_no_permission))
        }
    }

    fun requestProjection() {
        val manager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projectionLauncher.launch(manager.createScreenCaptureIntent())
    }

    //Izin mikrofon (juga dibutuhkan untuk merekam suara game): bila ditolak, lanjut tanpa suara
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) RecordingState.emit(RecordEvent(R.string.record_error_mic_permission))
        requestProjection()
    }

    fun onStartClick() {
        val needAudio = AllSettings.recordMic.getValue() || AllSettings.recordGameAudio.getValue()
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (needAudio && !hasPermission) {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            requestProjection()
        }
    }

    val listState = rememberLazyListState()
    LazyColumn(
        modifier = modifier.lazyScrollWithBar(listState),
        state = listState,
        contentPadding = PaddingValues(all = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //Status dan tombol kontrol
        item {
            BackgroundCard(
                modifier = Modifier.fillMaxWidth(),
                influencedByBackground = false,
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = when (phase) {
                            RecordPhase.IDLE -> stringResource(R.string.record_status_idle)
                            RecordPhase.STARTING -> stringResource(R.string.record_status_starting)
                            RecordPhase.RECORDING -> stringResource(R.string.record_status_recording, elapsedText)
                            RecordPhase.PAUSED -> stringResource(R.string.record_status_paused, elapsedText)
                            RecordPhase.SAVING -> stringResource(R.string.record_status_saving)
                        },
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (phase) {
                            RecordPhase.IDLE -> {
                                MenuTextButton(
                                    modifier = Modifier.weight(1f),
                                    text = stringResource(R.string.record_action_start),
                                    onClick = { onStartClick() },
                                    color = color,
                                    contentColor = contentColor,
                                )
                            }

                            RecordPhase.RECORDING -> {
                                MenuTextButton(
                                    modifier = Modifier.weight(1f),
                                    text = stringResource(R.string.record_action_pause),
                                    onClick = { RecordingService.pause(context) },
                                    color = color,
                                    contentColor = contentColor,
                                )
                                MenuTextButton(
                                    modifier = Modifier.weight(1f),
                                    text = stringResource(R.string.record_action_save),
                                    onClick = { RecordingService.stopAndSave(context) },
                                    color = color,
                                    contentColor = contentColor,
                                )
                            }

                            RecordPhase.PAUSED -> {
                                MenuTextButton(
                                    modifier = Modifier.weight(1f),
                                    text = stringResource(R.string.record_action_resume),
                                    onClick = { RecordingService.resume(context) },
                                    color = color,
                                    contentColor = contentColor,
                                )
                                MenuTextButton(
                                    modifier = Modifier.weight(1f),
                                    text = stringResource(R.string.record_action_save),
                                    onClick = { RecordingService.stopAndSave(context) },
                                    color = color,
                                    contentColor = contentColor,
                                )
                            }

                            RecordPhase.STARTING, RecordPhase.SAVING -> {
                                MenuTextButton(
                                    modifier = Modifier.weight(1f),
                                    text = stringResource(
                                        if (phase == RecordPhase.STARTING) R.string.record_status_starting
                                        else R.string.record_status_saving
                                    ),
                                    enabled = false,
                                    color = color,
                                    contentColor = contentColor,
                                )
                            }
                        }
                    }

                    if (active) {
                        Text(
                            text = stringResource(R.string.record_option_locked),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        //Suara game
        item {
            MenuSwitchButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.record_option_game_audio),
                switch = AllSettings.recordGameAudio.state,
                onSwitch = { AllSettings.recordGameAudio.save(it) },
                enabled = !active,
                color = color,
                contentColor = contentColor,
            )
        }
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.record_option_game_volume),
                value = AllSettings.recordGameVolume.state,
                valueRange = AllSettings.recordGameVolume.floatRange,
                onValueChange = { AllSettings.recordGameVolume.updateState(it) },
                onValueChangeFinished = { AllSettings.recordGameVolume.save(it) },
                suffix = "%",
                enabled = !active && AllSettings.recordGameAudio.state,
                color = color,
                contentColor = contentColor,
            )
        }

        //Mikrofon
        item {
            MenuSwitchButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.record_option_mic),
                switch = AllSettings.recordMic.state,
                onSwitch = { AllSettings.recordMic.save(it) },
                enabled = !active,
                color = color,
                contentColor = contentColor,
            )
        }
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.record_option_mic_volume),
                value = AllSettings.recordMicVolume.state,
                valueRange = AllSettings.recordMicVolume.floatRange,
                onValueChange = { AllSettings.recordMicVolume.updateState(it) },
                onValueChangeFinished = { AllSettings.recordMicVolume.save(it) },
                suffix = "%",
                enabled = !active && AllSettings.recordMic.state,
                color = color,
                contentColor = contentColor,
            )
        }

        //Noise Cancelling
        item {
            MenuListLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.record_option_noise_cancel),
                items = NoiseCancelLevel.entries,
                currentItem = AllSettings.recordNoiseCancel.state,
                onItemChange = { AllSettings.recordNoiseCancel.save(it) },
                getItemText = { stringResource(it.nameRes) },
                enabled = !active && AllSettings.recordMic.state,
                color = color,
                contentColor = contentColor,
            )
        }
        item {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = stringResource(R.string.record_option_noise_cancel_hint),
                style = MaterialTheme.typography.bodySmall
            )
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        //Kualitas video
        item {
            MenuListLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.record_option_quality),
                items = RecordQuality.entries,
                currentItem = AllSettings.recordQuality.state,
                onItemChange = { AllSettings.recordQuality.save(it) },
                getItemText = { stringResource(it.nameRes) },
                enabled = !active,
                color = color,
                contentColor = contentColor,
            )
        }
    }
}
