package one.adverse.progress

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProgressComposableTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun perimeterProgressBarComposesWithOutOfRangeProgress() {
        compose.setContent {
            PerimeterProgressBar(
                progress = 1.4f,
                color = Color.Green,
                modifier = Modifier.size(180.dp)
            )
        }

        compose.waitForIdle()
    }

    @Test
    fun rectProgressBarComposesWithNegativeProgress() {
        compose.setContent {
            RectProgressBar(
                progress = -0.5f,
                color = Color.Green,
                modifier = Modifier.size(width = 180.dp, height = 24.dp)
            )
        }

        compose.waitForIdle()
    }

    @Test
    fun seekablePerimeterProgressReceivesEdgeTap() {
        var sawSeek = false

        compose.setContent {
            Box(
                modifier = Modifier
                    .testTag("seekable")
                    .size(200.dp)
                    .seekablePerimeterProgress(
                        progress = 0f,
                        cornerRadius = 24.dp,
                        strokeWidth = 8.dp,
                        edgeSlop = 24.dp,
                        onSeek = { sawSeek = true }
                    )
            )
        }

        compose.onNodeWithTag("seekable")
            .performTouchInput {
                click(topCenter)
            }

        compose.waitForIdle()

        assertTrue(sawSeek)
    }
}
