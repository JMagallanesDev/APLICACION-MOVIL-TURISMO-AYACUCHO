"use client";

import { useEffect } from "react";
import { refrescarSesion } from "@/lib/apiCliente";
import { useSesion } from "@/stores/sesion";

/**
 * Al montar la app intenta recuperar la sesión con la cookie httpOnly del
 * refresh token (plan, sección 5.3): si el usuario ya había iniciado sesión,
 * vuelve a tener su access token en memoria sin volver a escribir la clave.
 */
export default function ProveedorSesion({ children }: { children: React.ReactNode }) {
  const establecerSesion = useSesion((s) => s.establecerSesion);
  const terminarRestauracion = useSesion((s) => s.terminarRestauracion);

  useEffect(() => {
    let activo = true;
    refrescarSesion().then((renovada) => {
      if (!activo) return;
      if (renovada) {
        establecerSesion(renovada.usuario, renovada.accessToken);
      } else {
        terminarRestauracion();
      }
    });
    return () => {
      activo = false;
    };
  }, [establecerSesion, terminarRestauracion]);

  return <>{children}</>;
}
