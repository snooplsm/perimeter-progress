import SwiftUI

public struct LinearFillProgressBar: View {
    public let progress: CGFloat
    public let color: Color
    public let backgroundColor: Color
    public let cornerRadius: CGFloat

    public init(
        progress: CGFloat,
        color: Color = .accentColor,
        backgroundColor: Color = Color.secondary.opacity(0.18),
        cornerRadius: CGFloat = 16
    ) {
        self.progress = progress
        self.color = color
        self.backgroundColor = backgroundColor
        self.cornerRadius = cornerRadius
    }

    public var body: some View {
        GeometryReader { geo in
            let clamped = min(max(progress, 0), 1)
            let fillWidth = max(0, geo.size.width * clamped)

            ZStack(alignment: .leading) {
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .fill(backgroundColor)

                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .fill(color)
                    .frame(width: fillWidth)
            }
            .clipShape(RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
        }
    }
}

