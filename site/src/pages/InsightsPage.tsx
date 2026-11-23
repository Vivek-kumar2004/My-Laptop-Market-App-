import { useEffect } from "react";
import { Shell } from "../components/Shell";
import { FaqSection, InsightsSection } from "../components/InsightsFaq";
import { CtaSection } from "../components/Footer";

export function InsightsPage() {
  useEffect(() => {
    document.title = "Insights — My Laptop Market";
    return () => {
      document.title = "My Laptop Market — India's Live B2B IT Marketplace";
    };
  }, []);

  return (
    <Shell>
      {(api) => (
        <>
          <InsightsSection />
          <FaqSection />
          <CtaSection onRegister={api.onRegister} onRequirement={api.onRequirement} />
        </>
      )}
    </Shell>
  );
}
