import { defineRouting } from "next-intl/routing";

/**
 * Idiomas del MVP: español (default) e inglés (RF-60).
 * Detección automática del navegador con fallback (RF-64 recortado a es/en).
 * Agregar un idioma = un JSON nuevo + una entrada aquí (plan, sección 5.6).
 */
export const routing = defineRouting({
  locales: ["es", "en"],
  defaultLocale: "es",
  localeDetection: true,
});

export type Locale = (typeof routing.locales)[number];
