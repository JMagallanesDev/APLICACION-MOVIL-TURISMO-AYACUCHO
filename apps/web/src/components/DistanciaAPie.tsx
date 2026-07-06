"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";

/**
 * Distancia y tiempo a pie desde la posición del usuario (RF-09c).
 * Haversine en cliente; velocidad de caminata 3.4 km/h, ajustada por la
 * altitud de Huamanga (2,760 msnm) frente a los 4.5 km/h estándar.
 */
const VELOCIDAD_KMH_ALTURA = 3.4;

function haversineKm(lat1: number, lon1: number, lat2: number, lon2: number) {
  const R = 6371;
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLon / 2) ** 2;
  return 2 * R * Math.asin(Math.sqrt(a));
}

export default function DistanciaAPie({
  latitud,
  longitud,
}: {
  latitud: number;
  longitud: number;
}) {
  const t = useTranslations("Lugares");
  const [estado, setEstado] = useState<"inicial" | "cargando" | "error">("inicial");
  const [resultado, setResultado] = useState<{ km: number; min: number } | null>(null);

  function calcular() {
    if (!navigator.geolocation) {
      setEstado("error");
      return;
    }
    setEstado("cargando");
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const km = haversineKm(pos.coords.latitude, pos.coords.longitude, latitud, longitud);
        setResultado({ km, min: Math.round((km / VELOCIDAD_KMH_ALTURA) * 60) });
        setEstado("inicial");
      },
      () => setEstado("error"),
      { enableHighAccuracy: true, timeout: 10_000 },
    );
  }

  if (resultado) {
    return (
      <p className="text-sm font-medium text-secondary">
        {t("distanciaAPie", {
          min: resultado.min,
          km: resultado.km.toFixed(resultado.km < 10 ? 1 : 0),
        })}
      </p>
    );
  }

  return (
    <div className="flex flex-col gap-1">
      <button
        type="button"
        onClick={calcular}
        disabled={estado === "cargando"}
        className="min-h-11 w-fit rounded-full border border-border px-4 text-sm font-medium hover:bg-muted disabled:opacity-60"
      >
        {estado === "cargando" ? t("calculando") : t("cuantoCaminando")}
      </button>
      {estado === "error" && (
        <span className="text-xs text-red-600">{t("errorUbicacion")}</span>
      )}
    </div>
  );
}
