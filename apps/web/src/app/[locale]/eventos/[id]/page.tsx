import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { getFormatter, getTranslations, setRequestLocale } from "next-intl/server";
import { Link } from "@/i18n/navigation";
import { obtenerEvento } from "@/lib/api";
import { SITIO_URL } from "@/lib/sitio";
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
  const titulo = `${tr.nombre} · Turismo Huamanga`;
  const ruta = `/eventos/${id}`;
  return {
    title: titulo,
    description: tr.descripcion ?? undefined,
    alternates: {
      canonical: `/${locale}${ruta}`,
      languages: { es: `/es${ruta}`, en: `/en${ruta}` },
    },
    openGraph: {
      title: titulo,
      description: tr.descripcion ?? undefined,
      url: `/${locale}${ruta}`,
      ...(evento.portadaUrl && { images: [{ url: evento.portadaUrl }] }),
    },
    twitter: {
      card: evento.portadaUrl ? "summary_large_image" : "summary",
      title: titulo,
      description: tr.descripcion ?? undefined,
    },
  };
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

  // Datos estructurados schema.org (SEO): agenda como Event
  const jsonLd = {
    "@context": "https://schema.org",
    "@type": "Event",
    name: tr.nombre,
    ...(tr.descripcion && { description: tr.descripcion }),
    startDate: evento.fechaInicio,
    endDate: evento.fechaFin,
    url: `${SITIO_URL}/${locale}/eventos/${evento.id}`,
    eventAttendanceMode: "https://schema.org/OfflineEventAttendanceMode",
    location: {
      "@type": "Place",
      name: "Huamanga, Ayacucho",
      address: {
        "@type": "PostalAddress",
        addressLocality: "Ayacucho",
        addressRegion: "Ayacucho",
        addressCountry: "PE",
      },
    },
    ...(evento.portadaUrl && { image: [evento.portadaUrl] }),
    ...(tr.organizador && {
      organizer: { "@type": "Organization", name: tr.organizador },
    }),
  };

  return (
    <main className="mx-auto max-w-3xl px-6 py-6">
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />
      <Encabezado />
      <nav className="mt-6 text-sm opacity-80">
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
          <p className="mt-2 text-sm opacity-80">
            {t("organiza")}: {tr.organizador}
          </p>
        )}

        {tr.descripcion && <p className="mt-6 leading-relaxed">{tr.descripcion}</p>}

        {evento.recurrenteAnual && (
          <p className="mt-6 rounded-xl bg-muted px-4 py-3 text-sm">🔁 {t("recurrente")}</p>
        )}
      </article>
    </main>
  );
}
