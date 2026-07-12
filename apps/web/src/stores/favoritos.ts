import { create } from "zustand";

/**
 * Slugs favoritos del usuario (RF-35): se sincronizan al iniciar sesión y se
 * actualizan de forma optimista al marcar/quitar. Solo estado de UI; la
 * verdad vive en el backend.
 */
interface EstadoFavoritos {
  slugs: string[];
  cargado: boolean;
  establecer: (slugs: string[]) => void;
  agregar: (slug: string) => void;
  quitar: (slug: string) => void;
  limpiar: () => void;
}

export const useFavoritos = create<EstadoFavoritos>((set) => ({
  slugs: [],
  cargado: false,
  establecer: (slugs) => set({ slugs, cargado: true }),
  agregar: (slug) => set((s) => ({ slugs: [...s.slugs, slug] })),
  quitar: (slug) => set((s) => ({ slugs: s.slugs.filter((x) => x !== slug) })),
  limpiar: () => set({ slugs: [], cargado: false }),
}));
