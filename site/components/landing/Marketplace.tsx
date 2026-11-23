"use client";

import { useEffect, useMemo, useState } from "react";
import { categories, cities, inr, products, savings, type Product } from "@/lib/landing/data";
import { cn } from "@/lib/landing/cn";
import { Icon } from "./Icon";
import { Reveal } from "@/lib/landing/motion";

const categoryFilters = ["All", "Laptops", "Servers", "Networking", "Monitors"] as const;
const sortOptions = [
  { value: "newest", label: "Newest first" },
  { value: "price-asc", label: "Price: low to high" },
  { value: "price-desc", label: "Price: high to low" },
  { value: "stock", label: "Most units" },
] as const;

export function Marketplace({
  initialQuery,
  initialCategory,
  initialCity,
  queryKey,
  onQuickView,
  onRequirement,
}: {
  initialQuery: string;
  initialCategory: string;
  initialCity: string;
  queryKey: number;
  onQuickView: (product: Product) => void;
  onRequirement: () => void;
}) {
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState<(typeof categoryFilters)[number]>("All");
  const [city, setCity] = useState("All cities");
  const [condition, setCondition] = useState("All grades");
  const [sort, setSort] = useState<(typeof sortOptions)[number]["value"]>("newest");
  const [showFilters, setShowFilters] = useState(false);
  const [favorites, setFavorites] = useState<string[]>([]);

  useEffect(() => {
    setQuery(initialQuery);
    const matched = categoryFilters.find((item) => item.toLowerCase() === initialCategory.toLowerCase());
    setCategory(matched ?? "All");
    setCity(cities.includes(initialCity) ? initialCity : "All cities");
    setSort("newest");
  }, [initialQuery, initialCategory, initialCity, queryKey]);

  const toggleFavorite = (id: string) =>
    setFavorites((current) => (current.includes(id) ? current.filter((item) => item !== id) : [...current, id]));

  const filtered = useMemo(() => {
    const needle = query.trim().toLowerCase();
    const result = products.filter((product) => {
      const matchesQuery =
        !needle ||
        [product.name, product.brand, product.vendor.name, product.city, product.category, ...product.specs]
          .join(" ")
          .toLowerCase()
          .includes(needle);
      const matchesCategory = category === "All" || product.category === category;
      const matchesCity = city === "All cities" || product.city === city;
      const matchesCondition =
        condition === "All grades" ||
        (condition === "Grade A+" ? product.condition === "Grade A+" : product.condition.startsWith("Grade A"));
      return matchesQuery && matchesCategory && matchesCity && matchesCondition;
    });
    const sorted = [...result];
    if (sort === "price-asc") sorted.sort((a, b) => a.price - b.price);
    if (sort === "price-desc") sorted.sort((a, b) => b.price - a.price);
    if (sort === "stock") sorted.sort((a, b) => b.units - a.units);
    return sorted;
  }, [query, category, city, condition, sort]);

  const activeFilters = (city !== "All cities" ? 1 : 0) + (condition !== "All grades" ? 1 : 0);
  const reset = () => {
    setQuery("");
    setCategory("All");
    setCity("All cities");
    setCondition("All grades");
    setSort("newest");
  };

  return (
    <section id="marketplace" className="relative scroll-mt-24 border-y border-line/70 bg-mist py-14 sm:py-16 lg:py-20">
      <div className="wrap">
        <div className="flex flex-col justify-between gap-7 md:flex-row md:items-end">
          <Reveal className="max-w-[560px]">
            <span className="eyebrow">The live marketplace</span>
            <h2 className="display mt-4 text-[clamp(2.1rem,4.2vw,3.35rem)] text-navy">
              Fresh stock.
              <br />
              <span className="text-brand">Verified sellers.</span>
            </h2>
            <p className="mt-4 max-w-[470px] text-[14.5px] leading-relaxed text-slate-ink">
              Real hardware listed by real IT businesses — with quantities, grades and trade prices in plain sight.
            </p>
          </Reveal>

          <Reveal delay={100} className="flex flex-wrap items-center gap-2.5">
            <div className="mr-2 hidden items-center gap-2 rounded-full border border-line bg-white px-3.5 py-2 text-[11px] font-semibold text-slate-ink lg:flex">
              <span className="relative flex h-2 w-2">
                <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-verified opacity-70" />
                <span className="relative inline-flex h-2 w-2 rounded-full bg-verified" />
              </span>
              <span className="font-bold text-navy">{filtered.length}</span> of {products.length} listings shown
            </div>
            <button onClick={onRequirement} className="link-arrow h-11 rounded-full border border-line bg-white px-4 text-[12px] transition hover:border-brand/30">
              Post a requirement <Icon name="plus" size={14} />
            </button>
            <a
              href="https://mylaptopmarket.com/products.html"
              target="_blank"
              rel="noreferrer"
              className="btn btn-ghost h-11 min-h-0 px-4 text-[12.5px]"
            >
              All listings <Icon name="arrow-up-right" size={14} className="arrow" />
            </a>
          </Reveal>
        </div>

        <Reveal delay={60}>
          <div className="mt-9 rounded-[20px] border border-line bg-white p-3 shadow-[0_10px_40px_-30px_rgba(11,27,58,0.5)]">
            <div className="flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
              <div className="no-scrollbar flex gap-1 overflow-x-auto">
                {categoryFilters.map((item) => {
                  const count =
                    item === "All" ? products.length : products.filter((product) => product.category === item).length;
                  return (
                    <button
                      key={item}
                      onClick={() => setCategory(item)}
                      className={cn(
                        "flex shrink-0 items-center gap-1.5 rounded-full px-3.5 py-2 text-[12px] font-semibold transition-all duration-300",
                        category === item
                          ? "bg-navy text-white shadow-[0_8px_20px_-12px_rgba(11,27,58,0.7)]"
                          : "text-slate-ink hover:bg-mist hover:text-navy",
                      )}
                    >
                      {item}
                      <span className={cn("rounded-full px-1.5 text-[9.5px] font-bold", category === item ? "bg-white/15 text-white" : "bg-mist text-slate-ink/70")}>
                        {count}
                      </span>
                    </button>
                  );
                })}
              </div>

              <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
                <label className="flex h-10 min-w-0 flex-1 items-center gap-2 rounded-full border border-line px-3.5 transition focus-within:border-brand/40 sm:w-[210px]">
                  <Icon name="search" size={15} className="text-slate-ink/70" />
                  <input
                    value={query}
                    onChange={(event) => setQuery(event.target.value)}
                    placeholder="Search this stock"
                    className="w-full bg-transparent text-[12px] font-medium text-navy outline-none placeholder:text-slate-ink/60"
                    aria-label="Filter listings"
                  />
                  {query && (
                    <button onClick={() => setQuery("")} aria-label="Clear search" className="text-slate-ink/60 hover:text-navy">
                      <Icon name="x" size={13} />
                    </button>
                  )}
                </label>
                <button
                  onClick={() => setShowFilters((value) => !value)}
                  className={cn(
                    "flex h-10 items-center justify-center gap-2 rounded-full border px-3.5 text-[12px] font-semibold transition",
                    showFilters || activeFilters
                      ? "border-brand/30 bg-sky text-brand"
                      : "border-line bg-white text-navy hover:bg-mist",
                  )}
                  aria-expanded={showFilters}
                >
                  <Icon name="sliders" size={14} /> Filters
                  {activeFilters > 0 && (
                    <span className="flex h-4 w-4 items-center justify-center rounded-full bg-brand text-[9px] text-white">{activeFilters}</span>
                  )}
                </button>
                <label className="relative flex h-10 items-center gap-1.5 rounded-full border border-line bg-white pl-3 pr-7 text-[12px] font-semibold text-navy">
                  <Icon name="arrow-down" size={13} className="text-slate-ink/70" />
                  <select
                    value={sort}
                    onChange={(event) => setSort(event.target.value as typeof sort)}
                    className="cursor-pointer appearance-none bg-transparent text-[12px] font-semibold outline-none"
                    aria-label="Sort listings"
                  >
                    {sortOptions.map((option) => (
                      <option key={option.value} value={option.value}>{option.label}</option>
                    ))}
                  </select>
                  <Icon name="chevron-down" size={12} className="pointer-events-none absolute right-3 text-slate-ink/70" />
                </label>
              </div>
            </div>

            {showFilters && (
              <div className="fade-slide-in mt-3 flex flex-col gap-3 border-t border-line/80 pt-3 sm:flex-row sm:items-center">
                <p className="text-[10px] font-bold uppercase tracking-[0.14em] text-slate-ink">Refine</p>
                <div className="grid flex-1 gap-2 sm:grid-cols-2 lg:max-w-[480px]">
                  <label className="flex h-9 items-center gap-2 rounded-lg border border-line px-3 text-[11.5px] font-medium text-navy">
                    <Icon name="pin" size={13} className="text-brand" />
                    <select
                      value={city}
                      onChange={(event) => setCity(event.target.value)}
                      className="w-full cursor-pointer appearance-none bg-transparent outline-none"
                      aria-label="Filter by city"
                    >
                      <option>All cities</option>
                      {cities.map((item) => <option key={item}>{item}</option>)}
                    </select>
                    <Icon name="chevron-down" size={12} className="text-slate-ink/60" />
                  </label>
                  <label className="flex h-9 items-center gap-2 rounded-lg border border-line px-3 text-[11.5px] font-medium text-navy">
                    <Icon name="badge" size={13} className="text-brand" />
                    <select
                      value={condition}
                      onChange={(event) => setCondition(event.target.value)}
                      className="w-full cursor-pointer appearance-none bg-transparent outline-none"
                      aria-label="Filter by grade"
                    >
                      <option>All grades</option>
                      <option>Grade A+</option>
                      <option>Grade A</option>
                    </select>
                    <Icon name="chevron-down" size={12} className="text-slate-ink/60" />
                  </label>
                </div>
                <button onClick={reset} className="text-left text-[11px] font-semibold text-brand hover:text-brand-deep">
                  Reset all filters
                </button>
                <p className="ml-auto hidden items-center gap-1.5 text-[10.5px] text-slate-ink lg:flex">
                  <Icon name="clock" size={12} /> Updated 2 minutes ago
                </p>
              </div>
            )}
          </div>
        </Reveal>

        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {filtered.map((product, index) => (
            <ProductCard
              key={product.id}
              product={product}
              index={index}
              favorite={favorites.includes(product.id)}
              onFavorite={() => toggleFavorite(product.id)}
              onQuickView={() => onQuickView(product)}
            />
          ))}
        </div>

        {filtered.length === 0 && (
          <div className="mt-6 flex flex-col items-center justify-center rounded-3xl border border-dashed border-line bg-white px-6 py-16 text-center">
            <span className="flex h-12 w-12 items-center justify-center rounded-2xl bg-sky text-brand">
              <Icon name="search" size={22} />
            </span>
            <h3 className="mt-4 text-lg font-bold text-navy">No stock matches those filters</h3>
            <p className="mt-1.5 max-w-[360px] text-[13px] leading-relaxed text-slate-ink">
              Reset the filters, or tell the network what you need — verified dealers respond with matched stock.
            </p>
            <div className="mt-5 flex flex-wrap justify-center gap-2">
              <button onClick={reset} className="btn btn-ghost h-10 min-h-0 px-4 text-[12px]">Clear filters</button>
              <button onClick={onRequirement} className="btn btn-primary h-10 min-h-0 px-4 text-[12px]">Post a requirement</button>
            </div>
          </div>
        )}

        <Reveal delay={100}>
          <div className="mt-5 grid gap-3 rounded-[18px] border border-line bg-white p-4 sm:grid-cols-3">
            {[
              { icon: "shield" as const, title: "Verified vendors only", body: "Business documents reviewed before a profile goes live." },
              { icon: "refresh" as const, title: "Transparent condition", body: "Grades, specs, quantities and MOQ listed up front." },
              { icon: "truck" as const, title: "Pan-India fulfilment", body: "Logistics partners for outbound units across 85+ cities." },
            ].map((item) => (
              <div key={item.title} className="flex items-start gap-3 sm:border-l sm:border-line sm:pl-4 sm:first:border-l-0 sm:first:pl-0">
                <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-sky text-brand">
                  <Icon name={item.icon} size={15} />
                </span>
                <div>
                  <p className="text-[11.5px] font-bold text-navy">{item.title}</p>
                  <p className="mt-0.5 text-[10px] leading-relaxed text-slate-ink">{item.body}</p>
                </div>
              </div>
            ))}
          </div>
        </Reveal>
      </div>
    </section>
  );
}

function ProductCard({
  product,
  index,
  favorite,
  onFavorite,
  onQuickView,
}: {
  product: Product;
  index: number;
  favorite: boolean;
  onFavorite: () => void;
  onQuickView: () => void;
}) {
  return (
    <Reveal delay={(index % 3) * 70} className="h-full">
      <article className="group flex h-full flex-col overflow-hidden rounded-[20px] border border-line bg-white transition duration-500 hover:-translate-y-1 hover:border-brand/25 hover:shadow-[0_30px_60px_-32px_rgba(11,27,58,0.4)]">
        <div className="relative overflow-hidden bg-[#f2f6fc]">
          <button onClick={onQuickView} className="block w-full" aria-label={`View ${product.name}`}>
            <img
              src={product.image}
              alt={product.name}
              loading="lazy"
              className="h-[192px] w-full object-cover transition-transform duration-[900ms] ease-out group-hover:scale-[1.06]"
            />
          </button>
          <div className="pointer-events-none absolute inset-x-0 top-0 flex items-start justify-between p-3">
            <span className="rounded-full border border-navy/5 bg-white/95 px-2.5 py-1 text-[9.5px] font-bold text-navy shadow-sm backdrop-blur">
              {product.condition} · {product.category}
            </span>
            <div className="flex items-center gap-1.5">
              {product.featured && (
                <span className="flex items-center gap-1 rounded-full bg-brand px-2 py-1 text-[9px] font-bold text-white shadow-sm">
                  <Icon name="zap" size={9} className="fill-white" /> Featured
                </span>
              )}
              <button
                onClick={onFavorite}
                aria-label={favorite ? "Remove from saved" : "Save listing"}
                aria-pressed={favorite}
                className={cn(
                  "pointer-events-auto flex h-7 w-7 items-center justify-center rounded-full border border-white/60 bg-white/95 shadow-sm backdrop-blur transition hover:scale-110",
                  favorite ? "text-[#e0446c]" : "text-slate-ink hover:text-brand",
                )}
              >
                <Icon name="heart" size={13} className={cn(favorite && "fill-current")} />
              </button>
            </div>
          </div>
          <button
            onClick={onQuickView}
            className="absolute bottom-3 left-1/2 flex -translate-x-1/2 translate-y-2 items-center gap-1.5 whitespace-nowrap rounded-full bg-navy/90 px-3.5 py-1.5 text-[10.5px] font-semibold text-white opacity-0 shadow-lg backdrop-blur transition-all duration-300 hover:bg-brand group-hover:translate-y-0 group-hover:opacity-100"
          >
            Quick view <Icon name="arrow-up-right" size={12} />
          </button>
          <span className="absolute bottom-3 right-3 rounded-full bg-white/90 px-2 py-1 text-[9px] font-bold text-[#0f9a38] shadow-sm backdrop-blur">
            −{savings(product.price, product.mrp)}% vs new
          </span>
        </div>

        <div className="flex flex-1 flex-col p-4">
          <div className="flex items-start justify-between gap-2">
            <div className="min-w-0">
              <p className="text-[9.5px] font-bold uppercase tracking-[0.15em] text-brand">{product.brand}</p>
              <button onClick={onQuickView} className="mt-1 block truncate text-left text-[14.5px] font-bold tracking-[-0.015em] text-navy transition hover:text-brand">
                {product.name}
              </button>
            </div>
            <span className="flex shrink-0 items-center gap-1 text-[10px] font-semibold text-slate-ink">
              <Icon name="pin" size={11} className="text-slate-ink/70" /> {product.city}
            </span>
          </div>

          <div className="mt-2.5 flex flex-wrap gap-1">
            {product.specs.slice(0, 3).map((spec) => (
              <span key={spec} className="rounded-md border border-line bg-mist/70 px-1.5 py-1 text-[9.5px] font-medium text-slate-ink">
                {spec}
              </span>
            ))}
          </div>

          <div className="mt-3 flex items-center justify-between border-t border-line/70 pt-2.5">
            <div className="flex items-center gap-1.5 min-w-0">
              <span className={`flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-gradient-to-br ${product.vendor.tint} text-[8px] font-bold text-white`}>
                {product.vendor.initials}
              </span>
              <span className="truncate text-[10px] font-semibold text-navy/80">{product.vendor.name}</span>
              {product.vendor.verified && <Icon name="badge" size={11} className="shrink-0 text-verified" />}
            </div>
            <span className="shrink-0 rounded bg-sky px-1.5 py-0.5 text-[9px] font-bold text-brand">{product.units} units</span>
          </div>

          <div className="mt-auto flex items-end justify-between gap-2 pt-3">
            <div>
              <p className="text-[9px] font-semibold uppercase tracking-[0.1em] text-slate-ink/70">Trade price</p>
              <p className="text-[17px] font-bold leading-tight tracking-[-0.03em] text-navy">{inr(product.price)}</p>
              <p className="text-[9.5px] text-slate-ink/65">
                <span className="line-through">{inr(product.mrp)}</span> new · MOQ {product.moq}
              </p>
            </div>
            <button
              onClick={onQuickView}
              className="flex h-9 items-center gap-1.5 rounded-full bg-brand px-3.5 text-[11px] font-bold text-white transition hover:bg-brand-deep active:scale-95"
            >
              Get quote <Icon name="arrow-up-right" size={12} className="arrow" />
            </button>
          </div>
        </div>
      </article>
    </Reveal>
  );
}

export function BrandMarquee() {
  const brandList = ["Dell", "HP", "Lenovo", "Apple", "HPE", "Cisco", "Microsoft", "ASUS", "Acer", "Samsung"];
  return (
    <div className="border-b border-line/70 bg-white py-6">
      <div className="wrap flex flex-col items-center gap-5 sm:flex-row sm:gap-8">
        <p className="shrink-0 text-center text-[9.5px] font-bold uppercase tracking-[0.18em] text-slate-ink/65 sm:text-left">
          India&apos;s IT channel
          <br />
          <span className="text-navy/50">trades here</span>
        </p>
        <div className="marquee-mask relative w-full min-w-0 flex-1 overflow-hidden">
          <div className="animate-marquee-slow flex w-max items-center gap-10 pr-10">
            {[...brandList, ...brandList].map((brand, index) => (
              <span key={`${brand}-${index}`} className="flex items-center gap-2 font-display text-[17px] font-bold tracking-[-0.02em] text-navy/35 transition hover:text-navy/70">
                {brand}
                <span className="h-1 w-1 rounded-full bg-brand/30" />
              </span>
            ))}
          </div>
        </div>
        <span className="hidden shrink-0 items-center gap-1.5 rounded-full border border-line px-3 py-1.5 text-[10px] font-semibold text-slate-ink lg:flex">
          <Icon name="shield" size={11} className="text-brand" /> Verified businesses only
        </span>
      </div>
    </div>
  );
}

export { categories };
