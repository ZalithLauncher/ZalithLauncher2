package com.movtery.zalithlauncher.game.renderer

/**
 * 简易渲染器信息，仅记录渲染器名称，和是否属于插件
 * @param name 当前游戏使用的渲染器的名称
 * @param isPlugin 当前使用的渲染器是否为插件
 */
class SimpleRendererInfo(
    val name: String,
    val isPlugin: Boolean,
)