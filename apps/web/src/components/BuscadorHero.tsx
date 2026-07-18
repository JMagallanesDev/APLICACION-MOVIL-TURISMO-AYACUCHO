"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { useRouter } from "@/i18n/navigation";

/** Buscador del hero: envía a /lugares?q= (RF-02). */
export default function BuscadorHero() {
  const t = useTranslations("Lugares");
  const tHome = useTranslations("Home");
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
      className="flex w-full max-w-2xl items-center gap-2 rounded-full border border-white/30 bg-background/95 p-1.5 pl-5 shadow-lg backdrop-blur-md"
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
        className="min-h-11 w-full bg-transparent text-base outline-none placeholder:opacity-60"
      />
      <button
        type="submit"
        className="min-h-11 shrink-0 rounded-full bg-primary px-6 text-sm font-medium text-primary-foreground transition-opacity hover:opacity-90"
      >
        {tHome("buscar")}
      </button>
    </form>
  );
}
