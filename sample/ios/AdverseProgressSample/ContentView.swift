import SwiftUI

struct ContentView: View {
    @State private var progress: CGFloat = 0.42
    private let accent = Color(red: 0.0627, green: 0.6392, blue: 0.498)
    private let perimeterLineWidth: CGFloat = 10
    private let perimeterCornerRadius: CGFloat = 34
    private let perimeterStart: CGFloat = 0
    private let perimeterEdgeSlop: CGFloat = 24

    var body: some View {
        VStack(alignment: .leading, spacing: 24) {
            VStack(alignment: .leading, spacing: 6) {
                Text("Progress")
                    .font(.system(size: 32, weight: .black, design: .default))
                Text("\(Int(progress * 100))%")
                    .foregroundStyle(.secondary)
            }

            let perimeterPathCornerRadius = perimeterCornerRadius + perimeterLineWidth

            ZStack {
                RoundedRectangle(cornerRadius: perimeterCornerRadius, style: .continuous)
                    .fill(.black)
                    .padding(perimeterLineWidth)

                Text("Perimeter")
                    .font(.title2.weight(.semibold))
                    .foregroundStyle(.white.opacity(0.74))

                PerimeterProgressBar(
                    progress: progress,
                    color: accent,
                    trackColor: .black.opacity(0.12),
                    lineWidth: perimeterLineWidth,
                    cornerRadius: perimeterPathCornerRadius,
                    perimeterStart: perimeterStart
                )
            }
            .contentShape(Rectangle())
            .seekablePerimeterProgress(
                progress: $progress,
                lineWidth: perimeterLineWidth,
                cornerRadius: perimeterPathCornerRadius,
                perimeterStart: perimeterStart,
                edgeSlop: perimeterEdgeSlop
            )
            .frame(maxWidth: .infinity)
            .frame(height: 420)

            GeometryReader { geo in
                LinearFillProgressBar(
                    progress: progress,
                    color: accent,
                    backgroundColor: .black.opacity(0.1),
                    cornerRadius: 7
                )
                .frame(height: 14)
                .frame(maxHeight: .infinity, alignment: .center)
                .contentShape(Rectangle())
                .gesture(
                    DragGesture(minimumDistance: 0, coordinateSpace: .local)
                        .onChanged { value in
                            let target = linearProgressForTap(
                                width: geo.size.width,
                                x: value.location.x
                            )
                            progress = seamAwareProgress(current: progress, target: target)
                        }
                )
            }
            .frame(height: 44)

            Slider(value: $progress, in: 0...1)
        }
        .padding(24)
        .background(Color(red: 0.968, green: 0.968, blue: 0.972))
    }
}

private func linearProgressForTap(width: CGFloat, x: CGFloat) -> CGFloat {
    guard width > 0 else { return 0 }
    return min(max(x / width, 0), 1)
}

private func seamAwareProgress(current: CGFloat, target: CGFloat) -> CGFloat {
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

#Preview {
    ContentView()
}
