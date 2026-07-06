import createMiddleware from "next-intl/middleware";
import { routing } from "./i18n/routing";

/**
 * Routing por locale (/es/... y /en/...) con detección del navegador y
 * persistencia en cookie NEXT_LOCALE (RF-63). La protección de /perfil y
 * /admin se suma aquí cuando exista sesión en el frontend (Sprint 4+).
 */
export default createMiddleware(routing);

export const config = {
  // Excluye /api (webhook ISR), estáticos e internos de Next
  matcher: "/((?!api|_next|_vercel|.*\\..*).*)",
};
