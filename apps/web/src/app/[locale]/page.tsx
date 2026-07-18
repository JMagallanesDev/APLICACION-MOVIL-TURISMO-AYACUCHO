import Image from "next/image";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { Link } from "@/i18n/navigation";
import { CATEGORIAS, obtenerProximosEventos } from "@/lib/api";
import AgendaProximos from "@/components/AgendaProximos";
import BuscadorHero from "@/components/BuscadorHero";
import MenuUsuario from "@/components/MenuUsuario";
import QueHagoAhora from "@/components/QueHagoAhora";
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

  const eventos = await obtenerProximosEventos(locale, 3);

  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-40 border-b border-border bg-background/90 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-6 py-3">
          <Link href="/" className="text-lg font-semibold text-primary">
            Turismo Huamanga
          </Link>
          <nav className="hidden items-center gap-6 text-sm font-medium md:flex">
            <Link href="/lugares" className="transition-colors hover:text-primary">
              {nav("lugares")}
            </Link>
            <Link href="/mapa" className="transition-colors hover:text-primary">
              {nav("mapa")}
            </Link>
            <Link href="/eventos" className="transition-colors hover:text-primary">
              {nav("eventos")}
            </Link>
            <Link href="/negocios" className="transition-colors hover:text-primary">
              {nav("negocios")}
            </Link>
          </nav>
          <div className="flex items-center gap-3">
            <MenuUsuario />
            <SelectorIdioma />
          </div>
        </div>
      </header>

      <main className="flex-1">
        {/* Hero (identidad visual RF-90) */}
        <section className="relative flex min-h-120 items-center justify-center overflow-hidden md:min-h-140">
          <Image
            src="/hero-huamanga.jpg"
            alt=""
            fill
            priority
            sizes="100vw"
            className="object-cover"
          />
          <div
            aria-hidden
            className="absolute inset-0 bg-linear-to-b from-black/55 via-black/30 to-background"
          />
          <div className="relative z-10 flex w-full max-w-4xl flex-col items-center gap-6 px-6 py-16 text-center">
            <h1 className="text-4xl font-bold tracking-tight text-white drop-shadow-lg sm:text-6xl">
              {t("titulo")}
            </h1>
            <p className="max-w-2xl text-lg text-white/90 drop-shadow md:text-xl">
              {t("subtitulo")}
            </p>

            <BuscadorHero />

            <div className="flex flex-wrap justify-center gap-2">
              {CATEGORIAS.map((codigo) => (
                <Link
                  key={codigo}
                  href={{ pathname: "/lugares", query: { categoria: codigo } }}
                  className="rounded-full border border-white/40 bg-white/15 px-4 py-1.5 text-sm font-medium text-white backdrop-blur-sm transition-colors hover:bg-white/30"
                >
                  {tLugares(`categorias.${codigo}`)}
                </Link>
              ))}
            </div>
          </div>
        </section>

        {/* ¿Qué hago ahora? (RF-08) */}
        <section className="mx-auto w-full max-w-6xl px-6 py-14">
          <QueHagoAhora />
        </section>

        {/* Próximos eventos (RF-79) */}
        {eventos.length > 0 && (
          <section className="mx-auto w-full max-w-6xl px-6 pb-20">
            <div className="flex flex-wrap items-end justify-between gap-3">
              <h2 className="text-2xl font-bold sm:text-3xl">{tEventos("proximos")}</h2>
              <Link
                href="/eventos"
                className="text-sm font-medium text-primary hover:underline"
              >
                {t("verTodos")} →
              </Link>
            </div>
            <div className="mt-6">
              <AgendaProximos eventos={eventos} />
            </div>
          </section>
        )}
      </main>

      <footer className="border-t border-border px-6 py-6 text-center text-sm opacity-75">
        {footer("hechoEn")} · {footer("proyecto")}
      </footer>
    </div>
  );
}
