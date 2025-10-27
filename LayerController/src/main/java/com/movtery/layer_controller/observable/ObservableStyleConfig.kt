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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.data.ButtonStyle

/**
 * 可观察的StyleConfig包装类
 */
class ObservableStyleConfig(config: ButtonStyle.StyleConfig): Packable<ButtonStyle.StyleConfig> {
    var alpha by mutableFloatStateOf(config.alpha)
    var backgroundColor by mutableStateOf(config.backgroundColor)
    var contentColor by mutableStateOf(config.contentColor)
    var borderWidth by mutableIntStateOf(config.borderWidth)
    var borderColor by mutableStateOf(config.borderColor)
    var borderRadius by mutableStateOf(config.borderRadius)
    var pressedAlpha by mutableFloatStateOf(config.pressedAlpha)
    var pressedBackgroundColor by mutableStateOf(config.pressedBackgroundColor)
    var pressedContentColor by mutableStateOf(config.pressedContentColor)
    var pressedBorderWidth by mutableIntStateOf(config.pressedBorderWidth)
    var pressedBorderColor by mutableStateOf(config.pressedBorderColor)
    var pressedBorderRadius by mutableStateOf(config.pressedBorderRadius)

    override fun pack(): ButtonStyle.StyleConfig {
        return ButtonStyle.StyleConfig(
            alpha = this.alpha,
            backgroundColor = this.backgroundColor,
            contentColor = this.contentColor,
            borderWidth = this.borderWidth,
            borderColor = this.borderColor,
            borderRadius = this.borderRadius,
            pressedAlpha = this.pressedAlpha,
            pressedBackgroundColor = this.pressedBackgroundColor,
            pressedContentColor = this.pressedContentColor,
            pressedBorderWidth = this.pressedBorderWidth,
            pressedBorderColor = this.pressedBorderColor,
            pressedBorderRadius = this.pressedBorderRadius
        )
    }
}