import { CountUp, Reveal } from "../lib/motion";
import { Icon, type IconName } from "./Icon";
import { SITE_ORIGIN } from "../lib/api";

const stats = [
  { value: 1200, suffix: "+", label: "Verified IT businesses" },
  { value: 85, suffix: "+", label: "Indian cities covered" },
  { value: 40, suffix: "+", label: "Product categories" },
  { value: 100, suffix: "%", label: "B2B focused platform" },
];

const cats: { label: string; icon: IconName; href: string }[] = [
  { label: "Laptops", icon: "laptop", href: `${SITE_ORIGIN}/products.html?category=Laptops` },
  { label: "Desktops", icon: "cpu", href: `${SITE_ORIGIN}/products.html?category=Desktops` },
  { label: "Monitors", icon: "monitor", href: `${SITE_ORIGIN}/products.html?category=Monitors` },
  { label: "Servers", icon: "server", href: `${SITE_ORIGIN}/products.html?category=Servers` },
  { label: "Networking", icon: "network", href: `${SITE_ORIGIN}/products.html?category=Networking` },
  { label: "All stock", icon: "grid", href: `${SITE_ORIGIN}/products.html` },
];

export function CategoryStrip() {
  return (
    <section className="relative border-b border-line/70 bg-white py-8 sm:py-10">
      <div className="wrap">
        <Reveal>
          <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <span className="eyebrow">Browse by category</span>
              <h2 className="mt-2 font-display text-[clamp(1.45rem,2.8vw,1.9rem)] font-bold tracking-[-0.03em] text-navy">
                What are you sourcing today?
              </h2>
            </div>
            <p className="max-w-[360px] text-[13px] leading-relaxed text-slate-ink">
              Jump into live refurbished stock from verified dealers across India.
            </p>
          </div>
        </Reveal>
        <div className="mt-6 grid grid-cols-2 gap-2.5 sm:grid-cols-3 lg:grid-cols-6">
          {cats.map((cat, index) => (
            <Reveal key={cat.label} delay={index * 40}>
              <a
                href={cat.href}
                className="group flex flex-col items-center gap-2.5 rounded-[18px] border border-line bg-mist/50 px-3 py-4 text-center transition hover:-translate-y-0.5 hover:border-brand/30 hover:bg-white hover:shadow-soft"
              >
                <span className="flex h-11 w-11 items-center justify-center rounded-full bg-white text-brand shadow-sm transition group-hover:bg-brand group-hover:text-white">
                  <Icon name={cat.icon} size={18} />
                </span>
                <span className="text-[12px] font-bold text-navy">{cat.label}</span>
              </a>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  );
}

export function StatsBand() {
  return (
    <section className="relative overflow-hidden border-y border-line/70 bg-navy py-12 text-white sm:py-14">
      <div className="pointer-events-none absolute inset-0 opacity-[0.14] [background-image:radial-gradient(rgba(255,255,255,0.45)_1px,transparent_1px)] [background-size:24px_24px]" />
      <div className="pointer-events-none absolute -left-20 top-0 h-56 w-56 rounded-full bg-brand/30 blur-[90px]" />
      <div className="wrap relative">
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {stats.map((stat, index) => (
            <Reveal key={stat.label} delay={index * 60}>
              <div className="rounded-[20px] border border-white/10 bg-white/[0.04] px-5 py-5 backdrop-blur-sm">
                <p className="font-display text-[clamp(2rem,3.5vw,2.6rem)] font-bold tracking-[-0.04em] text-white">
                  <CountUp value={stat.value} suffix={stat.suffix} />
                </p>
                <p className="mt-1.5 text-[11px] font-semibold uppercase tracking-[0.14em] text-white/55">{stat.label}</p>
              </div>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  );
}
