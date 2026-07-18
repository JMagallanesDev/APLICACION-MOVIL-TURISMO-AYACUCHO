"use client";

import { useEffect, useState } from "react";
import { useFormatter, useTranslations } from "next-intl";
import { Link } from "@/i18n/navigation";
import type { EventoResumen } from "@/lib/api";

/**
 * Card destacada de la agenda cultural (estilo mockup): fondo oscuro,
 * countdown en vivo y acceso directo a "Durante mi visita" (RF-84b).
 */
export default function AgendaDestacada({ evento }: { evento: EventoResumen }) {
  const t = useTranslations("Eventos");
  const format = useFormatter();
  const [cuenta, setCuenta] = useState<string | null>(null);

  const inicio = new Date(evento.fechaInicio + "T00:00:00");
  const fin = new Date(evento.fechaFin + "T00:00:00");
  const mismoDia = evento.fechaInicio === evento.fechaFin;

  useEffect(() => {
    function actualizar() {
      const ms = inicio.getTime() - Date.now();
      if (ms <= 0) {
        setCuenta(null);
        return;
      }
      const dias = Math.floor(ms / 86_400_000);
      const horas = Math.floor((ms % 86_400_000) / 3_600_000);
      setCuenta(dias > 0 ? `${dias}d ${horas}h` : `${horas}h`);
    }
    actualizar();
    const timer = setInterval(actualizar, 60_000);
    return () => clearInterval(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [evento.fechaInicio]);

  return (
    <article className="overflow-hidden rounded-2xl bg-[#241a17] p-6 text-white shadow-md dark:border dark:border-border">
      <div className="flex items-center justify-between gap-3">
        <span className="rounded-full bg-accent/25 px-3 py-1 text-[11px] font-bold uppercase tracking-wider text-accent">
          {t(`tipos.${evento.tipo}`)}
        </span>
        {cuenta && (
          <span className="rounded-full bg-white/10 px-3 py-1 text-xs font-semibold">
            {t("enTiempo", { tiempo: cuenta })}
          </span>
        )}
      </div>

      <h3 className="mt-4 text-2xl font-bold leading-tight sm:text-3xl">{evento.nombre}</h3>

      <p className="mt-3 flex flex-wrap items-center gap-4 text-sm text-white/85">
        <span>
          📅{" "}
          {mismoDia
            ? format.dateTime(inicio, { day: "numeric", month: "long" })
            : `${format.dateTime(inicio, { day: "numeric", month: "short" })} – ${format.dateTime(fin, { day: "numeric", month: "short" })}`}
        </span>
        <span>📍 Huamanga</span>
      </p>

      <Link
        href="/eventos"
        className="mt-5 flex min-h-11 w-full items-center justify-center rounded-full bg-primary px-6 text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90"
      >
        {t("duranteMiVisita")}
      </Link>
    </article>
  );
}
