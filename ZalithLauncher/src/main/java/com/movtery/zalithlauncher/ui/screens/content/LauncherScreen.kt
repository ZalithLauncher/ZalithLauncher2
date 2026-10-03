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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.path.URL_RELEASES
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
import com.movtery.zalithlauncher.ui.screens.content.navigateToDownload
import com.movtery.zalithlauncher.ui.screens.content.elements.CommonVersionInfoLayout
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.screens.content.home.MiraiHomeDashboard
import com.movtery.zalithlauncher.ui.screens.content.home.PlayerSkinStage
import com.movtery.zalithlauncher.ui.screens.content.home.resolveRendererBadgeDetail
import com.movtery.zalithlauncher.ui.screens.content.home.resolveRendererShortLabel
import com.movtery.zalithlauncher.ui.screens.content.home.LocalActionMenuDrag
import com.movtery.zalithlauncher.ui.screens.content.home.actionMenuDragAnchor
import com.movtery.zalithlauncher.ui.screens.content.home.actionMenuDragExclusion
import com.movtery.zalithlauncher.ui.screens.content.home.rememberActionMenuDragState
import com.movtery.zalithlauncher.ui.screens.content.home.version.LocalHomeCardLauncher
import com.movtery.zalithlauncher.ui.screens.content.home.version.LocalHomeCardVersionSettings
import com.movtery.zalithlauncher.ui.theme.MiraiThemeManager
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlin.math.roundToInt

private const val ContentWeight = 7.4f
private const val ActionMenuWeight = 2.6f

/**
 * 操作菜单停泊槽位与屏幕边缘的间距
 */
private val ActionMenuOuterPadding = 12.dp

@Composable
fun LauncherScreen(
    backStackViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    onLaunchGame: (Version?) -> Unit,
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
            val showActionMenu = maxWidth >= 500.dp
            LaunchedEffect(showActionMenu) {
                if (!showActionMenu) dragState.onDragCancel()
            }
            val contentWeight = if (showActionMenu) ContentWeight else 1f
            dragState.parentWidthPx = parentWidthPx
            dragState.outerPaddingPx = with(LocalDensity.current) { ActionMenuOuterPadding.toPx() }
            dragState.menuSpanPx = if (showActionMenu) {
                parentWidthPx * (ActionMenuWeight / (ActionMenuWeight + ContentWeight))
            } else 0f

            //拖拽期间以预览侧为准
            val effectiveSide = dragState.previewSide ?: dockedSide

            //卡片尺寸与停泊槽内容区保持一致；compact layouts use the account/library routes instead.
            val cardWidth = if (showActionMenu) {
                maxWidth * (ActionMenuWeight / (ActionMenuWeight + ContentWeight)) - ActionMenuOuterPadding
            } else 0.dp
            val cardHeight = maxHeight - ActionMenuOuterPadding * 2

            val toAccountManageScreen: () -> Unit = {
                val menu = if (AccountsManager.currentAccountFlow.value == null) {
                    FirstLoginMenu.CHOOSE_LOGIN_TYPE
                } else {
                    FirstLoginMenu.NONE
                }
                backStackViewModel.mainScreen.navigateTo(
                    screenKey = NormalNavKey.AccountManager(menu),
                    useClassEquality = true
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
                // On narrow screens, account, skin, and version routes remain available
                // from the primary rail and the dashboard; keep the canvas usable.
                if (showActionMenu && dockedSide == ActionMenuSide.START) {
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
                            .weight(contentWeight)
                            .offset { IntOffset(x = dragState.previewShift.value.roundToInt(), y = 0) },
                        isVisible = isVisible,
                        onLaunchGame = onLaunchGame,
                        onOpenVersionSettings = navigateToVersions,
                        onExploreContent = {
                            backStackViewModel.navigateToDownload(backStackViewModel.downloadModScreen)
                        },
                        onCreateInstance = {
                            backStackViewModel.navigateToDownload(backStackViewModel.downloadGameScreen)
                        },
                        onManageVersions = toVersionManageScreen,
                        onOpenFileManager = {
                            backStackViewModel.mainScreen.backStack.navigateToFileSelector(
                                startPath = PathManager.DIR_FILES_EXTERNAL.absolutePath,
                                selectFile = false,
                                saveKey = NormalNavKey.LauncherMain
                            ) {}
                        }
                    )
                }

                // ActionMenu 对接到了 End，留出空位
                if (showActionMenu && dockedSide == ActionMenuSide.END) {
                    Spacer(modifier = Modifier.weight(ActionMenuWeight))
                }
            }

            val isActionMenuTaller = maxHeight >= 600.dp
            if (showActionMenu) {
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
                            toVersionSettingsScreen = toVersionSettingsScreen,
                            onOpenLink = onOpenLink
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentMenu(
    isVisible: Boolean,
    onLaunchGame: (Version?) -> Unit,
    onOpenVersionSettings: (Version) -> Unit,
    onExploreContent: () -> Unit,
    onCreateInstance: () -> Unit,
    onManageVersions: () -> Unit,
    onOpenFileManager: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val yOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    CompositionLocalProvider(
        LocalHomeCardLauncher provides { version -> onLaunchGame(version) },
        LocalHomeCardVersionSettings provides onOpenVersionSettings
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MiraiHomeDashboard(
                onLaunchVersion = { version -> onLaunchGame(version) },
                onOpenVersionSettings = onOpenVersionSettings,
                onExploreContent = onExploreContent,
                onCreateInstance = onCreateInstance,
                onManageVersions = onManageVersions,
                onOpenFileManager = onOpenFileManager,
                modifier = Modifier.fillMaxSize()
            )
        }
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
            modifier = Modifier.weight(1f),
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
        Icon(
            painter = painterResource(R.drawable.ic_arrow_right_rounded),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
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
    var showPick by remember { mutableStateOf(false) }
    var versionManagerRow by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val versionSubtitle = remember(version) {
        val info = version?.getVersionInfo()
        val mcVer = info?.minecraftVersion
        val loader = info?.loaderInfo?.loader?.displayName
        when {
            loader != null && mcVer != null -> "$loader $mcVer"
            mcVer != null -> "Minecraft $mcVer"
            else -> "Select Instance"
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Compact 2-line Instance Selector Pill
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .onGloballyPositioned { coordinates ->
                    versionManagerRow = coordinates
                }
                .clip(RoundedCornerShape(14.dp))
                .clickable {
                    if (version != null) showList = true else toVersionManageScreen()
                }
                .guideNode(
                    key = GuideKeys.Main.Step.VersionList,
                    preferSide = GuideSide.Above,
                ),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF171A20),
            border = BorderStroke(1.dp, Color(0xFF2E333E))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (isRefreshing) {
                            "Loading..."
                        } else {
                            version?.getVersionName() ?: stringResource(R.string.versions_manage_no_versions)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = versionSubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MiraiThemeManager.currentAccent(),
                        maxLines = 1
                    )
                }

                val menuAnchor = versionManagerRow
                val menuAnchorBounds = menuAnchor?.boundsInParent()
                val menuAnchorX = menuAnchorBounds?.left ?: 0f
                val menuAnchorHeight = menuAnchorBounds?.height ?: 0f

                DropdownMenu(
                    expanded = showList && menuAnchor != null,
                    onDismissRequest = { showList = false },
                    modifier = Modifier.width(240.dp),
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
                                        iconSize = 26.dp
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
                                            tint = Color(0xFF1BD96A)
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
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Manage All Instances...",
                                color = Color(0xFF1BD96A),
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        onClick = {
                            showList = false
                            toVersionManageScreen()
                        }
                    )
                }
            }
        }

        // Vibrant Pill PLAY Button (Mockup #1)
        val activeAccent = MiraiThemeManager.currentAccent()
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .padding(bottom = 10.dp)
                .height(42.dp)
                .clip(RoundedCornerShape(21.dp))
                .combinedClickable(
                    role = Role.Button,
                    onClick = { onLaunchGame(null) },
                    onLongClick = { showPick = true }
                ),
            shape = RoundedCornerShape(21.dp),
            color = activeAccent,
            contentColor = Color(0xFF06210F)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "PLAY",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF06210F)
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_play_arrow_filled),
                    contentDescription = stringResource(R.string.main_launch_game),
                    tint = Color(0xFF06210F),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (showPick) {
            AlertDialog(
                onDismissRequest = { showPick = false },
                title = { Text("Choose before launch") },
                text = {
                    Column {
                        Text("Version: ${version?.getVersionName() ?: "None selected"}")
                        TextButton(onClick = {
                            AllSettings.graphicsApi.save(com.movtery.zalithlauncher.game.version.installed.GraphicsApi.DEFAULT_OPENGL)
                            AllSettings.miraiVulkanFailCount.save(0)
                            showPick = false
                        }) { Text("Use OpenGL") }
                        TextButton(onClick = {
                            val vulkan = com.movtery.zalithlauncher.game.version.installed.GraphicsApi.entries.firstOrNull { it.name.contains("VULKAN") }
                            if (vulkan != null) AllSettings.graphicsApi.save(vulkan)
                            showPick = false
                        }) { Text("Use Vulkan") }
                        TextButton(onClick = { toVersionManageScreen(); showPick = false }) { Text("Pick a version") }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPick = false; onLaunchGame(null) }) { Text("Launch") }
                }
            )
        }
        if (AllSettings.miraiVulkanFailCount.state >= 2 && AllSettings.graphicsApi.state.name.contains("VULKAN")) {
            AlertDialog(
                onDismissRequest = { AllSettings.miraiVulkanFailCount.save(0) },
                title = { Text("Vulkan crashed twice") },
                text = { Text("Switch the next launch to OpenGL?") },
                confirmButton = {
                    TextButton(onClick = {
                        AllSettings.graphicsApi.save(com.movtery.zalithlauncher.game.version.installed.GraphicsApi.DEFAULT_OPENGL)
                        AllSettings.miraiVulkanFailCount.save(0)
                    }) { Text("Use OpenGL") }
                },
                dismissButton = {
                    TextButton(onClick = { AllSettings.miraiVulkanFailCount.save(0) }) { Text("Keep Vulkan") }
                }
            )
        }
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
    val refreshWardrobe by AccountsManager.refreshWardrobe.collectAsStateWithLifecycle()
    val skinFile = remember(account, refreshWardrobe) {
        account?.getSkinFile()?.takeIf { it.exists() }
    }
    val capeFile = remember(account, refreshWardrobe) {
        account?.getCapeFile()?.takeIf { it.exists() }
    }

    Surface(
        modifier = Modifier
            .actionMenuDragAnchor()
            .guideNode(GuideKeys.Main.Step.CardDrag)
            .then(modifier),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E2128),
        border = BorderStroke(1.dp, Color(0xFF2C303A))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 3D Skin & Cape Stage on Dark Pedestal (Mockup #1) — Tapping opens Account Creation / Management
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 6.dp, start = 8.dp, end = 8.dp)
                    .actionMenuDragExclusion()
                    .guideNode(
                        key = GuideKeys.Main.Step.Account,
                        preferSide = GuideSide.Below
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = toAccountManageScreen),
                contentAlignment = Alignment.Center
            ) {
                // Dark 3D Pedestal Block at bottom of stage with account/create label
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-4).dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF13151A),
                    border = BorderStroke(1.dp, MiraiThemeManager.currentAccent().copy(alpha = 0.45f))
                ) {
                    Text(
                        text = account?.username ?: "+ Add Account",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (account != null) Color.White else MiraiThemeManager.currentAccent(),
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                SkinPreview3D(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 14.dp),
                    skinFile = skinFile,
                    capeFile = capeFile,
                    modelType = account?.skinModelType,
                    animation = null,
                    interactionEnabled = false,
                    azimuth = 28,
                )

                // Top-layer transparent touch catcher so WebView never swallows clicks
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = toAccountManageScreen)
                )
            }

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
private fun ActionMenuTallerContent(
    modifier: Modifier = Modifier,
    account: Account?,
    dockedSide: ActionMenuSide,
    onLaunchGame: (Version?) -> Unit,
    toAccountManageScreen: () -> Unit,
    toVersionManageScreen: () -> Unit,
    toVersionSettingsScreen: () -> Unit,
    onOpenLink: (String) -> Unit
) {
    ActionMenuCardContent(
        modifier = modifier,
        account = account,
        onLaunchGame = onLaunchGame,
        toAccountManageScreen = toAccountManageScreen,
        toVersionManageScreen = toVersionManageScreen,
        toVersionSettingsScreen = toVersionSettingsScreen
    )
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
    toVersionSettingsScreen: () -> Unit = {},
    onOpenLink: (String) -> Unit = {}
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
            onOpenLink = onOpenLink
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
