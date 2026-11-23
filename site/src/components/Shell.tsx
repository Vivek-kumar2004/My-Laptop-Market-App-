import { useCallback, useEffect, useState, type ReactNode } from "react";
import { useNavigate } from "react-router-dom";
import type { Product } from "../data";
import { cn } from "../utils/cn";
import { useScrollProgress, useScrolled } from "../lib/motion";
import { Icon } from "./Icon";
import { Header } from "./Header";
import { SideDock } from "./SideDock";
import { Footer } from "./Footer";
import { floatingDockConfig } from "../lib/floatingDock";
import { AppPromoModal } from "./AppPromoModal";
import { FloatingDock } from "./FloatingDock";
import { ProductModal, RegisterDialog, type DialogIntent } from "./Dialogs";

const WHATSAPP = floatingDockConfig.whatsappUrl;

export function Shell({
  children,
}: {
  children: ReactNode | ((api: ShellApi) => ReactNode);
}) {
  const navigate = useNavigate();
  const progress = useScrollProgress();
  const scrolled = useScrolled(560);
  const [dialog, setDialog] = useState<{ intent: DialogIntent } | null>(null);
  const [quickView, setQuickView] = useState<Product | null>(null);

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
    if (window.location.pathname === "/" || window.location.pathname === "") {
      document.getElementById("marketplace")?.scrollIntoView({ behavior: "smooth", block: "start" });
      return;
    }
    navigate("/#marketplace");
  }, [navigate]);

  const api: ShellApi = {
    openDialog,
    setQuickView,
    scrollToMarketplace,
    onRequirement: () => openDialog("buyer"),
    onRegister: () => openDialog("vendor"),
  };

  return (
    <div className="relative min-h-screen bg-white">
      <div className="fixed inset-x-0 top-0 z-[80] h-[3px] bg-transparent">
        <div
          className="h-full bg-gradient-to-r from-brand via-brand-bright to-verified transition-[width] duration-150 ease-out"
          style={{ width: `${progress * 100}%` }}
        />
      </div>

      <a
        href="/#marketplace"
        className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-[130] focus:rounded-full focus:bg-navy focus:px-5 focus:py-3 focus:text-sm focus:font-semibold focus:text-white"
      >
        Skip to live marketplace
      </a>

      <Header onRegister={() => openDialog("vendor")} onOpenProduct={setQuickView} />
      <SideDock onRegister={() => openDialog("vendor")} />

      <main>{typeof children === "function" ? children(api) : children}</main>

      <Footer onRegister={() => openDialog("vendor")} />

      <FloatingDock visible={scrolled && !overlayOpen} />

      <div
        className={cn(
          "fixed inset-x-3 bottom-3 z-40 transition-all duration-500 lg:hidden",
          scrolled && !overlayOpen ? "translate-y-0 opacity-100" : "pointer-events-none translate-y-24 opacity-0",
        )}
      >
        <div className="flex items-center gap-1.5 rounded-2xl border border-line bg-white/92 p-1.5 shadow-[0_18px_50px_-18px_rgba(11,27,58,0.45)] backdrop-blur-xl">
          <button
            type="button"
            onClick={() => openDialog("buyer")}
            className="btn btn-primary h-10 min-h-0 flex-1 px-4 text-[12px]"
          >
            <Icon name="package" size={14} /> Post a requirement
          </button>
          <a
            href="/#marketplace"
            className="flex h-10 w-11 items-center justify-center rounded-full border border-line text-navy"
            aria-label="Browse the live marketplace"
          >
            <Icon name="grid" size={16} />
          </a>
          <a
            href={WHATSAPP}
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
      <AppPromoModal blocked={overlayOpen} />
    </div>
  );
}

export type ShellApi = {
  openDialog: (intent: DialogIntent) => void;
  setQuickView: (product: Product | null) => void;
  scrollToMarketplace: () => void;
  onRequirement: () => void;
  onRegister: () => void;
};
