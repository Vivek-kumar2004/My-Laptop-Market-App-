import json
import re

import functions_framework
from firebase_admin import initialize_app
from flask import Request, Response

from accounts import delete_account, profile_update, register, shop_trust
from admin_web import dashboard_page as admin_dashboard_page
from admin_web import login_auth as admin_login_auth
from admin_web import login_page as admin_login_page
from admin_web import logout as admin_logout
from admin_web import vendor_activate as admin_vendor_activate
from admin_web import vendor_deactivate as admin_vendor_deactivate
from admin_web import vendor_delete as admin_vendor_delete
from admin_web import inquiry_delete_page as admin_inquiry_delete
from admin_web import inquiry_detail_page as admin_inquiry_detail
from admin_web import chat_list_page as admin_chat_list
from admin_web import chat_thread_page as admin_chat_thread
from admin_web import inquiry_list_page as admin_inquiry_list
from admin_web import invoice_list_page as admin_invoice_list
from admin_web import invoice_page as admin_invoice_page
from admin_web import vendor_add_page as admin_vendor_add
from admin_web import vendor_edit_page as admin_vendor_edit
from admin_web import vendor_list_page as admin_vendor_list
from admin_web import vendor_store_page as admin_vendor_store
from admin_web import vendor_update_page as admin_vendor_update
from admin_api import (
    activate,
    admin_delete_product,
    save_announcement,
    warn_vendor,
    admin_products,
    current_admin,
    create_inquiry,
    dashboard,
    deactivate,
    delete_vendor,
    inquiries,
    inquiry_delete,
    inquiry_show,
    invoice_html,
    invoice_show,
    invoices,
    login as admin_login,
    requirement_close,
    requirement_show,
    requirements,
    vendor_show,
    vendor_store,
    vendor_update,
    vendors,
)
from auth import acknowledge_warning, consume_app_handoff, issue_app_handoff, login, login_with_firebase, vendor_status
from catalog import announcement_text, home_showcase, list_products, product_categories, public_product_list
from market import list_market, market_listing
from messages import admin_thread, admin_threads, chat, contacts, conversations, send_message
from retail import categories as retail_categories
from retail import inquiry as retail_inquiry
from retail import product as retail_product
from retail import products as retail_products
from requirements_api import close as close_requirement
from requirements_api import create as create_requirement
from requirements_api import delete as delete_requirement
from requirements_api import feed as requirement_feed
from requirements_api import mine as my_requirements
from push_apns import gst_alert, push_test
from push_tokens import delete_admin as delete_admin_push
from push_tokens import delete_vendor as delete_vendor_push
from push_tokens import save_admin as save_admin_push
from push_tokens import save_vendor as save_vendor_push
from subscription import app_success, checkout, create_order, verify as verify_payment
from web_session import acknowledge as vendor_acknowledge
from web_session import auth_status, dashboard as vendor_dashboard
from web_session import forgot_page as vendor_forgot_page
from web_session import forgot_send as vendor_forgot_send
from web_session import attach_vendor_cookie
from web_session import google_auth_page as vendor_google_auth_page
from web_session import login_auth as vendor_login_auth
from web_session import login_page as vendor_login_page
from web_session import logout as vendor_logout
from web_session import register_page as vendor_register_page
from web_session import phone_auth_page as vendor_phone_auth_page
from web_session import register_send_otp as vendor_register_send_otp
from web_session import register_verify_otp as vendor_register_verify_otp
from web_session import register_send_mobile_otp as vendor_register_send_mobile_otp
from web_session import register_verify_mobile_otp as vendor_register_verify_mobile_otp
from web_session import reset_store as vendor_reset_store
from web_session import register_store as vendor_register_store
from vendor_invoices import invoice_list as vendor_invoice_list
from vendor_invoices import invoice_page as vendor_invoice_page
from vendor_messages import inbox as vendor_messages
from vendor_messages import poll as vendor_message_poll
from vendor_requirements import board as vendor_requirements
from vendor_search import search_page as vendor_search_page
from vendor_requirements import close_post as vendor_requirement_close
from vendor_requirements import create_page as vendor_requirement_create
from vendor_requirements import remove_post as vendor_requirement_delete
from vendor_requirements import store as vendor_requirement_store
from vendor_messages import send as vendor_message_send
from vendor_profile import profile_page as vendor_profile_page
from vendor_profile import profile_store as vendor_profile_store
from vendor_profile import verification_page as vendor_verification_page
from vendor_profile import verification_store as vendor_verification_store
from vendor_products import activate as vendor_product_activate
from vendor_products import add_form as vendor_product_add
from vendor_products import deactivate as vendor_product_deactivate
from vendor_products import delete_image as vendor_product_delete_image
from vendor_products import edit_form as vendor_product_edit
from vendor_products import product_list as vendor_product_list
from vendor_products import remove as vendor_product_remove
from vendor_products import stock_sheet_page as vendor_stock_sheet
from vendor_products import save as vendor_product_save
from vendor_products import store as vendor_product_store
from web_session import vendor_session
from password_reset import forgot_password, reset_password
from email_verify import confirm_email_otp, send_email_otp
from phone_verify import confirm_mobile_otp
from products_write import (
    add_product,
    delete_product,
    delete_stock_sheet,
    read_product_image,
    read_stock_sheet,
    read_stored_image,
    update_product,
    upload_stock_sheet,
)

initialize_app()


SITE_ORIGINS = {
    "https://mylaptopmarket.com",
    "https://www.mylaptopmarket.com",
    "https://mylaptopmarket-app.firebaseapp.com",
    "https://mylaptopmarket-app.web.app",
}


def _cors(response: Response, req: Request) -> Response:
    origin = (req.headers.get("Origin") or "").rstrip("/")
    if origin in SITE_ORIGINS:
        response.headers["Access-Control-Allow-Origin"] = origin
        response.headers["Access-Control-Allow-Credentials"] = "true"
        response.headers["Vary"] = "Origin"
    else:
        response.headers["Access-Control-Allow-Origin"] = "*"
    return response


def _firebase_sign_in_result(result: dict, handoff: str) -> Response:
    vendor = result.get("vendor") or {}
    vendor_id = int(vendor.get("id") or result.get("vendor_id") or 0)
    if handoff == "app" and vendor_id > 0:
        code = issue_app_handoff(vendor_id)
        return _json({"redirect": "mylaptopmarket://auth?code=" + code})
    if result.get("success"):
        response = _json({"success": True, "redirect": "dashboard", "vendor": vendor})
        return attach_vendor_cookie(response, vendor)
    return _json(result)


def _firebase_sign_in(req: Request) -> Response:
    body = req.get_json(silent=True) or {}
    handoff = body.get("handoff") if body.get("handoff") == "app" else ""
    return _firebase_sign_in_result(login_with_firebase(body.get("id_token")), handoff)


def _json(body: dict, status: int = 200) -> Response:
    response = Response(json.dumps(body, default=str), status=status, mimetype="application/json")
    try:
        from flask import request as current
        return _cors(response, current)
    except RuntimeError:
        response.headers["Access-Control-Allow-Origin"] = "*"
        return response


def _path(req: Request) -> str:
    path = (req.path or "/").split("?", 1)[0]
    if path.startswith("/mylaptop/"):
        path = path[len("/mylaptop") :]
    host = (req.host or "").split(":")[0].lower()
    if host in {"mylaptopmarket.com", "www.mylaptopmarket.com"} and path.startswith("/api/"):
        path = path[len("/api") :]
    if not path.startswith("/"):
        path = "/" + path
    return path.rstrip("/") or "/"


@functions_framework.http
def api(req: Request) -> Response:
    if req.method == "OPTIONS":
        response = Response("", status=204)
        response.headers["Access-Control-Allow-Methods"] = "GET,POST,OPTIONS"
        response.headers["Access-Control-Allow-Headers"] = "Content-Type, Authorization, X-Admin-Token"
        return _cors(response, req)

    path = _path(req)
    if req.method == "GET" and path == "/subscription/checkout":
        return checkout(req)
    if req.method == "POST" and path == "/subscription/create_order":
        payload, status = create_order(req)
        return _json(payload, status)
    if req.method == "POST" and path == "/subscription/verify":
        return verify_payment(req)
    if req.method == "GET" and path == "/subscription/app-success":
        return app_success(req)

    if req.method == "GET" and path == "/admin/login":
        return admin_login_page()
    if req.method == "POST" and path == "/admin/loginAuth":
        return admin_login_auth(req)
    if req.method == "GET" and path == "/admin/dashboard":
        return admin_dashboard_page(req)
    if req.method == "GET" and path == "/admin/logout":
        return admin_logout(req)
    if req.method == "GET" and path == "/admin/vendor_list":
        return admin_vendor_list(req)
    admin_activate = re.fullmatch(r"/admin/vendor_activate/(\d+)", path)
    if req.method == "GET" and admin_activate:
        return admin_vendor_activate(req, int(admin_activate.group(1)))
    admin_deactivate = re.fullmatch(r"/admin/vendor_deactivate/(\d+)", path)
    if req.method == "GET" and admin_deactivate:
        return admin_vendor_deactivate(req, int(admin_deactivate.group(1)))
    admin_vendor_delete_match = re.fullmatch(r"/admin/vendor_delete/(\d+)", path)
    if req.method == "POST" and admin_vendor_delete_match:
        return admin_vendor_delete(req, int(admin_vendor_delete_match.group(1)))
    if req.method == "GET" and path == "/admin/add_vendor":
        return admin_vendor_add(req)
    if req.method == "POST" and path == "/admin/vendor_store":
        return admin_vendor_store(req)
    admin_vendor_edit_match = re.fullmatch(r"/admin/vendor_edit/(\d+)", path)
    if req.method == "GET" and admin_vendor_edit_match:
        return admin_vendor_edit(req, int(admin_vendor_edit_match.group(1)))
    admin_vendor_update_match = re.fullmatch(r"/admin/vendor_update/(\d+)", path)
    if req.method == "POST" and admin_vendor_update_match:
        return admin_vendor_update(req, int(admin_vendor_update_match.group(1)))
    if req.method == "GET" and path == "/admin/invoices":
        return admin_invoice_list(req)
    admin_invoice_match = re.fullmatch(r"/admin/invoice/(\d+)", path)
    if req.method == "GET" and admin_invoice_match:
        return admin_invoice_page(req, int(admin_invoice_match.group(1)))
    if req.method == "GET" and path == "/admin/chats":
        return admin_chat_list(req)
    admin_chat_match = re.fullmatch(r"/admin/chats/(\d+)/(\d+)", path)
    if req.method == "GET" and admin_chat_match:
        return admin_chat_thread(req, int(admin_chat_match.group(1)), int(admin_chat_match.group(2)))
    if req.method == "GET" and path == "/admin/inquiries":
        return admin_inquiry_list(req)
    admin_inquiry_match = re.fullmatch(r"/admin/inquiry/(\d+)", path)
    if req.method == "GET" and admin_inquiry_match:
        return admin_inquiry_detail(req, int(admin_inquiry_match.group(1)))
    admin_inquiry_delete_match = re.fullmatch(r"/admin/inquiry/(\d+)/delete", path)
    if req.method == "POST" and admin_inquiry_delete_match:
        return admin_inquiry_delete(req, int(admin_inquiry_delete_match.group(1)))
    if req.method == "GET" and path == "/vendor/register":
        return vendor_register_page()
    if req.method == "GET" and path == "/vendor/phone-auth":
        return vendor_phone_auth_page(req.args.get("mobile") or "")
    if req.method == "POST" and path == "/vendor/registerStore":
        return vendor_register_store(req)
    if req.method == "POST" and path == "/vendor/email-otp":
        return vendor_register_send_otp(req)
    if req.method == "POST" and path == "/vendor/email-verify":
        return vendor_register_verify_otp(req)
    if req.method == "POST" and path == "/vendor/mobile-otp":
        return vendor_register_send_mobile_otp(req)
    if req.method == "POST" and path == "/vendor/mobile-verify":
        return vendor_register_verify_mobile_otp(req)
    if req.method == "GET" and path == "/vendor/forgot-password":
        return vendor_forgot_page(req.args.get("email") or "")
    if req.method == "POST" and path == "/vendor/forgot-password":
        return vendor_forgot_send(req)
    if req.method == "POST" and path == "/vendor/reset-password":
        return vendor_reset_store(req)
    if req.method == "GET" and path == "/vendor/login":
        success = "Password updated. You can log in now." if req.args.get("reset") == "1" else ""
        return vendor_login_page(success=success)
    if req.method == "GET" and path == "/vendor/google-auth":
        return vendor_google_auth_page()
    if req.method == "POST" and path == "/vendor/loginAuth":
        return vendor_login_auth(req)
    if req.method == "POST" and path in {"/vendor/firebase-login", "/api/vendor/firebase-login"}:
        return _firebase_sign_in(req)
    if req.method == "POST" and path in {"/vendor/firebase-code", "/api/vendor/firebase-code"}:
        body = req.get_json(silent=True) or {}
        return _json(consume_app_handoff(body.get("code")))
    if req.method == "GET" and path == "/vendor/dashboard":
        return vendor_dashboard(req)
    if req.method == "GET" and path == "/vendor/profile":
        return vendor_profile_page(req)
    if req.method == "POST" and path == "/vendor/profile":
        return vendor_profile_store(req)
    if req.method == "GET" and path == "/vendor/seller-verification":
        return vendor_verification_page(req)
    if req.method == "POST" and path == "/vendor/seller-verification":
        return vendor_verification_store(req)
    if req.method == "GET" and path == "/vendor/search_products":
        return vendor_search_page(req)
    if req.method == "GET" and path == "/vendor/requirements":
        return vendor_requirements(req)
    if req.method == "GET" and path == "/vendor/requirements/create":
        return vendor_requirement_create(req)
    if req.method == "POST" and path == "/vendor/requirements":
        return vendor_requirement_store(req)
    requirement_close_page = re.fullmatch(r"/vendor/requirements/(\d+)/close", path)
    if req.method == "POST" and requirement_close_page:
        return vendor_requirement_close(req, int(requirement_close_page.group(1)))
    requirement_delete_page = re.fullmatch(r"/vendor/requirements/(\d+)/delete", path)
    if req.method == "POST" and requirement_delete_page:
        return vendor_requirement_delete(req, int(requirement_delete_page.group(1)))
    if req.method == "GET" and path == "/vendor/invoices":
        return vendor_invoice_list(req)
    invoice_view = re.fullmatch(r"/vendor/invoice/(\d+)", path)
    if req.method == "GET" and invoice_view:
        return vendor_invoice_page(req, int(invoice_view.group(1)))
    if req.method == "GET" and path == "/vendor/messages":
        return vendor_messages(req)
    message_poll = re.fullmatch(r"/vendor/messages/poll/(\d+)", path)
    if req.method == "GET" and message_poll:
        return vendor_message_poll(req, int(message_poll.group(1)))
    if req.method == "POST" and path == "/vendor/messages/send":
        return vendor_message_send(req)
    if req.method == "GET" and path == "/vendor/product_list":
        return vendor_product_list(req)
    stock_page = re.fullmatch(r"/vendor/stock-sheet/(\d+)", path)
    if req.method == "GET" and stock_page:
        return vendor_stock_sheet(req, int(stock_page.group(1)))
    if req.method == "GET" and path == "/vendor/add_product":
        return vendor_product_add(req)
    if req.method == "POST" and path == "/vendor/product_store":
        return vendor_product_store(req)
    product_edit = re.fullmatch(r"/vendor/product_edit/(\d+)", path)
    if req.method == "GET" and product_edit:
        return vendor_product_edit(req, int(product_edit.group(1)))
    product_save = re.fullmatch(r"/vendor/product_update/(\d+)", path)
    if req.method == "POST" and product_save:
        return vendor_product_save(req, int(product_save.group(1)))
    image_gone = re.fullmatch(r"/vendor/product_image_delete/(\d+)/([A-Za-z0-9._-]+)", path)
    if req.method == "GET" and image_gone:
        return vendor_product_delete_image(req, int(image_gone.group(1)), image_gone.group(2))
    product_on = re.fullmatch(r"/vendor/product_activate/(\d+)", path)
    if req.method == "GET" and product_on:
        return vendor_product_activate(req, int(product_on.group(1)))
    product_off = re.fullmatch(r"/vendor/product_deactivate/(\d+)", path)
    if req.method == "GET" and product_off:
        return vendor_product_deactivate(req, int(product_off.group(1)))
    product_gone = re.fullmatch(r"/vendor/product_delete/(\d+)", path)
    if req.method == "GET" and product_gone:
        return vendor_product_remove(req, int(product_gone.group(1)))
    if req.method == "POST" and path == "/vendor/warning/ack":
        return vendor_acknowledge(req)
    if req.method == "GET" and path == "/vendor/logout":
        return vendor_logout(req)

    if req.method == "GET" and path in {"/api/web/products", "/api/view_products"}:
        category = (req.args.get("category") or "").strip()
        vendor_id = (req.args.get("vendor_id") or "").strip()
        search = (req.args.get("search") or "").strip()
        for_web = path == "/api/web/products"
        session = vendor_session(req) if for_web else None
        products, vendor_cards = list_products(
            category,
            vendor_id,
            search,
            include_vendor=session is not None if for_web else True,
            for_web=for_web,
        )
        if path == "/api/web/products":
            return _json(
                {
                    "status": True,
                    "message": "Product list fetched successfully",
                    "authenticated": session is not None,
                    "vendor": auth_status(req)["vendor"],
                    "vendors": vendor_cards,
                    "data": products,
                }
            )
        return _json(
            {
                "status": True,
                "success": True,
                "message": "Product list fetched successfully",
                "data": products,
            }
        )

    if req.method == "GET" and path == "/api/searchProducts":
        search = (req.args.get("search") or "").strip()
        products, _vendors = list_products(search=search, include_vendor=True)
        return _json(
            {
                "status": True,
                "success": True,
                "message": "Product list fetched successfully",
                "data": products,
            }
        )

    vendor_match = re.fullmatch(r"/api/vendor_products/(\d+)", path)
    if req.method == "GET" and vendor_match:
        products, _vendors = list_products(vendor_id=vendor_match.group(1), include_vendor=True, only_active=False)
        return _json(
            {
                "status": True,
                "success": True,
                "message": "Vendor products fetched successfully",
                "data": products,
            }
        )

    if req.method == "GET" and path == "/api/categories":
        return _json(product_categories())

    if req.method == "GET" and path == "/api/market-listings":
        return _json(list_market(req.args.get("category") or "", req.args.get("search") or ""))

    market_one = re.fullmatch(r"/api/market-listings/(\d+)", path)
    if req.method == "GET" and market_one:
        payload, status = market_listing(int(market_one.group(1)))
        return _json(payload, status)

    if req.method == "GET" and path == "/api/public/product_list":
        return _json(public_product_list(req.args.get("category") or "", req.args.get("search") or ""))

    if req.method == "GET" and path == "/api/web/home-showcase":
        return _json(home_showcase())

    if req.method == "GET" and path == "/api/web/auth-status":
        return _json(auth_status(req))

    if req.method == "GET" and path == "/api/announcement":
        return _json({"success": True, "data": {"text": announcement_text()}})

    if req.method == "GET" and path == "/api/requirements/mine":
        payload, status = my_requirements(int(req.args.get("vendor_id") or 0))
        return _json(payload, status)

    if req.method == "GET" and path == "/api/requirements":
        vendor_raw = (req.args.get("vendor_id") or "").strip()
        return _json(requirement_feed(int(vendor_raw) if vendor_raw.isdigit() else 0))

    if req.method == "POST" and path == "/api/requirements":
        payload, status = create_requirement(req)
        return _json(payload, status)

    requirement_close_public = re.fullmatch(r"/api/requirements/(\d+)/close", path)
    if req.method == "POST" and requirement_close_public:
        payload, status = close_requirement(req, int(requirement_close_public.group(1)))
        return _json(payload, status)

    requirement_delete_public = re.fullmatch(r"/api/requirements/(\d+)/delete", path)
    if req.method == "POST" and requirement_delete_public:
        payload, status = delete_requirement(req, int(requirement_delete_public.group(1)))
        return _json(payload, status)

    if req.method == "GET" and path == "/api/retail/products":
        return _json(retail_products(req.args.get("search") or "", req.args.get("category") or ""))

    retail_one = re.fullmatch(r"/api/retail/products/(\d+)", path)
    if req.method == "GET" and retail_one:
        payload, status = retail_product(int(retail_one.group(1)))
        return _json(payload, status)

    if req.method == "GET" and path == "/api/retail/categories":
        return _json(retail_categories())

    if req.method == "POST" and path == "/api/retail/inquiry":
        payload, status = retail_inquiry(req)
        return _json(payload, status)

    if req.method == "POST" and path == "/api/contact-inquiry":
        payload, status = create_inquiry(req)
        return _json(payload, status)

    if req.method == "POST" and path == "/api/vendor/forgot_password":
        body = req.get_json(silent=True) or {}
        payload, status = forgot_password(body)
        return _json(payload, status)

    if req.method == "POST" and path == "/api/vendor/reset_password":
        body = req.get_json(silent=True) or {}
        payload, status = reset_password(body)
        return _json(payload, status)

    if req.method == "POST" and path == "/api/vendor/login":
        body = req.get_json(silent=True) or {}
        return _json(login(body.get("username"), body.get("password")))

    if req.method == "POST" and path == "/api/vendor/email_otp":
        body = req.get_json(silent=True) or {}
        payload, status = send_email_otp(body)
        return _json(payload, status)

    if req.method == "POST" and path == "/api/vendor/email_verify":
        body = req.get_json(silent=True) or {}
        payload, status = confirm_email_otp(body)
        return _json(payload, status)

    if req.method == "POST" and path == "/api/vendor/mobile_verify":
        body = req.get_json(silent=True) or {}
        payload, status = confirm_mobile_otp(body)
        return _json(payload, status)

    if req.method == "POST" and path == "/api/vendor/register":
        return _json(register(req.get_json(silent=True) or {}))

    if req.method == "POST" and path == "/api/vendor/profile":
        return _json(profile_update(req))

    if req.method == "POST" and path == "/api/vendor/shop_trust":
        return _json(shop_trust(req))

    if req.method == "POST" and path == "/api/vendor/delete_account":
        body, status = delete_account(req.get_json(silent=True) or {})
        return _json(body, status)

    if req.method == "POST" and path == "/api/vendor/push-token":
        payload, status = save_vendor_push(req)
        return _json(payload, status)

    if req.method == "POST" and path == "/api/vendor/push-token/delete":
        payload, status = delete_vendor_push(req)
        return _json(payload, status)

    if req.method == "POST" and path == "/api/vendor/warning/ack":
        body = req.get_json(silent=True) or {}
        raw_vendor = body.get("vendor_id") or 0
        try:
            vendor_id = int(raw_vendor)
        except (TypeError, ValueError):
            vendor_id = 0
        payload, status = acknowledge_warning(vendor_id)
        return _json(payload, status)

    if req.method == "POST" and path == "/api/admin/login":
        body = req.get_json(silent=True) or {}
        payload, status = admin_login(body.get("username"), body.get("password"))
        return _json(payload, status)

    if path.startswith("/api/admin/") and current_admin(req) is None:
        return _json({"success": False, "message": "Admin login required."}, 401)

    if req.method == "GET" and path == "/api/admin/dashboard":
        return _json(dashboard())

    if req.method == "GET" and path == "/api/admin/vendors":
        return _json(vendors(req.args.get("filter") or "all"))

    if req.method == "POST" and path == "/api/admin/vendors":
        payload, status = vendor_store(req)
        return _json(payload, status)

    admin_vendor = re.fullmatch(r"/api/admin/vendors/(\d+)", path)
    if req.method == "GET" and admin_vendor:
        payload, status = vendor_show(int(admin_vendor.group(1)))
        return _json(payload, status)

    if req.method == "POST" and admin_vendor:
        payload, status = vendor_update(req, int(admin_vendor.group(1)))
        return _json(payload, status)

    admin_activate = re.fullmatch(r"/api/admin/vendors/(\d+)/activate", path)
    if req.method == "POST" and admin_activate:
        payload, status = activate(int(admin_activate.group(1)))
        return _json(payload, status)

    admin_deactivate = re.fullmatch(r"/api/admin/vendors/(\d+)/deactivate", path)
    if req.method == "POST" and admin_deactivate:
        payload, status = deactivate(int(admin_deactivate.group(1)))
        return _json(payload, status)

    admin_delete = re.fullmatch(r"/api/admin/vendors/(\d+)/delete", path)
    if req.method == "POST" and admin_delete:
        payload, status = delete_vendor(int(admin_delete.group(1)))
        return _json(payload, status)

    admin_warn = re.fullmatch(r"/api/admin/vendors/(\d+)/warn", path)
    if req.method == "POST" and admin_warn:
        payload, status = warn_vendor(req, int(admin_warn.group(1)))
        return _json(payload, status)

    if req.method == "POST" and path == "/api/admin/announcement":
        payload, status = save_announcement(req)
        return _json(payload, status)

    if req.method == "POST" and path == "/api/admin/gst-alert":
        payload, status = gst_alert()
        return _json(payload, status)

    if req.method == "POST" and path == "/api/admin/push-test":
        payload, status = push_test()
        return _json(payload, status)

    if req.method == "POST" and path == "/api/admin/push-token":
        payload, status = save_admin_push(req, current_admin(req) or {})
        return _json(payload, status)

    if req.method == "POST" and path == "/api/admin/push-token/delete":
        payload, status = delete_admin_push(req)
        return _json(payload, status)

    if req.method == "GET" and path == "/api/admin/inquiries":
        return _json(inquiries())

    if req.method == "GET" and path == "/api/admin/chats":
        return _json({"success": True, "data": admin_threads()})

    admin_chat_json = re.fullmatch(r"/api/admin/chats/(\d+)/(\d+)", path)
    if req.method == "GET" and admin_chat_json:
        left = int(admin_chat_json.group(1))
        right = int(admin_chat_json.group(2))
        return _json({"success": True, "data": admin_thread(left, right)})

    inquiry_match = re.fullmatch(r"/api/admin/inquiries/(\d+)", path)
    if req.method == "GET" and inquiry_match:
        payload, status = inquiry_show(int(inquiry_match.group(1)))
        return _json(payload, status)

    inquiry_delete_match = re.fullmatch(r"/api/admin/inquiries/(\d+)/delete", path)
    if req.method == "POST" and inquiry_delete_match:
        payload, status = inquiry_delete(int(inquiry_delete_match.group(1)))
        return _json(payload, status)

    if req.method == "GET" and path == "/api/admin/invoices":
        return _json(invoices())

    invoice_page = re.fullmatch(r"/api/admin/invoices/(\d+)/html", path)
    if req.method == "GET" and invoice_page:
        return invoice_html(int(invoice_page.group(1)))

    invoice_match = re.fullmatch(r"/api/admin/invoices/(\d+)", path)
    if req.method == "GET" and invoice_match:
        payload, status = invoice_show(int(invoice_match.group(1)))
        return _json(payload, status)

    if req.method == "GET" and path == "/api/admin/products":
        return _json(admin_products())

    product_delete = re.fullmatch(r"/api/admin/products/(\d+)/delete", path)
    if req.method == "POST" and product_delete:
        payload, status = admin_delete_product(int(product_delete.group(1)))
        return _json(payload, status)

    if req.method == "GET" and path == "/api/admin/requirements":
        return _json(requirements(req.args.get("filter") or "all"))

    requirement_match = re.fullmatch(r"/api/admin/requirements/(\d+)", path)
    if req.method == "GET" and requirement_match:
        payload, status = requirement_show(int(requirement_match.group(1)))
        return _json(payload, status)

    requirement_close_match = re.fullmatch(r"/api/admin/requirements/(\d+)/close", path)
    if req.method == "POST" and requirement_close_match:
        payload, status = requirement_close(int(requirement_close_match.group(1)))
        return _json(payload, status)

    status_match = re.fullmatch(r"/api/vendor/status/(\d+)", path)
    if req.method == "GET" and status_match:
        body, status = vendor_status(status_match.group(1))
        return _json(body, status)

    conv_match = re.fullmatch(r"/api/messages/conversations/(\d+)", path)
    if req.method == "GET" and conv_match:
        return _json(conversations(int(conv_match.group(1))))

    chat_match = re.fullmatch(r"/api/messages/chat/(\d+)/(\d+)", path)
    if req.method == "GET" and chat_match:
        return _json(chat(int(chat_match.group(1)), int(chat_match.group(2))))

    if req.method == "POST" and path == "/api/messages/send":
        body = req.get_json(silent=True) or {}
        payload, status = send_message(body.get("sender_id"), body.get("receiver_id"), body.get("message"))
        return _json(payload, status)

    contact_match = re.fullmatch(r"/api/messages/contacts/(\d+)", path)
    if req.method == "GET" and contact_match:
        return _json(contacts(int(contact_match.group(1))))

    if req.method == "POST" and path == "/api/products/add":
        body, status = add_product(req)
        return _json(body, status)

    update_match = re.fullmatch(r"/api/products/update/(\d+)", path)
    if req.method == "POST" and update_match:
        body, status = update_product(req, int(update_match.group(1)))
        return _json(body, status)

    delete_match = re.fullmatch(r"/api/products/delete/(\d+)", path)
    if req.method == "POST" and delete_match:
        body, status = delete_product(req, int(delete_match.group(1)))
        return _json(body, status)

    sheet_delete = re.fullmatch(r"/api/products/(\d+)/stock-sheet/delete", path)
    if req.method == "POST" and sheet_delete:
        body, status = delete_stock_sheet(req, int(sheet_delete.group(1)))
        return _json(body, status)

    sheet_match = re.fullmatch(r"/api/products/(\d+)/stock-sheet", path)
    if sheet_match:
        product_id = int(sheet_match.group(1))
        if req.method == "POST":
            body, status = upload_stock_sheet(req, product_id)
            return _json(body, status)
        if req.method == "GET":
            vendor_raw = (req.args.get("vendor_id") or "").strip()
            body, status = read_stock_sheet(product_id, int(vendor_raw) if vendor_raw.isdigit() else 0)
            return _json(body, status)

    media_match = re.fullmatch(r"/api/media/products/([A-Za-z0-9._-]+)", path)
    if req.method == "GET" and media_match:
        return read_product_image(media_match.group(1))

    vendor_media = re.fullmatch(r"/api/media/vendors/([A-Za-z0-9._-]+)", path)
    if req.method == "GET" and vendor_media:
        return read_stored_image("vendors", vendor_media.group(1))

    return _json({"status": False, "message": "Not found"}, 404)
