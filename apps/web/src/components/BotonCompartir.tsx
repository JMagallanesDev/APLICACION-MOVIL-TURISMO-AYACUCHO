"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";

/**
 * Compartir lugar (RF-15): share nativo si el dispositivo lo soporta,
 * copiar enlace como fallback, y código QR para mostrar en persona.
 */
export default function BotonCompartir({ titulo }: { titulo: string }) {
  const t = useTranslations("Lugares");
  const [copiado, setCopiado] = useState(false);
  const [qrVisible, setQrVisible] = useState(false);

  async function compartir() {
    const url = window.location.href;
    if (navigator.share) {
      try {
        await navigator.share({ title: titulo, url });
        return;
      } catch {
        // usuario canceló: caer al portapapeles
      }
    }
    await navigator.clipboard.writeText(url);
    setCopiado(true);
    setTimeout(() => setCopiado(false), 2500);
  }

  return (
    <div className="relative flex items-center gap-2">
      <button
        type="button"
        onClick={compartir}
        className="min-h-9 rounded-full border border-border px-4 text-sm font-medium hover:bg-muted"
      >
        {copiado ? `✓ ${t("enlaceCopiado")}` : `↗ ${t("compartir")}`}
      </button>
      <button
        type="button"
        onClick={() => setQrVisible((v) => !v)}
        aria-expanded={qrVisible}
        className="min-h-9 rounded-full border border-border px-4 text-sm font-medium hover:bg-muted"
      >
        {t("codigoQR")}
      </button>
      {qrVisible && (
        <div className="absolute right-0 top-11 z-10 rounded-2xl border border-border bg-background p-3 shadow-lg">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src={`https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${encodeURIComponent(
              typeof window !== "undefined" ? window.location.href : "",
            )}`}
            alt={t("codigoQR")}
            width={180}
            height={180}
          />
        </div>
      )}
    </div>
  );
}
