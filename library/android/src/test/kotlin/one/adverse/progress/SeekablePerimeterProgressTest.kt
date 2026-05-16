package one.adverse.progress

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SeekablePerimeterProgressTest {
    @Test
    fun seamAwareProgressClampsEndWhenDraggingAcrossWrapPoint() {
        val progress = seekablePerimeterSeamAwareProgress(
            currentProgress = 0.95f,
            targetProgress = 0.02f
        )

        assertEquals(1f, progress, 0.0001f)
    }

    @Test
    fun seamAwareProgressClampsStartWhenDraggingAcrossWrapPoint() {
        val progress = seekablePerimeterSeamAwareProgress(
            currentProgress = 0.04f,
            targetProgress = 0.98f
        )

        assertEquals(0f, progress, 0.0001f)
    }

    @Test
    fun seamAwareProgressAllowsNormalMovementAwayFromWrapPoint() {
        val progress = seekablePerimeterSeamAwareProgress(
            currentProgress = 0.42f,
            targetProgress = 0.74f
        )

        assertEquals(0.74f, progress, 0.0001f)
    }

    @Test
    fun nearEdgeMatchesOnlyTheConfiguredPerimeterSlop() {
        val size = IntSize(width = 300, height = 500)

        assertTrue(isNearPerimeterEdge(Offset(24f, 250f), size, edgeSlopPx = 24f))
        assertTrue(isNearPerimeterEdge(Offset(10f, 250f), size, edgeSlopPx = 24f))
        assertTrue(isNearPerimeterEdge(Offset(290f, 250f), size, edgeSlopPx = 24f))
        assertTrue(isNearPerimeterEdge(Offset(150f, 10f), size, edgeSlopPx = 24f))
        assertTrue(isNearPerimeterEdge(Offset(150f, 490f), size, edgeSlopPx = 24f))
        assertFalse(isNearPerimeterEdge(Offset(150f, 250f), size, edgeSlopPx = 24f))
    }

    @Test
    fun progressForTapRebasesAroundTheConfiguredPerimeterStart() {
        val size = IntSize(width = 300, height = 500)
        val tap = Offset(285f, 250f)
        val baseProgress = seekablePerimeterProgressForTap(
            size = size,
            tap = tap,
            strokePx = 10f,
            cornerRadiusPx = 44f,
            perimeterStart = 0f
        )
        val shiftedProgress = seekablePerimeterProgressForTap(
            size = size,
            tap = tap,
            strokePx = 10f,
            cornerRadiusPx = 44f,
            perimeterStart = 0.25f
        )
        val expectedShifted = if (baseProgress >= 0.25f) {
            baseProgress - 0.25f
        } else {
            baseProgress + 0.75f
        }

        assertEquals(expectedShifted, shiftedProgress, 0.006f)
    }

    @Test
    fun progressForTapAlwaysReturnsAClampedProgressValue() {
        val size = IntSize(width = 300, height = 500)

        val progress = seekablePerimeterProgressForTap(
            size = size,
            tap = Offset(-200f, 1200f),
            strokePx = 10f,
            cornerRadiusPx = 44f,
            perimeterStart = 0.66f
        )

        assertTrue(progress in 0f..1f)
    }

    @Test
    fun progressForTapReturnsZeroForEmptySize() {
        val progress = seekablePerimeterProgressForTap(
            size = IntSize.Zero,
            tap = Offset.Zero,
            strokePx = 10f,
            cornerRadiusPx = 44f,
            perimeterStart = 0.66f
        )

        assertEquals(0f, progress, 0.0001f)
    }
}
