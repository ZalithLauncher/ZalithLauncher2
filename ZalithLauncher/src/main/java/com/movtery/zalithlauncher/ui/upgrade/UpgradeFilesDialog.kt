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

package com.movtery.zalithlauncher.ui.upgrade

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SimpleListDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.DisabledAlpha
import com.movtery.zalithlauncher.upgrade.RemoteData
import com.movtery.zalithlauncher.utils.device.Architecture
import com.movtery.zalithlauncher.utils.file.formatFileSize

@Composable
fun UpgradeFilesDialog(
    data: RemoteData,
    onDismissRequest: () -> Unit,
    onFileSelected: (RemoteData.RemoteFile) -> Unit
) {
    //当前设备的架构信息
    val currentArch: RemoteData.RemoteFile.Arch = remember(data) {
        val arch = Architecture.getDeviceArchitecture()
        when (arch) {
            Architecture.ARCH_ARM -> RemoteData.RemoteFile.Arch.ARM
            Architecture.ARCH_ARM64 -> RemoteData.RemoteFile.Arch.ARM64
            Architecture.ARCH_X86 -> RemoteData.RemoteFile.Arch.X86
            Architecture.ARCH_X86_64 -> RemoteData.RemoteFile.Arch.X86_64
            else -> RemoteData.RemoteFile.Arch.ALL
        }
    }

    val current = remember(data) {
        data.files.find { it.arch == currentArch }
    }

    SimpleListDialog(
        title = stringResource(R.string.upgrade_files),
        items = data.files,
        onItemSelected = { file ->
            onFileSelected(file)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        current = current,
        itemLayout = { item, isCurrent, onClick ->
            UpgradeFileLayout(
                modifier = Modifier.fillMaxWidth(),
                file = item,
                currentArch = currentArch,
                selected = isCurrent,
                onClick = onClick,
                //根据设备架构决定哪些安装包不能选择，避免下载到错误架构的安装包（允许选择全架构）
                enabled = item.arch == RemoteData.RemoteFile.Arch.ALL || item.arch == currentArch
            )
        },
        showConfirm = true,
        confirmText = {
            MarqueeText(text = stringResource(R.string.generic_download))
        }
    )
}

@Composable
private fun UpgradeFileLayout(
    file: RemoteData.RemoteFile,
    currentArch: RemoteData.RemoteFile.Arch,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.large)
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled
        )
        Column(
            modifier = Modifier.alpha(if (enabled) 1.0f else DisabledAlpha),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            //文件名
            MarqueeText(
                text = file.fileName,
                style = MaterialTheme.typography.labelMedium
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.7f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentArch == file.arch) {
                    Icon(
                        modifier = Modifier.size(12.dp),
                        painter = painterResource(R.drawable.ic_star_filled),
                        contentDescription = null
                    )
                }
                Row(
                    modifier = Modifier.basicMarquee(Int.MAX_VALUE),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    //架构信息
                    Text(
                        text = file.arch.getDisplayString(),
                        style = MaterialTheme.typography.labelSmall
                    )
                    //大小
                    val sizeString = remember(file) {
                        formatFileSize(file.size)
                    }
                    Text(
                        text = stringResource(R.string.upgrade_version_size, sizeString),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun RemoteData.RemoteFile.Arch.getDisplayString(): String {
    return when (this) {
        RemoteData.RemoteFile.Arch.ALL -> stringResource(R.string.upgrade_files_arch, stringResource(R.string.generic_all))
        RemoteData.RemoteFile.Arch.ARM -> stringResource(R.string.upgrade_files_arch, "arm")
        RemoteData.RemoteFile.Arch.ARM64 -> stringResource(R.string.upgrade_files_arch, "arm64")
        RemoteData.RemoteFile.Arch.X86 -> stringResource(R.string.upgrade_files_arch, "x86")
        RemoteData.RemoteFile.Arch.X86_64 -> stringResource(R.string.upgrade_files_arch, "x86_64")
    }
}