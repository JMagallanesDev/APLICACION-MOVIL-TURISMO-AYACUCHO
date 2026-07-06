# Guía de despliegue — esqueleto (Sprint 1)

Estrategia "deploy temprano del esqueleto" (plan v8.0, sección 7.3): cada push
a `main` despliega frontend (Vercel) y backend (Railway) automáticamente.
Flyway aplica las migraciones contra Supabase en el primer arranque.

## Estado: DESPLEGADO (5 jul 2026)

| Componente | URL / detalle |
|---|---|
| API (Railway) | https://web-production-616c6.up.railway.app/api/v1/health |
| Frontend (Vercel) | https://aplicacion-movil-turismo-ayacucho-w.vercel.app |
| BD (Supabase) | São Paulo, Session pooler 5432, PostGIS 3.3.7, migraciones V1–V6 aplicadas |
| Redis (Upstash) | sa-east-1, TLS (`rediss://`) |

## 1. Supabase (base de datos)

1. Crear proyecto nuevo → región `South America (São Paulo)` (la más cercana a Perú).
2. Guardar la contraseña de la BD que defines al crear el proyecto.
3. En **Connect → Session pooler** copiar host, puerto y usuario. Usar el
   **Session pooler** (funciona sobre IPv4 y soporta prepared statements de
   Hibernate; el modo Transaction NO sirve para Flyway/JPA).
4. PostGIS: en **SQL Editor** no hace falta nada — la migración `V1` ejecuta
   `CREATE EXTENSION IF NOT EXISTS postgis;` (Supabase la trae disponible).

Variables resultantes (formato):

```
DATABASE_URL=jdbc:postgresql://aws-0-sa-east-1.pooler.supabase.com:5432/postgres?sslmode=require
DATABASE_USER=postgres.<ref-del-proyecto>
DATABASE_PASSWORD=<contraseña>
```

## 2. Upstash (Redis)

1. Crear cuenta con GitHub en upstash.com → **Create Database** (región AWS
   São Paulo, plan Free).
2. Copiar la URL TLS (empieza con `rediss://default:...@...upstash.io:6379`).

```
REDIS_URL=rediss://default:<password>@<host>.upstash.io:6379
```

## 3. Railway (backend)

1. **New Project → Deploy from GitHub repo** → elegir
   `APLICACION-MOVIL-TURISMO-AYACUCHO`.
2. En **Settings → Root Directory**: `apps/api` (Railway detecta el
   Dockerfile automáticamente).
3. En **Variables** agregar: `DATABASE_URL`, `DATABASE_USER`,
   `DATABASE_PASSWORD`, `REDIS_URL` (valores de los pasos 1 y 2) y
   `SPRING_PROFILES_ACTIVE=prod`.
4. En **Settings → Networking → Generate Domain** para obtener la URL pública.
5. Verificar: `https://<dominio>.up.railway.app/api/v1/health` debe responder
   `{"database":"UP","redis":"UP","status":"UP"}`.

> ⚠️ **Incidencia conocida (resuelta el 5 jul 2026):** al conectar el repo,
> Railway autodetecta el frontend del monorepo y configura start command
> `pnpm start`, build command `pnpm --filter web build` y watch paths
> `/apps/web/**`. El build del Dockerfile pasa, pero el deploy falla con
> `The executable 'pnpm' could not be found`. Solución: **borrar el
> start/build command** en Settings (para que use el ENTRYPOINT del
> Dockerfile) y cambiar los watch paths a `/apps/api/**` (así los cambios
> del frontend o docs no re-despliegan el backend).

> ⚠️ **Zero-downtime (6 jul 2026):** `apps/api/railway.json` define
> `healthcheckPath: /api/v1/health` + `overlapSeconds: 60` para que el
> contenedor viejo siga sirviendo hasta que el nuevo pase el healthcheck.
> **Verificar además en el dashboard**: Settings → Deploy → Healthcheck Path
> debe mostrar `/api/v1/health` (cinturón y tirantes: si el config-as-code
> no se aplicara, el valor del panel manda). Cada deploy debe mostrar el
> paso "Healthcheck" en su log antes de pasar a Active.

> ⚠️ **Latencia conocida:** el trial de Railway no permite elegir región y
> el servicio quedó en US West, mientras BD y Redis están en São Paulo
> (~150-180 ms extra por query). Funciona, pero es un punto a favor del
> plan B: Koyeb/Fly.io sí permiten Sudamérica/us-east en free tier.
> Gracias al RNF-39 migrar es mover variables de entorno.

> Plan B (sección 7.6): si el crédito de Railway no alcanza, el MISMO
> Dockerfile se despliega en Koyeb o Fly.io cambiando solo dónde se pegan
> las variables.

## 4. Vercel (frontend)

1. **Add New → Project** → importar el repo.
2. **Root Directory**: `apps/web` (Vercel detecta Next.js y pnpm workspace).
3. Variables: `NEXT_PUBLIC_API_URL=https://<dominio-railway>/api/v1`.
4. Deploy. La URL pública queda como `https://<proyecto>.vercel.app`.

## 5. Verificación final del esqueleto

- [ ] `https://<railway>/api/v1/health` → database UP, redis UP
- [ ] `https://<vercel>.vercel.app` → página Next.js carga
- [ ] Push a `main` → ambos re-despliegan solos
- [ ] En Supabase → Table Editor: se ven las 35 tablas y los 5 lugares demo

## Seguridad

- El admin del seed (`admin@turismohuamanga.pe` / `CambiarEnProd_2026!`) debe
  cambiar de contraseña apenas exista el endpoint de auth (Sprint 2).
- Secrets solo en paneles de Railway/Vercel y `.env.local` (RNF-17).
