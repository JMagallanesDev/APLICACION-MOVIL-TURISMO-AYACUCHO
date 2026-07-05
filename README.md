# Sistema de Turismo Inteligente Huamanga

PWA mobile-first para descubrir lugares patrimoniales de Huamanga (Ayacucho, Perú):
recomendaciones contextuales, mapa 3D, clima, agenda cultural, directorio de negocios
y reporte ciudadano de atentados al patrimonio.

**Tesis** — Universidad Nacional de San Cristóbal de Huamanga · Ingeniería de Sistemas.

## Stack

| Capa | Tecnología |
|---|---|
| Frontend | Next.js 15 (App Router) + TypeScript + Tailwind CSS v4 |
| Backend | Spring Boot 3.5 + Java 21 (Temurin) |
| Base de datos | PostgreSQL 16 + PostGIS (Docker local / Supabase prod) |
| Caché | Redis (Docker local / Upstash prod) |
| Multimedia | Cloudinary · Mapas: MapLibre GL + MapTiler · Clima: OpenWeatherMap |

Documentos: `docs/PLAN_SPRINTS_COMPRIMIDO.md` (calendario vigente) y `PLAN_DE_DESARROLLO_v8.pdf` (plan maestro).

## Requisitos

- Node.js 20+ y pnpm 9+
- Java 21 (Temurin)
- Docker Desktop
- Git

## Cómo correr el proyecto

```bash
# 1. Levantar PostgreSQL + PostGIS y Redis locales
docker compose up -d

# 2. Instalar dependencias del frontend
pnpm install

# 3. Frontend (http://localhost:3000)
pnpm --filter web dev

# 4. Backend (http://localhost:8080/api/v1)  — en otra terminal
cd apps/api
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run

# Verificación de salud (BD + Redis)
curl http://localhost:8080/api/v1/health
```

## Estructura

```
apps/
  web/   Next.js 15 — src/app, src/styles/tokens.css (paleta Ayacucho)
  api/   Spring Boot 3.5 — src/main/java/com/huamanga/tourism
docs/    Planificación y decisiones
docker-compose.yml   PostgreSQL+PostGIS (5432) y Redis (6379)
```

## Convenciones

- Ramas: `feature/{RF}-descripcion`, `fix/descripcion`; Conventional Commits en español.
- Secrets solo en `.env.local` (nunca en Git — RNF-17).
- Cero hex en componentes (solo `tokens.css`), cero textos hardcodeados (next-intl).
