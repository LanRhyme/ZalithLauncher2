package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_layer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutListItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSwitchItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutTextItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.getVisibilityText

@Composable
fun EditControlLayerDialog(
    layer: ObservableControlLayer,
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
    onMergeDownward: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
        )
    ) {
        Box(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.padding(all = 3.dp),
                shadowElevation = 3.dp,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(all = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MarqueeText(
                        text = stringResource(R.string.control_editor_layers_attribute),
                        style = MaterialTheme.typography.titleMedium
                    )

                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .verticalScroll(state = scrollState),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val focusManager = LocalFocusManager.current

                        //控件层名称
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = layer.name,
                            onValueChange = { layer.name = it },
                            label = {
                                Text(stringResource(R.string.control_editor_layers_attribute_name))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus(true)
                                }
                            ),
                            shape = MaterialTheme.shapes.large
                        )

                        //可见场景
                        InfoLayoutListItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(R.string.control_editor_edit_visibility),
                            items = VisibilityType.entries,
                            selectedItem = layer.visibilityType,
                            onItemSelected = { layer.visibilityType = it },
                            getItemText = { it.getVisibilityText() },
                            useMenu = false
                        )

                        //默认隐藏控件层
                        InfoLayoutSwitchItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(R.string.control_editor_layers_attribute_hide),
                            value = layer.hide,
                            onValueChange = { layer.hide = it }
                        )

                        //合并控件至下层
                        InfoLayoutTextItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(R.string.control_editor_layers_merge_downward),
                            showArrow = false,
                            onClick = {
                                onDismissRequest()
                                onMergeDownward()
                            }
                        )

                        Spacer(Modifier)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(1f, fill = false),
                            onClick = onDelete
                        ) {
                            MarqueeText(
                                text = stringResource(R.string.generic_delete)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            modifier = Modifier.weight(1f, fill = false),
                            onClick = onDismissRequest
                        ) {
                            MarqueeText(
                                text = stringResource(R.string.generic_close)
                            )
                        }
                    }
                }
            }
        }
    }
}