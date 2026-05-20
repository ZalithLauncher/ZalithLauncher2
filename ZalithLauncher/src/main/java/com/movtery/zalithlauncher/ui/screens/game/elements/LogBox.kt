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

package com.movtery.zalithlauncher.ui.screens.game.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.LoggerBridge
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.game.elements.log_parser.LogHighlighter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Collections

@Composable
fun LogBox(
    enableLog: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberLazyListState()

    val logList = remember { mutableStateListOf<AnnotatedString>() }
    val buffer = remember { Collections.synchronizedList(mutableListOf<AnnotatedString>()) }
    val scrollChannel = remember { mutableStateOf<Channel<Unit>?>(null) }

    val logHighlighter = remember { LogHighlighter() }
    var autoScrollDown by remember { mutableStateOf(true) }

    val config = remember {
        object {
            /** 缓冲区刷新间隔，单位：ms */
            val BUFFER_FLUSH_INTERVAL: Long = AllSettings.logBufferFlushInterval.getValue().toLong()
        }
    }

    LaunchedEffect(enableLog) {
        if (enableLog) {
            scrollChannel.value = Channel(capacity = 100)

            LoggerBridge.setListener { log ->
                synchronized(buffer) {
                    val string = logHighlighter.highlight(log)
                    buffer.add(string)
                }
            }

            launch(Dispatchers.Default) {
                val mutex = Mutex()
                while (isActive) {
                    try {
                        ensureActive()
                        delay(config.BUFFER_FLUSH_INTERVAL)
                        val pending = mutableListOf<AnnotatedString>()

                        mutex.withLock {
                            synchronized(buffer) {
                                if (buffer.isNotEmpty()) {
                                    pending.addAll(buffer)
                                    buffer.clear()
                                }
                            }
                        }
                        if (pending.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                logList.addAll(pending)
                                if (autoScrollDown) {
                                    //尝试进行滚动
                                    scrollChannel.value?.trySend(Unit)
                                }
                            }
                        }
                    } catch (_: CancellationException) {
                        break
                    }
                }
            }

            //自动滚动部分
            launch(Dispatchers.Main) {
                scrollChannel.value?.consumeAsFlow()?.collect {
                    runCatching {
                        val targetIndex = logList.lastIndex
                        if (targetIndex >= 0 && targetIndex < logList.size) {
                            scrollState.animateScrollToItem(targetIndex)
                        }
                    }
                }
            }
        } else {
            scrollChannel.value = null
            LoggerBridge.setListener(null)
            logList.clear()
            buffer.clear()
        }
    }

    if (enableLog) {
        Row(
            modifier = modifier.fillMaxSize()
        ) {
            Surface(
                modifier = modifier
                    .weight(1f)
                    .fillMaxHeight(),
                color = Color.Black.copy(alpha = 0.5f),
                contentColor = Color.White
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = scrollState
                ) {
                    items(logList) { log ->
                        val textSize = AllSettings.logTextSize.state
                        val fontSize = remember(textSize) {
                            TextUnit(textSize.toFloat(), TextUnitType.Sp)
                        }
                        val lineHeight = remember(textSize) {
                            val height = textSize.toFloat() * 1.1f
                            TextUnit(height, TextUnitType.Sp)
                        }

                        Text(
                            text = log,
                            modifier = Modifier.fillParentMaxWidth(),
                            fontSize = fontSize,
                            lineHeight = lineHeight
                        )
                    }
                }
            }

            //右侧控制区域
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                color = Color.White.copy(0.4f)
            )
            Surface(
                modifier = Modifier.fillMaxHeight(),
                color = Color.Black.copy(alpha = 0.7f),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    //关闭
                    LogBoxIconButton(
                        onClick = onClose,
                        toggle = false
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = null
                        )
                    }
                    //清理
                    LogBoxIconButton(
                        onClick = {
                            synchronized(buffer) {
                                logList.clear()
                                buffer.clear()
                            }
                        },
                        toggle = false
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(R.drawable.ic_delete_outlined),
                            contentDescription = null
                        )
                    }
                    //自动滚动
                    LogBoxIconButton(
                        onClick = {
                            val value = !autoScrollDown
                            autoScrollDown = value
                            if (value) {
                                scrollChannel.value?.trySend(Unit)
                            }
                        },
                        toggle = autoScrollDown,
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(R.drawable.ic_list_down),
                            contentDescription = null
                        )
                    }
                    //滚动到底部
                    LogBoxIconButton(
                        onClick = {
                            scrollChannel.value?.trySend(Unit)
                        },
                        toggle = false
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(R.drawable.ic_arrow_cool_down),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogBoxIconButton(
    onClick: () -> Unit,
    toggle: Boolean,
    icon: @Composable (() -> Unit)
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (toggle) {
                Color.White.copy(0.4f)
            } else {
                Color.Transparent
            }
        ),
        shape = MaterialTheme.shapes.medium,
        content = icon
    )
}