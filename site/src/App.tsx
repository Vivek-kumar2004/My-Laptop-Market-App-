import { useCallback, useEffect, useState } from "react";
import type { Product } from "./data";
import { cn } from "./utils/cn";
import { Icon } from "./components/Icon";
import { Header } from "./components/Header";
import { Hero } from "./components/Hero";
import { BrandMarquee, Marketplace } from "./components/Marketplace";
import { HowItWorks, NetworkSection } from "./components/NetworkSection";
import { VendorsSection } from "./components/Vendors";
import { FaqSection, InsightsSection } from "./components/InsightsFaq";
import { CtaSection, Footer } from "./components/Footer";
import { ProductModal, RegisterDialog, type DialogIntent } from "./components/Dialogs";
import { useScrollProgress, useScrolled } from "./lib/motion";

export default function App() {
  const progress = useScrollProgress();
  const scrolled = useScrolled(560);
  const [dialog, setDialog] = useState<{ intent: DialogIntent } | null>(null);
  const [quickView, setQuickView] = useState<Product | null>(null);
  const [search, setSearch] = useState({ query: "", category: "All", city: "All cities", key: 0 });

  const overlayOpen = Boolean(dialog || quickView);

  useEffect(() => {
    if (!overlayOpen) return;
    const onKey = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setDialog(null);
        setQuickView(null);
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [overlayOpen]);

  const openDialog = useCallback((intent: DialogIntent) => {
    setQuickView(null);
    setDialog({ intent });
  }, []);

  const scrollToMarketplace = useCallback(() => {
    document.getElementById("marketplace")?.scrollIntoView({ behavior: "smooth", block: "start" });
  }, []);

  const handleSearch = useCallback((query: string, category: string, city: string) => {
    setSearch({
      query,
      category: category === "All categories" ? "All" : category,
      city,
      key: Date.now(),
    });
    window.setTimeout(scrollToMarketplace, 40);
  }, [scrollToMarketplace]);

  return (
    <div className="relative min-h-screen bg-white">
      <div className="fixed inset-x-0 top-0 z-[80] h-[3px] bg-transparent">
        <div
          className="h-full bg-gradient-to-r from-brand via-brand-bright to-verified transition-[width] duration-150 ease-out"
          style={{ width: `${progress * 100}%` }}
        />
      </div>

      <a
        href="#marketplace"
        className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-[130] focus:rounded-full focus:bg-navy focus:px-5 focus:py-3 focus:text-sm focus:font-semibold focus:text-white"
      >
        Skip to live marketplace
      </a>

      <Header onRegister={() => openDialog("vendor")} onOpenProduct={setQuickView} />

      <main>
        <Hero onSearch={handleSearch} onQuickView={setQuickView} />
        <BrandMarquee />
        <Marketplace
          initialQuery={search.query}
          initialCategory={search.category}
          initialCity={search.city}
          queryKey={search.key}
          onQuickView={setQuickView}
          onRequirement={() => openDialog("buyer")}
        />
        <NetworkSection onRegister={() => openDialog("vendor")} />
        <HowItWorks onRegister={() => openDialog("vendor")} />
        <VendorsSection onRegister={() => openDialog("vendor")} />
        <InsightsSection />
        <FaqSection />
        <CtaSection onRegister={() => openDialog("vendor")} onRequirement={() => openDialog("buyer")} />
      </main>

      <Footer onRegister={() => openDialog("vendor")} />

      <div
        className={cn(
          "fixed right-4 bottom-4 z-40 hidden flex-col gap-2 transition-all duration-500 lg:flex",
          scrolled && !overlayOpen ? "translate-y-0 opacity-100" : "pointer-events-none translate-y-6 opacity-0",
        )}
      >
        <a
          href="https://wa.me/919718199976"
          target="_blank"
          rel="noreferrer"
          className="group flex h-11 w-11 items-center justify-center rounded-full border border-line bg-white text-navy shadow-soft transition hover:-translate-y-1 hover:border-brand/30 hover:text-brand"
          aria-label="Chat with us on WhatsApp"
        >
          <Icon name="whatsapp" size={18} />
        </a>
        <button
          onClick={() => window.scrollTo({ top: 0, behavior: "smooth" })}
          className="flex h-11 w-11 items-center justify-center rounded-full border border-line bg-white text-navy shadow-soft transition hover:-translate-y-1 hover:border-brand/30 hover:text-brand"
          aria-label="Back to top"
        >
          <Icon name="arrow-down" size={17} className="rotate-180" />
        </button>
      </div>

      <div
        className={cn(
          "fixed inset-x-3 bottom-3 z-40 transition-all duration-500 lg:hidden",
          scrolled && !overlayOpen ? "translate-y-0 opacity-100" : "pointer-events-none translate-y-24 opacity-0",
        )}
      >
        <div className="flex items-center gap-1.5 rounded-2xl border border-line bg-white/92 p-1.5 shadow-[0_18px_50px_-18px_rgba(11,27,58,0.45)] backdrop-blur-xl">
          <button onClick={() => openDialog("buyer")} className="btn btn-primary h-10 min-h-0 flex-1 px-4 text-[12px]">
            <Icon name="package" size={14} /> Post a requirement
          </button>
          <a
            href="#marketplace"
            className="flex h-10 w-11 items-center justify-center rounded-full border border-line text-navy"
            aria-label="Browse the live marketplace"
          >
            <Icon name="grid" size={16} />
          </a>
          <a
            href="https://wa.me/919718199976"
            target="_blank"
            rel="noreferrer"
            className="flex h-10 w-11 items-center justify-center rounded-full bg-navy text-white"
            aria-label="Chat on WhatsApp"
          >
            <Icon name="whatsapp" size={16} />
          </a>
        </div>
      </div>

      <RegisterDialog
        open={Boolean(dialog)}
        intent={dialog?.intent ?? "vendor"}
        onClose={() => setDialog(null)}
        onExplore={() => {
          setDialog(null);
          scrollToMarketplace();
        }}
      />
      <ProductModal
        product={quickView}
        onClose={() => setQuickView(null)}
        onRequirement={() => openDialog("buyer")}
      />
    </div>
  );
}
