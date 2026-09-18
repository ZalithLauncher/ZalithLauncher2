package com.movtery.zalithlauncher.game.version.mod.update

import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 更新模组需用到的清单
 * @param new 获取到的新模组版本信息
 */
data class ModManifest(
    val data: ModData,
    val new: PlatformVersion,
)

/**
 * 获取清单中全部的模组新版本信息
 */
fun List<ModManifest>.allNews() = map { it.new }

fun List<ModManifest>.toSelectableList() = map { manifest ->
    SelectableModManifest(
        data = manifest.data,
        new = manifest.new,
    )
}

/**
 * 可记录选择状态的更新模组清单
 * @see ModManifest
 * @property selected 是否选中该清单
 */
class SelectableModManifest(
    val data: ModData,
    val new: PlatformVersion,
) {
    private val _selected = MutableStateFlow(true)
    val selected = _selected.asStateFlow()

    fun updateSelected(value: Boolean) {
        _selected.update { value }
    }

    fun selected() = _selected.value
}

fun List<SelectableModManifest>.toFinalList() = mapNotNull { manifest ->
    if (manifest.selected()) {
        ModManifest(
            data = manifest.data,
            new = manifest.new,
        )
    } else null
}