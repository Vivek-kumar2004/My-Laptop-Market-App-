import UIKit
import WebKit
import ComposeApp

private final class PhoneAuthHandler: NSObject, WKScriptMessageHandler {
    func userContentController(
        _ userContentController: WKUserContentController,
        didReceive message: WKScriptMessage
    ) {
        let body: [String: Any]
        if let typed = message.body as? [String: Any] {
            body = typed
        } else if let object = message.body as? NSDictionary {
            var mapped: [String: Any] = [:]
            for (key, value) in object {
                if let name = key as? String {
                    mapped[name] = value
                }
            }
            body = mapped
        } else {
            body = [:]
        }
        let type = body["type"] as? String ?? ""
        DispatchQueue.main.async {
            if type == "sent" {
                PhoneAuthSetup.hideSheet()
                PhoneAuthBridge.shared.emitCodeSent(verificationId: "sent")
                return
            }
            if type == "token" {
                let token = body["token"] as? String ?? ""
                PhoneAuthSetup.close()
                if token.isEmpty {
                    PhoneAuthBridge.shared.emitError(message: "Could not verify this mobile number.")
                } else {
                    PhoneAuthBridge.shared.emitVerified(idToken: token)
                }
                return
            }
            let text = (body["message"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines)
            PhoneAuthBridge.shared.emitError(message: (text?.isEmpty == false ? text! : "Could not verify this mobile number."))
        }
    }
}

private final class PhoneAuthController: UIViewController {
    var onCancel: (() -> Void)?
    let webView: WKWebView
    private let cancelButton = UIButton(type: .system)
    private var cancelReady = false

    init(webView: WKWebView) {
        self.webView = webView
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        cancelButton.setTitle("Cancel", for: .normal)
        cancelButton.titleLabel?.font = .systemFont(ofSize: 17, weight: .semibold)
        cancelButton.addTarget(self, action: #selector(cancelTapped), for: .touchUpInside)
        cancelButton.translatesAutoresizingMaskIntoConstraints = false
        webView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(cancelButton)
        view.addSubview(webView)
        NSLayoutConstraint.activate([
            cancelButton.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor, constant: 16),
            cancelButton.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 8),
            webView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            webView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            webView.topAnchor.constraint(equalTo: cancelButton.bottomAnchor, constant: 8),
            webView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.8) { [weak self] in
            self?.cancelReady = true
        }
    }

    @objc private func cancelTapped() {
        guard cancelReady else { return }
        onCancel?()
    }
}

enum PhoneAuthSetup {
    private static var webView: WKWebView?
    private static var sheet: UIViewController?
    private static var sheetWindow: UIWindow?
    private static var parkWindow: UIWindow?
    private static var session = 0

    static func install() {
        PhoneAuthBridge.shared.start = { phone in
            let mobile = phone.replacingOccurrences(of: "+91", with: "").filter(\.isNumber)
            DispatchQueue.main.async { present(mobile: String(mobile.prefix(10))) }
        }
        PhoneAuthBridge.shared.confirm = { _, code in
            let digits = String(code.filter(\.isNumber).prefix(6))
            DispatchQueue.main.async {
                guard let webView else {
                    PhoneAuthBridge.shared.emitError(message: "Send an OTP to this mobile number first.")
                    return
                }
                webView.evaluateJavaScript("confirmCode('\(digits)')") { _, error in
                    if error != nil {
                        DispatchQueue.main.async {
                            PhoneAuthBridge.shared.emitError(message: "Could not verify this mobile number. Send the SMS again.")
                        }
                    }
                }
            }
        }
    }

    private static func present(mobile: String) {
        close()
        let sessionId = session
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.35) {
            guard sessionId == session else { return }
            show(mobile: mobile, sessionId: sessionId)
        }
    }

    private static func show(mobile: String, sessionId: Int) {
        guard sessionId == session else { return }
        guard let scene = foregroundScene() else {
            PhoneAuthBridge.shared.emitError(message: "Phone verification is not ready in this build.")
            return
        }
        let config = WKWebViewConfiguration()
        config.userContentController.add(PhoneAuthHandler(), name: "mlm")
        let web = WKWebView(frame: .zero, configuration: config)
        web.customUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Mobile/15E148 Safari/604.1"
        let controller = PhoneAuthController(webView: web)
        controller.onCancel = { userCancelled(sessionId: sessionId) }
        let window = UIWindow(windowScene: scene)
        window.frame = scene.screen.bounds
        window.windowLevel = UIWindow.Level.normal + 1
        window.rootViewController = controller
        window.makeKeyAndVisible()
        sheetWindow = window
        sheet = controller
        webView = web
        let encoded = mobile.addingPercentEncoding(withAllowedCharacters: .decimalDigits) ?? ""
        if let url = URL(string: "https://mylaptopmarket.com/api/vendor/phone-auth?mobile=\(encoded)") {
            web.load(URLRequest(url: url))
        }
    }

    static func hideSheet() {
        session += 1
        if let webView {
            webView.removeFromSuperview()
            park(webView)
        }
        sheet = nil
        sheetWindow?.isHidden = true
        sheetWindow = nil
        restoreAppWindow()
    }

    static func close() {
        session += 1
        let presented = sheet
        sheet = nil
        sheetWindow?.isHidden = true
        sheetWindow = nil
        parkWindow?.isHidden = true
        parkWindow = nil
        webView?.stopLoading()
        webView?.removeFromSuperview()
        webView = nil
        presented?.dismiss(animated: false)
        restoreAppWindow()
    }

    private static func userCancelled(sessionId: Int) {
        guard sessionId == session, sheet != nil else { return }
        close()
        PhoneAuthBridge.shared.emitError(message: "SMS OTP was cancelled.")
    }

    private static func foregroundScene() -> UIWindowScene? {
        let scenes = UIApplication.shared.connectedScenes.compactMap { $0 as? UIWindowScene }
        return scenes.first { $0.activationState == .foregroundActive } ?? scenes.first
    }

    private static func restoreAppWindow() {
        guard let scene = foregroundScene() else { return }
        let appWindow = scene.windows.first { window in
            window !== sheetWindow && window !== parkWindow && window.windowLevel == .normal
        }
        appWindow?.makeKey()
    }

    private static func park(_ webView: WKWebView) {
        guard let scene = foregroundScene() else { return }
        let window = UIWindow(windowScene: scene)
        window.frame = CGRect(x: -400, y: -400, width: 320, height: 480)
        window.windowLevel = UIWindow.Level(rawValue: -100)
        let host = UIViewController()
        webView.translatesAutoresizingMaskIntoConstraints = true
        webView.frame = CGRect(x: 0, y: 0, width: 320, height: 480)
        host.view.addSubview(webView)
        window.rootViewController = host
        window.isHidden = false
        parkWindow = window
    }
}
