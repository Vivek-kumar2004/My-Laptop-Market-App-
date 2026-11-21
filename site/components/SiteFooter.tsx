import Image from "next/image";
import type { ReactNode } from "react";
import {
  ArrowRight,
  BarChart3,
  ChevronRight,
  Handshake,
  Headset,
  LayoutGrid,
  Mail,
  MapPin,
  Phone,
  Shield,
} from "lucide-react";

export function SiteFooter() {
  return (
    <footer className="relative overflow-hidden border-t border-line bg-white text-ink">
      <div
        className="pointer-events-none absolute inset-x-0 bottom-[44px] top-[120px] bg-[linear-gradient(180deg,#ffffff_0%,#f3f8ff_42%,#e6f0fc_100%)]"
        aria-hidden
      />

      <section className="relative z-[1] px-0 pt-12 pb-6">
        <div className="mx-auto grid w-[min(100%-40px,1180px)] gap-8 md:grid-cols-2 lg:grid-cols-[1.35fr_0.95fr_1.05fr_1.15fr_0.95fr]">
          <div>
            <Image
              src="/images/mylaptoplogo.png"
              alt="My Laptop Market"
              width={168}
              height={50}
              className="mb-3.5 h-[50px] w-auto"
            />
            <p className="mb-4 max-w-[30ch] text-[13px] leading-relaxed text-ink-2">
              India&apos;s trusted B2B marketplace for laptops and IT hardware. Connect, trade and
              grow with confidence.
            </p>
            <strong className="mb-2 block text-[13px] font-bold text-navy">Download Our App</strong>
            <div className="mb-4 flex flex-wrap items-center gap-2">
              <a
                href="https://play.google.com/store/apps/details?id=com.mylaptopmarket"
                target="_blank"
                rel="noopener noreferrer"
              >
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  src="/images/google-play-badge.png"
                  alt="Get it on Google Play"
                  className="h-[44px] w-auto"
                />
              </a>
              <a
                href="https://apps.apple.com/in/app/my-laptop-market/id6811815946"
                target="_blank"
                rel="noopener noreferrer"
              >
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  src="/images/app-store-badge.svg"
                  alt="Download on the App Store"
                  className="h-[34px] w-auto"
                />
              </a>
            </div>
            <div className="flex gap-2">
              <SocialLink
                href="https://www.linkedin.com/company/my-laptop-market/?viewAsMember=true"
                label="LinkedIn"
              >
                <BrandIcon path="M4.98 3.5C4.98 4.88 3.88 6 2.5 6S0 4.88 0 3.5 1.12 1 2.5 1s2.48 1.12 2.48 2.5zM.5 8.5h4V23h-4V8.5zM8.5 8.5h3.8v2h.05c.53-1 1.84-2.05 3.8-2.05 4.06 0 4.8 2.67 4.8 6.15V23h-4v-6.6c0-1.57-.03-3.6-2.2-3.6-2.2 0-2.54 1.72-2.54 3.5V23h-4V8.5z" />
              </SocialLink>
              <SocialLink href="#" label="YouTube">
                <BrandIcon path="M23.5 6.2a3 3 0 0 0-2.1-2.1C19.5 3.5 12 3.5 12 3.5s-7.5 0-9.4.6A3 3 0 0 0 .5 6.2 31.5 31.5 0 0 0 0 12a31.5 31.5 0 0 0 .5 5.8 3 3 0 0 0 2.1 2.1c1.9.6 9.4.6 9.4.6s7.5 0 9.4-.6a3 3 0 0 0 2.1-2.1A31.5 31.5 0 0 0 24 12a31.5 31.5 0 0 0-.5-5.8zM9.75 15.5v-7l6.5 3.5-6.5 3.5z" />
              </SocialLink>
              <SocialLink href="#" label="X">
                <XIcon />
              </SocialLink>
              <SocialLink href="#" label="Facebook">
                <BrandIcon path="M14 9h3V6h-3c-2.2 0-4 1.8-4 4v2H7v3h3v7h3v-7h3l1-3h-4v-2c0-.6.4-1 1-1z" />
              </SocialLink>
            </div>
          </div>

          <div>
            <h3 className="mb-4 flex items-center gap-2 text-sm font-bold text-navy">
              <LayoutGrid className="size-4 text-brand" /> Quick Links
            </h3>
            <ul className="space-y-3 text-[13px] text-ink-2">
              {[
                ["Home", "/"],
                ["About Us", "/about.html"],
                ["Services", "/services.html"],
                ["Products", "/products.html"],
                ["Why Us", "/why-us.html"],
                ["Contact Us", "/contact.html"],
              ].map(([label, href]) => (
                <li key={label}>
                  <a
                    href={href}
                    className="flex items-center justify-between gap-2 transition hover:text-brand"
                  >
                    {label}
                    <ChevronRight className="size-3 text-ink-3" />
                  </a>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h3 className="mb-4 flex items-center gap-2 text-sm font-bold text-navy">
              <Headset className="size-4 text-brand" /> Our Services
            </h3>
            <ul className="space-y-3 text-[13px] text-ink-2">
              {[
                "Vendor Registration",
                "Supplier & Dealer Discovery",
                "B2B Lead Generation",
                "Business Matchmaking",
              ].map((label) => (
                <li key={label}>
                  <a
                    href="/services.html"
                    className="flex items-center justify-between gap-2 transition hover:text-brand"
                  >
                    {label}
                    <ChevronRight className="size-3 text-ink-3" />
                  </a>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h3 className="mb-4 flex items-center gap-2 text-sm font-bold text-navy">
              <Handshake className="size-4 text-brand" /> Get in Touch
            </h3>
            <div className="grid gap-4">
              <ContactRow
                icon={<Phone className="size-3.5" />}
                strong="+91 97181 99976"
                small="Mon - Sat, 9 AM - 7 PM"
                href="tel:+919718199976"
              />
              <ContactRow
                icon={<Mail className="size-3.5" />}
                strong="support@mylaptopmarket.com"
                href="mailto:support@mylaptopmarket.com"
              />
              <ContactRow
                icon={<MapPin className="size-3.5" />}
                strong="New Delhi, India 110091"
                href="https://www.google.com/maps/search/?api=1&query=Plot+no+19+Office+no+5,+Veer+Savarkar+Block,+Shakarpur+110091,+Delhi"
              />
            </div>
          </div>

          <div className="h-fit rounded-2xl border border-[rgba(7,103,216,0.14)] bg-[#eaf2fc] p-5">
            <div className="mb-3 grid size-11 place-items-center rounded-xl bg-white text-green shadow-[0_4px_12px_rgba(15,40,90,0.06)]">
              <Shield className="size-5" />
            </div>
            <strong className="mb-3 block text-[15px] text-navy">Trusted Platform</strong>
            <ul className="mb-4 space-y-2 text-[13px] text-ink-2">
              {["Verified Businesses", "Secure Transactions", "Pan India Network"].map((item) => (
                <li
                  key={item}
                  className="relative pl-3.5 before:absolute before:left-0 before:font-bold before:text-green before:content-['•']"
                >
                  {item}
                </li>
              ))}
            </ul>
            <a
              href="https://mylaptopmarket.com/api/vendor/register"
              className="inline-flex h-11 w-full items-center justify-center gap-2 rounded-full border-[1.5px] border-brand bg-white px-4 text-sm font-bold text-brand transition hover:bg-brand hover:text-white"
            >
              Partner With Us <ArrowRight className="size-4" />
            </a>
          </div>
        </div>
      </section>

      <FooterSkyline />

      <div className="relative z-[2] flex flex-wrap items-center justify-between gap-3 bg-[#02144f] px-5 py-3.5 text-[12px] text-white/85">
        <div className="mx-auto flex w-[min(100%,1180px)] flex-wrap items-center justify-between gap-3">
          <div>
            © 2026 My Laptop Market. All Rights Reserved. |{" "}
            <a href="/privacy-policy.html" className="text-white hover:underline">
              Privacy Policy
            </a>{" "}
            |{" "}
            <a href="/privacy-policy.html" className="text-white hover:underline">
              Terms &amp; Conditions
            </a>
          </div>
          <div className="flex flex-wrap items-center gap-4 font-semibold">
            <span className="inline-flex items-center gap-1.5">
              <Shield className="size-3.5 text-green" /> Secure &amp; Reliable
            </span>
            <MakeInIndiaBadge />
            <span className="inline-flex items-center gap-1.5">
              <BarChart3 className="size-3.5 text-green" /> B2B Trusted Partner
            </span>
          </div>
        </div>
      </div>
    </footer>
  );
}

function SocialLink({
  href,
  label,
  children,
}: {
  href: string;
  label: string;
  children: ReactNode;
}) {
  return (
    <a
      href={href}
      aria-label={label}
      target={href.startsWith("http") ? "_blank" : undefined}
      rel={href.startsWith("http") ? "noopener noreferrer" : undefined}
      className="grid size-8 place-items-center rounded-full bg-brand-soft text-brand transition hover:bg-brand hover:text-white"
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

function ContactRow({
  icon,
  strong,
  small,
  href,
}: {
  icon: ReactNode;
  strong: string;
  small?: string;
  href: string;
}) {
  return (
    <a
      href={href}
      className="flex items-start gap-3"
      target={href.startsWith("http") ? "_blank" : undefined}
      rel={href.startsWith("http") ? "noopener noreferrer" : undefined}
    >
      <span className="mt-0.5 grid size-8 shrink-0 place-items-center rounded-lg bg-brand-soft text-brand">
        {icon}
      </span>
      <span>
        <strong className="block text-[13px] font-bold text-ink">{strong}</strong>
        {small ? <small className="mt-0.5 block text-[11px] text-ink-3">{small}</small> : null}
      </span>
    </a>
  );
}

function MakeInIndiaBadge() {
  return (
    <span className="inline-flex items-center gap-1.5" title="Make in India">
      <svg viewBox="0 0 28 28" className="size-5" aria-hidden>
        <circle cx="14" cy="14" r="13" fill="none" stroke="#ff9933" strokeWidth="1.5" />
        <path
          d="M14 5c2.5 3 4 6.2 4 9.2 0 2.4-1.6 4.3-4 4.3s-4-1.9-4-4.3C10 11.2 11.5 8 14 5z"
          fill="#ff9933"
        />
        <path d="M10.5 18.5c1.2 2.2 2.4 3.2 3.5 3.5 1.1-.3 2.3-1.3 3.5-3.5" fill="none" stroke="#138808" strokeWidth="1.4" />
      </svg>
      <span className="text-[11px] leading-tight">
        <span className="block font-extrabold tracking-wide text-[#ff9933]">MAKE IN</span>
        <span className="block font-extrabold tracking-wide text-[#138808]">INDIA</span>
      </span>
    </span>
  );
}

function FooterSkyline() {
  return (
    <div className="relative z-0 -mt-2 h-[168px] overflow-hidden md:h-[210px]" aria-hidden>
      <svg
        viewBox="0 0 1440 260"
        className="absolute inset-x-0 bottom-0 mx-auto h-full w-full max-w-[1400px]"
        preserveAspectRatio="xMidYMax meet"
      >
        <defs>
          <pattern id="dotMap" width="10" height="10" patternUnits="userSpaceOnUse">
            <circle cx="1.5" cy="1.5" r="1.2" fill="rgba(7,103,216,0.22)" />
          </pattern>
          <linearGradient id="skyFade" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#ffffff" stopOpacity="0" />
            <stop offset="35%" stopColor="#eef5ff" stopOpacity="0.65" />
            <stop offset="100%" stopColor="#d9e8fb" stopOpacity="1" />
          </linearGradient>
        </defs>

        <rect x="0" y="0" width="1440" height="260" fill="url(#skyFade)" />

        {/* dotted India map silhouette */}
        <g opacity="0.55">
          <path
            d="M520 40c55-28 150-36 210-8 48 22 95 10 130-12 42-26 110-8 130 32 22 45-8 95-52 118-62 32-165 28-245 8-70-18-128-52-140-95-6-22 0-34-33-43z"
            fill="url(#dotMap)"
          />
        </g>

        {/* left landmarks */}
        <g fill="rgba(7,103,216,0.16)" transform="translate(40 70)">
          <rect x="20" y="90" width="28" height="70" rx="2" />
          <rect x="56" y="70" width="34" height="90" rx="2" />
          <rect x="98" y="95" width="24" height="65" rx="2" />
          <path d="M150 165h90v-55h-90z" />
          <path d="M168 110h54v-28h-54z" />
          <path d="M182 62h26v20h-26z" />
          <path d="M192 40h6v22h-6z" />
          <circle cx="195" cy="36" r="5" />
        </g>

        {/* right Taj-like */}
        <g fill="rgba(7,103,216,0.15)" transform="translate(1120 85)">
          <path d="M20 145h220v-40H20z" />
          <path d="M70 105c30-70 110-70 140 0H70z" />
          <rect x="120" y="55" width="30" height="28" rx="2" />
          <circle cx="135" cy="48" r="10" />
          <rect x="40" y="90" width="18" height="55" rx="2" />
          <rect x="202" y="90" width="18" height="55" rx="2" />
        </g>

        {/* soft ground glow */}
        <ellipse cx="720" cy="245" rx="420" ry="48" fill="rgba(7,103,216,0.08)" />

        {/* three laptops */}
        <g transform="translate(430 95)">
          <Laptop x={0} y={28} scale={0.92} />
          <Laptop x={170} y={0} scale={1.12} accent />
          <Laptop x={370} y={30} scale={0.95} />
        </g>
      </svg>
    </div>
  );
}

function Laptop({
  x,
  y,
  scale = 1,
  accent,
}: {
  x: number;
  y: number;
  scale?: number;
  accent?: boolean;
}) {
  const body = accent ? "#0b1b3a" : "#1e3a5f";
  const lid = accent ? "#0767d8" : "#3b82f6";
  return (
    <g transform={`translate(${x} ${y}) scale(${scale})`}>
      <rect x="8" y="6" width="168" height="98" rx="10" fill={body} />
      <rect x="16" y="14" width="152" height="82" rx="5" fill="#eef5ff" />
      <rect x="28" y="28" width="58" height="10" rx="3" fill={lid} />
      <rect x="28" y="46" width="78" height="7" rx="2" fill="#93c5fd" />
      <rect x="28" y="60" width="64" height="7" rx="2" fill="#bfdbfe" />
      <rect
        x="122"
        y="30"
        width="34"
        height="48"
        rx="5"
        fill="#fff"
        stroke={lid}
        strokeWidth="2.5"
      />
      <circle cx="139" cy="48" r="6" fill={accent ? "#20ca47" : "#0767d8"} opacity="0.85" />
      <rect x="0" y="104" width="184" height="14" rx="4" fill={body} />
      <rect x="62" y="108" width="60" height="5" rx="2" fill="#64748b" />
      <ellipse cx="92" cy="126" rx="70" ry="8" fill="rgba(2,20,79,0.12)" />
    </g>
  );
}
