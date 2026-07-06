"use client";

import { useLocale } from "next-intl";
import { usePathname, useRouter } from "@/i18n/navigation";
import { routing } from "@/i18n/routing";

/**
 * Cambio de idioma inmediato sin recarga completa (RF-59): navega a la misma
 * ruta en el otro locale; next-intl persiste la elección en la cookie
 * NEXT_LOCALE (RF-63).
 */
export default function SelectorIdioma() {
  const locale = useLocale();
  const pathname = usePathname();
  const router = useRouter();

  return (
    <div
      role="group"
      aria-label="Idioma / Language"
      className="inline-flex rounded-full border border-[var(--border)] p-1"
    >
      {routing.locales.map((idioma) => (
        <button
          key={idioma}
          type="button"
          onClick={() => router.replace(pathname, { locale: idioma })}
          aria-pressed={idioma === locale}
          className={`min-w-11 min-h-8 rounded-full px-3 text-sm font-medium transition-colors ${
            idioma === locale
              ? "bg-[var(--primary)] text-[var(--primary-foreground)]"
              : "text-[var(--foreground)] hover:bg-[var(--muted)]"
          }`}
        >
          {idioma.toUpperCase()}
        </button>
      ))}
    </div>
  );
}
