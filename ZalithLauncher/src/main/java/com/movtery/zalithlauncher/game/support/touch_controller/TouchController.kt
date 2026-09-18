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

package com.movtery.zalithlauncher.game.support.touch_controller

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.CorrectionInfo
import android.view.inputmethod.CursorAnchorInfo
import android.view.inputmethod.EditorBoundsInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputContentInfo
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.PlatformTextInputModifierNode
import androidx.compose.ui.platform.establishTextInputSession
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastForEach
import androidx.core.content.getSystemService
import androidx.core.view.inputmethod.EditorInfoCompat
import com.movtery.zalithlauncher.game.input.LWJGLCharSender
import com.movtery.zalithlauncher.game.support.touch_controller.ControllerProxy.proxyClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import top.fifthlight.touchcontroller.proxy.client.LauncherProxyClient
import top.fifthlight.touchcontroller.proxy.message.FloatRect
import top.fifthlight.touchcontroller.proxy.message.input.TextInputState
import top.fifthlight.touchcontroller.proxy.message.input.TextRange
import top.fifthlight.touchcontroller.proxy.message.input.compositionText
import top.fifthlight.touchcontroller.proxy.message.input.doArrowLeft
import top.fifthlight.touchcontroller.proxy.message.input.doArrowRight
import top.fifthlight.touchcontroller.proxy.message.input.doBackspace
import top.fifthlight.touchcontroller.proxy.message.input.doDelete
import top.fifthlight.touchcontroller.proxy.message.input.doShiftLeft
import top.fifthlight.touchcontroller.proxy.message.input.doShiftRight
import top.fifthlight.touchcontroller.proxy.message.input.selectionText

private data class TouchControllerInputModifier(
    private val screenSize: IntSize,
    private val onCursorRectUpdated: (IntRect?) -> Unit,
    private val onInputAreaRectUpdated: (IntRect?) -> Unit,
) :
    ModifierNodeElement<TouchControllerInputModifierNode>() {
    override fun create() =
        TouchControllerInputModifierNode(screenSize, onCursorRectUpdated, onInputAreaRectUpdated)

    override fun update(node: TouchControllerInputModifierNode) {
        node.screenSize = screenSize
        node.onCursorRectUpdated = onCursorRectUpdated
        node.onInputAreaRectUpdated = onInputAreaRectUpdated
    }

    override fun InspectorInfo.inspectableProperties() {
        // Show nothing in the inspector.
    }
}

private class TouchControllerInputConnection(
    scope: CoroutineScope,
    private val view: View,
    private val inputMethodManager: InputMethodManager,
    initialState: TextInputState,
    private val onStateChanged: (TextInputState) -> Unit,
    private val cursorRect: StateFlow<IntRect?>,
    private val inputAreaRect: StateFlow<IntRect?>,
) : InputConnection {
    private var state: TextInputState = initialState
    private val clipboardManager: ClipboardManager? by lazy {
        view.context.getSystemService()
    }
    private var inBatchEdit: Int = 0
    private var delayedNewStateByBatchEdit: TextInputState? = null
    private var extractTextToken: Int? = null
    private var hasZeroExtractToken = false

    private fun TextRange.isEmpty() = length == 0
    private fun String.removeRange(range: TextRange) =
        substring(0, range.start) + substring(range.end)

    private fun String.substring(range: TextRange) = substring(range.start, range.end)
    private fun String.replaceRange(range: TextRange, newText: CharSequence) =
        substring(0, range.start) + newText + substring(range.end)

    private fun String.insertAt(index: Int, text: CharSequence) =
        substring(0, index) + text + substring(index)

    private operator fun TextRange.minus(other: TextRange): TextRange {
        val e1 = this.end
        val s2 = other.start
        val e2 = other.end

        val newStart = if (this.start < s2) {
            this.start
        } else {
            maxOf(this.start, e2) - other.length
        }

        val part1 = minOf(e1, s2) - this.start
        val part2 = e1 - maxOf(this.start, e2)

        val newLength = maxOf(0, part1) + maxOf(0, part2)
        return TextRange(newStart, newLength)
    }

    init {
        refreshState()
        scope.launch {
            cursorRect.combine(inputAreaRect, ::Pair).collect { (cursorRect, inputAreaRect) ->
                val state = state
                inputMethodManager.updateCursorAnchorInfo(view, CursorAnchorInfo.Builder().apply {
                    setSelectionRange(state.selection.start, state.selection.end)
                    if (!state.composition.isEmpty()) {
                        setComposingText(state.composition.start, state.compositionText)
                    }
                    cursorRect?.let {
                        setInsertionMarkerLocation(
                            cursorRect.left.toFloat(),
                            cursorRect.top.toFloat(),
                            cursorRect.bottom.toFloat(),
                            cursorRect.bottom.toFloat(),
                            CursorAnchorInfo.FLAG_HAS_VISIBLE_REGION,
                        )
                    }
                    inputAreaRect?.let {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            setEditorBoundsInfo(EditorBoundsInfo.Builder().apply {
                                setEditorBounds(
                                    RectF(
                                        inputAreaRect.left.toFloat(),
                                        inputAreaRect.top.toFloat(),
                                        inputAreaRect.bottom.toFloat(),
                                        inputAreaRect.bottom.toFloat(),
                                    )
                                )
                            }.build())
                        }
                    }
                    setMatrix(view.matrix)
                }.build())
            }
        }
    }

    private fun refreshState() {
        inputMethodManager.updateSelection(
            view,
            state.selection.start,
            state.selection.end,
            state.composition.start,
            state.composition.end
        )
        val extractedText = getExtractedText()
        if (hasZeroExtractToken) {
            inputMethodManager.updateExtractedText(view, 0, extractedText)
        }
        extractTextToken?.let { token ->
            inputMethodManager.updateExtractedText(view, token, extractedText)
        }
    }

    fun updateState(newState: TextInputState) {
        if (inBatchEdit > 0) {
            delayedNewStateByBatchEdit = newState
            return
        }
        if (state == newState) {
            return
        }
        if (state.text != newState.text) {
            inputMethodManager.restartInput(view)
        }
        state = newState
        refreshState()
    }

    private fun updateState(updater: (TextInputState) -> TextInputState) =
        updateState(refresh = true, updater = updater)

    private fun updateState(
        refresh: Boolean = true,
        updater: (TextInputState) -> TextInputState
    ): TextInputState {
        val newState = updater(state)
        state = newState
        if (inBatchEdit == 0) {
            if (refresh) {
                refreshState()
            }
            onStateChanged(newState)
        }
        return newState
    }

    override fun beginBatchEdit(): Boolean {
        inBatchEdit++
        return true
    }

    override fun clearMetaKeyStates(states: Int): Boolean {
        return true
    }

    override fun closeConnection() {
    }

    override fun commitCompletion(text: CompletionInfo?): Boolean {
        return true
    }

    override fun commitContent(
        inputContentInfo: InputContentInfo,
        flags: Int,
        opts: Bundle?
    ): Boolean {
        return false
    }

    override fun commitCorrection(correctionInfo: CorrectionInfo?): Boolean {
        return false
    }

    private fun TextInputState.commitTextAsNewState(text: CharSequence, newCursorPosition: Int) =
        if (composition.isEmpty()) {
            val newText = this.text.replaceRange(selection, text)
            val finalCursorPosition = when {
                newCursorPosition > 0 -> selection.start + text.length + newCursorPosition - 1
                else -> selection.start - newCursorPosition
            }.coerceIn(0..newText.length)
            TextInputState(
                text = newText,
                selection = TextRange(finalCursorPosition),
                composition = TextRange.EMPTY,
            )
        } else {
            val newText = this.text.replaceRange(composition, text)
            val finalCursorPosition = when {
                newCursorPosition > 0 -> composition.start + text.length + newCursorPosition - 1
                else -> composition.start - newCursorPosition
            }.coerceIn(0..newText.length)
            TextInputState(
                text = newText,
                selection = TextRange(finalCursorPosition),
                composition = TextRange.EMPTY,
            )
        }

    override fun commitText(text: CharSequence, newCursorPosition: Int): Boolean {
        var enterCount = 0
        val filteredText = text.filter {
            val isNewLine = it == '\n'
            if (isNewLine) {
                enterCount++
            }
            !isNewLine
        }
        if (filteredText.isNotEmpty()) {
            updateState { currentState ->
                currentState.commitTextAsNewState(filteredText, newCursorPosition)
            }
        }
        repeat(enterCount) {
            LWJGLCharSender.sendEnter()
        }
        return true
    }

    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
        updateState { currentState ->
            val limitedBeforeLength = minOf(beforeLength, currentState.selection.start)
            val limitedAfterLength =
                minOf(afterLength, currentState.text.length - currentState.selection.end)
            val beforeText =
                currentState.text.substring(0, currentState.selection.start - limitedBeforeLength)
            val selectedText = currentState.text.substring(currentState.selection)
            val afterText =
                currentState.text.substring(currentState.selection.end + limitedAfterLength)
            val removedLeftRange =
                TextRange(currentState.selection.start - limitedBeforeLength, limitedBeforeLength)
            val removedRightRange = TextRange(currentState.selection.end, limitedAfterLength)
            TextInputState(
                text = beforeText + selectedText + afterText,
                selection = TextRange(
                    start = beforeText.length,
                    length = selectedText.length,
                ),
                composition = currentState.composition - removedRightRange - removedLeftRange,
            )
        }
        return true
    }

    override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean {
        val currentState = state
        val text = currentState.text
        val selectionStart = currentState.selection.start

        var remainingBefore = beforeLength
        var charCountBefore = 0
        var index = selectionStart - 1

        while (remainingBefore > 0 && index >= 0) {
            val codePoint = Character.codePointBefore(text, index + 1)
            val charCount = Character.charCount(codePoint)
            charCountBefore += charCount
            index -= charCount
            remainingBefore--
        }

        var remainingAfter = afterLength
        var charCountAfter = 0
        index = selectionStart

        while (remainingAfter > 0 && index < text.length) {
            val codePoint = Character.codePointAt(text, index)
            val charCount = Character.charCount(codePoint)
            charCountAfter += charCount
            index += charCount
            remainingAfter--
        }

        return deleteSurroundingText(charCountBefore, charCountAfter)
    }

    override fun endBatchEdit(): Boolean {
        inBatchEdit--
        if (inBatchEdit == 0) {
            delayedNewStateByBatchEdit?.let { delayed ->
                updateState(delayed)
                delayedNewStateByBatchEdit = null
            } ?: run {
                refreshState()
                onStateChanged(state)
            }
        }
        return inBatchEdit > 0
    }

    override fun finishComposingText(): Boolean {
        updateState { currentState ->
            currentState.copy(composition = TextRange.EMPTY)
        }
        return true
    }

    override fun getCursorCapsMode(reqModes: Int): Int {
        return state.let { state ->
            TextUtils.getCapsMode(
                state.text, if (state.selectionLeft) {
                    state.selection.start
                } else {
                    state.selection.end
                }, reqModes
            )
        }
    }

    private fun getExtractedText() = ExtractedText().apply {
        state.let {
            text = it.text
            selectionStart = it.selection.start
            selectionEnd = it.selection.end
            startOffset = 0
            partialStartOffset = -1
            partialEndOffset = 0
        }
    }

    override fun getExtractedText(request: ExtractedTextRequest, flags: Int): ExtractedText {
        if (request.token == 0) {
            hasZeroExtractToken = true
        } else {
            this.extractTextToken = request.token
        }
        return getExtractedText()
    }

        override

    fun getHandler() = null

    override fun getSelectedText(flags: Int): CharSequence? {
        return if (!state.selection.isEmpty()) {
            state.text.substring(state.selection)
        } else {
            null
        }
    }

    override fun getTextAfterCursor(n: Int, flags: Int): CharSequence {
        val start = state.selection.end
        val end = (start + n).coerceAtMost(state.text.length)
        return state.text.substring(start, end)
    }

    override fun getTextBeforeCursor(n: Int, flags: Int): CharSequence {
        val end = state.selection.start
        val start = (end - n).coerceAtLeast(0)
        return state.text.substring(start, end)
    }

    override fun performContextMenuAction(id: Int): Boolean {
        when (id) {
            android.R.id.selectAll -> {
                updateState { state ->
                    TextInputState(
                        text = state.text,
                        selection = TextRange(0, state.text.length),
                        composition = TextRange.EMPTY,
                    )
                }
            }

            android.R.id.cut -> {
                val cutText = state.selectionText
                updateState(state.let { state ->
                    TextInputState(
                        text = state.text.removeRange(state.selection),
                        selection = TextRange(state.selection.start),
                        composition = state.composition - state.selection,
                    )
                })
                clipboardManager?.setPrimaryClip(ClipData.newPlainText(null, cutText))
            }

            android.R.id.copy -> {
                clipboardManager?.setPrimaryClip(ClipData.newPlainText(null, state.selectionText))
            }

            android.R.id.paste -> {
                clipboardManager?.primaryClip?.takeIf { it.itemCount > 0 }
                    ?.getItemAt(0)?.text?.toString()?.let { text ->
                        updateState(state.commitTextAsNewState(text, 1))
                    }
            }

            else -> return false
        }
        return true
    }

    override fun performEditorAction(editorAction: Int): Boolean {
        return false
    }

    override fun performPrivateCommand(action: String?, data: Bundle?): Boolean {
        return false
    }

    override fun reportFullscreenMode(enabled: Boolean): Boolean {
        if (!inputMethodManager.isFullscreenMode) {
            extractTextToken = null
            hasZeroExtractToken = false
        }
        return true
    }

    override fun requestCursorUpdates(cursorUpdateMode: Int): Boolean {
        return false
    }

    override fun sendKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
            if (event.action == KeyEvent.ACTION_UP) {
                return true
            }
            LWJGLCharSender.sendEnter()
        } else if (event.keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (event.action == KeyEvent.ACTION_UP) {
                return true
            }
            if (event.isShiftPressed) {
                updateState(TextInputState::doShiftLeft)
            } else {
                updateState(TextInputState::doArrowLeft)
            }
        } else if (event.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (event.action == KeyEvent.ACTION_UP) {
                return true
            }
            if (event.isShiftPressed) {
                updateState(TextInputState::doShiftRight)
            } else {
                updateState(TextInputState::doArrowRight)
            }
        } else if (event.keyCode == KeyEvent.KEYCODE_DEL) {
            if (event.action == KeyEvent.ACTION_UP) {
                return true
            }
            updateState(TextInputState::doBackspace)
        } else if (event.keyCode == KeyEvent.KEYCODE_FORWARD_DEL) {
            if (event.action == KeyEvent.ACTION_UP) {
                return true
            }
            updateState(TextInputState::doDelete)
        } else {
            LWJGLCharSender.sendOther(event)
        }
        return true
    }

    override fun setComposingRegion(start: Int, end: Int): Boolean {
        updateState(refresh = false) { currentState ->
            currentState.copy(
                composition = TextRange(start, end - start)
            )
        }
        return true
    }

    override fun setComposingText(text: CharSequence, newCursorPosition: Int): Boolean {
        updateState { currentState ->
            if (!currentState.composition.isEmpty()) {
                val newText = currentState.text.replaceRange(currentState.composition, text)
                val finalCursorPosition = when {
                    newCursorPosition > 0 -> currentState.composition.start + text.length + newCursorPosition - 1
                    else -> currentState.composition.start - newCursorPosition
                }.coerceIn(0..newText.length)
                TextInputState(
                    text = newText,
                    selection = TextRange(finalCursorPosition),
                    composition = TextRange(currentState.composition.start, text.length),
                )
            } else {
                val newText = currentState.text.replaceRange(currentState.selection, text)
                val finalCursorPosition = when {
                    newCursorPosition > 0 -> currentState.selection.start + text.length + newCursorPosition - 1
                    else -> currentState.selection.start - newCursorPosition
                }.coerceIn(0..newText.length)
                TextInputState(
                    text = newText,
                    selection = TextRange(finalCursorPosition),
                    composition = TextRange(currentState.selection.start, text.length),
                )
            }
        }
        return true
    }

    override fun setSelection(start: Int, end: Int): Boolean {
        updateState { currentState ->
            currentState.copy(
                selection = TextRange(start, end - start)
            )
        }
        return true
    }
}

private class TouchControllerInputModifierNode(
    var screenSize: IntSize,
    var onCursorRectUpdated: (IntRect?) -> Unit,
    var onInputAreaRectUpdated: (IntRect?) -> Unit,
) : Modifier.Node(), PlatformTextInputModifierNode {
    private val cursorRect = MutableStateFlow<IntRect?>(null)
    private val inputAreaRect = MutableStateFlow<IntRect?>(null)
    private val inputStateChannel = Channel<TextInputState?>()
    private val inputHandler = object : LauncherProxyClient.InputHandler {
        override fun updateState(textInputState: TextInputState?) {
            inputStateChannel.trySend(textInputState)
        }

        private fun FloatRect.toIntRect() = IntRect(
            left = (this.left * screenSize.width).toInt(),
            top = (this.top * screenSize.height).toInt(),
            right = ((this.left + this.width) * screenSize.width).toInt(),
            bottom = ((this.top + this.height) * screenSize.height).toInt(),
        )

        override fun updateCursor(cursorRect: FloatRect?) {
            val rect = cursorRect?.toIntRect()
            this@TouchControllerInputModifierNode.cursorRect.value = rect
            onCursorRectUpdated(rect)
        }

        override fun updateArea(inputAreaRect: FloatRect?) {
            val rect = inputAreaRect?.toIntRect()
            this@TouchControllerInputModifierNode.inputAreaRect.value = rect
            onInputAreaRectUpdated(rect)
        }
    }

    override fun onAttach() {
        coroutineScope.launch {
            proxyClient.collect { client ->
                if (client == null) {
                    return@collect
                }
                client.inputHandler = inputHandler
            }
        }
        coroutineScope.launch {
            var latestState: TextInputState?
            while (true) {
                latestState = inputStateChannel.receive()
                val notNullState = latestState ?: continue
                try {
                    coroutineScope {
                        establishTextInputSession {
                            val inputMethodManager =
                                view.context.getSystemService<InputMethodManager>()
                                    ?: error("InputMethodManager not supported")
                            val connection = TouchControllerInputConnection(
                                scope = this@coroutineScope,
                                view = view,
                                inputMethodManager = inputMethodManager,
                                initialState = notNullState,
                                onStateChanged = {
                                    proxyClient.value?.updateTextInputState(it)
                                },
                                cursorRect = cursorRect,
                                inputAreaRect = inputAreaRect,
                            )
                            launch {
                                while (true) {
                                    val state = inputStateChannel.receive() ?: break
                                    latestState = state
                                    connection.updateState(state)
                                }
                                this@establishTextInputSession.cancel()
                            }
                            startInputMethod { info ->
                                info.apply {
                                    EditorInfoCompat.setInitialSurroundingText(
                                        this,
                                        notNullState.text
                                    )
                                    initialSelStart = notNullState.selection.start
                                    initialSelEnd = notNullState.selection.end
                                    inputType =
                                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                                }
                                connection
                            }
                        }
                    }
                } catch (_: CancellationException) {
                }
            }
        }
    }
}

/**
 * 提供文本输入处理，为TouchController模组的控制代理提供信息
 */
@Composable
fun Modifier.touchControllerInputModifier(
    screenSize: IntSize,
    onCursorRectUpdated: (IntRect?) -> Unit = {},
    onInputAreaRectUpdated: (IntRect?) -> Unit = {},
) = this then TouchControllerInputModifier(screenSize, onCursorRectUpdated, onInputAreaRectUpdated)

/**
 * 单独捕获触摸事件，为TouchController模组的控制代理提供信息
 */
@Composable
fun Modifier.touchControllerTouchModifier(
    screenSize: IntSize,
) = this.pointerInput(Unit) {
    awaitPointerEventScope {
        val activePointers = mutableMapOf<PointerId, Int>()
        var nextPointerId = 1

        fun PointerInputChange.toProxyOffset(): Pair<Float, Float> {
            val normalizedX = position.x / screenSize.width
            val normalizedY = position.y / screenSize.height
            return Pair(normalizedX, normalizedY)
        }

        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val proxyClient = proxyClient.value
            event.changes.fastForEach { change ->
                if (change.isConsumed) return@fastForEach
                if (change.changedToDownIgnoreConsumed()) {
                    if (!activePointers.containsKey(change.id)) {
                        val pointerId = nextPointerId++
                        activePointers[change.id] = pointerId
                        val (x, y) = change.toProxyOffset()
                        proxyClient?.addPointer(pointerId, x, y)
                    }
                } else if (change.changedToUpIgnoreConsumed()) {
                    activePointers.remove(change.id)?.let { pointerId ->
                        proxyClient?.removePointer(pointerId)
                    }
                } else if (change.pressed && event.type == PointerEventType.Move) {
                    activePointers[change.id]?.let { pointerId ->
                        val (x, y) = change.toProxyOffset()
                        proxyClient?.addPointer(pointerId, x, y)
                    }
                }
            }
        }
    }
}
