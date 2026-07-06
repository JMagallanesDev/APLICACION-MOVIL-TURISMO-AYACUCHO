import { getRequestConfig } from "next-intl/server";
import { hasLocale } from "next-intl";
import { routing } from "./routing";

/**
 * Carga de mensajes por locale en el servidor (SSR con i18n, RF-59/RNF-05).
 * Los textos de interfaz viven en el frontend porque son parte del software,
 * no del contenido de dominio (plan, sección 5.5).
 */
export default getRequestConfig(async ({ requestLocale }) => {
  const requested = await requestLocale;
  const locale = hasLocale(routing.locales, requested)
    ? requested
    : routing.defaultLocale;

  return {
    locale,
    messages: (await import(`../locales/${locale}.json`)).default,
  };
});
