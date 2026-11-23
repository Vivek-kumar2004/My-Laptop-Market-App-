"use client";

import { useState } from "react";
import { cn } from "@/lib/landing/cn";
import { Icon, LogoMark, Wordmark } from "./Icon";
import { Reveal } from "@/lib/landing/motion";

export function CtaSection({
  onRegister,
  onRequirement,
}: {
  onRegister: () => void;
  onRequirement: () => void;
}) {
  return (
    <section className="relative bg-white pb-16 pt-4 sm:pb-20 lg:pb-24">
      <div className="wrap">
        <Reveal variant="scale">
          <div className="relative overflow-hidden rounded-[30px] bg-gradient-to-br from-brand via-[#0767D8] to-[#043a7c] px-6 py-10 text-white sm:rounded-[36px] sm:px-10 sm:py-12 lg:px-14 lg:py-16">
            <div className="grain pointer-events-none absolute inset-0 opacity-50" />
            <div className="pointer-events-none absolute inset-0 opacity-[0.16] [background-image:radial-gradient(#fff_1px,transparent_1px)] [background-size:22px_22px] [mask-image:radial-gradient(ellipse_at_top_left,#000,transparent_65%)]" />
            <div className="pointer-events-none absolute -right-20 -top-28 h-80 w-80 rounded-full bg-white/15 blur-3xl" />
            <div className="pointer-events-none absolute -bottom-24 left-1/3 h-64 w-64 rounded-full bg-brand-bright/40 blur-3xl" />
            <svg viewBox="0 0 400 400" className="pointer-events-none absolute -right-10 top-1/2 hidden h-[420px] w-[420px] -translate-y-1/2 opacity-20 lg:block" fill="none" aria-hidden="true">
              <circle cx="200" cy="200" r="150" stroke="white" strokeWidth="1" />
              <circle cx="200" cy="200" r="112" stroke="white" strokeWidth="1" strokeDasharray="5 7" />
              <circle cx="200" cy="200" r="74" stroke="white" strokeWidth="1" />
              <path d="M200 50v300M50 200h300" stroke="white" strokeWidth="0.7" strokeDasharray="3 9" />
              <circle cx="200" cy="50" r="5" fill="white" />
              <circle cx="350" cy="200" r="4" fill="white" />
              <circle cx="88" cy="286" r="4" fill="white" />
            </svg>

            <div className="relative grid gap-10 lg:grid-cols-[1.35fr_0.65fr] lg:items-center">
              <div>
                <span className="inline-flex items-center gap-2 rounded-full border border-white/25 bg-white/10 px-3 py-1.5 text-[9.5px] font-bold uppercase tracking-[0.14em] backdrop-blur">
                  <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-[#7cffa1]" />
                  Join 1,200+ verified businesses
                </span>
                <h2 className="display mt-5 max-w-[620px] text-[clamp(2.2rem,5vw,3.8rem)] text-white">
                  Your next deal is already in the network.
                </h2>
                <p className="mt-4 max-w-[470px] text-[13.5px] leading-relaxed text-white/70 sm:text-[14.5px]">
                  List your business free, get discovered by serious buyers and trade with India&apos;s refurbished IT
                  channel. No listing fee. No commission.
                </p>
                <div className="mt-7 flex flex-col gap-2.5 sm:flex-row">
                  <button onClick={onRegister} className="btn btn-white h-12 px-6 text-[13px]">
                    List your business <Icon name="arrow-up-right" size={16} className="arrow" />
                  </button>
                  <button
                    onClick={onRequirement}
                    className="btn h-12 border border-white/25 bg-white/5 px-6 text-[13px] text-white backdrop-blur transition hover:bg-white/15"
                  >
                    <Icon name="package" size={15} /> Post a requirement
                  </button>
                </div>
                <div className="mt-6 flex flex-wrap items-center gap-x-5 gap-y-2 text-[10.5px] font-medium text-white/60">
                  {["4-minute setup", "Verified businesses only", "Cancel anytime"].map((item) => (
                    <span key={item} className="flex items-center gap-1.5">
                      <Icon name="check" size={12} className="text-white" strokeWidth={2.6} /> {item}
                    </span>
                  ))}
                </div>
              </div>

              <div className="relative hidden lg:block">
                <div className="ml-auto w-full max-w-[260px] rounded-2xl border border-white/25 bg-white/10 p-4 backdrop-blur-md">
                  <div className="flex items-center justify-between border-b border-white/15 pb-3">
                    <span className="flex items-center gap-1.5 text-[9px] font-bold uppercase tracking-[0.14em] text-white/70">
                      <span className="h-1.5 w-1.5 rounded-full bg-[#7cffa1]" /> Live enquiry
                    </span>
                    <Icon name="arrow-up-right" size={13} className="text-white/60" />
                  </div>
                  <p className="mt-3 text-[11.5px] leading-snug text-white/80">
                    <span className="font-bold text-white">Bengaluru dealer</span> requested a quote for 40 × Latitude 5420
                  </p>
                  <div className="mt-3 flex items-center justify-between rounded-lg bg-white/10 px-2.5 py-2">
                    <span className="text-[9px] font-medium text-white/60">Response window</span>
                    <span className="text-[10px] font-bold text-white">Under 4 hours</span>
                  </div>
                  <div className="mt-2 flex items-center justify-between rounded-lg bg-white/10 px-2.5 py-2">
                    <span className="text-[9px] font-medium text-white/60">Commission</span>
                    <span className="text-[10px] font-bold text-[#7cffa1]">₹0</span>
                  </div>
                </div>
                <div className="animate-float absolute -bottom-8 -left-3 flex items-center gap-2 rounded-xl border border-white/20 bg-white px-3 py-2 text-navy shadow-xl">
                  <span className="flex h-6 w-6 items-center justify-center rounded-full bg-verified/15 text-verified">
                    <Icon name="shield" size={13} strokeWidth={2.2} />
                  </span>
                  <div className="leading-tight">
                    <p className="text-[9.5px] font-bold">Business verified</p>
                    <p className="text-[8px] text-slate-ink">Reviewed by our team</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </Reveal>
      </div>
    </section>
  );
}

const footerLinks = [
  {
    title: "Marketplace",
    links: ["Refurbished laptops", "Enterprise servers", "Networking gear", "Monitors & peripherals", "Corporate ITAD"],
  },
  {
    title: "Platform",
    links: ["Why My Laptop Market", "How it works", "Verified vendors", "Insights & guides", "Vendor registration"],
  },
  {
    title: "Company",
    links: ["About us", "Contact", "Partner with us", "Careers", "Newsroom"],
  },
];

export function Footer({ onRegister }: { onRegister: () => void }) {
  const [email, setEmail] = useState("");
  const [status, setStatus] = useState<"idle" | "error" | "success">("idle");

  const subscribe = (event: React.FormEvent) => {
    event.preventDefault();
    const valid = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email.trim());
    if (!valid) {
      setStatus("error");
      return;
    }
    setStatus("success");
    setEmail("");
  };

  return (
    <footer id="newsletter" className="relative overflow-hidden bg-ink pt-14 text-white sm:pt-16">
      <div className="pointer-events-none absolute inset-0 opacity-[0.13] [background-image:radial-gradient(rgba(255,255,255,0.5)_1px,transparent_1px)] [background-size:28px_28px] [mask-image:radial-gradient(ellipse_at_top,#000,transparent_75%)]" />
      <div className="pointer-events-none absolute -right-24 top-0 h-80 w-80 rounded-full bg-brand/20 blur-[100px]" />

      <div className="wrap relative">
        <div className="grid gap-10 border-b border-white/10 pb-12 lg:grid-cols-12 lg:gap-8">
          <div className="lg:col-span-3">
            <a href="#top" className="inline-flex items-center gap-2.5" aria-label="My Laptop Market home">
              <LogoMark className="h-10 w-10" />
              <Wordmark light />
            </a>
            <p className="mt-4 max-w-[250px] text-[11.5px] leading-relaxed text-white/50">
              India&apos;s B2B network for IT vendors, dealers, suppliers and technology businesses. Connecting refurbished IT
              dealers since day one.
            </p>
            <div className="mt-5 flex items-center gap-2">
              {[
                { name: "linkedin" as const, label: "LinkedIn" },
                { name: "instagram" as const, label: "Instagram" },
                { name: "youtube" as const, label: "YouTube" },
                { name: "whatsapp" as const, label: "WhatsApp" },
              ].map((social) => (
                <a
                  key={social.name}
                  href="https://mylaptopmarket.com"
                  target="_blank"
                  rel="noreferrer"
                  aria-label={social.label}
                  className="flex h-8 w-8 items-center justify-center rounded-full border border-white/15 text-white/60 transition hover:border-brand-bright hover:bg-brand hover:text-white"
                >
                  <Icon name={social.name} size={14} />
                </a>
              ))}
            </div>
          </div>

          {footerLinks.map((column) => (
            <div key={column.title} className="sm:col-span-1 lg:col-span-2">
              <p className="text-[9.5px] font-bold uppercase tracking-[0.18em] text-white/40">{column.title}</p>
              <ul className="mt-3.5 space-y-2.5">
                {column.links.map((link) => (
                  <li key={link}>
                    <a
                      href={
                        link === "Why My Laptop Market"
                          ? "#why"
                          : link === "How it works"
                            ? "#how"
                            : link === "Verified vendors"
                              ? "#vendors"
                              : link === "Insights & guides"
                                ? "#insights"
                                : "#marketplace"
                      }
                      className="group inline-flex items-center gap-1 text-[11.5px] font-medium text-white/65 transition hover:text-white"
                    >
                      {link}
                      <Icon name="arrow-up-right" size={10} className="opacity-0 transition group-hover:opacity-100" />
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}

          <div className="lg:col-span-3">
            <p className="text-[9.5px] font-bold uppercase tracking-[0.18em] text-white/40">Market intel, monthly</p>
            <p className="mt-3.5 text-[11.5px] leading-relaxed text-white/55">
              Pricing trends, stock insights and vendor opportunities. No noise.
            </p>
            {status === "success" ? (
              <div className="fade-slide-in mt-4 flex items-start gap-2.5 rounded-xl border border-verified/30 bg-verified/10 p-3">
                <span className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-verified text-white">
                  <Icon name="check" size={11} strokeWidth={3} />
                </span>
                <div>
                  <p className="text-[11px] font-bold text-white">You&apos;re on the list.</p>
                  <p className="mt-0.5 text-[9.5px] text-white/50">First issue lands in your inbox next month.</p>
                </div>
              </div>
            ) : (
              <form onSubmit={subscribe} className="mt-4">
                <div className={cn("flex items-center gap-1 rounded-full border bg-white/5 p-1 transition", status === "error" ? "border-red-400/60" : "border-white/15 focus-within:border-brand-bright/60")}>
                  <input
                    type="email"
                    value={email}
                    onChange={(event) => {
                      setEmail(event.target.value);
                      setStatus("idle");
                    }}
                    placeholder="Work email"
                    aria-label="Work email for newsletter"
                    className="h-9 w-full bg-transparent pl-3 text-[11.5px] text-white outline-none placeholder:text-white/40"
                  />
                  <button
                    type="submit"
                    aria-label="Subscribe"
                    className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-brand text-white transition hover:bg-brand-bright active:scale-95"
                  >
                    <Icon name="arrow-right" size={14} />
                  </button>
                </div>
                {status === "error" && <p className="mt-1.5 pl-3 text-[9.5px] font-medium text-red-300">Please enter a valid work email.</p>}
              </form>
            )}
            <p className="mt-2.5 flex items-center gap-1.5 pl-1 text-[9px] text-white/35">
              <Icon name="lock" size={10} /> We respect your inbox. Unsubscribe anytime.
            </p>
            <button onClick={onRegister} className="mt-4 flex items-center gap-1.5 text-[10.5px] font-bold text-brand-bright transition hover:text-white">
              Ready to trade? List your business <Icon name="arrow-up-right" size={12} />
            </button>
          </div>
        </div>

        <div className="flex flex-col items-center justify-between gap-4 border-b border-white/10 py-5 sm:flex-row">
          <p className="text-[10px] text-white/40">© 2026 My Laptop Market. All rights reserved.</p>
          <div className="flex flex-wrap items-center justify-center gap-x-5 gap-y-2 text-[10px] text-white/40">
            <a href="https://mylaptopmarket.com" className="transition hover:text-white">Privacy</a>
            <a href="https://mylaptopmarket.com" className="transition hover:text-white">Terms</a>
            <a href="https://mylaptopmarket.com" className="transition hover:text-white">Sitemap</a>
            <span className="flex items-center gap-1.5 text-white/50">
              <span className="h-1.5 w-1.5 rounded-full bg-verified" /> All systems operational
            </span>
          </div>
        </div>

        <div className="relative pb-24 pt-6 lg:pb-8">
          <div className="pointer-events-none select-none overflow-hidden">
            <p className="display whitespace-nowrap text-center text-[clamp(2.4rem,8.4vw,7.2rem)] leading-[0.9] text-white/[0.055]">
              MY LAPTOP MARKET
            </p>
          </div>
          <div className="mt-2 flex items-center justify-between text-[9px] font-medium uppercase tracking-[0.16em] text-white/25">
            <span>Built for the channel</span>
            <span className="hidden items-center gap-1.5 sm:flex">Made in India <span className="text-white/40">·</span> Serving 85+ cities</span>
            <span>Est. 2019</span>
          </div>
        </div>
      </div>
    </footer>
  );
}
