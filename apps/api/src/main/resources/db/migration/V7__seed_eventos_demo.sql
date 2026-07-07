-- ============================================================================
-- V7: Eventos demo de la agenda cultural (Módulo 9). Fechas de referencia
-- 2026; el admin las clona cada año (RF-86). distrito Ayacucho del seed V5.
-- ============================================================================

-- 1. Semana Santa de Ayacucho (la mayor festividad, abril)
INSERT INTO evento (id, lugar_id, distrito_id, tipo, fecha_inicio, fecha_fin,
                    recurrente_anual, estado, created_by) VALUES
('01980000-0000-7000-8000-000000000a01',
 '01980000-0000-7000-8000-000000000802', -- Catedral
 '01980000-0000-7000-8000-000000000301',
 'FESTIVIDAD_RELIGIOSA', '2026-03-29', '2026-04-05', TRUE, 'PUBLICADO',
 '01980000-0000-7000-8000-000000000001');

INSERT INTO evento_traduccion (evento_id, idioma, nombre, descripcion, organizador) VALUES
('01980000-0000-7000-8000-000000000a01', 'es', 'Semana Santa de Ayacucho',
 'La celebración religiosa más importante del Perú y una de las más grandes de América. Diez días de procesiones, alfombras de flores y arte sacro en la "Sevilla de América".',
 'Arzobispado de Ayacucho'),
('01980000-0000-7000-8000-000000000a01', 'en', 'Ayacucho Holy Week',
 'Peru''s most important religious celebration and one of the largest in the Americas. Ten days of processions, flower carpets and sacred art in the "Seville of America".',
 'Archdiocese of Ayacucho');

-- 2. Feria de artesanía de Semana Santa
INSERT INTO evento (id, lugar_id, distrito_id, tipo, fecha_inicio, fecha_fin,
                    recurrente_anual, estado, created_by) VALUES
('01980000-0000-7000-8000-000000000a02',
 NULL,
 '01980000-0000-7000-8000-000000000301',
 'FERIA', '2026-04-02', '2026-04-05', TRUE, 'PUBLICADO',
 '01980000-0000-7000-8000-000000000001');

INSERT INTO evento_traduccion (evento_id, idioma, nombre, descripcion, organizador) VALUES
('01980000-0000-7000-8000-000000000a02', 'es', 'Feria de Artesanía de Semana Santa',
 'Retablos, cerámica de Quinua, tallado en piedra de Huamanga y textiles en el corazón de la ciudad.',
 'Municipalidad Provincial de Huamanga'),
('01980000-0000-7000-8000-000000000a02', 'en', 'Holy Week Handicraft Fair',
 'Retablos, Quinua pottery, Huamanga stone carving and textiles in the heart of the city.',
 'Provincial Municipality of Huamanga');

-- 3. Aniversario de la Batalla de Ayacucho (diciembre)
INSERT INTO evento (id, lugar_id, distrito_id, tipo, fecha_inicio, fecha_fin,
                    recurrente_anual, estado, created_by) VALUES
('01980000-0000-7000-8000-000000000a03',
 NULL,
 '01980000-0000-7000-8000-000000000306', -- Quinua
 'CULTURAL', '2026-12-09', '2026-12-09', TRUE, 'PUBLICADO',
 '01980000-0000-7000-8000-000000000001');

INSERT INTO evento_traduccion (evento_id, idioma, nombre, descripcion, organizador) VALUES
('01980000-0000-7000-8000-000000000a03', 'es', 'Aniversario de la Batalla de Ayacucho',
 'Conmemoración de la batalla que selló la independencia de América del Sur (1824), con escenificación en la Pampa de Quinua.',
 'Gobierno Regional de Ayacucho'),
('01980000-0000-7000-8000-000000000a03', 'en', 'Anniversary of the Battle of Ayacucho',
 'Commemoration of the battle that sealed South America''s independence (1824), with a reenactment at the Pampa de Quinua.',
 'Regional Government of Ayacucho');

-- 4. Festival gastronómico (evento futuro cercano de muestra)
INSERT INTO evento (id, lugar_id, distrito_id, tipo, fecha_inicio, fecha_fin,
                    recurrente_anual, estado, created_by) VALUES
('01980000-0000-7000-8000-000000000a04',
 NULL,
 '01980000-0000-7000-8000-000000000301',
 'GASTRONOMICO', '2026-08-15', '2026-08-17', FALSE, 'PUBLICADO',
 '01980000-0000-7000-8000-000000000001');

INSERT INTO evento_traduccion (evento_id, idioma, nombre, descripcion, organizador) VALUES
('01980000-0000-7000-8000-000000000a04', 'es', 'Festival del Puca Picante',
 'Celebración del plato bandera de Ayacucho: puca picante, mondongo, qapchi y más sabores tradicionales.',
 'Asociación de Restaurantes de Huamanga'),
('01980000-0000-7000-8000-000000000a04', 'en', 'Puca Picante Festival',
 'A celebration of Ayacucho''s signature dish: puca picante, mondongo, qapchi and more traditional flavors.',
 'Huamanga Restaurant Association');
