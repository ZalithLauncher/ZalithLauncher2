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

package com.movtery.zalithlauncher.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.movtery.layer_controller.data.ButtonStyle
import com.movtery.layer_controller.data.HideLayerWhen
import com.movtery.layer_controller.data.JoystickStyle
import com.movtery.layer_controller.data.cloneNew
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.layout.ControlLayout
import com.movtery.layer_controller.layout.createNewLayer
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableClickEventsProvider
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.layer_controller.observable.ObservableJoystickData
import com.movtery.layer_controller.observable.ObservableJoystickStyle
import com.movtery.layer_controller.observable.ObservableNormalData
import com.movtery.layer_controller.observable.ObservableTextData
import com.movtery.layer_controller.observable.ObservableTranslatableString
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.layer_controller.observable.cloneJoystick
import com.movtery.layer_controller.observable.cloneNormal
import com.movtery.layer_controller.observable.cloneText
import com.movtery.layer_controller.utils.saveToFile
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.control.CabinetStyleItem
import com.movtery.zalithlauncher.game.control.StyleCabinetState
import com.movtery.zalithlauncher.ui.components.MenuState
import com.movtery.zalithlauncher.ui.screens.main.control_editor.EditorOperation
import com.movtery.zalithlauncher.ui.screens.main.control_editor.EditorWarningOperation
import com.movtery.zalithlauncher.ui.screens.main.control_editor.EditorWidgetOperation
import com.movtery.zalithlauncher.ui.screens.main.control_editor.PreviewScenario
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget.SelectedWidgetData
import com.movtery.zalithlauncher.ui.screens.main.control_editor.style_cabinet.OpenCabinetSource
import com.movtery.zalithlauncher.ui.theme.showThemed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 控制布局编辑器
 */
class EditorViewModel : ViewModel() {
    lateinit var observableLayout: ObservableControlLayout
        private set

    /**
     * 样式收藏柜
     */
    val styleCabinet = StyleCabinetState()

    /**
     * 当前选中的控件层
     */
    var selectedLayer by mutableStateOf<ObservableControlLayer?>(null)

    /**
     * 当前选中的组件（仅用于编辑组件对话框）
     */
    var selectedWidget by mutableStateOf<SelectedWidgetData?>(null)

    /**
     * 编辑器菜单状态
     */
    var editorMenu by mutableStateOf(MenuState.HIDE)

    /** 编辑器菜单悬浮球当前的位置 */
    var editorBallPosition by mutableStateOf(Offset.Zero)

    /**
     * 编辑器各种操作项（主操作状态机）
     */
    var editorOperation by mutableStateOf<EditorOperation>(EditorOperation.None)
        private set

    /**
     * 编辑器对于控件的操作项（控件操作状态机，可叠加于主操作之上）
     */
    var editorWidgetOperation by mutableStateOf<EditorWidgetOperation>(EditorWidgetOperation.None)
        private set

    /**
     * 编辑器的一些警告状态项（警告状态机，独立弹出）
     */
    var editorWarningOperation by mutableStateOf<EditorWarningOperation>(EditorWarningOperation.None)
        private set

    /**
     * 是否开启控件层聚焦模式
     */
    var isLayerFocus by mutableStateOf(false)

    /**
     * 是否为预览控制布局模式
     */
    var isPreviewMode by mutableStateOf(false)

    /**
     * 预览控制布局的场景
     */
    var previewScenario by mutableStateOf(PreviewScenario.InMenu)

    /**
     * 预览控制布局时根据设备隐藏控制层
     */
    var previewHideLayerWhen by mutableStateOf(HideLayerWhen.None)



    fun initLayout(layout: ControlLayout) {
        if (!::observableLayout.isInitialized) {
            this.observableLayout = ObservableControlLayout(layout)
        }
    }



    /**
     * 在编辑画布上点击了一个控件
     * 已选中时再次点击则打开控件编辑器
     */
    fun onWidgetTapped(data: ObservableWidget, layer: ObservableControlLayer) {
        val current = selectedWidget?.data
        selectedWidget = SelectedWidgetData(data, layer)
        if (current == data) {
            //选中后再点击一次，打开编辑菜单
            editorOperation = EditorOperation.SelectButton
        }
    }

    /**
     * 打开当前选中控件的编辑器
     */
    fun openWidgetEditor() {
        if (selectedWidget != null) {
            editorOperation = EditorOperation.SelectButton
        }
    }

    /**
     * 新建一个控件层并进入它的属性编辑
     */
    fun createLayer(defaultLayerName: String) {
        val newLayer = observableLayout.addLayer(
            layer = createNewLayer(defaultLayerName = defaultLayerName)
        )
        editorOperation = EditorOperation.EditLayer(newLayer)
    }

    /**
     * 编辑一个控件层的属性
     */
    fun editLayer(layer: ObservableControlLayer) {
        editorOperation = EditorOperation.EditLayer(layer)
    }

    /**
     * 复制一个控件层为新的控件层，并进入新层的属性编辑
     */
    fun copyLayer(layer: ObservableControlLayer, defaultLayerName: String) {
        val baseLayer = layer.pack()
        val newLayer = observableLayout.addLayer(
            layer = createNewLayer(defaultLayerName = defaultLayerName).copy(
                hide = baseLayer.hide,
                hideWhenMouse = baseLayer.hideWhenMouse,
                hideWhenGamepad = baseLayer.hideWhenGamepad,
                visibilityType = baseLayer.visibilityType,
                normalButtons = baseLayer.normalButtons,
                textBoxes = baseLayer.textBoxes,
                joystickButtons = baseLayer.joystickButtons
            )
        )
        editorOperation = EditorOperation.EditLayer(newLayer)
    }

    /**
     * 请求删除一个控件层，弹出对话框
     */
    fun requestDeleteLayer(layer: ObservableControlLayer) {
        editorOperation = EditorOperation.DeleteLayer(layer)
    }

    /**
     * 确认删除控件层，并清理关联的选中态
     */
    fun confirmDeleteLayer(layer: ObservableControlLayer) {
        val isWidgetLayer = selectedWidget?.layer == layer
        removeLayer(layer)
        if (isWidgetLayer) {
            selectedWidget = null
        }
        editorOperation = EditorOperation.None
    }

    /**
     * 将一个控件层合并至下层
     */
    fun mergeLayerDownward(layer: ObservableControlLayer) {
        observableLayout.mergeDownward(layer)
    }

    /**
     * 控件层隐藏状态变化，必要时清理选中控件
     */
    fun onLayerHideChanged(hide: Boolean, layer: ObservableControlLayer) {
        if (hide && selectedWidget?.layer == layer) {
            selectedWidget = null
        }
    }

    /**
     * 切换编辑器菜单
     */
    fun switchMenu() {
        editorMenu = editorMenu.next()
    }

    /**
     * 移除控件层
     */
    fun removeLayer(layer: ObservableControlLayer) {
        if (layer == selectedLayer) selectedLayer = null
        observableLayout.removeLayer(layer.uuid)
    }

    /**
     * 为控件层添加控件
     */
    fun addWidget(layers: List<ObservableControlLayer>, addToLayer: (ObservableControlLayer) -> Unit) {
        val layer = selectedLayer
        if (layers.isEmpty()) {
            editorWarningOperation = EditorWarningOperation.WarningNoLayers
        } else if (layer == null) {
            editorWarningOperation = EditorWarningOperation.WarningNoSelectLayer
        } else {
            addToLayer(layer)
        }
    }

    /**
     * 在控件层移除控件
     */
    fun removeWidget(layer: ObservableControlLayer, widget: ObservableWidget) {
        when (widget) {
            is ObservableNormalData -> layer.removeNormalButton(widget.uuid)
            is ObservableTextData -> layer.removeTextBox(widget.uuid)
            is ObservableJoystickData -> layer.removeJoystickButton(widget.uuid)
        }
    }


    /**
     * 关闭当前主操作
     */
    fun dismissOperation() {
        editorOperation = EditorOperation.None
    }

    /**
     * 打开控件外观列表
     */
    fun openStyleList() {
        editorOperation = EditorOperation.OpenStyleList
    }

    /**
     * 打开摇杆样式列表
     */
    fun openJoystickStyleList() {
        editorOperation = EditorOperation.OpenJoystickStyleList
    }

    /**
     * 开始创建控件外观，弹出对话框
     */
    fun startCreateStyle() {
        editorOperation = EditorOperation.CreateStyle
    }

    /**
     * 确认创建控件外观，回到控件外观列表
     */
    fun confirmCreateStyle(name: String) {
        createNewStyle(name)
        editorOperation = EditorOperation.OpenStyleList
    }

    /**
     * 开始创建摇杆样式，弹出对话框
     */
    fun startCreateJoystickStyle() {
        editorOperation = EditorOperation.CreateJoystickStyle
    }

    /**
     * 确认创建摇杆样式，回到摇杆样式列表
     */
    fun confirmCreateJoystickStyle(name: String) {
        createNewJoystickStyle(name)
        editorOperation = EditorOperation.OpenJoystickStyleList
    }

    /**
     * 编辑布局内的控件外观样式
     */
    fun editStyle(style: ObservableButtonStyle) {
        editorOperation = EditorOperation.EditButtonStyle(style)
    }

    /**
     * 编辑布局内的摇杆样式
     */
    fun editJoystickStyle(style: ObservableJoystickStyle) {
        editorOperation = EditorOperation.EditJoystickStyle(style)
    }

    /**
     * 打开删除控件外观样式对话框
     */
    fun requestDeleteStyle(style: ObservableButtonStyle) {
        editorOperation = EditorOperation.DeleteButtonStyle(style)
    }

    /**
     * 确认删除控件外观样式
     */
    fun confirmDeleteStyle(style: ObservableButtonStyle) {
        removeStyle(style)
        editorOperation = EditorOperation.None
    }

    /**
     * 请求删除摇杆样式，弹出对话框
     */
    fun requestDeleteJoystickStyle(style: ObservableJoystickStyle) {
        editorOperation = EditorOperation.DeleteJoystickStyle(style)
    }

    /**
     * 确认删除摇杆样式
     */
    fun confirmDeleteJoystickStyle(style: ObservableJoystickStyle) {
        removeJoystickStyle(style)
        editorOperation = EditorOperation.None
    }



    /**
     * 关闭当前控件操作
     */
    fun dismissWidgetOperation() {
        editorWidgetOperation = EditorWidgetOperation.None
    }

    /**
     * 请求复制一个控件，询问目标控制层
     */
    fun requestCloneWidget(data: ObservableWidget, layer: ObservableControlLayer) {
        editorWidgetOperation = EditorWidgetOperation.CloneButton(data, layer)
    }

    /**
     * 请求删除一个控件，弹出对话框
     */
    fun requestDeleteWidget(data: ObservableWidget, layer: ObservableControlLayer) {
        editorWidgetOperation = EditorWidgetOperation.DeleteButton(data, layer)
    }

    /**
     * 确认将控件复制到所选控制层
     */
    fun confirmCloneWidgets(widget: ObservableWidget, layers: List<ObservableControlLayer>) {
        cloneWidgetToLayers(widget, layers)
        editorWidgetOperation = EditorWidgetOperation.None
    }

    /**
     * 确认删除控件
     */
    fun confirmDeleteWidget(widget: ObservableWidget, layer: ObservableControlLayer) {
        removeWidget(layer, widget)
        selectedWidget = null
        editorOperation = EditorOperation.None
        editorWidgetOperation = EditorWidgetOperation.None
    }

    /**
     * 编辑控件的显示文本
     */
    fun editWidgetText(string: ObservableTranslatableString) {
        editorWidgetOperation = EditorWidgetOperation.EditWidgetText(string)
    }

    /**
     * 编辑切换控件层可见性事件
     */
    fun switchLayersVisibility(data: ObservableClickEventsProvider, type: ClickEvent.Type) {
        editorWidgetOperation = EditorWidgetOperation.SwitchLayersVisibility(data, type)
    }

    /**
     * 编辑控件发送的文本
     */
    fun sendText(data: ObservableClickEventsProvider) {
        editorWidgetOperation = EditorWidgetOperation.SendText(data)
    }

    /**
     * 确认控件发送的文本，应用事件变更
     */
    fun confirmSendText(data: ObservableClickEventsProvider, value: String) {
        //清除所有发送文本事件，如果文本不为空则再添加
        data.onRemoveAllEvents(ClickEvent.Type.SendText)
        if (value.isNotEmpty()) {
            data.onAddEvent(ClickEvent(ClickEvent.Type.SendText, value))
        }
        editorWidgetOperation = EditorWidgetOperation.None
    }



    /**
     * 关闭当前警告
     */
    fun dismissWarning() {
        editorWarningOperation = EditorWarningOperation.None
    }



    /**
     * 创建一个新的控件外观
     */
    fun createNewStyle(name: String) {
        observableLayout.addStyle(
            com.movtery.layer_controller.data.createNewButtonStyle(name)
        )
    }

    /**
     * 复制控件外观
     */
    fun cloneStyle(style: ObservableButtonStyle) {
        observableLayout.cloneStyle(style)
    }

    /**
     * 删除一个控件外观
     */
    fun removeStyle(style: ObservableButtonStyle) {
        observableLayout.removeStyle(style.uuid)
    }

    /**
     * 移除一个摇杆样式
     */
    fun removeJoystickStyle(style: ObservableJoystickStyle) {
        observableLayout.removeJoystickStyle(style.uuid)
    }

    /**
     * 创建一个新的摇杆样式
     */
    fun createNewJoystickStyle(name: String) {
        observableLayout.addJoystickStyle(
            com.movtery.layer_controller.data.createNewJoystickStyle(name)
        )
    }

    /**
     * 复制摇杆样式
     */
    fun cloneJoystickStyle(style: ObservableJoystickStyle) {
        observableLayout.cloneJoystickStyle(style)
    }

    /**
     * 将控件外观样式收藏到样式收藏柜
     * 已收藏同 uuid 样式时转入冲突处理
     */
    fun favoriteStyle(style: ObservableButtonStyle, onResult: (Boolean) -> Unit) {
        if (styleCabinet.exists(isJoystick = false, uuid = style.uuid)) {
            editorOperation = EditorOperation.FavoriteButtonStyleConflict(style)
        } else {
            performFavorite(style.pack(), independent = false, onResult)
        }
    }

    /**
     * 将摇杆样式收藏到样式收藏柜
     * 已收藏同 uuid 样式时转入冲突处理
     */
    fun favoriteJoystickStyle(style: ObservableJoystickStyle, onResult: (Boolean) -> Unit) {
        if (styleCabinet.exists(isJoystick = true, uuid = style.uuid)) {
            editorOperation = EditorOperation.FavoriteJoystickStyleConflict(style)
        } else {
            performFavorite(style.pack(), independent = false, onResult)
        }
    }

    /**
     * 处理收藏冲突
     * @param independent 为 true 时生成新 uuid 作为独立收藏，否则覆盖现有
     */
    fun resolveFavoriteConflict(
        independent: Boolean,
        onResult: (Boolean) -> Unit
    ) {
        when (val operation = editorOperation) {
            is EditorOperation.FavoriteButtonStyleConflict -> {
                performFavorite(operation.style.pack(), independent, onResult)
                editorOperation = EditorOperation.OpenStyleList
            }
            is EditorOperation.FavoriteJoystickStyleConflict -> {
                performFavorite(operation.style.pack(), independent, onResult)
                editorOperation = EditorOperation.OpenJoystickStyleList
            }
            else -> {}
        }
    }

    /**
     * 取消收藏冲突处理，直接回到来源的样式列表
     */
    fun cancelFavoriteConflict() {
        when (editorOperation) {
            is EditorOperation.FavoriteButtonStyleConflict -> editorOperation = EditorOperation.OpenStyleList
            is EditorOperation.FavoriteJoystickStyleConflict -> editorOperation = EditorOperation.OpenJoystickStyleList
            else -> {}
        }
    }

    /**
     * 打开样式收藏柜
     * @param source 来源
     */
    fun openStyleCabinet(source: OpenCabinetSource) {
        editorOperation = EditorOperation.OpenStyleCabinet(source)
    }

    /**
     * 关闭样式收藏柜，返回打开它的样式列表
     */
    fun closeStyleCabinet() {
        val source = (editorOperation as? EditorOperation.OpenStyleCabinet)?.source
        val back = when (source) {
            OpenCabinetSource.ControlStyle -> EditorOperation.OpenStyleList
            OpenCabinetSource.JoystickStyle -> EditorOperation.OpenJoystickStyleList
            else -> EditorOperation.None
        }
        editorOperation = back
    }

    /**
     * 编辑收藏柜中的一个样式项
     */
    fun editCabinetItem(item: CabinetStyleItem) {
        val source = (editorOperation as? EditorOperation.OpenStyleCabinet)?.source ?: OpenCabinetSource.Menu
        editorOperation = when (item) {
            is CabinetStyleItem.Button -> EditorOperation.EditCabinetButtonStyle(item, source)
            is CabinetStyleItem.Joystick -> EditorOperation.EditCabinetJoystickStyle(item, source)
        }
    }

    /**
     * 删除一个收藏项
     */
    fun deleteCabinetItem(item: CabinetStyleItem) {
        styleCabinet.delete(item)
    }

    /**
     * 将柜内样式加入当前控制布局
     * 布局内已存在同 uuid 样式时转入冲突处理对话框
     * @param onAdded 成功加入后的回调
     */
    fun addCabinetStyleToLayout(item: CabinetStyleItem, onAdded: () -> Unit) {
        val source = (editorOperation as? EditorOperation.OpenStyleCabinet)?.source ?: OpenCabinetSource.Menu
        when (item) {
            is CabinetStyleItem.Button -> {
                if (observableLayout.styles.value.any { it.uuid == item.style.uuid }) {
                    editorOperation = EditorOperation.AddToLayoutButtonStyleConflict(item, source)
                } else {
                    observableLayout.addStyle(item.style.pack())
                    onAdded()
                }
            }
            is CabinetStyleItem.Joystick -> {
                if (observableLayout.joystickStyles.value.any { it.uuid == item.style.uuid }) {
                    editorOperation = EditorOperation.AddToLayoutJoystickStyleConflict(item, source)
                } else {
                    observableLayout.addJoystickStyle(item.style.pack())
                    onAdded()
                }
            }
        }
    }

    /**
     * 处理加入布局冲突
     * @param overwrite 为 true 时以柜内样式覆盖布局内同 uuid 样式，
     *                  为 false 时创建新的样式
     */
    fun resolveAddToLayoutConflict(overwrite: Boolean) {
        when (val operation = editorOperation) {
            is EditorOperation.AddToLayoutButtonStyleConflict -> {
                if (overwrite) {
                    observableLayout.replaceStyle(operation.item.style.pack())
                } else {
                    observableLayout.addStyle(operation.item.style.pack().cloneNew())
                }
                editorOperation = EditorOperation.OpenStyleCabinet(operation.source)
            }
            is EditorOperation.AddToLayoutJoystickStyleConflict -> {
                if (overwrite) {
                    observableLayout.replaceJoystickStyle(operation.item.style.pack())
                } else {
                    observableLayout.addJoystickStyle(operation.item.style.pack().cloneNew())
                }
                editorOperation = EditorOperation.OpenStyleCabinet(operation.source)
            }
            else -> {}
        }
    }

    /**
     * 取消加入布局冲突处理，直接回到样式收藏柜
     */
    fun cancelAddToLayoutConflict() {
        val source = when (val operation = editorOperation) {
            is EditorOperation.AddToLayoutButtonStyleConflict -> operation.source
            is EditorOperation.AddToLayoutJoystickStyleConflict -> operation.source
            else -> return
        }
        editorOperation = EditorOperation.OpenStyleCabinet(source)
    }

    /**
     * 关闭柜内样式编辑器，返回样式收藏柜
     */
    fun closeCabinetEdit(item: CabinetStyleItem, source: OpenCabinetSource) {
        styleCabinet.save(item) // 立即保存编辑结果
        editorOperation = EditorOperation.OpenStyleCabinet(source)
    }

    /**
     * 关闭控件外观样式编辑器，并按来源分派后续状态
     */
    fun closeButtonStyleEdit() {
        when (val operation = editorOperation) {
            is EditorOperation.EditCabinetButtonStyle -> {
                closeCabinetEdit(operation.item, operation.source)
            }
            is EditorOperation.EditButtonStyle -> {
                editorOperation = EditorOperation.None
            }
            else -> {}
        }
    }

    /**
     * 关闭摇杆样式编辑器，并按来源分派后续状态
     */
    fun closeJoystickStyleEdit() {
        when (val operation = editorOperation) {
            is EditorOperation.EditCabinetJoystickStyle -> {
                closeCabinetEdit(operation.item, operation.source)
            }
            is EditorOperation.EditJoystickStyle -> {
                editorOperation = EditorOperation.None
            }
            else -> {}
        }
    }

    /**
     * 保存柜内样式当前的编辑结果
     */
    fun saveCabinetItem(item: CabinetStyleItem) {
        styleCabinet.save(item)
    }

    /**
     * 将控件复制到控件层
     */
    fun cloneWidgetToLayers(widget: ObservableWidget, layers: List<ObservableControlLayer>) {
        when (widget) {
            is ObservableNormalData -> {
                layers.forEach { layer ->
                    val newData = widget.cloneNormal()
                    layer.addNormalButton(newData)
                }
            }
            is ObservableTextData -> {
                layers.forEach { layer ->
                    val newData = widget.cloneText()
                    layer.addTextBox(newData)
                }
            }
            is ObservableJoystickData -> {
                layers.forEach { layer ->
                    val newData = widget.cloneJoystick()
                    layer.addJoystickButton(newData)
                }
            }
        }
    }

    /**
     * 将编辑器内层级隐藏状态同步到实际隐藏状态
     * 供预览模式下使用正确的隐藏状态
     */
    fun applyEditorHide() {
        observableLayout.applyEditorHide()
    }

    /**
     * 保存控制布局
     */
    fun save(
        targetFile: File,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            editorOperation = EditorOperation.Saving
            val layout = observableLayout.pack()
            runCatching {
                layout.saveToFile(targetFile)
            }.onFailure { e ->
                editorOperation = EditorOperation.SaveFailed(e)
            }.onSuccess {
                editorOperation = EditorOperation.None
                onSaved()
            }
        }
    }

    fun onBackPressed(
        context: Context,
        onExit: () -> Unit
    ) {
        //检查并退出控件对话框、编辑控件样式对话框
        when (editorOperation) {
            is EditorOperation.SelectButton,
            is EditorOperation.EditButtonStyle,
            is EditorOperation.OpenStyleCabinet -> {
                editorOperation = EditorOperation.None
            }
            //柜内样式编辑器，保存编辑结果并返回样式收藏柜
            is EditorOperation.EditCabinetButtonStyle -> closeButtonStyleEdit()
            is EditorOperation.EditCabinetJoystickStyle -> closeJoystickStyleEdit()
            else -> {
                showExitEditorDialog(
                    context = context,
                    onExit = onExit
                )
            }
        }
    }

    /**
     * 用于检查控制布局是否被修改过
     */
    private val checkModified = Mutex()

    /**
     * 弹出退出控制布局编辑器的对话框
     * @param onExit 用户点击确认，退出编辑器
     */
    fun showExitEditorDialog(
        context: Context,
        onExit: () -> Unit
    ) {
        viewModelScope.launch {
            val isModified = checkModified.withLock {
                observableLayout.isModified()
            }
            if (isModified) {
                showExitEditorDialogSuspend(
                    context = context,
                    onExit = onExit
                )
            } else {
                //未被修改，可以直接退出
                onExit()
            }
        }
    }

    private suspend fun showExitEditorDialogSuspend(
        context: Context,
        onExit: () -> Unit
    ) = withContext(Dispatchers.Main) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.generic_warning)
            .setMessage(R.string.control_editor_exit_message)
            .setPositiveButton(R.string.generic_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton(R.string.control_editor_exit_confirm) { dialog, _ ->
                dialog.dismiss()
                onExit()
            }
            .showThemed()
    }

    /**
     * 执行一次样式收藏写入
     * @param independent 为 true 时生成新 uuid 作为独立收藏，否则同 uuid 覆盖
     */
    private fun performFavorite(style: ButtonStyle, independent: Boolean, onResult: (Boolean) -> Unit) {
        val data = if (independent) style.cloneNew() else style
        styleCabinet.favorite(data, onResult)
    }

    private fun performFavorite(style: JoystickStyle, independent: Boolean, onResult: (Boolean) -> Unit) {
        val data = if (independent) style.cloneNew() else style
        styleCabinet.favorite(data, onResult)
    }
}