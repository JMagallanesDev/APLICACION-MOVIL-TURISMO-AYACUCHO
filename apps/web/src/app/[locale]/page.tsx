import { useTranslations } from "next-intl";
import { setRequestLocale } from "next-intl/server";
import { use } from "react";
import { Link } from "@/i18n/navigation";
import MenuUsuario from "@/components/MenuUsuario";
import QueHagoAhora from "@/components/QueHagoAhora";
import SelectorIdioma from "@/components/SelectorIdioma";

export default function Home({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = use(params);
  setRequestLocale(locale);

  const t = useTranslations("Home");
  const nav = useTranslations("Nav");
  const footer = useTranslations("Footer");

  return (
    <div className="flex min-h-screen flex-col">
      <header className="flex items-center justify-between px-6 py-4">
        <span className="text-lg font-semibold text-primary">
          Turismo Huamanga
        </span>
        <div className="flex items-center gap-3">
          <MenuUsuario />
          <SelectorIdioma />
        </div>
      </header>

      <main className="flex flex-1 flex-col items-center justify-center gap-6 px-6 text-center">
        <h1 className="max-w-2xl text-4xl font-bold sm:text-5xl">
          {t("titulo")}
        </h1>
        <p className="max-w-xl text-lg opacity-80">{t("subtitulo")}</p>

        <div className="mt-2 flex flex-wrap items-center justify-center gap-3">
          <Link
            href="/lugares"
            className="inline-flex min-h-11 items-center rounded-full bg-primary px-6 font-medium text-primary-foreground transition-opacity hover:opacity-90"
          >
            {t("explorarLugares")}
          </Link>
          <Link
            href="/mapa"
            className="inline-flex min-h-11 items-center rounded-full border border-border px-6 font-medium transition-colors hover:bg-muted"
          >
            {t("verMapa")}
          </Link>
        </div>

        <div className="mt-10 flex w-full justify-center">
          <QueHagoAhora />
        </div>

        <nav className="mt-6 flex flex-wrap justify-center gap-4 text-sm">
          <Link href="/lugares" className="opacity-70 hover:opacity-100 hover:underline">
            {nav("lugares")}
          </Link>
          <Link href="/mapa" className="opacity-70 hover:opacity-100 hover:underline">
            {nav("mapa")}
          </Link>
          <Link href="/reportar" className="opacity-70 hover:opacity-100 hover:underline">
            {nav("reportar")}
          </Link>
          <Link href="/eventos" className="opacity-70 hover:opacity-100 hover:underline">
            {nav("eventos")}
          </Link>
          <Link href="/negocios" className="opacity-70 hover:opacity-100 hover:underline">
            {nav("negocios")}
          </Link>
          <Link href="/mapa-incidentes" className="opacity-70 hover:opacity-100 hover:underline">
            {nav("mapaIncidentes")}
          </Link>
        </nav>
      </main>

      <footer className="px-6 py-4 text-center text-sm opacity-60">
        {footer("hechoEn")} · {footer("proyecto")}
      </footer>
    </div>
  );
}
