"use client";

import { useTranslations } from "next-intl";
import { useRouter } from "@/i18n/navigation";
import { alternarFavorito } from "@/lib/apiCliente";
import { useFavoritos } from "@/stores/favoritos";
import { useSesion } from "@/stores/sesion";

/**
 * Corazón de favoritos (RF-35) con actualización optimista: cambia al
 * instante y revierte si el API falla. Los anónimos van a iniciar sesión.
 */
export default function BotonFavorito({ slug }: { slug: string }) {
  const t = useTranslations("Lugares");
  const router = useRouter();
  const usuario = useSesion((s) => s.usuario);
  const esFavorito = useFavoritos((s) => s.slugs.includes(slug));
  const { agregar, quitar } = useFavoritos.getState();

  async function alternar(e: React.MouseEvent) {
    // Dentro de cards que son <Link>: no navegar al tocar el corazón
    e.preventDefault();
    e.stopPropagation();

    if (!usuario) {
      router.push("/cuenta");
      return;
    }
    const marcar = !esFavorito;
    if (marcar) agregar(slug);
    else quitar(slug);
    try {
      await alternarFavorito(slug, marcar);
    } catch {
      if (marcar) quitar(slug);
      else agregar(slug); // revertir
    }
  }

  return (
    <button
      type="button"
      onClick={alternar}
      aria-pressed={esFavorito}
      aria-label={esFavorito ? t("quitarFavorito") : t("marcarFavorito")}
      title={esFavorito ? t("quitarFavorito") : t("marcarFavorito")}
      className={`inline-flex h-8 w-8 items-center justify-center rounded-full text-xl leading-none transition-transform hover:scale-110 ${
        esFavorito ? "text-red-500" : "opacity-40 hover:opacity-80"
      }`}
    >
      {esFavorito ? "♥" : "♡"}
    </button>
  );
}
