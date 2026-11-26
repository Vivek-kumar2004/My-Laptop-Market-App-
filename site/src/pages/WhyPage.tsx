import { useEffect } from "react";
import { Shell } from "../components/Shell";
import { NetworkSection } from "../components/NetworkSection";
import { CtaSection } from "../components/Footer";

export function WhyPage() {
  useEffect(() => {
    document.title = "Why Us — My Laptop Market";
    return () => {
      document.title = "My Laptop Market — India's Live B2B IT Marketplace";
    };
  }, []);

  return (
    <Shell>
      {(api) => (
        <>
          <NetworkSection onRegister={api.onRegister} />
          <CtaSection onRegister={api.onRegister} onRequirement={api.onRequirement} />
        </>
      )}
    </Shell>
  );
}
