import { create } from "zustand";

/**
 * Sesión del cliente (plan, sección 5.3): el access token vive SOLO en esta
 * memoria — nunca en localStorage ni en cookies legibles (mitiga XSS). Al
 * recargar, ProveedorSesion lo recupera silenciosamente con el refresh token
 * httpOnly. Zustand para estado global de UI, como dicta la sección 5.6.
 */

export interface UsuarioSesion {
  id: string;
  email: string;
  nombre: string;
  rol: string;
}

interface EstadoSesion {
  usuario: UsuarioSesion | null;
  accessToken: string | null;
  /** true mientras se intenta restaurar la sesión al cargar la app */
  restaurando: boolean;
  establecerSesion: (usuario: UsuarioSesion, accessToken: string) => void;
  limpiarSesion: () => void;
  terminarRestauracion: () => void;
}

export const useSesion = create<EstadoSesion>((set) => ({
  usuario: null,
  accessToken: null,
  restaurando: true,
  establecerSesion: (usuario, accessToken) =>
    set({ usuario, accessToken, restaurando: false }),
  limpiarSesion: () => set({ usuario: null, accessToken: null, restaurando: false }),
  terminarRestauracion: () => set({ restaurando: false }),
}));
