package com.movtery.zalithlauncher.ui.control.control

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.ZLBridgeStates
import com.movtery.zalithlauncher.game.keycodes.Lwjgl2Keycode
import com.movtery.zalithlauncher.game.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.game.keycodes.MinecraftKeyBindingMapper
import com.movtery.zalithlauncher.game.launch.MCOptions
import kotlinx.coroutines.delay

enum class HotbarRule(val nameRes: Int) {
    /**
     * 自动计算(一些情况下并不精准)
     */
    Auto(R.string.game_menu_option_hotbar_rule_auto),

    /**
     * 完全自定义大小
     */
    Custom(R.string.game_menu_option_hotbar_rule_custom);

    companion object {
        /**
         * 自定义大小：0~1000比例下，计算百分比值
         */
        fun Int.hotbarPercentage() = this / 1000f
    }
}

/**
 * 快捷栏按键绑定键
 */
private val hotbarList = listOf(
    "key_key.hotbar.1",
    "key_key.hotbar.2",
    "key_key.hotbar.3",
    "key_key.hotbar.4",
    "key_key.hotbar.5",
    "key_key.hotbar.6",
    "key_key.hotbar.7",
    "key_key.hotbar.8",
    "key_key.hotbar.9",
)

private val keyList = listOf(
    LwjglGlfwKeycode.GLFW_KEY_1,
    LwjglGlfwKeycode.GLFW_KEY_2,
    LwjglGlfwKeycode.GLFW_KEY_3,
    LwjglGlfwKeycode.GLFW_KEY_4,
    LwjglGlfwKeycode.GLFW_KEY_5,
    LwjglGlfwKeycode.GLFW_KEY_6,
    LwjglGlfwKeycode.GLFW_KEY_7,
    LwjglGlfwKeycode.GLFW_KEY_8,
    LwjglGlfwKeycode.GLFW_KEY_9
)

/**
 * Minecraft 快捷栏判定箱
 * 根据屏幕分辨率定位 MC 的快捷栏位置
 * 点击、滑动快捷栏，会计算指针处于哪个槽位中，并触发 [onClickSlot] 回调
 *
 * @param isGrabbing 处于鼠标抓获模式下，才会开启判定箱
 * @param resolutionRatio 当前分辨率缩放
 */
@Composable
fun BoxScope.MinecraftHotbar(
    rule: HotbarRule,
    widthPercentage: Float,
    heightPercentage: Float,
    onClickSlot: (key: Int) -> Unit,
    isGrabbing: Boolean = false,
    resolutionRatio: Int,
    onOccupiedPointer: (PointerId) -> Unit,
    onReleasePointer: (PointerId) -> Unit
) {
    val screenSize = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current

    var hotbarSize by remember { mutableStateOf(DpSize(0.dp, 0.dp)) }
    val hotbarUpdateAnim = remember { Animatable(0f) }

    when (rule) {
        HotbarRule.Auto -> {
            LaunchedEffect(
                isGrabbing, MCOptions.refreshKey, screenSize, density,
                resolutionRatio, ZLBridgeStates.windowChangeKey
            ) {
                val guiScale = getMCGuiScale(
                    width = (screenSize.width * resolutionRatio / 100f).toInt(),
                    height = (screenSize.height * resolutionRatio / 100f).toInt()
                )
                val slotSize = guiScale * 20

                with(density) {
                    hotbarSize = DpSize((slotSize * hotbarList.size).toDp(), slotSize.toDp())
                }
            }
        }
        HotbarRule.Custom -> {
            var isInitialized by remember { mutableStateOf(false) }

            LaunchedEffect(
                widthPercentage, heightPercentage
            ) {
                val width = (screenSize.width * widthPercentage).toInt()
                val height = (screenSize.height * heightPercentage).toInt()

                with(density) {
                    hotbarSize = DpSize(width.toDp(), height.toDp())
                }

                if (isInitialized) {
                    hotbarUpdateAnim.snapTo(0.5f)
                    delay(1000)
                    hotbarUpdateAnim.animateTo(0f, tween(800))
                } else {
                    isInitialized = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .size(hotbarSize)
            .align(Alignment.BottomCenter)
            .then(
                if (rule == HotbarRule.Custom) Modifier.background(Color.Red.copy(alpha = hotbarUpdateAnim.value))
                else Modifier
            )
            .then(
                if (isGrabbing) Modifier
                    .mainTouchLogic(
                        slotCount = hotbarList.size,
                        hotbarSize = hotbarSize,
                        density = density,
                        onClickSlot = { index: Int ->
                            val keyCode = hotbarList[index].mapToKeycode() ?: keyList[index].toInt()
                            onClickSlot(keyCode)
                        },
                        onOccupiedPointer = onOccupiedPointer,
                        onReleasePointer = onReleasePointer
                    )
                else Modifier
            )
    )
}

private fun Modifier.mainTouchLogic(
    slotCount: Int,
    hotbarSize: DpSize,
    density: Density,
    onClickSlot: (index: Int) -> Unit,
    onOccupiedPointer: (PointerId) -> Unit,
    onReleasePointer: (PointerId) -> Unit
) = this.pointerInput(slotCount, hotbarSize, density) {
    awaitPointerEventScope {
        /** 所有被占用的指针 */
        val occupiedPointers = mutableSetOf<PointerId>()
        var lastSlotIndex = -1

        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)

            event.changes.forEach { change ->
                if (change.type == PointerType.Touch) {
                    val pointerId = change.id
                    if (pointerId !in occupiedPointers) {
                        onOccupiedPointer(pointerId)
                        occupiedPointers.add(pointerId)
                    }

                    change.consume()

                    when {
                        //手指刚按下
                        change.pressed && !change.previousPressed -> {
                            val x = change.position.x
                            val currentSlotIndex = calculateSlotIndex(x, hotbarSize, slotCount, density)

                            if (currentSlotIndex != -1) {
                                onClickSlot(currentSlotIndex)
                                lastSlotIndex = currentSlotIndex
                            }
                        }
                        //按下、滑动
                        change.pressed && change.previousPressed -> {
                            val x = change.position.x
                            val currentSlotIndex = calculateSlotIndex(x, hotbarSize, slotCount, density)

                            if (currentSlotIndex != -1 && currentSlotIndex != lastSlotIndex) {
                                onClickSlot(currentSlotIndex)
                                lastSlotIndex = currentSlotIndex
                            }
                        }
                        //松开手指
                        !change.pressed && change.previousPressed -> {
                            lastSlotIndex = -1
                            if (pointerId in occupiedPointers) {
                                occupiedPointers.remove(pointerId)
                                onReleasePointer(pointerId)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getMCGuiScale(width: Int, height: Int): Int {
    val guiScale = MCOptions.get("guiScale")?.toIntOrNull() ?: 4
    val scale = minOf(width / 320, height / 240).coerceAtLeast(1)
    return if (scale < guiScale || guiScale == 0) scale else guiScale
}

private fun calculateSlotIndex(
    x: Float,
    hotbarSize: DpSize,
    slotCount: Int,
    density: Density
): Int {
    val totalWidth = with(density) { hotbarSize.width.toPx() }
    val slotWidth = totalWidth / slotCount
    return (x / slotWidth).toInt().coerceIn(0, slotCount - 1)
}

/**
 * 将字符串键映射到其对应的键码
 * @return 如果找到映射则返回键码，否则返回 `null`
 */
private fun String?.mapToKeycode(): Int? {
    val binding = this ?: return null

    return if (binding.startsWith("key.")) {
        //新版MC键绑定映射
        MinecraftKeyBindingMapper.getGlfwKeycode(binding)?.toInt()
    } else {
        binding.toIntOrNull()?.let { lwjgl2Code ->
            //MC旧版本直接存了LWJGL2的键值
            //将旧版本LWJGL2的键码转换为GLFW
            Lwjgl2Keycode.lwjgl2ToGlfw(lwjgl2Code)
        }
    }
}