package com.movtery.zalithlauncher.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
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
    /** Awal gradasi: warna utama tema aktif. */
    val GradientStart: Color
        @Composable get() = MaterialTheme.colorScheme.primary
    /** Akhir gradasi: warna tersier tema aktif. */
    val GradientEnd: Color
        @Composable get() = MaterialTheme.colorScheme.tertiary
    /** Teks/ikon di atas gradasi. */
    val OnGradient: Color
        @Composable get() = MaterialTheme.colorScheme.onPrimary
    val Panel: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainer
    val PanelDeep: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow
    val SurfaceHigh: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh
    val SurfaceHighest: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerHighest
    val Outline: Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant
    val OutlineBright: Color
        @Composable get() = lerp(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            0.5f
        )
    val OnPanel: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val DangerTile: Color
        @Composable get() = MaterialTheme.colorScheme.errorContainer
    val DangerContent: Color
        @Composable get() = MaterialTheme.colorScheme.error
    val Muted: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

    val Gradient: Brush
        @Composable get() = Brush.horizontalGradient(listOf(GradientStart, GradientEnd))

    val GradientDiagonal: Brush
        @Composable get() = Brush.linearGradient(listOf(GradientStart, GradientEnd))

    val PanelBrush: Brush
        @Composable get() = Brush.verticalGradient(listOf(Panel, PanelDeep))

    /** Brush satu warna (untuk latar polos yang tetap bertipe Brush). */
    @Composable
    fun solid(color: Color): Brush = Brush.horizontalGradient(listOf(color, color))
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
                shape = nevShape(24.dp)
            )
            .background(NevRed.PanelBrush, nevShape(24.dp))
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
            shape = nevShape(14.dp),
            color = Color.Transparent,
            border = if (danger) BorderStroke(1.dp, NevRed.DangerContent.copy(alpha = 0.6f)) else null
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        if (danger) NevRed.solid(NevRed.DangerTile)
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
            color = if (danger) NevRed.DangerContent else NevRed.OnPanel,
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
        NevPillStyle.Neutral -> NevRed.OnPanel
        NevPillStyle.Danger -> NevRed.DangerContent
    }
    val circleColor = when (style) {
        NevPillStyle.Gradient -> NevRed.OnGradient
        NevPillStyle.Neutral -> NevRed.SurfaceHighest
        NevPillStyle.Danger -> NevRed.DangerTile
    }
    val circleIcon = when (style) {
        NevPillStyle.Gradient -> NevRed.GradientEnd
        else -> contentColor
    }
    val shape = nevPillShape()
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
                        NevPillStyle.Neutral -> NevRed.solid(NevRed.SurfaceHigh)
                        NevPillStyle.Danger -> NevRed.solid(NevRed.DangerTile.copy(alpha = 0.6f))
                    }
                )
                .padding(start = 6.dp, end = 18.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(circleColor, nevPillShape()),
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
    val shape = nevShape(18.dp)
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
                    else NevRed.solid(NevRed.Panel)
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
                        if (selected) NevRed.OnGradient else NevRed.OnPanel
            ) {
                content()
            }
        }
    }
}

/**
 * Tab atas (ikon + label di bawahnya) bergaya Nev; yang terpilih memakai gradasi merah-oranye.
 * Dipakai di layar Pengaturan dan Unduh.
 */
@Composable
fun NevTopTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit
) {
    val shape = nevShape(16.dp)
    Surface(
        onClick = onClick,
        modifier = (if (selected) modifier.nevGlow(shape, 10.dp, 0.9f) else modifier)
            .semantics { role = Role.Tab },
        shape = shape,
        color = Color.Transparent,
        border = if (selected) BorderStroke(1.dp, NevRed.OutlineBright) else null
    ) {
        Column(
            modifier = Modifier
                .then(
                    if (selected) Modifier.background(NevRed.GradientDiagonal) else Modifier
                )
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides
                        if (selected) NevRed.OnGradient else NevRed.Muted
            ) {
                icon()
                label()
            }
        }
    }
}

/**
 * Tombol kecil bergradasi merah-oranye (mis. "Main" di kartu server).
 */
@Composable
fun NevGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val shape = nevPillShape()
    Surface(
        onClick = onClick,
        modifier = modifier
            .nevGlow(shape, 10.dp, 0.9f)
            .semantics { role = Role.Button },
        shape = shape,
        color = Color.Transparent,
        contentColor = NevRed.OnGradient
    ) {
        Row(
            modifier = Modifier
                .background(NevRed.Gradient)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }
}

/** Warna kolom teks bergaris oranye gelap (kolom cari, dialog tambah server, dll). */
@Composable
fun nevTextFieldColors() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NevRed.OutlineBright,
    unfocusedBorderColor = NevRed.Outline,
    cursorColor = NevRed.GradientEnd,
    focusedContainerColor = NevRed.Panel,
    unfocusedContainerColor = NevRed.Panel
)

/** Border gradasi merah menyala untuk kartu. */
val NevCardBorder: BorderStroke
    @Composable get() = BorderStroke(
        LocalNevStyle.current.cardBorderWidth,
        Brush.linearGradient(
            listOf(
                NevRed.GradientStart.copy(alpha = 0.85f),
                NevRed.Outline,
                NevRed.GradientEnd.copy(alpha = 0.85f)
            )
        )
    )

/**
 * Efek glow merah: bayangan berwarna tema di sekeliling bentuk (di luar isi, tidak menutupi konten).
 * @param radius jarak sebar cahaya
 * @param intensity 0..1, kekuatan cahaya
 */
fun Modifier.nevGlow(
    shape: Shape,
    radius: Dp = 8.dp,
    intensity: Float = 0.6f
): Modifier = composed {
    val c = MaterialTheme.colorScheme.primary
    val style = LocalNevStyle.current
    val glow = (intensity * style.glowIntensityScale).coerceIn(0f, 1f)
    shadow(
        elevation = radius * style.glowRadiusScale,
        shape = shape,
        clip = false,
        ambientColor = c.copy(alpha = glow),
        spotColor = c.copy(alpha = glow)
    )
}

/**
 * Tab atas bergaya kapsul untuk layar Unduh: lingkaran ikon di kiri, nomor kecil ("01") di atas label.
 * Yang terpilih memakai gradasi merah-oranye.
 */
@Composable
fun NevPillTab(
    number: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit
) {
    val shape = nevPillShape()
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { role = Role.Tab },
        shape = shape,
        color = Color.Transparent,
        border = if (selected) BorderStroke(1.dp, NevRed.OutlineBright) else null
    ) {
        Row(
            modifier = Modifier
                .then(if (selected) Modifier.background(NevRed.Gradient) else Modifier)
                .padding(start = 6.dp, end = 18.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val content = if (selected) NevRed.OnGradient else NevRed.OnPanel
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(
                        1.dp,
                        if (selected) NevRed.OnGradient.copy(alpha = 0.35f) else NevRed.Outline,
                        nevPillShape()
                    )
                    .background(
                        if (selected) NevRed.OnGradient.copy(alpha = 0.18f)
                        else NevRed.SurfaceHigh.copy(alpha = 0.6f),
                        nevPillShape()
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides
                            if (selected) content else NevRed.Muted
                ) {
                    icon()
                }
            }
            Column {
                Text(
                    text = "%02d".format(number),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = if (selected) NevRed.OnGradient.copy(alpha = 0.8f) else NevRed.GradientEnd
                )
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides content
                ) {
                    androidx.compose.material3.ProvideTextStyle(
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) {
                        label()
                    }
                }
            }
        }
    }
}

/** Wadah kapsul bergaris gradasi untuk deretan tab atas. */
@Composable
fun NevPillBar(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = nevPillShape()
    Box(
        modifier = modifier
            .border(
                BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(listOf(NevRed.OutlineBright.copy(alpha = 0.8f), NevRed.Outline))
                ),
                shape
            )
            .background(NevRed.Panel.copy(alpha = 0.72f), shape)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        content()
    }
}

/** Ikon berlian (kotak diputar 45°) kecil untuk chip filter. */
@Composable
fun NevDiamondIcon(
    color: Color,
    modifier: Modifier = Modifier,
    filled: Boolean = false
) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(18.dp)) {
        val s = size.minDimension
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(s / 2f, 0f)
            lineTo(s, s / 2f)
            lineTo(s / 2f, s)
            lineTo(0f, s / 2f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.12f)
        )
        if (filled) {
            val inner = androidx.compose.ui.graphics.Path().apply {
                moveTo(s / 2f, s * 0.28f)
                lineTo(s * 0.72f, s / 2f)
                lineTo(s / 2f, s * 0.72f)
                lineTo(s * 0.28f, s / 2f)
                close()
            }
            drawPath(inner, color)
        }
    }
}

/**
 * Chip filter bergaya Nev: berlian + teks kapital monospasi.
 * Terpilih: gradasi merah-oranye; tidak terpilih: panel gelap bergaris oranye.
 */
@Composable
fun NevFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = nevShape(16.dp)
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { role = Role.Checkbox },
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (selected) NevRed.OutlineBright else NevRed.Outline.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .background(
                    if (selected) NevRed.GradientDiagonal
                    else NevRed.solid(NevRed.Panel.copy(alpha = 0.72f))
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NevDiamondIcon(
                color = if (selected) NevRed.OnGradient else NevRed.GradientEnd.copy(alpha = 0.9f),
                filled = selected
            )
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (selected) NevRed.OnGradient else NevRed.OnPanel,
                maxLines = 1
            )
        }
    }
}

/** Bingkai besar bergaris gradasi oranye untuk satu layar penuh (dialog-like). */
@Composable
fun NevPanelFrame(
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 36.dp,
    content: @Composable () -> Unit
) {
    val shape = nevShape(cornerRadius)
    Box(
        modifier = modifier
            .border(
                BorderStroke(
                    1.2.dp,
                    Brush.horizontalGradient(listOf(NevRed.OutlineBright.copy(alpha = 0.8f), NevRed.Outline))
                ),
                shape
            )
            .background(
                Brush.verticalGradient(
                    listOf(NevRed.Panel.copy(alpha = 0.82f), NevRed.PanelDeep.copy(alpha = 0.9f))
                ),
                shape
            )
            .padding(12.dp)
    ) {
        content()
    }
}

/** Panel dalam (mis. panel info di kiri) dengan garis tepi berwarna aksen. */
@Composable
fun NevInnerPanel(
    modifier: Modifier = Modifier,
    accent: Color = NevRed.OutlineBright,
    cornerRadius: androidx.compose.ui.unit.Dp = 26.dp,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val shape = nevShape(cornerRadius)
    Column(
        modifier = modifier
            .border(1.dp, accent.copy(alpha = 0.75f), shape)
            .background(
                Brush.verticalGradient(
                    listOf(accent.copy(alpha = 0.14f), NevRed.PanelDeep.copy(alpha = 0.85f))
                ),
                shape
            )
            .padding(16.dp),
        content = content
    )
}

/**
 * Tombol aksi bergradasi merah-oranye (mis. "Pasang"), ikon opsional di kiri teks.
 */
@Composable
fun NevActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    enabled: Boolean = true,
    cornerRadius: androidx.compose.ui.unit.Dp = 20.dp
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.semantics { role = Role.Button },
        shape = nevShape(cornerRadius),
        color = Color.Transparent,
        contentColor = NevRed.OnGradient
    ) {
        Row(
            modifier = Modifier
                .background(
                    if (enabled) NevRed.Gradient
                    else NevRed.solid(NevRed.SurfaceHigh)
                )
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            if (painter != null) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painter,
                    contentDescription = null
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
