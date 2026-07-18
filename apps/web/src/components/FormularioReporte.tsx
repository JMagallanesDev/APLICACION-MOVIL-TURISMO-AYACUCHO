"use client";

import { useEffect, useRef, useState } from "react";
import maplibregl from "maplibre-gl";
import "maplibre-gl/dist/maplibre-gl.css";
import { useTranslations } from "next-intl";
import { Link } from "@/i18n/navigation";
import type { TipoIncidente } from "@/lib/api";

/**
 * Reporte ciudadano completable en < 60 s (RF-69): selector visual de tipo
 * (RF-70), ubicación GPS + pin arrastrable (RF-71), fotos y anonimato (RF-72).
 * Envío multipart a POST /reportes (público, sin cuenta). No requiere login.
 */

const CENTRO_PLAZA: [number, number] = [-74.2236, -13.1631];

export default function FormularioReporte({ tipos }: { tipos: TipoIncidente[] }) {
  const t = useTranslations("Reportar");
  const contenedor = useRef<HTMLDivElement>(null);
  const marcadorRef = useRef<maplibregl.Marker | null>(null);

  const [tipoId, setTipoId] = useState<string>("");
  const [descripcion, setDescripcion] = useState("");
  const [direccion, setDireccion] = useState("");
  const [anonimo, setAnonimo] = useState(true);
  const [nombre, setNombre] = useState("");
  const [coords, setCoords] = useState<[number, number]>(CENTRO_PLAZA);
  const [fotos, setFotos] = useState<File[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [enviado, setEnviado] = useState(false);

  const maptilerKey = process.env.NEXT_PUBLIC_MAPTILER_KEY;

  useEffect(() => {
    if (!contenedor.current || marcadorRef.current) return;
    const mapa = new maplibregl.Map({
      container: contenedor.current,
      style: maptilerKey
        ? `https://api.maptiler.com/maps/streets-v2/style.json?key=${maptilerKey}`
        : "https://demotiles.maplibre.org/style.json",
      center: CENTRO_PLAZA,
      zoom: maptilerKey ? 15 : 12,
      maxBounds: [
        [-75.5, -15.5],
        [-73.0, -12.5],
      ],
    });
    const marcador = new maplibregl.Marker({ draggable: true, color: "#7b1e3c" })
      .setLngLat(CENTRO_PLAZA)
      .addTo(mapa);
    marcador.on("dragend", () => {
      const ll = marcador.getLngLat();
      setCoords([ll.lng, ll.lat]);
    });
    // Tocar el mapa reposiciona el pin (RF-71)
    mapa.on("click", (e) => {
      marcador.setLngLat(e.lngLat);
      setCoords([e.lngLat.lng, e.lngLat.lat]);
    });
    marcadorRef.current = marcador;
    return () => {
      mapa.remove();
      marcadorRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function usarMiUbicacion() {
    if (!navigator.geolocation) return;
    navigator.geolocation.getCurrentPosition((pos) => {
      const nuevo: [number, number] = [pos.coords.longitude, pos.coords.latitude];
      setCoords(nuevo);
      marcadorRef.current?.setLngLat(nuevo);
    });
  }

  async function enviar(e: React.FormEvent) {
    e.preventDefault();
    if (!tipoId) {
      setError(t("eligeTipo"));
      return;
    }
    if (descripcion.trim().length < 5) {
      setError(t("describeMas"));
      return;
    }
    setError(null);
    setEnviando(true);
    try {
      const datos = {
        tipoIncidenteId: tipoId,
        descripcion: descripcion.trim(),
        latitud: coords[1],
        longitud: coords[0],
        direccionReferencial: direccion.trim() || null,
        esAnonimo: anonimo,
        nombreReportante: anonimo ? null : nombre.trim() || null,
      };
      const form = new FormData();
      form.append(
        "datos",
        new Blob([JSON.stringify(datos)], { type: "application/json" }),
      );
      fotos.slice(0, 5).forEach((f) => form.append("fotos", f));

      const res = await fetch("/api/v1/reportes", { method: "POST", body: form });
      if (!res.ok) {
        const cuerpo = await res.json().catch(() => null);
        throw new Error(cuerpo?.mensaje ?? t("errorEnvio"));
      }
      setEnviado(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : t("errorEnvio"));
    } finally {
      setEnviando(false);
    }
  }

  if (enviado) {
    return (
      <div className="mx-auto mt-10 max-w-md rounded-2xl border border-border bg-emerald-600/10 p-6 text-center">
        <span aria-hidden className="text-4xl">
          🛡️
        </span>
        <h2 className="mt-3 text-xl font-semibold">{t("graciasTitulo")}</h2>
        <p className="mt-2 text-sm opacity-80">{t("graciasTexto")}</p>
        <div className="mt-4 flex justify-center gap-3">
          <Link
            href="/mapa-incidentes"
            className="min-h-11 rounded-full bg-primary px-6 text-sm font-medium text-primary-foreground hover:opacity-90"
          >
            {t("verMapaIncidentes")}
          </Link>
        </div>
      </div>
    );
  }

  return (
    <form onSubmit={enviar} className="mt-6 flex flex-col gap-5">
      {/* Selector visual de tipo (RF-70) */}
      <fieldset>
        <legend className="text-sm font-medium">{t("tipoIncidente")}</legend>
        <div className="mt-2 grid grid-cols-2 gap-2 sm:grid-cols-3">
          {tipos.map((tipo) => (
            <button
              key={tipo.id}
              type="button"
              onClick={() => setTipoId(tipo.id)}
              aria-pressed={tipoId === tipo.id}
              className={`min-h-11 rounded-xl border px-3 py-2 text-sm transition-colors ${
                tipoId === tipo.id
                  ? "border-primary bg-primary/10 font-medium"
                  : "border-border hover:bg-muted"
              }`}
              style={tipoId === tipo.id && tipo.colorHex ? { borderColor: tipo.colorHex } : undefined}
            >
              {tipo.nombre}
            </button>
          ))}
        </div>
      </fieldset>

      <label className="flex flex-col gap-1 text-sm font-medium">
        {t("descripcion")}
        <textarea
          value={descripcion}
          onChange={(e) => setDescripcion(e.target.value)}
          maxLength={2000}
          rows={3}
          placeholder={t("descripcionPlaceholder")}
          className="resize-none rounded-xl border border-border bg-background px-4 py-2 text-sm font-normal outline-none focus:border-primary"
        />
      </label>

      {/* Ubicación GPS + pin ajustable (RF-71) */}
      <div>
        <div className="flex items-center justify-between">
          <span className="text-sm font-medium">{t("ubicacion")}</span>
          <button
            type="button"
            onClick={usarMiUbicacion}
            className="text-sm font-medium text-secondary hover:underline"
          >
            {t("usarMiUbicacion")}
          </button>
        </div>
        <div
          ref={contenedor}
          className="mt-2 h-56 w-full overflow-hidden rounded-2xl border border-border"
        />
        <p className="mt-1 text-xs opacity-75">{t("ayudaPin")}</p>
        <input
          type="text"
          value={direccion}
          onChange={(e) => setDireccion(e.target.value)}
          placeholder={t("referenciaPlaceholder")}
          className="mt-2 min-h-11 w-full rounded-xl border border-border bg-background px-4 text-sm outline-none focus:border-primary"
        />
      </div>

      {/* Fotos opcionales */}
      <label className="flex flex-col gap-1 text-sm font-medium">
        {t("fotos")}
        <input
          type="file"
          accept="image/jpeg,image/png,image/webp"
          multiple
          onChange={(e) => setFotos(Array.from(e.target.files ?? []).slice(0, 5))}
          className="text-sm font-normal"
        />
        {fotos.length > 0 && (
          <span className="text-xs opacity-80">{t("fotosSeleccionadas", { n: fotos.length })}</span>
        )}
      </label>

      {/* Anonimato (RF-72) */}
      <label className="flex items-center gap-2 text-sm">
        <input
          type="checkbox"
          checked={anonimo}
          onChange={(e) => setAnonimo(e.target.checked)}
          className="h-4 w-4"
        />
        {t("reportarAnonimo")}
      </label>
      {!anonimo && (
        <input
          type="text"
          value={nombre}
          onChange={(e) => setNombre(e.target.value)}
          placeholder={t("tuNombre")}
          className="min-h-11 rounded-xl border border-border bg-background px-4 text-sm outline-none focus:border-primary"
        />
      )}
      <p className="rounded-xl bg-muted px-4 py-2 text-xs opacity-80">🔒 {t("privacidad")}</p>

      {error && (
        <p role="alert" className="text-sm text-red-600">
          {error}
        </p>
      )}

      <button
        type="submit"
        disabled={enviando}
        className="min-h-12 rounded-full bg-primary font-medium text-primary-foreground hover:opacity-90 disabled:opacity-75"
      >
        {enviando ? t("enviando") : t("enviarReporte")}
      </button>
    </form>
  );
}
