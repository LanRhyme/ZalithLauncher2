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

package com.movtery.layer_controller.observable

import com.movtery.layer_controller.data.ButtonStyle
import com.movtery.layer_controller.layout.ControlLayer
import com.movtery.layer_controller.layout.ControlLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * 可观察的ControlLayout包装类，用于监听变化
 */
class ObservableControlLayout(private val layout: ControlLayout): Packable<ControlLayout> {
    val info = ObservableControlInfo(layout.info)

    private val _layers = MutableStateFlow(layout.layers.map { ObservableControlLayer(it) })
    val layers: StateFlow<List<ObservableControlLayer>> = _layers
    
    private val _styles = MutableStateFlow(layout.styles.map { ObservableButtonStyle(it) })
    val styles: StateFlow<List<ObservableButtonStyle>> = _styles

    /**
     * 添加控制层
     */
    fun addLayer(layer: ControlLayer) {
        //               在顶部添加
        _layers.update { listOf(ObservableControlLayer(layer)) + it }
    }

    /**
     * 移除控制层
     */
    fun removeLayer(uuid: String) {
        _layers.update { oldLayers ->
            oldLayers.filterNot { it.uuid == uuid }
        }
    }

    /**
     * 合并至下层
     */
    fun mergeDownward(layer: ObservableControlLayer) {
        _layers.update { oldLayers ->
            if (oldLayers.isEmpty()) return@update oldLayers

            val layers = oldLayers.toMutableList()
            val index = layers.indexOf(layer)
            if (index == -1 || index + 1 >= layers.size) return@update oldLayers

            val downLayer = layers[index + 1]

            layer.normalButtons.value.takeIf {
                it.isNotEmpty()
            }?.let {
                downLayer.addAllNormalButton(it)
            }

            layer.textBoxes.value.takeIf {
                it.isNotEmpty()
            }?.let {
                downLayer.addAllTextBox(it)
            }

            layers.removeAt(index)
            layers
        }
    }

    /**
     * 调换层级顺序
     */
    fun reorder(fromIndex: Int, toIndex: Int) {
        _layers.update { oldLayers ->
            oldLayers.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }
        }
    }

    /**
     * 添加新的按钮样式
     */
    fun addStyle(style: ButtonStyle) {
        _styles.update { it + ObservableButtonStyle(style) }
    }

    /**
     * 复制控件样式
     */
    fun cloneStyle(style: ObservableButtonStyle) {
        _styles.update { it + style.cloneNew() }
    }

    /**
     * 移除按钮样式
     */
    fun removeStyle(uuid: String) {
        _styles.update { oldStyles ->
            oldStyles.filterNot { it.uuid == uuid }
        }
        layers.value.forEach { layer ->
            layer.normalButtons.value.forEach { button ->
                if (button.buttonStyle == uuid) {
                    button.buttonStyle = null
                }
            }
            layer.textBoxes.value.forEach { textBox ->
                if (textBox.buttonStyle == uuid) {
                    textBox.buttonStyle = null
                }
            }
        }
    }

    /**
     * 将编辑器内层级隐藏状态同步到实际隐藏状态
     * 供预览模式下使用正确的隐藏状态
     */
    fun applyEditorHide() {
        layers.value.forEach { layer ->
            layer.hide = layer.editorHide
        }
    }

    override fun pack(): ControlLayout {
        return layout.copy(
            info = info.pack(),
            layers = _layers.value.map { it.pack() },
            styles = _styles.value.map { it.pack() }
        )
    }
}