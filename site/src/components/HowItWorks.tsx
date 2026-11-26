import { useEffect, useState, type ReactNode } from "react";
import { cn } from "../utils/cn";
import { Icon } from "./Icon";
import { Reveal, useInView } from "../lib/motion";
import { SITE_ORIGIN } from "../lib/api";
import { images } from "../assets";

const REGISTER_URL = `${SITE_ORIGIN}/api/vendor/register`;

function Typed({ text }: { text: string }) {
  const { ref, inView } = useInView<HTMLSpanElement>(0.6);
  const [n, setN] = useState(0);

  useEffect(() => {
    if (!inView) return;
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
      setN(text.length);
      return;
    }
    let i = 0;
    const id = window.setInterval(() => {
      i += 1;
      setN(i);
      if (i >= text.length) window.clearInterval(id);
    }, 65);
    return () => window.clearInterval(id);
  }, [inView, text]);

  return (
    <span ref={ref}>
      {text.slice(0, n)}
      <span className="caret text-brand" aria-hidden />
    </span>
  );
}

function Mini({ children }: { children: ReactNode }) {
  return <p className="text-[10px] font-bold uppercase tracking-[0.1em] text-slate-ink">{children}</p>;
}

function Chip({
  label,
  on,
  delay,
  dark,
}: {
  label: string;
  on: boolean;
  delay: number;
  dark?: boolean;
}) {
  const { ref, inView } = useInView<HTMLSpanElement>(0.5);
  const active = inView && on;

  return (
    <span
      ref={ref}
      style={{ transitionDelay: `${delay}ms` }}
      className={cn(
        "rounded-full border px-3 py-1.5 text-[12px] font-semibold transition-all duration-300",
        active
          ? "border-brand bg-brand text-white"
          : dark
            ? "border-white/20 bg-transparent text-white"
            : "border-line bg-white text-navy",
      )}
    >
      {label}
    </span>
  );
}

function RegisterVisual() {
  const cats: [string, boolean][] = [
    ["Laptops", true],
    ["Servers", true],
    ["Networking", false],
    ["Monitors", true],
    ["Components", false],
  ];
  const areas: [string, boolean][] = [
    ["Delhi NCR", true],
    ["Pan-India", true],
  ];

  return (
    <div className="w-full max-w-[350px] rounded-2xl border border-line bg-white p-5 shadow-[0_28px_60px_-32px_rgba(5,7,11,0.4)]">
      <p className="text-[10px] font-bold uppercase tracking-[0.12em] text-slate-ink">Business profile</p>
      <div className="mt-4 rounded-xl border border-line px-3.5 py-3">
        <Mini>Business name</Mini>
        <p className="mt-1.5 text-[15px] font-bold text-navy">
          <Typed text="Your IT business" />
        </p>
      </div>
      <div className="mt-4">
        <Mini>Categories</Mini>
        <div className="mt-2 flex flex-wrap gap-1.5">
          {cats.map(([label, on], i) => (
            <Chip key={label} label={label} on={on} delay={1100 + i * 200} />
          ))}
        </div>
      </div>
      <div className="mt-4">
        <Mini>Service areas</Mini>
        <div className="mt-2 flex flex-wrap gap-1.5">
          {areas.map(([label, on], i) => (
            <Chip key={label} label={label} on={on} delay={2200 + i * 200} />
          ))}
        </div>
      </div>
      <a
        href={REGISTER_URL}
        className="mt-5 flex h-11 items-center justify-between rounded-full bg-navy pl-5 pr-1.5 text-[13px] font-bold text-white transition hover:bg-navy/90"
      >
        Create profile
        <span className="grid h-8 w-8 place-items-center rounded-full bg-brand">
          <Icon name="arrow-right" size={14} strokeWidth={2.4} />
        </span>
      </a>
    </div>
  );
}

function VerifyVisual() {
  const { ref, inView } = useInView<HTMLDivElement>(0.45);
  const items = ["Company information", "Categories & service areas", "Profile reviewed"];

  return (
    <div ref={ref} className="relative w-full max-w-[350px] rounded-2xl border border-white/10 bg-navy-soft p-5">
      <div className="flex items-center gap-3">
        <span className="grid h-11 w-11 place-items-center rounded-xl bg-white text-[12px] font-bold text-navy">IT</span>
        <div>
          <p className="text-[15px] font-bold text-white">Your IT business</p>
          <p className="mt-1 text-[10px] font-bold uppercase tracking-[0.1em] text-white/50">Profile review</p>
        </div>
      </div>
      <ul className="mt-5 space-y-2.5">
        {items.map((item, i) => (
          <li
            key={item}
            className="flex items-center justify-between rounded-xl border border-white/10 px-3.5 py-3 text-[14px] text-white"
          >
            {item}
            <span
              className={cn(
                "grid h-6 w-6 place-items-center rounded-full bg-brand text-white transition-all duration-500",
                inView ? "scale-100 opacity-100" : "scale-0 opacity-0",
              )}
              style={{ transitionDelay: `${500 + i * 350}ms` }}
            >
              <Icon name="check" size={12} strokeWidth={2.8} />
            </span>
          </li>
        ))}
      </ul>
      <div
        className={cn(
          "absolute -right-3 -top-7 grid h-[92px] w-[92px] place-items-center rounded-full border-2 border-white bg-brand text-white shadow-[0_16px_40px_-10px_rgba(7,103,216,0.9)] transition-all duration-700",
          inView ? "scale-100 rotate-[-12deg] opacity-100" : "scale-150 rotate-[-34deg] opacity-0",
        )}
        style={{ transitionDelay: "1750ms", transitionTimingFunction: "cubic-bezier(0.34, 1.56, 0.64, 1)" }}
      >
        <span className="flex flex-col items-center gap-1">
          <Icon name="badge" size={22} strokeWidth={1.8} />
          <span className="text-[9px] font-bold uppercase tracking-[0.12em]">Verified</span>
        </span>
      </div>
    </div>
  );
}

const NODES = [
  { t: "Suppliers", x: 21, y: 15 },
  { t: "Dealers", x: 80, y: 11 },
  { t: "Distributors", x: 79, y: 59 },
  { t: "Service providers", x: 58, y: 92 },
  { t: "Asset managers", x: 24, y: 72 },
];

function ConnectVisual() {
  const { ref, inView } = useInView<HTMLDivElement>(0.4);

  return (
    <div ref={ref} className="relative aspect-square w-full max-w-[340px]">
      <svg viewBox="0 0 100 100" preserveAspectRatio="none" className="absolute inset-0 h-full w-full overflow-visible" aria-hidden>
        {NODES.map((n, i) => (
          <line
            key={n.t}
            x1={50}
            y1={46}
            x2={n.x}
            y2={n.y}
            stroke="#fff"
            strokeWidth={1.6}
            vectorEffect="non-scaling-stroke"
            className="dash-flow"
            style={{
              opacity: inView ? 0.75 : 0,
              transition: `opacity 0.5s ease ${350 + i * 140}ms`,
            }}
          />
        ))}
      </svg>
      <div
        className={cn(
          "absolute left-1/2 top-[46%] grid h-[84px] w-[84px] -translate-x-1/2 -translate-y-1/2 place-items-center rounded-full bg-white text-navy shadow-[0_18px_40px_-12px_rgba(5,7,11,0.5)] transition-transform duration-500",
          inView ? "scale-100" : "scale-0",
        )}
        style={{ transitionTimingFunction: "cubic-bezier(0.34, 1.56, 0.64, 1)" }}
      >
        <span className="absolute inset-0 animate-ping rounded-full border border-white opacity-50 motion-reduce:hidden" />
        <img src={images.logoMark} alt="" className="relative h-10 w-10 object-contain" />
      </div>
      {NODES.map((n, i) => (
        <span
          key={n.t}
          style={{
            left: `${n.x}%`,
            top: `${n.y}%`,
            transitionDelay: `${500 + i * 140}ms`,
          }}
          className={cn(
            "absolute -translate-x-1/2 -translate-y-1/2 whitespace-nowrap rounded-full bg-navy px-3.5 py-2 text-[12px] font-bold text-white transition-all duration-500",
            inView ? "scale-100 opacity-100" : "scale-0 opacity-0",
          )}
        >
          {n.t}
        </span>
      ))}
    </div>
  );
}

const STEPS = [
  {
    n: "01",
    title: "Register your business",
    body: "Create a professional profile with categories, service areas, and capabilities.",
    tone: "border border-line bg-mist text-navy",
    Visual: RegisterVisual,
  },
  {
    n: "02",
    title: "Get verified",
    body: "Strengthen credibility with a reviewed presence and clear company information.",
    tone: "bg-navy text-white",
    Visual: VerifyVisual,
  },
  {
    n: "03",
    title: "Connect & grow",
    body: "Reach suppliers, dealers, distributors, asset managers, and service providers.",
    tone: "bg-brand text-white",
    Visual: ConnectVisual,
  },
];

export function HowItWorks(_opts?: { onRegister?: () => void }) {
  void _opts;
  return (
    <section id="how" className="relative scroll-mt-24 bg-white py-16 sm:py-20 lg:py-28">
      <div className="wrap grid gap-12 lg:grid-cols-12 lg:gap-10">
        <div className="lg:col-span-4">
          <div className="lg:sticky lg:top-28">
            <Reveal>
              <span className="eyebrow">Three simple steps</span>
              <h2 className="display mt-5 text-[clamp(2.2rem,4.6vw,3.8rem)] text-navy">
                How it <span className="text-brand">works</span>
              </h2>
            </Reveal>
            <Reveal delay={80}>
              <p className="mt-5 max-w-xs text-[15px] leading-relaxed text-slate-ink">
                Three simple steps to trade, connect and grow on{" "}
                <strong className="font-bold text-navy">My Laptop Market</strong>.
              </p>
              <div className="mt-8">
                <a href={REGISTER_URL} className="btn btn-dark h-11 min-h-0 px-5 text-[12.5px]">
                  Register Your Business <Icon name="arrow-up-right" size={15} className="arrow" />
                </a>
              </div>
            </Reveal>
          </div>
        </div>

        <ol className="lg:col-span-8">
          {STEPS.map((step, i) => (
            <li
              key={step.n}
              className="mb-6 list-none last:mb-0 lg:sticky lg:mb-10"
              style={{ top: `calc(6.75rem + ${i * 1.75}rem)` }}
            >
              <Reveal delay={i * 60}>
                <article
                  className={cn(
                    "grid gap-10 overflow-hidden rounded-[2rem] p-7 sm:p-10 md:min-h-[440px] md:grid-cols-2 md:gap-8",
                    step.tone,
                  )}
                >
                  <div className="flex flex-col">
                    <span className="flex items-center gap-3 text-[11px] font-bold uppercase tracking-[0.14em] opacity-70">
                      Step {step.n}
                      <span aria-hidden className="h-px w-10 bg-current opacity-40" />
                    </span>
                    <h3 className="display mt-8 text-[clamp(1.7rem,3vw,2.6rem)] md:mt-auto md:pt-10">{step.title}</h3>
                    <p className="mt-4 max-w-sm text-[15px] leading-relaxed opacity-80">{step.body}</p>
                  </div>
                  <div className="grid place-items-center">
                    <step.Visual />
                  </div>
                </article>
              </Reveal>
            </li>
          ))}
        </ol>
      </div>
    </section>
  );
}
