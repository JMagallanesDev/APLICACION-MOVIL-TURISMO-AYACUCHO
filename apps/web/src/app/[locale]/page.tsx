import { getTranslations, setRequestLocale } from "next-intl/server";
import { Link } from "@/i18n/navigation";
import { CATEGORIAS, obtenerProximosEventos, obtenerRutas } from "@/lib/api";
import { ICONO_CATEGORIA } from "@/lib/categorias";
import AgendaDestacada from "@/components/AgendaDestacada";
import BotonTema from "@/components/BotonTema";
import BuscadorHero from "@/components/BuscadorHero";
import EventoCard from "@/components/EventoCard";
import MenuUsuario from "@/components/MenuUsuario";
import QueHagoAhora from "@/components/QueHagoAhora";
import RutasImperdibles from "@/components/RutasImperdibles";
import SelectorIdioma from "@/components/SelectorIdioma";

export default async function Home({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);

  const t = await getTranslations("Home");
  const nav = await getTranslations("Nav");
  const footer = await getTranslations("Footer");
  const tLugares = await getTranslations("Lugares");
  const tEventos = await getTranslations("Eventos");

  const [eventos, rutas] = await Promise.all([
    obtenerProximosEventos(locale, 3),
    obtenerRutas(locale),
  ]);

  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-40 border-b border-border bg-background/90 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-5 py-3">
          <Link href="/" className="text-xl font-bold text-primary">
            Huamanga
          </Link>
          <nav className="hidden items-center gap-6 text-sm font-medium md:flex">
            <Link href="/lugares" className="transition-colors hover:text-primary">
              {nav("lugares")}
            </Link>
            <Link href="/mapa" className="transition-colors hover:text-primary">
              {nav("mapa")}
            </Link>
            <Link href="/rutas" className="transition-colors hover:text-primary">
              {nav("rutas")}
            </Link>
            <Link href="/eventos" className="transition-colors hover:text-primary">
              {nav("eventos")}
            </Link>
            <Link href="/negocios" className="transition-colors hover:text-primary">
              {nav("negocios")}
            </Link>
          </nav>
          <div className="flex items-center gap-2">
            <SelectorIdioma />
            <BotonTema />
            <div className="hidden md:block">
              <MenuUsuario />
            </div>
          </div>
        </div>
      </header>

      <main className="mx-auto w-full max-w-6xl flex-1 px-5 pb-10">
        <h1 className="sr-only">{t("titulo")}</h1>

        {/* Buscador (RF-02) */}
        <section className="pt-4">
          <BuscadorHero />
        </section>

        {/* Tiles de categorías (RF-04) */}
        <section aria-label={t("categorias")} className="mt-5">
          <div className="grid grid-cols-4 gap-3 md:grid-cols-8">
            {CATEGORIAS.map((codigo) => (
              <Link
                key={codigo}
                href={{ pathname: "/lugares", query: { categoria: codigo } }}
                className="group flex min-w-0 flex-col items-center gap-1.5 rounded-2xl border border-border bg-background p-2.5 text-center shadow-sm transition-shadow hover:shadow-md"
              >
                <span
                  aria-hidden
                  className="flex h-10 w-10 items-center justify-center rounded-xl bg-muted text-xl"
                >
                  {ICONO_CATEGORIA[codigo] ?? "📍"}
                </span>
                <span className="w-full truncate text-[10px] font-bold uppercase leading-tight tracking-wide opacity-80 group-hover:text-primary group-hover:opacity-100">
                  {tLugares(`categoriasCortas.${codigo}`)}
                </span>
              </Link>
            ))}
          </div>
        </section>

        {/* ¿Qué hago ahora? (RF-08) */}
        <section className="mt-10">
          <QueHagoAhora />
        </section>

        {/* Rutas imperdibles (RF-53) */}
        <div className="mt-10">
          <RutasImperdibles rutas={rutas} />
        </div>

        {/* Agenda cultural (RF-79/84b) */}
        {eventos.length > 0 && (
          <section className="mt-10">
            <div className="flex items-end justify-between gap-3">
              <h2 className="text-xl font-bold sm:text-2xl">{t("agendaTitulo")}</h2>
              <Link href="/eventos" className="text-sm font-medium text-primary hover:underline">
                {t("verTodos")} →
              </Link>
            </div>
            <div className="mt-4 grid gap-4 lg:grid-cols-2">
              <AgendaDestacada evento={eventos[0]} />
              {eventos.length > 1 && (
                <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-1">
                  {eventos.slice(1, 3).map((e) => (
                    <EventoCard key={e.id} evento={e} />
                  ))}
                </div>
              )}
            </div>
            <p className="sr-only">{tEventos("proximos")}</p>
          </section>
        )}
      </main>

      <footer className="border-t border-border px-6 py-6 text-center text-sm opacity-75">
        {footer("hechoEn")} · {footer("proyecto")}
      </footer>
    </div>
  );
}
