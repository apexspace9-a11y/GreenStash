import SwiftUI

struct VunQuyBackdrop: View {
    var body: some View {
        ZStack {
            Color(.systemBackground)
            LinearGradient(
                colors: [
                    Color.green.opacity(0.14),
                    Color.teal.opacity(0.08),
                    Color.clear,
                    Color.mint.opacity(0.10)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()

            Circle()
                .fill(Color.mint.opacity(0.16))
                .frame(width: 320, height: 320)
                .blur(radius: 70)
                .offset(x: 150, y: -260)

            Circle()
                .fill(Color.teal.opacity(0.12))
                .frame(width: 280, height: 280)
                .blur(radius: 80)
                .offset(x: -170, y: 300)
        }
        .ignoresSafeArea()
    }
}

struct GlassPanel<Content: View>: View {
    let cornerRadius: CGFloat
    let content: Content

    init(cornerRadius: CGFloat = 28, @ViewBuilder content: () -> Content) {
        self.cornerRadius = cornerRadius
        self.content = content()
    }

    var body: some View {
        content
            .background {
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .fill(.ultraThinMaterial)
                    .overlay {
                        LinearGradient(
                            colors: [
                                Color.white.opacity(0.42),
                                Color.white.opacity(0.08),
                                Color.mint.opacity(0.08),
                                Color.clear
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                        .clipShape(RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
                    }
                    .overlay {
                        RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                            .stroke(
                                LinearGradient(
                                    colors: [Color.white.opacity(0.62), Color.white.opacity(0.12), Color.mint.opacity(0.25)],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                ),
                                lineWidth: 0.8
                            )
                    }
            }
            .shadow(color: .black.opacity(0.12), radius: 22, x: 0, y: 12)
    }
}

struct BrandMark: View {
    var size: CGFloat = 52

    var body: some View {
        ZStack {
            Circle()
                .fill(
                    LinearGradient(
                        colors: [Color(red: 0.04, green: 0.42, blue: 0.29), Color(red: 0.08, green: 0.70, blue: 0.48)],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )

            Circle()
                .stroke(Color.white.opacity(0.48), lineWidth: max(1, size * 0.018))
                .padding(size * 0.08)

            Capsule()
                .fill(Color.white)
                .frame(width: size * 0.075, height: size * 0.38)
                .offset(y: size * 0.10)

            Capsule()
                .fill(Color.white)
                .frame(width: size * 0.16, height: size * 0.30)
                .rotationEffect(.degrees(-48))
                .offset(x: -size * 0.09, y: -size * 0.035)

            Capsule()
                .fill(Color.mint.opacity(0.95))
                .frame(width: size * 0.17, height: size * 0.34)
                .rotationEffect(.degrees(48))
                .offset(x: size * 0.10, y: -size * 0.055)

            LinearGradient(
                colors: [Color.white.opacity(0.42), Color.clear],
                startPoint: .topLeading,
                endPoint: .center
            )
            .clipShape(Circle())
        }
        .frame(width: size, height: size)
        .shadow(color: Color.green.opacity(0.22), radius: size * 0.18, y: size * 0.10)
        .accessibilityHidden(true)
    }
}

extension View {
    func glassButtonStyle() -> some View {
        self
            .padding(.horizontal, 18)
            .padding(.vertical, 11)
            .background(.thinMaterial, in: Capsule())
            .overlay(Capsule().stroke(Color.white.opacity(0.38), lineWidth: 0.8))
    }
}
