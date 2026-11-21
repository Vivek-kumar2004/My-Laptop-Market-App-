(function () {
  const API_BASE = "https://asia-south1-mylaptopmarket-app.cloudfunctions.net/api/api";
  const FALLBACK_IMG = window.location.origin + "/mylaptop/assets/images/no-product.svg";
  const productsGrid = document.getElementById("homeProductsGrid");
  const vendorsGrid = document.getElementById("homeVendorsGrid");

  if (!productsGrid && !vendorsGrid) return;

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

  function renderProducts(products) {
    if (!productsGrid) return;
    if (!products || !products.length) {
      productsGrid.innerHTML =
        '<p class="home-empty">New listings will appear here as vendors go live.</p>';
      return;
    }

    productsGrid.innerHTML = products
      .map(function (p) {
        const img = p.main_image || (p.image_urls && p.image_urls[0]) || FALLBACK_IMG;
        const letter = escapeHtml(initial(p.vendor_name));
        const logo = p.vendor_logo_url
          ? '<img src="' +
            escapeHtml(p.vendor_logo_url) +
            '" alt="" onerror="this.style.display=\'none\';this.nextElementSibling.style.display=\'grid\'">' +
            '<span class="vendor-fallback" style="display:none">' +
            letter +
            "</span>"
          : '<span class="vendor-fallback">' + letter + "</span>";

        return (
          '<a class="home-product-card" href="' +
          (p.id ? "product.html?id=" + encodeURIComponent(p.id) : "products.html") +
          '">' +
          '<div class="home-product-visual">' +
          (p.category
            ? '<span class="home-product-badge">' + escapeHtml(p.category) + "</span>"
            : "") +
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
          '<div class="home-product-vendor">' +
          logo +
          "<span>" +
          escapeHtml(p.vendor_name || "Verified vendor") +
          "</span></div></div></a>"
        );
      })
      .join("");
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
          "<h3>" +
          escapeHtml(v.vendor_name || "Vendor") +
          "</h3>" +
          "<p>" +
          (v.vendor_location ? escapeHtml(v.vendor_location) + " · " : "") +
          count +
          " listing" +
          (count !== 1 ? "s" : "") +
          "</p></a>"
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
