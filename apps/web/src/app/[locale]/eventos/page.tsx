import { getTranslations, setRequestLocale } from "next-intl/server";
import { obtenerProximosEventos } from "@/lib/api";
import AgendaProximos from "@/components/AgendaProximos";
import DuranteMiVisita from "@/components/DuranteMiVisita";
import Encabezado from "@/components/Encabezado";

/** Agenda cultural (RF-79/84/84b/85). */
export const dynamic = "force-dynamic";

export default async function PaginaEventos({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  const t = await getTranslations("Eventos");
  const proximos = await obtenerProximosEventos(locale, 30);

  return (
    <div className="mx-auto max-w-6xl px-6 py-6">
      <Encabezado />
      <h1 className="mt-8 text-3xl font-bold">{t("titulo")}</h1>
      <p className="mt-2 text-sm opacity-75">{t("subtitulo")}</p>

      <div className="mt-6">
        <DuranteMiVisita />
      </div>

      <section className="mt-10">
        <h2 className="text-xl font-semibold">{t("proximos")}</h2>
        <div className="mt-4">
          <AgendaProximos eventos={proximos} />
        </div>
      </section>
    </div>
  );
}
