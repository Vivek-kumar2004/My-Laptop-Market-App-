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
    <footer className="mt-6 border-t border-line bg-white text-ink">
      <section className="bg-[radial-gradient(ellipse_45%_55%_at_80%_100%,rgba(7,103,216,0.07),transparent_60%)] px-0 pt-14 pb-8">
        <div className="mx-auto grid w-[min(100%-40px,1180px)] gap-7 md:grid-cols-2 lg:grid-cols-[1.5fr_1fr_1fr_1.2fr_0.9fr]">
          <div>
            <Image
              src="/images/mylaptoplogo.png"
              alt="My Laptop Market"
              width={160}
              height={48}
              className="mb-3 h-12 w-auto rounded-[10px]"
            />
            <p className="mb-4 max-w-[28ch] text-[13px] leading-relaxed text-ink-2">
              India&apos;s trusted B2B marketplace for laptops and IT hardware. Connect, trade and
              grow with confidence.
            </p>
            <strong className="mb-2.5 block text-[13px] font-bold text-navy">Download Our App</strong>
            <div className="mb-3.5 flex flex-wrap gap-2">
              <a
                href="https://play.google.com/store/apps/details?id=com.mylaptopmarket"
                target="_blank"
                rel="noopener noreferrer"
              >
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  src="/images/google-play-badge.png"
                  alt="Get it on Google Play"
                  className="h-[52px] w-auto"
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
                  className="h-9 w-auto"
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
            <h3 className="mb-3.5 flex items-center gap-2 text-sm font-bold text-navy">
              <LayoutGrid className="size-4 text-brand" /> Quick Links
            </h3>
            <ul className="space-y-2.5 text-[13px] text-ink-2">
              {[
                ["Home", "/"],
                ["About Us", "/about.html"],
                ["Services", "/services.html"],
                ["Products", "/products.html"],
                ["Why Us", "/why-us.html"],
                ["Contact Us", "/contact.html"],
              ].map(([label, href]) => (
                <li key={label}>
                  <a href={href} className="flex items-center justify-between gap-2 hover:text-brand">
                    {label}
                    <ChevronRight className="size-2.5 text-ink-3" />
                  </a>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h3 className="mb-3.5 flex items-center gap-2 text-sm font-bold text-navy">
              <Headset className="size-4 text-brand" /> Our Services
            </h3>
            <ul className="space-y-2.5 text-[13px] text-ink-2">
              {[
                "Vendor Registration",
                "Supplier & Dealer Discovery",
                "B2B Lead Generation",
                "Business Matchmaking",
              ].map((label) => (
                <li key={label}>
                  <a
                    href="/services.html"
                    className="flex items-center justify-between gap-2 hover:text-brand"
                  >
                    {label}
                    <ChevronRight className="size-2.5 text-ink-3" />
                  </a>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h3 className="mb-3.5 flex items-center gap-2 text-sm font-bold text-navy">
              <Handshake className="size-4 text-brand" /> Get in Touch
            </h3>
            <div className="grid gap-3">
              <ContactRow
                icon={<Phone className="size-3" />}
                small="Sales assistant"
                strong="Mr Shaizan Siddqui"
                em="9718199976"
                href="tel:+919718199976"
              />
              <ContactRow
                icon={<Phone className="size-3" />}
                small="Technical assistant"
                strong="Mr Vivek Kumar"
                em="7295834965"
                href="tel:+917295834965"
              />
              <ContactRow
                icon={<Mail className="size-3" />}
                small="Email"
                strong="support@mylaptopmarket.com"
                href="mailto:support@mylaptopmarket.com"
              />
              <ContactRow
                icon={<MapPin className="size-3" />}
                small="Office"
                strong="Shakarpur, New Delhi"
                href="https://www.google.com/maps/search/?api=1&query=Plot+no+19+Office+no+5,+Veer+Savarkar+Block,+Shakarpur+110091,+Delhi"
              />
            </div>
          </div>

          <div className="rounded-2xl border border-line bg-brand-soft p-[18px]">
            <div className="mb-2.5 grid size-10 place-items-center rounded-xl bg-white text-green">
              <Shield className="size-5" />
            </div>
            <strong className="mb-2 block text-navy">Trusted Platform</strong>
            <ul className="mb-3.5 space-y-1.5 text-xs text-ink-2">
              <li className="relative pl-3.5 before:absolute before:left-0 before:text-green before:content-['•']">
                Verified Businesses
              </li>
              <li className="relative pl-3.5 before:absolute before:left-0 before:text-green before:content-['•']">
                Secure Transactions
              </li>
              <li className="relative pl-3.5 before:absolute before:left-0 before:text-green before:content-['•']">
                Pan India Network
              </li>
            </ul>
            <a
              href="https://mylaptopmarket.com/api/vendor/register"
              className="inline-flex h-[42px] w-full items-center justify-center gap-2 rounded-full border-[1.5px] border-brand/45 bg-white px-[18px] text-sm font-bold text-brand"
            >
              Partner With Us <ArrowRight className="size-4" />
            </a>
          </div>
        </div>
      </section>

      <FooterSkyline />

      <div className="flex flex-wrap items-center justify-between gap-3 bg-navy px-5 py-3.5 text-xs text-white/80">
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
        <div className="flex flex-wrap items-center gap-3.5 font-semibold">
          <span className="inline-flex items-center gap-1.5">
            <Shield className="size-3.5 text-green" /> Secure &amp; Reliable
          </span>
          <span className="inline-flex items-center gap-1.5 rounded bg-white/10 px-2 py-1 text-[10px] tracking-wide text-[#ff9933]">
            <span className="font-extrabold text-white">Make in</span>
            <span className="font-extrabold text-[#138808]">India</span>
          </span>
          <span className="inline-flex items-center gap-1.5">
            <BarChart3 className="size-3.5 text-green" /> B2B Trusted Partner
          </span>
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
      className="grid size-[34px] place-items-center rounded-full bg-brand-soft text-brand transition hover:bg-brand hover:text-white"
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
  small,
  strong,
  em,
  href,
}: {
  icon: ReactNode;
  small: string;
  strong: string;
  em?: string;
  href: string;
}) {
  return (
    <a
      href={href}
      className="flex items-start gap-2.5"
      target={href.startsWith("http") ? "_blank" : undefined}
      rel={href.startsWith("http") ? "noopener noreferrer" : undefined}
    >
      <span className="grid size-8 shrink-0 place-items-center rounded-lg bg-brand-soft text-brand">
        {icon}
      </span>
      <span>
        <small className="mb-0.5 block text-[11px] text-ink-3">{small}</small>
        <strong className="block text-[13px] font-bold text-ink">{strong}</strong>
        {em ? (
          <em className="mt-1 inline-block text-xs font-bold not-italic text-brand">{em}</em>
        ) : null}
      </span>
    </a>
  );
}

function FooterSkyline() {
  return (
    <div
      className="relative overflow-hidden border-t border-line bg-[linear-gradient(180deg,#f4f8ff_0%,#e8f1fc_55%,#dce9fb_100%)]"
      aria-hidden
    >
      <div className="pointer-events-none absolute inset-0 opacity-40 [background-image:radial-gradient(circle_at_20%_80%,rgba(7,103,216,0.12),transparent_40%),radial-gradient(circle_at_80%_70%,rgba(32,202,71,0.08),transparent_35%)]" />
      <svg
        viewBox="0 0 1200 220"
        className="relative mx-auto block h-[140px] w-full max-w-[1180px] md:h-[180px]"
        preserveAspectRatio="xMidYMax meet"
      >
        <ellipse cx="600" cy="210" rx="520" ry="70" fill="rgba(7,103,216,0.06)" />
        {/* India Gate-ish */}
        <g fill="rgba(7,103,216,0.18)">
          <rect x="90" y="110" width="120" height="90" rx="4" />
          <rect x="120" y="70" width="60" height="40" rx="3" />
          <rect x="135" y="40" width="30" height="30" rx="2" />
          <path d="M145 20h10v20h-10z" />
        </g>
        {/* Taj-ish dome */}
        <g fill="rgba(7,103,216,0.16)">
          <path d="M980 200h140v-50h-140z" />
          <path d="M1000 150c20-55 80-55 100 0H1000z" />
          <rect x="1040" y="95" width="20" height="20" />
          <circle cx="1050" cy="88" r="8" />
        </g>
        {/* soft map blob */}
        <path
          d="M420 70c40-30 120-40 170-10 35 20 70 15 95-5 30-24 80-10 95 25 18 40-5 80-40 95-50 22-130 18-190 5-55-12-100-40-110-75-5-18 0-28-20-35z"
          fill="rgba(7,103,216,0.07)"
        />
        {/* three laptops */}
        <g transform="translate(310 78)">
          <LaptopSvg />
        </g>
        <g transform="translate(500 58) scale(1.15)">
          <LaptopSvg accent />
        </g>
        <g transform="translate(720 82)">
          <LaptopSvg />
        </g>
      </svg>
    </div>
  );
}

function LaptopSvg({ accent }: { accent?: boolean }) {
  const lid = accent ? "#0767d8" : "#3b82f6";
  const base = accent ? "#02144f" : "#1e3a5f";
  return (
    <g>
      <rect x="10" y="8" width="150" height="88" rx="8" fill={base} />
      <rect x="18" y="16" width="134" height="72" rx="4" fill="#e8f1fc" />
      <rect x="28" y="28" width="50" height="8" rx="2" fill={lid} opacity="0.85" />
      <rect x="28" y="42" width="70" height="6" rx="2" fill="#93c5fd" />
      <rect x="28" y="54" width="58" height="6" rx="2" fill="#bfdbfe" />
      <rect x="110" y="28" width="32" height="42" rx="4" fill="#fff" stroke={lid} strokeWidth="2" />
      <rect x="0" y="96" width="170" height="12" rx="3" fill={base} />
      <rect x="60" y="99" width="50" height="4" rx="2" fill="#64748b" />
    </g>
  );
}
