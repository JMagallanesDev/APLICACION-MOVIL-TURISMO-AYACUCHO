import { create } from "zustand";
import { persist } from "zustand/middleware";

/**
 * Fechas de viaje del turista (RF-84b "Durante mi visita"): se guardan en
 * localStorage SIN cuenta (plan, sección 5.6). Permiten filtrar la agenda a
 * los eventos que coinciden con la estadía.
 */
interface EstadoViaje {
  desde: string | null;
  hasta: string | null;
  establecer: (desde: string, hasta: string) => void;
  limpiar: () => void;
}

export const useViaje = create<EstadoViaje>()(
  persist(
    (set) => ({
      desde: null,
      hasta: null,
      establecer: (desde, hasta) => set({ desde, hasta }),
      limpiar: () => set({ desde: null, hasta: null }),
    }),
    { name: "fechas-viaje" },
  ),
);
