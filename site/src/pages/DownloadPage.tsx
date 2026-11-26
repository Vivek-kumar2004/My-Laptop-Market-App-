import { useEffect } from "react";
import { DownloadApp } from "../components/DownloadApp";
import { CtaSection } from "../components/Footer";
import { Shell } from "../components/Shell";

export function DownloadPage() {
  useEffect(() => {
    document.title = "Download App — My Laptop Market";
    return () => {
      document.title = "My Laptop Market — India's Live B2B IT Marketplace";
    };
  }, []);

  return (
    <Shell>
      {(api) => (
        <>
          <DownloadApp />
          <CtaSection onRegister={api.onRegister} onRequirement={api.onRequirement} />
        </>
      )}
    </Shell>
  );
}
