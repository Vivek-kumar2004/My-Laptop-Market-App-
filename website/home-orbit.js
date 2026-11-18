(function () {
  const API_BASE = "https://asia-south1-mylaptopmarket-app.cloudfunctions.net/api/api";
  const FALLBACK_IMG = window.location.origin + "/mylaptop/assets/images/no-product.svg";
  /* perspective magnification of the cards nearest the camera */
  const FRONT_SCALE = 1.3;
  const FRONT_SCALE_MOBILE = 1.15;
  /* the inner ring's radius as a share of the outer one */
  const RING_RATIO = 0.82;

  const section = document.getElementById("homeOrbit");
  const stage = document.getElementById("homeOrbitStage");
  const rig = document.getElementById("orbitRig");
  const laptop = document.getElementById("orbitLaptop");
  const ringTop = document.getElementById("orbitRingTop");
  const ringBottom = document.getElementById("orbitRingBottom");
  const cap = document.getElementById("orbitCap");

  if (!section || !stage || !ringTop || !ringBottom) return;

  let products = [];
  let angle = 0;
  let tiltShift = 0;
  let spinning = true;
  let drag = null;

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

  function cardHtml(product) {
    const img = product.main_image || (product.image_urls && product.image_urls[0]) || FALLBACK_IMG;
    return (
      '<a class="orbit-card" href="products.html">' +
      '<img src="' +
      escapeHtml(img) +
      '" alt="' +
      escapeHtml(product.product_name || "Product") +
      '" loading="lazy" onerror="this.onerror=null;this.src=\'' +
      escapeHtml(FALLBACK_IMG) +
      "'\">" +
      '<div class="orbit-card-meta">' +
      "<h3>" +
      escapeHtml(product.product_name || "Product") +
      "</h3>" +
      '<div class="orbit-price">' +
      formatPrice(product.price) +
      "</div>" +
      '<p class="orbit-vendor">' +
      escapeHtml(product.category || "IT hardware") +
      "</p></div></a>"
    );
  }

  function placeRing(ringEl, radius, yOffset, angleShift) {
    const step = 360 / ringEl.children.length;
    Array.prototype.forEach.call(ringEl.children, function (card, i) {
      card.style.transform =
        "rotateY(" +
        (step * i + angleShift) +
        "deg) translateZ(" +
        radius +
        "px) translateY(" +
        yOffset +
        "px)";
    });
  }

  function render() {
    const stageW = Math.max(stage.clientWidth > 100 ? stage.clientWidth : 0, window.innerWidth || 900);
    const narrow = stageW < 900;

    if (!products.length) {
      section.hidden = true;
      return;
    }
    section.hidden = false;

    /* one card is rendered on its own first so the ring maths can work from
       its real width and height instead of hard-coded numbers */
    ringTop.innerHTML = cardHtml(products[0]);
    const sample = ringTop.children[0];
    const cardHalf = sample.offsetHeight / 2;
    const pitch = sample.offsetWidth + (narrow ? 10 : 34);

    /* outer ring hugs the stage edge, inner ring sits well inside it so the
       two rings never cross each other on screen */
    const baseR = narrow
      ? Math.max(165, stageW * 0.44)
      : Math.max(360, Math.min(stageW * 0.36, 470));
    /* the rings may run a little wider than the stage: the cards out at the
       sides are turned edge-on, so they take almost no horizontal room */
    const maxR = stageW * 0.58;

    /* split the whole catalog across the rings, dropping only what a ring this
       wide physically cannot hold without cards touching */
    const perRingMax = Math.max(3, Math.floor((2 * Math.PI * maxR * RING_RATIO) / pitch));
    const perRing = Math.min(Math.ceil(products.length / 2), perRingMax);
    const visible = products.slice(0, perRing * 2);

    if (cap) {
      if (products.length > visible.length) {
        cap.hidden = false;
        cap.textContent =
          "Showing " + visible.length + " of " + products.length + " live listings";
      } else {
        cap.hidden = true;
      }
    }

    const split = Math.ceil(visible.length / 2);
    const upper = visible.slice(0, split);
    const lower = visible.slice(split);

    /* every ring is at least wide enough that its own cards cannot overlap */
    const spread = function (count) {
      return (count * pitch) / (2 * Math.PI);
    };
    const outerR = Math.min(maxR, Math.max(baseR, spread(lower.length), spread(upper.length) / RING_RATIO));
    /* close radii keep both rings at a similar perspective scale, so equal
       lifts read as evenly spaced above and below the laptop */
    const innerR = Math.max(spread(upper.length), outerR * RING_RATIO);

    ringTop.innerHTML = upper.map(cardHtml).join("");
    ringBottom.innerHTML = lower.map(cardHtml).join("");

    /* centre each card on its own measured height */
    Array.prototype.forEach.call(stage.querySelectorAll(".orbit-card"), function (card) {
      card.style.marginTop = -cardHalf + "px";
    });

    const scale = narrow ? FRONT_SCALE_MOBILE : FRONT_SCALE;
    const stageH = stage.clientHeight || 420;
    const edgePad = narrow ? 14 : 22;
    const lift = Math.max(cardHalf + 18, (stageH / 2 - edgePad) / scale - cardHalf);

    placeRing(ringTop, innerR, -lift, 0);
    placeRing(ringBottom, outerR, lift, lower.length ? 180 / lower.length : 0);

    if (laptop) {
      /* the laptop only fills the middle ~58% of its square image, so keep its
         visible half inside the clear band between the two rings */
      const bandHalf = (lift - cardHalf) * scale - 12;
      const cssWidth = narrow ? Math.min(140, stageW * 0.32) : Math.min(200, stageW * 0.17);
      laptop.style.width = Math.round(Math.max(80, Math.min(cssWidth, bandHalf / 0.29))) + "px";
    }

    /* the -8deg tilt drops the near side of both rings by a share of the radius */
    tiltShift = Math.round(outerR * 0.14);

    applyTransform();
    balance();
  }

  /* the tilt estimate is only approximate, so measure what actually landed on
     screen and even out the space above and below the rings */
  function balance() {
    const cards = stage.querySelectorAll(".orbit-card");
    if (!cards.length) return;
    const stageRect = stage.getBoundingClientRect();
    let top = Infinity;
    let bottom = -Infinity;
    cards.forEach(function (card) {
      const r = card.getBoundingClientRect();
      top = Math.min(top, r.top);
      bottom = Math.max(bottom, r.bottom);
    });
    tiltShift += Math.round((top - stageRect.top - (stageRect.bottom - bottom)) / 2);
    applyTransform();
  }

  function applyTransform() {
    /* the tilt drops the near side of both rings, so the rig is nudged back up
       and the laptop is pushed down by the same amount to stay dead centre */
    if (rig) rig.style.transform = "translateY(-" + tiltShift + "px) rotateX(-8deg)";
    if (laptop) {
      laptop.style.transform =
        "translate(-50%, -50%) translateY(" + tiltShift + "px) translateZ(0)";
    }
    /* rings turn in opposite directions so cards never stay stacked */
    ringTop.style.transform = "rotateY(" + angle + "deg)";
    ringBottom.style.transform = "rotateY(" + -angle + "deg)";
  }

  function tick() {
    if (spinning) angle += 0.16;
    applyTransform();
    requestAnimationFrame(tick);
  }

  function bindDrag() {
    stage.addEventListener("pointerdown", function (e) {
      if (e.target.closest(".orbit-card")) return;
      drag = { id: e.pointerId, x: e.clientX, angle: angle };
      spinning = false;
      stage.classList.add("is-dragging");
      stage.setPointerCapture(e.pointerId);
    });

    stage.addEventListener("pointermove", function (e) {
      if (!drag || e.pointerId !== drag.id) return;
      angle = drag.angle + (e.clientX - drag.x) * 0.28;
    });

    function endDrag(e) {
      if (!drag || (e && e.pointerId !== drag.id)) return;
      drag = null;
      stage.classList.remove("is-dragging");
      spinning = true;
    }

    stage.addEventListener("pointerup", endDrag);
    stage.addEventListener("pointercancel", endDrag);

    stage.addEventListener("mouseover", function (e) {
      const card = e.target.closest(".orbit-card");
      if (!card) return;
      spinning = false;
      stage.querySelectorAll(".orbit-card.is-front").forEach(function (c) {
        c.classList.remove("is-front");
      });
      card.classList.add("is-front");
    });

    stage.addEventListener("mouseout", function (e) {
      const card = e.target.closest(".orbit-card");
      if (card && (!e.relatedTarget || !card.contains(e.relatedTarget))) {
        card.classList.remove("is-front");
        if (!drag) spinning = true;
      }
    });
  }

  bindDrag();

  let resizeTimer = 0;
  window.addEventListener("resize", function () {
    clearTimeout(resizeTimer);
    resizeTimer = setTimeout(function () {
      if (products.length) render();
    }, 150);
  });

  fetch(API_BASE + "/web/products", { credentials: "include" })
    .then(function (res) {
      if (!res.ok) throw new Error("Products failed");
      return res.json();
    })
    .then(function (data) {
      products = data.data || [];
      render();
      if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
        spinning = false;
        applyTransform();
      } else {
        requestAnimationFrame(tick);
      }
    })
    .catch(function (err) {
      console.warn("Home orbit failed:", err);
      section.hidden = true;
    });
})();
