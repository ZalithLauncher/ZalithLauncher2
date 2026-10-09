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

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.cardgrid.state.rememberCardGridState
import com.movtery.guide.GuideSide
import com.movtery.guide.guideNode
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.ActionMenuSide
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.SkinPreview3D
import com.movtery.zalithlauncher.ui.guide.GuideKeys
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.CommonVersionInfoLayout
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.screens.content.home.HomeGrid
import com.movtery.zalithlauncher.ui.screens.content.home.LocalActionMenuDrag
import com.movtery.zalithlauncher.ui.screens.content.home.actionMenuDragAnchor
import com.movtery.zalithlauncher.ui.screens.content.home.actionMenuDragExclusion
import com.movtery.zalithlauncher.ui.screens.content.home.rememberActionMenuDragState
import com.movtery.zalithlauncher.ui.screens.content.home.server.LocalServerCardQuickPlay
import com.movtery.zalithlauncher.ui.screens.content.home.version.LocalHomeCardLauncher
import com.movtery.zalithlauncher.ui.screens.content.home.version.LocalHomeCardVersionSettings
import com.movtery.zalithlauncher.ui.screens.game.elements.PerformanceSettingsDialog
import com.movtery.zalithlauncher.ui.screens.game.elements.PerformanceSettingsOperation
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.screens.removeAndNavigateTo
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlin.math.roundToInt

private const val ContentWeight = 7f
private const val ActionMenuWeight = 3f

/**
 * 操作菜单停泊槽位与屏幕边缘的间距
 */
private val ActionMenuOuterPadding = 12.dp

@Composable
fun LauncherScreen(
    backStackViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    onLaunchGame: (Version?) -> Unit,
    onQuickPlayServer: (Version?, String) -> Unit,
    onOpenLink: (String) -> Unit,
    startGuideOnce: (GuideKeys.Keys) -> Unit,
) {
    LaunchedEffect(Unit) {
        //发起新手引导
        startGuideOnce(GuideKeys.Main)
    }

    BaseScreen(
        screenKey = NormalNavKey.LauncherMain,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val dockedSide = AllSettings.launcherActionMenuSide.state
        val dragState = rememberActionMenuDragState(
            onCommit = { side -> AllSettings.launcherActionMenuSide.save(side) }
        )
        dragState.dockedSide = dockedSide
        dragState.isRtl = isRtl

        LaunchedEffect(isVisible) {
            //屏幕切走时打断进行中的拖拽
            if (!isVisible) dragState.onDragCancel()
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    dragState.parentOrigin = coordinates.positionInRoot()
                }
        ) {
            val parentWidthPx = constraints.maxWidth.toFloat()
            dragState.parentWidthPx = parentWidthPx
            dragState.outerPaddingPx = with(LocalDensity.current) { ActionMenuOuterPadding.toPx() }
            dragState.menuSpanPx = parentWidthPx * (ActionMenuWeight / (ActionMenuWeight + ContentWeight))

            //拖拽期间以预览侧为准
            val effectiveSide = dragState.previewSide ?: dockedSide

            //卡片尺寸与停泊槽内容区保持一致
            val cardWidth = maxWidth * (ActionMenuWeight / (ActionMenuWeight + ContentWeight)) - ActionMenuOuterPadding
            val cardHeight = maxHeight - ActionMenuOuterPadding * 2

            val toAccountManageScreen: () -> Unit = {
                backStackViewModel.mainScreen.navigateTo(
                    screenKey = NormalNavKey.AccountManager(FirstLoginMenu.NONE)
                )
            }
            val toVersionManageScreen: () -> Unit = {
                backStackViewModel.mainScreen.removeAndNavigateTo(
                    remove = NestedNavKey.VersionSettings::class,
                    screenKey = NormalNavKey.VersionsManager
                )
            }
            val toVersionSettingsScreen: () -> Unit = {
                VersionsManager.currentVersion.value?.let { version ->
                    navigateToVersions(version)
                }
            }

            // 内容区域
            Row(modifier = Modifier.fillMaxSize()) {
                // ActionMenu 对接到了 Start，留出空位
                if (dockedSide == ActionMenuSide.START) {
                    Spacer(modifier = Modifier.weight(ActionMenuWeight))
                }

                CompositionLocalProvider(
                    LocalUriHandler provides object : UriHandler {
                        override fun openUri(uri: String) {
                            onOpenLink(uri)
                        }
                    }
                ) {
                ContentMenu(
                    modifier = Modifier
                        .guideNode(
                            key = GuideKeys.Main.Step.CardTip,
                            holeRadius = 0.dp
                        )
                        .weight(ContentWeight)
                        .offset { IntOffset(x = dragState.previewShift.value.roundToInt(), y = 0) },
                    isVisible = isVisible,
                    onLaunchGame = { version ->
                        onLaunchGame(version)
                    },
                    onQuickPlayServer = onQuickPlayServer,
                    onOpenVersionSettings = navigateToVersions
                )
                }

                // ActionMenu 对接到了 End，留出空位
                if (dockedSide == ActionMenuSide.END) {
                    Spacer(modifier = Modifier.weight(ActionMenuWeight))
                }
            }

            val isActionMenuTaller = maxHeight >= 600.dp
            CompositionLocalProvider(LocalActionMenuDrag provides dragState) {
                Box(
                    modifier = Modifier
                        .offset {
                            val translation = if (dragState.floating) {
                                dragState.cardPosition
                            } else {
                                dragState.landingOf(effectiveSide) + dragState.settleOffset.value
                            }
                            val x = if (isRtl) parentWidthPx - cardWidth.toPx() - translation.x else translation.x
                            IntOffset(x.roundToInt(), translation.y.roundToInt())
                        }
                        .size(cardWidth, cardHeight)
                ) {
                    ActionMenu(
                        modifier = Modifier.fillMaxSize(),
                        isVisible = isVisible,
                        isTaller = isActionMenuTaller,
                        dockedSide = dockedSide,
                        onLaunchGame = onLaunchGame,
                        swapTargetValue = if (dockedSide == ActionMenuSide.END) 40.dp else (-40).dp,
                        pickUpScale = { dragState.scale },
                        toAccountManageScreen = toAccountManageScreen,
                        toVersionManageScreen = toVersionManageScreen,
                        toVersionSettingsScreen = toVersionSettingsScreen
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentMenu(
    isVisible: Boolean,
    onLaunchGame: (Version) -> Unit,
    onQuickPlayServer: (Version?, String) -> Unit,
    onOpenVersionSettings: (Version) -> Unit,
    modifier: Modifier = Modifier,
) {
    val yOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )
    val gridState = rememberCardGridState()

    CompositionLocalProvider(
        LocalHomeCardLauncher provides onLaunchGame,
        LocalHomeCardVersionSettings provides onOpenVersionSettings,
        LocalServerCardQuickPlay provides onQuickPlayServer
    ) {
        HomeGrid(
            state = gridState,
            isVisible = isVisible,
            modifier = modifier
                .fillMaxSize()
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
        )
    }
}

@Composable
private fun AccountAvatarCenter(
    modifier: Modifier = Modifier,
    account: Account?,
    refreshKey: Any? = null,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(all = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (account != null) {
                PlayerFace(
                    account = account,
                    avatarSize = 64.dp,
                    refreshKey = refreshKey
                )
            } else {
                Icon(
                    modifier = Modifier.size(40.dp),
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = account?.username ?: stringResource(R.string.account_add_new_account),
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall
                )
                if (account != null) {
                    Text(
                        text = getAccountTypeName(account),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountAvatarRow(
    modifier: Modifier = Modifier,
    account: Account?,
    refreshKey: Any? = null,
) {
    Row(
        modifier = modifier.padding(all = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (account != null) {
            PlayerFace(
                modifier = Modifier.padding(start = 4.dp),
                account = account,
                avatarSize = 48.dp,
                refreshKey = refreshKey
            )
        } else {
            Icon(
                modifier = Modifier.size(40.dp),
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null
            )
        }

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = account?.username ?: stringResource(R.string.account_add_new_account),
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall
            )
            if (account != null) {
                Text(
                    text = getAccountTypeName(account),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun VersionsContent(
    onLaunchGame: (Version?) -> Unit,
    toVersionManageScreen: () -> Unit,
    toVersionSettingsScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val version by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val isRefreshing by VersionsManager.isRefreshing.collectAsStateWithLifecycle()

    var showList by remember { mutableStateOf(false) }
    var versionManagerRow by remember { mutableStateOf<LayoutCoordinates?>(null) }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned { coordinates ->
                            versionManagerRow = coordinates
                        }
                ) {
                    VersionManagerLayout(
                        isRefreshing = isRefreshing,
                        version = version,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        swapToVersionManage = toVersionManageScreen,
                        openListMenu = { showList = true },
                    )
                }
                version?.takeIf { !isRefreshing && it.isValid() }?.let {
                    IconButton(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = toVersionSettingsScreen
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings_filled),
                            contentDescription = stringResource(R.string.versions_manage_settings)
                        )
                    }
                }
            }

            val menuAnchor = versionManagerRow
            val menuAnchorBounds = menuAnchor?.boundsInParent()
            val menuAnchorX = menuAnchorBounds?.left ?: 0f
            val menuAnchorHeight = menuAnchorBounds?.height ?: 0f

            DropdownMenu(
                expanded = showList && menuAnchor != null,
                onDismissRequest = { showList = false },
                modifier = Modifier.width(260.dp),
                offset = DpOffset(
                    x = with(LocalDensity.current) { menuAnchorX.toDp() },
                    y = with(LocalDensity.current) { (-menuAnchorHeight).toDp() } - 8.dp
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                val versions by VersionsManager.versions.collectAsStateWithLifecycle()
                versions.forEach { version0 ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CommonVersionInfoLayout(
                                    modifier = Modifier.weight(1f),
                                    version = version0,
                                    iconSize = 28.dp
                                )
                                IconButton(
                                    onClick = {
                                        onLaunchGame(version0)
                                        showList = false
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_play_arrow_filled),
                                        contentDescription = stringResource(R.string.main_launch_game),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        onClick = {
                            if (version == version0) return@DropdownMenuItem
                            VersionsManager.saveVersion(version0)
                            showList = false
                        }
                    )
                }
            }
        }

        ScalingActionButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingValues(horizontal = 12.dp))
                .padding(bottom = 8.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
            onClick = {
                onLaunchGame(null)
            },
            content = {
                MarqueeText(text = stringResource(R.string.main_launch_game))
            }
        )
    }
}

@Composable
private fun ActionMenuCardContent(
    modifier: Modifier = Modifier,
    account: Account?,
    onLaunchGame: (Version?) -> Unit,
    toAccountManageScreen: () -> Unit,
    toVersionManageScreen: () -> Unit,
    toVersionSettingsScreen: () -> Unit,
) {
    BackgroundCard(
        modifier = Modifier
            .actionMenuDragAnchor()
            .guideNode(GuideKeys.Main.Step.CardDrag)
            .then(modifier),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (accountAvatar, versionManagerLayout) = createRefs()

            AccountAvatarCenter(
                modifier = Modifier
                    .constrainAs(accountAvatar) {
                        top.linkTo(parent.top)
                        bottom.linkTo(versionManagerLayout.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }.guideNode(
                        key = GuideKeys.Main.Step.Account,
                        preferSide = GuideSide.Below
                    ),
                account = account,
                onClick = toAccountManageScreen
            )

            VersionsContent(
                modifier = Modifier.constrainAs(versionManagerLayout) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                },
                onLaunchGame = onLaunchGame,
                toVersionManageScreen = toVersionManageScreen,
                toVersionSettingsScreen = toVersionSettingsScreen,
            )
        }
    }
}

@Composable
private fun ActionMenuTallerContent(
    modifier: Modifier = Modifier,
    account: Account?,
    dockedSide: ActionMenuSide,
    onLaunchGame: (Version?) -> Unit,
    toAccountManageScreen: () -> Unit,
    toVersionManageScreen: () -> Unit,
    toVersionSettingsScreen: () -> Unit,
) {
    val refreshWardrobe by AccountsManager.refreshWardrobe.collectAsStateWithLifecycle()
    val skinFile = remember(account, refreshWardrobe) {
        account?.getSkinFile()?.takeIf { it.exists() }
    }
    val capeFile = remember(account, refreshWardrobe) {
        account?.getCapeFile()?.takeIf { it.exists() }
    }

    Column(
        modifier = Modifier
            .actionMenuDragAnchor()
            .guideNode(
                key = GuideKeys.Main.Step.CardDrag,
                holeRadius = 0.dp,
            )
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val azimuth = (if (dockedSide == ActionMenuSide.START) -35 else 35) * (if (isRtl) -1 else 1)

        SkinPreview3D(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            skinFile = skinFile,
            capeFile = capeFile,
            modelType = account?.skinModelType,
            interactionEnabled = false,
            azimuth = azimuth,
        )

        BackgroundCard(
            modifier = Modifier
                .guideNode(
                    key = GuideKeys.Main.Step.Account,
                    preferSide = GuideSide.Above,
                    holeRadius = 28.dp
                )
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            onClick = toAccountManageScreen
        ) {
            AccountAvatarRow(
                modifier = Modifier.fillMaxWidth(),
                account = account,
            )
        }

        BackgroundCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            VersionsContent(
                modifier = Modifier.fillMaxWidth(),
                onLaunchGame = onLaunchGame,
                toVersionManageScreen = toVersionManageScreen,
                toVersionSettingsScreen = toVersionSettingsScreen,
            )
        }
    }
}

@Composable
private fun ActionMenu(
    isVisible: Boolean,
    isTaller: Boolean,
    dockedSide: ActionMenuSide,
    onLaunchGame: (Version?) -> Unit,
    swapTargetValue: Dp,
    pickUpScale: () -> Float,
    modifier: Modifier = Modifier,
    toAccountManageScreen: () -> Unit = {},
    toVersionManageScreen: () -> Unit = {},
    toVersionSettingsScreen: () -> Unit = {}
) {
    val xOffset by swapAnimateDpAsState(
        targetValue = swapTargetValue,
        swapIn = isVisible,
        isHorizontal = true
    )

    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()

    val contentModifier = modifier.graphicsLayer {
        val scale = pickUpScale()
        scaleX = scale
        scaleY = scale
    }.offset { IntOffset(x = xOffset.roundToPx(), y = 0) }

    if (isTaller) {
        ActionMenuTallerContent(
            modifier = contentModifier,
            account = account,
            dockedSide = dockedSide,
            onLaunchGame = onLaunchGame,
            toAccountManageScreen = toAccountManageScreen,
            toVersionManageScreen = toVersionManageScreen,
            toVersionSettingsScreen = toVersionSettingsScreen,
        )
    } else {
        ActionMenuCardContent(
            modifier = contentModifier,
            account = account,
            onLaunchGame = onLaunchGame,
            toAccountManageScreen = toAccountManageScreen,
            toVersionManageScreen = toVersionManageScreen,
            toVersionSettingsScreen = toVersionSettingsScreen,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VersionManagerLayout(
    isRefreshing: Boolean,
    version: Version?,
    swapToVersionManage: () -> Unit,
    openListMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .actionMenuDragExclusion()
            .clip(shape = MaterialTheme.shapes.large)
            .combinedClickable(
                role = Role.Button,
                onClick = swapToVersionManage,
                onLongClick = {
                    if (version != null) openListMenu()
                }
            ).guideNode(
                key = GuideKeys.Main.Step.VersionList,
                preferSide = GuideSide.Above,
            ).padding(PaddingValues(all = 8.dp))
    ) {
        if (isRefreshing) {
            Box(modifier = Modifier.fillMaxWidth()) {
                LoadingIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center)
                )
            }
        } else {
            VersionIconImage(
                version = version,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(8.dp))

            if (version == null) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .basicMarquee(iterations = Int.MAX_VALUE),
                    text = stringResource(R.string.versions_manage_no_versions),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        text = version.getVersionName(),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                    if (version.isValid()) {
                        Text(
                            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                            text = version.getVersionSummary(),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

