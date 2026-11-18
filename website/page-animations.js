/**
 * Inner pages — scroll reveals, FAQ, counters, navbar
 */
(function () {
  "use strict";

  const hamburger = document.getElementById("hamburger");
  const navLinks = document.getElementById("navLinks");
  if (hamburger && navLinks) {
    hamburger.addEventListener("click", (e) => {
      e.preventDefault();
      e.stopPropagation();
      const open = navLinks.classList.toggle("active");
      document.body.classList.toggle("nav-open", open);
      hamburger.setAttribute("aria-expanded", open ? "true" : "false");
    });

    navLinks.querySelectorAll("a").forEach((link) => {
      link.addEventListener("click", () => {
        navLinks.classList.remove("active");
        document.body.classList.remove("nav-open");
        hamburger.setAttribute("aria-expanded", "false");
      });
    });
  }

  const navbar = document.querySelector(".navbar");
  if (navbar) {
    let ticking = false;
    const onScroll = () => {
      if (ticking) return;
      ticking = true;
      requestAnimationFrame(() => {
        navbar.classList.toggle("scrolled", window.scrollY > 40);
        ticking = false;
      });
    };
    window.addEventListener("scroll", onScroll, { passive: true });
    onScroll();
  }

  document.querySelectorAll(".faq-btn").forEach((button) => {
    button.addEventListener("click", () => {
      const item = button.closest(".faq-item");
      const content = button.nextElementSibling;
      const isOpen = item && item.classList.contains("open");

      document.querySelectorAll(".faq-item").forEach((el) => {
        el.classList.remove("open");
        const c = el.querySelector(".faq-content");
        if (c) c.classList.remove("active");
      });

      if (!isOpen && item && content) {
        item.classList.add("open");
        content.classList.add("active");
      }
    });
  });

  if (typeof gsap === "undefined" || typeof ScrollTrigger === "undefined") return;
  gsap.registerPlugin(ScrollTrigger);

  const prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  if (prefersReducedMotion) return;

  gsap.utils.toArray("[data-reveal]").forEach((el) => {
    gsap.from(el, {
      scrollTrigger: { trigger: el, start: "top 90%", once: true },
      opacity: 0,
      y: 36,
      duration: 0.65,
      ease: "power2.out",
    });
  });

  document.querySelectorAll(".feature-card, .service-card, .info-box, .glass-card").forEach((card) => {
    gsap.from(card, {
      scrollTrigger: { trigger: card, start: "top 88%", once: true },
      opacity: 0,
      y: 28,
      duration: 0.55,
      ease: "power2.out",
    });
  });

  document.querySelectorAll(".counter").forEach((counter) => {
    const target = +counter.getAttribute("data-target");
    if (!target) return;
    const obj = { val: 0 };
    ScrollTrigger.create({
      trigger: counter,
      start: "top 85%",
      once: true,
      onEnter: () => {
        gsap.to(obj, {
          val: target,
          duration: 2,
          ease: "power2.out",
          onUpdate: () => {
            counter.textContent = Math.floor(obj.val) + (counter.textContent.includes("+") ? "+" : "");
          },
        });
      },
    });
  });
})();
