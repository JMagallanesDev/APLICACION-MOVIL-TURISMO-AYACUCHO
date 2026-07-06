import { createNavigation } from "next-intl/navigation";
import { routing } from "./routing";

/** Link/router conscientes del locale: navegar nunca pierde el idioma. */
export const { Link, redirect, usePathname, useRouter, getPathname } =
  createNavigation(routing);
