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

package com.movtery.zalithlauncher.viewmodel

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.launcherName
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.path.GLOBAL_JSON
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.sponsor.LastMonthSponsors
import com.movtery.zalithlauncher.sponsor.MonthlySponsors
import com.movtery.zalithlauncher.sponsor.Sponsor
import com.movtery.zalithlauncher.sponsor.SponsorPlan
import com.movtery.zalithlauncher.sponsor.SponsorPlanEffect
import com.movtery.zalithlauncher.sponsor.SponsorUser
import com.movtery.zalithlauncher.ui.components.SponsorEffectBox
import com.movtery.zalithlauncher.ui.components.SponsorEffectClock
import com.movtery.zalithlauncher.ui.components.SponsorText
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.components.rememberSponsorEffectClock
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.upgrade.GithubContentApi
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.safeBodyAsJson
import com.movtery.zalithlauncher.utils.network.withRetry
import com.movtery.zalithlauncher.utils.string.decodeBase64
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val TAG = "SponsorsVM"

private const val SPONSORS_FILE = "monthly_sponsors.json"
private const val SPONSORS_API_URL =
    "https://api.github.com/repos/ZalithLauncher/Zalith-Info/contents/$SPONSORS_FILE"

/** 赞助者数据的缓存有效期 */
private val CACHE_TTL = TimeUnit.DAYS.toMillis(1L)

/** 实付金额与按标价应付金额的差值超过半分钱时，视为有优惠 */
private const val DISCOUNT_EPSILON = 0.005

sealed interface DataOperation {
    /** 数据加载中 */
    data object Loading : DataOperation
    /** 当月赞助者 */
    data class Result(val data: MonthlySponsors) : DataOperation
    /** 本月还没有赞助者 */
    data class NoSponsors(val data: MonthlySponsors) : DataOperation
    /** 拉取失败且没有可展示的数据 */
    data object Empty : DataOperation
}

private fun toDataOperation(data: MonthlySponsors?): DataOperation = when {
    data == null -> DataOperation.Empty
    data.sponsors.isEmpty() -> DataOperation.NoSponsors(data)
    else -> DataOperation.Result(data)
}

private fun DataOperation.dataOrNull(): MonthlySponsors? = when (this) {
    is DataOperation.Result -> data
    is DataOperation.NoSponsors -> data
    else -> null
}

sealed interface SponsorsOperation {
    data object None : SponsorsOperation
    /** 展示赞助者对话框 */
    data object Show : SponsorsOperation
    /** 展示上月赞助者结算报告对话框 */
    data object LastMonth : SponsorsOperation
}

@Serializable
private data class SponsorsCache(
    @SerialName("fetchTime")
    val fetchTime: Long,
    @SerialName("data")
    val data: MonthlySponsors
)

class SponsorsViewModel : ViewModel() {
    private val _data = MutableStateFlow<DataOperation>(DataOperation.Loading)
    val data = _data.asStateFlow()

    private val _ui = MutableStateFlow<SponsorsOperation>(SponsorsOperation.None)
    val ui = _ui.asStateFlow()


    private val mutex = Mutex()
    private var cache: MonthlySponsors? = null
    private var cacheTime = 0L

    /**
     * 启动时预拉取数据并刷新缓存
     */
    fun loadOnStart(showLastMonth: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            _data.update { DataOperation.Loading }

            val newData = toDataOperation(ensureData())
            _data.update { newData }
            if (showLastMonth) checkLastMonthReport(newData)
        }
    }

    private var refreshJob: Job? = null
    /**
     * 刷新赞助者列表并更新缓存
     */
    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch(Dispatchers.Main) {
            _data.update { DataOperation.Loading }

            val newData = toDataOperation(refreshData())

            _data.update { newData }
            refreshJob = null
        }
    }

    /**
     * 打开当月赞助者对话框
     */
    fun openDialog() {
        _ui.update { SponsorsOperation.Show }
        //数据此前拉取失败且无缓存时，打开对话框时自动重试
        if (_data.value is DataOperation.Empty) {
            refresh()
        }
    }

    /**
     * 关闭对话框
     */
    fun closeDialog() {
        _ui.update { SponsorsOperation.None }
    }

    /**
     * 关闭上月赞助者结算报告对话框，并写入已结算月份标记
     */
    fun closeLastMonthDialog(settledMonth: String) {
        AllSettings.lastSponsorReportMonth.save(settledMonth)
        _ui.update { SponsorsOperation.None }
    }

    private fun checkLastMonthReport(operation: DataOperation) {
        val lastMonth = operation.dataOrNull()?.lastMonth ?: return
        if (lastMonth.month == AllSettings.lastSponsorReportMonth.getValue()) return

        if (lastMonth.list.isEmpty()) {
            AllSettings.lastSponsorReportMonth.save(lastMonth.month)
        } else {
            _ui.update { SponsorsOperation.LastMonth }
        }
    }

    private fun isFresh(): Boolean {
        return System.currentTimeMillis() - cacheTime < CACHE_TTL
    }

    private suspend fun ensureData(): MonthlySponsors? = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (cache == null) readCache()
            if (cache != null && isFresh()) return@withLock cache

            fetchAndCache() ?: cache
        }
    }

    private suspend fun refreshData(): MonthlySponsors? = withContext(Dispatchers.IO) {
        mutex.withLock {
            fetchAndCache() ?: cache
        }
    }

    /**
     * 拉取远端数据并更新缓存
     */
    private suspend fun fetchAndCache(): MonthlySponsors? {
        return fetchRemote()?.let { data ->
            cache = data
            cacheTime = System.currentTimeMillis()
            runCatching {
                writeCache(data)
            }.onFailure { e ->
                Logger.warning(TAG, "Failed to write sponsors cache!", e)
            }
            data
        }
    }

    private fun readCache() {
        val file = PathManager.FILE_SPONSORS_CACHE
        if (!file.isFile) return

        runCatching {
            GLOBAL_JSON.decodeFromString(SponsorsCache.serializer(), file.readText())
        }.onSuccess { cached ->
            cache = cached.data
            cacheTime = cached.fetchTime
        }.onFailure { e ->
            Logger.warning(TAG, "Failed to read sponsors cache!", e)
        }
    }

    private fun writeCache(data: MonthlySponsors) {
        PathManager.FILE_SPONSORS_CACHE.writeText(
            GLOBAL_JSON.encodeToString(
                SponsorsCache.serializer(),
                SponsorsCache(System.currentTimeMillis(), data)
            )
        )
    }

    private suspend fun fetchRemote(): MonthlySponsors? {
        return withContext(Dispatchers.IO) {
            runCatching {
                withRetry(logTag = "Sponsors", maxRetries = 2) {
                    val api = GLOBAL_CLIENT.get(SPONSORS_API_URL).safeBodyAsJson<GithubContentApi>()
                    //需要Base64解密
                    val contentString = decodeBase64(api.content)
                    GLOBAL_JSON.decodeFromString(MonthlySponsors.serializer(), contentString)
                }
            }.getOrElse { e ->
                Logger.warning(TAG, "Failed to fetch monthly sponsors!", e)
                null
            }
        }
    }
}

@Composable
fun SponsorsDialog(
    viewModel: SponsorsViewModel,
    onSupportClick: () -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val data by viewModel.data.collectAsStateWithLifecycle()

    when (ui) {
        SponsorsOperation.None -> {}
        SponsorsOperation.LastMonth -> {
            when (val report = data.dataOrNull()?.lastMonth) {
                is LastMonthSponsors -> {
                    val effectClock = rememberSponsorEffectClock()
                    val outsideColor = onCardColor()

                    LastMonthReportDialog(
                        report = report,
                        effectClock = effectClock,
                        outsideColor = outsideColor,
                        onSupportClick = onSupportClick,
                        onClose = {
                            viewModel.closeLastMonthDialog(report.month)
                        }
                    )
                }
                else -> {
                    LaunchedEffect(Unit) { viewModel.closeDialog() }
                }
            }
        }
        SponsorsOperation.Show -> {
            val dialogBackground = cardColor(false)
            val outsideColor = onCardColor()

            Dialog(
                onDismissRequest = viewModel::closeDialog,
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .padding(all = 16.dp)
                        .heightIn(max = rememberDialogMaxHeight())
                        .fillMaxHeight()
                        .fillMaxWidth(0.6f),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(all = 6.dp)
                            .fillMaxWidth()
                            .heightIn(max = (maxHeight - 12.dp).coerceAtMost(rememberDialogMaxHeight())),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = dialogBackground,
                        contentColor = onCardColor(),
                        shadowElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.sponsors_dialog_title),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                val launcherName = remember { launcherName() }
                                Text(
                                    modifier = Modifier.alpha(0.7f),
                                    text = stringResource(R.string.about_sponsors_summary, launcherName),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            when (val operation = data) {
                                is DataOperation.Loading -> {
                                    OperationStatusLayout {
                                        CircularProgressIndicator()
                                    }
                                }
                                is DataOperation.Empty,
                                is DataOperation.NoSponsors -> {
                                    OperationStatusLayout {
                                        Text(
                                            text = stringResource(R.string.sponsors_empty),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                is DataOperation.Result -> {
                                    val state = rememberLazyListState()
                                    val effectClock = rememberSponsorEffectClock()
                                    LazyColumn(
                                        modifier = Modifier
                                            .fadeEdge(state = state)
                                            .weight(1f, fill = false),
                                        state = state,
                                        contentPadding = PaddingValues(all = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(operation.data.sponsors) { sponsor ->
                                            SponsorItem(
                                                sponsor = sponsor,
                                                effectClock = effectClock,
                                                outsideColor = outsideColor
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .padding(horizontal = 12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalButton(
                                    modifier = Modifier.weight(0.5f),
                                    onClick = viewModel::refresh,
                                    enabled = data !is DataOperation.Loading,
                                ) {
                                    Text(stringResource(R.string.generic_refresh))
                                }

                                Button(
                                    modifier = Modifier.weight(0.5f),
                                    onClick = viewModel::closeDialog
                                ) {
                                    Text(stringResource(R.string.generic_close))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LastMonthReportDialog(
    report: LastMonthSponsors,
    effectClock: SponsorEffectClock,
    outsideColor: Color,
    onSupportClick: () -> Unit,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .padding(all = 16.dp)
                .heightIn(max = rememberDialogMaxHeight())
                .fillMaxHeight()
                .fillMaxWidth(0.6f),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 6.dp)
                    .fillMaxWidth()
                    .heightIn(max = (maxHeight - 12.dp).coerceAtMost(rememberDialogMaxHeight())),
                shape = MaterialTheme.shapes.extraLarge,
                color = cardColor(false),
                contentColor = onCardColor(),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.sponsors_last_month_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        val launcherName = remember { launcherName() }
                        Text(
                            modifier = Modifier.alpha(0.7f),
                            text = stringResource(R.string.sponsors_last_month_summary, launcherName),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    val state = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier
                            .fadeEdge(state = state)
                            .weight(1f, fill = false),
                        state = state,
                        contentPadding = PaddingValues(all = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(report.list) { sponsor ->
                            SponsorItem(
                                sponsor = sponsor,
                                effectClock = effectClock,
                                outsideColor = outsideColor
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .padding(horizontal = 12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(0.5f),
                            onClick = onSupportClick
                        ) {
                            Text(stringResource(R.string.about_sponsor))
                        }

                        Button(
                            modifier = Modifier.weight(0.5f),
                            onClick = onClose
                        ) {
                            Text(stringResource(R.string.generic_close))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OperationStatusLayout(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
private fun SponsorItem(
    sponsor: Sponsor,
    effectClock: SponsorEffectClock,
    outsideColor: Color,
    modifier: Modifier = Modifier
) {
    val effect = remember(sponsor.plan.id) { SponsorPlanEffect.of(sponsor.plan.id) }
    SponsorEffectBox(
        effect = effect,
        clock = effectClock,
        seed = sponsor.hashCode(),
        outsideColor = outsideColor,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SponsorAvatar(
                modifier = Modifier
                    .size(34.dp)
                    .clip(shape = CircleShape)
                    .align(Alignment.CenterVertically),
                avatar = sponsor.user.avatar
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                SponsorText(
                    text = sponsor.user.name,
                    style = MaterialTheme.typography.bodyMedium
                )
                SponsorText(
                    text = sponsor.time,
                    style = MaterialTheme.typography.bodySmall,
                    alpha = 0.7f
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (sponsor.plan.name.isNotEmpty()) {
                    SponsorText(
                        text = sponsor.plan.name,
                        style = MaterialTheme.typography.bodySmall,
                        alpha = 0.7f
                    )
                }
                SponsorText(
                    text = sponsorAmountText(sponsor.plan),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun SponsorAvatar(
    avatar: String,
    modifier: Modifier = Modifier
) {
    if (avatar.isEmpty()) {
        Image(
            modifier = modifier,
            painter = painterResource(R.drawable.ic_unknown_icon),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    } else {
        AsyncImage(
            modifier = modifier,
            model = avatar,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_unknown_icon),
            error = painterResource(R.drawable.ic_unknown_icon)
        )
    }
}

/**
 * 构建条目金额文本：正常仅显示方案标价（乘以数量）；实付比按标价应付少时，
 * 划线标价并附加实付金额；方案标价缺失时仅显示实付金额
 */
private fun sponsorAmountText(plan: SponsorPlan): AnnotatedString {
    val totalText = formatAmount(plan.totalAmount)
    val price = plan.price ?: return AnnotatedString(totalText)

    val base = buildString {
        append(formatAmount(price))
        if (plan.count > 1) {
            append(" × ").append(plan.count)
        }
    }
    if (price * plan.count - plan.totalAmount > DISCOUNT_EPSILON) {
        return buildAnnotatedString {
            withStyle(style = SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                append(base)
            }
            append(' ')
            append(totalText)
        }
    }
    return AnnotatedString(base)
}

private fun formatAmount(value: Double): String {
    return String.format(Locale.ROOT, "¥%.2f", value)
}

private val sponsorPreviewItems = listOf(
    Sponsor(
        user = SponsorUser(name = "青蓝", avatar = ""),
        time = "2026/10/22",
        plan = SponsorPlan(
            id = "c1e7e62e",
            name = "咬过一口的青金石",
            price = 5.20,
            totalAmount = 5.20,
            showAmount = 5.20
        )
    ),
    Sponsor(
        user = SponsorUser(name = "一位名字特别长的赞助者", avatar = ""),
        time = "2026/10/23",
        plan = SponsorPlan(
            id = "c3a32536",
            name = "红\uD83D\uDC31帽",
            price = 10.45,
            totalAmount = 10.45,
            showAmount = 10.45
        )
    ),
    Sponsor(
        user = SponsorUser(name = "令牌", avatar = ""),
        time = "2026/10/15",
        plan = SponsorPlan(
            id = "c4b309b8",
            name = "闪闪黄金令牌",
            price = 20.0,
            totalAmount = 16.0,
            showAmount = 20.0
        )
    ),
    Sponsor(
        user = SponsorUser(name = "绿星", avatar = ""),
        time = "2026/10/24",
        plan = SponsorPlan(
            id = "",
            name = "自选金额",
            price = 5.0,
            totalAmount = 5.0,
            showAmount = 5.0
        )
    )
)

@PreviewLightDark
@Composable
private fun SponsorItemsPreview() {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceBright) {
            val effectClock = rememberSponsorEffectClock()
            Column(modifier = Modifier.fillMaxWidth()) {
                sponsorPreviewItems.forEach { sponsor ->
                    SponsorItem(
                        sponsor = sponsor,
                        effectClock = effectClock,
                        outsideColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
