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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.components.InstallableItem
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedDirection
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.animated
import com.movtery.zalithlauncher.ui.components.rememberAnimatedContext
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.viewmodel.SplashBackStackViewModel

@Composable
fun UnpackScreen(
    items: List<InstallableItem>,
    screenViewModel: SplashBackStackViewModel,
    onAgreeClick: () -> Unit = {}
) {
    BaseScreen(
        screenKey = SplashOperation.Unpacking,
        currentKey = screenViewModel.splashScreen.currentKey
    ) { isVisible ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UnpackTaskList(
                modifier = Modifier
                    .weight(7f)
                    .fillMaxHeight(),
                isVisible = isVisible,
                items = items,

            )

            ActionMenu(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight(),
                isVisible = isVisible,
                items = items,
                onAgreeClick = onAgreeClick
            )
        }
    }
}

@Composable
private fun ActionMenu(
    isVisible: Boolean,
    items: List<InstallableItem>,
    modifier: Modifier = Modifier,
    onAgreeClick: () -> Unit = {}
) {
    val states = items.map { item ->
        item.state.collectAsStateWithLifecycle().value
    }
    val installing = states.any { it == InstallableItem.State.RUNNING }
    val hasFailed = states.any { it == InstallableItem.State.FAILED }
    val ctx = rememberAnimatedContext(isVisible = isVisible)

    Column(
        modifier = modifier.animated(ctx, from = AnimatedDirection.Right),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(state = rememberScrollState())
                .weight(1f)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = if (installing) {
                    stringResource(R.string.splash_screen_installing)
                } else {
                    stringResource(R.string.splash_screen_unpack_desc)
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }

        ScalingActionButton(
            enabled = !installing && states.any {
                it == InstallableItem.State.NOT_STARTED ||
                it == InstallableItem.State.PENDING ||
                it == InstallableItem.State.FAILED
            },
            modifier = Modifier.fillMaxWidth(),
            onClick = onAgreeClick
        ) {
            MarqueeText(
                text = stringResource(
                    if (hasFailed) R.string.generic_retry else R.string.splash_screen_agree
                )
            )
        }
    }
}

@Composable
private fun UnpackTaskList(
    isVisible: Boolean,
    items: List<InstallableItem>,
    modifier: Modifier = Modifier,
) {
    val ctx = rememberAnimatedContext(isVisible = isVisible)

    BackgroundCard(
        modifier = modifier.animated(ctx),
        influencedByBackground = false,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        val scrollState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nonInteractiveScrollbar(
                    state = scrollState.scrollIndicatorState!!,
                    orientation = Orientation.Vertical,
                ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            state = scrollState,
        ) {
            items(items) { item ->
                TaskItem(
                    item = item,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun TaskItem(
    item: InstallableItem,
    modifier: Modifier = Modifier
) {
    val state by item.state.collectAsStateWithLifecycle()
    val message by item.task.taskMessage.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = itemColor(),
        contentColor = onItemColor(),
    ) {
        Row {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                    .animateContentSize(animationSpec = getAnimateTween())
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.labelMedium
                )
                item.summary?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                if (state == InstallableItem.State.RUNNING || state == InstallableItem.State.FAILED) {
                    message?.let { taskMessage ->
                        Text(
                            text = taskMessage,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                }
            }

            val iconModifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp))
                .size(18.dp)
            when (state) {
                InstallableItem.State.NOT_STARTED -> {
                    Icon(
                        modifier = iconModifier,
                        painter = painterResource(R.drawable.ic_folder_zip_outlined),
                        contentDescription = null
                    )
                }
                InstallableItem.State.PENDING -> {
                    Icon(
                        modifier = iconModifier,
                        painter = painterResource(R.drawable.ic_update),
                        contentDescription = null
                    )
                }
                InstallableItem.State.RUNNING -> {
                    CircularProgressIndicator(
                        modifier = iconModifier,
                        strokeWidth = 2.dp
                    )
                }
                InstallableItem.State.FINISHED -> {
                    Icon(
                        modifier = iconModifier,
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null
                    )
                }
                InstallableItem.State.FAILED -> {
                    Icon(
                        modifier = iconModifier,
                        painter = painterResource(R.drawable.ic_warning_filled),
                        contentDescription = null
                    )
                }
                else -> {}
            }
        }
    }
}