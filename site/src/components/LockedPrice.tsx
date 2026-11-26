import { inr } from "../data";
import { useVendorAuth } from "../lib/auth";
import { cn } from "../utils/cn";
import { Icon } from "./Icon";

export function LockedPrice({
  price,
  className,
  amountClassName,
  label = "Trade price",
  showLabel = true,
}: {
  price: number;
  className?: string;
  amountClassName?: string;
  label?: string;
  showLabel?: boolean;
}) {
  const { authenticated, loading, loginUrl } = useVendorAuth();

  if (authenticated) {
    return (
      <div className={className}>
        {showLabel ? (
          <p className="text-[8px] font-semibold uppercase tracking-[0.1em] text-slate-ink/70">{label}</p>
        ) : null}
        <p className={cn("text-[15px] font-bold leading-tight tracking-[-0.03em] text-navy", amountClassName)}>
          {price > 0 ? inr(price) : "Ask"}
        </p>
      </div>
    );
  }

  return (
    <div className={className}>
      {showLabel ? (
        <p className="text-[8px] font-semibold uppercase tracking-[0.1em] text-slate-ink/70">{label}</p>
      ) : null}
      {loading ? (
        <p className={cn("text-[13px] font-bold text-slate-ink/50", amountClassName)}>· · ·</p>
      ) : (
        <a
          href={loginUrl}
          className={cn(
            "inline-flex items-center gap-1 rounded-full border border-line bg-mist/80 px-2 py-1 text-[10px] font-bold text-navy transition hover:border-brand/30 hover:text-brand",
            amountClassName,
          )}
        >
          <Icon name="lock" size={11} className="text-brand" />
          Login to view price
        </a>
      )}
    </div>
  );
}
