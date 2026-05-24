-- ========================================
-- SEED DATA PARA MÓDULO DE MANTENIMIENTO
-- ========================================
-- Ejecutar en SSMS contra SALSIAOREF

-- EQUIPOS
INSERT INTO tbl_EQUIPO (Codigo, Nombre, Area, Marca, Modelo, Fecha_compra, Estado, Observaciones) VALUES
('EQ-001', 'Nevera Industrial 1', 'Cocina', 'Whirlpool', 'WRX-200', '2023-01-15', 'Bueno', 'Nevera principal de cocina'),
('EQ-002', 'Nevera Industrial 2', 'Cocina', 'LG', 'LRN-350', '2023-06-20', 'Regular', 'Enfría lentamente'),
('EQ-003', 'Estufa 6 Hornillas', 'Cocina', 'Whirlpool', 'W6B-500', '2022-11-10', 'Bueno', 'Estufa principal'),
('EQ-004', 'Freidora Industrial', 'Cocina', 'Pitco', 'PT-40', '2024-02-01', 'Bueno', 'Freidora de papas y pollo'),
('EQ-005', 'Extractor de Aire', 'Cocina', 'S&P', 'SP-2000', '2023-09-05', 'Averiado', 'Hace ruido al encender'),
('EQ-006', 'Aire Acondicionado 1', 'Comedor', 'Midea', 'MS-12000', '2023-04-10', 'En reparación', 'No enfria suficiente'),
('EQ-007', 'Aire Acondicionado 2', 'Administración', 'Midea', 'MS-9000', '2024-01-20', 'Bueno', 'Funciona correctamente'),
('EQ-008', 'Computadora Caja', 'Ventas', 'Dell', 'Optiplex 3080', '2022-12-01', 'Bueno', 'PC del punto de venta'),
('EQ-009', 'Impresora Tickets', 'Ventas', 'Epson', 'TM-T20', '2023-03-15', 'Regular', 'A veces no imprime'),
('EQ-010', 'Router Principal', 'Almacén', 'MikroTik', 'RB-750', '2023-07-01', 'Bueno', 'Router del establecimiento'),
('EQ-011', 'Cámara Fría', 'Almacén', 'FríoRex', 'FR-500', '2022-10-01', 'Bueno', 'Almacenamiento de carnes'),
('EQ-012', 'Batidora Industrial', 'Panadería', 'KitchenAid', 'KA-PRO', '2024-05-10', 'Bueno', 'Batidora de masas'),
('EQ-013', 'Horno Convector', 'Panadería', 'Rational', 'RNC-600', '2023-08-20', 'Averiado', 'No calienta uniformemente'),
('EQ-014', 'Microondas', 'Cocina', 'Samsung', 'MS-23K', '2024-03-01', 'Bueno', 'Microondas auxiliar'),
('EQ-015', 'Licuadora Industrial', 'Cocina', 'Vitamix', 'VM-5200', '2024-06-15', 'Bueno', 'Para salsas y batidos');

-- TÉCNICOS (adaptado a la estructura existente de tbl_TECNICO)
INSERT INTO tbl_TECNICO (nombre_tecnico, especialidad, telefono, empresa_proviene) VALUES
('Carlos Pérez', 'Refrigeración', '809-555-0101', 'FríoTotal SRL'),
('Juan Martínez', 'Electricidad', '809-555-0102', ''),
('Pedro Ramírez', 'Gas/Plomería', '809-555-0103', 'GasFix'),
('Ana López', 'Electrónica', '809-555-0104', ''),
('Luis Fernández', 'General', '809-555-0105', ''),
('Roberto Gómez', 'Aire Acondicionado', '809-555-0106', 'ClimaTotal'),
('María Rodríguez', 'Informática', '809-555-0107', 'TechSolutions'),
('José Castillo', 'Mecánica', '809-555-0108', 'MecanicCenter');

-- MANTENIMIENTOS (solicitudes)
INSERT INTO tbl_MANTENIMIENTO (Codigo, fk_ID_equipo, Descripcion_problema, Fecha_reporte, Prioridad, Persona_reporta, fk_ID_tecnico, Estado, Solucion_aplicada, Fecha_solucion, Tiempo_reparacion, Costo_piezas, Costo_mano_obra, Costo_total, Observaciones) VALUES
('MT-0001', 5, 'El extractor hace un ruido fuerte al encender y vibra demasiado', '2025-01-10', 'Alta', 'María González', 1, 'Resuelto', 'Se cambiaron rodamientos y se lubricó el motor', '2025-01-12', '3 horas', 1500, 2000, 3500, 'Reparación exitosa'),
('MT-0002', 6, 'El aire acondicionado del comedor no enfría, solo ventila', '2025-01-15', 'Alta', 'Carlos Méndez', 6, 'Resuelto', 'Se recargó gas refrigerante y se limpió el filtro', '2025-01-17', '2 horas', 2500, 1500, 4000, ''),
('MT-0003', 9, 'La impresora de tickets no imprime correctamente, sale borroso', '2025-01-20', 'Media', 'Rosa Jiménez', 7, 'Resuelto', 'Se limpió el cabezal y se reemplazó el rollo de papel', '2025-01-21', '1 hora', 400, 800, 1200, ''),
('MT-0004', 2, 'La nevera 2 no enfría lo suficiente, temperatura irregular', '2025-02-01', 'Alta', 'Pedro Sánchez', 1, 'En proceso', '', NULL, '', 0, 0, 0, 'Pendiente de reparación'),
('MT-0005', 13, 'El horno no calienta de manera uniforme, se quema un lado', '2025-02-05', 'Media', 'Luisa Fernández', NULL, 'Pendiente', '', NULL, '', 0, 0, 0, 'Sin técnico asignado aún'),
('MT-0006', 4, 'La freidora no enciende, revisar resistencia', '2025-02-10', 'Baja', 'José Pérez', 2, 'Pendiente', '', NULL, '', 0, 0, 0, ''),
('MT-0007', 12, 'La batidora hace un sonido extraño al usarla en velocidad alta', '2025-02-15', 'Media', 'Ana Ramírez', 3, 'En proceso', '', NULL, '', 0, 0, 0, 'Se están consiguiendo repuestos'),
('MT-0008', 8, 'La computadora de caja se apaga aleatoriamente', '2025-02-20', 'Alta', 'Rosa Jiménez', 7, 'Resuelto', 'Se reemplazó la fuente de poder y se limpió el polvo interno', '2025-02-22', '4 horas', 3200, 1500, 4700, ''),
('MT-0009', 11, 'Revisión programada de la cámara fría', '2025-03-01', 'Baja', 'Carlos Méndez', 1, 'Pendiente', '', NULL, '', 0, 0, 0, 'Mantenimiento preventivo'),
('MT-0010', 1, 'La nevera 1 pierde agua por debajo', '2025-03-05', 'Media', 'María González', NULL, 'Pendiente', '', NULL, '', 0, 0, 0, 'Sin técnico asignado');

-- CALENDARIO MANTENIMIENTO PREVENTIVO
INSERT INTO tbl_CALENDARIO_MANTENIMIENTO (fk_ID_equipo, Fecha_programada, Tipo_mantenimiento, fk_ID_tecnico, Estado, Observaciones) VALUES
(1, '2025-04-01', 'Limpieza de condensadores', 1, 'Pendiente', 'Limpieza trimestral'),
(5, '2025-04-05', 'Limpieza de extractor', 3, 'Pendiente', 'Limpieza de ductos y filtros'),
(6, '2025-04-10', 'Mantenimiento AA', 6, 'Pendiente', 'Revisión de gas y filtros'),
(11, '2025-04-15', 'Revisión de nevera', 1, 'Pendiente', 'Revisión de temperatura y puertas'),
(13, '2025-04-20', 'Revisión eléctrica', 2, 'Pendiente', 'Revisión de resistencias'),
(3, '2025-03-25', 'Limpieza general', 5, 'Realizado', 'Limpieza de hornillas y quemadores'),
(8, '2025-03-28', 'Revisión de red', 7, 'Realizado', 'Actualización de software'),
(10, '2025-04-01', 'Revisión de red', 7, 'Pendiente', 'Revisión de cableado y routers'),
(4, '2025-04-12', 'Cambio de piezas', 2, 'Pendiente', 'Cambio de resistencia'),
(2, '2025-04-08', 'Revisión de nevera', 1, 'Pendiente', 'Diagnóstico de temperatura');

-- Verificar datos insertados
SELECT 'EQUIPOS' AS Tabla, COUNT(*) AS Filas FROM tbl_EQUIPO
UNION ALL SELECT 'TECNICOS', COUNT(*) FROM tbl_TECNICO
UNION ALL SELECT 'MANTENIMIENTOS', COUNT(*) FROM tbl_MANTENIMIENTO
UNION ALL SELECT 'CALENDARIO', COUNT(*) FROM tbl_CALENDARIO_MANTENIMIENTO;
