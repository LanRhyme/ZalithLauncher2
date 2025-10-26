package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.data.TextAlignment
import com.movtery.layer_controller.observable.ObservableNormalData
import com.movtery.layer_controller.observable.ObservableTextData
import com.movtery.layer_controller.observable.ObservableTranslatableString
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSelectItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSwitchItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutTextItem

@Composable
fun EditTextStyle(
    data: ObservableWidget,
    onEditWidgetText: (ObservableTranslatableString) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .padding(start = 4.dp, end = 8.dp)
            .fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when(data) {
            is ObservableTextData -> {
                commonStyles(
                    onEditWidgetText = {
                        onEditWidgetText(data.text)
                    },
                    textAlignment = data.textAlignment,
                    onTextAlignmentChanged = { value ->
                        data.textAlignment = value
                    },
                    textBold = data.textBold,
                    onTextBoldChanged = { value ->
                        data.textBold = value
                    },
                    textItalic = data.textItalic,
                    onTextItalicChanged = { value ->
                        data.textItalic = value
                    },
                    textUnderline = data.textUnderline,
                    onTextUnderlineChanged = { value ->
                        data.textUnderline = value
                    }
                )
            }
            is ObservableNormalData -> {
                commonStyles(
                    onEditWidgetText = {
                        onEditWidgetText(data.text)
                    },
                    textAlignment = data.textAlignment,
                    onTextAlignmentChanged = { value ->
                        data.textAlignment = value
                    },
                    textBold = data.textBold,
                    onTextBoldChanged = { value ->
                        data.textBold = value
                    },
                    textItalic = data.textItalic,
                    onTextItalicChanged = { value ->
                        data.textItalic = value
                    },
                    textUnderline = data.textUnderline,
                    onTextUnderlineChanged = { value ->
                        data.textUnderline = value
                    }
                )
            }
        }
    }
}

private fun LazyListScope.commonStyles(
    onEditWidgetText: () -> Unit,
    textAlignment: TextAlignment,
    onTextAlignmentChanged: (TextAlignment) -> Unit,
    textBold: Boolean,
    onTextBoldChanged: (Boolean) -> Unit,
    textItalic: Boolean,
    onTextItalicChanged: (Boolean) -> Unit,
    textUnderline: Boolean,
    onTextUnderlineChanged: (Boolean) -> Unit
) {
    //编辑文本
    item {
        InfoLayoutTextItem(
            title = stringResource(R.string.control_editor_edit_text),
            onClick = {
                onEditWidgetText()
            }
        )
    }

    //文本对齐
    item {
        InfoLayoutSelectItem(
            title = stringResource(R.string.control_editor_edit_text_alignment),
            options = TextAlignment.entries,
            current = textAlignment,
            onClick = { value ->
                if (textAlignment != value) onTextAlignmentChanged(value)
            },
            label = { item ->
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    val icon = when (item) {
                        TextAlignment.Left -> Icons.AutoMirrored.Filled.FormatAlignLeft
                        TextAlignment.Center -> Icons.Default.FormatAlignCenter
                        TextAlignment.Right -> Icons.AutoMirrored.Filled.FormatAlignRight
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null
                    )
                }
            }
        )
    }

    //启用粗体
    item {
        InfoLayoutSwitchItem(
            title = stringResource(R.string.control_editor_edit_text_bold),
            value = textBold,
            onValueChange = onTextBoldChanged
        )
    }

    //启用斜体
    item {
        InfoLayoutSwitchItem(
            title = stringResource(R.string.control_editor_edit_text_italic),
            value = textItalic,
            onValueChange = onTextItalicChanged
        )
    }

    //启用下划线
    item {
        InfoLayoutSwitchItem(
            title = stringResource(R.string.control_editor_edit_text_underline),
            value = textUnderline,
            onValueChange = onTextUnderlineChanged
        )
    }
}