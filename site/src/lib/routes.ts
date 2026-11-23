export const routes = {
  home: "/",
  marketplace: "/#marketplace",
  why: "/why/",
  how: "/how/",
  vendors: "/vendors/",
  insights: "/insights/",
  faq: "/insights/#faq",
} as const;

export type NavId = "marketplace" | "why" | "how" | "vendors" | "insights";

export const navItems: { label: string; href: string; id: NavId }[] = [
  { label: "Live marketplace", href: routes.marketplace, id: "marketplace" },
  { label: "Why us", href: routes.why, id: "why" },
  { label: "How it works", href: routes.how, id: "how" },
  { label: "Vendors", href: routes.vendors, id: "vendors" },
  { label: "Insights", href: routes.insights, id: "insights" },
];
