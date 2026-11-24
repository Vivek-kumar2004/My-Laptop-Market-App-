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
  onOpenProduct,
}: {
  onOpenProduct: (product: Product) => void;
}) {
  const scrolled = useScrolled(24);
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);
  const [paletteOpen, setPaletteOpen] = useState(false);
  const [scrollActive, setScrollActive] = useState<string>("marketplace");

  const pathActive = useMemo(() => {
    const path = location.pathname.replace(/\/index\.html$/, "").replace(/\/?$/, "/") || "/";
    if (path.startsWith("/why")) return "why";
    if (path.startsWith("/how")) return "how";
    if (path.startsWith("/vendors")) return "vendors";
    if (path.startsWith("/insights")) return "insights";
    if (location.hash === "#marketplace") return "marketplace";
    return path === "/" ? null : "";
  }, [location.pathname, location.hash]);

  const active = pathActive ?? scrollActive;

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

  useEffect(() => {
    const onHome = location.pathname === "/" || location.pathname === "";
    if (!onHome) return;
    const sections = navItems
      .map((item) => document.getElementById(item.id))
      .filter((el): el is HTMLElement => el !== null);
    if (!sections.length) return;
    const observer = new IntersectionObserver(
      (entries) => {
        const visible = entries
          .filter((entry) => entry.isIntersecting)
          .sort((a, b) => b.intersectionRatio - a.intersectionRatio)[0];
        if (visible) setScrollActive(visible.target.id);
      },
      { rootMargin: "-30% 0px -55% 0px", threshold: [0.05, 0.2, 0.5] },
    );
    sections.forEach((section) => observer.observe(section));
    return () => observer.disconnect();
  }, [location.pathname]);

  const solid = scrolled || menuOpen;

  return (
    <>
      <header className="fixed inset-x-0 top-0 z-[60]">
        <div className="relative z-10 px-3 pt-3 sm:px-5 sm:pt-4 xl:pl-20">
          <div
            className={cn(
              "mx-auto flex items-center justify-between gap-3 rounded-full border py-2 pl-3 pr-2 transition-[max-width,background-color,border-color,box-shadow] duration-700 sm:pl-5",
              solid
                ? "max-w-[1120px] border-line/80 bg-white/88 shadow-[0_12px_40px_-14px_rgba(5,7,11,0.28)] backdrop-blur-xl"
                : "max-w-[1320px] border-transparent bg-white/0",
            )}
          >
            <Link to={routes.home} className="group flex shrink-0 items-center" aria-label="My Laptop Market home">
              <BrandLogo className="h-9 max-w-[170px] sm:h-10 sm:max-w-[200px]" />
            </Link>

            <nav aria-label="Main navigation" className="hidden items-center xl:flex">
              {navItems.map((item) => (
                <Link
                  key={item.id}
                  to={item.href}
                  className={cn(
                    "relative px-3.5 py-2 text-[13px] font-semibold transition-colors after:absolute after:inset-x-3.5 after:bottom-1 after:h-[2px] after:origin-left after:rounded-full after:bg-brand after:transition-transform after:duration-300",
                    active === item.id
                      ? "text-brand after:scale-x-100"
                      : "text-navy/70 after:scale-x-0 hover:text-navy hover:after:scale-x-100",
                  )}
                >
                  {item.label}
                </Link>
              ))}
            </nav>

            <div className="flex items-center gap-1.5">
              <button
                type="button"
                onClick={openPalette}
                className="hidden h-10 w-10 items-center justify-center rounded-full text-navy/70 transition hover:bg-mist hover:text-navy md:inline-flex"
                aria-label="Search the marketplace"
              >
                <Icon name="search" size={16} />
              </button>
              <a
                href="https://mylaptopmarket.com/api/vendor/login"
                className="hidden rounded-full px-4 py-2.5 text-[13px] font-semibold text-navy/75 transition hover:bg-mist hover:text-navy md:inline-flex"
              >
                Vendor Login
              </a>
              <a
                href="https://mylaptopmarket.com/api/vendor/register"
                className="btn btn-primary hidden h-10 min-h-0 px-4 text-[12.5px] sm:inline-flex"
              >
                Register as Dealer
                <Icon name="arrow-up-right" size={14} className="arrow" />
              </a>
              <button
                type="button"
                onClick={() => setMenuOpen((value) => !value)}
                className="grid h-10 w-10 place-items-center rounded-full bg-navy text-white xl:hidden"
                aria-label={menuOpen ? "Close menu" : "Open menu"}
                aria-expanded={menuOpen}
              >
                <span className="relative block h-3 w-4">
                  <span
                    className={cn(
                      "absolute left-0 top-0 h-[2px] w-4 bg-current transition-transform duration-300",
                      menuOpen && "translate-y-[5px] rotate-45",
                    )}
                  />
                  <span
                    className={cn(
                      "absolute bottom-0 left-0 h-[2px] w-4 bg-current transition-transform duration-300",
                      menuOpen && "-translate-y-[5px] -rotate-45",
                    )}
                  />
                </span>
              </button>
            </div>
          </div>
        </div>

        <div
          className={cn(
            "mx-3 mt-2 overflow-hidden rounded-[22px] border border-line bg-white/95 shadow-soft backdrop-blur-xl transition-[max-height,opacity] duration-500 xl:hidden",
            menuOpen ? "max-h-[80vh] opacity-100" : "pointer-events-none max-h-0 border-transparent opacity-0",
          )}
        >
          <div className="flex flex-col gap-1 px-4 py-5">
            <button
              type="button"
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
              <a
                href="https://mylaptopmarket.com/api/vendor/register"
                className="btn btn-primary flex-1"
                onClick={() => setMenuOpen(false)}
              >
                Register as Dealer <Icon name="arrow-up-right" size={15} className="arrow" />
              </a>
              <a
                href="https://mylaptopmarket.com/api/vendor/login"
                className="btn btn-ghost h-12 min-h-0 px-4 text-[12px]"
              >
                Login
              </a>
            </div>
          </div>
        </div>
      </header>

      {/* Spacer so content clears the fixed floating bar */}
      <div className="h-[72px] sm:h-[80px]" aria-hidden />

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
