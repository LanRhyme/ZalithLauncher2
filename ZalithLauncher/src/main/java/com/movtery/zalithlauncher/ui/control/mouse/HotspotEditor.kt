package com.movtery.zalithlauncher.ui.control.mouse

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.CursorShape
import com.movtery.zalithlauncher.setting.unit.IntSettingUnit
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSliderItem
import com.movtery.zalithlauncher.utils.file.ifExists

/**
 * 鼠标热点位置编辑对话框，编辑热点X、Y坐标百分比位置值
 * @param xPercent X坐标百分比值
 * @param yPercent Y坐标百分比值
 * @param cursorShape 指针形状，用于自动选择鼠标图片
 */
@Composable
fun MouseHotspotEditorDialog(
    xPercent: IntSettingUnit,
    yPercent: IntSettingUnit,
    cursorShape: CursorShape,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.padding(all = 3.dp),
                shadowElevation = 3.dp,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .padding(all = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MouseHotspotPreview(
                        xPercent = xPercent.state,
                        yPercent = yPercent.state,
                        cursorShape = cursorShape,
                        mouseSize = 68.dp
                    )

                    VerticalDivider(
                        modifier = Modifier
                            .fillMaxHeight(0.5f)
                            .padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        //X坐标
                        InfoLayoutSliderItem(
                            title = stringResource(R.string.settings_control_mouse_pointer_hotspot_x_percent),
                            value = xPercent.state.toFloat(),
                            onValueChange = { value ->
                                xPercent.updateState(value.toInt())
                            },
                            onValueChangeFinished = {
                                xPercent.save(xPercent.state)
                            },
                            decimalFormat = "#0",
                            suffix = "%",
                            valueRange = 0f..100f,
                            fineTuningStep = 1f
                        )

                        //Y坐标
                        InfoLayoutSliderItem(
                            title = stringResource(R.string.settings_control_mouse_pointer_hotspot_y_percent),
                            value = yPercent.state.toFloat(),
                            onValueChange = { value ->
                                yPercent.updateState(value.toInt())
                            },
                            onValueChangeFinished = {
                                yPercent.save(yPercent.state)
                            },
                            decimalFormat = "#0",
                            suffix = "%",
                            valueRange = 0f..100f,
                            fineTuningStep = 1f
                        )
                    }
                }
            }
        }
    }
}

/**
 * 鼠标热点预览，在底层渲染鼠标指针，在新的一层绘制热点位置
 * 将以小红点实时预览热点位置
 *
 * 布局方向强制锁定左向
 * @param xPercent X坐标百分比值
 * @param yPercent Y坐标百分比值
 * @param cursorShape 指针形状，用于自动选择鼠标图片
 */
@Composable
private fun MouseHotspotPreview(
    xPercent: Int,
    yPercent: Int,
    cursorShape: CursorShape,
    modifier: Modifier = Modifier,
    mouseSize: Dp = 48.dp,
    dotSize: Dp = 8.dp
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        //闪烁动画
        val infiniteTransition = rememberInfiniteTransition()
        val alpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = modifier.size(mouseSize)
        ) {
            //鼠标预览
            MousePointer(
                cursorShape = cursorShape,
                mouseSize = mouseSize,
                arrowMouseFile = arrowPointerFile.ifExists(),
                linkMouseFile = linkPointerFile.ifExists(),
                iBeamMouseFile = iBeamPointerFile.ifExists()
            )

            //坐标预览
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(alpha)
            ) {
                val radius = dotSize.toPx() / 2
                val x = size.width * xPercent / 100f
                val y = size.height * yPercent / 100f
                val center = Offset(x, y)

                drawCircle(
                    color = Color(0xFFC9350F),
                    radius = radius,
                    center = center
                )

                //白色边框
                drawCircle(
                    color = Color.White,
                    radius = radius - 1.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}