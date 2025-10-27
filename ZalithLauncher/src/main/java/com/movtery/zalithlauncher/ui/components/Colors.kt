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

package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.setting.AllSettings

/**
 * 降低颜色的饱和度
 */
fun Color.desaturate(factor: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[1] *= factor.coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

@Composable
fun itemLayoutColor(): Color {
    return if (isSystemInDarkTheme()) {
        lerp(MaterialTheme.colorScheme.surfaceVariant, Color.Black, 0.15f)
    } else {
        MaterialTheme.colorScheme.surface
    }
}

@Composable
fun itemLayoutColorOnSurface(elevation: Dp = 2.dp): Color {
    return MaterialTheme.colorScheme.surfaceColorAtElevation(elevation)
}

/**
 * 启动器背景元素颜色
 * @param influencedByBackground 如果启动器设置了背景内容，则根据用户设置的不透明度设置alpha值
 */
@Composable
fun backgroundLayoutColor(
    influencedByBackground: Boolean = true
): Color {
    val color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
    return if (influencedByBackground) {
        color.copy(alpha = AllSettings.launcherBackgroundOpacity.state.toFloat() / 100f)
    } else {
        color
    }
}

/**
 * 在 secondaryContainer 背景上使用的 NavigationDrawerItem 颜色
 */
@Composable
fun secondaryContainerDrawerItemColors(): NavigationDrawerItemColors {
    val colorScheme = MaterialTheme.colorScheme
    return NavigationDrawerItemDefaults.colors(
        selectedContainerColor = colorScheme.secondaryContainer.desaturate(0.5f),
        unselectedContainerColor = Color.Transparent,
        selectedIconColor = colorScheme.onSecondaryContainer,
        unselectedIconColor = colorScheme.onSurface,
        selectedTextColor = colorScheme.onSecondaryContainer,
        unselectedTextColor = colorScheme.onSurface,
    )
}