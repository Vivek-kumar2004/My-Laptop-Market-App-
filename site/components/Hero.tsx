import Link from "next/link";
import type { ReactNode } from "react";
import {
  ArrowRight,
  Bolt,
  Play,
  Shield,
  Users,
  Laptop,
  Monitor,
  Server,
  Cpu,
  MemoryStick,
  HardDrive,
  Puzzle,
  Bell,
  ShoppingCart,
  Upload,
  List,
  ClipboardList,
  MessageSquare,
  Plus,
  Store,
  User,
} from "lucide-react";

const cats = [
  { href: "/products.html?category=Laptops", label: "Laptops", Icon: Laptop },
  { href: "/products.html?category=Desktops", label: "Desktops", Icon: Monitor },
  { href: "/products.html?category=Monitors", label: "Monitors", Icon: Monitor },
  { href: "/products.html?category=Servers", label: "Servers", Icon: Server },
  { href: "/products.html?category=Graphics%20Cards", label: "Graphics Cards", Icon: Cpu },
  { href: "/products.html?category=RAM", label: "RAM", Icon: MemoryStick },
  { href: "/products.html?category=SSD", label: "SSD", Icon: HardDrive },
  { href: "/products.html", label: "Modules & Components", Icon: Puzzle },
];

export function Hero() {
  return (
    <section className="relative overflow-hidden bg-[linear-gradient(135deg,#e8f1ff_0%,#f5f9ff_42%,#e7f0fe_100%)] px-0 pb-6 pt-10">
      <div
        className="pointer-events-none absolute inset-0 opacity-45 [background-image:radial-gradient(rgba(7,103,216,0.14)_1.2px,transparent_1.2px)] [background-size:22px_22px] [mask-image:linear-gradient(90deg,transparent_0%,#000_55%,#000_100%)]"
        aria-hidden
      />

      <div className="relative mx-auto grid w-[min(100%-40px,1180px)] items-center gap-9 md:grid-cols-[1.05fr_0.95fr]">
        <div>
          <div className="mb-4 inline-flex h-[30px] items-center gap-2 rounded-full border border-line bg-white/85 px-3 text-xs font-bold text-brand">
            <Shield className="size-3.5" />
            India&apos;s Premier B2B Platform
          </div>
          <h1 className="mb-4 max-w-[14ch] text-[clamp(2rem,4vw,3.1rem)] font-bold leading-[1.08] tracking-[-0.03em] text-navy md:max-w-none">
            Turn Industry Connections into Qualified{" "}
            <span className="mark">Business Leads</span>
          </h1>
          <p className="mb-6 max-w-[36rem] text-base leading-relaxed text-ink-2">
            Build visibility among Refurbished IT Dealers India, Networking Equipment Vendors,
            ITAD companies, and corporate IT sourcing teams.
          </p>
          <div className="mb-5 flex flex-wrap gap-3">
            <a
              href="https://mylaptopmarket.com/api/vendor/register"
              className="inline-flex h-[50px] items-center gap-2 rounded-full bg-brand px-6 text-[15px] font-bold text-white shadow-[0_10px_24px_rgba(7,103,216,0.28)] transition hover:-translate-y-px hover:bg-brand-dark"
            >
              Register Your Business
              <ArrowRight className="size-4" />
            </a>
            <Link
              href="/about.html"
              className="inline-flex h-[50px] items-center gap-2 rounded-full border-[1.5px] border-brand/35 bg-white px-6 text-[15px] font-bold text-brand transition hover:bg-brand-soft"
            >
              <Play className="size-4 fill-brand" />
              Know More
            </Link>
          </div>
          <div className="flex flex-wrap gap-x-[22px] gap-y-4">
            <TrustItem icon={<Users className="size-3.5" />} text="1000+ Verified Dealers" />
            <TrustItem icon={<Shield className="size-3.5" />} text="Trusted Platform Across India" />
            <TrustItem icon={<Bolt className="size-3.5" />} text="Buy · Sell · Connect · Grow" />
          </div>
        </div>

        <div className="relative flex min-h-[440px] items-center justify-center" aria-hidden>
          <div className="relative z-[1] w-full max-w-[460px] rounded-t-[14px] rounded-b-md bg-slate-800 px-2.5 pt-2.5 shadow-[0_16px_40px_rgba(15,40,90,0.1)]">
            <div className="grid min-h-[268px] grid-cols-[44px_1fr] overflow-hidden rounded-lg bg-[#f4f7fc]">
              <aside className="flex flex-col items-center gap-2.5 bg-[#0b1b3a] py-3.5">
                {[0, 1, 2, 3, 4].map((i) => (
                  <span
                    key={i}
                    className={`size-4 rounded-[5px] ${i === 0 ? "bg-brand" : "bg-white/22"}`}
                  />
                ))}
              </aside>
              <div className="p-3">
                <div className="mb-2.5 flex items-center justify-between">
                  <strong className="text-[13px] text-navy">Dashboard</strong>
                  <em className="rounded-full bg-green-soft px-2 py-0.5 text-[11px] font-bold not-italic text-green">
                    Live
                  </em>
                </div>
                <div className="mb-2.5 grid grid-cols-3 gap-2">
                  <DashStat label="Total Listings" value="1,250" />
                  <DashStat label="Active Buyers" value="850" green />
                  <DashStat label="Total Messages" value="420" />
                </div>
                <div className="rounded-[10px] border border-line bg-white p-2.5">
                  <div className="mb-1.5 text-[11px] font-bold text-ink-2">Business Growth</div>
                  <svg viewBox="0 0 280 90" className="block h-[72px] w-full" preserveAspectRatio="none">
                    <defs>
                      <linearGradient id="gFill" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor="#0767d8" stopOpacity="0.35" />
                        <stop offset="100%" stopColor="#0767d8" stopOpacity="0" />
                      </linearGradient>
                    </defs>
                    <path
                      d="M0 70 C40 65 55 40 90 45 C130 52 150 20 190 28 C230 36 250 18 280 22 L280 90 L0 90 Z"
                      fill="url(#gFill)"
                    />
                    <path
                      d="M0 70 C40 65 55 40 90 45 C130 52 150 20 190 28 C230 36 250 18 280 22"
                      fill="none"
                      stroke="#0767d8"
                      strokeWidth="3"
                      strokeLinecap="round"
                    />
                  </svg>
                </div>
              </div>
            </div>
            <div className="-mx-4 h-3 rounded-b-[10px] bg-slate-600" />
          </div>

          <div className="absolute bottom-[6%] right-[2%] z-[2] w-[168px] rounded-[22px] border-2 border-slate-800 bg-slate-900 p-2.5 pb-3 shadow-[0_22px_50px_rgba(15,40,90,0.28)] max-[900px]:right-[6%] max-[900px]:w-[140px]">
            <div className="mx-auto mb-2 h-[5px] w-[54px] rounded bg-slate-600" />
            <div className="rounded-[14px] bg-white p-2.5">
              <div className="mb-2 flex items-center justify-between text-[9px] text-brand">
                <strong>MY LAPTOP MARKET</strong>
                <Bell className="size-3" />
              </div>
              <div className="mb-2 rounded-[10px] bg-brand p-2.5 text-white">
                <span className="mb-2 block text-[10px] font-bold">Dealer Registration</span>
                <a
                  href="https://mylaptopmarket.com/api/vendor/register"
                  className="inline-flex rounded-full bg-white px-2.5 py-1.5 text-[10px] font-extrabold text-brand"
                >
                  Register as Dealer →
                </a>
              </div>
              <a
                href="/products.html"
                className="mb-1.5 flex h-8 items-center justify-center gap-1.5 rounded-lg bg-green text-[11px] font-extrabold text-white"
              >
                <ShoppingCart className="size-3.5" /> BUY STOCK
              </a>
              <a
                href="https://mylaptopmarket.com/api/vendor/register"
                className="mb-1.5 flex h-8 items-center justify-center gap-1.5 rounded-lg bg-brand text-[11px] font-extrabold text-white"
              >
                <Upload className="size-3.5" /> SELL STOCK
              </a>
              <div className="grid grid-cols-2 gap-1.5">
                <PhoneTile icon={<List className="size-3" />} label="Latest Listings" />
                <PhoneTile icon={<ClipboardList className="size-3" />} label="Requirements" />
                <PhoneTile icon={<MessageSquare className="size-3" />} label="Messages" />
                <PhoneTile icon={<Plus className="size-3" />} label="Add Product" />
                <PhoneTile icon={<Store className="size-3" />} label="Floor Desk" />
                <PhoneTile icon={<User className="size-3" />} label="My Profile" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="relative z-[2] mx-auto mt-7 w-[min(100%-40px,1180px)]">
        <div className="no-scrollbar flex gap-2 overflow-x-auto rounded-[18px] border border-line bg-white px-4 py-3.5 shadow-[0_8px_24px_rgba(15,40,90,0.08)]">
          {cats.map(({ href, label, Icon }) => (
            <a
              key={label}
              href={href}
              className="inline-flex h-10 shrink-0 items-center gap-2 rounded-full bg-brand-soft px-3.5 text-[13px] font-bold whitespace-nowrap text-navy transition hover:bg-brand hover:text-white"
            >
              <Icon className="size-4 text-brand group-hover:text-white" />
              {label}
            </a>
          ))}
        </div>
      </div>
    </section>
  );
}

function TrustItem({ icon, text }: { icon: ReactNode; text: string }) {
  return (
    <div className="inline-flex items-center gap-2 text-[13px] font-semibold text-ink-2">
      <span className="grid size-7 place-items-center rounded-lg bg-brand-soft text-brand">
        {icon}
      </span>
      {text}
    </div>
  );
}

function DashStat({
  label,
  value,
  green,
}: {
  label: string;
  value: string;
  green?: boolean;
}) {
  return (
    <div className="rounded-[10px] border border-line bg-white px-2 py-2.5">
      <span className="mb-1 block text-[10px] text-ink-3">{label}</span>
      <strong className={`text-base tabular-nums ${green ? "text-green" : "text-navy"}`}>
        {value}
      </strong>
    </div>
  );
}

function PhoneTile({ icon, label }: { icon: ReactNode; label: string }) {
  return (
    <span className="grid place-items-center gap-0.5 rounded-lg bg-[#eef4ff] px-1 py-2 text-center text-[8px] font-bold text-ink-2">
      <span className="text-brand">{icon}</span>
      {label}
    </span>
  );
}
