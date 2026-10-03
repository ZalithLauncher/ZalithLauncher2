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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.COPY_LABEL_ACCOUNT_UUID
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.auth_server.data.AuthServer
import com.movtery.zalithlauncher.game.account.isAuthServerAccount
import com.movtery.zalithlauncher.game.account.isLocalAccount
import com.movtery.zalithlauncher.game.account.isMicrosoftAccount
import com.movtery.zalithlauncher.game.account.isMicrosoftLogging
import com.movtery.zalithlauncher.game.account.yggdrasil.PlayerProfile
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.components.SimpleListDialog
import com.movtery.zalithlauncher.ui.components.SimpleListItem
import com.movtery.zalithlauncher.ui.components.SkinPreview3D
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountItem
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountSkinOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.ChangeSkinDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.LocalLoginDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.LocalLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.LoginMenuDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.LoginMenuOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftLoginTipDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftReloginDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.OtherAccountReloginDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.OtherLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.OtherServerLoginDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.ServerOperation
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.copyText
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.AccountManageEffect
import com.movtery.zalithlauncher.viewmodel.AccountManageIntent
import com.movtery.zalithlauncher.viewmodel.AccountManageViewModel
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.LocalBackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

/**
 * 封装账号界面 UI 交互的回调函数
 * 
 * @property onIntent 发送 MVI Intent 到 ViewModel
 * @property openLink 打开外部链接
 * @property backToMainScreen 返回主界面
 * @property navigateToWeb 导航到应用内浏览器界面
 * @property checkIfInWebScreen 检查当前是否在浏览器界面中（用于微软登录逻辑判断）
 * @property formatError 格式化异常为本地化字符串
 * @property submitError 提交错误到全局错误展示系统
 */
private data class AccountActions(
    val onIntent: (AccountManageIntent) -> Unit,
    val openLink: (url: String) -> Unit,
    val backToMainScreen: () -> Unit,
    val navigateToWeb: (url: String) -> Unit,
    val checkIfInWebScreen: () -> Boolean,
    val formatError: (Throwable) -> AndroidStringText,
    val submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
)

/**
 * 进入账号管理器时，可附加的打开登录菜单选项
 */
enum class FirstLoginMenu {
    /** 不打开菜单 */
    NONE,
    /** 打开微软登录菜单 */
    MICROSOFT,
    /** 打开总登录菜单 */
    NORMAL
}

/**
 * 账号管理主界面
 *
 * @param backStackViewModel 屏幕堆栈管理器
 * @param backToMainScreen 返回主屏幕的回调
 * @param openLink 外部链接跳转回调
 * @param submitError 全局错误提交回调
 */
@Composable
fun AccountManageScreen(
    key: NormalNavKey.AccountManager,
    backStackViewModel: ScreenBackStackViewModel,
    backToMainScreen: () -> Unit,
    openLink: (url: String) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    eventViewModel: EventViewModel
) {
    val viewModel: AccountManageViewModel = hiltViewModel { factory: AccountManageViewModel.Factory ->
        factory.create(eventViewModel)
    }

    val loginUiState by viewModel.loginUiState.collectAsStateWithLifecycle()
    val profileUiState by viewModel.profileUiState.collectAsStateWithLifecycle()
    val operationUiState by viewModel.operationUiState.collectAsStateWithLifecycle()

    val actions = remember(
        viewModel,
        backToMainScreen,
        openLink,
        backStackViewModel,
        submitError
    ) {
        AccountActions(
            onIntent = viewModel::onIntent,
            openLink = openLink,
            backToMainScreen = backToMainScreen,
            navigateToWeb = { url -> backStackViewModel.mainScreen.backStack.navigateToWeb(url) },
            checkIfInWebScreen = { backStackViewModel.mainScreen.currentKey is NormalNavKey.WebScreen },
            formatError = { th -> viewModel.formatAccountError(th) },
            submitError = submitError,
        )
    }

    LaunchedEffect(Unit) {
        when (key.loginMenu) {
            FirstLoginMenu.NONE -> {}
            FirstLoginMenu.MICROSOFT -> {
                actions.onIntent(AccountManageIntent.UpdateMicrosoftLoginOp(MicrosoftLoginOperation.Tip))
            }
            FirstLoginMenu.NORMAL -> {
                actions.onIntent(AccountManageIntent.UpdateLoginMenuOp(LoginMenuOperation.Login))
            }
        }

        viewModel.effect.collect { effect ->
            when (effect) {
                is AccountManageEffect.ShowError -> {
                    submitError(ErrorViewModel.ThrowableMessage(effect.title, effect.message))
                }
            }
        }
    }

    BaseScreen(
        screenKey = key,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        AccountManageContent(
            isVisible = isVisible,
            loginUiState = loginUiState,
            profileUiState = profileUiState,
            operationUiState = operationUiState,
            actions = actions
        )
    }
}

/**
 * 账号管理界面的实际内容布局
 */
@Composable
private fun AccountManageContent(
    isVisible: Boolean,
    loginUiState: AccountManageViewModel.LoginUiState,
    profileUiState: AccountManageViewModel.ProfileUiState,
    operationUiState: AccountManageViewModel.OperationUiState,
    actions: AccountActions,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        AccountsLayout(
            isVisible = isVisible,
            modifier = Modifier.fillMaxSize(),
            accounts = profileUiState.accounts,
            currentAccount = profileUiState.currentAccount,
            isOffline = profileUiState.isOffline,
            accountOperation = operationUiState.accountOp,
            accountSkinOperation = operationUiState.accountSkinOp,
            accountSkinDialogState = operationUiState.accountSkinDialogState,
            accountCapes = profileUiState.accountCapeOpMap,
            actions = actions
        )
    }

    LoginMenuOperation(loginUiState.menuOp, actions, profileUiState.authServers)
    MicrosoftLoginOperation(loginUiState.microsoftOp, actions)
    LocalLoginOperation(loginUiState.localOp, actions)
    OtherLoginOperation(loginUiState.otherOp, actions)
    ServerTypeOperation(operationUiState.serverOp, actions)
}

/**
 * 右侧 3D 皮肤与披风衣柜组件 (Mockup #5)
 */
@Composable
private fun ActionsLayout(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    currentAccount: Account?,
    actions: AccountActions
) {
    val xOffset by swapAnimateDpAsState(
        targetValue = 40.dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    val skinPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null && currentAccount != null) {
            actions.onIntent(
                AccountManageIntent.UpdateAccountSkinOp(
                    AccountSkinOperation.ChangeSkin(currentAccount)
                )
            )
            actions.onIntent(AccountManageIntent.OnSkinPicked(currentAccount, uri))
        }
    }
    val capePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null && currentAccount != null) {
            actions.onIntent(
                AccountManageIntent.UpdateAccountSkinOp(
                    AccountSkinOperation.ChangeSkin(currentAccount)
                )
            )
            actions.onIntent(AccountManageIntent.OnCapePicked(currentAccount, uri))
        }
    }

    Surface(
        modifier = modifier
            .offset { IntOffset(x = xOffset.roundToPx(), y = 0) }
            .fillMaxHeight(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF21242B),
        border = BorderStroke(1.dp, Color(0xFF2E333E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header + Wide/Slim Toggle Pill (Mockup #6)
            val isSlim = currentAccount?.skinModelType?.name?.contains("SLIM", ignoreCase = true) == true
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Skin & Cape Wardrobe",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF16181D),
                    border = BorderStroke(1.dp, Color(0xFF2E333E)),
                    onClick = {
                        currentAccount?.let { acc ->
                            actions.onIntent(
                                AccountManageIntent.UpdateAccountSkinOp(
                                    AccountSkinOperation.ChangeSkin(acc)
                                )
                            )
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (!isSlim) Color(0xFF1BD96A) else Color.Transparent
                        ) {
                            Text(
                                text = "Wide (4px)",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (!isSlim) Color(0xFF06210F) else Color(0xFF9CA3AF)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSlim) Color(0xFF1BD96A) else Color.Transparent
                        ) {
                            Text(
                                text = "Slim (3px)",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSlim) Color(0xFF06210F) else Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }

            // 3D Model Stage with Grass Block Pedestal (animation = null to prevent continuous WebGL repaints)
            val refreshWardrobe by AccountsManager.refreshWardrobe.collectAsStateWithLifecycle()
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF17191E),
                border = BorderStroke(1.dp, Color(0xFF282C36))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_old_grass_block),
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = (-12).dp),
                        alpha = 0.85f
                    )
                    SkinPreview3D(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 18.dp),
                        skinFile = remember(currentAccount, refreshWardrobe) {
                            currentAccount?.getSkinFile()?.takeIf { it.exists() }
                        },
                        capeFile = remember(currentAccount, refreshWardrobe) {
                            currentAccount?.getCapeFile()?.takeIf { it.exists() }
                        },
                        modelType = currentAccount?.skinModelType,
                        animation = null
                    )
                }
            }

            // Bottom Wardrobe Action Buttons (Mockup #6: Change Skin + Equip Cape)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    enabled = currentAccount != null,
                    shape = RoundedCornerShape(19.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1BD96A),
                        contentColor = Color(0xFF06210F)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    onClick = {
                        skinPicker.launch(arrayOf("image/png"))
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_checkroom),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "Change Skin",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(19.dp),
                    color = Color(0xFF181A20),
                    border = BorderStroke(1.dp, Color(0xFF2E333E)),
                    onClick = {
                        if (currentAccount?.isLocalAccount() == true) {
                            capePicker.launch(arrayOf("image/png"))
                        } else if (currentAccount != null) {
                            actions.onIntent(
                                AccountManageIntent.UpdateAccountSkinOp(
                                    AccountSkinOperation.ChangeSkin(currentAccount)
                                )
                            )
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_upload),
                            contentDescription = null,
                            tint = Color(0xFFE5E7EB),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "Equip Cape",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginMenuOperation(
    operation: LoginMenuOperation,
    actions: AccountActions,
    authServers: List<AuthServer>
) {
    when (operation) {
        LoginMenuOperation.None -> {}
        LoginMenuOperation.Login -> {
            LoginMenuDialog(
                onDismissRequest = {
                    actions.onIntent(
                        AccountManageIntent.UpdateLoginMenuOp(LoginMenuOperation.None)
                    )
                },
                authServers = authServers,
                onMicrosoftLogin = {
                    if (!isMicrosoftLogging()) {
                        actions.onIntent(
                            AccountManageIntent.UpdateMicrosoftLoginOp(
                                MicrosoftLoginOperation.Tip
                            )
                        )
                    }
                },
                onLocalLogin = {
                    actions.onIntent(AccountManageIntent.UpdateLocalLoginOp(LocalLoginOperation.Edit))
                },
                onAuthServerLogin = { server ->
                    actions.onIntent(
                        AccountManageIntent.UpdateOtherLoginOp(
                            OtherLoginOperation.OnLogin(server)
                        )
                    )
                },
                onAddAuthServer = {
                    actions.onIntent(AccountManageIntent.UpdateServerOp(ServerOperation.AddNew))
                },
                onDeleteAuthServer = { server ->
                    actions.onIntent(
                        AccountManageIntent.UpdateServerOp(
                            ServerOperation.Delete(server)
                        )
                    )
                }
            )
        }
    }
}

/**
 * 微软登录相关逻辑处理
 */
@Composable
private fun MicrosoftLoginOperation(
    operation: MicrosoftLoginOperation,
    actions: AccountActions
) {
    when (operation) {
        is MicrosoftLoginOperation.None -> {}
        is MicrosoftLoginOperation.Tip -> {
            MicrosoftLoginTipDialog(
                onDismissRequest = {
                    actions.onIntent(
                        AccountManageIntent.UpdateMicrosoftLoginOp(
                            MicrosoftLoginOperation.None
                        )
                    )
                },
                onConfirm = {
                    actions.onIntent(
                        AccountManageIntent.UpdateMicrosoftLoginOp(
                            MicrosoftLoginOperation.None
                        )
                    )
                    actions.onIntent(
                        AccountManageIntent.PerformMicrosoftLogin(
                            toWeb = actions.navigateToWeb,
                            backToMain = actions.backToMainScreen,
                            checkIfInWebScreen = actions.checkIfInWebScreen
                        )
                    )
                },
                openLink = actions.openLink
            )
        }
    }
}

/**
 * 离线账号登录相关逻辑处理
 */
@Composable
private fun LocalLoginOperation(
    operation: LocalLoginOperation,
    actions: AccountActions
) {
    when (operation) {
        is LocalLoginOperation.None -> {}
        is LocalLoginOperation.Edit -> {
            LocalLoginDialog(
                onDismissRequest = {
                    actions.onIntent(
                        AccountManageIntent.UpdateLocalLoginOp(
                            LocalLoginOperation.None
                        )
                    )
                },
                onConfirm = { name ->
                    actions.onIntent(
                        AccountManageIntent.UpdateLocalLoginOp(
                            LocalLoginOperation.Create(name)
                        )
                    )
                }
            )
        }

        is LocalLoginOperation.Create -> {
            LaunchedEffect(operation) {
                actions.onIntent(
                    AccountManageIntent.CreateLocalAccount(operation.userName)
                )
            }
        }
    }
}

/**
 * 第三方验证服务器登录逻辑处理
 */
@Composable
private fun OtherLoginOperation(
    operation: OtherLoginOperation,
    actions: AccountActions
) {
    when (operation) {
        is OtherLoginOperation.None -> {}
        is OtherLoginOperation.OnLogin -> {
            OtherServerLoginDialog(
                server = operation.server,
                onRegisterClick = { url ->
                    actions.openLink(url)
                    actions.onIntent(AccountManageIntent.UpdateOtherLoginOp(OtherLoginOperation.None))
                },
                onDismissRequest = {
                    actions.onIntent(
                        AccountManageIntent.UpdateOtherLoginOp(
                            OtherLoginOperation.None
                        )
                    )
                },
                onConfirm = { email, password ->
                    actions.onIntent(AccountManageIntent.UpdateOtherLoginOp(OtherLoginOperation.None))
                    actions.onIntent(
                        AccountManageIntent.LoginWithOtherServer(
                            operation.server,
                            email,
                            password
                        )
                    )
                }
            )
        }

        is OtherLoginOperation.OnFailed -> {
            LaunchedEffect(operation) {
                actions.submitError(
                    ErrorViewModel.ThrowableMessage(
                        title = androidText(R.string.account_logging_in_failed),
                        message = actions.formatError(operation.th)
                    )
                )
                actions.onIntent(AccountManageIntent.UpdateOtherLoginOp(OtherLoginOperation.None))
            }
        }

        is OtherLoginOperation.SelectRole -> {
            SimpleListDialog(
                title = stringResource(R.string.account_other_login_select_role),
                items = operation.profiles,
                onItemSelected = { operation.selected(it) },
                onDismissRequest = {
                    actions.onIntent(
                        AccountManageIntent.UpdateOtherLoginOp(
                            OtherLoginOperation.None
                        )
                    )
                },
                itemLayout = { item, isCurrent, onClick ->
                    SimpleListItem(
                        selected = isCurrent,
                        itemName = item.name,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onClick
                    )
                }
            )
        }
    }
}

/**
 * 验证服务器管理操作逻辑处理
 */
@Composable
private fun ServerTypeOperation(
    operation: ServerOperation,
    actions: AccountActions
) {
    when (operation) {
        is ServerOperation.AddNew -> {
            var serverUrl by rememberSaveable { mutableStateOf("") }
            SimpleEditDialog(
                title = stringResource(R.string.account_add_new_server),
                value = serverUrl,
                onValueChange = { serverUrl = it.trim() },
                label = { Text(text = stringResource(R.string.account_label_server_url)) },
                singleLine = true,
                onDismissRequest = {
                    actions.onIntent(
                        AccountManageIntent.UpdateServerOp(
                            ServerOperation.None
                        )
                    )
                },
                onConfirm = {
                    if (serverUrl.isNotEmpty()) {
                        actions.onIntent(AccountManageIntent.AddServer(serverUrl))
                    }
                }
            )
        }

        is ServerOperation.Delete -> {
            SimpleAlertDialog(
                title = stringResource(R.string.account_other_login_delete_server_title),
                text = stringResource(
                    R.string.account_other_login_delete_server_message,
                    operation.server.serverName
                ),
                onDismiss = { actions.onIntent(AccountManageIntent.UpdateServerOp(ServerOperation.None)) },
                onConfirm = { actions.onIntent(AccountManageIntent.DeleteServer(operation.server)) }
            )
        }

        is ServerOperation.OnThrowable -> {
            LaunchedEffect(operation) {
                actions.submitError(
                    ErrorViewModel.ThrowableMessage(
                        title = androidText(R.string.account_other_login_adding_failure),
                        message = androidText(operation.throwable.getMessageOrToString())
                    )
                )
                actions.onIntent(AccountManageIntent.UpdateServerOp(ServerOperation.None))
            }
        }

        is ServerOperation.None -> {}
    }
}

/**
 * 账号列表组件
 */
@Composable
private fun AccountsLayout(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    accounts: List<Account>,
    currentAccount: Account?,
    isOffline: Boolean,
    accountOperation: AccountOperation,
    accountSkinOperation: AccountSkinOperation,
    accountSkinDialogState: AccountManageViewModel.AccountSkinDialogState,
    accountCapes: Map<String, List<PlayerProfile.Cape>>,
    actions: AccountActions
) {
    val yOffset by swapAnimateDpAsState(targetValue = (-40).dp, swapIn = isVisible)
    val context = LocalContext.current

    AccountOperation(accountOperation, actions)

    AccountSkinOperation(
        accountSkinOperation = accountSkinOperation,
        skinDialogState = accountSkinDialogState,
        accountCapes = accountCapes,
        actions = actions
    )

    Column(
        modifier = modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Unified Single-Row Header + Quick Add Pills (Mockup #6)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Accounts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1BD96A),
                    onClick = {
                        if (!isMicrosoftLogging()) {
                            actions.onIntent(
                                AccountManageIntent.UpdateMicrosoftLoginOp(
                                    MicrosoftLoginOperation.Tip
                                )
                            )
                        }
                    }
                ) {
                    Text(
                        text = "+ Microsoft",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF06210F)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF21242B),
                    border = BorderStroke(1.dp, Color(0xFF2E333E)),
                    onClick = {
                        actions.onIntent(AccountManageIntent.UpdateLocalLoginOp(LocalLoginOperation.Edit))
                    }
                ) {
                    Text(
                        text = "+ Offline",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF21242B),
                    border = BorderStroke(1.dp, Color(0xFF2E333E)),
                    onClick = {
                        actions.onIntent(AccountManageIntent.UpdateLoginMenuOp(LoginMenuOperation.Login))
                    }
                ) {
                    Text(
                        text = "+ Auth Server",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

            if (accounts.isNotEmpty()) {
                val scrollState = rememberLazyListState()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .nonInteractiveScrollbar(
                            state = scrollState.scrollIndicatorState!!,
                            orientation = Orientation.Vertical,
                        ),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    state = scrollState,
                ) {
                    items(accounts, key = { it.uniqueUUID }) { account ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            AccountItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                currentAccount = currentAccount,
                                account = account,
                                enabled = !isOffline,
                                onSelected = { AccountsManager.setCurrentAccount(it) },
                                openChangeSkinDialog = {
                                    if (!account.isAuthServerAccount()) {
                                        actions.onIntent(
                                            AccountManageIntent.UpdateAccountSkinOp(
                                                AccountSkinOperation.ChangeSkin(account)
                                            )
                                        )
                                    }
                                },
                                onRefreshClick = {
                                    actions.onIntent(
                                        AccountManageIntent.RefreshAccount(
                                            account
                                        )
                                    )
                                },
                                onCopyUUID = {
                                    copyText(COPY_LABEL_ACCOUNT_UUID, account.profileId, context, true)
                                },
                                onDeleteClick = {
                                    actions.onIntent(
                                        AccountManageIntent.UpdateAccountOp(
                                            AccountOperation.Delete(account)
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ScalingLabel(
                        text = stringResource(R.string.account_no_account)
                    )
                }
            }
    }
}

@Composable
private fun LocalAccountWardrobeActions(
    account: Account,
    onIntent: (AccountManageIntent) -> Unit
) {
    val skinPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { selectedUri ->
            onIntent(
                AccountManageIntent.UpdateAccountSkinOp(
                    AccountSkinOperation.ChangeSkin(account)
                )
            )
            onIntent(AccountManageIntent.OnSkinPicked(account, selectedUri))
        }
    }
    val capePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { selectedUri ->
            onIntent(
                AccountManageIntent.UpdateAccountSkinOp(
                    AccountSkinOperation.ChangeSkin(account)
                )
            )
            onIntent(AccountManageIntent.OnCapePicked(account, selectedUri))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { skinPicker.launch(arrayOf("image/png")) }
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.drawable.ic_checkroom),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.account_local_add_skin))
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { capePicker.launch(arrayOf("image/png")) }
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.drawable.ic_upload),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.account_local_add_cape))
        }
    }
}

/**
 * 账号皮肤操作逻辑处理
 */
@Composable
private fun AccountSkinOperation(
    accountSkinOperation: AccountSkinOperation,
    skinDialogState: AccountManageViewModel.AccountSkinDialogState,
    accountCapes: Map<String, List<PlayerProfile.Cape>>,
    actions: AccountActions
) {
    when (accountSkinOperation) {
        is AccountSkinOperation.None -> {}
        is AccountSkinOperation.ChangeSkin -> {
            val account = accountSkinOperation.account
            ChangeSkinDialog(
                account = account,
                availableCapes = accountCapes[account.uniqueUUID] ?: emptyList(),
                skinState = skinDialogState.pendingSkinData,
                onSkinStateChange = { skinState ->
                    actions.onIntent(
                        AccountManageIntent.UpdatePendingSkinData(
                            skinState
                        )
                    )
                },
                capeState = skinDialogState.pendingCapeData,
                onCapeStateChange = { capeState ->
                    actions.onIntent(
                        AccountManageIntent.UpdatePendingCapeData(
                            capeState
                        )
                    )
                },
                isImportingSkin = skinDialogState.importingSkin,
                isImportingCape = skinDialogState.importingCape,
                onSkinPicked = { uri ->
                    actions.onIntent(
                        AccountManageIntent.OnSkinPicked(account, uri)
                    )
                },
                onCapePicked = { uri ->
                    actions.onIntent(
                        AccountManageIntent.OnCapePicked(account, uri)
                    )
                },
                onDismissRequest = {
                    actions.onIntent(AccountManageIntent.ResetAccountSkinDialogState)
                    actions.onIntent(AccountManageIntent.UpdateAccountSkinOp(AccountSkinOperation.None))
                },
                onResetSkin = {
                    actions.onIntent(AccountManageIntent.ResetSkin(account))
                },
                onFetchCapes = {
                    actions.onIntent(AccountManageIntent.FetchMicrosoftCapes(account))
                },
                onApplySkin = { file, model ->
                    actions.onIntent(AccountManageIntent.ApplySkin(account, file, model))
                },
                onApplyCape = { cape ->
                    actions.onIntent(AccountManageIntent.ApplyMicrosoftCape(account, cape))
                },
                onApplyLocalCape = { file ->
                    actions.onIntent(AccountManageIntent.ApplyLocalCape(account, file))
                }
            )
        }
    }
}

/**
 * 通用账号管理操作逻辑处理（如删除确认）
 */
@Composable
private fun AccountOperation(
    operation: AccountOperation,
    actions: AccountActions
) {
    when (operation) {
        is AccountOperation.Delete -> {
            SimpleAlertDialog(
                title = stringResource(R.string.account_delete_title),
                text = stringResource(R.string.account_delete_message, operation.account.username),
                onConfirm = { actions.onIntent(AccountManageIntent.DeleteAccount(operation.account)) },
                onDismiss = { actions.onIntent(AccountManageIntent.UpdateAccountOp(AccountOperation.None)) }
            )
        }

        is AccountOperation.OnFailed -> {
            LaunchedEffect(operation) {
                actions.submitError(
                    ErrorViewModel.ThrowableMessage(
                        title = androidText(R.string.account_logging_in_failed),
                        message = actions.formatError(operation.th)
                    )
                )
                actions.onIntent(AccountManageIntent.UpdateAccountOp(AccountOperation.None))
            }
        }

        is AccountOperation.OnRelogin -> {
            if (operation.account.isMicrosoftAccount()) {
                MicrosoftReloginDialog(
                    onDismissRequest = {
                        actions.onIntent(AccountManageIntent.UpdateAccountOp(AccountOperation.None))
                    },
                    onConfirm = {
                        actions.onIntent(AccountManageIntent.UpdateAccountOp(AccountOperation.None))
                        actions.onIntent(
                            AccountManageIntent.PerformMicrosoftLogin(
                                toWeb = actions.navigateToWeb,
                                backToMain = actions.backToMainScreen,
                                checkIfInWebScreen = actions.checkIfInWebScreen
                            )
                        )
                    }
                )
            } else {
                OtherAccountReloginDialog(
                    account = operation.account,
                    logging = operation.logging,
                    error = operation.error,
                    onDismissRequest = {
                        actions.onIntent(AccountManageIntent.UpdateAccountOp(AccountOperation.None))
                    },
                    onConfirm = { password ->
                        actions.onIntent(
                            AccountManageIntent.UpdateAccountOp(
                                AccountOperation.OnRelogin(operation.account, logging = true)
                            )
                        )
                        actions.onIntent(
                            AccountManageIntent.ReloginOtherAccount(
                                account = operation.account,
                                password = password
                            )
                        )
                    }
                )
            }
        }

        is AccountOperation.None -> {}
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 480)
@Composable
private fun AccountManageContentPreview() {
    CompositionLocalProvider(LocalBackgroundViewModel provides null) {
        MaterialExpressiveTheme {
            Surface {
                AccountManageContent(
                    isVisible = true,
                    loginUiState = AccountManageViewModel.LoginUiState(),
                    profileUiState = AccountManageViewModel.ProfileUiState(),
                    operationUiState = AccountManageViewModel.OperationUiState(),
                    actions = AccountActions(
                        onIntent = {},
                        openLink = {},
                        backToMainScreen = {},
                        navigateToWeb = {},
                        checkIfInWebScreen = { false },
                        formatError = { AndroidStringText.Text("") },
                        submitError = {},
                    )
                )
            }
        }
    }
}
