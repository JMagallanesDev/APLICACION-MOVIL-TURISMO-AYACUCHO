"use client";

import { Suspense, useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { useRouter } from "@/i18n/navigation";
import MapaPin from "@/components/admin/MapaPin";
import {
  DISTRITO_AYACUCHO,
  guardarLugar,
  type HorarioAdmin,
  type LugarPayload,
  type TraduccionLugarAdmin,
} from "@/lib/apiClienteAdmin";

/** Editor de lugar (RF-47): traducciones es/en, horarios por turnos, datos prácticos. */

const CENTRO_PLAZA = { lat: -13.1631, lng: -74.2236 };
const DIAS = ["Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"];
const ORDEN_DIAS = [1, 2, 3, 4, 5, 6, 0]; // mostrar lunes→domingo

interface Turno {
  apertura: string;
  cierre: string;
}

interface DiaHorario {
  abierto: boolean;
  turnos: Turno[];
}

interface CamposTraduccion {
  nombre: string;
  descripcion: string;
  historia: string;
  consejos: string;
}

interface CategoriaOpcion {
  id: string;
  codigo: string;
  nombre: string;
}

const TRAD_VACIA: CamposTraduccion = { nombre: "", descripcion: "", historia: "", consejos: "" };

function diasIniciales(): DiaHorario[] {
  return Array.from({ length: 7 }, () => ({
    abierto: false,
    turnos: [{ apertura: "09:00", cierre: "17:00" }],
  }));
}

function aKebab(texto: string): string {
  return texto
    .toLowerCase()
    .normalize("NFD")
    .replace(/[̀-ͯ]/g, "")
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

function EditorLugar() {
  const parametros = useSearchParams();
  const slugParam = parametros.get("slug");
  const router = useRouter();

  const [cargando, setCargando] = useState(Boolean(slugParam));
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [id, setId] = useState<string | null>(null);
  const [slug, setSlug] = useState("");
  const [slugTocado, setSlugTocado] = useState(Boolean(slugParam));
  const [categorias, setCategorias] = useState<CategoriaOpcion[]>([]);
  const [categoriaLugarId, setCategoriaLugarId] = useState("");
  const [estado, setEstado] = useState("BORRADOR");
  const [lat, setLat] = useState(CENTRO_PLAZA.lat);
  const [lng, setLng] = useState(CENTRO_PLAZA.lng);
  const [direccion, setDireccion] = useState("");
  const [telefono, setTelefono] = useState("");
  const [precio, setPrecio] = useState("");
  const [duracion, setDuracion] = useState("");
  const [costoTaxi, setCostoTaxi] = useState("");
  const [aceptaTarjeta, setAceptaTarjeta] = useState("");
  const [tieneBanos, setTieneBanos] = useState("");
  const [accesible, setAccesible] = useState("");
  const [aptoNinos, setAptoNinos] = useState("");
  const [requiereGuia, setRequiereGuia] = useState("");
  const [tradEs, setTradEs] = useState<CamposTraduccion>(TRAD_VACIA);
  const [tradEn, setTradEn] = useState<CamposTraduccion>(TRAD_VACIA);
  const [dias, setDias] = useState<DiaHorario[]>(diasIniciales);

  useEffect(() => {
    let vivo = true;
    fetch("/api/v1/categorias-lugar?idioma=es")
      .then((r) => r.json())
      .then((cats: CategoriaOpcion[]) => {
        if (!vivo) return;
        setCategorias(cats);
        if (!slugParam && cats.length > 0) {
          setCategoriaLugarId((previa) => previa || cats[0].id);
        }
      })
      .catch(() => {});

    if (slugParam) {
      fetch(`/api/v1/lugares/${slugParam}`)
        .then((r) => {
          if (!r.ok) throw new Error("No se encontró el lugar");
          return r.json();
        })
        .then((d) => {
          if (!vivo) return;
          setId(d.id);
          setSlug(d.slug);
          setCategoriaLugarId(d.categoriaLugarId);
          setEstado(d.estado);
          setLat(d.latitud);
          setLng(d.longitud);
          setDireccion(d.direccion ?? "");
          setTelefono(d.telefono ?? "");
          setPrecio(d.precioEntradaPen != null ? String(d.precioEntradaPen) : "");
          setDuracion(d.duracionVisitaMin != null ? String(d.duracionVisitaMin) : "");
          setCostoTaxi(d.costoTaxiDesdePlazaPen != null ? String(d.costoTaxiDesdePlazaPen) : "");
          setAceptaTarjeta(d.aceptaTarjeta == null ? "" : d.aceptaTarjeta ? "si" : "no");
          setTieneBanos(d.tieneBanos == null ? "" : d.tieneBanos ? "si" : "no");
          setAccesible(d.accesibleSillaRuedas == null ? "" : d.accesibleSillaRuedas ? "si" : "no");
          setAptoNinos(d.aptoNinos == null ? "" : d.aptoNinos ? "si" : "no");
          setRequiereGuia(d.requiereGuia == null ? "" : d.requiereGuia ? "si" : "no");
          for (const t of d.traducciones as TraduccionLugarAdmin[]) {
            const campos: CamposTraduccion = {
              nombre: t.nombre ?? "",
              descripcion: t.descripcion ?? "",
              historia: t.historia ?? "",
              consejos: t.consejos ?? "",
            };
            if (t.idioma === "es") setTradEs(campos);
            if (t.idioma === "en") setTradEn(campos);
          }
          const nuevos = diasIniciales();
          for (const h of d.horarios as HorarioAdmin[]) {
            const dia = nuevos[h.diaSemana];
            if (h.cerrado || !h.horaApertura || !h.horaCierre) continue;
            const turno = { apertura: h.horaApertura.slice(0, 5), cierre: h.horaCierre.slice(0, 5) };
            if (!dia.abierto) {
              dia.abierto = true;
              dia.turnos = [turno];
            } else {
              dia.turnos.push(turno);
            }
          }
          setDias(nuevos);
          setCargando(false);
        })
        .catch((e) => {
          if (vivo) {
            setError(e instanceof Error ? e.message : "Error al cargar");
            setCargando(false);
          }
        });
    }
    return () => {
      vivo = false;
    };
  }, [slugParam]);

  function cambiarNombreEs(nombre: string) {
    setTradEs((t) => ({ ...t, nombre }));
    if (!slugTocado) setSlug(aKebab(nombre));
  }

  function cambiarDia(indice: number, cambio: Partial<DiaHorario>) {
    setDias((previos) => previos.map((d, i) => (i === indice ? { ...d, ...cambio } : d)));
  }

  function cambiarTurno(indiceDia: number, indiceTurno: number, campo: keyof Turno, valor: string) {
    setDias((previos) =>
      previos.map((d, i) =>
        i === indiceDia
          ? {
              ...d,
              turnos: d.turnos.map((t, j) => (j === indiceTurno ? { ...t, [campo]: valor } : t)),
            }
          : d,
      ),
    );
  }

  function triState(valor: string): boolean | null {
    return valor === "" ? null : valor === "si";
  }

  function numeroONull(valor: string): number | null {
    const n = Number(valor);
    return valor.trim() === "" || Number.isNaN(n) ? null : n;
  }

  async function enviar(e: React.FormEvent) {
    e.preventDefault();
    setError(null);

    if (!tradEs.nombre.trim()) {
      setError("El nombre en español es obligatorio.");
      return;
    }
    if (!slug.trim() || !categoriaLugarId) {
      setError("Completa el slug y la categoría.");
      return;
    }

    const traducciones: TraduccionLugarAdmin[] = [
      {
        idioma: "es",
        nombre: tradEs.nombre.trim(),
        descripcion: tradEs.descripcion.trim() || null,
        historia: tradEs.historia.trim() || null,
        consejos: tradEs.consejos.trim() || null,
      },
    ];
    if (tradEn.nombre.trim()) {
      traducciones.push({
        idioma: "en",
        nombre: tradEn.nombre.trim(),
        descripcion: tradEn.descripcion.trim() || null,
        historia: tradEn.historia.trim() || null,
        consejos: tradEn.consejos.trim() || null,
      });
    }

    const horarios: HorarioAdmin[] = [];
    dias.forEach((dia, diaSemana) => {
      if (!dia.abierto) {
        horarios.push({ diaSemana, horaApertura: null, horaCierre: null, cerrado: true });
        return;
      }
      const validos = dia.turnos.filter((t) => t.apertura && t.cierre && t.apertura < t.cierre);
      if (validos.length === 0) {
        horarios.push({ diaSemana, horaApertura: null, horaCierre: null, cerrado: true });
        return;
      }
      for (const t of validos) {
        horarios.push({ diaSemana, horaApertura: t.apertura, horaCierre: t.cierre, cerrado: false });
      }
    });

    const payload: LugarPayload = {
      slug: slug.trim(),
      categoriaLugarId,
      distritoId: DISTRITO_AYACUCHO,
      latitud: lat,
      longitud: lng,
      direccion: direccion.trim() || null,
      telefono: telefono.trim() || null,
      precioEntradaPen: numeroONull(precio),
      duracionVisitaMin: numeroONull(duracion),
      aceptaTarjeta: triState(aceptaTarjeta),
      tieneBanos: triState(tieneBanos),
      accesibleSillaRuedas: triState(accesible),
      aptoNinos: triState(aptoNinos),
      costoTaxiDesdePlazaPen: numeroONull(costoTaxi),
      requiereGuia: triState(requiereGuia),
      estado,
      traducciones,
      horarios,
    };

    setGuardando(true);
    try {
      await guardarLugar(payload, id);
      router.push("/admin/lugares");
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo guardar el lugar.");
      setGuardando(false);
    }
  }

  if (cargando) {
    return <div className="h-64 animate-pulse rounded-2xl bg-muted" />;
  }

  const claseCampo =
    "w-full rounded-xl border border-border bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary";
  const claseEtiqueta = "mb-1 block text-sm font-medium";

  const selectSiNo = (valor: string, setter: (v: string) => void, etiqueta: string) => (
    <div>
      <label className={claseEtiqueta}>{etiqueta}</label>
      <select value={valor} onChange={(e) => setter(e.target.value)} className={claseCampo}>
        <option value="">No especificado</option>
        <option value="si">Sí</option>
        <option value="no">No</option>
      </select>
    </div>
  );

  const camposIdioma = (
    titulo: string,
    trad: CamposTraduccion,
    setter: React.Dispatch<React.SetStateAction<CamposTraduccion>>,
    esEspanol: boolean,
  ) => (
    <fieldset className="rounded-2xl border border-border p-4">
      <legend className="px-2 text-sm font-semibold">{titulo}</legend>
      <div className="flex flex-col gap-3">
        <div>
          <label className={claseEtiqueta}>
            Nombre {esEspanol ? "*" : "(vacío = sin traducción)"}
          </label>
          <input
            type="text"
            value={trad.nombre}
            maxLength={150}
            onChange={(e) =>
              esEspanol ? cambiarNombreEs(e.target.value) : setter((t) => ({ ...t, nombre: e.target.value }))
            }
            className={claseCampo}
          />
        </div>
        <div>
          <label className={claseEtiqueta}>Descripción</label>
          <textarea
            value={trad.descripcion}
            rows={3}
            onChange={(e) => setter((t) => ({ ...t, descripcion: e.target.value }))}
            className={claseCampo}
          />
        </div>
        <div>
          <label className={claseEtiqueta}>Historia</label>
          <textarea
            value={trad.historia}
            rows={3}
            onChange={(e) => setter((t) => ({ ...t, historia: e.target.value }))}
            className={claseCampo}
          />
        </div>
        <div>
          <label className={claseEtiqueta}>Consejos</label>
          <textarea
            value={trad.consejos}
            rows={2}
            onChange={(e) => setter((t) => ({ ...t, consejos: e.target.value }))}
            className={claseCampo}
          />
        </div>
      </div>
    </fieldset>
  );

  return (
    <div>
      <h1 className="text-2xl font-bold">{id ? "Editar lugar" : "Nuevo lugar"}</h1>

      <form onSubmit={enviar} className="mt-6 flex max-w-3xl flex-col gap-6">
        <div className="grid gap-4 sm:grid-cols-2">
          {camposIdioma("Español *", tradEs, setTradEs, true)}
          {camposIdioma("Inglés (opcional)", tradEn, setTradEn, false)}
        </div>

        <div className="grid gap-4 sm:grid-cols-3">
          <div>
            <label className={claseEtiqueta}>Slug (URL) *</label>
            <input
              type="text"
              value={slug}
              onChange={(e) => {
                setSlugTocado(true);
                setSlug(aKebab(e.target.value));
              }}
              className={claseCampo}
            />
          </div>
          <div>
            <label className={claseEtiqueta}>Categoría *</label>
            <select
              value={categoriaLugarId}
              onChange={(e) => setCategoriaLugarId(e.target.value)}
              className={claseCampo}
            >
              {categorias.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.nombre}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className={claseEtiqueta}>Estado *</label>
            <select value={estado} onChange={(e) => setEstado(e.target.value)} className={claseCampo}>
              <option value="BORRADOR">Borrador (no visible)</option>
              <option value="PUBLICADO">Publicado</option>
              <option value="ARCHIVADO">Archivado</option>
            </select>
          </div>
        </div>

        <fieldset className="rounded-2xl border border-border p-4">
          <legend className="px-2 text-sm font-semibold">Ubicación</legend>
          <p className="mb-3 text-xs opacity-70">
            Toca el mapa o arrastra el pin hasta la ubicación exacta del lugar.
          </p>
          <MapaPin latitud={lat} longitud={lng} onCambio={(la, lo) => { setLat(la); setLng(lo); }} />
          <div className="mt-3 grid gap-4 sm:grid-cols-3">
            <div>
              <label className={claseEtiqueta}>Latitud</label>
              <input
                type="number"
                step="0.000001"
                value={lat}
                onChange={(e) => setLat(Number(e.target.value))}
                className={claseCampo}
              />
            </div>
            <div>
              <label className={claseEtiqueta}>Longitud</label>
              <input
                type="number"
                step="0.000001"
                value={lng}
                onChange={(e) => setLng(Number(e.target.value))}
                className={claseCampo}
              />
            </div>
            <div>
              <label className={claseEtiqueta}>Dirección</label>
              <input
                type="text"
                value={direccion}
                maxLength={255}
                onChange={(e) => setDireccion(e.target.value)}
                className={claseCampo}
              />
            </div>
          </div>
        </fieldset>

        <fieldset className="rounded-2xl border border-border p-4">
          <legend className="px-2 text-sm font-semibold">Datos prácticos (Antes de ir)</legend>
          <div className="grid gap-4 sm:grid-cols-3">
            <div>
              <label className={claseEtiqueta}>Precio entrada (S/)</label>
              <input
                type="number"
                min="0"
                step="0.5"
                value={precio}
                onChange={(e) => setPrecio(e.target.value)}
                placeholder="Vacío = gratis/sin dato"
                className={claseCampo}
              />
            </div>
            <div>
              <label className={claseEtiqueta}>Duración visita (min)</label>
              <input
                type="number"
                min="1"
                value={duracion}
                onChange={(e) => setDuracion(e.target.value)}
                className={claseCampo}
              />
            </div>
            <div>
              <label className={claseEtiqueta}>Taxi desde la Plaza (S/)</label>
              <input
                type="number"
                min="0"
                step="0.5"
                value={costoTaxi}
                onChange={(e) => setCostoTaxi(e.target.value)}
                className={claseCampo}
              />
            </div>
            <div>
              <label className={claseEtiqueta}>Teléfono</label>
              <input
                type="tel"
                value={telefono}
                maxLength={30}
                onChange={(e) => setTelefono(e.target.value)}
                className={claseCampo}
              />
            </div>
            {selectSiNo(aceptaTarjeta, setAceptaTarjeta, "Acepta tarjeta")}
            {selectSiNo(tieneBanos, setTieneBanos, "Tiene baños")}
            {selectSiNo(accesible, setAccesible, "Accesible silla de ruedas")}
            {selectSiNo(aptoNinos, setAptoNinos, "Apto para niños")}
            {selectSiNo(requiereGuia, setRequiereGuia, "Requiere guía")}
          </div>
        </fieldset>

        <fieldset className="rounded-2xl border border-border p-4">
          <legend className="px-2 text-sm font-semibold">Horarios</legend>
          <div className="flex flex-col gap-2">
            {ORDEN_DIAS.map((diaSemana) => {
              const dia = dias[diaSemana];
              return (
                <div
                  key={diaSemana}
                  className="flex flex-wrap items-center gap-3 rounded-xl border border-border px-3 py-2"
                >
                  <label className="flex w-36 items-center gap-2 text-sm">
                    <input
                      type="checkbox"
                      checked={dia.abierto}
                      onChange={(e) => cambiarDia(diaSemana, { abierto: e.target.checked })}
                    />
                    {DIAS[diaSemana]}
                  </label>
                  {dia.abierto ? (
                    <div className="flex flex-wrap items-center gap-2">
                      {dia.turnos.map((turno, j) => (
                        <span key={j} className="flex items-center gap-1 text-sm">
                          <input
                            type="time"
                            value={turno.apertura}
                            onChange={(e) => cambiarTurno(diaSemana, j, "apertura", e.target.value)}
                            className="rounded-lg border border-border bg-background px-2 py-1"
                          />
                          –
                          <input
                            type="time"
                            value={turno.cierre}
                            onChange={(e) => cambiarTurno(diaSemana, j, "cierre", e.target.value)}
                            className="rounded-lg border border-border bg-background px-2 py-1"
                          />
                        </span>
                      ))}
                      {dia.turnos.length === 1 ? (
                        <button
                          type="button"
                          onClick={() =>
                            cambiarDia(diaSemana, {
                              turnos: [...dia.turnos, { apertura: "15:00", cierre: "18:00" }],
                            })
                          }
                          className="text-xs text-primary underline"
                        >
                          + 2.º turno
                        </button>
                      ) : (
                        <button
                          type="button"
                          onClick={() => cambiarDia(diaSemana, { turnos: dia.turnos.slice(0, 1) })}
                          className="text-xs text-red-600 underline"
                        >
                          Quitar 2.º turno
                        </button>
                      )}
                    </div>
                  ) : (
                    <span className="text-sm opacity-60">Cerrado</span>
                  )}
                </div>
              );
            })}
          </div>
        </fieldset>

        {error && (
          <p className="rounded-xl bg-red-600/10 px-4 py-3 text-sm text-red-700 dark:text-red-400">
            {error}
          </p>
        )}

        <div className="flex items-center gap-3">
          <button
            type="submit"
            disabled={guardando}
            className="min-h-11 rounded-full bg-primary px-6 text-sm font-medium text-primary-foreground hover:opacity-90 disabled:opacity-50"
          >
            {guardando ? "Guardando…" : id ? "Guardar cambios" : "Crear lugar"}
          </button>
          <button
            type="button"
            onClick={() => router.push("/admin/lugares")}
            className="min-h-11 rounded-full border border-border px-6 text-sm hover:bg-muted"
          >
            Cancelar
          </button>
        </div>
      </form>
    </div>
  );
}

export default function PaginaEditorLugar() {
  return (
    <Suspense fallback={<div className="h-64 animate-pulse rounded-2xl bg-muted" />}>
      <EditorLugar />
    </Suspense>
  );
}
