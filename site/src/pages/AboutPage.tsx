import { useEffect } from "react";
import { AboutSection } from "../components/AboutSection";
import { CtaSection } from "../components/Footer";
import { Shell } from "../components/Shell";

export function AboutPage() {
  useEffect(() => {
    document.title = "About Us — My Laptop Market";
    return () => {
      document.title = "My Laptop Market — India's Live B2B IT Marketplace";
    };
  }, []);

  return (
    <Shell>
      {(api) => (
        <>
          <AboutSection onRegister={api.onRegister} />
          <CtaSection onRegister={api.onRegister} onRequirement={api.onRequirement} />
        </>
      )}
    </Shell>
  );
}
