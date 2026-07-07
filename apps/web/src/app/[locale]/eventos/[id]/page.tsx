import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { getFormatter, getTranslations, setRequestLocale } from "next-intl/server";
import { Link } from "@/i18n/navigation";
import { obtenerEvento } from "@/lib/api";
import Encabezado from "@/components/Encabezado";

function traduccion(
  evento: NonNullable<Awaited<ReturnType<typeof obtenerEvento>>>,
  locale: string,
) {
  return (
    evento.traducciones.find((x) => x.idioma === locale) ??
    evento.traducciones.find((x) => x.idioma === "es") ??
    evento.traducciones[0]
  );
}

export async function generateMetadata({
  params,
}: {
  params: Promise<{ locale: string; id: string }>;
}): Promise<Metadata> {
  const { locale, id } = await params;
  const evento = await obtenerEvento(id);
  if (!evento) return {};
  const tr = traduccion(evento, locale);
  return { title: `${tr.nombre} · Turismo Huamanga`, description: tr.descripcion ?? undefined };
}

export default async function PaginaEvento({
  params,
}: {
  params: Promise<{ locale: string; id: string }>;
}) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  const t = await getTranslations("Eventos");
  const format = await getFormatter();

  const evento = await obtenerEvento(id);
  if (!evento) notFound();
  const tr = traduccion(evento, locale);

  const inicio = new Date(evento.fechaInicio + "T00:00:00");
  const fin = new Date(evento.fechaFin + "T00:00:00");
  const mismoDia = evento.fechaInicio === evento.fechaFin;

  return (
    <div className="mx-auto max-w-3xl px-6 py-6">
      <Encabezado />
      <nav className="mt-6 text-sm opacity-70">
        <Link href="/eventos" className="hover:underline">
          ← {t("volver")}
        </Link>
      </nav>

      <article className="mt-4">
        <span className="text-xs font-medium uppercase tracking-wide text-secondary">
          {t(`tipos.${evento.tipo}`)}
        </span>
        <h1 className="mt-2 text-3xl font-bold">{tr.nombre}</h1>

        <div className="mt-4 flex flex-wrap items-center gap-4 text-sm">
          <span className="font-medium">
            📅{" "}
            {mismoDia
              ? format.dateTime(inicio, { weekday: "long", day: "numeric", month: "long", year: "numeric" })
              : `${format.dateTime(inicio, { day: "numeric", month: "long" })} – ${format.dateTime(fin, { day: "numeric", month: "long", year: "numeric" })}`}
          </span>
          {evento.lugarSlug && (
            <Link
              href={`/lugares/${evento.lugarSlug}`}
              className="font-medium text-secondary hover:underline"
            >
              {t("verLugar")} →
            </Link>
          )}
        </div>

        {tr.organizador && (
          <p className="mt-2 text-sm opacity-70">
            {t("organiza")}: {tr.organizador}
          </p>
        )}

        {tr.descripcion && <p className="mt-6 leading-relaxed">{tr.descripcion}</p>}

        {evento.recurrenteAnual && (
          <p className="mt-6 rounded-xl bg-muted px-4 py-3 text-sm">🔁 {t("recurrente")}</p>
        )}
      </article>
    </div>
  );
}
