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

package com.movtery.zalithlauncher.ui.screens.content.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.plugin.vpl.PluginTrustManager
import com.movtery.zalithlauncher.game.plugin.vpl.formatFingerprint
import com.movtery.zalithlauncher.game.plugin.vpl.singleLine
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.vpl.verifiedpluginload.model.TrustActionResult
import com.vpl.verifiedpluginload.model.TrustActionStatus
import kotlinx.coroutines.launch

private const val TAG = "PluginTrustManageScreen"

/** A pending revocation the user still has to confirm. */
private sealed interface RevokeOperation {
    data object None : RevokeOperation
    data class Author(val entry: PluginTrustManager.AuthorTrustEntry) : RevokeOperation
    data class Key(val entry: PluginTrustManager.KeyTrustEntry) : RevokeOperation
}

@Composable
fun PluginTrustManageScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current

    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.PluginTrustManager, settingsScreenKey, false)
    ) { isVisible ->
        val yOffset by swapAnimateDpAsState(
            targetValue = (-40).dp,
            swapIn = isVisible
        )

        var data by remember { mutableStateOf<PluginTrustManager.TrustManagementData?>(null) }
        var refreshToken by remember { mutableStateOf(0) }
        var operation by remember { mutableStateOf<RevokeOperation>(RevokeOperation.None) }

        LaunchedEffect(refreshToken) {
            data = runCatching { PluginTrustManager.load(context) }
                .onFailure { Logger.error(TAG, "Could not load plugin trust records", it) }
                .getOrNull()
        }

        RevokeConfirmDialog(
            operation = operation,
            dismiss = { operation = RevokeOperation.None },
            onConfirmed = { refreshToken++ },
            submitError = submitError
        )

        SettingsCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 12.dp)
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
            position = CardPosition.Single
        ) {
            CardTitleLayout {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconTextButton(
                        onClick = { refreshToken++ },
                        painter = painterResource(R.drawable.ic_refresh),
                        contentDescription = stringResource(R.string.generic_refresh),
                        text = stringResource(R.string.generic_refresh),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScrollWithBar(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.plugin_trust_management_description),
                    style = MaterialTheme.typography.bodySmall
                )

                val current = data
                if (current == null) {
                    Text(
                        text = stringResource(R.string.generic_loading),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    return@Column
                }

                // A damaged record that the store rolled back is the user's business: the entries
                // they see may not be the ones they last approved.
                if (current.recoveredFromCorruption) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.plugin_trust_management_recovered_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.plugin_trust_management_recovered_message),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                SectionHeading(
                    stringResource(
                        R.string.plugin_trust_management_authors_count,
                        current.authorEntries.size
                    )
                )
                if (current.authorEntries.isEmpty()) {
                    EmptyNote(stringResource(R.string.plugin_trust_management_authors_empty))
                } else {
                    current.authorEntries.forEach { entry ->
                        AuthorTrustRow(entry) { operation = RevokeOperation.Author(entry) }
                    }
                }

                SectionHeading(
                    stringResource(
                        R.string.plugin_trust_management_keys_count,
                        current.keyEntries.size
                    )
                )
                if (current.keyEntries.isEmpty()) {
                    EmptyNote(stringResource(R.string.plugin_trust_management_keys_empty))
                } else {
                    current.keyEntries.forEach { entry ->
                        KeyTrustRow(entry) { operation = RevokeOperation.Key(entry) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(text: String) {
    Column {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun EmptyNote(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun AuthorTrustRow(
    entry: PluginTrustManager.AuthorTrustEntry,
    onRevoke: () -> Unit
) {
    TrustRow(
        // A record whose publisher the signed list no longer describes still has to be revocable,
        // so it is shown by UUID rather than hidden.
        title = entry.author?.name?.let { singleLine(it, 128) }
            ?: stringResource(R.string.plugin_trust_management_unknown_author),
        subtitle = entry.author?.let { singleLine(it.description, 128) }
            ?: stringResource(R.string.plugin_trust_management_author_unavailable),
        detail = singleLine(entry.authorUuid, 128),
        monospaceDetail = true,
        affectedPlugins = entry.affectedPlugins,
        onRevoke = onRevoke
    )
}

@Composable
private fun KeyTrustRow(
    entry: PluginTrustManager.KeyTrustEntry,
    onRevoke: () -> Unit
) {
    TrustRow(
        title = stringResource(R.string.plugin_trust_management_certificate),
        subtitle = null,
        detail = formatFingerprint(entry.keyHash.sha256),
        monospaceDetail = true,
        affectedPlugins = entry.affectedPlugins,
        onRevoke = onRevoke
    )
}

@Composable
private fun TrustRow(
    title: String,
    subtitle: String?,
    detail: String?,
    monospaceDetail: Boolean,
    affectedPlugins: List<PluginTrustManager.InstalledPlugin>,
    onRevoke: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (detail != null) {
                Text(
                    text = detail,
                    style = if (monospaceDetail)
                        MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    else
                        MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (affectedPlugins.isEmpty()) {
                    stringResource(R.string.plugin_trust_management_no_installed_impact)
                } else {
                    stringResource(
                        R.string.plugin_trust_management_impact,
                        affectedPlugins.joinToString(", ") { singleLine(it.label, 64) }
                    )
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconTextButton(
            onClick = onRevoke,
            painter = painterResource(R.drawable.ic_delete_outlined),
            contentDescription = stringResource(R.string.plugin_trust_management_revoke),
            text = stringResource(R.string.plugin_trust_management_revoke)
        )
    }
}

@Composable
private fun RevokeConfirmDialog(
    operation: RevokeOperation,
    dismiss: () -> Unit,
    onConfirmed: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    /**
     * A revocation that reports SUCCESS but leaves the record in place would tell the user their
     * trust is gone while the next launch still loads the plugin, so anything but SUCCESS surfaces.
     */
    fun runRevoke(action: suspend () -> TrustActionResult) {
        scope.launch {
            runCatching { action() }
                .onSuccess { result ->
                    if (result.status != TrustActionStatus.SUCCESS) {
                        submitError(
                            ErrorViewModel.ThrowableMessage(
                                title = androidText(R.string.plugin_trust_management_revoke),
                                message = androidText(
                                    R.string.plugin_trust_management_error,
                                    result.status.name
                                )
                            )
                        )
                    }
                    onConfirmed()
                }
                .onFailure { e ->
                    Logger.error(TAG, "Could not revoke plugin trust", e)
                    submitError(
                        ErrorViewModel.ThrowableMessage(
                            title = androidText(R.string.plugin_trust_management_revoke),
                            message = androidText(
                                R.string.plugin_trust_management_error,
                                e.getMessageOrToString()
                            )
                        )
                    )
                    onConfirmed()
                }
        }
    }

    when (operation) {
        is RevokeOperation.None -> Unit

        is RevokeOperation.Author -> {
            val name = operation.entry.author?.name?.let { singleLine(it, 128) }
                ?: operation.entry.authorUuid
            SimpleAlertDialog(
                title = stringResource(R.string.plugin_trust_management_revoke),
                text = stringResource(R.string.plugin_trust_management_revoke_author_confirm, name),
                onConfirm = {
                    runRevoke { PluginTrustManager.revokeAuthor(context, operation.entry.authorUuid) }
                    dismiss()
                },
                onDismiss = dismiss
            )
        }

        is RevokeOperation.Key -> {
            val name = operation.entry.keyHash.sha256.take(16)
            SimpleAlertDialog(
                title = stringResource(R.string.plugin_trust_management_revoke),
                text = stringResource(R.string.plugin_trust_management_revoke_key_confirm, name),
                onConfirm = {
                    runRevoke { PluginTrustManager.revokeKey(context, operation.entry.keyHash) }
                    dismiss()
                },
                onDismiss = dismiss
            )
        }
    }
}
