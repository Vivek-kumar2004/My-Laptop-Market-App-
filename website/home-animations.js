/**
 * Homepage animations — optimized for performance
 */
(function () {
  "use strict";

  const prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  const isMobile = window.matchMedia("(max-width: 768px)").matches;
  const enableHeavyEffects = !prefersReducedMotion && !isMobile;

  /* ── Hamburger menu ── */
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

  /* ── Navbar scroll (throttled) ── */
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

  if (typeof gsap === "undefined") return;
  gsap.registerPlugin(ScrollTrigger);

  /* Lenis disabled — main cause of scroll lag on many devices */

  /* ── Hero slider ── */
  const slides = document.querySelectorAll(".hero > .slide");
  const copySlides = document.querySelectorAll(".hero-copy-slides .hero-content");
  const dots = document.querySelectorAll(".hero-dot");
  let currentSlide = 0;
  let slideInterval = null;

  function animateHeroContent(content) {
    const els = content.querySelectorAll(".hero-title, .hero-subtitle");
    gsap.fromTo(
      els,
      { opacity: 0, y: 30 },
      {
        opacity: 1,
        y: 0,
        duration: enableHeavyEffects ? 0.7 : 0.3,
        stagger: 0.12,
        ease: "power2.out",
      }
    );
  }

  function goToSlide(index) {
    if (index === currentSlide || !slides.length) return;

    const prev = slides[currentSlide];
    const next = slides[index];
    const prevCopy = copySlides[currentSlide];
    const nextCopy = copySlides[index];
    const prevText = prevCopy
      ? prevCopy.querySelectorAll(".hero-title, .hero-subtitle")
      : [];

    gsap.to(prevText, {
      opacity: 0,
      y: -15,
      duration: 0.4,
      ease: "power2.in",
      onComplete: () => {
        prev.classList.remove("active");
        next.classList.add("active");
        if (prevCopy) prevCopy.classList.remove("active");
        if (nextCopy) nextCopy.classList.add("active");
        if (nextCopy) animateHeroContent(nextCopy);
      },
    });

    dots.forEach((d, i) => d.classList.toggle("active", i === index));
    currentSlide = index;
  }

  function startSlider() {
    if (slideInterval) clearInterval(slideInterval);
    if (document.hidden) return;
    slideInterval = setInterval(() => {
      goToSlide((currentSlide + 1) % slides.length);
    }, 6000);
  }

  function stopSlider() {
    if (slideInterval) clearInterval(slideInterval);
    slideInterval = null;
  }

  if (slides.length) {
    copySlides.forEach((content, i) => {
      if (i !== 0) {
        gsap.set(content.querySelectorAll(".hero-title, .hero-subtitle"), {
          opacity: 0,
        });
      }
    });
    if (copySlides[0]) animateHeroContent(copySlides[0]);

    const permanentCta = document.querySelector(".hero-btn-permanent");
    if (permanentCta) {
      gsap.fromTo(
        permanentCta,
        { autoAlpha: 0 },
        { autoAlpha: 1, duration: 0.6, delay: 0.4, ease: "power2.out" }
      );
    }

    startSlider();

    document.addEventListener("visibilitychange", () => {
      document.hidden ? stopSlider() : startSlider();
    });

    dots.forEach((dot, i) => {
      dot.addEventListener("click", () => {
        goToSlide(i);
        startSlider();
      });
    });
  }

  /* ── Scroll reveals — one trigger per section, not per card ── */
  if (!prefersReducedMotion) {
    gsap.utils.toArray("[data-reveal]").forEach((el) => {
      gsap.from(el, {
        scrollTrigger: { trigger: el, start: "top 90%", once: true },
        opacity: 0,
        y: 40,
        duration: 0.6,
        ease: "power2.out",
      });
    });

    document.querySelectorAll(
        ".about, .laptop-solutions, .quick-assistance, .features, .quality-guarantee, .services-home, .testimonials, .home-showcase, .home-vendors"
    ).forEach((section) => {
      const cards = section.querySelectorAll(
        ".glass-card, .assistance-card, .feature-card, .guarantee-card, .service-card, .home-product-card, .home-vendor-card"
      );
      if (!cards.length) return;

      gsap.from(cards, {
        scrollTrigger: { trigger: section, start: "top 85%", once: true },
        opacity: 0,
        y: 30,
        duration: 0.5,
        stagger: 0.06,
        ease: "power2.out",
      });
    });
  }

  /* ── GSAP counters ── */
  document.querySelectorAll(".counter").forEach((counter) => {
    const target = +counter.getAttribute("data-target");
    const obj = { val: 0 };

    ScrollTrigger.create({
      trigger: counter,
      start: "top 85%",
      once: true,
      onEnter: () => {
        gsap.to(obj, {
          val: target,
          duration: 1.5,
          ease: "power2.out",
          onUpdate: () => {
            counter.textContent = Math.floor(obj.val);
          },
        });
      },
    });
  });

  /* ── FAQ accordion ── */
  document.querySelectorAll(".faq-item").forEach((item) => {
    const btn = item.querySelector(".faq-btn");
    const content = item.querySelector(".faq-content");
    if (!btn || !content) return;

    gsap.set(content, { height: 0 });

    btn.addEventListener("click", () => {
      const isOpen = item.classList.contains("open");

      document.querySelectorAll(".faq-item.open").forEach((openItem) => {
        if (openItem !== item) {
          openItem.classList.remove("open");
          gsap.to(openItem.querySelector(".faq-content"), {
            height: 0,
            duration: 0.35,
            ease: "power2.inOut",
          });
        }
      });

      if (isOpen) {
        item.classList.remove("open");
        gsap.to(content, { height: 0, duration: 0.35, ease: "power2.inOut" });
      } else {
        item.classList.add("open");
        gsap.to(content, { height: "auto", duration: 0.4, ease: "power2.out" });
      }
    });
  });

  /* ── Testimonial slider ── */
  const track = document.querySelector(".testimonial-track");
  const cards = document.querySelectorAll(".testimonial-card");
  let tIndex = 0;
  let testimonialInterval = null;

  function getVisibleCards() {
    if (window.innerWidth <= 426) return 1;
    if (window.innerWidth <= 769) return 2;
    return 4;
  }

  function slideTestimonials() {
    if (!track || !cards.length || document.hidden) return;
    const visible = getVisibleCards();
    tIndex++;
    if (tIndex > cards.length - visible) tIndex = 0;
    const cardWidth = cards[0].offsetWidth + 20;
    gsap.to(track, { x: -tIndex * cardWidth, duration: 0.6, ease: "power2.inOut" });
  }

  if (track && cards.length) {
    testimonialInterval = setInterval(slideTestimonials, 5000);
    document.addEventListener("visibilitychange", () => {
      if (document.hidden) {
        clearInterval(testimonialInterval);
      } else {
        testimonialInterval = setInterval(slideTestimonials, 5000);
      }
    });
    window.addEventListener("resize", () => {
      tIndex = 0;
      gsap.set(track, { x: 0 });
    }, { passive: true });
  }
})();
