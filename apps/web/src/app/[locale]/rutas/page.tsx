import type { Metadata } from "next";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { Link } from "@/i18n/navigation";
import { obtenerRutas } from "@/lib/api";
import Encabezado from "@/components/Encabezado";

export async function generateMetadata({
  params,
}: {
  params: Promise<{ locale: string }>;
}): Promise<Metadata> {
  const { locale } = await params;
  const t = await getTranslations({ locale, namespace: "Rutas" });
  return { title: `${t("titulo")} · Turismo Huamanga`, description: t("subtitulo") };
}

/** Rutas temáticas (RF-53): recorridos sugeridos con paradas en orden. */
export default async function PaginaRutas({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  const t = await getTranslations("Rutas");
  const rutas = await obtenerRutas(locale);

  return (
    <main className="mx-auto max-w-3xl px-6 py-6">
      <Encabezado />

      <h1 className="mt-8 text-3xl font-bold">{t("titulo")}</h1>
      <p className="mt-2 opacity-80">{t("subtitulo")}</p>

      {rutas.length === 0 ? (
        <p className="mt-10 text-center opacity-80">{t("sinRutas")}</p>
      ) : (
        <div className="mt-8 flex flex-col gap-6">
          {rutas.map((ruta) => (
            <article
              key={ruta.id}
              className="overflow-hidden rounded-2xl border border-border"
            >
              <div
                className="h-2 w-full"
                style={{ background: ruta.colorHex ?? "#7b1e3c" }}
                aria-hidden
              />
              <div className="p-6">
                <h2 className="text-xl font-semibold">{ruta.nombre}</h2>
                {ruta.descripcion && (
                  <p className="mt-2 text-sm opacity-85">{ruta.descripcion}</p>
                )}
                <div className="mt-3 flex flex-wrap gap-2 text-xs font-medium">
                  {ruta.duracionEstimadaMin != null && (
                    <span className="rounded-full bg-muted px-2.5 py-1">
                      {t("horas", { horas: (ruta.duracionEstimadaMin / 60).toFixed(1) })}
                    </span>
                  )}
                  <span className="rounded-full bg-muted px-2.5 py-1">
                    {t("paradas", { total: ruta.totalLugares })}
                  </span>
                </div>

                <ol className="mt-5 flex flex-col gap-1">
                  {ruta.lugares.map((parada, i) => (
                    <li key={parada.slug} className="flex items-center gap-3">
                      <span
                        aria-hidden
                        className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-bold text-primary-foreground"
                      >
                        {i + 1}
                      </span>
                      <Link
                        href={`/lugares/${parada.slug}`}
                        className="min-h-9 flex-1 rounded-xl px-2 py-1.5 font-medium hover:bg-muted hover:text-primary"
                      >
                        {parada.nombre}
                      </Link>
                    </li>
                  ))}
                </ol>
              </div>
            </article>
          ))}
        </div>
      )}
    </main>
  );
}
