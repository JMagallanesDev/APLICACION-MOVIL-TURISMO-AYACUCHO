import { getTranslations, setRequestLocale } from "next-intl/server";
import { Link } from "@/i18n/navigation";
import { CATEGORIAS, obtenerLugares } from "@/lib/api";
import Buscador from "@/components/Buscador";
import Encabezado from "@/components/Encabezado";
import LugarCard from "@/components/LugarCard";

/** Listado público de lugares (RF-01) con búsqueda (RF-02), filtros (RF-04) y ranking (RF-06). */
export default async function PaginaLugares({
  params,
  searchParams,
}: {
  params: Promise<{ locale: string }>;
  searchParams: Promise<{ q?: string; categoria?: string; orden?: string; pagina?: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  const { q, categoria, orden, pagina } = await searchParams;
  const t = await getTranslations("Lugares");

  const paginaActual = Math.max(0, Number(pagina ?? 0) || 0);
  const porCalificacion = orden === "calificacion";
  const datos = await obtenerLugares({
    idioma: locale,
    q,
    categoria,
    orden: porCalificacion ? "calificacion" : undefined,
    pagina: paginaActual,
  });

  // query base que conservan los enlaces de orden y paginación
  const filtros = { ...(q && { q }), ...(categoria && { categoria }) };
  const filtrosConOrden = { ...filtros, ...(porCalificacion && { orden: "calificacion" }) };

  return (
    <div className="mx-auto max-w-6xl px-6 py-6">
      <Encabezado />

      <h1 className="mt-8 text-3xl font-bold">{t("titulo")}</h1>

      <div className="mt-4">
        <Buscador />
      </div>

      {/* Filtros por categoría (RF-04) */}
      <div className="mt-4 flex flex-wrap gap-2">
        <Link
          href={{ pathname: "/lugares", query: { ...(porCalificacion && { orden: "calificacion" }) } }}
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
            href={{
              pathname: "/lugares",
              query: { categoria: codigo, ...(porCalificacion && { orden: "calificacion" }) },
            }}
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

      <div className="mt-6 flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm opacity-60">{t("resultados", { total: datos.totalElements })}</p>

        {/* Orden (RF-06) — la búsqueda ordena por relevancia, sin selector */}
        {!q && (
          <div className="flex items-center gap-2 text-sm">
            <span className="opacity-60">{t("ordenar")}</span>
            <Link
              href={{ pathname: "/lugares", query: filtros }}
              className={`min-h-8 rounded-full border px-3 py-1 ${
                !porCalificacion
                  ? "border-primary bg-primary text-primary-foreground"
                  : "border-border hover:bg-muted"
              }`}
            >
              {t("ordenNombre")}
            </Link>
            <Link
              href={{ pathname: "/lugares", query: { ...filtros, orden: "calificacion" } }}
              className={`min-h-8 rounded-full border px-3 py-1 ${
                porCalificacion
                  ? "border-primary bg-primary text-primary-foreground"
                  : "border-border hover:bg-muted"
              }`}
            >
              ★ {t("ordenCalificacion")}
            </Link>
          </div>
        )}
      </div>

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
                query: { ...filtrosConOrden, pagina: paginaActual - 1 },
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
                query: { ...filtrosConOrden, pagina: paginaActual + 1 },
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
