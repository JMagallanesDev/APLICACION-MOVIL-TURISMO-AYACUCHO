"use client";

import { useEffect, useState } from "react";
import { Link } from "@/i18n/navigation";
import { eliminarLugar, listarLugaresAdmin, type LugarAdminItem } from "@/lib/apiClienteAdmin";
import { useSesion } from "@/stores/sesion";

/** Gestión de lugares (RF-47). Panel interno en español. */

const ESTADO_ESTILO: Record<string, string> = {
  PUBLICADO: "bg-emerald-600/15 text-emerald-700 dark:text-emerald-400",
  BORRADOR: "bg-accent/15 text-accent",
  ARCHIVADO: "bg-muted opacity-80",
};

export default function GestionLugares() {
  const usuario = useSesion((s) => s.usuario);
  const [lugares, setLugares] = useState<LugarAdminItem[] | null>(null);
  const [confirmando, setConfirmando] = useState<string | null>(null);

  useEffect(() => {
    if (usuario?.rol !== "ADMIN") return;
    let vivo = true;
    listarLugaresAdmin().then((r) => {
      if (vivo) setLugares(r);
    });
    return () => {
      vivo = false;
    };
  }, [usuario]);

  async function borrar(id: string) {
    await eliminarLugar(id);
    setConfirmando(null);
    setLugares(await listarLugaresAdmin());
  }

  return (
    <div>
      <div className="flex items-center justify-between gap-4">
        <h1 className="text-2xl font-bold">Gestión de lugares</h1>
        <Link
          href="/admin/lugares/editor"
          className="min-h-11 rounded-full bg-primary px-5 text-sm font-medium text-primary-foreground hover:opacity-90"
        >
          + Nuevo lugar
        </Link>
      </div>

      {lugares === null ? (
        <div className="mt-6 h-40 animate-pulse rounded-2xl bg-muted" />
      ) : lugares.length === 0 ? (
        <p className="mt-6 text-sm opacity-80">No hay lugares registrados.</p>
      ) : (
        <ul className="mt-6 flex flex-col gap-2">
          {lugares.map((l) => (
            <li
              key={l.id}
              className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-border p-4"
            >
              <div>
                <p className="font-medium">{l.nombre}</p>
                <p className="text-xs opacity-75">
                  {l.slug} · {l.categoriaCodigo}
                </p>
              </div>
              <div className="flex items-center gap-2">
                <span
                  className={`rounded-full px-3 py-1 text-xs font-medium ${ESTADO_ESTILO[l.estado] ?? ""}`}
                >
                  {l.estado}
                </span>
                <Link
                  href={{ pathname: "/admin/lugares/editor", query: { slug: l.slug } }}
                  className="min-h-9 rounded-full border border-border px-4 py-1.5 text-sm hover:bg-muted"
                >
                  Editar
                </Link>
                {confirmando === l.id ? (
                  <>
                    <button
                      type="button"
                      onClick={() => borrar(l.id)}
                      className="min-h-9 rounded-full bg-red-600 px-4 text-sm font-medium text-white"
                    >
                      Confirmar
                    </button>
                    <button
                      type="button"
                      onClick={() => setConfirmando(null)}
                      className="min-h-9 rounded-full border border-border px-3 text-sm"
                    >
                      ✕
                    </button>
                  </>
                ) : (
                  <button
                    type="button"
                    onClick={() => setConfirmando(l.id)}
                    className="min-h-9 rounded-full border border-border px-4 text-sm text-red-600 hover:bg-muted"
                  >
                    Eliminar
                  </button>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
