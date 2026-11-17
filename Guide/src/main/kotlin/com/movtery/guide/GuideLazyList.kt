package com.movtery.guide

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * 登记懒加载列表的锚点寻址能力
 * 引导步骤的锚点缺失时，库会逐屏滚动列表并用稳定键反查目标 item 的位置精确定位
 * @param keyOf 将引导键映射为目标 item 声明的稳定键，null 表示该键不归此列表
 */
fun Modifier.guideLazyList(
    state: LazyListState,
    keyOf: (GuideKey) -> Any?
): Modifier = composed {
    val registry = LocalGuideRegistry.current ?: return@composed this
    val currentKeyOf by rememberUpdatedState(keyOf)
    DisposableEffect(registry) {
        val attempted = mutableSetOf<GuideKey>()
        val handler: GuideScrollHandler = handler@{ key ->
            val target = currentKeyOf(key) ?: return@handler false
            if (!attempted.add(key)) return@handler false
            seekToItem(registry, state, key, target)
        }
        registry.addScrollHandler(handler)
        onDispose { registry.removeScrollHandler(handler) }
    }
    this
}

/** 寻址的最大滚动步数，防止超长列表或映射错误导致的无界扫描 */
private const val MAX_SEEK_ATTEMPTS = 64

private suspend fun seekToItem(
    registry: GuideRegistry,
    state: LazyListState,
    key: GuideKey,
    itemKey: Any
): Boolean {
    // 列表可能刚组合、尚无内容，先等它完成首次布局
    var frames = 0
    while (state.layoutInfo.totalItemsCount == 0 && frames < 30) {
        withFrameNanos { }
        frames++
    }
    if (state.layoutInfo.totalItemsCount == 0) return false

    var index = 0
    var attempts = 0
    while (attempts < MAX_SEEK_ATTEMPTS) {
        state.scrollToItem(index)

        // 等待两帧，让组合与布局生效
        withFrameNanos { }
        withFrameNanos { }

        if (registry.rectsFor(key).isNotEmpty()) return true

        val visible = state.layoutInfo.visibleItemsInfo
        val target = visible.firstOrNull { it.key == itemKey }
        if (target != null) {
            // 稳定键命中可见 item，精确滚动到其索引
            state.scrollToItem(target.index)
            withFrameNanos { }
            withFrameNanos { }
            if (registry.rectsFor(key).isNotEmpty()) return true
        }

        // 目标不在本屏，跳到当前最后一个可见 item 的下一项继续扫描
        val lastIndex = visible.lastOrNull()?.index ?: return false
        if (lastIndex >= state.layoutInfo.totalItemsCount - 1) return false
        index = lastIndex + 1
        attempts++
    }
    return false
}
