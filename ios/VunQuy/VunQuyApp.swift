import SwiftUI
import LocalAuthentication

@main
struct VunQuyApp: App {
    @StateObject private var store = GoalStore()
    @Environment(\.scenePhase) private var scenePhase
    @AppStorage("biometricLockEnabled") private var biometricLockEnabled = false
    @State private var unlocked = true

    var body: some Scene {
        WindowGroup {
            Group {
                if biometricLockEnabled && !unlocked {
                    BiometricLockView(unlocked: $unlocked)
                } else {
                    ContentView()
                }
            }
            .environmentObject(store)
            .onAppear {
                if biometricLockEnabled {
                    unlocked = false
                }
            }
            .onChange(of: scenePhase) { _, phase in
                if phase != .active && biometricLockEnabled {
                    unlocked = false
                }
            }
        }
    }
}

private struct BiometricLockView: View {
    @Binding var unlocked: Bool
    @State private var authenticating = false
    @State private var message = ""

    var body: some View {
        ZStack {
            VunQuyBackdrop()
            VStack(spacing: 22) {
                BrandMark(size: 82)
                Text("VunQuỹ")
                    .font(.largeTitle.bold())
                Text("Ứng dụng đang được khóa")
                    .foregroundStyle(.secondary)

                Button {
                    authenticate()
                } label: {
                    Label("Mở khóa", systemImage: "faceid")
                        .font(.headline)
                        .glassButtonStyle()
                }
                .buttonStyle(.plain)
                .disabled(authenticating)

                if !message.isEmpty {
                    Text(message)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                }
            }
            .padding(30)
        }
        .task { authenticate() }
    }

    private func authenticate() {
        guard !authenticating else { return }
        authenticating = true
        message = ""

        let context = LAContext()
        var error: NSError?
        guard context.canEvaluatePolicy(.deviceOwnerAuthentication, error: &error) else {
            authenticating = false
            message = "Thiết bị chưa thiết lập phương thức xác thực phù hợp."
            return
        }

        context.evaluatePolicy(
            .deviceOwnerAuthentication,
            localizedReason: "Mở khóa VunQuỹ"
        ) { success, _ in
            DispatchQueue.main.async {
                authenticating = false
                if success {
                    unlocked = true
                } else {
                    message = "Chưa xác thực. Chạm Mở khóa để thử lại."
                }
            }
        }
    }
}
