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

package com.movtery.zalithlauncher.ui.screens.content.versions.export

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.export.PackType
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedLazyColumn
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.WarningCard
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.rememberSettingsCardShape

/**
 * 整合包导出类型选择页
 */
@Composable
fun ExportTypeSelectScreen(
    mainScreenKey: TitledNavKey?,
    exportScreenKey: TitledNavKey?,
    onTypeSelect: (PackType) -> Unit
) {
    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.VersionExport::class.java, mainScreenKey)
        ),
        Triple(NormalNavKey.VersionExports.SelectType, exportScreenKey, false)
    ) { isVisible ->
        AnimatedLazyColumn(
            isVisible = isVisible,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) { scope ->
            animatedItem(scope) { yOffset ->
                WarningCard(
                    modifier = Modifier
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    title = stringResource(R.string.generic_tip),
                    icon = { innerModifier ->
                        Icon(
                            modifier = innerModifier,
                            painter = painterResource(R.drawable.ic_lightbulb),
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.versions_export_tip_1),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = stringResource(R.string.versions_export_tip_2),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = stringResource(R.string.versions_export_tip_3),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = stringResource(R.string.versions_export_tip_4),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = stringResource(R.string.versions_export_tip_5),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )
            }

            animatedItem(scope) { yOffset ->
                Column(
                    modifier = Modifier
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    //MCBBS
                    TypeItem(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        title = stringResource(R.string.versions_export_type_mcbbs),
                        summary = stringResource(R.string.versions_export_type_mcbbs_summary, BuildKeys.LAUNCHER_SHORT_NAME),
                        icon = painterResource(R.drawable.img_chest),
                        onClick = { onTypeSelect(PackType.MCBBS) }
                    )

                    //Modrinth
                    TypeItem(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        title = stringResource(R.string.versions_export_type_modrinth),
                        summary = stringResource(R.string.versions_export_type_summary_common),
                        icon = painterResource(R.drawable.img_platform_modrinth),
                        onClick = { onTypeSelect(PackType.Modrinth) }
                    )

                    //CurseForge
                    TypeItem(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        title = stringResource(R.string.versions_export_type_curseforge),
                        summary = stringResource(R.string.versions_export_type_summary_common),
                        icon = painterResource(R.drawable.img_platform_curseforge),
                        onClick = { onTypeSelect(PackType.CurseForge) }
                    )

                    //MultiMC
                    TypeItem(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        title = stringResource(R.string.versions_export_type_multimc),
                        summary = stringResource(R.string.versions_export_type_multimc_summary, BuildKeys.LAUNCHER_SHORT_NAME),
                        icon = painterResource(R.drawable.img_platform_multimc),
                        onClick = { onTypeSelect(PackType.MultiMC) }
                    )
                }
            }
        }
    }
}


/**
 * 导出类型布局
 */
@Composable
private fun TypeItem(
    title: String,
    summary: String,
    icon: Painter,
    position: CardPosition,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = rememberSettingsCardShape(position = position)

    BackgroundCard(
        modifier = modifier,
        onClick = onClick,
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //图标
            Image(
                modifier = Modifier.size(28.dp),
                painter = icon,
                contentDescription = title,
                contentScale = ContentScale.FillBounds
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = summary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Icon(
                painter = painterResource(R.drawable.ic_arrow_right_rounded),
                contentDescription = null
            )
        }
    }
}