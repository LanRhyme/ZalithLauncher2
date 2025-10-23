package com.movtery.zalithlauncher.ui.screens.content.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.CursorShape
import com.movtery.zalithlauncher.context.copyLocalFile
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.GestureActionType
import com.movtery.zalithlauncher.setting.enums.MouseControlMode
import com.movtery.zalithlauncher.setting.unit.ParcelableSettingUnit
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedColumn
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.TitleAndSummary
import com.movtery.zalithlauncher.ui.components.TooltipIconButton
import com.movtery.zalithlauncher.ui.components.infiniteShimmer
import com.movtery.zalithlauncher.ui.control.gyroscope.isGyroscopeAvailable
import com.movtery.zalithlauncher.ui.control.mouse.CursorHotspot
import com.movtery.zalithlauncher.ui.control.mouse.MouseHotspotEditorDialog
import com.movtery.zalithlauncher.ui.control.mouse.MousePointer
import com.movtery.zalithlauncher.ui.control.mouse.arrowPointerFile
import com.movtery.zalithlauncher.ui.control.mouse.crossHairPointerFile
import com.movtery.zalithlauncher.ui.control.mouse.iBeamPointerFile
import com.movtery.zalithlauncher.ui.control.mouse.linkPointerFile
import com.movtery.zalithlauncher.ui.control.mouse.notAllowedPointerFile
import com.movtery.zalithlauncher.ui.control.mouse.resizeAllPointerFile
import com.movtery.zalithlauncher.ui.control.mouse.resizeEWPointerFile
import com.movtery.zalithlauncher.ui.control.mouse.resizeNSPointerFile
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsBackground
import com.movtery.zalithlauncher.utils.formatKeyCode
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

@Composable
fun ControlSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: NavKey?,
    mainScreenKey: NavKey?,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.Control, settingsScreenKey, false)
    ) { isVisible ->
        AnimatedColumn(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(all = 12.dp),
            isVisible = isVisible
        ) { scope ->
            AnimatedItem(scope) { yOffset ->
                SettingsBackground(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    SwitchSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.physicalMouseMode,
                        title = stringResource(R.string.settings_control_mouse_physical_mouse_mode_title),
                        summary = stringResource(R.string.settings_control_mouse_physical_mouse_mode_summary),
                        trailingIcon = {
                            TooltipIconButton(
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(horizontal = 8.dp),
                                tooltipTitle = stringResource(R.string.generic_warning),
                                tooltipMessage = stringResource(R.string.settings_control_mouse_physical_mouse_warning)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = stringResource(R.string.generic_warning),
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    )

                    var operation by remember { mutableStateOf<PhysicalKeyOperation>(PhysicalKeyOperation.None) }
                    PhysicalKeyImeTrigger(
                        modifier = Modifier.fillMaxWidth(),
                        operation = operation,
                        changeOperation = { operation = it },
                        eventViewModel = eventViewModel
                    )
                }
            }

            AnimatedItem(scope) { yOffset ->
                SettingsBackground(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    val mouseSize = AllSettings.mouseSize.state

                    var arrowMouseOperation by remember { mutableStateOf<MousePointerOperation>(MousePointerOperation.None) }
                    MousePointerLayout(
                        title = stringResource(R.string.settings_control_mouse_pointer_arrow_title),
                        summary = stringResource(R.string.settings_control_mouse_pointer_arrow_summary),
                        mouseSize = mouseSize,
                        mousePointerFile = arrowPointerFile,
                        cursorShape = CursorShape.Arrow,
                        hotspot = AllSettings.arrowMouseHotspot,
                        mouseOperation = arrowMouseOperation,
                        changeOperation = { arrowMouseOperation = it },
                        submitError = submitError
                    )

                    var linkMouseOperation by remember { mutableStateOf<MousePointerOperation>(MousePointerOperation.None) }
                    MousePointerLayout(
                        title = stringResource(R.string.settings_control_mouse_pointer_link_title),
                        summary = stringResource(R.string.settings_control_mouse_pointer_link_summary),
                        mouseSize = mouseSize,
                        mousePointerFile = linkPointerFile,
                        cursorShape = CursorShape.Hand,
                        hotspot = AllSettings.linkMouseHotspot,
                        mouseOperation = linkMouseOperation,
                        changeOperation = { linkMouseOperation = it },
                        submitError = submitError
                    )

                    var ibeamMouseOperation by remember { mutableStateOf<MousePointerOperation>(MousePointerOperation.None) }
                    MousePointerLayout(
                        title = stringResource(R.string.settings_control_mouse_pointer_ibeam_title),
                        summary = stringResource(R.string.settings_control_mouse_pointer_ibeam_summary),
                        mouseSize = mouseSize,
                        mousePointerFile = iBeamPointerFile,
                        cursorShape = CursorShape.IBeam,
                        hotspot = AllSettings.iBeamMouseHotspot,
                        mouseOperation = ibeamMouseOperation,
                        changeOperation = { ibeamMouseOperation = it },
                        submitError = submitError
                    )

                    var crosshairMouseOperation by remember { mutableStateOf<MousePointerOperation>(MousePointerOperation.None) }
                    MousePointerLayout(
                        title = stringResource(R.string.settings_control_mouse_pointer_crosshair_title),
                        summary = stringResource(R.string.settings_control_mouse_pointer_common_summary),
                        mouseSize = mouseSize,
                        mousePointerFile = crossHairPointerFile,
                        cursorShape = CursorShape.CrossHair,
                        hotspot = AllSettings.crossHairMouseHotspot,
                        mouseOperation = crosshairMouseOperation,
                        changeOperation = { crosshairMouseOperation = it },
                        submitError = submitError
                    )

                    var resizeNSMouseOperation by remember { mutableStateOf<MousePointerOperation>(MousePointerOperation.None) }
                    MousePointerLayout(
                        title = stringResource(R.string.settings_control_mouse_pointer_resize_ns_title),
                        summary = stringResource(R.string.settings_control_mouse_pointer_resize_ns_summary),
                        mouseSize = mouseSize,
                        mousePointerFile = resizeNSPointerFile,
                        cursorShape = CursorShape.ResizeNS,
                        hotspot = AllSettings.resizeNSMouseHotspot,
                        mouseOperation = resizeNSMouseOperation,
                        changeOperation = { resizeNSMouseOperation = it },
                        submitError = submitError
                    )

                    var resizeEWMouseOperation by remember { mutableStateOf<MousePointerOperation>(MousePointerOperation.None) }
                    MousePointerLayout(
                        title = stringResource(R.string.settings_control_mouse_pointer_resize_ew_title),
                        summary = stringResource(R.string.settings_control_mouse_pointer_resize_ew_summary),
                        mouseSize = mouseSize,
                        mousePointerFile = resizeEWPointerFile,
                        cursorShape = CursorShape.ResizeEW,
                        hotspot = AllSettings.resizeEWMouseHotspot,
                        mouseOperation = resizeEWMouseOperation,
                        changeOperation = { resizeEWMouseOperation = it },
                        submitError = submitError
                    )

                    var resizeAllMouseOperation by remember { mutableStateOf<MousePointerOperation>(MousePointerOperation.None) }
                    MousePointerLayout(
                        title = stringResource(R.string.settings_control_mouse_pointer_resize_all_title),
                        summary = stringResource(R.string.settings_control_mouse_pointer_common_summary),
                        mouseSize = mouseSize,
                        mousePointerFile = resizeAllPointerFile,
                        cursorShape = CursorShape.ResizeAll,
                        hotspot = AllSettings.resizeAllMouseHotspot,
                        mouseOperation = resizeAllMouseOperation,
                        changeOperation = { resizeAllMouseOperation = it },
                        submitError = submitError
                    )

                    var notAllowedMouseOperation by remember { mutableStateOf<MousePointerOperation>(MousePointerOperation.None) }
                    MousePointerLayout(
                        title = stringResource(R.string.settings_control_mouse_pointer_not_allowed_title),
                        summary = stringResource(R.string.settings_control_mouse_pointer_not_allowed_summary),
                        mouseSize = mouseSize,
                        mousePointerFile = notAllowedPointerFile,
                        cursorShape = CursorShape.NotAllowed,
                        hotspot = AllSettings.notAllowedMouseHotspot,
                        mouseOperation = notAllowedMouseOperation,
                        changeOperation = { notAllowedMouseOperation = it },
                        submitError = submitError
                    )

                    SliderSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.mouseSize,
                        title = stringResource(R.string.settings_control_mouse_size_title),
                        valueRange = 5f..50f,
                        suffix = "Dp",
                        fineTuningControl = true
                    )
                }
            }

            AnimatedItem(scope) { yOffset ->
                SettingsBackground(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    SwitchSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.hideMouse,
                        title = stringResource(R.string.settings_control_mouse_hide_title),
                        summary = stringResource(R.string.settings_control_mouse_hide_summary),
                        enabled = AllSettings.mouseControlMode.state == MouseControlMode.CLICK //仅点击模式下可更改设置
                    )

                    ListSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.mouseControlMode,
                        items = MouseControlMode.entries,
                        title = stringResource(R.string.settings_control_mouse_control_mode_title),
                        summary = stringResource(R.string.settings_control_mouse_control_mode_summary),
                        getItemText = { stringResource(it.nameRes) }
                    )

                    SliderSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.cursorSensitivity,
                        title = stringResource(R.string.settings_control_mouse_sensitivity_title),
                        summary = stringResource(R.string.settings_control_mouse_sensitivity_summary),
                        valueRange = 25f..300f,
                        suffix = "%",
                        fineTuningControl = true
                    )

                    SliderSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.mouseCaptureSensitivity,
                        title = stringResource(R.string.settings_control_mouse_capture_sensitivity_title),
                        summary = stringResource(R.string.settings_control_mouse_capture_sensitivity_summary),
                        valueRange = 25f..300f,
                        suffix = "%",
                        fineTuningControl = true
                    )

                    SliderSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.mouseLongPressDelay,
                        title = stringResource(R.string.settings_control_mouse_long_press_delay_title),
                        summary = stringResource(R.string.settings_control_mouse_long_press_delay_summary),
                        valueRange = 100f..1000f,
                        suffix = "ms",
                        fineTuningControl = true
                    )
                }
            }

            AnimatedItem(scope) { yOffset ->
                SettingsBackground(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    SwitchSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.gestureControl,
                        title = stringResource(R.string.settings_control_gesture_control_title),
                        summary = stringResource(R.string.settings_control_gesture_control_summary)
                    )

                    ListSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.gestureTapMouseAction,
                        items = GestureActionType.entries,
                        title = stringResource(R.string.settings_control_gesture_tap_action_title),
                        summary = stringResource(R.string.settings_control_gesture_tap_action_summary),
                        getItemText = { stringResource(it.nameRes) },
                        enabled = AllSettings.gestureControl.state
                    )

                    ListSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.gestureLongPressMouseAction,
                        items = GestureActionType.entries,
                        title = stringResource(R.string.settings_control_gesture_long_press_action_title),
                        summary = stringResource(R.string.settings_control_gesture_long_press_action_summary),
                        getItemText = { stringResource(it.nameRes) },
                        enabled = AllSettings.gestureControl.state
                    )

                    SliderSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.gestureLongPressDelay,
                        title = stringResource(R.string.settings_control_gesture_long_press_delay_title),
                        summary = stringResource(R.string.settings_control_mouse_long_press_delay_summary),
                        valueRange = 100f..1000f,
                        suffix = "ms",
                        enabled = AllSettings.gestureControl.state,
                        fineTuningControl = true
                    )
                }
            }

            AnimatedItem(scope) { yOffset ->
                SettingsBackground(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    //检查陀螺仪是否可用
                    val context = LocalContext.current
                    val isGyroscopeAvailable = remember(context) {
                        isGyroscopeAvailable(context = context)
                    }

                    SwitchSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.gyroscopeControl,
                        title = stringResource(R.string.settings_control_gyroscope_title),
                        summary = stringResource(R.string.settings_control_gyroscope_summary),
                        enabled = isGyroscopeAvailable,
                        trailingIcon = if (!isGyroscopeAvailable) {
                            @Composable {
                                TooltipIconButton(
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(horizontal = 8.dp),
                                    tooltipTitle = stringResource(R.string.generic_warning),
                                    tooltipMessage = stringResource(R.string.settings_control_gyroscope_unsupported)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = stringResource(R.string.generic_warning),
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        } else null
                    )

                    SliderSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.gyroscopeSensitivity,
                        title = stringResource(R.string.settings_control_gyroscope_sensitivity_title),
                        valueRange = 25f..300f,
                        suffix = "%",
                        enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state,
                        fineTuningControl = true
                    )

                    SliderSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.gyroscopeSampleRate,
                        title = stringResource(R.string.settings_control_gyroscope_sample_rate_title),
                        summary = stringResource(R.string.settings_control_gyroscope_sample_rate_summary),
                        valueRange = 5f..50f,
                        suffix = "ms",
                        enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state,
                        fineTuningControl = true
                    )

                    SwitchSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.gyroscopeSmoothing,
                        title = stringResource(R.string.settings_control_gyroscope_smoothing_title),
                        summary = stringResource(R.string.settings_control_gyroscope_smoothing_summary),
                        enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state
                    )

                    SliderSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.gyroscopeSmoothingWindow,
                        title = stringResource(R.string.settings_control_gyroscope_smoothing_window_title),
                        summary = stringResource(R.string.settings_control_gyroscope_smoothing_window_summary),
                        valueRange = 2f..10f,
                        enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state && AllSettings.gyroscopeSmoothing.state
                    )

                    SwitchSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.gyroscopeInvertX,
                        title = stringResource(R.string.settings_control_gyroscope_invert_x_title),
                        summary = stringResource(R.string.settings_control_gyroscope_invert_x_summary),
                        enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state
                    )

                    SwitchSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.gyroscopeInvertY,
                        title = stringResource(R.string.settings_control_gyroscope_invert_y_title),
                        summary = stringResource(R.string.settings_control_gyroscope_invert_y_summary),
                        enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state
                    )
                }
            }
        }
    }
}

private sealed interface PhysicalKeyOperation {
    data object None: PhysicalKeyOperation
    data object Bind: PhysicalKeyOperation
}

@Composable
private fun PhysicalKeyImeTrigger(
    modifier: Modifier = Modifier,
    operation: PhysicalKeyOperation,
    changeOperation: (PhysicalKeyOperation) -> Unit,
    eventViewModel: EventViewModel
) {
    Row(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(shape = RoundedCornerShape(22.0.dp))
                .clickable { changeOperation(PhysicalKeyOperation.Bind) }
                .padding(all = 8.dp)
                .padding(bottom = 4.dp)
                .animateContentSize()
        ) Column@{
            TitleAndSummary(
                title = stringResource(R.string.settings_control_physical_key_bind_ime_title),
                summary = stringResource(R.string.settings_control_physical_key_bind_ime_summary)
            )
            when (operation) {
                PhysicalKeyOperation.None -> {}
                PhysicalKeyOperation.Bind -> {
                    LaunchedEffect(Unit) {
                        eventViewModel.sendEvent(EventViewModel.Event.Key.StartKeyCapture)
                        //接收Activity发送的按键事件
                        eventViewModel.events
                            .filterIsInstance<EventViewModel.Event.Key.OnKeyDown>()
                            .collect { event ->
                                changeOperation(PhysicalKeyOperation.None)
                                AllSettings.physicalKeyImeCode.save(event.key.keyCode)
                            }
                    }

                    DisposableEffect(Unit) {
                        onDispose {
                            eventViewModel.sendEvent(EventViewModel.Event.Key.StopKeyCapture)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LittleTextLabel(text = stringResource(R.string.control_keyboard_bind_title))
                        MarqueeText(
                            modifier = Modifier
                                .weight(1f)
                                .infiniteShimmer(
                                    initialValue = 0.5f,
                                    targetValue = 1f
                                ),
                            text = stringResource(R.string.control_keyboard_bind_summary),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(start = 8.dp, end = 4.dp)
                .align(Alignment.CenterVertically)
        ) {
            val code = AllSettings.physicalKeyImeCode.state
            when {
                code == null -> {
                    Text(
                        modifier = Modifier.padding(end = 12.dp),
                        text = stringResource(R.string.settings_control_physical_key_bind_ime_un_bind),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                else -> {
                    IconTextButton(
                        onClick = { AllSettings.physicalKeyImeCode.save(null) },
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = stringResource(R.string.generic_reset),
                        text = stringResource(
                            R.string.settings_control_physical_key_bind_ime_bound,
                            formatKeyCode(code)
                        )
                    )
                }
            }
        }
    }
}

private sealed interface MousePointerOperation {
    data object None: MousePointerOperation
    /** 重置鼠标指针前的提醒 */
    data object PreReset: MousePointerOperation
    /** 重置鼠标指针 */
    data object Reset: MousePointerOperation
    /** 变更鼠标热点 */
    data object Hotspot: MousePointerOperation
}

@Composable
private fun MousePointerLayout(
    title: String,
    summary: String,
    mouseSize: Int,
    mousePointerFile: File,
    cursorShape: CursorShape,
    hotspot: ParcelableSettingUnit<CursorHotspot>,
    mouseOperation: MousePointerOperation,
    changeOperation: (MousePointerOperation) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current
    var triggerState by remember { mutableIntStateOf(0) }
    var fileExists by remember { mutableStateOf(false) }

    LaunchedEffect(triggerState) {
        fileExists = withContext(Dispatchers.IO) { mousePointerFile.exists() }
    }

    MousePointerOperation(
        operation = mouseOperation,
        changeOperation = changeOperation,
        mousePointerFile = mousePointerFile,
        hotspot = hotspot,
        cursorShape = cursorShape,
        onRefresh = {
            triggerState++
        }
    )

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { result ->
        if (result != null) {
            TaskSystem.submitTask(
                Task.runTask(
                    dispatcher = Dispatchers.IO,
                    task = {
                        context.copyLocalFile(result, mousePointerFile)
                        triggerState++
                        changeOperation(MousePointerOperation.None)
                    },
                    onError = { th ->
                        FileUtils.deleteQuietly(mousePointerFile)
                        submitError(
                            ErrorViewModel.ThrowableMessage(
                                title = context.getString(R.string.error_import_image),
                                message = th.getMessageOrToString()
                            )
                        )
                    }
                )
            )
        }
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(shape = RoundedCornerShape(22.0.dp))
                .clickable { filePicker.launch(arrayOf("image/*")) }
                .padding(all = 8.dp)
                .padding(bottom = 4.dp)
        ) {
            TitleAndSummary(
                title = title,
                summary = summary
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MousePointer(
                modifier = Modifier.padding(all = 8.dp),
                mouseSize = mouseSize.dp,
                cursorShape = cursorShape,
                mouseFile = mousePointerFile,
                centerIcon = true,
                triggerRefresh = triggerState,
                useGlobalImageLoader = true
            )

            IconTextButton(
                onClick = {
                    if (mouseOperation == MousePointerOperation.None) {
                        changeOperation(MousePointerOperation.Hotspot)
                    }
                },
                painter = painterResource(R.drawable.ic_highlight_mouse_cursor),
                contentDescription = stringResource(R.string.settings_control_mouse_pointer_hotspot),
                text = stringResource(R.string.settings_control_mouse_pointer_hotspot)
            )

            AnimatedVisibility(
                visible = fileExists
            ) {
                IconTextButton(
                    onClick = {
                        if (mouseOperation == MousePointerOperation.None) {
                            changeOperation(MousePointerOperation.PreReset)
                        }
                    },
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = stringResource(R.string.generic_reset),
                    text = stringResource(R.string.generic_reset)
                )
            }
        }
    }
}

@Composable
private fun MousePointerOperation(
    operation: MousePointerOperation,
    changeOperation: (MousePointerOperation) -> Unit,
    mousePointerFile: File,
    hotspot: ParcelableSettingUnit<CursorHotspot>,
    cursorShape: CursorShape,
    onRefresh: () -> Unit
) {
    when (operation) {
        is MousePointerOperation.None -> {}
        is MousePointerOperation.PreReset -> {
            SimpleAlertDialog(
                title = stringResource(R.string.generic_reset),
                text = stringResource(R.string.settings_control_mouse_pointer_reset_message),
                onConfirm = {
                    //正式开始重置鼠标指针
                    changeOperation(MousePointerOperation.Reset)
                },
                onDismiss = {
                    changeOperation(MousePointerOperation.None)
                }
            )
        }
        is MousePointerOperation.Reset -> {
            LaunchedEffect(Unit) {
                FileUtils.deleteQuietly(mousePointerFile)
                onRefresh()
                changeOperation(MousePointerOperation.None)
            }
        }
        is MousePointerOperation.Hotspot -> {
            MouseHotspotEditorDialog(
                hotspot = hotspot,
                cursorShape = cursorShape,
                onClose = {
                    changeOperation(MousePointerOperation.None)
                }
            )
        }
    }
}