"use client";

import { useEffect, useRef, useState } from "react";
import { cities, inr, type Product } from "@/lib/landing/data";
import { cn } from "@/lib/landing/cn";
import { Icon, LogoMark, Wordmark } from "./Icon";

export type DialogIntent = "vendor" | "buyer";

function useDialogChrome(open: boolean, onClose: () => void, focusRef?: React.RefObject<HTMLElement | null>) {
  useEffect(() => {
    if (!open) return;
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    const onKey = (event: KeyboardEvent) => {
      if (event.key === "Escape") onClose();
    };
    window.addEventListener("keydown", onKey);
    const timer = window.setTimeout(() => focusRef?.current?.focus(), 80);
    return () => {
      document.body.style.overflow = previous;
      window.removeEventListener("keydown", onKey);
      window.clearTimeout(timer);
    };
  }, [open, onClose, focusRef]);
}

export function RegisterDialog({
  open,
  intent,
  onClose,
  onExplore,
}: {
  open: boolean;
  intent: DialogIntent;
  onClose: () => void;
  onExplore: () => void;
}) {
  const [mode, setMode] = useState<DialogIntent>(intent);
  const [submitted, setSubmitted] = useState(false);
  const [form, setForm] = useState({
    business: "",
    name: "",
    email: "",
    phone: "",
    city: "Mumbai",
    category: "Laptops",
    notes: "",
  });
  const firstFieldRef = useRef<HTMLInputElement>(null);

  useDialogChrome(open, onClose, firstFieldRef);

  useEffect(() => {
    if (open) {
      setMode(intent);
      setSubmitted(false);
    }
  }, [open, intent]);

  if (!open) return null;

  const isVendor = mode === "vendor";

  return (
    <div className="fixed inset-0 z-[120] flex items-start justify-center overflow-y-auto px-3 py-4 sm:items-center sm:px-5 sm:py-8">
      <button className="fixed inset-0 cursor-default bg-navy/60 backdrop-blur-[7px]" onClick={onClose} aria-label="Close dialog" />
      <div
        role="dialog"
        aria-modal="true"
        aria-label={isVendor ? "List your business" : "Post a requirement"}
        className="fade-slide-in relative my-auto w-full max-w-[940px] overflow-hidden rounded-[24px] border border-white/20 bg-white shadow-[0_40px_120px_-24px_rgba(7,20,38,0.6)] sm:rounded-[28px]"
      >
        <button
          onClick={onClose}
          className="absolute right-3.5 top-3.5 z-20 flex h-8 w-8 items-center justify-center rounded-full border border-line bg-white/90 text-navy shadow-sm backdrop-blur transition hover:border-navy/20 hover:bg-mist"
          aria-label="Close"
        >
          <Icon name="x" size={15} />
        </button>

        <div className="grid lg:grid-cols-[0.82fr_1.18fr]">
          <aside className="relative hidden overflow-hidden bg-gradient-to-br from-navy via-[#0c2550] to-brand-deep p-7 text-white lg:flex lg:flex-col">
            <div className="grain pointer-events-none absolute inset-0 opacity-40" />
            <div className="pointer-events-none absolute inset-0 opacity-[0.15] [background-image:radial-gradient(#fff_1px,transparent_1px)] [background-size:20px_20px]" />
            <div className="pointer-events-none absolute -right-16 top-1/4 h-56 w-56 rounded-full bg-brand/40 blur-3xl" />
            <div className="relative flex items-center gap-2.5">
              <LogoMark className="h-9 w-9" />
              <Wordmark light />
            </div>

            <div className="relative mt-10">
              <p className="text-[9.5px] font-bold uppercase tracking-[0.2em] text-brand-bright">
                {isVendor ? "For businesses" : "For sourcing teams"}
              </p>
              <h3 className="mt-2 font-display text-[30px] font-bold leading-[1.06] tracking-[-0.035em]">
                {isVendor ? "Get discovered by the whole channel." : "One brief. The right vendors."}
              </h3>
              <p className="mt-3 text-[12px] leading-relaxed text-white/60">
                {isVendor
                  ? "A verified profile puts your stock in front of dealers, ITADs and corporate buyers across 85+ cities."
                  : "Tell us the model, quantity and city. Verified vendors respond with matched stock and pricing."}
              </p>
            </div>

            <ul className="relative mt-7 space-y-3">
              {(isVendor
                ? ["Free business profile, no listing fee", "Verified badge after review", "Direct enquiries, zero commission"]
                : ["Live stock from 1,200+ verified businesses", "Bulk quantities and MOQ in plain sight", "Replies from real dealers, usually within 4 hours"]
              ).map((item) => (
                <li key={item} className="flex items-start gap-2.5 text-[11px] leading-snug text-white/80">
                  <span className="mt-0.5 flex h-4 w-4 shrink-0 items-center justify-center rounded-full bg-white/15">
                    <Icon name="check" size={10} strokeWidth={3} />
                  </span>
                  {item}
                </li>
              ))}
            </ul>

            <div className="relative mt-auto pt-10">
              <div className="rounded-xl border border-white/15 bg-white/[0.07] p-3.5 backdrop-blur">
                <div className="flex items-center gap-2">
                  <span className="flex h-7 w-7 items-center justify-center rounded-full bg-gradient-to-br from-brand-bright to-brand text-[8px] font-bold">RM</span>
                  <div>
                    <p className="text-[10px] font-bold">Rohit Malhotra</p>
                    <p className="text-[8.5px] text-white/50">TechRenew India · Mumbai</p>
                  </div>
                  <Icon name="badge" size={12} className="ml-auto text-[#7cffa1]" />
                </div>
                <p className="mt-2.5 text-[10px] leading-relaxed text-white/65">
                  “Three repeat corporate deals in our first month. The buyers are actually businesses.”
                </p>
              </div>
            </div>
          </aside>

          <div className="max-h-[calc(100vh-32px)] overflow-y-auto p-5 sm:p-7 lg:max-h-[86vh]">
            {submitted ? (
              <div className="flex min-h-[420px] flex-col items-center justify-center text-center">
                <div className="relative flex h-16 w-16 items-center justify-center rounded-full bg-verified/10">
                  <span className="absolute inset-0 animate-ping rounded-full bg-verified/15" />
                  <span className="flex h-12 w-12 items-center justify-center rounded-full bg-verified text-white">
                    <Icon name="check" size={24} strokeWidth={2.6} />
                  </span>
                </div>
                <p className="mt-5 text-[9.5px] font-bold uppercase tracking-[0.18em] text-brand">
                  {isVendor ? "Profile request received" : "Requirement posted"}
                </p>
                <h3 className="mt-2 font-display text-[27px] font-bold tracking-[-0.03em] text-navy">
                  {isVendor ? "You're in the queue." : "The network is on it."}
                </h3>
                <p className="mt-2.5 max-w-[330px] text-[12px] leading-relaxed text-slate-ink">
                  {isVendor
                    ? "Our team will review your business details and reach out within one working day to verify your profile."
                    : "Verified vendors matching your requirement will reach out directly. Expect the first responses within 4 working hours."}
                </p>
                <div className="mt-5 flex flex-wrap justify-center gap-2">
                  <span className="chip"><Icon name="badge" size={11} className="text-verified" /> {form.business || "Your business"}</span>
                  <span className="chip"><Icon name="pin" size={11} className="text-brand" /> {form.city}</span>
                  <span className="chip"><Icon name="phone" size={11} className="text-brand" /> {form.phone || "Number on file"}</span>
                </div>
                <div className="mt-6 flex w-full max-w-[330px] flex-col gap-2">
                  <button onClick={onExplore} className="btn btn-primary h-11 min-h-0 px-5 text-[12px]">
                    Explore live stock <Icon name="arrow-right" size={14} className="arrow" />
                  </button>
                  <button onClick={onClose} className="btn btn-ghost h-10 min-h-0 text-[11.5px]">Back to the site</button>
                </div>
                <p className="mt-3 flex items-center gap-1.5 text-[9.5px] text-slate-ink">
                  <Icon name="lock" size={11} /> No payment required. Nothing shared publicly without your approval.
                </p>
              </div>
            ) : (
              <>
                <div className="pr-8">
                  <p className="text-[9.5px] font-bold uppercase tracking-[0.18em] text-brand">
                    {isVendor ? "Become a verified vendor" : "Post a sourcing requirement"}
                  </p>
                  <h2 className="mt-1.5 font-display text-[25px] font-bold leading-tight tracking-[-0.03em] text-navy sm:text-[28px]">
                    {isVendor ? "List your business, free." : "Tell us what you need."}
                  </h2>
                  <p className="mt-1.5 text-[11.5px] leading-relaxed text-slate-ink">
                    {isVendor
                      ? "Four minutes now, qualified enquiries later. No listing fees, ever."
                      : "No fees. No obligation. Just matched stock from verified dealers."}
                  </p>
                </div>

                <div className="mt-4 grid grid-cols-2 gap-1 rounded-xl bg-mist p-1">
                  {(["vendor", "buyer"] as DialogIntent[]).map((option) => (
                    <button
                      key={option}
                      onClick={() => setMode(option)}
                      className={cn(
                        "flex h-9 items-center justify-center gap-1.5 rounded-lg text-[11.5px] font-bold transition-all",
                        mode === option ? "bg-white text-navy shadow-sm" : "text-slate-ink hover:text-navy",
                      )}
                    >
                      <Icon name={option === "vendor" ? "store" : "package"} size={13} />
                      {option === "vendor" ? "I want to sell" : "I want to source"}
                    </button>
                  ))}
                </div>

                <form
                  onSubmit={(event) => {
                    event.preventDefault();
                    setSubmitted(true);
                  }}
                  className="mt-4 space-y-3"
                >
                  <div className="grid gap-3 sm:grid-cols-2">
                    <Field label={isVendor ? "Business name" : "Company name"}>
                      <input
                        ref={firstFieldRef}
                        required
                        value={form.business}
                        onChange={(event) => setForm({ ...form, business: event.target.value })}
                        placeholder="e.g. TechRenew India"
                        className="dialog-input"
                      />
                    </Field>
                    <Field label="Your name">
                      <input
                        required
                        value={form.name}
                        onChange={(event) => setForm({ ...form, name: event.target.value })}
                        placeholder="Full name"
                        className="dialog-input"
                      />
                    </Field>
                    <Field label="Work email">
                      <input
                        required
                        type="email"
                        value={form.email}
                        onChange={(event) => setForm({ ...form, email: event.target.value })}
                        placeholder="you@company.com"
                        className="dialog-input"
                      />
                    </Field>
                    <Field label="Phone / WhatsApp">
                      <input
                        required
                        type="tel"
                        minLength={10}
                        value={form.phone}
                        onChange={(event) => setForm({ ...form, phone: event.target.value })}
                        placeholder="+91 98765 43210"
                        className="dialog-input"
                      />
                    </Field>
                    <Field label="City">
                      <div className="relative">
                        <select
                          value={form.city}
                          onChange={(event) => setForm({ ...form, city: event.target.value })}
                          className="dialog-input appearance-none pr-8"
                        >
                          {cities.map((city) => <option key={city}>{city}</option>)}
                        </select>
                        <Icon name="chevron-down" size={13} className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-slate-ink" />
                      </div>
                    </Field>
                    <Field label={isVendor ? "Primary category" : "Sourcing category"}>
                      <div className="relative">
                        <select
                          value={form.category}
                          onChange={(event) => setForm({ ...form, category: event.target.value })}
                          className="dialog-input appearance-none pr-8"
                        >
                          {["Laptops", "Servers", "Networking", "Monitors", "Mixed IT hardware"].map((option) => <option key={option}>{option}</option>)}
                        </select>
                        <Icon name="chevron-down" size={13} className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-slate-ink" />
                      </div>
                    </Field>
                  </div>

                  <Field label={isVendor ? "What do you trade? (optional)" : "Requirement details"}>
                    <textarea
                      required={!isVendor}
                      rows={2}
                      value={form.notes}
                      onChange={(event) => setForm({ ...form, notes: event.target.value })}
                      placeholder={isVendor ? "Briefly describe your stock and capabilities…" : "e.g. 40 × Dell Latitude 5420, Grade A, 16GB RAM, delivered to Bengaluru"}
                      className="dialog-input resize-none py-2.5"
                    />
                  </Field>

                  <button type="submit" className="btn btn-primary h-11 w-full min-h-0 text-[12.5px]">
                    {isVendor ? "Create my free profile" : "Post my requirement"}
                    <Icon name="arrow-right" size={15} className="arrow" />
                  </button>
                  <p className="flex items-center justify-center gap-1.5 text-center text-[9px] text-slate-ink">
                    <Icon name="lock" size={10} /> By submitting, you agree to our terms. Your details stay private.
                  </p>
                  <a
                    href="https://mylaptopmarket.com/api/vendor/register"
                    target="_blank"
                    rel="noreferrer"
                    className="mx-auto flex w-fit items-center gap-1 text-[9.5px] font-semibold text-brand hover:text-brand-deep"
                  >
                    Prefer the full registration form? <Icon name="arrow-up-right" size={10} />
                  </a>
                </form>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <label className="block">
      <span className="mb-1 block text-[9.5px] font-bold uppercase tracking-[0.1em] text-slate-ink">{label}</span>
      {children}
    </label>
  );
}

export function ProductModal({
  product,
  onClose,
  onRequirement,
}: {
  product: Product | null;
  onClose: () => void;
  onRequirement: () => void;
}) {
  const [quantity, setQuantity] = useState(1);
  const [submitted, setSubmitted] = useState(false);
  const [form, setForm] = useState({ name: "", business: "", phone: "" });
  const firstFieldRef = useRef<HTMLInputElement>(null);

  useDialogChrome(Boolean(product), onClose, firstFieldRef);

  useEffect(() => {
    setQuantity(product?.moq ?? 1);
    setSubmitted(false);
    setForm({ name: "", business: "", phone: "" });
  }, [product]);

  if (!product) return null;

  const estimate = product.price * quantity;
  const saving = product.mrp * quantity - estimate;

  return (
    <div className="fixed inset-0 z-[120] flex items-start justify-center overflow-y-auto px-3 py-4 sm:px-5 sm:py-8">
      <button className="fixed inset-0 cursor-default bg-navy/60 backdrop-blur-[7px]" onClick={onClose} aria-label="Close product" />
      <div
        role="dialog"
        aria-modal="true"
        aria-label={`${product.name} quick view`}
        className="fade-slide-in relative my-auto w-full max-w-[1020px] overflow-hidden rounded-[24px] border border-white/20 bg-white shadow-[0_40px_120px_-24px_rgba(7,20,38,0.6)] sm:rounded-[28px]"
      >
        <button
          onClick={onClose}
          className="absolute right-3.5 top-3.5 z-20 flex h-8 w-8 items-center justify-center rounded-full border border-line bg-white/90 text-navy shadow-sm backdrop-blur transition hover:border-navy/20 hover:bg-mist"
          aria-label="Close"
        >
          <Icon name="x" size={15} />
        </button>

        <div className="grid lg:grid-cols-[1.06fr_0.94fr]">
          <div className="max-h-[calc(100vh-32px)] overflow-y-auto p-4 pr-4 sm:p-6 lg:max-h-[86vh]">
            <div className="relative overflow-hidden rounded-2xl border border-line bg-[#f2f6fc]">
              <img src={product.image} alt={product.name} className="h-[230px] w-full object-cover sm:h-[290px]" />
              <span className="absolute left-3 top-3 rounded-full border border-navy/5 bg-white/95 px-2.5 py-1 text-[9.5px] font-bold text-navy shadow-sm backdrop-blur">
                {product.condition} · Refurbished
              </span>
              <span className="absolute right-3 top-3 flex items-center gap-1 rounded-full bg-verified px-2 py-1 text-[9px] font-bold text-white shadow-sm">
                <Icon name="shield" size={10} strokeWidth={2.4} /> Verified stock
              </span>
              <span className="absolute bottom-3 left-3 flex items-center gap-1 rounded-full bg-navy/70 px-2.5 py-1 text-[9.5px] font-medium text-white backdrop-blur">
                <Icon name="pin" size={10} /> {product.city} · {product.units} units available
              </span>
            </div>

            <div className="mt-4 flex items-start justify-between gap-3">
              <div>
                <p className="text-[9.5px] font-bold uppercase tracking-[0.16em] text-brand">{product.brand} · {product.category}</p>
                <h2 className="mt-1 font-display text-[24px] font-bold leading-tight tracking-[-0.03em] text-navy sm:text-[27px]">{product.name}</h2>
                <p className="mt-1 flex items-center gap-2 text-[10px] text-slate-ink">
                  Listed {product.listed} <span className="h-1 w-1 rounded-full bg-line" /> MOQ {product.moq} units
                </p>
              </div>
              <span className="shrink-0 whitespace-nowrap rounded-lg bg-verified/10 px-2 py-1.5 text-center">
                <span className="block text-[14px] font-bold leading-none text-[#0f9a38]">{Math.round(((product.mrp - product.price) / product.mrp) * 100)}%</span>
                <span className="mt-0.5 block text-[8px] font-bold uppercase tracking-wide text-[#0f9a38]/80">below new</span>
              </span>
            </div>

            <div className="mt-4 grid grid-cols-2 gap-2 sm:grid-cols-4">
              {product.specs.map((spec, index) => (
                <div key={spec} className="rounded-lg border border-line bg-mist/50 px-2.5 py-2">
                  <p className="text-[8px] font-bold uppercase tracking-[0.1em] text-slate-ink/60">{["Processor", "Memory", "Storage", "Display"][index] ?? "Spec"}</p>
                  <p className="mt-0.5 text-[10.5px] font-semibold leading-tight text-navy">{spec}</p>
                </div>
              ))}
            </div>

            <div className="mt-4 rounded-xl border border-line p-3.5">
              <p className="text-[8.5px] font-bold uppercase tracking-[0.14em] text-slate-ink/60">Listed by</p>
              <div className="mt-2 flex items-center gap-2.5">
                <span className={`flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br ${product.vendor.tint} text-[10px] font-bold text-white`}>
                  {product.vendor.initials}
                </span>
                <div className="min-w-0 flex-1">
                  <p className="flex items-center gap-1.5 text-[12px] font-bold text-navy">
                    {product.vendor.name} <Icon name="badge" size={12} className="text-verified" />
                  </p>
                  <p className="flex items-center gap-2 text-[9.5px] text-slate-ink">
                    <Icon name="pin" size={9} /> {product.vendor.city}
                    <Icon name="star" size={9} className="fill-brand text-brand" /> {product.vendor.rating} · {product.vendor.deals} deals
                  </p>
                </div>
                <a
                  href="https://mylaptopmarket.com"
                  target="_blank"
                  rel="noreferrer"
                  className="flex items-center gap-1 rounded-full border border-line px-2.5 py-1.5 text-[9.5px] font-bold text-navy transition hover:border-brand/30 hover:text-brand"
                >
                  Profile <Icon name="arrow-up-right" size={10} />
                </a>
              </div>
            </div>

            <div className="mt-3 grid grid-cols-3 gap-2">
              {[
                { icon: "scan" as const, label: "7-point checked" },
                { icon: "refresh" as const, label: "Return window" },
                { icon: "truck" as const, label: "Pan-India ship" },
              ].map((item) => (
                <div key={item.label} className="flex flex-col items-center gap-1 rounded-lg bg-mist px-1 py-2 text-center">
                  <Icon name={item.icon} size={13} className="text-brand" />
                  <span className="text-[8px] font-semibold leading-tight text-slate-ink">{item.label}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="border-t border-line bg-mist/60 p-4 sm:p-6 lg:max-h-[86vh] lg:overflow-y-auto lg:border-l lg:border-t-0">
            {submitted ? (
              <div className="flex min-h-[360px] flex-col items-center justify-center text-center">
                <span className="flex h-14 w-14 items-center justify-center rounded-full bg-verified text-white">
                  <Icon name="check" size={26} strokeWidth={2.6} />
                </span>
                <p className="mt-4 text-[9.5px] font-bold uppercase tracking-[0.18em] text-brand">Quote request sent</p>
                <h3 className="mt-1.5 font-display text-[23px] font-bold tracking-[-0.03em] text-navy">Over to the vendor.</h3>
                <p className="mt-2 max-w-[270px] text-[11.5px] leading-relaxed text-slate-ink">
                  {product.vendor.name} has received your request for {quantity} × {product.name}. They usually respond within a few hours.
                </p>
                <div className="mt-4 w-full rounded-xl border border-line bg-white p-3 text-left">
                  <div className="flex justify-between text-[10.5px] text-slate-ink"><span>Quantity</span><span className="font-bold text-navy">{quantity} units</span></div>
                  <div className="mt-1.5 flex justify-between text-[10.5px] text-slate-ink"><span>Est. trade value</span><span className="font-bold text-navy">{inr(estimate)}</span></div>
                </div>
                <div className="mt-4 flex w-full flex-col gap-2">
                  <a href={`https://wa.me/919718199976?text=${encodeURIComponent(`Hi, I'm interested in ${quantity} × ${product.name} listed on My Laptop Market.`)}`} target="_blank" rel="noreferrer" className="btn btn-primary h-10 min-h-0 text-[11.5px]">
                    <Icon name="whatsapp" size={14} /> Continue on WhatsApp
                  </a>
                  <button onClick={onClose} className="btn btn-ghost h-9 min-h-0 text-[11px]">Keep browsing</button>
                </div>
              </div>
            ) : (
              <>
                <div className="flex items-start justify-between gap-4 pr-9">
                  <div>
                    <p className="text-[9.5px] font-bold uppercase tracking-[0.16em] text-brand">Request a quote</p>
                    <h3 className="mt-1 font-display text-[22px] font-bold tracking-[-0.03em] text-navy">Talk numbers.</h3>
                    <p className="mt-1 text-[11px] leading-relaxed text-slate-ink">Direct to the vendor. No fees, no obligation.</p>
                  </div>
                  <span className="flex items-center gap-1 rounded-full border border-verified/25 bg-verified/10 px-2 py-1 text-[9px] font-bold text-[#0f9a38]">
                    <span className="h-1 w-1 rounded-full bg-verified" /> Online
                  </span>
                </div>

                <div className="mt-4 rounded-xl border border-line bg-white p-3">
                  <div className="flex items-end justify-between">
                    <div>
                      <p className="text-[8.5px] font-bold uppercase tracking-[0.1em] text-slate-ink/60">Trade price / unit</p>
                      <p className="text-[22px] font-bold leading-tight tracking-[-0.03em] text-navy">{inr(product.price)}</p>
                      <p className="text-[9.5px] text-slate-ink"><span className="line-through">{inr(product.mrp)}</span> new · you save {inr(product.mrp - product.price)}</p>
                    </div>
                    <div className="flex items-center gap-1.5 rounded-full border border-line p-1">
                      <button
                        onClick={() => setQuantity((value) => Math.max(product.moq, value - 1))}
                        className="flex h-6 w-6 items-center justify-center rounded-full bg-mist text-navy transition hover:bg-sky disabled:opacity-40"
                        disabled={quantity <= product.moq}
                        aria-label="Decrease quantity"
                      >
                        <Icon name="minus" size={11} />
                      </button>
                      <span className="w-7 text-center text-[12px] font-bold text-navy">{quantity}</span>
                      <button
                        onClick={() => setQuantity((value) => Math.min(product.units, value + 1))}
                        className="flex h-6 w-6 items-center justify-center rounded-full bg-mist text-navy transition hover:bg-sky disabled:opacity-40"
                        disabled={quantity >= product.units}
                        aria-label="Increase quantity"
                      >
                        <Icon name="plus" size={11} />
                      </button>
                    </div>
                  </div>
                  <div className="mt-2.5 flex items-center justify-between border-t border-line/70 pt-2 text-[10px]">
                    <span className="font-medium text-slate-ink">Est. total <span className="text-slate-ink/60">({quantity} units)</span></span>
                    <span className="font-bold text-navy">{inr(estimate)}</span>
                  </div>
                  <p className="mt-1 text-[8.5px] text-[#0f9a38]">You save {inr(saving)} vs new</p>
                </div>

                <form
                  onSubmit={(event) => {
                    event.preventDefault();
                    setSubmitted(true);
                  }}
                  className="mt-3 space-y-2.5"
                >
                  <div className="grid grid-cols-2 gap-2">
                    <Field label="Your name">
                      <input ref={firstFieldRef} required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} placeholder="Full name" className="dialog-input !h-9 text-[11px]" />
                    </Field>
                    <Field label="Business">
                      <input required value={form.business} onChange={(event) => setForm({ ...form, business: event.target.value })} placeholder="Company" className="dialog-input !h-9 text-[11px]" />
                    </Field>
                  </div>
                  <Field label="Phone / WhatsApp">
                    <input required type="tel" minLength={10} value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} placeholder="+91 98765 43210" className="dialog-input !h-9 text-[11px]" />
                  </Field>
                  <label className="flex items-start gap-2 rounded-lg bg-white/70 p-2.5 text-[9px] leading-snug text-slate-ink">
                    <input type="checkbox" required className="mt-0.5 h-3 w-3 accent-[#0767D8]" />
                    I confirm I&apos;m buying on behalf of a registered business.
                  </label>
                  <button type="submit" className="btn btn-primary h-10 w-full min-h-0 text-[12px]">
                    Send quote request <Icon name="send" size={13} />
                  </button>
                </form>

                <div className="mt-3 flex gap-2">
                  <a href="tel:+919718199976" className="btn btn-ghost h-9 min-h-0 flex-1 px-2 text-[10.5px]">
                    <Icon name="phone" size={12} /> Call vendor
                  </a>
                  <button onClick={() => { onClose(); onRequirement(); }} className="btn btn-ghost h-9 min-h-0 flex-1 px-2 text-[10.5px]">
                    <Icon name="package" size={12} /> Similar need
                  </button>
                </div>
                <p className="mt-3 flex items-center justify-center gap-1.5 text-[9px] text-slate-ink">
                  <Icon name="shield" size={10} className="text-verified" /> Vendor verified by My Laptop Market
                </p>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
