import { getTranslations, setRequestLocale } from "next-intl/server";
import Encabezado from "@/components/Encabezado";
import FormularioNegocio from "@/components/FormularioNegocio";

/** Registro y panel del negocio propio (RF-104/107). */
export default async function PaginaRegistrarNegocio({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  const t = await getTranslations("Negocios");

  return (
    <div className="mx-auto max-w-2xl px-6 py-6">
      <Encabezado />
      <main className="mt-8">
        <h1 className="text-3xl font-bold">{t("miNegocio")}</h1>
        <p className="mt-2 text-sm opacity-75">{t("miNegocioAyuda")}</p>
        <FormularioNegocio />
      </main>
    </div>
  );
}
