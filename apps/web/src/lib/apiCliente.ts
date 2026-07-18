"use client";

import { useSesion, type UsuarioSesion } from "@/stores/sesion";

/**
 * Cliente del API para el navegador. Siempre rutas relativas: el proxy de
 * next.config las reenvía al backend manteniendo same-origin (la cookie
 * httpOnly del refresh viaja sin CORS ni SameSite=None).
 */

const API = "/api/v1";

/**
 * Marca local de "hubo sesión": evita llamar a /auth/refresh en visitantes
 * anónimos (el 401 esperado ensuciaba la consola y Lighthouse). La cookie
 * httpOnly sigue siendo la única credencial real.
 */
const MARCA_SESION = "th_hubo_sesion";

function marcarSesion(activa: boolean) {
  try {
    if (activa) {
      localStorage.setItem(MARCA_SESION, "1");
    } else {
      localStorage.removeItem(MARCA_SESION);
    }
  } catch {
    // almacenamiento bloqueado: solo se pierde la optimización
  }
}

export function huboSesion(): boolean {
  try {
    return localStorage.getItem(MARCA_SESION) === "1";
  } catch {
    return false;
  }
}

interface RespuestaTokens {
  accessToken: string;
  usuario: UsuarioSesion;
}

export interface ErrorApi {
  error: string;
  mensaje: string;
}

/** Lanza el cuerpo de error del API para mostrar su mensaje en la UI. */
async function procesar<T>(res: Response): Promise<T> {
  if (res.ok) {
    return res.status === 204 ? (undefined as T) : res.json();
  }
  let cuerpo: ErrorApi;
  try {
    cuerpo = await res.json();
  } catch {
    cuerpo = { error: "ERROR_RED", mensaje: "El servidor no respondió. Intenta de nuevo." };
  }
  throw cuerpo;
}

export async function iniciarSesion(email: string, password: string): Promise<RespuestaTokens> {
  const res = await fetch(`${API}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include", // recibe la cookie httpOnly del refresh
    body: JSON.stringify({ email, password }),
  });
  const datos = await procesar<RespuestaTokens>(res);
  marcarSesion(true);
  return datos;
}

export async function registrarse(
  email: string,
  password: string,
  nombre: string,
): Promise<RespuestaTokens> {
  const res = await fetch(`${API}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify({ email, password, nombre }),
  });
  const datos = await procesar<RespuestaTokens>(res);
  marcarSesion(true);
  return datos;
}

/** Recupera la sesión con la cookie httpOnly; null si no hay sesión válida. */
export async function refrescarSesion(): Promise<RespuestaTokens | null> {
  try {
    const res = await fetch(`${API}/auth/refresh`, {
      method: "POST",
      credentials: "include",
    });
    if (!res.ok) {
      marcarSesion(false);
      return null;
    }
    const datos: RespuestaTokens = await res.json();
    marcarSesion(true);
    return datos;
  } catch {
    return null;
  }
}

export async function cerrarSesion(): Promise<void> {
  marcarSesion(false);
  try {
    await fetch(`${API}/auth/logout`, { method: "POST", credentials: "include" });
  } catch {
    // la sesión local se limpia igual
  }
}

/**
 * Petición autenticada con reintento transparente: si el access token
 * expiró (401), rota el refresh y reintenta una vez (plan, sección 5.3).
 */
export async function conAutenticacion(ruta: string, init: RequestInit): Promise<Response> {
  const { accessToken, establecerSesion, limpiarSesion } = useSesion.getState();
  const ejecutar = (token: string | null) =>
    fetch(`${API}${ruta}`, {
      ...init,
      headers: {
        ...init.headers,
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    });

  let res = await ejecutar(accessToken);
  if (res.status === 401) {
    const renovada = await refrescarSesion();
    if (renovada) {
      establecerSesion(renovada.usuario, renovada.accessToken);
      res = await ejecutar(renovada.accessToken);
    } else {
      limpiarSesion();
    }
  }
  return res;
}

// ---- Favoritos (RF-35) ----

export async function obtenerFavoritosSlugs(): Promise<string[]> {
  const res = await conAutenticacion("/favoritos/slugs", { method: "GET" });
  return res.ok ? res.json() : [];
}

export async function obtenerFavoritosCards(idioma: string): Promise<unknown[]> {
  const res = await conAutenticacion(`/favoritos?idioma=${idioma}`, { method: "GET" });
  return res.ok ? res.json() : [];
}

export async function alternarFavorito(slug: string, marcar: boolean): Promise<void> {
  const res = await conAutenticacion(`/lugares/${encodeURIComponent(slug)}/favorito`, {
    method: marcar ? "PUT" : "DELETE",
  });
  await procesar(res);
}

// ---- Fotos de lugares (RF-38) ----

export async function subirFoto(slug: string, archivo: File): Promise<void> {
  const form = new FormData();
  form.append("archivo", archivo);
  // sin Content-Type manual: el navegador arma el boundary del multipart
  const res = await conAutenticacion(`/lugares/${encodeURIComponent(slug)}/fotos`, {
    method: "POST",
    body: form,
  });
  await procesar(res);
}

// ---- Reporte de contenido (RF-45) ----

export async function reportarContenido(
  objetivo: { resenaId?: string; fotoId?: string },
  motivo: string,
): Promise<void> {
  const res = await conAutenticacion("/reportes-contenido", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ ...objetivo, motivo }),
  });
  await procesar(res);
}

export interface DatosNegocio {
  categoriaNegocioId: string;
  nombre: string;
  whatsapp: string;
  telefono: string;
  direccion: string;
  horario: string;
  descripcionEs: string;
  descripcionEn: string;
}

/** Respuesta de /negocios/mio (contrato NegocioResponse del backend). */
export interface MiNegocio {
  id: string;
  nombre: string;
  categoriaCodigo: string;
  descripcion: string | null;
  whatsapp: string;
  telefono: string | null;
  direccion: string | null;
  horario: string | null;
  estado: string;
}

/** null si el usuario aún no registró negocio. */
export async function obtenerMiNegocio(): Promise<MiNegocio | null> {
  const res = await conAutenticacion("/negocios/mio", { method: "GET" });
  if (res.status === 404) return null;
  return procesar(res);
}

export async function guardarNegocio(datos: DatosNegocio, esNuevo: boolean): Promise<void> {
  const res = await conAutenticacion("/negocios" + (esNuevo ? "" : "/mio"), {
    method: esNuevo ? "POST" : "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      ...datos,
      ruc: null,
      telefono: datos.telefono || null,
      direccion: datos.direccion || null,
      horario: datos.horario || null,
      descripcionEn: datos.descripcionEn || null,
    }),
  });
  await procesar(res);
}

export async function crearResena(
  slug: string,
  calificacion: number,
  comentario: string,
): Promise<void> {
  const res = await conAutenticacion(`/lugares/${encodeURIComponent(slug)}/resenas`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ calificacion, comentario: comentario.trim() || null }),
  });
  await procesar(res);
}
