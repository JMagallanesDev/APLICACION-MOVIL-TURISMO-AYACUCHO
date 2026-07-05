-- ============================================================================
-- V2: Esquema base — modelo de datos completo (plan v8.0, sección 6)
-- Convenciones: UUID v7 como PK (generado en backend con uuid-creator),
-- auditoría explícita, soft delete, estados como VARCHAR con CHECK,
-- multi-idioma vía tablas de traducción, coordenadas PostGIS SRID 4326.
-- Nota: el plan dice "31 entidades" pero su lista por grupos (6.2) suma 35
-- tablas; se implementa la lista completa.
-- Nota: se usa geometry(Point,4326) también donde el plan menciona GEOGRAPHY
-- (punto_captura) para mapeo uniforme con hibernate-spatial; las distancias
-- se calculan con ST_DistanceSphere o cast a geography en la consulta.
-- ============================================================================

-- ---------------------------------------------------------------------------
-- Grupo 0 — Jerarquía geográfica
-- ---------------------------------------------------------------------------
CREATE TABLE provincia (
    id          UUID PRIMARY KEY,
    codigo      VARCHAR(10)  NOT NULL UNIQUE, -- UBIGEO
    nombre      VARCHAR(100) NOT NULL,
    orden       INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE distrito (
    id            UUID PRIMARY KEY,
    provincia_id  UUID         NOT NULL REFERENCES provincia(id),
    codigo        VARCHAR(10)  NOT NULL UNIQUE, -- UBIGEO
    nombre        VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------------
-- Grupo 1 — Núcleo de usuarios
-- ---------------------------------------------------------------------------
CREATE TABLE rol (
    id          UUID PRIMARY KEY,
    nombre      VARCHAR(30)  NOT NULL UNIQUE, -- VISITANTE | USUARIO | NEGOCIO | ADMIN
    descripcion VARCHAR(200),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE usuario (
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE
                  CHECK (email ~* '^[^@[:space:]]+@[^@[:space:]]+\.[^@[:space:]]+$'),
    password_hash VARCHAR(100) NOT NULL,
    nombre        VARCHAR(120) NOT NULL,
    rol_id        UUID         NOT NULL REFERENCES rol(id),
    estado        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO'
                  CHECK (estado IN ('ACTIVO','SUSPENDIDO','ELIMINADO')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at    TIMESTAMPTZ
);

CREATE TABLE refresh_token (
    id          UUID PRIMARY KEY,
    usuario_id  UUID         NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    token_hash  VARCHAR(100) NOT NULL UNIQUE,
    expira_en   TIMESTAMPTZ  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------------
-- Grupo 2 — Lugares y su categorización
-- ---------------------------------------------------------------------------
CREATE TABLE categoria_lugar (
    id         UUID PRIMARY KEY,
    codigo     VARCHAR(30) NOT NULL UNIQUE,
    icono      VARCHAR(50),
    color_hex  VARCHAR(7),
    orden      INT         NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE categoria_lugar_traduccion (
    categoria_lugar_id UUID         NOT NULL REFERENCES categoria_lugar(id) ON DELETE CASCADE,
    idioma             VARCHAR(5)   NOT NULL, -- es | en
    nombre             VARCHAR(100) NOT NULL,
    updated_by         UUID REFERENCES usuario(id),
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (categoria_lugar_id, idioma)
);

CREATE TABLE lugar (
    id                         UUID PRIMARY KEY,
    slug                       VARCHAR(150) NOT NULL UNIQUE,
    categoria_lugar_id         UUID         NOT NULL REFERENCES categoria_lugar(id),
    distrito_id                UUID         NOT NULL REFERENCES distrito(id),
    ubicacion                  geometry(Point,4326) NOT NULL
                               CHECK (ST_X(ubicacion) BETWEEN -75.5 AND -73.0
                                  AND ST_Y(ubicacion) BETWEEN -15.5 AND -12.5), -- RF-22b bounds Ayacucho
    direccion                  VARCHAR(255),
    telefono                   VARCHAR(30),
    precio_entrada_pen         NUMERIC(8,2) CHECK (precio_entrada_pen >= 0),
    duracion_visita_min        INT          CHECK (duracion_visita_min > 0),  -- RF-09c
    acepta_tarjeta             BOOLEAN,                                       -- RF-09d (nullables)
    tiene_banos                BOOLEAN,
    accesible_silla_ruedas     BOOLEAN,
    apto_ninos                 BOOLEAN,
    costo_taxi_desde_plaza_pen NUMERIC(6,2) CHECK (costo_taxi_desde_plaza_pen >= 0),
    requiere_guia              BOOLEAN,
    estado                     VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR'
                               CHECK (estado IN ('BORRADOR','PUBLICADO','ARCHIVADO')),
    created_by                 UUID REFERENCES usuario(id),
    updated_by                 UUID REFERENCES usuario(id),
    created_at                 TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                 TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at                 TIMESTAMPTZ
);

-- Horarios estructurados por día y turno (RF-09b/RF-08/RF-29).
-- Varias filas por día = varios turnos (mañana/tarde, típico de iglesias).
CREATE TABLE horario_lugar (
    id            UUID PRIMARY KEY,
    lugar_id      UUID        NOT NULL REFERENCES lugar(id) ON DELETE CASCADE,
    dia_semana    SMALLINT    NOT NULL CHECK (dia_semana BETWEEN 0 AND 6), -- 0=domingo
    hora_apertura TIME,
    hora_cierre   TIME,
    cerrado       BOOLEAN     NOT NULL DEFAULT FALSE,
    updated_by    UUID REFERENCES usuario(id),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (cerrado = TRUE OR (hora_apertura IS NOT NULL AND hora_cierre IS NOT NULL
                              AND hora_apertura < hora_cierre))
);

CREATE TABLE lugar_traduccion (
    lugar_id    UUID         NOT NULL REFERENCES lugar(id) ON DELETE CASCADE,
    idioma      VARCHAR(5)   NOT NULL,
    nombre      VARCHAR(150) NOT NULL,
    descripcion TEXT,
    historia    TEXT,
    consejos    TEXT, -- RF-09d, traducible
    updated_by  UUID REFERENCES usuario(id),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (lugar_id, idioma)
);

CREATE TABLE lugar_imagen_historica (
    id                  UUID PRIMARY KEY,
    lugar_id            UUID         NOT NULL REFERENCES lugar(id) ON DELETE CASCADE,
    titulo              VARCHAR(150),
    url_historica       VARCHAR(500) NOT NULL,
    public_id_historica VARCHAR(255),
    anio_historico      INT CHECK (anio_historico BETWEEN 1500 AND 1990), -- límite fijo inmutable;
                        -- la regla "≥ 50 años de antigüedad" se valida en Bean Validation
    url_actual          VARCHAR(500),
    public_id_actual    VARCHAR(255),
    credito_historico   VARCHAR(255),
    punto_captura       geometry(Point,4326), -- RF-11b "Párate aquí" (nullable)
    orden               INT          NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------------
-- Grupo 3 — Contenido generado por usuarios
-- ---------------------------------------------------------------------------
CREATE TABLE resena (
    id           UUID PRIMARY KEY,
    usuario_id   UUID        NOT NULL REFERENCES usuario(id),
    lugar_id     UUID        NOT NULL REFERENCES lugar(id),
    calificacion SMALLINT    NOT NULL CHECK (calificacion BETWEEN 1 AND 5),
    comentario   VARCHAR(500) CHECK (LENGTH(comentario) <= 500),
    estado       VARCHAR(20) NOT NULL DEFAULT 'PUBLICADA'
                 CHECK (estado IN ('PUBLICADA','OCULTA','ELIMINADA','EN_REVISION')),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT idx_resena_unique UNIQUE (usuario_id, lugar_id)
);

CREATE TABLE foto (
    id                   UUID PRIMARY KEY,
    usuario_id           UUID         NOT NULL REFERENCES usuario(id),
    lugar_id             UUID         NOT NULL REFERENCES lugar(id),
    cloudinary_url       VARCHAR(500) NOT NULL,
    cloudinary_public_id VARCHAR(255) NOT NULL,
    estado               VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE'
                         CHECK (estado IN ('PENDIENTE','APROBADA','RECHAZADA','EN_REVISION')),
    motivo_rechazo       VARCHAR(300),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE favorito (
    usuario_id UUID        NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    lugar_id   UUID        NOT NULL REFERENCES lugar(id)   ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (usuario_id, lugar_id)
);

CREATE TABLE check_in (
    id            UUID PRIMARY KEY,
    usuario_id    UUID        NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    lugar_id      UUID        NOT NULL REFERENCES lugar(id)   ON DELETE CASCADE,
    ubicacion_gps geometry(Point,4326),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Dos FKs nullables + CHECK de exactamente una no nula (anti-patrón polimórfico
-- descartado; decisión 6.6 del plan). UNIQUEs parciales en V3.
CREATE TABLE reporte_contenido (
    id         UUID PRIMARY KEY,
    usuario_id UUID         NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    foto_id    UUID REFERENCES foto(id)   ON DELETE CASCADE,
    resena_id  UUID REFERENCES resena(id) ON DELETE CASCADE,
    motivo     VARCHAR(300) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CHECK ((foto_id IS NOT NULL AND resena_id IS NULL)
        OR (foto_id IS NULL AND resena_id IS NOT NULL))
);

-- ---------------------------------------------------------------------------
-- Grupo 4 — Agenda cultural
-- ---------------------------------------------------------------------------
CREATE TABLE evento (
    id                     UUID PRIMARY KEY,
    lugar_id               UUID REFERENCES lugar(id), -- opcional
    distrito_id            UUID        NOT NULL REFERENCES distrito(id),
    tipo                   VARCHAR(30) NOT NULL
                           CHECK (tipo IN ('FESTIVIDAD_RELIGIOSA','CONCIERTO','FERIA',
                                           'GASTRONOMICO','CULTURAL','DEPORTIVO','OTRO')),
    fecha_inicio           DATE        NOT NULL,
    fecha_fin              DATE        NOT NULL,
    cloudinary_url_portada VARCHAR(500),
    recurrente_anual       BOOLEAN     NOT NULL DEFAULT FALSE,
    estado                 VARCHAR(20) NOT NULL DEFAULT 'BORRADOR'
                           CHECK (estado IN ('BORRADOR','PUBLICADO','CANCELADO','ARCHIVADO')),
    created_by             UUID REFERENCES usuario(id),
    updated_by             UUID REFERENCES usuario(id),
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at             TIMESTAMPTZ,
    CHECK (fecha_inicio <= fecha_fin)
);

CREATE TABLE evento_traduccion (
    evento_id   UUID         NOT NULL REFERENCES evento(id) ON DELETE CASCADE,
    idioma      VARCHAR(5)   NOT NULL,
    nombre      VARCHAR(150) NOT NULL,
    descripcion TEXT,
    organizador VARCHAR(150), -- vive aquí para soportar traducción
    updated_by  UUID REFERENCES usuario(id),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (evento_id, idioma)
);

-- ---------------------------------------------------------------------------
-- Grupo 5 — Preservación ciudadana (diferenciador)
-- ---------------------------------------------------------------------------
CREATE TABLE tipo_incidente (
    id         UUID PRIMARY KEY,
    codigo     VARCHAR(30) NOT NULL UNIQUE,
    icono      VARCHAR(50),
    color_hex  VARCHAR(7),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE tipo_incidente_traduccion (
    tipo_incidente_id UUID         NOT NULL REFERENCES tipo_incidente(id) ON DELETE CASCADE,
    idioma            VARCHAR(5)   NOT NULL,
    nombre            VARCHAR(100) NOT NULL,
    updated_by        UUID REFERENCES usuario(id),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (tipo_incidente_id, idioma)
);

-- NO almacena IP ni hash de IP bajo ninguna forma: el anti-spam vive
-- exclusivamente en Redis con TTL 24 h (privacidad por diseño, sección 6.6).
CREATE TABLE reporte (
    id                    UUID PRIMARY KEY,
    tipo_incidente_id     UUID        NOT NULL REFERENCES tipo_incidente(id),
    usuario_id            UUID REFERENCES usuario(id), -- nullable: reporte anónimo
    nombre_reportante     VARCHAR(120),
    descripcion           TEXT        NOT NULL,
    ubicacion             geometry(Point,4326) NOT NULL
                          CHECK (ST_X(ubicacion) BETWEEN -75.5 AND -73.0
                             AND ST_Y(ubicacion) BETWEEN -15.5 AND -12.5),
    direccion_referencial VARCHAR(255),
    estado                VARCHAR(20) NOT NULL DEFAULT 'RECIBIDO'
                          CHECK (estado IN ('RECIBIDO','EN_REVISION','APROBADO',
                                            'DESCARTADO','RESUELTO')),
    notas_admin           TEXT,
    es_anonimo            BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE foto_reporte (
    id                   UUID PRIMARY KEY,
    reporte_id           UUID         NOT NULL REFERENCES reporte(id) ON DELETE CASCADE,
    cloudinary_url       VARCHAR(500) NOT NULL,
    cloudinary_public_id VARCHAR(255),
    orden                INT          NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------------
-- Grupo 6 — Rutas temáticas
-- ---------------------------------------------------------------------------
CREATE TABLE ruta_tematica (
    id         UUID PRIMARY KEY,
    slug       VARCHAR(150) NOT NULL UNIQUE,
    color_hex  VARCHAR(7),
    icono      VARCHAR(50),
    activa     BOOLEAN      NOT NULL DEFAULT TRUE,
    orden      INT          NOT NULL DEFAULT 0,
    created_by UUID REFERENCES usuario(id),
    updated_by UUID REFERENCES usuario(id),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE TABLE ruta_traduccion (
    ruta_tematica_id UUID         NOT NULL REFERENCES ruta_tematica(id) ON DELETE CASCADE,
    idioma           VARCHAR(5)   NOT NULL,
    nombre           VARCHAR(150) NOT NULL,
    descripcion      TEXT,
    updated_by       UUID REFERENCES usuario(id),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (ruta_tematica_id, idioma)
);

CREATE TABLE lugar_ruta (
    ruta_tematica_id UUID NOT NULL REFERENCES ruta_tematica(id) ON DELETE CASCADE,
    lugar_id         UUID NOT NULL REFERENCES lugar(id)         ON DELETE CASCADE,
    orden            INT  NOT NULL DEFAULT 0, -- orden de visita
    PRIMARY KEY (ruta_tematica_id, lugar_id)
);

-- ---------------------------------------------------------------------------
-- Grupo 7 — Directorio de negocios y auditoría
-- ---------------------------------------------------------------------------
CREATE TABLE categoria_negocio (
    id         UUID PRIMARY KEY,
    codigo     VARCHAR(30) NOT NULL UNIQUE,
    icono      VARCHAR(50),
    orden      INT         NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE categoria_negocio_traduccion (
    categoria_negocio_id UUID         NOT NULL REFERENCES categoria_negocio(id) ON DELETE CASCADE,
    idioma               VARCHAR(5)   NOT NULL,
    nombre               VARCHAR(100) NOT NULL,
    updated_by           UUID REFERENCES usuario(id),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (categoria_negocio_id, idioma)
);

CREATE TABLE negocio (
    id                   UUID PRIMARY KEY,
    usuario_id           UUID         NOT NULL REFERENCES usuario(id), -- gestor
    categoria_negocio_id UUID         NOT NULL REFERENCES categoria_negocio(id),
    distrito_id          UUID         NOT NULL REFERENCES distrito(id),
    nombre               VARCHAR(150) NOT NULL,
    ruc                  VARCHAR(11)  CHECK (ruc ~ '^[0-9]{11}$'),
    telefono             VARCHAR(30),
    whatsapp             VARCHAR(20),
    direccion            VARCHAR(255),
    ubicacion            geometry(Point,4326),
    horario              VARCHAR(255), -- texto simple: no participa en "abierto ahora"
    estado               VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE'
                         CHECK (estado IN ('PENDIENTE','APROBADO','RECHAZADO','SUSPENDIDO')),
    created_by           UUID REFERENCES usuario(id),
    updated_by           UUID REFERENCES usuario(id),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at           TIMESTAMPTZ
);

CREATE TABLE negocio_traduccion (
    negocio_id  UUID        NOT NULL REFERENCES negocio(id) ON DELETE CASCADE,
    idioma      VARCHAR(5)  NOT NULL,
    descripcion TEXT,
    updated_by  UUID REFERENCES usuario(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (negocio_id, idioma)
);

-- Log inmutable del admin (RF-56). Aquí la IP sí se guarda: es auditoría
-- interna del administrador, no del usuario público.
CREATE TABLE registro_actividad (
    id         UUID PRIMARY KEY,
    usuario_id UUID        NOT NULL REFERENCES usuario(id),
    accion     VARCHAR(50) NOT NULL,
    entidad    VARCHAR(50) NOT NULL,
    entidad_id UUID,
    detalles   JSONB,
    ip         VARCHAR(45),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------------
-- Grupo 8 — Analítica de tráfico (tablas de hechos agregadas por día;
-- los eventos crudos no se persisten por diseño: privacidad y volumen)
-- ---------------------------------------------------------------------------
CREATE TABLE visita_resumen_diario (
    id             UUID PRIMARY KEY,
    tipo_pagina    VARCHAR(30) NOT NULL
                   CHECK (tipo_pagina IN ('HOME','LUGARES','LUGAR_DETALLE','MAPA',
                                          'EVENTOS','REPORTES','NEGOCIOS')),
    fecha          DATE        NOT NULL,
    total_visitas  INT         NOT NULL DEFAULT 0,
    visitas_unicas INT         NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT idx_visita_resumen_unique UNIQUE (tipo_pagina, fecha)
);

CREATE TABLE visita_negocio_diario (
    id                UUID PRIMARY KEY,
    negocio_id        UUID        NOT NULL REFERENCES negocio(id) ON DELETE CASCADE,
    fecha             DATE        NOT NULL,
    total_visitas     INT         NOT NULL DEFAULT 0,
    clics_whatsapp    INT         NOT NULL DEFAULT 0,
    clics_como_llegar INT         NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT idx_visita_negocio_unique UNIQUE (negocio_id, fecha)
);

-- ---------------------------------------------------------------------------
-- Grupo 9 — Gamificación
-- ---------------------------------------------------------------------------
CREATE TABLE insignia (
    id         UUID PRIMARY KEY,
    codigo     VARCHAR(50) NOT NULL UNIQUE,
    icono      VARCHAR(50),
    criterio   JSONB       NOT NULL, -- ej. {"tipo":"checkins_categoria","categoria":"IGLESIAS","cantidad":5}
    orden      INT         NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE insignia_traduccion (
    insignia_id UUID         NOT NULL REFERENCES insignia(id) ON DELETE CASCADE,
    idioma      VARCHAR(5)   NOT NULL,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(300),
    updated_by  UUID REFERENCES usuario(id),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (insignia_id, idioma)
);

-- El progreso por ruta NO se almacena (violaría 3FN): se calcula con COUNT
-- sobre check_in x lugar_ruta. Solo se persiste el hecho inmutable.
CREATE TABLE insignia_usuario (
    usuario_id  UUID        NOT NULL REFERENCES usuario(id)  ON DELETE CASCADE,
    insignia_id UUID        NOT NULL REFERENCES insignia(id) ON DELETE CASCADE,
    obtenida_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (usuario_id, insignia_id)
);
