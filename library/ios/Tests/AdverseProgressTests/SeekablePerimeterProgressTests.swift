import CoreGraphics
import XCTest
@testable import AdverseProgress

final class SeekablePerimeterProgressTests: XCTestCase {
    func testSeamAwareProgressClampsEndWhenDraggingAcrossWrapPoint() {
        let progress = seekablePerimeterSeamAwareProgress(
            current: 0.95,
            target: 0.02
        )

        XCTAssertEqual(progress, 1, accuracy: 0.0001)
    }

    func testSeamAwareProgressClampsStartWhenDraggingAcrossWrapPoint() {
        let progress = seekablePerimeterSeamAwareProgress(
            current: 0.04,
            target: 0.98
        )

        XCTAssertEqual(progress, 0, accuracy: 0.0001)
    }

    func testSeamAwareProgressAllowsNormalMovementAwayFromWrapPoint() {
        let progress = seekablePerimeterSeamAwareProgress(
            current: 0.42,
            target: 0.74
        )

        XCTAssertEqual(progress, 0.74, accuracy: 0.0001)
    }

    func testNearEdgeMatchesOnlyTheConfiguredPerimeterSlop() {
        let size = CGSize(width: 300, height: 500)

        XCTAssertTrue(seekablePerimeterIsNearEdge(location: CGPoint(x: 10, y: 250), size: size, edgeSlop: 24))
        XCTAssertTrue(seekablePerimeterIsNearEdge(location: CGPoint(x: 290, y: 250), size: size, edgeSlop: 24))
        XCTAssertTrue(seekablePerimeterIsNearEdge(location: CGPoint(x: 150, y: 10), size: size, edgeSlop: 24))
        XCTAssertTrue(seekablePerimeterIsNearEdge(location: CGPoint(x: 150, y: 490), size: size, edgeSlop: 24))
        XCTAssertFalse(seekablePerimeterIsNearEdge(location: CGPoint(x: 150, y: 250), size: size, edgeSlop: 24))
    }

    func testProgressForTapRebasesAroundTheConfiguredPerimeterStart() {
        let size = CGSize(width: 300, height: 500)
        let location = CGPoint(x: 285, y: 250)
        let baseProgress = seekablePerimeterProgressForTap(
            size: size,
            location: location,
            lineWidth: 10,
            cornerRadius: 44,
            perimeterStart: 0
        )
        let shiftedProgress = seekablePerimeterProgressForTap(
            size: size,
            location: location,
            lineWidth: 10,
            cornerRadius: 44,
            perimeterStart: 0.25
        )
        let expectedShifted = baseProgress >= 0.25
            ? baseProgress - 0.25
            : baseProgress + 0.75

        XCTAssertEqual(shiftedProgress, expectedShifted, accuracy: 0.006)
    }

    func testProgressForTapAlwaysReturnsAClampedProgressValue() {
        let progress = seekablePerimeterProgressForTap(
            size: CGSize(width: 300, height: 500),
            location: CGPoint(x: -200, y: 1200),
            lineWidth: 10,
            cornerRadius: 44,
            perimeterStart: 0.66
        )

        XCTAssertGreaterThanOrEqual(progress, 0)
        XCTAssertLessThanOrEqual(progress, 1)
    }
}
