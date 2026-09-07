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

package com.movtery.zalithlauncher.ui.screens.content.download.assets.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.favorites.AssetFavorite
import com.movtery.zalithlauncher.game.download.assets.favorites.hasFixedVersion
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformReleaseType
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsIcon
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.ClassesIdentifier
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.FavoriteToggleButton
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.PlatformIdentifier
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween

/**
 * 收藏列表中同一项目分组合并后的数据
 * @param project 项目收藏记录（仅收藏了版本而未收藏项目本身时为 null）
 * @param versions 该项目下已收藏的版本列表
 */
data class FavoriteGroup(
    val project: AssetFavorite?,
    val versions: List<AssetFavorite>
) {
    val platform get() = (project ?: versions.first()).platform
    val classes get() = (project ?: versions.first()).classes
    val projectId get() = (project ?: versions.first()).projectId
    val title get() = (project ?: versions.first()).title
    val author get() = (project ?: versions.first()).author
    val description get() = (project ?: versions.first()).description
    val iconUrl get() = (project ?: versions.first()).iconUrl

    /** 组内最近一次收藏时间，作为列表排序依据 */
    val latestSavedAt: Long get() = (listOfNotNull(project) + versions).maxOf { it.savedAt }
}

/**
 * 收藏列表卡片：头部展示项目信息，可展开版本子列表逐条操作（下载/复制直链/移除）
 * @param group 同一项目聚合后的收藏组
 * @param modifier 修饰符
 * @param shape 卡片形状
 * @param influencedByBackground 是否受启动器背景影响（毛玻璃穿透）
 * @param color 卡片颜色
 * @param contentColor 卡片内容颜色
 * @param blur 背景模糊强度
 * @param onClick 点击卡片本体，跳转资源详情页
 * @param onDownloadVersion 下载指定版本收藏（先在线重取，失败由调用方回退快照）
 * @param onCopyLink 复制指定版本收藏的下载直链
 * @param onRemoveVersion 移除指定版本收藏
 * @param onRemoveAll 请求删除该项目组的全部收藏（需经确认对话框）
 */
@Composable
fun FavoritesCard(
    group: FavoriteGroup,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    influencedByBackground: Boolean = true,
    color: Color = cardColor(influencedByBackground),
    contentColor: Color = onCardColor(),
    blur: Int = AllSettings.backgroundBlur.state,
    onClick: () -> Unit = {},
    onDownloadVersion: (AssetFavorite) -> Unit = {},
    onCopyLink: (AssetFavorite) -> Unit = {},
    onRemoveVersion: (AssetFavorite) -> Unit = {},
    onRemoveAll: () -> Unit = {}
) {
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }

    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        shape = shape,
        color = color,
        contentColor = contentColor,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .backgroundGlass(blur, color, influencedByBackground)
                .padding(all = 8.dp)
        ) {
            //卡片头部：图标 + 项目信息 + 收藏/溢出菜单按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AssetsIcon(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(10.dp))
                        .align(Alignment.CenterVertically),
                    size = 64.dp,
                    iconUrl = group.iconUrl
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    //标题 + 作者
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MarqueeText(
                            modifier = Modifier.weight(1f, fill = false),
                            text = group.title,
                            style = MaterialTheme.typography.titleSmall
                        )
                        group.author?.let { author ->
                            VerticalDivider(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                modifier = Modifier.alpha(0.7f),
                                text = stringResource(R.string.download_assets_result_authors, author),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    //描述
                    group.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    //标识行：类别 + 平台 + 版本计数/无版本徽标 + 展开箭头
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClassesIdentifier(classes = group.classes)
                        PlatformIdentifier(platform = group.platform)
                        if (group.versions.isNotEmpty()) {
                            LittleTextLabel(
                                text = stringResource(
                                    R.string.download_favorites_version_count,
                                    group.versions.size
                                )
                            )
                        } else {
                            //仅收藏了项目本身，未绑定任何版本
                            LittleTextLabel(
                                text = stringResource(R.string.download_favorites_no_version)
                            )
                        }
                        if (group.versions.isNotEmpty()) {
                            //展开/收起版本子列表（紧跟版本计数徽标）
                            val rotation by animateFloatAsState(
                                targetValue = if (expanded) -180f else 0f,
                                animationSpec = getAnimateTween()
                            )
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .rotate(rotation),
                                    painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                                    contentDescription = stringResource(
                                        if (expanded) R.string.generic_collapse
                                        else R.string.generic_expand
                                    )
                                )
                            }
                        }
                    }
                }

                //心形按钮：删除该项目组的全部收藏（由调用方弹确认对话框）
                FavoriteToggleButton(
                    isFavorite = true,
                    onToggle = onRemoveAll
                )
            }

            //版本子列表：每条版本收藏一行
            AnimatedVisibility(
                visible = expanded && group.versions.isNotEmpty(),
                enter = expandVertically(getAnimateTween()),
                exit = shrinkVertically(getAnimateTween()) + fadeOut(getAnimateTween())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    group.versions.forEach { version ->
                        FavoriteVersionItem(
                            favorite = version,
                            onDownload = { onDownloadVersion(version) },
                            onCopyLink = { onCopyLink(version) },
                            onRemove = { onRemoveVersion(version) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 版本子列表中的单个版本收藏行
 * @param favorite 版本收藏项
 * @param onDownload 下载该版本
 * @param onCopyLink 复制该版本下载直链
 * @param onRemove 移除该版本收藏
 * @param modifier 修饰符
 */
@Composable
private fun FavoriteVersionItem(
    favorite: AssetFavorite,
    onDownload: () -> Unit,
    onCopyLink: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.medium)
            .padding(all = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //发布类型徽标
        val releaseType = favorite.releaseType ?: PlatformReleaseType.RELEASE
        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .size(28.dp)
                .clip(shape = CircleShape)
                .background(releaseType.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = releaseType.name.take(1),
                style = MaterialTheme.typography.labelMedium,
                color = releaseType.color
            )
        }

        //版本信息
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = favorite.versionName ?: favorite.versionFileName ?: favorite.title,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val summary = listOf(
                favorite.gameVersions.joinToString(", "),
                favorite.loaders.joinToString(", ")
            ).filter { it.isNotBlank() }.joinToString(" | ")
            if (summary.isNotBlank()) {
                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = summary,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        //下载该版本（无固定版本/直链的快照不可下载）
        IconButton(
            onClick = onDownload,
            enabled = favorite.hasFixedVersion
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_download),
                contentDescription = stringResource(R.string.generic_download)
            )
        }
        //复制下载直链（快照无直链时禁用）
        IconButton(
            onClick = onCopyLink,
            enabled = favorite.downloadUrl.isNotBlank()
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_link),
                contentDescription = stringResource(R.string.download_favorites_copy_link)
            )
        }
        //移除该版本收藏
        IconButton(onClick = onRemove) {
            Icon(
                painter = painterResource(R.drawable.ic_delete_outlined),
                contentDescription = stringResource(R.string.download_favorites_remove)
            )
        }
    }
}
