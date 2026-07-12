"use client";

import { useRef, useState } from "react";
import { useTranslations } from "next-intl";
import { Link } from "@/i18n/navigation";
import { subirFoto, type ErrorApi } from "@/lib/apiCliente";
import { useSesion } from "@/stores/sesion";

/** Subida de fotos (RF-38): quedan PENDIENTES hasta moderación. */
export default function SubirFoto({ slug }: { slug: string }) {
  const t = useTranslations("Lugares");
  const usuario = useSesion((s) => s.usuario);
  const inputRef = useRef<HTMLInputElement>(null);
  const [estado, setEstado] = useState<"inicial" | "subiendo" | "enviada">("inicial");
  const [error, setError] = useState<string | null>(null);

  if (!usuario) {
    return (
      <p className="text-sm">
        <Link href="/cuenta" className="font-medium text-secondary hover:underline">
          {t("iniciaSesionParaFoto")}
        </Link>
      </p>
    );
  }

  if (estado === "enviada") {
    return (
      <p className="rounded-xl bg-emerald-600/10 px-4 py-2 text-sm text-emerald-700 dark:text-emerald-400">
        {t("fotoEnviada")}
      </p>
    );
  }

  async function alElegir(e: React.ChangeEvent<HTMLInputElement>) {
    const archivo = e.target.files?.[0];
    if (!archivo) return;
    setError(null);
    setEstado("subiendo");
    try {
      await subirFoto(slug, archivo);
      setEstado("enviada");
    } catch (err) {
      setError((err as ErrorApi).mensaje ?? t("errorFoto"));
      setEstado("inicial");
      if (inputRef.current) inputRef.current.value = "";
    }
  }

  return (
    <div className="flex flex-col gap-1">
      <label className="inline-flex w-fit cursor-pointer items-center gap-2 rounded-full border border-border px-4 py-2 text-sm font-medium hover:bg-muted">
        📷 {estado === "subiendo" ? t("subiendoFoto") : t("subirFoto")}
        <input
          ref={inputRef}
          type="file"
          accept="image/jpeg,image/png,image/webp"
          onChange={alElegir}
          disabled={estado === "subiendo"}
          className="hidden"
        />
      </label>
      {error && (
        <span role="alert" className="text-xs text-red-600">
          {error}
        </span>
      )}
    </div>
  );
}
