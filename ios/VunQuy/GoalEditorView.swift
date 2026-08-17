import SwiftUI

struct GoalEditorView: View {
    @EnvironmentObject private var store: GoalStore
    @Environment(\.dismiss) private var dismiss

    private let existingGoal: SavingGoal?

    @State private var title: String
    @State private var targetAmount: Double
    @State private var notes: String
    @State private var priority: GoalPriority
    @State private var remindersEnabled: Bool
    @State private var hasDeadline: Bool
    @State private var deadline: Date

    init(goal: SavingGoal? = nil) {
        self.existingGoal = goal
        _title = State(initialValue: goal?.title ?? "")
        _targetAmount = State(initialValue: goal?.targetAmount ?? 0)
        _notes = State(initialValue: goal?.notes ?? "")
        _priority = State(initialValue: goal?.priority ?? .normal)
        _remindersEnabled = State(initialValue: goal?.remindersEnabled ?? false)
        _hasDeadline = State(initialValue: goal?.deadline != nil)
        _deadline = State(initialValue: goal?.deadline ?? Calendar.current.date(byAdding: .month, value: 3, to: Date()) ?? Date())
    }

    var body: some View {
        NavigationStack {
            ZStack {
                VunQuyBackdrop()
                Form {
                    Section("Mục tiêu") {
                        TextField("Tên mục tiêu", text: $title)
                            .textInputAutocapitalization(.sentences)

                        TextField(
                            "Số tiền mục tiêu",
                            value: $targetAmount,
                            format: .number.precision(.fractionLength(0...2))
                        )
                        .keyboardType(.decimalPad)

                        Picker("Mức ưu tiên", selection: $priority) {
                            ForEach(GoalPriority.allCases) { item in
                                Text(item.title).tag(item)
                            }
                        }
                    }

                    Section("Thời gian") {
                        Toggle("Đặt hạn chót", isOn: $hasDeadline.animation())
                        if hasDeadline {
                            DatePicker(
                                "Hạn chót",
                                selection: $deadline,
                                in: Date()...,
                                displayedComponents: .date
                            )
                        }
                    }

                    Section("Nhắc nhở") {
                        Toggle("Nhắc tiết kiệm", isOn: $remindersEnabled)
                        Text("Tần suất nhắc thay đổi theo mức ưu tiên của mục tiêu.")
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                    }

                    Section("Ghi chú") {
                        TextField("Ghi chú không bắt buộc", text: $notes, axis: .vertical)
                            .lineLimit(3...7)
                    }
                }
                .scrollContentBackground(.hidden)
            }
            .navigationTitle(existingGoal == nil ? "Mục tiêu mới" : "Chỉnh sửa mục tiêu")
            .navigationBarTitleDisplayMode(.inline)
            .toolbarBackground(.hidden, for: .navigationBar)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Hủy") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Lưu") { save() }
                        .fontWeight(.semibold)
                        .disabled(!isValid)
                }
            }
        }
    }

    private var isValid: Bool {
        !title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && targetAmount > 0
    }

    private func save() {
        guard isValid else { return }
        let cleanTitle = title.trimmingCharacters(in: .whitespacesAndNewlines)
        let cleanNotes = notes.trimmingCharacters(in: .whitespacesAndNewlines)

        if var goal = existingGoal {
            goal.title = cleanTitle
            goal.targetAmount = targetAmount
            goal.deadline = hasDeadline ? deadline : nil
            goal.notes = cleanNotes
            goal.priority = priority
            goal.remindersEnabled = remindersEnabled
            store.update(goal)
        } else {
            store.add(
                SavingGoal(
                    title: cleanTitle,
                    targetAmount: targetAmount,
                    currencyCode: store.defaultCurrency,
                    deadline: hasDeadline ? deadline : nil,
                    notes: cleanNotes,
                    priority: priority,
                    remindersEnabled: remindersEnabled
                )
            )
        }
        dismiss()
    }
}
