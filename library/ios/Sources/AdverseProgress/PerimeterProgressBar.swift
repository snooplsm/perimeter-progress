import SwiftUI

public struct PerimeterProgressBar: View {
    public let progress: CGFloat
    public let color: Color
    public let trackColor: Color?
    public let lineWidth: CGFloat
    public let cornerRadius: CGFloat
    public let perimeterStart: CGFloat
    public let minVisibleProgress: CGFloat

    public init(
        progress: CGFloat,
        color: Color = .accentColor,
        trackColor: Color? = nil,
        lineWidth: CGFloat = 8,
        cornerRadius: CGFloat = 50,
        perimeterStart: CGFloat = 0,
        minVisibleProgress: CGFloat = 0.015
    ) {
        self.progress = progress
        self.color = color
        self.trackColor = trackColor
        self.lineWidth = lineWidth
        self.cornerRadius = cornerRadius
        self.perimeterStart = perimeterStart
        self.minVisibleProgress = minVisibleProgress
    }

    public var body: some View {
        GeometryReader { geo in
            let clamped = min(max(progress, 0), 1)
            let visibleProgress = clamped > 0 ? max(clamped, minVisibleProgress) : 0
            let inset = lineWidth / 2
            let rect = CGRect(
                x: inset,
                y: inset,
                width: geo.size.width - (inset * 2),
                height: geo.size.height - (inset * 2)
            )
            let centerlineCornerRadius = max(0, cornerRadius - inset)

            Canvas { context, _ in
                guard rect.width > 0, rect.height > 0 else { return }

                let basePath = perimeterPath(in: rect, cornerRadius: centerlineCornerRadius)
                let style = StrokeStyle(lineWidth: lineWidth, lineCap: .round, lineJoin: .round)

                if let trackColor {
                    context.stroke(basePath, with: .color(trackColor), style: style)
                }

                guard visibleProgress > 0 else { return }

                let trimmedPath = trimmedPerimeterPath(basePath, progress: visibleProgress)
                context.stroke(trimmedPath, with: .color(color), style: style)
            }
            .frame(width: geo.size.width, height: geo.size.height)
            .animation(.interpolatingSpring(stiffness: 220, damping: 28), value: visibleProgress)
        }
    }

    private func perimeterPath(in rect: CGRect, cornerRadius: CGFloat) -> Path {
        let radius = min(cornerRadius, min(rect.width, rect.height) / 2)
        var path = Path()

        path.move(to: CGPoint(x: rect.maxX, y: rect.midY))
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.maxY - radius))
        path.addArc(
            center: CGPoint(x: rect.maxX - radius, y: rect.maxY - radius),
            radius: radius,
            startAngle: .degrees(0),
            endAngle: .degrees(90),
            clockwise: false
        )
        path.addLine(to: CGPoint(x: rect.minX + radius, y: rect.maxY))
        path.addArc(
            center: CGPoint(x: rect.minX + radius, y: rect.maxY - radius),
            radius: radius,
            startAngle: .degrees(90),
            endAngle: .degrees(180),
            clockwise: false
        )
        path.addLine(to: CGPoint(x: rect.minX, y: rect.minY + radius))
        path.addArc(
            center: CGPoint(x: rect.minX + radius, y: rect.minY + radius),
            radius: radius,
            startAngle: .degrees(180),
            endAngle: .degrees(270),
            clockwise: false
        )
        path.addLine(to: CGPoint(x: rect.maxX - radius, y: rect.minY))
        path.addArc(
            center: CGPoint(x: rect.maxX - radius, y: rect.minY + radius),
            radius: radius,
            startAngle: .degrees(-90),
            endAngle: .degrees(0),
            clockwise: false
        )
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.midY))

        return path
    }

    private func trimmedPerimeterPath(_ path: Path, progress: CGFloat) -> Path {
        let start = min(max(perimeterStart, 0), 1)
        let end = start + min(max(progress, 0), 1)

        if end <= 1 {
            return path.trimmedPath(from: start, to: end)
        }

        var combined = path.trimmedPath(from: start, to: 1)
        combined.addPath(path.trimmedPath(from: 0, to: end - 1))
        return combined
    }
}

