export const images = {
  hp: "/images/product-hp.png",
  dell: "/images/product-dell.png",
  thinkpad: "/images/product-thinkpad.png",
  macbook: "/images/product-macbook.png",
  server: "/images/product-server.png",
  monitor: "/images/product-monitor.png",
  network: "/images/product-network.png",
  lifestyle: "/images/lap1.jpg",
  lifestyleAlt: "/images/lap2.jpg",
  logo: "/images/mylaptoplogo.png",
  logoMark: "/images/logo.png",
  appDashboard: "/images/app/dashboard.png",
  appProducts: "/images/app/products.png",
  appSearch: "/images/app/search.png",
  appMessages: "/images/app/messages.png",
  googlePlayBadge: "/images/google-play-badge.png",
  appStoreBadge: "/images/app-store-badge.svg",
};

export const storeLinks = {
  googlePlay: "https://play.google.com/store/apps/details?id=com.mylaptopmarket",
  appStore: "https://apps.apple.com/in/app/my-laptop-market/id6811815946",
} as const;

export type AssetKey = keyof typeof images;
