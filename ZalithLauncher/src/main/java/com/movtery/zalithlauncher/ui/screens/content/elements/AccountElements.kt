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

package com.movtery.zalithlauncher.ui.screens.content.elements

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountType
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.accountUUID
import com.movtery.zalithlauncher.game.account.auth_server.data.AuthServer
import com.movtery.zalithlauncher.game.account.auth_server.models.AuthResult
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.game.account.getUUIDFromUserName
import com.movtery.zalithlauncher.game.account.isLocalAccount
import com.movtery.zalithlauncher.game.account.isMicrosoftAccount
import com.movtery.zalithlauncher.game.account.isSkinChangeAllowed
import com.movtery.zalithlauncher.game.account.wardrobe.EmptyCape
import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
import com.movtery.zalithlauncher.game.account.wardrobe.capeLocalRes
import com.movtery.zalithlauncher.game.account.yggdrasil.PlayerProfile
import com.movtery.zalithlauncher.game.account.yggdrasil.findUsing
import com.movtery.zalithlauncher.game.account.yggdrasil.getFile
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.path.URL_MINECRAFT_PURCHASE
import com.movtery.zalithlauncher.ui.components.BaseIconTextButton
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ModelAnimation
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.PlayerSkin
import com.movtery.zalithlauncher.ui.components.RadioCard
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleListDialog
import com.movtery.zalithlauncher.ui.components.SingleLineTextCheck
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutTextItem
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.regex.Pattern
import kotlin.math.roundToInt

private const val TAG = "AccountElements"

/** 账号登录菜单操作状态 */
sealed interface LoginMenuOperation {
    data object None : LoginMenuOperation

    /** 呼出登陆账号菜单，将所有登录方式放到一个对话框中展示 */
    data object Login : LoginMenuOperation
}

/**
 * 微软登录的操作状态
 */
sealed interface MicrosoftLoginOperation {
    data object None : MicrosoftLoginOperation

    /** 微软账号相关提示Dialog流程 */
    data object Tip : MicrosoftLoginOperation
}

/**
 * 离线登陆的操作状态
 */
sealed interface LocalLoginOperation {
    data object None : LocalLoginOperation

    /** 编辑用户名流程 */
    data object Edit : LocalLoginOperation

    /** 创建账号流程 */
    data class Create(val userName: String, val userUUID: String?) : LocalLoginOperation

    /** 警告非法用户名流程 */
    data class Alert(val userName: String, val userUUID: String?) : LocalLoginOperation
}

/**
 * 添加认证服务器时的状态
 */
sealed interface ServerOperation {
    data object None : ServerOperation
    /** 添加认证服务器对话框 */
    data object AddNew : ServerOperation
    /** 删除认证服务器对话框 */
    data class Delete(val server: AuthServer) : ServerOperation
    data class OnThrowable(val throwable: Throwable) : ServerOperation
}

/**
 * 账号操作的状态
 */
sealed interface AccountOperation {
    data object None : AccountOperation
    data class Delete(val account: Account) : AccountOperation
    data class OnFailed(val th: Throwable) : AccountOperation
}

/**
 * 更换账号皮肤的状态
 */
sealed interface AccountSkinOperation {
    data object None : AccountSkinOperation

    /** 修改皮肤主对话框 */
    data class ChangeSkin(val account: Account) : AccountSkinOperation
}

/**
 * 认证服务器登陆时的状态
 */
sealed interface OtherLoginOperation {
    data object None : OtherLoginOperation

    /** 账号登陆（输入账号密码Dialog）流程 */
    data class OnLogin(val server: AuthServer) : OtherLoginOperation

    /** 登陆失败流程 */
    data class OnFailed(val th: Throwable) : OtherLoginOperation

    /** 账号存在多角色的情况，多角色处理流程 */
    data class SelectRole(
        val profiles: List<AuthResult.AvailableProfiles>,
        val selected: (AuthResult.AvailableProfiles) -> Unit
    ) : OtherLoginOperation
}

@Composable
fun AccountAvatar(
    modifier: Modifier = Modifier,
    account: Account?,
    avatarSize: Dp = 64.dp,
    refreshKey: Any? = null,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(all = 12.dp)
        ) {
            if (account != null) {
                PlayerFace(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    account = account,
                    avatarSize = avatarSize,
                    refreshKey = refreshKey
                )
            } else {
                Icon(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterHorizontally),
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = account?.username ?: stringResource(R.string.account_add_new_account),
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall
            )
            if (account != null) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = getAccountTypeName(account),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun PlayerFace(
    modifier: Modifier = Modifier,
    account: Account,
    avatarSize: Dp = 64.dp,
    refreshKey: Any? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val refreshWardrobe by AccountsManager.refreshWardrobe.collectAsStateWithLifecycle()
    val avatarBitmap = remember(account, refreshKey, refreshWardrobe, density) {
        getSkinAvatarFromAccount(context, account, avatarSize, density).asImageBitmap()
    }

    Image(
        modifier = modifier.size(avatarSize),
        bitmap = avatarBitmap,
        contentDescription = null
    )
}

@Composable
fun AccountItem(
    modifier: Modifier = Modifier,
    currentAccount: Account?,
    account: Account,
    avatarSize: Dp = 46.dp,
    color: Color = itemColor(),
    contentColor: Color = onItemColor(),
    enabled: Boolean = true,
    refreshKey: Any? = null,
    onSelected: (Account) -> Unit = {},
    openChangeSkinDialog: () -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onCopyUUID: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val selected = currentAccount?.uniqueUUID == account.uniqueUUID
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }
    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        onClick = {
            if (selected || !enabled) return@Surface
            onSelected(account)
        },
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = MaterialTheme.shapes.large)
                .padding(all = 8.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = {
                    if (selected || !enabled) return@RadioButton
                    onSelected(account)
                },
                enabled = enabled
            )
            PlayerFace(
                modifier = Modifier.align(Alignment.CenterVertically),
                account = account,
                avatarSize = avatarSize,
                refreshKey = refreshKey
            )
            Spacer(modifier = Modifier.width(18.dp))
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            ) {
                Text(text = account.username)
                Text(
                    text = getAccountTypeName(account),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Row {
                //更换皮肤/披风
                Row {
                    IconButton(
                        onClick = { openChangeSkinDialog() },
                        enabled = account.isSkinChangeAllowed()
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(R.drawable.ic_checkroom),
                            contentDescription = stringResource(R.string.account_change_skin)
                        )
                    }
                }

                //刷新
                IconButton(
                    onClick = onRefreshClick,
                    enabled = account.accountType != AccountType.LOCAL.tag
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_refresh),
                        contentDescription = stringResource(R.string.generic_refresh)
                    )
                }

                //复制 UUID
                IconButton(
                    onClick = onCopyUUID
                ) {
                    Icon(
                        modifier = Modifier.size(22.dp),
                        painter = painterResource(R.drawable.ic_copy_all_outlined),
                        contentDescription = stringResource(R.string.account_local_uuid_copy)
                    )
                }

                //删除
                IconButton(
                    onClick = onDeleteClick
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_delete_outlined),
                        contentDescription = stringResource(R.string.generic_delete)
                    )
                }
            }
        }
    }
}

@Composable
fun LoginMenuDialog(
    onDismissRequest: () -> Unit,
    onMicrosoftLogin: () -> Unit,
    onLocalLogin: () -> Unit,
    authServers: List<AuthServer>,
    onAuthServerLogin: (server: AuthServer) -> Unit,
    onAddAuthServer: () -> Unit,
    onDeleteAuthServer: (server: AuthServer) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .padding(all = 16.dp)
                .fillMaxHeight()
                .fillMaxWidth(0.6f),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 6.dp)
                    .fillMaxWidth()
                    .heightIn(max = maxHeight - 12.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = cardColor(false),
                contentColor = onCardColor(),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 12.dp)
                                .padding(start = 12.dp, end = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            //微软登录
                            LoginItem(
                                modifier = Modifier.fillMaxWidth(),
                                title = stringResource(R.string.account_type_microsoft),
                                onClick = {
                                    onMicrosoftLogin()
                                    onDismissRequest()
                                }
                            )
                            //离线登录
                            LoginItem(
                                modifier = Modifier.fillMaxWidth(),
                                title = stringResource(R.string.account_type_local),
                                onClick = {
                                    onLocalLogin()
                                    onDismissRequest()
                                }
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(
                                start = 6.dp,
                                top = 12.dp,
                                end = 12.dp,
                                bottom = 12.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                //添加认证服务器
                                InfoLayoutTextItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = stringResource(R.string.account_add_new_server_button),
                                    showArrow = true,
                                    onClick = {
                                        onAddAuthServer()
                                        onDismissRequest()
                                    }
                                )
                            }

                            items(authServers) { server ->
                                LoginItem(
                                    title = server.serverName,
                                    icon = {
                                        IconButton(
                                            modifier = Modifier.size(22.dp),
                                            onClick = {
                                                onDeleteAuthServer(server)
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_delete_outlined),
                                                contentDescription = stringResource(R.string.generic_delete)
                                            )
                                        }
                                    },
                                    onClick = {
                                        onAuthServerLogin(server)
                                        onDismissRequest()
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 8.dp),
                        onClick = onDismissRequest
                    ) {
                        Text(stringResource(R.string.generic_close))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 480)
@Composable
private fun PreviewLoginMenuDialog() {
    MaterialExpressiveTheme {
        LoginMenuDialog(
            onDismissRequest = {},
            onMicrosoftLogin = {},
            onLocalLogin = {},
            authServers = emptyList(),
            onAuthServerLogin = {},
            onAddAuthServer = {},
            onDeleteAuthServer = {}
        )
    }
}

@Composable
fun LoginItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: @Composable () -> Unit = @Composable {
        Icon(
            modifier = Modifier.size(22.dp),
            painter = painterResource(R.drawable.ic_login),
            contentDescription = null
        )
    },
    onClick: () -> Unit
) {
    InfoLayoutTextItem(
        modifier = modifier,
        title = title,
        icon = icon,
        onClick = onClick
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 120)
@Composable
private fun PreviewLoginItem() {
    MaterialExpressiveTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LoginItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 32.dp),
                title = stringResource(R.string.account_type_microsoft),
                onClick = {}
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MicrosoftLoginTipDialog(
    onDismissRequest: () -> Unit = {},
    onConfirm: () -> Unit = {},
    openLink: (url: String) -> Unit = {}
) {
    SimpleAlertDialog(
        title = stringResource(R.string.account_supporting_microsoft_tip_title),
        text = {
            Text(
                text = stringResource(R.string.account_supporting_microsoft_tip_link_text),
                style = MaterialTheme.typography.bodyMedium
            )
            FlowRow {
                IconTextButton(
                    onClick = {
                        openLink(URL_MINECRAFT_PURCHASE)
                    },
                    painter = painterResource(R.drawable.ic_link),
                    contentDescription = null,
                    text = stringResource(R.string.account_supporting_microsoft_tip_link_purchase)
                )
                IconTextButton(
                    onClick = {
                        openLink("https://www.minecraft.net/msaprofile/mygames/editprofile")
                    },
                    painter = painterResource(R.drawable.ic_link),
                    contentDescription = null,
                    text = stringResource(R.string.account_supporting_microsoft_tip_link_make_gameid)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.account_supporting_microsoft_tip_hint_t1),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.account_supporting_microsoft_tip_hint_t2))
                    append(
                        stringResource(
                            R.string.account_supporting_microsoft_tip_hint_t3,
                            BuildKeys.LAUNCHER_NAME
                        )
                    )
                    append(stringResource(R.string.account_supporting_microsoft_tip_hint_t4))
                    append(stringResource(R.string.account_supporting_microsoft_tip_hint_t5))
                    append(stringResource(R.string.account_supporting_microsoft_tip_hint_t6))
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.account_supporting_microsoft_tip_hint_t7))
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.account_supporting_microsoft_tip_hint_t8))
                    }
                },
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmText = stringResource(R.string.account_login),
        onConfirm = onConfirm,
        onCancel = onDismissRequest,
        onDismissRequest = onDismissRequest
    )
}

private val localNamePattern = Pattern.compile("[^a-zA-Z0-9_]")

@Composable
fun LocalLoginDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (isUserNameInvalid: Boolean, userName: String, userUUID: String?) -> Unit,
    openLink: (url: String) -> Unit
) {
    /** 用户输入的用户名 */
    var userName by rememberSaveable { mutableStateOf("") }

    /** 用户名是否无效 */
    var isUserNameInvalid by rememberSaveable { mutableStateOf(false) }

    /** 用户编辑了UUID */
    var userEditedUUID by rememberSaveable { mutableStateOf(false) }

    /** 用户输入的UUID */
    var userUUID by rememberSaveable { mutableStateOf("") }

    /** 根据用户名生成的待定UUID */
    val pendingUUID = remember(userName) {
        runCatching {
            getUUIDFromUserName(userName).toString()
        }.getOrElse {
            ""
        }.also { uuid ->
            if (!userEditedUUID) userUUID = uuid
        }
    }

    /** 用户UUID是否无效 */
    val isUserUUIDInvalid: Boolean = remember(userUUID) {
        if (userUUID.isEmpty()) false
        else {
            runCatching {
                accountUUID(userUUID)
                false
            }.getOrElse {
                true
            }
        }
    }

    var editUUID by rememberSaveable { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 6.dp)
                    .heightIn(max = maxHeight - 12.dp)
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                color = cardColor(false),
                contentColor = onCardColor(),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.account_local_create_account),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.size(16.dp))

                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .weight(1f, fill = false)
                            .verticalScroll(state = scrollState)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SingleLineTextCheck(
                            text = userName,
                            onSingleLined = { userName = it }
                        )

                        OwnOutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = userName,
                            onValueChange = {
                                userName = it
                            },
                            isError = isUserNameInvalid,
                            label = { Text(text = stringResource(R.string.account_label_username)) },
                            supportingText = {
                                val errorText = when {
                                    userName.isEmpty() -> stringResource(R.string.account_supporting_username_invalid_empty)
                                    userName.length <= 2 -> stringResource(R.string.account_supporting_username_invalid_short)
                                    userName.length > 16 -> stringResource(R.string.account_supporting_username_invalid_long)
                                    localNamePattern.matcher(userName)
                                        .find() -> stringResource(R.string.account_supporting_username_invalid_illegal_characters)

                                    else -> ""
                                }.also {
                                    isUserNameInvalid = it.isNotEmpty()
                                }
                                if (isUserNameInvalid) {
                                    Text(text = errorText)
                                }
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.large
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconTextButton(
                                onClick = {
                                    openLink(URL_MINECRAFT_PURCHASE)
                                },
                                painter = painterResource(R.drawable.ic_link),
                                contentDescription = null,
                                text = stringResource(R.string.account_supporting_microsoft_tip_link_purchase)
                            )

                            //打开高级设置
                            BaseIconTextButton(
                                onClick = {
                                    editUUID = !editUUID
                                },
                                icon = { iconModifier ->
                                    val rotate by animateFloatAsState(
                                        if (editUUID) 0f
                                        else 180f
                                    )

                                    Icon(
                                        modifier = iconModifier
                                            .size(24.dp)
                                            .rotate(rotate),
                                        painter = painterResource(R.drawable.ic_arrow_drop_up_rounded),
                                        contentDescription = null
                                    )
                                },
                                text = stringResource(R.string.account_advanced)
                            )
                        }

                        //编辑自定义 UUID
                        AnimatedVisibility(
                            visible = editUUID
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(modifier = Modifier.size(8.dp))

                                SingleLineTextCheck(
                                    text = userUUID,
                                    onSingleLined = { userUUID = it }
                                )

                                OwnOutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = userUUID,
                                    onValueChange = {
                                        userUUID = it
                                        userEditedUUID = true
                                    },
                                    isError = isUserUUIDInvalid,
                                    label = { Text(text = stringResource(R.string.account_local_uuid)) },
                                    supportingText = {
                                        if (isUserUUIDInvalid) {
                                            Text(text = stringResource(R.string.account_local_uuid_invalid))
                                        }
                                    },
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.large
                                )

                                //关于 UUID 的提示
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(all = 8.dp),
                                    ) {
                                        Text(
                                            text = stringResource(R.string.account_local_uuid_tip_1),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = stringResource(R.string.account_local_uuid_tip_2),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = stringResource(R.string.account_local_uuid_tip_3),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = stringResource(R.string.account_local_uuid_tip_4),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = onDismissRequest
                        ) {
                            MarqueeText(text = stringResource(R.string.generic_cancel))
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (userName.isNotEmpty()) {
                                    if (userUUID.isNotEmpty()) {
                                        runCatching {
                                            val uuid = accountUUID(userUUID)
                                            val uuidString = accountUUID(uuid)
                                            onConfirm(isUserNameInvalid, userName, uuidString)
                                        }
                                    } else {
                                        //如果未填写UUID，则默认使用待定UUID
                                        onConfirm(
                                            isUserNameInvalid,
                                            userName,
                                            pendingUUID.takeIf { it.isNotEmpty() })
                                    }
                                }
                            }
                        ) {
                            MarqueeText(text = stringResource(R.string.generic_confirm))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OtherServerLoginDialog(
    server: AuthServer,
    onRegisterClick: (url: String) -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onConfirm: (email: String, password: String) -> Unit = { _, _ -> }
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val confirmAction = { //确认操作
        if (email.isNotEmpty() && password.isNotEmpty()) {
            onConfirm(email, password)
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 6.dp)
                    .heightIn(max = maxHeight - 12.dp)
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                color = cardColor(false),
                contentColor = onCardColor(),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = server.serverName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.size(16.dp))

                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .weight(1f, fill = false)
                            .verticalScroll(state = scrollState)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val passwordFocus = remember { FocusRequester() }
                        val focusManager = LocalFocusManager.current

                        SingleLineTextCheck(
                            text = email,
                            onSingleLined = { email = it }
                        )

                        OwnOutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = email,
                            onValueChange = {
                                email = it
                            },
                            isError = email.isEmpty(),
                            label = { Text(text = stringResource(R.string.account_label_email)) },
                            supportingText = {
                                if (email.isEmpty()) {
                                    Text(text = stringResource(R.string.account_supporting_email_invalid_empty))
                                }
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    //自动跳到密码输入框，无缝衔接
                                    passwordFocus.requestFocus()
                                }
                            ),
                            singleLine = true,
                            shape = MaterialTheme.shapes.large
                        )

                        Spacer(modifier = Modifier.size(8.dp))
                        /** 是否显示密码 */
                        var showPassword by rememberSaveable { mutableStateOf(false) }

                        SingleLineTextCheck(
                            text = password,
                            onSingleLined = { password = it }
                        )

                        OwnOutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(passwordFocus),
                            value = password,
                            onValueChange = {
                                password = it
                            },
                            isError = password.isEmpty(),
                            label = { Text(text = stringResource(R.string.account_label_password)) },
                            visualTransformation = if (showPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Transparent,
                            ),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        painter = painterResource(
                                            if (showPassword) {
                                                R.drawable.ic_visibility_outlined
                                            } else {
                                                R.drawable.ic_visibility_off_outlined
                                            }
                                        ),
                                        contentDescription = stringResource(R.string.account_label_password)
                                    )
                                }
                            },
                            supportingText = {
                                if (password.isEmpty()) {
                                    Text(text = stringResource(R.string.account_supporting_password_invalid_empty))
                                }
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Password
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    //用户按下返回，甚至可以在这里直接进行登陆
                                    focusManager.clearFocus(true)
                                    confirmAction()
                                }
                            ),
                            singleLine = true,
                            shape = MaterialTheme.shapes.large
                        )
                        if (!server.register.isNullOrEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                IconTextButton(
                                    onClick = {
                                        onRegisterClick(server.register!!)
                                    },
                                    painter = painterResource(R.drawable.ic_link),
                                    contentDescription = null,
                                    text = stringResource(R.string.account_other_login_register)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = onDismissRequest
                        ) {
                            MarqueeText(text = stringResource(R.string.generic_cancel))
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = confirmAction
                        ) {
                            MarqueeText(text = stringResource(R.string.generic_confirm))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 更改皮肤流程需要让 uri 与皮肤模型深度绑定
 * 重置或者确认更改时，能更方便的处理数据
 */
sealed interface ChangeSkin {
    data object None : ChangeSkin

    data class ChangeSkinData(
        val cacheFile: File,
        val skinModel: SkinModelType = SkinModelType.STEVE
    ) : ChangeSkin

    /**
     * 重置离线皮肤
     */
    data object ResetSkin : ChangeSkin
}

/**
 * 更改披风流程
 */
sealed interface ChangeCape {
    data object None : ChangeCape
    data class ChangeCapeData(
        val cape: PlayerProfile.Cape
    ) : ChangeCape
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ChangeSkinDialog(
    account: Account,
    availableCapes: List<PlayerProfile.Cape> = emptyList(),
    skinState: ChangeSkin,
    onSkinStateChange: (ChangeSkin) -> Unit,
    capeState: ChangeCape,
    onCapeStateChange: (ChangeCape) -> Unit,
    isImportingSkin: Boolean,
    onSkinPicked: (Uri) -> Unit,
    onDismissRequest: () -> Unit,
    onResetSkin: () -> Unit,
    onApplySkin: (File, SkinModelType) -> Unit,
    onApplyCape: (PlayerProfile.Cape) -> Unit,
    onFetchCapes: () -> Unit
) {
    val context = LocalContext.current
    val playerSkin = remember { PlayerSkin(context) }

    DisposableEffect(Unit) {
        onDispose {
            playerSkin.destroy()
        }
    }

    var showCapeSelector by remember { mutableStateOf(false) }

    var isFetchingCapes by remember { mutableStateOf(false) }

    var currentCapeToLoad by remember { mutableStateOf(EmptyCape) }
    var currentUsingCape by remember { mutableStateOf(EmptyCape) }

    LaunchedEffect(availableCapes) {
        if (account.isMicrosoftAccount()) {
            if (availableCapes.isNotEmpty()) {
                isFetchingCapes = false
                val currentUsingCape0 = availableCapes.findUsing() ?: EmptyCape
                currentUsingCape = currentUsingCape0
                currentCapeToLoad = currentUsingCape0
            } else {
                isFetchingCapes = true
                onFetchCapes()
            }
        }
    }

    val skinPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let(onSkinPicked)
        }

    /**
     * 初始化账号设置的皮肤
     */
    fun loadSkin() {
        playerSkin.loadSkin(
            skinId = account.uniqueUUID.takeIf { account.hasSkinFile },
            model = account.skinModelType
        )
    }

    /**
     * 重置皮肤预览
     */
    fun resetSkin() {
        playerSkin.resetSkin()
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .padding(all = 16.dp)
                .fillMaxHeight()
                .fillMaxWidth(0.6f),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 6.dp)
                    .fillMaxHeight(0.85f)
                    .heightIn(max = maxHeight - 12.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = cardColor(false),
                contentColor = onCardColor(),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                                .clip(MaterialTheme.shapes.large)
                                .background(itemColor(false)),
                            contentAlignment = Alignment.Center
                        ) {
                            var pageFinished by remember { mutableStateOf(false) }

                            if (!pageFinished) {
                                //加载皮肤预览中
                                LoadingIndicator()
                            }

                            AndroidView(
                                factory = { context ->
                                    playerSkin.loadWebView(
                                        context = context,
                                        onPageFinished = {
                                            pageFinished = true
                                            playerSkin.startAnim(ModelAnimation.Walking, 0.8f)
                                            playerSkin.setAzimuthAndPitch(0, 10, 50)
                                        }
                                    )
                                },
                                update = {
                                    if (pageFinished) {
                                        when (skinState) {
                                            ChangeSkin.None -> loadSkin()
                                            is ChangeSkin.ChangeSkinData -> {
                                                runCatching {
                                                    skinState.cacheFile.inputStream().use { stream ->
                                                        playerSkin.loadSkin(stream, skinState.skinModel)
                                                    }
                                                }.onFailure {
                                                    playerSkin.loadSkin(
                                                        skinId = null,
                                                        skinState.skinModel
                                                    )
                                                }
                                            }

                                            is ChangeSkin.ResetSkin -> resetSkin()
                                        }
                                        if (account.isMicrosoftAccount()) {
                                            playerSkin.loadCape(currentCapeToLoad)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            //更换皮肤：选择皮肤图片文件
                            when (skinState) {
                                ChangeSkin.None, ChangeSkin.ResetSkin -> {
                                    InfoLayoutTextItem(
                                        modifier = Modifier.fillMaxWidth(),
                                        title = stringResource(R.string.account_change_skin),
                                        icon = {
                                            if (isImportingSkin) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(22.dp),
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Icon(
                                                    modifier = Modifier.size(22.dp),
                                                    painter = painterResource(R.drawable.ic_upload),
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        onClick = {
                                            skinPicker.launch(arrayOf("image/png"))
                                        },
                                        enabled = !isImportingSkin
                                    )
                                }

                                is ChangeSkin.ChangeSkinData -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.account_change_skin_arm_style),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        //选择样式
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            //粗臂
                                            RadioCard(
                                                selected = skinState.skinModel == SkinModelType.STEVE,
                                                text = stringResource(R.string.account_change_skin_arm_wide),
                                                onClick = {
                                                    onSkinStateChange(
                                                        skinState.copy(
                                                            skinModel = SkinModelType.STEVE
                                                        )
                                                    )
                                                }
                                            )
                                            //细臂
                                            RadioCard(
                                                selected = skinState.skinModel == SkinModelType.ALEX,
                                                text = stringResource(R.string.account_change_skin_arm_slim),
                                                onClick = {
                                                    onSkinStateChange(
                                                        skinState.copy(
                                                            skinModel = SkinModelType.ALEX
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            //仅微软账号支持更改披风
                            if (account.isMicrosoftAccount()) {
                                InfoLayoutTextItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = if (isFetchingCapes) {
                                        stringResource(R.string.account_change_cape_fetch_all)
                                    } else {
                                        stringResource(R.string.account_change_cape)
                                    },
                                    icon = {
                                        if (isFetchingCapes) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(22.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                modifier = Modifier.size(22.dp),
                                                painter = painterResource(R.drawable.ic_styler),
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    onClick = {
                                        showCapeSelector = true
                                    },
                                    enabled = !isFetchingCapes
                                )
                            }

                            //离线账号重置皮肤
                            if (account.isLocalAccount() && account.hasSkinFile && skinState != ChangeSkin.ResetSkin) {
                                InfoLayoutTextItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = stringResource(R.string.generic_reset),
                                    icon = {
                                        Icon(
                                            modifier = Modifier.size(22.dp),
                                            painter = painterResource(R.drawable.ic_restart_alt),
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        onSkinStateChange(ChangeSkin.ResetSkin)
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = onDismissRequest
                        ) {
                            Text(text = stringResource(R.string.generic_cancel))
                        }

                        Button(
                            modifier = Modifier.weight(1f),
                            enabled = skinState != ChangeSkin.None || capeState != ChangeCape.None,
                            onClick = {
                                when (skinState) {
                                    is ChangeSkin.ChangeSkinData -> {
                                        onApplySkin(skinState.cacheFile, skinState.skinModel)
                                    }

                                    is ChangeSkin.ResetSkin -> {
                                        onResetSkin()
                                    }

                                    ChangeSkin.None -> {}
                                }

                                if (capeState is ChangeCape.ChangeCapeData) {
                                    onApplyCape(capeState.cape)
                                }

                                onDismissRequest()
                            }
                        ) {
                            Text(text = stringResource(R.string.generic_confirm))
                        }
                    }
                }
            }
        }
    }

    if (showCapeSelector) {
        //若当前未更改披风，则使用使用中的披风
        val cape = if (capeState is ChangeCape.ChangeCapeData) {
            capeState.cape
        } else {
            currentUsingCape
        }

        SelectCapeDialog(
            capes = buildList {
                add(EmptyCape)
                addAll(availableCapes)
            },
            selectedCape = cape,
            onSelected = { cape, _ ->
                //检查是否已经为正在使用的披风
                val state = if (cape != currentUsingCape) {
                    ChangeCape.ChangeCapeData(cape)
                } else {
                    ChangeCape.None
                }
                onCapeStateChange(state)
                currentCapeToLoad = cape
                showCapeSelector = false
            },
            onDismiss = {
                showCapeSelector = false
            }
        )
    }
}

@Composable
fun SelectCapeDialog(
    capes: List<PlayerProfile.Cape>,
    selectedCape: PlayerProfile.Cape?,
    onSelected: (PlayerProfile.Cape, translatedName: String) -> Unit,
    onDismiss: () -> Unit,
    capeSize: Dp = 32.dp,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val capeLocals = remember(capes) {
        buildMap {
            capes.forEach { cape ->
                val translatedName = cape.capeLocalRes()
                    ?.let { context.getString(it) }
                if (translatedName != null) {
                    put(cape, translatedName)
                }
            }
        }
    }

    SimpleListDialog(
        title = stringResource(R.string.account_change_cape_select_cape),
        items = capes,
        itemTextProvider = { cape ->
            capeLocals[cape] ?: cape.alias
        },
        onItemSelected = { cape ->
            val name = capeLocals[cape] ?: cape.alias
            onSelected(cape, name)
        },
        current = selectedCape,
        itemLayout = { cape, isCurrent, text, onClick ->
            CapeListItem(
                modifier = Modifier.fillMaxWidth(),
                selected = isCurrent,
                cape = cape,
                name = text,
                size = capeSize,
                density = density,
                onClick = onClick,
            )
        },
        onDismissRequest = { selected ->
            if (!selected) {
                onDismiss()
            }
        }
    )
}

@Composable
fun CapeListItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    cape: PlayerProfile.Cape,
    name: String,
    size: Dp,
    density: Density,
    onClick: () -> Unit,
) {
    val avatar = remember(cape, density) {
        if (cape != EmptyCape) {
            getCapeAvatar(
                cape = cape,
                size = size,
                density = density
            )?.asImageBitmap()
        } else null
    }

    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )

        if (avatar != null) {
            val displayWidth = with(density) { avatar.width.toDp() }
            val displayHeight = with(density) { avatar.height.toDp() }
            Image(
                modifier = Modifier
                    .width(displayWidth)
                    .height(displayHeight),
                bitmap = avatar,
                contentDescription = null
            )

            Spacer(Modifier.width(12.dp))
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

private val avatarPaint = Paint().apply { isFilterBitmap = false }

private fun getCapeAvatar(
    cape: PlayerProfile.Cape,
    size: Dp,
    density: Density
): Bitmap? {
    val capeFile = cape.getFile(PathManager.DIR_ACCOUNT_CAPE)
    if (capeFile.exists()) {
        runCatching {
            Files.newInputStream(capeFile.toPath()).use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                    ?: throw IOException("Failed to read the cape picture and try to parse it to a bitmap")
                return getCapeAvatar(bitmap, size, density)
            }
        }.onFailure { e ->
            Logger.error(TAG, "Failed to load cape avatar from locally!", e)
        }
    }
    return null
}

private fun getCapeAvatar(
    cape: Bitmap,
    size: Dp,
    density: Density
): Bitmap {
    val pixelSize = with(density) { size.roundToPx() }
    val scaleFactor = cape.width / 64.0f
    val start = scaleFactor.roundToInt()
    val capeWidth = (10 * scaleFactor).roundToInt()
    val capeHeight = (16 * scaleFactor).roundToInt()
    val targetWidth = (pixelSize.toFloat() * capeWidth / capeHeight).roundToInt()
    val avatar = createBitmap(targetWidth, pixelSize)
    val canvas = Canvas(avatar)

    canvas.drawBitmap(
        cape,
        Rect(start, start, start + capeWidth, start + capeHeight),
        RectF(0f, 0f, targetWidth.toFloat(), pixelSize.toFloat()),
        avatarPaint
    )
    return avatar
}

private fun getSkinAvatarFromAccount(
    context: Context,
    account: Account,
    size: Dp,
    density: Density
): Bitmap {
    val skin = account.getSkinFile()
    if (skin.exists()) {
        runCatching {
            Files.newInputStream(skin.toPath()).use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                    ?: throw IOException("Failed to read the skin picture and try to parse it to a bitmap")
                return getSkinAvatar(bitmap, size, density)
            }
        }.onFailure { e ->
            Logger.error(TAG, "Failed to load skin avatar from locally!", e)
        }
    }
    return getDefaultAvatar(context, size, density)
}

@Throws(Exception::class)
private fun getDefaultAvatar(
    context: Context,
    size: Dp,
    density: Density
): Bitmap {
    return getSkinAvatar(
        skin = BitmapFactory.decodeStream(
            context.assets.open("steve.png")
        ),
        size = size,
        density = density
    )
}

private fun getSkinAvatar(
    skin: Bitmap,
    size: Dp,
    density: Density
): Bitmap {
    val pixelSize = with(density) { size.roundToPx() }
    val faceOffset = (pixelSize / 18.0).roundToInt().toFloat()
    val scaleFactor = skin.width / 64.0f
    val faceSize = (8 * scaleFactor).roundToInt()
    val faceEndY = faceSize * 2
    val hatSrcX = (40 * scaleFactor).roundToInt()
    val avatar = createBitmap(pixelSize, pixelSize)
    val canvas = Canvas(avatar)

    val innerEnd = pixelSize - faceOffset
    canvas.drawBitmap(
        skin,
        Rect(faceSize, faceSize, faceEndY, faceEndY),
        RectF(faceOffset, faceOffset, innerEnd, innerEnd),
        avatarPaint
    )

    canvas.drawBitmap(
        skin,
        Rect(hatSrcX, faceSize, hatSrcX + faceSize, faceEndY),
        RectF(0f, 0f, pixelSize.toFloat(), pixelSize.toFloat()),
        avatarPaint
    )
    return avatar
}
