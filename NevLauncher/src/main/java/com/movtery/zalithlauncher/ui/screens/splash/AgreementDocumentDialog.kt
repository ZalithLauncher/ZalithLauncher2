/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.ui.screens.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.readAssetFile
import com.movtery.zalithlauncher.legal.AvailableAgreement
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.components.MarkdownView
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private sealed interface DocumentOperation {
    data object Loading : DocumentOperation
    data class Loaded(val content: String): DocumentOperation
    data object Failed : DocumentOperation
}

@Composable
fun AgreementDocumentDialog(
    document: AvailableAgreement.DocumentFile.Data,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var content by remember {
        mutableStateOf<DocumentOperation>(DocumentOperation.Loading)
    }

    LaunchedEffect(document.path) {
        content = DocumentOperation.Loading
        content = withContext(Dispatchers.IO) {
            runCatching {
                val string = context.readAssetFile(document.path)
                DocumentOperation.Loaded(string)
            }.onFailure { e ->
                Logger.warning(
                    "AgreementDocumentDialog",
                    "Failed to read agreement document: ${document.path}",
                    e
                )
            }.getOrDefault(DocumentOperation.Failed)
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f)
                .padding(all = 6.dp),
            shape = MaterialTheme.shapes.large,
            color = cardColor(false),
            contentColor = onCardColor(),
            shadowElevation = 6.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                CardTitleLayout(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .padding(all = 4.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .weight(1f),
                            text = document.title
                        )

                        TextButton(
                            onClick = onDismissRequest
                        ) {
                            Text(text = stringResource(R.string.generic_close))
                        }
                    }
                }

                when (val operation = content) {
                    is DocumentOperation.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is DocumentOperation.Loaded -> {
                        val listState = rememberScrollState()
                        MarkdownView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .fadeEdge(listState)
                                .verticalScrollWithBar(listState)
                                .padding(all = 12.dp),
                            content = operation.content.ifEmpty {
                                stringResource(R.string.agreement_content_unavailable)
                            }
                        )
                    }
                    is DocumentOperation.Failed -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.agreement_content_unavailable),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
