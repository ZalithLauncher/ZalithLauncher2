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

package com.movtery.zalithlauncher.ui.screens.content.download.assets.elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformFilterCode
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchData
import com.movtery.zalithlauncher.game.download.assets.utils.ModTranslations
import com.movtery.zalithlauncher.game.download.assets.utils.getMcmodTitle
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.buildAppendedText
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SmallOutlinedEditField
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.formatNumberByLocale
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank

sealed interface SearchAssetsState {
    data object Searching: SearchAssetsState
    data class Success(val page: AssetsPage): SearchAssetsState
    data class Error(val message: AndroidStringText): SearchAssetsState
}

/**
 * 资源搜索结果展示列表
 * @param swapToDownload 跳转到下载详情页
 * @param onNavigatePage 导航到指定页面
 */
@Composable
fun ResultListLayout(
    modifier: Modifier = Modifier,
    classes: PlatformClasses,
    searchState: SearchAssetsState,
    controllerHeight: Dp = 54.dp,
    controllerMinScale: Float = 0.9f,
    controllerMinAlpha: Float = 0.8f,
    onReload: () -> Unit = {},
    onPreviousPage: (pageNumber: Int) -> Unit,
    onNextPage: (pageNumber: Int, isLastPage: Boolean) -> Unit,
    onNavigatePage: (Int) -> Unit,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> }
) {
    when (searchState) {
        is SearchAssetsState.Searching -> {
            Box(
                modifier.padding(all = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                LinearWavyProgressIndicator(
                    modifier = Modifier.width(168.dp),
                    wavelength = 32.dp
                )
            }
        }
        is SearchAssetsState.Success -> {
            val page = searchState.page

            val listState = rememberLazyListState()
            val maxCollapsePx = with(LocalDensity.current) { controllerHeight.toPx() }

            //计算缩放比例，滑动偏移限制在0 ~ maxCollapsePx之间
            val fraction by remember {
                derivedStateOf {
                    val index = listState.firstVisibleItemIndex
                    val offset = listState.firstVisibleItemScrollOffset.toFloat()

                    when {
                        index > 0 -> 1f
                        else -> (offset.coerceIn(0f, maxCollapsePx) / maxCollapsePx)
                    }
                }
            }

            Box(modifier = modifier) {
                ResultList(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 60.dp, bottom = 6.dp),
                    classes = classes,
                    data = page.data,
                    swapToDownload = swapToDownload
                )

                val targetScale = 1f - (1f - controllerMinScale) * fraction
                val animatedScale by animateFloatAsState(targetScale)
                val targetAlpha = 1f - (1f - controllerMinAlpha) * fraction
                val animatedAlpha by animateFloatAsState(targetAlpha)

                Row(
                    modifier = Modifier
                        .height(controllerHeight)
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 6.dp)
                        .alpha(animatedAlpha)
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                            transformOrigin = TransformOrigin(1f, 0f)
                        }
                ) {
                    PageController(
                        modifier = Modifier.padding(end = 6.dp),
                        page = page,
                        onPreviousPage = {
                            onPreviousPage(page.pageNumber)
                        },
                        onNextPage = {
                            onNextPage(page.pageNumber, page.isLastPage)
                        },
                        onNavigatePage = onNavigatePage,
                    )
                }
            }
        }
        is SearchAssetsState.Error -> {
            Box(modifier.padding(all = 12.dp)) {
                ScalingLabel(
                    modifier = Modifier.align(Alignment.Center),
                    text = {
                        AndroidStringText(
                            text = buildAppendedText {
                                append(R.string.download_assets_failed_to_get_result)
                                append(searchState.message)
                            }
                        )
                    },
                    onClick = onReload
                )
            }
        }
    }
}

@Composable
private fun PageController(
    modifier: Modifier = Modifier,
    page: AssetsPage,
    shape: Shape = MaterialTheme.shapes.large,
    influencedByBackground: Boolean = true,
    color: Color = cardColor(influencedByBackground),
    contentColor: Color = onCardColor(),
    blur: Int = AllSettings.backgroundBlur.state,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onNavigatePage: (Int) -> Unit
) {
    var editPageNumber by remember {
        mutableStateOf(false)
    }

    @Composable
    fun PageNumber(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.CenterStart
        ) {
            AnimatedVisibility(
                visible = editPageNumber,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut(),
            ) {
                var number by remember { mutableIntStateOf(page.pageNumber) }
                var numberText by remember { mutableStateOf("${page.pageNumber}") }
                //编辑页码
                SmallOutlinedEditField(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 2.dp)
                        .padding(start = 2.dp, end = 8.dp)
                        .width(72.dp),
                    value = numberText,
                    onValueChange = onValueChange@ { value ->
                        if (page.totalPage <= 0) return@onValueChange
                        val number0 = if (value.isEmptyOrBlank()) {
                            1 //为了编辑体验，留空时视为1
                        } else {
                            value.toIntOrNull() ?: return@onValueChange
                        }
                        number = number0.coerceIn(1, page.totalPage)
                        numberText = if (value.isEmptyOrBlank()) value else number.toString()
                    },
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (number != page.pageNumber) {
                                onNavigatePage(number)
                            }
                            editPageNumber = false
                        }
                    ),
                    singleLine = true
                )
            }

            //页码
            AnimatedVisibility(
                visible = !editPageNumber,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut(),
            ) {
                Text(
                    modifier = Modifier.padding(start = 16.dp),
                    text = "${page.pageNumber} ",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .backgroundGlass(blur, color, influencedByBackground)
                .padding(all = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clickable(enabled = !editPageNumber && page.totalPage > 0) {
                        editPageNumber = true
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                PageNumber(
                    modifier = Modifier.fillMaxHeight()
                )

                Text(
                    modifier = Modifier.padding(end = 16.dp),
                    text = "/ ${page.totalPage}",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            IconButton(
                enabled = page.pageNumber > 1, //不是第一页
                onClick = {
                    onPreviousPage()
                    editPageNumber = false
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_left_rounded),
                    contentDescription = stringResource(R.string.download_assets_result_previous_page)
                )
            }

            IconButton(
                enabled = !page.isLastPage, //不是最后一页
                onClick = {
                    onNextPage()
                    editPageNumber = false
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right_rounded),
                    contentDescription = stringResource(R.string.download_assets_result_next_page)
                )
            }
        }
    }
}

@Composable
private fun ResultList(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
    classes: PlatformClasses,
    data: List<Pair<PlatformSearchData, ModTranslations.McMod?>>,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding
    ) {
        items(data) { (item, mcmod) ->
            val platform = remember(item) { item.platform() }
            val title = remember(item) { item.platformTitle() }
            val description = remember(item) { item.platformDescription() }
            val iconUrl = remember(item) { item.platformIconUrl() }
            val author = remember(item) { item.platformAuthor() }
            val downloads = remember(item) { item.platformDownloadCount() }
            val follows = remember(item) { item.platformFollows() }
            val modloaders = remember(item) { item.platformModLoaders() }
            val categories = remember(item, classes) { item.platformCategories(classes) }

            ResultProjectLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                platform = platform,
                title = mcmod.getMcmodTitle(title, context),
                description = description,
                iconUrl = iconUrl,
                author = author,
                downloads = downloads,
                follows = follows,
                modloaders = modloaders,
                categories = categories?.sortedWith { o1, o2 -> o1.index() - o2.index() },
                onClick = {
                    swapToDownload(platform, item.platformId(), iconUrl)
                }
            )
        }
    }
}

@Composable
fun ResultProjectLayout(
    modifier: Modifier = Modifier,
    platform: Platform,
    title: String,
    description: String,
    classes: PlatformClasses? = null,
    iconUrl: String? = null,
    author: String? = null,
    downloads: Long = 0L,
    follows: Long? = null,
    modloaders: List<PlatformDisplayLabel>? = null,
    categories: List<PlatformFilterCode>? = null,
    shape: Shape = MaterialTheme.shapes.large,
    influencedByBackground: Boolean = true,
    color: Color = cardColor(influencedByBackground),
    contentColor: Color = onCardColor(),
    blur: Int = AllSettings.backgroundBlur.state,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }

    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        shape = shape,
        color = color,
        contentColor = contentColor,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .backgroundGlass(blur, color, influencedByBackground)
                .padding(all = 8.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AssetsIcon(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(10.dp))
                    .align(Alignment.CenterVertically),
                size = 72.dp,
                iconUrl = iconUrl
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ProjectTitleHead(
                    platform = platform,
                    title = title,
                    author = author
                )

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    //描述
                    Text(
                        modifier = Modifier.weight(1f),
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    //下载量、收藏量
                    Column(
                        modifier = Modifier.alpha(0.7f),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                painter = painterResource(R.drawable.ic_download_2_outlined),
                                contentDescription = null
                            )
                            Text(
                                text = formatNumberByLocale(context, downloads),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        follows?.let {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    modifier = Modifier.size(14.dp),
                                    painter = painterResource(R.drawable.ic_favorite_outlined),
                                    contentDescription = null
                                )
                                Text(
                                    text = formatNumberByLocale(context, it),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    //标签栏
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .basicMarquee(Int.MAX_VALUE)
                            .alpha(0.7f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        modloaders?.let {
                            it.forEach { modloader ->
                                Text(
                                    text = modloader.getDisplayName(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        categories?.let {
                            it.forEach { category ->
                                Text(
                                    text = stringResource(category.getDisplayName()),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    //资源的类别
                    if (classes != null) {
                        ClassesIdentifier(classes = classes)
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectTitleHead(
    modifier: Modifier = Modifier,
    platform: Platform,
    title: String,
    author: String?
) {
    //标题栏、作者栏、平台标签
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //标题栏、作者栏
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier
                    .weight(0.6f, fill = false)
                    .basicMarquee(iterations = Int.MAX_VALUE),
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
            )
            author?.let {
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Text(
                    modifier = Modifier
                        .weight(0.4f, fill = false)
                        .alpha(0.7f),
                    text = stringResource(R.string.download_assets_result_authors, it),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
        //平台标签
        PlatformIdentifier(platform = platform)
    }
}