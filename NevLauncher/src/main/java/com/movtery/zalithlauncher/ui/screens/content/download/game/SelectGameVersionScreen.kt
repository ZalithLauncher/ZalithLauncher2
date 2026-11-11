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

package com.movtery.zalithlauncher.ui.screens.content.download.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.movtery.zalithlauncher.ui.theme.NevActionButton
import com.movtery.zalithlauncher.ui.theme.NevFilterChip
import com.movtery.zalithlauncher.ui.theme.NevInnerPanel
import com.movtery.zalithlauncher.ui.theme.NevPanelFrame
import com.movtery.zalithlauncher.ui.theme.NevRed
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
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
import com.movtery.zalithlauncher.ui.components.EdgeDirection
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.components.animated
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.rememberAnimatedContext
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
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

    /** Grup rilis yang sedang dibuka (mis. "1.21"); null = tampilkan kartu grup */
    var openGroup by mutableStateOf<String?>(null)

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

/**
 * 选择 Minecraft 版本屏幕的宿主信息，提供可见性判定所需的导航层级
 */
sealed interface SelectGameVersionHost {
    /** 下载游戏流程 */
    data class Download(
        val mainScreenKey: TitledNavKey?,
        val downloadScreenKey: TitledNavKey?,
        val downloadGameScreenKey: TitledNavKey?
    ) : SelectGameVersionHost

    /** 版本设置流程（修改版本） */
    data class VersionSettings(
        val mainScreenKey: TitledNavKey?,
        val versionSettingsScreenKey: TitledNavKey?
    ) : SelectGameVersionHost
}

@Composable
fun SelectGameVersionScreen(
    host: SelectGameVersionHost,
    eventViewModel: EventViewModel,
    onVersionSelect: (MinecraftVersion) -> Unit = {}
) {
    val viewModel = viewModel(
        key = NormalNavKey.DownloadGame.SelectGameVersion.toString()
    ) {
        VersionsViewModel()
    }

    when (host) {
        is SelectGameVersionHost.Download -> BaseScreen(
            levels1 = listOf(
                Pair(NestedNavKey.Download::class.java, host.mainScreenKey),
                Pair(NestedNavKey.DownloadGame::class.java, host.downloadScreenKey)
            ),
            Triple(NormalNavKey.DownloadGame.SelectGameVersion, host.downloadGameScreenKey, false)
        ) { isVisible ->
            SelectGameVersionContent(
                isVisible = isVisible,
                viewModel = viewModel,
                eventViewModel = eventViewModel,
                onVersionSelect = onVersionSelect
            )
        }

        is SelectGameVersionHost.VersionSettings -> BaseScreen(
            levels1 = listOf(
                Pair(NestedNavKey.VersionSettings::class.java, host.mainScreenKey)
            ),
            Triple(NormalNavKey.Versions.ModifyVersion, host.versionSettingsScreenKey, false)
        ) { isVisible ->
            SelectGameVersionContent(
                isVisible = isVisible,
                viewModel = viewModel,
                eventViewModel = eventViewModel,
                onVersionSelect = onVersionSelect
            )
        }
    }
}

@Composable
private fun SelectGameVersionContent(
    isVisible: Boolean,
    viewModel: VersionsViewModel,
    eventViewModel: EventViewModel,
    onVersionSelect: (MinecraftVersion) -> Unit
) {
    val ctx = rememberAnimatedContext(isVisible = isVisible)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .animated(ctx)
    ) {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = false,
            enabled = viewModel.versionState !is VersionState.Loading,
            onRefresh = { viewModel.refresh(true) }
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
                    Column {
                        VersionHeader(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            versionFilter = viewModel.versionFilter,
                            onVersionFilterChange = { viewModel.filterWith(it) },
                            itemContainerColor = cardColor(),
                            itemContentColor = onCardColor(),
                            onRefreshClick = {
                                viewModel.refresh(true)
                            }
                        )

                        VersionBrowser(
                            modifier = Modifier.weight(1f),
                            versions = state.versions,
                            openGroup = viewModel.openGroup,
                            onOpenGroup = { viewModel.openGroup = it },
                            onVersionSelect = onVersionSelect,
                            openLink = { url ->
                                eventViewModel.sendEvent(EventViewModel.Event.OpenLink(url))
                            }
                        )
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
    Column(modifier = modifier.padding(top = 10.dp)) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
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
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, NevRed.OutlineBright.copy(alpha = 0.7f), RoundedCornerShape(50)),
                        value = versionFilter.id,
                        onValueChange = { onVersionFilterChange(versionFilter.copy(id = it)) },
                        color = NevRed.Panel.copy(alpha = 0.72f),
                        contentColor = itemContentColor,
                        contextPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        singleLine = true,
                        hint = {
                            Text(
                                text = stringResource(R.string.generic_search),
                                style = TextStyle(color = itemContentColor).copy(fontSize = 14.sp)
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
    NevFilterChip(
        modifier = modifier,
        text = text,
        selected = selected,
        onClick = onClick
    )
}

@Composable
private fun VersionList(
    modifier: Modifier = Modifier,
    versions: List<MinecraftVersion>,
    onVersionSelect: (MinecraftVersion) -> Unit,
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
                    onVersionSelect(version)
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

/** Grup rilis Minecraft berdasarkan seri versi (mis. 1.21, 1.20, 26). */
private data class VersionGroup(
    val id: String,
    val type: MinecraftVersion.Type,
    /** Teks besar di kartu (mis. "1.21", "2025", "1.7") */
    val key: String,
    /** Nama update resmi (hanya untuk rilis tertentu) */
    val title: String?,
    val versions: List<MinecraftVersion>
) {
    val latest: MinecraftVersion get() = versions.maxBy { it.version.releaseTime }
}

private val GROUP_TITLES = mapOf(
    "26" to "Drop 2026",
    "1.21" to "Tricky Trials",
    "1.20" to "Trails & Tales",
    "1.19" to "The Wild Update",
    "1.18" to "Caves & Cliffs II",
    "1.17" to "Caves & Cliffs I",
    "1.16" to "Nether Update",
    "1.15" to "Buzzy Bees",
    "1.14" to "Village & Pillage",
    "1.13" to "Update Aquatic",
    "1.12" to "World of Color",
    "1.11" to "Exploration Update",
    "1.10" to "Frostburn Update",
    "1.9" to "Combat Update",
    "1.8" to "Bountiful Update",
    "1.6" to "Horse Update",
    "1.5" to "Redstone Update",
    "1.4" to "Pretty Scary Update",
    "1.2" to "Anvil Update",
)

/** Warna aksen kartu grup (bergantian). */
private val GROUP_ACCENTS = listOf(
    Color(0xFF7FE3FF),
    Color(0xFFFFA25C),
    Color(0xFFFF8FC8),
    Color(0xFF8CE99A),
    Color(0xFFB69CFF),
    Color(0xFFFFD866),
)

private fun groupKeyOf(id: String): String {
    val parts = id.split('.')
    val major = parts.firstOrNull()?.toIntOrNull() ?: return id
    return if (major >= 2 || parts.size < 2) parts[0] else "${parts[0]}.${parts[1].takeWhile { it.isDigit() }}"
}

private fun groupKeyOrder(key: String): List<Int> =
    key.split('.').map { it.toIntOrNull() ?: 0 }

private val TYPE_ORDER = listOf(
    MinecraftVersion.Type.Release,
    MinecraftVersion.Type.Snapshot,
    MinecraftVersion.Type.AprilFools,
    MinecraftVersion.Type.OldBeta,
    MinecraftVersion.Type.OldAlpha,
    MinecraftVersion.Type.Unknown,
)

/** Kunci grup (teks besar kartu) untuk satu versi, sesuai tipenya. */
private fun groupKeyFor(version: MinecraftVersion): String {
    val id = version.version.id
    return when (version.type) {
        MinecraftVersion.Type.Release -> groupKeyOf(id)
        //Beta/Alpha lama: "b1.7.3" -> "1.7", "a1.2.6" -> "1.2"
        MinecraftVersion.Type.OldBeta,
        MinecraftVersion.Type.OldAlpha -> {
            val k = groupKeyOf(id.dropWhile { !it.isDigit() })
            //Contoh "rd-132211" / "inf-20100618": pakai awalan saja agar teks kartu tidak kepanjangan
            if (k.length > 6) id.substringBefore('-') else k
        }
        //Snapshot, April Mop, dan lainnya dikelompokkan per tahun rilis
        else -> version.version.releaseTime.take(4).ifEmpty { id }
    }
}

/** Kelompokkan semua versi menjadi kartu seri: per tipe, lalu terbaru dulu. */
private fun List<MinecraftVersion>.toGroups(): List<VersionGroup> {
    return groupBy { it.type to groupKeyFor(it) }
        .map { (pair, list) ->
            val (type, key) = pair
            VersionGroup(
                id = "${type.name}:$key",
                type = type,
                key = key,
                title = if (type == MinecraftVersion.Type.Release) GROUP_TITLES[key] else null,
                versions = list
            )
        }
        .sortedWith { a, b ->
            val ta = TYPE_ORDER.indexOf(a.type)
            val tb = TYPE_ORDER.indexOf(b.type)
            if (ta != tb) return@sortedWith ta.compareTo(tb)
            val ka = groupKeyOrder(a.key)
            val kb = groupKeyOrder(b.key)
            var r = 0
            for (i in 0 until maxOf(ka.size, kb.size)) {
                r = (kb.getOrElse(i) { 0 }).compareTo(ka.getOrElse(i) { 0 })
                if (r != 0) break
            }
            r
        }
}

@Composable
private fun groupTypeName(type: MinecraftVersion.Type): String = stringResource(
    when (type) {
        MinecraftVersion.Type.Release -> R.string.download_game_type_release
        MinecraftVersion.Type.Snapshot -> R.string.download_game_type_snapshot
        MinecraftVersion.Type.AprilFools -> R.string.download_game_type_april_fools
        MinecraftVersion.Type.OldBeta -> R.string.download_game_type_old_beta
        MinecraftVersion.Type.OldAlpha -> R.string.download_game_type_old_alpha
        MinecraftVersion.Type.Unknown -> R.string.download_game_type_old
    }
)

/** Nama kartu: nama update resmi untuk rilis, nama tipe untuk Snapshot/April Mop/Beta/Alpha. */
@Composable
private fun VersionGroup.displayTitle(): String? =
    title ?: if (type == MinecraftVersion.Type.Release) null else groupTypeName(type)

@Composable
private fun VersionBrowser(
    modifier: Modifier = Modifier,
    versions: List<MinecraftVersion>,
    openGroup: String?,
    onOpenGroup: (String?) -> Unit,
    onVersionSelect: (MinecraftVersion) -> Unit,
    openLink: (url: String) -> Unit
) {
    val groups = remember(versions) { versions.toGroups() }
    val opened = openGroup?.let { id -> groups.firstOrNull { it.id == id } }

    BackHandler(enabled = opened != null) { onOpenGroup(null) }

    if (opened != null) {
        VersionGroupDetail(
            modifier = modifier,
            group = opened,
            accent = GROUP_ACCENTS[groups.indexOf(opened).coerceAtLeast(0) % GROUP_ACCENTS.size],
            onClose = { onOpenGroup(null) },
            onVersionSelect = onVersionSelect,
            openLink = openLink
        )
        return
    }

    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    LazyVerticalGrid(
        modifier = modifier,
        state = gridState,
        columns = GridCells.Adaptive(minSize = 200.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        gridItems(groups, key = { "group-${it.id}" }) { group ->
            val index = groups.indexOf(group)
            VersionGroupCard(
                group = group,
                accent = GROUP_ACCENTS[index % GROUP_ACCENTS.size],
                onClick = { onOpenGroup(group.id) }
            )
        }
    }
}

@Composable
private fun VersionGroupCard(
    group: VersionGroup,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .graphicsLayer(scaleX = scale.value, scaleY = scale.value),
        onClick = onClick,
        shape = shape,
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Brush.linearGradient(listOf(accent.copy(alpha = 0.9f), accent.copy(alpha = 0.35f)))
        )
    ) {
        Box(
            modifier = Modifier.background(
                Brush.verticalGradient(
                    listOf(accent.copy(alpha = 0.28f), NevRed.PanelDeep, NevRed.Panel)
                )
            )
        ) {
            //Dekorasi blok Minecraft samar sebagai latar kartu
            Image(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(120.dp)
                    .alpha(0.22f),
                painter = painterResource(R.drawable.img_minecraft),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
            //Peredupan agar teks tetap terbaca
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 16.dp)
            ) {
                Text(
                    text = "MC // ${groupTypeName(group.type).uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = accent.copy(alpha = 0.9f),
                    maxLines = 1
                )
                Text(
                    text = group.key,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent,
                    maxLines = 1
                )
                group.displayTitle()?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .border(1.dp, accent.copy(alpha = 0.8f), RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.download_game_group_count, group.versions.size),
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.Monospace,
                        color = accent,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.download_game_group_latest, group.latest.version.id),
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }
        }
    }
}


/**
 * Detail satu seri rilis: panel info di kiri (kategori, nomor seri, sampul, jumlah versi, tombol Tutup)
 * dan daftar versi bernomor di kanan dengan tombol Pasang.
 */
@Composable
private fun VersionGroupDetail(
    modifier: Modifier = Modifier,
    group: VersionGroup,
    accent: Color,
    onClose: () -> Unit,
    onVersionSelect: (MinecraftVersion) -> Unit,
    openLink: (url: String) -> Unit
) {
    val latestId = group.latest.version.id
    NevPanelFrame(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val wide = maxWidth >= 600.dp
            val list: @Composable (Modifier) -> Unit = { m ->
                val listState = rememberLazyListState()
                LazyColumn(
                    modifier = m.nonInteractiveScrollbar(
                        state = listState.scrollIndicatorState!!,
                        orientation = Orientation.Vertical,
                    ),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(group.versions, key = { _, v -> v.version.id }) { index, version ->
                        VersionNumberedRow(
                            number = index + 1,
                            version = version,
                            accent = accent,
                            isLatest = version.version.id == latestId,
                            onClick = { onVersionSelect(version) },
                            openLink = openLink
                        )
                    }
                }
            }

            if (wide) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VersionGroupInfoPanel(
                        modifier = Modifier
                            .weight(0.38f)
                            .fillMaxHeight(),
                        group = group,
                        accent = accent,
                        onClose = onClose
                    )
                    list(Modifier.weight(0.62f).fillMaxHeight())
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = listOfNotNull(group.key, group.displayTitle()).joinToString("  ·  "),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            maxLines = 1
                        )
                        Text(
                            modifier = Modifier
                                .border(1.dp, NevRed.Outline, RoundedCornerShape(50))
                                .clickable(onClick = onClose)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            text = stringResource(R.string.generic_close),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    list(Modifier.weight(1f).fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun VersionGroupInfoPanel(
    modifier: Modifier = Modifier,
    group: VersionGroup,
    accent: Color,
    onClose: () -> Unit
) {
    NevInnerPanel(modifier = modifier, accent = accent) {
        Text(
            text = stringResource(R.string.download_game_group_category).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            color = accent.copy(alpha = 0.9f),
            maxLines = 1
        )
        Text(
            text = group.key,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            color = accent,
            maxLines = 1
        )
        group.displayTitle()?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        //Sampul seri (placeholder bergradasi + blok Minecraft)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3.2f)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(listOf(accent.copy(alpha = 0.35f), NevRed.PanelDeep))
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 10.dp),
                painter = painterResource(R.drawable.img_minecraft),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = stringResource(R.string.download_game_group_count, group.versions.size),
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Monospace,
            color = accent,
            maxLines = 1
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            modifier = Modifier.alpha(0.8f),
            text = stringResource(R.string.download_game_group_hint),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClose,
            shape = RoundedCornerShape(50),
            color = Color.Transparent,
            border = androidx.compose.foundation.BorderStroke(1.dp, NevRed.OutlineBright.copy(alpha = 0.8f))
        ) {
            Box(
                modifier = Modifier
                    .background(NevRed.SurfaceHigh.copy(alpha = 0.7f))
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.generic_close),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun VersionNumberedRow(
    number: Int,
    version: MinecraftVersion,
    accent: Color,
    isLatest: Boolean,
    onClick: () -> Unit,
    openLink: (url: String) -> Unit
) {
    val (_, _, wikiUrl, _) = getVersionComponents(version)
    val shape = RoundedCornerShape(24.dp)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = shape,
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isLatest) NevRed.OutlineBright else NevRed.Outline.copy(alpha = 0.75f)
        )
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(NevRed.Panel.copy(alpha = 0.8f), NevRed.PanelDeep.copy(alpha = 0.9f))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "%02d".format(number),
                style = MaterialTheme.typography.labelLarge,
                fontFamily = FontFamily.Monospace,
                color = accent
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = version.version.id,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (isLatest) {
                        Text(
                            modifier = Modifier
                                .background(NevRed.Gradient, RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                            text = stringResource(R.string.download_game_group_badge_latest).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = NevRed.OnGradient,
                            maxLines = 1
                        )
                    }
                }
                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = formatDate(
                        input = version.version.releaseTime,
                        pattern = stringResource(R.string.date_format)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace
                )
            }
            wikiUrl?.let { url ->
                IconButton(
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, NevRed.Outline, RoundedCornerShape(50)),
                    onClick = { openLink(url) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_link),
                        contentDescription = "Wiki",
                        tint = NevRed.GradientEnd
                    )
                }
            }
            NevActionButton(
                text = stringResource(R.string.download_game_install_button),
                painter = painterResource(R.drawable.ic_download_2_filled),
                onClick = onClick
            )
        }
    }
}
