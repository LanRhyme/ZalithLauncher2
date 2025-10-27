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

package com.movtery.layer_controller.data

import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 简易的描述按钮的各角的数据类，单位：Dp
 */
@Serializable
data class ButtonShape public constructor(
    @SerialName("topStart")
    val topStart: Float,
    @SerialName("topEnd")
    val topEnd: Float,
    @SerialName("bottomEnd")
    val bottomEnd: Float,
    @SerialName("bottomStart")
    val bottomStart: Float
) {
    constructor(size: Float) : this(size, size, size, size)
}

/**
 * 形状有效值范围
 */
val buttonShapeRange = 0f..100f

/**
 * 转换为 RoundedCornerShape
 */
fun ButtonShape.toAndroidShape() = RoundedCornerShape(
    topStart = topStart,
    topEnd = topEnd,
    bottomEnd = bottomEnd,
    bottomStart = bottomStart
)