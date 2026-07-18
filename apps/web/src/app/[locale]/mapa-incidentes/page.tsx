import { getTranslations, setRequestLocale } from "next-intl/server";
import { Link } from "@/i18n/navigation";
import { obtenerIncidentesMapa } from "@/lib/api";
import Encabezado from "@/components/Encabezado";
import MapaIncidentes from "@/components/MapaIncidentes";

/** Mapa público de incidentes aprobados (RF-74). */
export const dynamic = "force-dynamic";

export default async function PaginaMapaIncidentes({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  const t = await getTranslations("MapaIncidentes");
  const incidentes = await obtenerIncidentesMapa();

  return (
    <main className="mx-auto max-w-6xl px-6 py-6">
      <Encabezado />
      <div className="mt-8 flex items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold">{t("titulo")}</h1>
          <p className="mt-1 text-sm opacity-80">{t("subtitulo", { total: incidentes.length })}</p>
        </div>
        <Link
          href="/reportar"
          className="min-h-11 rounded-full bg-primary px-5 text-sm font-medium text-primary-foreground hover:opacity-90"
        >
          {t("reportar")}
        </Link>
      </div>
      <div className="mt-6">
        <MapaIncidentes incidentes={incidentes} />
      </div>
    </main>
  );
}
