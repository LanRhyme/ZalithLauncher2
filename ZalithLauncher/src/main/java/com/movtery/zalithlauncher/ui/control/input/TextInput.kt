package com.movtery.zalithlauncher.ui.control.input

import android.os.Bundle
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

/**
 * 一个用于处理 UI 元素文本输入的可组合修饰符
 *
 * @param mode 控制文本输入启用或禁用的 [TextInputMode]
 * @param sender 用于将字符发送到游戏的 [CharacterSenderStrategy]
 */
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

                        val connection = InputConnectionImpl(view, inputMethodManager)

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
        private val imm: InputMethodManager
    ) : InputConnection {
        private val textBuffer = StringBuilder()
        private var cursorPosition = 0
        private var composingStart = -1
        private var composingEnd = -1

        override fun commitText(text: CharSequence, newCursorPosition: Int): Boolean {
            finishComposingText()

            //插入提交的文本
            textBuffer.insert(cursorPosition, text)
            cursorPosition += text.length

            val newText = text.toString()
            newText.forEach { char -> sender.sendChar(char) }

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
            //如果有活动的组合文本，先删除它
            if (composingStart >= 0 && composingEnd > composingStart) {
                textBuffer.delete(composingStart, composingEnd)
                cursorPosition = composingStart
            } else {
                //如果没有活动的组合文本，但光标位置不在缓冲区末尾，可能需要调整
                //这确保输入法总是在正确的位置插入组合文本
                composingStart = cursorPosition
            }

            //插入新的组合文本
            textBuffer.insert(cursorPosition, text)
            composingStart = cursorPosition
            composingEnd = cursorPosition + text.length
            cursorPosition = if (newCursorPosition > 0) {
                composingStart + newCursorPosition
            } else {
                composingEnd + newCursorPosition
            }.coerceIn(composingStart, composingEnd)

            updateInputMethodState()
            return true
        }

        override fun finishComposingText(): Boolean {
            if (composingStart >= 0 && composingEnd > composingStart) {
                //提交组合文本
                val composedText = textBuffer.substring(composingStart, composingEnd)
                composedText.forEach { char -> sender.sendChar(char) }

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
                repeat(beforeLength) { sender.sendBackspace() }
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
                repeat(beforeLength) { sender.sendBackspace() }
                updateInputMethodState()
            }
            return true
        }

        override fun beginBatchEdit(): Boolean = true
        override fun endBatchEdit(): Boolean = true
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

        override fun getExtractedText(request: ExtractedTextRequest?, flags: Int): ExtractedText? {
            return ExtractedText().apply {
                text = textBuffer
                startOffset = 0
                partialStartOffset = -1
                partialEndOffset = -1
                selectionStart = cursorPosition
                selectionEnd = cursorPosition
            }
        }

        override fun getHandler() = null

        private fun updateCursorAnchorInfo() {
            imm.updateCursorAnchorInfo(
                view,
                CursorAnchorInfo.Builder().apply {
                    setSelectionRange(cursorPosition, cursorPosition)
                    //设置组合文本范围
                    if (composingStart >= 0 && composingEnd > composingStart) {
                        setComposingText(composingStart, textBuffer.substring(composingStart, composingEnd))
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