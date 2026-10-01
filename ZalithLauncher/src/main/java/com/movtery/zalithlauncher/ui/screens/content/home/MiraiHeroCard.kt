/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 * Copyright (C) 2026 Mirai Launcher contributors.
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
 * along with this program. If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.content.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager

@Composable
fun MiraiHeroCard(
    onLaunch: (Version?) -> Unit,
    onExplore: () -> Unit,
    onManageVersions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val currentAccount by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val shape = RoundedCornerShape(24.dp)

    BoxWithConstraints(modifier = modifier) {
        val compact = maxWidth < 540.dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 206.dp else 162.dp)
                .clip(shape)
        ) {
            Image(
                painter = painterResource(R.drawable.mirai_hero_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            0.0f to Color.Black.copy(alpha = 0.90f),
                            0.56f to Color(0xFF07120A).copy(alpha = 0.68f),
                            1.0f to Color(0xFF2BCB69).copy(alpha = 0.20f)
                        )
                    )
            )
    
            if (compact) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    BrandAndNotice()
    
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = stringResource(R.string.home_welcome_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentAccount?.let {
                                "${it.username} · ${getAccountTypeName(it)}"
                            } ?: stringResource(R.string.home_account_not_set),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD2E0D5),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onManageVersions)
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.home_selected_installation).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFAFC0B2),
                                maxLines = 1
                            )
                            Text(
                                text = currentVersion?.getVersionName()
                                    ?: stringResource(R.string.versions_manage_no_versions),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PrimaryActionButton(
                            modifier = Modifier.weight(1f),
                            currentVersion = currentVersion,
                            onLaunch = onLaunch,
                            onManageVersions = onManageVersions,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 9.dp)
                        )
                        OutlinedButton(
                            onClick = onExplore,
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.home_explore_content),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            BrandAndNotice()
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.home_welcome_title),
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentAccount?.let {
                                    "${it.username} · ${getAccountTypeName(it)}"
                                } ?: stringResource(R.string.home_account_not_set),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD2E0D5),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
    
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PrimaryActionButton(
                                currentVersion = currentVersion,
                                onLaunch = onLaunch,
                                onManageVersions = onManageVersions
                            )
                            OutlinedButton(
                                onClick = onExplore,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.home_explore_content),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
    
                    Surface(
                        modifier = Modifier
                            .padding(start = 14.dp)
                            .align(Alignment.Bottom),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xB809120B),
                        contentColor = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 13.dp, vertical = 10.dp)
                                .width(160.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.home_selected_installation).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFAFC0B2),
                                maxLines = 1
                            )
                            Text(
                                text = currentVersion?.getVersionName()
                                    ?: stringResource(R.string.versions_manage_no_versions),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            OutlinedButton(
                                onClick = onManageVersions,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = ButtonDefaults.ContentPadding
                            ) {
                                Text(
                                    text = stringResource(R.string.home_manage_versions),
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandAndNotice() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.launcher_brand_short).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF8EF0AA),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Surface(
            modifier = Modifier.weight(1f, fill = false),
            shape = RoundedCornerShape(30.dp),
            color = Color(0x4425E36B),
            contentColor = Color(0xFFD8FFE3)
        ) {
            Text(
                text = stringResource(R.string.app_unofficial_modified),
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PrimaryActionButton(
    currentVersion: Version?,
    onLaunch: (Version?) -> Unit,
    onManageVersions: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    Button(
        onClick = {
            if (currentVersion != null) onLaunch(currentVersion)
            else onManageVersions()
        },
        modifier = modifier,
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF35DD71),
            contentColor = Color(0xFF04200D)
        )
    ) {
        Text(
            text = stringResource(
                if (currentVersion != null) R.string.home_play
                else R.string.home_choose_version
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
