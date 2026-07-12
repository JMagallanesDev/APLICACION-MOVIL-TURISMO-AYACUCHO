"use client";

import { useEffect, useState } from "react";
import { Link } from "@/i18n/navigation";
import { obtenerMetricas, type Metricas } from "@/lib/apiClienteAdmin";
import { useSesion } from "@/stores/sesion";

/** Dashboard con totales y colas de moderación (RF-52). Panel interno en español. */
export default function DashboardAdmin() {
  const usuario = useSesion((s) => s.usuario);
  const [metricas, setMetricas] = useState<Metricas | null>(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (usuario?.rol !== "ADMIN") return;
    obtenerMetricas().then(setMetricas).catch(() => setError(true));
  }, [usuario]);

  if (error) {
    return <p className="text-sm text-red-600">No se pudieron cargar las métricas.</p>;
  }
  if (!metricas) {
    return (
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="h-24 animate-pulse rounded-2xl bg-muted" />
        ))}
      </div>
    );
  }

  const tarjetas = [
    { etiqueta: "Lugares publicados", valor: metricas.lugaresPublicados },
    { etiqueta: "Usuarios registrados", valor: metricas.usuariosRegistrados },
    { etiqueta: "Reseñas publicadas", valor: metricas.resenasPublicadas },
    { etiqueta: "Incidentes en el mapa", valor: metricas.reportesAprobados },
  ];
  const colas = [
    { etiqueta: "Reportes por revisar", valor: metricas.reportesRecibidos },
    { etiqueta: "Fotos por revisar", valor: metricas.fotosPendientes },
    { etiqueta: "Reseñas por revisar", valor: metricas.resenasEnRevision },
    { etiqueta: "Negocios por aprobar", valor: metricas.negociosPendientes },
  ];

  return (
    <div className="flex flex-col gap-8">
      <section>
        <h1 className="text-2xl font-bold">Panel de administración</h1>
        <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {tarjetas.map((c) => (
            <div key={c.etiqueta} className="rounded-2xl border border-border p-5">
              <p className="text-3xl font-bold text-primary">{c.valor}</p>
              <p className="mt-1 text-sm opacity-70">{c.etiqueta}</p>
            </div>
          ))}
        </div>
      </section>

      <section>
        <h2 className="text-lg font-semibold">Pendientes de moderación</h2>
        <div className="mt-3 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {colas.map((c) => (
            <Link
              key={c.etiqueta}
              href="/admin/moderacion"
              className={`rounded-2xl border p-5 transition-colors hover:bg-muted ${
                c.valor > 0 ? "border-accent bg-accent/5" : "border-border"
              }`}
            >
              <p className="text-3xl font-bold">{c.valor}</p>
              <p className="mt-1 text-sm opacity-70">{c.etiqueta}</p>
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
}
