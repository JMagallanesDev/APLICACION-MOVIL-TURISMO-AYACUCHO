import { setRequestLocale } from "next-intl/server";
import { Link } from "@/i18n/navigation";
import Encabezado from "@/components/Encabezado";
import GuardAdmin from "@/components/admin/GuardAdmin";

/** El panel de administración es interno: interfaz solo en español. */
export default async function AdminLayout({
  children,
  params,
}: {
  children: React.ReactNode;
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);

  return (
    <div className="mx-auto max-w-6xl px-6 py-6">
      <Encabezado />
      <GuardAdmin>
        <nav className="mt-8 flex gap-2 text-sm">
          <Link
            href="/admin"
            className="rounded-full border border-border px-4 py-2 hover:bg-muted"
          >
            Resumen
          </Link>
          <Link
            href="/admin/moderacion"
            className="rounded-full border border-border px-4 py-2 hover:bg-muted"
          >
            Moderación
          </Link>
        </nav>
        <div className="mt-6">{children}</div>
      </GuardAdmin>
    </div>
  );
}
