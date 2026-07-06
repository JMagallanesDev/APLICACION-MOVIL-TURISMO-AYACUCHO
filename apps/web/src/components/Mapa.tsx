"use client";

import { useEffect, useRef, useState } from "react";
import maplibregl from "maplibre-gl";
import "maplibre-gl/dist/maplibre-gl.css";
import { useLocale, useTranslations } from "next-intl";
import type { LugarResumen } from "@/lib/api";

/**
 * Mapa MapLibre (RF-17/18/19/22b): marcadores por categoría con clusters,
 * GPS del usuario, restricción a la región Ayacucho y vista 3D con edificios
 * extruidos cuando hay key de MapTiler (fill-extrusion nativo, sin three.js).
 * Sin key degrada a los demo tiles de MapLibre en 2D.
 */

/** Colores del seed de categorías (V5) — misma paleta que la BD. */
const COLOR_CATEGORIA: Record<string, string> = {
  IGLESIAS: "#7b1e3c",
  MIRADORES: "#2b4c7e",
  MUSEOS: "#8a5a3b",
  PLAZAS: "#c9942a",
  SITIOS_ARQUEOLOGICOS: "#6d4c2f",
  CASONAS: "#9c3d54",
  TALLERES_ARTESANALES: "#b06e2c",
  NATURALEZA: "#3e7048",
};

/** Expresión match de MapLibre construida desde el catálogo de colores. */
const EXPRESION_COLOR = [
  "match",
  ["get", "categoria"],
  ...Object.entries(COLOR_CATEGORIA).flat(),
  "#7b1e3c",
] as unknown as maplibregl.ExpressionSpecification;

const PITCH_3D = 55;
// Bounds de la región Ayacucho (RF-22b, mismos límites que el CHECK en BD)
const BOUNDS_AYACUCHO: [[number, number], [number, number]] = [
  [-75.5, -15.5],
  [-73.0, -12.5],
];

export default function Mapa({ lugares }: { lugares: LugarResumen[] }) {
  const t = useTranslations("Mapa");
  const locale = useLocale();
  const contenedor = useRef<HTMLDivElement>(null);
  const mapaRef = useRef<maplibregl.Map | null>(null);
  const [en3D, setEn3D] = useState(true);

  const maptilerKey = process.env.NEXT_PUBLIC_MAPTILER_KEY;
  const con3D = Boolean(maptilerKey);

  useEffect(() => {
    if (!contenedor.current || mapaRef.current) {
      return;
    }

    const mapa = new maplibregl.Map({
      container: contenedor.current,
      style: maptilerKey
        ? `https://api.maptiler.com/maps/streets-v2/style.json?key=${maptilerKey}`
        : "https://demotiles.maplibre.org/style.json",
      center: [-74.2236, -13.1631], // Plaza Mayor de Ayacucho
      zoom: maptilerKey ? 15 : 12,
      pitch: con3D ? PITCH_3D : 0,
      maxBounds: BOUNDS_AYACUCHO,
      attributionControl: { compact: true },
    });
    mapaRef.current = mapa;

    mapa.addControl(new maplibregl.NavigationControl({ visualizePitch: true }), "top-right");
    // GPS del usuario en tiempo real (RF-19)
    mapa.addControl(
      new maplibregl.GeolocateControl({
        positionOptions: { enableHighAccuracy: true },
        trackUserLocation: true,
      }),
      "top-right",
    );

    mapa.on("load", () => {
      // Edificios extruidos (RF-17): capa building de los tiles MapTiler
      if (maptilerKey) {
        mapa.addLayer({
          id: "edificios-3d",
          type: "fill-extrusion",
          source: "openmaptiles",
          "source-layer": "building",
          minzoom: 14,
          paint: {
            "fill-extrusion-color": "#d9cbb5",
            "fill-extrusion-height": ["coalesce", ["get", "render_height"], 6],
            "fill-extrusion-base": ["coalesce", ["get", "render_min_height"], 0],
            "fill-extrusion-opacity": 0.55,
          },
        });
      }

      // Marcadores con clusters (RF-18)
      mapa.addSource("lugares", {
        type: "geojson",
        cluster: true,
        clusterRadius: 45,
        data: {
          type: "FeatureCollection",
          features: lugares.map((l) => ({
            type: "Feature",
            geometry: { type: "Point", coordinates: [l.longitud, l.latitud] },
            properties: {
              slug: l.slug,
              nombre: l.nombre,
              categoria: l.categoriaCodigo,
              abierto: l.abiertoAhora,
            },
          })),
        },
      });

      mapa.addLayer({
        id: "clusters",
        type: "circle",
        source: "lugares",
        filter: ["has", "point_count"],
        paint: {
          "circle-color": "#7b1e3c",
          "circle-radius": ["step", ["get", "point_count"], 16, 10, 22, 25, 28],
          "circle-stroke-width": 2,
          "circle-stroke-color": "#ffffff",
        },
      });
      mapa.addLayer({
        id: "cluster-count",
        type: "symbol",
        source: "lugares",
        filter: ["has", "point_count"],
        layout: {
          "text-field": ["get", "point_count_abbreviated"],
          "text-size": 13,
        },
        paint: { "text-color": "#ffffff" },
      });
      mapa.addLayer({
        id: "lugares-puntos",
        type: "circle",
        source: "lugares",
        filter: ["!", ["has", "point_count"]],
        paint: {
          "circle-color": EXPRESION_COLOR,
          "circle-radius": 9,
          "circle-stroke-width": 2,
          "circle-stroke-color": "#ffffff",
        },
      });

      // Popup con enlace a la ficha
      mapa.on("click", "lugares-puntos", (e) => {
        const feature = e.features?.[0];
        if (!feature) return;
        const { slug, nombre, abierto } = feature.properties as {
          slug: string;
          nombre: string;
          abierto: string;
        };
        const [lon, lat] = (feature.geometry as { coordinates: [number, number] }).coordinates;
        const badge =
          abierto === "true"
            ? `<span style="color:#059669">● ${t("abierto")}</span>`
            : abierto === "false"
              ? `<span style="color:#dc2626">● ${t("cerrado")}</span>`
              : "";
        new maplibregl.Popup({ offset: 12 })
          .setLngLat([lon, lat])
          .setHTML(
            `<strong>${nombre}</strong><br/>${badge}<br/>` +
              `<a href="/${locale}/lugares/${slug}" style="color:#2b4c7e;font-weight:600">${t("verFicha")} →</a>`,
          )
          .addTo(mapa);
      });

      // Zoom al hacer clic en un cluster
      mapa.on("click", "clusters", async (e) => {
        const feature = e.features?.[0];
        if (!feature) return;
        const source = mapa.getSource("lugares") as maplibregl.GeoJSONSource;
        const zoom = await source.getClusterExpansionZoom(
          feature.properties!.cluster_id as number,
        );
        mapa.easeTo({
          center: (feature.geometry as { coordinates: [number, number] }).coordinates,
          zoom,
        });
      });

      mapa.on("mouseenter", "lugares-puntos", () => (mapa.getCanvas().style.cursor = "pointer"));
      mapa.on("mouseleave", "lugares-puntos", () => (mapa.getCanvas().style.cursor = ""));
    });

    return () => {
      mapa.remove();
      mapaRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function alternar3D() {
    const mapa = mapaRef.current;
    if (!mapa) return;
    const nuevo = !en3D;
    setEn3D(nuevo);
    mapa.easeTo({ pitch: nuevo ? PITCH_3D : 0, duration: 800 });
  }

  return (
    <div className="relative h-[70vh] w-full overflow-hidden rounded-2xl border border-border">
      <div ref={contenedor} className="h-full w-full" />
      {con3D && (
        <button
          type="button"
          onClick={alternar3D}
          className="absolute bottom-4 left-4 min-h-11 rounded-full bg-background px-4 text-sm font-semibold shadow-md hover:bg-muted"
        >
          {en3D ? "2D" : "3D"}
        </button>
      )}
      {!con3D && (
        <p className="absolute bottom-4 left-4 rounded-full bg-background/90 px-4 py-2 text-xs shadow">
          {t("sinKey")}
        </p>
      )}
    </div>
  );
}
