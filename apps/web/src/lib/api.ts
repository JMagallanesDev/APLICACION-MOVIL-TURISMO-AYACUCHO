/**
 * Cliente HTTP del API (server-side). Listado con ISR de 5 min y detalle de
 * 1 h + revalidación bajo demanda vía webhook (plan, sección 5.5); las
 * búsquedas no se cachean (cada q es distinta).
 */

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1";

/**
 * Fetch con reintento ante fallos transitorios (red caída o 5xx/404 de
 * plataforma durante un redeploy de Railway). Un reintento con espera corta
 * cubre la ventana típica de swap de contenedores.
 */
async function fetchConReintento(url: string, init: RequestInit): Promise<Response> {
  const esperasMs = [0, 800, 2000];
  let ultimoError: unknown;
  for (let intento = 0; intento < esperasMs.length; intento++) {
    if (esperasMs[intento] > 0) {
      await new Promise((r) => setTimeout(r, esperasMs[intento]));
    }
    try {
      const res = await fetch(url, init);
      // 404 del API real trae JSON ({error:...}); un 404 HTML es la
      // plataforma sin app detrás (deploy en curso): se reintenta.
      const es404DePlataforma =
        res.status === 404 && !res.headers.get("content-type")?.includes("json");
      if (res.ok || (!es404DePlataforma && res.status < 500)) {
        return res;
      }
      ultimoError = new Error(`HTTP ${res.status}`);
    } catch (e) {
      ultimoError = e;
    }
  }
  throw ultimoError instanceof Error ? ultimoError : new Error("API no disponible");
}

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
  calificacionPromedio: number | null;
  totalResenas: number | null;
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
  calificacionPromedio: number | null;
  totalResenas: number | null;
  traducciones: TraduccionLugar[];
  horarios: Horario[];
}

export interface Resena {
  id: string;
  calificacion: number;
  comentario: string | null;
  autorNombre: string;
  creadaEn: string;
}

export interface FotoLugar {
  id: string;
  url: string;
  autorNombre: string;
}

export async function obtenerFotos(slug: string): Promise<FotoLugar[]> {
  const res = await fetchConReintento(
    `${API_URL}/lugares/${encodeURIComponent(slug)}/fotos?tamano=24`,
    { next: { revalidate: 120 } },
  );
  if (!res.ok) return [];
  const pagina = await res.json();
  return pagina.content ?? [];
}

export interface TipoIncidente {
  id: string;
  codigo: string;
  nombre: string;
  icono: string | null;
  colorHex: string | null;
}

export interface IncidenteMapa {
  id: string;
  tipoIncidenteCodigo: string;
  icono: string | null;
  colorHex: string | null;
  latitud: number;
  longitud: number;
  estado: string;
}

export interface EventoResumen {
  id: string;
  tipo: string;
  nombre: string;
  fechaInicio: string;
  fechaFin: string;
  portadaUrl: string | null;
}

export interface TraduccionEvento {
  idioma: string;
  nombre: string;
  descripcion: string | null;
  organizador: string | null;
}

export interface EventoDetalle {
  id: string;
  tipo: string;
  distritoId: string;
  lugarId: string | null;
  lugarSlug: string | null;
  fechaInicio: string;
  fechaFin: string;
  recurrenteAnual: boolean;
  portadaUrl: string | null;
  traducciones: TraduccionEvento[];
}

export const TIPOS_EVENTO = [
  "FESTIVIDAD_RELIGIOSA",
  "CONCIERTO",
  "FERIA",
  "GASTRONOMICO",
  "CULTURAL",
  "DEPORTIVO",
  "OTRO",
] as const;

// ---- Rutas temáticas (RF-53, seed) ----

export interface ParadaRuta {
  slug: string;
  nombre: string;
  orden: number;
}

export interface Ruta {
  id: string;
  slug: string;
  colorHex: string | null;
  icono: string | null;
  nombre: string;
  descripcion: string | null;
  totalLugares: number;
  duracionEstimadaMin: number | null;
  lugares: ParadaRuta[];
}

export async function obtenerRutas(idioma: string): Promise<Ruta[]> {
  try {
    const res = await fetchConReintento(`${API_URL}/rutas?idioma=${idioma}`, {
      next: { revalidate: 300 },
    });
    return res.ok ? res.json() : [];
  } catch {
    return [];
  }
}

export async function obtenerProximosEventos(idioma: string, limite = 6): Promise<EventoResumen[]> {
  try {
    const res = await fetchConReintento(
      `${API_URL}/eventos/proximos?idioma=${idioma}&limite=${limite}`,
      { next: { revalidate: 300 } },
    );
    return res.ok ? res.json() : [];
  } catch {
    return []; // sin API (p. ej. en build) el home sale sin la sección de eventos
  }
}

export async function obtenerEventosEnRango(
  idioma: string,
  desde: string,
  hasta: string,
): Promise<EventoResumen[]> {
  const res = await fetchConReintento(
    `${API_URL}/eventos?idioma=${idioma}&desde=${desde}&hasta=${hasta}`,
    { cache: "no-store" },
  );
  return res.ok ? res.json() : [];
}

export async function obtenerEvento(id: string): Promise<EventoDetalle | null> {
  const res = await fetchConReintento(`${API_URL}/eventos/${encodeURIComponent(id)}`, {
    next: { revalidate: 300 },
  });
  if (res.status === 404) return null;
  return res.ok ? res.json() : null;
}

export interface CategoriaNegocio {
  id: string;
  codigo: string;
  nombre: string;
  icono: string | null;
}

export interface Negocio {
  id: string;
  nombre: string;
  categoriaCodigo: string;
  descripcion: string | null;
  whatsapp: string;
  telefono: string | null;
  direccion: string | null;
  horario: string | null;
  estado: string | null;
}

export async function obtenerCategoriasNegocio(idioma: string): Promise<CategoriaNegocio[]> {
  const res = await fetchConReintento(`${API_URL}/categorias-negocio?idioma=${idioma}`, {
    next: { revalidate: 3600 },
  });
  return res.ok ? res.json() : [];
}

export async function obtenerNegocios(
  idioma: string,
  categoriaId?: string,
): Promise<Negocio[]> {
  const params = new URLSearchParams({ idioma, tamano: "50" });
  if (categoriaId) params.set("categoriaId", categoriaId);
  const res = await fetchConReintento(`${API_URL}/negocios?${params}`, {
    next: { revalidate: 300 },
  });
  if (!res.ok) return [];
  const pagina = await res.json();
  return pagina.content ?? [];
}

export async function obtenerTiposIncidente(idioma: string): Promise<TipoIncidente[]> {
  const res = await fetchConReintento(`${API_URL}/tipos-incidente?idioma=${idioma}`, {
    next: { revalidate: 3600 },
  });
  return res.ok ? res.json() : [];
}

export async function obtenerIncidentesMapa(): Promise<IncidenteMapa[]> {
  const res = await fetchConReintento(`${API_URL}/reportes/mapa`, {
    next: { revalidate: 120 },
  });
  return res.ok ? res.json() : [];
}

export async function obtenerResenas(slug: string): Promise<Resena[]> {
  const res = await fetchConReintento(
    `${API_URL}/lugares/${encodeURIComponent(slug)}/resenas?tamano=10`,
    { next: { revalidate: 60 } },
  );
  if (!res.ok) {
    return [];
  }
  const pagina = await res.json();
  return pagina.content ?? [];
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
  orden?: string;
  pagina?: number;
  tamano?: number;
}): Promise<PaginaLugares> {
  const params = new URLSearchParams({
    idioma: opciones.idioma,
    tamano: String(opciones.tamano ?? 12),
  });
  if (opciones.categoria) params.set("categoria", opciones.categoria);
  if (opciones.q) params.set("q", opciones.q);
  if (opciones.orden) params.set("orden", opciones.orden);
  if (opciones.pagina) params.set("pagina", String(opciones.pagina));

  const res = await fetchConReintento(`${API_URL}/lugares?${params}`, {
    ...(opciones.q ? { cache: "no-store" } : { next: { revalidate: 300 } }),
  });
  if (!res.ok) {
    throw new Error(`Error al listar lugares: ${res.status}`);
  }
  return res.json();
}

export async function obtenerLugar(slug: string): Promise<LugarDetalle | null> {
  const res = await fetchConReintento(`${API_URL}/lugares/${encodeURIComponent(slug)}`, {
    next: { revalidate: 3600 },
  });
  // Solo el 404 JSON del API significa "lugar no existe"; cualquier otro
  // fallo lanza (y el error boundary evita cachear un falso not-found)
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
