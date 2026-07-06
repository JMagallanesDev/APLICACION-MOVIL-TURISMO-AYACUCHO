import { useTranslations } from "next-intl";
import { Link } from "@/i18n/navigation";
import type { LugarResumen } from "@/lib/api";
import BadgeAbierto from "./BadgeAbierto";
import Estrellas from "./Estrellas";

/** Card del listado (RF-01) con badge abierto/cerrado (RF-09b). */
export default function LugarCard({ lugar }: { lugar: LugarResumen }) {
  const t = useTranslations("Lugares");

  return (
    <Link
      href={`/lugares/${lugar.slug}`}
      className="group flex flex-col gap-2 rounded-2xl border border-border bg-background p-5 transition-shadow hover:shadow-md"
    >
      <div className="flex items-start justify-between gap-2">
        <span className="text-xs font-medium uppercase tracking-wide text-secondary">
          {t(`categorias.${lugar.categoriaCodigo}`)}
        </span>
        <BadgeAbierto abierto={lugar.abiertoAhora} />
      </div>

      <h3 className="text-lg font-semibold group-hover:text-primary">
        {lugar.nombre}
      </h3>

      <Estrellas promedio={lugar.calificacionPromedio} total={lugar.totalResenas} />

      {lugar.descripcion && (
        <p className="line-clamp-2 text-sm opacity-75">{lugar.descripcion}</p>
      )}

      <div className="mt-auto flex flex-wrap gap-3 pt-2 text-sm opacity-80">
        <span>
          {lugar.precioEntradaPen == null || Number(lugar.precioEntradaPen) === 0
            ? t("gratis")
            : `S/ ${Number(lugar.precioEntradaPen).toFixed(2)}`}
        </span>
        {lugar.duracionVisitaMin != null && (
          <span>· {t("duracionMin", { min: lugar.duracionVisitaMin })}</span>
        )}
      </div>
    </Link>
  );
}
