import { useEffect } from "react";
import { Shell } from "../components/Shell";
import { VendorsSection } from "../components/Vendors";
import { CtaSection } from "../components/Footer";

export function VendorsPage() {
  useEffect(() => {
    document.title = "Vendors — My Laptop Market";
    return () => {
      document.title = "My Laptop Market — India's Live B2B IT Marketplace";
    };
  }, []);

  return (
    <Shell>
      {(api) => (
        <>
          <VendorsSection onRegister={api.onRegister} />
          <CtaSection onRegister={api.onRegister} onRequirement={api.onRequirement} />
        </>
      )}
    </Shell>
  );
}
