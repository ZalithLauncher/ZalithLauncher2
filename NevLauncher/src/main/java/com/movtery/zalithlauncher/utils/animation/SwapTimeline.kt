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

package com.movtery.zalithlauncher.utils.animation

import com.movtery.zalithlauncher.setting.AllSettings

/**
 * 页面切换的串行时间轴
 */
object SwapTimeline {
    /** 旧页单项退场时长 */
    const val EXIT_ITEM = 70
    /** 旧页单项淡出时长 */
    const val EXIT_ALPHA = 70
    /** 旧页退场相邻项错峰间隔 */
    const val EXIT_STAGGER = 15
    /** 旧页退场错峰封顶 */
    const val EXIT_STAGGER_CAP = 40
    /** 旧页独占退场的总窗口 */
    const val EXIT_WINDOW = EXIT_ITEM + EXIT_STAGGER_CAP
    /** 旧页退场结束到新页进场的间隔 */
    const val GAP = 30



    /** 新页单项位移时长 */
    const val ENTER_ITEM = 350
    /** 新页单项淡入时长 */
    const val ENTER_ALPHA = 200
    /** 新页进场相邻项错峰间隔 */
    const val ENTER_STAGGER = 25
    /** 新页进场错峰封顶 */
    const val ENTER_STAGGER_CAP = 250



    /** 通用内容动画的基准时长 */
    const val CONTENT_BASE = 350
    /** 退场位移距离相对进场位移距离的比例 */
    const val EXIT_DISTANCE_SCALE = 6f / 16f



    /**
     * @return 页面切换动画是否关闭
     */
    fun isSwapClosed(): Boolean = !AllSettings.launcherSwapAnimEnabled.state

    /**
     * @return 速度倍率
     */
    fun speedScale(): Float {
        return (AllSettings.launcherAnimateSpeedLevel.state / 10f).coerceIn(0.1f, 3f)
    }

    /**
     * @return 按速度倍率缩放时长
     */
    fun time(ms: Int): Int = (ms / speedScale()).toInt()

    /**
     * @return 按速度倍率缩放页面切换时间轴时长
     */
    fun swapTime(ms: Int): Int = if (isSwapClosed()) 0 else time(ms)

    /**
     * @return 进场第 [index] 项的错峰延迟
     */
    fun enterStagger(index: Int): Int = swapTime(minOf(index * ENTER_STAGGER, ENTER_STAGGER_CAP))

    /**
     * @return 退场第 [index] 项的错峰延迟
     */
    fun exitStagger(index: Int): Int = swapTime(minOf(index * EXIT_STAGGER, EXIT_STAGGER_CAP))
}


/**
 * 一次页面切换的延迟窗口
 * @param exitDelayMillis 旧页场景层开始淡出前的等待时长
 * @param enterDelayMillis 新页场景层开始淡入前的等待时长
 */
class SwapWindow(
    val exitDelayMillis: Int,
    val enterDelayMillis: Int
)

object SwapClock {
    /** 当前切换段落的起始场景；无进行中的切换时为 null */
    @Volatile
    private var swapInitial: Any? = null

    /** 当前切换段落的目标场景；无进行中的切换时为 null */
    @Volatile
    private var swapTarget: Any? = null

    /** 当前切换中，旧内容完成退场的时刻（ms） */
    @Volatile
    private var exitEndAtMillis: Long = 0

    /** 当前切换中，新内容允许进场的时刻（ms） */
    @Volatile
    var enterAtMillis: Long = 0
        private set

    /**
     * 场景层切换段落变更时打点，计算本次切换的退场/进场延迟
     * @param initial 切换段落的起始场景
     * @param target 切换段落的目标场景
     */
    @Synchronized
    fun record(initial: Any?, target: Any?): SwapWindow {
        val now = System.nanoTime() / 1_000_000
        if (initial != target) {
            // 场景真正发生变更才算一次切换
            // 否则只是段落回稳（如首次组合、切换动画播放完毕）
            val continuing = now < enterAtMillis &&
                    (initial == swapInitial || initial == swapTarget ||
                            target == swapInitial || target == swapTarget)
            if (!continuing) {
                swapInitial = initial
                swapTarget = target
                exitEndAtMillis = now + SwapTimeline.swapTime(SwapTimeline.EXIT_WINDOW)
                enterAtMillis = exitEndAtMillis + SwapTimeline.swapTime(SwapTimeline.GAP)
            }
        }
        return SwapWindow(
            exitDelayMillis = (exitEndAtMillis - now).coerceAtLeast(0).toInt(),
            enterDelayMillis = (enterAtMillis - now).coerceAtLeast(0).toInt()
        )
    }
}
