import {
  Box,
  Building2,
  Handshake,
  Megaphone,
  Search,
  Shield,
  UserPlus,
  Users,
} from "lucide-react";

export function HowItWorks() {
  return (
    <section className="px-0 py-[72px]">
      <div className="mx-auto w-[min(100%-40px,1180px)]">
        <div className="mb-14 grid items-center gap-7 md:grid-cols-[1.15fr_0.85fr]">
          <div>
            <div className="mb-7 max-w-[40rem]">
              <h2 className="mb-2.5 text-[clamp(1.7rem,3vw,2.35rem)] font-bold text-navy">
                How it works
              </h2>
              <p className="text-base text-ink-2">
                Three simple steps to trade, connect and grow on{" "}
                <strong className="text-green">My Laptop Market</strong>.
              </p>
            </div>
            <div className="grid gap-3">
              {[
                {
                  Icon: UserPlus,
                  num: "Step 01",
                  title: "Register your business",
                  body: "Create a professional profile with categories, service areas, and capabilities.",
                },
                {
                  Icon: Shield,
                  num: "Step 02",
                  title: "Get verified",
                  body: "Strengthen credibility with a reviewed presence and clear company information.",
                },
                {
                  Icon: Handshake,
                  num: "Step 03",
                  title: "Connect & grow",
                  body: "Reach suppliers, dealers, distributors, asset managers, and service providers.",
                },
              ].map(({ Icon, num, title, body }) => (
                <div
                  key={num}
                  className="grid grid-cols-[56px_1fr] items-start gap-3.5 rounded-2xl border border-line bg-white px-5 py-[18px] shadow-[0_8px_24px_rgba(15,40,90,0.08)]"
                >
                  <span className="grid size-14 place-items-center rounded-2xl bg-brand-soft text-brand">
                    <Icon className="size-6" />
                  </span>
                  <div>
                    <span className="mb-1.5 block text-xs font-extrabold text-brand">{num}</span>
                    <h3 className="mb-2 text-lg font-bold text-navy">{title}</h3>
                    <p className="text-sm leading-relaxed text-ink-2">{body}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
          <div className="overflow-hidden rounded-[20px]">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src="/images/lap2.jpg"
              alt="IT hardware traded on My Laptop Market"
              className="h-auto w-full object-cover"
            />
          </div>
        </div>

        <div className="mb-6 max-w-[40rem]">
          <div className="mb-3 inline-flex h-7 items-center gap-2 rounded-full bg-green-soft px-3 text-xs font-bold text-[#0f9a38]">
            Our Platform
          </div>
          <h2 className="mb-2.5 text-[clamp(1.7rem,3vw,2.35rem)] font-bold text-navy">
            What the platform <span className="mark text-green">supports</span>
          </h2>
          <p className="text-base text-ink-2">
            Registration, discovery, lead generation, matchmaking, and industry collaboration for IT
            businesses.
          </p>
        </div>

        <div className="grid gap-3 md:grid-cols-3">
          {[
            {
              Icon: Search,
              title: "Verified business profiles",
              body: "Searchable presence that trade partners can trust.",
            },
            {
              Icon: Users,
              title: "Supplier & dealer discovery",
              body: "Find the right counterpart beyond local referrals.",
            },
            {
              Icon: Megaphone,
              title: "B2B lead generation",
              body: "Attract inquiries from companies that buy and sell IT hardware.",
            },
            {
              Icon: Handshake,
              title: "Business matchmaking",
              body: "Connect for distribution, sourcing, and service partnerships.",
            },
            {
              Icon: Box,
              title: "Corporate asset sourcing",
              body: "Reach teams moving surplus and enterprise equipment.",
            },
            {
              Icon: Building2,
              title: "Industry collaboration",
              body: "Stay visible inside India’s IT vendor network.",
            },
          ].map(({ Icon, title, body }) => (
            <div
              key={title}
              className="flex items-center gap-3.5 rounded-xl border border-line bg-white p-4 shadow-[0_8px_24px_rgba(15,40,90,0.08)]"
            >
              <span className="grid size-[42px] shrink-0 place-items-center rounded-xl bg-brand-soft text-brand">
                <Icon className="size-5" />
              </span>
              <div>
                <h3 className="mb-1 text-sm font-bold text-navy">{title}</h3>
                <p className="text-xs leading-snug text-ink-3">{body}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
