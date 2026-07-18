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

export default function QueHagoAhora() {
  const t = useTranslations("Recomendaciones");
  const locale = useLocale();
  const [datos, setDatos] = useState<Recomendacion | null>(null);
  const [estado, setEstado] = useState<"cargando" | "listo" | "error">("cargando");

  useEffect(() => {
    fetch(`${API_URL}/recomendaciones/ahora?idioma=${locale}&limite=4`)
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
      <section className="w-full max-w-md animate-pulse rounded-2xl border border-border bg-muted p-6">
        <div className="h-5 w-48 rounded bg-background/60" />
        <div className="mt-3 h-4 w-full rounded bg-background/60" />
        <div className="mt-2 h-4 w-3/4 rounded bg-background/60" />
      </section>
    );
  }

  return (
    <section className="w-full max-w-md rounded-2xl border border-border bg-muted p-6 text-left">
      <div className="flex items-center justify-between gap-3">
        <h2 className="font-semibold text-secondary">{t("titulo")}</h2>
        {datos!.clima && (
          <span className="rounded-full bg-background px-3 py-1 text-xs font-medium">
            {Math.round(datos!.clima.temperaturaC)}°C · {datos!.clima.descripcion}
            {datos!.clima.esFallback && " *"}
          </span>
        )}
      </div>

      <ul className="mt-4 space-y-2">
        {datos!.lugares.map((lugar) => (
          <li key={lugar.slug}>
            <Link
              href={`/lugares/${lugar.slug}`}
              className="group flex items-center justify-between gap-3 rounded-xl bg-background px-4 py-3 transition-shadow hover:shadow-sm"
            >
              <span className="font-medium group-hover:text-primary">{lugar.nombre}</span>
              <span className="shrink-0 text-xs opacity-80">
                {t(`razones.${lugar.razonClave}`)}
              </span>
            </Link>
          </li>
        ))}
      </ul>
    </section>
  );
}
