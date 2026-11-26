import { useEffect } from "react";
import { Shell } from "../components/Shell";
import { HowItWorks } from "../components/HowItWorks";
import { CtaSection } from "../components/Footer";

export function HowPage() {
  useEffect(() => {
    document.title = "How It Works — My Laptop Market";
    return () => {
      document.title = "My Laptop Market — India's Live B2B IT Marketplace";
    };
  }, []);

  return (
    <Shell>
      {(api) => (
        <>
          <HowItWorks onRegister={api.onRegister} />
          <CtaSection onRegister={api.onRegister} onRequirement={api.onRequirement} />
        </>
      )}
    </Shell>
  );
}
