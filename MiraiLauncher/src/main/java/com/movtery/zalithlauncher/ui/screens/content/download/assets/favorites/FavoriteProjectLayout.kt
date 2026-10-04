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

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.favorites.FavoriteEntry
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformFilterCode
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsIcon
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.FavoriteToggleLabel
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.ProjectTitleHead
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.formatNumberByLocale

/**
 * 收藏项目条目
 *
 * 远端项目不可用时整卡减淡并展示失效标识，点击不再生效，仍可取消收藏
 */
@Composable
fun FavoriteProjectLayout(
    entry: FavoriteEntry,
    onClick: () -> Unit,
    onUnfavorite: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    influencedByBackground: Boolean = true,
    color: Color = cardColor(influencedByBackground),
    contentColor: Color = onCardColor(),
    blur: Int = AllSettings.backgroundBlur.state
) {
    val context = LocalContext.current
    val project = entry.project
    val remote = entry.remote
    val invalid = entry.invalid

    val title = remote?.platformTitle() ?: project.title
    val description = remote?.platformSummary() ?: project.description
    val author = (remote?.platformAuthors() ?: project.authors).joinToString(", ")
    val iconUrl = remote?.platformIconUrl() ?: project.iconUrl
    val downloads = remote?.platformDownloadCount()
    val modloaders = remote?.platformModLoaders()
    val categories: List<PlatformFilterCode>? = remote?.platformCategories(project.classes)

    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }

    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        shape = shape,
        color = color,
        contentColor = contentColor,
        onClick = onClick.takeIf { !invalid } ?: {}
    ) {
        Row(
            modifier = Modifier
                .backgroundGlass(blur, color, influencedByBackground)
                .padding(all = 8.dp)
                .height(IntrinsicSize.Min)
                .alpha(if (invalid) 0.5f else 1f),
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
                    platform = entry.platform,
                    title = title,
                    author = author.takeIf { it.isNotBlank() },
                    classes = project.classes,
                    reserveAuthor = true
                )

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    //描述，固定两行占位
                    Text(
                        modifier = Modifier.weight(1f),
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        minLines = 2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    //下载量
                    Row(
                        modifier = Modifier.alpha(0.7f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(R.drawable.ic_download_2_outlined),
                            contentDescription = null
                        )
                        Text(
                            text = downloads?.let { formatNumberByLocale(context, it) } ?: "",
                            style = MaterialTheme.typography.labelSmall
                        )
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
                        modloaders?.forEach { modloader ->
                            Text(
                                text = modloader.getDisplayName(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        categories?.forEach { category ->
                            Text(
                                text = stringResource(category.getDisplayName()),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    //失效标识
                    if (invalid) {
                        UnavailableIdentifier()
                    }

                    //取消收藏
                    FavoriteToggleLabel(
                        isFavorite = true,
                        onClick = onUnfavorite
                    )
                }
            }
        }
    }
}

/**
 * 失效标识元素，远端项目已不可用时展示
 */
@Composable
private fun UnavailableIdentifier(
    modifier: Modifier = Modifier
) {
    val text = stringResource(R.string.favorites_item_unavailable)

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                modifier = Modifier.size(12.dp),
                painter = painterResource(R.drawable.ic_block_outlined),
                contentDescription = text
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
            )
        }
    }
}
