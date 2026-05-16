-- =============================================
-- SCRIPT COMPLETO DE BASE DE DATOS - SALSIAO PF2
-- =============================================

USE SALSIAO;
GO

-- =============================================
-- TABLA: Clientes
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Clientes')
CREATE TABLE Clientes (
    id_cliente INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion VARCHAR(200),
    fecha_registro DATETIME DEFAULT GETDATE(),
    estado VARCHAR(20) DEFAULT 'Activo'
);

-- =============================================
-- TABLA: Empleados
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Empleados')
CREATE TABLE Empleados (
    id_empleado INT IDENTITY(1,1) PRIMARY KEY,
    cedula VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion VARCHAR(200),
    fecha_ingreso DATE,
    cargo VARCHAR(50),
    departamento VARCHAR(50),
    salario DECIMAL(10,2),
    tipo_contrato VARCHAR(30),
    estado VARCHAR(20) DEFAULT 'Activo'
);

-- =============================================
-- TABLA: DocumentosLaborales
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'DocumentosLaborales')
CREATE TABLE DocumentosLaborales (
    id_documento INT IDENTITY(1,1) PRIMARY KEY,
    id_empleado INT REFERENCES Empleados(id_empleado),
    tipo_documento VARCHAR(50),
    fecha_creacion DATE DEFAULT GETDATE(),
    estado VARCHAR(20),
    observaciones VARCHAR(500)
);

-- =============================================
-- TABLA: Contrataciones
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Contrataciones')
CREATE TABLE Contrataciones (
    id_proceso INT IDENTITY(1,1) PRIMARY KEY,
    id_empleado INT REFERENCES Empleados(id_empleado),
    fecha_inicio DATE,
    estado VARCHAR(30),
    observaciones VARCHAR(500)
);

-- =============================================
-- TABLA: Capacitaciones
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Capacitaciones')
CREATE TABLE Capacitaciones (
    id_programa INT IDENTITY(1,1) PRIMARY KEY,
    id_empleado INT REFERENCES Empleados(id_empleado),
    id_mentor INT REFERENCES Empleados(id_empleado),
    periodo_adaptacion VARCHAR(30),
    calificacion DECIMAL(5,2),
    estado VARCHAR(30)
);

-- =============================================
-- TABLA: ConflictosSalidas
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ConflictosSalidas')
CREATE TABLE ConflictosSalidas (
    id_caso INT IDENTITY(1,1) PRIMARY KEY,
    id_empleado INT REFERENCES Empleados(id_empleado),
    tipo_gestion VARCHAR(30),
    fecha_proceso DATE,
    estado VARCHAR(30),
    observaciones VARCHAR(500)
);

-- =============================================
-- TABLA: Locales
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Locales')
CREATE TABLE Locales (
    id_local INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(200),
    estado VARCHAR(20) DEFAULT 'Activo'
);

-- =============================================
-- TABLA: Areas
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Areas')
CREATE TABLE Areas (
    id_area INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(50),
    tamano DECIMAL(10,2),
    estado VARCHAR(20) DEFAULT 'Activo',
    id_local INT REFERENCES Locales(id_local)
);

-- =============================================
-- TABLA: Equipos
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Equipos')
CREATE TABLE Equipos (
    id_equipo INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(50),
    marca VARCHAR(50),
    fecha_compra DATE,
    estado VARCHAR(30),
    ubicacion VARCHAR(100),
    id_area INT REFERENCES Areas(id_area)
);

-- =============================================
-- TABLA: Tecnicos
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Tecnicos')
CREATE TABLE Tecnicos (
    id_tecnico INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    empresa VARCHAR(100),
    especialidad VARCHAR(50),
    telefono VARCHAR(20),
    estado VARCHAR(20) DEFAULT 'Activo'
);

-- =============================================
-- TABLA: Conserjes
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Conserjes')
CREATE TABLE Conserjes (
    id_conserje INT IDENTITY(1,1) PRIMARY KEY,
    cedula VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(30),
    tamano VARCHAR(50),
    estado VARCHAR(20) DEFAULT 'Activo'
);

-- =============================================
-- TABLA: Limpiezas
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Limpiezas')
CREATE TABLE Limpiezas (
    id_limpieza INT IDENTITY(1,1) PRIMARY KEY,
    fecha DATE,
    turno VARCHAR(30),
    tipo_limpieza VARCHAR(50),
    estado VARCHAR(30),
    id_conserje INT REFERENCES Conserjes(id_conserje),
    id_area INT REFERENCES Areas(id_area),
    id_local INT REFERENCES Locales(id_local),
    tareas TEXT
);

-- =============================================
-- TABLA: Mantenimientos
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Mantenimientos')
CREATE TABLE Mantenimientos (
    id_mantenimiento INT IDENTITY(1,1) PRIMARY KEY,
    fecha DATE,
    tipo VARCHAR(30),
    descripcion VARCHAR(300),
    costo DECIMAL(10,2),
    estado VARCHAR(30),
    proxima_revision DATE,
    id_equipo INT REFERENCES Equipos(id_equipo),
    id_tecnico INT REFERENCES Tecnicos(id_tecnico)
);

-- =============================================
-- TABLA: Proveedores
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Proveedores')
CREATE TABLE Proveedores (
    id_proveedor INT IDENTITY(1,1) PRIMARY KEY,
    codigo VARCHAR(20),
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    tipo_productos VARCHAR(100),
    estado VARCHAR(20) DEFAULT 'Activo'
);

-- =============================================
-- TABLA: Ingredientes
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Ingredientes')
CREATE TABLE Ingredientes (
    id_ingrediente INT IDENTITY(1,1) PRIMARY KEY,
    codigo VARCHAR(20),
    nombre VARCHAR(100) NOT NULL,
    categoria VARCHAR(50),
    unidad_medida VARCHAR(20),
    precio_unitario DECIMAL(10,2),
    id_proveedor INT REFERENCES Proveedores(id_proveedor),
    fecha_vencimiento DATE,
    notas VARCHAR(300),
    estado VARCHAR(20) DEFAULT 'Activo'
);

-- =============================================
-- TABLA: OrdenesCompra
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'OrdenesCompra')
CREATE TABLE OrdenesCompra (
    id_orden INT IDENTITY(1,1) PRIMARY KEY,
    numero_orden VARCHAR(20),
    fecha DATE,
    id_proveedor INT REFERENCES Proveedores(id_proveedor),
    nombre VARCHAR(100),
    tipo_productos VARCHAR(100),
    observaciones TEXT,
    estado VARCHAR(30) DEFAULT 'Pendiente'
);

-- =============================================
-- TABLA: DetalleOrdenCompra
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'DetalleOrdenCompra')
CREATE TABLE DetalleOrdenCompra (
    id_detalle INT IDENTITY(1,1) PRIMARY KEY,
    id_orden INT REFERENCES OrdenesCompra(id_orden),
    id_ingrediente INT REFERENCES Ingredientes(id_ingrediente),
    cantidad DECIMAL(10,2),
    precio DECIMAL(10,2),
    importe DECIMAL(10,2)
);

-- =============================================
-- TABLA: Recepciones
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Recepciones')
CREATE TABLE Recepciones (
    id_recepcion INT IDENTITY(1,1) PRIMARY KEY,
    numero_orden VARCHAR(20),
    id_proveedor INT REFERENCES Proveedores(id_proveedor),
    fecha_recepcion DATE,
    responsable VARCHAR(100),
    estado_producto VARCHAR(50),
    cantidad_recibida DECIMAL(10,2),
    observaciones TEXT
);

-- =============================================
-- TABLA: Pagos
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Pagos')
CREATE TABLE Pagos (
    id_pago INT IDENTITY(1,1) PRIMARY KEY,
    metodo_pago VARCHAR(50),
    fecha_pago DATE,
    monto DECIMAL(10,2),
    itbis DECIMAL(10,2),
    referencia VARCHAR(100),
    id_orden INT REFERENCES OrdenesCompra(id_orden)
);

-- =============================================
-- TABLA: Ventas
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Ventas')
CREATE TABLE Ventas (
    id_venta INT IDENTITY(1,1) PRIMARY KEY,
    numero_venta VARCHAR(20),
    fecha DATE,
    hora VARCHAR(10),
    id_cliente INT REFERENCES Clientes(id_cliente),
    subtotal DECIMAL(10,2),
    itbis DECIMAL(10,2),
    total DECIMAL(10,2),
    observaciones VARCHAR(300),
    estado VARCHAR(20) DEFAULT 'Completada'
);

-- =============================================
-- TABLA: DetalleVentas
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'DetalleVentas')
CREATE TABLE DetalleVentas (
    id_detalle INT IDENTITY(1,1) PRIMARY KEY,
    id_venta INT REFERENCES Ventas(id_venta),
    id_ingrediente INT REFERENCES Ingredientes(id_ingrediente),
    producto VARCHAR(100),
    cantidad DECIMAL(10,2),
    precio DECIMAL(10,2),
    subtotal DECIMAL(10,2)
);

-- =============================================
-- TABLA: Stock
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Stock')
CREATE TABLE Stock (
    id_stock INT IDENTITY(1,1) PRIMARY KEY,
    id_ingrediente INT REFERENCES Ingredientes(id_ingrediente),
    cantidad_disponible DECIMAL(10,2) DEFAULT 0,
    cantidad_minima DECIMAL(10,2) DEFAULT 0,
    cantidad_maxima DECIMAL(10,2) DEFAULT 0,
    estado VARCHAR(30)
);

-- =============================================
-- TABLA: HistorialVentas
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'HistorialVentas')
CREATE TABLE HistorialVentas (
    id_historial INT IDENTITY(1,1) PRIMARY KEY,
    numero_venta VARCHAR(20),
    fecha DATE,
    cliente VARCHAR(100),
    total DECIMAL(10,2),
    metodo_pago VARCHAR(50)
);

-- =============================================
-- INSERTAR DATOS DE PRUEBA
-- =============================================

-- Locales de prueba
IF NOT EXISTS (SELECT 1 FROM Locales)
INSERT INTO Locales (nombre, direccion, estado) VALUES
('Local Principal', 'Av. Principal #123', 'Activo'),
('Sucursal Norte', 'Calle Norte #456', 'Activo');

-- Proveedores de prueba
IF NOT EXISTS (SELECT 1 FROM Proveedores)
INSERT INTO Proveedores (codigo, nombre, telefono, tipo_productos, estado) VALUES
('PRV-001', 'Distribuidora Alimentos SA', '809-555-0101', 'Carnes, Vegetales', 'Activo'),
('PRV-002', 'Lacteos del Caribe', '809-555-0202', 'Lacteos, Quesos', 'Activo'),
('PRV-003', 'Bebidas Tropical', '809-555-0303', 'Bebidas, Jugos', 'Activo');

PRINT 'Base de datos SALSIAO creada/actualizada correctamente.';
GO
