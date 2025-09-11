package com.movtery.zalithlauncher.ui.control.mouse

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.GifDecoder
import coil3.request.ImageRequest
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.MouseControlMode
import com.movtery.zalithlauncher.utils.device.PhysicalMouseChecker
import com.movtery.zalithlauncher.utils.file.child
import java.io.File

/**
 * 鼠标指针图片文件
 */
val mousePointerFile: File = PathManager.DIR_MOUSE_POINTER.child("default_pointer.image")

/**
 * 获取鼠标指针图片（检查是否存在）
 */
fun getMousePointerFileAvailable(): File? = mousePointerFile.takeIf { it.exists() }

/**
 * 虚拟指针模拟层
 * @param controlMode               控制模式：SLIDE（滑动控制）、CLICK（点击控制）
 * @param longPressTimeoutMillis    长按触发检测时长
 * @param requestPointerCapture     是否使用鼠标抓取方案
 * @param hideMouseInClickMode      是否在鼠标为点击控制模式时，隐藏鼠标指针
 * @param lastMousePosition         上次虚拟鼠标指针位置
 * @param onTap                     点击回调，参数是触摸点在控件内的绝对坐标
 * @param onLongPress               长按开始回调
 * @param onLongPressEnd            长按结束回调
 * @param onPointerMove             指针移动回调，参数在 SLIDE 模式下是指针位置，CLICK 模式下是手指当前位置
 * @param onMouseScroll             实体鼠标指针滚轮滑动
 * @param onMouseButton             实体鼠标指针按钮按下反馈
 * @param isMoveOnlyPointer         指针是否被父级标记为仅可滑动指针
 * @param onOccupiedPointer         占用指针回调
 * @param onReleasePointer          释放指针回调
 * @param mouseSize                 指针大小
 * @param cursorSensitivity         指针灵敏度（滑动模式生效）
 */
@Composable
fun VirtualPointerLayout(
    modifier: Modifier = Modifier,
    controlMode: MouseControlMode = AllSettings.mouseControlMode.state,
    longPressTimeoutMillis: Long = AllSettings.mouseLongPressDelay.state.toLong(),
    requestPointerCapture: Boolean = !AllSettings.physicalMouseMode.state,
    hideMouseInClickMode: Boolean = AllSettings.hideMouse.state,
    lastMousePosition: Offset? = null,
    onTap: (Offset) -> Unit = {},
    onLongPress: () -> Unit = {},
    onLongPressEnd: () -> Unit = {},
    onPointerMove: (Offset) -> Unit = {},
    onMouseScroll: (Offset) -> Unit = {},
    onMouseButton: (button: Int, pressed: Boolean) -> Unit = { _, _ -> },
    isMoveOnlyPointer: (PointerId) -> Boolean = { false },
    onOccupiedPointer: (PointerId) -> Unit = {},
    onReleasePointer: (PointerId) -> Unit = {},
    mouseSize: Dp = AllSettings.mouseSize.state.dp,
    cursorSensitivity: Int = AllSettings.cursorSensitivity.state,
    requestFocusKey: Any? = null
) {
    val speedFactor = cursorSensitivity / 100f

    val windowSize = LocalWindowInfo.current.containerSize
    val screenWidth: Float = windowSize.width.toFloat()
    val screenHeight: Float = windowSize.height.toFloat()

    var showMousePointer by remember {
        mutableStateOf(requestPointerCapture)
    }
    fun updateMousePointer(show: Boolean) {
        showMousePointer = show
    }
    LaunchedEffect(hideMouseInClickMode) {
        updateMousePointer(
            show = when {
                //物理鼠标已连接：是否为抓获控制模式
                PhysicalMouseChecker.physicalMouseConnected -> requestPointerCapture
                //点击控制模式：由隐藏虚拟鼠标设置决定
                controlMode == MouseControlMode.CLICK -> !hideMouseInClickMode
                //滑动控制始终显示
                else -> controlMode == MouseControlMode.SLIDE
            }
        )
    }

    var pointerPosition by remember {
        val pos = lastMousePosition?.takeIf {
            //如果当前正在使用物理鼠标，则使用上次虚拟鼠标的位置
            //否则默认将鼠标放到屏幕正中心
            !showMousePointer
        } ?: Offset(screenWidth / 2f, screenHeight / 2f)
        onPointerMove(pos)
        mutableStateOf(pos)
    }

    Box(modifier = modifier) {
        if (showMousePointer) {
            MousePointer(
                modifier = Modifier.absoluteOffset(
                    x = with(LocalDensity.current) { pointerPosition.x.toDp() },
                    y = with(LocalDensity.current) { pointerPosition.y.toDp() }
                ),
                mouseSize = mouseSize,
                mouseFile = getMousePointerFileAvailable()
            )
        }

        TouchpadLayout(
            modifier = Modifier.fillMaxSize(),
            controlMode = controlMode,
            longPressTimeoutMillis = longPressTimeoutMillis,
            requestPointerCapture = requestPointerCapture,
            onTap = { fingerPos ->
                onTap(
                    if (controlMode == MouseControlMode.CLICK) {
                        updateMousePointer(!hideMouseInClickMode)
                        //当前手指的绝对坐标
                        pointerPosition = fingerPos
                        fingerPos
                    } else {
                        pointerPosition
                    }
                )
            },
            onLongPress = onLongPress,
            onLongPressEnd = onLongPressEnd,
            onPointerMove = { offset ->
                pointerPosition = if (controlMode == MouseControlMode.SLIDE) {
                    updateMousePointer(true)
                    Offset(
                        x = (pointerPosition.x + offset.x * speedFactor).coerceIn(0f, screenWidth),
                        y = (pointerPosition.y + offset.y * speedFactor).coerceIn(0f, screenHeight)
                    )
                } else {
                    updateMousePointer(!hideMouseInClickMode)
                    //当前手指的绝对坐标
                    offset
                }
                onPointerMove(pointerPosition)
            },
            onMouseMove = { offset ->
                if (requestPointerCapture) {
                    updateMousePointer(true)
                    pointerPosition = Offset(
                        x = (pointerPosition.x + offset.x * speedFactor).coerceIn(0f, screenWidth),
                        y = (pointerPosition.y + offset.y * speedFactor).coerceIn(0f, screenHeight)
                    )
                    onPointerMove(pointerPosition)
                } else {
                    //非鼠标抓取模式
                    updateMousePointer(false)
                    pointerPosition = offset
                    onPointerMove(pointerPosition)
                }
            },
            onMouseScroll = onMouseScroll,
            onMouseButton = onMouseButton,
            isMoveOnlyPointer = isMoveOnlyPointer,
            onOccupiedPointer = onOccupiedPointer,
            onReleasePointer = onReleasePointer,
            inputChange = arrayOf<Any>(speedFactor, controlMode),
            requestFocusKey = requestFocusKey
        )
    }
}

@Composable
fun MousePointer(
    modifier: Modifier = Modifier,
    mouseSize: Dp = AllSettings.mouseSize.state.dp,
    mouseFile: File?,
    centerIcon: Boolean = false,
    triggerRefresh: Any? = null
) {
    val context = LocalContext.current

    val imageLoader = remember(triggerRefresh, context) {
        ImageLoader.Builder(context)
            .components { add(GifDecoder.Factory()) }
            .build()
    }

    val imageAlignment = if (centerIcon) Alignment.Center else Alignment.TopStart
    val imageModifier = modifier.size(mouseSize)

    val (model, defaultRes) = remember(mouseFile, triggerRefresh, context) {
        val default = null to R.drawable.img_mouse_pointer
        when {
            mouseFile == null -> default
            else -> {
                if (mouseFile.exists()) {
                    val model = ImageRequest.Builder(context)
                        .data(mouseFile)
                        .build()
                    model to null
                } else {
                    default
                }
            }
        }
    }

    if (model != null) {
        AsyncImage(
            model = model,
            imageLoader = imageLoader,
            contentDescription = null,
            alignment = imageAlignment,
            contentScale = ContentScale.Fit,
            modifier = imageModifier
        )
    } else {
        Image(
            painter = painterResource(id = defaultRes!!),
            contentDescription = null,
            alignment = imageAlignment,
            contentScale = ContentScale.Fit,
            modifier = imageModifier
        )
    }
}