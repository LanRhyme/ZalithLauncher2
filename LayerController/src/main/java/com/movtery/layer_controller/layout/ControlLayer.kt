package com.movtery.layer_controller.layout

import com.movtery.layer_controller.data.NormalData
import com.movtery.layer_controller.data.TextData
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.utils.randomUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 控制布局单个层级，像图层一样存储控制组件
 * @param name 层级的名称
 * @param hide 是否隐藏层级
 * @param hideWhenMouse 是否在实体鼠标操控后隐藏
 * @param hideWhenGamepad 是否在手柄操控后隐藏
 * @param visibilityType 层级的可见场景
 * @param normalButtons 普通的按钮列表
 * @param textBoxes 文本显示框列表
 */
@Serializable
data class ControlLayer(
    @SerialName("name")
    val name: String,
    @SerialName("uuid")
    val uuid: String,
    @SerialName("hide")
    val hide: Boolean,
    @SerialName("hideWhenMouse")
    val hideWhenMouse: Boolean = true,
    @SerialName("hideWhenGamepad")
    val hideWhenGamepad: Boolean = true,
    @SerialName("visibilityType")
    val visibilityType: VisibilityType,
    @SerialName("normalButtons")
    val normalButtons: List<NormalData> = emptyList(),
    @SerialName("textBoxes")
    val textBoxes: List<TextData> = emptyList()
)

public fun createNewLayer(defaultLayerName: String = ""): ControlLayer {
    return ControlLayer(
        name = defaultLayerName,
        uuid = randomUUID(),
        hide = false,
        hideWhenMouse = true,
        hideWhenGamepad = true,
        visibilityType = VisibilityType.ALWAYS
    )
}