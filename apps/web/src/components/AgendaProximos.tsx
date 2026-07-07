"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { TIPOS_EVENTO, type EventoResumen } from "@/lib/api";
import EventoCard from "./EventoCard";

/** Lista de próximos eventos con filtro por tipo (RF-84/RF-85). */
export default function AgendaProximos({ eventos }: { eventos: EventoResumen[] }) {
  const t = useTranslations("Eventos");
  const [tipo, setTipo] = useState<string | null>(null);

  const tiposPresentes = TIPOS_EVENTO.filter((x) => eventos.some((e) => e.tipo === x));
  const filtrados = tipo ? eventos.filter((e) => e.tipo === tipo) : eventos;

  return (
    <div>
      <div className="flex flex-wrap gap-2">
        <button
          type="button"
          onClick={() => setTipo(null)}
          className={`min-h-8 rounded-full border px-3 py-1 text-sm ${
            tipo === null ? "border-primary bg-primary text-primary-foreground" : "border-border hover:bg-muted"
          }`}
        >
          {t("todos")}
        </button>
        {tiposPresentes.map((x) => (
          <button
            key={x}
            type="button"
            onClick={() => setTipo(x)}
            className={`min-h-8 rounded-full border px-3 py-1 text-sm ${
              tipo === x ? "border-primary bg-primary text-primary-foreground" : "border-border hover:bg-muted"
            }`}
          >
            {t(`tipos.${x}`)}
          </button>
        ))}
      </div>

      {filtrados.length === 0 ? (
        <p className="mt-6 text-sm opacity-70">{t("sinEventos")}</p>
      ) : (
        <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {filtrados.map((e) => (
            <EventoCard key={e.id} evento={e} />
          ))}
        </div>
      )}
    </div>
  );
}
