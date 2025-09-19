package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_style

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import com.movtery.layer_controller.data.ButtonShape
import com.movtery.layer_controller.data.buttonShapeRange
import com.movtery.layer_controller.layout.RendererStyleBox
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableStyleConfig
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.ColorPickerDialog
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.itemLayoutColorOnSurface
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSliderItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSwitchItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutTextItem

private data class TabItem(val titleRes: Int)

@Composable
fun EditStyleDialog(
    style: ObservableButtonStyle,
    onClose: () -> Unit
) {
    val tabs = remember {
        listOf(
            TabItem(R.string.control_editor_edit_style_config_light),
            TabItem(R.string.control_editor_edit_style_config_dark)
        )
    }

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
    }
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            selectedTabIndex = pagerState.currentPage
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 3.dp),
                shadowElevation = 3.dp,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Row(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(all = 12.dp)
                            .weight(0.4f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RendererBox(
                            style = style,
                            modifier = Modifier.weight(1f)
                        )
                        //控件外观名称
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = style.name,
                            onValueChange = { style.name = it },
                            singleLine = true,
                            label = {
                                Text(text = stringResource(R.string.control_editor_edit_style_config_name))
                            },
                            shape = MaterialTheme.shapes.large
                        )
                        //启用动画过渡
                        InfoLayoutSwitchItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(R.string.control_editor_edit_style_config_animate_swap),
                            value = style.animateSwap,
                            onValueChange = { style.animateSwap = it }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                    ) {
                        //顶贴标签栏
                        TabRow(selectedTabIndex = selectedTabIndex) {
                            tabs.forEachIndexed { index, item ->
                                Tab(
                                    selected = index == selectedTabIndex,
                                    onClick = {
                                        selectedTabIndex = index
                                    },
                                    text = {
                                        MarqueeText(text = stringResource(item.titleRes))
                                    }
                                )
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                        ) { page ->
                            when (page) {
                                0 -> {
                                    StyleConfigEditor(
                                        modifier = Modifier.fillMaxSize(),
                                        styleConfig = style.lightStyle
                                    )
                                }
                                1 -> {
                                    StyleConfigEditor(
                                        modifier = Modifier.fillMaxSize(),
                                        styleConfig = style.darkStyle
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 渲染样式在不同状态下的外观
 */
@Composable
private fun RendererBox(
    style: ObservableButtonStyle,
    modifier: Modifier = Modifier,
    color: Color = itemLayoutColorOnSurface(3.dp),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = MaterialTheme.shapes.large
) {
    Surface(
        modifier = modifier.border(
            width = 4.dp,
            color = borderColor,
            shape = shape
        ),
        color = color,
        contentColor = contentColor,
        shape = shape,
        shadowElevation = 1.dp
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize().padding(all = 16.dp)
        ) {
            val (lightNormal, lightPressed, darkNormal, darkPressed) = createRefs()
            val boxModifier = Modifier.size(50.dp)

            //浅色 普通状态
            RendererStyleBox(
                style = style,
                isDark = false,
                isPressed = false,
                text = "abc",
                modifier = boxModifier.constrainAs(lightNormal) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(lightPressed.start)
                    bottom.linkTo(darkNormal.top)
                }
            )

            //浅色 按下状态
            RendererStyleBox(
                style = style,
                isDark = false,
                isPressed = true,
                text = "abc",
                modifier = boxModifier.constrainAs(lightPressed) {
                    top.linkTo(parent.top)
                    start.linkTo(lightNormal.end)
                    end.linkTo(parent.end)
                    bottom.linkTo(darkPressed.top)
                }
            )

            //暗色 普通状态
            RendererStyleBox(
                style = style,
                isDark = true,
                isPressed = false,
                text = "abc",
                modifier = boxModifier.constrainAs(darkNormal) {
                    top.linkTo(lightNormal.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(darkPressed.start)
                    bottom.linkTo(parent.bottom)
                }
            )

            //暗色 按下状态
            RendererStyleBox(
                style = style,
                isDark = true,
                isPressed = true,
                text = "abc",
                modifier = boxModifier.constrainAs(darkPressed) {
                    top.linkTo(lightPressed.bottom)
                    start.linkTo(darkNormal.end)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
            )
        }
    }
}

@Composable
private fun StyleConfigEditor(
    modifier: Modifier = Modifier,
    styleConfig: ObservableStyleConfig
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val itemModifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp)

        //普通
        item {
            Text(
                modifier = itemModifier,
                text = stringResource(R.string.control_editor_edit_style_config_normal)
            )
        }

        commonStyleConfig(
            itemModifier = itemModifier,
            alpha = styleConfig.alpha,
            onAlphaChange = { styleConfig.alpha = it },
            backgroundColor = styleConfig.backgroundColor,
            onBackgroundColorChange = { styleConfig.backgroundColor = it },
            contentColor = styleConfig.contentColor,
            onContentColorChange = { styleConfig.contentColor = it },
            borderWidth = styleConfig.borderWidth,
            onBorderWidthChange = { styleConfig.borderWidth = it },
            borderColor = styleConfig.borderColor,
            onBorderColorChange = { styleConfig.borderColor = it },
            borderRadius = styleConfig.borderRadius,
            onBorderRadiusChange = { styleConfig.borderRadius = it }
        )

        item {
            Spacer(Modifier.height(4.dp))
        }

        //按下
        item {
            Text(
                text = stringResource(R.string.control_editor_edit_style_config_pressed)
            )
        }

        commonStyleConfig(
            itemModifier = itemModifier,
            alpha = styleConfig.pressedAlpha,
            onAlphaChange = { styleConfig.pressedAlpha = it },
            backgroundColor = styleConfig.pressedBackgroundColor,
            onBackgroundColorChange = { styleConfig.pressedBackgroundColor = it },
            contentColor = styleConfig.pressedContentColor,
            onContentColorChange = { styleConfig.pressedContentColor = it },
            borderWidth = styleConfig.pressedBorderWidth,
            onBorderWidthChange = { styleConfig.pressedBorderWidth = it },
            borderColor = styleConfig.pressedBorderColor,
            onBorderColorChange = { styleConfig.pressedBorderColor = it },
            borderRadius = styleConfig.pressedBorderRadius,
            onBorderRadiusChange = { styleConfig.pressedBorderRadius = it }
        )
    }
}

private fun LazyListScope.commonStyleConfig(
    itemModifier: Modifier,
    alpha: Float,
    onAlphaChange: (Float) -> Unit,
    backgroundColor: Color,
    onBackgroundColorChange: (Color) -> Unit,
    contentColor: Color,
    onContentColorChange: (Color) -> Unit,
    borderWidth: Int,
    onBorderWidthChange: (Int) -> Unit,
    borderColor: Color,
    onBorderColorChange: (Color) -> Unit,
    borderRadius: ButtonShape,
    onBorderRadiusChange: (ButtonShape) -> Unit
) {
    //整体不透明度
    item {
        InfoLayoutSliderItem(
            modifier = itemModifier,
            title = stringResource(R.string.control_editor_edit_style_config_alpha),
            value = alpha,
            onValueChange = { onAlphaChange(it) },
            valueRange = 0f..1f,
            suffix = "%",
            fineTuningStep = 0.1f
        )
    }

    //背景颜色
    item {
        InfoLayoutColorItem(
            modifier = itemModifier,
            title = stringResource(R.string.control_editor_edit_style_config_background_color),
            color = backgroundColor,
            onColorChanged = onBackgroundColorChange
        )
    }

    //内容颜色
    item {
        InfoLayoutColorItem(
            modifier = itemModifier,
            title = stringResource(R.string.control_editor_edit_style_config_content_color),
            color = contentColor,
            onColorChanged = onContentColorChange
        )
    }

    //边框粗细
    item {
        InfoLayoutSliderItem(
            modifier = itemModifier,
            title = stringResource(R.string.control_editor_edit_style_config_border_width),
            value = borderWidth.toFloat(),
            onValueChange = { onBorderWidthChange(it.toInt()) },
            valueRange = 0f..50f,
            decimalFormat = "#0",
            suffix = "Dp",
            fineTuningStep = 1f,
        )
    }

    //边框颜色
    item {
        InfoLayoutColorItem(
            modifier = itemModifier,
            title = stringResource(R.string.control_editor_edit_style_config_border_color),
            color = borderColor,
            onColorChanged = onBorderColorChange
        )
    }

    item {
        Text(
            text = stringResource(R.string.control_editor_edit_style_config_widget_radius)
        )
    }

    //控件圆角
    item {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //左上角
            InfoLayoutSliderItem(
                modifier = itemModifier,
                title = stringResource(R.string.control_editor_edit_style_config_widget_radius_left_top),
                value = borderRadius.topStart,
                onValueChange = { onBorderRadiusChange(borderRadius.copy(topStart = it)) },
                valueRange = buttonShapeRange,
                suffix = "Dp"
            )
            //右上角
            InfoLayoutSliderItem(
                modifier = itemModifier,
                title = stringResource(R.string.control_editor_edit_style_config_widget_radius_right_top),
                value = borderRadius.topEnd,
                onValueChange = { onBorderRadiusChange(borderRadius.copy(topEnd = it)) },
                valueRange = buttonShapeRange,
                suffix = "Dp"
            )
            //左下角
            InfoLayoutSliderItem(
                modifier = itemModifier,
                title = stringResource(R.string.control_editor_edit_style_config_widget_radius_left_bottom),
                value = borderRadius.bottomStart,
                onValueChange = { onBorderRadiusChange(borderRadius.copy(bottomStart = it)) },
                valueRange = buttonShapeRange,
                suffix = "Dp"
            )
            //右下角
            InfoLayoutSliderItem(
                modifier = itemModifier,
                title = stringResource(R.string.control_editor_edit_style_config_widget_radius_right_bottom),
                value = borderRadius.bottomEnd,
                onValueChange = { onBorderRadiusChange(borderRadius.copy(bottomEnd = it)) },
                valueRange = buttonShapeRange,
                suffix = "Dp"
            )
        }
    }
}

@Composable
private fun InfoLayoutColorItem(
    modifier: Modifier = Modifier,
    title: String,
    color: Color,
    onColorChanged: (Color) -> Unit
) {
    var showColorDialog by remember { mutableStateOf(false) }

    InfoLayoutTextItem(
        modifier = modifier,
        title = title,
        onClick = {
            showColorDialog = true
        }
    )

    if (showColorDialog) {
        val initialColor = remember { color }
        ColorPickerDialog(
            onDismissRequest = {
                showColorDialog = false
            },
            initialColor = initialColor,
            onColorChanged = onColorChanged,
            onConfirm = { color ->
                showColorDialog = false
                onColorChanged(color)
            }
        )
    }
}


