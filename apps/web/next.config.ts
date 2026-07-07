import type { NextConfig } from "next";
import createNextIntlPlugin from "next-intl/plugin";

const withNextIntl = createNextIntlPlugin("./src/i18n/request.ts");

const nextConfig: NextConfig = {
  /**
   * Proxy same-origin hacia el backend: el navegador solo habla con este
   * dominio, así la cookie httpOnly del refresh token (SameSite=Lax) viaja
   * sin fricción cross-site y el API no necesita CORS para el navegador.
   */
  async rewrites() {
    const api = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1";
    return [
      {
        source: "/api/v1/:path*",
        destination: `${api}/:path*`,
      },
    ];
  },
};

export default withNextIntl(nextConfig);
