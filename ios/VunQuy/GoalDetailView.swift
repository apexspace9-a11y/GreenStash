import SwiftUI

struct GoalDetailView: View {
    @EnvironmentObject private var store: GoalStore
    @Environment(\.dismiss) private var dismiss

    let goalID: UUID

    @State private var showEditor = false
    @State private var showTransaction = false
    @State private var transactionKind: SavingTransaction.Kind = .deposit
    @State private var confirmDelete = false

    private var goal: SavingGoal? {
        store.goals.first { $0.id == goalID }
    }

    var body: some View {
        ZStack {
            VunQuyBackdrop()
            if let goal {
                ScrollView {
                    LazyVStack(spacing: 16) {
                        hero(goal)
                        actions(goal)

                        if !goal.notes.isEmpty {
                            GlassPanel(cornerRadius: 24) {
                                VStack(alignment: .leading, spacing: 8) {
                                    Label("Ghi chú", systemImage: "note.text")
                                        .font(.headline)
                                    Text(goal.notes)
                                        .foregroundStyle(.secondary)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                }
                                .padding(17)
                            }
                        }

                        transactionHistory(goal)
                    }
                    .padding(16)
                    .padding(.bottom, 30)
                }
            } else {
                ContentUnavailableView("Không tìm thấy mục tiêu", systemImage: "questionmark.folder")
            }
        }
        .navigationTitle(goal?.title ?? "Chi tiết")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(.hidden, for: .navigationBar)
        .toolbar {
            if let goal {
                ToolbarItem(placement: .topBarTrailing) {
                    Menu {
                        Button {
                            showEditor = true
                        } label: {
                            Label("Chỉnh sửa", systemImage: "pencil")
                        }
                        if goal.isCompleted && !goal.isArchived {
                            Button {
                                store.archive(goal)
                                dismiss()
                            } label: {
                                Label("Lưu trữ", systemImage: "archivebox")
                            }
                        }
                        Button(role: .destructive) {
                            confirmDelete = true
                        } label: {
                            Label("Xóa mục tiêu", systemImage: "trash")
                        }
                    } label: {
                        Image(systemName: "ellipsis.circle")
                    }
                }
            }
        }
        .sheet(isPresented: $showEditor) {
            if let goal {
                GoalEditorView(goal: goal)
                    .environmentObject(store)
            }
        }
        .sheet(isPresented: $showTransaction) {
            if let goal {
                TransactionEntryView(goal: goal, kind: transactionKind)
                    .environmentObject(store)
            }
        }
        .alert("Xóa mục tiêu?", isPresented: $confirmDelete) {
            Button("Hủy", role: .cancel) {}
            Button("Xóa", role: .destructive) {
                if let goal {
                    store.delete(goal)
                    dismiss()
                }
            }
        } message: {
            Text("Mục tiêu và toàn bộ lịch sử giao dịch của mục tiêu sẽ bị xóa.")
        }
    }

    private func hero(_ goal: SavingGoal) -> some View {
        GlassPanel(cornerRadius: 30) {
            VStack(alignment: .leading, spacing: 14) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Đã tiết kiệm")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                        Text(goal.savedAmount, format: .currency(code: goal.currencyCode))
                            .font(.system(size: 30, weight: .bold, design: .rounded))
                    }
                    Spacer()
                    if goal.isCompleted {
                        Image(systemName: "checkmark.seal.fill")
                            .font(.system(size: 34))
                            .foregroundStyle(.green)
                    }
                }

                ProgressView(value: goal.progress)
                    .tint(goal.isCompleted ? .green : .mint)

                HStack {
                    Text("Mục tiêu")
                    Spacer()
                    Text(goal.targetAmount, format: .currency(code: goal.currencyCode))
                        .fontWeight(.semibold)
                }
                .font(.subheadline)

                Divider().opacity(0.5)

                HStack {
                    Label(goal.priority.title, systemImage: "flag.fill")
                    Spacer()
                    if let deadline = goal.deadline {
                        Label(deadline.formatted(date: .abbreviated, time: .omitted), systemImage: "calendar")
                    } else {
                        Label("Không có hạn chót", systemImage: "calendar.badge.minus")
                    }
                }
                .font(.caption)
                .foregroundStyle(.secondary)
            }
            .padding(20)
        }
    }

    private func actions(_ goal: SavingGoal) -> some View {
        HStack(spacing: 12) {
            Button {
                transactionKind = .deposit
                showTransaction = true
            } label: {
                Label("Thêm tiền", systemImage: "plus.circle.fill")
                    .frame(maxWidth: .infinity)
                    .glassButtonStyle()
            }
            .buttonStyle(.plain)

            Button {
                transactionKind = .withdrawal
                showTransaction = true
            } label: {
                Label("Rút tiền", systemImage: "minus.circle.fill")
                    .frame(maxWidth: .infinity)
                    .glassButtonStyle()
            }
            .buttonStyle(.plain)
            .disabled(goal.savedAmount <= 0)
            .opacity(goal.savedAmount <= 0 ? 0.45 : 1)
        }
    }

    private func transactionHistory(_ goal: SavingGoal) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Lịch sử giao dịch")
                .font(.headline)
                .padding(.horizontal, 4)

            if goal.transactions.isEmpty {
                GlassPanel(cornerRadius: 22) {
                    Text("Chưa có giao dịch")
                        .foregroundStyle(.secondary)
                        .frame(maxWidth: .infinity)
                        .padding(22)
                }
            } else {
                ForEach(goal.transactions) { transaction in
                    GlassPanel(cornerRadius: 22) {
                        HStack(spacing: 13) {
                            Image(systemName: transaction.kind == .deposit ? "arrow.down.circle.fill" : "arrow.up.circle.fill")
                                .font(.title2)
                                .foregroundStyle(transaction.kind == .deposit ? Color.green : Color.orange)
                            VStack(alignment: .leading, spacing: 3) {
                                Text(transaction.kind.title)
                                    .font(.subheadline.weight(.semibold))
                                Text(transaction.date.formatted(date: .abbreviated, time: .shortened))
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                if !transaction.note.isEmpty {
                                    Text(transaction.note)
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                        .lineLimit(2)
                                }
                            }
                            Spacer()
                            Text(
                                transaction.kind == .deposit ? transaction.amount : -transaction.amount,
                                format: .currency(code: goal.currencyCode)
                            )
                            .font(.subheadline.bold())
                        }
                        .padding(15)
                    }
                }
            }
        }
    }
}

private struct TransactionEntryView: View {
    @EnvironmentObject private var store: GoalStore
    @Environment(\.dismiss) private var dismiss

    let goal: SavingGoal
    let kind: SavingTransaction.Kind

    @State private var amount: Double = 0
    @State private var note = ""
    @State private var errorMessage = ""

    var body: some View {
        NavigationStack {
            ZStack {
                VunQuyBackdrop()
                Form {
                    Section(kind.title) {
                        TextField(
                            "Số tiền",
                            value: $amount,
                            format: .number.precision(.fractionLength(0...2))
                        )
                        .keyboardType(.decimalPad)
                        TextField("Ghi chú không bắt buộc", text: $note)
                    }

                    if kind == .withdrawal {
                        Section {
                            LabeledContent("Có thể rút tối đa") {
                                Text(goal.savedAmount, format: .currency(code: goal.currencyCode))
                            }
                        }
                    }

                    if !errorMessage.isEmpty {
                        Section {
                            Text(errorMessage)
                                .foregroundStyle(.red)
                        }
                    }
                }
                .scrollContentBackground(.hidden)
            }
            .navigationTitle(kind.title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbarBackground(.hidden, for: .navigationBar)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Hủy") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Lưu") { save() }
                        .fontWeight(.semibold)
                        .disabled(amount <= 0)
                }
            }
        }
    }

    private func save() {
        errorMessage = store.addTransaction(
            goalID: goal.id,
            kind: kind,
            amount: amount,
            note: note.trimmingCharacters(in: .whitespacesAndNewlines)
        ) ?? ""
        if errorMessage.isEmpty {
            dismiss()
        }
    }
}
