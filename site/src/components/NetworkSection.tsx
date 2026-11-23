import { useRef, useState } from "react";
import { networkNodes, steps } from "../data";
import { cn } from "../utils/cn";
import { Icon } from "./Icon";
import { CountUp, Reveal } from "../lib/motion";
import { images } from "../assets";

export function NetworkSection({ onRegister }: { onRegister: () => void }) {
  const [activeCity, setActiveCity] = useState(networkNodes[3]!);
  const spotlightRef = useRef<HTMLDivElement>(null);

  const onMouseMove = (event: React.MouseEvent<HTMLDivElement>) => {
    const el = spotlightRef.current;
    if (!el) return;
    const rect = el.getBoundingClientRect();
    el.style.setProperty("--mx", `${event.clientX - rect.left}px`);
    el.style.setProperty("--my", `${event.clientY - rect.top}px`);
  };

  return (
    <section id="why" className="relative scroll-mt-24 overflow-hidden bg-navy py-16 text-white sm:py-20 lg:py-24">
      <div className="grain pointer-events-none absolute inset-0 opacity-60" />
      <div className="pointer-events-none absolute inset-0 opacity-[0.18] [background-image:radial-gradient(rgba(255,255,255,0.35)_1px,transparent_1px)] [background-size:26px_26px] [mask-image:radial-gradient(ellipse_at_top_right,#000,transparent_72%)]" />
      <div className="pointer-events-none absolute -left-40 top-20 h-[420px] w-[420px] rounded-full bg-brand/25 blur-[110px]" />
      <div className="pointer-events-none absolute -right-24 top-1/3 h-[360px] w-[360px] rounded-full bg-brand-bright/15 blur-[100px]" />

      <div className="wrap relative">
        <div className="flex flex-col justify-between gap-7 lg:flex-row lg:items-end">
          <Reveal className="max-w-[640px]">
            <span className="eyebrow !text-brand-bright">Built different</span>
            <h2 className="display mt-4 text-[clamp(2.2rem,4.6vw,3.6rem)] text-white">
              Not a classifieds site.
              <br />
              <span className="text-brand-bright/90">A verified business network.</span>
            </h2>
            <p className="mt-5 max-w-[510px] text-[14.5px] leading-relaxed text-white/60">
              My Laptop Market exists exclusively for IT vendors, dealers, distributors, ITAD companies and corporate
              sourcing teams. Every profile is reviewed, every buyer is a business.
            </p>
          </Reveal>
          <Reveal delay={120} className="flex flex-wrap gap-2.5">
            <button onClick={onRegister} className="btn btn-primary h-11 min-h-0 px-5 text-[12.5px]">
              Join the network <Icon name="arrow-up-right" size={15} className="arrow" />
            </button>
            <a
              href="https://mylaptopmarket.com/why-us.html"
              target="_blank"
              rel="noreferrer"
              className="btn h-11 min-h-0 border border-white/20 bg-white/5 px-5 text-[12.5px] text-white backdrop-blur hover:bg-white/10"
            >
              See inside the platform
            </a>
          </Reveal>
        </div>

        <div ref={spotlightRef} onMouseMove={onMouseMove} className="relative mt-10 grid gap-3.5 lg:grid-cols-12">
          <Reveal className="lg:col-span-8">
            <div className="relative h-full min-h-[400px] overflow-hidden rounded-[26px] border border-white/10 bg-white/[0.045] p-5 backdrop-blur-sm sm:p-6">
              <div className="pointer-events-none absolute inset-0 opacity-70 [background-image:linear-gradient(rgba(255,255,255,0.045)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.045)_1px,transparent_1px)] [background-size:44px_44px]" />
              <div className="relative flex flex-wrap items-start justify-between gap-3">
                <div>
                  <p className="flex items-center gap-2 text-[10px] font-bold uppercase tracking-[0.18em] text-brand-bright">
                    <span className="relative flex h-1.5 w-1.5">
                      <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-verified opacity-70" />
                      <span className="relative inline-flex h-1.5 w-1.5 rounded-full bg-verified" />
                    </span>
                    Live network map
                  </p>
                  <h3 className="mt-2 font-display text-[21px] font-bold tracking-[-0.02em]">1,200+ businesses. One channel.</h3>
                </div>
                <div className="flex items-center gap-3 rounded-full border border-white/10 bg-white/5 px-3 py-1.5 text-[9.5px] font-medium text-white/60">
                  <span className="flex items-center gap-1"><span className="h-1.5 w-1.5 rounded-full bg-verified" /> Active</span>
                  <span className="flex items-center gap-1"><span className="h-1.5 w-1.5 rounded-full bg-white/25" /> Expanding</span>
                </div>
              </div>

              <div className="relative mt-5 h-[240px] sm:h-[260px]">
                <svg viewBox="0 0 100 100" preserveAspectRatio="none" className="absolute inset-0 h-full w-full" aria-hidden="true">
                  <defs>
                    <linearGradient id="networkLine" x1="0" y1="0" x2="1" y2="1">
                      <stop offset="0%" stopColor="#2B8BFF" stopOpacity="0.55" />
                      <stop offset="100%" stopColor="#2B8BFF" stopOpacity="0.08" />
                    </linearGradient>
                  </defs>
                  {networkNodes.map((node, index) => {
                    const next = networkNodes[(index + 1) % networkNodes.length]!;
                    return (
                      <line
                        key={`${node.city}-line`}
                        x1={node.x}
                        y1={node.y}
                        x2={next.x}
                        y2={next.y}
                        stroke="url(#networkLine)"
                        strokeWidth="0.32"
                        strokeDasharray="1.4 1.8"
                      />
                    );
                  })}
                  {networkNodes.map((node) => (
                    <line
                      key={`hub-${node.city}`}
                      x1={networkNodes[3]!.x}
                      y1={networkNodes[3]!.y}
                      x2={node.x}
                      y2={node.y}
                      stroke="#2B8BFF"
                      strokeOpacity={activeCity.city === node.city ? 0.65 : 0.14}
                      strokeWidth="0.28"
                    />
                  ))}
                </svg>

                {networkNodes.map((node) => {
                  const isActive = activeCity.city === node.city;
                  return (
                    <button
                      key={node.city}
                      onClick={() => setActiveCity(node)}
                      onMouseEnter={() => setActiveCity(node)}
                      style={{ left: `${node.x}%`, top: `${node.y}%` }}
                      className={cn(
                        "group absolute z-10 flex -translate-x-1/2 -translate-y-1/2 flex-col items-center gap-1 transition",
                        isActive ? "z-20" : "",
                      )}
                      aria-label={`${node.city}: ${node.count} verified businesses`}
                    >
                      <span className="relative flex items-center justify-center">
                        {isActive && <span className="absolute h-7 w-7 animate-ping rounded-full border border-brand-bright/50" />}
                        <span
                          className={cn(
                            "flex items-center justify-center rounded-full border transition-all duration-300",
                            isActive
                              ? "h-4 w-4 border-brand-bright bg-brand-bright shadow-[0_0_0_5px_rgba(43,139,255,0.18)]"
                              : "h-2.5 w-2.5 border-white/50 bg-white/40 group-hover:h-3.5 group-hover:w-3.5 group-hover:border-brand-bright group-hover:bg-brand-bright",
                          )}
                        >
                          {isActive && <span className="h-1.5 w-1.5 rounded-full bg-white" />}
                        </span>
                      </span>
                      <span
                        className={cn(
                          "whitespace-nowrap rounded-full border px-2 py-0.5 text-[9px] font-semibold transition-all",
                          isActive
                            ? "border-brand-bright/40 bg-brand text-white shadow-lg"
                            : "border-white/10 bg-navy/70 text-white/65 backdrop-blur-sm group-hover:border-brand-bright/30 group-hover:text-white",
                        )}
                      >
                        {node.city}
                      </span>
                    </button>
                  );
                })}
              </div>

              <div className="relative mt-1 flex flex-wrap items-end justify-between gap-4 rounded-2xl border border-white/10 bg-navy/50 p-4 backdrop-blur">
                <div className="flex items-center gap-3">
                  <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand/20 text-brand-bright">
                    <Icon name="pin" size={18} />
                  </span>
                  <div>
                    <p className="text-[9px] font-semibold uppercase tracking-[0.16em] text-white/45">Selected market</p>
                    <p className="text-[15px] font-bold text-white">
                      {activeCity.city}, India
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-6">
                  <div>
                    <p className="text-[9px] font-semibold uppercase tracking-[0.14em] text-white/45">Verified businesses</p>
                    <p className="text-[19px] font-bold leading-tight text-white">{activeCity.count}</p>
                  </div>
                  <span className="h-8 w-px bg-white/10" />
                  <div>
                    <p className="text-[9px] font-semibold uppercase tracking-[0.14em] text-white/45">Trading now</p>
                    <p className="flex items-center gap-1.5 text-[19px] font-bold leading-tight text-verified">
                      <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-verified" />
                      {Math.round(activeCity.count * 0.18)}
                    </p>
                  </div>
                  <span className="hidden h-8 w-px bg-white/10 sm:block" />
                  <p className="hidden max-w-[150px] text-[9.5px] leading-relaxed text-white/45 sm:block">
                    Hover a city to explore the dealer density.
                  </p>
                </div>
              </div>
            </div>
          </Reveal>

          <div className="grid gap-3.5 lg:col-span-4">
            <Reveal delay={80}>
              <div className="relative h-full overflow-hidden rounded-[26px] border border-brand/30 bg-gradient-to-br from-brand via-brand to-brand-deep p-5 sm:p-6">
                <div className="pointer-events-none absolute -right-10 -top-10 h-36 w-36 rounded-full bg-white/20 blur-2xl" />
                <div className="flex items-start justify-between">
                  <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-white/15 text-white backdrop-blur">
                    <Icon name="building" size={18} />
                  </span>
                  <span className="rounded-full bg-white/15 px-2.5 py-1 text-[9px] font-bold uppercase tracking-[0.12em] text-white/90 backdrop-blur">
                    B2B only
                  </span>
                </div>
                <p className="mt-4 font-display text-[38px] font-bold leading-none tracking-[-0.04em]">
                  <CountUp value={100} suffix="%" />
                </p>
                <p className="mt-1 text-[11px] font-semibold text-white/80">business-focused platform</p>
                <p className="mt-3 text-[11px] leading-relaxed text-white/65">
                  Dealers, distributors, ITADs and corporate IT. No consumer noise, no time-wasters.
                </p>
                <div className="mt-4 flex flex-wrap gap-1.5">
                  {["Dealers", "ITADs", "Corporates"].map((item) => (
                    <span key={item} className="rounded-full border border-white/25 px-2 py-0.5 text-[9px] font-medium text-white/85">{item}</span>
                  ))}
                </div>
              </div>
            </Reveal>

            <Reveal delay={140}>
              <div className="relative h-full overflow-hidden rounded-[26px] border border-white/10 bg-white/[0.045] p-5 backdrop-blur-sm sm:p-6">
                <div className="flex items-start justify-between">
                  <span className="flex h-9 w-9 items-center justify-center rounded-xl border border-white/10 bg-white/5 text-brand-bright">
                    <Icon name="rupee" size={18} />
                  </span>
                  <Icon name="trending" size={16} className="text-white/30" />
                </div>
                <div className="mt-4 flex items-end gap-1.5">
                  <span className="font-display text-[38px] font-bold leading-none tracking-[-0.04em] text-white">₹0</span>
                  <span className="pb-1 text-[10.5px] font-medium text-white/45">commission</span>
                </div>
                <p className="mt-2 text-[11px] leading-relaxed text-white/50">
                  on deals you close directly. Keep the margin you negotiate.
                </p>
                <div className="mt-4 flex items-center gap-1">
                  {Array.from({ length: 22 }).map((_, index) => (
                    <span
                      key={index}
                      className={cn("h-5 flex-1 rounded-sm", index < 15 ? "bg-brand-bright/70" : "bg-white/10")}
                      style={{ height: `${8 + (index % 5) * 3}px` }}
                    />
                  ))}
                </div>
              </div>
            </Reveal>
          </div>

          <Reveal delay={120} className="lg:col-span-4">
            <div className="group relative h-full min-h-[210px] overflow-hidden rounded-[26px] border border-white/10">
              <img
                src={images.lifestyle}
                alt="Technician inspecting a refurbished business laptop"
                className="absolute inset-0 h-full w-full object-cover transition-transform duration-[1200ms] group-hover:scale-105"
                loading="lazy"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-navy via-navy/55 to-transparent" />
              <div className="relative flex h-full flex-col justify-end p-5">
                <span className="mb-auto inline-flex w-fit items-center gap-1.5 rounded-full border border-white/20 bg-white/10 px-2.5 py-1 text-[9px] font-semibold text-white backdrop-blur">
                  <Icon name="scan" size={10} /> Real businesses, on the ground
                </span>
                <p className="font-display text-[18px] font-bold leading-tight tracking-[-0.02em] text-white">
                  Accountable by default.
                </p>
                <p className="mt-1 text-[10.5px] leading-relaxed text-white/60">
                  Every profile belongs to an operating business — with a city, a team and a reputation to protect.
                </p>
              </div>
            </div>
          </Reveal>

          <Reveal delay={180} className="lg:col-span-8">
            <div className="grid h-full grid-cols-1 overflow-hidden rounded-[26px] border border-white/10 bg-white/[0.03] sm:grid-cols-3">
              {[
                { icon: "badge" as const, title: "Document verified", body: "GST & business checks before the badge goes live." },
                { icon: "truck" as const, title: "Pan-India reach", body: "85+ cities and logistics partners for outbound units." },
                { icon: "message" as const, title: "Talk direct", body: "No middle layer. Reach the decision maker yourself." },
              ].map((feature, index) => (
                <div
                  key={feature.title}
                  className={cn(
                    "group p-5 transition-colors hover:bg-white/[0.04]",
                    index > 0 && "border-t border-white/10 sm:border-l sm:border-t-0",
                  )}
                >
                  <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand/15 text-brand-bright transition-transform duration-300 group-hover:scale-110">
                    <Icon name={feature.icon} size={16} />
                  </span>
                  <p className="mt-3 text-[13px] font-bold text-white">{feature.title}</p>
                  <p className="mt-1 text-[10.5px] leading-relaxed text-white/45">{feature.body}</p>
                </div>
              ))}
            </div>
          </Reveal>
        </div>
      </div>
    </section>
  );
}

export function HowItWorks({ onRegister }: { onRegister: () => void }) {
  const [active, setActive] = useState(0);

  return (
    <section id="how" className="relative scroll-mt-24 overflow-hidden bg-white py-16 sm:py-20 lg:py-24">
      <div className="pointer-events-none absolute right-0 top-0 h-[500px] w-[500px] translate-x-1/3 rounded-full bg-sky/80 blur-[100px]" />
      <div className="wrap relative grid gap-10 lg:grid-cols-[0.85fr_1.15fr] lg:gap-16">
        <Reveal>
          <div className="lg:sticky lg:top-28">
            <span className="eyebrow">How it works</span>
            <h2 className="display mt-4 max-w-[440px] text-[clamp(2.1rem,4.2vw,3.3rem)] text-navy">
              From sign-up to signed deal, in{" "}
              <span className="relative inline-block text-brand">
                three
                <svg viewBox="0 0 120 10" className="absolute -bottom-1.5 left-0 w-full text-brand/40" fill="none" aria-hidden="true">
                  <path d="M2 7.5C32 3 70 2.5 118 5.5" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" />
                </svg>
              </span>{" "}
              steps.
            </h2>
            <p className="mt-5 max-w-[360px] text-[14px] leading-relaxed text-slate-ink">
              Designed to be quick for serious businesses. No approval queues, no listing fees, no gatekeeping on your growth.
            </p>

            <div className="mt-8 hidden gap-8 border-t border-line pt-6 sm:flex lg:block">
              <div className="lg:flex lg:gap-8">
                <div>
                  <p className="font-display text-[28px] font-bold tracking-[-0.03em] text-navy"><CountUp value={4} suffix=" min" /></p>
                  <p className="mt-0.5 text-[10.5px] font-medium text-slate-ink">avg. setup time</p>
                </div>
                <span className="hidden h-10 w-px bg-line lg:block" />
                <div className="ml-8 lg:ml-0">
                  <p className="font-display text-[28px] font-bold tracking-[-0.03em] text-navy"><CountUp value={85} suffix="+" /></p>
                  <p className="mt-0.5 text-[10.5px] font-medium text-slate-ink">cities in network</p>
                </div>
              </div>
            </div>

            <button onClick={onRegister} className="btn btn-dark mt-8 h-11 min-h-0 px-5 text-[12.5px]">
              Create your free profile <Icon name="arrow-up-right" size={15} className="arrow" />
            </button>
          </div>
        </Reveal>

        <div className="flex flex-col gap-3">
          {steps.map((step, index) => {
            const isActive = active === index;
            return (
              <Reveal key={step.number} delay={index * 70}>
                <button
                  onClick={() => setActive(index)}
                  aria-expanded={isActive}
                  className={cn(
                    "group w-full overflow-hidden rounded-[22px] border p-5 text-left transition-all duration-500 sm:p-6",
                    isActive
                      ? "border-brand/25 bg-white shadow-[0_26px_60px_-38px_rgba(11,27,58,0.55)]"
                      : "border-line bg-mist/50 hover:border-brand/20 hover:bg-white",
                  )}
                >
                  <div className="flex items-start gap-4">
                    <span
                      className={cn(
                        "flex h-11 w-11 shrink-0 items-center justify-center rounded-full border text-[11px] font-bold tracking-wide transition-all duration-500",
                        isActive ? "border-brand bg-brand text-white shadow-[0_8px_20px_-10px_rgba(7,103,216,0.9)]" : "border-line bg-white text-slate-ink",
                      )}
                    >
                      {isActive ? <Icon name={step.icon} size={18} /> : step.number}
                    </span>
                    <div className="min-w-0 flex-1">
                      <p className={cn("text-[9px] font-bold uppercase tracking-[0.18em] transition-colors", isActive ? "text-brand" : "text-slate-ink/60")}>
                        Step {step.number} · {step.short}
                      </p>
                      <h3 className="mt-1 font-display text-[18px] font-bold leading-snug tracking-[-0.02em] text-navy sm:text-[20px]">
                        {step.title}
                      </h3>
                      <div
                        className={cn(
                          "grid transition-all duration-500",
                          isActive ? "mt-2 grid-rows-[1fr] opacity-100" : "grid-rows-[0fr] opacity-0",
                        )}
                      >
                        <div className="overflow-hidden">
                          <p className="text-[12.5px] leading-relaxed text-slate-ink">{step.description}</p>
                          <div className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-2">
                            {step.bullets.map((bullet) => (
                              <span key={bullet} className="flex items-center gap-1.5 text-[10.5px] font-semibold text-navy">
                                <Icon name="check" size={12} className="text-verified" strokeWidth={2.4} /> {bullet}
                              </span>
                            ))}
                          </div>
                        </div>
                      </div>
                    </div>
                    <div className="flex shrink-0 flex-col items-end gap-2">
                      <span
                        className={cn(
                          "flex h-7 w-7 items-center justify-center rounded-full border transition-all duration-300",
                          isActive ? "rotate-45 border-brand bg-brand text-white" : "border-line bg-white text-slate-ink group-hover:border-brand/30 group-hover:text-brand",
                        )}
                      >
                        <Icon name="plus" size={14} />
                      </span>
                      {isActive && (
                        <div className="hidden text-right sm:block">
                          <p className="text-[16px] font-bold leading-none text-brand">{step.metric}</p>
                          <p className="mt-0.5 text-[8.5px] font-medium text-slate-ink">{step.metricLabel}</p>
                        </div>
                      )}
                    </div>
                  </div>
                  {isActive && (
                    <div className="mt-4 h-[3px] overflow-hidden rounded-full bg-sky-2">
                      <div className="h-full rounded-full bg-gradient-to-r from-brand to-brand-bright transition-all duration-700" style={{ width: `${((index + 1) / steps.length) * 100}%` }} />
                    </div>
                  )}
                </button>
              </Reveal>
            );
          })}
          <Reveal delay={240} className="mt-1 flex items-center gap-2 text-[10.5px] text-slate-ink">
            <Icon name="lock" size={13} className="text-brand" /> Your business details stay private until you choose to connect.
          </Reveal>
        </div>
      </div>
    </section>
  );
}
