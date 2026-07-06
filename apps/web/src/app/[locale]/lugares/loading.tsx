/** Skeleton del listado (RF-96): evita CLS mientras carga (RNF-24). */
export default function CargandoLugares() {
  return (
    <div className="mx-auto max-w-6xl animate-pulse px-6 py-6">
      <div className="h-7 w-48 rounded bg-muted" />
      <div className="mt-10 h-9 w-64 rounded bg-muted" />
      <div className="mt-4 h-11 w-full max-w-md rounded-full bg-muted" />
      <div className="mt-4 flex gap-2">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="h-8 w-24 rounded-full bg-muted" />
        ))}
      </div>
      <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="h-44 rounded-2xl bg-muted" />
        ))}
      </div>
    </div>
  );
}
