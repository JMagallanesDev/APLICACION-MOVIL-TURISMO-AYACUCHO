"use client";

import { Link } from "@/i18n/navigation";
import { useSesion } from "@/stores/sesion";

/**
 * Muestra el panel solo si la sesión es de rol ADMIN. Es guard de UX: la
 * seguridad real la impone el backend (@PreAuthorize ADMIN en cada endpoint,
 * RNF-16); aquí solo evitamos renderizar la interfaz a quien no corresponde.
 * El panel de administración es interno y va solo en español.
 */
export default function GuardAdmin({ children }: { children: React.ReactNode }) {
  const usuario = useSesion((s) => s.usuario);
  const restaurando = useSesion((s) => s.restaurando);

  if (restaurando) {
    return (
      <div className="mx-auto max-w-md px-6 py-20 text-center">
        <div className="mx-auto h-8 w-40 animate-pulse rounded bg-muted" />
      </div>
    );
  }

  if (!usuario || usuario.rol !== "ADMIN") {
    return (
      <div className="mx-auto max-w-md px-6 py-20 text-center">
        <span aria-hidden className="text-4xl">
          🔒
        </span>
        <h1 className="mt-3 text-xl font-semibold">Acceso restringido</h1>
        <p className="mt-2 text-sm opacity-75">Esta sección es solo para administradores.</p>
        <Link
          href="/cuenta"
          className="mt-4 inline-flex min-h-11 items-center rounded-full bg-primary px-6 text-sm font-medium text-primary-foreground hover:opacity-90"
        >
          Iniciar sesión
        </Link>
      </div>
    );
  }

  return <>{children}</>;
}
