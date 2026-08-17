import Foundation

@MainActor
final class GoalStore: ObservableObject {
    @Published var goals: [SavingGoal] = [] {
        didSet {
            persist()
            let snapshot = goals
            Task { await NotificationService.shared.sync(goals: snapshot) }
        }
    }

    private let fileURL: URL
    private let encoder: JSONEncoder
    private let decoder: JSONDecoder

    init() {
        let support = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!
        let directory = support.appendingPathComponent("VunQuy", isDirectory: true)
        try? FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        self.fileURL = directory.appendingPathComponent("goals.json")

        self.encoder = JSONEncoder()
        self.encoder.outputFormatting = [.prettyPrinted, .sortedKeys]
        self.encoder.dateEncodingStrategy = .iso8601

        self.decoder = JSONDecoder()
        self.decoder.dateDecodingStrategy = .iso8601

        load()
    }

    var activeGoals: [SavingGoal] {
        goals
            .filter { !$0.isArchived }
            .sorted { lhs, rhs in
                if lhs.isCompleted != rhs.isCompleted { return !lhs.isCompleted }
                return lhs.createdAt > rhs.createdAt
            }
    }

    var archivedGoals: [SavingGoal] {
        goals.filter(\.isArchived).sorted { $0.createdAt > $1.createdAt }
    }

    var totalSaved: Double {
        activeGoals.reduce(0) { $0 + max($1.savedAmount, 0) }
    }

    var defaultCurrency: String {
        get { UserDefaults.standard.string(forKey: "defaultCurrency") ?? "VND" }
        set { UserDefaults.standard.set(newValue, forKey: "defaultCurrency") }
    }

    func add(_ goal: SavingGoal) {
        goals.insert(goal, at: 0)
    }

    func update(_ goal: SavingGoal) {
        guard let index = goals.firstIndex(where: { $0.id == goal.id }) else { return }
        goals[index] = goal
    }

    func delete(_ goal: SavingGoal) {
        NotificationService.shared.remove(goalID: goal.id)
        goals.removeAll { $0.id == goal.id }
    }

    func archive(_ goal: SavingGoal) {
        guard let index = goals.firstIndex(where: { $0.id == goal.id }) else { return }
        goals[index].isArchived = true
        NotificationService.shared.remove(goalID: goal.id)
    }

    func restore(_ goal: SavingGoal) {
        guard let index = goals.firstIndex(where: { $0.id == goal.id }) else { return }
        goals[index].isArchived = false
    }

    @discardableResult
    func addTransaction(
        goalID: UUID,
        kind: SavingTransaction.Kind,
        amount: Double,
        note: String = ""
    ) -> String? {
        guard amount > 0 else { return "Số tiền phải lớn hơn 0" }
        guard let index = goals.firstIndex(where: { $0.id == goalID }) else {
            return "Không tìm thấy mục tiêu"
        }

        if kind == .withdrawal && amount > goals[index].savedAmount {
            return "Số tiền rút lớn hơn số tiền hiện có"
        }

        goals[index].transactions.insert(
            SavingTransaction(kind: kind, amount: amount, note: note),
            at: 0
        )
        return nil
    }

    func exportData() throws -> Data {
        try encoder.encode(goals)
    }

    func importData(_ data: Data) throws {
        let imported = try decoder.decode([SavingGoal].self, from: data)
        goals = imported
    }

    private func load() {
        guard let data = try? Data(contentsOf: fileURL), !data.isEmpty else { return }
        if let decoded = try? decoder.decode([SavingGoal].self, from: data) {
            goals = decoded
        }
    }

    private func persist() {
        guard let data = try? encoder.encode(goals) else { return }
        try? data.write(to: fileURL, options: [.atomic])
    }
}
