import { useState } from "react";
import { images, storeLinks } from "../assets";
import { Reveal } from "../lib/motion";
import { cn } from "../utils/cn";
import { Icon } from "./Icon";

const highlights = [
  "Buy & sell refurbished IT stock on the go",
  "Browse live listings and dealer requirements",
  "Message verified businesses directly",
  "Same network as the web — pocket-ready",
] as const;

const shots = [
  {
    key: "dashboard",
    src: images.appDashboard,
    label: "Dealer dashboard",
    caption: "See stock, leads and activity in one place.",
  },
  {
    key: "products",
    src: images.appProducts,
    label: "Product listings",
    caption: "Browse refurbished inventory from verified dealers.",
  },
  {
    key: "search",
    src: images.appSearch,
    label: "Search & filter",
    caption: "Find units by brand, city, condition and quantity.",
  },
  {
    key: "messages",
    src: images.appMessages,
    label: "Dealer messages",
    caption: "Talk direct with businesses already in the network.",
  },
] as const;

function StoreBadges({ size = "lg" }: { size?: "lg" | "md" }) {
  const playH = size === "lg" ? "h-[56px] sm:h-[62px]" : "h-[48px] sm:h-[54px]";
  const appleH = size === "lg" ? "h-[38px] sm:h-11" : "h-[34px] sm:h-10";

  return (
    <div className="flex flex-wrap items-center gap-2.5 sm:gap-3">
      <a
        href={storeLinks.googlePlay}
        target="_blank"
        rel="noopener noreferrer"
        className="app-store-btn inline-flex rounded-xl transition hover:-translate-y-0.5 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand"
      >
        <img src={images.googlePlayBadge} alt="Get it on Google Play" className={cn("w-auto", playH)} />
      </a>
      <a
        href={storeLinks.appStore}
        target="_blank"
        rel="noopener noreferrer"
        className="app-store-btn inline-flex rounded-xl transition hover:-translate-y-0.5 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand"
      >
        <img src={images.appStoreBadge} alt="Download on the App Store" className={cn("w-auto", appleH)} />
      </a>
    </div>
  );
}

export function DownloadApp() {
  const [active, setActive] = useState(0);
  const current = shots[active]!;

  return (
    <>
      <section id="download" className="relative scroll-mt-24 overflow-hidden bg-navy py-16 text-white sm:py-20 lg:py-24">
        <div className="grain pointer-events-none absolute inset-0 opacity-55" />
        <div className="pointer-events-none absolute inset-0 opacity-[0.14] [background-image:radial-gradient(rgba(255,255,255,0.4)_1px,transparent_1px)] [background-size:22px_22px]" />
        <div className="pointer-events-none absolute -left-32 top-10 h-[380px] w-[380px] rounded-full bg-brand/30 blur-[110px]" />
        <div className="pointer-events-none absolute -right-20 bottom-0 h-[320px] w-[320px] rounded-full bg-brand-bright/20 blur-[100px]" />

        <div className="wrap relative">
          <div className="grid items-center gap-12 lg:grid-cols-12 lg:gap-10">
            <Reveal className="lg:col-span-6">
              <span className="eyebrow !text-brand-bright">Mobile app</span>
              <h1 className="display mt-4 text-[clamp(2.2rem,4.6vw,3.6rem)] text-white">
                Your laptop business.
                <br />
                <span className="text-brand-bright/90">Now on mobile.</span>
              </h1>
              <p className="mt-5 max-w-[480px] text-[14.5px] leading-relaxed text-white/60">
                Buy, sell and connect with verified refurbished IT dealers across India. Access live stock, explore
                requirements and grow your business — anytime, anywhere.
              </p>

              <ul className="mt-7 grid gap-2.5">
                {highlights.map((item) => (
                  <li key={item} className="flex items-start gap-2.5 text-[13px] font-semibold text-white/85">
                    <span className="mt-0.5 flex h-[18px] w-[18px] shrink-0 items-center justify-center rounded-full bg-brand/25 text-brand-bright">
                      <Icon name="check" size={11} strokeWidth={2.8} />
                    </span>
                    {item}
                  </li>
                ))}
              </ul>

              <div className="mt-8">
                <p className="text-[10px] font-bold uppercase tracking-[0.16em] text-white/45">
                  Download the My Laptop Market app
                </p>
                <div className="mt-3">
                  <StoreBadges />
                </div>
                <p className="mt-3 flex items-center gap-1.5 text-[11px] text-white/50">
                  <Icon name="shield" size={12} className="text-verified" />
                  Built for verified B2B dealers — iOS &amp; Android
                </p>
              </div>
            </Reveal>

            <Reveal delay={100} className="relative lg:col-span-6">
              <div className="relative mx-auto flex max-w-[420px] items-end justify-center gap-3 sm:max-w-[480px] sm:gap-4">
                <div className="mb-8 hidden w-[38%] overflow-hidden rounded-[22px] border border-white/15 bg-white/95 shadow-[0_28px_60px_-20px_rgba(0,0,0,0.55)] rotate-[-7deg] sm:block">
                  <img
                    src={images.appSearch}
                    alt="Search refurbished IT stock in the app"
                    className="aspect-[9/16] h-auto w-full object-cover object-top"
                    loading="lazy"
                  />
                </div>
                <div className="relative z-10 w-[62%] max-w-[260px] sm:w-[52%]">
                  <div className="rounded-[36px] border-[3px] border-white/25 bg-[#050d1a] p-[10px] shadow-[0_34px_80px_-22px_rgba(0,0,0,0.7)]">
                    <div className="relative overflow-hidden rounded-[28px] bg-mist">
                      <div className="absolute left-1/2 top-2 z-10 h-[22px] w-[88px] -translate-x-1/2 rounded-full bg-[#050d1a]" />
                      <img
                        src={images.appDashboard}
                        alt="My Laptop Market dealer dashboard on mobile"
                        className="aspect-[9/19] h-auto w-full object-cover object-top"
                        loading="eager"
                      />
                    </div>
                  </div>
                </div>
                <div className="mb-10 hidden w-[38%] overflow-hidden rounded-[22px] border border-white/15 bg-white shadow-[0_28px_60px_-20px_rgba(0,0,0,0.55)] rotate-[7deg] sm:block">
                  <img
                    src={images.appProducts}
                    alt="Product listings in the My Laptop Market app"
                    className="aspect-[9/16] h-auto w-full object-cover object-top"
                    loading="lazy"
                  />
                </div>
              </div>
            </Reveal>
          </div>
        </div>
      </section>

      <section className="relative bg-white py-16 sm:py-20 lg:py-24">
        <div className="wrap">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <Reveal>
              <span className="eyebrow">App screenshots</span>
              <h2 className="display mt-3 text-[clamp(1.9rem,3.8vw,2.8rem)] text-navy">
                See the dealer app in action.
              </h2>
              <p className="mt-3 max-w-[460px] text-[14px] leading-relaxed text-slate-ink">
                Dashboard, listings, search and messaging — the same B2B workflow you use on the web, designed for mobile.
              </p>
            </Reveal>
            <Reveal delay={80}>
              <StoreBadges size="md" />
            </Reveal>
          </div>

          <div className="mt-10 grid gap-6 lg:grid-cols-12 lg:items-start">
            <Reveal className="lg:col-span-5">
              <div className="overflow-hidden rounded-[28px] border border-line bg-mist/50 p-4 sm:p-5">
                <div className="mx-auto w-full max-w-[280px]">
                  <div className="rounded-[32px] border-[3px] border-navy/90 bg-[#050d1a] p-[9px] shadow-soft">
                    <div className="relative overflow-hidden rounded-[24px] bg-white">
                      <div className="absolute left-1/2 top-2 z-10 h-[20px] w-[80px] -translate-x-1/2 rounded-full bg-[#050d1a]" />
                      <img
                        key={current.key}
                        src={current.src}
                        alt={current.label}
                        className="aspect-[9/19] h-auto w-full object-cover object-top"
                      />
                    </div>
                  </div>
                </div>
                <div className="mt-5 text-center">
                  <p className="text-[14px] font-bold text-navy">{current.label}</p>
                  <p className="mt-1 text-[12.5px] text-slate-ink">{current.caption}</p>
                </div>
              </div>
            </Reveal>

            <div className="grid gap-3 sm:grid-cols-2 lg:col-span-7">
              {shots.map((shot, index) => {
                const selected = index === active;
                return (
                  <Reveal key={shot.key} delay={index * 40}>
                    <button
                      type="button"
                      onClick={() => setActive(index)}
                      className={cn(
                        "group flex w-full flex-col overflow-hidden rounded-[22px] border text-left transition",
                        selected
                          ? "border-brand bg-white shadow-soft ring-2 ring-brand/20"
                          : "border-line bg-white hover:-translate-y-0.5 hover:border-brand/30 hover:shadow-soft",
                      )}
                    >
                      <div className="relative aspect-[16/10] overflow-hidden bg-mist">
                        <img
                          src={shot.src}
                          alt={shot.label}
                          className="h-full w-full object-cover object-top transition duration-500 group-hover:scale-[1.03]"
                          loading="lazy"
                        />
                        {selected && (
                          <span className="absolute left-3 top-3 inline-flex items-center gap-1 rounded-full bg-brand px-2.5 py-1 text-[9px] font-bold uppercase tracking-[0.12em] text-white">
                            Viewing
                          </span>
                        )}
                      </div>
                      <div className="p-4">
                        <p className="text-[13px] font-bold text-navy">{shot.label}</p>
                        <p className="mt-1 text-[11.5px] leading-relaxed text-slate-ink">{shot.caption}</p>
                      </div>
                    </button>
                  </Reveal>
                );
              })}
            </div>
          </div>
        </div>
      </section>

      <section className="relative overflow-hidden border-t border-line/70 bg-mist/40 py-14 sm:py-16">
        <div className="wrap">
          <Reveal>
            <div className="relative overflow-hidden rounded-[28px] border border-line bg-white px-6 py-10 sm:rounded-[32px] sm:px-10 sm:py-12 lg:px-14">
              <div className="pointer-events-none absolute -right-16 -top-16 h-56 w-56 rounded-full bg-brand/10 blur-3xl" />
              <div className="relative flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
                <div className="max-w-[520px]">
                  <h2 className="display text-[clamp(1.7rem,3.2vw,2.4rem)] text-navy">
                    Ready to trade from your phone?
                  </h2>
                  <p className="mt-3 text-[14px] leading-relaxed text-slate-ink">
                    Install My Laptop Market on iOS or Android and stay connected to India&apos;s refurbished IT channel.
                  </p>
                </div>
                <StoreBadges />
              </div>
            </div>
          </Reveal>
        </div>
      </section>
    </>
  );
}
