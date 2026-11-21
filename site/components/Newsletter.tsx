import { ArrowRight, Mail } from "lucide-react";

export function Newsletter() {
  return (
    <section className="px-0 pt-7 pb-2">
      <div className="mx-auto w-[min(100%-40px,1180px)]">
        <div className="relative flex flex-wrap items-center gap-5 overflow-hidden rounded-[20px] border border-line bg-[linear-gradient(90deg,#eef4ff_0%,#f3fbf6_55%,#e8f1fc_100%)] px-8 py-7">
          <div
            className="pointer-events-none absolute inset-y-0 right-0 w-1/2 opacity-50 [background:radial-gradient(ellipse_at_80%_50%,rgba(32,202,71,0.12),transparent_55%)]"
            aria-hidden
          />
          <div className="relative flex min-w-[240px] flex-1 items-center gap-3.5">
            <div className="grid size-12 place-items-center rounded-full bg-brand-soft text-brand shadow-[0_0_0_6px_rgba(7,103,216,0.08)]">
              <Mail className="size-[18px]" />
            </div>
            <div>
              <strong className="mb-0.5 block text-[17px] text-navy">
                Stay Updated with My Laptop Market
              </strong>
              <span className="text-[13px] text-ink-2">
                Get the latest updates, features and industry insights.
              </span>
            </div>
          </div>
          <form
            className="relative flex min-w-[260px] flex-1 flex-wrap gap-2.5"
            action="mailto:support@mylaptopmarket.com"
            method="get"
          >
            <label className="relative min-w-[180px] flex-1">
              <Mail className="pointer-events-none absolute top-1/2 left-3.5 size-4 -translate-y-1/2 text-ink-3" />
              <input
                type="email"
                name="subject"
                required
                aria-label="Email address"
                placeholder="Enter your email address"
                className="h-[46px] w-full rounded-full border border-line bg-white py-0 pr-4 pl-10 text-sm outline-none focus:border-brand focus:shadow-[0_0_0_3px_rgba(7,103,216,0.15)]"
              />
            </label>
            <button
              type="submit"
              className="inline-flex h-[46px] items-center gap-2 rounded-full bg-navy px-[18px] text-sm font-bold text-white transition hover:bg-brand"
            >
              Subscribe <ArrowRight className="size-4" />
            </button>
          </form>
        </div>
      </div>
    </section>
  );
}
