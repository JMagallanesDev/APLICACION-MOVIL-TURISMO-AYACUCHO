"use client";

import { useCallback, useEffect, useState } from "react";
import {
  listarFotosPendientes,
  listarNegociosPendientes,
  listarResenasEnRevision,
  listarReportes,
  moderarFoto,
  moderarNegocio,
  moderarResena,
  moderarReporte,
  type FotoModeracion,
  type NegocioModeracion,
  type ResenaModeracion,
  type ReporteModeracion,
} from "@/lib/apiClienteAdmin";
import { useSesion } from "@/stores/sesion";

/** Colas de moderación (RF-49 fotos, RF-50 reseñas, RF-76 reportes). Panel interno en español. */

type Pestana = "reportes" | "fotos" | "resenas" | "negocios";

const TIPO_INCIDENTE: Record<string, string> = {
  VANDALISMO: "Vandalismo",
  GRAFITI: "Grafiti",
  DANO_ESTRUCTURAL: "Daño estructural",
  BASURA_ACUMULADA: "Basura acumulada",
  COMERCIO_INFORMAL: "Comercio informal",
  CONSTRUCCION_ILEGAL: "Construcción ilegal",
  OTRO: "Otro",
};

const PESTANAS: { clave: Pestana; etiqueta: string }[] = [
  { clave: "reportes", etiqueta: "Reportes ciudadanos" },
  { clave: "fotos", etiqueta: "Fotos" },
  { clave: "resenas", etiqueta: "Reseñas" },
  { clave: "negocios", etiqueta: "Negocios" },
];

const CATEGORIA_NEGOCIO: Record<string, string> = {
  RESTAURANTES: "Restaurantes",
  HOSPEDAJES: "Hospedajes",
  ARTESANIAS: "Artesanías",
  CAFETERIAS: "Cafeterías",
  AGENCIAS_TURISMO: "Agencias de turismo",
  TRANSPORTE: "Transporte",
  ENTRETENIMIENTO: "Entretenimiento",
};

export default function ModeracionAdmin() {
  const usuario = useSesion((s) => s.usuario);
  const [pestana, setPestana] = useState<Pestana>("reportes");
  const [reportes, setReportes] = useState<ReporteModeracion[]>([]);
  const [fotos, setFotos] = useState<FotoModeracion[]>([]);
  const [resenas, setResenas] = useState<ResenaModeracion[]>([]);
  const [negocios, setNegocios] = useState<NegocioModeracion[]>([]);
  const [cargando, setCargando] = useState(true);

  const recargar = useCallback(async () => {
    if (usuario?.rol !== "ADMIN") return;
    try {
      if (pestana === "reportes") setReportes(await listarReportes("RECIBIDO"));
      else if (pestana === "fotos") setFotos(await listarFotosPendientes());
      else if (pestana === "negocios") setNegocios(await listarNegociosPendientes());
      else setResenas(await listarResenasEnRevision());
    } finally {
      setCargando(false);
    }
  }, [pestana, usuario]);

  useEffect(() => {
    recargar();
  }, [recargar]);

  function cambiarPestana(p: Pestana) {
    setCargando(true);
    setPestana(p);
  }

  return (
    <div>
      <h1 className="text-2xl font-bold">Moderación</h1>

      <div className="mt-4 flex gap-2">
        {PESTANAS.map((p) => (
          <button
            key={p.clave}
            type="button"
            onClick={() => cambiarPestana(p.clave)}
            aria-pressed={pestana === p.clave}
            className={`min-h-9 rounded-full px-4 text-sm font-medium transition-colors ${
              pestana === p.clave
                ? "bg-primary text-primary-foreground"
                : "border border-border hover:bg-muted"
            }`}
          >
            {p.etiqueta}
          </button>
        ))}
      </div>

      <div className="mt-6">
        {cargando ? (
          <div className="h-24 animate-pulse rounded-2xl bg-muted" />
        ) : pestana === "reportes" ? (
          <ColaReportes items={reportes} onAccion={recargar} />
        ) : pestana === "fotos" ? (
          <ColaFotos items={fotos} onAccion={recargar} />
        ) : pestana === "negocios" ? (
          <ColaNegocios items={negocios} onAccion={recargar} />
        ) : (
          <ColaResenas items={resenas} onAccion={recargar} />
        )}
      </div>
    </div>
  );
}

function Vacio({ texto }: { texto: string }) {
  return <p className="rounded-2xl border border-border p-6 text-center text-sm opacity-70">{texto}</p>;
}

function ColaReportes({ items, onAccion }: { items: ReporteModeracion[]; onAccion: () => void }) {
  if (items.length === 0) return <Vacio texto="No hay reportes pendientes. ¡Todo al día!" />;

  async function accion(id: string, estado: string) {
    await moderarReporte(id, estado);
    onAccion();
  }

  return (
    <ul className="flex flex-col gap-3">
      {items.map((r) => (
        <li key={r.id} className="rounded-2xl border border-border p-4">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <span className="text-sm font-semibold text-secondary">
              {TIPO_INCIDENTE[r.tipoIncidenteCodigo] ?? r.tipoIncidenteCodigo}
            </span>
            <span className="text-xs opacity-60">
              {r.esAnonimo ? "Anónimo · " : ""}
              {new Date(r.creadoEn).toLocaleDateString("es-PE")}
            </span>
          </div>
          <p className="mt-2 text-sm">{r.descripcion}</p>
          {r.direccionReferencial && (
            <p className="mt-1 text-xs opacity-60">📍 {r.direccionReferencial}</p>
          )}
          {r.fotos.length > 0 && (
            <div className="mt-2 flex gap-2 overflow-x-auto">
              {r.fotos.map((url) => (
                // eslint-disable-next-line @next/next/no-img-element
                <img key={url} src={url} alt="" className="h-20 w-20 rounded-lg object-cover" />
              ))}
            </div>
          )}
          <div className="mt-3 flex flex-wrap gap-2">
            <button
              type="button"
              onClick={() => accion(r.id, "APROBADO")}
              className="min-h-9 rounded-full bg-emerald-600 px-4 text-sm font-medium text-white hover:opacity-90"
            >
              Aprobar
            </button>
            <button
              type="button"
              onClick={() => accion(r.id, "RESUELTO")}
              className="min-h-9 rounded-full border border-border px-4 text-sm font-medium hover:bg-muted"
            >
              Marcar resuelto
            </button>
            <button
              type="button"
              onClick={() => accion(r.id, "DESCARTADO")}
              className="min-h-9 rounded-full border border-border px-4 text-sm font-medium text-red-600 hover:bg-muted"
            >
              Descartar
            </button>
          </div>
        </li>
      ))}
    </ul>
  );
}

function ColaFotos({ items, onAccion }: { items: FotoModeracion[]; onAccion: () => void }) {
  if (items.length === 0) return <Vacio texto="No hay fotos pendientes de revisión." />;

  async function accion(id: string, aprobar: boolean) {
    await moderarFoto(id, aprobar);
    onAccion();
  }

  return (
    <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {items.map((f) => (
        <li key={f.id} className="overflow-hidden rounded-2xl border border-border">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={f.url} alt="" className="h-40 w-full object-cover" />
          <div className="p-3">
            <p className="text-xs opacity-60">{f.autorNombre}</p>
            <div className="mt-2 flex gap-2">
              <button
                type="button"
                onClick={() => accion(f.id, true)}
                className="min-h-9 flex-1 rounded-full bg-emerald-600 text-sm font-medium text-white hover:opacity-90"
              >
                Aprobar
              </button>
              <button
                type="button"
                onClick={() => accion(f.id, false)}
                className="min-h-9 flex-1 rounded-full border border-border text-sm font-medium text-red-600 hover:bg-muted"
              >
                Rechazar
              </button>
            </div>
          </div>
        </li>
      ))}
    </ul>
  );
}

function ColaNegocios({ items, onAccion }: { items: NegocioModeracion[]; onAccion: () => void }) {
  if (items.length === 0) return <Vacio texto="No hay negocios pendientes de aprobación." />;

  async function accion(id: string, estado: string) {
    await moderarNegocio(id, estado);
    onAccion();
  }

  return (
    <ul className="flex flex-col gap-3">
      {items.map((n) => (
        <li key={n.id} className="rounded-2xl border border-border p-4">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <span className="font-medium">{n.nombre}</span>
            <span className="text-xs opacity-60">
              {CATEGORIA_NEGOCIO[n.categoriaCodigo] ?? n.categoriaCodigo}
            </span>
          </div>
          {n.descripcion && <p className="mt-2 text-sm opacity-85">{n.descripcion}</p>}
          <p className="mt-1 text-xs opacity-60">
            💬 {n.whatsapp}
            {n.direccion ? ` · 📍 ${n.direccion}` : ""}
          </p>
          <div className="mt-3 flex flex-wrap gap-2">
            <button
              type="button"
              onClick={() => accion(n.id, "APROBADO")}
              className="min-h-9 rounded-full bg-emerald-600 px-4 text-sm font-medium text-white hover:opacity-90"
            >
              Aprobar
            </button>
            <button
              type="button"
              onClick={() => accion(n.id, "RECHAZADO")}
              className="min-h-9 rounded-full border border-border px-4 text-sm font-medium text-red-600 hover:bg-muted"
            >
              Rechazar
            </button>
          </div>
        </li>
      ))}
    </ul>
  );
}

function ColaResenas({ items, onAccion }: { items: ResenaModeracion[]; onAccion: () => void }) {
  if (items.length === 0) return <Vacio texto="No hay reseñas en revisión." />;

  async function ocultar(id: string) {
    await moderarResena(id, true);
    onAccion();
  }

  return (
    <ul className="flex flex-col gap-3">
      {items.map((r) => (
        <li key={r.id} className="rounded-2xl border border-border p-4">
          <div className="flex items-center justify-between gap-3 text-sm">
            <span className="font-medium">{r.autorNombre}</span>
            <span className="text-accent">{"★".repeat(r.calificacion)}</span>
          </div>
          {r.comentario && <p className="mt-2 text-sm opacity-85">{r.comentario}</p>}
          <button
            type="button"
            onClick={() => ocultar(r.id)}
            className="mt-3 min-h-9 rounded-full border border-border px-4 text-sm font-medium text-red-600 hover:bg-muted"
          >
            Ocultar
          </button>
        </li>
      ))}
    </ul>
  );
}
