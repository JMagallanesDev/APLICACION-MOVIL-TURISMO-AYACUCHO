import { getTranslations, setRequestLocale } from "next-intl/server";
import { Link } from "@/i18n/navigation";
import { CATEGORIAS, obtenerLugares } from "@/lib/api";
import Buscador from "@/components/Buscador";
import LugarCard from "@/components/LugarCard";
import SelectorIdioma from "@/components/SelectorIdioma";

/** Listado público de lugares (RF-01) con búsqueda (RF-02) y filtros (RF-04). */
export default async function PaginaLugares({
  params,
  searchParams,
}: {
  params: Promise<{ locale: string }>;
  searchParams: Promise<{ q?: string; categoria?: string; pagina?: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  const { q, categoria, pagina } = await searchParams;
  const t = await getTranslations("Lugares");

  const paginaActual = Math.max(0, Number(pagina ?? 0) || 0);
  const datos = await obtenerLugares({
    idioma: locale,
    q,
    categoria,
    pagina: paginaActual,
  });

  return (
    <div className="mx-auto max-w-6xl px-6 py-6">
      <header className="flex items-center justify-between gap-4">
        <Link href="/" className="text-lg font-semibold text-primary">
          Turismo Huamanga
        </Link>
        <SelectorIdioma />
      </header>

      <h1 className="mt-8 text-3xl font-bold">{t("titulo")}</h1>

      <div className="mt-4">
        <Buscador />
      </div>

      {/* Filtros por categoría (RF-04) */}
      <div className="mt-4 flex flex-wrap gap-2">
        <Link
          href="/lugares"
          className={`min-h-8 rounded-full border px-3 py-1 text-sm ${
            !categoria && !q
              ? "border-primary bg-primary text-primary-foreground"
              : "border-border hover:bg-muted"
          }`}
        >
          {t("todas")}
        </Link>
        {CATEGORIAS.map((codigo) => (
          <Link
            key={codigo}
            href={{ pathname: "/lugares", query: { categoria: codigo } }}
            className={`min-h-8 rounded-full border px-3 py-1 text-sm ${
              categoria === codigo
                ? "border-primary bg-primary text-primary-foreground"
                : "border-border hover:bg-muted"
            }`}
          >
            {t(`categorias.${codigo}`)}
          </Link>
        ))}
      </div>

      <p className="mt-6 text-sm opacity-60">
        {t("resultados", { total: datos.totalElements })}
      </p>

      {datos.content.length === 0 ? (
        <p className="mt-10 text-center opacity-70">{t("sinResultados")}</p>
      ) : (
        <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {datos.content.map((lugar) => (
            <LugarCard key={lugar.id} lugar={lugar} />
          ))}
        </div>
      )}

      {/* Paginación (RF-01) */}
      {datos.totalPages > 1 && (
        <nav className="mt-8 flex items-center justify-center gap-3 text-sm">
          {paginaActual > 0 && (
            <Link
              href={{
                pathname: "/lugares",
                query: { ...(q && { q }), ...(categoria && { categoria }), pagina: paginaActual - 1 },
              }}
              className="rounded-full border border-border px-4 py-2 hover:bg-muted"
            >
              ← {t("anterior")}
            </Link>
          )}
          <span className="opacity-70">
            {t("paginaDe", { actual: paginaActual + 1, total: datos.totalPages })}
          </span>
          {paginaActual + 1 < datos.totalPages && (
            <Link
              href={{
                pathname: "/lugares",
                query: { ...(q && { q }), ...(categoria && { categoria }), pagina: paginaActual + 1 },
              }}
              className="rounded-full border border-border px-4 py-2 hover:bg-muted"
            >
              {t("siguiente")} →
            </Link>
          )}
        </nav>
      )}
    </div>
  );
}
