package one.adverse.progress.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import one.adverse.progress.PerimeterProgressBar
import one.adverse.progress.RectProgressBar
import one.adverse.progress.seekablePerimeterProgress

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                ProgressSampleApp()
            }
        }
    }
}

@Composable
private fun ProgressSampleApp() {
    var progress by remember { mutableFloatStateOf(0.42f) }
    val accent = Color(0xFF10A37F)
    val perimeterStrokeWidth = 10.dp
    val perimeterCornerRadius = 34.dp
    val perimeterPathCornerRadius = perimeterCornerRadius + perimeterStrokeWidth
    val perimeterStart = 0.66f
    val perimeterEdgeSlop = 24.dp

    Surface(color = Color(0xFFF7F7F8)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = Color(0xFF4B5563),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .seekablePerimeterProgress(
                        progress = progress,
                        cornerRadius = perimeterPathCornerRadius,
                        strokeWidth = perimeterStrokeWidth,
                        perimeterStart = perimeterStart,
                        edgeSlop = perimeterEdgeSlop
                    ) { progress = it },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(perimeterStrokeWidth)
                        .clip(RoundedCornerShape(perimeterCornerRadius))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Perimeter",
                        color = Color.White.copy(alpha = 0.74f),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                PerimeterProgressBar(
                    progress = progress,
                    strokeWidth = perimeterStrokeWidth,
                    cornerRadius = perimeterPathCornerRadius,
                    color = accent,
                    trackColor = Color.Black.copy(alpha = 0.12f),
                    perimeterStart = perimeterStart,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .seekableHorizontalProgress(currentProgress = progress) { progress = it },
                contentAlignment = Alignment.Center
            ) {
                RectProgressBar(
                    progress = progress,
                    color = accent,
                    backgroundColor = Color.Black.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Slider(
                value = progress,
                onValueChange = { progress = it.coerceIn(0f, 1f) }
            )
        }
    }
}

@Composable
private fun Modifier.seekableHorizontalProgress(
    currentProgress: Float,
    onSeek: (Float) -> Unit
): Modifier {
    val latestProgress by rememberUpdatedState(currentProgress)
    val latestOnSeek by rememberUpdatedState(onSeek)

    return this
        .pointerInput(Unit) {
            fun updateProgress(x: Float) {
                val width = size.width.takeIf { it > 0 } ?: return
                val target = (x / width).coerceIn(0f, 1f)
                latestOnSeek(seamAwareProgress(latestProgress, target))
            }

            detectTapGestures { offset ->
                updateProgress(offset.x)
            }
        }
        .pointerInput(Unit) {
            fun updateProgress(x: Float) {
                val width = size.width.takeIf { it > 0 } ?: return
                val target = (x / width).coerceIn(0f, 1f)
                latestOnSeek(seamAwareProgress(latestProgress, target))
            }

            detectDragGestures(
                onDragStart = { offset -> updateProgress(offset.x) },
                onDrag = { change, _ ->
                    updateProgress(change.position.x)
                    change.consume()
                }
            )
        }
}

private fun seamAwareProgress(currentProgress: Float, targetProgress: Float): Float {
    val current = currentProgress.coerceIn(0f, 1f)
    val target = targetProgress.coerceIn(0f, 1f)
    return when {
        current >= 0.9f && target <= 0.1f -> 1f
        current <= 0.1f && target >= 0.9f -> 0f
        else -> target
    }
}
