import type { IconName } from "@/components/landing/Icon";
import { images } from "@/lib/landing/assets";

export type Category = "Laptops" | "Servers" | "Networking" | "Monitors";

export type Vendor = {
  name: string;
  initials: string;
  city: string;
  verified: boolean;
  rating: number;
  deals: number;
  tint: string;
};

export type Product = {
  id: string;
  name: string;
  brand: string;
  category: Category;
  condition: string;
  specs: string[];
  price: number;
  mrp: number;
  city: string;
  units: number;
  moq: number;
  listed: string;
  image: string;
  vendor: Vendor;
  featured?: boolean;
  tags: string[];
};

export const products: Product[] = [
  {
    id: "hp-elitebook-840-g8",
    name: "HP EliteBook 840 G8",
    brand: "HP",
    category: "Laptops",
    condition: "Grade A",
    specs: ["i5-1145G7", "16GB RAM", "512GB SSD", '14" FHD'],
    price: 38500,
    mrp: 61990,
    city: "Mumbai",
    units: 24,
    moq: 5,
    listed: "12 min ago",
    image: images.hp,
    featured: true,
    tags: ["Bulk ready", "Warranty"],
    vendor: {
      name: "TechRenew India",
      initials: "TR",
      city: "Mumbai",
      verified: true,
      rating: 4.9,
      deals: 312,
      tint: "from-[#0767D8] to-[#1E86FF]",
    },
  },
  {
    id: "dell-latitude-5420",
    name: "Dell Latitude 5420",
    brand: "Dell",
    category: "Laptops",
    condition: "Grade A+",
    specs: ["i5-1145G7", "16GB RAM", "512GB SSD", '14" FHD'],
    price: 31200,
    mrp: 54500,
    city: "Bengaluru",
    units: 40,
    moq: 10,
    listed: "38 min ago",
    image: images.dell,
    featured: true,
    tags: ["40 units", "Invoice"],
    vendor: {
      name: "ReCompute Labs",
      initials: "RL",
      city: "Bengaluru",
      verified: true,
      rating: 4.8,
      deals: 248,
      tint: "from-[#0B1B3A] to-[#0767D8]",
    },
  },
  {
    id: "lenovo-thinkpad-t14-g3",
    name: "Lenovo ThinkPad T14 Gen 3",
    brand: "Lenovo",
    category: "Laptops",
    condition: "Grade A",
    specs: ["i7-1265U", "16GB RAM", "1TB SSD", '14" WUXGA'],
    price: 41900,
    mrp: 72990,
    city: "New Delhi",
    units: 12,
    moq: 3,
    listed: "1 h ago",
    image: images.thinkpad,
    tags: ["Warranty", "Business"],
    vendor: {
      name: "Sharma IT Traders",
      initials: "ST",
      city: "New Delhi",
      verified: true,
      rating: 4.7,
      deals: 190,
      tint: "from-[#054FA8] to-[#2B8BFF]",
    },
  },
  {
    id: "macbook-pro-14-m1",
    name: 'Apple MacBook Pro 14"',
    brand: "Apple",
    category: "Laptops",
    condition: "Grade A+",
    specs: ["M1 Pro", "16GB RAM", "512GB SSD", "Liquid Retina"],
    price: 94500,
    mrp: 159900,
    city: "Pune",
    units: 8,
    moq: 2,
    listed: "2 h ago",
    image: images.macbook,
    featured: true,
    tags: ["8 units", "Battery 91%"],
    vendor: {
      name: "iAsset Recovery",
      initials: "IA",
      city: "Pune",
      verified: true,
      rating: 4.9,
      deals: 421,
      tint: "from-[#14294D] to-[#2B8BFF]",
    },
  },
  {
    id: "hpe-proliant-dl380",
    name: "HPE ProLiant DL380 Gen10",
    brand: "HPE",
    category: "Servers",
    condition: "Grade A",
    specs: ["Xeon Silver 4210", "64GB ECC", "2× 960GB SSD", "2U Rack"],
    price: 145000,
    mrp: 310000,
    city: "Hyderabad",
    units: 6,
    moq: 1,
    listed: "3 h ago",
    image: images.server,
    tags: ["Tested", "Rails included"],
    vendor: {
      name: "ServerSphere",
      initials: "SS",
      city: "Hyderabad",
      verified: true,
      rating: 4.8,
      deals: 137,
      tint: "from-[#0B1B3A] to-[#054FA8]",
    },
  },
  {
    id: "dell-ultrasharp-u2723qe",
    name: "Dell UltraSharp U2723QE",
    brand: "Dell",
    category: "Monitors",
    condition: "Grade A",
    specs: ['27" 4K IPS', "Type-C 90W", "USB Hub", "HDR400"],
    price: 28400,
    mrp: 46800,
    city: "Chennai",
    units: 30,
    moq: 5,
    listed: "4 h ago",
    image: images.monitor,
    tags: ["Bulk ready", "Zero spots"],
    vendor: {
      name: "Chennai Peripherals",
      initials: "CP",
      city: "Chennai",
      verified: true,
      rating: 4.6,
      deals: 96,
      tint: "from-[#0767D8] to-[#5EA9FF]",
    },
  },
  {
    id: "cisco-catalyst-9200",
    name: "Cisco Catalyst 9200 48-Port",
    brand: "Cisco",
    category: "Networking",
    condition: "Grade A",
    specs: ["48× 1G Ports", "4× SFP+", "Managed L3", "Dual PSU"],
    price: 62700,
    mrp: 125000,
    city: "Noida",
    units: 15,
    moq: 2,
    listed: "5 h ago",
    image: images.network,
    tags: ["Reset & tested", "Warranty"],
    vendor: {
      name: "Netline Systems",
      initials: "NS",
      city: "Noida",
      verified: true,
      rating: 4.7,
      deals: 154,
      tint: "from-[#054FA8] to-[#1E86FF]",
    },
  },
];

export const categories: { label: string; icon: IconName; count: string }[] = [
  { label: "Laptops", icon: "laptop", count: "620+ listings" },
  { label: "Servers", icon: "server", count: "180+ listings" },
  { label: "Networking", icon: "network", count: "140+ listings" },
  { label: "Monitors", icon: "monitor", count: "210+ listings" },
  { label: "Desktops & AIO", icon: "cpu", count: "120+ listings" },
  { label: "Accessories", icon: "headphones", count: "90+ listings" },
];

export const brands = ["Dell", "HP", "Lenovo", "Apple", "HPE", "Cisco", "Microsoft", "ASUS", "Acer", "Samsung"];

export const cities = ["Mumbai", "New Delhi", "Bengaluru", "Hyderabad", "Pune", "Chennai", "Ahmedabad", "Noida", "Kolkata", "Jaipur"];

export const networkNodes = [
  { city: "New Delhi", x: 51, y: 22, count: 186 },
  { city: "Jaipur", x: 36, y: 34, count: 74 },
  { city: "Ahmedabad", x: 23, y: 47, count: 96 },
  { city: "Mumbai", x: 27, y: 62, count: 224 },
  { city: "Pune", x: 34, y: 68, count: 142 },
  { city: "Hyderabad", x: 52, y: 69, count: 158 },
  { city: "Bengaluru", x: 48, y: 82, count: 201 },
  { city: "Chennai", x: 62, y: 79, count: 118 },
  { city: "Kolkata", x: 73, y: 47, count: 102 },
  { city: "Guwahati", x: 86, y: 33, count: 28 },
];

export const steps = [
  {
    number: "01",
    title: "Create your business profile",
    short: "Register",
    description:
      "Add your company, categories, service cities and capabilities. It takes under four minutes — no listing fee, no commission.",
    icon: "store" as IconName,
    bullets: ["GST & business details", "Product categories you trade", "Cities you serve"],
    metric: "4 min",
    metricLabel: "Average setup time",
  },
  {
    number: "02",
    title: "Get verified, build trust",
    short: "Get verified",
    description:
      "Our team reviews your business so corporate buyers and fellow dealers know exactly who they are trading with.",
    icon: "shield" as IconName,
    bullets: ["Business document review", "Verified badge on your profile", "Stronger response rates"],
    metric: "1,200+",
    metricLabel: "Verified businesses",
  },
  {
    number: "03",
    title: "Trade, connect and grow",
    short: "Trade",
    description:
      "List live stock, receive qualified enquiries and build long-term supply relationships across India's IT channel.",
    icon: "handshake" as IconName,
    bullets: ["Unlimited enquiries", "Pan-India buyer reach", "Direct vendor-to-vendor deals"],
    metric: "85+",
    metricLabel: "Cities in the network",
  },
];

export const testimonials = [
  {
    quote:
      "We moved from WhatsApp groups to My Laptop Market and closed three repeat corporate deals in our first month. The buyers are actually businesses.",
    name: "Rohit Malhotra",
    role: "Director, TechRenew India",
    city: "Mumbai",
    initials: "RM",
    metric: "₹12.4L",
    metricLabel: "first-month GMV",
  },
  {
    quote:
      "Sourcing 200 ThinkPads used to take a fortnight of calls. I posted one requirement and had verified quotes from four cities the same day.",
    name: "Ananya Rao",
    role: "Procurement Lead, ReCompute Labs",
    city: "Bengaluru",
    initials: "AR",
    metric: "200",
    metricLabel: "units sourced in 48h",
  },
  {
    quote:
      "The verification is what makes it work. Every profile has a real business behind it — that changes how you negotiate.",
    name: "Imran Qureshi",
    role: "Founder, Netline Systems",
    city: "Noida",
    initials: "IQ",
    metric: "4.8★",
    metricLabel: "vendor rating",
  },
];

export const insights = [
  {
    category: "Market watch",
    title: "Refurbished laptop prices in India: the 2026 buyer's index",
    excerpt: "What ₹30,000 actually buys across Dell, HP and Lenovo — and which generations hold their value.",
    image: images.dell,
    read: "8 min read",
    date: "18 Dec 2025",
  },
  {
    category: "Playbook",
    title: "How to inspect a used ThinkPad before a bulk purchase",
    excerpt: "A seven-point checklist dealers use to grade laptops, from battery cycles to hinge tolerance.",
    image: images.thinkpad,
    read: "6 min read",
    date: "11 Dec 2025",
  },
  {
    category: "Sustainability",
    title: "The ITAD opportunity: turning retired fleets into working capital",
    excerpt: "Why corporate asset disposition is becoming the quiet engine of India's refurbished IT trade.",
    image: images.hp,
    read: "5 min read",
    date: "02 Dec 2025",
  },
];

export const faqs = [
  {
    question: "Who can buy and sell on My Laptop Market?",
    answer:
      "The platform is built exclusively for businesses — refurbished IT dealers, distributors, ITAD companies, system integrators, corporate IT sourcing teams and service providers. We do not support consumer-to-consumer sales, which keeps the network focused and the pricing B2B.",
  },
  {
    question: "How does vendor verification work?",
    answer:
      "Every vendor submits their business details, GST information and operating city. Our team reviews the documentation before a verified badge is applied. Buyers can see the verification status, business location and trading history on every profile and listing.",
  },
  {
    question: "Is there a listing fee or commission?",
    answer:
      "Creating a vendor profile is free. There is no commission on the deals you close directly with another business — the platform exists to generate visibility, qualified enquiries and trade relationships.",
  },
  {
    question: "Can I source large or recurring quantities?",
    answer:
      "Yes. Every listing shows available units and minimum order quantities so you can plan bulk purchases. You can also post a requirement and let verified vendor systems and dealers respond with matching stock and pricing.",
  },
  {
    question: "How do I close a deal and arrange shipping?",
    answer:
      "Once a seller accepts your enquiry, you connect directly to confirm inspection, invoicing, warranty terms and logistics. Both parties deal with each other directly, with pan-India logistics partners available through the network.",
  },
];

export const stats = [
  { value: 1200, suffix: "+", label: "Verified IT businesses" },
  { value: 85, suffix: "+", label: "Indian cities covered" },
  { value: 40, suffix: "+", label: "Product categories" },
];

export const listingActivity = [
  "TechRenew India just listed 24 × HP EliteBook 840 G8 in Mumbai",
  "New requirement posted: 200 × business laptops, Hyderabad",
  "ReCompute Labs verified as a Grade A vendor in Bengaluru",
  "ServerSphere just listed 6 × HPE DL380 Gen10 in Hyderabad",
  "iAsset Recovery just listed 8 × MacBook Pro 14 in Pune",
];

export const inr = (value: number) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(value);

export const savings = (price: number, mrp: number) => Math.max(0, Math.round(((mrp - price) / mrp) * 100));
