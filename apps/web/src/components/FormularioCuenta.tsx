"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { useRouter } from "@/i18n/navigation";
import { iniciarSesion, registrarse, type ErrorApi } from "@/lib/apiCliente";
import { useSesion } from "@/stores/sesion";

/** Iniciar sesión / crear cuenta (RF-31/RF-32) con validación y mensajes claros. */
export default function FormularioCuenta() {
  const t = useTranslations("Cuenta");
  const router = useRouter();
  const establecerSesion = useSesion((s) => s.establecerSesion);

  const [modo, setModo] = useState<"login" | "registro">("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [nombre, setNombre] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function enviar(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setEnviando(true);
    try {
      const datos =
        modo === "login"
          ? await iniciarSesion(email, password)
          : await registrarse(email, password, nombre);
      establecerSesion(datos.usuario, datos.accessToken);
      router.push("/lugares");
      router.refresh();
    } catch (err) {
      setError((err as ErrorApi).mensaje ?? t("errorGenerico"));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="mx-auto mt-10 w-full max-w-sm">
      <div className="mb-6 flex rounded-full border border-border p-1">
        {(["login", "registro"] as const).map((m) => (
          <button
            key={m}
            type="button"
            onClick={() => {
              setModo(m);
              setError(null);
            }}
            aria-pressed={modo === m}
            className={`min-h-9 flex-1 rounded-full text-sm font-medium transition-colors ${
              modo === m ? "bg-primary text-primary-foreground" : "hover:bg-muted"
            }`}
          >
            {t(m === "login" ? "iniciarSesion" : "crearCuenta")}
          </button>
        ))}
      </div>

      <form onSubmit={enviar} className="flex flex-col gap-3">
        {modo === "registro" && (
          <label className="flex flex-col gap-1 text-sm">
            {t("nombre")}
            <input
              type="text"
              required
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              className="min-h-11 rounded-xl border border-border bg-background px-4 outline-none focus:border-primary"
            />
          </label>
        )}
        <label className="flex flex-col gap-1 text-sm">
          {t("email")}
          <input
            type="email"
            required
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="min-h-11 rounded-xl border border-border bg-background px-4 outline-none focus:border-primary"
          />
        </label>
        <label className="flex flex-col gap-1 text-sm">
          {t("password")}
          <input
            type="password"
            required
            minLength={8}
            autoComplete={modo === "login" ? "current-password" : "new-password"}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="min-h-11 rounded-xl border border-border bg-background px-4 outline-none focus:border-primary"
          />
          {modo === "registro" && (
            <span className="text-xs opacity-60">{t("passwordAyuda")}</span>
          )}
        </label>

        {error && (
          <p role="alert" className="rounded-xl bg-red-600/10 px-4 py-2 text-sm text-red-700 dark:text-red-400">
            {error}
          </p>
        )}

        <button
          type="submit"
          disabled={enviando}
          className="mt-2 min-h-11 rounded-full bg-primary font-medium text-primary-foreground hover:opacity-90 disabled:opacity-60"
        >
          {enviando ? t("enviando") : t(modo === "login" ? "iniciarSesion" : "crearCuenta")}
        </button>
      </form>
    </div>
  );
}
