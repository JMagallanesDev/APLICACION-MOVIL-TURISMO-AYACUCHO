-- ============================================================================
-- V6: Lugares demo (5) con traducciones ES/EN y horarios estructurados,
-- más 1 ruta temática de seed (RF-53 recortado a seed en el plan comprimido).
-- Coordenadas aproximadas del centro de Ayacucho; datos de precios/horarios
-- referenciales de desarrollo: verificar en campo antes del Sprint 11 (carga real).
-- dia_semana: 0=domingo ... 6=sábado. Días sin fila = cerrado ese día.
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 1. Plaza Mayor de Ayacucho
-- ---------------------------------------------------------------------------
INSERT INTO lugar (id, slug, categoria_lugar_id, distrito_id, ubicacion, direccion,
                   precio_entrada_pen, duracion_visita_min, acepta_tarjeta, tiene_banos,
                   accesible_silla_ruedas, apto_ninos, costo_taxi_desde_plaza_pen,
                   requiere_guia, estado, created_by) VALUES
('01980000-0000-7000-8000-000000000801', 'plaza-mayor-de-ayacucho',
 '01980000-0000-7000-8000-000000000404', '01980000-0000-7000-8000-000000000301',
 ST_SetSRID(ST_MakePoint(-74.22357, -13.16307), 4326),
 'Portal Municipal s/n, centro histórico', 0, 45, NULL, TRUE, TRUE, TRUE, 0, FALSE,
 'PUBLICADO', '01980000-0000-7000-8000-000000000001');

INSERT INTO lugar_traduccion (lugar_id, idioma, nombre, descripcion, historia, consejos) VALUES
('01980000-0000-7000-8000-000000000801', 'es', 'Plaza Mayor de Ayacucho',
 'Corazón del centro histórico, rodeada de portales de piedra y la Catedral. Punto de partida ideal para recorrer la ciudad.',
 'Trazada en el siglo XVI tras la fundación española de San Juan de la Frontera de Huamanga en 1540. Fue escenario de la proclamación de la independencia tras la batalla de Ayacucho (1824).',
 'Visítala al atardecer cuando se encienden los portales. Los domingos hay izamiento de bandera a las 8:00.'),
('01980000-0000-7000-8000-000000000801', 'en', 'Ayacucho Main Square',
 'Heart of the historic center, surrounded by stone arcades and the Cathedral. The ideal starting point to explore the city.',
 'Laid out in the 16th century after the Spanish founding of San Juan de la Frontera de Huamanga in 1540. It witnessed the proclamation of independence after the Battle of Ayacucho (1824).',
 'Visit at sunset when the arcades light up. On Sundays there is a flag-raising ceremony at 8:00 AM.');

-- Abierta todos los días, 24 horas
INSERT INTO horario_lugar (id, lugar_id, dia_semana, hora_apertura, hora_cierre, cerrado)
SELECT gen_random_uuid(), '01980000-0000-7000-8000-000000000801', d, '00:00', '23:59', FALSE
FROM generate_series(0, 6) AS d;

-- ---------------------------------------------------------------------------
-- 2. Catedral de Ayacucho
-- ---------------------------------------------------------------------------
INSERT INTO lugar (id, slug, categoria_lugar_id, distrito_id, ubicacion, direccion,
                   precio_entrada_pen, duracion_visita_min, acepta_tarjeta, tiene_banos,
                   accesible_silla_ruedas, apto_ninos, costo_taxi_desde_plaza_pen,
                   requiere_guia, estado, created_by) VALUES
('01980000-0000-7000-8000-000000000802', 'catedral-de-ayacucho',
 '01980000-0000-7000-8000-000000000401', '01980000-0000-7000-8000-000000000301',
 ST_SetSRID(ST_MakePoint(-74.22321, -13.16350), 4326),
 'Portal Municipal 46, Plaza Mayor', 0, 40, NULL, FALSE, FALSE, TRUE, 0, FALSE,
 'PUBLICADO', '01980000-0000-7000-8000-000000000001');

INSERT INTO lugar_traduccion (lugar_id, idioma, nombre, descripcion, historia, consejos) VALUES
('01980000-0000-7000-8000-000000000802', 'es', 'Catedral Basílica de Santa María',
 'Catedral barroca del siglo XVII con retablos dorados en pan de oro, una de las joyas del "Sevilla peruana" y sede central de la Semana Santa ayacuchana.',
 'Construida entre 1632 y 1672 por orden del obispo Cristóbal de Castilla y Zamora. Combina fachada sobria de piedra con un interior barroco de retablos churriguerescos.',
 'Entra durante la misa de la mañana para verla iluminada. En Semana Santa es el epicentro de las procesiones.'),
('01980000-0000-7000-8000-000000000802', 'en', 'Cathedral Basilica of Saint Mary',
 'A 17th-century baroque cathedral with gold-leaf altarpieces, one of the jewels of the "Peruvian Seville" and the heart of Ayacucho''s Holy Week.',
 'Built between 1632 and 1672 by order of Bishop Cristóbal de Castilla y Zamora. It combines a sober stone facade with a baroque interior of Churrigueresque altarpieces.',
 'Enter during morning mass to see it illuminated. During Holy Week it is the epicenter of the processions.');

-- Mar–Dom: 9–12 y 16–18. Lunes cerrado (fila explícita).
INSERT INTO horario_lugar (id, lugar_id, dia_semana, hora_apertura, hora_cierre, cerrado)
SELECT gen_random_uuid(), '01980000-0000-7000-8000-000000000802', d, '09:00', '12:00', FALSE
FROM generate_series(0, 6) AS d WHERE d <> 1;
INSERT INTO horario_lugar (id, lugar_id, dia_semana, hora_apertura, hora_cierre, cerrado)
SELECT gen_random_uuid(), '01980000-0000-7000-8000-000000000802', d, '16:00', '18:00', FALSE
FROM generate_series(0, 6) AS d WHERE d <> 1;
INSERT INTO horario_lugar (id, lugar_id, dia_semana, hora_apertura, hora_cierre, cerrado) VALUES
(gen_random_uuid(), '01980000-0000-7000-8000-000000000802', 1, NULL, NULL, TRUE);

-- ---------------------------------------------------------------------------
-- 3. Templo de Santo Domingo
-- ---------------------------------------------------------------------------
INSERT INTO lugar (id, slug, categoria_lugar_id, distrito_id, ubicacion, direccion,
                   precio_entrada_pen, duracion_visita_min, acepta_tarjeta, tiene_banos,
                   accesible_silla_ruedas, apto_ninos, costo_taxi_desde_plaza_pen,
                   requiere_guia, estado, created_by) VALUES
('01980000-0000-7000-8000-000000000803', 'templo-de-santo-domingo',
 '01980000-0000-7000-8000-000000000401', '01980000-0000-7000-8000-000000000301',
 ST_SetSRID(ST_MakePoint(-74.22463, -13.16147), 4326),
 'Jr. 9 de Diciembre, cuadra 2', 0, 30, NULL, FALSE, FALSE, TRUE, 3.00, FALSE,
 'PUBLICADO', '01980000-0000-7000-8000-000000000001');

INSERT INTO lugar_traduccion (lugar_id, idioma, nombre, descripcion, historia, consejos) VALUES
('01980000-0000-7000-8000-000000000803', 'es', 'Templo de Santo Domingo',
 'Uno de los templos más antiguos de Ayacucho (1548), con una fachada de estilo renacentista y campanario de tres cuerpos.',
 'Fundado en 1548 por los dominicos. Según la tradición, en su atrio se estrenó el primer toque de campanas por la victoria de la batalla de Ayacucho.',
 'Combínalo con un paseo por el Jr. 9 de Diciembre hacia el Arco del Triunfo.'),
('01980000-0000-7000-8000-000000000803', 'en', 'Santo Domingo Temple',
 'One of the oldest temples in Ayacucho (1548), with a Renaissance-style facade and a three-body bell tower.',
 'Founded in 1548 by the Dominicans. According to tradition, the first bell toll celebrating the victory of the Battle of Ayacucho rang from its atrium.',
 'Combine it with a walk along 9 de Diciembre street towards the Arch of Triumph.');

INSERT INTO horario_lugar (id, lugar_id, dia_semana, hora_apertura, hora_cierre, cerrado)
SELECT gen_random_uuid(), '01980000-0000-7000-8000-000000000803', d, '09:00', '12:30', FALSE
FROM generate_series(0, 6) AS d;

-- ---------------------------------------------------------------------------
-- 4. Mirador de Acuchimay
-- ---------------------------------------------------------------------------
INSERT INTO lugar (id, slug, categoria_lugar_id, distrito_id, ubicacion, direccion,
                   precio_entrada_pen, duracion_visita_min, acepta_tarjeta, tiene_banos,
                   accesible_silla_ruedas, apto_ninos, costo_taxi_desde_plaza_pen,
                   requiere_guia, estado, created_by) VALUES
('01980000-0000-7000-8000-000000000804', 'mirador-de-acuchimay',
 '01980000-0000-7000-8000-000000000402', '01980000-0000-7000-8000-000000000302',
 ST_SetSRID(ST_MakePoint(-74.22689, -13.17246), 4326),
 'Cerro Acuchimay, Carmen Alto', 0, 60, NULL, TRUE, FALSE, TRUE, 6.00, FALSE,
 'PUBLICADO', '01980000-0000-7000-8000-000000000001');

INSERT INTO lugar_traduccion (lugar_id, idioma, nombre, descripcion, historia, consejos) VALUES
('01980000-0000-7000-8000-000000000804', 'es', 'Mirador de Acuchimay',
 'El mejor mirador de la ciudad: vista panorámica de los 33 templos de Huamanga y los cerros que la rodean, coronado por la estatua del Cristo Blanco.',
 'El cerro Acuchimay fue un antiguo adoratorio prehispánico. El mirador actual y su cristo se construyeron en el siglo XX y es tradición subir en Semana Santa.',
 'Sube al atardecer para la mejor luz. Lleva abrigo: el viento corre fuerte después de las 17:00.'),
('01980000-0000-7000-8000-000000000804', 'en', 'Acuchimay Viewpoint',
 'The best viewpoint in the city: panoramic views of Huamanga''s 33 temples and the surrounding hills, crowned by the White Christ statue.',
 'Acuchimay hill was an ancient pre-Hispanic shrine. The current viewpoint and its Christ statue were built in the 20th century; climbing it during Holy Week is a tradition.',
 'Go up at sunset for the best light. Bring a jacket: the wind picks up after 5:00 PM.');

INSERT INTO horario_lugar (id, lugar_id, dia_semana, hora_apertura, hora_cierre, cerrado)
SELECT gen_random_uuid(), '01980000-0000-7000-8000-000000000804', d, '06:00', '19:00', FALSE
FROM generate_series(0, 6) AS d;

-- ---------------------------------------------------------------------------
-- 5. Museo de la Memoria
-- ---------------------------------------------------------------------------
INSERT INTO lugar (id, slug, categoria_lugar_id, distrito_id, ubicacion, direccion,
                   precio_entrada_pen, duracion_visita_min, acepta_tarjeta, tiene_banos,
                   accesible_silla_ruedas, apto_ninos, costo_taxi_desde_plaza_pen,
                   requiere_guia, estado, created_by) VALUES
('01980000-0000-7000-8000-000000000805', 'museo-de-la-memoria',
 '01980000-0000-7000-8000-000000000403', '01980000-0000-7000-8000-000000000301',
 ST_SetSRID(ST_MakePoint(-74.22661, -13.15473), 4326),
 'Prolongación Libertad 1229', 2.00, 50, FALSE, TRUE, FALSE, FALSE, 5.00, FALSE,
 'PUBLICADO', '01980000-0000-7000-8000-000000000001');

INSERT INTO lugar_traduccion (lugar_id, idioma, nombre, descripcion, historia, consejos) VALUES
('01980000-0000-7000-8000-000000000805', 'es', 'Museo de la Memoria',
 'Museo dedicado a la memoria de las víctimas del conflicto armado interno (1980-2000), creado por la asociación ANFASEP.',
 'Fundado en 2005 por las madres de ANFASEP, pioneras en la búsqueda de justicia. Sus salas recogen testimonios, objetos y retablos de la memoria.',
 'Visita respetuosa: es un espacio de memoria. Ideal combinarlo con guía local para el contexto histórico.'),
('01980000-0000-7000-8000-000000000805', 'en', 'Museum of Memory',
 'Museum dedicated to the memory of the victims of the internal armed conflict (1980-2000), created by the ANFASEP association.',
 'Founded in 2005 by the mothers of ANFASEP, pioneers in the search for justice. Its rooms gather testimonies, objects and memory retablos.',
 'A respectful visit: this is a memorial space. Best combined with a local guide for historical context.');

-- Lun–Vie 9–13 y 14–17; Sáb 9–13; Dom cerrado
INSERT INTO horario_lugar (id, lugar_id, dia_semana, hora_apertura, hora_cierre, cerrado)
SELECT gen_random_uuid(), '01980000-0000-7000-8000-000000000805', d, '09:00', '13:00', FALSE
FROM generate_series(1, 6) AS d;
INSERT INTO horario_lugar (id, lugar_id, dia_semana, hora_apertura, hora_cierre, cerrado)
SELECT gen_random_uuid(), '01980000-0000-7000-8000-000000000805', d, '14:00', '17:00', FALSE
FROM generate_series(1, 5) AS d;
INSERT INTO horario_lugar (id, lugar_id, dia_semana, hora_apertura, hora_cierre, cerrado) VALUES
(gen_random_uuid(), '01980000-0000-7000-8000-000000000805', 0, NULL, NULL, TRUE);

-- ---------------------------------------------------------------------------
-- Ruta temática de seed: "Ruta del centro histórico"
-- ---------------------------------------------------------------------------
INSERT INTO ruta_tematica (id, slug, color_hex, icono, activa, orden, created_by) VALUES
('01980000-0000-7000-8000-000000000901', 'ruta-del-centro-historico',
 '#7b1e3c', 'landmark', TRUE, 1, '01980000-0000-7000-8000-000000000001');

INSERT INTO ruta_traduccion (ruta_tematica_id, idioma, nombre, descripcion) VALUES
('01980000-0000-7000-8000-000000000901', 'es', 'Ruta del centro histórico',
 'Recorrido a pie por el corazón colonial de Huamanga: plazas, templos y miradores en medio día.'),
('01980000-0000-7000-8000-000000000901', 'en', 'Historic Center Route',
 'A walking tour through the colonial heart of Huamanga: squares, temples and viewpoints in half a day.');

INSERT INTO lugar_ruta (ruta_tematica_id, lugar_id, orden) VALUES
('01980000-0000-7000-8000-000000000901', '01980000-0000-7000-8000-000000000801', 1),
('01980000-0000-7000-8000-000000000901', '01980000-0000-7000-8000-000000000802', 2),
('01980000-0000-7000-8000-000000000901', '01980000-0000-7000-8000-000000000803', 3),
('01980000-0000-7000-8000-000000000901', '01980000-0000-7000-8000-000000000804', 4);
