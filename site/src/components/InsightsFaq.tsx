import { useState } from "react";
import { faqs, insights } from "../data";
import { cn } from "../utils/cn";
import { Icon } from "./Icon";
import { Reveal } from "../lib/motion";

export function InsightsSection() {
  const [featured, ...rest] = insights;

  return (
    <section id="insights" className="relative scroll-mt-24 border-y border-line/70 bg-mist py-16 sm:py-20 lg:py-24">
      <div className="wrap">
        <div className="flex flex-col justify-between gap-6 md:flex-row md:items-end">
          <Reveal className="max-w-[600px]">
            <span className="eyebrow">From the channel</span>
            <h2 className="display mt-4 text-[clamp(2.1rem,4.2vw,3.35rem)] text-navy">
              Notes, numbers &
              <br />
              <span className="text-brand">know-how.</span>
            </h2>
            <p className="mt-4 max-w-[470px] text-[14.5px] leading-relaxed text-slate-ink">
              Practical intelligence for the people moving India&apos;s refurbished IT — pricing, inspection playbooks and
              market shifts.
            </p>
          </Reveal>
          <Reveal delay={100}>
            <a
              href="https://mylaptopmarket.com"
              target="_blank"
              rel="noreferrer"
              className="link-arrow rounded-full border border-line bg-white px-4 py-2.5 text-[12px] transition hover:border-brand/30"
            >
              View all insights <Icon name="arrow-up-right" size={14} />
            </a>
          </Reveal>
        </div>

        <div className="mt-9 grid gap-4 lg:grid-cols-12">
          {featured && (
            <Reveal className="lg:col-span-5">
              <a
                href="https://mylaptopmarket.com"
                target="_blank"
                rel="noreferrer"
                className="group flex h-full flex-col overflow-hidden rounded-[24px] border border-line bg-white transition duration-500 hover:-translate-y-1 hover:border-brand/25 hover:shadow-[0_30px_60px_-36px_rgba(11,27,58,0.45)]"
              >
                <div className="relative overflow-hidden bg-[#f2f6fc]">
                  <img
                    src={featured.image}
                    alt=""
                    loading="lazy"
                    className="h-[240px] w-full object-cover transition-transform duration-[1100ms] group-hover:scale-105 sm:h-[280px]"
                  />
                  <span className="absolute left-4 top-4 rounded-full bg-navy/85 px-2.5 py-1 text-[9px] font-bold uppercase tracking-[0.12em] text-white backdrop-blur">
                    Featured report
                  </span>
                  <span className="absolute bottom-4 right-4 flex h-9 w-9 items-center justify-center rounded-full bg-white/90 text-navy shadow-sm backdrop-blur transition group-hover:bg-brand group-hover:text-white">
                    <Icon name="arrow-up-right" size={16} />
                  </span>
                </div>
                <div className="flex flex-1 flex-col p-5">
                  <div className="flex items-center gap-2 text-[9.5px] font-semibold text-slate-ink">
                    <span className="text-brand">{featured.category}</span>
                    <span className="h-1 w-1 rounded-full bg-line" />
                    {featured.date}
                    <span className="ml-auto flex items-center gap-1"><Icon name="clock" size={10} /> {featured.read}</span>
                  </div>
                  <h3 className="mt-2.5 font-display text-[22px] font-bold leading-[1.2] tracking-[-0.025em] text-navy transition group-hover:text-brand sm:text-[24px]">
                    {featured.title}
                  </h3>
                  <p className="mt-2.5 text-[12.5px] leading-relaxed text-slate-ink">{featured.excerpt}</p>
                  <span className="mt-auto flex items-center gap-1.5 pt-4 text-[11px] font-bold text-brand">
                    Read the report <Icon name="arrow-right" size={13} className="transition-transform duration-300 group-hover:translate-x-1" />
                  </span>
                </div>
              </a>
            </Reveal>
          )}

          <div className="flex flex-col gap-4 lg:col-span-7">
            {rest.map((insight, index) => (
              <Reveal key={insight.title} delay={80 + index * 70} className="flex-1">
                <a
                  href="https://mylaptopmarket.com"
                  target="_blank"
                  rel="noreferrer"
                  className="group grid h-full overflow-hidden rounded-[24px] border border-line bg-white transition duration-500 hover:-translate-y-1 hover:border-brand/25 hover:shadow-[0_26px_50px_-36px_rgba(11,27,58,0.4)] sm:grid-cols-[0.8fr_1.2fr]"
                >
                  <div className="relative min-h-[170px] overflow-hidden bg-[#f2f6fc]">
                    <img
                      src={insight.image}
                      alt=""
                      loading="lazy"
                      className="absolute inset-0 h-full w-full object-cover transition-transform duration-[1100ms] group-hover:scale-105"
                    />
                    <span className="absolute left-3 top-3 rounded-full bg-white/90 px-2 py-1 text-[8.5px] font-bold uppercase tracking-[0.12em] text-navy backdrop-blur">
                      {insight.category}
                    </span>
                  </div>
                  <div className="flex flex-col justify-center p-5 sm:p-6">
                    <div className="flex items-center gap-2 text-[9.5px] font-semibold text-slate-ink">
                      {insight.date}
                      <span className="h-1 w-1 rounded-full bg-line" />
                      <span className="flex items-center gap-1"><Icon name="clock" size={10} /> {insight.read}</span>
                    </div>
                    <h3 className="mt-2 font-display text-[19px] font-bold leading-[1.25] tracking-[-0.02em] text-navy transition group-hover:text-brand sm:text-[21px]">
                      {insight.title}
                    </h3>
                    <p className="mt-2 text-[12px] leading-relaxed text-slate-ink">{insight.excerpt}</p>
                    <span className="mt-3 flex items-center gap-1.5 text-[10.5px] font-bold text-brand">
                      Read guide
                      <Icon name="arrow-right" size={12} className="transition-transform duration-300 group-hover:translate-x-1" />
                    </span>
                  </div>
                </a>
              </Reveal>
            ))}
            <Reveal delay={220}>
              <div className="flex flex-col items-start justify-between gap-3 rounded-[20px] border border-line bg-white/60 px-5 py-4 sm:flex-row sm:items-center">
                <p className="flex items-center gap-2 text-[11.5px] font-medium text-slate-ink">
                  <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-brand/10 text-brand">
                    <Icon name="send" size={14} />
                  </span>
                  Monthly market intel, straight to your inbox.
                </p>
                <a href="#newsletter" className="link-arrow text-[11.5px]">
                  Subscribe <Icon name="arrow-down" size={13} className="-rotate-90" />
                </a>
              </div>
            </Reveal>
          </div>
        </div>
      </div>
    </section>
  );
}

export function FaqSection() {
  const [open, setOpen] = useState<number | null>(0);

  return (
    <section id="faq" className="relative scroll-mt-24 overflow-hidden bg-white py-16 sm:py-20 lg:py-24">
      <div className="pointer-events-none absolute -left-32 top-24 h-[420px] w-[420px] rounded-full bg-sky/70 blur-[110px]" />
      <div className="wrap relative grid gap-10 lg:grid-cols-[0.8fr_1.2fr] lg:gap-16">
        <Reveal>
          <div className="lg:sticky lg:top-28">
            <span className="eyebrow">Questions</span>
            <h2 className="display mt-4 max-w-[400px] text-[clamp(2.1rem,4.2vw,3.2rem)] text-navy">
              Straight answers,
              <br />
              <span className="text-brand">no fine print.</span>
            </h2>
            <p className="mt-4 max-w-[340px] text-[14px] leading-relaxed text-slate-ink">
              Everything businesses ask before joining. Still unsure? Our team is a WhatsApp message away.
            </p>

            <div className="mt-7 overflow-hidden rounded-[22px] border border-line bg-gradient-to-br from-white to-sky/60 p-5">
              <div className="flex items-center gap-3">
                <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand text-white shadow-[0_8px_20px_-10px_rgba(7,103,216,0.9)]">
                  <Icon name="headphones" size={19} />
                </span>
                <div>
                  <p className="text-[12.5px] font-bold text-navy">Talk to a real person</p>
                  <p className="text-[10.5px] text-slate-ink">Mon–Sat, 10am–7pm IST</p>
                </div>
              </div>
              <div className="mt-4 flex flex-wrap gap-2">
                <a href="https://wa.me/919718199976" target="_blank" rel="noreferrer" className="btn btn-primary h-9 min-h-0 flex-1 px-3 text-[11px]">
                  <Icon name="whatsapp" size={14} /> WhatsApp
                </a>
                <a href="tel:+919718199976" className="btn btn-ghost h-9 min-h-0 flex-1 px-3 text-[11px]">
                  <Icon name="phone" size={13} /> Call
                </a>
              </div>
            </div>
          </div>
        </Reveal>

        <div className="flex flex-col gap-2.5">
          {faqs.map((faq, index) => {
            const isOpen = open === index;
            return (
              <Reveal key={faq.question} delay={index * 45}>
                <div
                  className={cn(
                    "overflow-hidden rounded-[18px] border transition-all duration-500",
                    isOpen ? "border-brand/25 bg-white shadow-[0_20px_44px_-36px_rgba(11,27,58,0.5)]" : "border-line bg-mist/40 hover:border-line hover:bg-white",
                  )}
                >
                  <button
                    onClick={() => setOpen(isOpen ? null : index)}
                    aria-expanded={isOpen}
                    className="flex w-full items-center gap-4 px-5 py-4.5 text-left sm:px-6 sm:py-5"
                  >
                    <span className={cn("text-[10px] font-bold tabular-nums transition-colors", isOpen ? "text-brand" : "text-slate-ink/50")}>
                      {String(index + 1).padStart(2, "0")}
                    </span>
                    <span className="flex-1 text-[13.5px] font-bold tracking-[-0.01em] text-navy sm:text-[14.5px]">{faq.question}</span>
                    <span
                      className={cn(
                        "flex h-7 w-7 shrink-0 items-center justify-center rounded-full border transition-all duration-300",
                        isOpen ? "rotate-45 border-brand bg-brand text-white" : "border-line bg-white text-navy",
                      )}
                    >
                      <Icon name="plus" size={13} />
                    </span>
                  </button>
                  <div className={cn("grid transition-all duration-500 ease-out", isOpen ? "grid-rows-[1fr] opacity-100" : "grid-rows-[0fr] opacity-0")}>
                    <div className="overflow-hidden">
                      <p className="px-5 pb-5 pl-[3rem] pr-8 text-[13px] leading-[1.7] text-slate-ink sm:px-6 sm:pl-[3.5rem]">
                        {faq.answer}
                      </p>
                    </div>
                  </div>
                </div>
              </Reveal>
            );
          })}
          <Reveal delay={250} className="mt-2 flex items-center gap-2 text-[10.5px] text-slate-ink">
            <Icon name="shield" size={13} className="text-verified" />
            Verified businesses only. We review every profile before it goes live.
          </Reveal>
        </div>
      </div>
    </section>
  );
}
