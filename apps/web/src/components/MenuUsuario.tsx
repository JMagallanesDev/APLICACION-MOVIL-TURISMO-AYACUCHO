"use client";

import { useTranslations } from "next-intl";
import { Link, useRouter } from "@/i18n/navigation";
import { cerrarSesion } from "@/lib/apiCliente";
import { useSesion } from "@/stores/sesion";

/**
 * Estado de sesión en el encabezado (RF-32/RF-34): botón "Iniciar sesión"
 * para anónimos, nombre + salir para autenticados. Navegación anónima
 * siempre permitida.
 */
export default function MenuUsuario() {
  const t = useTranslations("Cuenta");
  const router = useRouter();
  const usuario = useSesion((s) => s.usuario);
  const restaurando = useSesion((s) => s.restaurando);
  const limpiarSesion = useSesion((s) => s.limpiarSesion);

  if (restaurando) {
    return <span className="h-8 w-20 animate-pulse rounded-full bg-muted" aria-hidden />;
  }

  if (!usuario) {
    return (
      <Link
        href="/cuenta"
        className="inline-flex min-h-9 items-center rounded-full bg-primary px-4 text-sm font-medium text-primary-foreground hover:opacity-90"
      >
        {t("iniciarSesion")}
      </Link>
    );
  }

  async function salir() {
    await cerrarSesion();
    limpiarSesion();
    router.refresh();
  }

  return (
    <div className="flex items-center gap-3">
      <Link
        href="/favoritos"
        aria-label={t("misFavoritos")}
        title={t("misFavoritos")}
        className="text-lg text-red-500 hover:scale-110"
      >
        ♥
      </Link>
      {usuario.rol === "ADMIN" && (
        <Link
          href="/admin"
          className="min-h-9 rounded-full border border-primary px-4 text-sm font-medium text-primary hover:bg-primary/10"
        >
          {t("panel")}
        </Link>
      )}
      <span className="hidden text-sm sm:inline" title={usuario.email}>
        {t("hola", { nombre: usuario.nombre.split(" ")[0] })}
      </span>
      <button
        type="button"
        onClick={salir}
        className="min-h-9 rounded-full border border-border px-4 text-sm font-medium hover:bg-muted"
      >
        {t("salir")}
      </button>
    </div>
  );
}
