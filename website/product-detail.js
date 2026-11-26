(function () {
  const API_BASE = "https://mylaptopmarket.com/api/api";
  const VENDOR_LOGIN_URL = "https://mylaptopmarket.com/api/vendor/login";
  const MESSAGES_URL = "https://mylaptopmarket.com/api/vendor/messages";
  const FALLBACK_IMG = window.location.origin + "/mylaptop/assets/images/no-product.svg";
  const RELATED_CAP = 4;

  const els = {
    loading: document.getElementById("detailLoading"),
    error: document.getElementById("detailError"),
    errorText: document.getElementById("detailErrorText"),
    layout: document.getElementById("detailLayout"),
    breadcrumbName: document.getElementById("breadcrumbName"),
    mainImage: document.getElementById("detailMainImage"),
    badge: document.getElementById("detailBadge"),
    thumbs: document.getElementById("detailThumbs"),
    name: document.getElementById("detailName"),
    price: document.getElementById("detailPrice"),
    facts: document.getElementById("detailFacts"),
    description: document.getElementById("detailDescription"),
    video: document.getElementById("detailVideo"),
    videoFrame: document.getElementById("detailVideoFrame"),
    vendor: document.getElementById("detailVendor"),
    sheet: document.getElementById("detailSheet"),
    sheetBody: document.getElementById("detailSheetBody"),
    related: document.getElementById("detailRelated"),
    relatedGrid: document.getElementById("relatedGrid"),
  };

  let isAuthenticated = false;
  let currentVendorId = null;

  function escapeHtml(str) {
    const div = document.createElement("div");
    div.textContent = str == null ? "" : String(str);
    return div.innerHTML;
  }

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

  function showError(message) {
    els.loading.hidden = true;
    els.loading.style.display = "none";
    els.layout.hidden = true;
    els.error.hidden = false;
    if (message) els.errorText.textContent = message;
  }

  function youtubeEmbed(url) {
    if (!url) return "";
    const match = String(url).match(
      /(?:youtube\.com\/(?:watch\?v=|embed\/|shorts\/)|youtu\.be\/)([\w-]{6,})/
    );
    return match ? "https://www.youtube.com/embed/" + match[1] : "";
  }

  function renderGallery(product) {
    const images = (product.image_urls && product.image_urls.length
      ? product.image_urls
      : [product.main_image]
    ).filter(Boolean);
    const list = images.length ? images : [FALLBACK_IMG];

    els.mainImage.src = list[0];
    els.mainImage.alt = product.product_name || "Product";
    els.mainImage.onerror = function () {
      els.mainImage.onerror = null;
      els.mainImage.src = FALLBACK_IMG;
    };

    if (product.category) {
      els.badge.hidden = false;
      els.badge.textContent = product.category;
    }

    if (list.length < 2) {
      els.thumbs.innerHTML = "";
      return;
    }

    els.thumbs.innerHTML = list
      .map(function (src, i) {
        return (
          '<button type="button" class="detail-thumb' +
          (i === 0 ? " active" : "") +
          '" data-src="' +
          escapeHtml(src) +
          '"><img src="' +
          escapeHtml(src) +
          '" alt="View ' +
          (i + 1) +
          '" loading="lazy"></button>'
        );
      })
      .join("");

    els.thumbs.addEventListener("click", function (e) {
      const btn = e.target.closest(".detail-thumb");
      if (!btn) return;
      els.mainImage.src = btn.dataset.src;
      els.thumbs.querySelectorAll(".detail-thumb").forEach(function (t) {
        t.classList.remove("active");
      });
      btn.classList.add("active");
    });
  }

  function renderFacts(product) {
    const facts = [
      { label: "Category", value: product.category },
      { label: "Stock", value: product.stock == null ? null : product.stock + " units" },
      { label: "Listing ID", value: "#" + product.id },
    ];

    if (isAuthenticated && product.vendor_location) {
      facts.push({ label: "Location", value: product.vendor_location });
    }

    els.facts.innerHTML = facts
      .filter(function (f) {
        return f.value !== null && f.value !== undefined && f.value !== "";
      })
      .map(function (f) {
        return (
          '<li><span class="fact-label">' +
          escapeHtml(f.label) +
          '</span><span class="fact-value">' +
          escapeHtml(f.value) +
          "</span></li>"
        );
      })
      .join("");
  }

  function messageHref(product) {
    return (
      MESSAGES_URL +
      "?contact=" +
      encodeURIComponent(product.vendor_id) +
      "&product=" +
      encodeURIComponent(product.id)
    );
  }

  function canMessageVendor(product) {
    return (
      isAuthenticated &&
      product.vendor_id &&
      String(product.vendor_id) !== String(currentVendorId || "")
    );
  }

  function renderVendor(product) {
    if (isAuthenticated && product.vendor_name) {
      const canMessage = canMessageVendor(product);
      els.vendor.innerHTML =
        '<div class="vendor-section">' +
        '<div class="vendor-details">' +
        '<span class="vendor-name"><i class="fa-solid fa-store"></i> ' +
        escapeHtml(product.vendor_name) +
        (product.vendor_shop_trusted ? ' <span class="vendor-verified">Verified</span>' : "") +
        "</span>" +
        (product.vendor_location
          ? '<span class="vendor-info"><i class="fa-solid fa-location-dot"></i> ' +
            escapeHtml(product.vendor_location) +
            "</span>"
          : "") +
        (product.vendor_number
          ? '<span class="vendor-info"><i class="fa-solid fa-phone"></i> ' +
            escapeHtml(product.vendor_number) +
            "</span>"
          : "") +
        '<div class="vendor-cta">' +
        (product.vendor_number
          ? '<a href="tel:' +
            escapeHtml(product.vendor_number) +
            '" class="vendor-call-btn"><i class="fa-solid fa-phone"></i> Call Vendor</a>'
          : "") +
        (canMessage
          ? '<a href="' +
            escapeHtml(messageHref(product)) +
            '" class="vendor-msg-btn"><i class="fa-solid fa-comment-dots"></i> Message Vendor</a>'
          : "") +
        "</div></div></div>";
      return;
    }

    els.vendor.innerHTML =
      '<div class="vendor-section locked">' +
      '<div class="vendor-locked-overlay">' +
      '<i class="fa-solid fa-lock"></i>' +
      "<p>Vendor details are available after login</p>" +
      '<a href="' +
      VENDOR_LOGIN_URL +
      '">Vendor Login</a>' +
      "</div></div>";
  }

  function renderStockSheet(product) {
    if (!product.has_stock_sheet) {
      els.sheet.hidden = true;
      els.sheetBody.innerHTML = "";
      return;
    }

    els.sheet.hidden = false;
    if (!isAuthenticated || !currentVendorId) {
      els.sheetBody.innerHTML =
        '<div class="detail-sheet-login">' +
        '<i class="fa-solid fa-lock"></i>' +
        "<p>Stock sheet rows are visible only to logged-in vendors.</p>" +
        '<a href="' +
        VENDOR_LOGIN_URL +
        '">Vendor Login</a>' +
        "</div>";
      return;
    }

    els.sheetBody.innerHTML =
      '<button type="button" class="vendor-msg-btn" id="openSheetBtn">' +
      '<i class="fa-solid fa-table"></i> View stock sheet</button>' +
      '<div id="sheetTableWrap" hidden></div>';

    document.getElementById("openSheetBtn").addEventListener("click", function () {
      loadStockSheet(product.id);
    });
  }

  function loadStockSheet(productId) {
    const wrap = document.getElementById("sheetTableWrap");
    const button = document.getElementById("openSheetBtn");
    if (!wrap) return;
    wrap.hidden = false;
    wrap.innerHTML = '<p class="detail-sheet-note">Loading sheet...</p>';
    if (button) button.disabled = true;

    fetch(
      API_BASE +
        "/products/" +
        encodeURIComponent(productId) +
        "/stock-sheet?vendor_id=" +
        encodeURIComponent(currentVendorId),
      { credentials: "include" }
    )
      .then(function (res) {
        return res.json().then(function (data) {
          if (!res.ok || !data.status) {
            throw new Error(data.message || "Could not open stock sheet");
          }
          return data;
        });
      })
      .then(function (data) {
        const headers = data.headers || [];
        const rows = data.rows || [];
        const headHtml = headers
          .map(function (cell) {
            return "<th>" + escapeHtml(cell) + "</th>";
          })
          .join("");
        const bodyHtml = rows.length
          ? rows
              .map(function (row) {
                return (
                  "<tr>" +
                  (row || [])
                    .map(function (cell) {
                      return "<td>" + escapeHtml(cell) + "</td>";
                    })
                    .join("") +
                  "</tr>"
                );
              })
              .join("")
          : '<tr><td colspan="' +
            Math.max(1, headers.length) +
            '">This sheet has no data rows.</td></tr>';

        wrap.innerHTML =
          (data.original_name
            ? '<p class="detail-sheet-note">' + escapeHtml(data.original_name) + "</p>"
            : "") +
          '<p class="detail-sheet-note">Rows can be read here. The Excel file cannot be downloaded or shared.</p>' +
          (data.truncated
            ? '<p class="detail-sheet-note">Showing the first rows and columns only.</p>'
            : "") +
          '<div class="detail-sheet-table-wrap">' +
          '<div class="detail-sheet-scroll">' +
          '<table class="detail-sheet-table"><thead><tr>' +
          headHtml +
          "</tr></thead><tbody>" +
          bodyHtml +
          "</tbody></table>" +
          "</div>" +
          '<div class="detail-sheet-watermark">My Laptop Market</div>' +
          "</div>";
      })
      .catch(function (err) {
        wrap.innerHTML =
          '<p class="detail-sheet-note">' +
          escapeHtml(err.message || "Could not open stock sheet") +
          "</p>";
        if (button) button.disabled = false;
      });
  }

  function renderRelated(product, all) {
    const sameCategory = all.filter(function (p) {
      return p.id !== product.id && p.category === product.category;
    });
    const others = all.filter(function (p) {
      return p.id !== product.id && p.category !== product.category;
    });
    const list = sameCategory.concat(others).slice(0, RELATED_CAP);

    if (!list.length) return;

    els.related.hidden = false;
    els.relatedGrid.innerHTML = list
      .map(function (p) {
        const href = "product.html?id=" + encodeURIComponent(p.id);
        return (
          '<article class="product-card">' +
          '<a class="product-card-image" href="' +
          href +
          '"><img src="' +
          escapeHtml(p.main_image || FALLBACK_IMG) +
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
          '<a class="product-details-btn" href="' +
          href +
          '">View details <i class="fa-solid fa-arrow-right"></i></a>' +
          "</div></article>"
        );
      })
      .join("");
  }

  function render(product, all) {
    document.title = (product.product_name || "Product") + " - My Laptop Market";
    els.breadcrumbName.textContent = product.product_name || "Product";
    els.name.textContent = product.product_name || "Product";
    if (!isAuthenticated) {
      els.price.innerHTML = displayPrice(product.price);
    } else {
      els.price.textContent = formatPrice(product.price);
    }
    els.description.textContent =
      product.description || "No description provided by the vendor.";

    renderGallery(product);
    renderFacts(product);
    renderVendor(product);
    renderStockSheet(product);

    const embed = youtubeEmbed(product.youtube_video);
    if (embed) {
      els.video.hidden = false;
      els.videoFrame.innerHTML =
        '<iframe src="' +
        escapeHtml(embed) +
        '" title="Product video" allowfullscreen loading="lazy"></iframe>';
    }

    renderRelated(product, all);

    els.loading.hidden = true;
    els.loading.style.display = "none";
    els.layout.hidden = false;
  }

  const productId = new URLSearchParams(window.location.search).get("id");

  if (!productId) {
    showError("No product selected. Browse the catalog to pick one.");
    return;
  }

  fetch(API_BASE + "/web/products", { credentials: "include" })
    .then(function (res) {
      if (!res.ok) throw new Error("Request failed: " + res.status);
      return res.json();
    })
    .then(function (data) {
      isAuthenticated = !!data.authenticated;
      currentVendorId = data.vendor && data.vendor.id ? data.vendor.id : null;
      const all = data.data || [];
      const product = all.find(function (p) {
        return String(p.id) === String(productId);
      });

      if (!product) {
        showError("This product is no longer available.");
        return;
      }

      render(product, all);
    })
    .catch(function (err) {
      console.error("Product detail load failed:", err);
      showError("Unable to load this product. Please try again.");
    });
})();
