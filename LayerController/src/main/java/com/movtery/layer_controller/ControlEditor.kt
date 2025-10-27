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

package com.movtery.layer_controller

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.layout.TextButton
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.layer_controller.observable.ObservableNormalData
import com.movtery.layer_controller.observable.ObservableTextData
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.layer_controller.utils.getWidgetPosition
import com.movtery.layer_controller.utils.snap.GuideLine
import com.movtery.layer_controller.utils.snap.LineDirection
import com.movtery.layer_controller.utils.snap.SnapMode
import kotlin.math.roundToInt

/**
 * 控制布局编辑器渲染层
 * @param enableSnap 是否开启吸附
 * @param snapInAllLayers 是否在全控制层范围内吸附
 * @param snapMode 吸附模式
 * @param localSnapRange 局部吸附范围（仅在Local模式下有效）
 * @param snapThresholdValue 吸附距离阈值
 */
@Composable
fun ControlEditorLayer(
    observedLayout: ObservableControlLayout,
    onButtonTap: (data: ObservableWidget, layer: ObservableControlLayer) -> Unit,
    enableSnap: Boolean,
    snapInAllLayers: Boolean,
    snapMode: SnapMode,
    localSnapRange: Dp = 20.dp,
    snapThresholdValue: Dp = 4.dp
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        val layers by observedLayout.layers.collectAsState()
        val styles by observedLayout.styles.collectAsState()

        val guideLines = remember { mutableStateMapOf<ObservableWidget, List<GuideLine>>() }

        //反转：将最后一层视为底层，逐步向上渲染
        val renderingLayers = layers.reversed()

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            ControlWidgetRenderer(
                renderingLayers = renderingLayers,
                styles = styles,
                enableSnap = enableSnap,
                snapInAllLayers = snapInAllLayers,
                snapMode = snapMode,
                localSnapRange = localSnapRange,
                snapThresholdValue = snapThresholdValue,
                onButtonTap = onButtonTap,
                drawLine = { data, line ->
                    guideLines[data] = line
                },
                onLineCancel = { data ->
                    guideLines.remove(data)
                }
            )
            //绘制参考线
            Canvas(modifier = Modifier.fillMaxSize()) {
                guideLines.values.forEach { guidelines ->
                    guidelines.forEach { guideline ->
                        drawLine(
                            guideline = guideline
                        )
                    }
                }
            }
        }
    }
}

/**
 * 根据吸附参考线绘制线条
 */
private fun DrawScope.drawLine(
    guideline: GuideLine,
    color: Color = Color(0xFFFF5252),
    strokeWidth: Float = 2f
) {
    when (guideline.direction) {
        LineDirection.Vertical -> {
            drawLine(
                color = color,
                start = Offset(guideline.coordinate, 0f),
                end = Offset(guideline.coordinate, size.height),
                strokeWidth = strokeWidth
            )
        }
        LineDirection.Horizontal -> {
            drawLine(
                color = color,
                start = Offset(0f, guideline.coordinate),
                end = Offset(size.width, guideline.coordinate),
                strokeWidth = strokeWidth
            )
        }
    }
}

/**
 * @param enableSnap 是否开启吸附功能
 * @param snapMode 吸附模式
 * @param snapInAllLayers 是否在全控制层范围内吸附
 * @param localSnapRange 局部吸附范围（仅在Local模式下有效）
 * @param snapThresholdValue 吸附距离阈值
 * @param drawLine 绘制吸附参考线
 * @param onLineCancel 取消吸附参考线
 */
@Composable
private fun BoxWithConstraintsScope.ControlWidgetRenderer(
    renderingLayers: List<ObservableControlLayer>,
    styles: List<ObservableButtonStyle>,
    enableSnap: Boolean,
    snapInAllLayers: Boolean,
    snapMode: SnapMode,
    localSnapRange: Dp,
    snapThresholdValue: Dp,
    onButtonTap: (data: ObservableWidget, layer: ObservableControlLayer) -> Unit,
    drawLine: (ObservableWidget, List<GuideLine>) -> Unit,
    onLineCancel: (ObservableWidget) -> Unit
) {
    val sizes = remember { mutableStateMapOf<ObservableWidget, IntSize>() }
    val density = LocalDensity.current
    val screenSize = remember(maxWidth, maxHeight) {
        with(density) {
            IntSize(
                width = maxWidth.roundToPx(),
                height = maxHeight.roundToPx()
            )
        }
    }

    val allWidgetsMap = remember { mutableStateMapOf<ObservableControlLayer, List<ObservableWidget>>() }
    val snapInAllLayers1 by rememberUpdatedState(snapInAllLayers)

    @Composable
    fun RenderWidget(
        data: ObservableWidget,
        layer: ObservableControlLayer,
        isPressed: Boolean
    ) {
        TextButton(
            isEditMode = true,
            data = data,
            screenSize = screenSize,
            getSize = { d1 -> sizes[d1] ?: IntSize.Zero },
            enableSnap = enableSnap,
            snapMode = snapMode,
            localSnapRange = localSnapRange,
            getOtherWidgets = {
                allWidgetsMap
                    .filter { (layer1, _) ->
                        if (layer1.editorHide) return@filter false
                        snapInAllLayers1 || layer1 == layer
                    }
                    .values.flatten().filter { it != data }
            },
            snapThresholdValue = snapThresholdValue,
            drawLine = drawLine,
            onLineCancel = onLineCancel,
            getStyle = {
                val buttonStyle = when (data) {
                    is ObservableNormalData -> data.buttonStyle
                    is ObservableTextData -> data.buttonStyle
                    else -> error("Unknown widget type")
                }
                styles.takeIf { buttonStyle != null }
                    ?.find { it.uuid == buttonStyle }
            },
            isPressed = isPressed,
            onTapInEditMode = remember(data, layer) {
                { onButtonTap(data, layer) }
            }
        )
    }

    Layout(
        content = {
            //按图层顺序渲染所有可见的控件
            renderingLayers.forEach { layer ->
                if (!layer.editorHide) {
                    val normalButtons by layer.normalButtons.collectAsState()
                    val textBoxes by layer.textBoxes.collectAsState()

                    val widgetsInLayer = normalButtons + textBoxes
                    allWidgetsMap[layer] = widgetsInLayer

                    textBoxes.forEach { data ->
                        RenderWidget(data, layer, isPressed = false)
                    }

                    normalButtons.forEach { data ->
                        RenderWidget(data, layer, data.isPressed)
                    }
                }
            }
        }
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        var index = 0
        renderingLayers.forEach { layer ->
            if (!layer.editorHide) {
                layer.textBoxes.value.forEach { data ->
                    if (index < placeables.size) {
                        val placeable = placeables[index]
                        sizes[data] = IntSize(placeable.width, placeable.height)
                        index++
                    }
                }

                layer.normalButtons.value.forEach { data ->
                    if (index < placeables.size) {
                        val placeable = placeables[index]
                        sizes[data] = IntSize(placeable.width, placeable.height)
                        index++
                    }
                }
            }
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            var placeableIndex = 0
            renderingLayers.forEach { layer ->
                if (!layer.editorHide) {
                    layer.textBoxes.value.forEach { data ->
                        if (placeableIndex < placeables.size) {
                            val placeable = placeables[placeableIndex]
                            val position = getWidgetPosition(
                                data = data,
                                widgetSize = IntSize(placeable.width, placeable.height),
                                screenSize = screenSize
                            )
                            placeable.place(position.x.toInt(), position.y.toInt())
                            placeableIndex++
                        }
                    }

                    layer.normalButtons.value.forEach { data ->
                        if (placeableIndex < placeables.size) {
                            val placeable = placeables[placeableIndex]
                            val position = getWidgetPosition(
                                data = data,
                                widgetSize = IntSize(placeable.width, placeable.height),
                                screenSize = screenSize
                            )
                            placeable.place(position.x.roundToInt(), position.y.roundToInt())
                            placeableIndex++
                        }
                    }
                }
            }
        }
    }
}