import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { Link } from "@/i18n/navigation";
import { obtenerLugar, obtenerResenas, traduccionDe, type Horario } from "@/lib/api";
import BadgeAbierto from "@/components/BadgeAbierto";
import DistanciaAPie from "@/components/DistanciaAPie";
import Estrellas from "@/components/Estrellas";
import SelectorIdioma from "@/components/SelectorIdioma";

export async function generateMetadata({
  params,
}: {
  params: Promise<{ locale: string; slug: string }>;
}): Promise<Metadata> {
  const { locale, slug } = await params;
  const lugar = await obtenerLugar(slug);
  if (!lugar) {
    return {};
  }
  const traduccion = traduccionDe(lugar, locale);
  return {
    title: `${traduccion.nombre} · Turismo Huamanga`,
    description: traduccion.descripcion ?? undefined,
  };
}

/** Agrupa turnos por día para la grilla semanal. */
function turnosPorDia(horarios: Horario[]): Map<number, Horario[]> {
  const mapa = new Map<number, Horario[]>();
  for (const h of horarios) {
    const lista = mapa.get(h.diaSemana) ?? [];
    lista.push(h);
    mapa.set(h.diaSemana, lista);
  }
  return mapa;
}

const hhmm = (hora: string) => hora.slice(0, 5);

/** Ficha completa del lugar (RF-09/09b/09c/09d). */
export default async function PaginaLugar({
  params,
}: {
  params: Promise<{ locale: string; slug: string }>;
}) {
  const { locale, slug } = await params;
  setRequestLocale(locale);
  const t = await getTranslations("Lugares");

  const lugar = await obtenerLugar(slug);
  if (!lugar) {
    notFound();
  }
  const resenas = await obtenerResenas(slug);
  const traduccion = traduccionDe(lugar, locale);
  const dias = turnosPorDia(lugar.horarios);
  const antesDeIr: Array<[string, boolean | null]> = [
    ["aceptaTarjeta", lugar.aceptaTarjeta],
    ["tieneBanos", lugar.tieneBanos],
    ["accesibleSillaRuedas", lugar.accesibleSillaRuedas],
    ["aptoNinos", lugar.aptoNinos],
    ["requiereGuia", lugar.requiereGuia],
  ];

  return (
    <div className="mx-auto max-w-3xl px-6 py-6">
      <header className="flex items-center justify-between gap-4">
        <Link href="/" className="text-lg font-semibold text-primary">
          Turismo Huamanga
        </Link>
        <SelectorIdioma />
      </header>

      <nav className="mt-6 text-sm opacity-70">
        <Link href="/lugares" className="hover:underline">
          ← {t("volverAlListado")}
        </Link>
      </nav>

      <article className="mt-4">
        <div className="flex flex-wrap items-center gap-3">
          <span className="text-xs font-medium uppercase tracking-wide text-secondary">
            {t(`categorias.${lugar.categoriaCodigo}`)}
          </span>
          <BadgeAbierto abierto={lugar.abiertoAhora} />
        </div>

        <h1 className="mt-2 text-3xl font-bold">{traduccion.nombre}</h1>

        <div className="mt-1">
          <Estrellas promedio={lugar.calificacionPromedio} total={lugar.totalResenas} />
        </div>

        {lugar.direccion && <p className="mt-1 text-sm opacity-70">{lugar.direccion}</p>}

        <div className="mt-4 flex flex-wrap items-center gap-4 text-sm">
          <span className="font-medium">
            {lugar.precioEntradaPen == null || Number(lugar.precioEntradaPen) === 0
              ? t("gratis")
              : `S/ ${Number(lugar.precioEntradaPen).toFixed(2)}`}
          </span>
          {lugar.duracionVisitaMin != null && (
            <span>{t("duracionMin", { min: lugar.duracionVisitaMin })}</span>
          )}
          <a
            href={`https://www.google.com/maps/dir/?api=1&destination=${lugar.latitud},${lugar.longitud}`}
            target="_blank"
            rel="noopener noreferrer"
            className="font-medium text-secondary hover:underline"
          >
            {t("comoLlegar")} ↗
          </a>
        </div>

        <div className="mt-4">
          <DistanciaAPie latitud={lugar.latitud} longitud={lugar.longitud} />
        </div>

        {traduccion.descripcion && (
          <p className="mt-6 leading-relaxed">{traduccion.descripcion}</p>
        )}

        {traduccion.historia && (
          <section className="mt-6">
            <h2 className="text-xl font-semibold">{t("historia")}</h2>
            <p className="mt-2 leading-relaxed opacity-90">{traduccion.historia}</p>
          </section>
        )}

        {/* Horarios estructurados (RF-09b) */}
        {lugar.horarios.length > 0 && (
          <section className="mt-6">
            <h2 className="text-xl font-semibold">{t("horarios")}</h2>
            <ul className="mt-2 divide-y divide-border rounded-2xl border border-border">
              {Array.from({ length: 7 }, (_, dia) => {
                const turnos = dias.get(dia);
                return (
                  <li key={dia} className="flex justify-between gap-4 px-4 py-2 text-sm">
                    <span className="font-medium">{t(`dias.${dia}`)}</span>
                    <span className="text-right opacity-80">
                      {!turnos || turnos.every((x) => x.cerrado)
                        ? t("cerrado")
                        : turnos
                            .filter((x) => !x.cerrado)
                            .map((x) => `${hhmm(x.horaApertura!)}–${hhmm(x.horaCierre!)}`)
                            .join(", ")}
                    </span>
                  </li>
                );
              })}
            </ul>
          </section>
        )}

        {/* Bloque "Antes de ir" (RF-09d) */}
        <section className="mt-6">
          <h2 className="text-xl font-semibold">{t("antesDeIr")}</h2>
          <ul className="mt-2 grid gap-2 sm:grid-cols-2">
            {antesDeIr
              .filter(([, valor]) => valor !== null)
              .map(([clave, valor]) => (
                <li
                  key={clave}
                  className="flex items-center gap-2 rounded-xl border border-border px-4 py-2 text-sm"
                >
                  <span aria-hidden>{valor ? "✓" : "✕"}</span>
                  <span className={valor ? "" : "opacity-60"}>{t(`practicos.${clave}`)}</span>
                </li>
              ))}
            {lugar.costoTaxiDesdePlazaPen != null && Number(lugar.costoTaxiDesdePlazaPen) > 0 && (
              <li className="flex items-center gap-2 rounded-xl border border-border px-4 py-2 text-sm">
                <span aria-hidden>🚕</span>
                {t("taxiDesdePlaza", {
                  costo: Number(lugar.costoTaxiDesdePlazaPen).toFixed(2),
                })}
              </li>
            )}
          </ul>
          {traduccion.consejos && (
            <p className="mt-3 rounded-xl bg-muted px-4 py-3 text-sm">
              💡 {traduccion.consejos}
            </p>
          )}
        </section>

        {/* Reseñas de la comunidad (RF-37) */}
        <section className="mt-8">
          <h2 className="text-xl font-semibold">{t("resenasTitulo")}</h2>
          {resenas.length === 0 ? (
            <p className="mt-2 text-sm opacity-70">{t("sinResenas")}</p>
          ) : (
            <ul className="mt-3 space-y-3">
              {resenas.map((resena) => (
                <li key={resena.id} className="rounded-2xl border border-border p-4">
                  <div className="flex items-center justify-between gap-3 text-sm">
                    <span className="font-medium">{resena.autorNombre}</span>
                    <span aria-label={`${resena.calificacion}/5`} className="text-accent">
                      {"★".repeat(resena.calificacion)}
                      <span className="opacity-30">{"★".repeat(5 - resena.calificacion)}</span>
                    </span>
                  </div>
                  {resena.comentario && (
                    <p className="mt-2 text-sm opacity-85">{resena.comentario}</p>
                  )}
                </li>
              ))}
            </ul>
          )}
          <p className="mt-3 text-xs opacity-60">{t("resenasProximamente")}</p>
        </section>
      </article>
    </div>
  );
}
