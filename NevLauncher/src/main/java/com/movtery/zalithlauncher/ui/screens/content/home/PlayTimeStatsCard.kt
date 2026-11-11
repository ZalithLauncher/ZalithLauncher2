package com.movtery.zalithlauncher.ui.screens.content.home

import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.stats.DayPlayTime
import com.movtery.zalithlauncher.game.stats.PlayTimeStats
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.format.TextStyle
import java.util.Locale

private val ChartHeight = 120.dp
private val MinBarHeight = 4.dp

/**
 * Kartu statistik waktu bermain, grafik batang Senin sampai Minggu pada pekan ini.
 */
@Composable
fun PlayTimeStatsCard(
    modifier: Modifier = Modifier
) {
    //naikkan kunci ini untuk memuat ulang data (saat kembali dari game, dan berkala)
    var refreshKey by remember { mutableIntStateOf(0) }
    var week by remember { mutableStateOf<List<DayPlayTime>>(emptyList()) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshKey++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(refreshKey) {
        week = withContext(Dispatchers.IO) {
            PlayTimeStats.pruneOld()
            PlayTimeStats.weekOf()
        }
        //muat ulang berkala selama kartu tampil (mis. lewat tengah malam)
        delay(60_000L)
        refreshKey++
    }

    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    val totalSeconds = week.sumOf { it.seconds }
    val maxSeconds = (week.maxOfOrNull { it.seconds } ?: 0L).coerceAtLeast(1L)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    BackgroundCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.play_time_stats_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    modifier = Modifier.alpha(0.8f),
                    text = if (totalSeconds > 0L) {
                        stringResource(R.string.play_time_stats_total, formatDuration(totalSeconds))
                    } else {
                        stringResource(R.string.play_time_stats_none)
                    },
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ChartHeight + 44.dp)
                    .drawBehind {
                        //garis bantu putus-putus di belakang batang
                        val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        val top = 18.dp.toPx()
                        val bottom = size.height - 26.dp.toPx()
                        listOf(top, top + (bottom - top) / 2f).forEach { y ->
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = dash
                            )
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                week.forEach { day ->
                    DayBar(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        day = day,
                        fraction = day.seconds.toFloat() / maxSeconds.toFloat(),
                        label = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayBar(
    day: DayPlayTime,
    fraction: Float,
    label: String,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    //batang berisi memakai gradasi tema (primer ke tersier), hari kosong abu-abu datar
    val barBrush = when {
        day.seconds <= 0L -> Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
            )
        )
        day.isToday -> Brush.verticalGradient(listOf(tertiary, primary))
        else -> Brush.verticalGradient(listOf(tertiary.copy(alpha = 0.85f), primary.copy(alpha = 0.85f)))
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //waktu bermain di atas batang
        Text(
            modifier = Modifier
                .height(18.dp)
                .alpha(if (day.seconds > 0L) 0.9f else 0f),
            text = if (day.seconds > 0L) formatDuration(day.seconds) else "",
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            val barHeight = (ChartHeight * fraction.coerceIn(0f, 1f)).coerceAtLeast(MinBarHeight)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(barHeight)
                    .background(barBrush, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
            )
        }

        Text(
            modifier = Modifier
                .padding(top = 6.dp)
                .alpha(if (day.isFuture) 0.5f else 1f),
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

@Composable
private fun formatDuration(totalSeconds: Long): String {
    val totalMinutes = totalSeconds / 60L
    val hours = (totalMinutes / 60L).toInt()
    val minutes = (totalMinutes % 60L).toInt()
    return if (hours > 0) {
        stringResource(R.string.play_time_stats_hours_minutes, hours, minutes)
    } else {
        stringResource(R.string.play_time_stats_minutes, minutes)
    }
}
