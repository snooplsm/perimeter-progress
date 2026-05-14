package one.adverse.progress

import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.seekablePerimeterProgress(
    progress: Float,
    cornerRadius: Dp,
    strokeWidth: Dp,
    perimeterStart: Float = 0.66f,
    edgeSlop: Dp = 24.dp,
    enabled: Boolean = true,
    onSeekStart: (() -> Unit)? = null,
    onSeekEnd: ((Float) -> Unit)? = null,
    onSeek: (Float) -> Unit
): Modifier {
    if (!enabled) return this

    var perimeterSize by remember { mutableStateOf(IntSize.Zero) }
    val latestProgress by rememberUpdatedState(progress)
    val latestOnSeekStart by rememberUpdatedState(onSeekStart)
    val latestOnSeek by rememberUpdatedState(onSeek)
    val latestOnSeekEnd by rememberUpdatedState(onSeekEnd)
    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }
    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    val edgeSlopPx = with(density) { edgeSlop.toPx() }

    return this
        .onSizeChanged { perimeterSize = it }
        .pointerInput(perimeterSize, cornerRadiusPx, strokeWidthPx, edgeSlopPx, perimeterStart) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                if (perimeterSize == IntSize.Zero) return@awaitEachGesture

                if (!isNearPerimeterEdge(down.position, perimeterSize, edgeSlopPx)) {
                    return@awaitEachGesture
                }

                latestOnSeekStart?.invoke()

                var currentPosition = down.position

                fun seekFor(position: Offset): Float {
                    val target = seekablePerimeterProgressForTap(
                        size = perimeterSize,
                        tap = position,
                        strokePx = strokeWidthPx,
                        cornerRadiusPx = cornerRadiusPx,
                        perimeterStart = perimeterStart
                    )
                    val nextProgress = seekablePerimeterSeamAwareProgress(latestProgress, target)
                    latestOnSeek(nextProgress)
                    return nextProgress
                }

                var finalProgress = seekFor(currentPosition)
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (change.pressed) {
                        currentPosition = change.position
                        if (change.positionChanged()) {
                            change.consume()
                            finalProgress = seekFor(currentPosition)
                        }
                    } else {
                        currentPosition = change.position
                        change.consume()
                        finalProgress = seekFor(currentPosition)
                        break
                    }
                }

                latestOnSeekEnd?.invoke(finalProgress)
            }
        }
}

internal fun seekablePerimeterSeamAwareProgress(currentProgress: Float, targetProgress: Float): Float {
    val current = currentProgress.coerceIn(0f, 1f)
    val target = targetProgress.coerceIn(0f, 1f)
    return when {
        current >= 0.9f && target <= 0.1f -> 1f
        current <= 0.1f && target >= 0.9f -> 0f
        else -> target
    }
}

internal fun isNearPerimeterEdge(
    position: Offset,
    size: IntSize,
    edgeSlopPx: Float
): Boolean {
    val width = size.width.toFloat()
    val height = size.height.toFloat()
    return position.x <= edgeSlopPx ||
        position.x >= width - edgeSlopPx ||
        position.y <= edgeSlopPx ||
        position.y >= height - edgeSlopPx
}

internal fun seekablePerimeterProgressForTap(
    size: IntSize,
    tap: Offset,
    strokePx: Float,
    cornerRadiusPx: Float,
    perimeterStart: Float
): Float {
    val path = Path()
    val inset = strokePx / 2f
    val rect = RectF(
        inset,
        inset,
        size.width - inset,
        size.height - inset
    )
    path.addRoundRect(rect, cornerRadiusPx, cornerRadiusPx, Path.Direction.CW)
    val pathMeasure = PathMeasure(path, false)
    val total = pathMeasure.length
    if (total <= 0f) return 0f

    val samples = 220
    var bestDistance = 0f
    var bestScore = Float.MAX_VALUE
    val pos = FloatArray(2)
    for (i in 0..samples) {
        val distance = total * (i.toFloat() / samples.toFloat())
        pathMeasure.getPosTan(distance, pos, null)
        val dx = pos[0] - tap.x
        val dy = pos[1] - tap.y
        val score = dx * dx + dy * dy
        if (score < bestScore) {
            bestScore = score
            bestDistance = distance
        }
    }

    val startOffset = total * perimeterStart.coerceIn(0f, 1f)
    val adjusted = if (bestDistance >= startOffset) {
        bestDistance - startOffset
    } else {
        total - (startOffset - bestDistance)
    }
    return (adjusted / total).coerceIn(0f, 1f)
}
