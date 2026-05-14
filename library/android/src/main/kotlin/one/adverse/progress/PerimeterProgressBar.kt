package one.adverse.progress

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PerimeterProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    cornerRadius: Dp = 50.dp,
    color: Color = Color(0xFF10A37F),
    trackColor: Color = Color.Transparent,
    perimeterStart: Float = 0.66f,
    direction: Path.Direction = Path.Direction.Clockwise,
    animate: Boolean = true
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val clampedStart = perimeterStart.coerceIn(0f, 1f)

    val animatedProgress by if (animate) {
        animateFloatAsState(
            targetValue = clampedProgress,
            animationSpec = spring(),
            label = "perimeterProgress"
        )
    } else {
        rememberUpdatedState(clampedProgress)
    }

    val path = remember { Path() }
    val partialPath = remember { Path() }
    val wrapPath = remember { Path() }
    val pathMeasure = remember { PathMeasure() }
    var cachedSize by remember { mutableStateOf(Size.Zero) }
    var cachedLength by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val strokePx = remember(strokeWidth, density) { with(density) { strokeWidth.toPx() } }
    val strokeStyle = remember(strokePx) { Stroke(width = strokePx, cap = StrokeCap.Round) }

    Canvas(modifier = modifier) {
        val radiusPx = cornerRadius.toPx()
        val inset = strokePx / 2f

        if (size != cachedSize) {
            cachedSize = size
            path.reset()
            path.addRoundRect(
                RoundRect(
                    left = inset,
                    top = inset,
                    right = size.width - inset,
                    bottom = size.height - inset,
                    cornerRadius = CornerRadius(radiusPx)
                ),
                direction
            )
            pathMeasure.setPath(path, false)
            cachedLength = pathMeasure.length
        }

        val totalLength = cachedLength
        val progressLength = totalLength * animatedProgress
        if (totalLength <= 0f) return@Canvas

        if (trackColor != Color.Transparent) {
            drawPath(path = path, color = trackColor, style = strokeStyle)
        }

        if (progressLength <= 0f) return@Canvas

        val startOffset = totalLength * clampedStart
        val endOffset = startOffset + progressLength

        partialPath.reset()
        if (endOffset <= totalLength) {
            pathMeasure.getSegment(startOffset, endOffset, partialPath, true)
        } else {
            pathMeasure.getSegment(startOffset, totalLength, partialPath, true)
            wrapPath.reset()
            pathMeasure.getSegment(0f, endOffset - totalLength, wrapPath, true)
            partialPath.addPath(wrapPath)
        }

        drawPath(
            path = partialPath,
            color = color,
            style = strokeStyle
        )
    }
}
