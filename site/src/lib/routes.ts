export const routes = {
  home: "/",
  marketplace: "/#marketplace",
  why: "/why/",
  how: "/how/",
  vendors: "/vendors/",
  insights: "/insights/",
  about: "/about/",
  download: "/download/",
  faq: "/#faq",
} as const;

export type NavId = "marketplace" | "why" | "how" | "vendors" | "insights" | "about" | "download";

export const navItems: { label: string; href: string; id: NavId }[] = [
  { label: "Download app", href: routes.download, id: "download" },
  { label: "Why us", href: routes.why, id: "why" },
  { label: "How it works", href: routes.how, id: "how" },
  { label: "Our Partners", href: routes.vendors, id: "vendors" },
  { label: "About us", href: routes.about, id: "about" },
];
