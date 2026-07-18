"use client";

import { useEffect, useState } from "react";
import { useLocale, useTranslations } from "next-intl";
import { Link } from "@/i18n/navigation";
import { obtenerCategoriasNegocio, type CategoriaNegocio } from "@/lib/api";
import {
  guardarNegocio,
  obtenerMiNegocio,
  type DatosNegocio,
  type ErrorApi,
  type MiNegocio,
} from "@/lib/apiCliente";
import { useSesion } from "@/stores/sesion";

/**
 * Registro y panel propio del negocio (RF-104/107): si el usuario ya tiene
 * uno, muestra su estado y permite editarlo; si no, formulario de alta que
 * queda PENDIENTE hasta aprobación del admin.
 */

const VACIO: DatosNegocio = {
  categoriaNegocioId: "",
  nombre: "",
  whatsapp: "",
  telefono: "",
  direccion: "",
  horario: "",
  descripcionEs: "",
  descripcionEn: "",
};

export default function FormularioNegocio() {
  const t = useTranslations("Negocios");
  const locale = useLocale();
  const usuario = useSesion((s) => s.usuario);
  const restaurando = useSesion((s) => s.restaurando);

  const [categorias, setCategorias] = useState<CategoriaNegocio[]>([]);
  const [existente, setExistente] = useState<MiNegocio | null>(null);
  const [form, setForm] = useState<DatosNegocio>(VACIO);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [exito, setExito] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    if (restaurando) return;
    let vivo = true;
    // Carga conjunta: las categorías permiten resolver el id desde el código
    // que devuelve el API en /negocios/mio
    Promise.all([
      obtenerCategoriasNegocio(locale),
      usuario ? obtenerMiNegocio().catch(() => null) : Promise.resolve(null),
    ])
      .then(([cats, n]) => {
        if (!vivo) return;
        setCategorias(cats);
        setExistente(n);
        if (n) {
          const categoria = cats.find((c) => c.codigo === n.categoriaCodigo);
          setForm({
            categoriaNegocioId: categoria?.id ?? "",
            nombre: n.nombre,
            whatsapp: n.whatsapp,
            telefono: n.telefono ?? "",
            direccion: n.direccion ?? "",
            horario: n.horario ?? "",
            descripcionEs: n.descripcion ?? "",
            descripcionEn: "",
          });
        }
      })
      .finally(() => {
        if (vivo) setCargando(false);
      });
    return () => {
      vivo = false;
    };
  }, [usuario, restaurando, locale]);

  if (restaurando) {
    return <div className="mt-8 h-40 animate-pulse rounded-2xl bg-muted" />;
  }

  if (!usuario) {
    return (
      <p className="mt-8 rounded-2xl bg-muted px-5 py-4 text-sm">
        <Link href="/cuenta" className="font-medium text-secondary hover:underline">
          {t("iniciaSesionParaRegistrar")}
        </Link>
      </p>
    );
  }

  if (cargando) {
    return <div className="mt-8 h-40 animate-pulse rounded-2xl bg-muted" />;
  }

  const esNuevo = existente === null;

  async function enviar(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setExito(null);
    setEnviando(true);
    try {
      await guardarNegocio(form, esNuevo);
      setExito(esNuevo ? t("registrado") : t("actualizado"));
      if (esNuevo) {
        const n = await obtenerMiNegocio();
        setExistente(n);
      }
    } catch (err) {
      setError((err as ErrorApi).mensaje ?? t("errorGuardar"));
    } finally {
      setEnviando(false);
    }
  }

  const set = <K extends keyof DatosNegocio>(k: K, v: string) =>
    setForm((f) => ({ ...f, [k]: v }));

  return (
    <div className="mt-6">
      {existente && (
        <p
          className={`mb-4 rounded-xl px-4 py-3 text-sm font-medium ${
            existente.estado === "APROBADO"
              ? "bg-emerald-600/10 text-emerald-700 dark:text-emerald-400"
              : existente.estado === "PENDIENTE"
                ? "bg-accent/15 text-accent"
                : "bg-red-600/10 text-red-700 dark:text-red-400"
          }`}
        >
          {t(`estados.${existente.estado}`)}
        </p>
      )}

      <form onSubmit={enviar} className="grid gap-4 rounded-2xl border border-border p-5 sm:grid-cols-2">
        <label className="flex flex-col gap-1 text-sm sm:col-span-2">
          {t("nombreNegocio")}
          <input
            required
            maxLength={150}
            value={form.nombre}
            onChange={(e) => set("nombre", e.target.value)}
            className="min-h-11 rounded-xl border border-border bg-background px-4 outline-none focus:border-primary"
          />
        </label>

        <label className="flex flex-col gap-1 text-sm">
          {t("categoria")}
          <select
            required
            value={form.categoriaNegocioId}
            onChange={(e) => set("categoriaNegocioId", e.target.value)}
            className="min-h-11 rounded-xl border border-border bg-background px-3 outline-none focus:border-primary"
          >
            <option value="" disabled>
              {t("eligeCategoria")}
            </option>
            {categorias.map((c) => (
              <option key={c.id} value={c.id}>
                {c.nombre}
              </option>
            ))}
          </select>
        </label>

        <label className="flex flex-col gap-1 text-sm">
          {t("whatsapp")}
          <input
            required
            placeholder="51966123456"
            value={form.whatsapp}
            onChange={(e) => set("whatsapp", e.target.value)}
            className="min-h-11 rounded-xl border border-border bg-background px-4 outline-none focus:border-primary"
          />
          <span className="text-xs opacity-75">{t("whatsappAyuda")}</span>
        </label>

        <label className="flex flex-col gap-1 text-sm">
          {t("direccion")}
          <input
            value={form.direccion}
            onChange={(e) => set("direccion", e.target.value)}
            className="min-h-11 rounded-xl border border-border bg-background px-4 outline-none focus:border-primary"
          />
        </label>

        <label className="flex flex-col gap-1 text-sm">
          {t("horario")}
          <input
            placeholder={t("horarioEjemplo")}
            value={form.horario}
            onChange={(e) => set("horario", e.target.value)}
            className="min-h-11 rounded-xl border border-border bg-background px-4 outline-none focus:border-primary"
          />
        </label>

        <label className="flex flex-col gap-1 text-sm sm:col-span-2">
          {t("descripcionEs")}
          <textarea
            required
            rows={3}
            value={form.descripcionEs}
            onChange={(e) => set("descripcionEs", e.target.value)}
            className="resize-none rounded-xl border border-border bg-background px-4 py-2 outline-none focus:border-primary"
          />
        </label>

        <label className="flex flex-col gap-1 text-sm sm:col-span-2">
          {t("descripcionEn")}
          <textarea
            rows={3}
            value={form.descripcionEn}
            onChange={(e) => set("descripcionEn", e.target.value)}
            className="resize-none rounded-xl border border-border bg-background px-4 py-2 outline-none focus:border-primary"
          />
        </label>

        <div className="sm:col-span-2">
          {error && (
            <p role="alert" className="text-sm text-red-600">
              {error}
            </p>
          )}
          {exito && <p className="text-sm text-emerald-600">{exito}</p>}
          <button
            type="submit"
            disabled={enviando}
            className="mt-2 min-h-12 w-full rounded-full bg-primary font-medium text-primary-foreground hover:opacity-90 disabled:opacity-75 sm:w-auto sm:px-8"
          >
            {enviando ? t("enviando") : esNuevo ? t("registrar") : t("guardarCambios")}
          </button>
        </div>
      </form>
    </div>
  );
}
