import { useTranslations } from "next-intl";
import { setRequestLocale } from "next-intl/server";
import { use } from "react";
import { Link } from "@/i18n/navigation";
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
        <SelectorIdioma />
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
          <span className="inline-flex min-h-11 cursor-not-allowed items-center rounded-full border border-border px-6 font-medium opacity-80">
            {t("verMapa")}
          </span>
        </div>

        <section className="mt-10 max-w-md rounded-2xl border border-border bg-muted p-6">
          <h2 className="font-semibold text-secondary">
            {t("seccionDiferenciador")}
          </h2>
          <p className="mt-2 text-sm opacity-80">{t("textoDiferenciador")}</p>
        </section>

        <nav aria-hidden className="mt-6 flex gap-4 text-sm opacity-50">
          <span>{nav("lugares")}</span>
          <span>{nav("mapa")}</span>
          <span>{nav("eventos")}</span>
          <span>{nav("reportar")}</span>
        </nav>
      </main>

      <footer className="px-6 py-4 text-center text-sm opacity-60">
        {footer("hechoEn")} · {footer("proyecto")}
      </footer>
    </div>
  );
}
