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

package com.movtery.zalithlauncher.filemanager.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.filemanager.logic.task.TaskKind
import com.movtery.zalithlauncher.filemanager.logic.task.TaskProgress
import com.movtery.zalithlauncher.filemanager.ui.components.FmDialogSurface
import com.movtery.zalithlauncher.filemanager.ui.theme.fmSecondaryTextColor
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.utils.file.formatFileSize

/**
 * 任务进度对话框，展示进度、当前条目与速度。
 * @param progress 任务进度状态，为 null 时不显示弹窗
 * @param onCancel 取消任务并关闭弹窗
 */
@Composable
fun FmProgressDialog(
    progress: TaskProgress?,
    onCancel: () -> Unit
) {
    if (progress == null) return
    val ratio = progress.ratio.coerceIn(0f, 1f)

    FmDialogSurface(onDismissRequest = onCancel) {
        Text(
            text = stringResource(R.string.fm_progress_title),
            style = MaterialTheme.typography.titleMedium
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            if (progress.total > 0) {
                Text(
                    text = "${progress.completed} / ${progress.total}",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = stringResource(progress.kind.loadingLabelRes()),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            progress.currentName?.let { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    color = fmSecondaryTextColor(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (progress.bytesPerSecond > 0L) {
                Text(
                    text = "${formatFileSize(progress.bytesPerSecond)}/s",
                    style = MaterialTheme.typography.bodySmall,
                    color = fmSecondaryTextColor(),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onCancel
        ) {
            MarqueeText(text = stringResource(R.string.generic_cancel))
        }
    }
}

private fun TaskKind.loadingLabelRes(): Int = when (this) {
    TaskKind.LIST, TaskKind.SEARCH -> R.string.generic_loading
    TaskKind.COPY -> R.string.fm_progress_kind_copy
    TaskKind.MOVE -> R.string.fm_progress_kind_move
    TaskKind.DELETE -> R.string.fm_progress_kind_delete
    TaskKind.COMPRESS -> R.string.fm_progress_kind_compress
    TaskKind.EXTRACT -> R.string.fm_progress_kind_extract
    TaskKind.IMPORT -> R.string.fm_progress_kind_import
    TaskKind.TRASH_RESTORE -> R.string.fm_progress_kind_trash_restore
    TaskKind.TRASH_PURGE -> R.string.fm_progress_kind_trash_purge
    TaskKind.TRASH_CLEAR -> R.string.fm_progress_kind_trash_clear
}
