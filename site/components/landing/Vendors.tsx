"use client";

import { useState } from "react";
import { testimonials } from "@/lib/landing/data";
import { cn } from "@/lib/landing/cn";
import { Icon } from "./Icon";
import { CountUp, Reveal } from "@/lib/landing/motion";

const vendors = [
  {
    name: "TechRenew India",
    initials: "TR",
    city: "Mumbai",
    rating: 4.9,
    deals: 312,
    specialties: ["Enterprise laptops", "Bulk supply"],
    since: "2016",
    tint: "from-[#0767D8] to-[#1E86FF]",
    response: "Replies in 1h",
  },
  {
    name: "ReCompute Labs",
    initials: "RL",
    city: "Bengaluru",
    rating: 4.8,
    deals: 248,
    specialties: ["Laptops", "Corporate ITAD"],
    since: "2014",
    tint: "from-[#0B1B3A] to-[#0767D8]",
    response: "Replies in 2h",
  },
  {
    name: "ServerSphere",
    initials: "SS",
    city: "Hyderabad",
    rating: 4.8,
    deals: 137,
    specialties: ["Servers", "Storage"],
    since: "2018",
    tint: "from-[#054FA8] to-[#2B8BFF]",
    response: "Replies in 3h",
  },
  {
    name: "iAsset Recovery",
    initials: "IA",
    city: "Pune",
    rating: 4.9,
    deals: 421,
    specialties: ["Apple", "Asset recovery"],
    since: "2012",
    tint: "from-[#14294D] to-[#2B8BFF]",
    response: "Replies in 1h",
  },
  {
    name: "Netline Systems",
    initials: "NS",
    city: "Noida",
    rating: 4.7,
    deals: 154,
    specialties: ["Networking", "Cisco"],
    since: "2017",
    tint: "from-[#054FA8] to-[#1E86FF]",
    response: "Replies in 2h",
  },
  {
    name: "Chennai Peripherals",
    initials: "CP",
    city: "Chennai",
    rating: 4.6,
    deals: 96,
    specialties: ["Monitors", "Peripherals"],
    since: "2019",
    tint: "from-[#0767D8] to-[#5EA9FF]",
    response: "Replies in 4h",
  },
];

export function VendorsSection({ onRegister }: { onRegister: () => void }) {
  const [active, setActive] = useState(0);
  const testimonial = testimonials[active]!;

  return (
    <section id="vendors" className="relative scroll-mt-24 overflow-hidden bg-white py-16 sm:py-20 lg:py-24">
      <div className="pointer-events-none absolute inset-x-0 top-0 h-px bg-line" />
      <div className="wrap">
        <div className="flex flex-col justify-between gap-6 md:flex-row md:items-end">
          <Reveal className="max-w-[600px]">
            <span className="eyebrow">Popular vendors</span>
            <h2 className="display mt-4 text-[clamp(2.1rem,4.2vw,3.35rem)] text-navy">
              Businesses that do this
              <br />
              <span className="text-brand">for a living.</span>
            </h2>
            <p className="mt-4 max-w-[480px] text-[14.5px] leading-relaxed text-slate-ink">
              Meet a few of the verified dealers, distributors and ITAD specialists trading across the platform every day.
            </p>
          </Reveal>

          <Reveal delay={100} className="flex items-center gap-4">
            <div className="flex items-center gap-3">
              <div className="flex -space-x-2.5">
                {vendors.slice(0, 4).map((vendor) => (
                  <span
                    key={vendor.initials}
                    className={`flex h-9 w-9 items-center justify-center rounded-full border-2 border-white bg-gradient-to-br ${vendor.tint} text-[9px] font-bold text-white`}
                  >
                    {vendor.initials}
                  </span>
                ))}
                <span className="flex h-9 w-9 items-center justify-center rounded-full border-2 border-white bg-sky text-[8.5px] font-bold text-brand">
                  +1k
                </span>
              </div>
              <div className="text-[11px] leading-tight">
                <p className="font-bold text-navy"><CountUp value={1200} suffix="+" /> verified businesses</p>
                <p className="text-slate-ink">Across 85+ Indian cities</p>
              </div>
            </div>
            <a
              href="https://mylaptopmarket.com"
              target="_blank"
              rel="noreferrer"
              className="flex h-10 w-10 items-center justify-center rounded-full border border-line text-navy transition hover:border-brand hover:bg-brand hover:text-white"
              aria-label="See all vendors"
            >
              <Icon name="arrow-up-right" size={16} />
            </a>
          </Reveal>
        </div>

        <div className="mt-9 grid gap-3.5 sm:grid-cols-2 lg:grid-cols-3">
          {vendors.map((vendor, index) => (
            <Reveal key={vendor.name} delay={(index % 3) * 60}>
              <a
                href="https://mylaptopmarket.com"
                target="_blank"
                rel="noreferrer"
                className="group relative flex h-full flex-col overflow-hidden rounded-[20px] border border-line bg-white p-5 transition-all duration-500 hover:-translate-y-1 hover:border-brand/25 hover:shadow-[0_26px_50px_-32px_rgba(11,27,58,0.4)]"
              >
                <span className="pointer-events-none absolute -right-8 -top-8 h-24 w-24 rounded-full bg-sky opacity-0 blur-2xl transition group-hover:opacity-100" />
                <div className="relative flex items-start justify-between gap-3">
                  <div className="flex items-center gap-3">
                    <span className={`flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br ${vendor.tint} font-display text-[13px] font-bold text-white shadow-md`}>
                      {vendor.initials}
                    </span>
                    <div>
                      <p className="flex items-center gap-1.5 text-[13.5px] font-bold tracking-[-0.01em] text-navy">
                        {vendor.name}
                        <Icon name="badge" size={13} className="text-verified" />
                      </p>
                      <p className="mt-0.5 flex items-center gap-1 text-[10.5px] text-slate-ink">
                        <Icon name="pin" size={10} /> {vendor.city} · Since {vendor.since}
                      </p>
                    </div>
                  </div>
                  <span className="flex items-center gap-1 rounded-md bg-mist px-1.5 py-1 text-[10px] font-bold text-navy">
                    <Icon name="star" size={10} className="fill-brand text-brand" strokeWidth={1} /> {vendor.rating}
                  </span>
                </div>

                <div className="relative mt-4 flex flex-wrap gap-1.5">
                  {vendor.specialties.map((specialty) => (
                    <span key={specialty} className="rounded-md border border-line bg-mist/60 px-2 py-1 text-[9.5px] font-medium text-slate-ink">
                      {specialty}
                    </span>
                  ))}
                </div>

                <div className="relative mt-auto flex items-center justify-between border-t border-line/70 pt-3.5">
                  <div className="flex items-center gap-3 text-[10px] text-slate-ink">
                    <span><span className="font-bold text-navy">{vendor.deals}</span> deals</span>
                    <span className="h-3 w-px bg-line" />
                    <span className="flex items-center gap-1"><Icon name="clock" size={10} /> {vendor.response}</span>
                  </div>
                  <span className="flex items-center gap-1 text-[10.5px] font-bold text-brand">
                    View profile
                    <Icon name="arrow-up-right" size={12} className="transition-transform duration-300 group-hover:translate-x-0.5 group-hover:-translate-y-0.5" />
                  </span>
                </div>
              </a>
            </Reveal>
          ))}
        </div>

        <Reveal delay={80}>
          <div className="mt-3.5 flex flex-col items-center justify-between gap-3 rounded-[18px] border border-dashed border-brand/25 bg-sky/45 px-5 py-4 sm:flex-row">
            <p className="flex items-center gap-2 text-center text-[12px] font-medium text-navy sm:text-left">
              <Icon name="store" size={16} className="text-brand" />
              Your business belongs in this list.
            </p>
            <button onClick={onRegister} className="link-arrow text-[12px]">
              List your business free <Icon name="arrow-right" size={14} />
            </button>
          </div>
        </Reveal>

        <Reveal delay={100}>
          <div className="relative mt-10 overflow-hidden rounded-[26px] border border-line bg-sky/70">
            <div className="pointer-events-none absolute inset-0 opacity-[0.5] [background-image:radial-gradient(#0767D8_1px,transparent_1px)] [background-size:22px_22px] [mask-image:linear-gradient(120deg,#000,transparent_65%)]" />
            <div className="pointer-events-none absolute -right-16 -top-20 h-72 w-72 rounded-full bg-brand/15 blur-3xl" />
            <svg viewBox="0 0 200 200" className="pointer-events-none absolute -right-8 bottom-0 h-64 w-64 text-brand/10" fill="none" aria-hidden="true">
              <circle cx="100" cy="100" r="70" stroke="currentColor" strokeWidth="1.5" />
              <circle cx="100" cy="100" r="52" stroke="currentColor" strokeWidth="1.5" strokeDasharray="4 6" />
              <circle cx="100" cy="100" r="32" stroke="currentColor" strokeWidth="1.5" />
            </svg>

            <div className="relative grid gap-7 p-6 sm:p-8 lg:grid-cols-[1.45fr_0.85fr] lg:items-center lg:gap-10 lg:p-9">
              <div>
                <div className="flex items-center gap-2">
                  <Icon name="quote" size={26} className="text-brand" strokeWidth={0} fill="currentColor" />
                  <span className="rounded-full border border-brand/20 bg-white/70 px-2.5 py-1 text-[9px] font-bold uppercase tracking-[0.14em] text-brand">
                    Verified vendor story
                  </span>
                </div>
                <div key={active} className="fade-slide-in">
                  <blockquote className="mt-4 font-display text-[clamp(1.25rem,2.5vw,1.85rem)] font-semibold leading-[1.35] tracking-[-0.02em] text-navy">
                    “{testimonial.quote}”
                  </blockquote>
                  <div className="mt-4 flex items-center gap-3">
                    <span className="flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-br from-brand to-brand-bright text-[11px] font-bold text-white">
                      {testimonial.initials}
                    </span>
                    <div>
                      <p className="flex items-center gap-1.5 text-[12px] font-bold text-navy">
                        {testimonial.name} <Icon name="badge" size={12} className="text-verified" />
                      </p>
                      <p className="text-[10.5px] text-slate-ink">{testimonial.role} · {testimonial.city}</p>
                    </div>
                  </div>
                </div>
                <div className="mt-6 flex items-center gap-2">
                  <button
                    onClick={() => setActive((value) => (value - 1 + testimonials.length) % testimonials.length)}
                    className="flex h-9 w-9 items-center justify-center rounded-full border border-navy/10 bg-white/80 text-navy transition hover:border-brand hover:bg-brand hover:text-white"
                    aria-label="Previous story"
                  >
                    <Icon name="arrow-left" size={15} />
                  </button>
                  <button
                    onClick={() => setActive((value) => (value + 1) % testimonials.length)}
                    className="flex h-9 w-9 items-center justify-center rounded-full border border-navy/10 bg-white/80 text-navy transition hover:border-brand hover:bg-brand hover:text-white"
                    aria-label="Next story"
                  >
                    <Icon name="arrow-right" size={15} />
                  </button>
                  <div className="ml-2 flex items-center gap-1.5">
                    {testimonials.map((item, index) => (
                      <button
                        key={item.name}
                        onClick={() => setActive(index)}
                        aria-label={`Story ${index + 1}`}
                        aria-current={active === index}
                        className={cn(
                          "h-1.5 rounded-full transition-all duration-300",
                          active === index ? "w-6 bg-brand" : "w-1.5 bg-navy/20 hover:bg-navy/40",
                        )}
                      />
                    ))}
                  </div>
                  <span className="ml-auto text-[10px] font-semibold text-slate-ink">
                    {String(active + 1).padStart(2, "0")} / {String(testimonials.length).padStart(2, "0")}
                  </span>
                </div>
              </div>

              <div key={`metric-${active}`} className="fade-slide-in relative overflow-hidden rounded-2xl border border-white/60 bg-white/80 p-5 shadow-[0_20px_50px_-36px_rgba(11,27,58,0.6)] backdrop-blur">
                <p className="text-[9.5px] font-bold uppercase tracking-[0.16em] text-brand">Result on platform</p>
                <p className="mt-2 font-display text-[36px] font-bold leading-none tracking-[-0.04em] text-navy">
                  {testimonial.metric}
                </p>
                <p className="mt-1 text-[11px] font-medium text-slate-ink">{testimonial.metricLabel}</p>
                <div className="mt-4 space-y-2">
                  {[
                    { label: "Verified business", value: 100 },
                    { label: "Response rate", value: 94 },
                    { label: "Deal satisfaction", value: 98 },
                  ].map((metric) => (
                    <div key={metric.label}>
                      <div className="flex items-center justify-between text-[9.5px] font-semibold">
                        <span className="text-slate-ink">{metric.label}</span>
                        <span className="text-navy">{metric.value}%</span>
                      </div>
                      <div className="mt-1 h-1 overflow-hidden rounded-full bg-sky-2">
                        <div
                          className="h-full rounded-full bg-gradient-to-r from-brand to-brand-bright"
                          style={{ width: `${metric.value}%` }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
                <div className="mt-4 flex items-center gap-1.5 border-t border-line pt-3 text-[9.5px] font-medium text-slate-ink">
                  <Icon name="shield" size={12} className="text-verified" /> Identity & business reviewed
                </div>
              </div>
            </div>
          </div>
        </Reveal>
      </div>
    </section>
  );
}
