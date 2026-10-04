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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.filemanager.ui.components.FmAlertDialog
import com.movtery.zalithlauncher.filemanager.ui.components.FmCheckEditDialog
import com.movtery.zalithlauncher.filemanager.ui.components.FmDialogSurface
import com.movtery.zalithlauncher.filemanager.ui.components.FmIcons
import com.movtery.zalithlauncher.filemanager.ui.theme.fmSecondaryTextColor
import com.movtery.zalithlauncher.filemanager.viewmodel.SearchHitView
import com.movtery.zalithlauncher.filemanager.viewmodel.SearchUiState
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.lazyScrollWithBar

/**
 * 搜索设置对话框。
 * @param initialKeyword 输入框的初始关键词
 * @param onSearch 发起搜索，回传关键词与大小写敏感设置
 */
@Composable
fun FmSearchSetupDialog(
    initialKeyword: String = "",
    onDismiss: () -> Unit,
    onSearch: (keyword: String, caseSensitive: Boolean) -> Unit
) {
    var keyword by remember { mutableStateOf(initialKeyword) }
    var caseSensitive by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current

    FmCheckEditDialog(
        title = stringResource(R.string.generic_search),
        value = keyword,
        checked = caseSensitive,
        checkBoxText = stringResource(R.string.fm_search_case_sensitive),
        onValueChange = { keyword = it },
        onCheckedChange = { caseSensitive = it },
        label = { Text(stringResource(R.string.fm_search_hint)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                if (keyword.isNotBlank()) {
                    keyboard?.hide()
                    onSearch(keyword.trim(), caseSensitive)
                }
            }
        ),
        confirmText = stringResource(R.string.generic_search),
        confirmEnabled = keyword.isNotBlank(),
        onDismissRequest = onDismiss,
        onCancel = onDismiss,
        onConfirm = {
            if (keyword.isNotBlank()) {
                keyboard?.hide()
                onSearch(keyword.trim(), caseSensitive)
            }
        }
    )
}

/**
 * 搜索进行中对话框。
 * @param searchUi 搜索任务的实时状态
 * @param onCancel 取消搜索并关闭对话框
 */
@Composable
fun FmSearchTaskDialog(
    searchUi: SearchUiState,
    onCancel: () -> Unit
) {
    FmAlertDialog(
        title = stringResource(R.string.generic_search),
        text = {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(
                        R.string.fm_search_current,
                        searchUi.currentDir?.toString() ?: ""
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        confirmText = stringResource(R.string.generic_cancel),
        onDismiss = onCancel
    )
}

/**
 * 搜索结果列表对话框。
 * @param searchUi 搜索任务的实时状态（含命中结果）
 * @param onClear 清空结果并返回搜索设置
 * @param onResumeSearch 重新发起搜索
 * @param onSelect 选择某个命中结果
 */
@Composable
fun FmSearchResultDialog(
    searchUi: SearchUiState,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    onResumeSearch: () -> Unit,
    onSelect: (SearchHitView) -> Unit
) {
    FmDialogSurface(
        onDismissRequest = onDismiss,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.generic_search),
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.fm_search_result_count, searchUi.hits.size),
                style = MaterialTheme.typography.bodySmall,
                color = fmSecondaryTextColor(),
                modifier = Modifier.weight(1f)
            )
            if (searchUi.hits.isNotEmpty()) {
                IconButton(
                    modifier = Modifier.size(32.dp),
                    onClick = onClear,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.generic_clear),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            IconButton(
                modifier = Modifier.size(32.dp),
                onClick = onResumeSearch,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = stringResource(R.string.generic_search),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (searchUi.hits.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.fm_search_empty),
                    color = fmSecondaryTextColor()
                )
            }
        } else {
            val listState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .fadeEdge(state = listState)
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .lazyScrollWithBar(state = listState)
                    .weight(1f, fill = false),
                state = listState
            ) {
                items(searchUi.hits, key = { it.path.toString() }) { hit ->
                    SearchResultRow(
                        hit = hit,
                        onClick = { onSelect(hit) }
                    )
                }
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onDismiss
        ) {
            MarqueeText(text = stringResource(R.string.generic_close))
        }
    }
}

@Composable
private fun SearchResultRow(
    hit: SearchHitView,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FmIcons.IconFor(
            name = hit.name,
            isDirectory = hit.isDirectory,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = hit.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = hit.path.parent?.toString() ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = fmSecondaryTextColor(),
                maxLines = 1,
                overflow = TextOverflow.StartEllipsis
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_keyboard_arrow_right),
            contentDescription = null,
            tint = fmSecondaryTextColor()
        )
    }
}
