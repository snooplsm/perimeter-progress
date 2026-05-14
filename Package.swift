// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "AdverseProgress",
    platforms: [
        .iOS(.v16),
        .macOS(.v13)
    ],
    products: [
        .library(
            name: "AdverseProgress",
            targets: ["AdverseProgress"]
        )
    ],
    targets: [
        .target(
            name: "AdverseProgress",
            path: "library/ios/Sources/AdverseProgress"
        ),
        .testTarget(
            name: "AdverseProgressTests",
            dependencies: ["AdverseProgress"],
            path: "library/ios/Tests/AdverseProgressTests"
        )
    ]
)
