(function () {
  const API = "https://asia-south1-mylaptopmarket-app.cloudfunctions.net/api/api";
  const section = document.getElementById("openMarket");
  const grid = document.getElementById("openMarketGrid");
  const pills = document.getElementById("openMarketPills");
  const count = document.getElementById("openMarketCount");
  const detailRoot = document.getElementById("marketDetail");
  let rows = [];
  let chip = "All";

  function escapeHtml(str) {
    const div = document.createElement("div");
    div.textContent = str == null ? "" : String(str);
    return div.innerHTML;
  }

  function money(price) {
    const num = parseFloat(price);
    if (isNaN(num) || num <= 0) return "Ask for price";
    return "₹" + num.toLocaleString("en-IN");
  }

  function card(item) {
    const photo = item.images && item.images[0];
    const phone = item.contact_phone || "";
    const specs = (item.specs || [])
      .slice(0, 4)
      .map(function (spec) {
        return "<span>" + escapeHtml(spec) + "</span>";
      })
      .join("");
    const href = "market.html?id=" + encodeURIComponent(item.id);
    return (
      '<article class="om-card">' +
      '<a class="om-photo" href="' +
      href +
      '">' +
      (photo
        ? '<img src="' + escapeHtml(photo) + '" alt="' + escapeHtml(item.name) + '" loading="lazy">'
        : '<div class="om-photo-empty"><i class="fa-solid fa-microchip"></i></div>') +
      '<span class="om-cat">' +
      escapeHtml(item.category || "Stock") +
      "</span></a>" +
      '<div class="om-body">' +
      "<h3><a href=\"" +
      href +
      '">' +
      escapeHtml(item.name) +
      "</a></h3>" +
      (specs ? '<div class="om-specs">' + specs + "</div>" : "") +
      '<div class="om-meta"><strong>' +
      money(item.price) +
      "</strong><span>" +
      (item.stock ? escapeHtml(String(item.stock)) + " in stock" : "") +
      "</span></div>" +
      '<p class="om-who">' +
      escapeHtml(item.contact_name || "Dealer post") +
      (item.city ? " · " + escapeHtml(item.city) : "") +
      "</p>" +
      '<div class="om-actions">' +
      (phone
        ? '<a class="om-call" href="tel:+91' +
          encodeURIComponent(phone) +
          '"><i class="fa-solid fa-phone"></i> Call</a>' +
          '<a class="om-wa" target="_blank" rel="noopener" href="https://wa.me/91' +
          encodeURIComponent(phone) +
          '"><i class="fa-brands fa-whatsapp"></i> WhatsApp</a>'
        : "") +
      '<a class="om-more" href="' +
      href +
      '">Details</a></div>' +
      '<p class="om-note">Independent listing · not a registered vendor</p>' +
      "</div></article>"
    );
  }

  function renderPills() {
    if (!pills) return;
    const names = ["All"].concat(
      Array.from(
        new Set(
          rows
            .map(function (row) {
              return row.category;
            })
            .filter(Boolean)
        )
      )
    );
    pills.innerHTML = names
      .map(function (name) {
        return (
          '<button type="button" data-chip="' +
          escapeHtml(name) +
          '" class="' +
          (name === chip ? "is-on" : "") +
          '">' +
          escapeHtml(name) +
          "</button>"
        );
      })
      .join("");
    pills.querySelectorAll("button").forEach(function (button) {
      button.addEventListener("click", function () {
        chip = button.getAttribute("data-chip") || "All";
        render();
      });
    });
  }

  function render() {
    if (!section || !grid) return;
    if (window.__omHide) {
      section.hidden = true;
      return;
    }
    let list = rows.slice();
    const pageCategory = window.__omCategory || "";
    const query = (window.__omSearch || "").trim().toLowerCase();
    if (chip !== "All") list = list.filter(function (row) { return row.category === chip; });
    else if (pageCategory) list = list.filter(function (row) { return row.category === pageCategory; });
    if (query) {
      list = list.filter(function (row) {
        const blob = [row.name, row.category, row.city, row.contact_name, (row.specs || []).join(" ")]
          .join(" ")
          .toLowerCase();
        return blob.indexOf(query) !== -1;
      });
    }
    section.hidden = list.length === 0 && rows.length === 0;
    if (count) {
      count.textContent = list.length
        ? list.length + " independent listing" + (list.length === 1 ? "" : "s")
        : "No open-market listings for this filter.";
    }
    grid.innerHTML = list.map(card).join("");
    renderPills();
  }

  window.filterOpenMarket = function (category, search, hide) {
    window.__omCategory = category || "";
    window.__omSearch = search || "";
    window.__omHide = !!hide;
    render();
  };

  function renderDetail(item) {
    if (!detailRoot) return;
    const images = item.images && item.images.length ? item.images : [];
    const phone = item.contact_phone || "";
    detailRoot.innerHTML =
      '<a class="market-back" href="products.html#openMarket"><i class="fa-solid fa-arrow-left"></i> Back to products</a>' +
      '<div class="market-hero">' +
      '<div><div class="market-stage" id="marketStage">' +
      (images[0]
        ? '<img id="marketMain" src="' + escapeHtml(images[0]) + '" alt="' + escapeHtml(item.name) + '">'
        : '<div class="om-photo-empty"><i class="fa-solid fa-microchip"></i></div>') +
      "</div>" +
      (images.length > 1
        ? '<div class="market-thumbs">' +
          images
            .map(function (src, index) {
              return (
                '<button type="button" data-src="' +
                escapeHtml(src) +
                '" aria-label="Photo ' +
                (index + 1) +
                '"><img src="' +
                escapeHtml(src) +
                '" alt=""></button>'
              );
            })
            .join("") +
          "</div>"
        : "") +
      "</div>" +
      '<div class="market-copy">' +
      '<p class="open-market-kicker"><span></span> Open Market</p>' +
      "<h1>" +
      escapeHtml(item.name) +
      "</h1>" +
      '<p class="market-price">' +
      money(item.price) +
      "</p>" +
      '<p class="om-who">' +
      (item.stock ? escapeHtml(String(item.stock)) + " in stock" : "") +
      (item.city ? " · " + escapeHtml(item.city) : "") +
      "</p>" +
      ((item.specs || []).length
        ? '<div class="om-specs">' +
          item.specs
            .map(function (spec) {
              return "<span>" + escapeHtml(spec) + "</span>";
            })
            .join("") +
          "</div>"
        : "") +
      '<p class="om-who">' +
      escapeHtml(item.contact_name || "Dealer post") +
      "</p>" +
      '<div class="om-actions">' +
      (phone
        ? '<a class="om-call" href="tel:+91' +
          encodeURIComponent(phone) +
          '"><i class="fa-solid fa-phone"></i> Call ' +
          escapeHtml(phone) +
          "</a>" +
          '<a class="om-wa" target="_blank" rel="noopener" href="https://wa.me/91' +
          encodeURIComponent(phone) +
          '"><i class="fa-brands fa-whatsapp"></i> WhatsApp</a>'
        : "") +
      "</div>" +
      '<p class="om-note">Independent listing. This seller is not a registered vendor on My Laptop Market.</p>' +
      "</div></div>" +
      '<div class="market-story">' +
      escapeHtml(item.description || "No extra description was posted with this stock.") +
      "</div>";
    detailRoot.querySelectorAll(".market-thumbs button").forEach(function (button) {
      button.addEventListener("click", function () {
        const image = document.getElementById("marketMain");
        if (image) image.src = button.getAttribute("data-src");
      });
    });
  }

  async function load() {
    if (detailRoot) {
      const id = new URLSearchParams(window.location.search).get("id");
      if (!id) {
        detailRoot.innerHTML = '<p class="om-who">Listing not found. <a href="products.html#openMarket">Back to products</a></p>';
        return;
      }
      try {
        const response = await fetch(API + "/market-listings/" + encodeURIComponent(id));
        const payload = await response.json();
        if (!response.ok || !payload.status || !payload.data) throw new Error("missing");
        renderDetail(payload.data);
        document.title = payload.data.name + " | Open Market";
      } catch (error) {
        detailRoot.innerHTML = '<p class="om-who">This listing is not available. <a href="products.html#openMarket">Back to products</a></p>';
      }
      return;
    }
    if (!section) return;
    try {
      const response = await fetch(API + "/market-listings");
      const payload = await response.json();
      rows = payload.data || [];
      render();
    } catch (error) {
      section.hidden = true;
    }
  }

  load();
})();
