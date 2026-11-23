import { useEffect, useState } from "react";
import { categories, cities, inr, listingActivity, products, savings, type Product } from "../data";
import { Icon } from "./Icon";
import { CountUp, Reveal, useTilt } from "../lib/motion";

export function Hero({
  onSearch,
  onQuickView,
}: {
  onSearch: (query: string, category: string, city: string) => void;
  onQuickView: (product: Product) => void;
}) {
  const tilt = useTilt(5);
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("All categories");
  const [city, setCity] = useState("All India");
  const [activityIndex, setActivityIndex] = useState(0);
  const heroProduct = products[0]!;

  useEffect(() => {
    const timer = window.setInterval(() => {
      setActivityIndex((value) => (value + 1) % listingActivity.length);
    }, 4600);
    return () => window.clearInterval(timer);
  }, []);

  return (
    <section id="top" className="relative overflow-hidden bg-white pb-16 pt-10 sm:pb-20 sm:pt-14 lg:pb-24 lg:pt-16">
      <div
        className="pointer-events-none absolute inset-0 -z-10"
        style={{
          background:
            "radial-gradient(1000px 640px at 88% 4%, rgba(43,139,255,0.16), transparent 62%), radial-gradient(780px 520px at 4% 16%, rgba(238,244,255,0.95), transparent 68%)",
        }}
      />
      <div className="dot-grid pointer-events-none absolute inset-x-0 top-0 -z-10 h-[430px] opacity-50 [mask-image:linear-gradient(180deg,#000,transparent_88%)]" />

      <div className="wrap">
        <div className="grid items-center gap-14 lg:grid-cols-[1.02fr_0.98fr] lg:gap-10 xl:gap-16">
          <div className="relative z-10 min-w-0">
            <Reveal>
              <div className="inline-flex items-center gap-2.5 rounded-full border border-brand/15 bg-white/80 py-1.5 pl-1.5 pr-3.5 shadow-[0_6px_20px_-14px_rgba(7,103,216,0.6)] backdrop-blur">
                <span className="flex h-6 w-6 items-center justify-center rounded-full bg-brand text-white">
                  <Icon name="shield" size={13} strokeWidth={2} />
                </span>
                <span className="text-[11.5px] font-bold tracking-[0.01em] text-navy">India&apos;s B2B marketplace for refurbished IT</span>
                <span className="hidden h-1 w-1 rounded-full bg-navy/20 sm:block" />
                <span className="hidden items-center gap-1 text-[11px] font-semibold text-slate-ink sm:flex">
                  <span className="h-1.5 w-1.5 rounded-full bg-verified" /> 1,200+ verified
                </span>
              </div>
            </Reveal>

            <Reveal delay={70}>
              <h1 className="display mt-7 text-[clamp(2.6rem,5.35vw,4.5rem)] text-navy">
                India&apos;s IT trade,
                <br />
                <span className="relative inline-block">
                  <span className="text-gradient-blue">re-engineered.</span>
                  <svg
                    viewBox="0 0 420 14"
                    fill="none"
                    className="absolute -bottom-1.5 left-0 w-full text-brand/45 sm:-bottom-2"
                    aria-hidden="true"
                    preserveAspectRatio="none"
                  >
                    <path d="M3 10.5C92 3 248 2 417 7" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" />
                  </svg>
                </span>
              </h1>
            </Reveal>

            <Reveal delay={140}>
              <p className="mt-6 max-w-[27rem] text-[14.5px] leading-[1.7] text-slate-ink sm:mt-7 sm:text-[15.5px]">
                Buy, sell and move refurbished laptops, servers and network gear with verified IT businesses across India.
                Real stock. Real vendors. <span className="font-semibold text-navy">Zero commission</span> on deals you close.
              </p>
            </Reveal>

            <Reveal delay={200}>
              <form
                onSubmit={(event) => {
                  event.preventDefault();
                  onSearch(query, category, city);
                }}
                className="mt-8 rounded-[20px] border border-line bg-white p-2 shadow-[0_18px_50px_-24px_rgba(11,27,58,0.35)] sm:rounded-full"
              >
                <div className="flex flex-col gap-1 sm:flex-row sm:items-center">
                  <label className="flex h-12 flex-1 items-center gap-2.5 rounded-xl px-3.5 transition focus-within:bg-mist/70 sm:rounded-full">
                    <Icon name="search" size={18} className="text-brand" />
                    <input
                      value={query}
                      onChange={(event) => setQuery(event.target.value)}
                      placeholder="What are you sourcing?"
                      className="w-full min-w-[190px] bg-transparent text-[13.5px] font-medium text-navy outline-none placeholder:text-slate-ink/60"
                      aria-label="Search products"
                    />
                  </label>
                  <span className="hidden h-6 w-px bg-line sm:block" />
                  <label className="hidden h-12 items-center gap-2 rounded-xl px-3 text-[12.5px] font-semibold text-navy transition hover:bg-mist/70 sm:flex sm:rounded-full">
                    <Icon name="layers" size={15} className="hidden text-slate-ink sm:block" />
                    <select
                      value={category}
                      onChange={(event) => setCategory(event.target.value)}
                      className="w-full cursor-pointer appearance-none bg-transparent pr-5 text-[12.5px] font-semibold outline-none sm:w-auto"
                      aria-label="Category"
                    >
                      <option>All categories</option>
                      {categories.slice(0, 4).map((item) => (
                        <option key={item.label}>{item.label}</option>
                      ))}
                    </select>
                    <Icon name="chevron-down" size={13} className="-ml-4 text-slate-ink/70" />
                  </label>
                  <span className="hidden h-6 w-px bg-line md:block" />
                  <label className="hidden h-12 items-center gap-2 rounded-xl px-3 text-[12.5px] font-semibold text-navy transition hover:bg-mist/70 sm:flex sm:rounded-full">
                    <Icon name="pin" size={15} className="hidden text-slate-ink sm:block" />
                    <select
                      value={city}
                      onChange={(event) => setCity(event.target.value)}
                      className="w-full cursor-pointer appearance-none bg-transparent pr-5 text-[12.5px] font-semibold outline-none sm:w-auto"
                      aria-label="City"
                    >
                      <option>All India</option>
                      {cities.slice(0, 6).map((item) => (
                        <option key={item}>{item}</option>
                      ))}
                    </select>
                    <Icon name="chevron-down" size={13} className="-ml-4 text-slate-ink/70" />
                  </label>
                  <button type="submit" className="btn btn-primary h-12 min-h-0 shrink-0 px-5 text-[13px]">
                    Search stock
                    <Icon name="arrow-right" size={15} className="arrow" />
                  </button>
                </div>
              </form>
            </Reveal>

            <Reveal delay={260}>
              <div className="mt-4 flex min-w-0 flex-wrap items-center gap-x-3 gap-y-2 text-[11.5px] text-slate-ink">
                <span className="font-semibold text-navy/60">Popular:</span>
                {["ThinkPad T14", "Dell Latitude", "HPE ProLiant", "MacBook Pro"].map((term) => (
                  <button
                    key={term}
                    type="button"
                    onClick={() => {
                      setQuery(term.split(" ")[0] ?? term);
                      onSearch(term, "All categories", "All India");
                    }}
                    className="shrink-0 whitespace-nowrap rounded-full border border-line bg-white/70 px-3 py-1.5 font-medium text-navy/75 transition hover:-translate-y-0.5 hover:border-brand/30 hover:text-brand"
                  >
                    {term}
                  </button>
                ))}
              </div>
            </Reveal>

            <Reveal delay={320}>
              <div className="mt-9 flex flex-wrap items-center gap-x-6 gap-y-4">
                <div className="flex items-center gap-3">
                  <div className="flex -space-x-2.5">
                    {[
                      { label: "TR", tint: "bg-[#0767D8]" },
                      { label: "RL", tint: "bg-[#0B1B3A]" },
                      { label: "IA", tint: "bg-[#2B8BFF]" },
                      { label: "SS", tint: "bg-[#1E3A5F]" },
                    ].map((avatar) => (
                      <span
                        key={avatar.label}
                        className={`flex h-8 w-8 items-center justify-center rounded-full border-2 border-white text-[9px] font-bold tracking-wide text-white ${avatar.tint}`}
                      >
                        {avatar.label}
                      </span>
                    ))}
                    <span className="flex h-8 w-8 items-center justify-center rounded-full border-2 border-white bg-sky text-[9px] font-bold text-brand">
                      +1k
                    </span>
                  </div>
                  <div className="text-[11.5px] leading-tight">
                    <p className="font-bold text-navy">
                      <CountUp value={1200} suffix="+" /> businesses
                    </p>
                    <p className="text-slate-ink">Verified and trading every day</p>
                  </div>
                </div>
                <div className="hidden h-8 w-px bg-line sm:block" />
                <div className="text-[11.5px] leading-tight">
                  <div className="flex items-center gap-1 text-brand">
                    {Array.from({ length: 5 }).map((_, index) => (
                      <Icon key={index} name="star" size={12} strokeWidth={1.2} className="fill-brand" />
                    ))}
                    <span className="ml-1 font-bold text-navy">4.9/5</span>
                  </div>
                  <p className="mt-0.5 text-slate-ink">from 380+ vendor reviews</p>
                </div>
              </div>
            </Reveal>
          </div>

          <Reveal delay={160} variant="scale" className="relative min-w-0">
            <div className="relative mx-auto w-full min-w-0 max-w-[540px] lg:max-w-none">
              <div className="absolute -inset-6 -z-10 rounded-[42px] bg-gradient-to-br from-sky via-white to-sky-2 lg:-inset-8" />
              <div className="absolute -right-3 -top-3 -z-10 h-40 w-40 rounded-full bg-brand/15 blur-3xl" />

              <div ref={tilt.ref} onMouseMove={tilt.onMove} onMouseLeave={tilt.onLeave} className="tilt-card relative">
                <div className="relative overflow-hidden rounded-[26px] border border-white/70 bg-white shadow-[0_36px_90px_-34px_rgba(11,27,58,0.5)]">
                  <div className="flex items-center justify-between border-b border-line/80 px-4 py-3">
                    <div className="flex items-center gap-2 text-[10.5px] font-bold tracking-wide text-navy">
                      <span className="relative flex h-2 w-2">
                        <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-verified opacity-70" />
                        <span className="relative inline-flex h-2 w-2 rounded-full bg-verified" />
                      </span>
                      LIVE LISTING
                      <span className="font-medium text-slate-ink/60">/ {heroProduct.listed}</span>
                    </div>
                    <span className="inline-flex items-center gap-1 rounded-full bg-sky px-2.5 py-1 text-[10px] font-bold text-brand">
                      <Icon name="shield" size={11} strokeWidth={2} /> Verified vendor
                    </span>
                  </div>

                  <button onClick={() => onQuickView(heroProduct)} className="group relative block w-full overflow-hidden bg-[#f2f6fc] text-left">
                    <span className="absolute left-4 top-4 z-10 rounded-full border border-navy/5 bg-white/90 px-2.5 py-1 text-[10px] font-bold text-navy shadow-sm backdrop-blur">
                      {heroProduct.condition} · Refurbished
                    </span>
                    <span className="absolute right-4 top-4 z-10 flex h-8 w-8 items-center justify-center rounded-full bg-white/90 text-navy shadow-sm backdrop-blur transition group-hover:bg-brand group-hover:text-white">
                      <Icon name="arrow-up-right" size={15} />
                    </span>
                    <img
                      src={heroProduct.image}
                      alt={heroProduct.name}
                      className="h-[236px] w-full object-cover transition-transform duration-700 ease-out group-hover:scale-[1.04] sm:h-[268px]"
                    />
                    <div className="absolute inset-x-0 bottom-0 h-20 bg-gradient-to-t from-navy/25 to-transparent" />
                    <div className="absolute bottom-4 left-4 flex items-center gap-1.5 rounded-full border border-white/30 bg-navy/55 px-2.5 py-1 text-[10px] font-semibold text-white backdrop-blur-md">
                      <Icon name="pin" size={11} /> {heroProduct.city} · {heroProduct.units} units available
                    </div>
                  </button>

                  <div className="px-4 pb-4 pt-3.5 sm:px-5">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="text-[10px] font-bold uppercase tracking-[0.14em] text-brand">{heroProduct.brand} · {heroProduct.category}</p>
                        <button onClick={() => onQuickView(heroProduct)} className="mt-1 block text-left text-[16px] font-bold tracking-[-0.02em] text-navy hover:text-brand sm:text-[17px]">
                          {heroProduct.name}
                        </button>
                      </div>
                      <span className="shrink-0 rounded-full bg-verified/10 px-2 py-1 text-[10px] font-bold text-[#0f9a38]">Save {savings(heroProduct.price, heroProduct.mrp)}%</span>
                    </div>
                    <div className="mt-3 flex flex-wrap gap-1.5">
                      {heroProduct.specs.map((spec) => (
                        <span key={spec} className="chip px-2 py-1 text-[10px]">{spec}</span>
                      ))}
                    </div>
                    <div className="mt-4 flex items-end justify-between border-t border-line/80 pt-3">
                      <div>
                        <p className="text-[9.5px] font-semibold uppercase tracking-[0.12em] text-slate-ink/70">Trade price / unit</p>
                        <p className="text-[22px] font-bold leading-tight tracking-[-0.03em] text-navy">{inr(heroProduct.price)}</p>
                      </div>
                      <div className="flex items-center gap-2">
                        <div className={`flex h-8 w-8 items-center justify-center rounded-full bg-gradient-to-br ${heroProduct.vendor.tint} text-[9px] font-bold text-white`}>
                          {heroProduct.vendor.initials}
                        </div>
                        <div className="text-right text-[10px] leading-tight">
                          <p className="font-bold text-navy">{heroProduct.vendor.name}</p>
                          <p className="flex items-center justify-end gap-0.5 text-slate-ink">
                            <Icon name="badge" size={9} className="text-verified" /> MOQ {heroProduct.moq}
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="animate-float absolute -left-5 bottom-[30%] hidden w-[166px] rounded-2xl border border-line bg-white/95 p-3 shadow-[0_20px_50px_-22px_rgba(11,27,58,0.4)] backdrop-blur sm:block">
                  <div className="flex items-center gap-2">
                    <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-verified/12 text-[#0f9a38]">
                      <Icon name="shield" size={14} strokeWidth={2} />
                    </span>
                    <div className="leading-tight">
                      <p className="text-[10.5px] font-bold text-navy">7-point checked</p>
                      <p className="text-[9px] text-slate-ink">Every listed unit</p>
                    </div>
                  </div>
                  <div className="mt-2.5 h-1 overflow-hidden rounded-full bg-sky-2">
                    <div className="h-full w-[86%] rounded-full bg-gradient-to-r from-brand to-brand-bright" />
                  </div>
                  <p className="mt-1.5 text-[9px] font-medium text-slate-ink">Condition score <span className="font-bold text-navy">8.6/10</span></p>
                </div>

                <div className="absolute -right-4 -top-5 hidden h-[82px] w-[82px] items-center justify-center rounded-full border border-brand/15 bg-white/90 shadow-soft backdrop-blur lg:flex">
                  <svg viewBox="0 0 100 100" className="animate-orbit absolute inset-0 h-full w-full" aria-hidden="true">
                    <defs>
                      <path id="heroCircle" d="M50,50 m-36,0 a36,36 0 1,1 72,0 a36,36 0 1,1 -72,0" />
                    </defs>
                    <text className="fill-navy/60" fontSize="7.2" fontWeight="700" letterSpacing="2.2">
                      <textPath href="#heroCircle">VERIFIED STOCK · PAN-INDIA ·</textPath>
                    </text>
                  </svg>
                  <span className="flex h-9 w-9 items-center justify-center rounded-full bg-brand text-white">
                    <Icon name="badge" size={18} strokeWidth={1.9} />
                  </span>
                </div>
              </div>

              <div className="relative mt-3 flex items-center gap-2">
                <div className="flex flex-1 items-center gap-2 rounded-xl border border-line/80 bg-white/80 px-3 py-2 backdrop-blur">
                  <Icon name="laptop" size={13} className="text-brand" />
                  <span className="text-[9.5px] font-semibold text-navy">Laptops</span>
                  <span className="ml-auto rounded bg-mist px-1.5 py-0.5 text-[8.5px] font-bold text-slate-ink">620+</span>
                </div>
                <div className="flex flex-1 items-center gap-2 rounded-xl border border-line/80 bg-white/80 px-3 py-2 backdrop-blur">
                  <Icon name="server" size={13} className="text-brand" />
                  <span className="text-[9.5px] font-semibold text-navy">Servers</span>
                  <span className="ml-auto rounded bg-mist px-1.5 py-0.5 text-[8.5px] font-bold text-slate-ink">180+</span>
                </div>
                <div className="flex flex-1 items-center gap-2 rounded-xl border border-line/80 bg-white/80 px-3 py-2 backdrop-blur">
                  <Icon name="network" size={13} className="text-brand" />
                  <span className="text-[9.5px] font-semibold text-navy">Network</span>
                  <span className="ml-auto rounded bg-mist px-1.5 py-0.5 text-[8.5px] font-bold text-slate-ink">140+</span>
                </div>
              </div>

              <div className="relative mt-2.5 flex items-center gap-3 overflow-hidden rounded-xl border border-line/70 bg-white/75 px-3.5 py-2.5 backdrop-blur">
                <span className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-verified/12">
                  <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-verified" />
                </span>
                <p key={activityIndex} className="fade-slide-in truncate text-[10.5px] font-medium text-slate-ink">
                  <span className="font-bold text-navy">Live now · </span>
                  {listingActivity[activityIndex]}
                </p>
              </div>
            </div>
          </Reveal>
        </div>
      </div>
    </section>
  );
}
