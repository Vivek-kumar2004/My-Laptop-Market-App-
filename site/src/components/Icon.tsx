import type { SVGProps } from "react";
import { cn } from "../utils/cn";

export type IconName =
  | "search"
  | "arrow-up-right"
  | "arrow-right"
  | "arrow-left"
  | "arrow-down"
  | "chevron-down"
  | "chevron-right"
  | "chevron-left"
  | "plus"
  | "minus"
  | "check"
  | "x"
  | "menu"
  | "shield"
  | "sparkles"
  | "laptop"
  | "server"
  | "network"
  | "monitor"
  | "cpu"
  | "building"
  | "pin"
  | "users"
  | "truck"
  | "badge"
  | "zap"
  | "globe"
  | "package"
  | "layers"
  | "heart"
  | "sliders"
  | "message"
  | "phone"
  | "mail"
  | "instagram"
  | "linkedin"
  | "youtube"
  | "quote"
  | "star"
  | "clock"
  | "trending"
  | "refresh"
  | "play"
  | "command"
  | "store"
  | "book"
  | "headphones"
  | "handshake"
  | "rupee"
  | "send"
  | "whatsapp"
  | "grid"
  | "filter"
  | "lock"
  | "calendar"
  | "scan"
  | "location"
  | "copy";

type IconProps = SVGProps<SVGSVGElement> & {
  name: IconName;
  size?: number;
  strokeWidth?: number;
};

const paths: Record<IconName, React.ReactNode> = {
  search: (
    <>
      <circle cx="11" cy="11" r="7" />
      <path d="m20 20-3.5-3.5" />
    </>
  ),
  "arrow-up-right": (
    <>
      <path d="M7 17 17 7" />
      <path d="M8 7h9v9" />
    </>
  ),
  "arrow-right": (
    <>
      <path d="M4 12h15" />
      <path d="m13 6 6 6-6 6" />
    </>
  ),
  "arrow-left": (
    <>
      <path d="M20 12H5" />
      <path d="m11 6-6 6 6 6" />
    </>
  ),
  "arrow-down": <path d="M12 4v15m0 0 6-6m-6 6-6-6" />,
  "chevron-down": <path d="m6 9 6 6 6-6" />,
  "chevron-right": <path d="m9 6 6 6-6 6" />,
  "chevron-left": <path d="m15 6-6 6 6 6" />,
  plus: <path d="M12 5v14M5 12h14" />,
  minus: <path d="M5 12h14" />,
  check: <path d="m4.5 12.5 5 5 10-11" />,
  x: <path d="M6 6l12 12M18 6 6 18" />,
  menu: <path d="M4 7h16M4 12h16M4 17h10" />,
  shield: (
    <>
      <path d="M12 3l7.5 3v5.6c0 4.6-3.1 7.9-7.5 9.4-4.4-1.5-7.5-4.8-7.5-9.4V6L12 3Z" />
      <path d="m8.8 12 2.2 2.2 4.3-4.5" />
    </>
  ),
  sparkles: (
    <>
      <path d="M12 3.5 13.6 9 19 10.5 13.6 12 12 17.5 10.4 12 5 10.5 10.4 9 12 3.5Z" />
      <path d="M18.5 15.5 19.2 18l2.3.8-2.3.7-.7 2.5-.7-2.5-2.3-.7 2.3-.8.7-2.5Z" />
    </>
  ),
  laptop: (
    <>
      <rect x="4" y="5" width="16" height="11" rx="1.8" />
      <path d="M2 19.5h20M9 19.5l.6-3h4.8l.6 3" />
    </>
  ),
  server: (
    <>
      <rect x="3" y="4" width="18" height="6.5" rx="1.6" />
      <rect x="3" y="13.5" width="18" height="6.5" rx="1.6" />
      <path d="M7 7.2h.01M7 16.8h.01M11 7.2h4M11 16.8h4" />
    </>
  ),
  network: (
    <>
      <rect x="9" y="3" width="6" height="5" rx="1.2" />
      <rect x="2.5" y="16" width="6" height="5" rx="1.2" />
      <rect x="15.5" y="16" width="6" height="5" rx="1.2" />
      <path d="M12 8v4m0 0H5.5v4M12 12h6.5v4" />
    </>
  ),
  monitor: (
    <>
      <rect x="2.5" y="4" width="19" height="12.5" rx="1.8" />
      <path d="M9 20.5h6M12 16.5v4" />
    </>
  ),
  cpu: (
    <>
      <rect x="6.5" y="6.5" width="11" height="11" rx="2" />
      <rect x="10" y="10" width="4" height="4" rx="1" />
      <path d="M9 3v3.5M15 3v3.5M9 17.5V21M15 17.5V21M3 9h3.5M3 15h3.5M17.5 9H21M17.5 15H21" />
    </>
  ),
  building: (
    <>
      <path d="M4 21V6.5L12 3v18M12 21h8V10l-8-3.5" />
      <path d="M7.5 9h1M7.5 13h1M7.5 17h1M15.5 13h1M15.5 17h1" />
    </>
  ),
  pin: (
    <>
      <path d="M19 10c0 5-7 11-7 11s-7-6-7-11a7 7 0 1 1 14 0Z" />
      <circle cx="12" cy="10" r="2.4" />
    </>
  ),
  "location": (
    <>
      <path d="M19 10c0 5-7 11-7 11s-7-6-7-11a7 7 0 1 1 14 0Z" />
      <circle cx="12" cy="10" r="2.4" />
    </>
  ),
  users: (
    <>
      <circle cx="9" cy="8" r="3.5" />
      <path d="M2.5 20c.5-3.6 3.2-6 6.5-6s6 2.4 6.5 6" />
      <path d="M16.5 5a3.5 3.5 0 0 1 0 6.8M18 14.2c2.1.6 3.3 2.5 3.5 5.8" />
    </>
  ),
  truck: (
    <>
      <path d="M2.5 6h11v11h-11zM13.5 10H18l3.5 3.5V17h-8" />
      <circle cx="7" cy="18.5" r="1.8" />
      <circle cx="17.5" cy="18.5" r="1.8" />
    </>
  ),
  badge: (
    <>
      <path d="M12 2.8l2.5 1.6 3-.2.9 2.8 2.3 1.9-1.2 2.7.5 3-2.7 1.4-1.4 2.7-3-.5-2.6 1.5-2.2-2-3-.3-.3-3-1.9-2.3L5 9.5 4.8 6.4l3-.6L9.7 3l2.3.9Z" />
      <path d="m8.7 11.9 2.1 2.1 4.3-4.4" />
    </>
  ),
  zap: <path d="M13.5 2.5 4 14h6l-1.5 7.5L20 9h-6l-.5-6.5Z" />,
  globe: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M3.5 9h17M3.5 15h17M12 3a15 15 0 0 1 0 18 15 15 0 0 1 0-18Z" />
    </>
  ),
  package: (
    <>
      <path d="M12 3 3.5 7.5v9L12 21l8.5-4.5v-9L12 3Z" />
      <path d="m3.5 7.5 8.5 4.6 8.5-4.6M12 12.1V21" />
    </>
  ),
  layers: (
    <>
      <path d="m12 3 9 5-9 5-9-5 9-5Z" />
      <path d="m3 12.5 9 5 9-5M3 16.8l9 5 9-5" />
    </>
  ),
  heart: <path d="M12 20.3S3.5 15.4 3.5 9.4A4.6 4.6 0 0 1 12 6.6a4.6 4.6 0 0 1 8.5 2.8c0 6-8.5 10.9-8.5 10.9Z" />,
  sliders: (
    <>
      <path d="M4 7h9M17 7h3M4 17h3M11 17h9" />
      <circle cx="15" cy="7" r="2.3" />
      <circle cx="9" cy="17" r="2.3" />
    </>
  ),
  grid: (
    <>
      <rect x="3.5" y="3.5" width="7" height="7" rx="1.5" />
      <rect x="13.5" y="3.5" width="7" height="7" rx="1.5" />
      <rect x="3.5" y="13.5" width="7" height="7" rx="1.5" />
      <rect x="13.5" y="13.5" width="7" height="7" rx="1.5" />
    </>
  ),
  filter: <path d="M3 5h18l-7 8v6l-4 2v-8L3 5Z" />,
  message: <path d="M21 12a8.4 8.4 0 0 1-9 8.3 9.6 9.6 0 0 1-3.6-.7L3 21l1.4-4.8A8.2 8.2 0 0 1 3.5 12 8.5 8.5 0 0 1 12 3.7 8.5 8.5 0 0 1 21 12Z" />,
  phone: <path d="M6.6 3.5h3l1.5 4-2 1.5a13 13 0 0 0 6 6l1.5-2 4 1.5v3a2 2 0 0 1-2.2 2A17.5 17.5 0 0 1 4.6 5.7a2 2 0 0 1 2-2.2Z" />,
  mail: (
    <>
      <rect x="2.5" y="5" width="19" height="14" rx="2" />
      <path d="m3.5 7 8.5 6 8.5-6" />
    </>
  ),
  instagram: (
    <>
      <rect x="3" y="3" width="18" height="18" rx="5" />
      <circle cx="12" cy="12" r="4" />
      <path d="M17.5 6.6h.01" />
    </>
  ),
  linkedin: (
    <>
      <rect x="3" y="3" width="18" height="18" rx="3" />
      <path d="M7.5 10v7M7.5 7.2v.01M11.5 17v-4a2.5 2.5 0 0 1 5 0v4" />
    </>
  ),
  youtube: (
    <>
      <rect x="2.5" y="5.5" width="19" height="13" rx="4" />
      <path d="m10.5 9 5 3-5 3V9Z" />
    </>
  ),
  whatsapp: (
    <>
      <path d="M21 11.7a8.4 8.4 0 0 1-9 8.3 9.6 9.6 0 0 1-3.6-.7L3 21l1.4-4.8A8.2 8.2 0 0 1 3.5 11.7 8.5 8.5 0 0 1 12 3.4a8.5 8.5 0 0 1 9 8.3Z" />
      <path d="M8.8 8.4c0 3 2.4 5.4 5.4 5.4l1.4-1.5-1.9-1-1.1 1.1a4.6 4.6 0 0 1-2.4-2.4l1.1-1.1-1-1.9-1.5 1.4Z" />
    </>
  ),
  quote: <path d="M9.5 6.5C6.5 8 5 10.4 5 13.2c0 2.5 1.4 4.3 3.5 4.3 1.9 0 3.2-1.3 3.2-3.1 0-1.7-1.2-3-2.9-3-.3 0-.6 0-.8.1.4-1.5 1.5-2.8 3.1-3.7l-1.6-1.3Zm9 0C15.5 8 14 10.4 14 13.2c0 2.5 1.4 4.3 3.5 4.3 1.9 0 3.2-1.3 3.2-3.1 0-1.7-1.2-3-2.9-3-.3 0-.6 0-.8.1.4-1.5 1.5-2.8 3.1-3.7l-1.6-1.3Z" />,
  star: <path d="m12 3.5 2.7 5.5 6 .9-4.4 4.2 1.1 6L12 17.3l-5.4 2.8 1.1-6-4.4-4.2 6-.9L12 3.5Z" />,
  clock: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5.3l3.4 2" />
    </>
  ),
  trending: (
    <>
      <path d="m3 16 5.5-5.5 3.5 3.5L21 5" />
      <path d="M16 5h5v5" />
    </>
  ),
  refresh: (
    <>
      <path d="M20 11a8 8 0 0 0-14-5L3 9" />
      <path d="M3 4v5h5M4 13a8 8 0 0 0 14 5l3-3" />
      <path d="M21 20v-5h-5" />
    </>
  ),
  play: <path d="m8 5 11 7-11 7V5Z" />,
  command: <path d="M8 3a3 3 0 1 1 3 3v12a3 3 0 1 1-3-3h8a3 3 0 1 1-3 3V6a3 3 0 1 1 3 3H8Z" />,
  store: (
    <>
      <path d="M4 9.5V20h16V9.5M3 9.5l2-5.5h14l2 5.5a3 3 0 0 1-5.7 1.2 3 3 0 0 1-5.6 0A3 3 0 0 1 3 9.5Z" />
      <path d="M9.5 20v-5.5h5V20" />
    </>
  ),
  book: (
    <>
      <path d="M4 4.5h6a3 3 0 0 1 3 3V21a3 3 0 0 0-3-3H4V4.5Z" />
      <path d="M20 4.5h-4a3 3 0 0 0-3 3V21a3 3 0 0 1 3-3h4V4.5Z" />
    </>
  ),
  headphones: (
    <>
      <path d="M4 14v-2a8 8 0 0 1 16 0v2" />
      <path d="M4 14h3v6H5.5A1.5 1.5 0 0 1 4 18.5V14ZM20 14h-3v6h1.5a1.5 1.5 0 0 0 1.5-1.5V14Z" />
    </>
  ),
  handshake: (
    <>
      <path d="m2.5 12 4-4 4 1.5 3-1.5 4.5 2 3.5 3-2.5 4-4-2-2 1.5-3-2-3 2-4.5-3V12Z" />
      <path d="m6.5 8 3.5 3.5M17.5 14.5 14 11" />
    </>
  ),
  rupee: <path d="M6.5 3.5h11M6.5 8h11M15.5 3.5c0 4-3 5.5-7 5.5 3 0 5 1 6.5 4l2.5 5" />,
  send: (
    <>
      <path d="m21.5 2.5-7 19-3.5-7.5-7.5-3.5 18-8Z" />
      <path d="M21.5 2.5 11 13" />
    </>
  ),
  lock: (
    <>
      <rect x="4" y="10" width="16" height="11" rx="2.5" />
      <path d="M8 10V7a4 4 0 0 1 8 0v3M12 14.5v2.5" />
    </>
  ),
  calendar: (
    <>
      <rect x="3.5" y="5" width="17" height="16" rx="2" />
      <path d="M3.5 10h17M8 3v4M16 3v4" />
    </>
  ),
  scan: (
    <>
      <path d="M3.5 8V5.5A2 2 0 0 1 5.5 3.5H8M16 3.5h2.5a2 2 0 0 1 2 2V8M20.5 16v2.5a2 2 0 0 1-2 2H16M8 20.5H5.5a2 2 0 0 1-2-2V16M3.5 12h17" />
    </>
  ),
  copy: (
    <>
      <rect x="8.5" y="8.5" width="12" height="12" rx="2" />
      <path d="M15.5 8.5v-3a2 2 0 0 0-2-2h-9a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h3" />
    </>
  ),
};

export function Icon({ name, size = 18, strokeWidth = 1.7, className, ...props }: IconProps) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={strokeWidth}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      className={cn("shrink-0", className)}
      {...props}
    >
      {paths[name]}
    </svg>
  );
}

export function LogoMark({ className }: { className?: string }) {
  return (
    <img
      src="/images/logo.png"
      alt=""
      aria-hidden="true"
      className={cn("h-10 w-10 rounded-[11px] object-cover", className)}
    />
  );
}

export function Wordmark({ light = false, className }: { light?: boolean; className?: string }) {
  return (
    <span className={cn("flex flex-col leading-none", className)}>
      <span className="font-display text-[21px] font-bold tracking-[-0.035em]">
        <span className={light ? "text-white" : "text-navy"}>MY </span>
        <span className="text-brand">LAPTOP</span>
      </span>
      <span className={cn("mt-1 text-[8.5px] font-semibold tracking-[0.34em]", light ? "text-white/55" : "text-slate-ink/70")}>
        MARKET
      </span>
    </span>
  );
}

/** Full brand lockup used on the marketing site header/footer. */
export function BrandLogo({
  light = false,
  className,
}: {
  light?: boolean;
  className?: string;
}) {
  return (
    <img
      src="/images/mylaptoplogo.png"
      alt="My Laptop Market"
      className={cn(
        "h-11 w-auto max-w-[200px] object-contain object-left sm:h-12 sm:max-w-[220px]",
        light && "rounded-md bg-white/95 p-1",
        className,
      )}
    />
  );
}
