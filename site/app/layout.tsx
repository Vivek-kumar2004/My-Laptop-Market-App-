import type { Metadata } from "next";
import { Bricolage_Grotesque, Plus_Jakarta_Sans } from "next/font/google";
import "./globals.css";

const jakarta = Plus_Jakarta_Sans({
  variable: "--font-jakarta",
  subsets: ["latin"],
  weight: ["400", "500", "600", "700", "800"],
});

const bricolage = Bricolage_Grotesque({
  variable: "--font-bricolage",
  subsets: ["latin"],
  weight: ["500", "600", "700", "800"],
});

export const metadata: Metadata = {
  title: "My Laptop Market - India's B2B IT Vendors Network",
  description:
    "My Laptop Market is a B2B networking and lead-generation platform for India's IT industry. Connect refurbished IT dealers, technology suppliers, and vendors nationwide.",
  metadataBase: new URL("https://mylaptopmarket.com"),
  openGraph: {
    title: "My Laptop Market - India's B2B IT Vendors Network",
    description:
      "India's B2B IT vendors network. Connect refurbished IT dealers, technology suppliers, and businesses nationwide.",
    url: "https://mylaptopmarket.com/",
    siteName: "My Laptop Market",
    images: [
      {
        url: "/images/og-brand.jpg",
        width: 1200,
        height: 630,
      },
    ],
  },
  twitter: {
    card: "summary_large_image",
    title: "My Laptop Market - India's B2B IT Vendors Network",
    images: ["/images/og-brand.jpg"],
  },
  icons: {
    icon: "/favicon.ico",
    apple: "/apple-touch-icon.png",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className={`${jakarta.variable} ${bricolage.variable} h-full antialiased`}>
      <body className="min-h-full font-sans text-navy">{children}</body>
    </html>
  );
}
