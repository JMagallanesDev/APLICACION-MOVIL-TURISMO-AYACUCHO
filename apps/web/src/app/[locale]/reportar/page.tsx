import { getTranslations, setRequestLocale } from "next-intl/server";
import { obtenerTiposIncidente } from "@/lib/api";
import Encabezado from "@/components/Encabezado";
import FormularioReporte from "@/components/FormularioReporte";

/** Reporte ciudadano de atentados al patrimonio (módulo diferenciador). */
export const dynamic = "force-dynamic";

export default async function PaginaReportar({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  const t = await getTranslations("Reportar");
  const tipos = await obtenerTiposIncidente(locale);

  return (
    <div className="mx-auto max-w-2xl px-6 py-6">
      <Encabezado />
      <main className="mt-8">
        <h1 className="text-3xl font-bold">{t("titulo")}</h1>
        <p className="mt-2 text-sm opacity-75">{t("subtitulo")}</p>
        <FormularioReporte tipos={tipos} />
      </main>
    </div>
  );
}
