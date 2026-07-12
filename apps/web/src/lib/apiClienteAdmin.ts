"use client";

import { conAutenticacion } from "./apiCliente";

/**
 * Cliente del panel de administración: todas las llamadas van con el Bearer
 * del admin por el proxy same-origin; el backend exige rol ADMIN (RNF-16),
 * así que la seguridad real está en el servidor, no en el guard del cliente.
 */

export interface Metricas {
  lugaresPublicados: number;
  usuariosRegistrados: number;
  resenasPublicadas: number;
  fotosPendientes: number;
  resenasEnRevision: number;
  reportesRecibidos: number;
  reportesAprobados: number;
  negociosPendientes: number;
}

export interface FotoModeracion {
  id: string;
  url: string;
  estado: string;
  motivoRechazo: string | null;
  autorNombre: string;
  creadaEn: string;
}

export interface ResenaModeracion {
  id: string;
  calificacion: number;
  comentario: string | null;
  autorNombre: string;
  creadaEn: string;
}

export interface ReporteModeracion {
  id: string;
  tipoIncidenteCodigo: string;
  descripcion: string;
  latitud: number;
  longitud: number;
  direccionReferencial: string | null;
  estado: string;
  esAnonimo: boolean;
  fotos: string[];
  creadoEn: string;
}

interface Pagina<T> {
  content: T[];
  totalElements: number;
}

async function jsonAutenticado<T>(ruta: string, init?: RequestInit): Promise<T> {
  const res = await conAutenticacion(ruta, init ?? { method: "GET" });
  if (!res.ok) {
    throw new Error(`HTTP ${res.status}`);
  }
  return res.json();
}

export function obtenerMetricas(): Promise<Metricas> {
  return jsonAutenticado<Metricas>("/admin/metricas");
}

export async function listarFotosPendientes(): Promise<FotoModeracion[]> {
  const p = await jsonAutenticado<Pagina<FotoModeracion>>(
    "/admin/moderacion/fotos?estado=PENDIENTE&tamano=50",
  );
  return p.content;
}

export function moderarFoto(id: string, aprobar: boolean, motivo?: string): Promise<void> {
  return conAutenticacion(`/admin/moderacion/fotos/${id}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      estado: aprobar ? "APROBADA" : "RECHAZADA",
      motivoRechazo: aprobar ? null : (motivo ?? "No cumple los criterios"),
    }),
  }).then(() => undefined);
}

export async function listarResenasEnRevision(): Promise<ResenaModeracion[]> {
  const p = await jsonAutenticado<Pagina<ResenaModeracion>>(
    "/admin/moderacion/resenas?estado=EN_REVISION&tamano=50",
  );
  return p.content;
}

export function moderarResena(id: string, ocultar: boolean): Promise<void> {
  return conAutenticacion(`/admin/moderacion/resenas/${id}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ estado: ocultar ? "OCULTA" : "PUBLICADA" }),
  }).then(() => undefined);
}

export async function listarReportes(estado: string): Promise<ReporteModeracion[]> {
  const p = await jsonAutenticado<Pagina<ReporteModeracion>>(
    `/admin/moderacion/reportes?estado=${estado}&tamano=50`,
  );
  return p.content;
}

export function moderarReporte(id: string, estado: string, notas?: string): Promise<void> {
  return conAutenticacion(`/admin/moderacion/reportes/${id}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ estado, notasAdmin: notas ?? null }),
  }).then(() => undefined);
}

// ---- Moderación de negocios (RF-104) ----

export interface NegocioModeracion {
  id: string;
  nombre: string;
  categoriaCodigo: string;
  descripcion: string | null;
  whatsapp: string;
  direccion: string | null;
  estado: string;
}

export async function listarNegociosPendientes(): Promise<NegocioModeracion[]> {
  const p = await jsonAutenticado<Pagina<NegocioModeracion>>(
    "/admin/moderacion/negocios?estado=PENDIENTE&tamano=50",
  );
  return p.content;
}

export function moderarNegocio(id: string, estado: string): Promise<void> {
  return conAutenticacion(`/admin/moderacion/negocios/${id}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ estado }),
  }).then(() => undefined);
}

// ---- Gestión de eventos (RF-86) ----

// Distrito Ayacucho (seed V5): destino por defecto del MVP (solo Huamanga)
export const DISTRITO_AYACUCHO = "01980000-0000-7000-8000-000000000301";

export interface NuevoEvento {
  tipo: string;
  fechaInicio: string;
  fechaFin: string;
  recurrenteAnual: boolean;
  estado: string;
  nombreEs: string;
  descripcionEs: string;
  nombreEn: string;
  descripcionEn: string;
  organizador: string;
}

export async function crearEvento(e: NuevoEvento): Promise<void> {
  const traducciones = [
    { idioma: "es", nombre: e.nombreEs, descripcion: e.descripcionEs || null, organizador: e.organizador || null },
  ];
  if (e.nombreEn.trim()) {
    traducciones.push({
      idioma: "en",
      nombre: e.nombreEn,
      descripcion: e.descripcionEn || null,
      organizador: e.organizador || null,
    });
  }
  const res = await conAutenticacion("/eventos", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      tipo: e.tipo,
      distritoId: DISTRITO_AYACUCHO,
      lugarId: null,
      fechaInicio: e.fechaInicio,
      fechaFin: e.fechaFin,
      recurrenteAnual: e.recurrenteAnual,
      estado: e.estado,
      traducciones,
    }),
  });
  if (!res.ok) {
    const cuerpo = await res.json().catch(() => null);
    throw new Error(cuerpo?.mensaje ?? `HTTP ${res.status}`);
  }
}

export function clonarEvento(id: string, anios: number): Promise<void> {
  return conAutenticacion(`/eventos/${id}/clonar?anios=${anios}`, { method: "POST" }).then(
    () => undefined,
  );
}

export function eliminarEvento(id: string): Promise<void> {
  return conAutenticacion(`/eventos/${id}`, { method: "DELETE" }).then(() => undefined);
}
