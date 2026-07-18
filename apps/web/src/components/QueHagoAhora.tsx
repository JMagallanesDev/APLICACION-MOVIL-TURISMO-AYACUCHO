"use client";

import { useEffect, useState } from "react";
import { useLocale, useTranslations } from "next-intl";
import { Link } from "@/i18n/navigation";
import { ACENTO_CATEGORIA, ICONO_CATEGORIA } from "@/lib/categorias";

/**
 * Bloque "¿Qué hago ahora?" (RF-08): consulta el motor de recomendaciones
 * contextual (hora + clima + abierto/cerrado). Cliente porque depende del
 * momento exacto de la visita; si el API falla, el bloque se oculta.
 */

interface LugarRecomendado {
  slug: string;
  nombre: string;
  categoriaCodigo: string;
  abiertoAhora: boolean | null;
  razonClave: string;
}

interface Recomendacion {
  franja: string;
  clima: {
    temperaturaC: number;
    descripcion: string;
    lluvia: boolean;
    esFallback: boolean;
  } | null;
  lugares: LugarRecomendado[];
}

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1";

export default function QueHagoAhora() {
  const t = useTranslations("Recomendaciones");
  const tLugares = useTranslations("Lugares");
  const locale = useLocale();
  const [datos, setDatos] = useState<Recomendacion | null>(null);
  const [estado, setEstado] = useState<"cargando" | "listo" | "error">("cargando");

  useEffect(() => {
    fetch(`${API_URL}/recomendaciones/ahora?idioma=${locale}&limite=3`)
      .then((res) => (res.ok ? res.json() : Promise.reject(new Error(String(res.status)))))
      .then((json: Recomendacion) => {
        setDatos(json);
        setEstado("listo");
      })
      .catch(() => setEstado("error"));
  }, [locale]);

  if (estado === "error" || (estado === "listo" && !datos?.lugares.length)) {
    return null; // degradación: sin recomendaciones no se muestra nada roto
  }

  if (estado === "cargando") {
    return (
      <section className="w-full animate-pulse">
        <div className="h-7 w-56 rounded bg-muted" />
        <div className="mt-4 flex flex-col gap-3">
          {[0, 1].map((i) => (
            <div key={i} className="h-28 rounded-2xl bg-muted" />
          ))}
        </div>
      </section>
    );
  }

  return (
    <section className="w-full">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="text-xl font-bold sm:text-2xl">{t("titulo")}</h2>
          <p className="mt-0.5 text-sm opacity-80">{t("subtitulo")}</p>
        </div>
        {datos!.clima && (
          <span className="flex shrink-0 items-center gap-1.5 rounded-full bg-accent/15 px-3 py-1.5 text-sm font-medium text-accent">
            {datos!.clima.lluvia ? "🌧" : "☀"} {Math.round(datos!.clima.temperaturaC)}°C
            {datos!.clima.esFallback && " *"}
          </span>
        )}
      </div>

      <div className="mt-4 grid gap-3 md:grid-cols-3">
        {datos!.lugares.map((lugar) => (
          <Link
            key={lugar.slug}
            href={`/lugares/${lugar.slug}`}
            className="group flex gap-4 rounded-2xl border border-border bg-background p-3 shadow-sm transition-shadow hover:shadow-md"
          >
            <div
              aria-hidden
              className={`flex h-24 w-24 shrink-0 items-center justify-center rounded-xl bg-linear-to-br text-3xl ${
                ACENTO_CATEGORIA[lugar.categoriaCodigo] ?? "from-primary/70 to-primary/30"
              }`}
            >
              {ICONO_CATEGORIA[lugar.categoriaCodigo] ?? "📍"}
            </div>
            <div className="flex min-w-0 flex-col justify-center gap-1">
              {lugar.abiertoAhora === true && (
                <span className="w-max rounded-full bg-emerald-600/15 px-2 py-0.5 text-[11px] font-bold uppercase tracking-wide text-emerald-800 dark:text-emerald-400">
                  {tLugares("abiertoAhora")}
                </span>
              )}
              <h3 className="truncate font-semibold leading-tight group-hover:text-primary">
                {lugar.nombre}
              </h3>
              <p className="truncate text-xs opacity-80">{t(`razones.${lugar.razonClave}`)}</p>
              <span className="text-sm font-medium text-primary">{t("verDetalles")} →</span>
            </div>
          </Link>
        ))}
      </div>
    </section>
  );
}
