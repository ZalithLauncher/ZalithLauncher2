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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.assets.utils.ModTranslations
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.ShimmerBox
import com.movtery.zalithlauncher.ui.components.rememberMaxHeight
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.formatNumberByLocale
import com.movtery.zalithlauncher.utils.getTimeAgo
import com.movtery.zalithlauncher.utils.isChinese
import com.movtery.zalithlauncher.utils.string.isNotEmptyOrBlank
import org.jackhuang.hmcl.util.versioning.GameVersionNumber

sealed interface DownloadAssetsState<T> {
    class Getting<T> : DownloadAssetsState<T>
    data class Success<T>(val result: T) : DownloadAssetsState<T>
    data class Error<T>(val message: AndroidStringText) : DownloadAssetsState<T>
}

sealed interface DownloadAssetsVersionLoading {
    data object None: DownloadAssetsVersionLoading
    /** 开始加载分页数据 */
    data object StartLoadPage: DownloadAssetsVersionLoading
    /** 加载分页数据 */
    data class LoadingPage(val chunk: Int, val page: Int): DownloadAssetsVersionLoading
    /** 正在加载并缓存依赖项目 */
    data object LoadingDepProject: DownloadAssetsVersionLoading
}

/**
 * 版本、模组加载器 版本信息分组
 */
class VersionInfoMap(
    val gameVersion: String,
    val loader: PlatformDisplayLabel?,
    val versions: List<PlatformVersion>,
    val isAdapt: Boolean
)

suspend fun <E: PlatformVersion> List<E>.initAllGeneric(
    currentProjectId: String,
    also: suspend (E) -> Unit = {}
): List<E> {
    return mapNotNull { version ->
        if (!version.initFile(currentProjectId)) return@mapNotNull null
        version.also {
            also(it)
        }
    }.sortedByDescending {
        //排序：最新的版本在前
        it.platformDatePublished()
    }
}

/**
 * 初始化全部版本数据，并筛选出成功初始化的所有版本
 */
suspend fun List<PlatformVersion>.initAll(
    currentProjectId: String,
    also: suspend (PlatformVersion) -> Unit = {}
): List<PlatformVersion> {
    return initAllGeneric<PlatformVersion>(currentProjectId, also)
}

fun List<PlatformVersion>.mapWithVersions(classes: PlatformClasses): List<VersionInfoMap> {
    val grouped = mutableMapOf<Pair<String, PlatformDisplayLabel?>, MutableList<PlatformVersion>>()

    forEach { version ->
        val labels = version.platformLoaders().ifEmpty { listOf(null) }
        version.platformGameVersion().forEach { gameVer ->
            labels.forEach { loaderLabel ->
                grouped.getOrPut(gameVer to loaderLabel) { mutableListOf() } += version
            }
        }
    }

    val currentVersion = VersionsManager.currentVersion.value

    return grouped.map { (key, versions) ->
        VersionInfoMap(
            gameVersion = key.first,
            loader = key.second,
            versions = versions,
            isAdapt = when (classes) {
                PlatformClasses.MOD_PACK -> false //整合包将作为单独的版本下载，不再需要与现有版本进行匹配
                else -> isVersionAdapt(currentVersion, key.first, key.second)
            }
        )
    }.sortedByVersionAndLoader()
}

private fun List<VersionInfoMap>.sortedByVersionAndLoader(): List<VersionInfoMap> {
    return sortedWith { a, b ->
        // 比较版本号
        val versionCompare = -GameVersionNumber.compare(a.gameVersion, b.gameVersion)
        if (versionCompare != 0) {
            versionCompare
        } else {
            when {
                a.loader == null && b.loader == null -> 0
                a.loader == null -> 1
                b.loader == null -> -1
                else -> a.loader.getDisplayName().compareTo(b.loader.getDisplayName())
            }
        }
    }
}

/**
 * 当前资源版本是否与当前选择的游戏版本匹配
 */
private fun isVersionAdapt(
    currentVersion: Version?,
    gameVersion: String,
    loader: PlatformDisplayLabel?
): Boolean {
    return if (currentVersion == null) {
        false //没安装版本，无法判断
    } else {
        if (currentVersion.getVersionInfo()?.minecraftVersion != gameVersion) {
            false //游戏版本不匹配
        } else {
            //判断模组加载器匹配情况
            val loaderInfo = currentVersion.getVersionInfo()?.loaderInfo
            when {
                loader == null -> true //资源没有模组加载器信息，直接判定适配
                loaderInfo == null -> false //资源有模组加载器，但当前版本没有模组加载器信息，不适配
                else -> loaderInfo.loader.displayName.equals(loader.getDisplayName(), true)
            }
        }
    }
}

/**
 * 资源版本分组可折叠列表
 * @param trailingContent 版本行尾插槽（如收藏按钮），按行注入；为 null 时版本行保持原布局
 */
@Composable
fun AssetsVersionItemLayout(
    modifier: Modifier = Modifier,
    infoMap: VersionInfoMap,
    maxListHeight: Dp = rememberMaxHeight(),
    shape: Shape = MaterialTheme.shapes.large,
    influencedByBackground: Boolean = true,
    color: Color = cardColor(influencedByBackground),
    contentColor: Color = onCardColor(),
    blur: Int = AllSettings.backgroundBlur.state,
    trailingContent: (@Composable (PlatformVersion) -> Unit)? = null,
    onItemClicked: (PlatformVersion) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .backgroundGlass(blur, color, influencedByBackground)
        ) {
            AssetsVersionHeadLayout(
                modifier = Modifier.fillMaxWidth(),
                infoMap = infoMap,
                isAdapt = infoMap.isAdapt,
                expanded = expanded,
                onClick = { expanded = !expanded }
            )

            if (infoMap.versions.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(animationSpec = getAnimateTween()),
                        exit = shrinkVertically(animationSpec = getAnimateTween()) + fadeOut(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = maxListHeight)
                                .padding(vertical = 4.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(infoMap.versions) { version ->
                                AssetsVersionListItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 4.dp),
                                    version = version,
                                    trailingContent = trailingContent,
                                    onClick = {
                                        onItemClicked(version)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssetsVersionHeadLayout(
    modifier: Modifier = Modifier,
    infoMap: VersionInfoMap,
    isAdapt: Boolean,
    expanded: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = infoMap.gameVersion,
                style = MaterialTheme.typography.titleSmall
            )
            infoMap.loader?.let { loader ->
                LittleTextLabel(
                    text = loader.getDisplayName(),
                    shape = MaterialTheme.shapes.small
                )
            }
        }
        if (isAdapt) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.drawable.ic_star_filled),
                contentDescription = null
            )
        }
        if (!infoMap.versions.isEmpty()) {
            Row(
                modifier = Modifier.padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val rotation by animateFloatAsState(
                    targetValue = if (expanded) -180f else 0f,
                    animationSpec = getAnimateTween()
                )
                Icon(
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation),
                    painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                    contentDescription = stringResource(if (expanded) R.string.generic_expand else R.string.generic_collapse)
                )
            }
        }
    }
}

@Composable
private fun AssetsVersionListItem(
    modifier: Modifier = Modifier,
    version: PlatformVersion,
    trailingContent: (@Composable (PlatformVersion) -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //直观的版本状态
        val releaseType = remember { version.platformReleaseType() }
        val displayName = remember { version.platformDisplayName() }
        val downloadCount = remember { version.platformDownloadCount() }
        val date = remember { version.platformDatePublished() }

        Box(
            modifier = Modifier
                .padding(start = 12.dp, end = 8.dp)
                .size(34.dp)
                .clip(shape = CircleShape)
                .background(releaseType.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = releaseType.name.take(1),
                style = MaterialTheme.typography.labelLarge,
                color = releaseType.color
            )
        }

        //版本简要信息
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.labelLarge
            )

            val context = LocalContext.current

            Row(
                modifier = Modifier.alpha(0.7f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                //下载量
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
                        text = formatNumberByLocale(context, downloadCount),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                //更新时间
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(R.drawable.ic_autorenew),
                        contentDescription = null
                    )
                    Text(
                        text = getTimeAgo(
                            context = LocalContext.current,
                            pastInstant = date
                        ),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                //版本状态
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(R.drawable.ic_package_2_outlined),
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(releaseType.textRes),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        //行尾插槽（如收藏按钮），为 null 时不占位
        trailingContent?.invoke(version)
    }
}

/**
 * 项目相关链接UI
 */
@Composable
fun ProjectUrlsContent(
    platform: Platform,
    urls: PlatformProject.Urls,
    mcmod: ModTranslations.McMod?,
    mod: ModTranslations,
    openLink: (String) -> Unit,
) {
    urls.projectUrl?.takeIf { it.isNotEmptyOrBlank() }?.let { url ->
        IconTextButton(
            onClick = { openLink(url) },
            iconSize = 18.dp,
            painter = when (platform) {
                Platform.CURSEFORGE -> painterResource(R.drawable.img_platform_curseforge)
                Platform.MODRINTH -> painterResource(R.drawable.img_platform_modrinth)
            },
            text = stringResource(R.string.download_assets_project_link)
        )
    }
    urls.sourceUrl?.takeIf { it.isNotEmptyOrBlank() }?.let { url ->
        IconTextButton(
            onClick = { openLink(url) },
            iconSize = 18.dp,
            painter = painterResource(R.drawable.ic_code),
            text = stringResource(R.string.download_assets_source_link)
        )
    }
    urls.issuesUrl?.takeIf { it.isNotEmptyOrBlank() }?.let { url ->
        IconTextButton(
            onClick = { openLink(url) },
            iconSize = 18.dp,
            painter = painterResource(R.drawable.ic_chat_info),
            text = stringResource(R.string.download_assets_issues_link)
        )
    }
    urls.wikiUrl?.takeIf { it.isNotEmptyOrBlank() }?.let { url ->
        IconTextButton(
            onClick = { openLink(url) },
            iconSize = 18.dp,
            painter = painterResource(R.drawable.ic_import_contacts_outlined),
            text = stringResource(R.string.download_assets_wiki_link)
        )
    }
    val context = LocalContext.current
    mcmod?.takeIf {
        isChinese(context)
    }?.let {
        mod.getMcmodUrl(it)
    }?.takeIf {
        it.isNotEmptyOrBlank()
    }?.let { url ->
        IconTextButton(
            onClick = { openLink(url) },
            iconSize = 18.dp,
            painter = painterResource(R.drawable.ic_link),
            text = "MC 百科" //品牌名不需要翻译，硬编码
        )
    }
}

/**
 * 屏幕截图与描述UI
 */
@Composable
fun ScreenshotItemLayout(
    screenshot: PlatformProject.Screenshot,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
) {
    val context = LocalContext.current

    val imageRequest = remember(screenshot) {
        screenshot.imageUrl.takeIf { it.isNotBlank() }?.let {
            ImageRequest.Builder(context)
                .data(it)
                .crossfade(true)
                .build()
        }
    }

    val painter = rememberAsyncImagePainter(
        model = imageRequest,
        placeholder = null,
        error = painterResource(R.drawable.ic_unknown_icon)
    )

    val state by painter.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (state) {
            AsyncImagePainter.State.Empty -> {
                //NONE
            }
            is AsyncImagePainter.State.Error, is AsyncImagePainter.State.Loading -> {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(shape)
                )
            }
            is AsyncImagePainter.State.Success -> {
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(painter.intrinsicSize.width / painter.intrinsicSize.height)
                        .clip(shape)
                )
            }
        }

        //标题与简介部分
        if (screenshot.title != null && screenshot.title == screenshot.description) {
            //标题与简介内容相同，则不需要两个都显示
            //会有作者喜欢把标题与简介设置成一样的内容
            Text(
                text = screenshot.title,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        } else {
            screenshot.title?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )
            }
            screenshot.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}