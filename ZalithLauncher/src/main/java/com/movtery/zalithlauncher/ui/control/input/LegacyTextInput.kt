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

package com.movtery.zalithlauncher.ui.control.input

import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.CorrectionInfo
import android.view.inputmethod.CursorAnchorInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputContentInfo
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.PlatformTextInputModifierNode
import androidx.compose.ui.platform.establishTextInputSession
import androidx.compose.ui.unit.IntRect
import androidx.core.content.getSystemService
import com.movtery.zalithlauncher.game.input.CharacterSenderStrategy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.text.forEach

/**
 * 一个用于处理 UI 元素文本输入的可组合修饰符
 *
 * @param mode 控制文本输入启用或禁用的 [TextInputMode]
 * @param sender 用于将字符发送到游戏的 [CharacterSenderStrategy]
 */
@Deprecated("因兼容性问题，现已改用输入栏UI代理输入法输入，此处代码已不再使用，仅作参考")
@Composable
fun Modifier.textInputHandler(
    mode: TextInputMode,
    sender: CharacterSenderStrategy,
    onCloseInputMethod: () -> Unit = {}
): Modifier {
    OnKeyboardClosed {
        if (mode == TextInputMode.ENABLE) {
            onCloseInputMethod()
        }
    }
    val textMode by rememberUpdatedState(mode)
    val onCloseInputMethod1 by rememberUpdatedState(onCloseInputMethod)
    return this then TextInputModifier(sender, textMode, onCloseInputMethod1)
}

private data class TextInputModifier(
    private val sender: CharacterSenderStrategy,
    private val textMode: TextInputMode,
    private val onCloseInputMethod: () -> Unit = {}
) : ModifierNodeElement<TextInputNode>() {
    override fun create() = TextInputNode(sender, textMode, onCloseInputMethod)
    override fun update(node: TextInputNode) {
        node.update(sender, textMode, onCloseInputMethod)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "simulatorTextInputCore"
    }
}

/**
 * 使用 Android 的输入法引擎（IME）来捕获文本输入
 *
 * 该类作为 Compose UI 框架与底层 Android 文本输入系统之间的桥梁
 * 它建立文本输入会话，配置编辑器信息（例如，输入类型、IME 操作），
 * 并提供 [InputConnection] 来处理文本提交、按键事件以及其他 IME 交互
 *
 * @param sender 用于发送处理后字符的 [CharacterSenderStrategy]
 */
private class TextInputNode(
    private var sender: CharacterSenderStrategy,
    private var textInputMode: TextInputMode,
    private var onCloseInputMethod: () -> Unit
) : Modifier.Node(), PlatformTextInputModifierNode {
    private var session: Job? = null
    private val fakeCursorRect = IntRect(100, 500, 100, 550)

    override fun onAttach() {
        if (textInputMode == TextInputMode.ENABLE) {
            session = coroutineScope.launch {
                try {
                    establishTextInputSession {
                        val inputMethodManager = view.context.getSystemService<InputMethodManager>()
                            ?: error("InputMethodManager not supported")

                        val inputMethodIdentifier = Settings.Secure.getString(
                            view.context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD
                        )

                        val connection = InputConnectionImpl(view, inputMethodManager, inputMethodIdentifier)

                        inputMethodManager.updateCursorAnchorInfo(
                            view,
                            CursorAnchorInfo.Builder().apply {
                                setSelectionRange(0, 0)
                                setInsertionMarkerLocation(
                                    fakeCursorRect.left.toFloat(),
                                    fakeCursorRect.top.toFloat(),
                                    fakeCursorRect.right.toFloat(),
                                    fakeCursorRect.bottom.toFloat(),
                                    CursorAnchorInfo.FLAG_HAS_VISIBLE_REGION
                                )
                                setMatrix(view.matrix)
                            }.build()
                        )

                        startInputMethod { info ->
                            info.inputType = InputType.TYPE_CLASS_TEXT or
                                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                                    InputType.TYPE_TEXT_VARIATION_NORMAL
                            info.imeOptions = EditorInfo.IME_ACTION_DONE or
                                    //尽量不要进入全屏模式
                                    EditorInfo.IME_FLAG_NO_FULLSCREEN or
                                    //尽量不要显示额外的辅助UI
                                    EditorInfo.IME_FLAG_NO_EXTRACT_UI

                            info.packageName = view.context.packageName
                            info.fieldId = view.id

                            info.initialSelStart = 0
                            info.initialSelEnd = 0

                            connection
                        }
                    }
                } catch (_: CancellationException) {
                }
            }
        }
    }

    private fun stopInput() {
        session?.cancel()
        session = null
    }

    /**
     * 更新 [sender] 和 [textInputMode] 的值，并重新启动
     */
    fun update(
        sender: CharacterSenderStrategy,
        textInputMode: TextInputMode,
        onCloseInputMethod: () -> Unit
    ) {
        this.sender = sender
        this.onCloseInputMethod = onCloseInputMethod
        if (this.textInputMode != textInputMode) {
            this.textInputMode = textInputMode
            stopInput()
            if (textInputMode == TextInputMode.ENABLE) {
                onAttach() //重新启动
            }
        } else {
            this.textInputMode = textInputMode
        }
    }

    /**
     * 处理来自 IME 的文本输入和按键事件
     * 它将收到的字符和关键操作转换为通过提供的 [CharacterSenderStrategy] 发送的相应操作
     *
     * 该类重写 [InputConnection] 中的各种方法来处理文本提交、按键事件、撰写文本等
     * 大多数未实现的方法都返回默认值或执行无操作操作，因为它们对于此特定用例而言不是必需的
     */
    private inner class InputConnectionImpl(
        private val view: View,
        private val imm: InputMethodManager,
        private val inputMethodIdentifier: String
    ) : InputConnection {
        private val textBuffer = StringBuilder()
        private var cursorPosition = 0
        private var composingStart = -1
        private var composingEnd = -1

        private var inBatchEdit = false
        private var pendingBackspaceCount = 0
        private var pendingTextToSend = StringBuilder()

        private val isMicrosoftSwiftKey: Boolean
            get() = inputMethodIdentifier.contains("com.microsoft.swiftkey") ||
                    inputMethodIdentifier.contains("com.touchtype.swiftkey") ||
                    inputMethodIdentifier.contains("swiftkey")

        /**
         * 向游戏发送文本输入
         */
        private fun sendText(text: String) {
            text.forEach { char -> sender.sendChar(char) }
            if (isMicrosoftSwiftKey) {
                //发送文本之后，针对 Microsoft SwiftKey，应该完全清除缓冲区
                cursorPosition = 0
                textBuffer.clear()
            }
        }

        /**
         * 批量编辑结束时，发送待处理的文本
         */
        private fun flushPendingText() {
            repeat(pendingBackspaceCount) { sender.sendBackspace() }
            pendingBackspaceCount = 0

            if (pendingTextToSend.isNotEmpty()) {
                sendText(pendingTextToSend.toString())
                pendingTextToSend.clear()
            }
        }

        override fun commitText(text: CharSequence, newCursorPosition: Int): Boolean {
            var composingLength = 0
            //如果当前有组合文本，先删除组合区
            if (composingStart in 0..<composingEnd) {
                val safeStart = composingStart.coerceIn(0, textBuffer.length)
                val safeEnd = composingEnd.coerceIn(0, textBuffer.length)
                composingLength = safeEnd - safeStart
                if (safeStart < safeEnd) {
                    textBuffer.delete(safeStart, safeEnd)
                    cursorPosition = safeStart
                }
                composingStart = -1
                composingEnd = -1
            }

            //插入提交的文本
            textBuffer.insert(cursorPosition, text)
            cursorPosition += text.length

            val newText = text.toString()

            if (isMicrosoftSwiftKey) {
                sendText(newText)
            } else {
                if (inBatchEdit) {
                    if (composingLength > 0) {
                        pendingBackspaceCount += composingLength
                    }
                    pendingTextToSend.append(newText)
                } else {
                    if (composingLength > 0) {
                        repeat(composingLength) { sender.sendBackspace() }
                    }
                    if (newText.isNotEmpty()) {
                        sendText(newText)
                    }
                }
            }

            updateInputMethodState()
            return true
        }

        override fun sendKeyEvent(event: KeyEvent): Boolean {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    when (event.keyCode) {
                        KeyEvent.KEYCODE_ENTER -> {
                            sender.sendEnter()
                            onCloseInputMethod()
                        }
                        KeyEvent.KEYCODE_DEL -> {
                            if (cursorPosition > 0) {
                                textBuffer.deleteCharAt(cursorPosition - 1)
                                cursorPosition--
                                updateInputMethodState()
                            }
                            sender.sendBackspace()
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> sender.sendLeft()
                        KeyEvent.KEYCODE_DPAD_RIGHT -> sender.sendRight()
                        KeyEvent.KEYCODE_DPAD_UP -> sender.sendUp()
                        KeyEvent.KEYCODE_DPAD_DOWN -> sender.sendDown()
                        else -> {
                            if (event.unicodeChar != 0) {
                                val char = event.unicodeChar.toChar()
                                textBuffer.insert(cursorPosition, char)
                                cursorPosition++
                                updateInputMethodState()
                                sender.sendChar(char)
                            } else {
                                sender.sendOther(event)
                            }
                        }
                    }
                }
            }
            return true
        }

        override fun setComposingRegion(start: Int, end: Int): Boolean {
            if (start in 0..textBuffer.length && end in 0..textBuffer.length && start <= end) {
                composingStart = start
                composingEnd = end
                updateInputMethodState()
                return true
            }
            return false
        }

        override fun getTextBeforeCursor(length: Int, flags: Int): CharSequence {
            val start = max(0, cursorPosition - length)
            return textBuffer.substring(start, cursorPosition)
        }

        override fun getTextAfterCursor(length: Int, flags: Int): CharSequence {
            val end = min(textBuffer.length, cursorPosition + length)
            return textBuffer.substring(cursorPosition, end)
        }

        override fun getSelectedText(p0: Int): CharSequence? = null

        override fun setComposingText(text: CharSequence, newCursorPosition: Int): Boolean {
            //删除当前组合区
            if (composingStart in 0..<composingEnd) {
                val safeStart = composingStart.coerceIn(0, textBuffer.length)
                val safeEnd = composingEnd.coerceIn(0, textBuffer.length)
                if (isMicrosoftSwiftKey || safeStart < safeEnd) {
                    textBuffer.delete(safeStart, safeEnd)
                    cursorPosition = safeStart
                }
            } else {
                //如果没有明确的组合区，但输入法重新开始组合，删除可能的末尾重复
                if (cursorPosition < textBuffer.length) {
                    textBuffer.delete(cursorPosition, textBuffer.length)
                }
                composingStart = cursorPosition
            }

            //插入新的组合文本
            textBuffer.insert(cursorPosition, text)
            composingStart = cursorPosition
            composingEnd = composingStart + text.length
            cursorPosition = composingEnd

            updateInputMethodState()
            return true
        }

        override fun finishComposingText(): Boolean {
            if (composingStart in 0..<composingEnd) {
                //提交组合文本
                val safeStart = composingStart.coerceIn(0, textBuffer.length)
                val safeEnd = composingEnd.coerceIn(0, textBuffer.length)
                if (safeStart < safeEnd) {
                    val composedText = textBuffer.substring(safeStart, safeEnd)
                    if (isMicrosoftSwiftKey) {
                        sendText(composedText)
                    } else {
                        if (inBatchEdit) {
                            pendingTextToSend.append(composedText)
                        } else {
                            sendText(composedText)
                        }
                    }
                }

                composingStart = -1
                composingEnd = -1
            }

            updateInputMethodState()
            return true
        }

        override fun setSelection(start: Int, end: Int): Boolean {
            if (start in 0..textBuffer.length && end in 0..textBuffer.length) {
                cursorPosition = end //只关心光标位置
                updateInputMethodState()
                return true
            }
            return false
        }

        override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
            val deleteStart = max(0, cursorPosition - beforeLength)
            val deleteEnd = cursorPosition
            if (deleteStart < deleteEnd) {
                textBuffer.delete(deleteStart, deleteEnd)
                cursorPosition = deleteStart
                if (isMicrosoftSwiftKey || !inBatchEdit) {
                    repeat(beforeLength) { sender.sendBackspace() }
                } else {
                    pendingBackspaceCount += beforeLength
                }
            }
            updateInputMethodState()
            return true
        }

        override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean {
            val deleteStart = max(0, cursorPosition - beforeLength)
            val deleteEnd = cursorPosition
            if (deleteStart < deleteEnd) {
                textBuffer.delete(deleteStart, deleteEnd)
                cursorPosition = deleteStart
                if (isMicrosoftSwiftKey) {
                    repeat(beforeLength) { sender.sendBackspace() }
                }
                updateInputMethodState()
            }
            return true
        }

        override fun beginBatchEdit(): Boolean {
            if (!isMicrosoftSwiftKey) {
                inBatchEdit = true
                //重置待处理状态
                pendingBackspaceCount = 0
                pendingTextToSend.clear()
            }
            return true
        }

        override fun endBatchEdit(): Boolean {
            if (!isMicrosoftSwiftKey) {
                inBatchEdit = false
                //批量编辑结束，发送积累的操作
                flushPendingText()
            }
            return true
        }

        override fun clearMetaKeyStates(p0: Int): Boolean = true
        override fun closeConnection() {}
        override fun commitCompletion(p0: CompletionInfo?): Boolean = false
        override fun commitContent(p0: InputContentInfo, p1: Int, p2: Bundle?): Boolean = false
        override fun commitCorrection(p0: CorrectionInfo?): Boolean = false

        override fun performEditorAction(editorAction: Int): Boolean {
            //用户点击了编辑器的操作按钮（可以视为用户按下回车）
            sender.sendEnter()
            onCloseInputMethod()
            return true
        }

        override fun performContextMenuAction(p0: Int): Boolean = false
        override fun performPrivateCommand(p0: String?, p1: Bundle?): Boolean = false
        override fun reportFullscreenMode(p0: Boolean): Boolean = true

        override fun requestCursorUpdates(cursorUpdateMode: Int): Boolean {
            if (cursorUpdateMode and InputConnection.CURSOR_UPDATE_IMMEDIATE != 0) {
                updateCursorAnchorInfo()
                return true
            }
            return false
        }

        override fun getCursorCapsMode(p0: Int): Int = 0

        override fun getExtractedText(request: ExtractedTextRequest?, flags: Int): ExtractedText {
            return ExtractedText().apply {
                text = textBuffer
                startOffset = 0
                partialStartOffset = -1
                partialEndOffset = -1
                selectionStart = cursorPosition
                selectionEnd = cursorPosition
            }
        }

        override fun getHandler(): Handler? = null

        private fun updateCursorAnchorInfo() {
            imm.updateCursorAnchorInfo(
                view,
                CursorAnchorInfo.Builder().apply {
                    setSelectionRange(cursorPosition, cursorPosition)
                    //设置组合文本范围
                    if (composingStart in 0..<composingEnd) {
                        val safeStart = composingStart.coerceIn(0, textBuffer.length)
                        val safeEnd = composingEnd.coerceIn(0, textBuffer.length)
                        if (safeStart < safeEnd) {
                            setComposingText(safeStart, textBuffer.substring(safeStart, safeEnd))
                        }
                    }
                    setInsertionMarkerLocation(
                        fakeCursorRect.left.toFloat(),
                        fakeCursorRect.top.toFloat(),
                        fakeCursorRect.right.toFloat(),
                        fakeCursorRect.bottom.toFloat(),
                        CursorAnchorInfo.FLAG_HAS_VISIBLE_REGION
                    )
                    setMatrix(view.matrix)
                }.build()
            )
        }

        private fun updateInputMethodState() {
            imm.updateSelection(
                view,
                cursorPosition, cursorPosition,
                composingStart, composingEnd
            )
            updateCursorAnchorInfo()
        }
    }
}