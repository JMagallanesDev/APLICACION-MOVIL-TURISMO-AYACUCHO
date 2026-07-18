import type { MetadataRoute } from "next";
import { obtenerLugares, obtenerProximosEventos } from "@/lib/api";
import { SITIO_URL } from "@/lib/sitio";

/**
 * Sitemap dinámico (SEO): páginas fijas + cada lugar publicado + próximos
 * eventos, en ambos idiomas con alternates hreflang. Se regenera cada hora.
 */
export const revalidate = 3600;

const LOCALES = ["es", "en"] as const;

function entrada(
  ruta: string,
  opciones?: Partial<Pick<MetadataRoute.Sitemap[number], "priority" | "changeFrequency" | "lastModified">>,
): MetadataRoute.Sitemap {
  return LOCALES.map((locale) => ({
    url: `${SITIO_URL}/${locale}${ruta}`,
    changeFrequency: opciones?.changeFrequency ?? "weekly",
    priority: opciones?.priority ?? 0.6,
    ...(opciones?.lastModified && { lastModified: opciones.lastModified }),
    alternates: {
      languages: Object.fromEntries(LOCALES.map((l) => [l, `${SITIO_URL}/${l}${ruta}`])),
    },
  }));
}

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const entradas: MetadataRoute.Sitemap = [
    ...entrada("", { priority: 1, changeFrequency: "daily" }),
    ...entrada("/lugares", { priority: 0.9, changeFrequency: "daily" }),
    ...entrada("/mapa", { priority: 0.8 }),
    ...entrada("/rutas", { priority: 0.7 }),
    ...entrada("/eventos", { priority: 0.8, changeFrequency: "daily" }),
    ...entrada("/negocios", { priority: 0.7 }),
    ...entrada("/mapa-incidentes", { priority: 0.5 }),
    ...entrada("/reportar", { priority: 0.5 }),
  ];

  // Lugares publicados (paginado: el API limita a 50 por página)
  try {
    let pagina = 0;
    let totalPaginas = 1;
    while (pagina < totalPaginas && pagina < 10) {
      const datos = await obtenerLugares({ idioma: "es", pagina, tamano: 50 });
      totalPaginas = datos.totalPages;
      for (const lugar of datos.content) {
        entradas.push(...entrada(`/lugares/${lugar.slug}`, { priority: 0.8 }));
      }
      pagina += 1;
    }
  } catch {
    // sin API disponible el sitemap sale solo con las páginas fijas
  }

  try {
    const eventos = await obtenerProximosEventos("es", 50);
    for (const evento of eventos) {
      entradas.push(...entrada(`/eventos/${evento.id}`, { priority: 0.7 }));
    }
  } catch {
    // ídem
  }

  return entradas;
}
