import Foundation
import UserNotifications

final class NotificationService {
    static let shared = NotificationService()
    private let center = UNUserNotificationCenter.current()

    private init() {}

    func requestPermission() async -> Bool {
        do {
            return try await center.requestAuthorization(options: [.alert, .sound, .badge])
        } catch {
            return false
        }
    }

    func sync(goals: [SavingGoal]) async {
        let identifiers = goals.map { reminderIdentifier(for: $0.id) }
        center.removePendingNotificationRequests(withIdentifiers: identifiers)

        let enabledGoals = goals.filter { $0.remindersEnabled && !$0.isArchived && !$0.isCompleted }
        guard !enabledGoals.isEmpty else { return }
        guard await requestPermission() else { return }

        for goal in enabledGoals {
            let content = UNMutableNotificationContent()
            content.title = "VunQuỹ • \(goal.title)"
            content.body = "Mỗi khoản nhỏ đều đưa bạn gần mục tiêu hơn."
            content.sound = .default

            let interval: TimeInterval
            switch goal.priority {
            case .high:
                interval = 24 * 60 * 60
            case .normal:
                interval = 3 * 24 * 60 * 60
            case .low:
                interval = 7 * 24 * 60 * 60
            }

            let trigger = UNTimeIntervalNotificationTrigger(timeInterval: interval, repeats: true)
            let request = UNNotificationRequest(
                identifier: reminderIdentifier(for: goal.id),
                content: content,
                trigger: trigger
            )
            try? await center.add(request)
        }
    }

    func remove(goalID: UUID) {
        center.removePendingNotificationRequests(withIdentifiers: [reminderIdentifier(for: goalID)])
    }

    private func reminderIdentifier(for goalID: UUID) -> String {
        "vn.vunquy.goal.\(goalID.uuidString)"
    }
}
