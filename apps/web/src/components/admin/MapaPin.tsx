"use client";

import { useEffect, useRef } from "react";
import maplibregl from "maplibre-gl";
import "maplibre-gl/dist/maplibre-gl.css";

/**
 * Selector de coordenadas con pin arrastrable (RF-71/RF-47): toca el mapa o
 * arrastra el pin. Controlado: sigue las coords del padre si cambian.
 */
export default function MapaPin({
  latitud,
  longitud,
  onCambio,
}: {
  latitud: number;
  longitud: number;
  onCambio: (lat: number, lng: number) => void;
}) {
  const contenedor = useRef<HTMLDivElement>(null);
  const marcadorRef = useRef<maplibregl.Marker | null>(null);

  const maptilerKey = process.env.NEXT_PUBLIC_MAPTILER_KEY;

  useEffect(() => {
    if (!contenedor.current || marcadorRef.current) return;
    const mapa = new maplibregl.Map({
      container: contenedor.current,
      style: maptilerKey
        ? `https://api.maptiler.com/maps/streets-v2/style.json?key=${maptilerKey}`
        : "https://demotiles.maplibre.org/style.json",
      center: [longitud, latitud],
      zoom: maptilerKey ? 15 : 12,
      maxBounds: [
        [-75.5, -15.5],
        [-73.0, -12.5],
      ],
    });
    const marcador = new maplibregl.Marker({ draggable: true, color: "#7b1e3c" })
      .setLngLat([longitud, latitud])
      .addTo(mapa);
    marcador.on("dragend", () => {
      const ll = marcador.getLngLat();
      onCambio(ll.lat, ll.lng);
    });
    mapa.on("click", (e) => {
      marcador.setLngLat(e.lngLat);
      onCambio(e.lngLat.lat, e.lngLat.lng);
    });
    marcadorRef.current = marcador;
    return () => {
      mapa.remove();
      marcadorRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Si el padre cambia las coords (ej. al cargar un lugar), mover el pin
  useEffect(() => {
    marcadorRef.current?.setLngLat([longitud, latitud]);
  }, [latitud, longitud]);

  return <div ref={contenedor} className="h-64 w-full overflow-hidden rounded-2xl border border-border" />;
}
