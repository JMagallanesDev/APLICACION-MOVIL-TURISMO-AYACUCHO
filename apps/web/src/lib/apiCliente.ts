"use client";

import { useSesion, type UsuarioSesion } from "@/stores/sesion";

/**
 * Cliente del API para el navegador. Siempre rutas relativas: el proxy de
 * next.config las reenvía al backend manteniendo same-origin (la cookie
 * httpOnly del refresh viaja sin CORS ni SameSite=None).
 */

const API = "/api/v1";

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
  return procesar(res);
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
  return procesar(res);
}

/** Recupera la sesión con la cookie httpOnly; null si no hay sesión válida. */
export async function refrescarSesion(): Promise<RespuestaTokens | null> {
  try {
    const res = await fetch(`${API}/auth/refresh`, {
      method: "POST",
      credentials: "include",
    });
    if (!res.ok) {
      return null;
    }
    return await res.json();
  } catch {
    return null;
  }
}

export async function cerrarSesion(): Promise<void> {
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
