import type { ReactNode } from "react";
import { floatingDockConfig } from "../lib/floatingDock";
import { cn } from "../utils/cn";
import { Icon } from "./Icon";

function GooglePlayMark({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" className={className} aria-hidden="true" focusable="false">
      <path fill="#EA4335" d="M3.6 2.3 13.4 12 3.6 21.7A1.9 1.9 0 0 1 3 20.1V3.9c0-.7.2-1.2.6-1.6Z" />
      <path fill="#FBBC04" d="m13.4 12 2.7-2.7 4.6 2.6c.7.4.7 1.1 0 1.5l-4.6 2.6L13.4 12Z" />
      <path fill="#4285F4" d="M13.4 12 3.6 2.3c.4-.4 1-.5 1.6-.2L16.1 9.3 13.4 12Z" />
      <path fill="#34A853" d="M13.4 12 16.1 14.7 5.2 21.9c-.6.3-1.2.2-1.6-.2L13.4 12Z" />
    </svg>
  );
}

function AppleMark({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" className={className} aria-hidden="true" focusable="false" fill="currentColor">
      <path d="M16.65 12.65c0-2.05 1.72-3.05 1.8-3.1-.98-1.43-2.5-1.63-3.04-1.65-1.3-.13-2.53.76-3.18.76-.66 0-1.67-.74-2.75-.72-1.41.02-2.72.82-3.45 2.08-1.48 2.56-.38 6.35 1.06 8.43.7 1.02 1.54 2.16 2.64 2.12 1.06-.04 1.46-.68 2.74-.68 1.27 0 1.63.68 2.75.66 1.14-.02 1.86-1.03 2.55-2.05.8-1.17 1.13-2.3 1.15-2.36-.02-.01-2.2-.84-2.22-3.49zm-2.08-6.13c.58-.7.97-1.67.86-2.64-.83.03-1.84.55-2.43 1.25-.53.62-1 1.61-.87 2.56.92.07 1.86-.47 2.44-1.17z" />
    </svg>
  );
}

function DockButton({
  href,
  label,
  className,
  children,
}: {
  href: string;
  label: string;
  className?: string;
  children: ReactNode;
}) {
  return (
    <a
      href={href}
      target="_blank"
      rel="noopener noreferrer"
      aria-label={label}
      title={label}
      className={cn(
        "group relative flex h-11 w-11 items-center justify-center rounded-full border border-line bg-white text-navy shadow-soft transition hover:-translate-y-1 hover:border-brand/30 hover:shadow-lift",
        className,
      )}
    >
      {children}
      <span
        role="tooltip"
        className="pointer-events-none absolute right-full top-1/2 mr-2.5 -translate-y-1/2 whitespace-nowrap rounded-md bg-navy px-2 py-1 text-[10px] font-semibold text-white opacity-0 shadow-soft transition group-hover:opacity-100 group-focus-visible:opacity-100 max-lg:hidden"
      >
        {label}
      </span>
    </a>
  );
}

export function FloatingDock({
  visible,
  showBackToTop = true,
}: {
  visible: boolean;
  showBackToTop?: boolean;
}) {
  const { showWhatsApp, showGooglePlay, showAppStore, whatsappUrl, googlePlayUrl, appStoreUrl } =
    floatingDockConfig;

  const actionButtons = [
    showWhatsApp ? (
      <DockButton key="whatsapp" href={whatsappUrl} label="WhatsApp" className="hover:text-[#25D366]">
        <Icon name="whatsapp" size={18} />
      </DockButton>
    ) : null,
    showGooglePlay ? (
      <DockButton key="play" href={googlePlayUrl} label="Download on Google Play">
        <GooglePlayMark className="h-[18px] w-[18px]" />
      </DockButton>
    ) : null,
    showAppStore ? (
      <DockButton key="apple" href={appStoreUrl} label="Download on App Store" className="text-navy">
        <AppleMark className="h-[18px] w-[18px]" />
      </DockButton>
    ) : null,
  ].filter(Boolean);

  if (!actionButtons.length && !showBackToTop) return null;

  return (
    <div
      className={cn(
        "fixed right-3 z-40 flex flex-col gap-2 transition-all duration-500 sm:right-4",
        /* Sit above the mobile bottom CTA bar; desktop uses the normal corner. */
        "bottom-[calc(4.75rem+env(safe-area-inset-bottom,0px))] lg:bottom-4",
        visible ? "translate-y-0 opacity-100" : "pointer-events-none translate-y-6 opacity-0",
      )}
    >
      {actionButtons}
      {showBackToTop ? (
        <button
          type="button"
          onClick={() => window.scrollTo({ top: 0, behavior: "smooth" })}
          className="group relative hidden h-11 w-11 items-center justify-center rounded-full border border-line bg-white text-navy shadow-soft transition hover:-translate-y-1 hover:border-brand/30 hover:text-brand lg:flex"
          aria-label="Back to top"
          title="Back to top"
        >
          <Icon name="arrow-down" size={17} className="rotate-180" />
        </button>
      ) : null}
    </div>
  );
}
