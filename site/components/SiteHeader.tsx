"use client";

import Image from "next/image";
import Link from "next/link";
import { ArrowRight, Menu, User, X } from "lucide-react";
import { useEffect, useState } from "react";

const links = [
  { href: "/", label: "Home", active: true },
  { href: "/about.html", label: "About" },
  { href: "/services.html", label: "Services" },
  { href: "/products.html", label: "Products" },
  { href: "/why-us.html", label: "Why Us" },
  { href: "/contact.html", label: "Contact Us" },
];

export function SiteHeader() {
  const [open, setOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 40);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  return (
    <header
      className={`sticky top-0 z-50 border-b border-line bg-white/92 backdrop-blur-md ${
        scrolled ? "shadow-[0_8px_24px_rgba(15,40,90,0.06)]" : ""
      }`}
    >
      <div className="mx-auto flex h-[76px] w-[min(100%-40px,1180px)] items-center gap-5">
        <Link href="/" className="shrink-0">
          <Image
            src="/images/mylaptoplogo.png"
            alt="My Laptop Market"
            width={160}
            height={48}
            className="h-12 w-auto rounded-[10px]"
            priority
          />
        </Link>

        <nav className="hidden flex-1 items-center justify-center gap-1.5 md:flex">
          {links.map((link) => (
            <a
              key={link.href}
              href={link.href}
              className={`relative inline-flex h-9 items-center rounded-lg px-3 text-sm font-semibold transition ${
                link.active
                  ? "text-brand"
                  : "text-ink-2 hover:bg-brand-soft hover:text-brand"
              }`}
            >
              {link.label}
              {link.active ? (
                <span className="absolute inset-x-3 bottom-1 h-0.5 rounded bg-brand" />
              ) : null}
            </a>
          ))}
        </nav>

        <div className="ml-auto hidden items-center gap-2.5 md:flex">
          <a
            id="vendorNavBtn"
            href="https://mylaptopmarket.com/api/vendor/login"
            className="inline-flex h-[42px] items-center gap-2 rounded-full border-[1.5px] border-brand/45 bg-white px-[18px] text-sm font-bold text-brand transition hover:-translate-y-px hover:bg-brand-soft"
          >
            <User className="size-4" />
            Vendor Login
          </a>
          <a
            id="heroRegisterBtn"
            href="https://mylaptopmarket.com/api/vendor/register"
            className="inline-flex h-[42px] items-center gap-2 rounded-full bg-brand px-[18px] text-sm font-bold text-white shadow-[0_10px_24px_rgba(7,103,216,0.28)] transition hover:-translate-y-px hover:bg-brand-dark"
          >
            Register as Dealer
            <ArrowRight className="size-4" />
          </a>
        </div>

        <button
          type="button"
          className="ml-auto grid size-[42px] place-items-center rounded-[10px] border border-line bg-white text-navy md:hidden"
          aria-label={open ? "Close menu" : "Open menu"}
          aria-expanded={open}
          onClick={() => setOpen((v) => !v)}
        >
          {open ? <X className="size-5" /> : <Menu className="size-5" />}
        </button>
      </div>

      {open ? (
        <div className="border-t border-line bg-white px-4 py-3 shadow-lg md:hidden">
          <div className="flex flex-col gap-1">
            {links.map((link) => (
              <a
                key={link.href}
                href={link.href}
                className="rounded-lg px-3.5 py-3 text-sm font-semibold text-ink-2"
                onClick={() => setOpen(false)}
              >
                {link.label}
              </a>
            ))}
            <a
              id="mobileVendorNavLink"
              href="https://mylaptopmarket.com/api/vendor/login"
              className="mt-2 inline-flex h-11 items-center justify-center rounded-full bg-brand text-sm font-bold text-white"
            >
              Vendor Login
            </a>
          </div>
        </div>
      ) : null}
    </header>
  );
}
