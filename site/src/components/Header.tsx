import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { cn } from "../utils/cn";
import { brands, type Product } from "../data";
import { BrandLogo, Icon } from "./Icon";
import { useCatalog } from "../lib/catalog";
import { useScrolled } from "../lib/motion";
import { navItems, routes } from "../lib/routes";

const pageLinks = [
  { label: "Home", href: routes.home },
  { label: "Live marketplace", href: routes.marketplace },
  { label: "Why My Laptop Market", href: routes.why },
  { label: "How it works", href: routes.how },
  { label: "Popular vendors", href: routes.vendors },
  { label: "Insights & guides", href: routes.insights },
  { label: "FAQs", href: routes.faq },
];

export function Header({
  onRegister,
  onOpenProduct,
}: {
  onRegister: () => void;
  onOpenProduct: (product: Product) => void;
}) {
  const scrolled = useScrolled(24);
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);
  const [paletteOpen, setPaletteOpen] = useState(false);

  const active = useMemo(() => {
    const path = location.pathname.replace(/\/index\.html$/, "").replace(/\/?$/, "/") || "/";
    if (path.startsWith("/why")) return "why";
    if (path.startsWith("/how")) return "how";
    if (path.startsWith("/vendors")) return "vendors";
    if (path.startsWith("/insights")) return "insights";
    if (location.hash === "#marketplace") return "marketplace";
    return path === "/" ? "marketplace" : "";
  }, [location.pathname, location.hash]);

  const openPalette = () => {
    setMenuOpen(false);
    setPaletteOpen(true);
  };

  useEffect(() => {
    const onKey = (event: KeyboardEvent) => {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
        event.preventDefault();
        setPaletteOpen((value) => !value);
      }
      if (event.key === "Escape") {
        setPaletteOpen(false);
        setMenuOpen(false);
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, []);

  useEffect(() => {
    document.body.style.overflow = menuOpen || paletteOpen ? "hidden" : "";
    return () => {
      document.body.style.overflow = "";
    };
  }, [menuOpen, paletteOpen]);

  return (
    <>
      <div className="relative z-[60] bg-navy text-white">
        <div className="wrap flex h-9 items-center justify-between gap-4 text-[11px] font-medium">
          <span className="hidden items-center gap-2 text-white/70 sm:flex">
            <span className="relative flex h-1.5 w-1.5">
              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-verified opacity-75" />
              <span className="relative inline-flex h-1.5 w-1.5 rounded-full bg-verified" />
            </span>
            India&apos;s B2B marketplace for refurbished IT
          </span>
          <div className="flex min-w-0 flex-1 items-center gap-2 overflow-hidden sm:hidden">
            <span className="h-1.5 w-1.5 shrink-0 rounded-full bg-verified" />
            <div className="marquee-mask relative min-w-0 flex-1 overflow-hidden">
              <div className="animate-marquee flex w-max gap-8 whitespace-nowrap text-white/70">
                {[...listingTicker, ...listingTicker].map((item, index) => (
                  <span key={`${item}-${index}`}>{item}</span>
                ))}
              </div>
            </div>
          </div>
          <div className="hidden items-center gap-5 text-white/65 sm:flex">
            <a className="inline-flex items-center gap-1.5 transition hover:text-white" href="tel:+919718199976">
              <Icon name="phone" size={12} /> +91 97181 99976
            </a>
            <span className="h-3 w-px bg-white/15" />
            <Link className="inline-flex items-center gap-1.5 transition hover:text-white" to={routes.vendors}>
              <Icon name="store" size={12} /> Become a vendor
            </Link>
          </div>
        </div>
      </div>

      <header
        className={cn(
          "sticky top-0 z-50 transition-all duration-500",
          scrolled ? "border-b border-line/80 bg-white/85 shadow-[0_8px_30px_-24px_rgba(11,27,58,0.5)] backdrop-blur-xl" : "bg-white/0",
        )}
      >
        <div className={cn("wrap flex items-center justify-between gap-6 transition-all duration-500", scrolled ? "h-[68px]" : "h-[82px]")}>
          <Link to={routes.home} className="group flex shrink-0 items-center" aria-label="My Laptop Market home">
            <BrandLogo className="transition-transform duration-500 group-hover:scale-[1.02]" />
          </Link>

          <nav className="hidden items-center gap-1 lg:flex" aria-label="Main navigation">
            {navItems.map((item) => (
              <Link
                key={item.id}
                to={item.href}
                className={cn(
                  "relative rounded-full px-3.5 py-2 text-[13px] font-semibold transition-colors",
                  active === item.id ? "text-brand" : "text-navy/70 hover:text-navy",
                )}
              >
                {item.label}
                <span
                  className={cn(
                    "absolute inset-x-3.5 -bottom-0.5 h-[2px] origin-left rounded-full bg-brand transition-transform duration-300",
                    active === item.id ? "scale-x-100" : "scale-x-0",
                  )}
                />
              </Link>
            ))}
          </nav>

          <div className="flex items-center gap-2">
            <button
              onClick={openPalette}
              className="group hidden h-10 items-center gap-2 rounded-full border border-line bg-mist px-3 text-[12.5px] font-medium text-slate-ink transition hover:border-brand/30 hover:bg-white hover:text-navy sm:flex"
              aria-label="Search the marketplace"
            >
              <Icon name="search" size={15} />
              <span className="hidden md:inline">Search stock…</span>
              <kbd className="ml-1 hidden items-center gap-0.5 rounded-md border border-line bg-white px-1.5 py-0.5 font-sans text-[10px] font-semibold text-slate-ink/70 md:inline-flex">
                ⌘K
              </kbd>
            </button>
            <a
              href="https://mylaptopmarket.com/api/vendor/login"
              className="hidden rounded-full px-3 py-2 text-[13px] font-semibold text-navy/70 transition hover:text-navy xl:block"
            >
              Sign in
            </a>
            <button onClick={onRegister} className="btn btn-primary hidden h-10 min-h-0 px-4 text-[12.5px] sm:inline-flex">
              List your business
              <Icon name="arrow-up-right" size={14} className="arrow" />
            </button>
            <button
              onClick={() => setMenuOpen((value) => !value)}
              className="flex h-10 w-10 items-center justify-center rounded-full border border-line bg-white text-navy transition hover:bg-mist lg:hidden"
              aria-label={menuOpen ? "Close menu" : "Open menu"}
              aria-expanded={menuOpen}
            >
              <Icon name={menuOpen ? "x" : "menu"} size={19} />
            </button>
          </div>
        </div>

        <div
          className={cn(
            "overflow-hidden border-line bg-white/97 backdrop-blur-xl transition-[max-height,opacity] duration-500 lg:hidden",
            menuOpen ? "max-h-[80vh] border-t opacity-100" : "max-h-0 opacity-0",
          )}
        >
          <div className="wrap flex flex-col gap-1 py-5">
            <button
              onClick={openPalette}
              className="mb-3 flex h-12 items-center gap-3 rounded-xl border border-line bg-mist px-4 text-sm font-medium text-slate-ink"
            >
              <Icon name="search" size={17} /> Search laptops, servers, vendors…
              <span className="ml-auto rounded-md border border-line bg-white px-1.5 py-0.5 text-[10px] font-bold">⌘K</span>
            </button>
            {navItems.map((item, index) => (
              <Link
                key={item.id}
                to={item.href}
                onClick={() => setMenuOpen(false)}
                className="flex items-center justify-between border-b border-line/60 py-3 text-[15px] font-semibold text-navy"
                style={{ animationDelay: `${index * 40}ms` }}
              >
                {item.label}
                <Icon name="arrow-up-right" size={16} className="text-brand" />
              </Link>
            ))}
            <div className="mt-4 flex gap-2">
              <button onClick={() => { setMenuOpen(false); onRegister(); }} className="btn btn-primary flex-1">
                List your business <Icon name="arrow-up-right" size={15} className="arrow" />
              </button>
              <a href="tel:+919718199976" className="btn btn-ghost h-12 w-12 min-h-0 p-0" aria-label="Call us">
                <Icon name="phone" size={17} />
              </a>
            </div>
            <p className="mt-4 text-center text-[11px] text-slate-ink">
              1,200+ verified IT businesses · 85+ cities
            </p>
          </div>
        </div>
      </header>

      {paletteOpen && (
        <CommandPalette
          onClose={() => setPaletteOpen(false)}
          onOpenProduct={(product) => {
            setPaletteOpen(false);
            onOpenProduct(product);
          }}
        />
      )}
    </>
  );
}

const listingTicker = [
  "1,240+ verified IT businesses",
  "85+ cities covered",
  "12,480 live listings",
  "Zero commission on direct deals",
];

function CommandPalette({
  onClose,
  onOpenProduct,
}: {
  onClose: () => void;
  onOpenProduct: (product: Product) => void;
}) {
  const navigate = useNavigate();
  const { products } = useCatalog();
  const [query, setQuery] = useState("");
  const [cursor, setCursor] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const timer = window.setTimeout(() => inputRef.current?.focus(), 40);
    return () => window.clearTimeout(timer);
  }, []);

  const matches = useMemo(() => {
    const needle = query.trim().toLowerCase();
    if (!needle) return products.slice(0, 4);
    return products
      .filter((product) =>
        [product.name, product.brand, product.category, product.city, product.vendor.name, ...product.specs]
          .join(" ")
          .toLowerCase()
          .includes(needle),
      )
      .slice(0, 5);
  }, [query, products]);

  const filteredPages = useMemo(() => {
    const needle = query.trim().toLowerCase();
    if (!needle) return pageLinks.slice(0, 4);
    return pageLinks.filter((link) => link.label.toLowerCase().includes(needle)).slice(0, 3);
  }, [query]);

  const total = matches.length + filteredPages.length;

  useEffect(() => setCursor(0), [query]);

  const select = (index: number) => {
    if (index < matches.length) {
      const product = matches[index];
      if (product) onOpenProduct(product);
      return;
    }
    const link = filteredPages[index - matches.length];
    if (link) {
      navigate(link.href);
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 z-[100] flex items-start justify-center px-4 pt-[12vh] sm:pt-[14vh]">
      <button
        className="absolute inset-0 cursor-default bg-navy/55 backdrop-blur-[6px]"
        onClick={onClose}
        aria-label="Close search"
      />
      <div
        role="dialog"
        aria-modal="true"
        aria-label="Search My Laptop Market"
        className="fade-slide-in relative w-full max-w-[620px] overflow-hidden rounded-2xl border border-white/20 bg-white shadow-[0_40px_120px_-20px_rgba(7,20,38,0.6)]"
      >
        <div className="flex items-center gap-3 border-b border-line px-5 py-4">
          <Icon name="search" size={19} className="text-brand" />
          <input
            ref={inputRef}
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === "ArrowDown") {
                event.preventDefault();
                setCursor((value) => (value + 1) % Math.max(total, 1));
              }
              if (event.key === "ArrowUp") {
                event.preventDefault();
                setCursor((value) => (value - 1 + Math.max(total, 1)) % Math.max(total, 1));
              }
              if (event.key === "Enter") {
                event.preventDefault();
                select(cursor);
              }
            }}
            placeholder="Search by model, brand, city or vendor…"
            className="h-7 w-full bg-transparent text-[15px] font-medium text-navy outline-none placeholder:text-slate-ink/55"
          />
          <button
            onClick={onClose}
            className="rounded-md border border-line px-1.5 py-0.5 text-[10px] font-bold text-slate-ink transition hover:border-navy/30 hover:text-navy"
          >
            ESC
          </button>
        </div>

        <div className="max-h-[52vh] overflow-y-auto p-2">
          {matches.length > 0 && (
            <div>
              <p className="px-3 pb-1.5 pt-2 text-[10px] font-bold uppercase tracking-[0.16em] text-slate-ink/70">Live inventory</p>
              {matches.map((product, index) => (
                <button
                  key={product.id}
                  onMouseEnter={() => setCursor(index)}
                  onClick={() => select(index)}
                  className={cn(
                    "flex w-full items-center gap-3 rounded-xl px-2.5 py-2.5 text-left transition",
                    cursor === index ? "bg-sky" : "hover:bg-mist",
                  )}
                >
                  <div className="h-11 w-14 shrink-0 overflow-hidden rounded-lg border border-line bg-white">
                    <img src={product.image} alt="" className="h-full w-full object-cover" loading="lazy" />
                  </div>
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-[13px] font-semibold text-navy">{product.name}</p>
                    <p className="truncate text-[11px] text-slate-ink">
                      {product.category} · {product.city} · {product.units} units
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-[12px] font-bold text-navy">₹{product.price.toLocaleString("en-IN")}</p>
                    <p className="flex items-center justify-end gap-1 text-[10px] font-medium text-slate-ink">
                      <Icon name="badge" size={10} className="text-verified" /> Verified
                    </p>
                  </div>
                  <Icon name="arrow-up-right" size={14} className={cn("ml-1", cursor === index ? "text-brand" : "text-slate-ink/40")} />
                </button>
              ))}
            </div>
          )}

          {filteredPages.length > 0 && (
            <div className="mt-1 border-t border-line/70 pt-1">
              <p className="px-3 pb-1.5 pt-2 text-[10px] font-bold uppercase tracking-[0.16em] text-slate-ink/70">Jump to</p>
              {filteredPages.map((link, index) => {
                const itemIndex = matches.length + index;
                return (
                  <button
                    key={link.href}
                    onMouseEnter={() => setCursor(itemIndex)}
                    onClick={() => select(itemIndex)}
                    className={cn(
                      "flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-[13px] font-semibold text-navy transition",
                      cursor === itemIndex ? "bg-sky" : "hover:bg-mist",
                    )}
                  >
                    <Icon name="arrow-right" size={14} className="text-brand" />
                    {link.label}
                    <span className="ml-auto text-[10px] font-medium text-slate-ink">Page</span>
                  </button>
                );
              })}
            </div>
          )}

          {total === 0 && (
            <div className="px-4 py-10 text-center">
              <div className="mx-auto mb-3 flex h-11 w-11 items-center justify-center rounded-full bg-sky text-brand">
                <Icon name="search" size={20} />
              </div>
              <p className="text-sm font-semibold text-navy">No stock matches “{query}”</p>
              <p className="mt-1 text-xs text-slate-ink">Try a brand or city, or post a requirement and let dealers come to you.</p>
            </div>
          )}
        </div>

        <div className="flex items-center justify-between border-t border-line bg-mist/60 px-4 py-2.5 text-[10px] font-medium text-slate-ink">
          <span className="flex items-center gap-3">
            <span className="flex items-center gap-1">
              <kbd className="rounded border border-line bg-white px-1 py-0.5 font-sans">↑</kbd>
              <kbd className="rounded border border-line bg-white px-1 py-0.5 font-sans">↓</kbd> navigate
            </span>
            <span className="flex items-center gap-1">
              <kbd className="rounded border border-line bg-white px-1 py-0.5 font-sans">↵</kbd> open
            </span>
          </span>
          <span className="hidden items-center gap-1 sm:flex">
            Trading across {brands.slice(0, 4).join(" · ")} & more
          </span>
        </div>
      </div>
    </div>
  );
}
