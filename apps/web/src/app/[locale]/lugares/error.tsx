"use client";

import { useTranslations } from "next-intl";
import { Link } from "@/i18n/navigation";

/**
 * Error boundary del catálogo: si el API no responde (p. ej. redeploy del
 * backend), el usuario ve un mensaje claro con reintento en vez de la
 * pantalla de error de Next (RNF-23).
 */
export default function ErrorLugares({ reset }: { error: Error; reset: () => void }) {
  const t = useTranslations("Lugares");

  return (
    <div className="mx-auto flex min-h-[60vh] max-w-md flex-col items-center justify-center gap-4 px-6 text-center">
      <span aria-hidden className="text-4xl">
        ⛅
      </span>
      <h1 className="text-xl font-semibold">{t("errorCargaTitulo")}</h1>
      <p className="text-sm opacity-75">{t("errorCargaTexto")}</p>
      <div className="mt-2 flex gap-3">
        <button
          type="button"
          onClick={() => reset()}
          className="min-h-11 rounded-full bg-primary px-6 font-medium text-primary-foreground hover:opacity-90"
        >
          {t("reintentar")}
        </button>
        <Link
          href="/"
          className="inline-flex min-h-11 items-center rounded-full border border-border px-6 font-medium hover:bg-muted"
        >
          {t("irAlInicio")}
        </Link>
      </div>
    </div>
  );
}
