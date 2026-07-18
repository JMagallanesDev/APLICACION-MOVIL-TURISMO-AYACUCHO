import type { MetadataRoute } from "next";
import { SITIO_URL } from "@/lib/sitio";

/** robots.txt (SEO): el panel admin y las cuentas no se indexan. */
export default function robots(): MetadataRoute.Robots {
  return {
    rules: [
      {
        userAgent: "*",
        allow: "/",
        disallow: ["/es/admin", "/en/admin", "/es/cuenta", "/en/cuenta", "/api/"],
      },
    ],
    sitemap: `${SITIO_URL}/sitemap.xml`,
  };
}
