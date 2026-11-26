import { storeLinks } from "../assets";

/**
 * Toggle individual floating dock buttons without touching the component.
 * Set any flag to `false` to hide that button; others keep working.
 */
export const floatingDockConfig = {
  showWhatsApp: true,
  showGooglePlay: true,
  showAppStore: true,
  whatsappUrl: "https://wa.me/919718199976",
  googlePlayUrl: storeLinks.googlePlay,
  appStoreUrl: storeLinks.appStore,
} as const;
