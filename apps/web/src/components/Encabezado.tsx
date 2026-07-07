import { Link } from "@/i18n/navigation";
import MenuUsuario from "./MenuUsuario";
import SelectorIdioma from "./SelectorIdioma";

/** Encabezado compartido: marca + idioma + estado de sesión. */
export default function Encabezado() {
  return (
    <header className="flex items-center justify-between gap-4">
      <Link href="/" className="text-lg font-semibold text-primary">
        Turismo Huamanga
      </Link>
      <div className="flex items-center gap-3">
        <MenuUsuario />
        <SelectorIdioma />
      </div>
    </header>
  );
}
