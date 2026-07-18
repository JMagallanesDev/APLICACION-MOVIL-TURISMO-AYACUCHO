import { getTranslations, setRequestLocale } from "next-intl/server";
import { Link } from "@/i18n/navigation";
import { obtenerLugares } from "@/lib/api";
import Encabezado from "@/components/Encabezado";
import Mapa from "@/components/Mapa";

/**
 * Mapa interactivo (RF-17/18/19/22b) con todos los lugares publicados.
 * Dinámica: el badge abierto/cerrado de los marcadores depende de la hora;
 * el fetch interno igual usa el data cache de 5 min.
 */
export const dynamic = "force-dynamic";

export default async function PaginaMapa({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  const t = await getTranslations("Mapa");

  const datos = await obtenerLugares({ idioma: locale, pagina: 0, tamano: 50 });

  return (
    <main className="mx-auto max-w-6xl px-6 py-6">
      <Encabezado />

      <div className="mt-8 flex items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold">{t("titulo")}</h1>
          <p className="mt-1 text-sm opacity-80">{t("subtitulo")}</p>
        </div>
        <Link href="/lugares" className="text-sm font-medium text-secondary hover:underline">
          {t("verListado")} →
        </Link>
      </div>

      <div className="mt-6">
        <Mapa lugares={datos.content} />
      </div>
    </main>
  );
}
