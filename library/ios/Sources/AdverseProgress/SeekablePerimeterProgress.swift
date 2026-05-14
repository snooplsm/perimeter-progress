import SwiftUI

public extension View {
    func seekablePerimeterProgress(
        progress: Binding<CGFloat>,
        lineWidth: CGFloat,
        cornerRadius: CGFloat,
        perimeterStart: CGFloat = 0,
        edgeSlop: CGFloat = 24,
        enabled: Bool = true,
        onSeekStart: (() -> Void)? = nil,
        onSeek: ((CGFloat) -> Void)? = nil,
        onSeekEnd: ((CGFloat) -> Void)? = nil
    ) -> some View {
        modifier(
            SeekablePerimeterProgressModifier(
                progress: progress,
                lineWidth: lineWidth,
                cornerRadius: cornerRadius,
                perimeterStart: perimeterStart,
                edgeSlop: edgeSlop,
                enabled: enabled,
                onSeekStart: onSeekStart,
                onSeek: onSeek,
                onSeekEnd: onSeekEnd
            )
        )
    }
}

private struct SeekablePerimeterProgressModifier: ViewModifier {
    @Binding var progress: CGFloat
    let lineWidth: CGFloat
    let cornerRadius: CGFloat
    let perimeterStart: CGFloat
    let edgeSlop: CGFloat
    let enabled: Bool
    let onSeekStart: (() -> Void)?
    let onSeek: ((CGFloat) -> Void)?
    let onSeekEnd: ((CGFloat) -> Void)?

    @State private var isSeeking = false

    func body(content: Content) -> some View {
        content
            .overlay {
                GeometryReader { geo in
                    Color.clear
                        .contentShape(Rectangle())
                        .allowsHitTesting(enabled)
                        .gesture(
                            DragGesture(minimumDistance: 0, coordinateSpace: .local)
                                .onChanged { value in
                                    guard beginSeekingIfNeeded(location: value.location, size: geo.size) else {
                                        return
                                    }
                                    seek(location: value.location, size: geo.size)
                                }
                                .onEnded { value in
                                    guard isSeeking else { return }
                                    let finalProgress = seek(location: value.location, size: geo.size)
                                    isSeeking = false
                                    onSeekEnd?(finalProgress)
                                }
                        )
                }
            }
    }

    private func beginSeekingIfNeeded(location: CGPoint, size: CGSize) -> Bool {
        if isSeeking {
            return true
        }

        guard seekablePerimeterIsNearEdge(location: location, size: size, edgeSlop: edgeSlop) else {
            return false
        }

        isSeeking = true
        onSeekStart?()
        return true
    }

    @discardableResult
    private func seek(location: CGPoint, size: CGSize) -> CGFloat {
        let target = seekablePerimeterProgressForTap(
            size: size,
            location: location,
            lineWidth: lineWidth,
            cornerRadius: cornerRadius,
            perimeterStart: perimeterStart
        )
        let nextProgress = seekablePerimeterSeamAwareProgress(current: progress, target: target)
        progress = nextProgress
        onSeek?(nextProgress)
        return nextProgress
    }
}

func seekablePerimeterSeamAwareProgress(current: CGFloat, target: CGFloat) -> CGFloat {
    let clampedCurrent = min(max(current, 0), 1)
    let clampedTarget = min(max(target, 0), 1)
    if clampedCurrent >= 0.9 && clampedTarget <= 0.1 {
        return 1
    }
    if clampedCurrent <= 0.1 && clampedTarget >= 0.9 {
        return 0
    }
    return clampedTarget
}

func seekablePerimeterIsNearEdge(
    location: CGPoint,
    size: CGSize,
    edgeSlop: CGFloat
) -> Bool {
    location.x <= edgeSlop ||
        location.x >= size.width - edgeSlop ||
        location.y <= edgeSlop ||
        location.y >= size.height - edgeSlop
}

func seekablePerimeterProgressForTap(
    size: CGSize,
    location: CGPoint,
    lineWidth: CGFloat,
    cornerRadius: CGFloat,
    perimeterStart: CGFloat
) -> CGFloat {
    let inset = lineWidth / 2
    let rect = CGRect(
        x: inset,
        y: inset,
        width: size.width - (inset * 2),
        height: size.height - (inset * 2)
    )
    guard rect.width > 0, rect.height > 0 else { return 0 }

    let radius = min(max(0, cornerRadius - inset), min(rect.width, rect.height) / 2)
    let rightLower = max(0, (rect.height / 2) - radius)
    let rightUpper = rightLower
    let horizontal = max(0, rect.width - (radius * 2))
    let vertical = max(0, rect.height - (radius * 2))
    let arc = (.pi * radius) / 2
    let total = rightLower + arc + horizontal + arc + vertical + arc + horizontal + arc + rightUpper
    guard total > 0 else { return 0 }

    let samples = 220
    var bestDistance: CGFloat = 0
    var bestScore = CGFloat.greatestFiniteMagnitude

    for index in 0...samples {
        let distance = total * (CGFloat(index) / CGFloat(samples))
        let point = perimeterPoint(distance: distance, rect: rect, radius: radius)
        let dx = point.x - location.x
        let dy = point.y - location.y
        let score = (dx * dx) + (dy * dy)
        if score < bestScore {
            bestScore = score
            bestDistance = distance
        }
    }

    let startOffset = total * min(max(perimeterStart, 0), 1)
    let adjusted = bestDistance >= startOffset
        ? bestDistance - startOffset
        : total - (startOffset - bestDistance)
    return min(max(adjusted / total, 0), 1)
}

private func perimeterPoint(
    distance inputDistance: CGFloat,
    rect: CGRect,
    radius: CGFloat
) -> CGPoint {
    let rightLower = max(0, (rect.height / 2) - radius)
    let horizontal = max(0, rect.width - (radius * 2))
    let vertical = max(0, rect.height - (radius * 2))
    let arc = (.pi * radius) / 2
    var distance = inputDistance

    if distance <= rightLower {
        return CGPoint(x: rect.maxX, y: rect.midY + distance)
    }
    distance -= rightLower

    if distance <= arc {
        let angle = distance / radius
        return CGPoint(
            x: rect.maxX - radius + cos(angle) * radius,
            y: rect.maxY - radius + sin(angle) * radius
        )
    }
    distance -= arc

    if distance <= horizontal {
        return CGPoint(x: rect.maxX - radius - distance, y: rect.maxY)
    }
    distance -= horizontal

    if distance <= arc {
        let angle = (.pi / 2) + (distance / radius)
        return CGPoint(
            x: rect.minX + radius + cos(angle) * radius,
            y: rect.maxY - radius + sin(angle) * radius
        )
    }
    distance -= arc

    if distance <= vertical {
        return CGPoint(x: rect.minX, y: rect.maxY - radius - distance)
    }
    distance -= vertical

    if distance <= arc {
        let angle = .pi + (distance / radius)
        return CGPoint(
            x: rect.minX + radius + cos(angle) * radius,
            y: rect.minY + radius + sin(angle) * radius
        )
    }
    distance -= arc

    if distance <= horizontal {
        return CGPoint(x: rect.minX + radius + distance, y: rect.minY)
    }
    distance -= horizontal

    if distance <= arc {
        let angle = (3 * .pi / 2) + (distance / radius)
        return CGPoint(
            x: rect.maxX - radius + cos(angle) * radius,
            y: rect.minY + radius + sin(angle) * radius
        )
    }
    distance -= arc

    return CGPoint(x: rect.maxX, y: rect.minY + radius + distance)
}
