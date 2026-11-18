(function () {
  const VENDOR_BASE = "https://mylaptopmarket.com/api/vendor";
  const API = "https://mylaptopmarket.com/api/api/web/auth-status";

  function applyLoggedInState(vendor) {
    const dashUrl = VENDOR_BASE + "/dashboard";
    const label = vendor.shop_name || vendor.username || "Dashboard";
    const shortLabel = label.length > 16 ? "Dashboard" : label;

    const navBtn = document.getElementById("vendorNavBtn");
    const mobileLink = document.getElementById("mobileVendorNavLink");
    const registerBtn = document.getElementById("heroRegisterBtn");

    if (navBtn) {
      navBtn.href = dashUrl;
      navBtn.textContent = shortLabel;
      navBtn.classList.add("vendor-logged-in");
      navBtn.title = "Go to vendor dashboard";
    }

    if (mobileLink) {
      mobileLink.href = dashUrl;
      mobileLink.textContent = "Dashboard";
      mobileLink.classList.add("vendor-logged-in");
    }

    if (registerBtn) {
      registerBtn.href = dashUrl;
      registerBtn.textContent = "Vendor Dashboard";
      registerBtn.classList.add("vendor-logged-in");
    }
  }

  fetch(API, { credentials: "include" })
    .then(function (res) {
      return res.json();
    })
    .then(function (data) {
      if (data.authenticated && data.vendor) {
        applyLoggedInState(data.vendor);
      }
    })
    .catch(function () {});
})();
