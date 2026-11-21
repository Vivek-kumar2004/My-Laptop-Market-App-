"use client";

import { useEffect, useRef, useState } from "react";
import { ArrowRight, Check, ChevronLeft, ChevronRight, MapPin } from "lucide-react";

const API =
  "https://asia-south1-mylaptopmarket-app.cloudfunctions.net/api/api/web/home-showcase";
const FALLBACK = "/images/lap1.jpg";

type Product = {
  id?: string;
  product_name?: string;
  price?: number | string;
  category?: string;
  vendor_name?: string;
  vendor_logo_url?: string;
  main_image?: string;
  image_urls?: string[];
};

type Vendor = {
  vendor_id: string;
  vendor_name?: string;
  vendor_location?: string;
  vendor_logo_url?: string;
  product_count?: number;
};

function formatPrice(price: number | string | undefined) {
  const num = parseFloat(String(price ?? ""));
  if (Number.isNaN(num) || num <= 0) return "Ask for price";
  return "₹" + num.toLocaleString("en-IN");
}

function initial(name?: string) {
  return (name || "V").trim().charAt(0).toUpperCase() || "V";
}

export function HomeShowcase() {
  const [products, setProducts] = useState<Product[]>([]);
  const [vendors, setVendors] = useState<Vendor[]>([]);
  const [loading, setLoading] = useState(true);
  const carouselRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetch(API, { credentials: "include" })
      .then((res) => {
        if (!res.ok) throw new Error("showcase failed");
        return res.json();
      })
      .then((data) => {
        setProducts(data.products || []);
        setVendors(data.vendors || []);
      })
      .catch(() => {
        setProducts([]);
        setVendors([]);
      })
      .finally(() => setLoading(false));
  }, []);

  const scrollCarousel = (dir: number) => {
    carouselRef.current?.scrollBy({ left: dir * 240, behavior: "smooth" });
  };

  return (
    <>
      <section className="px-0 py-[72px]" id="liveMarketplace">
        <div className="mx-auto w-[min(100%-40px,1180px)]">
          <div className="mx-auto mb-9 max-w-[40rem] text-center">
            <div className="mb-3 inline-flex h-7 items-center gap-2 rounded-full bg-green-soft px-3 text-xs font-bold text-[#0f9a38]">
              <span className="size-2 rounded-full bg-green shadow-[0_0_0_4px_rgba(32,202,71,0.2)]" />
              Live Marketplace
            </div>
            <h2 className="mb-2.5 text-[clamp(1.7rem,3vw,2.35rem)] font-bold leading-[1.15] tracking-[-0.025em] text-navy">
              Real Listings, <span className="mark">Spinning in 3D</span>
            </h2>
            <p className="text-base text-ink-2">
              Drag the ring to explore hardware currently listed by verified vendors.
            </p>
            <div className="mt-3.5 flex items-center justify-center gap-4 text-[13px] font-semibold text-ink-3">
              <span className="h-px w-16 bg-line" />
              {loading
                ? "Loading live listings…"
                : products.length
                  ? `Showing ${products.length} live listing${products.length === 1 ? "" : "s"}`
                  : "Live listings from verified vendors"}
              <span className="h-px w-16 bg-line" />
            </div>
          </div>

          <div className="relative mb-7">
            <button
              type="button"
              aria-label="Previous"
              onClick={() => scrollCarousel(-1)}
              className="absolute top-[40%] left-[-8px] z-[2] grid size-10 place-items-center rounded-full border border-line bg-white text-brand shadow-[0_8px_24px_rgba(15,40,90,0.08)]"
            >
              <ChevronLeft className="size-4" />
            </button>
            <div
              ref={carouselRef}
              className="no-scrollbar flex gap-4 overflow-x-auto scroll-smooth px-1 py-2 pb-4"
            >
              {(loading ? Array.from({ length: 6 }) : products.slice(0, 12)).map((p, i) =>
                loading ? (
                  <div
                    key={i}
                    className="h-[260px] w-[220px] shrink-0 animate-pulse rounded-2xl bg-slate-200"
                  />
                ) : (
                  <ProductCard key={(p as Product).id || i} product={p as Product} carousel />
                ),
              )}
              {!loading && !products.length ? (
                <p className="w-full rounded-2xl border border-dashed border-line bg-white p-10 text-center text-ink-3">
                  New listings will appear here as vendors go live.
                </p>
              ) : null}
            </div>
            <button
              type="button"
              aria-label="Next"
              onClick={() => scrollCarousel(1)}
              className="absolute top-[40%] right-[-8px] z-[2] grid size-10 place-items-center rounded-full border border-line bg-white text-brand shadow-[0_8px_24px_rgba(15,40,90,0.08)]"
            >
              <ChevronRight className="size-4" />
            </button>
          </div>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {(loading ? Array.from({ length: 4 }) : products.slice(0, 4)).map((p, i) =>
              loading ? (
                <div key={i} className="min-h-[260px] animate-pulse rounded-2xl bg-slate-200" />
              ) : (
                <ProductCard key={(p as Product).id || i} product={p as Product} />
              ),
            )}
          </div>

          <div className="mt-7 text-center">
            <a
              href="/products.html"
              className="inline-flex h-[42px] items-center gap-2 rounded-full bg-brand px-[18px] text-sm font-bold text-white shadow-[0_10px_24px_rgba(7,103,216,0.28)]"
            >
              Browse all products <ArrowRight className="size-4" />
            </a>
          </div>
        </div>
      </section>

      <section className="bg-[#f4f8ff] px-0 py-[72px]" id="freshListings">
        <div className="mx-auto w-[min(100%-40px,1180px)]">
          <div className="mb-6 max-w-[40rem]">
            <div className="mb-3 inline-flex h-7 items-center gap-2 rounded-full bg-green-soft px-3 text-xs font-bold text-[#0f9a38]">
              <span className="size-2 rounded-full bg-green" />
              Live Marketplace
            </div>
            <h2 className="mb-2.5 text-[clamp(1.7rem,3vw,2.35rem)] font-bold text-navy">
              Fresh listings from verified vendors
            </h2>
            <p className="text-base text-ink-2">
              Real hardware currently listed by verified IT businesses.
            </p>
          </div>
          <div className="mb-6 flex flex-wrap items-center gap-2">
            {[
              ["All Products", "/products.html"],
              ["Laptops", "/products.html?category=Laptops"],
              ["Desktops", "/products.html?category=Desktops"],
              ["Monitors", "/products.html?category=Monitors"],
              ["Servers", "/products.html?category=Servers"],
              ["RAM", "/products.html?category=RAM"],
              ["SSD", "/products.html?category=SSD"],
            ].map(([label, href], i) => (
              <a
                key={label}
                href={href}
                className={`inline-flex h-10 items-center rounded-full px-3.5 text-[13px] font-bold ${
                  i === 0 ? "bg-brand text-white" : "bg-[#e8f0fe] text-navy"
                }`}
              >
                {label}
              </a>
            ))}
            <span className="ml-auto inline-flex h-10 items-center rounded-full border border-line bg-white px-3.5 text-[13px] font-semibold text-ink-2">
              Sort By: Latest
            </span>
          </div>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {(loading ? Array.from({ length: 8 }) : products.slice(0, 8)).map((p, i) =>
              loading ? (
                <div key={i} className="min-h-[260px] animate-pulse rounded-2xl bg-slate-200" />
              ) : (
                <ProductCard key={`b-${(p as Product).id || i}`} product={p as Product} />
              ),
            )}
          </div>
        </div>
      </section>

      <section className="bg-[#f4f8ff] px-0 py-[72px]" id="popularVendors">
        <div className="mx-auto w-[min(100%-40px,1180px)]">
          <div className="mb-9 grid items-center gap-8 md:grid-cols-2">
            <div>
              <div className="mb-3 inline-flex h-7 items-center gap-2 rounded-full bg-green-soft px-3 text-xs font-bold text-[#0f9a38]">
                <Check className="size-3.5" /> Trusted by 100+ Businesses
              </div>
              <h2 className="mb-2.5 text-[clamp(1.8rem,3vw,2.5rem)] font-bold text-navy">
                <span className="mark">Popular</span>{" "}
                <span className="text-green">Vendors</span>
              </h2>
              <p className="mb-4 text-base text-ink-2">
                Work with leading brands and trusted laptop suppliers on My Laptop Market.
              </p>
            </div>
            <div className="overflow-hidden rounded-[20px] bg-gradient-to-br from-sky-100 to-blue-50 shadow-[0_16px_40px_rgba(15,40,90,0.1)]">
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src="/images/lap1.jpg"
                alt="Business laptops from verified vendors"
                className="h-[280px] w-full object-cover"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 gap-3.5 sm:grid-cols-2 lg:grid-cols-4">
            {(loading ? Array.from({ length: 4 }) : vendors).map((v, i) =>
              loading ? (
                <div key={i} className="min-h-[88px] animate-pulse rounded-xl bg-slate-200" />
              ) : (
                <VendorCard key={(v as Vendor).vendor_id} vendor={v as Vendor} />
              ),
            )}
            {!loading && !vendors.length ? (
              <p className="col-span-full rounded-2xl border border-dashed border-line bg-white p-10 text-center text-ink-3">
                Popular vendors will show here after listings go live.
              </p>
            ) : null}
          </div>

          <div className="mt-7 flex flex-wrap items-center gap-4 rounded-2xl border border-line bg-[linear-gradient(90deg,#e8f1fc,#eef8ff)] px-[22px] py-[18px]">
            <strong className="text-navy">Partner with Us</strong>
            <span className="flex-1 text-sm text-ink-2">
              Grow your business with My Laptop Market&apos;s trusted platform.
            </span>
            <a
              href="https://mylaptopmarket.com/api/vendor/register"
              className="inline-flex h-[42px] items-center gap-2 rounded-full bg-brand px-[18px] text-sm font-bold text-white"
            >
              Become a Vendor <ArrowRight className="size-4" />
            </a>
          </div>
        </div>
      </section>
    </>
  );
}

function ProductCard({
  product,
  carousel,
}: {
  product: Product;
  carousel?: boolean;
}) {
  const img = product.main_image || product.image_urls?.[0] || FALLBACK;
  const href = product.id
    ? `/product.html?id=${encodeURIComponent(product.id)}`
    : "/products.html";
  const letter = initial(product.vendor_name);

  if (carousel) {
    return (
      <a
        href={href}
        className="w-[220px] shrink-0 snap-start overflow-hidden rounded-2xl border border-line bg-white shadow-[0_8px_24px_rgba(15,40,90,0.08)] transition hover:-translate-y-0.5"
      >
        <div className="relative aspect-[4/3] bg-slate-100">
          <span className="absolute top-2.5 right-2.5 z-[1] grid size-6 place-items-center rounded-full bg-green text-[11px] text-white shadow">
            <Check className="size-3" />
          </span>
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src={img}
            alt={product.product_name || "Product"}
            className="size-full object-cover"
            onError={(e) => {
              (e.target as HTMLImageElement).src = FALLBACK;
            }}
          />
        </div>
        <div className="p-3.5">
          <h3 className="mb-1.5 line-clamp-2 text-sm font-bold text-navy">
            {product.product_name || "Product"}
          </h3>
          <div className="text-base font-extrabold text-brand tabular-nums">
            {formatPrice(product.price)}
          </div>
          {product.category ? (
            <span className="mt-2 inline-flex rounded-full bg-brand-soft px-2.5 py-1 text-[11px] font-bold text-brand">
              {product.category}
            </span>
          ) : null}
        </div>
      </a>
    );
  }

  return (
    <a
      href={href}
      className="flex flex-col overflow-hidden rounded-2xl border border-line bg-white shadow-[0_8px_24px_rgba(15,40,90,0.08)] transition hover:-translate-y-0.5 hover:shadow-[0_16px_40px_rgba(15,40,90,0.1)]"
    >
      <div className="relative aspect-[1.15] bg-slate-100">
        {product.category ? (
          <span className="absolute top-2.5 left-2.5 z-[1] rounded-md bg-brand px-2.5 py-1 text-[10px] font-extrabold tracking-wide text-white uppercase">
            {product.category}
          </span>
        ) : null}
        <span className="absolute top-2.5 right-2.5 z-[1] grid size-6 place-items-center rounded-full bg-green text-white">
          <Check className="size-3" />
        </span>
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img
          src={img}
          alt={product.product_name || "Product"}
          className="size-full object-cover"
          onError={(e) => {
            (e.target as HTMLImageElement).src = FALLBACK;
          }}
        />
      </div>
      <div className="flex flex-1 flex-col gap-2 p-3.5">
        <h3 className="text-[15px] leading-snug font-bold text-navy">
          {product.product_name || "Product"}
        </h3>
        <div className="text-base font-extrabold text-brand tabular-nums">
          {formatPrice(product.price)}
        </div>
        <div className="mt-auto flex items-center justify-between gap-2 pt-1">
          <div className="flex items-center gap-2 text-xs text-ink-3">
            {product.vendor_logo_url ? (
              // eslint-disable-next-line @next/next/no-img-element
              <img
                src={product.vendor_logo_url}
                alt=""
                className="size-[22px] rounded-full object-cover"
              />
            ) : (
              <span className="grid size-[22px] place-items-center rounded-full bg-brand-soft text-[11px] font-bold text-brand">
                {letter}
              </span>
            )}
            <span>{product.vendor_name || "Verified vendor"}</span>
          </div>
          <span className="inline-flex h-8 items-center gap-1.5 rounded-full border-[1.5px] border-brand/35 px-3 text-xs font-bold text-brand">
            View Details <ArrowRight className="size-3" />
          </span>
        </div>
      </div>
    </a>
  );
}

function VendorCard({ vendor }: { vendor: Vendor }) {
  const count = Number(vendor.product_count) || 0;
  const href = `/products.html?vendor_id=${encodeURIComponent(vendor.vendor_id)}&vendor_name=${encodeURIComponent(vendor.vendor_name || "")}`;
  const letter = initial(vendor.vendor_name);

  return (
    <a
      href={href}
      className="grid grid-cols-[56px_1fr_auto] items-center gap-3 rounded-xl border border-line border-l-4 border-l-brand bg-white p-4 shadow-[0_8px_24px_rgba(15,40,90,0.08)] transition hover:-translate-y-0.5"
    >
      {vendor.vendor_logo_url ? (
        // eslint-disable-next-line @next/next/no-img-element
        <img
          src={vendor.vendor_logo_url}
          alt={vendor.vendor_name || "Vendor"}
          className="size-14 rounded-xl object-cover"
        />
      ) : (
        <div className="grid size-14 place-items-center rounded-xl bg-brand-soft text-xl font-extrabold text-brand">
          {letter}
        </div>
      )}
      <div>
        <h3 className="mb-1 text-sm font-bold text-navy">{vendor.vendor_name || "Vendor"}</h3>
        <p className="text-xs text-ink-3">
          {vendor.vendor_location ? (
            <>
              <MapPin className="mr-1 inline size-3" />
              {vendor.vendor_location} ·{" "}
            </>
          ) : null}
          <span className="font-bold text-green">
            <Check className="mr-0.5 inline size-3" />
            {count} listing{count === 1 ? "" : "s"}
          </span>
        </p>
      </div>
      <span className="grid size-[34px] place-items-center rounded-full bg-brand text-white">
        <ArrowRight className="size-3.5" />
      </span>
    </a>
  );
}
