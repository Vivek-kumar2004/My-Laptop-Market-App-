(function () {
  const API_BASE = "https://asia-south1-mylaptopmarket-app.cloudfunctions.net/api/api";
  const FALLBACK_IMG = window.location.origin + "/mylaptop/assets/images/no-product.svg";
  const productsGrid = document.getElementById("homeProductsGrid");
  const productsGridB = document.getElementById("homeProductsGridB");
  const vendorsGrid = document.getElementById("homeVendorsGrid");
  const carousel = document.getElementById("homeCarousel");
  const listingMeta = document.getElementById("listingMeta");

  if (!productsGrid && !productsGridB && !vendorsGrid && !carousel) return;

  function escapeHtml(str) {
    const div = document.createElement("div");
    div.textContent = str || "";
    return div.innerHTML;
  }

  function formatPrice(price) {
    const num = parseFloat(price);
    if (isNaN(num) || num <= 0) return "Ask for price";
    return "₹" + num.toLocaleString("en-IN");
  }

  function initial(name) {
    return (name || "V").trim().charAt(0).toUpperCase() || "V";
  }

  function productCard(p, opts) {
    opts = opts || {};
    const img = p.main_image || (p.image_urls && p.image_urls[0]) || FALLBACK_IMG;
    const letter = escapeHtml(initial(p.vendor_name));
    const href = p.id ? "product.html?id=" + encodeURIComponent(p.id) : "products.html";
    const logo = p.vendor_logo_url
      ? '<img src="' +
        escapeHtml(p.vendor_logo_url) +
        '" alt="" onerror="this.style.display=\'none\';this.nextElementSibling.style.display=\'grid\'">' +
        '<span class="vendor-fallback" style="display:none">' +
        letter +
        "</span>"
      : '<span class="vendor-fallback">' + letter + "</span>";

    if (opts.carousel) {
      return (
        '<a class="pdf-carousel-card" href="' +
        href +
        '">' +
        '<div class="img">' +
        '<span class="pdf-verified" aria-hidden="true"><i class="fa-solid fa-check"></i></span>' +
        '<img src="' +
        escapeHtml(img) +
        '" alt="' +
        escapeHtml(p.product_name || "Product") +
        '" loading="lazy" onerror="this.onerror=null;this.src=\'' +
        escapeHtml(FALLBACK_IMG) +
        "'\">" +
        "</div>" +
        '<div class="body">' +
        "<h3>" +
        escapeHtml(p.product_name || "Product") +
        "</h3>" +
        '<div class="pdf-price">' +
        formatPrice(p.price) +
        "</div>" +
        (p.category ? '<span class="pdf-chip">' + escapeHtml(p.category) + "</span>" : "") +
        "</div></a>"
      );
    }

    return (
      '<a class="home-product-card" href="' +
      href +
      '">' +
      '<div class="home-product-visual">' +
      (p.category
        ? '<span class="home-product-badge">' + escapeHtml(p.category) + "</span>"
        : "") +
      '<span class="pdf-verified" aria-hidden="true"><i class="fa-solid fa-check"></i></span>' +
      '<img src="' +
      escapeHtml(img) +
      '" alt="' +
      escapeHtml(p.product_name || "Product") +
      '" loading="lazy" onerror="this.onerror=null;this.src=\'' +
      escapeHtml(FALLBACK_IMG) +
      "'\">" +
      "</div>" +
      '<div class="home-product-body">' +
      "<h3>" +
      escapeHtml(p.product_name || "Product") +
      "</h3>" +
      '<div class="home-product-price">' +
      formatPrice(p.price) +
      "</div>" +
      '<div class="pdf-card-foot">' +
      '<div class="home-product-vendor">' +
      logo +
      "<span>" +
      escapeHtml(p.vendor_name || "Verified vendor") +
      "</span></div>" +
      '<span class="pdf-view">View Details <i class="fa-solid fa-arrow-right"></i></span>' +
      "</div></div></a>"
    );
  }

  function renderProducts(products) {
    const list = products || [];
    const empty =
      '<p class="home-empty">New listings will appear here as vendors go live.</p>';

    if (carousel) {
      carousel.innerHTML = list.length
        ? list
            .slice(0, 12)
            .map(function (p) {
              return productCard(p, { carousel: true });
            })
            .join("")
        : empty;
    }

    if (productsGrid) {
      productsGrid.innerHTML = list.length
        ? list
            .slice(0, 4)
            .map(function (p) {
              return productCard(p);
            })
            .join("")
        : empty;
    }

    if (productsGridB) {
      productsGridB.innerHTML = list.length
        ? list
            .slice(0, 8)
            .map(function (p) {
              return productCard(p);
            })
            .join("")
        : empty;
    }

    if (listingMeta && list.length) {
      listingMeta.textContent =
        "Showing " + list.length + " live listing" + (list.length === 1 ? "" : "s");
    }
  }

  function renderVendors(vendors) {
    if (!vendorsGrid) return;
    if (!vendors || !vendors.length) {
      vendorsGrid.innerHTML =
        '<p class="home-empty">Popular vendors will show here after listings go live.</p>';
      return;
    }

    vendorsGrid.innerHTML = vendors
      .map(function (v) {
        const count = Number(v.product_count) || 0;
        const href =
          "products.html?vendor_id=" +
          encodeURIComponent(v.vendor_id) +
          "&vendor_name=" +
          encodeURIComponent(v.vendor_name || "");
        const letter = escapeHtml(initial(v.vendor_name));
        const logo = v.vendor_logo_url
          ? '<div class="home-vendor-logo-wrap">' +
            '<img class="home-vendor-logo" src="' +
            escapeHtml(v.vendor_logo_url) +
            '" alt="' +
            escapeHtml(v.vendor_name || "Vendor") +
            '" loading="lazy" onerror="this.style.display=\'none\';this.nextElementSibling.style.display=\'grid\'">' +
            '<div class="home-vendor-logo fallback" style="display:none">' +
            letter +
            "</div></div>"
          : '<div class="home-vendor-logo-wrap"><div class="home-vendor-logo fallback">' +
            letter +
            "</div></div>";

        return (
          '<a class="home-vendor-card" href="' +
          href +
          '">' +
          logo +
          "<div><h3>" +
          escapeHtml(v.vendor_name || "Vendor") +
          "</h3><p>" +
          (v.vendor_location
            ? '<i class="fa-solid fa-location-dot"></i> ' + escapeHtml(v.vendor_location) + " · "
            : "") +
          '<span style="color:#20ca47;font-weight:700"><i class="fa-solid fa-check"></i> ' +
          count +
          " listing" +
          (count !== 1 ? "s" : "") +
          "</span></p></div>" +
          '<span class="pdf-vendor-go" aria-hidden="true"><i class="fa-solid fa-arrow-right"></i></span></a>'
        );
      })
      .join("");
  }

  fetch(API_BASE + "/web/home-showcase", { credentials: "include" })
    .then(function (res) {
      if (!res.ok) throw new Error("Showcase failed");
      return res.json();
    })
    .then(function (data) {
      renderProducts(data.products || []);
      renderVendors(data.vendors || []);
    })
    .catch(function (err) {
      console.warn("Home showcase failed:", err);
      renderProducts([]);
      renderVendors([]);
    });
})();
