import Image from "next/image";
import { getTranslations } from "next-intl/server";
import { Link } from "@/i18n/navigation";
import type { Ruta } from "@/lib/api";

/**
 * Carrusel horizontal de rutas temáticas (RF-53): en móvil se desliza con
 * scroll-snap, en escritorio es una grilla. La primera ruta lleva la foto
 * del centro histórico; el resto usa su color de seed.
 */
export default async function RutasImperdibles({ rutas }: { rutas: Ruta[] }) {
  const t = await getTranslations("Rutas");

  if (rutas.length === 0) {
    return null;
  }

  return (
    <section>
      <div className="flex items-end justify-between gap-3">
        <h2 className="text-xl font-bold sm:text-2xl">{t("titulo")}</h2>
        <Link href="/rutas" className="text-sm font-medium text-primary hover:underline">
          {t("verTodas")} →
        </Link>
      </div>

      <div className="hide-scrollbar mt-4 flex snap-x snap-mandatory gap-4 overflow-x-auto pb-2 md:grid md:grid-cols-2 md:overflow-visible lg:grid-cols-3">
        {rutas.map((ruta, i) => (
          <Link
            key={ruta.id}
            href="/rutas"
            className="group flex w-72 shrink-0 snap-start flex-col overflow-hidden rounded-2xl border border-border bg-background shadow-sm transition-shadow hover:shadow-md md:w-auto"
          >
            <div className="relative h-36 w-full overflow-hidden">
              {i === 0 ? (
                <Image
                  src="/hero-huamanga.jpg"
                  alt=""
                  fill
                  sizes="(max-width: 768px) 288px, 33vw"
                  className="object-cover transition-transform duration-500 group-hover:scale-105"
                />
              ) : (
                <div
                  aria-hidden
                  className="h-full w-full"
                  style={{
                    background: `linear-gradient(135deg, ${ruta.colorHex ?? "#7b1e3c"}, ${ruta.colorHex ?? "#7b1e3c"}66)`,
                  }}
                />
              )}
            </div>
            <div className="flex flex-1 flex-col gap-2 p-4">
              <h3 className="font-semibold leading-tight group-hover:text-primary">
                {ruta.nombre}
              </h3>
              {ruta.descripcion && (
                <p className="line-clamp-2 text-sm opacity-80">{ruta.descripcion}</p>
              )}
              <div className="mt-auto flex flex-wrap gap-2 pt-2 text-xs font-medium">
                {ruta.duracionEstimadaMin != null && (
                  <span className="rounded-full bg-muted px-2.5 py-1">
                    {t("horas", { horas: (ruta.duracionEstimadaMin / 60).toFixed(1) })}
                  </span>
                )}
                <span className="rounded-full bg-muted px-2.5 py-1">
                  {t("paradas", { total: ruta.totalLugares })}
                </span>
              </div>
            </div>
          </Link>
        ))}
      </div>
    </section>
  );
}
