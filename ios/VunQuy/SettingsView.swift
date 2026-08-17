import SwiftUI
import UniformTypeIdentifiers

struct SettingsView: View {
    @EnvironmentObject private var store: GoalStore
    @AppStorage("biometricLockEnabled") private var biometricLockEnabled = false

    @State private var currency = "VND"
    @State private var exporting = false
    @State private var importing = false
    @State private var backupDocument = BackupDocument()
    @State private var alertMessage = ""
    @State private var showAlert = false

    private let currencies = ["VND", "USD", "EUR", "JPY", "KRW", "SGD", "THB", "AUD"]

    var body: some View {
        NavigationStack {
            ZStack {
                VunQuyBackdrop()
                ScrollView {
                    VStack(spacing: 16) {
                        GlassPanel(cornerRadius: 28) {
                            HStack(spacing: 14) {
                                BrandMark(size: 58)
                                VStack(alignment: .leading, spacing: 4) {
                                    Text("VunQuỹ")
                                        .font(.title2.bold())
                                    Text("Mục tiêu tài chính cá nhân")
                                        .font(.subheadline)
                                        .foregroundStyle(.secondary)
                                }
                                Spacer()
                            }
                            .padding(18)
                        }

                        GlassPanel(cornerRadius: 26) {
                            VStack(spacing: 0) {
                                HStack {
                                    Label("Đơn vị tiền tệ mặc định", systemImage: "banknote")
                                    Spacer()
                                    Picker("Tiền tệ", selection: $currency) {
                                        ForEach(currencies, id: \.self) { code in
                                            Text(code).tag(code)
                                        }
                                    }
                                    .labelsHidden()
                                }
                                .padding(16)

                                Divider().padding(.leading, 16)

                                Toggle(isOn: $biometricLockEnabled) {
                                    Label("Khóa sinh trắc học", systemImage: "faceid")
                                }
                                .padding(16)
                            }
                        }

                        GlassPanel(cornerRadius: 26) {
                            VStack(spacing: 0) {
                                Button {
                                    prepareExport()
                                } label: {
                                    HStack {
                                        Label("Xuất bản sao lưu", systemImage: "square.and.arrow.up")
                                        Spacer()
                                        Image(systemName: "chevron.right")
                                            .foregroundStyle(.tertiary)
                                    }
                                    .contentShape(Rectangle())
                                    .padding(16)
                                }
                                .buttonStyle(.plain)

                                Divider().padding(.leading, 16)

                                Button {
                                    importing = true
                                } label: {
                                    HStack {
                                        Label("Khôi phục từ bản sao lưu", systemImage: "square.and.arrow.down")
                                        Spacer()
                                        Image(systemName: "chevron.right")
                                            .foregroundStyle(.tertiary)
                                    }
                                    .contentShape(Rectangle())
                                    .padding(16)
                                }
                                .buttonStyle(.plain)
                            }
                        }

                        Text("Dữ liệu mục tiêu được lưu cục bộ trên thiết bị. Bản sao lưu chỉ được tạo khi bạn chủ động xuất tệp.")
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 12)
                    }
                    .padding(16)
                    .padding(.bottom, 30)
                }
            }
            .navigationTitle("Cài đặt")
            .toolbarBackground(.hidden, for: .navigationBar)
            .onAppear { currency = store.defaultCurrency }
            .onChange(of: currency) { _, value in
                store.defaultCurrency = value
            }
            .fileExporter(
                isPresented: $exporting,
                document: backupDocument,
                contentType: .json,
                defaultFilename: "VunQuy-backup"
            ) { result in
                if case .failure = result {
                    alertMessage = "Không thể xuất bản sao lưu."
                    showAlert = true
                }
            }
            .fileImporter(
                isPresented: $importing,
                allowedContentTypes: [.json],
                allowsMultipleSelection: false
            ) { result in
                restore(result)
            }
            .alert("VunQuỹ", isPresented: $showAlert) {
                Button("Đồng ý", role: .cancel) {}
            } message: {
                Text(alertMessage)
            }
        }
    }

    private func prepareExport() {
        do {
            backupDocument = BackupDocument(data: try store.exportData())
            exporting = true
        } catch {
            alertMessage = "Không thể tạo bản sao lưu."
            showAlert = true
        }
    }

    private func restore(_ result: Result<[URL], Error>) {
        do {
            let urls = try result.get()
            guard let url = urls.first else { return }
            let hasAccess = url.startAccessingSecurityScopedResource()
            defer { if hasAccess { url.stopAccessingSecurityScopedResource() } }
            let data = try Data(contentsOf: url)
            try store.importData(data)
            alertMessage = "Đã khôi phục dữ liệu thành công."
            showAlert = true
        } catch {
            alertMessage = "Tệp sao lưu không hợp lệ hoặc không thể đọc."
            showAlert = true
        }
    }
}

struct BackupDocument: FileDocument {
    static var readableContentTypes: [UTType] { [.json] }

    var data: Data

    init(data: Data = Data()) {
        self.data = data
    }

    init(configuration: ReadConfiguration) throws {
        self.data = configuration.file.regularFileContents ?? Data()
    }

    func fileWrapper(configuration: WriteConfiguration) throws -> FileWrapper {
        FileWrapper(regularFileWithContents: data)
    }
}
