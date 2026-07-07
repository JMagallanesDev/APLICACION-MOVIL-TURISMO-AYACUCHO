"use client";

import { useEffect, useRef } from "react";
import maplibregl from "maplibre-gl";
import "maplibre-gl/dist/maplibre-gl.css";
import { useTranslations } from "next-intl";
import type { IncidenteMapa } from "@/lib/api";

/** Mapa público de incidentes aprobados (RF-74). */
export default function MapaIncidentes({ incidentes }: { incidentes: IncidenteMapa[] }) {
  const t = useTranslations("MapaIncidentes");
  const contenedor = useRef<HTMLDivElement>(null);
  const mapaRef = useRef<maplibregl.Map | null>(null);

  const maptilerKey = process.env.NEXT_PUBLIC_MAPTILER_KEY;

  useEffect(() => {
    if (!contenedor.current || mapaRef.current) return;
    const mapa = new maplibregl.Map({
      container: contenedor.current,
      style: maptilerKey
        ? `https://api.maptiler.com/maps/streets-v2/style.json?key=${maptilerKey}`
        : "https://demotiles.maplibre.org/style.json",
      center: [-74.2236, -13.1631],
      zoom: maptilerKey ? 14 : 12,
      maxBounds: [
        [-75.5, -15.5],
        [-73.0, -12.5],
      ],
    });
    mapaRef.current = mapa;
    mapa.addControl(new maplibregl.NavigationControl(), "top-right");

    for (const inc of incidentes) {
      const el = document.createElement("div");
      el.style.cssText = `width:18px;height:18px;border-radius:50%;border:2px solid #fff;
        box-shadow:0 1px 4px rgba(0,0,0,.4);background:${inc.colorHex ?? "#c0392b"}`;
      new maplibregl.Marker({ element: el })
        .setLngLat([inc.longitud, inc.latitud])
        .setPopup(
          new maplibregl.Popup({ offset: 12 }).setHTML(
            `<strong>${t(`tipos.${inc.tipoIncidenteCodigo}`)}</strong><br/>` +
              `<span style="opacity:.7">${t(`estados.${inc.estado}`)}</span>`,
          ),
        )
        .addTo(mapa);
    }
    return () => {
      mapa.remove();
      mapaRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="relative h-[70vh] w-full overflow-hidden rounded-2xl border border-border">
      <div ref={contenedor} className="h-full w-full" />
      {incidentes.length === 0 && (
        <p className="absolute left-1/2 top-4 -translate-x-1/2 rounded-full bg-background/90 px-4 py-2 text-sm shadow">
          {t("sinIncidentes")}
        </p>
      )}
    </div>
  );
}
