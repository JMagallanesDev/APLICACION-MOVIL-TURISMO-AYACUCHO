import { revalidatePath } from "next/cache";
import { NextRequest, NextResponse } from "next/server";

/**
 * Webhook de revalidación ISR (plan, sección 5.5): el backend lo invoca al
 * guardar contenido en el panel admin para regenerar las páginas afectadas
 * sin esperar el revalidate por tiempo. Protegido por secreto compartido.
 */
export async function POST(req: NextRequest) {
  const secreto = process.env.REVALIDATE_SECRET;
  if (!secreto || req.headers.get("x-revalidate-secret") !== secreto) {
    return NextResponse.json(
      { error: "NO_AUTORIZADO", mensaje: "Secreto de revalidación inválido." },
      { status: 401 },
    );
  }

  let paths: unknown;
  try {
    ({ paths } = await req.json());
  } catch {
    return NextResponse.json(
      { error: "DATOS_INVALIDOS", mensaje: "Body JSON con arreglo 'paths' requerido." },
      { status: 400 },
    );
  }

  if (!Array.isArray(paths) || paths.length === 0) {
    return NextResponse.json(
      { error: "DATOS_INVALIDOS", mensaje: "Se requiere un arreglo 'paths' no vacío." },
      { status: 400 },
    );
  }

  const revalidados: string[] = [];
  for (const path of paths.slice(0, 20)) {
    if (typeof path === "string" && path.startsWith("/")) {
      revalidatePath(path);
      revalidados.push(path);
    }
  }

  return NextResponse.json({ revalidados });
}
