"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { Link, useRouter } from "@/i18n/navigation";
import { crearResena, type ErrorApi } from "@/lib/apiCliente";
import { useSesion } from "@/stores/sesion";

/**
 * Formulario de reseña (RF-37): visible solo para usuarios autenticados; a
 * los anónimos les muestra un enlace a iniciar sesión (navegación anónima
 * permitida, RF-34).
 */
export default function FormularioResena({ slug }: { slug: string }) {
  const t = useTranslations("Lugares");
  const router = useRouter();
  const usuario = useSesion((s) => s.usuario);
  const restaurando = useSesion((s) => s.restaurando);

  const [calificacion, setCalificacion] = useState(0);
  const [hover, setHover] = useState(0);
  const [comentario, setComentario] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [enviada, setEnviada] = useState(false);

  if (restaurando) {
    return <div className="mt-3 h-11 w-full animate-pulse rounded-xl bg-muted" />;
  }

  if (!usuario) {
    return (
      <p className="mt-3 rounded-xl bg-muted px-4 py-3 text-sm">
        <Link href="/cuenta" className="font-medium text-secondary hover:underline">
          {t("iniciaSesionParaResenar")}
        </Link>
      </p>
    );
  }

  if (enviada) {
    return (
      <p className="mt-3 rounded-xl bg-emerald-600/10 px-4 py-3 text-sm text-emerald-700 dark:text-emerald-400">
        {t("resenaEnviada")}
      </p>
    );
  }

  async function enviar(e: React.FormEvent) {
    e.preventDefault();
    if (calificacion < 1) {
      setError(t("eligeCalificacion"));
      return;
    }
    setError(null);
    setEnviando(true);
    try {
      await crearResena(slug, calificacion, comentario);
      setEnviada(true);
      router.refresh(); // revalida la lista de reseñas del server component
    } catch (err) {
      setError((err as ErrorApi).mensaje ?? t("errorResena"));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form onSubmit={enviar} className="mt-3 rounded-2xl border border-border p-4">
      <div className="flex items-center gap-1" role="radiogroup" aria-label={t("tuCalificacion")}>
        {[1, 2, 3, 4, 5].map((n) => (
          <button
            key={n}
            type="button"
            role="radio"
            aria-checked={calificacion === n}
            aria-label={`${n}`}
            onClick={() => setCalificacion(n)}
            onMouseEnter={() => setHover(n)}
            onMouseLeave={() => setHover(0)}
            className="text-2xl leading-none text-accent"
          >
            {n <= (hover || calificacion) ? "★" : <span className="opacity-30">★</span>}
          </button>
        ))}
      </div>

      <textarea
        value={comentario}
        onChange={(e) => setComentario(e.target.value)}
        maxLength={500}
        rows={3}
        placeholder={t("comentarioPlaceholder")}
        className="mt-3 w-full resize-none rounded-xl border border-border bg-background px-4 py-2 text-sm outline-none focus:border-primary"
      />
      <div className="mt-1 text-right text-xs opacity-50">{comentario.length}/500</div>

      {error && (
        <p role="alert" className="text-sm text-red-600">
          {error}
        </p>
      )}

      <button
        type="submit"
        disabled={enviando}
        className="mt-2 min-h-10 rounded-full bg-primary px-6 text-sm font-medium text-primary-foreground hover:opacity-90 disabled:opacity-75"
      >
        {enviando ? t("enviando") : t("publicarResena")}
      </button>
    </form>
  );
}
