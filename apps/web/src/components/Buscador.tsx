"use client";

import { useEffect, useRef, useState } from "react";
import { useSearchParams } from "next/navigation";
import { useTranslations } from "next-intl";
import { usePathname, useRouter } from "@/i18n/navigation";

/** Búsqueda con debounce de 300 ms (RF-02) sincronizada con la URL. */
export default function Buscador() {
  const t = useTranslations("Lugares");
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const [valor, setValor] = useState(searchParams.get("q") ?? "");
  const primeraCarga = useRef(true);

  useEffect(() => {
    if (primeraCarga.current) {
      primeraCarga.current = false;
      return;
    }
    const timer = setTimeout(() => {
      const params = new URLSearchParams(searchParams.toString());
      if (valor.trim()) {
        params.set("q", valor.trim());
        params.delete("categoria");
      } else {
        params.delete("q");
      }
      params.delete("pagina");
      router.replace(`${pathname}?${params.toString()}` as never, { scroll: false });
    }, 300);
    return () => clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [valor]);

  return (
    <input
      type="search"
      value={valor}
      onChange={(e) => setValor(e.target.value)}
      placeholder={t("buscarPlaceholder")}
      aria-label={t("buscarPlaceholder")}
      className="min-h-11 w-full max-w-md rounded-full border border-border bg-background px-5 text-sm outline-none focus:border-primary"
    />
  );
}
