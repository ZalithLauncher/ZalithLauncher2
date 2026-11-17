package com.movtery.guide

import androidx.compose.ui.geometry.Rect

/**
 * 引导流的运行状态
 */
sealed interface GuideState {
    /**
     * 未激活
     */
    data object Idle : GuideState

    /**
     * 激活中，[entry] 为当前步骤
     */
    data class Active(
        val index: Int,
        val entry: GuideEntry,

        /**
         * 当前步骤锚点边界（根坐标），由引导画布解析后回写
         */
        val anchors: List<Rect>
    ) : GuideState {
        /**
         * 当前步骤是否已可展示内容：介绍步骤即时就绪，锚点步骤待锚点解析
         */
        val isReady: Boolean
            get() = entry.isIntro || anchors.isNotEmpty()
    }

    /**
     * 已结束，可重新 [GuideController.start]
     */
    data object Finished : GuideState
}

/**
 * 引导流激活期间系统返回键的行为
 */
enum class GuideBack {
    /**
     * 拦截返回键，引导流继续
     */
    Block,

    /**
     * 拦截返回键并结束引导流
     */
    Finish
}
