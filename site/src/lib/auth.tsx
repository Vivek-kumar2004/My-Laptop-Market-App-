import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { SITE_ORIGIN, VENDOR_LOGIN_URL } from "./api";

type VendorAuth = {
  authenticated: boolean;
  loading: boolean;
  loginUrl: string;
};

const AuthContext = createContext<VendorAuth>({
  authenticated: false,
  loading: true,
  loginUrl: VENDOR_LOGIN_URL,
});

export function VendorAuthProvider({ children }: { children: ReactNode }) {
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const res = await fetch(`${SITE_ORIGIN}/api/api/web/auth-status`, {
          credentials: "include",
        });
        if (!res.ok) throw new Error(`auth ${res.status}`);
        const data = (await res.json()) as { authenticated?: boolean };
        if (!cancelled) setAuthenticated(Boolean(data.authenticated));
      } catch {
        if (!cancelled) setAuthenticated(false);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const value = useMemo(
    () => ({ authenticated, loading, loginUrl: VENDOR_LOGIN_URL }),
    [authenticated, loading],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useVendorAuth() {
  return useContext(AuthContext);
}
