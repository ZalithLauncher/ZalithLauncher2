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

package com.movtery.zalithlauncher.ui.screens.game.elements

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 纵轴分割段数 */
private const val FPS_SEGMENTS = 5

/** 纵轴刻度步长的候选"好数"，贴合帧率语境（1/2/5/10/15/20/30/60/120…） */
private val FPS_AXIS_STEPS = intArrayOf(1, 2, 5, 10, 15, 20, 30, 60, 120, 240)

/**
 * 计算纵轴的"好数"范围：均分为5段后每个刻度都落在规整数字上，
 * 完整覆盖实际帧率范围，两端尽量对称地留出余量，且最低刻度不小于0
 */
private fun niceFpsBounds(fpsMin: Int, fpsMax: Int): Pair<Int, Int> {
    val range = (fpsMax - fpsMin).coerceAtLeast(0)
    for (step in FPS_AXIS_STEPS) {
        val span = step * FPS_SEGMENTS
        if (span < range) continue
        val extra = span - range
        //向下取整到步长网格（余量为负时向负无穷取整）
        val raw = fpsMin - extra / 2
        var axisMin = raw / step * step
        if (raw < 0 && raw % step != 0) axisMin -= step
        var axisMax = axisMin + span
        if (axisMax < fpsMax) {
            axisMax = (fpsMax + step - 1) / step * step
            axisMin = axisMax - span
        }
        if (axisMin < 0) {
            axisMin = 0
            axisMax = span
        }
        return axisMin to axisMax
    }
    return fpsMin to fpsMax
}

/** 纵轴标注字号：尽可能小但可读 */
private val FpsLabelStyle = TextStyle(fontSize = 9.sp)

/** 纵轴标注框高度，数字在其中垂直居中，使中心与对应分割线对齐 */
private val FpsLabelHeight = 12.dp

/**
 * 游戏内帧率历史图表：
 * 纵轴为帧率（范围向外取整为"好数"后分5段标注刻度），
 * 横轴为时间（最多记录15个时间节点，不显示具体时间）；
 * 最后一个点旁标注当前帧率
 */
@Composable
fun FpsChart(
    history: List<Int>,
    fpsMax: Int,
    fpsMin: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val lineColor = primaryColor.copy(alpha = 0.3f)
    val labelColor = LocalContentColor.current
    val textMeasurer = rememberTextMeasurer()
    //纵轴刻度数字：范围向外取整为"好数"后均分5段（含两端）
    val bounds = remember(fpsMax, fpsMin) { niceFpsBounds(fpsMin, fpsMax) }
    val ticks = remember(bounds) {
        val step = (bounds.second - bounds.first) / FPS_SEGMENTS
        (0..FPS_SEGMENTS).map { bounds.first + step * it }
    }

    Row(
        modifier = modifier
            .width(180.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        //纵轴帧率标注：最高帧在上，最低帧在下
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            ticks.reversed().forEach { tick ->
                Box(
                    modifier = Modifier.height(FpsLabelHeight),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = tick.toString(),
                        style = FpsLabelStyle,
                        color = LocalContentColor.current
                    )
                }
            }
        }

        Spacer(Modifier.width(4.dp))

        //帧率曲线与分割线
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            drawFpsChart(
                history = history,
                axisMin = bounds.first,
                axisMax = bounds.second,
                curveColor = primaryColor,
                lineColor = lineColor,
                labelColor = labelColor,
                textMeasurer = textMeasurer
            )
        }
    }
}

/**
 * 绘制帧率图表：每个刻度一条水平分割线，坐标区左缘以纵轴分割线与数字分隔，
 * 帧率数值对应的点按纵轴范围（好数边界）映射，相邻点之间用带轻微圆滑度的折线相连，
 * 最后一个点旁标注它所代表的帧率
 */
private fun DrawScope.drawFpsChart(
    history: List<Int>,
    axisMin: Int,
    axisMax: Int,
    curveColor: Color,
    lineColor: Color,
    labelColor: Color,
    textMeasurer: TextMeasurer
) {
    val pointRadius = 1.25.dp.toPx()
    val gridStroke = 1.dp.toPx()
    //横向内缩半个点加半条分割线，避免曲线与点被边缘裁切；
    //纵向内缩半个标注框，使刻度数字中心与分割线对齐
    val insetX = pointRadius + gridStroke / 2f
    val insetY = FpsLabelHeight.toPx() / 2f
    val axisRange = axisMax - axisMin

    //水平分割线：每个刻度一条，从纵轴延伸到右缘
    for (k in 0..FPS_SEGMENTS) {
        val fraction = k / FPS_SEGMENTS.toFloat()
        val y = insetY + fraction * (size.height - insetY * 2)
        drawLine(
            color = lineColor,
            start = Offset(insetX, y),
            end = Offset(size.width, y),
            strokeWidth = gridStroke
        )
    }
    //纵轴分割线：坐标区与数字的交界
    drawLine(
        color = lineColor,
        start = Offset(insetX, insetY),
        end = Offset(insetX, size.height - insetY),
        strokeWidth = gridStroke
    )

    if (history.isEmpty()) return

    //点在全宽范围内均匀分布，帧率映射到纵轴（好数边界为轴范围）
    val points = history.mapIndexed { index, fps ->
        val x = if (history.size == 1) {
            (insetX + size.width) / 2f
        } else {
            insetX + index * (size.width - insetX * 2) / (history.size - 1)
        }
        val y = insetY + (1f - (fps - axisMin).toFloat() / axisRange) * (size.height - insetY * 2)
        Offset(x, y)
    }

    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        //控制点沿水平方向偏移一小段距离，形成轻微圆滑的过渡
        for (i in 1 until points.size) {
            val start = points[i - 1]
            val end = points[i]
            val offset = (end.x - start.x) * 0.25f
            cubicTo(
                x1 = start.x + offset,
                y1 = start.y,
                x2 = end.x - offset,
                y2 = end.y,
                x3 = end.x,
                y3 = end.y
            )
        }
    }
    drawPath(
        path = path,
        color = curveColor,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )
    points.forEach { point ->
        drawCircle(color = curveColor, radius = pointRadius, center = point)
    }

    //当前帧率标注：放在最后一个点的上/下方，避开来向线条，且不越过上下刻度线
    val last = points.last()
    val measured = textMeasurer.measure(text = history.last().toString(), style = FpsLabelStyle)
    val gap = 2.dp.toPx()
    val canBelow = last.y + gap + measured.size.height <= size.height - insetY
    val canAbove = last.y - gap - measured.size.height >= insetY
    val prevY = points.getOrNull(points.size - 2)?.y
    val placeBelow = when {
        canBelow && !canAbove -> true
        !canBelow && canAbove -> false
        prevY != null && prevY < last.y -> true
        prevY != null && prevY > last.y -> false
        else -> last.y <= size.height / 2f
    }
    val topLeft = if (placeBelow) {
        Offset(size.width - measured.size.width, last.y + gap)
    } else {
        Offset(size.width - measured.size.width, last.y - gap - measured.size.height)
    }
    drawText(textLayoutResult = measured, color = labelColor, topLeft = topLeft)
}
