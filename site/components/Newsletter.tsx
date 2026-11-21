import { ArrowRight, Mail } from "lucide-react";

export function Newsletter() {
  return (
    <section className="px-0 pt-7 pb-2">
      <div className="mx-auto w-[min(100%-40px,1180px)]">
        <div className="flex flex-wrap items-center gap-5 rounded-[20px] border border-line bg-[linear-gradient(90deg,#eef4ff,#f3fbf6)] px-8 py-7">
          <div className="flex min-w-[240px] flex-1 items-center gap-3.5">
            <div className="grid size-12 place-items-center rounded-full bg-brand-soft text-brand">
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
            className="flex min-w-[260px] flex-1 flex-wrap gap-2.5"
            action="mailto:support@mylaptopmarket.com"
            method="get"
          >
            <input
              type="email"
              name="subject"
              required
              aria-label="Email address"
              placeholder="Enter your email address"
              className="h-[46px] min-w-[180px] flex-1 rounded-full border border-line bg-white px-4 text-sm outline-none focus:border-brand focus:shadow-[0_0_0_3px_rgba(7,103,216,0.15)]"
            />
            <button
              type="submit"
              className="inline-flex h-[46px] items-center gap-2 rounded-full bg-brand px-[18px] text-sm font-bold text-white"
            >
              Subscribe <ArrowRight className="size-4" />
            </button>
          </form>
        </div>
      </div>
    </section>
  );
}
