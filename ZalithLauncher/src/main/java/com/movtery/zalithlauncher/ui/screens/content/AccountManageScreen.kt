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

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.movtery.zalithlauncher.ui.components.lazyScrollWithBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.COPY_LABEL_ACCOUNT_UUID
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.auth_server.ELY_BY_AUTH_SERVER_URL
import com.movtery.zalithlauncher.game.account.auth_server.data.AuthServer
import com.movtery.zalithlauncher.game.account.isAuthServerAccount
import com.movtery.zalithlauncher.game.account.isElyByAccount
import com.movtery.zalithlauncher.game.account.isLocalAccount
import com.movtery.zalithlauncher.game.account.isMicrosoftAccount
import com.movtery.zalithlauncher.game.account.isMicrosoftLogging
import com.movtery.zalithlauncher.game.account.yggdrasil.PlayerProfile
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.components.ModelAnimation
import com.movtery.zalithlauncher.ui.components.PlayerSkin
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleListDialog
import com.movtery.zalithlauncher.ui.components.SimpleListItem
import com.movtery.zalithlauncher.ui.components.SkinPreview3D
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.navigateTo
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
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace
import com.movtery.zalithlauncher.ui.screens.content.elements.ServerOperation
import com.movtery.zalithlauncher.game.account.isMicrosoftAccount
import com.movtery.zalithlauncher.utils.PlayTimeUtils
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.checkStoragePermissions
import com.movtery.zalithlauncher.utils.copyText
import com.movtery.zalithlauncher.utils.settings.SettingsTransferUtils
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.AccountManageEffect
import com.movtery.zalithlauncher.viewmodel.AccountManageIntent
import com.movtery.zalithlauncher.viewmodel.AccountManageViewModel
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.LocalBackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

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
    val navigateToCapeGallery: (accountUUID: String) -> Unit,
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
            navigateToCapeGallery = { uuid ->
                backStackViewModel.mainScreen.backStack.navigateTo(
                    NormalNavKey.CapeGallery(uuid)
                )
            },
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
 * 账号管理界面的实际内容布局 - iki panel
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AccountManageContent(
    isVisible: Boolean,
    loginUiState: AccountManageViewModel.LoginUiState,
    profileUiState: AccountManageViewModel.ProfileUiState,
    operationUiState: AccountManageViewModel.OperationUiState,
    actions: AccountActions,
) {
    val refreshWardrobe by AccountsManager.refreshWardrobe.collectAsStateWithLifecycle()
    val currentAccount = profileUiState.currentAccount
    val context = LocalContext.current


    val accountSkin = remember(currentAccount, refreshWardrobe) {
        currentAccount?.getSkinFile()?.takeIf { it.exists() }
    }
    val accountCape = remember(currentAccount, refreshWardrobe) {
        currentAccount?.getCapeFile()?.takeIf { it.exists() }
    }
    val playerSkin = remember { PlayerSkin(context) }
    var pageFinished by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { playerSkin.destroy() }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Left panel: skin preview + add account ──
        Column(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BackgroundCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // 3D skin preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                playerSkin.loadWebView(
                                    context = ctx,
                                    onPageFinished = {
                                        pageFinished = true
                                        playerSkin.startAnim(ModelAnimation.NewIdle)
                                        playerSkin.setAzimuthAndPitch(-35, 10)
                                    }
                                )
                            },
                            update = {
                                if (pageFinished) {
                                    runCatching {
                                        accountSkin?.inputStream().use { inputStream ->
                                            playerSkin.loadSkin(inputStream, currentAccount?.skinModelType)
                                        }
                                    }
                                    runCatching {
                                        accountCape?.inputStream().use { inputStream ->
                                            playerSkin.loadCape(inputStream)
                                        }
                                    }
                                }
                            }
                        )
                        if (!pageFinished) {
                            LoadingIndicator()
                        }
                    }

                    HorizontalDivider(modifier = Modifier.alpha(0.2f))

                    // Account info + chroma
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (currentAccount != null) {
                            Text(
                                text = currentAccount.username,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = getAccountTypeName(context, currentAccount),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.alpha(0.7f)
                            )
                            val totalMs = AllSettings.playTime.getValue()
                            val rank = PlayTimeUtils.getRankName(context, totalMs)
                            Text(
                                text = rank,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.account_no_account),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.alpha(0.6f)
                            )
                        }
                    }

                }
            }

            // Add account button at bottom of left panel
            ScalingActionButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    actions.onIntent(AccountManageIntent.UpdateLoginMenuOp(LoginMenuOperation.Login))
                }
            ) {
                MarqueeText(text = stringResource(R.string.account_add_new_account))
            }

            // Import/Export buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val scope = rememberCoroutineScope()
                val importLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri ->
                    uri?.let {
                        scope.launch {
                            val success = SettingsTransferUtils.importData(context, it)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    if (success) R.string.settings_import_success else R.string.settings_import_failed,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val activity = context as? android.app.Activity ?: return@FilledTonalButton
                        checkStoragePermissions(
                            activity = activity,
                            title = R.string.storage_permission_request_title,
                            message = context.getString(R.string.storage_permission_request_message),
                            hasPermission = {
                                scope.launch {
                                    val file = SettingsTransferUtils.exportAccounts(context)
                                    withContext(Dispatchers.Main) {
                                        if (file != null) {
                                            Toast.makeText(context, context.getString(R.string.settings_export_success, file.absolutePath), Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, R.string.settings_export_failed, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        )
                    }
                ) {
                    Icon(painter = painterResource(R.drawable.ic_share_filled), contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text(stringResource(R.string.settings_export_accounts), style = MaterialTheme.typography.labelMedium)
                }
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = { importLauncher.launch("application/json") }
                ) {
                    Icon(painter = painterResource(R.drawable.ic_upload), contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text(stringResource(R.string.settings_import_accounts), style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // ── Right panel: account cards with drag-and-drop ──
        BackgroundCard(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxHeight(),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            val accounts = profileUiState.accounts
            if (accounts.isNotEmpty()) {
                val scrollState = rememberLazyListState()
                val reorderableState = rememberReorderableLazyListState(
                    lazyListState = scrollState,
                    onMove = { from, to ->
                        actions.onIntent(AccountManageIntent.ReorderAccount(from.index, to.index))
                    }
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .lazyScrollWithBar(state = scrollState),
                    state = scrollState,
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(accounts, key = { it.uniqueUUID }) { account ->
                        ReorderableItem(
                            state = reorderableState,
                            key = account.uniqueUUID
                        ) { isDragging ->
                            val elevation by animateDpAsState(
                                targetValue = if (isDragging) 6.dp else 0.dp,
                                animationSpec = spring(),
                                label = "cardElevation"
                            )
                            val dragHandleModifier = Modifier.draggableHandle()
                            AccountCard(
                                modifier = Modifier.fillMaxWidth(),
                                account = account,
                                currentAccount = currentAccount,
                                elevation = elevation,
                                dragHandleModifier = dragHandleModifier,
                                onSelected = { AccountsManager.setCurrentAccount(account) },
                                openChangeSkinDialog = {
                                    if (!account.isAuthServerAccount() || account.isElyByAccount()) {
                                        actions.onIntent(
                                            AccountManageIntent.UpdateAccountSkinOp(
                                                AccountSkinOperation.ChangeSkin(account)
                                            )
                                        )
                                    }
                                },
                                onRefreshClick = {
                                    actions.onIntent(AccountManageIntent.RefreshAccount(account))
                                },
                                onCopyUUID = {
                                    copyText(COPY_LABEL_ACCOUNT_UUID, account.profileId, context, false)
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.account_local_uuid_copied, account.username),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onDeleteClick = {
                                    actions.onIntent(
                                        AccountManageIntent.UpdateAccountOp(AccountOperation.Delete(account))
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    ScalingLabel(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(R.string.account_no_account)
                    )
                }
            }
        }
    }

    AccountOperation(operationUiState.accountOp, actions)
    LoginMenuOperation(loginUiState.menuOp, actions, profileUiState.authServers)
    MicrosoftLoginOperation(loginUiState.microsoftOp, actions)
    LocalLoginOperation(loginUiState.localOp, actions)
    OtherLoginOperation(loginUiState.otherOp, actions)
    ServerTypeOperation(operationUiState.serverOp, actions)
    AccountSkinOperation(
        accountSkinOperation = operationUiState.accountSkinOp,
        skinDialogState = operationUiState.accountSkinDialogState,
        accountCapes = profileUiState.accountCapeOpMap,
        actions = actions,
    )
}

/**
 * 左侧登录方式菜单组件
 */
@Composable
private fun AccountCard(
    modifier: Modifier = Modifier,
    account: Account,
    currentAccount: Account?,
    elevation: Dp = 0.dp,
    dragHandleModifier: Modifier = Modifier,
    onSelected: () -> Unit,
    openChangeSkinDialog: () -> Unit,
    onRefreshClick: () -> Unit,
    onCopyUUID: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isSelected = currentAccount?.uniqueUUID == account.uniqueUUID
    val context = LocalContext.current

    BackgroundCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        onClick = onSelected,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Drag handle + profile head
                Box(
                    modifier = dragHandleModifier,
                    contentAlignment = Alignment.Center
                ) {
                    PlayerFace(
                        modifier = Modifier.size(44.dp),
                        account = account,
                        avatarSize = 44.dp
                    )
                }

                // Account info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = account.username,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isSelected) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = getAccountTypeName(context, account),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.alpha(0.6f)
                    )
                }

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (!account.isAuthServerAccount() || account.isElyByAccount()) {
                        IconButton(
                            modifier = Modifier.size(36.dp),
                            onClick = openChangeSkinDialog
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_checkroom),
                                contentDescription = stringResource(R.string.account_change_skin),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    if (!account.isLocalAccount()) {
                        IconButton(
                            modifier = Modifier.size(36.dp),
                            onClick = onRefreshClick
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_refresh),
                                contentDescription = stringResource(R.string.generic_refresh),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    IconButton(
                        modifier = Modifier.size(36.dp),
                        onClick = onCopyUUID
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_copy_all_outlined),
                            contentDescription = stringResource(R.string.account_local_uuid_copy),
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(
                        modifier = Modifier.size(36.dp),
                        onClick = onDeleteClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete_outlined),
                            contentDescription = stringResource(R.string.generic_delete),
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }

        }
    }
}

@Composable
private fun getAccountTypeName(context: Context, account: Account): String {
    return com.movtery.zalithlauncher.game.account.getAccountTypeName(account)
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
                onConfirm = { isInvalid, name, uuid ->
                    val nextOp = if (isInvalid) LocalLoginOperation.Alert(
                        name,
                        uuid
                    ) else LocalLoginOperation.Create(name, uuid)
                    actions.onIntent(AccountManageIntent.UpdateLocalLoginOp(nextOp))
                },
                openLink = actions.openLink
            )
        }

        is LocalLoginOperation.Create -> {
            LaunchedEffect(operation) {
                actions.onIntent(
                    AccountManageIntent.CreateLocalAccount(
                        operation.userName,
                        operation.userUUID
                    )
                )
            }
        }

        is LocalLoginOperation.Alert -> {
            SimpleAlertDialog(
                title = stringResource(R.string.account_supporting_username_invalid_title),
                text = {
                    Text(text = stringResource(R.string.account_supporting_username_invalid_local_message_hint1))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.account_supporting_username_invalid_local_message_hint2),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = stringResource(R.string.account_supporting_username_invalid_local_message_hint3))
                    Text(text = stringResource(R.string.account_supporting_username_invalid_local_message_hint4))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.account_supporting_username_invalid_local_message_hint5),
                        fontWeight = FontWeight.Bold
                    )
                },
                confirmText = stringResource(R.string.account_supporting_username_invalid_still_use),
                onConfirm = {
                    actions.onIntent(
                        AccountManageIntent.UpdateLocalLoginOp(
                            LocalLoginOperation.Create(operation.userName, operation.userUUID)
                        )
                    )
                },
                onCancel = {
                    actions.onIntent(
                        AccountManageIntent.UpdateLocalLoginOp(
                            LocalLoginOperation.None
                        )
                    )
                }
            )
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
                extraBody = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        //快速填入Ely.by的authlib-injector地址，方便用户一键添加Ely.by验证服务器
                        AssistChip(
                            onClick = { serverUrl = ELY_BY_AUTH_SERVER_URL },
                            label = { Text(text = stringResource(R.string.account_add_server_quick_ely_by)) },
                            colors = AssistChipDefaults.assistChipColors()
                        )
                    }
                },
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
                        AccountManageIntent.OnSkinPicked(uri)
                    )
                },
                onCapePicked = { account, uri ->
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
                onResetCape = {
                    actions.onIntent(AccountManageIntent.ResetCape(account))
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
                onApplyCustomCape = { file ->
                    actions.onIntent(AccountManageIntent.ApplyCustomCape(account, file))
                },

                onInstallCapes = {
                    actions.navigateToCapeGallery(account.uniqueUUID)
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
                        navigateToCapeGallery = {},
                        checkIfInWebScreen = { false },
                        formatError = { AndroidStringText.Text("") },
                        submitError = {},
                    )
                )
            }
        }
    }
}
