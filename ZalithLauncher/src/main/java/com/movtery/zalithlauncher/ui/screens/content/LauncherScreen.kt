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

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.cardgrid.state.rememberCardGridState
import com.movtery.guide.GuideSide
import com.movtery.guide.guideNode
import com.movtery.petlib.PetAnimation
import com.movtery.petlib.PetTapReaction
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.game.account.wardrobe.toArmModel
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.setting.LauncherSettings
import com.movtery.zalithlauncher.setting.enums.ActionMenuSide
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedDirection
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.CutoutBox
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.PetModelView
import com.movtery.zalithlauncher.ui.components.ScalingIconButton
import com.movtery.zalithlauncher.ui.components.animated
import com.movtery.zalithlauncher.ui.components.rememberAnimatedContext
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
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.rememberSettingsCardShape
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        val dockedSide = LauncherSettings.launcherActionMenuSide.state
        val dragState = rememberActionMenuDragState(
            onCommit = { side -> LauncherSettings.launcherActionMenuSide.save(side) }
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
                                holeShape = RectangleShape
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
                        dockedSide = dockedSide,
                        onLaunchGame = onLaunchGame,
                        swapDirection = if (dockedSide == ActionMenuSide.END) AnimatedDirection.Right else AnimatedDirection.Left,
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
    val ctx = rememberAnimatedContext(isVisible = isVisible)
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
                .animated(ctx)
        )
    }
}

@Composable
private fun AccountAvatarRow(
    modifier: Modifier = Modifier,
    avatarSize: Dp = 48.dp,
    placeholderSize: Dp = 40.dp,
    textSpacer: Dp = 4.dp,
    refreshKey: Any? = null,
) {
    val account0 by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val account = account0

    Row(
        modifier = modifier.padding(all = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (account != null) {
            PlayerFace(
                modifier = Modifier.padding(start = 4.dp),
                account = account,
                avatarSize = avatarSize,
                refreshKey = refreshKey
            )
        } else {
            Icon(
                modifier = Modifier.size(placeholderSize),
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null
            )
        }

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(textSpacer)
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
private fun VersionListMenu(
    show: Boolean,
    versions: List<Version>,
    menuAnchor: LayoutCoordinates?,
    version: Version?,
    onLaunchGame: (Version?) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val menuAnchorBounds = menuAnchor?.boundsInParent()
    val menuAnchorX = menuAnchorBounds?.left ?: 0f
    val menuAnchorHeight = menuAnchorBounds?.height ?: 0f

    DropdownMenu(
        expanded = show && menuAnchor != null && versions.isNotEmpty(),
        onDismissRequest = onDismissRequest,
        modifier = Modifier.width(260.dp),
        offset = DpOffset(
            x = with(LocalDensity.current) { menuAnchorX.toDp() },
            y = with(LocalDensity.current) { (-menuAnchorHeight).toDp() } - 8.dp
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
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
                                onDismissRequest()
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
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
private fun Modifier.versionLayout(
    swapToVersionManage: () -> Unit,
    requestOpenVersionList: () -> Unit,
) = this
    .actionMenuDragExclusion()
    .combinedClickable(
        role = Role.Button,
        onClick = swapToVersionManage,
        onLongClick = requestOpenVersionList,
    )
    .guideNode(
        key = GuideKeys.Main.Step.VersionList,
        holeShape = RectangleShape,
        preferSide = GuideSide.Above,
    )


@Composable
private fun VersionsContent(
    version: Version?,
    onLaunchGame: (Version?) -> Unit,
    toVersionManageScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Max),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var showList by remember { mutableStateOf(false) }
        var versionManagerRow by remember { mutableStateOf<LayoutCoordinates?>(null) }

        if (version == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clickable(onClick = toVersionManageScreen)
                    .padding(all = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MarqueeText(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.versions_manage_no_versions),
                    style = MaterialTheme.typography.labelMedium,
                )

                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right_rounded),
                    contentDescription = stringResource(R.string.versions_manage_install_new)
                )
            }
        } else {
            val versions by VersionsManager.versions.collectAsStateWithLifecycle()

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 版本列表
                VersionListMenu(
                    show = showList,
                    versions = versions,
                    menuAnchor = versionManagerRow,
                    version = version,
                    onLaunchGame = onLaunchGame,
                    onDismissRequest = { showList = false },
                )

                // 版本名称/描述
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .versionLayout(
                            swapToVersionManage = toVersionManageScreen,
                            requestOpenVersionList = { showList = versions.isNotEmpty() }
                        )
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                        .onGloballyPositioned { coordinates ->
                            versionManagerRow = coordinates
                        }
                ) {
                    MarqueeText(
                        text = version.getVersionName(),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    if (version.isValid()) {
                        MarqueeText(
                            text = version.getVersionSummary(),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            // 启动游戏按钮
            ScalingIconButton(
                modifier = Modifier.padding(end = 8.dp),
                onClick = {
                    onLaunchGame(null)
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play_arrow_filled),
                    contentDescription = stringResource(R.string.main_launch_game),
                )
            }
        }
    }
}

@Composable
private fun PetModelContent(
    dockedSide: ActionMenuSide,
    modifier: Modifier = Modifier,
) {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val refreshWardrobe by AccountsManager.refreshWardrobe.collectAsStateWithLifecycle()

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    // 偏航角符号与停泊侧相反，让模型面向屏幕内容一侧
    val yaw = Math.toRadians(
        ((if (dockedSide == ActionMenuSide.START) 35.0 else -(35.0)) * (if (isRtl) -1 else 1))
    ).toFloat()

    val armModel = account?.skinModelType?.toArmModel()
    val skinStamp = account?.getSkinFile()?.lastModified()
    val capeStamp = account?.getCapeFile()?.lastModified()
    val skinBytes by produceState<ByteArray?>(null, account, refreshWardrobe, skinStamp) {
        value = withContext(Dispatchers.IO) {
            account?.getSkinFile()?.takeIf { it.exists() }
                ?.inputStream()?.use { it.readBytes() }
        }
    }
    val capeBytes by produceState<ByteArray?>(null, account, refreshWardrobe, capeStamp) {
        value = withContext(Dispatchers.IO) {
            account?.getCapeFile()?.takeIf { it.exists() }
                ?.inputStream()?.use { it.readBytes() }
        }
    }

    PetModelView(
        modifier = modifier,
        skin = skinBytes,
        cape = capeBytes,
        arm = armModel,
        yaw = yaw,
        animation = PetAnimation.Idle,
        tapReaction = PetTapReaction.Interact,
        dragYaw = false
    )
}

@Composable
private fun ActionMenuContent(
    modifier: Modifier = Modifier,
    dockedSide: ActionMenuSide,
    onLaunchGame: (Version?) -> Unit,
    toAccountManageScreen: () -> Unit,
    toVersionManageScreen: () -> Unit,
    toVersionSettingsScreen: () -> Unit,
) {
    ConstraintLayout(
        modifier = Modifier
            .actionMenuDragAnchor()
            .guideNode(
                key = GuideKeys.Main.Step.CardDrag,
                holeShape = MaterialTheme.shapes.extraLarge,
            )
            .then(modifier)
    ) {
        val (versionLayout, petModel) = createRefs()

        PetModelContent(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(petModel) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(anchor = versionLayout.top)
                    height = Dimension.fillToConstraints
                },
            dockedSide = dockedSide,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(versionLayout) {
                    bottom.linkTo(anchor = parent.bottom)
                },
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val accShape = rememberSettingsCardShape(CardPosition.Top)
            val bottomShape = rememberSettingsCardShape(CardPosition.Bottom)

            BackgroundCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .guideNode(
                        key = GuideKeys.Main.Step.Account,
                        holeShape = accShape,
                        preferSide = GuideSide.Below
                    ),
                shape = accShape,
                onClick = toAccountManageScreen,
            ) {
                AccountAvatarRow(
                    modifier = Modifier.fillMaxWidth(),
                    avatarSize = 38.dp,
                    placeholderSize = 32.dp,
                    textSpacer = 0.dp,
                )
            }

            val isRefreshing by VersionsManager.isRefreshing.collectAsStateWithLifecycle()
            if (isRefreshing) {
                BackgroundCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .guideNode(
                            key = GuideKeys.Main.Step.VersionList,
                            holeShape = bottomShape,
                            preferSide = GuideSide.Above,
                        ),
                    shape = rememberSettingsCardShape(CardPosition.Bottom),
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 26.dp)
                            .fillMaxWidth()
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    val version0 by VersionsManager.currentVersion.collectAsStateWithLifecycle()
                    val version = version0

                    // 版本图标
                    version?.let {
                        BackgroundCard(
                            modifier = Modifier.fillMaxHeight(),
                            shape = rememberSettingsCardShape(CardPosition.BottomStart),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .clickable(onClick = toVersionSettingsScreen)
                                    .padding(start = 12.dp, end = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CutoutBox(
                                    modifier = Modifier.size(38.dp),
                                    cutoutRadius = 20.dp,
                                    overlayAlignment = Alignment.BottomEnd,
                                    overlay = {
                                        Icon(
                                            modifier = Modifier.size(16.dp),
                                            painter = painterResource(R.drawable.ic_settings_filled),
                                            contentDescription = stringResource(R.string.versions_manage_settings)
                                        )
                                    }
                                ) {
                                    VersionIconImage(
                                        modifier = Modifier.fillMaxSize(),
                                        version = version,
                                    )
                                }
                            }
                        }
                    }

                    BackgroundCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = if (version == null) {
                            bottomShape
                        } else {
                            rememberSettingsCardShape(CardPosition.BottomEnd)
                        },
                    ) {
                        VersionsContent(
                            modifier = Modifier
                                .fillMaxWidth()
                                .let {
                                    if (version == null) it.guideNode(
                                        key = GuideKeys.Main.Step.VersionList,
                                        holeShape = bottomShape,
                                        preferSide = GuideSide.Above,
                                    ) else it
                                },
                            version = version,
                            onLaunchGame = onLaunchGame,
                            toVersionManageScreen = toVersionManageScreen,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionMenu(
    isVisible: Boolean,
    dockedSide: ActionMenuSide,
    onLaunchGame: (Version?) -> Unit,
    swapDirection: AnimatedDirection,
    pickUpScale: () -> Float,
    modifier: Modifier = Modifier,
    toAccountManageScreen: () -> Unit = {},
    toVersionManageScreen: () -> Unit = {},
    toVersionSettingsScreen: () -> Unit = {}
) {
    val ctx = rememberAnimatedContext(isVisible = isVisible)

    ActionMenuContent(
        modifier = modifier
            .graphicsLayer {
                val scale = pickUpScale()
                scaleX = scale
                scaleY = scale
            }
            .animated(ctx, from = swapDirection),
        dockedSide = dockedSide,
        onLaunchGame = onLaunchGame,
        toAccountManageScreen = toAccountManageScreen,
        toVersionManageScreen = toVersionManageScreen,
        toVersionSettingsScreen = toVersionSettingsScreen,
    )
}
