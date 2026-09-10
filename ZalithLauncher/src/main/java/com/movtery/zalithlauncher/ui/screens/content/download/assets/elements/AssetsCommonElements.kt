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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.modpack.platform.PackPlatform
import com.movtery.zalithlauncher.ui.components.ShimmerBox
import com.movtery.zalithlauncher.utils.logging.Logger

private const val TAG = "AssetsCommonElements"

/**
 * 本地已安装标识
 */
@Composable
fun InstalledModBadge(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.tertiary,
    checkColor: Color = MaterialTheme.colorScheme.onTertiary,
    size: Dp = 14.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(size * 0.7f),
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            tint = checkColor
        )
    }
}

/**
 * 平台标识元素，展示平台Logo + 平台名称
 */
@Composable
fun PlatformIdentifier(
    modifier: Modifier = Modifier,
    platform: Platform,
    iconSize: Dp = 12.dp,
    color: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = MaterialTheme.colorScheme.onTertiary,
    shape: Shape = MaterialTheme.shapes.large,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
) {
    BasicIdentifier(
        painter = painterResource(platform.getDrawable()),
        text = platform.displayName,
        modifier = modifier,
        iconSize = iconSize,
        color = color,
        contentColor = contentColor,
        shape = shape,
        textStyle = textStyle,
    )
}

/**
 * 获取平台的LOGO
 */
fun Platform.getDrawable() = when (this) {
    Platform.CURSEFORGE -> R.drawable.img_platform_curseforge
    Platform.MODRINTH -> R.drawable.img_platform_modrinth
}

/**
 * 资源类型标识元素
 */
@Composable
fun ClassesIdentifier(
    classes: PlatformClasses,
    modifier: Modifier = Modifier,
    iconSize: Dp = 12.dp,
    color: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = MaterialTheme.colorScheme.onTertiary,
    shape: Shape = MaterialTheme.shapes.large,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
) {
    BasicIdentifier(
        painter = painterResource(classes.getDrawable()),
        text = stringResource(classes.getText()),
        modifier = modifier,
        iconSize = iconSize,
        color = color,
        contentColor = contentColor,
        shape = shape,
        textStyle = textStyle,
    )
}

private fun PlatformClasses.getDrawable() = when (this) {
    PlatformClasses.MOD -> R.drawable.ic_extension_outlined
    PlatformClasses.MOD_PACK -> R.drawable.ic_package_2_outlined
    PlatformClasses.RESOURCE_PACK -> R.drawable.ic_format_paint_outlined
    PlatformClasses.SAVES -> R.drawable.ic_public
    PlatformClasses.SHADERS -> R.drawable.ic_lightbulb
}

private fun PlatformClasses.getText() = when (this) {
    PlatformClasses.MOD -> R.string.download_category_mod
    PlatformClasses.MOD_PACK -> R.string.download_category_modpack
    PlatformClasses.RESOURCE_PACK -> R.string.download_category_resource_pack
    PlatformClasses.SAVES -> R.string.download_category_saves
    PlatformClasses.SHADERS -> R.string.download_category_shaders
}

@Composable
private fun BasicIdentifier(
    painter: Painter,
    text: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 12.dp,
    color: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = MaterialTheme.colorScheme.onTertiary,
    shape: Shape = MaterialTheme.shapes.large,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = shape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painter,
                contentDescription = text
            )
            Text(
                text = text,
                style = textStyle
            )
        }
    }
}

/**
 * 资源封面网络图标
 * @param iconUrl 图标链接
 */
@Composable
fun AssetsIcon(
    modifier: Modifier = Modifier,
    size: Dp,
    iconUrl: String? = null,
    colorFilter: ColorFilter? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val pxSize = with(density) { size.roundToPx() }

    val imageRequest = remember(iconUrl, pxSize) {
        iconUrl?.takeIf { it.isNotBlank() }?.let {
            ImageRequest.Builder(context)
                .data(it)
                .size(pxSize) //固定大小
                .listener(
                    onError = { _, result -> Logger.warning(TAG, "Coil: error = ${result.throwable}") }
                )
                .crossfade(true)
                .build()
        }
    }

    //预加载
    LaunchedEffect(imageRequest) {
        imageRequest?.let { context.imageLoader.enqueue(it) }
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
                colorFilter = colorFilter
            )
        }
    }
}

/**
 * 整合包格式标识元素，展示格式图标 + 格式名称
 *
 * 以Image展示图标
 */
@Composable
fun PackIdentifier(
    modifier: Modifier = Modifier,
    platform: PackPlatform,
    iconSize: Dp = 12.dp,
    color: Color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.1f),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = MaterialTheme.shapes.large,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = shape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Image(
                modifier = Modifier.size(iconSize),
                painter = platform.getIcon(),
                contentDescription = platform.getText()
            )
            Text(
                text = platform.getText(),
                style = textStyle
            )
        }
    }
}