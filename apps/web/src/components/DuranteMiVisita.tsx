"use client";

import { useEffect, useState } from "react";
import { useLocale, useTranslations } from "next-intl";
import { obtenerEventosEnRango, type EventoResumen } from "@/lib/api";
import { useViaje } from "@/stores/viaje";
import EventoCard from "./EventoCard";

/**
 * "Durante mi visita" (RF-84b): el turista ingresa sus fechas de viaje (se
 * guardan en localStorage sin cuenta) y ve solo los eventos que coinciden.
 */
export default function DuranteMiVisita() {
  const t = useTranslations("Eventos");
  const locale = useLocale();
  const { desde, hasta, establecer, limpiar } = useViaje();

  // Inicialización perezosa desde el store persistido (evita setState en efecto)
  const [d, setD] = useState(() => desde ?? "");
  const [h, setH] = useState(() => hasta ?? "");
  const [eventos, setEventos] = useState<EventoResumen[] | null>(null);
  const [cargando, setCargando] = useState(() => Boolean(desde && hasta));

  // Busca cuando cambian las fechas confirmadas (al montar si ya había, o al confirmar)
  useEffect(() => {
    if (!desde || !hasta) return;
    let vivo = true;
    obtenerEventosEnRango(locale, desde, hasta)
      .then((r) => {
        if (vivo) setEventos(r);
      })
      .finally(() => {
        if (vivo) setCargando(false);
      });
    return () => {
      vivo = false;
    };
  }, [desde, hasta, locale]);

  function buscar(e: React.FormEvent) {
    e.preventDefault();
    if (d && h && h >= d) {
      setCargando(true);
      establecer(d, h);
    }
  }

  return (
    <section className="rounded-2xl border border-border bg-muted p-6">
      <h2 className="text-lg font-semibold text-secondary">{t("duranteMiVisita")}</h2>
      <p className="mt-1 text-sm opacity-75">{t("duranteMiVisitaAyuda")}</p>

      <form onSubmit={buscar} className="mt-4 flex flex-wrap items-end gap-3">
        <label className="flex flex-col gap-1 text-sm">
          {t("desde")}
          <input
            type="date"
            value={d}
            onChange={(e) => setD(e.target.value)}
            className="min-h-11 rounded-xl border border-border bg-background px-3 outline-none focus:border-primary"
          />
        </label>
        <label className="flex flex-col gap-1 text-sm">
          {t("hasta")}
          <input
            type="date"
            value={h}
            min={d}
            onChange={(e) => setH(e.target.value)}
            className="min-h-11 rounded-xl border border-border bg-background px-3 outline-none focus:border-primary"
          />
        </label>
        <button
          type="submit"
          className="min-h-11 rounded-full bg-primary px-6 text-sm font-medium text-primary-foreground hover:opacity-90"
        >
          {t("buscarEventos")}
        </button>
        {(desde || hasta) && (
          <button
            type="button"
            onClick={() => {
              limpiar();
              setD("");
              setH("");
              setEventos(null);
            }}
            className="min-h-11 rounded-full border border-border px-4 text-sm font-medium hover:bg-background"
          >
            {t("limpiar")}
          </button>
        )}
      </form>

      {cargando && <div className="mt-4 h-20 animate-pulse rounded-xl bg-background" />}

      {!cargando && eventos !== null && (
        <div className="mt-4">
          {eventos.length === 0 ? (
            <p className="text-sm opacity-80">{t("sinEventosEnFechas")}</p>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2">
              {eventos.map((e) => (
                <EventoCard key={e.id} evento={e} />
              ))}
            </div>
          )}
        </div>
      )}
    </section>
  );
}
