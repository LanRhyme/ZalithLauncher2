package com.movtery.zalithlauncher.ui.control.mouse

import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.isBackPressed
import androidx.compose.ui.input.pointer.isForwardPressed
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalViewConfiguration
import com.movtery.zalithlauncher.setting.enums.MouseControlMode
import com.movtery.zalithlauncher.ui.components.FocusableBox
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 拖动状态数据类
 */
private data class DragState(
    var isDragging: Boolean = false,
    var longPressTriggered: Boolean = false,
    val startPosition: Offset
)

/**
 * 原始触摸控制模拟层
 * @param controlMode               控制模式：SLIDE（滑动控制）、CLICK（点击控制）
 * @param longPressTimeoutMillis    长按触发检测时长
 * @param requestPointerCapture     是否使用鼠标抓取方案
 * @param pointerIcon               实体指针图标
 * @param onTouch                   触摸到鼠标层
 * @param onMouse                   实体鼠标交互事件
 * @param onTap                     点击回调，参数是触摸点在控件内的绝对坐标
 * @param onLongPress               长按开始回调
 * @param onLongPressEnd            长按结束回调
 * @param onPointerMove             指针移动回调，参数在 SLIDE 模式下是指针位置，CLICK 模式下是手指当前位置
 * @param onMouseMove               实体鼠标指针移动回调
 * @param onMouseScroll             实体鼠标指针滚轮滑动
 * @param onMouseButton             实体鼠标指针按钮按下反馈
 * @param isMoveOnlyPointer         指针是否被父级标记为仅可滑动指针
 * @param onOccupiedPointer         占用指针回调
 * @param onReleasePointer          释放指针回调
 * @param inputChange               重新启动内部的 pointerInput 块，让触摸逻辑能够实时拿到最新的外部参数
 */
@Composable
fun TouchpadLayout(
    modifier: Modifier = Modifier,
    controlMode: MouseControlMode = MouseControlMode.SLIDE,
    longPressTimeoutMillis: Long = -1L,
    requestPointerCapture: Boolean = true,
    pointerIcon: PointerIcon = PointerIcon.Default,
    onTouch: () -> Unit = {},
    onMouse: () -> Unit = {},
    onTap: (Offset) -> Unit = {},
    onLongPress: () -> Unit = {},
    onLongPressEnd: () -> Unit = {},
    onPointerMove: (Offset) -> Unit = {},
    onMouseMove: (Offset) -> Unit = {},
    onMouseScroll: (Offset) -> Unit = {},
    onMouseButton: (button: Int, pressed: Boolean) -> Unit = { _, _ -> },
    isMoveOnlyPointer: (PointerId) -> Boolean = { false },
    onOccupiedPointer: (PointerId) -> Unit = {},
    onReleasePointer: (PointerId) -> Unit = {},
    inputChange: Array<out Any> = arrayOf(Unit),
    requestFocusKey: Any? = null
) {
    val viewConfig = LocalViewConfiguration.current
    val interactionSource = remember { MutableInteractionSource() }

    //确保 pointerInput 中总是调用到最新的回调，避免闭包捕获旧值
    val currentOnTouch by rememberUpdatedState(onTouch)
    val currentControlMode by rememberUpdatedState(controlMode)
    val currentLongPressTimeoutMillis by rememberUpdatedState(longPressTimeoutMillis)
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnLongPress by rememberUpdatedState(onLongPress)
    val currentOnLongPressEnd by rememberUpdatedState(onLongPressEnd)
    val currentOnPointerMove by rememberUpdatedState(onPointerMove)

    FocusableBox(
        modifier = modifier
            .hoverable(interactionSource)
            .pointerHoverIcon(pointerIcon)
            .pointerInput(*inputChange) {
                coroutineScope {
                    /** 所有被占用的指针 */
                    val occupiedPointers = mutableSetOf<PointerId>()
                    /** 当前正在被处理的指针 */
                    var activePointer: PointerId? = null
                    val longPressJobs = mutableMapOf<PointerId, Job>()
                    /** 每个指针的拖动状态 */
                    val dragStates = mutableMapOf<PointerId, DragState>()

                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()

                            event.changes
                                .filter { it.pressed && !it.previousPressed && !it.isConsumed && it.type == PointerType.Touch }
                                .forEach { change ->
                                    if (change.changedToDown()) {
                                        //刚触摸到屏幕时，触发触摸事件回调
                                        currentOnTouch()
                                    }

                                    val pointerId = change.id
                                    //是否被父级标记为仅处理滑动
                                    val isMoveOnly = isMoveOnlyPointer(pointerId)

                                    if (pointerId !in occupiedPointers) {
                                        onOccupiedPointer(pointerId)
                                        occupiedPointers.add(pointerId)
                                    }

                                    //如果没有活跃指针，则开始处理这个指针
                                    if (activePointer == null) {
                                        activePointer = pointerId

                                        dragStates[pointerId] = DragState(startPosition = change.position)

                                        if (!isMoveOnly && currentControlMode == MouseControlMode.SLIDE) {
                                            longPressJobs[pointerId] = launch {
                                                //只在滑动点击模式下进行长按计时
                                                val timeout = if (currentLongPressTimeoutMillis > 0) {
                                                    currentLongPressTimeoutMillis
                                                } else {
                                                    viewConfig.longPressTimeoutMillis
                                                }
                                                delay(timeout)

                                                //检查是否仍在处理此指针且未开始拖动
                                                if (activePointer == pointerId && dragStates[pointerId]?.isDragging != true) {
                                                    dragStates[pointerId]?.longPressTriggered = true
                                                    currentOnLongPress()
                                                }
                                            }
                                        }

                                        if (!isMoveOnly && currentControlMode == MouseControlMode.CLICK) {
                                            //点击模式下，如果触摸，无论如何都应该更新指针位置
                                            currentOnPointerMove(change.position)
                                        }
                                    }
                                }

                            //处理移动事件（仅处理活跃指针）
                            activePointer?.let { pointerId ->
                                event.changes
                                    .firstOrNull { it.id == pointerId && it.positionChanged() && !it.isConsumed }
                                    ?.let { moveChange ->
                                        val dragState = dragStates[pointerId] ?: return@let
                                        //是否被父级标记为仅处理滑动
                                        val isMoveOnly = isMoveOnlyPointer(pointerId)

                                        if (currentControlMode == MouseControlMode.SLIDE) {
                                            val distanceFromStart = (moveChange.position - dragState.startPosition).getDistance()

                                            if (distanceFromStart > viewConfig.touchSlop && !dragState.isDragging) {
                                                //超出了滑动检测距离，说明是真的在进行滑动
                                                dragState.isDragging = true
                                                longPressJobs.remove(pointerId)?.cancel() //取消长按计时
                                            }

                                            if (dragState.isDragging || dragState.longPressTriggered) {
                                                val delta = moveChange.positionChange()
                                                currentOnPointerMove(delta)
                                            }
                                        } else {
                                            if (!isMoveOnly && !dragState.longPressTriggered) {
                                                dragState.longPressTriggered = true
                                                longPressJobs.remove(pointerId)?.cancel()
                                                currentOnLongPress()
                                            }
                                            currentOnPointerMove(moveChange.position)
                                        }

                                        moveChange.consume()
                                    }
                            }

                            //释放
                            event.changes
                                .filter { !it.pressed && it.previousPressed && it.type == PointerType.Touch }
                                .forEach { change ->
                                    val pointerId = change.id
                                    //是否被父级标记为仅处理滑动
                                    val isMoveOnly = isMoveOnlyPointer(pointerId)

                                    longPressJobs.remove(pointerId)?.cancel()
                                    val dragState = dragStates.remove(pointerId)

                                    //如果是活跃指针，处理释放逻辑
                                    if (pointerId == activePointer) {
                                        if (!isMoveOnly) {
                                            if (dragState?.longPressTriggered == true) {
                                                currentOnLongPressEnd()
                                            } else {
                                                when (currentControlMode) {
                                                    MouseControlMode.SLIDE -> {
                                                        if (dragState?.isDragging != true) {
                                                            currentOnTap(change.position)
                                                        }
                                                    }
                                                    MouseControlMode.CLICK -> {
                                                        //未进入长按，算一次点击事件
                                                        currentOnTap(change.position)
                                                    }
                                                }
                                            }
                                        }

                                        activePointer = null
                                    }

                                    if (pointerId in occupiedPointers) {
                                        occupiedPointers.remove(pointerId)
                                        onReleasePointer(pointerId)
                                    }
                                }
                        }
                    }
                }
            }
            .then(
                Modifier.mouseEventModifier(
                    disabled = requestPointerCapture,
                    inputChange = inputChange,
                    onMouse = onMouse,
                    onMouseMove = onMouseMove,
                    onMouseScroll = onMouseScroll,
                    onMouseButton = onMouseButton
                )
            ),
        requestKey = requestFocusKey
    )

    SimpleMouseCapture(
        enabled = requestPointerCapture,
        onMouse = onMouse,
        onMouseMove = onMouseMove,
        onMouseScroll = onMouseScroll,
        onMouseButton = onMouseButton
    )
}

/**
 * 简单的实体鼠标捕获层
 * @param enabled                   是否使用鼠标抓取方案
 * @param onMouse                   实体鼠标开始响应事件的回调
 * @param onMouseMove               实体鼠标指针移动回调
 * @param onMouseScroll             实体鼠标指针滚轮滑动
 * @param onMouseButton             实体鼠标指针按钮按下反馈
 */
@Composable
private fun SimpleMouseCapture(
    enabled: Boolean,
    onMouse: () -> Unit,
    onMouseMove: (Offset) -> Unit,
    onMouseScroll: (Offset) -> Unit,
    onMouseButton: (button: Int, pressed: Boolean) -> Unit
) {
    val view = LocalView.current
    val currentOnMouse by rememberUpdatedState(onMouse)
    val currentOnMouseMove by rememberUpdatedState(onMouseMove)
    val currentOnMouseScroll by rememberUpdatedState(onMouseScroll)
    val currentOnMouseButton by rememberUpdatedState(onMouseButton)

    DisposableEffect(view, enabled) {
        view.setOnCapturedPointerListener(null)

        val focusListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (enabled && hasFocus) {
                view.requestPointerCapture()
            }
        }
        view.viewTreeObserver.addOnWindowFocusChangeListener(focusListener)

        if (enabled) {
            view.requestFocus()
            if (view.hasWindowFocus()) {
                view.requestPointerCapture()
            }

            val pointerListener = View.OnCapturedPointerListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_HOVER_MOVE, MotionEvent.ACTION_MOVE -> {
                        var deltaX = 0f
                        var deltaY = 0f

                        val relX = event.getAxisValue(MotionEvent.AXIS_RELATIVE_X)
                        val relY = event.getAxisValue(MotionEvent.AXIS_RELATIVE_Y)
                        deltaX += if (relX != 0f) relX else event.x
                        deltaY += if (relY != 0f) relY else event.y

                        val historySize = event.historySize
                        for (i in 0 until historySize) {
                            deltaX += event.getHistoricalAxisValue(MotionEvent.AXIS_RELATIVE_X, i)
                            deltaY += event.getHistoricalAxisValue(MotionEvent.AXIS_RELATIVE_Y, i)
                        }

                        currentOnMouse()
                        currentOnMouseMove(Offset(deltaX, deltaY))
                        true
                    }
                    MotionEvent.ACTION_SCROLL -> {
                        currentOnMouseScroll(
                            Offset(
                                event.getAxisValue(MotionEvent.AXIS_HSCROLL),
                                event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                            )
                        )
                        true
                    }
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_BUTTON_PRESS -> {
                        currentOnMouseButton(event.actionButton, true)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_BUTTON_RELEASE -> {
                        currentOnMouseButton(event.actionButton, false)
                        true
                    }
                    else -> false
                }
            }

            view.setOnCapturedPointerListener(pointerListener)
        } else {
            view.releasePointerCapture()
            view.setOnCapturedPointerListener(null)
        }

        onDispose {
            view.viewTreeObserver.removeOnWindowFocusChangeListener(focusListener)
            view.setOnCapturedPointerListener(null)
        }
    }
}

/**
 * 实体鼠标指针事件监听
 * @param disabled 是否禁用
 */
private fun Modifier.mouseEventModifier(
    disabled: Boolean,
    inputChange: Array<out Any> = arrayOf(Unit),
    onMouse: () -> Unit,
    onMouseMove: (Offset) -> Unit,
    onMouseScroll: (Offset) -> Unit,
    onMouseButton: (Int, Boolean) -> Unit
) = this.pointerInput(*inputChange, disabled) {
    val previousButtonStates = mutableMapOf<Int, Boolean>()

    if (disabled) return@pointerInput

    awaitEachGesture {
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull()

            val pointerType = change?.type
            if (pointerType != PointerType.Mouse) {
                //过滤掉不是实体鼠标的类型
                continue
            }

            if (event.type == PointerEventType.Move) {
                onMouse()
                onMouseMove(change.position)
            }

            //滚动，但是方向要进行取反
            if (event.type == PointerEventType.Scroll) {
                onMouseScroll(-change.scrollDelta)
            }

            detectButtonChanges(previousButtonStates, event, onMouseButton)

            event.changes.forEach { it.consume() }
        }
    }
}

private fun detectButtonChanges(
    previousButtonStates: MutableMap<Int, Boolean>,
    event: PointerEvent,
    onMouseButton: (Int, Boolean) -> Unit
) {
    val buttons = event.buttons

    val buttonStates = mapOf(
        MotionEvent.BUTTON_PRIMARY to buttons.isPrimaryPressed,
        MotionEvent.BUTTON_SECONDARY to buttons.isSecondaryPressed,
        MotionEvent.BUTTON_TERTIARY to buttons.isTertiaryPressed,
        MotionEvent.BUTTON_BACK to buttons.isBackPressed,
        MotionEvent.BUTTON_FORWARD to buttons.isForwardPressed
    )

    for ((button, isPressed) in buttonStates) {
        val previousPressed = previousButtonStates[button] ?: false
        if (previousPressed != isPressed) {
            onMouseButton(button, isPressed)
        }
    }

    previousButtonStates.clear()
    previousButtonStates.putAll(buttonStates)
}