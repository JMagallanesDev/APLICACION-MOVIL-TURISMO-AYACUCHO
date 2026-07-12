"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { reportarContenido, type ErrorApi } from "@/lib/apiCliente";
import { useSesion } from "@/stores/sesion";

/**
 * Reporte de contenido inapropiado (RF-45): 3 reportes únicos lo mandan a
 * revisión. Solo visible para usuarios con sesión.
 */
export default function BotonReportar({
  resenaId,
  fotoId,
}: {
  resenaId?: string;
  fotoId?: string;
}) {
  const t = useTranslations("Lugares");
  const usuario = useSesion((s) => s.usuario);
  const [abierto, setAbierto] = useState(false);
  const [estado, setEstado] = useState<"inicial" | "enviado" | "duplicado">("inicial");

  if (!usuario || estado === "enviado" || estado === "duplicado") {
    if (estado === "enviado") {
      return <span className="text-xs text-emerald-600">{t("reporteEnviado")}</span>;
    }
    if (estado === "duplicado") {
      return <span className="text-xs opacity-60">{t("yaReportado")}</span>;
    }
    return null;
  }

  async function enviar(motivo: string) {
    try {
      await reportarContenido({ resenaId, fotoId }, motivo);
      setEstado("enviado");
    } catch (err) {
      setEstado((err as ErrorApi).error === "YA_REPORTADO" ? "duplicado" : "inicial");
    } finally {
      setAbierto(false);
    }
  }

  const motivos = ["ofensivo", "spam", "noCorresponde"] as const;

  return (
    <span className="relative inline-flex text-xs">
      <button
        type="button"
        onClick={() => setAbierto((v) => !v)}
        className="opacity-50 hover:opacity-100 hover:underline"
      >
        ⚑ {t("reportarContenido")}
      </button>
      {abierto && (
        <span className="absolute right-0 top-6 z-10 flex w-44 flex-col overflow-hidden rounded-xl border border-border bg-background shadow-lg">
          {motivos.map((m) => (
            <button
              key={m}
              type="button"
              onClick={() => enviar(t(`motivos.${m}`))}
              className="px-3 py-2 text-left hover:bg-muted"
            >
              {t(`motivos.${m}`)}
            </button>
          ))}
        </span>
      )}
    </span>
  );
}
