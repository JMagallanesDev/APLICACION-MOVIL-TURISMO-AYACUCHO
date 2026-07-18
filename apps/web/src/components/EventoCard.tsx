"use client";

import { useFormatter, useTranslations } from "next-intl";
import { Link } from "@/i18n/navigation";
import type { EventoResumen } from "@/lib/api";

/** Card de la agenda cultural con rango de fechas y tipo (RF-79/84). */
export default function EventoCard({ evento }: { evento: EventoResumen }) {
  const t = useTranslations("Eventos");
  const format = useFormatter();

  const inicio = new Date(evento.fechaInicio + "T00:00:00");
  const fin = new Date(evento.fechaFin + "T00:00:00");
  const mismoDia = evento.fechaInicio === evento.fechaFin;
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);
  const diasFalta = Math.round((inicio.getTime() - hoy.getTime()) / 86_400_000);

  return (
    <Link
      href={`/eventos/${evento.id}`}
      className="group flex flex-col gap-2 rounded-2xl border border-border bg-background p-5 transition-shadow hover:shadow-md"
    >
      <div className="flex items-center justify-between gap-2">
        <span className="text-xs font-medium uppercase tracking-wide text-secondary">
          {t(`tipos.${evento.tipo}`)}
        </span>
        {diasFalta > 0 && diasFalta <= 60 && (
          <span className="rounded-full bg-accent/15 px-2.5 py-0.5 text-xs font-medium text-accent">
            {t("enDias", { dias: diasFalta })}
          </span>
        )}
      </div>
      <h2 className="text-lg font-semibold group-hover:text-primary">{evento.nombre}</h2>
      <p className="mt-auto text-sm opacity-75">
        📅{" "}
        {mismoDia
          ? format.dateTime(inicio, { day: "numeric", month: "long", year: "numeric" })
          : `${format.dateTime(inicio, { day: "numeric", month: "short" })} – ${format.dateTime(fin, { day: "numeric", month: "short", year: "numeric" })}`}
      </p>
    </Link>
  );
}
