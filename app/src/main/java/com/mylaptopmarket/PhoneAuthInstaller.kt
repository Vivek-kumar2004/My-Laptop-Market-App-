package com.mylaptopmarket

import android.app.Activity
import android.app.AlertDialog
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.mylaptopmarket.service.PhoneAuthBridge

object PhoneAuthInstaller {
    private var webView: WebView? = null
    private var dialog: AlertDialog? = null
    private var session = 0
    private val main = Handler(Looper.getMainLooper())

    fun install(activity: Activity) {
        PhoneAuthBridge.start = { phone ->
            val mobile = phone.removePrefix("+91").filter { it.isDigit() }.take(10)
            activity.runOnUiThread { open(activity, mobile) }
        }
        PhoneAuthBridge.confirm = { _, code ->
            val digits = code.filter { it.isDigit() }.take(6)
            activity.runOnUiThread {
                val view = webView
                if (view == null) {
                    PhoneAuthBridge.emitError("Send an OTP to this mobile number first.")
                } else {
                    view.evaluateJavascript("confirmCode('$digits')", null)
                }
            }
        }
    }

    private fun open(activity: Activity, mobile: String) {
        session += 1
        val sessionId = session
        dialog?.dismiss()
        webView?.destroy()
        val density = activity.resources.displayMetrics.density
        val view = WebView(activity)
        webView = view
        view.settings.javaScriptEnabled = true
        view.settings.domStorageEnabled = true
        view.settings.userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        view.addJavascriptInterface(JsBridge(), "Android")
        view.webViewClient = WebViewClient()
        view.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            (460 * density).toInt()
        )
        view.loadUrl("https://mylaptopmarket.com/api/vendor/phone-auth?mobile=$mobile")
        dialog = AlertDialog.Builder(activity)
            .setTitle("Verify mobile")
            .setMessage("Tick the check, then the SMS OTP will be sent.")
            .setView(view)
            .setNegativeButton("Cancel") { _, _ ->
                if (sessionId != session || dialog == null) return@setNegativeButton
                close()
                PhoneAuthBridge.emitError("SMS OTP was cancelled.")
            }
            .create()
        dialog?.show()
    }

    private fun close() {
        session += 1
        val current = dialog
        dialog = null
        webView?.destroy()
        webView = null
        current?.dismiss()
    }

    private class JsBridge {
        @JavascriptInterface
        fun onSent() {
            PhoneAuthInstaller.main.post {
                session += 1
                val view = webView
                (view?.parent as? android.view.ViewGroup)?.removeView(view)
                val current = dialog
                dialog = null
                current?.dismiss()
                PhoneAuthBridge.emitCodeSent("sent")
            }
        }

        @JavascriptInterface
        fun onToken(token: String) {
            PhoneAuthInstaller.main.post {
                PhoneAuthInstaller.close()
                if (token.isBlank()) {
                    PhoneAuthBridge.emitError("Could not verify this mobile number.")
                } else {
                    PhoneAuthBridge.emitVerified(token)
                }
            }
        }

        @JavascriptInterface
        fun onError(message: String) {
            PhoneAuthInstaller.main.post {
                val text = message.trim().ifBlank { "Could not verify this mobile number." }
                PhoneAuthBridge.emitError(text.take(180))
            }
        }
    }
}
