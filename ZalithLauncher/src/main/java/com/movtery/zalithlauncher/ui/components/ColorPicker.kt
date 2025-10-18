package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import com.movtery.colorpicker.ColorPickerController
import com.movtery.colorpicker.components.AlphaBarPicker
import com.movtery.colorpicker.components.ColorSquarePicker
import com.movtery.colorpicker.components.HueBarPicker
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.utils.logging.Logger.lDebug

/**
 * 一个简易的颜色选择器
 * @param onChangeFinished 颜色完成变更
 * @param showAlpha 是否使用透明度调节器
 * @param showHue 是否使用色相调节器
 */
@Composable
fun ColorPickerDialog(
    colorController: ColorPickerController,
    onChangeFinished: () -> Unit = {},
    onCancel: () -> Unit,
    onConfirm: (Color) -> Unit,
    showAlpha: Boolean = true,
    showHue: Boolean = true
) {
    val selectedColor by colorController.color
    val selectedHex = remember(selectedColor) {
        selectedColor.toHex()
    }

    /**
     * 是否开启编辑Hex对话框
     */
    var editHex by remember {
        mutableStateOf(false)
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.padding(all = 16.dp),
                shadowElevation = 3.dp,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(all = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.theme_color_picker_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ColorSquarePicker(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(1f),
                                controller = colorController,
                                onChangeFinished = onChangeFinished
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (showAlpha || showHue) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (showAlpha) {
                                            AlphaBarPicker(
                                                modifier = Modifier
                                                    .height(30.dp)
                                                    .fillMaxWidth(),
                                                controller = colorController,
                                                onChangeFinished = onChangeFinished
                                            )
                                        }

                                        if (showHue) {
                                            HueBarPicker(
                                                modifier = Modifier
                                                    .height(30.dp)
                                                    .fillMaxWidth(),
                                                controller = colorController,
                                                onChangeFinished = onChangeFinished
                                            )
                                        }
                                    }
                                }

                                //颜色预览
                                ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                                    val (initialHex, initialBox, arrow, currentHex, currentBox, editHexButton) = createRefs()

                                    val originalColor = remember {
                                        colorController.getOriginalColor()
                                    }

                                    //初始颜色
                                    Text(
                                        modifier = Modifier.constrainAs(initialHex) {
                                            start.linkTo(parent.start)
                                            top.linkTo(parent.top)
                                        },
                                        text = originalColor.toHex(),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Box(
                                        modifier = Modifier
                                            .constrainAs(initialBox) {
                                                start.linkTo(parent.start)
                                                top.linkTo(anchor = initialHex.bottom, margin = 4.dp)
                                            }
                                            .size(50.dp)
                                            .background(color = originalColor, shape = MaterialTheme.shapes.medium)
                                    )

                                    Icon(
                                        modifier = Modifier
                                            .constrainAs(arrow) {
                                                top.linkTo(initialBox.top)
                                                bottom.linkTo(initialBox.bottom)
                                                start.linkTo(initialBox.end)
                                                end.linkTo(currentBox.start)
                                            },
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null
                                    )

                                    //当前颜色
                                    Text(
                                        modifier = Modifier.constrainAs(currentHex) {
                                            end.linkTo(currentBox.end)
                                            top.linkTo(parent.top)
                                        },
                                        text = selectedHex,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Box(
                                        modifier = Modifier
                                            .constrainAs(currentBox) {
                                                end.linkTo(editHexButton.start, margin = 4.dp)
                                                top.linkTo(anchor = currentHex.bottom, margin = 4.dp)
                                            }
                                            .size(50.dp)
                                            .background(color = selectedColor, shape = MaterialTheme.shapes.medium)
                                    )

                                    IconButton(
                                        modifier = Modifier
                                            .constrainAs(editHexButton) {
                                                end.linkTo(parent.end)
                                                top.linkTo(currentBox.top)
                                                bottom.linkTo(currentBox.bottom)
                                            }
                                            .size(36.dp),
                                        onClick = { editHex = true }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = stringResource(R.string.theme_color_picker_edit_hex)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onChangeFinished()
                                onCancel()
                            }
                        ) {
                            MarqueeText(text = stringResource(R.string.generic_cancel))
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onConfirm(selectedColor)
                            }
                        ) {
                            MarqueeText(text = stringResource(R.string.generic_confirm))
                        }
                    }
                }
            }
        }
    }

    if (editHex) {
        var value by remember {
            mutableStateOf(selectedHex)
        }
        val newColor = remember(value) {
            //尝试转换为颜色对象
            value.toColorOrNull()
        }

        SimpleEditDialog(
            title = stringResource(R.string.theme_color_picker_edit_hex),
            value = value,
            onValueChange = { new ->
                value = new
            },
            isError = newColor == null,
            supportingText = {
                if (newColor == null) {
                    Text(text = stringResource(R.string.theme_color_picker_edit_hex_invalid))
                }
            },
            onDismissRequest = { editHex = false },
            onConfirm = {
                if (newColor != null) {
                    //主题色不允许设置透明度，需要重置透明度为255
                    colorController.setColor(newColor.copy(alpha = 1f))
                    editHex = false
                }
            }
        )
    }
}

/**
 * 将颜色转换为Hex字符串
 */
fun Color.toHex(): String {
    val alpha = (alpha * 255).toInt().toString(16).padStart(2, '0')
    val red = (red * 255).toInt().toString(16).padStart(2, '0')
    val green = (green * 255).toInt().toString(16).padStart(2, '0')
    val blue = (blue * 255).toInt().toString(16).padStart(2, '0')
    return "$alpha$red$green$blue".uppercase()
}

/**
 * 将Hex字符串转换为颜色对象
 * 若无法转换，则返回null
 *
 * 支持格式 AARRGGBB 或 RRGGBB
 */
fun String.toColorOrNull(): Color? {
    val hex = this.removePrefix("#")
    return try {
        when (hex.length) {
            6 -> {
                val r = hex.substring(0, 2).toInt(16) / 255f
                val g = hex.substring(2, 4).toInt(16) / 255f
                val b = hex.substring(4, 6).toInt(16) / 255f
                Color(r, g, b)
            }
            8 -> {
                val a = hex.substring(0, 2).toInt(16) / 255f
                val r = hex.substring(2, 4).toInt(16) / 255f
                val g = hex.substring(4, 6).toInt(16) / 255f
                val b = hex.substring(6, 8).toInt(16) / 255f
                Color(r, g, b, a)
            }
            else -> null
        }
    } catch (_: Exception) {
        lDebug("Failed to convert hex to color, input: $this")
        null
    }
}