import { useTranslations } from "next-intl";

/** Badge abierto/cerrado ahora (RF-09b); null = sin horarios, no se muestra. */
export default function BadgeAbierto({ abierto }: { abierto: boolean | null }) {
  const t = useTranslations("Lugares");
  if (abierto === null) {
    return null;
  }
  return (
    <span
      className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium ${
        abierto
          ? "bg-emerald-600/15 text-emerald-800 dark:text-emerald-400"
          : "bg-red-600/15 text-red-800 dark:text-red-400"
      }`}
    >
      <span
        aria-hidden
        className={`h-1.5 w-1.5 rounded-full ${abierto ? "bg-emerald-600" : "bg-red-600"}`}
      />
      {abierto ? t("abiertoAhora") : t("cerradoAhora")}
    </span>
  );
}
