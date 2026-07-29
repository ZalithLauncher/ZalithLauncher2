/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.plugin.vpl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PluginTrustDialogHost() {
    val request by PluginTrustDialogState.currentRequest.collectAsState()

    request?.let { req ->
        when (req) {
            is PluginTrustDialogState.DialogRequest.AuthorTrust -> {
                AuthorTrustDialog(
                    title = req.title,
                    summary = req.summary,
                    message = req.message,
                    sections = req.sections,
                    severity = req.severity,
                    onTrust = { req.deferred.complete(PluginTrustDialogState.DialogAction.TRUST) },
                    onCancel = { req.deferred.complete(PluginTrustDialogState.DialogAction.CANCEL) }
                )
            }

            is PluginTrustDialogState.DialogRequest.KeyTrust -> {
                KeyTrustDialog(
                    title = req.title,
                    summary = req.summary,
                    message = req.message,
                    sections = req.sections,
                    cooldownSeconds = req.cooldownSeconds,
                    cooldownGeneration = req.cooldownGeneration,
                    onTrust = { req.deferred.complete(PluginTrustDialogState.DialogAction.TRUST) },
                    onCancel = { req.deferred.complete(PluginTrustDialogState.DialogAction.CANCEL) }
                )
            }

            is PluginTrustDialogState.DialogRequest.Error -> {
                ErrorDialog(
                    title = req.title,
                    summary = req.summary,
                    message = req.message,
                    sections = req.sections,
                    onClose = { req.deferred.complete(PluginTrustDialogState.DialogAction.CANCEL) }
                )
            }
        }
    }
}

@Composable
private fun AuthorTrustDialog(
    title: AndroidStringText,
    summary: AndroidStringText?,
    message: AndroidStringText?,
    sections: List<TrustSection>,
    severity: PluginTrustDialogState.Severity,
    onTrust: () -> Unit,
    onCancel: () -> Unit
) {
    val severityInfo = when (severity) {
        PluginTrustDialogState.Severity.ERROR -> SeverityInfo(
            label = stringResource(R.string.plugin_trust_level_error),
            icon = painterResource(R.drawable.ic_close),
            color = Color(0xFFB42318)
        )

        PluginTrustDialogState.Severity.WARNING -> SeverityInfo(
            label = stringResource(R.string.plugin_trust_level_warning),
            icon = painterResource(R.drawable.ic_warning_filled),
            color = Color(0xFFB54708)
        )

        PluginTrustDialogState.Severity.INFO -> SeverityInfo(
            label = stringResource(R.string.plugin_trust_level_info),
            icon = painterResource(R.drawable.ic_info_filled),
            color = Color(0xFF2563EB)
        )
    }

    TrustDialog(
        onDismissRequest = onCancel,
        title = {
            SeverityTitle(
                info = severityInfo,
                title = title
            )
        },
        body = {
            TrustDialogContent(
                summary = summary,
                message = message,
                sections = sections
            )
        },
        confirm = {
            Button(onClick = onTrust) {
                MarqueeText(text = stringResource(R.string.plugin_trust_author_action))
            }
        },
        cancel = {
            FilledTonalButton(onClick = onCancel) {
                MarqueeText(text = stringResource(R.string.plugin_trust_cancel))
            }
        }
    )
}

@Composable
private fun KeyTrustDialog(
    title: AndroidStringText,
    summary: AndroidStringText?,
    message: AndroidStringText?,
    sections: List<TrustSection>,
    cooldownSeconds: Int,
    cooldownGeneration: Int,
    onTrust: () -> Unit,
    onCancel: () -> Unit
) {
    var remainingSeconds by remember { mutableIntStateOf(cooldownSeconds) }
    val isReady = remainingSeconds <= 0

    // Keyed on the generation as well, so a handled configuration change restarts the countdown
    // rather than leaving an already-enabled button behind.
    LaunchedEffect(cooldownSeconds, cooldownGeneration) {
        remainingSeconds = cooldownSeconds
        while (remainingSeconds > 0) {
            delay(1_000L.milliseconds)
            remainingSeconds--
        }
    }

    TrustDialog(
        onDismissRequest = onCancel,
        title = {
            Column {
                SeverityTitle(
                    info = SeverityInfo(
                        label = stringResource(R.string.plugin_trust_level_warning),
                        icon = painterResource(R.drawable.ic_warning_filled),
                        color = Color(0xFFB54708)
                    ),
                    title = title
                )
            }
        },
        body = {
            TrustDialogContent(
                summary = summary,
                message = message,
                sections = sections
            )
        },
        confirm = {
            Button(
                onClick = onTrust,
                enabled = isReady
            ) {
                MarqueeText(
                    text = if (isReady) {
                        stringResource(R.string.plugin_trust_key_action)
                    } else {
                        stringResource(R.string.plugin_trust_key_wait, remainingSeconds)
                    }
                )
            }
        },
        cancel = {
            FilledTonalButton(onClick = onCancel) {
                MarqueeText(text = stringResource(R.string.plugin_trust_cancel))
            }
        }
    )
}

@Composable
private fun ErrorDialog(
    title: AndroidStringText,
    summary: AndroidStringText?,
    message: AndroidStringText?,
    sections: List<TrustSection>,
    onClose: () -> Unit
) {
    TrustDialog(
        onDismissRequest = onClose,
        title = {
            Column {
                SeverityTitle(
                    info = SeverityInfo(
                        label = stringResource(R.string.plugin_trust_level_error),
                        icon = painterResource(R.drawable.ic_close),
                        color = Color(0xFFB42318)
                    ),
                    title = title
                )
            }
        },
        body = {
            TrustDialogContent(
                summary = summary,
                message = message,
                sections = sections
            )
        },
        confirm = {
            Button(onClick = onClose) {
                MarqueeText(text = stringResource(R.string.plugin_trust_close))
            }
        }
    )
}

@Composable
private fun TrustDialogContent(
    summary: AndroidStringText?,
    message: AndroidStringText?,
    sections: List<TrustSection>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScrollWithBar(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (summary != null) {
            AndroidStringText(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (message != null) {
            AndroidStringText(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        sections.filter { it.hasContent }.forEach { section ->
            TrustSectionRow(section)
        }
    }
}

@Composable
private fun TrustSectionRow(section: TrustSection) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(section.title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        section.facts.forEach { fact ->
            if (fact.value != null) {
                FactRow(fact)
            }
        }
    }
}

@Composable
private fun FactRow(fact: TrustFact) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(fact.label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.widthIn(min = 80.dp, max = 100.dp)
        )
        if (fact.value != null) {
            AndroidStringText(
                text = fact.value,
                style = if (fact.monospace)
                    MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                else
                    MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
        }
    }
}



@Composable
private fun TrustDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    body: @Composable () -> Unit,
    confirm: (@Composable () -> Unit)? = null,
    cancel: (@Composable () -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 6.dp)
                    .heightIn(max = maxHeight - 12.dp)
                    .fillMaxWidth(0.65f)
                    .wrapContentHeight(),
                shadowElevation = 6.dp,
                color = cardColor(false),
                contentColor = onCardColor(),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    title()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, false)
                    ) {
                        body()
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        cancel?.invoke()
                        confirm?.invoke()
                    }
                }
            }
        }
    }
}

@Composable
private fun SeverityTitle(
    info: SeverityInfo,
    title: AndroidStringText,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = info.color.copy(alpha = 0.15f),
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                painter = info.icon,
                contentDescription = null,
                tint = info.color,
                modifier = Modifier
                    .padding(4.dp)
                    .size(24.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = info.label,
                style = MaterialTheme.typography.labelMedium,
                color = info.color,
                fontWeight = FontWeight.Bold
            )
            AndroidStringText(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

private data class SeverityInfo(
    val label: String,
    val icon: Painter,
    val color: Color
)
