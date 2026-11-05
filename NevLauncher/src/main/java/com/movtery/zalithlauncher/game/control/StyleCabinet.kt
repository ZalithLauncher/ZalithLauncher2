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

package com.movtery.zalithlauncher.game.control

import com.movtery.layer_controller.data.ButtonStyle
import com.movtery.layer_controller.data.JoystickStyle
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableJoystickStyle
import com.movtery.layer_controller.utils.loadButtonStyle
import com.movtery.layer_controller.utils.loadJoystickStyle
import com.movtery.layer_controller.utils.saveToStyleFile
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "StyleCabinet"

/**
 * 样式收藏柜中的样式项
 */
sealed interface CabinetStyleItem {
    /** 样式文件 */
    val file: File
    /** 样式显示名称 */
    val name: String

    /** 控件外观样式 */
    data class Button(
        override val file: File,
        val style: ObservableButtonStyle
    ) : CabinetStyleItem {
        override val name: String get() = style.name
    }

    /** 摇杆样式 */
    data class Joystick(
        override val file: File,
        val style: ObservableJoystickStyle
    ) : CabinetStyleItem {
        override val name: String get() = style.name
    }
}

/**
 * 样式收藏柜状态
 */
class StyleCabinetState {
    private val scope = CoroutineScope(Dispatchers.IO)

    /** 控件样式收藏本地目录 */
    private val dirControls = File(PathManager.DIR_CONTROL_LAYOUTS, "styles/controls")
    /** 摇杆样式收藏本地目录 */
    private val dirJoystick = File(PathManager.DIR_CONTROL_LAYOUTS, "styles/joystick")

    private val _items = MutableStateFlow<List<CabinetStyleItem>>(emptyList())
    val items = _items.asStateFlow()

    /**
     * 加载收藏柜内全部样式
     */
    fun load() {
        scope.launch {
            val buttons = loadItems(dirControls) { file ->
                CabinetStyleItem.Button(file, ObservableButtonStyle(loadButtonStyle(file)))
            }
            val joysticks = loadItems(dirJoystick) { file ->
                CabinetStyleItem.Joystick(file, ObservableJoystickStyle(loadJoystickStyle(file)))
            }
            _items.update { old ->
                //合并而非整表替换，避免异步加载覆盖加载完成前已收藏的项
                val loaded = buttons + joysticks
                sortItems(loaded.filterNot { item -> old.any { sameIdentity(it, item) } } + old)
            }
        }
    }

    /**
     * 检查样式是否已被收藏
     * @return 同类型下是否存在相同 uuid
     */
    fun exists(isJoystick: Boolean, uuid: String): Boolean {
        return _items.value.any { item ->
            when (item) {
                is CabinetStyleItem.Button -> !isJoystick && item.style.uuid == uuid
                is CabinetStyleItem.Joystick -> isJoystick && item.style.uuid == uuid
            }
        }
    }

    /**
     * 收藏一个控件外观样式，同 uuid 文件将被覆盖
     * @param onResult 收藏结果（主线程回调）
     */
    fun favorite(style: ButtonStyle, onResult: (Boolean) -> Unit = {}) {
        launchFavorite(onResult) {
            val file = style.saveToStyleFile(dirControls)
            CabinetStyleItem.Button(file, ObservableButtonStyle(style))
        }
    }

    /**
     * 收藏一个摇杆样式，同 uuid 文件将被覆盖
     * @param onResult 收藏结果（主线程回调）
     */
    fun favorite(style: JoystickStyle, onResult: (Boolean) -> Unit = {}) {
        launchFavorite(onResult) {
            val file = style.saveToStyleFile(dirJoystick)
            CabinetStyleItem.Joystick(file, ObservableJoystickStyle(style))
        }
    }

    /**
     * 保存柜内样式的编辑结果，不替换收藏柜内存中的可观察对象
     */
    fun save(item: CabinetStyleItem) {
        scope.launch {
            runCatching {
                when (item) {
                    is CabinetStyleItem.Button -> item.style.pack().saveToStyleFile(dirControls)
                    is CabinetStyleItem.Joystick -> item.style.pack().saveToStyleFile(dirJoystick)
                }
            }.onFailure { e ->
                Logger.warning(TAG, "Failed to save style file! file = ${item.file}", e)
            }
        }
    }

    /**
     * 删除一个收藏项，移除本地文件
     */
    fun delete(item: CabinetStyleItem) {
        scope.launch {
            runCatching {
                item.file.delete()
            }.onSuccess {
                _items.update { old -> old.filterNot { it == item } }
            }.onFailure { e ->
                Logger.warning(TAG, "Failed to delete style file! file = ${item.file}", e)
            }
        }
    }

    private fun launchFavorite(
        onResult: (Boolean) -> Unit,
        block: suspend () -> CabinetStyleItem
    ) {
        scope.launch {
            val result = runCatching { block() }
            if (result.isSuccess) {
                val newItem = result.getOrThrow()
                _items.update { old ->
                    sortItems(old.filterNot { sameIdentity(it, newItem) } + newItem)
                }
            } else {
                Logger.warning(TAG, "Failed to favorite style!", result.exceptionOrNull())
            }
            withContext(Dispatchers.Main) {
                onResult(result.isSuccess)
            }
        }
    }

    private fun loadItems(
        dir: File,
        create: (File) -> CabinetStyleItem
    ): List<CabinetStyleItem> {
        val files = dir.listFiles()
            ?.filter { it.isFile && it.exists() && it.extension.equals("json", true) }
            ?: return emptyList()
        return files.mapNotNull { file ->
            runCatching {
                create(file)
            }.onFailure { e ->
                Logger.warning(TAG, "Failed to load style file! file = $file", e)
            }.getOrNull()
        }
    }

    private fun sameIdentity(a: CabinetStyleItem, b: CabinetStyleItem): Boolean {
        return when {
            a is CabinetStyleItem.Button && b is CabinetStyleItem.Button ->
                a.style.uuid == b.style.uuid
            a is CabinetStyleItem.Joystick && b is CabinetStyleItem.Joystick ->
                a.style.uuid == b.style.uuid
            else -> false
        }
    }

    private fun sortItems(items: List<CabinetStyleItem>): List<CabinetStyleItem> {
        return items.sortedWith(
            compareBy({ it is CabinetStyleItem.Joystick }, { it.name })
        )
    }
}
