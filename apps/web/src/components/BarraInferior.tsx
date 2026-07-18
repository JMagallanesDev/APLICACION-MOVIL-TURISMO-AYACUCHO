"use client";

import { useTranslations } from "next-intl";
import { Link, usePathname } from "@/i18n/navigation";

/** Barra de navegación inferior (solo móvil), estilo app nativa. */
export default function BarraInferior() {
  const t = useTranslations("Nav");
  const pathname = usePathname();

  const pestanas = [
    { href: "/", icono: "🧭", etiqueta: t("explorar") },
    { href: "/rutas", icono: "🗺", etiqueta: t("rutas") },
    { href: "/eventos", icono: "📅", etiqueta: t("agenda") },
    { href: "/cuenta", icono: "👤", etiqueta: t("perfil") },
  ] as const;

  const activa = (href: string) =>
    href === "/" ? pathname === "/" : pathname.startsWith(href);

  return (
    <nav
      aria-label={t("inicio")}
      className="fixed inset-x-0 bottom-0 z-40 border-t border-border bg-background/95 backdrop-blur md:hidden"
    >
      <ul className="mx-auto flex max-w-md items-stretch justify-around">
        {pestanas.map((p) => (
          <li key={p.href} className="flex-1">
            <Link
              href={p.href}
              className={`flex min-h-14 flex-col items-center justify-center gap-0.5 text-xs font-medium ${
                activa(p.href) ? "text-primary" : "opacity-70 hover:opacity-100"
              }`}
            >
              <span aria-hidden className="text-lg leading-none">
                {p.icono}
              </span>
              {p.etiqueta}
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  );
}
