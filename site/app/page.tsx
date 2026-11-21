import { Hero } from "@/components/Hero";
import { HomeShowcase } from "@/components/HomeShowcase";
import { HowItWorks } from "@/components/HowItWorks";
import { Newsletter } from "@/components/Newsletter";
import { SiteFooter } from "@/components/SiteFooter";
import { SiteHeader } from "@/components/SiteHeader";
import { WhatsAppFloat } from "@/components/WhatsAppFloat";
import { WhyUs } from "@/components/WhyUs";

export default function HomePage() {
  return (
    <>
      <SiteHeader />
      <main>
        <Hero />
        <HomeShowcase />
        <WhyUs />
        <HowItWorks />
        <Newsletter />
      </main>
      <SiteFooter />
      <WhatsAppFloat />
    </>
  );
}
