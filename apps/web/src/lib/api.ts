/**
 * Cliente HTTP del API (server-side). Listado con ISR de 5 min y detalle de
 * 1 h + revalidación bajo demanda vía webhook (plan, sección 5.5); las
 * búsquedas no se cachean (cada q es distinta).
 */

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1";

export interface LugarResumen {
  id: string;
  slug: string;
  nombre: string;
  descripcion: string | null;
  categoriaCodigo: string;
  latitud: number;
  longitud: number;
  precioEntradaPen: number | null;
  duracionVisitaMin: number | null;
  abiertoAhora: boolean | null;
}

export interface PaginaLugares {
  content: LugarResumen[];
  totalPages: number;
  totalElements: number;
  number: number;
}

export interface TraduccionLugar {
  idioma: string;
  nombre: string;
  descripcion: string | null;
  historia: string | null;
  consejos: string | null;
}

export interface Horario {
  diaSemana: number;
  horaApertura: string | null;
  horaCierre: string | null;
  cerrado: boolean;
}

export interface LugarDetalle {
  id: string;
  slug: string;
  categoriaCodigo: string;
  latitud: number;
  longitud: number;
  direccion: string | null;
  telefono: string | null;
  precioEntradaPen: number | null;
  duracionVisitaMin: number | null;
  aceptaTarjeta: boolean | null;
  tieneBanos: boolean | null;
  accesibleSillaRuedas: boolean | null;
  aptoNinos: boolean | null;
  costoTaxiDesdePlazaPen: number | null;
  requiereGuia: boolean | null;
  abiertoAhora: boolean | null;
  traducciones: TraduccionLugar[];
  horarios: Horario[];
}

/** Códigos del catálogo seed (V5); las etiquetas viven en los JSON de i18n. */
export const CATEGORIAS = [
  "IGLESIAS",
  "MIRADORES",
  "MUSEOS",
  "PLAZAS",
  "SITIOS_ARQUEOLOGICOS",
  "CASONAS",
  "TALLERES_ARTESANALES",
  "NATURALEZA",
] as const;

export async function obtenerLugares(opciones: {
  idioma: string;
  categoria?: string;
  q?: string;
  pagina?: number;
}): Promise<PaginaLugares> {
  const params = new URLSearchParams({ idioma: opciones.idioma, tamano: "12" });
  if (opciones.categoria) params.set("categoria", opciones.categoria);
  if (opciones.q) params.set("q", opciones.q);
  if (opciones.pagina) params.set("pagina", String(opciones.pagina));

  const res = await fetch(`${API_URL}/lugares?${params}`, {
    ...(opciones.q ? { cache: "no-store" } : { next: { revalidate: 300 } }),
  });
  if (!res.ok) {
    throw new Error(`Error al listar lugares: ${res.status}`);
  }
  return res.json();
}

export async function obtenerLugar(slug: string): Promise<LugarDetalle | null> {
  const res = await fetch(`${API_URL}/lugares/${encodeURIComponent(slug)}`, {
    next: { revalidate: 3600 },
  });
  if (res.status === 404) {
    return null;
  }
  if (!res.ok) {
    throw new Error(`Error al obtener lugar: ${res.status}`);
  }
  return res.json();
}

/** Traducción del detalle en el idioma pedido, con fallback a español. */
export function traduccionDe(lugar: LugarDetalle, idioma: string): TraduccionLugar {
  return (
    lugar.traducciones.find((t) => t.idioma === idioma) ??
    lugar.traducciones.find((t) => t.idioma === "es") ??
    lugar.traducciones[0]
  );
}
