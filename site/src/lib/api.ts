import type { Product, Vendor } from "../data";

export const API_BASE = "https://asia-south1-mylaptopmarket-app.cloudfunctions.net/api/api";
export const SITE_ORIGIN = "https://mylaptopmarket.com";

export type LiveVendor = {
  vendor_id: number;
  vendor_name: string;
  vendor_location?: string;
  product_count?: number;
  vendor_logo_url?: string;
};

type ApiProduct = {
  id: number | string;
  product_name?: string;
  category?: string;
  price?: string | number;
  stock?: number;
  main_image?: string;
  image_urls?: string[];
  vendor_id?: number;
  vendor_name?: string;
  vendor_logo_url?: string;
  vendor_shop_trusted?: boolean;
  vendor_location?: string;
};

const TINTS = [
  "from-[#0767D8] to-[#1E86FF]",
  "from-[#0B1B3A] to-[#0767D8]",
  "from-[#054FA8] to-[#2B8BFF]",
  "from-[#14294D] to-[#2B8BFF]",
  "from-[#0767D8] to-[#5EA9FF]",
];

function initials(name: string) {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  if (!parts.length) return "V";
  if (parts.length === 1) return parts[0]!.slice(0, 2).toUpperCase();
  return `${parts[0]![0] ?? ""}${parts[1]![0] ?? ""}`.toUpperCase();
}

function brandFromName(name: string) {
  const first = name.trim().split(/\s+/)[0] ?? "";
  const known = ["HP", "Dell", "Lenovo", "Apple", "Acer", "ASUS", "Microsoft", "HPE", "Cisco", "Samsung"];
  const hit = known.find((b) => first.toLowerCase() === b.toLowerCase() || name.toLowerCase().startsWith(b.toLowerCase()));
  return hit ?? (first || "IT");
}

function specsFromName(name: string): string[] {
  const specs: string[] = [];
  const ram = name.match(/(\d+)\s*\/\s*(\d+)\s*GB/i) || name.match(/(\d+)\s*GB/i);
  const ssd = name.match(/(\d+)\s*GB\s*SSD/i) || name.match(/\/(\d+)GB/i);
  const gen = name.match(/(\d+)(?:th|st|nd|rd)?\s*Gen/i) || name.match(/i([3579])/i);
  if (gen) specs.push(gen[0]!.replace(/\s+/g, " "));
  if (ram) specs.push(`${ram[1]}GB RAM`);
  if (ssd && !String(ssd[0]).includes("/")) specs.push(ssd[0]!);
  const slash = name.match(/(\d+)\s*\/\s*(\d+)/);
  if (slash && specs.length < 2) {
    specs.push(`${slash[1]}GB / ${slash[2]}GB`);
  }
  return specs.slice(0, 4);
}

function mapCategory(raw?: string): Product["category"] {
  const value = (raw || "Laptops").trim();
  if (/server/i.test(value)) return "Servers";
  if (/network|switch|router/i.test(value)) return "Networking";
  if (/monitor|display/i.test(value)) return "Monitors";
  if (/laptop|notebook|macbook/i.test(value)) return "Laptops";
  return "Laptops";
}

export function productDetailUrl(id: string | number) {
  return `${SITE_ORIGIN}/product.html?id=${encodeURIComponent(String(id))}`;
}

export function mapApiProduct(p: ApiProduct, index = 0): Product {
  const name = p.product_name?.trim() || "IT listing";
  const price = Number(p.price) || 0;
  const image = p.main_image || p.image_urls?.[0] || "/images/lap1.jpg";
  const vendorName = p.vendor_name?.trim() || "Verified vendor";
  const vendor: Vendor = {
    name: vendorName,
    initials: initials(vendorName),
    city: p.vendor_location?.trim() || "India",
    verified: Boolean(p.vendor_shop_trusted ?? true),
    rating: 4.8,
    deals: Math.max(1, Number(p.stock) || 1),
    tint: TINTS[index % TINTS.length]!,
    logoUrl: p.vendor_logo_url,
  };

  return {
    id: String(p.id),
    name,
    brand: brandFromName(name),
    category: mapCategory(p.category),
    condition: "Refurbished",
    specs: specsFromName(name),
    price,
    mrp: price > 0 ? Math.round(price * 1.35) : 0,
    city: vendor.city,
    units: Math.max(1, Number(p.stock) || 1),
    moq: 1,
    listed: "Live now",
    image,
    vendor,
    featured: index < 3,
    tags: p.stock && p.stock >= 10 ? ["Bulk ready"] : ["Live stock"],
    detailUrl: productDetailUrl(p.id),
  };
}

export async function fetchHomeShowcase(): Promise<{ products: Product[]; vendors: LiveVendor[] }> {
  const res = await fetch(`${API_BASE}/web/home-showcase`, { credentials: "omit" });
  if (!res.ok) throw new Error(`Showcase ${res.status}`);
  const data = (await res.json()) as { products?: ApiProduct[]; vendors?: LiveVendor[] };
  const vendors = data.vendors || [];
  const cityByVendor = new Map(vendors.map((v) => [v.vendor_id, v.vendor_location?.trim() || ""]));
  const products = (data.products || []).map((p, i) => {
    const loc = p.vendor_id != null ? cityByVendor.get(p.vendor_id) : "";
    return mapApiProduct({ ...p, vendor_location: p.vendor_location || loc || undefined }, i);
  });
  return { products, vendors };
}

export async function fetchWebProducts(limit = 24): Promise<Product[]> {
  const res = await fetch(`${API_BASE}/web/products`, { credentials: "omit" });
  if (!res.ok) throw new Error(`Products ${res.status}`);
  const data = (await res.json()) as { data?: ApiProduct[] };
  return (data.data || []).slice(0, limit).map((p, i) => mapApiProduct(p, i));
}
