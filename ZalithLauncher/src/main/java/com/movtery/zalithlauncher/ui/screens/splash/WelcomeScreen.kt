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

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.legal.AvailableAgreement
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.ScreenType
import com.movtery.zalithlauncher.ui.components.ScreenTypeLayout
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.swapAnimateAlphaProgressAsState
import com.movtery.zalithlauncher.ui.components.swapAnimateProgressAsState
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutTextItem
import com.movtery.zalithlauncher.utils.animation.SwapTimeline
import com.movtery.zalithlauncher.viewmodel.SplashBackStackViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun WelcomeScreen(
    onFinished: () -> Unit,
    availableAgreement: StateFlow<AvailableAgreement?>,
    screenViewModel: SplashBackStackViewModel,
    modifier: Modifier = Modifier
) {
    BaseScreen(
        screenKey = SplashOperation.Welcome,
        currentKey = screenViewModel.splashScreen.currentKey
    ) { isVisible ->
        val agreement by availableAgreement.collectAsStateWithLifecycle()
        var started by remember { mutableStateOf(false) }
        var agreementRequired by remember(agreement) { mutableStateOf(agreement != null) }
        var agreementChecked by remember(agreement) { mutableStateOf(false) }
        val onStart = {
            if (!agreementRequired || agreementChecked) {
                started = true
                onFinished()
            }
        }

        ScreenTypeLayout(
            modifier = modifier.background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) { screenType ->
            when (screenType) {
                ScreenType.Watch -> {
                    // 手表等方形或略宽的极小屏布局
                    WatchWelcomeScreen(
                        isVisible = isVisible,
                        started = started,
                        onStart = onStart,
                        availableAgreement = availableAgreement,
                        agreementRequired = agreementRequired,
                        agreementChecked = agreementChecked,
                        onAgreementCheckedChange = { agreementChecked = it },
                        onAgreementRequiredChange = { agreementRequired = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                ScreenType.Tablet -> {
                    // 平板布局
                    TabletWelcomeScreen(
                        isVisible = isVisible,
                        started = started,
                        onStart = onStart,
                        availableAgreement = availableAgreement,
                        agreementRequired = agreementRequired,
                        agreementChecked = agreementChecked,
                        onAgreementCheckedChange = { agreementChecked = it },
                        onAgreementRequiredChange = { agreementRequired = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                ScreenType.Phone -> {
                    // 手机横板布局
                    val availableWidth = minOf(maxWidth, 1280.dp) - 60.dp
                    val terminalWidth = availableWidth * (1.35f / 2.35f) - 20.dp

                    Row(
                        modifier = Modifier
                            .widthIn(max = 1280.dp)
                            .fillMaxSize()
                            .padding(all = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // 终端卡片布局
                        WelcomeTerminal(
                            isVisible = isVisible,
                            modifier = Modifier
                                .width(terminalWidth)
                                .fillMaxHeight()
                                .welcomeEntrance(isVisible = isVisible)
                        )

                        WelcomeInformation(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .welcomeEntrance(isVisible = isVisible, delayMillis = 300)
                                .padding(vertical = 4.dp),
                            started = started,
                            onStart = onStart,
                            availableAgreement = availableAgreement,
                            agreementRequired = agreementRequired,
                            agreementChecked = agreementChecked,
                            onAgreementCheckedChange = { agreementChecked = it },
                            onAgreementRequiredChange = { agreementRequired = it },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Modifier.welcomeEntrance(
    isVisible: Boolean,
    delayMillis: Int = 0,
    durationMillis: Int = 3000,
    enter: Boolean = true,
    offsetX: Dp = 180.dp,
    offsetY: Dp = 0.dp
): Modifier {
    val slideProgress = swapAnimateProgressAsState(
        isVisible = isVisible && enter,
        animationSpec = if (isVisible) {
            tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
            )
        } else {
            tween(
                durationMillis = SwapTimeline.swapTime(SwapTimeline.EXIT_ITEM),
                easing = LinearEasing
            )
        }
    )
    val opacity = swapAnimateAlphaProgressAsState(
        isVisible = isVisible && enter,
        animationSpec = if (isVisible) {
            tween(
                durationMillis = 800,
                delayMillis = delayMillis,
                easing = LinearEasing
            )
        } else {
            tween(
                durationMillis = SwapTimeline.swapTime(SwapTimeline.EXIT_ALPHA),
                easing = LinearEasing
            )
        }
    )

    return this.graphicsLayer {
        translationX = offsetX.toPx() * (1f - slideProgress.value)
        translationY = offsetY.toPx() * (1f - slideProgress.value)
        alpha = opacity.value
    }
}

@Composable
private fun WelcomeTerminal(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    var welcomeText by remember { mutableStateOf("") }
    var welcomeFinished by remember { mutableStateOf(false) }
    val cursorTransition = rememberInfiniteTransition(label = "WelcomeCursor")
    // 终端指针循环动画
    val cursorAlpha by cursorTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                1f at 0
                1f at 499
                0f at 500
                0f at 999
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "WelcomeCursorAlpha"
    )

    // 控制WELCOME标题的打字动画
    LaunchedEffect(Unit) {
        welcomeText = ""
        welcomeFinished = false
        delay(700.milliseconds)
        "WEL".forEach { character ->
            welcomeText += character
            delay(85.milliseconds)
        }

        // 中途小小休息
        delay(420.milliseconds)

        "COME!".forEach { character ->
            welcomeText += character
            delay(85.milliseconds)
        }
        welcomeFinished = true
    }

    val contentPadding = if (compact) 14.dp else 28.dp

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(if (compact) 22.dp else 36.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val titleWidth = (maxWidth - contentPadding * 2).coerceAtLeast(0.dp)
            val titleFontSize = with(LocalDensity.current) {
                (titleWidth.toPx() / 6f).toSp().value
            }.coerceIn(12f, if (compact) 30f else 56f).sp

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(
                        x = if (compact) 16.dp else 32.dp,
                        y = if (compact) (-20).dp else (-36).dp
                    )
                    .size(if (compact) 80.dp else 156.dp)
                    .rotate(18f)
                    .background(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(if (compact) 24.dp else 44.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(
                        x = if (compact) (-16).dp else (-30).dp,
                        y = if (compact) 16.dp else 30.dp
                    )
                    .size(if (compact) 64.dp else 116.dp)
                    .background(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = contentPadding)
            ) {
                // 左上角标签
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        modifier = Modifier.padding(
                            horizontal = if (compact) 8.dp else 12.dp,
                            vertical = if (compact) 4.dp else 7.dp
                        ),
                        text = "♥ MADE WITH HEART ♥",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = if (compact) 0.6.sp else 1.4.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = welcomeText,
                        style = MaterialTheme.typography.displayLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = titleFontSize,
                        letterSpacing = if (compact) (-1).sp else (-2).sp,
                        maxLines = 1
                    )
                    // 模拟终端指针
                    Text(
                        modifier = Modifier.alpha(cursorAlpha),
                        text = "_",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = titleFontSize,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(if (compact) 4.dp else 8.dp))

                Text(
                    modifier = Modifier.welcomeEntrance(
                        isVisible = isVisible,
                        durationMillis = 2000,
                        enter = welcomeFinished,
                        offsetX = 0.dp,
                        offsetY = if (compact) 0.dp else 48.dp
                    ),
                    text = stringResource(R.string.welcome_screen_subtitle),
                    style = if (compact) {
                        MaterialTheme.typography.bodySmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WatchWelcomeScreen(
    isVisible: Boolean,
    started: Boolean,
    onStart: () -> Unit,
    availableAgreement: StateFlow<AvailableAgreement?>,
    agreementRequired: Boolean,
    agreementChecked: Boolean,
    onAgreementCheckedChange: (Boolean) -> Unit,
    onAgreementRequiredChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val terminalHeight = (maxHeight * 0.36f).coerceIn(144.dp, 160.dp)

        Box(modifier = Modifier.fillMaxSize()) {
            val listState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .fadeEdge(listState)
                    .verticalScrollWithBar(listState)
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProductBanner(
                    compact = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .welcomeEntrance(
                            isVisible = isVisible,
                            delayMillis = 300,
                            offsetX = 32.dp
                        )
                )

                WelcomeTerminal(
                    isVisible = isVisible,
                    compact = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(terminalHeight)
                        .welcomeEntrance(isVisible = isVisible, offsetX = 32.dp)
                )

                WelcomeIntroduction(
                    availableAgreement = availableAgreement,
                    agreementChecked = agreementChecked,
                    onAgreementCheckedChange = onAgreementCheckedChange,
                    onAgreementRequiredChange = onAgreementRequiredChange,
                    compact = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .welcomeEntrance(
                            isVisible = isVisible,
                            delayMillis = 300,
                            offsetX = 32.dp
                        )
                )
            }

            StartButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .welcomeEntrance(
                        isVisible = isVisible,
                        delayMillis = 300,
                        offsetX = 32.dp
                    ),
                started = started,
                onStart = onStart,
                canStart = !agreementRequired || agreementChecked,
            )
        }
    }
}

@Composable
private fun TabletWelcomeScreen(
    isVisible: Boolean,
    started: Boolean,
    onStart: () -> Unit,
    availableAgreement: StateFlow<AvailableAgreement?>,
    agreementRequired: Boolean,
    agreementChecked: Boolean,
    onAgreementCheckedChange: (Boolean) -> Unit,
    onAgreementRequiredChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val terminalHeight = (maxHeight * 0.42f).coerceIn(260.dp, 380.dp)

        Box(
            modifier = Modifier
                .widthIn(max = 1120.dp)
                .fillMaxSize()
        ) {
            val listState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .fadeEdge(listState)
                    .verticalScrollWithBar(listState)
                    .padding(start = 28.dp, end = 28.dp, top = 28.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            ) {
                ProductBanner(
                    modifier = Modifier.welcomeEntrance(isVisible = isVisible, delayMillis = 300)
                )

                // 终端卡片布局
                WelcomeTerminal(
                    isVisible = isVisible,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(terminalHeight)
                        .welcomeEntrance(isVisible = isVisible)
                )

                WelcomeIntroduction(
                    availableAgreement = availableAgreement,
                    agreementChecked = agreementChecked,
                    onAgreementCheckedChange = onAgreementCheckedChange,
                    onAgreementRequiredChange = onAgreementRequiredChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .welcomeEntrance(isVisible = isVisible, delayMillis = 300)
                )
            }

            StartButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(28.dp)
                    .welcomeEntrance(isVisible = isVisible, delayMillis = 300),
                started = started,
                onStart = onStart,
                canStart = !agreementRequired || agreementChecked,
            )
        }
    }
}

@Composable
private fun WelcomeInformation(
    started: Boolean,
    onStart: () -> Unit,
    availableAgreement: StateFlow<AvailableAgreement?>,
    agreementRequired: Boolean,
    agreementChecked: Boolean,
    onAgreementCheckedChange: (Boolean) -> Unit,
    onAgreementRequiredChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        val listState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fadeEdge(listState)
                .verticalScrollWithBar(listState)
                .padding(bottom = 84.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProductBanner(
                modifier = Modifier.fillMaxWidth()
            )

            WelcomeIntroduction(
                availableAgreement = availableAgreement,
                agreementChecked = agreementChecked,
                onAgreementCheckedChange = onAgreementCheckedChange,
                onAgreementRequiredChange = onAgreementRequiredChange,
                modifier = Modifier.fillMaxWidth()
            )
        }

        StartButton(
            started = started,
            onStart = onStart,
            canStart = !agreementRequired || agreementChecked,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

/** 产品横幅 */
@Composable
private fun ProductBanner(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            space = if (compact) 10.dp else 16.dp,
            alignment = if (compact) Alignment.CenterHorizontally else Alignment.Start
        )
    ) {
        Image(
            modifier = Modifier.size(if (compact) 36.dp else 52.dp),
            painter = painterResource(R.drawable.img_launcher),
            contentDescription = null
        )

        Text(
            text = BuildKeys.LAUNCHER_NAME,
            style = if (compact) {
                MaterialTheme.typography.titleSmall
            } else {
                MaterialTheme.typography.headlineSmall
            },
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun WelcomeIntroduction(
    availableAgreement: StateFlow<AvailableAgreement?>,
    agreementChecked: Boolean,
    onAgreementCheckedChange: (Boolean) -> Unit,
    onAgreementRequiredChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val agreement by availableAgreement.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var currentAgreement by remember(agreement) {
        mutableStateOf<AvailableAgreement.DocumentFile?>(null)
    }
    var documents by remember(agreement) {
        mutableStateOf<List<AvailableAgreement.DocumentFile.Data>>(emptyList())
    }
    var selectedDocument by remember(agreement) {
        mutableStateOf<AvailableAgreement.DocumentFile.Data?>(null)
    }

    LaunchedEffect(agreement) {
        val region = if (Locale.getDefault().country.equals("CN", ignoreCase = true)) "zh" else "global"
        val selectedAgreement = agreement?.files?.firstOrNull {
            it.effectiveRegion.equals(region, ignoreCase = true)
        } ?: agreement?.files?.firstOrNull {
            it.effectiveRegion.equals("global", ignoreCase = true)
        }
        currentAgreement = selectedAgreement

        val availableDocuments = withContext(Dispatchers.IO) {
            selectedAgreement?.dataList.orEmpty().filter {
                it.title.isNotBlank() && it.path.isNotBlank() && runCatching {
                    context.assets.open(it.path).close()
                }.isSuccess
            }
        }
        documents = availableDocuments
        onAgreementRequiredChange(availableDocuments.isNotEmpty())
        if (availableDocuments.isEmpty()) onAgreementCheckedChange(false)
    }

    CompositionLocalProvider(
        LocalTextStyle provides (if (compact) {
            MaterialTheme.typography.bodySmall
        } else {
            MaterialTheme.typography.bodyMedium
        })
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.welcome_screen_intro_primary, BuildKeys.LAUNCHER_NAME),
                    lineHeight = if (compact) 20.sp else 26.sp
                )
                Text(
                    text = stringResource(R.string.welcome_screen_intro_core),
                    lineHeight = if (compact) 20.sp else 26.sp
                )
            }

            if (documents.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.welcome_screen_agreement_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        documents.forEach { document ->
                            InfoLayoutTextItem(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { selectedDocument = document },
                                title = document.title,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = agreementChecked,
                            onCheckedChange = onAgreementCheckedChange
                        )

                        Text(
                            text = stringResource(R.string.welcome_screen_agreement_confirmation),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }

    selectedDocument?.let { document ->
        AgreementDocumentDialog(
            document = document,
            onDismissRequest = { selectedDocument = null }
        )
    }
}

@Composable
private fun StartButton(
    started: Boolean,
    onStart: () -> Unit,
    canStart: Boolean,
    modifier: Modifier = Modifier
) {
    ScalingActionButton(
        modifier = modifier.defaultMinSize(minHeight = 52.dp),
        enabled = canStart && !started,
        onClick = onStart
    ) {
        Text(text = stringResource(R.string.welcome_screen_start))
    }
}
