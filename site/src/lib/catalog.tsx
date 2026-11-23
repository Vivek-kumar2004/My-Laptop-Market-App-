import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { products as fallbackProducts, type Product } from "../data";
import { fetchHomeShowcase, fetchWebProducts, mapApiProduct, type LiveVendor } from "./api";

type CatalogState = {
  products: Product[];
  vendors: LiveVendor[];
  loading: boolean;
  error: string | null;
  live: boolean;
};

const CatalogContext = createContext<CatalogState>({
  products: fallbackProducts,
  vendors: [],
  loading: true,
  error: null,
  live: false,
});

export function CatalogProvider({ children }: { children: ReactNode }) {
  const [products, setProducts] = useState<Product[]>(fallbackProducts);
  const [vendors, setVendors] = useState<LiveVendor[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [live, setLive] = useState(false);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const showcase = await fetchHomeShowcase();
        let next = showcase.products;
        try {
          const more = await fetchWebProducts(100);
          const seen = new Set(next.map((p) => p.id));
          next = [...next, ...more.filter((p) => !seen.has(p.id))];
        } catch {
          /* showcase alone is enough */
        }
        if (cancelled) return;
        if (next.length) {
          setProducts(next);
          setLive(true);
        }
        setVendors(showcase.vendors);
        setError(null);
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "Failed to load stock");
          setLive(false);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const value = useMemo(
    () => ({ products, vendors, loading, error, live }),
    [products, vendors, loading, error, live],
  );

  return <CatalogContext.Provider value={value}>{children}</CatalogContext.Provider>;
}

export function useCatalog() {
  return useContext(CatalogContext);
}

// re-export for callers that need mapping helpers
export { mapApiProduct };
