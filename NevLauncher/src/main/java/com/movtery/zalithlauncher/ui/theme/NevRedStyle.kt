package com.movtery.zalithlauncher.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Gaya tema merah Nev Launcher: gradasi merah-oranye, latar merah gelap, garis tepi oranye.
 */
object NevRed {
    val GradientStart = Color(0xFFE8402F)
    val GradientEnd = Color(0xFFFFB02E)
    val Panel = Color(0xFF1C0C0B)
    val PanelDeep = Color(0xFF140808)
    val Outline = Color(0xFF7A3A22)
    val OutlineBright = Color(0xFFFF8A3D)
    val OnGradient = Color(0xFF2A0A06)
    val DangerTile = Color(0xFF5A2230)
    val DangerContent = Color(0xFFFF6B8A)
    val Muted = Color(0xFFD9B8B2)

    val Gradient: Brush
        get() = Brush.horizontalGradient(listOf(GradientStart, GradientEnd))

    val GradientDiagonal: Brush
        get() = Brush.linearGradient(listOf(GradientStart, GradientEnd))

    val PanelBrush: Brush
        get() = Brush.verticalGradient(listOf(Panel, PanelDeep))
}

/**
 * Wadah popup bergaya Nev (dipakai untuk popup Pengaturan / Klon / Hapus).
 */
@Composable
fun NevPopupContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .border(
                border = BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(listOf(NevRed.Outline, NevRed.OutlineBright))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .background(NevRed.PanelBrush, RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        content()
    }
}

/**
 * Ubin ikon bergradasi dengan label di bawahnya.
 * @param danger true untuk aksi berbahaya (Hapus), memakai warna merah muda redup
 */
@Composable
fun NevIconTile(
    painter: Painter,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    danger: Boolean = false
) {
    Column(
        modifier = modifier
            .semantics { role = Role.Button }
            .padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            color = Color.Transparent,
            border = if (danger) BorderStroke(1.dp, NevRed.DangerContent.copy(alpha = 0.6f)) else null
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        if (danger) Brush.linearGradient(listOf(NevRed.DangerTile, NevRed.DangerTile))
                        else NevRed.GradientDiagonal
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painter,
                    contentDescription = text,
                    tint = if (danger) NevRed.DangerContent else NevRed.OnGradient
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (danger) NevRed.DangerContent else Color.White,
            maxLines = 1
        )
    }
}

/** Pemisah vertikal tipis antar ubin di popup. */
@Composable
fun NevPopupDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(44.dp)
            .background(NevRed.Outline.copy(alpha = 0.6f))
    )
}

/**
 * Baris popup: beberapa ubin ikon dengan pemisah.
 */
@Composable
fun NevPopupRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

enum class NevPillStyle { Gradient, Neutral, Danger }

/**
 * Tombol pil bergaya Nev dengan lingkaran ikon di kiri.
 */
@Composable
fun NevPillButton(
    text: String,
    painter: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: NevPillStyle = NevPillStyle.Neutral
) {
    val contentColor = when (style) {
        NevPillStyle.Gradient -> NevRed.OnGradient
        NevPillStyle.Neutral -> Color.White
        NevPillStyle.Danger -> NevRed.DangerContent
    }
    val circleColor = when (style) {
        NevPillStyle.Gradient -> NevRed.OnGradient
        NevPillStyle.Neutral -> Color(0xFF3A1F1E)
        NevPillStyle.Danger -> Color(0xFF4A2030)
    }
    val circleIcon = when (style) {
        NevPillStyle.Gradient -> NevRed.GradientEnd
        else -> contentColor
    }
    val shape = RoundedCornerShape(50)
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { role = Role.Button },
        shape = shape,
        color = Color.Transparent,
        border = when (style) {
            NevPillStyle.Gradient -> BorderStroke(1.dp, NevRed.OutlineBright.copy(alpha = 0.8f))
            NevPillStyle.Neutral -> BorderStroke(1.dp, NevRed.Outline)
            NevPillStyle.Danger -> BorderStroke(1.dp, NevRed.DangerContent.copy(alpha = 0.5f))
        }
    ) {
        Row(
            modifier = Modifier
                .background(
                    when (style) {
                        NevPillStyle.Gradient -> NevRed.Gradient
                        NevPillStyle.Neutral -> Brush.horizontalGradient(listOf(Color(0xFF2A1413), Color(0xFF2A1413)))
                        NevPillStyle.Danger -> Brush.horizontalGradient(listOf(Color(0xFF3A1824), Color(0xFF3A1824)))
                    }
                )
                .padding(start = 6.dp, end = 18.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(circleColor, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painter,
                    contentDescription = null,
                    tint = circleIcon
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

/**
 * Item tab sisi bernomor ("01 Informasi"); yang terpilih memakai gradasi merah-oranye.
 */
@Composable
fun NevNumberedTab(
    number: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { role = Role.Tab },
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (selected) NevRed.OutlineBright else NevRed.Outline.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .background(
                    if (selected) NevRed.Gradient
                    else Brush.horizontalGradient(listOf(Color(0xFF200E0D), Color(0xFF200E0D)))
                )
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "%02d".format(number),
                style = MaterialTheme.typography.labelMedium,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = if (selected) NevRed.OnGradient.copy(alpha = 0.8f) else NevRed.GradientEnd
            )
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides
                        if (selected) NevRed.OnGradient else Color.White
            ) {
                content()
            }
        }
    }
}
