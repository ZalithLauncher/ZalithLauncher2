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

package com.movtery.zalithlauncher.ui.screens.content.versions

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.COPY_LABEL_SERVER_IP
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.multiplayer.AllServers
import com.movtery.zalithlauncher.game.version.multiplayer.ServerData
import com.movtery.zalithlauncher.game.version.multiplayer.description.ComponentDescription
import com.movtery.zalithlauncher.game.version.multiplayer.description.ComponentDescriptionRoot
import com.movtery.zalithlauncher.game.version.multiplayer.description.ServerDescription
import com.movtery.zalithlauncher.game.version.multiplayer.description.StringDescription
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.components.EdgeDirection
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.ShimmerBox
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.components.SingleLineTextCheck
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ComponentText
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.MinecraftColorText
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.MinecraftColorTextNormal
import com.movtery.zalithlauncher.ui.screens.content.versions.layouts.VersionChunkBackground
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.copyText
import com.movtery.zalithlauncher.utils.logging.Logger.lInfo
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank
import com.movtery.zalithlauncher.utils.string.stripColorCodes
import com.movtery.zalithlauncher.viewmodel.LaunchGameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

private sealed interface ServerListOperation {
    /** 服务器列表刷新中 */
    data object Loading : ServerListOperation
    /** 已加载服务器数据 */
    data object LoadedData : ServerListOperation
}

private sealed interface ServerDataOperation {
    data object None: ServerDataOperation
    /** 添加一个服务器 */
    data object AddServer : ServerDataOperation
    /** 删除一个服务器 */
    data class DeleteServer(val data: ServerData) : ServerDataOperation
    /** 编辑一个服务器 */
    data class EditServer(val data: ServerData) : ServerDataOperation
}

private class ServerListViewModel(
    val gamePath: File,
    val serverData: File
) : ViewModel() {
    var operation by mutableStateOf<ServerListOperation>(ServerListOperation.Loading)

    private val _servers = MutableStateFlow<List<ServerData>?>(emptyList())
    val servers = _servers.asStateFlow()

    private val allServers = AllServers()

    /**
     * 搜索服务器的名称
     */
    var searchName by mutableStateOf("")

    /**
     * 所有正在加载中的服务器
     */
    private val allLoadingServer = mutableMapOf<ServerData, Job>()

    private var refreshJob: Job? = null
    private var saveJobQueue = mutableListOf<Job>()
    private val dataMutex = Mutex()

    private var searchJob: Job? = null

    /**
     * 开始加载服务器列表数据
     */
    fun loadServer() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch(Dispatchers.IO) {
            dataMutex.withLock {
                loadServerSuspend()
            }
        }
    }

    private suspend fun loadServerSuspend() {
        withContext(Dispatchers.Main) {
            operation = ServerListOperation.Loading
            _servers.update { emptyList() }
        }

        //取消所有的加载任务
        allLoadingServer.forEach { (_, job) ->
            job.cancel()
        }
        allLoadingServer.clear()

        allServers.loadServers(serverData)

        withContext(Dispatchers.Main) {
            operation = ServerListOperation.LoadedData
            reloadServerList()
        }
    }

    /**
     * 仅重载当前的服务器列表
     */
    private suspend fun reloadServerList() {
        withContext(Dispatchers.Main) {
            _servers.update {
                if (allServers.serverList.isEmpty()) {
                    null
                } else {
                    filteredServers()
                }
            }
        }
    }

    /**
     * 是否正在保存服务器列表
     */
    var saving by mutableStateOf(false)
        private set
    /**
     * 对服务器的各种操作流程
     */
    var dataOperation by mutableStateOf<ServerDataOperation>(ServerDataOperation.None)

    /**
     * 添加一个新的服务器
     * @param serverName 服务器名称
     * @param serverAddress 服务器地址
     */
    fun addServer(
        serverName: String,
        serverAddress: String
    ) {
        saveServers(
            reason = "added server",
            beforeSave = {
                val data = ServerData(name = serverName, originIp = serverAddress)
                allServers.addServer(data)
            }
        )
    }

    /**
     * 删除一个服务器
     */
    fun deleteServer(
        data: ServerData
    ) {
        saveServers(
            reason = "deleted server",
            beforeSave = {
                allServers.removeServer(data)
                allLoadingServer[data]?.cancel()
                allLoadingServer.remove(data)
            }
        )
    }

    /**
     * 编辑过一个服务器
     */
    fun editedServer(
        data: ServerData
    ) {
        saveServers(
            reason = "edited server",
            beforeSave = {
                allLoadingServer[data]?.cancel()
                allLoadingServer.remove(data)
            },
            afterSave = {
                //未对服务器列表做增删，所以需要手动刷新这个服务器
                loadServer(data, true)
            }
        )
    }

    /**
     * 保存服务器列表
     * @param reason 保存服务器列表的理由，方便日志定位
     * @param gamePath 指定 servers.dat 文件的保存目录
     * @param beforeSave 在保存前可以进行的操作
     * @param beforeSave 在保存后可以进行的操作
     */
    private fun saveServers(
        reason: String? = null,
        reload: Boolean = true,
        beforeSave: suspend () -> Unit = {},
        afterSave: suspend () -> Unit = {}
    ) {
        val job = viewModelScope.launch(Dispatchers.IO) {
            dataMutex.withLock {
                lInfo("Saving server list, reason = $reason, reload UI? = $reload")

                withContext(Dispatchers.Main) { saving = true }
                beforeSave()
                allServers.save(gamePath)
                if (reload) reloadServerList()
                afterSave()
                withContext(Dispatchers.Main) { saving = false }
            }
        }
        saveJobQueue.add(job)
    }

    private fun filteredServers(): List<ServerData> {
        return allServers.serverList.filter { server ->
            server.name.contains(searchName)
        }
    }

    /**
     * 根据现有的搜索名称，刷新显示服务器列表
     */
    fun filterServers() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val servers = filteredServers()
            _servers.update { servers }
        }
    }

    /**
     * 尝试 Ping 服务器
     * @param isRefresh 是否强制刷新该服务器
     */
    fun loadServer(
        server: ServerData,
        isRefresh: Boolean = false
    ) {
        if (isRefresh) {
            allLoadingServer[server]?.cancel()
            allLoadingServer.remove(server)
        }

        if (server in allLoadingServer) return

        allLoadingServer[server] = viewModelScope.launch {
            server.load { reason ->
                saveServers(
                    reason = reason,
                    reload = false
                )
            }
        }
    }

    /**
     * 复制服务器ip
     */
    fun copy(
        context: Context,
        ip: String
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            copyText(label = COPY_LABEL_SERVER_IP, text = ip, context = context)
        }
    }

    init {
        loadServer()
    }

    override fun onCleared() {
        refreshJob?.cancel()
        refreshJob = null
        searchJob?.cancel()
        searchJob = null
        saveJobQueue.forEach { it.cancel() }
        saveJobQueue.clear()
    }
}

@Composable
private fun rememberServerListViewModel(
    gamePath: File,
    serverData: File,
    version: Version
): ServerListViewModel {
    return viewModel(
        key = version.toString() + "_" + "ServerList"
    ) {
        ServerListViewModel(
            gamePath = gamePath,
            serverData = serverData
        )
    }
}

@Composable
private fun ServerDataOperation(
    operation: ServerDataOperation,
    onChange: (ServerDataOperation) -> Unit,
    onAddServer: (name: String, ip: String) -> Unit,
    onEditedServer: (ServerData) -> Unit,
    onDeleteServer: (ServerData) -> Unit
) {
    when (operation) {
        is ServerDataOperation.None -> {}
        is ServerDataOperation.AddServer -> {
            ServerEditDialog(
                title = stringResource(R.string.servers_list_add_server),
                name = null,
                address = "",
                onDismissRequest = {
                    onChange(ServerDataOperation.None)
                },
                onApply = onAddServer
            )
        }
        is ServerDataOperation.DeleteServer -> {
            val serverName = remember(operation) {
                operation.data.name.stripColorCodes()
            }

            SimpleAlertDialog(
                title = stringResource(R.string.servers_list_delete_server),
                text = stringResource(R.string.servers_list_delete_server_text, serverName),
                onDismiss = {
                    onChange(ServerDataOperation.None)
                },
                onConfirm = {
                    onDeleteServer(operation.data)
                    onChange(ServerDataOperation.None)
                }
            )
        }
        is ServerDataOperation.EditServer -> {
            val data = operation.data
            ServerEditDialog(
                title = stringResource(R.string.servers_list_edit_server),
                name = data.name,
                address = data.originIp,
                onDismissRequest = {
                    onChange(ServerDataOperation.None)
                },
                onApply = { name, ip ->
                    data.name = name
                    data.originIp = ip
                    onEditedServer(data)
                }
            )
        }
    }
}

@Composable
fun ServerListScreen(
    mainScreenKey: TitledNavKey?,
    versionsScreenKey: TitledNavKey?,
    launchGameViewModel: LaunchGameViewModel,
    version: Version,
    backToMainScreen: () -> Unit,
) {
    if (!version.isValid()) {
        backToMainScreen()
        return
    }

    val dataFile = remember(version) {
        File(version.getGameDir(), "servers.dat")
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.VersionSettings::class.java, mainScreenKey)
        ),
        Triple(NormalNavKey.Versions.ServerList, versionsScreenKey, false),
    ) { isVisible ->
        val context = LocalContext.current

        val viewModel = rememberServerListViewModel(
            gamePath = version.getGameDir(),
            serverData = dataFile,
            version = version
        )

        val yOffset by swapAnimateDpAsState(
            targetValue = (-40).dp,
            swapIn = isVisible
        )

        ServerDataOperation(
            operation = viewModel.dataOperation,
            onChange = { viewModel.dataOperation = it },
            onAddServer = { name, ip ->
                viewModel.addServer(
                    serverName = name,
                    serverAddress = ip
                )
            },
            onEditedServer = { data ->
                viewModel.editedServer(
                    data = data
                )
            },
            onDeleteServer = { server ->
                viewModel.deleteServer(
                    data = server
                )
            }
        )

        VersionChunkBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 12.dp)
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
            paddingValues = PaddingValues()
        ) {
            when (viewModel.operation) {
                is ServerListOperation.Loading -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                }
                is ServerListOperation.LoadedData -> {
                    val servers by viewModel.servers.collectAsStateWithLifecycle()

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ServerListHeader(
                            searchName = viewModel.searchName,
                            onSearchNameChange = {
                                viewModel.searchName = it
                                viewModel.filterServers()
                            },
                            onAddServer = {
                                viewModel.dataOperation = ServerDataOperation.AddServer
                            },
                            refreshServers = {
                                viewModel.loadServer()
                            }
                        )

                        ServerListBody(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            servers = servers,
                            isSavingServer = viewModel.saving,
                            onLoad = { viewModel.loadServer(it) },
                            onRefresh = { viewModel.loadServer(it, true) },
                            onCopy = { viewModel.copy(context, it) },
                            onPlay = { address ->
                                launchGameViewModel.quickPlayServer(version, address)
                            },
                            onEdit = { data ->
                                viewModel.dataOperation = ServerDataOperation.EditServer(data)
                            },
                            onDelete = { data ->
                                viewModel.dataOperation = ServerDataOperation.DeleteServer(data)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerListHeader(
    searchName: String,
    onSearchNameChange: (String) -> Unit,
    onAddServer: () -> Unit,
    refreshServers: () -> Unit,
    modifier: Modifier = Modifier,
    inputFieldColor: Color = itemColor(),
    inputFieldContentColor: Color = onItemColor()
) {
    CardTitleLayout(modifier = modifier) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SimpleTextInputField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    value = searchName,
                    onValueChange = { onSearchNameChange(it) },
                    hint = {
                        Text(
                            text = stringResource(R.string.generic_search),
                            style = TextStyle(color = LocalContentColor.current).copy(fontSize = 12.sp)
                        )
                    },
                    color = inputFieldColor,
                    contentColor = inputFieldContentColor,
                    singleLine = true
                )

                val scrollState = rememberScrollState()
                LaunchedEffect(Unit) {
                    scrollState.scrollTo(scrollState.maxValue)
                }
                Row(
                    modifier = Modifier
                        .fadeEdge(
                            state = scrollState,
                            length = 32.dp,
                            direction = EdgeDirection.Horizontal
                        )
                        .widthIn(max = this@BoxWithConstraints.maxWidth / 2)
                        .horizontalScroll(scrollState),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(6.dp))

                    //添加服务器
                    IconTextButton(
                        onClick = onAddServer,
                        painter = painterResource(R.drawable.ic_add),
                        text = stringResource(R.string.servers_list_add_server)
                    )

                    IconButton(
                        onClick = refreshServers
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = stringResource(R.string.generic_refresh)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerListBody(
    servers: List<ServerData>?,
    isSavingServer: Boolean,
    onLoad: (ServerData) -> Unit,
    onRefresh: (ServerData) -> Unit,
    onCopy: (String) -> Unit,
    onPlay: (String) -> Unit,
    onEdit: (ServerData) -> Unit,
    onDelete: (ServerData) -> Unit,
    modifier: Modifier = Modifier,
) {
    servers?.let { list ->
        if (list.isNotEmpty()) {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(all = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(list) { server ->
                    ServerItem(
                        modifier = Modifier.fillMaxWidth(),
                        item = server,
                        isSavingServer = isSavingServer,
                        onLoad = { onLoad(server) },
                        onRefresh = { onRefresh(server) },
                        onCopy = { onCopy(server.originIp) },
                        onPlay = { onPlay(server.originIp) },
                        onEdit = { onEdit(server) },
                        onDelete = { onDelete(server) }
                    )
                }
            }
        } else {
            //如果列表是空的，则是由搜索导致的
            //展示“无匹配项”文本
            Box(modifier = Modifier.fillMaxSize()) {
                ScalingLabel(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.generic_no_matching_items)
                )
            }
        }
    } ?: run {
        //如果为null，则代表本身就没有存档可以展示
        Box(modifier = Modifier.fillMaxSize()) {
            ScalingLabel(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.servers_list_no_servers)
            )
        }
    }
}

@Composable
private fun ServerItem(
    item: ServerData,
    isSavingServer: Boolean,
    onLoad: () -> Unit,
    onRefresh: () -> Unit,
    onCopy: () -> Unit,
    onPlay: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    shape: Shape = MaterialTheme.shapes.large,
    itemColor: Color = itemColor(),
    itemContentColor: Color = onItemColor(),
) {
    val ot = item.operation

    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }

    LaunchedEffect(item) {
        onLoad()
    }

    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        onClick = onClick,
        shape = shape,
        color = itemColor,
        contentColor = itemContentColor,
    ) {
        val alphaModifier = Modifier.alpha(0.7f)

        Row(
            modifier = Modifier
                .padding(all = 8.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //服务器的图标
            ServerIcon(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(10.dp)),
                server = item,
                size = 64.dp,
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                //服务器名称、状态、描述
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        //服务器名称
                        MinecraftColorTextNormal(
                            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                            inputText = item.name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1
                        )

                        //显示服务器的延迟、在线人数
                        if (ot is ServerData.Operation.Loaded) {
                            val undefined = stringResource(R.string.servers_list_undefined)

                            //服务器延迟显示部分
                            val signalStrength = remember(ot) {
                                val pingMs = ot.result.pingMs
                                if (pingMs < 150L) 5
                                else if (pingMs < 300L) 4
                                else if (pingMs < 600L) 3
                                else if (pingMs < 1000L) 2
                                else 1
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ServerSignalIcon(
                                    modifier = Modifier.size(16.dp),
                                    signalStrength = signalStrength
                                )
                                Text(
                                    modifier = alphaModifier,
                                    text = "${ot.result.pingMs} ms",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            //在线人数显示部分
                            val playerFull = stringResource(R.string.servers_list_players_full)

                            val onlineStatus = remember(ot) {
                                val players = ot.result.status.players

                                buildString {
                                    val max = players.max
                                    val online = players.online

                                    //在线玩家数小于0，则认为服务器未定义
                                    if (online < 0) {
                                        append(undefined)
                                    } else {
                                        if (max > 0) {
                                            if (online in 0..players.max) {
                                                append(online)
                                            } else {
                                                append(playerFull)
                                            }
                                            append('/')
                                            append(players.max)
                                        } else {
                                            //服务器未定义最大玩家数
                                            //仅显示当前在线玩家数
                                            append(online)
                                        }
                                    }
                                }
                                "${players.online}/${players.max}"
                            }
                            //在线人数信息
                            Row(
                                modifier = alphaModifier,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    modifier = Modifier.size(16.dp),
                                    painter = painterResource(R.drawable.ic_person_outlined),
                                    contentDescription = null
                                )
                                Text(
                                    text = onlineStatus,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    when (ot) {
                        is ServerData.Operation.Loading -> {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }

                        is ServerData.Operation.Loaded -> {
                            ot.result.status.description?.let { des ->
                                DescriptionTextRender(
                                    description = des,
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                    maxLines = 2
                                )
                            }
                        }

                        is ServerData.Operation.Failed -> {
                            Text(
                                text = stringResource(R.string.servers_list_failed_to_connect),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                //服务器ip地址
                Text(
                    modifier = alphaModifier,
                    text = item.originIp,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Row {

                //快速启动
                IconButton(
                    onClick = onPlay,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_play_arrow_filled),
                        contentDescription = stringResource(R.string.main_launch_game)
                    )
                }

                Box {
                    var expanded by remember { mutableStateOf(false) }

                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(R.drawable.ic_more_horiz),
                            contentDescription = stringResource(R.string.generic_more)
                        )
                    }

                    //当前服务器是否正在加载中
                    val isLoading = ot is ServerData.Operation.Loading

                    DropdownMenu(
                        expanded = expanded,
                        shape = MaterialTheme.shapes.large,
                        shadowElevation = 3.dp,
                        onDismissRequest = { expanded = false },
                    ) {
                        //刷新服务器
                        DropdownMenuItem(
                            enabled = !isLoading,
                            text = { Text(text = stringResource(R.string.generic_refresh)) },
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(R.drawable.ic_refresh),
                                    contentDescription = stringResource(R.string.generic_refresh)
                                )
                            },
                            onClick = {
                                onRefresh()
                                expanded = false
                            }
                        )

                        //复制服务器ip
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.servers_list_copy_server_address)) },
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(R.drawable.ic_copy_all_filled),
                                    contentDescription = stringResource(R.string.servers_list_copy_server_address)
                                )
                            },
                            onClick = {
                                onCopy()
                                expanded = false
                            }
                        )

                        //编辑服务器
                        DropdownMenuItem(
                            enabled = !isSavingServer,
                            text = { Text(text = stringResource(R.string.servers_list_edit_server)) },
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(R.drawable.ic_edit_filled),
                                    contentDescription = stringResource(R.string.servers_list_edit_server)
                                )
                            },
                            onClick = {
                                onEdit()
                                expanded = false
                            }
                        )

                        //删除服务器
                        DropdownMenuItem(
                            enabled = !isSavingServer,
                            text = { Text(text = stringResource(R.string.servers_list_delete_server)) },
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(R.drawable.ic_delete_filled),
                                    contentDescription = stringResource(R.string.servers_list_delete_server)
                                )
                            },
                            onClick = {
                                onDelete()
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 服务器信号动态绘制图标
 */
@Composable
fun ServerSignalIcon(
    modifier: Modifier = Modifier,
    signalStrength: Int,
    mainColor: Color = MaterialTheme.colorScheme.primary,
    otherColor: Color = MaterialTheme.colorScheme.background,
    contentPadding: PaddingValues = PaddingValues(vertical = 2.dp)
) {
    require(signalStrength in 1..5) {
        "signalStrength must be between 1 and 5"
    }

    Canvas(modifier = modifier.padding(contentPadding)) {
        val barCount = 5
        val barWidth = size.width / (barCount * 2 - 1)

        for (i in 0 until barCount) {
            val barHeight = size.height * (i + 1) / barCount
            val xOffset = i * (barWidth + barWidth)
            val yOffset = size.height - barHeight

            drawRect(
                color = if (i < signalStrength) mainColor else otherColor,
                topLeft = Offset(xOffset, yOffset),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
private fun ServerIcon(
    server: ServerData,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val density = LocalDensity.current
    val pxSize = with(density) { size.roundToPx() }

    val imageRequest = remember(server, server.refreshUI, pxSize) {
        ImageRequest.Builder(context)
            .data(server.icon)
            .size(pxSize) //固定大小
            .crossfade(true)
            .build()
    }

    val painter = rememberAsyncImagePainter(
        model = imageRequest,
        placeholder = null,
        error = painterResource(R.drawable.ic_unknown_icon)
    )

    val state by painter.state.collectAsStateWithLifecycle()
    val sizeModifier = modifier.size(size)

    when (state) {
        AsyncImagePainter.State.Empty -> {
            Box(modifier = sizeModifier)
        }
        is AsyncImagePainter.State.Loading -> {
            ShimmerBox(
                modifier = sizeModifier
            )
        }
        is AsyncImagePainter.State.Error,
        is AsyncImagePainter.State.Success -> {
            Image(
                painter = painter,
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = sizeModifier,
            )
        }
    }
}

/**
 * 服务器描述文本渲染，尝试模仿 Minecraft 原版对于 Component 文本组件的渲染
 */
@Composable
private fun DescriptionTextRender(
    description: ServerDescription,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
) {
    when(description) {
        is ComponentDescriptionRoot -> {
            ComponentText(
                modifier = modifier,
                descriptions = description.values,
                fontSize = fontSize,
                maxLines = maxLines
            )
        }
        is ComponentDescription -> {
            ComponentText(
                modifier = modifier,
                descriptions = listOf(description),
                fontSize = fontSize,
                maxLines = maxLines
            )
        }
        is StringDescription -> {
            val value = remember(description) { description.value }
            MinecraftColorText(
                modifier = modifier,
                inputText = value,
                fontSize = fontSize,
                maxLines = maxLines
            )
        }
    }
}

/**
 * 编辑服务器信息对话框
 */
@Composable
private fun ServerEditDialog(
    title: String,
    onDismissRequest: () -> Unit,
    onApply: (name: String, ip: String) -> Unit,
    name: String? = null,
    address: String = "",
) {
    //默认的服务器名称，不填时使用它
    val defaultName = stringResource(R.string.servers_list_add_server_default_name)
    var name by remember { mutableStateOf(name?.takeIf { it.isNotEmpty() } ?: defaultName) }
    var ip by remember { mutableStateOf(address) }

    //仅检查服务器地址栏是否为空
    val isIpEmpty = remember(ip) {
        ip.isEmptyOrBlank()
    }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
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
                        text = title,
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
                        OwnOutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = name,
                            onValueChange = {
                                name = it
                            },
                            label = { Text(text = stringResource(R.string.servers_list_add_server_name)) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.large
                        )

                        Spacer(modifier = Modifier.size(8.dp))

                        SingleLineTextCheck(
                            text = ip,
                            onSingleLined = { ip = it }
                        )

                        OwnOutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = ip,
                            onValueChange = {
                                ip = it
                            },
                            isError = isIpEmpty,
                            label = { Text(text = stringResource(R.string.servers_list_add_server_ip)) },
                            supportingText = {
                                if (isIpEmpty) {
                                    Text(text = stringResource(R.string.generic_cannot_empty))
                                }
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.large
                        )
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
                                if (!isIpEmpty) {
                                    val name0 = name.ifEmpty { defaultName }

                                    onApply(name0, ip)
                                    onDismissRequest()
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