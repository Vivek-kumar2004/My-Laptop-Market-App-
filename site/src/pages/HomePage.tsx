import { useCallback, useState } from "react";
import { Shell, type ShellApi } from "../components/Shell";
import { Hero } from "../components/Hero";
import { BrandMarquee, Marketplace } from "../components/Marketplace";
import { FaqSection } from "../components/InsightsFaq";
import { CtaSection } from "../components/Footer";

export function HomePage() {
  const [search, setSearch] = useState({ query: "", category: "All", city: "All cities", key: 0 });

  return (
    <Shell>
      {(api: ShellApi) => (
        <HomeContent api={api} search={search} setSearch={setSearch} />
      )}
    </Shell>
  );
}

function HomeContent({
  api,
  search,
  setSearch,
}: {
  api: ShellApi;
  search: { query: string; category: string; city: string; key: number };
  setSearch: React.Dispatch<
    React.SetStateAction<{ query: string; category: string; city: string; key: number }>
  >;
}) {
  const handleSearch = useCallback(
    (query: string, category: string, city: string) => {
      setSearch({
        query,
        category: category === "All categories" ? "All" : category,
        city,
        key: Date.now(),
      });
      window.setTimeout(api.scrollToMarketplace, 40);
    },
    [api.scrollToMarketplace, setSearch],
  );

  return (
    <>
      <Hero onSearch={handleSearch} onQuickView={api.setQuickView} />
      <BrandMarquee />
      <Marketplace
        initialQuery={search.query}
        initialCategory={search.category}
        initialCity={search.city}
        queryKey={search.key}
        onQuickView={api.setQuickView}
        onRequirement={api.onRequirement}
      />
      <FaqSection />
      <CtaSection onRegister={api.onRegister} onRequirement={api.onRequirement} />
    </>
  );
}
