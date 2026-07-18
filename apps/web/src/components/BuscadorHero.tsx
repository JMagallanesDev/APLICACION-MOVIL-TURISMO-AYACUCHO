"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { useRouter } from "@/i18n/navigation";

/** Buscador principal del home: envía a /lugares?q= (RF-02). */
export default function BuscadorHero() {
  const t = useTranslations("Home");
  const router = useRouter();
  const [valor, setValor] = useState("");

  function buscar(e: React.FormEvent) {
    e.preventDefault();
    const q = valor.trim();
    router.push(q ? (`/lugares?q=${encodeURIComponent(q)}` as never) : "/lugares");
  }

  return (
    <form
      onSubmit={buscar}
      className="flex w-full items-center gap-2 rounded-full border border-border bg-muted/60 p-1.5 pl-4 focus-within:border-primary"
    >
      <span aria-hidden className="opacity-60">
        🔍
      </span>
      <input
        type="search"
        value={valor}
        onChange={(e) => setValor(e.target.value)}
        placeholder={t("buscarPlaceholder")}
        aria-label={t("buscarPlaceholder")}
        className="min-h-10 w-full bg-transparent text-base outline-none placeholder:opacity-60"
      />
      <button
        type="submit"
        className="min-h-10 shrink-0 rounded-full bg-primary px-5 text-sm font-medium text-primary-foreground transition-opacity hover:opacity-90"
      >
        {t("buscar")}
      </button>
    </form>
  );
}
