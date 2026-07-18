"use client";

import { useEffect, useState } from "react";
import { useLocale, useTranslations } from "next-intl";
import { Link } from "@/i18n/navigation";
import type { LugarResumen } from "@/lib/api";
import { obtenerFavoritosCards } from "@/lib/apiCliente";
import Encabezado from "@/components/Encabezado";
import LugarCard from "@/components/LugarCard";
import { useFavoritos } from "@/stores/favoritos";
import { useSesion } from "@/stores/sesion";

/** Mis lugares favoritos (RF-35). */
export default function PaginaFavoritos() {
  const t = useTranslations("Favoritos");
  const locale = useLocale();
  const usuario = useSesion((s) => s.usuario);
  const restaurando = useSesion((s) => s.restaurando);
  // re-consulta cuando el usuario marca/quita desde las cards de esta página
  const totalSlugs = useFavoritos((s) => s.slugs.length);

  const [lugares, setLugares] = useState<LugarResumen[] | null>(null);

  useEffect(() => {
    if (restaurando || !usuario) return;
    let vivo = true;
    obtenerFavoritosCards(locale).then((r) => {
      if (vivo) setLugares(r as LugarResumen[]);
    });
    return () => {
      vivo = false;
    };
  }, [usuario, restaurando, locale, totalSlugs]);

  return (
    <main className="mx-auto max-w-6xl px-6 py-6">
      <Encabezado />
      <h1 className="mt-8 text-3xl font-bold">{t("titulo")}</h1>

      {restaurando ? (
        <div className="mt-6 h-40 animate-pulse rounded-2xl bg-muted" />
      ) : !usuario ? (
        <p className="mt-6 rounded-2xl bg-muted px-5 py-4 text-sm">
          <Link href="/cuenta" className="font-medium text-secondary hover:underline">
            {t("iniciaSesion")}
          </Link>
        </p>
      ) : lugares === null ? (
        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="h-44 animate-pulse rounded-2xl bg-muted" />
          ))}
        </div>
      ) : lugares.length === 0 ? (
        <div className="mt-10 text-center">
          <span aria-hidden className="text-4xl">
            ♡
          </span>
          <p className="mt-3 text-sm opacity-80">{t("vacio")}</p>
          <Link
            href="/lugares"
            className="mt-4 inline-flex min-h-11 items-center rounded-full bg-primary px-6 text-sm font-medium text-primary-foreground hover:opacity-90"
          >
            {t("explorar")}
          </Link>
        </div>
      ) : (
        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {lugares.map((lugar) => (
            <LugarCard key={lugar.id} lugar={lugar} />
          ))}
        </div>
      )}
    </main>
  );
}
