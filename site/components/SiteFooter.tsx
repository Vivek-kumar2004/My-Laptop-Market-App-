import Image from "next/image";
import type { ReactNode } from "react";
import {
  ArrowRight,
  ChevronRight,
  Headset,
  Handshake,
  LayoutGrid,
  Mail,
  MapPin,
  Phone,
  Shield,
  BarChart3,
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
                  <a href="/services.html" className="flex items-center justify-between gap-2 hover:text-brand">
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
              <li>• Verified Businesses</li>
              <li>• Secure Transactions</li>
              <li>• Pan India Network</li>
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

      <div className="flex flex-wrap items-center justify-between gap-3 bg-navy px-5 py-3.5 text-xs text-white/80">
        <div>
          © 2026 My Laptop Market. All Rights Reserved. |{" "}
          <a href="/privacy-policy.html" className="text-white">
            Privacy Policy
          </a>
        </div>
        <div className="flex flex-wrap items-center gap-3.5 font-semibold">
          <span className="inline-flex items-center gap-1.5">
            <Shield className="size-3.5 text-green" /> Secure &amp; Reliable
          </span>
          <span className="inline-flex items-center gap-1.5">
            <BarChart3 className="size-3.5 text-green" /> B2B Trusted Partner
          </span>
        </div>
      </div>
    </footer>
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
    <a href={href} className="flex items-start gap-2.5" target={href.startsWith("http") ? "_blank" : undefined} rel={href.startsWith("http") ? "noopener noreferrer" : undefined}>
      <span className="grid size-8 shrink-0 place-items-center rounded-lg bg-brand-soft text-brand">
        {icon}
      </span>
      <span>
        <small className="mb-0.5 block text-[11px] text-ink-3">{small}</small>
        <strong className="block text-[13px] font-bold text-ink">{strong}</strong>
        {em ? <em className="mt-1 inline-block text-xs font-bold not-italic text-brand">{em}</em> : null}
      </span>
    </a>
  );
}
