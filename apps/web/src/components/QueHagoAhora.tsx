"use client";

import { useEffect, useState } from "react";
import { useLocale, useTranslations } from "next-intl";
import { Link } from "@/i18n/navigation";

/**
 * Bloque "¿Qué hago ahora?" (RF-08): consulta el motor de recomendaciones
 * contextual (hora + clima + abierto/cerrado). Cliente porque depende del
 * momento exacto de la visita; si el API falla, el bloque se oculta.
 */

interface LugarRecomendado {
  slug: string;
  nombre: string;
  categoriaCodigo: string;
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

/** Acento visual por categoría (gradiente del design system Ayacucho). */
const ACENTO: Record<string, string> = {
  IGLESIAS: "from-primary/80 to-primary/40",
  MIRADORES: "from-secondary/80 to-secondary/40",
  MUSEOS: "from-[#8a5a3b]/80 to-[#8a5a3b]/40",
  PLAZAS: "from-accent/80 to-accent/40",
  SITIOS_ARQUEOLOGICOS: "from-[#8a5a3b]/70 to-secondary/40",
  CASONAS: "from-primary/60 to-accent/40",
};

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
        <div className="h-8 w-64 rounded bg-muted" />
        <div className="mt-6 grid gap-6 md:grid-cols-3">
          {[0, 1, 2].map((i) => (
            <div key={i} className="h-48 rounded-2xl bg-muted" />
          ))}
        </div>
      </section>
    );
  }

  return (
    <section className="w-full">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h2 className="text-2xl font-bold sm:text-3xl">{t("titulo")}</h2>
          <p className="mt-1 opacity-80">{t("subtitulo")}</p>
        </div>
        {datos!.clima && (
          <span className="flex items-center gap-2 rounded-full bg-accent/15 px-4 py-2 text-sm font-medium text-accent">
            {datos!.clima.lluvia ? "🌧" : "☀"} {Math.round(datos!.clima.temperaturaC)}°C ·{" "}
            {datos!.clima.descripcion}
            {datos!.clima.esFallback && " *"}
          </span>
        )}
      </div>

      <div className="mt-6 grid gap-6 md:grid-cols-3">
        {datos!.lugares.map((lugar) => (
          <Link
            key={lugar.slug}
            href={`/lugares/${lugar.slug}`}
            className="group flex flex-col overflow-hidden rounded-2xl border border-border bg-background shadow-sm transition-shadow hover:shadow-md"
          >
            <div
              aria-hidden
              className={`flex h-28 items-end bg-linear-to-br p-4 ${
                ACENTO[lugar.categoriaCodigo] ?? "from-primary/70 to-primary/30"
              }`}
            >
              <span className="rounded-full bg-background/90 px-3 py-1 text-xs font-medium">
                {tLugares(`categorias.${lugar.categoriaCodigo}`)}
              </span>
            </div>
            <div className="flex flex-1 flex-col justify-between gap-2 p-5">
              <h3 className="text-lg font-semibold leading-tight group-hover:text-primary">
                {lugar.nombre}
              </h3>
              <p className="text-sm opacity-80">{t(`razones.${lugar.razonClave}`)}</p>
            </div>
          </Link>
        ))}
      </div>
    </section>
  );
}
