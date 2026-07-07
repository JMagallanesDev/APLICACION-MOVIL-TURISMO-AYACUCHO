import { setRequestLocale } from "next-intl/server";
import { getTranslations } from "next-intl/server";
import Encabezado from "@/components/Encabezado";
import FormularioCuenta from "@/components/FormularioCuenta";

export default async function PaginaCuenta({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  const t = await getTranslations("Cuenta");

  return (
    <div className="mx-auto max-w-6xl px-6 py-6">
      <Encabezado />
      <main className="mx-auto max-w-sm text-center">
        <h1 className="mt-10 text-3xl font-bold">{t("titulo")}</h1>
        <p className="mt-2 text-sm opacity-70">{t("subtitulo")}</p>
        <FormularioCuenta />
      </main>
    </div>
  );
}
