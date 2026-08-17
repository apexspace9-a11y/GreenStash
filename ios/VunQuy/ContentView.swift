import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            HomeView()
                .tabItem { Label("Trang chủ", systemImage: "house.fill") }

            ArchiveView()
                .tabItem { Label("Lưu trữ", systemImage: "archivebox.fill") }

            SettingsView()
                .tabItem { Label("Cài đặt", systemImage: "gearshape.fill") }
        }
        .tint(Color(red: 0.04, green: 0.52, blue: 0.35))
    }
}

private struct HomeView: View {
    @EnvironmentObject private var store: GoalStore
    @State private var showEditor = false

    var body: some View {
        NavigationStack {
            ZStack {
                VunQuyBackdrop()

                ScrollView {
                    LazyVStack(spacing: 16) {
                        header
                        summary

                        if store.activeGoals.isEmpty {
                            emptyState
                        } else {
                            ForEach(store.activeGoals) { goal in
                                NavigationLink {
                                    GoalDetailView(goalID: goal.id)
                                } label: {
                                    GoalCardView(goal: goal)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 110)
                }
            }
            .navigationBarHidden(true)
            .safeAreaInset(edge: .bottom) {
                HStack {
                    Spacer()
                    Button {
                        showEditor = true
                    } label: {
                        Label("Mục tiêu mới", systemImage: "plus")
                            .font(.headline)
                            .foregroundStyle(.primary)
                            .glassButtonStyle()
                            .shadow(color: .black.opacity(0.12), radius: 12, y: 7)
                    }
                    .buttonStyle(.plain)
                    .padding(.trailing, 18)
                    .padding(.bottom, 6)
                }
            }
            .sheet(isPresented: $showEditor) {
                GoalEditorView()
                    .environmentObject(store)
            }
        }
    }

    private var header: some View {
        HStack(spacing: 13) {
            BrandMark(size: 48)
            VStack(alignment: .leading, spacing: 2) {
                Text("VunQuỹ")
                    .font(.title2.bold())
                Text("Tích lũy theo cách của bạn")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
            Spacer()
        }
        .padding(.top, 12)
    }

    private var summary: some View {
        GlassPanel(cornerRadius: 30) {
            VStack(alignment: .leading, spacing: 12) {
                Text("Tổng đang tích lũy")
                    .font(.subheadline.weight(.medium))
                    .foregroundStyle(.secondary)
                Text(store.totalSaved, format: .currency(code: store.defaultCurrency))
                    .font(.system(size: 31, weight: .bold, design: .rounded))
                    .minimumScaleFactor(0.7)
                HStack {
                    Label("\(store.activeGoals.count) mục tiêu", systemImage: "target")
                    Spacer()
                    let completed = store.activeGoals.filter(\.isCompleted).count
                    Label("\(completed) hoàn thành", systemImage: "checkmark.seal.fill")
                }
                .font(.footnote.weight(.medium))
                .foregroundStyle(.secondary)
            }
            .padding(20)
        }
    }

    private var emptyState: some View {
        GlassPanel(cornerRadius: 30) {
            VStack(spacing: 14) {
                Image(systemName: "leaf.circle.fill")
                    .font(.system(size: 48))
                    .foregroundStyle(.green)
                Text("Bắt đầu mục tiêu đầu tiên")
                    .font(.headline)
                Text("Tạo một mục tiêu, ghi lại từng khoản thêm hoặc rút và theo dõi tiến độ ngay tại đây.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
            }
            .frame(maxWidth: .infinity)
            .padding(30)
        }
        .padding(.top, 8)
    }
}

struct GoalCardView: View {
    let goal: SavingGoal

    var body: some View {
        GlassPanel(cornerRadius: 26) {
            VStack(alignment: .leading, spacing: 13) {
                HStack(alignment: .firstTextBaseline) {
                    Text(goal.title)
                        .font(.headline)
                        .lineLimit(1)
                    Spacer()
                    if goal.isCompleted {
                        Image(systemName: "checkmark.seal.fill")
                            .foregroundStyle(.green)
                    } else {
                        Text(goal.priority.title)
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(.secondary)
                    }
                }

                ProgressView(value: goal.progress)
                    .tint(goal.isCompleted ? .green : .mint)

                HStack(alignment: .firstTextBaseline) {
                    Text(goal.savedAmount, format: .currency(code: goal.currencyCode))
                        .font(.title3.bold())
                    Text("/ \(goal.targetAmount.formatted(.currency(code: goal.currencyCode)))")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    Spacer()
                }

                HStack {
                    Label(
                        goal.deadline.map { $0.formatted(date: .abbreviated, time: .omitted) } ?? "Không có hạn chót",
                        systemImage: "calendar"
                    )
                    Spacer()
                    Text("\(Int(goal.progress * 100))%")
                        .fontWeight(.semibold)
                }
                .font(.caption)
                .foregroundStyle(.secondary)
            }
            .padding(18)
        }
    }
}

private struct ArchiveView: View {
    @EnvironmentObject private var store: GoalStore

    var body: some View {
        NavigationStack {
            ZStack {
                VunQuyBackdrop()
                ScrollView {
                    LazyVStack(spacing: 14) {
                        if store.archivedGoals.isEmpty {
                            GlassPanel {
                                VStack(spacing: 12) {
                                    Image(systemName: "archivebox")
                                        .font(.largeTitle)
                                        .foregroundStyle(.secondary)
                                    Text("Chưa có mục tiêu đã lưu trữ")
                                        .font(.headline)
                                }
                                .frame(maxWidth: .infinity)
                                .padding(28)
                            }
                        } else {
                            ForEach(store.archivedGoals) { goal in
                                GlassPanel(cornerRadius: 24) {
                                    HStack(spacing: 14) {
                                        VStack(alignment: .leading, spacing: 5) {
                                            Text(goal.title)
                                                .font(.headline)
                                            Text(goal.savedAmount, format: .currency(code: goal.currencyCode))
                                                .foregroundStyle(.secondary)
                                        }
                                        Spacer()
                                        Button("Khôi phục") {
                                            store.restore(goal)
                                        }
                                        .buttonStyle(.bordered)
                                    }
                                    .padding(16)
                                }
                            }
                        }
                    }
                    .padding(16)
                }
            }
            .navigationTitle("Đã lưu trữ")
            .toolbarBackground(.hidden, for: .navigationBar)
        }
    }
}
