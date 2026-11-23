import { useCallback, useEffect, useId, useRef, useState } from "react";
import { images, storeLinks } from "../assets";
import { cn } from "../utils/cn";
import { BrandLogo, Icon } from "./Icon";

const STORAGE_KEY = "mlm_app_promo_dismissed";
const OPEN_DELAY_MS = 2000;
const EXIT_MS = 280;

const highlights = [
  "Buy & Sell Refurbished IT Stock",
  "Connect with Dealers Across India",
  "Discover Latest Listings & Requirements",
  "Direct Dealer-to-Dealer Networking",
] as const;

function wasDismissed() {
  try {
    return sessionStorage.getItem(STORAGE_KEY) === "1";
  } catch {
    return true;
  }
}

function markDismissed() {
  try {
    sessionStorage.setItem(STORAGE_KEY, "1");
  } catch {
    /* private mode / blocked storage */
  }
}

export function AppPromoModal({ blocked = false }: { blocked?: boolean }) {
  const [armed, setArmed] = useState(false);
  const [open, setOpen] = useState(false);
  const [visible, setVisible] = useState(false);
  const closeBtnRef = useRef<HTMLButtonElement>(null);
  const titleId = useId();
  const descId = useId();

  useEffect(() => {
    if (wasDismissed()) return;
    const timer = window.setTimeout(() => setArmed(true), OPEN_DELAY_MS);
    return () => window.clearTimeout(timer);
  }, []);

  useEffect(() => {
    if (!armed || blocked || wasDismissed()) return;
    setOpen(true);
    const frame = window.requestAnimationFrame(() => setVisible(true));
    return () => window.cancelAnimationFrame(frame);
  }, [armed, blocked]);

  const close = useCallback(() => {
    markDismissed();
    setVisible(false);
    window.setTimeout(() => setOpen(false), EXIT_MS);
  }, []);

  useEffect(() => {
    if (!open) return;
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    const onKey = (event: KeyboardEvent) => {
      if (event.key === "Escape") close();
    };
    window.addEventListener("keydown", onKey);
    const focusTimer = window.setTimeout(() => closeBtnRef.current?.focus(), 80);
    return () => {
      document.body.style.overflow = previous;
      window.removeEventListener("keydown", onKey);
      window.clearTimeout(focusTimer);
    };
  }, [open, close]);

  useEffect(() => {
    if (blocked && open) close();
  }, [blocked, open, close]);

  if (!open) return null;

  return (
    <div
      className={cn(
        "fixed inset-0 z-[125] flex items-start justify-center overflow-y-auto px-3 py-4 sm:items-center sm:px-5 sm:py-8",
        "transition-opacity",
        visible ? "opacity-100" : "opacity-0",
      )}
      style={{ transitionDuration: `${EXIT_MS}ms` }}
    >
      <button
        type="button"
        className={cn(
          "fixed inset-0 cursor-default bg-navy/65 backdrop-blur-[8px] transition-opacity",
          visible ? "opacity-100" : "opacity-0",
        )}
        style={{ transitionDuration: `${EXIT_MS}ms` }}
        onClick={close}
        aria-label="Close app promotion"
      />

      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        aria-describedby={descId}
        className={cn(
          "app-promo-panel relative my-auto w-full max-w-[980px] overflow-hidden rounded-[24px] border border-white/20 bg-white shadow-[0_40px_120px_-24px_rgba(7,20,38,0.65)] sm:rounded-[28px]",
          visible ? "app-promo-panel--in" : "app-promo-panel--out",
        )}
      >
        <button
          ref={closeBtnRef}
          type="button"
          onClick={close}
          className="absolute right-3.5 top-3.5 z-30 flex h-9 w-9 items-center justify-center rounded-full border border-line bg-white/95 text-navy shadow-sm backdrop-blur transition hover:border-navy/20 hover:bg-mist focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand"
          aria-label="Close"
        >
          <Icon name="x" size={16} />
        </button>

        <div className="grid lg:grid-cols-[1.05fr_0.95fr]">
          <div className="relative max-h-[min(92vh,820px)] overflow-y-auto p-5 sm:p-7 lg:max-h-[86vh] lg:p-8 lg:pr-7">
            <div className="pr-10">
              <BrandLogo className="h-9 max-w-[170px] sm:h-10 sm:max-w-[190px]" />
              <p className="mt-5 text-[9.5px] font-bold uppercase tracking-[0.18em] text-brand">
                Mobile app for dealers
              </p>
              <h2
                id={titleId}
                className="mt-2 font-display text-[26px] font-bold leading-[1.08] tracking-[-0.035em] text-navy sm:text-[32px] lg:text-[34px]"
              >
                Your Laptop Business.{" "}
                <span className="text-gradient-blue">Now on Mobile.</span>
              </h2>
              <p id={descId} className="mt-3 max-w-[440px] text-[13px] leading-relaxed text-slate-ink sm:text-[13.5px]">
                Buy, Sell &amp; Connect with verified refurbished IT dealers across India. Access the
                latest stock, explore requirements, connect directly with dealers, and grow your
                business — anytime, anywhere.
              </p>
            </div>

            <ul className="mt-5 grid gap-2.5 sm:mt-6">
              {highlights.map((item) => (
                <li key={item} className="flex items-start gap-2.5 text-[12px] font-semibold leading-snug text-navy sm:text-[12.5px]">
                  <span className="mt-0.5 flex h-[18px] w-[18px] shrink-0 items-center justify-center rounded-full bg-brand/10 text-brand">
                    <Icon name="check" size={11} strokeWidth={2.8} />
                  </span>
                  {item}
                </li>
              ))}
            </ul>

            <div className="mt-6 border-t border-line/80 pt-5 sm:mt-7 sm:pt-6">
              <p className="text-[10px] font-bold uppercase tracking-[0.16em] text-slate-ink/70">
                Download the My Laptop Market App
              </p>
              <div className="mt-3 flex flex-wrap items-center gap-2.5 sm:gap-3">
                <a
                  href={storeLinks.googlePlay}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="app-store-btn inline-flex rounded-xl transition hover:-translate-y-0.5 hover:opacity-100 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand"
                >
                  <img
                    src={images.googlePlayBadge}
                    alt="Get it on Google Play"
                    className="h-[52px] w-auto sm:h-[58px]"
                  />
                </a>
                <a
                  href={storeLinks.appStore}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="app-store-btn inline-flex rounded-xl transition hover:-translate-y-0.5 hover:opacity-100 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand"
                >
                  <img
                    src={images.appStoreBadge}
                    alt="Download on the App Store"
                    className="h-[36px] w-auto sm:h-10"
                  />
                </a>
              </div>
              <p className="mt-3 flex items-center gap-1.5 text-[10px] text-slate-ink">
                <Icon name="shield" size={11} className="text-verified" />
                Built for verified B2B refurbished IT dealers — not a consumer storefront.
              </p>
            </div>
          </div>

          <aside className="relative hidden min-h-[420px] overflow-hidden bg-gradient-to-br from-navy via-[#0c2550] to-brand-deep lg:block">
            <div className="grain pointer-events-none absolute inset-0 opacity-40" />
            <div className="pointer-events-none absolute inset-0 opacity-[0.12] [background-image:radial-gradient(#fff_1px,transparent_1px)] [background-size:18px_18px]" />
            <div className="pointer-events-none absolute -left-20 top-10 h-56 w-56 rounded-full bg-brand/35 blur-3xl" />
            <div className="pointer-events-none absolute -right-16 bottom-0 h-64 w-64 rounded-full bg-brand-bright/25 blur-3xl" />

            <div className="relative flex h-full flex-col justify-between p-6 pb-7 pl-5 pr-7">
              <div className="flex items-center justify-between gap-3">
                <span className="inline-flex items-center gap-1.5 rounded-full border border-white/15 bg-white/10 px-2.5 py-1 text-[9.5px] font-bold uppercase tracking-[0.14em] text-white/85 backdrop-blur">
                  <span className="h-1.5 w-1.5 animate-pulse-dot rounded-full bg-verified" />
                  Live on mobile
                </span>
                <span className="text-[10px] font-semibold text-white/45">iOS &amp; Android</span>
              </div>

              <div className="relative mx-auto mt-4 flex w-full max-w-[340px] flex-1 items-center justify-center">
                <div className="absolute -left-2 top-8 z-0 w-[58%] overflow-hidden rounded-2xl border border-white/15 bg-white/95 shadow-[0_24px_50px_-18px_rgba(0,0,0,0.55)] rotate-[-8deg]">
                  <img
                    src={images.appDashboard}
                    alt="My Laptop Market dealer dashboard"
                    className="aspect-[4/3] h-auto w-full object-cover object-top"
                    loading="lazy"
                  />
                </div>

                <div className="absolute -right-1 bottom-10 z-0 w-[52%] overflow-hidden rounded-2xl border border-white/15 bg-white shadow-[0_24px_50px_-18px_rgba(0,0,0,0.55)] rotate-[7deg]">
                  <img
                    src={images.appProducts}
                    alt="My Laptop Market product listings"
                    className="aspect-[4/3] h-auto w-full object-cover object-top"
                    loading="lazy"
                  />
                </div>

                <div className="app-promo-phone relative z-10 w-[210px] shrink-0">
                  <div className="rounded-[36px] border-[3px] border-white/25 bg-[#050d1a] p-[9px] shadow-[0_30px_70px_-20px_rgba(0,0,0,0.7)]">
                    <div className="relative overflow-hidden rounded-[28px] bg-mist">
                      <div className="absolute left-1/2 top-2 z-10 h-[22px] w-[88px] -translate-x-1/2 rounded-full bg-[#050d1a]" />
                      <div className="relative bg-gradient-to-b from-navy to-[#0c2550] px-3.5 pb-3 pt-8 text-white">
                        <div className="flex items-center justify-between">
                          <img src={images.logoMark} alt="" className="h-7 w-7 rounded-lg object-cover" />
                          <span className="text-[9px] font-bold uppercase tracking-[0.12em] text-white/70">
                            Dealer app
                          </span>
                          <span className="flex h-7 w-7 items-center justify-center rounded-full bg-white/10">
                            <Icon name="bell" size={12} className="text-white/80" />
                          </span>
                        </div>
                        <p className="mt-3 text-[11px] font-bold leading-tight tracking-[-0.02em]">
                          Trade refurbished IT on the go
                        </p>
                        <div className="mt-2.5 grid grid-cols-2 gap-1.5">
                          <span className="rounded-lg bg-verified px-2 py-1.5 text-center text-[9px] font-bold">
                            Buy stock
                          </span>
                          <span className="rounded-lg bg-brand px-2 py-1.5 text-center text-[9px] font-bold">
                            Sell stock
                          </span>
                        </div>
                      </div>
                      <div className="space-y-2 bg-white p-2.5 pb-3">
                        {[
                          { img: images.dell, name: "Dell Latitude 5420", meta: "40 units · Bengaluru" },
                          { img: images.hp, name: "HP EliteBook 840", meta: "24 units · Mumbai" },
                          { img: images.thinkpad, name: "ThinkPad T14 Gen 3", meta: "12 units · Delhi" },
                        ].map((row) => (
                          <div
                            key={row.name}
                            className="flex items-center gap-2 rounded-xl border border-line bg-mist/70 p-1.5"
                          >
                            <img src={row.img} alt="" className="h-9 w-9 rounded-lg object-cover" />
                            <div className="min-w-0 flex-1">
                              <p className="truncate text-[9.5px] font-bold text-navy">{row.name}</p>
                              <p className="truncate text-[8px] text-slate-ink">{row.meta}</p>
                            </div>
                            <Icon name="badge" size={11} className="shrink-0 text-verified" />
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div className="mt-5 flex items-center justify-center gap-4 text-center">
                <div>
                  <p className="font-display text-[18px] font-bold tracking-[-0.03em] text-white">1,200+</p>
                  <p className="text-[9px] font-semibold uppercase tracking-[0.12em] text-white/45">Dealers</p>
                </div>
                <span className="h-8 w-px bg-white/15" />
                <div>
                  <p className="font-display text-[18px] font-bold tracking-[-0.03em] text-white">85+</p>
                  <p className="text-[9px] font-semibold uppercase tracking-[0.12em] text-white/45">Cities</p>
                </div>
                <span className="h-8 w-px bg-white/15" />
                <div>
                  <p className="font-display text-[18px] font-bold tracking-[-0.03em] text-white">B2B</p>
                  <p className="text-[9px] font-semibold uppercase tracking-[0.12em] text-white/45">Only</p>
                </div>
              </div>
            </div>
          </aside>

          {/* Compact visual for tablet / mobile */}
          <aside className="relative overflow-hidden border-t border-line bg-gradient-to-br from-navy via-[#0c2550] to-brand-deep px-5 py-5 sm:px-6 lg:hidden">
            <div className="grain pointer-events-none absolute inset-0 opacity-30" />
            <div className="relative flex items-center gap-3">
              <div className="relative h-[88px] w-[72px] shrink-0 overflow-hidden rounded-[14px] border-2 border-white/25 bg-[#050d1a] p-1 shadow-lg">
                <div className="h-full overflow-hidden rounded-[10px] bg-white">
                  <img
                    src={images.appSearch}
                    alt="My Laptop Market app search"
                    className="h-full w-full object-cover object-top"
                    loading="lazy"
                  />
                </div>
              </div>
              <div className="min-w-0 flex-1">
                <p className="text-[10px] font-bold uppercase tracking-[0.14em] text-brand-bright">
                  Same platform. Pocket-ready.
                </p>
                <p className="mt-1 text-[12px] font-semibold leading-snug text-white">
                  Stock, requirements and dealer chat — wherever your business takes you.
                </p>
              </div>
              <img
                src={images.appMessages}
                alt=""
                className="hidden h-[72px] w-[96px] rounded-xl border border-white/15 object-cover object-top opacity-90 sm:block"
                loading="lazy"
              />
            </div>
          </aside>
        </div>
      </div>
    </div>
  );
}
