import { useEffect, useMemo, useState } from "react";
import { cn } from "../utils/cn";
import { CountUp, Reveal } from "../lib/motion";
import { Icon } from "./Icon";
import { routes } from "../lib/routes";
import { images } from "../assets";
import { useCatalog } from "../lib/catalog";
import { inr, type Product } from "../data";
import { SITE_ORIGIN } from "../lib/api";

function LiveDot() {
  return (
    <span className="relative flex h-2 w-2">
      <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-current opacity-60" />
      <span className="relative inline-flex h-2 w-2 rounded-full bg-current" />
    </span>
  );
}

function relativeListed(listed: string, index: number) {
  if (listed && listed !== "Live now") return listed;
  if (index === 0) return "Just now";
  if (index === 1) return "2 min ago";
  return `${index + 1} min ago`;
}

function StockFeed({
  products,
  onQuickView,
}: {
  products: Product[];
  onQuickView?: (product: Product) => void;
}) {
  const leads = useMemo(() => {
    const ranked = [...products].sort((a, b) => {
      const aReal = a.vendor.name && a.vendor.name !== "Verified vendor" ? 1 : 0;
      const bReal = b.vendor.name && b.vendor.name !== "Verified vendor" ? 1 : 0;
      if (bReal !== aReal) return bReal - aReal;
      return (b.units || 0) - (a.units || 0);
    });
    return ranked.slice(0, 12).map((p) => ({
      id: p.id,
      qty: p.units,
      item: p.name,
      from: p.vendor.name,
      city: p.city,
      listed: p.listed,
      price: p.price,
      verified: p.vendor.verified,
      product: p,
    }));
  }, [products]);

  const [stack, setStack] = useState<{ lead: (typeof leads)[number]; key: number }[]>([]);

  useEffect(() => {
    if (!leads.length) {
      setStack([]);
      return;
    }
    setStack([
      { lead: leads[0]!, key: 0 },
      ...(leads[1] ? [{ lead: leads[1], key: 1 }] : []),
    ]);
  }, [leads]);

  useEffect(() => {
    if (leads.length < 2) return;
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    let n = 2;
    const id = window.setInterval(() => {
      const lead = leads[n % leads.length]!;
      n += 1;
      setStack((s) => [{ lead, key: n }, ...s].slice(0, 2));
    }, 3400);
    return () => window.clearInterval(id);
  }, [leads]);

  if (!stack.length) {
    return (
      <div className="rounded-2xl bg-white p-4 text-navy shadow-[0_22px_50px_-20px_rgba(5,7,11,0.55)] ring-1 ring-navy/5">
        <p className="text-[13px] font-semibold text-slate-ink">Loading live stock…</p>
      </div>
    );
  }

  return (
    <div className="relative flex flex-col gap-2.5">
      {stack.map((entry, i) => {
        const lead = entry.lead;
        const open = () => {
          if (onQuickView) onQuickView(lead.product);
          else if (lead.product.detailUrl) window.location.href = lead.product.detailUrl;
        };
        return (
          <button
            key={entry.key}
            type="button"
            onClick={open}
            className={cn(
              "fade-slide-in origin-top rounded-2xl bg-white p-4 text-left text-navy shadow-[0_22px_50px_-20px_rgba(5,7,11,0.55)] ring-1 ring-navy/5 transition hover:-translate-y-0.5 hover:shadow-[0_28px_56px_-22px_rgba(5,7,11,0.6)]",
              i > 0 && "max-sm:hidden",
            )}
            style={{ animationDelay: `${i * 40}ms` }}
          >
            <div className="flex items-center justify-between gap-3">
              <span className="inline-flex items-center gap-2 text-[10px] font-bold uppercase tracking-[0.1em] text-brand">
                <LiveDot /> Live stock
              </span>
              <span className="text-[10px] font-semibold uppercase tracking-[0.08em] text-slate-ink">
                {relativeListed(lead.listed, i)}
              </span>
            </div>
            <p className="mt-2.5 text-[15px] font-bold leading-tight tracking-[-0.015em]">
              {lead.qty} × {lead.item}
            </p>
            <div className="mt-2.5 flex items-center justify-between gap-2">
              <span className="truncate text-[12px] text-slate-ink">
                {lead.from} · {lead.city}
              </span>
              <span className="inline-flex shrink-0 items-center gap-1 rounded-full bg-sky px-2 py-1 text-[10px] font-bold text-brand">
                <Icon name="check" size={11} strokeWidth={2.4} />
                {lead.verified ? "Verified" : "In stock"}
              </span>
            </div>
            {lead.price > 0 ? (
              <p className="mt-2 text-[12px] font-bold text-navy">
                {inr(lead.price)}
                <span className="ml-1 font-semibold text-slate-ink">/ unit</span>
              </p>
            ) : null}
          </button>
        );
      })}
    </div>
  );
}

function BuySell() {
  const [side, setSide] = useState(0);
  useEffect(() => {
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    const id = window.setInterval(() => setSide((s) => 1 - s), 2600);
    return () => window.clearInterval(id);
  }, []);

  return (
    <div className="flex rounded-full bg-navy p-1.5 shadow-[0_18px_40px_-14px_rgba(5,7,11,0.6)]">
      {[
        { label: "Buy stock", href: routes.marketplace },
        { label: "Sell stock", href: `${SITE_ORIGIN}/api/vendor/register` },
      ].map((item, i) => (
        <a
          key={item.label}
          href={item.href}
          className={cn(
            "relative rounded-full px-4 py-2.5 text-[11px] font-bold uppercase tracking-[0.08em] transition-colors",
            side === i ? "text-navy" : "text-white/70 hover:text-white",
          )}
        >
          {side === i && <span className="absolute inset-0 rounded-full bg-white" />}
          <span className="relative">{item.label}</span>
        </a>
      ))}
    </div>
  );
}

export function HeroDashboard({ onQuickView }: { onQuickView?: (product: Product) => void }) {
  const { products, vendors, live } = useCatalog();

  const stats = useMemo(() => {
    const listingCount = products.length;
    const vendorCount = Math.max(
      vendors.length,
      new Set(products.map((p) => p.vendor.name).filter((n) => n && n !== "Verified vendor")).size,
    );
    const unitCount = products.reduce((sum, p) => sum + (p.units || 0), 0);
    return [
      { label: "Live listings", value: listingCount },
      { label: "Active vendors", value: vendorCount || listingCount },
      { label: "Units in stock", value: unitCount },
    ];
  }, [products, vendors]);

  const heroImage = useMemo(() => {
    const withImg = products.find((p) => p.image && !p.image.startsWith("/images/product-"));
    return withImg?.image || products[0]?.image || images.lifestyle;
  }, [products]);

  return (
    <Reveal delay={160} variant="scale" className="relative min-w-0">
      <div className="relative mx-auto aspect-[4/5] w-full max-w-[540px] sm:aspect-[10/11] lg:aspect-auto lg:h-[560px] xl:h-[600px]">
        <div className="absolute inset-0 overflow-hidden rounded-[32px] bg-brand shadow-[0_36px_90px_-34px_rgba(7,103,216,0.55)] sm:rounded-[36px]">
          <div
            aria-hidden
            className="pointer-events-none absolute inset-0 opacity-70"
            style={{
              backgroundImage:
                "linear-gradient(rgba(255,255,255,0.14) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.14) 1px, transparent 1px)",
              backgroundSize: "28px 28px",
            }}
          />
          <div className="relative z-10 p-6 text-white sm:p-7">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold uppercase tracking-[0.14em]">Dashboard</span>
              <span className="inline-flex items-center gap-2 rounded-full bg-white/15 px-3 py-1.5 text-[10px] font-bold uppercase tracking-[0.12em]">
                <LiveDot /> {live ? "Live" : "Stock"}
              </span>
            </div>
            <dl className="mt-6 grid grid-cols-3 gap-3 sm:gap-4">
              {stats.map((stat) => (
                <div key={stat.label} className="border-t border-white/35 pt-3">
                  <dt className="text-[9px] font-semibold uppercase leading-tight tracking-[0.1em] text-white/80 sm:text-[10px]">
                    {stat.label}
                  </dt>
                  <dd className="mt-2 font-display text-[clamp(1.35rem,4vw,2.05rem)] font-bold tracking-[-0.04em]">
                    <CountUp value={stat.value} />
                  </dd>
                </div>
              ))}
            </dl>
          </div>

          <img
            src={heroImage}
            alt=""
            className="pointer-events-none absolute -bottom-[8%] -right-[12%] w-[90%] max-w-none object-contain opacity-90 mix-blend-multiply"
          />
        </div>

        <div className="absolute -left-1 top-[38%] z-20 w-[82%] max-w-[330px] sm:-left-4 sm:top-[30%] lg:-left-6 xl:-left-10">
          <StockFeed products={products} onQuickView={onQuickView} />
        </div>

        <div className="absolute -bottom-4 left-4 z-20 sm:left-6">
          <BuySell />
        </div>
      </div>
    </Reveal>
  );
}
