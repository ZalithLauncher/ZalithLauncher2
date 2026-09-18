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

package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.observable.ObservableClickEventsProvider
import com.movtery.layer_controller.observable.ObservableNormalData
import com.movtery.layer_controller.observable.clickEventsProvider
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSwitchItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutTextItem
import com.movtery.zalithlauncher.ui.theme.cardColor

private data class TabItem(val title: Int)

@Composable
fun EditWidgetClickEvent(
    screenKey: TitledNavKey,
    currentKey: TitledNavKey?,
    data: ObservableNormalData,
    switchControlLayers: (ObservableClickEventsProvider, ClickEvent.Type) -> Unit,
    sendText: (ObservableClickEventsProvider) -> Unit
) {
    BaseScreen(
        screenKey = screenKey,
        currentKey = currentKey
    ) {
        Column(
            modifier = Modifier
                .padding(start = 4.dp, end = 8.dp)
                .fillMaxSize()
        ) {
            val tabs = remember {
                listOf(
                    TabItem(R.string.control_editor_edit_event_basic),
                    TabItem(R.string.control_editor_edit_event_launcher),
                    TabItem(R.string.control_editor_edit_event_key)
                )
            }

            val pagerState = rememberPagerState(pageCount = { tabs.size })
            var selectedTabIndex by remember { mutableIntStateOf(0) }

            LaunchedEffect(selectedTabIndex) {
                pagerState.animateScrollToPage(selectedTabIndex)
            }

            //顶贴标签栏
            SecondaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = cardColor(false)
            ) {
                tabs.forEachIndexed { index, item ->
                    Tab(
                        selected = index == selectedTabIndex,
                        onClick = {
                            selectedTabIndex = index
                        },
                        text = {
                            MarqueeText(text = stringResource(item.title))
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
            ) { page ->
                when (page) {
                    0 -> {
                        EditBasicEvent(
                            modifier = Modifier.fillMaxSize(),
                            data = data,
                            switchControlLayers = { data, type ->
                                switchControlLayers(clickEventsProvider(data), type)
                            }
                        )
                    }
                    1 -> {
                        val provider = clickEventsProvider(data)
                        LauncherEventsEdit(
                            provider = provider,
                            onSendText = {
                                sendText(provider)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 2.dp, vertical = 12.dp)
                        )
                    }
                    2 -> {
                        KeyEventEdit(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 2.dp),
                            provider = clickEventsProvider(data)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditBasicEvent(
    modifier: Modifier = Modifier,
    data: ObservableNormalData,
    switchControlLayers: (ObservableNormalData, ClickEvent.Type) -> Unit
) {
    Column(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .verticalScrollWithBar(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier)

        //滑动触发
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_swipple),
            value = data.isSwipple,
            onValueChange = { value ->
                data.isSwipple = value
            }
        )

        //带动鼠标
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_penetrable),
            value = data.isPenetrable,
            onValueChange = { value ->
                data.isPenetrable = value
            }
        )

        //可开关
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_toggleable),
            value = data.isToggleable,
            onValueChange = { value ->
                data.isToggleable = value
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        //切换控制层可见性
        InfoLayoutTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_switch_layers),
            onClick = {
                switchControlLayers(data, ClickEvent.Type.SwitchLayer)
            }
        )

        //强制显示控件层
        InfoLayoutTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_show_layers),
            onClick = {
                switchControlLayers(data, ClickEvent.Type.ShowLayer)
            }
        )

        //强制隐藏控件层
        InfoLayoutTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_hide_layers),
            onClick = {
                switchControlLayers(data, ClickEvent.Type.HideLayer)
            }
        )

        Spacer(Modifier)
    }
}