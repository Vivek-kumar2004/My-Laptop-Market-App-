import { Link } from "react-router-dom";
import { images } from "../assets";
import { CountUp, Reveal } from "../lib/motion";
import { routes } from "../lib/routes";
import { cn } from "../utils/cn";
import { Icon, type IconName } from "./Icon";

const pillars: { icon: IconName; title: string; body: string }[] = [
  {
    icon: "badge",
    title: "Verified vendor network",
    body: "GST and business checks before a profile goes live — so every conversation starts with a real company.",
  },
  {
    icon: "network",
    title: "Industry-focused platform",
    body: "Built only for refurbished IT dealers, distributors, ITADs, suppliers and corporate sourcing teams.",
  },
  {
    icon: "trending",
    title: "Business lead generation",
    body: "Post stock, surface requirements and reach decision-makers without consumer marketplace noise.",
  },
  {
    icon: "pin",
    title: "Nationwide connections",
    body: "Trade across 85+ Indian cities with logistics partners and dealers already in the channel.",
  },
];

const benefits: { icon: IconName; title: string; body: string }[] = [
  {
    icon: "building",
    title: "Expand your network",
    body: "Connect with dealers, wholesalers, distributors, importers, exporters and asset recovery teams.",
  },
  {
    icon: "message",
    title: "Generate business leads",
    body: "Receive inquiries for sourcing, distribution, refurbishment, ITAD and enterprise hardware partners.",
  },
  {
    icon: "scan",
    title: "Market visibility",
    body: "Show up in a searchable B2B network built for IT equipment trade — not retail storefronts.",
  },
  {
    icon: "shield",
    title: "Strategic partnerships",
    body: "Build lasting supplier relationships, corporate asset pipelines and regional collaboration.",
  },
];

export function AboutSection({ onRegister }: { onRegister: () => void }) {
  return (
    <>
      <section id="about" className="relative scroll-mt-24 overflow-hidden bg-navy py-16 text-white sm:py-20 lg:py-24">
        <div className="grain pointer-events-none absolute inset-0 opacity-60" />
        <div className="pointer-events-none absolute inset-0 opacity-[0.18] [background-image:radial-gradient(rgba(255,255,255,0.35)_1px,transparent_1px)] [background-size:26px_26px] [mask-image:radial-gradient(ellipse_at_top_right,#000,transparent_72%)]" />
        <div className="pointer-events-none absolute -left-40 top-20 h-[420px] w-[420px] rounded-full bg-brand/25 blur-[110px]" />
        <div className="pointer-events-none absolute -right-24 top-1/3 h-[360px] w-[360px] rounded-full bg-brand-bright/15 blur-[100px]" />

        <div className="wrap relative">
          <div className="grid gap-10 lg:grid-cols-12 lg:items-end lg:gap-8">
            <Reveal className="lg:col-span-7">
              <span className="eyebrow !text-brand-bright">About us</span>
              <h1 className="display mt-4 text-[clamp(2.2rem,4.6vw,3.6rem)] text-white">
                Building India&apos;s leading
                <br />
                <span className="text-brand-bright/90">IT vendors network.</span>
              </h1>
              <p className="mt-5 max-w-[540px] text-[14.5px] leading-relaxed text-white/60">
                My Laptop Market is a professional B2B networking and lead-generation platform for India&apos;s IT
                industry. We connect refurbished IT dealers, technology suppliers, distributors, ITADs and corporate
                sourcing teams through one trusted business ecosystem.
              </p>
            </Reveal>
            <Reveal delay={100} className="flex flex-wrap gap-2.5 lg:col-span-5 lg:justify-end">
              <button onClick={onRegister} className="btn btn-primary h-11 min-h-0 px-5 text-[12.5px]">
                Join the network <Icon name="arrow-up-right" size={15} className="arrow" />
              </button>
              <Link
                to={routes.download}
                className="btn h-11 min-h-0 border border-white/20 bg-white/5 px-5 text-[12.5px] text-white backdrop-blur hover:bg-white/10"
              >
                Download the app
              </Link>
            </Reveal>
          </div>

          <div className="mt-12 grid gap-3.5 lg:grid-cols-12">
            <Reveal className="lg:col-span-7">
              <div className="relative h-full min-h-[320px] overflow-hidden rounded-[26px] border border-white/10">
                <img
                  src={images.lifestyle}
                  alt="Technician working with refurbished business laptops"
                  className="absolute inset-0 h-full w-full object-cover"
                  loading="eager"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-navy via-navy/45 to-transparent" />
                <div className="relative flex h-full min-h-[320px] flex-col justify-end p-6 sm:p-7">
                  <span className="mb-auto inline-flex w-fit items-center gap-1.5 rounded-full border border-white/20 bg-white/10 px-2.5 py-1 text-[9px] font-semibold text-white backdrop-blur">
                    <Icon name="scan" size={10} /> Real businesses, on the ground
                  </span>
                  <p className="font-display text-[20px] font-bold leading-tight tracking-[-0.02em] text-white sm:text-[22px]">
                    Mission: simplify vendor discovery and qualified partnerships across India&apos;s IT supply chain.
                  </p>
                  <p className="mt-2 max-w-[480px] text-[12px] leading-relaxed text-white/65">
                    Vision: become the most credible technology business network for refurbished IT trade.
                  </p>
                </div>
              </div>
            </Reveal>

            <div className="grid gap-3.5 sm:grid-cols-2 lg:col-span-5 lg:grid-cols-1">
              {[
                { value: 1200, suffix: "+", label: "IT business connections" },
                { value: 85, suffix: "+", label: "Indian cities covered" },
                { value: 40, suffix: "+", label: "IT categories covered" },
              ].map((stat, index) => (
                <Reveal key={stat.label} delay={80 + index * 60}>
                  <div className="rounded-[22px] border border-white/10 bg-white/[0.045] px-5 py-5 backdrop-blur-sm">
                    <p className="font-display text-[clamp(2rem,3.2vw,2.5rem)] font-bold tracking-[-0.04em] text-white">
                      <CountUp value={stat.value} suffix={stat.suffix} />
                    </p>
                    <p className="mt-1.5 text-[11px] font-semibold uppercase tracking-[0.14em] text-white/55">
                      {stat.label}
                    </p>
                  </div>
                </Reveal>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section className="relative bg-white py-16 sm:py-20 lg:py-24">
        <div className="wrap">
          <div className="grid gap-10 lg:grid-cols-12 lg:gap-12">
            <Reveal className="lg:col-span-5">
              <span className="eyebrow">Why this platform</span>
              <h2 className="display mt-4 text-[clamp(2rem,4vw,3.1rem)] text-navy">
                Structured networking for serious IT trade.
              </h2>
              <p className="mt-5 max-w-[420px] text-[14.5px] leading-relaxed text-slate-ink">
                Traditional industry networking often depends on limited local contacts, scattered referrals and informal
                groups. My Laptop Market gives IT businesses a clearer way to build visibility, discover suppliers and
                generate leads across India&apos;s technology trade ecosystem.
              </p>
              <div className="mt-8">
                <button onClick={onRegister} className="btn btn-dark h-11 min-h-0 px-5 text-[12.5px]">
                  List your business <Icon name="arrow-up-right" size={15} className="arrow" />
                </button>
              </div>
            </Reveal>

            <div className="grid gap-3 sm:grid-cols-2 lg:col-span-7">
              {pillars.map((item, index) => (
                <Reveal key={item.title} delay={index * 50}>
                  <article className="group h-full rounded-[22px] border border-line bg-mist/40 p-5 transition hover:-translate-y-0.5 hover:border-brand/25 hover:bg-white hover:shadow-soft sm:p-6">
                    <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand/10 text-brand transition group-hover:bg-brand group-hover:text-white">
                      <Icon name={item.icon} size={18} />
                    </span>
                    <h3 className="mt-4 text-[15px] font-bold text-navy">{item.title}</h3>
                    <p className="mt-2 text-[12.5px] leading-relaxed text-slate-ink">{item.body}</p>
                  </article>
                </Reveal>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section className="relative overflow-hidden border-y border-line/70 bg-mist/40 py-16 sm:py-20 lg:py-24">
        <div className="wrap">
          <Reveal>
            <div className="mx-auto max-w-[640px] text-center">
              <span className="eyebrow">Business benefits</span>
              <h2 className="display mt-4 text-[clamp(2rem,4vw,3rem)] text-navy">
                Built for companies that buy and sell IT stock.
              </h2>
              <p className="mt-4 text-[14px] leading-relaxed text-slate-ink">
                Whether you source lots, move refurbished inventory or recover corporate assets — the network is designed
                around how your business actually trades.
              </p>
            </div>
          </Reveal>

          <div className="mt-10 grid gap-3.5 sm:grid-cols-2 lg:grid-cols-4">
            {benefits.map((item, index) => (
              <Reveal key={item.title} delay={index * 50}>
                <article
                  className={cn(
                    "h-full rounded-[22px] border border-line bg-white p-5 sm:p-6",
                    "transition hover:-translate-y-0.5 hover:border-brand/25 hover:shadow-soft",
                  )}
                >
                  <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-navy text-white">
                    <Icon name={item.icon} size={16} />
                  </span>
                  <h3 className="mt-4 text-[14px] font-bold text-navy">{item.title}</h3>
                  <p className="mt-2 text-[12px] leading-relaxed text-slate-ink">{item.body}</p>
                </article>
              </Reveal>
            ))}
          </div>
        </div>
      </section>
    </>
  );
}
