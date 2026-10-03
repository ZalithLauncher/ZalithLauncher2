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

package com.movtery.zalithlauncher.ui.screens.content.download.game

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.nonInteractiveScrollbar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersion
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.game.versioninfo.models.isType
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.CheckChip
import com.movtery.zalithlauncher.ui.components.EdgeDirection
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.classes.Quadruple
import com.movtery.zalithlauncher.utils.formatDate
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.toLocal
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

private const val TAG = "SelectGameVersion"

/** 版本列表加载状态 */
private sealed interface VersionState {
    /** 加载中 */
    data object Loading : VersionState
    /** 加载完成 */
    data class None(val versions: List<MinecraftVersion>) : VersionState
    /** 加载出现异常 */
    data class Failure(val message: AndroidStringText) : VersionState
}

/**
 * 版本过滤条件
 * @param release 是否保留正式版本
 * @param snapshot 是否保留快照版本
 * @param old 是否保留旧版本
 * @param id 搜索并过滤版本ID
 */
private data class VersionFilter(
    val release: Boolean = true,
    val snapshot: Boolean = false,
    val aprilFools: Boolean = false,
    val old: Boolean = false,
    val id: String = ""
)

private class VersionsViewModel: ViewModel() {
    var versionState by mutableStateOf<VersionState>(VersionState.Loading)
        private set

    //简易版本类型过滤器
    var versionFilter by mutableStateOf(VersionFilter())
        private set

    fun filterWith(filter: VersionFilter) {
        versionFilter = filter
        viewModelScope.launch {
            val allVersions = MinecraftVersions.allVersions.value
            versionState = VersionState.None(
                versions = allVersions.filterVersions(versionFilter)
            )
        }
    }

    fun refresh(forceReload: Boolean = false) {
        viewModelScope.launch {
            versionState = VersionState.Loading
            versionState = runCatching {
                MinecraftVersions.refreshVersions(forceReload)
                val allVersions = MinecraftVersions.allVersions.value
                VersionState.None(allVersions.filterVersions(versionFilter))
            }.getOrElse { e ->
                Logger.warning(TAG, "Failed to get version manifest!", e)
                val message: AndroidStringText = when(e) {
                    is HttpRequestTimeoutException -> androidText(R.string.error_timeout)
                    is UnknownHostException, is UnresolvedAddressException -> androidText(R.string.error_network_unreachable)
                    is ConnectException -> androidText(R.string.error_connection_failed)
                    is ResponseException -> e.toLocal()
                    else -> {
                        Logger.error(TAG, "An unknown exception was caught!", e)
                        androidText(e.localizedMessage ?: e.message ?: e::class.qualifiedName ?: "Unknown error")
                    }
                }
                VersionState.Failure(message)
            }
        }
    }

    init {
        //初始化后，刷新版本列表
        refresh()
    }

    override fun onCleared() {
        viewModelScope.cancel()
    }
}

@Composable
fun SelectGameVersionScreen(
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadGameScreenKey: TitledNavKey?,
    eventViewModel: EventViewModel,
    onVersionSelect: (String) -> Unit = {}
) {
    val viewModel = viewModel(
        key = NormalNavKey.DownloadGame.SelectGameVersion.toString()
    ) {
        VersionsViewModel()
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.Download::class.java, mainScreenKey),
            Pair(NestedNavKey.DownloadGame::class.java, downloadScreenKey)
        ),
        Triple(NormalNavKey.DownloadGame.SelectGameVersion, downloadGameScreenKey, false)
    ) { isVisible ->
        val yOffset by swapAnimateDpAsState(
            targetValue = (-40).dp,
            swapIn = isVisible
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
        ) {
            when (val state = viewModel.versionState) {
                is VersionState.Loading -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LinearWavyProgressIndicator(
                            modifier = Modifier.width(168.dp),
                            wavelength = 32.dp
                        )
                    }
                }

                is VersionState.Failure -> {
                    Box(Modifier.fillMaxSize()) {
                        ScalingLabel(
                            modifier = Modifier.align(Alignment.Center),
                            text = {
                                AndroidStringText(
                                    text = androidText(
                                        R.string.download_game_failed_to_get_versions,
                                        state.message
                                    )
                                )
                            },
                            onClick = {
                                viewModel.refresh(true)
                            }
                        )
                    }
                }

                is VersionState.None -> {
                    val versions = state.versions
                    var selectedVersionId by remember(versions) {
                        mutableStateOf(
                            versions.firstOrNull { it.version.id == "1.21.1" }?.version?.id
                                ?: versions.firstOrNull()?.version?.id
                                ?: "1.21.1"
                        )
                    }
                    var selectedLoader by remember { mutableStateOf("Fabric") }
                    var instanceNameInput by remember(selectedVersionId, selectedLoader) {
                        mutableStateOf("$selectedLoader $selectedVersionId")
                    }

                    val isLegacySelected = remember(selectedVersionId) {
                        selectedVersionId.startsWith("1.8") ||
                            selectedVersionId.startsWith("1.9") ||
                            selectedVersionId.startsWith("1.10") ||
                            selectedVersionId.startsWith("1.11") ||
                            selectedVersionId.startsWith("1.12") ||
                            selectedVersionId.startsWith("1.13") ||
                            selectedVersionId.startsWith("1.14") ||
                            selectedVersionId.startsWith("1.15") ||
                            selectedVersionId.startsWith("1.16")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // LEFT PANE: Instance Info & Minecraft Version List (Mockup #5)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Create New Instance",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                IconButton(
                                    onClick = { viewModel.refresh(true) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_refresh),
                                        contentDescription = stringResource(R.string.generic_refresh),
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Instance Info",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE5E7EB)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF21242B),
                                    border = BorderStroke(1.dp, Color(0xFF2E333E))
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.img_old_grass_block),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .size(28.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF21242B))
                                        .border(1.dp, Color(0xFF2E333E), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    BasicTextField(
                                        value = instanceNameInput,
                                        onValueChange = { instanceNameInput = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        cursorBrush = SolidColor(Color(0xFF1BD96A)),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Text(
                                text = "Minecraft Version",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE5E7EB)
                            )

                            // Filter Pills: Releases, Snapshots, Old Beta + compact search
                            val versionFilter = viewModel.versionFilter
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val filterItems = listOf(
                                    Triple("Releases", versionFilter.release) {
                                        viewModel.filterWith(versionFilter.copy(release = !versionFilter.release))
                                    },
                                    Triple("Snapshots", versionFilter.snapshot) {
                                        viewModel.filterWith(versionFilter.copy(snapshot = !versionFilter.snapshot))
                                    },
                                    Triple("Old Beta", versionFilter.old) {
                                        viewModel.filterWith(versionFilter.copy(old = !versionFilter.old))
                                    }
                                )
                                filterItems.forEach { (label, active, onToggle) ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (active) Color(0xFF1BD96A) else Color(0xFF21242B),
                                        border = BorderStroke(
                                            1.dp,
                                            if (active) Color(0xFF1BD96A) else Color(0xFF2E333E)
                                        ),
                                        onClick = onToggle
                                    ) {
                                        Text(
                                            text = label,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (active) Color(0xFF06210F) else Color(0xFFD1D5DB)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(28.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF21242B))
                                        .border(1.dp, Color(0xFF2E333E), RoundedCornerShape(14.dp))
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (versionFilter.id.isEmpty()) {
                                        Text(
                                            text = "Filter...",
                                            fontSize = 11.sp,
                                            color = Color(0xFF8A909E)
                                        )
                                    }
                                    BasicTextField(
                                        value = versionFilter.id,
                                        onValueChange = {
                                            viewModel.filterWith(versionFilter.copy(id = it))
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 11.sp
                                        ),
                                        cursorBrush = SolidColor(Color(0xFF1BD96A)),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                items(
                                    items = versions,
                                    key = { it.version.id }
                                ) { ver ->
                                    val verId = ver.version.id
                                    val isSelected = verId == selectedVersionId

                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) Color(0xFF172D22) else Color(0xFF21242B),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF1BD96A) else Color(0xFF2E333E)
                                        ),
                                        onClick = {
                                            selectedVersionId = verId
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Minecraft $verId",
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            if (isSelected) {
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color(0xFF1F4732)
                                                ) {
                                                    Text(
                                                        text = "Selected",
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF6EE7B7)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // RIGHT PANE: Modloader 2x3 Grid + Create CTA
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Modloader",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                val loaderRows = listOf(
                                    listOf("Vanilla" to "", "Fabric" to "0.16.9", "NeoForge" to "Latest"),
                                    listOf("Forge" to "Recommended", "Quilt" to "Latest", "OptiFine" to "HD U")
                                )

                                loaderRows.forEach { row ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        row.forEach { (loaderName, subLabel) ->
                                            val isSelected = selectedLoader == loaderName
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(74.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) Color(0xFF1B2B24) else Color(0xFF21242B),
                                                border = BorderStroke(
                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                    color = if (isSelected) Color(0xFF1BD96A) else Color(0xFF2E333E)
                                                ),
                                                onClick = { selectedLoader = loaderName }
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(8.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = loaderName,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color.White
                                                    )
                                                    if (subLabel.isNotEmpty()) {
                                                        Text(
                                                            text = subLabel,
                                                            fontSize = 11.sp,
                                                            color = if (isSelected) Color(0xFF1BD96A) else Color(0xFF9CA3AF)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = { onVersionSelect(selectedVersionId) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(22.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1BD96A),
                                    contentColor = Color(0xFF06210F)
                                )
                            ) {
                                Text(
                                    text = "Create & Install Instance",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 简易过滤器，过滤特定类型的版本
 */
private fun List<MinecraftVersion>.filterVersions(
    versionFilter: VersionFilter
) = this.filter { version ->
    version.isType(
        release = versionFilter.release,
        snapshot = versionFilter.snapshot,
        aprilFools = versionFilter.aprilFools,
        old = versionFilter.old
    )
}.filter { version ->
    //Fix：单独过滤版本名称
    val versionId = versionFilter.id
    versionId.isEmptyOrBlank() || version.version.id.contains(versionId)
}

@Composable
private fun VersionHeader(
    modifier: Modifier = Modifier,
    versionFilter: VersionFilter,
    onVersionFilterChange: (VersionFilter) -> Unit,
    itemContainerColor: Color,
    itemContentColor: Color,
    onRefreshClick: () -> Unit = {}
) {
    Column(modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fadeEdge(
                            state = scrollState,
                            direction = EdgeDirection.Horizontal
                        )
                        .widthIn(max = this@BoxWithConstraints.maxWidth / 5 * 3) //3/5
                        .horizontalScroll(scrollState),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    //版本筛选条件
                    VersionTypeItem(
                        selected = versionFilter.release,
                        onClick = {
                            onVersionFilterChange(versionFilter.copy(release = versionFilter.release.not()))
                        },
                        text = stringResource(R.string.download_game_type_release)
                    )
                    VersionTypeItem(
                        selected = versionFilter.snapshot,
                        onClick = {
                            onVersionFilterChange(versionFilter.copy(snapshot = versionFilter.snapshot.not()))
                        },
                        text = stringResource(R.string.download_game_type_snapshot)
                    )
                    VersionTypeItem(
                        selected = versionFilter.aprilFools,
                        onClick = {
                            onVersionFilterChange(versionFilter.copy(aprilFools = versionFilter.aprilFools.not()))
                        },
                        text = stringResource(R.string.download_game_type_april_fools)
                    )
                    VersionTypeItem(
                        selected = versionFilter.old,
                        onClick = {
                            onVersionFilterChange(versionFilter.copy(old = versionFilter.old.not()))
                        },
                        text = stringResource(R.string.download_game_type_old)
                    )
                }

                //搜索、刷新
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SimpleTextInputField(
                        modifier = Modifier.weight(1f),
                        value = versionFilter.id,
                        onValueChange = { onVersionFilterChange(versionFilter.copy(id = it)) },
                        color = itemContainerColor,
                        contentColor = itemContentColor,
                        singleLine = true,
                        hint = {
                            Text(
                                text = stringResource(R.string.generic_search),
                                style = TextStyle(color = itemContentColor).copy(fontSize = 12.sp)
                            )
                        }
                    )

                    IconButton(
                        onClick = onRefreshClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = stringResource(R.string.generic_refresh)
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun VersionTypeItem(
    selected: Boolean,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CheckChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = {
            Text(text)
        },
    )
}

@Composable
private fun VersionList(
    modifier: Modifier = Modifier,
    versions: List<MinecraftVersion>,
    onVersionSelect: (String) -> Unit,
    openLink: (url: String) -> Unit
) {
    val scrollState = rememberLazyListState()
    LazyColumn(
        modifier = modifier.nonInteractiveScrollbar(
            state = scrollState.scrollIndicatorState!!,
            orientation = Orientation.Vertical,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        state = scrollState,
    ) {
        items(versions) { version ->
            VersionItemLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                version = version,
                onClick = {
                    onVersionSelect(version.version.id)
                },
                onAccessWiki = { wikiUrl ->
                    openLink(wikiUrl)
                },
            )
        }
    }
}

@Composable
private fun VersionItemLayout(
    modifier: Modifier = Modifier,
    version: MinecraftVersion,
    onClick: () -> Unit = {},
    onAccessWiki: (String) -> Unit = {},
    shape: Shape = MaterialTheme.shapes.large,
    influencedByBackground: Boolean = true,
    color: Color = cardColor(influencedByBackground),
    contentColor: Color = onCardColor(),
    blur: Int = AllSettings.backgroundBlur.state,
) {
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }

    val (icon, versionType, wikiUrl, summary) = getVersionComponents(version)

    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        onClick = onClick,
        shape = shape,
        color = color,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .clip(shape = shape)
                .backgroundGlass(blur, color, influencedByBackground)
                .padding(all = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let { versionIcon ->
                Image(
                    modifier = Modifier.size(32.dp),
                    painter = versionIcon,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = version.version.id,
                        style = MaterialTheme.typography.labelLarge
                    )

                    LittleTextLabel(
                        text = versionType
                    )
                }

                summary?.let { text ->
                    Text(
                        modifier = Modifier.alpha(0.7f),
                        text = text,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = formatDate(
                        input = version.version.releaseTime,
                        pattern = stringResource(R.string.date_format)
                    ),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            wikiUrl?.let { url ->
                IconButton(
                    modifier = Modifier.size(32.dp),
                    onClick = { onAccessWiki(url) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_link),
                        contentDescription = "Wiki"
                    )
                }
            }
        }
    }
}

@Composable
private fun getVersionComponents(
    version: MinecraftVersion
): Quadruple<Painter?, String, String?, String?> {
    val vmVer = version.version
    val summary = version.summary?.let { stringResource(it) }
    val urlSuffix = version.urlSuffix ?: vmVer.id

    return when (version.type) {
        MinecraftVersion.Type.Release -> {
            Quadruple(
                painterResource(R.drawable.img_minecraft),
                stringResource(R.string.download_game_type_release),
                stringResource(R.string.url_wiki_minecraft_game_release, urlSuffix),
                summary
            )
        }
        MinecraftVersion.Type.Snapshot -> {
            Quadruple(
                painterResource(R.drawable.img_command_block),
                stringResource(R.string.download_game_type_snapshot),
                stringResource(R.string.url_wiki_minecraft_game_snapshot, urlSuffix),
                summary
            )
        }
        MinecraftVersion.Type.AprilFools -> {
            Quadruple(
                painterResource(R.drawable.img_diamond_block),
                stringResource(R.string.download_game_type_april_fools),
                stringResource(R.string.url_wiki_minecraft_game_snapshot, urlSuffix),
                summary
            )
        }
        MinecraftVersion.Type.OldBeta -> {
            Quadruple(
                painterResource(R.drawable.img_old_cobblestone),
                stringResource(R.string.download_game_type_old_beta),
                null,
                summary
            )
        }
        MinecraftVersion.Type.OldAlpha -> {
            Quadruple(
                painterResource(R.drawable.img_old_grass_block),
                stringResource(R.string.download_game_type_old_alpha),
                null,
                summary
            )
        }
        else -> {
            Quadruple(
                null,
                stringResource(R.string.generic_unknown),
                null,
                version.summary?.let { stringResource(it) }
            )
        }
    }
}