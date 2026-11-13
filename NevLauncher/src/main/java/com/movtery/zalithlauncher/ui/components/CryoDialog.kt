/*
 * Nev Launcher
 * Dialog bergaya Cryogenic: panel gelap, garis tepi gradasi, aksen siku di sudut,
 * ubin ikon, label "CRYO // SYSTEM", judul dengan kata aksen, dan tombol gradasi penuh.
 */

package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.theme.LocalNevStyle
import com.movtery.zalithlauncher.ui.theme.NevRed
import com.movtery.zalithlauncher.ui.theme.nevGlow

/** true bila gaya desain aktif memakai dialog ala Cryogenic. */
@Composable
internal fun useCryoDialog(): Boolean = LocalNevStyle.current.cutCorners

/**
 * Dialog Cryogenic.
 *
 * @param title judul; kata terakhir diberi warna aksen
 * @param confirmText teks tombol utama (gradasi, selebar dialog)
 * @param dismissText bila tidak null, tombol sekunder ditampilkan di samping tombol utama
 */
@Composable
internal fun CryoDialog(
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    dismissText: String? = null,
    onDismiss: () -> Unit = onDismissRequest,
    iconRes: Int = R.drawable.ic_info_filled,
    dismissOnOutside: Boolean = true,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = { if (dismissOnOutside) onDismissRequest() },
        properties = DialogProperties(
            dismissOnBackPress = dismissOnOutside,
            dismissOnClickOutside = dismissOnOutside
        )
    ) {
        val accent = MaterialTheme.colorScheme.primary
        val panelShape = RoundedCornerShape(24.dp)
        val bracketColor = NevRed.GradientStart

        Box(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .nevGlow(panelShape, 14.dp, 0.5f)
                .background(NevRed.PanelBrush, panelShape)
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(listOf(NevRed.OutlineBright, NevRed.Outline, NevRed.OutlineBright))
                    ),
                    panelShape
                )
                .drawBehind {
                    //aksen siku: kanan-atas dan kiri-bawah
                    val inset = 10.dp.toPx()
                    val len = 36.dp.toPx()
                    val sw = 2.dp.toPx()
                    val stroke = Stroke(width = sw, cap = StrokeCap.Round)
                    val r = 14.dp.toPx()
                    //kanan-atas
                    drawArc(
                        color = bracketColor,
                        startAngle = 270f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(size.width - inset - r * 2, inset),
                        size = Size(r * 2, r * 2),
                        style = stroke
                    )
                    drawLine(bracketColor, Offset(size.width - inset - r * 2 + r, inset), Offset(size.width - inset - len, inset), sw, StrokeCap.Round)
                    drawLine(bracketColor, Offset(size.width - inset, inset + r), Offset(size.width - inset, inset + len), sw, StrokeCap.Round)
                    //kiri-bawah
                    drawArc(
                        color = bracketColor,
                        startAngle = 90f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(inset, size.height - inset - r * 2),
                        size = Size(r * 2, r * 2),
                        style = stroke
                    )
                    drawLine(bracketColor, Offset(inset + r, size.height - inset), Offset(inset + len, size.height - inset), sw, StrokeCap.Round)
                    drawLine(bracketColor, Offset(inset, size.height - inset - r), Offset(inset, size.height - inset - len), sw, StrokeCap.Round)
                }
                .padding(horizontal = 26.dp, vertical = 24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                //kepala: ubin ikon + label + judul
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(NevRed.PanelDeep, RoundedCornerShape(16.dp))
                            .border(1.5.dp, accent, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(NevRed.GradientDiagonal, RoundedCornerShape(50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(iconRes),
                                contentDescription = null,
                                tint = NevRed.OnGradient
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "CRYO // SYSTEM",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = NevRed.GradientEnd
                        )
                        Text(
                            text = accentTitle(title, NevRed.GradientEnd),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = NevRed.OnPanel
                        )
                    }
                }

                //garis pemisah gradasi
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(NevRed.OutlineBright, NevRed.Outline.copy(alpha = 0.2f))
                            )
                        )
                )

                //isi
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .fadeEdge(state = scrollState)
                        .verticalScrollWithBar(state = scrollState)
                ) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.material3.LocalContentColor provides NevRed.OnPanel
                    ) {
                        content()
                    }
                }

                Spacer(Modifier.height(2.dp))

                //tombol
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dismissText != null) {
                        CryoButton(
                            modifier = Modifier.weight(1f),
                            text = dismissText,
                            filled = false,
                            onClick = onDismiss
                        )
                    }
                    CryoButton(
                        modifier = Modifier.weight(1f),
                        text = confirmText,
                        filled = true,
                        onClick = onConfirm
                    )
                }
            }
        }
    }
}

@Composable
private fun CryoButton(
    text: String,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)
    Surface(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 52.dp)
            .semantics { role = Role.Button },
        shape = shape,
        color = Color.Transparent,
        contentColor = if (filled) NevRed.OnGradient else NevRed.OnPanel,
        border = if (filled) {
            BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
        } else {
            BorderStroke(1.dp, NevRed.OutlineBright)
        }
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (filled) Modifier.background(NevRed.Gradient)
                    else Modifier.background(NevRed.PanelDeep)
                )
                .padding(horizontal = 18.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            MarqueeText(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/** Kata terakhir pada judul diberi warna aksen. */
private fun accentTitle(title: String, accent: Color) = buildAnnotatedString {
    val idx = title.trimEnd().lastIndexOf(' ')
    if (idx <= 0) {
        withStyle(SpanStyle(color = accent)) { append(title) }
    } else {
        append(title.substring(0, idx + 1))
        withStyle(SpanStyle(color = accent)) { append(title.substring(idx + 1)) }
    }
}
