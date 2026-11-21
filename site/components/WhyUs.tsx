import {
  ArrowRight,
  Bolt,
  Briefcase,
  Check,
  LayoutGrid,
  MapPin,
  Play,
  Smartphone,
  TrendingUp,
  Users,
} from "lucide-react";

export function WhyUs() {
  return (
    <section className="bg-[#f4f8ff] px-0 py-[72px]" id="whyUs">
      <div className="mx-auto grid w-[min(100%-40px,1180px)] items-center gap-10 md:grid-cols-[1.1fr_0.9fr]">
        <div>
          <div className="mb-3 inline-flex h-7 items-center gap-2 rounded-full bg-teal-500/10 px-3 text-xs font-bold text-teal-700">
            <Check className="size-3.5" /> Why Us
          </div>
          <h2 className="mb-3.5 text-[clamp(1.8rem,3vw,2.4rem)] font-bold leading-[1.15] text-navy">
            Why should you <span className="mark">register here?</span>
          </h2>
          <p className="mb-5 text-base leading-relaxed text-ink-2">
            India&apos;s only B2B platform built{" "}
            <strong>exclusively for IT vendors, dealers, distributors, and suppliers</strong> — not
            a general marketplace, not a classifieds site.
          </p>
          <ul className="mb-6 grid gap-2.5 sm:grid-cols-2">
            {[
              [Check, "Verified vendor profiles only"],
              [Users, "1,200+ active IT businesses"],
              [Bolt, "Instant nationwide visibility"],
              [Smartphone, "Mobile app + web platform"],
            ].map(([Icon, text]) => {
              const I = Icon as typeof Check;
              return (
                <li
                  key={String(text)}
                  className="flex items-start gap-2.5 rounded-xl border border-line bg-white/80 p-3 text-[13px] font-semibold text-ink"
                >
                  <span className="grid size-7 shrink-0 place-items-center rounded-lg bg-brand-soft text-brand">
                    <I className="size-3.5" />
                  </span>
                  {String(text)}
                </li>
              );
            })}
          </ul>
          <div className="flex flex-wrap gap-3">
            <a
              href="https://mylaptopmarket.com/api/vendor/register"
              className="inline-flex h-[50px] items-center gap-2 rounded-full bg-brand px-6 text-[15px] font-bold text-white"
            >
              Register Free <ArrowRight className="size-4" />
            </a>
            <a
              href="/why-us.html"
              className="inline-flex h-[50px] items-center gap-2 rounded-full border-[1.5px] border-brand/35 bg-white px-6 text-[15px] font-bold text-brand"
            >
              <Play className="size-4 fill-brand" /> See inside the platform
            </a>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3.5">
          {[
            [Briefcase, "1,200+", "Registered IT Businesses"],
            [MapPin, "85+", "Indian Cities Covered"],
            [LayoutGrid, "40+", "IT Product Categories"],
            [TrendingUp, "100%", "B2B Focused Platform"],
          ].map(([Icon, value, label]) => {
            const I = Icon as typeof Briefcase;
            return (
              <div
                key={String(label)}
                className="rounded-2xl border border-line bg-white px-[18px] py-[22px] text-center shadow-[0_16px_40px_rgba(15,40,90,0.1)]"
              >
                <span className="mx-auto mb-2.5 grid size-10 place-items-center rounded-xl bg-brand-soft text-brand">
                  <I className="size-5" />
                </span>
                <strong className="mb-1.5 block text-[2rem] leading-none text-brand tabular-nums">
                  {String(value)}
                </strong>
                <span className="block text-[11px] font-bold tracking-wide text-ink-3 uppercase">
                  {String(label)}
                </span>
              </div>
            );
          })}
        </div>
      </div>

      <div className="mx-auto mt-10 flex w-[min(100%-40px,1180px)] flex-wrap items-center gap-4 rounded-[14px] bg-[#eef2f7] px-[22px] py-[18px]">
        <div className="rounded-lg border border-slate-900/15 bg-white px-3 py-2 text-[11px] font-extrabold tracking-wider text-ink-2 uppercase">
          Trusted by leading brands
        </div>
        <div className="flex flex-wrap items-center gap-[18px] text-lg font-extrabold tracking-tight text-slate-700/75">
          <span>DELL</span>
          <span>HP</span>
          <span>Lenovo</span>
          <span>acer</span>
          <span>ASUS</span>
        </div>
        <em className="ml-auto text-[13px] font-semibold not-italic text-ink-3">And many more…</em>
      </div>
    </section>
  );
}
