import Image from "next/image";
import type { ReactNode } from "react";
import { ChevronRight, Handshake } from "lucide-react";

const quickLinks = [
  ["Home", "/"],
  ["About Us", "/about.html"],
  ["Services", "/services.html"],
  ["Products", "/products.html"],
  ["Why Us", "/why-us.html"],
  ["Contact Us", "/contact.html"],
] as const;

const businessNetwork = [
  ["Vendor Registration", "https://mylaptopmarket.com/api/vendor/register"],
  ["Supplier & Dealer Discovery", "/services.html"],
  ["B2B Lead Generation", "/services.html"],
  ["Business Matchmaking", "/services.html"],
  ["Corporate Asset Sourcing", "/services.html"],
  ["Industry Collaboration", "/services.html"],
] as const;

const itCategories = [
  ["Laptops & Desktops", "/products.html?category=Laptops"],
  ["Networking Equipment", "/products.html"],
  ["Servers & Workstations", "/products.html?category=Servers"],
  ["Monitors & Accessories", "/products.html?category=Monitors"],
  ["CCTV & Security", "/products.html"],
  ["IT Asset Management", "/services.html"],
] as const;

export function SiteFooter() {
  return (
    <footer className="mt-8 border-t border-[rgba(2,20,79,0.08)] bg-white text-ink">
      <section className="px-0 pt-12 pb-8">
        <div className="mx-auto w-[min(100%-40px,1180px)]">
          <div className="mb-10 flex flex-wrap items-start justify-between gap-8">
            <div className="max-w-[340px]">
              <Image
                src="/images/mylaptoplogo.png"
                alt="My Laptop Market"
                width={200}
                height={56}
                className="mb-3 h-14 w-auto"
              />
              <p className="text-[13px] leading-relaxed text-ink-2">
                India&apos;s B2B network for IT vendors, dealers, suppliers and technology
                businesses.
              </p>
            </div>

            <div className="w-full max-w-[360px]">
              <h3 className="mb-3 text-[15px] font-bold text-navy">
                Newsletter
                <span className="mt-1.5 block h-[3px] w-10 rounded-full bg-green" />
              </h3>
              <form
                className="mb-2 flex flex-wrap gap-2"
                action="mailto:support@mylaptopmarket.com"
                method="get"
              >
                <input
                  type="email"
                  name="subject"
                  required
                  aria-label="Email address"
                  placeholder="Enter email address"
                  className="h-11 min-w-[180px] flex-1 rounded-full border border-[rgba(2,20,79,0.12)] bg-white px-4 text-sm outline-none focus:border-brand focus:shadow-[0_0_0_3px_rgba(7,103,216,0.12)]"
                />
                <button
                  type="submit"
                  className="inline-flex h-11 items-center rounded-full bg-green px-5 text-sm font-bold text-white transition hover:brightness-95"
                >
                  Subscribe
                </button>
              </form>
              <p className="text-[12px] leading-snug text-ink-3">
                Get the latest updates on IT products, market trends and exclusive B2B offers.
              </p>
            </div>
          </div>

          <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
            <FooterLinks title="Quick Links" items={quickLinks} />
            <FooterLinks title="Business Network" items={businessNetwork} />
            <FooterLinks title="Explore IT Categories" items={itCategories} />

            <div>
              <h3 className="mb-3 text-[15px] font-bold text-navy">
                Connect With Us
                <span className="mt-1.5 block h-[3px] w-10 rounded-full bg-green" />
              </h3>
              <p className="mb-4 text-[13px] leading-relaxed text-ink-2">
                Be a part of India&apos;s growing IT business network.
              </p>
              <div className="mb-4 flex gap-2.5">
                <SocialLink
                  href="https://www.linkedin.com/company/my-laptop-market/?viewAsMember=true"
                  label="LinkedIn"
                  className="bg-[#0A66C2] text-white"
                >
                  <BrandIcon path="M4.98 3.5C4.98 4.88 3.88 6 2.5 6S0 4.88 0 3.5 1.12 1 2.5 1s2.48 1.12 2.48 2.5zM.5 8.5h4V23h-4V8.5zM8.5 8.5h3.8v2h.05c.53-1 1.84-2.05 3.8-2.05 4.06 0 4.8 2.67 4.8 6.15V23h-4v-6.6c0-1.57-.03-3.6-2.2-3.6-2.2 0-2.54 1.72-2.54 3.5V23h-4V8.5z" />
                </SocialLink>
                <SocialLink href="#" label="YouTube" className="bg-[#FF0000] text-white">
                  <BrandIcon path="M23.5 6.2a3 3 0 0 0-2.1-2.1C19.5 3.5 12 3.5 12 3.5s-7.5 0-9.4.6A3 3 0 0 0 .5 6.2 31.5 31.5 0 0 0 0 12a31.5 31.5 0 0 0 .5 5.8 3 3 0 0 0 2.1 2.1c1.9.6 9.4.6 9.4.6s7.5 0 9.4-.6a3 3 0 0 0 2.1-2.1A31.5 31.5 0 0 0 24 12a31.5 31.5 0 0 0-.5-5.8zM9.75 15.5v-7l6.5 3.5-6.5 3.5z" />
                </SocialLink>
                <SocialLink
                  href="#"
                  label="Instagram"
                  className="bg-[linear-gradient(45deg,#f58529,#dd2a7b,#8134af)] text-white"
                >
                  <BrandIcon path="M7 2h10a5 5 0 0 1 5 5v10a5 5 0 0 1-5 5H7a5 5 0 0 1-5-5V7a5 5 0 0 1 5-5zm10 2H7a3 3 0 0 0-3 3v10a3 3 0 0 0 3 3h10a3 3 0 0 0 3-3V7a3 3 0 0 0-3-3zm-5 3.5A5.5 5.5 0 1 1 6.5 13 5.5 5.5 0 0 1 12 7.5zm0 2A3.5 3.5 0 1 0 15.5 13 3.5 3.5 0 0 0 12 9.5zM17.5 6.75a1.25 1.25 0 1 1-1.25 1.25 1.25 1.25 0 0 1 1.25-1.25z" />
                </SocialLink>
                <SocialLink href="#" label="X" className="bg-black text-white">
                  <XIcon />
                </SocialLink>
              </div>
              <a
                href="https://mylaptopmarket.com/api/vendor/register"
                className="inline-flex h-11 w-full items-center justify-center gap-2 rounded-full border-2 border-green bg-white px-4 text-sm font-bold text-navy transition hover:bg-green-soft"
              >
                <Handshake className="size-4 text-green" />
                Partner With Us
              </a>
            </div>
          </div>
        </div>
      </section>

      <FooterNetworkArt />

      <div className="bg-[#02144f] px-5 py-3.5 text-[12px] text-white/90">
        <div className="mx-auto flex w-[min(100%,1180px)] flex-wrap items-center justify-between gap-3">
          <div>
            © 2026 <span className="font-semibold text-green">My Laptop Market.</span> All rights
            reserved.
          </div>
          <div className="flex flex-wrap items-center gap-x-2 gap-y-1">
            <a href="/privacy-policy.html" className="text-white hover:underline">
              Privacy Policy
            </a>
            <span className="text-white/40">|</span>
            <a href="/privacy-policy.html" className="text-white hover:underline">
              Terms &amp; Conditions
            </a>
            <span className="text-white/40">|</span>
            <a href="/contact.html" className="text-white hover:underline">
              Contact / Support
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
}

function FooterLinks({
  title,
  items,
}: {
  title: string;
  items: readonly (readonly [string, string])[];
}) {
  return (
    <div>
      <h3 className="mb-4 text-[15px] font-bold text-navy">{title}</h3>
      <ul className="space-y-2.5 text-[13px] text-ink-2">
        {items.map(([label, href]) => (
          <li key={label}>
            <a href={href} className="inline-flex items-center gap-1.5 transition hover:text-brand">
              <ChevronRight className="size-3 shrink-0 text-green" />
              {label}
            </a>
          </li>
        ))}
      </ul>
    </div>
  );
}

function SocialLink({
  href,
  label,
  className,
  children,
}: {
  href: string;
  label: string;
  className: string;
  children: ReactNode;
}) {
  return (
    <a
      href={href}
      aria-label={label}
      target={href.startsWith("http") ? "_blank" : undefined}
      rel={href.startsWith("http") ? "noopener noreferrer" : undefined}
      className={`grid size-9 place-items-center rounded-full transition hover:scale-105 ${className}`}
    >
      {children}
    </a>
  );
}

function BrandIcon({ path }: { path: string }) {
  return (
    <svg viewBox="0 0 24 24" className="size-3.5 fill-current" aria-hidden>
      <path d={path} />
    </svg>
  );
}

function XIcon() {
  return (
    <svg viewBox="0 0 24 24" className="size-3.5 fill-current" aria-hidden>
      <path d="M18.9 2H22l-6.8 7.78L23.2 22h-6.4l-5-6.55L6.1 22H3l7.3-8.35L.8 2h6.55l4.52 5.98L18.9 2zm-1.12 18h1.77L6.35 3.9H4.45L17.78 20z" />
    </svg>
  );
}

function FooterNetworkArt() {
  return (
    <div
      className="relative h-[150px] overflow-hidden bg-[linear-gradient(180deg,#f7fbff_0%,#e8f2fc_100%)] md:h-[190px]"
      aria-hidden
    >
      <svg
        viewBox="0 0 1440 220"
        className="absolute inset-x-0 bottom-0 mx-auto h-full w-full max-w-[1440px]"
        preserveAspectRatio="xMidYMax meet"
      >
        <defs>
          <pattern id="softDots" width="14" height="14" patternUnits="userSpaceOnUse">
            <circle cx="1" cy="1" r="1" fill="rgba(7,103,216,0.12)" />
          </pattern>
        </defs>

        <rect width="1440" height="220" fill="url(#softDots)" opacity="0.5" />

        {/* faint landmarks */}
        <g fill="rgba(7,103,216,0.12)">
          <path d="M70 200h110v-55H70z" />
          <path d="M90 145h70v-30H90z" />
          <path d="M112 95h26v20h-26z" />
          <path d="M122 72h6v23h-6z" />
          <path d="M1240 200h140v-45h-140z" />
          <path d="M1270 155c25-55 85-55 110 0H1270z" />
          <circle cx="1325" cy="98" r="9" />
        </g>

        {/* green network mesh */}
        <g fill="none" stroke="#20ca47" strokeWidth="1.6" strokeDasharray="4 5" opacity="0.85">
          <path d="M180 150 C320 80, 420 70, 520 120" />
          <path d="M520 120 C640 170, 760 40, 900 110" />
          <path d="M900 110 C1020 160, 1120 90, 1260 140" />
          <path d="M360 170 C480 40, 700 30, 860 150" />
          <path d="M260 90 C400 160, 600 180, 780 70" />
        </g>
        <g fill="#20ca47">
          {[
            [180, 150],
            [360, 170],
            [520, 120],
            [640, 70],
            [780, 70],
            [900, 110],
            [1040, 95],
            [1260, 140],
          ].map(([cx, cy], i) => (
            <circle key={i} cx={cx} cy={cy} r="4.5" />
          ))}
        </g>

        {/* hardware cluster */}
        <g transform="translate(250 95)">
          <LaptopMini />
        </g>
        <g transform="translate(470 70)">
          <ServerRack />
        </g>
        <g transform="translate(650 55)">
          <DesktopTower />
        </g>
        <g transform="translate(860 75)">
          <NetworkSwitch />
        </g>
        <g transform="translate(1060 95)">
          <LaptopMini flip />
        </g>
      </svg>
    </div>
  );
}

function LaptopMini({ flip }: { flip?: boolean }) {
  return (
    <g transform={flip ? "scale(-1,1) translate(-150,0)" : undefined}>
      <rect x="8" y="4" width="130" height="72" rx="7" fill="#0b1b3a" />
      <rect x="14" y="10" width="118" height="60" rx="4" fill="#e8f1fc" />
      <rect x="24" y="22" width="48" height="8" rx="2" fill="#0767d8" />
      <rect x="24" y="36" width="64" height="5" rx="2" fill="#93c5fd" />
      <rect x="24" y="48" width="52" height="5" rx="2" fill="#bfdbfe" />
      <rect x="0" y="76" width="146" height="11" rx="3" fill="#0b1b3a" />
    </g>
  );
}

function ServerRack() {
  return (
    <g>
      <rect x="0" y="10" width="88" height="110" rx="6" fill="#1e3a5f" />
      <rect x="8" y="20" width="72" height="18" rx="3" fill="#0b1b3a" />
      <rect x="8" y="46" width="72" height="18" rx="3" fill="#0b1b3a" />
      <rect x="8" y="72" width="72" height="18" rx="3" fill="#0b1b3a" />
      <circle cx="20" cy="29" r="3" fill="#20ca47" />
      <circle cx="20" cy="55" r="3" fill="#20ca47" />
      <circle cx="20" cy="81" r="3" fill="#0767d8" />
      <rect x="30" y="26" width="40" height="6" rx="2" fill="#64748b" />
      <rect x="30" y="52" width="40" height="6" rx="2" fill="#64748b" />
      <rect x="30" y="78" width="40" height="6" rx="2" fill="#64748b" />
    </g>
  );
}

function DesktopTower() {
  return (
    <g>
      <rect x="20" y="0" width="120" height="90" rx="6" fill="#e8f1fc" stroke="#0767d8" strokeWidth="3" />
      <rect x="32" y="12" width="96" height="55" rx="3" fill="#0b1b3a" />
      <rect x="40" y="22" width="40" height="6" rx="2" fill="#20ca47" />
      <rect x="40" y="34" width="55" height="4" rx="2" fill="#64748b" />
      <rect x="50" y="92" width="60" height="10" rx="2" fill="#1e3a5f" />
      <rect x="0" y="30" width="18" height="70" rx="3" fill="#1e3a5f" />
      <circle cx="9" cy="85" r="3" fill="#20ca47" />
    </g>
  );
}

function NetworkSwitch() {
  return (
    <g>
      <rect x="0" y="30" width="130" height="50" rx="6" fill="#0b1b3a" />
      {[0, 1, 2, 3, 4, 5].map((i) => (
        <g key={i}>
          <rect x={12 + i * 18} y="42" width="12" height="10" rx="1.5" fill="#0767d8" />
          <circle cx={18 + i * 18} cy="64" r="2.5" fill={i % 2 ? "#20ca47" : "#93c5fd"} />
        </g>
      ))}
    </g>
  );
}
