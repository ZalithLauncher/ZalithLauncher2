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

package com.movtery.zalithlauncher.ui.screens.content

import android.content.Context
import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.utils.file.shareFile
import com.movtery.zalithlauncher.utils.logging.GameLogArchiver
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormat
import java.util.Date

/** NusaLauncher: 已归档的一条游戏日志 */
private data class LogEntry(
    val file: File,
    val timeMillis: Long,
    val title: String,
    val subtitle: String
)

private fun loadEntries(context: Context): List<LogEntry> {
    val files = PathManager.DIR_GAME_LOGS.listFiles()
        ?.filter { it.isFile && GameLogArchiver.isArchiveName(it.name) }
        ?: return emptyList()
    val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    return files.map { file ->
        val info = GameLogArchiver.parseName(file.name)
        val time = info?.timeMillis ?: file.lastModified()
        val size = Formatter.formatShortFileSize(context, file.length())
        val subtitle = if (info != null) "${info.renderer} · $size" else size
        LogEntry(file, time, dateFormat.format(Date(time)), subtitle)
    }.sortedByDescending { it.timeMillis }
}

/**
 * NusaLauncher: 日志历史。列出每次游戏会话自动归档的日志（保留 30 天），可查看、分享、删除。
 */
@Composable
fun LogHistoryScreen(
    key: NormalNavKey.LogHistory,
    backStackViewModel: ScreenBackStackViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var entries by remember { mutableStateOf<List<LogEntry>?>(null) }
    var pendingDelete by remember { mutableStateOf<LogEntry?>(null) }
    var confirmDeleteAll by remember { mutableStateOf(false) }

    suspend fun reload() {
        entries = withContext(Dispatchers.IO) {
            runCatching { loadEntries(context) }.getOrDefault(emptyList())
        }
    }

    LaunchedEffect(Unit) { reload() }

    BaseScreen(
        screenKey = key,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.log_history_summary, GameLogArchiver.RETENTION_DAYS),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!entries.isNullOrEmpty()) {
                        TextButton(onClick = { confirmDeleteAll = true }) {
                            Text(stringResource(R.string.log_history_delete_all))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                val list = entries
                when {
                    list == null -> {
                        CircularProgressIndicator()
                    }
                    list.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.log_history_empty),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(list, key = { it.file.name }) { entry ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = entry.title,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = entry.subtitle,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(onClick = {
                                                backStackViewModel.mainScreen.backStack
                                                    .navigateToLogView(entry.file.absolutePath)
                                            }) { Text(stringResource(R.string.generic_view)) }
                                            TextButton(onClick = {
                                                shareFile(context = context, file = entry.file)
                                            }) { Text(stringResource(R.string.generic_share)) }
                                            TextButton(onClick = { pendingDelete = entry }) {
                                                Text(stringResource(R.string.generic_delete))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.log_history_delete_confirm)) },
            text = { Text(entry.title) },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = null
                    scope.launch {
                        withContext(Dispatchers.IO) { entry.file.delete() }
                        reload()
                    }
                }) { Text(stringResource(R.string.generic_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.generic_cancel))
                }
            }
        )
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text(stringResource(R.string.log_history_delete_all_confirm, entries?.size ?: 0)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDeleteAll = false
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            PathManager.DIR_GAME_LOGS.listFiles()
                                ?.filter { it.isFile && GameLogArchiver.isArchiveName(it.name) }
                                ?.forEach { it.delete() }
                        }
                        reload()
                    }
                }) { Text(stringResource(R.string.generic_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAll = false }) {
                    Text(stringResource(R.string.generic_cancel))
                }
            }
        )
    }
}
