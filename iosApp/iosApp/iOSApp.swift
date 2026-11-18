import SwiftUI
import UIKit
import AVFoundation
import Speech
import UserNotifications
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
            guard granted else { return }
            DispatchQueue.main.async {
                UIApplication.shared.registerForRemoteNotifications()
            }
        }
        return true
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        let token = deviceToken.map { String(format: "%02x", $0) }.joined()
        #if DEBUG
        let environment = "sandbox"
        #else
        let environment = "production"
        #endif
        if isAdminApp {
            AdminPushBridge.shared.onToken(token: token, environment: environment)
        } else {
            VendorPushBridge.shared.onToken(token: token, environment: environment)
        }
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        print("Admin APNs register failed: \(error.localizedDescription)")
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound, .badge])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let info = response.notification.request.content.userInfo
        let type = info["type"] as? String ?? ""
        if isAdminApp {
            AdminPushBridge.shared.onOpen(target: type)
        } else {
            VendorPushBridge.shared.onOpen(
                type: type,
                targetId: Self.text(info["target_id"]),
                targetName: Self.text(info["target_name"])
            )
        }
        completionHandler()
    }

    private var isAdminApp: Bool {
        (Bundle.main.bundleIdentifier ?? "").contains("admin")
    }

    private static func text(_ value: Any?) -> String {
        switch value {
        case let text as String:
            return text
        case let number as NSNumber:
            return number.stringValue
        default:
            return ""
        }
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @State private var storeUpdateURL: URL?

    init() {
        PhoneAuthSetup.install()
        FloorDeskSpeech.shared.install()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    IosDeepLinkBus.shared.publish(uri: url.absoluteString)
                }
                .task {
                    storeUpdateURL = await StoreUpdate.newerVersionURL()
                }
                .alert("Update available", isPresented: storeUpdatePresented) {
                    Button("Update") {
                        if let storeUpdateURL {
                            UIApplication.shared.open(storeUpdateURL)
                        }
                    }
                    Button("Not now", role: .cancel) {}
                } message: {
                    Text("A new version is on the App Store.")
                }
        }
    }

    private var storeUpdatePresented: Binding<Bool> {
        Binding(
            get: { storeUpdateURL != nil },
            set: { if !$0 { storeUpdateURL = nil } }
        )
    }
}

private enum StoreUpdate {
    static func newerVersionURL() async -> URL? {
        let bundleId = Bundle.main.bundleIdentifier ?? ""
        guard !bundleId.contains("admin") else { return nil }
        guard let current = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String else { return nil }
        guard let lookup = URL(string: "https://itunes.apple.com/lookup?bundleId=\(bundleId)&country=in") else { return nil }
        guard let (data, _) = try? await URLSession.shared.data(from: lookup),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let results = json["results"] as? [[String: Any]],
              let latest = results.first?["version"] as? String,
              isNewer(latest, than: current) else { return nil }
        let offeredKey = "mlm-offered-store-version"
        if UserDefaults.standard.string(forKey: offeredKey) == latest { return nil }
        UserDefaults.standard.set(latest, forKey: offeredKey)
        let trackId: String? = {
            switch results.first?["trackId"] {
            case let number as NSNumber:
                return number.stringValue
            case let text as String:
                return text
            default:
                return nil
            }
        }()
        guard let trackId else { return nil }
        return URL(string: "itms-apps://apps.apple.com/app/id\(trackId)")
    }

    private static func isNewer(_ store: String, than current: String) -> Bool {
        let left = store.split(separator: ".").map { Int($0) ?? 0 }
        let right = current.split(separator: ".").map { Int($0) ?? 0 }
        for index in 0..<max(left.count, right.count) {
            let storePart = index < left.count ? left[index] : 0
            let currentPart = index < right.count ? right[index] : 0
            if storePart != currentPart { return storePart > currentPart }
        }
        return false
    }
}

final class FloorDeskSpeech: NSObject, SFSpeechRecognizerDelegate {
    static let shared = FloorDeskSpeech()

    private let audioEngine = AVAudioEngine()
    private var recognizer: SFSpeechRecognizer?
    private var request: SFSpeechAudioBufferRecognitionRequest?
    private var task: SFSpeechRecognitionTask?
    private var tapInstalled = false
    private var sessionReady = false
    private var silenceWork: DispatchWorkItem?
    private var maxListenWork: DispatchWorkItem?

    func install() {
        IosSpeechBridge.shared.startListening = { [weak self] languageCode in
            self?.start(languageCode: languageCode)
        }
        IosSpeechBridge.shared.stopListening = { [weak self] in
            self?.stop(teardownEngine: true)
        }
    }

    func start(languageCode: String) {
        beginNewRecognition(languageCode: languageCode)
    }

    private func beginNewRecognition(languageCode: String) {
        silenceWork?.cancel()
        maxListenWork?.cancel()
        task?.cancel()
        request?.endAudio()
        task = nil
        request = nil

        let preferred = SFSpeechRecognizer(locale: Locale(identifier: languageCode))
        let fallback = SFSpeechRecognizer(locale: Locale(identifier: "en-US"))
        recognizer = (preferred?.isAvailable == true ? preferred : fallback)
        recognizer?.delegate = self

        guard recognizer?.isAvailable == true else {
            IosSpeechBridge.shared.emitError(message: "Speech recognition is not available on this iPhone.")
            return
        }

        SFSpeechRecognizer.requestAuthorization { status in
            DispatchQueue.main.async {
                guard status == .authorized else {
                    IosSpeechBridge.shared.emitError(message: "Speech recognition is off. Enable it in Settings > MyLaptopMarket.")
                    return
                }
                self.requestMicrophoneThenRecord()
            }
        }
    }

    private func requestMicrophoneThenRecord() {
        AVAudioSession.sharedInstance().requestRecordPermission { granted in
            DispatchQueue.main.async {
                if granted {
                    self.beginRecording()
                } else {
                    IosSpeechBridge.shared.emitError(message: "Microphone is off. Enable it in Settings > MyLaptopMarket.")
                }
            }
        }
    }

    private func beginRecording() {
        do {
            try ensureSession()
            try ensureEngineRunning()

            let request = SFSpeechAudioBufferRecognitionRequest()
            request.shouldReportPartialResults = true
            request.requiresOnDeviceRecognition = false
            self.request = request

            task = recognizer?.recognitionTask(with: request) { [weak self] result, error in
                guard let self else { return }
                if let result {
                    let text = result.bestTranscription.formattedString
                    if result.isFinal {
                        self.silenceWork?.cancel()
                        self.maxListenWork?.cancel()
                        IosSpeechBridge.shared.emitFinal(text: text)
                        self.finishCurrentRequest()
                    } else if !text.isEmpty {
                        IosSpeechBridge.shared.emitPartial(text: text)
                        self.scheduleFinalizeAfterPause()
                    }
                }
                if let error, result?.isFinal != true {
                    let message = error.localizedDescription
                    if message.lowercased().contains("cancel") { return }
                    IosSpeechBridge.shared.emitError(message: message)
                    self.finishCurrentRequest()
                }
            }

            maxListenWork?.cancel()
            let cap = DispatchWorkItem { [weak self] in
                self?.request?.endAudio()
            }
            maxListenWork = cap
            DispatchQueue.main.asyncAfter(deadline: .now() + 12, execute: cap)
        } catch {
            IosSpeechBridge.shared.emitError(message: "Could not start the microphone. Use a real iPhone — Simulator mic is limited.")
            stop(teardownEngine: true)
        }
    }

    private func ensureSession() throws {
        if sessionReady { return }
        let session = AVAudioSession.sharedInstance()
        try session.setCategory(.playAndRecord, mode: .default, options: [.defaultToSpeaker, .duckOthers])
        try session.setActive(true, options: .notifyOthersOnDeactivation)
        sessionReady = true
    }

    private func ensureEngineRunning() throws {
        if !tapInstalled {
            let inputNode = audioEngine.inputNode
            // nil format uses the node's hardware format and avoids simulator reconfig crashes
            inputNode.installTap(onBus: 0, bufferSize: 1024, format: nil) { [weak self] buffer, _ in
                self?.request?.append(buffer)
            }
            tapInstalled = true
        }
        if !audioEngine.isRunning {
            audioEngine.prepare()
            try audioEngine.start()
        }
    }

    private func scheduleFinalizeAfterPause() {
        silenceWork?.cancel()
        let work = DispatchWorkItem { [weak self] in
            self?.request?.endAudio()
        }
        silenceWork = work
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.3, execute: work)
    }

    private func finishCurrentRequest() {
        silenceWork?.cancel()
        maxListenWork?.cancel()
        request?.endAudio()
        task?.finish()
        task = nil
        request = nil
    }

    func stop(teardownEngine: Bool) {
        silenceWork?.cancel()
        maxListenWork?.cancel()
        silenceWork = nil
        maxListenWork = nil
        request?.endAudio()
        task?.cancel()
        task = nil
        request = nil
        guard teardownEngine else { return }
        if tapInstalled {
            audioEngine.inputNode.removeTap(onBus: 0)
            tapInstalled = false
        }
        if audioEngine.isRunning {
            audioEngine.stop()
        }
    }
}

