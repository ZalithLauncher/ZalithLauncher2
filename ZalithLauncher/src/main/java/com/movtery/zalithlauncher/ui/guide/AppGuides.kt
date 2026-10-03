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

package com.movtery.zalithlauncher.ui.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.guide.GuideController
import com.movtery.guide.NextTip
import com.movtery.guide.NodeClickMode
import com.movtery.guide.rememberGuide
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.EventViewModel

private const val TAG = "AppGuides"

/**
 * @param mainScreen 启动器主界面的引导
 * @param editorScreen 控制布局编辑器的引导
 */
class AppGuides(
    val mainScreen: GuideController,
    val editorScreen: GuideController,
) {
    /**
     * 启动指定组的引导流，启动成功后记录进度
     * @return 是否成功启动（流为空或已有其他流激活时失败）
     */
    fun start(group: GuideKeys.Keys): Boolean {
        val controller = when (group) {
            GuideKeys.Main -> mainScreen
            GuideKeys.Editor -> editorScreen
        }
        val started = controller.start()
        if (started) {
            GuideProgress.markPlayed(group)
            Logger.info(TAG, "Guide started: ${GuideProgress.keyOf(group)}")
        }
        return started
    }

    /**
     * 首次启动指定组的引导流，持久化存储，确保引导仅播放一次
     * @return 是否成功启动（流为空或已有其他流激活时失败，或者播放过）
     */
    fun startOnce(group: GuideKeys.Keys): Boolean {
        if (GuideProgress.isPlayed(group)) return false
        return start(group)
    }
}

@Composable
fun rememberAppGuides(
    eventViewModel: EventViewModel? = null
): AppGuides {
    val mainScreen = rememberMainGuides()
    val editorScreen = rememberEditorGuides()

    val guides = remember(mainScreen, editorScreen) {
        AppGuides(mainScreen, editorScreen)
    }

    eventViewModel?.let { viewModel ->
        LaunchedEffect(guides) {
            viewModel.events.collect { event ->
                when (event) {
                    is EventViewModel.Event.Guide.StartGuide -> guides.start(event.group)
                    is EventViewModel.Event.Guide.StartGuideOnce -> guides.startOnce(event.group)
                    else -> {}
                }
            }
        }
    }

    return guides
}

@Composable
private fun rememberMainGuides() = rememberGuide(holeRadius = 28.dp) {
    intro {
        GuideCard {
            Text(stringResource(R.string.guide_main_welcome_title, BuildKeys.LAUNCHER_NAME))
            Text(stringResource(R.string.guide_main_welcome_text))
        }
    }
    entry(GuideKeys.Main.Step.Account) {
        GuideCard {
            Text(stringResource(R.string.guide_main_account))
        }
    }
    entry(GuideKeys.Main.Step.VersionList) {
        GuideCard {
            Text(stringResource(R.string.guide_main_version_list))
        }
    }
    entry(GuideKeys.Main.Step.CardDrag) {
        GuideCard {
            Text(stringResource(R.string.guide_main_card_drag_title))
            Text(stringResource(R.string.guide_main_card_drag_text))
        }
    }
    entry(GuideKeys.Main.Step.CardTip) {
        GuideCard {
            Text(stringResource(R.string.guide_main_card_tip_title))
            Text(stringResource(
                R.string.guide_main_card_tip_text,
                stringResource(R.string.home_add_version_card)
            ))
        }
    }
}

@Composable
private fun rememberEditorGuides() = rememberGuide {
    intro {
        GuideCard {
            Text(stringResource(R.string.guide_editor_welcome_title))
            Text(stringResource(R.string.guide_editor_welcome_text))
        }
    }
    entry(
        key = GuideKeys.Editor.Step.MenuBall,
        nodeClick = NodeClickMode.PassThrough,
        advanceOnScrimClick = false
    ) {
        GuideCard {
            Text(stringResource(R.string.guide_editor_menu_ball_title))
            Text(stringResource(R.string.guide_editor_menu_ball_text))
        }
    }
    entry(GuideKeys.Editor.Step.LayerList) {
        GuideCard {
            Text(stringResource(R.string.guide_editor_layer_list_title))
            Text(stringResource(R.string.guide_editor_layer_list_text))
            Text(stringResource(R.string.guide_editor_layer_list_note))
        }
    }
    entry(GuideKeys.Editor.Step.CreateLayer) {
        GuideCard {
            Text(stringResource(R.string.guide_editor_create_layer))
        }
    }
    entry(GuideKeys.Editor.Step.AddButtons) {
        GuideCard {
            Text(stringResource(R.string.guide_editor_add_widgets_title))
            Text(stringResource(R.string.guide_editor_add_widgets_text))
        }
    }
    entry(GuideKeys.Editor.Step.AddStyles) {
        GuideCard {
            Text(stringResource(R.string.guide_editor_styles_title))
            Text(stringResource(R.string.guide_editor_styles_text))
        }
    }
    entry(GuideKeys.Editor.Step.Preview) {
        GuideCard {
            Text(stringResource(R.string.guide_editor_preview))
        }
    }
    entry(GuideKeys.Editor.Step.Save) {
        GuideCard {
            Text(stringResource(R.string.guide_editor_save))
        }
    }
}

/**
 * 发布启动引导事件
 */
fun EventViewModel.sendStartGuide(group: GuideKeys.Keys) {
    sendEvent(EventViewModel.Event.Guide.StartGuide(group))
}

/**
 * 发布启动引导事件，已播放过的引导将被忽略
 */
fun EventViewModel.sendStartGuideOnce(group: GuideKeys.Keys) {
    sendEvent(EventViewModel.Event.Guide.StartGuideOnce(group))
}

@Composable
fun NextTipLabel(tip: NextTip) {
    Box(
        modifier = Modifier
            .widthIn(max = 360.dp)
            .clip(MaterialTheme.shapes.large)
            .background(Color.Black.copy(alpha = 0.4f))
            .alpha(0.8f)
    ) {
        Text(
            modifier = Modifier.padding(all = 8.dp),
            text = when (tip) {
                NextTip.TapBlank -> stringResource(R.string.guide_tip_blank)
                NextTip.Finish -> stringResource(R.string.guide_tip_finish)
            },
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * 引导的文本卡片
 */
@Composable
private fun GuideCard(
    modifier: Modifier = Modifier,
    text: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .widthIn(max = 360.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Color.Black.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(all = 18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyMedium
            ) {
                text()
            }
        }
    }
}