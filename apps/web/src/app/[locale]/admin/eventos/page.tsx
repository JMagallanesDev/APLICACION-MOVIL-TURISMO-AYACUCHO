"use client";

import { useCallback, useEffect, useState } from "react";
import { obtenerProximosEventos, TIPOS_EVENTO, type EventoResumen } from "@/lib/api";
import {
  clonarEvento,
  crearEvento,
  eliminarEvento,
  type NuevoEvento,
} from "@/lib/apiClienteAdmin";
import { useSesion } from "@/stores/sesion";

/** Gestión de eventos del admin (RF-86). Panel interno en español. */

const TIPO_LABEL: Record<string, string> = {
  FESTIVIDAD_RELIGIOSA: "Festividad religiosa",
  CONCIERTO: "Concierto",
  FERIA: "Feria",
  GASTRONOMICO: "Gastronómico",
  CULTURAL: "Cultural",
  DEPORTIVO: "Deportivo",
  OTRO: "Otro",
};

const VACIO: NuevoEvento = {
  tipo: "CULTURAL",
  fechaInicio: "",
  fechaFin: "",
  recurrenteAnual: false,
  estado: "PUBLICADO",
  nombreEs: "",
  descripcionEs: "",
  nombreEn: "",
  descripcionEn: "",
  organizador: "",
};

export default function GestionEventos() {
  const usuario = useSesion((s) => s.usuario);
  const [eventos, setEventos] = useState<EventoResumen[]>([]);
  const [form, setForm] = useState<NuevoEvento>(VACIO);
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const recargar = useCallback(async () => {
    setEventos(await obtenerProximosEventos("es", 30));
  }, []);

  useEffect(() => {
    if (usuario?.rol !== "ADMIN") return;
    let vivo = true;
    obtenerProximosEventos("es", 30).then((r) => {
      if (vivo) setEventos(r);
    });
    return () => {
      vivo = false;
    };
  }, [usuario]);

  async function guardar(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMensaje(null);
    try {
      await crearEvento(form);
      setMensaje("Evento creado.");
      setForm(VACIO);
      recargar();
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo crear el evento.");
    }
  }

  async function clonar(id: string) {
    await clonarEvento(id, 1);
    setMensaje("Evento clonado al próximo año (queda en borrador para revisar).");
  }

  async function borrar(id: string) {
    await eliminarEvento(id);
    recargar();
  }

  const set = <K extends keyof NuevoEvento>(k: K, v: NuevoEvento[K]) =>
    setForm((f) => ({ ...f, [k]: v }));

  return (
    <div className="flex flex-col gap-8">
      <section>
        <h1 className="text-2xl font-bold">Gestión de eventos</h1>
        <form onSubmit={guardar} className="mt-4 grid gap-3 rounded-2xl border border-border p-5 sm:grid-cols-2">
          <label className="flex flex-col gap-1 text-sm sm:col-span-2">
            Nombre (español)
            <input
              required
              value={form.nombreEs}
              onChange={(e) => set("nombreEs", e.target.value)}
              className="min-h-10 rounded-lg border border-border bg-background px-3"
            />
          </label>
          <label className="flex flex-col gap-1 text-sm sm:col-span-2">
            Nombre (inglés, opcional)
            <input
              value={form.nombreEn}
              onChange={(e) => set("nombreEn", e.target.value)}
              className="min-h-10 rounded-lg border border-border bg-background px-3"
            />
          </label>
          <label className="flex flex-col gap-1 text-sm">
            Tipo
            <select
              value={form.tipo}
              onChange={(e) => set("tipo", e.target.value)}
              className="min-h-10 rounded-lg border border-border bg-background px-3"
            >
              {TIPOS_EVENTO.map((x) => (
                <option key={x} value={x}>
                  {TIPO_LABEL[x]}
                </option>
              ))}
            </select>
          </label>
          <label className="flex flex-col gap-1 text-sm">
            Estado
            <select
              value={form.estado}
              onChange={(e) => set("estado", e.target.value)}
              className="min-h-10 rounded-lg border border-border bg-background px-3"
            >
              <option value="PUBLICADO">Publicado</option>
              <option value="BORRADOR">Borrador</option>
            </select>
          </label>
          <label className="flex flex-col gap-1 text-sm">
            Fecha inicio
            <input
              type="date"
              required
              value={form.fechaInicio}
              onChange={(e) => set("fechaInicio", e.target.value)}
              className="min-h-10 rounded-lg border border-border bg-background px-3"
            />
          </label>
          <label className="flex flex-col gap-1 text-sm">
            Fecha fin
            <input
              type="date"
              required
              min={form.fechaInicio}
              value={form.fechaFin}
              onChange={(e) => set("fechaFin", e.target.value)}
              className="min-h-10 rounded-lg border border-border bg-background px-3"
            />
          </label>
          <label className="flex flex-col gap-1 text-sm sm:col-span-2">
            Organizador (opcional)
            <input
              value={form.organizador}
              onChange={(e) => set("organizador", e.target.value)}
              className="min-h-10 rounded-lg border border-border bg-background px-3"
            />
          </label>
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={form.recurrenteAnual}
              onChange={(e) => set("recurrenteAnual", e.target.checked)}
            />
            Se repite cada año
          </label>
          <div className="sm:col-span-2">
            {error && <p className="text-sm text-red-600">{error}</p>}
            {mensaje && <p className="text-sm text-emerald-600">{mensaje}</p>}
            <button
              type="submit"
              className="mt-2 min-h-11 rounded-full bg-primary px-6 text-sm font-medium text-primary-foreground hover:opacity-90"
            >
              Crear evento
            </button>
          </div>
        </form>
      </section>

      <section>
        <h2 className="text-lg font-semibold">Próximos eventos publicados</h2>
        {eventos.length === 0 ? (
          <p className="mt-3 text-sm opacity-70">No hay eventos próximos.</p>
        ) : (
          <ul className="mt-3 flex flex-col gap-2">
            {eventos.map((e) => (
              <li
                key={e.id}
                className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-border p-4"
              >
                <div>
                  <p className="font-medium">{e.nombre}</p>
                  <p className="text-xs opacity-60">
                    {TIPO_LABEL[e.tipo]} · {e.fechaInicio}
                    {e.fechaFin !== e.fechaInicio ? ` – ${e.fechaFin}` : ""}
                  </p>
                </div>
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => clonar(e.id)}
                    className="min-h-9 rounded-full border border-border px-4 text-sm hover:bg-muted"
                  >
                    Clonar +1 año
                  </button>
                  <button
                    type="button"
                    onClick={() => borrar(e.id)}
                    className="min-h-9 rounded-full border border-border px-4 text-sm text-red-600 hover:bg-muted"
                  >
                    Eliminar
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
