import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { notFound } from "next/navigation";
import { NextIntlClientProvider, hasLocale } from "next-intl";
import { getTranslations, setRequestLocale } from "next-intl/server";
import { routing } from "@/i18n/routing";
import { SITIO_URL } from "@/lib/sitio";
import BarraInferior from "@/components/BarraInferior";
import ProveedorSesion from "@/components/ProveedorSesion";
import "../globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export function generateStaticParams() {
  return routing.locales.map((locale) => ({ locale }));
}

export async function generateMetadata({
  params,
}: {
  params: Promise<{ locale: string }>;
}): Promise<Metadata> {
  const { locale } = await params;
  const t = await getTranslations({ locale, namespace: "Metadata" });
  return {
    metadataBase: new URL(SITIO_URL),
    title: t("titulo"),
    description: t("descripcion"),
    alternates: {
      languages: { es: "/es", en: "/en" },
    },
    openGraph: {
      type: "website",
      siteName: t("titulo"),
      title: t("titulo"),
      description: t("descripcion"),
      locale: locale === "es" ? "es_PE" : "en_US",
    },
    twitter: {
      card: "summary",
      title: t("titulo"),
      description: t("descripcion"),
    },
  };
}

export default async function LocaleLayout({
  children,
  params,
}: {
  children: React.ReactNode;
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  if (!hasLocale(routing.locales, locale)) {
    notFound();
  }
  // Habilita el renderizado estático (SSG/ISR) por locale
  setRequestLocale(locale);

  return (
    <html lang={locale}>
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        {/* Aplica el tema guardado antes del primer pintado (evita FOUC) */}
        <script
          dangerouslySetInnerHTML={{
            __html:
              'try{var t=localStorage.getItem("tema");if(t==="dark"||t==="light"){document.documentElement.dataset.theme=t}}catch(e){}',
          }}
        />
        <NextIntlClientProvider>
          <ProveedorSesion>
            {children}
            {/* Espacio para la barra inferior fija en móvil */}
            <div aria-hidden className="h-14 md:hidden" />
            <BarraInferior />
          </ProveedorSesion>
        </NextIntlClientProvider>
      </body>
    </html>
  );
}
