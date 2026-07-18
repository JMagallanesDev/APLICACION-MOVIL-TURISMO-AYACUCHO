"use client";

import { useEffect, useState } from "react";
import { useTranslations } from "next-intl";

/**
 * Alternador claro/oscuro (RF-94): fija data-theme en <html> y persiste en
 * localStorage. Sin preferencia guardada se respeta la del sistema
 * (prefers-color-scheme en tokens.css); el script del layout evita el FOUC.
 */
export default function BotonTema() {
  const t = useTranslations("Nav");
  const [tema, setTema] = useState<string | null>(null);

  useEffect(() => {
    // tras hidratar, sincroniza con el tema aplicado por el script del layout
    const id = requestAnimationFrame(() =>
      setTema(document.documentElement.dataset.theme ?? null),
    );
    return () => cancelAnimationFrame(id);
  }, []);

  function alternar() {
    const actual =
      document.documentElement.dataset.theme ??
      (window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light");
    const nuevo = actual === "dark" ? "light" : "dark";
    document.documentElement.dataset.theme = nuevo;
    try {
      localStorage.setItem("tema", nuevo);
    } catch {
      // sin almacenamiento el tema solo dura la sesión
    }
    setTema(nuevo);
  }

  return (
    <button
      type="button"
      onClick={alternar}
      aria-label={t("cambiarTema")}
      title={t("cambiarTema")}
      className="inline-flex h-9 w-9 items-center justify-center rounded-full border border-border transition-colors hover:bg-muted"
    >
      <span aria-hidden>{tema === "dark" ? "☀" : "🌙"}</span>
    </button>
  );
}
