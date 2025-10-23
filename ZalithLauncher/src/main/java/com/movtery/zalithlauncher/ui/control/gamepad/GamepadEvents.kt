package com.movtery.zalithlauncher.ui.control.gamepad

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.zalithlauncher.game.keycodes.ControlEventKeycode
import com.movtery.zalithlauncher.game.keycodes.Lwjgl2Keycode
import com.movtery.zalithlauncher.game.keycodes.MinecraftKeyBindingMapper
import com.movtery.zalithlauncher.game.launch.MCOptions
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SCROLL_DOWN_SINGLE
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SCROLL_UP_SINGLE
import com.movtery.zalithlauncher.viewmodel.GamepadRemapperViewModel
import com.movtery.zalithlauncher.viewmodel.GamepadViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.filterIsInstance

/**
 * 简单的手柄摇杆、按键事件捕获层
 */
@Composable
fun SimpleGamepadCapture(
    gamepadViewModel: GamepadViewModel,
    inGame: Boolean
) {
    val view = LocalView.current
    val remapperViewModel: GamepadRemapperViewModel = viewModel()

    GamepadRemapperDialog(
        operation = remapperViewModel.uiOperation,
        changeOperation = { remapperViewModel.uiOperation = it },
        remapperViewModel = remapperViewModel,
        steps = buildRemapperSteps(
            a = true, b = true, x = true, y = true,
            start = true, select = true,
            leftJoystick = true, leftJoystickButton = true,
            rightJoystick = true, rightJoystickButton = true,
            leftShoulder = true, rightShoulder = true,
            leftTrigger = true, rightTrigger = true,
            dpad = true
        )
    )

    //是否正在绑定键值
    val isBinding = remember(remapperViewModel.uiOperation) { remapperViewModel.uiOperation != GamepadRemapOperation.None }

    DisposableEffect(view, gamepadViewModel, isBinding) {
        val motionListener = View.OnGenericMotionListener { _, event ->
            if (isBinding) {
                remapperViewModel.sendEvent(
                    GamepadRemapperViewModel.Event.Axis(event)
                )
                true
            } else if (event.isGamepadEvent() && event.action == MotionEvent.ACTION_MOVE) {
                val deviceName = event.getDeviceName()
                val remapper = remapperViewModel.findMapping(deviceName)
                if (remapper == null) {
                    remapperViewModel.startRemapperUI(deviceName)
                } else {
                    remapper.handleMotionEventInput(event, gamepadViewModel)
                }
                true
            } else false
        }

        val keyListener = View.OnKeyListener { _, keyCode, keyEvent ->
            if (keyEvent.isFromGamepad()) {
                if (isBinding) {
                    remapperViewModel.sendEvent(
                        GamepadRemapperViewModel.Event.Button(keyCode, keyEvent)
                    )
                } else {
                    val deviceName = keyEvent.getDeviceName()
                    val remapper = remapperViewModel.findMapping(deviceName)
                    if (remapper == null) {
                        remapperViewModel.startRemapperUI(deviceName)
                    } else {
                        remapper.handleKeyEventInput(keyEvent, gamepadViewModel)
                    }
                }
                true
            } else false
        }

        view.setOnGenericMotionListener(motionListener)
        view.setOnKeyListener(keyListener)

        onDispose {
            view.setOnGenericMotionListener(null)
            view.setOnKeyListener(null)
        }
    }

    val inGame1 by rememberUpdatedState(inGame)
    LaunchedEffect(isBinding, gamepadViewModel.gamepadEngaged) {
        if (!isBinding && gamepadViewModel.gamepadEngaged) {
            while (true) {
                try {
                    ensureActive()
                    gamepadViewModel.pollJoystick(inGame1)
                    delay(16L) //~60fps
                } catch (_: CancellationException) {
                    break
                }
            }
        }
    }
}

/**
 * 统一实现的手柄按键事件监听器
 * @param isGrabbing 用于判断是否处于游戏中，区分游戏内、菜单内的按键绑定
 * @param onKeyEvent 键盘映射事件回调
 */
@Composable
fun GamepadKeyListener(
    gamepadViewModel: GamepadViewModel,
    isGrabbing: Boolean,
    onKeyEvent: (targets: List<ClickEvent>, pressed: Boolean) -> Unit
) {
    fun guessEvent(event: String): ClickEvent {
        return when (event) {
            ControlEventKeycode.GLFW_MOUSE_BUTTON_LEFT,
            ControlEventKeycode.GLFW_MOUSE_BUTTON_RIGHT,
            ControlEventKeycode.GLFW_MOUSE_BUTTON_MIDDLE
                -> ClickEvent(type = ClickEvent.Type.LauncherEvent, event)

            SPECIAL_KEY_MOUSE_SCROLL_UP -> ClickEvent(type = ClickEvent.Type.LauncherEvent, LAUNCHER_EVENT_SCROLL_UP_SINGLE)
            SPECIAL_KEY_MOUSE_SCROLL_DOWN -> ClickEvent(type = ClickEvent.Type.LauncherEvent, LAUNCHER_EVENT_SCROLL_DOWN_SINGLE)

            else -> ClickEvent(type = ClickEvent.Type.Key, event)
        }
    }

    val inGame by rememberUpdatedState(isGrabbing)
    val currentOnKeyEvent by rememberUpdatedState(onKeyEvent)

    val lastPressKey = remember { mutableStateMapOf<Int, List<ClickEvent>>() }
    val lastPressDpad = remember { mutableStateMapOf<GamepadViewModel.DpadDirection, List<ClickEvent>>() }

    LaunchedEffect(gamepadViewModel) {
        gamepadViewModel.events.collect { event ->
            when (event) {
                is GamepadViewModel.Event.Button -> {
                    if (!event.pressed) {
                        //松开时使用之前记录的按下事件
                        lastPressKey[event.code]?.let { lastEvents ->
                            currentOnKeyEvent(lastEvents, false)
                            lastPressKey.remove(event.code)
                        }
                    } else {
                        gamepadViewModel.findByCode(event.code, inGame)?.let { targets ->
                            val currentEvents = targets.map { guessEvent(it) }
                            lastPressKey[event.code] = currentEvents
                            currentOnKeyEvent(currentEvents, true)
                        }
                    }
                }
                is GamepadViewModel.Event.Dpad -> {
                    if (!event.pressed) {
                        lastPressDpad[event.direction]?.let { lastEvents ->
                            currentOnKeyEvent(lastEvents, false)
                            lastPressDpad.remove(event.direction)
                        }
                    } else {
                        gamepadViewModel.findByDpad(event.direction, inGame)?.let { targets ->
                            val currentEvents = targets.map { guessEvent(it) }
                            lastPressDpad[event.direction] = currentEvents
                            currentOnKeyEvent(currentEvents, true)
                        }
                    }
                }
                else -> null
            }
        }
    }
}

/**
 * 统一实现的手柄摇杆控制视角/鼠标指针的事件监听器
 * @param isGrabbing 判断当前是否在游戏中，若在游戏内，则根据事件中的摇杆类型判定以哪个摇杆的偏移量操作视角
 *                   若在游戏外，则所有摇杆都支持返回偏移量（操控鼠标指针）
 */
@Composable
fun GamepadStickCameraListener(
    gamepadViewModel: GamepadViewModel,
    isGrabbing: Boolean,
    onOffsetEvent: (Offset) -> Unit
) {
    val currentIsGrabbing by rememberUpdatedState(isGrabbing)
    val joystickControlMode by rememberUpdatedState(AllSettings.joystickControlMode.state)
    val onOffsetEvent1 by rememberUpdatedState(onOffsetEvent)
    LaunchedEffect(gamepadViewModel) {
        gamepadViewModel.events
            .filterIsInstance<GamepadViewModel.Event.StickOffset>()
            .collect { event ->
            if (currentIsGrabbing) {
                val cameraStick = when (joystickControlMode) {
                    JoystickMode.RightMovement -> JoystickType.Left
                    JoystickMode.LeftMovement -> JoystickType.Right
                }
                if (event.joystickType == cameraStick) {
                    onOffsetEvent1(event.offset)
                }
            } else {
                onOffsetEvent1(event.offset)
            }
        }
    }
}

/**
 * 统一实现的手柄摇杆控制玩家移动的事件监听器
 * @param isGrabbing 判断当前是否在游戏中，若在游戏内，则根据事件中的摇杆类型判定以哪个摇杆的方向，控制玩家移动
 * @param onKeyEvent 回调根据 options.txt 内保存的移动键键值转化的控制事件
 */
@Composable
fun GamepadStickMovementListener(
    gamepadViewModel: GamepadViewModel,
    isGrabbing: Boolean,
    onKeyEvent: (event: ClickEvent, pressed: Boolean) -> Unit
) {
    val currentIsGrabbing by rememberUpdatedState(isGrabbing)
    val joystickControlMode by rememberUpdatedState(AllSettings.joystickControlMode.state)
    val currentOnKeyEvent by rememberUpdatedState(onKeyEvent)

    val directionMapping = remember {
        mapOf(
            Joystick.Direction.East to listOf(MOVEMENT_RIGHT to true),
            Joystick.Direction.NorthEast to listOf(
                MOVEMENT_FORWARD to true,
                MOVEMENT_RIGHT to true
            ),
            Joystick.Direction.North to listOf(MOVEMENT_FORWARD to true),
            Joystick.Direction.NorthWest to listOf(
                MOVEMENT_FORWARD to true,
                MOVEMENT_LEFT to true
            ),
            Joystick.Direction.West to listOf(MOVEMENT_LEFT to true),
            Joystick.Direction.SouthWest to listOf(
                MOVEMENT_BACK to true,
                MOVEMENT_LEFT to true
            ),
            Joystick.Direction.South to listOf(MOVEMENT_BACK to true),
            Joystick.Direction.SouthEast to listOf(
                MOVEMENT_BACK to true,
                MOVEMENT_RIGHT to true
            ),
            Joystick.Direction.None to emptyList()
        )
    }

    val allAction = remember {
        listOf(MOVEMENT_FORWARD, MOVEMENT_BACK, MOVEMENT_LEFT, MOVEMENT_RIGHT)
    }

    //缓存已按下的事件，目的是当游戏进入菜单后，能够清除状态
    //避免回到游戏时出现一直移动的问题
    val allPressEvent = remember { mutableStateSetOf<String>() }

    fun sendKeyEvent(
        mcKey: String,
        pressed: Boolean
    ) {
        val binding = MCOptions.get(mcKey) ?: return
        if (binding.startsWith("key.")) {
            MinecraftKeyBindingMapper.getContentEvent(binding)
        } else {
            binding.toIntOrNull()?.let { lwjgl2Code ->
                //MC旧版本直接存了LWJGL2的键值
                //将旧版本LWJGL2的键码转换为控制事件标识
                Lwjgl2Keycode.lwjgl2ToControlEvent(lwjgl2Code)
            }
        }?.let { event ->
            if (pressed) {
                allPressEvent.add(event)
            } else {
                allPressEvent.remove(event)
            }

            currentOnKeyEvent(
                ClickEvent(type = ClickEvent.Type.Key, event),
                pressed
            )
        }
    }

    fun clearPressedEvent() {
        if (allPressEvent.isNotEmpty()) {
            allPressEvent.forEach { event ->
                currentOnKeyEvent(
                    ClickEvent(type = ClickEvent.Type.Key, event),
                    false
                )
            }
            allPressEvent.clear()
        }
    }

    LaunchedEffect(gamepadViewModel) {
        gamepadViewModel.events
            .filterIsInstance<GamepadViewModel.Event.StickDirection>()
            .collect { event ->
                if (!currentIsGrabbing) {
                    clearPressedEvent()
                    return@collect
                }

                val movementStick = when (joystickControlMode) {
                    JoystickMode.RightMovement -> JoystickType.Right
                    JoystickMode.LeftMovement -> JoystickType.Left
                }

                if (event.joystickType != movementStick) return@collect

                allAction.forEach { key ->
                    sendKeyEvent(key, false)
                }

                directionMapping[event.direction]?.forEach { (key, pressed) ->
                    sendKeyEvent(key, pressed)
                }
            }
    }
}

/**
 * 通过事件获取获取设备名称
 */
fun MotionEvent.getDeviceName(): String {
    return device.descriptor
}

/**
 * 通过事件获取获取设备名称
 */
fun KeyEvent.getDeviceName(): String {
    return device.descriptor
}

/**
 * 检查触摸事件是否来自手柄
 */
fun MotionEvent.isGamepadEvent(): Boolean {
    val source = this.source
    return (source and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK ||
            (source and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
            (source and InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD ||
            (source and InputDevice.SOURCE_CLASS_JOYSTICK) == InputDevice.SOURCE_CLASS_JOYSTICK
}

/**
 * 检查按键事件是否来自手柄
 */
fun KeyEvent.isFromGamepad(): Boolean {
    val source = this.source
    return (source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) ||
            (source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK) ||
            (source and InputDevice.SOURCE_DPAD == InputDevice.SOURCE_DPAD)
}

fun MotionEvent.isJoystickMoving(): Boolean {
    return isFromSource(InputDevice.SOURCE_JOYSTICK) && action == MotionEvent.ACTION_MOVE
}

fun KeyEvent.isGamepadKeyEvent(): Boolean {
    val isGamepad = isFromSource(InputDevice.SOURCE_GAMEPAD) ||
            (device != null && device.supportsSource(InputDevice.SOURCE_GAMEPAD))

    return isGamepad || isDpadKeyEvent()
}

private fun KeyEvent.isDpadKeyEvent(): Boolean {
    return (isFromSource(InputDevice.SOURCE_GAMEPAD) && isFromSource(InputDevice.SOURCE_DPAD)) &&
            device.keyboardType != InputDevice.KEYBOARD_TYPE_ALPHABETIC
}

fun InputDevice?.isGamepadDevice(): Boolean {
    if (this == null) return false
    return this.supportsSource(InputDevice.SOURCE_GAMEPAD) ||
            this.supportsSource(InputDevice.SOURCE_JOYSTICK)
}

fun MotionEvent.findTriggeredAxis(): Int? {
    return supportedAxis.find { axis ->
        getAxisValue(axis) >= 0.85
    }
}