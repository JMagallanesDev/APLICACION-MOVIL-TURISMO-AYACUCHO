import { useTranslations } from "next-intl";

/**
 * Calificación promedio (RF-06/RF-37): leída de la vista materializada.
 * No se muestra nada si aún no hay reseñas.
 */
export default function Estrellas({
  promedio,
  total,
}: {
  promedio: number | null;
  total: number | null;
}) {
  const t = useTranslations("Lugares");
  if (promedio == null || !total) {
    return null;
  }
  const llenas = Math.round(promedio);
  return (
    <span
      className="inline-flex items-center gap-1 text-sm"
      aria-label={t("calificacion", { promedio: promedio.toFixed(1), total })}
    >
      <span aria-hidden className="tracking-tight text-accent">
        {"★".repeat(llenas)}
        <span className="opacity-30">{"★".repeat(5 - llenas)}</span>
      </span>
      <span className="font-medium">{Number(promedio).toFixed(1)}</span>
      <span className="opacity-60">({total})</span>
    </span>
  );
}
