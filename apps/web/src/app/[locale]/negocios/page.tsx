import { getTranslations, setRequestLocale } from "next-intl/server";
import { Link } from "@/i18n/navigation";
import { obtenerCategoriasNegocio, obtenerNegocios } from "@/lib/api";
import Encabezado from "@/components/Encabezado";

/** Directorio público de negocios aprobados (RF-105) con WhatsApp (RF-110). */
export const dynamic = "force-dynamic";

export default async function PaginaNegocios({
  params,
  searchParams,
}: {
  params: Promise<{ locale: string }>;
  searchParams: Promise<{ categoria?: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  const { categoria } = await searchParams;
  const t = await getTranslations("Negocios");

  const [categorias, negocios] = await Promise.all([
    obtenerCategoriasNegocio(locale),
    obtenerNegocios(locale, categoria),
  ]);

  // Mensaje predefinido del botón WhatsApp (RF-110)
  const mensaje = encodeURIComponent(t("mensajeWhatsApp"));

  return (
    <main className="mx-auto max-w-6xl px-6 py-6">
      <Encabezado />
      <div className="mt-8 flex items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold">{t("titulo")}</h1>
          <p className="mt-1 text-sm opacity-80">{t("subtitulo")}</p>
        </div>
        <Link
          href="/negocios/registrar"
          className="min-h-11 shrink-0 rounded-full bg-primary px-5 text-sm font-medium text-primary-foreground hover:opacity-90"
        >
          {t("registrarMiNegocio")}
        </Link>
      </div>

      {/* Filtro por categoría */}
      <div className="mt-6 flex flex-wrap gap-2">
        <Link
          href="/negocios"
          className={`min-h-8 rounded-full border px-3 py-1 text-sm ${
            !categoria ? "border-primary bg-primary text-primary-foreground" : "border-border hover:bg-muted"
          }`}
        >
          {t("todas")}
        </Link>
        {categorias.map((c) => (
          <Link
            key={c.id}
            href={{ pathname: "/negocios", query: { categoria: c.id } }}
            className={`min-h-8 rounded-full border px-3 py-1 text-sm ${
              categoria === c.id
                ? "border-primary bg-primary text-primary-foreground"
                : "border-border hover:bg-muted"
            }`}
          >
            {c.nombre}
          </Link>
        ))}
      </div>

      {negocios.length === 0 ? (
        <p className="mt-10 text-center text-sm opacity-80">{t("sinNegocios")}</p>
      ) : (
        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {negocios.map((n) => (
            <div key={n.id} className="flex flex-col gap-2 rounded-2xl border border-border p-5">
              <span className="text-xs font-medium uppercase tracking-wide text-secondary">
                {t(`categorias.${n.categoriaCodigo}`)}
              </span>
              <h3 className="text-lg font-semibold">{n.nombre}</h3>
              {n.descripcion && <p className="text-sm opacity-75">{n.descripcion}</p>}
              <div className="mt-1 space-y-1 text-sm opacity-80">
                {n.direccion && <p>📍 {n.direccion}</p>}
                {n.horario && <p>🕐 {n.horario}</p>}
              </div>
              {/* Contacto directo (RF-110): la transacción ocurre fuera del sistema */}
              <a
                href={`https://wa.me/${n.whatsapp}?text=${mensaje}`}
                target="_blank"
                rel="noopener noreferrer"
                className="mt-auto inline-flex min-h-11 items-center justify-center gap-2 rounded-full bg-emerald-600 font-medium text-white hover:opacity-90"
              >
                💬 {t("contactarWhatsApp")}
              </a>
            </div>
          ))}
        </div>
      )}
    </main>
  );
}
