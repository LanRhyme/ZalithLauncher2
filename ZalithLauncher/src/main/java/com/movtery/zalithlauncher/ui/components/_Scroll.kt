package com.movtery.zalithlauncher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 为可滚动的组件增加边缘渐隐的效果，可以很直观的提醒用户这里可以被滑动，
 * 效果类似元安卓View体系下的 fading edge
 * - 本实现依赖 `graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)`，
 *   该设置会让内容在单独的离屏缓冲区中绘制，以便后续通过混合模式（如 DstOut）实现“擦除”效果
 * - 如果将此修饰符放在 `padding()`、`scroll()` 或其他修饰符之后，
 *   那么离屏层可能只包裹部分内容，导致渐隐区域无法覆盖整个可见范围，从而看起来“没有效果”
 * @param direction 控制渐隐的方向，默认是垂直方向
 * @param position 指定在哪个边缘显示渐隐效果（上、下或两边）
 * @param style 控制渐隐的样式
 */
@Composable
fun Modifier.fadeEdge(
    state: ScrollState,
    length: Dp = 12.dp,
    direction: EdgeDirection = EdgeDirection.Vertical,
    position: EdgeSide = EdgeSide.Both,
    style: FadeStyle = createDefaultFadeStyle(direction)
): Modifier {
    val fadePx = with(LocalDensity.current) { length.toPx() }
    val startFade by animateFloatAsState(
        targetValue = if (state.canScrollBackward) fadePx else 0f
    )
    val endFade by animateFloatAsState(
        targetValue = if (state.canScrollForward) fadePx else 0f
    )

    return this
        //使用离屏合成，让混合模式只影响当前组件，不污染父级与同级元素
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            drawContent()
            drawFadeEdges(startFade, endFade, direction, position, style)
        }
}

/**
 * 为可滚动的组件增加边缘渐隐的效果，可以很直观的提醒用户这里可以被滑动，
 * 效果类似元安卓View体系下的 fading edge
 * - 本实现依赖 `graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)`，
 *   该设置会让内容在单独的离屏缓冲区中绘制，以便后续通过混合模式（如 DstOut）实现“擦除”效果
 * - 如果将此修饰符放在 `padding()`、`scroll()` 或其他修饰符之后，
 *   那么离屏层可能只包裹部分内容，导致渐隐区域无法覆盖整个可见范围，从而看起来“没有效果”
 * @param direction 控制渐隐的方向，默认是垂直方向
 * @param position 指定在哪个边缘显示渐隐效果（上、下或两边）
 * @param style 控制渐隐的样式
 */
@Composable
fun Modifier.fadeEdge(
    state: LazyListState,
    length: Dp = 12.dp,
    direction: EdgeDirection = EdgeDirection.Vertical,
    position: EdgeSide = EdgeSide.Both,
    style: FadeStyle = createDefaultFadeStyle(direction)
): Modifier {
    val fadePx = with(LocalDensity.current) { length.toPx() }
    val startFade by animateFloatAsState(
        targetValue = if (state.canScrollBackward) fadePx else 0f
    )
    val endFade by animateFloatAsState(
        targetValue = if (state.canScrollForward) fadePx else 0f
    )

    return this
        //使用离屏合成，让混合模式只影响当前组件，不污染父级与同级元素
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            drawContent()
            drawFadeEdges(startFade, endFade, direction, position, style)
        }
}

/**
 * 内部统一渲染渐隐的函数
 */
private fun DrawScope.drawFadeEdges(
    startFade: Float,
    endFade: Float,
    direction: EdgeDirection,
    position: EdgeSide,
    style: FadeStyle
) {
    val totalSize = if (direction == EdgeDirection.Vertical) size.height else size.width

    if (position.includeStart && startFade > 0f) {
        drawFadeEdge(
            insetStart = 0f,
            insetEnd = totalSize - startFade,
            direction = direction,
            reverse = false,
            style = style
        )
    }

    if (position.includeEnd && endFade > 0f) {
        drawFadeEdge(
            insetStart = totalSize - endFade,
            insetEnd = 0f,
            direction = direction,
            reverse = true,
            style = style
        )
    }
}

private fun DrawScope.drawFadeEdge(
    insetStart: Float,
    insetEnd: Float,
    direction: EdgeDirection,
    reverse: Boolean,
    style: FadeStyle
) {
    val isVertical = direction == EdgeDirection.Vertical
    inset(
        left = if (!isVertical) insetStart else 0f,
        top = if (isVertical) insetStart else 0f,
        right = if (!isVertical) insetEnd else 0f,
        bottom = if (isVertical) insetEnd else 0f
    ) {
        rotate(if (reverse) 180f else 0f) {
            drawRect(brush = style.brush, blendMode = style.blendMode)
        }
    }
}

/**
 * 控制渐隐的方向
 */
@Immutable
enum class EdgeDirection {
    Vertical, Horizontal
}

/**
 * 指定在哪一侧显示渐隐效果
 */
@Immutable
enum class EdgeSide(val includeStart: Boolean, val includeEnd: Boolean) {
    /** 上/左 */
    Start(true, false),
    /** 下/右 */
    End(false, true),
    /** 两边 */
    Both(true, true)
}

/**
 * 渐隐样式定义,通常情况下使用默认的 [BlendMode.DstOut] 即可
 */
@Immutable
data class FadeStyle(
    val brush: Brush,
    val blendMode: BlendMode
)

/**
 * 黑色 -> 透明 的线性渐变，结合 [BlendMode.DstOut] 实现淡化效果
 * @param direction 决定渐变的方向
 */
fun createDefaultFadeStyle(
    direction: EdgeDirection
): FadeStyle {
    //黑色 -> 透明
    val colorStops = arrayOf(
        0f to Color.Black,
        1f to Color.Transparent
    )

    return FadeStyle(
        brush = when (direction) {
            EdgeDirection.Vertical -> Brush.verticalGradient(*colorStops)
            EdgeDirection.Horizontal -> Brush.horizontalGradient(*colorStops)
        },
        blendMode = BlendMode.DstOut
    )
}