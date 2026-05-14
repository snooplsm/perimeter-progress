package one.adverse.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RectProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(20.dp),
    color: Color = Color(0xFF10A37F),
    backgroundColor: Color = Color(0xFFE5E7EB),
    cornerRadius: Dp = 16.dp
) {
    val clamped = progress.coerceIn(0f, 1f)
    val shape = RoundedCornerShape(cornerRadius)
    val brush = Brush.horizontalGradient(
        colorStops = arrayOf(
            0f to color,
            clamped to color,
            clamped to Color.Transparent
        )
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(brush)
        )
    }
}
