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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.filemanager.ui.components.FmAlertDialog
import com.movtery.zalithlauncher.filemanager.ui.components.PropertyRow
import com.movtery.zalithlauncher.filemanager.viewmodel.DirScanUiState
import com.movtery.zalithlauncher.utils.file.formatFileSize

/**
 * 回收站条目属性对话框。
 * @param name 条目名称
 * @param sourcePath 条目被删除前的源路径
 * @param isDirectory 是否为目录
 * @param sizeText 大小文本
 * @param deletedText 删除时间文本
 * @param dirScan 目录扫描状态，用于展示目录的异步统计结果
 * @param onDismiss 关闭对话框
 */
@Composable
fun FmTrashPropertyDialog(
    name: String,
    sourcePath: String,
    isDirectory: Boolean,
    sizeText: String,
    deletedText: String,
    dirScan: DirScanUiState? = null,
    onDismiss: () -> Unit
) {
    val dirStats = dirScan?.stats

    FmAlertDialog(
        title = stringResource(R.string.fm_property),
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                PropertyRow(R.string.fm_new_name, name)
                PropertyRow(R.string.fm_property_source_path, sourcePath)
                PropertyRow(
                    R.string.fm_property_type,
                    stringResource(if (isDirectory) R.string.resource_pack_manage_type_folder else R.string.fm_new_file)
                )
                if (isDirectory) {
                    val scanning = dirScan?.running == true
                    PropertyRow(
                        R.string.fm_property_size,
                        if (dirStats != null) {
                            formatFileSize(dirStats.totalSize)
                        } else if (scanning) {
                            stringResource(R.string.fm_calculating)
                        } else sizeText
                    )
                    PropertyRow(
                        R.string.fm_property_files,
                        dirStats?.fileCount?.toString() ?: if (scanning) stringResource(R.string.fm_calculating) else "-"
                    )
                    PropertyRow(
                        R.string.fm_property_folders,
                        dirStats?.dirCount?.toString() ?: if (scanning) stringResource(R.string.fm_calculating) else "-"
                    )
                    if (scanning) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                    }
                } else {
                    PropertyRow(R.string.fm_property_size, sizeText)
                }
                PropertyRow(R.string.fm_property_deleted_at, deletedText)
            }
        },
        confirmText = stringResource(R.string.generic_close),
        onDismiss = onDismiss
    )
}
