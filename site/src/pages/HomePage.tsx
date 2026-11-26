import { Shell, type ShellApi } from "../components/Shell";
import { Marketplace } from "../components/Marketplace";
import { NetworkSection } from "../components/NetworkSection";
import { HowItWorks } from "../components/HowItWorks";
import { VendorsSection } from "../components/Vendors";
import { FaqSection, InsightsSection } from "../components/InsightsFaq";
import { CtaSection } from "../components/Footer";
import { StatsBand } from "../components/HomeExtras";

export function HomePage() {
  return (
    <Shell>
      {(api: ShellApi) => (
        <>
          <Marketplace
            initialQuery=""
            initialCategory="All"
            initialCity="All cities"
            queryKey={0}
            onQuickView={api.setQuickView}
            onRequirement={api.onRequirement}
          />
          <StatsBand />
          <NetworkSection onRegister={api.onRegister} />
          <HowItWorks onRegister={api.onRegister} />
          <VendorsSection onRegister={api.onRegister} />
          <InsightsSection />
          <FaqSection />
          <CtaSection onRegister={api.onRegister} onRequirement={api.onRequirement} />
        </>
      )}
    </Shell>
  );
}
