import { ArrowRight, Mail } from "lucide-react";

export function Newsletter() {
  return (
    <section className="relative z-[2] -mb-2 px-0 pt-7">
      <div className="mx-auto w-[min(100%-40px,1180px)]">
        <div className="relative flex flex-wrap items-center gap-5 overflow-hidden rounded-2xl border border-line bg-white px-7 py-6 shadow-[0_12px_32px_rgba(15,40,90,0.1)]">
          <div
            className="pointer-events-none absolute inset-y-[-20%] right-[-8%] w-[48%] rotate-[-8deg] opacity-90"
            aria-hidden
            style={{
              background:
                "linear-gradient(115deg, transparent 0%, rgba(7,103,216,0.08) 28%, rgba(32,202,71,0.14) 55%, rgba(7,103,216,0.1) 78%, transparent 100%)",
            }}
          />
          <div className="relative flex min-w-[240px] flex-1 items-center gap-3.5">
            <div className="grid size-11 place-items-center rounded-xl bg-brand text-white shadow-[0_8px_18px_rgba(7,103,216,0.28)]">
              <Mail className="size-[18px]" />
            </div>
            <div>
              <strong className="mb-0.5 block text-[17px] font-bold text-navy">
                Stay Updated with My Laptop Market
              </strong>
              <span className="text-[13px] text-ink-2">
                Get the latest updates, features and industry insights.
              </span>
            </div>
          </div>
          <form
            className="relative flex min-w-[280px] flex-1 flex-wrap gap-2.5"
            action="mailto:support@mylaptopmarket.com"
            method="get"
          >
            <label className="relative min-w-[200px] flex-1">
              <Mail className="pointer-events-none absolute top-1/2 left-3.5 size-4 -translate-y-1/2 text-ink-3" />
              <input
                type="email"
                name="subject"
                required
                aria-label="Email address"
                placeholder="Enter your email address"
                className="h-[46px] w-full rounded-full border border-line bg-[#f7faff] py-0 pr-4 pl-10 text-sm outline-none focus:border-brand focus:bg-white focus:shadow-[0_0_0_3px_rgba(7,103,216,0.15)]"
              />
            </label>
            <button
              type="submit"
              className="inline-flex h-[46px] items-center gap-2 rounded-full bg-brand px-5 text-sm font-bold text-white shadow-[0_8px_18px_rgba(7,103,216,0.28)] transition hover:bg-brand-dark"
            >
              Subscribe <ArrowRight className="size-4" />
            </button>
          </form>
        </div>
      </div>
    </section>
  );
}
