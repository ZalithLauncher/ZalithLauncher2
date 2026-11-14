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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedColumn
import com.movtery.zalithlauncher.ui.components.AnimatedDirection
import com.movtery.zalithlauncher.ui.components.animated
import com.movtery.zalithlauncher.ui.components.rememberAnimatedContext
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.device.Architecture
import com.movtery.zalithlauncher.viewmodel.SplashBackStackViewModel

@Composable
fun ArchMismatchScreen(
    screenViewModel: SplashBackStackViewModel,
    onExitClick: () -> Unit = {}
) {
    BaseScreen(
        screenKey = SplashOperation.ArchMismatch,
        currentKey = screenViewModel.splashScreen.currentKey
    ) { isVisible ->
        val packageArch = remember {
            Architecture.archAsStringAndroid(BuildKeys.BUILD_ARCH)
        }
        val deviceArch = remember {
            Architecture.archAsStringAndroid(
                Architecture.getDeviceArchitecture()
            )
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val maxContentHeight = maxHeight / 3 * 2
            AnimatedColumn(
                modifier = Modifier.padding(all = 24.dp),
                isVisible = isVisible,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
//                AnimatedItem { ctx ->
//                    Icon(
//                        modifier = Modifier
//                            .animated(ctx)
//                            .size(38.dp),
//                        painter = painterResource(R.drawable.ic_warning_filled),
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.error
//                    )
//                }

                AnimatedItem { ctx ->
                    Surface(
                        modifier = Modifier
                            .heightIn(max = maxContentHeight)
                            .animated(ctx)
                            .padding(all = 3.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = cardColor(false),
                        contentColor = onCardColor(),
                        shadowElevation = 3.dp,
                    ) {
                        Box {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 16.dp, y = (-20).dp)
                                    .size(80.dp)
                                    .rotate(18f)
                                    .background(
                                        color = itemColor(false).copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(x = (-16).dp, y = 16.dp)
                                    .size(80.dp)
                                    .background(
                                        color = itemColor(false).copy(alpha = 0.75f),
                                        shape = CircleShape
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .verticalScrollWithBar(rememberScrollState())
                                    .padding(all = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.splash_arch_mismatch_title),
                                    style = MaterialTheme.typography.headlineSmall
                                )

                                CompositionLocalProvider(
                                    LocalTextStyle provides MaterialTheme.typography.bodyMedium
                                ) {
                                    Column {
                                        Text(text = stringResource(R.string.splash_arch_mismatch_package_arch, packageArch))
                                        Text(text = stringResource(R.string.splash_arch_mismatch_device_arch, deviceArch))
                                    }

                                    Column {
                                        Text(text = stringResource(R.string.splash_arch_mismatch_desc1))
                                        Text(text = stringResource(R.string.splash_arch_mismatch_desc2, deviceArch))
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedItem { ctx ->
                    Button(
                        modifier = Modifier
                            .align(Alignment.End)
                            .animated(ctx),
                        onClick = onExitClick,
                    ) {
                        Text(text = stringResource(R.string.crash_exit))
                    }
                }
            }

            val ctx = rememberAnimatedContext(isVisible = isVisible)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(all = 12.dp)
                    .animated(ctx, from = AnimatedDirection.Bottom),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(R.drawable.ic_info_filled),
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.weight(1f, fill = false),
                    text = stringResource(R.string.splash_arch_mismatch_footer),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
