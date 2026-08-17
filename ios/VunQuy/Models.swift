import Foundation

enum GoalPriority: String, Codable, CaseIterable, Identifiable {
    case high
    case normal
    case low

    var id: String { rawValue }

    var title: String {
        switch self {
        case .high: "Cao"
        case .normal: "Bình thường"
        case .low: "Thấp"
        }
    }
}

struct SavingTransaction: Identifiable, Codable, Hashable {
    enum Kind: String, Codable {
        case deposit
        case withdrawal

        var title: String {
            switch self {
            case .deposit: "Thêm tiền"
            case .withdrawal: "Rút tiền"
            }
        }
    }

    let id: UUID
    let kind: Kind
    let amount: Double
    let date: Date
    let note: String

    init(
        id: UUID = UUID(),
        kind: Kind,
        amount: Double,
        date: Date = Date(),
        note: String = ""
    ) {
        self.id = id
        self.kind = kind
        self.amount = amount
        self.date = date
        self.note = note
    }
}

struct SavingGoal: Identifiable, Codable, Hashable {
    let id: UUID
    var title: String
    var targetAmount: Double
    var currencyCode: String
    var deadline: Date?
    var notes: String
    var priority: GoalPriority
    var remindersEnabled: Bool
    var isArchived: Bool
    let createdAt: Date
    var transactions: [SavingTransaction]

    init(
        id: UUID = UUID(),
        title: String,
        targetAmount: Double,
        currencyCode: String = "VND",
        deadline: Date? = nil,
        notes: String = "",
        priority: GoalPriority = .normal,
        remindersEnabled: Bool = false,
        isArchived: Bool = false,
        createdAt: Date = Date(),
        transactions: [SavingTransaction] = []
    ) {
        self.id = id
        self.title = title
        self.targetAmount = targetAmount
        self.currencyCode = currencyCode
        self.deadline = deadline
        self.notes = notes
        self.priority = priority
        self.remindersEnabled = remindersEnabled
        self.isArchived = isArchived
        self.createdAt = createdAt
        self.transactions = transactions
    }

    var savedAmount: Double {
        transactions.reduce(0) { partial, transaction in
            switch transaction.kind {
            case .deposit: partial + transaction.amount
            case .withdrawal: partial - transaction.amount
            }
        }
    }

    var progress: Double {
        guard targetAmount > 0 else { return 0 }
        return min(max(savedAmount / targetAmount, 0), 1)
    }

    var isCompleted: Bool {
        targetAmount > 0 && savedAmount >= targetAmount
    }
}
