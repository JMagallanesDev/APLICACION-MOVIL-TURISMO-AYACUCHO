# Calendario comprimido de sprints — v8.1 (re-baseline 5 jul 2026)

El plan de desarrollo v8.0 definía 15 sprints del 18 may al 13 dic 2026. El desarrollo
real inicia el **6 de julio de 2026**, por lo que se comprime a **11 sprints de 2 semanas**
manteniendo la sustentación en diciembre 2026. La compresión se ejecuta con el criterio
MoSCoW del propio plan (sección 4.2): los COULD se recortan desde el inicio y se
re-incorporan solo si hay adelanto. **Esto es una decisión de gestión ágil documentada,
no un incumplimiento** (sección 4.2 del plan v8.0).

## Recortes aplicados desde el inicio (todos COULD)

| RF | Nombre | Estado |
|---|---|---|
| RF-12 | Videos de festividades | Recortado (re-incorporable: es un embed) |
| RF-29 | Planificador de visitas por fecha | Recortado |
| RF-39 / RF-39b | Check-in GPS + pasaporte patrimonial | Recortado (primer candidato a volver si hay adelanto) |
| RF-52b | Analítica de tráfico | Recortado |
| RF-53 | CRUD de rutas temáticas | Rutas por seed, sin CRUD |
| RF-64 | Detección fr/de | Solo es/en |
| RF-88 | Clima pronosticado del evento | Recortado |
| RF-95 | Microinteracciones | Recortado (quedan las de Tailwind por defecto) |

El núcleo MUST queda intacto: módulos 1, 2, 3, 5, 6, 7, 8 (preservación ciudadana,
diferenciador), 10, más RF-08, RF-09b, RF-09c. SHOULD se mantiene: clima, agenda
(+RF-84b), negocios, slider RF-11/11b, dashboard RF-52, RF-09d, RF-19b.

## Calendario (11 sprints)

| Sprint | Fechas | Objetivo | Origen (plan v8.0) |
|---|---|---|---|
| 1 | 6 jul – 19 jul | Esqueleto desplegado (Vercel/Railway o plan B) + **modelo BD completo 31 entidades + Flyway + seed** | S1 + S2 |
| 2 | 20 jul – 2 ago | Autenticación JWT completa (access en memoria, refresh httpOnly, BCrypt, roles, rate limiting) | S3 |
| 3 | 3 ago – 16 ago | i18n base (next-intl es/en) + CRUD lugares backend (HorarioLugar, datos prácticos) + Swagger + webhook ISR | S4 |
| 4 | 17 ago – 30 ago | Listado, detalle y búsqueda frontend + badge abierto/cerrado + distancia a pie + dark mode desde ya | S5 |
| 5 | 31 ago – 13 sep | Mapa MapLibre 3D + clima con Resilience4j + rutas (seed) + RF-08 "¿Qué hago ahora?" + RF-19b "Estoy aquí" | S6 |
| 6 | 14 sep – 27 sep | Reseñas + calificaciones (vista materializada) + fotos Cloudinary + moderación | S7 |
| 7 | 28 sep – 11 oct | Preservación ciudadana completa (anti-spam Redis TTL, sin IP) + favoritos + reporte de contenido | S8 (recortado) + S9 |
| 8 | 12 oct – 25 oct | Agenda cultural completa + vista "Durante mi visita" (RF-84b) | S10 |
| 9 | 26 oct – 8 nov | Panel admin: dashboard Chart.js, usuarios/roles, audit log + registro de negocios (backend) | S11 (sin RF-52b) + inicio S12 |
| 10 | 9 nov – 22 nov | Directorio de negocios completo + slider RF-11/11b + compartir + identidad visual final + **CIERRE DE ALCANCE** | S12 + S13 |
| 11 | 23 nov – 6 dic | Pulido: Lighthouse ≥ 90, JMeter local, WCAG AA, tests ≥ 70% + documentación, datos reales, video demo | S14 + S15 |
| — | 7 dic – 13 dic | Semana de reserva: ensayos de sustentación, backup, entornos congelados. **Sin código nuevo.** | S15 |

## Reglas que NO cambian

- Semáforo quincenal: dos amarillos consecutivos → recorte adicional (siguientes candidatos: RF-11b, RF-09d, RF-19b).
- Cierre de alcance en el Sprint 10 (22 nov). Después: solo pulido, cero features.
- Cada sprint termina con entregable demostrable + clip de 2 minutos.
- Tests desde el Sprint 2; la cobertura no se "arregla al final".

## Correcciones técnicas al plan v8.0 detectadas en la revisión del 5 jul 2026

1. **PWA descartada (decisión del 18 jul 2026)**: la tesis se redefinió como **aplicación web** (no PWA). Se elimina el service worker/offline del alcance: el contenido es inherentemente en línea (mapa, clima en tiempo real, reseñas, agenda), por lo que el modo offline aportaba poco valor frente a su costo de mantenimiento. La app sigue siendo responsive y usable desde el navegador móvil.
2. **RNF-26 / Sprint 14**: `i18next-parser` no aplica (el stack es next-intl) → validar claves con script propio en CI.
3. **Access token 24 h** es largo → evaluar 15–60 min (el refresh de 7 d cubre la UX). Decidir en Sprint 2.
4. **Railway** ya no tiene free tier real → presupuestar 5 USD/mes desde el inicio o validar Koyeb/Fly.io en Sprint 1 (el plan B de la sección 7.6).
5. **Spring Boot**: se usa 3.5.x (última 3.x) y no 4.x, por madurez del ecosistema (ShedLock, SpringDoc, tutoriales). Documentado como decisión.
