package com.movtery.zalithlauncher.ui.control.gamepad

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.keycodes.ControlEventKeyName
import com.movtery.zalithlauncher.game.keycodes.ControlEventKeycode
import com.movtery.zalithlauncher.viewmodel.GamepadViewModel.DpadDirection

/**
 * 特殊键值：鼠标滚轮上移一次
 */
const val SPECIAL_KEY_MOUSE_SCROLL_UP = "SPECIAL_KEY_MOUSE_SCROLL_LEFT"

/**
 * 特殊键值：鼠标滚轮下移一次
 */
const val SPECIAL_KEY_MOUSE_SCROLL_DOWN = "SPECIAL_KEY_MOUSE_SCROLL_RIGHT"

enum class GamepadMap(
    val identifier: String,
    val gamepad: Int,
    val defaultKeysInGame: Set<String> = emptySet(),
    val defaultKeysInMenu: Set<String> = emptySet(),
    val dpadDirection: DpadDirection? = null
) {
    ButtonA("BUTTON_A", GamepadRemap.ButtonA.code, setOf(ControlEventKeycode.GLFW_KEY_SPACE), setOf(ControlEventKeycode.GLFW_MOUSE_BUTTON_LEFT)) {
        override fun getIconRes() = R.drawable.img_xbox_a
    },
    ButtonB("BUTTON_B", GamepadRemap.ButtonB.code, setOf(ControlEventKeycode.GLFW_KEY_LEFT_SHIFT), setOf(ControlEventKeycode.GLFW_KEY_ESCAPE)) {
        override fun getIconRes() = R.drawable.img_xbox_b
    },
    ButtonX("BUTTON_X", GamepadRemap.ButtonX.code, setOf(ControlEventKeycode.GLFW_KEY_F1)) {
        override fun getIconRes() = R.drawable.img_xbox_x
    },
    ButtonY("BUTTON_Y", GamepadRemap.ButtonY.code, setOf(ControlEventKeycode.GLFW_KEY_E)) {
        override fun getIconRes() = R.drawable.img_xbox_y
    },
    ButtonStart("BUTTON_START", GamepadRemap.ButtonStart.code, setOf(ControlEventKeycode.GLFW_KEY_ESCAPE), setOf(ControlEventKeycode.GLFW_KEY_ESCAPE)) {
        override fun getIconRes() = R.drawable.img_xbox_start
    },
    ButtonSelect("BUTTON_SELECT", GamepadRemap.ButtonSelect.code, setOf(ControlEventKeycode.GLFW_MOUSE_BUTTON_MIDDLE), setOf(ControlEventKeycode.GLFW_KEY_TAB)) {
        override fun getIconRes() = R.drawable.img_xbox_select
    },
    ButtonLB("BUTTON_LB", GamepadRemap.ButtonL1.code, setOf(SPECIAL_KEY_MOUSE_SCROLL_UP)) {
        override fun getIconRes() = R.drawable.img_xbox_lb
    },
    ButtonRB("BUTTON_RB", GamepadRemap.ButtonR1.code, setOf(SPECIAL_KEY_MOUSE_SCROLL_DOWN)) {
        override fun getIconRes() = R.drawable.img_xbox_rb
    },
    ButtonLT("BUTTON_LT", GamepadRemap.MotionLeftTrigger.code, setOf(ControlEventKeycode.GLFW_MOUSE_BUTTON_RIGHT)) {
        override fun getIconRes() = R.drawable.img_xbox_lt
    },
    ButtonRT("BUTTON_RT", GamepadRemap.MotionRightTrigger.code, setOf(ControlEventKeycode.GLFW_MOUSE_BUTTON_LEFT)) {
        override fun getIconRes() = R.drawable.img_xbox_rt
    },
    DPadUp("DPAD_UP", -1, setOf(ControlEventKeycode.GLFW_KEY_F5), dpadDirection = DpadDirection.Up) {
        override fun getIconRes() = R.drawable.img_xbox_dpad_up
    },
    DPadLeft("DPAD_LEFT", -1, setOf(ControlEventKeycode.GLFW_KEY_F), dpadDirection = DpadDirection.Left) {
        override fun getIconRes() = R.drawable.img_xbox_dpad_left
    },
    DPadDown("DPAD_DOWN", -1, setOf(ControlEventKeycode.GLFW_KEY_Q), dpadDirection = DpadDirection.Down) {
        override fun getIconRes() = R.drawable.img_xbox_dpad_down
    },
    DPadRight("DPAD_RIGHT", -1, setOf(ControlEventKeycode.GLFW_KEY_T), dpadDirection = DpadDirection.Right) {
        override fun getIconRes() = R.drawable.img_xbox_dpad_right
    },
    StickLeftClick("STICK_LEFT_CLICK", GamepadRemap.ButtonLeftStick.code, setOf(ControlEventKeycode.GLFW_KEY_LEFT_CONTROL)) {
        override fun getIconRes() = R.drawable.img_switch_ls_click
    },
    StickRightClick("STICK_RIGHT_CLICK", GamepadRemap.ButtonRightStick.code) {
        override fun getIconRes() = R.drawable.img_switch_rs_click
    };

    /**
     * https://opengameart.org/content/free-controller-prompts-xbox-playstation-switch-pc
     * This project contains assets by JStatz, licensed under CC0 1.0 Universal.
     */
    abstract fun getIconRes(): Int
}

@Composable
fun getNameByGamepadEvent(code: String): String {
    return when (code) {
        ControlEventKeycode.GLFW_MOUSE_BUTTON_LEFT -> stringResource(R.string.control_editor_edit_event_launcher_mouse_left)
        ControlEventKeycode.GLFW_MOUSE_BUTTON_RIGHT -> stringResource(R.string.control_editor_edit_event_launcher_mouse_right)
        ControlEventKeycode.GLFW_MOUSE_BUTTON_MIDDLE -> stringResource(R.string.control_editor_edit_event_launcher_mouse_middle)
        SPECIAL_KEY_MOUSE_SCROLL_UP -> stringResource(R.string.control_editor_edit_event_launcher_mouse_scroll_up_single)
        SPECIAL_KEY_MOUSE_SCROLL_DOWN -> stringResource(R.string.control_editor_edit_event_launcher_mouse_scroll_down_single)
        else -> remember(code) {
            ControlEventKeyName.getNameByKey(code) ?: code
        }
    }
}