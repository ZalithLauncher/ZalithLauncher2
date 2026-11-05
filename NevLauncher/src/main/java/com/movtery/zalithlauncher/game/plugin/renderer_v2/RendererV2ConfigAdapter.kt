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

package com.movtery.zalithlauncher.game.plugin.renderer_v2

import com.movtery.zalithlauncher.game.plugin.renderer_v2.data.RendererConfig
import com.movtery.zalithlauncher.game.renderer.config.ChoiceOption
import com.movtery.zalithlauncher.game.renderer.config.ConfigScalar
import com.movtery.zalithlauncher.game.renderer.config.ConfigTarget
import com.movtery.zalithlauncher.game.renderer.config.ConfigText
import com.movtery.zalithlauncher.game.renderer.config.RendererConfigSpec
import com.movtery.zalithlauncher.game.renderer.config.SingleChoiceEntry
import com.movtery.zalithlauncher.game.renderer.config.SwitchEntry
import com.movtery.zalithlauncher.game.renderer.config.TextEntry

/**
 * 将 V2 插件格式转换为中立配置规范
 */
fun RendererConfig.toConfigSpec(genText: (String) -> String?): RendererConfigSpec {
    val fixedEnv = env.filterIsInstance<RendererConfig.Env.NormalEnv>()
        .associate { it.key to it.value }

    val entries = env.mapNotNull { entry ->
        when (entry) {
            is RendererConfig.Env.NormalEnv -> null
            is RendererConfig.Env.ToggleableEnv -> SwitchEntry(
                id = entry.key,
                title = entry.title.dynamicText(genText),
                description = null,
                default = entry.toggle,
                enabledValue = ConfigScalar.StringValue(entry.value),
                target = ConfigTarget.Environment(entry.key)
            )
            is RendererConfig.Env.SelectableEnv -> {
                val values = buildList {
                    add(entry.items.defaultValue)
                    addAll(entry.items.values)
                }.distinct()
                SingleChoiceEntry(
                    id = entry.key,
                    title = entry.title.dynamicText(genText),
                    description = null,
                    options = values.map { ChoiceOption(ConfigScalar.StringValue(it)) },
                    defaultValue = ConfigScalar.StringValue(entry.items.defaultValue),
                    defaultEnabled = entry.check,
                    target = ConfigTarget.Environment(entry.key)
                )
            }
            is RendererConfig.Env.CustomizableEnv -> TextEntry(
                id = entry.key,
                title = entry.title.dynamicText(genText),
                description = null,
                default = entry.defaultValue.orEmpty(),
                target = ConfigTarget.Environment(entry.key)
            )
        }
    }
    return RendererConfigSpec(entries = entries, fixedEnv = fixedEnv)
}

private fun RendererConfig.MetaString?.dynamicText(genText: (String) -> String?): ConfigText? =
    this?.key?.let(genText)?.let(ConfigText::Dynamic)
