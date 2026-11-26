import { useEffect } from "react";
import { BrowserRouter, Navigate, Route, Routes, useLocation } from "react-router-dom";
import { VendorAuthProvider } from "./lib/auth";
import { CatalogProvider } from "./lib/catalog";
import { HomePage } from "./pages/HomePage";
import { WhyPage } from "./pages/WhyPage";
import { HowPage } from "./pages/HowPage";
import { VendorsPage } from "./pages/VendorsPage";
import { InsightsPage } from "./pages/InsightsPage";
import { AboutPage } from "./pages/AboutPage";
import { DownloadPage } from "./pages/DownloadPage";

function ScrollManager() {
  const { pathname, hash } = useLocation();

  useEffect(() => {
    if (hash) {
      const id = hash.replace("#", "");
      window.requestAnimationFrame(() => {
        document.getElementById(id)?.scrollIntoView({ behavior: "smooth", block: "start" });
      });
      return;
    }
    window.scrollTo(0, 0);
  }, [pathname, hash]);

  return null;
}

export default function App() {
  return (
    <BrowserRouter>
      <VendorAuthProvider>
        <CatalogProvider>
          <ScrollManager />
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/why" element={<Navigate to="/why/" replace />} />
            <Route path="/why/" element={<WhyPage />} />
            <Route path="/how" element={<Navigate to="/how/" replace />} />
            <Route path="/how/" element={<HowPage />} />
            <Route path="/vendors" element={<Navigate to="/vendors/" replace />} />
            <Route path="/vendors/" element={<VendorsPage />} />
            <Route path="/insights" element={<Navigate to="/insights/" replace />} />
            <Route path="/insights/" element={<InsightsPage />} />
            <Route path="/about" element={<Navigate to="/about/" replace />} />
            <Route path="/about/" element={<AboutPage />} />
            <Route path="/download" element={<Navigate to="/download/" replace />} />
            <Route path="/download/" element={<DownloadPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </CatalogProvider>
      </VendorAuthProvider>
    </BrowserRouter>
  );
}
