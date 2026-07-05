-- ============================================================================
-- V5: Seed de catálogos (plan v8.0, Sprint 2)
-- UUIDs fijos con formato v7 válido para reproducibilidad y referencias
-- estables entre migraciones y tests.
-- ============================================================================

-- ---------------------------------------------------------------------------
-- Roles (4)
-- ---------------------------------------------------------------------------
INSERT INTO rol (id, nombre, descripcion) VALUES
('01980000-0000-7000-8000-000000000101', 'VISITANTE', 'Turista sin cuenta registrada'),
('01980000-0000-7000-8000-000000000102', 'USUARIO',   'Turista con cuenta'),
('01980000-0000-7000-8000-000000000103', 'NEGOCIO',   'Dueño de negocio u operador local'),
('01980000-0000-7000-8000-000000000104', 'ADMIN',     'Gestor interno único');

-- ---------------------------------------------------------------------------
-- Administrador inicial
-- Contraseña de desarrollo: CambiarEnProd_2026!  (BCrypt cost 12)
-- IMPORTANTE: cambiarla en producción antes del despliegue público.
-- ---------------------------------------------------------------------------
INSERT INTO usuario (id, email, password_hash, nombre, rol_id, estado) VALUES
('01980000-0000-7000-8000-000000000001', 'admin@turismohuamanga.pe',
 '$2b$12$ZrVvefKDmzAGxEDuYCjW8.4fx42oRj23p2ISS6R2gb0ju0PZt3qiO',
 'Administrador', '01980000-0000-7000-8000-000000000104', 'ACTIVO');

-- ---------------------------------------------------------------------------
-- Provincias de Ayacucho (11) — códigos UBIGEO
-- ---------------------------------------------------------------------------
INSERT INTO provincia (id, codigo, nombre, orden) VALUES
('01980000-0000-7000-8000-000000000201', '0501', 'Huamanga', 1),
('01980000-0000-7000-8000-000000000202', '0502', 'Cangallo', 2),
('01980000-0000-7000-8000-000000000203', '0503', 'Huanca Sancos', 3),
('01980000-0000-7000-8000-000000000204', '0504', 'Huanta', 4),
('01980000-0000-7000-8000-000000000205', '0505', 'La Mar', 5),
('01980000-0000-7000-8000-000000000206', '0506', 'Lucanas', 6),
('01980000-0000-7000-8000-000000000207', '0507', 'Parinacochas', 7),
('01980000-0000-7000-8000-000000000208', '0508', 'Páucar del Sara Sara', 8),
('01980000-0000-7000-8000-000000000209', '0509', 'Sucre', 9),
('01980000-0000-7000-8000-000000000210', '0510', 'Víctor Fajardo', 10),
('01980000-0000-7000-8000-000000000211', '0511', 'Vilcas Huamán', 11);

-- ---------------------------------------------------------------------------
-- Distritos de Huamanga relevantes para el MVP (el resto se inserta cuando
-- haya contenido; escalar = INSERT, sin cambios estructurales)
-- ---------------------------------------------------------------------------
INSERT INTO distrito (id, provincia_id, codigo, nombre) VALUES
('01980000-0000-7000-8000-000000000301', '01980000-0000-7000-8000-000000000201', '050101', 'Ayacucho'),
('01980000-0000-7000-8000-000000000302', '01980000-0000-7000-8000-000000000201', '050104', 'Carmen Alto'),
('01980000-0000-7000-8000-000000000303', '01980000-0000-7000-8000-000000000201', '050110', 'San Juan Bautista'),
('01980000-0000-7000-8000-000000000304', '01980000-0000-7000-8000-000000000201', '050115', 'Jesús Nazareno'),
('01980000-0000-7000-8000-000000000305', '01980000-0000-7000-8000-000000000201', '050116', 'Andrés Avelino Cáceres Dorregaray'),
('01980000-0000-7000-8000-000000000306', '01980000-0000-7000-8000-000000000201', '050108', 'Quinua'),
('01980000-0000-7000-8000-000000000307', '01980000-0000-7000-8000-000000000201', '050107', 'Pacaycasa');

-- ---------------------------------------------------------------------------
-- Categorías de lugar (8) + traducciones es/en
-- ---------------------------------------------------------------------------
INSERT INTO categoria_lugar (id, codigo, icono, color_hex, orden) VALUES
('01980000-0000-7000-8000-000000000401', 'IGLESIAS',              'church',    '#7b1e3c', 1),
('01980000-0000-7000-8000-000000000402', 'MIRADORES',             'mountain',  '#2b4c7e', 2),
('01980000-0000-7000-8000-000000000403', 'MUSEOS',                'landmark',  '#8a5a3b', 3),
('01980000-0000-7000-8000-000000000404', 'PLAZAS',                'trees',     '#c9942a', 4),
('01980000-0000-7000-8000-000000000405', 'SITIOS_ARQUEOLOGICOS',  'pyramid',   '#6d4c2f', 5),
('01980000-0000-7000-8000-000000000406', 'CASONAS',               'building',  '#9c3d54', 6),
('01980000-0000-7000-8000-000000000407', 'TALLERES_ARTESANALES',  'hammer',    '#b06e2c', 7),
('01980000-0000-7000-8000-000000000408', 'NATURALEZA',            'leaf',      '#3e7048', 8);

INSERT INTO categoria_lugar_traduccion (categoria_lugar_id, idioma, nombre) VALUES
('01980000-0000-7000-8000-000000000401', 'es', 'Iglesias y templos'),
('01980000-0000-7000-8000-000000000401', 'en', 'Churches and temples'),
('01980000-0000-7000-8000-000000000402', 'es', 'Miradores'),
('01980000-0000-7000-8000-000000000402', 'en', 'Viewpoints'),
('01980000-0000-7000-8000-000000000403', 'es', 'Museos'),
('01980000-0000-7000-8000-000000000403', 'en', 'Museums'),
('01980000-0000-7000-8000-000000000404', 'es', 'Plazas y espacios públicos'),
('01980000-0000-7000-8000-000000000404', 'en', 'Squares and public spaces'),
('01980000-0000-7000-8000-000000000405', 'es', 'Sitios arqueológicos'),
('01980000-0000-7000-8000-000000000405', 'en', 'Archaeological sites'),
('01980000-0000-7000-8000-000000000406', 'es', 'Casonas coloniales'),
('01980000-0000-7000-8000-000000000406', 'en', 'Colonial mansions'),
('01980000-0000-7000-8000-000000000407', 'es', 'Talleres artesanales'),
('01980000-0000-7000-8000-000000000407', 'en', 'Artisan workshops'),
('01980000-0000-7000-8000-000000000408', 'es', 'Naturaleza'),
('01980000-0000-7000-8000-000000000408', 'en', 'Nature');

-- ---------------------------------------------------------------------------
-- Categorías de negocio (7) + traducciones es/en
-- ---------------------------------------------------------------------------
INSERT INTO categoria_negocio (id, codigo, icono, orden) VALUES
('01980000-0000-7000-8000-000000000501', 'RESTAURANTES',     'utensils',  1),
('01980000-0000-7000-8000-000000000502', 'HOSPEDAJES',       'bed',       2),
('01980000-0000-7000-8000-000000000503', 'ARTESANIAS',       'palette',   3),
('01980000-0000-7000-8000-000000000504', 'CAFETERIAS',       'coffee',    4),
('01980000-0000-7000-8000-000000000505', 'AGENCIAS_TURISMO', 'map',       5),
('01980000-0000-7000-8000-000000000506', 'TRANSPORTE',       'bus',       6),
('01980000-0000-7000-8000-000000000507', 'ENTRETENIMIENTO',  'music',     7);

INSERT INTO categoria_negocio_traduccion (categoria_negocio_id, idioma, nombre) VALUES
('01980000-0000-7000-8000-000000000501', 'es', 'Restaurantes'),
('01980000-0000-7000-8000-000000000501', 'en', 'Restaurants'),
('01980000-0000-7000-8000-000000000502', 'es', 'Hospedajes'),
('01980000-0000-7000-8000-000000000502', 'en', 'Lodging'),
('01980000-0000-7000-8000-000000000503', 'es', 'Artesanías'),
('01980000-0000-7000-8000-000000000503', 'en', 'Handicrafts'),
('01980000-0000-7000-8000-000000000504', 'es', 'Cafeterías'),
('01980000-0000-7000-8000-000000000504', 'en', 'Coffee shops'),
('01980000-0000-7000-8000-000000000505', 'es', 'Agencias de turismo'),
('01980000-0000-7000-8000-000000000505', 'en', 'Tour agencies'),
('01980000-0000-7000-8000-000000000506', 'es', 'Transporte'),
('01980000-0000-7000-8000-000000000506', 'en', 'Transportation'),
('01980000-0000-7000-8000-000000000507', 'es', 'Entretenimiento'),
('01980000-0000-7000-8000-000000000507', 'en', 'Entertainment');

-- ---------------------------------------------------------------------------
-- Tipos de incidente (7) + traducciones es/en (RF-70)
-- ---------------------------------------------------------------------------
INSERT INTO tipo_incidente (id, codigo, icono, color_hex) VALUES
('01980000-0000-7000-8000-000000000601', 'VANDALISMO',          'spray-can',      '#c0392b'),
('01980000-0000-7000-8000-000000000602', 'GRAFITI',             'paintbrush',     '#8e44ad'),
('01980000-0000-7000-8000-000000000603', 'DANO_ESTRUCTURAL',    'construction',   '#d35400'),
('01980000-0000-7000-8000-000000000604', 'BASURA_ACUMULADA',    'trash',          '#7f8c8d'),
('01980000-0000-7000-8000-000000000605', 'COMERCIO_INFORMAL',   'store',          '#f39c12'),
('01980000-0000-7000-8000-000000000606', 'CONSTRUCCION_ILEGAL', 'hard-hat',       '#2c3e50'),
('01980000-0000-7000-8000-000000000607', 'OTRO',                'alert-triangle', '#95a5a6');

INSERT INTO tipo_incidente_traduccion (tipo_incidente_id, idioma, nombre) VALUES
('01980000-0000-7000-8000-000000000601', 'es', 'Vandalismo'),
('01980000-0000-7000-8000-000000000601', 'en', 'Vandalism'),
('01980000-0000-7000-8000-000000000602', 'es', 'Grafiti en patrimonio'),
('01980000-0000-7000-8000-000000000602', 'en', 'Graffiti on heritage'),
('01980000-0000-7000-8000-000000000603', 'es', 'Daño estructural'),
('01980000-0000-7000-8000-000000000603', 'en', 'Structural damage'),
('01980000-0000-7000-8000-000000000604', 'es', 'Basura acumulada'),
('01980000-0000-7000-8000-000000000604', 'en', 'Accumulated garbage'),
('01980000-0000-7000-8000-000000000605', 'es', 'Comercio informal invasivo'),
('01980000-0000-7000-8000-000000000605', 'en', 'Invasive informal commerce'),
('01980000-0000-7000-8000-000000000606', 'es', 'Construcción ilegal'),
('01980000-0000-7000-8000-000000000606', 'en', 'Illegal construction'),
('01980000-0000-7000-8000-000000000607', 'es', 'Otro'),
('01980000-0000-7000-8000-000000000607', 'en', 'Other');

-- ---------------------------------------------------------------------------
-- Insignias base (8) + traducciones es/en (RF-39b; el esquema queda listo
-- aunque la feature esté recortada del MVP comprimido)
-- ---------------------------------------------------------------------------
INSERT INTO insignia (id, codigo, icono, criterio, orden) VALUES
('01980000-0000-7000-8000-000000000701', 'PRIMER_PASO',        'footprints', '{"tipo":"checkins_total","cantidad":1}',                          1),
('01980000-0000-7000-8000-000000000702', 'EXPLORADOR',         'compass',    '{"tipo":"checkins_total","cantidad":10}',                         2),
('01980000-0000-7000-8000-000000000703', 'EMBAJADOR',          'crown',      '{"tipo":"checkins_total","cantidad":25}',                         3),
('01980000-0000-7000-8000-000000000704', 'DEVOTO',             'church',     '{"tipo":"checkins_categoria","categoria":"IGLESIAS","cantidad":5}', 4),
('01980000-0000-7000-8000-000000000705', 'FOTOGRAFO',          'camera',     '{"tipo":"fotos_aprobadas","cantidad":5}',                         5),
('01980000-0000-7000-8000-000000000706', 'CRITICO',            'star',       '{"tipo":"resenas_publicadas","cantidad":5}',                      6),
('01980000-0000-7000-8000-000000000707', 'GUARDIAN',           'shield',     '{"tipo":"reportes_aprobados","cantidad":1}',                      7),
('01980000-0000-7000-8000-000000000708', 'RUTERO',             'route',      '{"tipo":"rutas_completadas","cantidad":1}',                       8);

INSERT INTO insignia_traduccion (insignia_id, idioma, nombre, descripcion) VALUES
('01980000-0000-7000-8000-000000000701', 'es', 'Primer paso', 'Hiciste tu primer check-in'),
('01980000-0000-7000-8000-000000000701', 'en', 'First step', 'You made your first check-in'),
('01980000-0000-7000-8000-000000000702', 'es', 'Explorador', 'Visitaste 10 lugares'),
('01980000-0000-7000-8000-000000000702', 'en', 'Explorer', 'You visited 10 places'),
('01980000-0000-7000-8000-000000000703', 'es', 'Embajador de Huamanga', 'Visitaste 25 lugares'),
('01980000-0000-7000-8000-000000000703', 'en', 'Huamanga Ambassador', 'You visited 25 places'),
('01980000-0000-7000-8000-000000000704', 'es', 'Devoto', 'Visitaste 5 iglesias'),
('01980000-0000-7000-8000-000000000704', 'en', 'Devotee', 'You visited 5 churches'),
('01980000-0000-7000-8000-000000000705', 'es', 'Fotógrafo', '5 fotos tuyas fueron aprobadas'),
('01980000-0000-7000-8000-000000000705', 'en', 'Photographer', '5 of your photos were approved'),
('01980000-0000-7000-8000-000000000706', 'es', 'Crítico', 'Publicaste 5 reseñas'),
('01980000-0000-7000-8000-000000000706', 'en', 'Critic', 'You published 5 reviews'),
('01980000-0000-7000-8000-000000000707', 'es', 'Guardián del patrimonio', 'Tu primer reporte fue aprobado'),
('01980000-0000-7000-8000-000000000707', 'en', 'Heritage Guardian', 'Your first report was approved'),
('01980000-0000-7000-8000-000000000708', 'es', 'Rutero', 'Completaste una ruta temática'),
('01980000-0000-7000-8000-000000000708', 'en', 'Trail Blazer', 'You completed a themed route');
