"""Vendor login / register / forgot UI matching the auth redesign prototype."""

from __future__ import annotations

import html
import json

from accounts import STATES
from feature_flags import SHOW_VENDOR_OTP

LOGO = "https://storage.googleapis.com/mylaptopmarket-app-web/images/mylaptoplogo.png"

AUTH_CSS = """
:root {
  --brand: #0767d8;
  --brand-hover: #055bb8;
  --ink: #0f172a;
  --muted: #64748b;
  --surface: #f5f7fa;
  --line: #e2e8f0;
  --success: #15803d;
  --success-soft: #ecfdf3;
  --danger: #b42318;
  --danger-soft: #fef3f2;
  --trust: #0767d8;
  --trust-soft: #eff6ff;
}
* { box-sizing: border-box; }
html, body { margin: 0; min-height: 100%; }
body.auth-body {
  font-family: Inter, ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto, sans-serif;
  color: var(--ink);
  background: var(--surface);
  -webkit-font-smoothing: antialiased;
}
a { color: var(--brand); text-decoration: none; }
a:hover { text-decoration: underline; text-underline-offset: 4px; }
button:focus-visible, a:focus-visible, input:focus-visible, select:focus-visible {
  outline: 2px solid var(--brand);
  outline-offset: 2px;
}
.auth-shell {
  min-height: 100dvh;
  display: flex;
  align-items: stretch;
  justify-content: center;
}
.auth-card {
  width: 100%;
  max-width: 100%;
  min-height: 100dvh;
  background: #fff;
  display: flex;
  flex-direction: column;
}
@media (min-width: 768px) {
  .auth-shell { align-items: center; padding: 32px; }
  .auth-card {
    min-height: 0;
    max-width: 420px;
    border: 1px solid var(--line);
    border-radius: 12px;
    overflow: hidden;
  }
}
.auth-header {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 56px;
  padding: 0 8px;
  flex-shrink: 0;
}
.auth-back {
  width: 48px; height: 48px;
  border: 0; background: transparent; border-radius: 12px;
  color: var(--ink); cursor: pointer;
  display: inline-flex; align-items: center; justify-content: center;
}
.auth-back:hover { background: var(--surface); }
.auth-step-label { font-size: 14px; font-weight: 500; color: var(--muted); }
.auth-progress { padding: 0 24px; }
.auth-progress-track {
  height: 4px; width: 100%; border-radius: 999px; background: var(--line); overflow: hidden;
}
.auth-progress-bar {
  height: 100%; border-radius: 999px; background: var(--ink);
  transition: width .3s ease;
}
.auth-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 24px;
}
.auth-footer {
  padding: 16px 24px max(24px, env(safe-area-inset-bottom));
  flex-shrink: 0;
}
.auth-logo { display: flex; align-items: center; gap: 10px; }
.auth-logo-mark {
  width: 32px; height: 32px; border-radius: 8px; background: var(--ink);
  display: inline-flex; align-items: center; justify-content: center; overflow: hidden;
}
.auth-logo-mark img { width: 100%; height: 100%; object-fit: cover; }
.auth-logo-text { font-size: 16px; font-weight: 600; letter-spacing: -0.01em; color: var(--ink); }
.auth-title { margin: 0; font-size: 24px; line-height: 32px; font-weight: 600; color: var(--ink); }
.auth-sub { margin: 8px 0 0; font-size: 16px; line-height: 24px; color: var(--muted); }
.auth-trust {
  display: flex; gap: 8px; align-items: flex-start;
  margin: 0 0 24px; padding: 12px; border-radius: 12px;
  background: var(--surface); color: var(--muted); font-size: 13px; line-height: 20px;
}
.auth-trust svg { flex-shrink: 0; margin-top: 2px; color: var(--success); }
.field { width: 100%; margin: 0 0 20px; }
.field-box {
  display: flex; align-items: stretch; height: 56px;
  border: 1px solid var(--line); border-radius: 12px; background: var(--surface);
  overflow: hidden; transition: border-color .15s, box-shadow .15s, background .15s;
}
.field-box:focus-within {
  border-color: var(--brand); background: #fff;
  box-shadow: 0 0 0 2px rgba(7,103,216,.2);
}
.field-box.is-error {
  border-color: var(--danger); background: #fff;
}
.field-box.is-error:focus-within { box-shadow: 0 0 0 2px rgba(180,35,24,.2); }
.field-prefix {
  display: flex; align-items: center; padding: 0 12px 0 16px;
  border-right: 1px solid var(--line); font-size: 16px; font-weight: 500; color: var(--ink);
}
.field-inner { position: relative; flex: 1; min-width: 0; }
.field-inner input, .field-inner select {
  width: 100%; height: 100%; border: 0; background: transparent;
  padding: 18px 16px 0; font-size: 16px; color: var(--ink); outline: none;
  appearance: none; -webkit-appearance: none;
}
.field-inner select { padding-right: 36px; cursor: pointer; }
.field-inner label {
  position: absolute; left: 16px; top: 50%; transform: translateY(-50%);
  font-size: 16px; color: var(--muted); pointer-events: none; transition: all .15s;
}
.field-inner input:focus + label,
.field-inner input:not(:placeholder-shown) + label,
.field-inner select:focus + label,
.field-inner.select-filled + label {
  top: 8px; transform: none; font-size: 12px; color: var(--brand);
}
.field-box.is-error .field-inner input:focus + label,
.field-box.is-error .field-inner input:not(:placeholder-shown) + label { color: var(--danger); }
.field-action { display: flex; align-items: center; padding-right: 8px; }
.field-action button, .field-action .inline-btn {
  height: 48px; border: 0; border-radius: 8px; padding: 0 12px;
  background: transparent; color: var(--brand); font-size: 14px; font-weight: 500; cursor: pointer;
}
.field-action button:hover { background: rgba(7,103,216,.05); }
.field-action .send-otp {
  height: 48px; border-radius: 8px; padding: 0 16px;
  background: var(--brand); color: #fff; font-size: 14px; font-weight: 600;
}
.field-action .send-otp:hover { background: var(--brand-hover); }
.field-msg { margin: 8px 0 0; font-size: 13px; line-height: 20px; color: var(--muted); }
.field-msg.error { color: var(--danger); }
.field-msg.ok { color: var(--success); }
.btn {
  display: inline-flex; align-items: center; justify-content: center; gap: 8px;
  width: 100%; min-height: 48px; border-radius: 12px; padding: 0 24px;
  font-size: 16px; font-weight: 600; cursor: pointer; border: 0;
  transition: background .15s, transform .1s;
}
.btn:active { transform: translateY(1px); }
.btn-primary { background: var(--brand); color: #fff; }
.btn-primary:hover { background: var(--brand-hover); }
.btn-primary:disabled { background: #e2e8f0; color: #64748b; cursor: not-allowed; transform: none; }
.btn-secondary { background: #fff; color: var(--ink); border: 1px solid var(--line); }
.btn-secondary:hover { background: var(--surface); }
.or-line {
  display: flex; align-items: center; gap: 16px;
  margin: 32px 0; color: var(--muted); font-size: 14px;
}
.or-line:before, .or-line:after { content: ""; flex: 1; height: 1px; background: var(--line); }
.google-icon { width: 20px; height: 20px; }
.footer-note { text-align: center; font-size: 14px; color: var(--muted); margin: 0; }
.alert {
  margin: 0 0 16px; padding: 12px; border-radius: 12px; font-size: 13px; line-height: 20px;
}
.alert-error { background: var(--danger-soft); color: var(--danger); }
.alert-ok { background: var(--success-soft); color: var(--success); }
.badge-verified {
  display: inline-flex; align-items: center; gap: 6px;
  border-radius: 8px; padding: 8px 12px;
  background: var(--success-soft); color: var(--success);
  font-size: 14px; font-weight: 600;
}
.badge-trust {
  display: none; align-items: center; gap: 8px;
  margin-top: 8px; border-radius: 10px; padding: 10px 12px;
  background: var(--trust-soft); color: var(--trust);
  font-size: 13px; font-weight: 600; line-height: 18px;
}
.badge-trust.show { display: inline-flex; }
.badge-trust svg { flex-shrink: 0; }
.strength { display: none; align-items: center; gap: 12px; margin-top: 12px; }
.strength.show { display: flex; }
.strength-bars { display: flex; flex: 1; gap: 4px; }
.strength-bars span {
  height: 4px; flex: 1; border-radius: 999px; background: var(--line);
}
.strength-bars span.on.weak { background: var(--danger); }
.strength-bars span.on.fair { background: #94a3b8; }
.strength-bars span.on.strong { background: var(--success); }
.strength-label { font-size: 13px; font-weight: 500; color: var(--muted); }
.strength-label.weak { color: var(--danger); }
.strength-label.fair { color: var(--muted); }
.strength-label.strong { color: var(--success); }
.terms {
  display: flex; align-items: flex-start; gap: 12px;
  min-height: 48px; padding: 8px 0; cursor: pointer;
}
.terms input { width: 20px; height: 20px; margin-top: 2px; accent-color: var(--brand); flex-shrink: 0; }
.terms span { font-size: 14px; line-height: 20px; color: var(--ink); }
.otp-row { display: flex; gap: 8px; margin-top: 12px; }
.otp-row input {
  width: 44px; height: 48px; text-align: center; font-size: 18px; font-weight: 600;
  border: 1px solid var(--line); border-radius: 10px; background: var(--surface);
}
.otp-row input:focus {
  outline: none; border-color: var(--brand); background: #fff;
  box-shadow: 0 0 0 2px rgba(7,103,216,.2);
}
.step-panel[hidden] { display: none !important; }
.username-status { margin-top: 8px; font-size: 13px; line-height: 20px; color: var(--muted); }
.username-status.ok { color: var(--success); }
.username-status.bad { color: var(--danger); }
.chevron {
  position: absolute; right: 14px; top: 50%; transform: translateY(-50%);
  pointer-events: none; color: var(--muted);
}
#recaptcha { margin: 8px 0; }
#sign-note .ok, #sign-note .error {
  margin: 0; padding: 12px; border-radius: 12px; font-size: 13px; line-height: 20px;
}
#sign-note .ok { background: var(--success-soft); color: var(--success); }
#sign-note .error { background: var(--danger-soft); color: var(--danger); }
.pay-link {
  display: block; text-align: center; margin-top: 12px;
  font-size: 14px; font-weight: 600; color: var(--brand);
}
"""

ICON_BACK = """<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M19 12H5"/><path d="m12 19-7-7 7-7"/></svg>"""
ICON_CHECK = """<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M20 6 9 17l-5-5"/></svg>"""
ICON_CHEVRON = """<svg class="chevron" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m6 9 6 6 6-6"/></svg>"""
GOOGLE_SVG = """<svg class="google-icon" width="20" height="20" viewBox="0 0 48 48" aria-hidden="true"><path fill="#FFC107" d="M43.611 20.083H42V20H24v8h11.303c-1.649 4.657-6.08 8-11.303 8-6.627 0-12-5.373-12-12s5.373-12 12-12c3.059 0 5.842 1.154 7.961 3.039l5.657-5.657C34.046 6.053 29.268 4 24 4 12.955 4 4 12.955 4 24s8.955 20 20 20 20-8.955 20-20c0-1.341-.138-2.65-.389-3.917z"/><path fill="#FF3D00" d="M6.306 14.691l6.571 4.819C14.655 15.108 18.961 12 24 12c3.059 0 5.842 1.154 7.961 3.039l5.657-5.657C34.046 6.053 29.268 4 24 4 16.318 4 9.656 8.337 6.306 14.691z"/><path fill="#4CAF50" d="M24 44c5.166 0 9.86-1.977 13.409-5.192l-6.19-5.238C29.211 35.091 26.715 36 24 36c-5.202 0-9.619-3.317-11.283-7.946l-6.522 5.025C9.505 39.556 16.227 44 24 44z"/><path fill="#1976D2" d="M43.611 20.083H42V20H24v8h11.303c-.792 2.237-2.231 4.166-4.087 5.571l.003-.002 6.19 5.238C36.971 39.205 44 34 44 24c0-1.341-.138-2.65-.389-3.917z"/></svg>"""


def _text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def _logo_block() -> str:
    return f"""
<div class="auth-logo">
  <span class="auth-logo-mark"><img src="{LOGO}" alt="" onerror="this.style.display='none'"></span>
  <span class="auth-logo-text">My Laptop Market</span>
</div>"""


def auth_document(
    title: str,
    main: str,
    *,
    back_href: str = "https://mylaptopmarket.com",
    back_label: str = "Back to home",
    step: tuple[int, int] | None = None,
    footer: str = "",
    scripts: str = "",
    status: int = 200,
) -> str:
    step_label = ""
    progress = ""
    if step:
        current, total = step
        pct = int((current / total) * 100)
        step_label = f'<p class="auth-step-label">Step {current} of {total}</p>'
        progress = f"""
<div class="auth-progress">
  <div class="auth-progress-track" role="progressbar" aria-valuemin="1" aria-valuemax="{total}" aria-valuenow="{current}" aria-label="Registration progress">
    <div class="auth-progress-bar" style="width:{pct}%"></div>
  </div>
</div>"""
    footer_html = f'<footer class="auth-footer">{footer}</footer>' if footer else ""
    return f"""<!DOCTYPE html>
<html lang="en"><head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{html.escape(title)}</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
<style>{AUTH_CSS}</style>
</head>
<body class="auth-body">
<div class="auth-shell">
  <div class="auth-card">
    <header class="auth-header">
      <a class="auth-back" href="{html.escape(back_href)}" aria-label="{html.escape(back_label)}">{ICON_BACK}</a>
      {step_label}
    </header>
    {progress}
    <main class="auth-main">{main}</main>
    {footer_html}
  </div>
</div>
{scripts}
</body></html>"""


def floating_field(
    name: str,
    label: str,
    *,
    value: str = "",
    kind: str = "text",
    autocomplete: str = "",
    maxlength: str = "",
    prefix: str = "",
    action: str = "",
    extra_attrs: str = "",
) -> str:
    safe_value = html.escape(_text(value))
    auto = f' autocomplete="{html.escape(autocomplete)}"' if autocomplete else ""
    max_attr = f' maxlength="{html.escape(maxlength)}"' if maxlength else ""
    prefix_html = f'<span class="field-prefix">{prefix}</span>' if prefix else ""
    action_html = f'<div class="field-action">{action}</div>' if action else ""
    return f"""
<div class="field" data-field="{html.escape(name)}">
  <div class="field-box">
    {prefix_html}
    <div class="field-inner">
      <input id="{html.escape(name)}" name="{html.escape(name)}" type="{html.escape(kind)}" placeholder=" " value="{safe_value}"{auto}{max_attr} {extra_attrs}>
      <label for="{html.escape(name)}">{html.escape(label)}</label>
    </div>
    {action_html}
  </div>
  <p class="field-msg" id="{html.escape(name)}-msg" hidden></p>
</div>"""


def login_main(error: str = "", vendor_id: int = 0, success: str = "", pay_html: str = "") -> str:
    alert = ""
    if success:
        alert += f'<div class="alert alert-ok" role="status">{html.escape(success)}</div>'
    if error:
        alert += f'<div class="alert alert-error" role="alert">{html.escape(error)}</div>'
    show_btn = '<button type="button" id="togglePassword" aria-label="Show password">Show</button>'
    return f"""
{_logo_block()}
<div style="margin-top:40px">
  <h1 class="auth-title">Welcome back</h1>
  <p class="auth-sub">Log in to manage your listings and leads.</p>
</div>
{alert}
<form action="loginAuth" method="post" class="auth-form" style="margin-top:32px" novalidate>
  {floating_field("username", "Username, email or mobile", autocomplete="username", extra_attrs='autocapitalize="none" spellcheck="false"')}
  {floating_field("password", "Password", kind="password", autocomplete="current-password", action=show_btn)}
  <div style="display:flex;justify-content:flex-end;margin:-8px 0 20px">
    <a href="forgot-password" style="height:48px;display:inline-flex;align-items:center;padding:0 4px;font-size:14px;font-weight:500">Forgot password?</a>
  </div>
  <button type="submit" class="btn btn-primary">Log in</button>
</form>
<div class="or-line">or</div>
<button type="button" class="btn btn-secondary" id="google-sign-in">{GOOGLE_SVG} Continue with Google</button>
<div id="sign-note" style="margin-top:12px"></div>
{pay_html}
<p class="footer-note" style="margin-top:32px">New vendor? <a href="register" style="font-weight:600">Create account</a></p>
"""


def login_scripts(firebase_js: str) -> str:
    return f"""
<script>
(function() {{
  var btn = document.getElementById("togglePassword");
  var input = document.getElementById("password");
  if (btn && input) {{
    btn.addEventListener("click", function() {{
      var show = input.type === "password";
      input.type = show ? "text" : "password";
      btn.textContent = show ? "Hide" : "Show";
      btn.setAttribute("aria-label", show ? "Hide password" : "Show password");
    }});
  }}
}})();
</script>
{firebase_js}
"""


def forgot_main(email: str = "", error: str = "", success: str = "") -> str:
    alert = ""
    if error:
        alert += f'<div class="alert alert-error" role="alert">{html.escape(error)}</div>'
    if success:
        alert += f'<div class="alert alert-ok" role="status">{html.escape(success)}</div>'
    safe_email = html.escape(email)
    return f"""
{_logo_block()}
<div style="margin-top:40px">
  <h1 class="auth-title">Reset password</h1>
  <p class="auth-sub">We will email a 6-digit OTP.</p>
</div>
{alert}
<form class="auth-form" action="forgot-password" method="post" style="margin-top:32px">
  {floating_field("email", "Email", value=email, kind="email", autocomplete="email")}
  <button type="submit" class="btn btn-primary">Send OTP</button>
</form>
<form class="auth-form" action="reset-password" method="post" style="margin-top:24px">
  <input type="hidden" name="email" value="{safe_email}">
  {floating_field("otp", "6-digit OTP", maxlength="6", autocomplete="one-time-code", extra_attrs='inputmode="numeric"')}
  {floating_field("password", "New password", kind="password", autocomplete="new-password")}
  {floating_field("password_confirmation", "Confirm password", kind="password", autocomplete="new-password")}
  <button type="submit" class="btn btn-primary">Update password</button>
</form>
<p class="footer-note" style="margin-top:24px"><a href="login" style="font-weight:600">Back to login</a></p>
"""


def register_main(error: str = "", values: dict | None = None, success: str = "", otp_enabled: bool = True) -> str:
    values = values or {}
    options = ['<option value=""></option>']
    selected_state = _text(values.get("state"))
    for name in STATES:
        sel = " selected" if selected_state == name else ""
        options.append(f'<option value="{html.escape(name)}"{sel}>{html.escape(name)}</option>')
    state_filled = " select-filled" if selected_state else ""
    alert = ""
    if error:
        alert += f'<div class="alert alert-error" role="alert">{html.escape(error)}</div>'
    if success:
        alert += f'<div class="alert alert-ok" role="status">{html.escape(success)}</div>'

    gst_action = '<button type="button" id="checkGst" class="inline-btn">Check</button>'
    show_pw = '<button type="button" id="togglePassword" aria-label="Show password">Show</button>'

    phone_block = f"""
<div id="phoneBlock">
  {floating_field("mobile", "Mobile number", value=_text(values.get("mobile")), kind="tel", autocomplete="tel", prefix="+91", action='<button type="button" class="send-otp" id="sendMobileOtp">Send OTP</button>' if otp_enabled else "")}
  <div id="recaptcha"></div>
  <div id="mobileOtpWrap" hidden>
    <p class="auth-sub" style="font-size:14px;margin:0 0 8px">Enter the 6-digit SMS code</p>
    <div class="otp-row" id="mobileOtpBoxes"></div>
    <input type="hidden" name="mobile_otp" id="mobile_otp" value="{html.escape(_text(values.get("mobile_otp")))}">
    <p class="field-msg" id="mobileNote"></p>
    <button type="button" class="btn btn-secondary" id="verifyMobileOtp" style="margin-top:12px">Verify mobile</button>
  </div>
  <div id="mobileVerified" class="badge-verified" hidden>{ICON_CHECK} Verified</div>
</div>
"""
    email_block = f"""
<div id="emailBlock" {"hidden" if otp_enabled else ""}>
  {floating_field("email", "Email", value=_text(values.get("email")), kind="email", autocomplete="email", action='<button type="button" class="send-otp" id="sendEmailOtp">Send OTP</button>' if otp_enabled else "")}
  <div id="emailOtpWrap" hidden>
    <p class="auth-sub" style="font-size:14px;margin:0 0 8px">Enter the 6-digit email code</p>
    <div class="otp-row" id="emailOtpBoxes"></div>
    <input type="hidden" name="email_otp" id="email_otp" value="{html.escape(_text(values.get("email_otp")))}">
    <p class="field-msg" id="emailNote"></p>
    <button type="button" class="btn btn-secondary" id="verifyEmailOtp" style="margin-top:12px">Verify email</button>
  </div>
  <div id="emailVerified" class="badge-verified" hidden>{ICON_CHECK} Verified</div>
</div>
"""

    return f"""
{alert}
<form class="vendor-register" id="registerForm" action="registerStore" method="post" novalidate>
<section class="step-panel" data-step="1">
  <p class="auth-trust">{ICON_CHECK} Join 1,200+ verified IT businesses across 85+ cities. Zero commission.</p>
  <h1 class="auth-title">Tell us about your business</h1>
  <p class="auth-sub">Takes about 2 minutes.</p>
  <div style="margin-top:24px">
    {floating_field("shop_name", "Shop or company name", value=_text(values.get("shop_name")), autocomplete="organization")}
    {floating_field("city", "City", value=_text(values.get("city")), autocomplete="address-level2")}
    <div class="field" data-field="state">
      <div class="field-box">
        <div class="field-inner{state_filled}">
          <select id="state" name="state" autocomplete="address-level1">
            {"".join(options)}
          </select>
          <label for="state">State</label>
          {ICON_CHEVRON}
        </div>
      </div>
      <p class="field-msg" id="state-msg" hidden></p>
    </div>
    {floating_field("gstin", "GSTIN (optional)", value=_text(values.get("gstin")), autocomplete="off", maxlength="15", action=gst_action, extra_attrs='style="text-transform:uppercase" spellcheck="false"')}
    <div class="badge-trust" id="gstTrust">{ICON_CHECK} <span>GST verified — builds dealer trust</span></div>
  </div>
</section>

<section class="step-panel" data-step="2" hidden>
  <h1 class="auth-title">Verify your contact</h1>
  <p class="auth-sub">We'll verify your mobile and email to keep the marketplace trusted.</p>
  <div style="margin-top:24px;display:flex;flex-direction:column;gap:24px">
    {phone_block}
    {email_block}
  </div>
  <p class="field-msg error" id="step2Error" hidden>Verify your mobile and email to continue.</p>
</section>

<section class="step-panel" data-step="3" hidden>
  <h1 class="auth-title">Create your login</h1>
  <p class="auth-sub">You'll use this to sign in on any device.</p>
  <div style="margin-top:24px">
    {floating_field("username", "Username", value=_text(values.get("username")), autocomplete="username", extra_attrs='autocapitalize="none" spellcheck="false"')}
    <p class="username-status" id="usernameStatus">We'll suggest a username from your shop name.</p>
    {floating_field("password", "Password", kind="password", autocomplete="new-password", action=show_pw)}
    <div class="strength" id="pwStrength" aria-live="polite">
      <div class="strength-bars" aria-hidden="true"><span></span><span></span><span></span></div>
      <span class="strength-label" id="pwStrengthLabel">Strength: Weak</span>
    </div>
    <label class="terms">
      <input type="checkbox" name="terms" id="terms" value="1">
      <span>I agree to the <a href="https://mylaptopmarket.com/privacy-policy.html" target="_blank" rel="noopener">Terms of Use</a> and <a href="https://mylaptopmarket.com/privacy-policy.html" target="_blank" rel="noopener">Privacy Policy</a>.</span>
    </label>
    <p class="field-msg error" id="termsError" hidden>Accept the terms to create your account.</p>
  </div>
</section>
</form>
"""


def register_footer() -> str:
    return '<button type="button" class="btn btn-primary" id="registerContinue">Continue</button>'


def register_scripts(firebase_config: dict, otp_enabled: bool = True) -> str:
    config_json = json.dumps(firebase_config or {})
    otp_flag = "true" if otp_enabled else "false"
    return f"""
<script>
(function() {{
  var OTP_ON = {otp_flag};
  var config = {config_json};
  var step = 1;
  var phoneVerified = !OTP_ON;
  var emailVerified = !OTP_ON;
  var usernameEdited = false;
  var suggestTimer = null;
  var gstTimer = null;

  var form = document.getElementById("registerForm");
  var continueBtn = document.getElementById("registerContinue");
  var progressBar = document.querySelector(".auth-progress-bar");
  var stepLabel = document.querySelector(".auth-step-label");
  var backBtn = document.querySelector(".auth-back");

  function panel(n) {{ return document.querySelector('.step-panel[data-step="'+n+'"]'); }}
  function showMsg(id, text, isError) {{
    var el = document.getElementById(id);
    if (!el) return;
    if (!text) {{ el.hidden = true; el.textContent = ""; return; }}
    el.hidden = false;
    el.textContent = text;
    el.className = "field-msg" + (isError ? " error" : "");
  }}
  function setStep(n) {{
    step = n;
    for (var i = 1; i <= 3; i++) {{
      var p = panel(i);
      if (p) p.hidden = i !== n;
    }}
    if (progressBar) progressBar.style.width = Math.round((n / 3) * 100) + "%";
    if (stepLabel) stepLabel.textContent = "Step " + n + " of 3";
    if (continueBtn) continueBtn.textContent = n === 3 ? "Create account" : "Continue";
    if (backBtn) {{
      if (n === 1) {{
        backBtn.setAttribute("href", "login");
        backBtn.setAttribute("aria-label", "Back to login");
      }} else {{
        backBtn.removeAttribute("href");
        backBtn.setAttribute("aria-label", "Previous step");
      }}
    }}
    window.scrollTo({{ top: 0, behavior: "smooth" }});
    if (n === 3) ensureUsername();
  }}

  if (backBtn) {{
    backBtn.addEventListener("click", function(e) {{
      if (step > 1) {{
        e.preventDefault();
        setStep(step - 1);
      }}
    }});
  }}

  var stateSelect = document.getElementById("state");
  if (stateSelect) {{
    stateSelect.addEventListener("change", function() {{
      var inner = stateSelect.parentElement;
      if (stateSelect.value) inner.classList.add("select-filled");
      else inner.classList.remove("select-filled");
    }});
  }}

  function val(name) {{
    var el = document.getElementById(name) || form.querySelector('[name="'+name+'"]');
    return el ? String(el.value || "").trim() : "";
  }}

  // GST check
  function runGstCheck() {{
    var gstin = val("gstin").toUpperCase();
    var trust = document.getElementById("gstTrust");
    var input = document.getElementById("gstin");
    if (input) input.value = gstin;
    if (!gstin) {{
      showMsg("gstin-msg", "");
      if (trust) trust.classList.remove("show");
      return;
    }}
    showMsg("gstin-msg", "Checking GST…");
    fetch("gst-check", {{
      method: "POST",
      headers: {{ "Content-Type": "application/json" }},
      credentials: "same-origin",
      body: JSON.stringify({{ gstin: gstin }})
    }}).then(function(r) {{ return r.json(); }}).then(function(data) {{
      if (data && data.verified) {{
        showMsg("gstin-msg", data.message || "GST verified", false);
        var msg = document.getElementById("gstin-msg");
        if (msg) {{ msg.className = "field-msg ok"; msg.hidden = false; }}
        if (trust) trust.classList.add("show");
        if (data.gstin && input) input.value = data.gstin;
      }} else if (data && data.empty) {{
        showMsg("gstin-msg", "");
        if (trust) trust.classList.remove("show");
      }} else {{
        showMsg("gstin-msg", (data && data.message) || "Could not verify GSTIN.", true);
        if (trust) trust.classList.remove("show");
      }}
    }}).catch(function() {{
      showMsg("gstin-msg", "GST check failed. Try again.", true);
      if (trust) trust.classList.remove("show");
    }});
  }}
  var checkGst = document.getElementById("checkGst");
  if (checkGst) checkGst.addEventListener("click", runGstCheck);
  var gstInput = document.getElementById("gstin");
  if (gstInput) {{
    gstInput.addEventListener("input", function() {{
      clearTimeout(gstTimer);
      gstTimer = setTimeout(function() {{
        if (val("gstin").length >= 15) runGstCheck();
      }}, 500);
    }});
  }}

  // Username suggest + availability
  function ensureUsername() {{
    if (usernameEdited && val("username")) {{
      checkUsername(val("username"));
      return;
    }}
    var shop = val("shop_name");
    fetch("username-available?shop=" + encodeURIComponent(shop), {{ credentials: "same-origin" }})
      .then(function(r) {{ return r.json(); }})
      .then(function(data) {{
        var input = document.getElementById("username");
        var status = document.getElementById("usernameStatus");
        var pick = (data && (data.suggestion || data.username)) || "";
        if (input && !usernameEdited) input.value = pick;
        if (status) {{
          if (data && data.available) {{
            status.className = "username-status ok";
            status.textContent = "Suggested from your shop name · available";
          }} else if (data && data.suggestion) {{
            status.className = "username-status ok";
            status.textContent = "Suggested available username: " + data.suggestion;
            if (input && !usernameEdited) input.value = data.suggestion;
          }} else {{
            status.className = "username-status bad";
            status.textContent = (data && data.message) || "Pick a username";
          }}
        }}
      }});
  }}
  function checkUsername(u) {{
    var status = document.getElementById("usernameStatus");
    if (!u) {{
      if (status) {{ status.className = "username-status"; status.textContent = "We'll suggest a username from your shop name."; }}
      return;
    }}
    fetch("username-available?u=" + encodeURIComponent(u), {{ credentials: "same-origin" }})
      .then(function(r) {{ return r.json(); }})
      .then(function(data) {{
        if (!status) return;
        if (data && data.available) {{
          status.className = "username-status ok";
          status.textContent = "Username is available";
        }} else if (data && data.suggestion) {{
          status.className = "username-status bad";
          status.textContent = (data.message || "Taken") + " Click to use " + data.suggestion;
          status.style.cursor = "pointer";
          status.onclick = function() {{
            var input = document.getElementById("username");
            if (input) input.value = data.suggestion;
            usernameEdited = true;
            checkUsername(data.suggestion);
          }};
        }} else {{
          status.className = "username-status bad";
          status.textContent = (data && data.message) || "Username not available";
        }}
      }});
  }}
  var usernameInput = document.getElementById("username");
  if (usernameInput) {{
    usernameInput.addEventListener("input", function() {{
      usernameEdited = true;
      clearTimeout(suggestTimer);
      suggestTimer = setTimeout(function() {{ checkUsername(val("username")); }}, 350);
    }});
  }}

  // Password strength + show/hide
  var pw = document.getElementById("password");
  var toggle = document.getElementById("togglePassword");
  if (toggle && pw) {{
    toggle.addEventListener("click", function() {{
      var show = pw.type === "password";
      pw.type = show ? "text" : "password";
      toggle.textContent = show ? "Hide" : "Show";
    }});
  }}
  if (pw) {{
    pw.addEventListener("input", function() {{
      var v = pw.value;
      var box = document.getElementById("pwStrength");
      var label = document.getElementById("pwStrengthLabel");
      var bars = box ? box.querySelectorAll(".strength-bars span") : [];
      if (!v) {{ if (box) box.classList.remove("show"); return; }}
      if (box) box.classList.add("show");
      var score = 0;
      if (v.length >= 8) score++;
      if (v.length >= 12) score++;
      if (/\\d/.test(v) && /[a-zA-Z]/.test(v)) score++;
      if (/[^a-zA-Z0-9]/.test(v) && /[A-Z]/.test(v)) score++;
      var cls = score <= 1 ? "weak" : score === 2 ? "fair" : "strong";
      var count = score <= 1 ? 1 : score === 2 ? 2 : 3;
      var text = score <= 1 ? "Weak" : score === 2 ? "Fair" : "Strong";
      for (var i = 0; i < bars.length; i++) {{
        bars[i].className = i < count ? ("on " + cls) : "";
      }}
      if (label) {{ label.className = "strength-label " + cls; label.textContent = "Strength: " + text; }}
    }});
  }}

  function buildOtpBoxes(containerId, hiddenId) {{
    var wrap = document.getElementById(containerId);
    if (!wrap || wrap.dataset.ready) return;
    wrap.dataset.ready = "1";
    for (var i = 0; i < 6; i++) {{
      var input = document.createElement("input");
      input.type = "text";
      input.inputMode = "numeric";
      input.maxLength = 1;
      input.dataset.idx = String(i);
      input.addEventListener("input", function(e) {{
        var el = e.target;
        el.value = el.value.replace(/\\D/g, "").slice(0, 1);
        var idx = Number(el.dataset.idx);
        if (el.value && idx < 5) {{
          var next = wrap.querySelector('input[data-idx="'+(idx+1)+'"]');
          if (next) next.focus();
        }}
        syncOtp(wrap, hiddenId);
      }});
      input.addEventListener("keydown", function(e) {{
        var el = e.target;
        var idx = Number(el.dataset.idx);
        if (e.key === "Backspace" && !el.value && idx > 0) {{
          var prev = wrap.querySelector('input[data-idx="'+(idx-1)+'"]');
          if (prev) prev.focus();
        }}
      }});
      wrap.appendChild(input);
    }}
  }}
  function syncOtp(wrap, hiddenId) {{
    var code = "";
    wrap.querySelectorAll("input").forEach(function(el) {{ code += el.value || ""; }});
    var hidden = document.getElementById(hiddenId);
    if (hidden) hidden.value = code;
  }}

  // Mobile OTP via Firebase
  var confirmation = null;
  var verifier = null;
  function sayMobile(text, ok) {{
    var note = document.getElementById("mobileNote");
    if (!note) return;
    note.hidden = !text;
    note.textContent = text || "";
    note.className = "field-msg" + (ok === true ? " ok" : ok === false ? " error" : "");
  }}
  var sendMobile = document.getElementById("sendMobileOtp");
  var verifyMobile = document.getElementById("verifyMobileOtp");
  if (OTP_ON && config.apiKey && sendMobile) {{
    if (!firebase.apps || !firebase.apps.length) firebase.initializeApp(config);
    sendMobile.addEventListener("click", function() {{
      var mobile = val("mobile").replace(/\\D/g, "");
      if (!/^[6-9]\\d{{9}}$/.test(mobile)) {{
        showMsg("mobile-msg", "Enter a valid 10-digit mobile number", true);
        return;
      }}
      showMsg("mobile-msg", "");
      sayMobile("Sending…");
      var wrap = document.getElementById("mobileOtpWrap");
      if (wrap) wrap.hidden = false;
      buildOtpBoxes("mobileOtpBoxes", "mobile_otp");
      if (!verifier) verifier = new firebase.auth.RecaptchaVerifier("recaptcha", {{ size: "normal" }});
      firebase.auth().signInWithPhoneNumber("+91" + mobile, verifier).then(function(result) {{
        confirmation = result;
        sayMobile("OTP sent to your mobile.", true);
      }}).catch(function(error) {{
        var code = error && error.code ? String(error.code) : "";
        sayMobile("Could not send SMS" + (code ? " (" + code.replace("auth/","") + ")" : "") + ".", false);
        if (verifier && verifier.clear) verifier.clear();
        verifier = null;
      }});
    }});
  }}
  if (OTP_ON && verifyMobile) {{
    verifyMobile.addEventListener("click", function() {{
      var mobile = val("mobile").replace(/\\D/g, "");
      var code = val("mobile_otp");
      if (!confirmation) {{ sayMobile("Send an OTP first.", false); return; }}
      if (code.length !== 6) {{ sayMobile("Enter the 6-digit code.", false); return; }}
      sayMobile("Checking…");
      confirmation.confirm(code).then(function(result) {{
        return result.user.getIdToken();
      }}).then(function(token) {{
        return fetch("mobile-verify", {{
          method: "POST",
          headers: {{ "Content-Type": "application/json" }},
          credentials: "same-origin",
          body: JSON.stringify({{ mobile: mobile, firebase_token: token }})
        }});
      }}).then(function(r) {{ return r.json(); }}).then(function(data) {{
        try {{ firebase.auth().signOut(); }} catch (e) {{}}
        if (data && data.success) {{
          phoneVerified = true;
          sayMobile(data.message || "Mobile verified.", true);
          var badge = document.getElementById("mobileVerified");
          var wrap = document.getElementById("mobileOtpWrap");
          var send = document.getElementById("sendMobileOtp");
          if (badge) badge.hidden = false;
          if (wrap) wrap.hidden = true;
          if (send) send.style.display = "none";
          var emailBlock = document.getElementById("emailBlock");
          if (emailBlock) emailBlock.hidden = false;
        }} else {{
          sayMobile((data && data.message) || "Could not verify mobile.", false);
        }}
      }}).catch(function() {{ sayMobile("Incorrect OTP.", false); }});
    }});
  }}

  // Email OTP via JSON API
  function sayEmail(text, ok) {{
    var note = document.getElementById("emailNote");
    if (!note) return;
    note.hidden = !text;
    note.textContent = text || "";
    note.className = "field-msg" + (ok === true ? " ok" : ok === false ? " error" : "");
  }}
  var sendEmail = document.getElementById("sendEmailOtp");
  var verifyEmail = document.getElementById("verifyEmailOtp");
  if (OTP_ON && sendEmail) {{
    sendEmail.addEventListener("click", function() {{
      var email = val("email");
      if (!/^[^\\s@]+@[^\\s@]+\\.[^\\s@]{{2,}}$/.test(email)) {{
        showMsg("email-msg", "Enter a valid email address", true);
        return;
      }}
      showMsg("email-msg", "");
      sayEmail("Sending…");
      var wrap = document.getElementById("emailOtpWrap");
      if (wrap) wrap.hidden = false;
      buildOtpBoxes("emailOtpBoxes", "email_otp");
      fetch("email-otp", {{
        method: "POST",
        headers: {{ "Content-Type": "application/json" }},
        credentials: "same-origin",
        body: JSON.stringify({{ email: email }})
      }}).then(function(r) {{ return r.json(); }}).then(function(data) {{
        if (data && (data.success || data.ok)) {{
          sayEmail(data.message || "OTP sent to your email.", true);
        }} else {{
          sayEmail((data && data.message) || "Could not send email OTP.", false);
        }}
      }}).catch(function() {{ sayEmail("Could not send email OTP.", false); }});
    }});
  }}
  if (OTP_ON && verifyEmail) {{
    verifyEmail.addEventListener("click", function() {{
      var email = val("email");
      var code = val("email_otp");
      if (code.length !== 6) {{ sayEmail("Enter the 6-digit code.", false); return; }}
      sayEmail("Checking…");
      fetch("email-verify", {{
        method: "POST",
        headers: {{ "Content-Type": "application/json" }},
        credentials: "same-origin",
        body: JSON.stringify({{ email: email, email_otp: code }})
      }}).then(function(r) {{ return r.json(); }}).then(function(data) {{
        if (data && (data.success || data.ok)) {{
          emailVerified = true;
          sayEmail(data.message || "Email verified.", true);
          var badge = document.getElementById("emailVerified");
          var wrap = document.getElementById("emailOtpWrap");
          var send = document.getElementById("sendEmailOtp");
          if (badge) badge.hidden = false;
          if (wrap) wrap.hidden = true;
          if (send) send.style.display = "none";
        }} else {{
          sayEmail((data && data.message) || "Could not verify email.", false);
        }}
      }}).catch(function() {{ sayEmail("Could not verify email.", false); }});
    }});
  }}

  function validateStep1() {{
    var ok = true;
    if (val("shop_name").length < 2) {{ showMsg("shop_name-msg", "Enter your shop or company name", true); ok = false; }}
    else showMsg("shop_name-msg", "");
    if (val("city").length < 2) {{ showMsg("city-msg", "Enter your city", true); ok = false; }}
    else showMsg("city-msg", "");
    if (!val("state")) {{ showMsg("state-msg", "Select your state", true); ok = false; }}
    else showMsg("state-msg", "");
    var gst = val("gstin");
    if (gst && gst.length !== 15) {{ showMsg("gstin-msg", "GSTIN must be 15 characters, or leave blank", true); ok = false; }}
    return ok;
  }}
  function validateStep2() {{
    var err = document.getElementById("step2Error");
    if (!OTP_ON) {{
      if (!/^[6-9]\\d{{9}}$/.test(val("mobile").replace(/\\D/g, ""))) {{
        showMsg("mobile-msg", "Enter a valid 10-digit mobile number", true);
        return false;
      }}
      if (!/^[^\\s@]+@[^\\s@]+\\.[^\\s@]{{2,}}$/.test(val("email"))) {{
        showMsg("email-msg", "Enter a valid email address", true);
        return false;
      }}
      if (err) err.hidden = true;
      return true;
    }}
    if (!(phoneVerified && emailVerified)) {{
      if (err) err.hidden = false;
      return false;
    }}
    if (err) err.hidden = true;
    return true;
  }}
  function validateStep3() {{
    var ok = true;
    var u = val("username");
    if (!/^[a-zA-Z0-9._]{{4,20}}$/.test(u)) {{
      showMsg("username-msg", "Use 4–20 letters, numbers, dots or underscores", true);
      ok = false;
    }} else showMsg("username-msg", "");
    if (val("password").length < 6) {{
      showMsg("password-msg", "Use at least 6 characters", true);
      ok = false;
    }} else showMsg("password-msg", "");
    var terms = document.getElementById("terms");
    var termsError = document.getElementById("termsError");
    if (!terms || !terms.checked) {{
      if (termsError) termsError.hidden = false;
      ok = false;
    }} else if (termsError) termsError.hidden = true;
    return ok;
  }}

  if (continueBtn) {{
    continueBtn.addEventListener("click", function() {{
      if (step === 1) {{
        if (!validateStep1()) return;
        setStep(2);
        return;
      }}
      if (step === 2) {{
        if (!validateStep2()) return;
        setStep(3);
        return;
      }}
      if (!validateStep3()) return;
      continueBtn.disabled = true;
      continueBtn.textContent = "Creating…";
      form.submit();
    }});
  }}

  // If OTP off, show email on step 2 immediately
  if (!OTP_ON) {{
    var emailBlock = document.getElementById("emailBlock");
    if (emailBlock) emailBlock.hidden = false;
  }}

  // After a failed create, jump back to login step if username/password were posted
  if (val("username") || document.querySelector(".alert-error")) {{
    if (val("username") && val("shop_name") && val("mobile") && val("email")) {{
      phoneVerified = true;
      emailVerified = true;
      var mb = document.getElementById("mobileVerified");
      var eb = document.getElementById("emailVerified");
      var emailBlock = document.getElementById("emailBlock");
      if (mb) mb.hidden = false;
      if (eb) eb.hidden = false;
      if (emailBlock) emailBlock.hidden = false;
      setStep(3);
    }} else if (val("shop_name") && val("mobile")) {{
      setStep(2);
      var emailBlock2 = document.getElementById("emailBlock");
      if (emailBlock2 && phoneVerified) emailBlock2.hidden = false;
    }}
  }}
}})();
</script>
"""


def mobile_firebase_scripts(config: dict) -> str:
    payload = json.dumps(config or {})
    return f"""
<script src="https://www.gstatic.com/firebasejs/10.14.1/firebase-app-compat.js"></script>
<script src="https://www.gstatic.com/firebasejs/10.14.1/firebase-auth-compat.js"></script>
<script>window.__MLM_FIREBASE__ = {payload};</script>
"""
