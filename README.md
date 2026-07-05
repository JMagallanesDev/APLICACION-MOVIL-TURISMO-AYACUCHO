# Sistema de Turismo Inteligente Huamanga

PWA mobile-first para descubrir lugares patrimoniales de Huamanga (Ayacucho, Perú):
recomendaciones contextuales, mapa 3D, clima, agenda cultural, directorio de negocios
y reporte ciudadano de atentados al patrimonio.


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
  web/   Next.js — src/app, src/styles/tokens.css (paleta Ayacucho)
  api/   Spring Boot 3.5 — src/main/java/com/huamanga/tourism
docs/    Planificación y decisiones
docker-compose.yml   PostgreSQL+PostGIS (5432) y Redis (6379)
```

### Arquitectura del backend (plan v8.0, sección 5.2 y RNF-31)

Paquetes **por feature del dominio**, y dentro de cada feature **carpetas por
capa** — cada cosa en su lugar:

```
com.huamanga.tourism/
├─ lugar/                  ← módulo del dominio
│  ├─ dominio/             ← entidades JPA, enums, IDs compuestos
│  ├─ repository/          ← Spring Data JPA
│  ├─ service/             ← lógica de negocio      (desde Sprint 2)
│  ├─ controller/          ← endpoints REST         (desde Sprint 2)
│  ├─ dto/                 ← Request/Response       (desde Sprint 2)
│  └─ mapper/              ← MapStruct              (desde Sprint 2)
├─ usuario/  evento/  reporte/  negocio/  ruta/  insignia/ ...
└─ common/                 ← EntidadBase, controller de salud, config
```

Regla: un Controller nunca toca un Repository directamente; siempre pasa por
el Service. Las entidades no salen del API: los DTOs son el contrato.

## Convenciones

- Ramas: `feature/{RF}-descripcion`, `fix/descripcion`; Conventional Commits en español.
- Secrets solo en `.env.local`.
- Cero hex en componentes (solo `tokens.css`), cero textos hardcodeados (next-intl).
