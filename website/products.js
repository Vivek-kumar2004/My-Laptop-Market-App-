(function () {
  const API_BASE = "https://mylaptopmarket.com/api/api";
  const VENDOR_LOGIN_URL = "https://mylaptopmarket.com/api/vendor/login";

  let allProducts = [];
  let vendorMatches = [];
  let categories = [];
  let isAuthenticated = false;
  let activeCategory = "";
  let searchTerm = "";
  let activeVendorId = null;
  let activeVendorName = "";

  const FALLBACK_IMG = window.location.origin + "/mylaptop/assets/images/no-product.svg";

  const els = {
    authBanner: document.getElementById("authBanner"),
    authMessage: document.getElementById("authMessage"),
    categoryPills: document.getElementById("categoryPills"),
    searchInput: document.getElementById("productSearch"),
    searchBtn: document.getElementById("searchBtn"),
    vendorMatches: document.getElementById("vendorMatches"),
    productsGrid: document.getElementById("productsGrid"),
    productsCount: document.getElementById("productsCount"),
    loading: document.getElementById("productsLoading"),
    empty: document.getElementById("productsEmpty"),
    error: document.getElementById("productsError"),
    section: document.getElementById("productsSection"),
  };

  function formatPrice(price) {
    const num = parseFloat(price);
    if (isNaN(num) || num <= 0) return "Ask for price";
    return "₹" + num.toLocaleString("en-IN");
  }

  function displayPrice(price) {
    if (!isAuthenticated) {
      return (
        '<a class="price-login-link" href="' +
        VENDOR_LOGIN_URL +
        '"><i class="fa-solid fa-lock"></i> Login to view price</a>'
      );
    }
    return formatPrice(price);
  }

  function escapeHtml(str) {
    const div = document.createElement("div");
    div.textContent = str || "";
    return div.innerHTML;
  }

  function renderAuthBanner(vendor) {
    if (!els.authBanner) return;

    if (isAuthenticated && vendor) {
      els.authBanner.classList.add("logged-in");
      els.authBanner.innerHTML =
        '<span id="authMessage"><i class="fa-solid fa-circle-check"></i> Logged in as <strong>' +
        escapeHtml(vendor.shop_name || vendor.username) +
        "</strong> — vendor details visible</span>";
      els.authMessage = document.getElementById("authMessage");
    } else {
      els.authBanner.classList.remove("logged-in");
      els.authBanner.innerHTML =
        '<span id="authMessage"><i class="fa-solid fa-lock"></i> Prices and vendor details are hidden. Login to view trade price, shop name, location &amp; contact.</span>' +
        '<a class="auth-banner-btn" href="' +
        VENDOR_LOGIN_URL +
        '">Login as Vendor</a>';
      els.authMessage = document.getElementById("authMessage");
    }
  }

  function renderCategories() {
    if (!els.categoryPills) return;

    let html =
      '<button class="category-pill' +
      (activeCategory === "" ? " active" : "") +
      '" data-category="">All Products</button>';

    categories.forEach(function (cat) {
      html +=
        '<button class="category-pill' +
        (activeCategory === cat ? " active" : "") +
        '" data-category="' +
        escapeHtml(cat) +
        '">' +
        escapeHtml(cat) +
        "</button>";
    });

    els.categoryPills.innerHTML = html;

    els.categoryPills.querySelectorAll(".category-pill").forEach(function (pill) {
      pill.addEventListener("click", function () {
        activeCategory = pill.dataset.category || "";
        renderCategories();
        loadProducts();
      });
    });
  }

  function renderVendorMatches(vendors) {
    if (!els.vendorMatches) return;

    if (!vendors || !vendors.length || activeVendorId) {
      els.vendorMatches.style.display = "none";
      els.vendorMatches.innerHTML = "";
      return;
    }

    els.vendorMatches.style.display = "block";
    els.vendorMatches.innerHTML =
      '<p class="vendor-matches-title"><i class="fa-solid fa-store"></i> Matching vendors</p>' +
      '<div class="vendor-matches-grid">' +
      vendors
        .map(function (v) {
          const count = Number(v.product_count) || 0;
          const countLabel = count + " product" + (count !== 1 ? "s" : "");
          const logo = v.vendor_logo_url
            ? '<img src="' +
              escapeHtml(v.vendor_logo_url) +
              '" alt="" class="vendor-match-logo" onerror="this.style.display=\'none\'">'
            : '<span class="vendor-match-logo placeholder"><i class="fa-solid fa-store"></i></span>';

          return (
            '<button type="button" class="vendor-match-card" data-vendor-id="' +
            escapeHtml(String(v.vendor_id)) +
            '" data-vendor-name="' +
            escapeHtml(v.vendor_name || "") +
            '">' +
            logo +
            '<div class="vendor-match-info">' +
            '<strong class="vendor-match-name">' +
            escapeHtml(v.vendor_name || "Vendor") +
            "</strong>" +
            (v.vendor_location
              ? '<span class="vendor-match-loc"><i class="fa-solid fa-location-dot"></i> ' +
                escapeHtml(v.vendor_location) +
                "</span>"
              : "") +
            '<span class="vendor-match-count">' +
            countLabel +
            "</span>" +
            "</div>" +
            '<i class="fa-solid fa-chevron-right vendor-match-arrow"></i>' +
            "</button>"
          );
        })
        .join("") +
      "</div>";

    els.vendorMatches.querySelectorAll(".vendor-match-card").forEach(function (card) {
      card.addEventListener("click", function () {
        activeVendorId = card.dataset.vendorId;
        activeVendorName = card.dataset.vendorName || "";
        loadProducts();
      });
    });
  }

  function renderActiveVendorFilter() {
    if (!els.vendorMatches || !activeVendorId) return;

    els.vendorMatches.style.display = "block";
    els.vendorMatches.innerHTML =
      '<div class="vendor-filter-chip">' +
      '<span><i class="fa-solid fa-store"></i> Showing products from <strong>' +
      escapeHtml(activeVendorName || "Vendor") +
      "</strong></span>" +
      '<button type="button" id="clearVendorFilter" class="clear-vendor-filter">' +
      '<i class="fa-solid fa-xmark"></i> Clear</button>' +
      "</div>";

    var clearBtn = document.getElementById("clearVendorFilter");
    if (clearBtn) {
      clearBtn.addEventListener("click", function () {
        activeVendorId = null;
        activeVendorName = "";
        loadProducts();
      });
    }
  }

  function renderVendorSection(product) {
    if (isAuthenticated && product.vendor_name) {
      return (
        '<div class="vendor-section">' +
        '<div class="vendor-details">' +
        '<span class="vendor-name"><i class="fa-solid fa-store"></i> ' +
        escapeHtml(product.vendor_name) +
        "</span>" +
        (product.vendor_location
          ? '<span class="vendor-info"><i class="fa-solid fa-location-dot"></i> ' +
            escapeHtml(product.vendor_location) +
            "</span>"
          : "") +
        (product.vendor_number
          ? '<span class="vendor-info"><i class="fa-solid fa-phone"></i> ' +
            escapeHtml(product.vendor_number) +
            "</span>" +
            '<a href="tel:' +
            escapeHtml(product.vendor_number) +
            '" class="vendor-call-btn"><i class="fa-solid fa-phone"></i> Call Vendor</a>'
          : "") +
        "</div></div>"
      );
    }

    return (
      '<div class="vendor-section locked">' +
      '<div class="vendor-locked-overlay">' +
      '<i class="fa-solid fa-lock"></i>' +
      "<p>Vendor details are available after login</p>" +
      '<a href="' +
      VENDOR_LOGIN_URL +
      '">Vendor Login</a>' +
      "</div></div>"
    );
  }

  function renderProducts(products) {
    if (!els.productsGrid) return;

    els.loading.style.display = "none";
    els.error.style.display = "none";

    if (!products.length) {
      els.empty.style.display = "block";
      els.productsGrid.innerHTML = "";
      if (els.section) els.section.classList.add("is-empty-catalog");
      els.productsCount.textContent = "0 products found";
      return;
    }

    if (els.section) els.section.classList.remove("is-empty-catalog");
    els.empty.style.display = "none";
    els.productsCount.textContent =
      products.length +
      " product" +
      (products.length !== 1 ? "s" : "") +
      " found" +
      (activeVendorName ? " for " + activeVendorName : "");

    els.productsGrid.innerHTML = products
      .map(function (p) {
        const href = "product.html?id=" + encodeURIComponent(p.id);
        return (
          '<article class="product-card">' +
          '<a class="product-card-image" href="' +
          href +
          '">' +
          '<img src="' +
          escapeHtml(p.main_image) +
          '" alt="' +
          escapeHtml(p.product_name) +
          '" loading="lazy" onerror="this.onerror=null;this.src=\'' +
          escapeHtml(FALLBACK_IMG) +
          "'\">" +
          (p.category
            ? '<span class="product-category-badge">' + escapeHtml(p.category) + "</span>"
            : "") +
          "</a>" +
          '<div class="product-card-body">' +
          '<h3><a class="product-card-link" href="' +
          href +
          '">' +
          escapeHtml(p.product_name) +
          "</a></h3>" +
          '<div class="product-price">' +
          displayPrice(p.price) +
          "</div>" +
          '<div class="product-stock">Stock: ' +
          escapeHtml(String(p.stock ?? "—")) +
          "</div>" +
          (p.description
            ? '<p class="product-desc">' + escapeHtml(p.description) + "</p>"
            : "") +
          '<a class="product-details-btn" href="' +
          href +
          '">View details <i class="fa-solid fa-arrow-right"></i></a>' +
          renderVendorSection(p) +
          "</div></article>"
        );
      })
      .join("");
  }

  async function fetchJson(url) {
    const res = await fetch(url, { credentials: "include" });
    if (!res.ok) throw new Error("Request failed: " + res.status);
    return res.json();
  }

  async function loadCategories() {
    try {
      const data = await fetchJson(API_BASE + "/categories");
      categories = data.data || [];
      renderCategories();
    } catch (e) {
      console.warn("Categories load failed:", e);
    }
  }

  async function loadProducts() {
    els.loading.style.display = "block";
    els.empty.style.display = "none";
    els.error.style.display = "none";
    els.productsGrid.innerHTML = "";

    try {
      let url = API_BASE + "/web/products?";
      if (activeCategory) url += "category=" + encodeURIComponent(activeCategory) + "&";
      if (searchTerm) url += "search=" + encodeURIComponent(searchTerm) + "&";
      if (activeVendorId) url += "vendor_id=" + encodeURIComponent(activeVendorId);

      const data = await fetchJson(url);
      isAuthenticated = !!data.authenticated;
      allProducts = data.data || [];
      vendorMatches = data.vendors || [];

      renderAuthBanner(data.vendor);

      if (activeVendorId) {
        renderActiveVendorFilter();
      } else {
        renderVendorMatches(vendorMatches);
      }

      renderProducts(allProducts);
      if (window.filterOpenMarket) window.filterOpenMarket(activeCategory, searchTerm, !!activeVendorId);
    } catch (e) {
      els.loading.style.display = "none";
      els.error.style.display = "block";
      if (els.section) els.section.classList.add("is-empty-catalog");
      if (els.vendorMatches) {
        els.vendorMatches.style.display = "none";
        els.vendorMatches.innerHTML = "";
      }
      console.error("Products load failed:", e);
      if (window.filterOpenMarket) window.filterOpenMarket(activeCategory, searchTerm, !!activeVendorId);
    }
  }

  function doSearch() {
    searchTerm = els.searchInput.value.trim();
    activeVendorId = null;
    activeVendorName = "";
    loadProducts();
  }

  if (els.searchBtn) {
    els.searchBtn.addEventListener("click", doSearch);
  }

  if (els.searchInput) {
    els.searchInput.addEventListener("keydown", function (e) {
      if (e.key === "Enter") doSearch();
    });
  }

  loadCategories();
  const params = new URLSearchParams(window.location.search);
  if (params.get("vendor_id")) {
    activeVendorId = params.get("vendor_id");
    activeVendorName = params.get("vendor_name") || "";
  }
  loadProducts();
})();
