-- =============================================================================
-- BASE DE DATOS SALSIAO PF2 - Sistema de Gestión Completo
-- Compatible con SQL Server
-- =============================================================================

-- Crear el usuario de base de datos (login a nivel servidor)
IF NOT EXISTS (SELECT name FROM sys.sql_logins WHERE name = 'salsiaoUser')
BEGIN
    CREATE LOGIN salsiaoUser WITH PASSWORD = 'Admin1234*';
    PRINT '✅ Login salsiaoUser creado exitosamente.';
END
ELSE
BEGIN
    PRINT 'ℹ️ Login salsiaoUser ya existe.';
END
GO

-- Crear la base de datos con nombre SalsiaoPF2
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'SalsiaoPF2')
BEGIN
    CREATE DATABASE SalsiaoPF2;
    PRINT '✅ Base de datos SalsiaoPF2 creada exitosamente.';
END
ELSE
BEGIN
    PRINT 'ℹ️ Base de datos SalsiaoPF2 ya existe.';
END
GO

-- Usar la base de datos
USE SalsiaoPF2;
GO

-- Crear el usuario de la base de datos vinculado al login
IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = 'salsiaoUser')
BEGIN
    CREATE USER salsiaoUser FOR LOGIN salsiaoUser;
    PRINT '✅ Usuario salsiaoUser creado en la base de datos.';
END
ELSE
BEGIN
    PRINT 'ℹ️ Usuario salsiaoUser ya existe en la base de datos.';
END
GO

-- Asignar permisos completos al usuario
EXEC sp_addrolemember 'db_owner', 'salsiaoUser';
PRINT '✅ Permisos de db_owner asignados a salsiaoUser.';
GO

-- Alternativa: asignar permisos específicos individuales
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE, REFERENCES ON SCHEMA::dbo TO salsiaoUser;
GRANT CREATE TABLE, CREATE PROCEDURE, CREATE VIEW, CREATE FUNCTION TO salsiaoUser;
GRANT ALTER ON SCHEMA::dbo TO salsiaoUser;
GRANT CONTROL ON SCHEMA::dbo TO salsiaoUser;
PRINT '✅ Permisos específicos adicionales asignados a salsiaoUser.';
GO

-- =============================================================================
-- TABLA: Clientes
-- =============================================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Clientes')
BEGIN
    CREATE TABLE Clientes (
        id_cliente INT IDENTITY(1,1) PRIMARY KEY,
        nombre NVARCHAR(100) NOT NULL,
        telefono NVARCHAR(20),
        email NVARCHAR(100),
        direccion NVARCHAR(200),
        estado NVARCHAR(20) DEFAULT 'Activo'
    );
END
GO

-- =============================================================================
-- TABLA: Proveedores
-- =============================================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Proveedores')
BEGIN
    CREATE TABLE Proveedores (
        id_proveedor INT IDENTITY(1,1) PRIMARY KEY,
        codigo NVARCHAR(50),
        nombre NVARCHAR(100) NOT NULL,
        telefono NVARCHAR(20),
        tipo_productos NVARCHAR(100),
        estado NVARCHAR(20) DEFAULT 'Activo'
    );
END
GO

-- =============================================================================
-- TABLA: OrdenesCompra
-- =============================================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'OrdenesCompra')
BEGIN
    CREATE TABLE OrdenesCompra (
        id_orden INT IDENTITY(1,1) PRIMARY KEY,
        numero_orden NVARCHAR(50) NOT NULL,
        fecha DATE,
        id_proveedor INT,
        nombre NVARCHAR(100),
        tipo_productos NVARCHAR(100),
        estado NVARCHAR(20) DEFAULT 'Pendiente',
        FOREIGN KEY (id_proveedor) REFERENCES Proveedores(id_proveedor)
    );
END
GO

-- =============================================================================
-- TABLA: Recepciones
-- =============================================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Recepciones')
BEGIN
    CREATE TABLE Recepciones (
        id_recepcion INT IDENTITY(1,1) PRIMARY KEY,
        numero_orden NVARCHAR(50),
        id_proveedor INT,
        fecha_recepcion DATE,
        responsable NVARCHAR(100),
        estado_producto NVARCHAR(50),
        cantidad_recibida DECIMAL(10,2),
        FOREIGN KEY (id_proveedor) REFERENCES Proveedores(id_proveedor)
    );
END
GO

-- =============================================================================
-- TABLA: Ingredientes
-- =============================================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Ingredientes')
BEGIN
    CREATE TABLE Ingredientes (
        id_ingrediente INT IDENTITY(1,1) PRIMARY KEY,
        codigo NVARCHAR(50),
        nombre NVARCHAR(100) NOT NULL,
        categoria NVARCHAR(50),
        unidad_medida NVARCHAR(30),
        precio_unitario DECIMAL(10,2),
        id_proveedor INT,
        fecha_vencimiento DATE,
        notas NVARCHAR(500),
        estado NVARCHAR(20) DEFAULT 'Activo',
        FOREIGN KEY (id_proveedor) REFERENCES Proveedores(id_proveedor)
    );
END
GO

-- =============================================================================
-- TABLA: Pagos
-- =============================================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Pagos')
BEGIN
    CREATE TABLE Pagos (
        id_pago INT IDENTITY(1,1) PRIMARY KEY,
        metodo_pago NVARCHAR(50),
        fecha_pago DATE,
        monto DECIMAL(10,2),
        itbis DECIMAL(10,2),
        referencia NVARCHAR(100)
    );
END
GO

-- =============================================================================
-- TABLA: Ventas
-- =============================================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Ventas')
BEGIN
    CREATE TABLE Ventas (
        id_venta INT IDENTITY(1,1) PRIMARY KEY,
        numero_venta NVARCHAR(50) NOT NULL,
        fecha DATE,
        hora NVARCHAR(20),
        id_cliente INT,
        subtotal DECIMAL(10,2),
        itbis DECIMAL(10,2),
        total DECIMAL(10,2),
        observaciones NVARCHAR(500),
        estado NVARCHAR(20) DEFAULT 'Completada',
        FOREIGN KEY (id_cliente) REFERENCES Clientes(id_cliente)
    );
END
GO

-- =============================================================================
-- TABLA: Stock
-- =============================================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Stock')
BEGIN
    CREATE TABLE Stock (
        id_stock INT IDENTITY(1,1) PRIMARY KEY,
        id_ingrediente INT,
        cantidad_disponible DECIMAL(10,2) DEFAULT 0,
        cantidad_minima DECIMAL(10,2) DEFAULT 0,
        cantidad_maxima DECIMAL(10,2) DEFAULT 0,
        fecha_actualizacion DATE DEFAULT GETDATE(),
        FOREIGN KEY (id_ingrediente) REFERENCES Ingredientes(id_ingrediente)
    );
END
GO

-- =============================================================================
-- TABLA: Empleados
-- =============================================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Empleados')
BEGIN
    CREATE TABLE Empleados (
        id_empleado INT IDENTITY(1,1) PRIMARY KEY,
        cedula NVARCHAR(20),
        nombre NVARCHAR(100) NOT NULL,
        apellido NVARCHAR(100),
        telefono NVARCHAR(20),
        email NVARCHAR(100),
        direccion NVARCHAR(200),
        fecha_ingreso DATE,
        cargo NVARCHAR(50),
        departamento NVARCHAR(50),
        salario NVARCHAR(50),
        tipo_contrato NVARCHAR(30),
        estado NVARCHAR(20) DEFAULT 'Activo'
    );
END
GO

-- =============================================================================
-- TABLA: Locales
-- =============================================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Locales')
BEGIN
    CREATE TABLE Locales (
        id_local INT IDENTITY(1,1) PRIMARY KEY,
        nombre NVARCHAR(100) NOT NULL,
        direccion NVARCHAR(200),
        estado NVARCHAR(20) DEFAULT 'Activo'
    );
END
GO

-- =============================================================================
-- TABLA: Areas
-- =============================================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Areas')
BEGIN
    CREATE TABLE Areas (
        id_area INT IDENTITY(1,1) PRIMARY KEY,
        nombre NVARCHAR(100) NOT NULL,
        tipo NVARCHAR(50),
        tamano NVARCHAR(30),
        estado NVARCHAR(20) DEFAULT 'Activo'
    );
END
GO

-- =============================================================================
-- TABLA: Equipos
-- =============================================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Equipos')
BEGIN
    CREATE TABLE Equipos (
        id_equipo INT IDENTITY(1,1) PRIMARY KEY,
        nombre NVARCHAR(100) NOT NULL,
        tipo NVARCHAR(50),
        marca NVARCHAR(50),
        estado NVARCHAR(20) DEFAULT 'Operativo',
        ubicacion NVARCHAR(100),
        fecha_compra DATE
    );
END
GO

-- =============================================================================
-- TABLA: Tecnicos
-- =============================================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Tecnicos')
BEGIN
    CREATE TABLE Tecnicos (
        id_tecnico INT IDENTITY(1,1) PRIMARY KEY,
        nombre NVARCHAR(100) NOT NULL,
        empresa NVARCHAR(100),
        especialidad NVARCHAR(100),
        telefono NVARCHAR(20)
    );
END
GO

-- =============================================================================
-- DATOS DE PRUEBA INICIALES
-- =============================================================================

-- Insertar clientes de ejemplo
IF NOT EXISTS (SELECT 1 FROM Clientes)
BEGIN
    INSERT INTO Clientes (nombre, telefono, email, direccion, estado) VALUES
    ('Cliente General', '809-555-0001', 'general@email.com', 'Santo Domingo', 'Activo'),
    ('Juan Pérez', '809-555-0002', 'juan@email.com', 'Santiago', 'Activo'),
    ('María García', '809-555-0003', 'maria@email.com', 'La Romana', 'Activo'),
    ('Pedro López', '809-555-0004', 'pedro@email.com', 'Puerto Plata', 'Activo');
END
GO

-- Insertar proveedores de ejemplo
IF NOT EXISTS (SELECT 1 FROM Proveedores)
BEGIN
    INSERT INTO Proveedores (codigo, nombre, telefono, tipo_productos, estado) VALUES
    ('PROV001', 'Distribuidora ABC', '809-555-1001', 'Bebidas, Snacks', 'Activo'),
    ('PROV002', 'Carnes del Caribe', '809-555-1002', 'Carnes, Aves', 'Activo'),
    ('PROV003', 'Frescos RD', '809-555-1003', 'Verduras, Frutas', 'Activo'),
    ('PROV004', 'Lácteos Tropical', '809-555-1004', 'Lácteos, Quesos', 'Activo');
END
GO

-- Insertar ingredientes de ejemplo
IF NOT EXISTS (SELECT 1 FROM Ingredientes)
BEGIN
    INSERT INTO Ingredientes (codigo, nombre, categoria, unidad_medida, precio_unitario, estado) VALUES
    ('ING001', 'Pollo Fresco', 'Carnes', 'kg', 95.00, 'Activo'),
    ('ING002', 'Arroz Blanco', 'Granos', 'kg', 25.00, 'Activo'),
    ('ING003', 'Aceite Vegetal', 'Aceites', 'lt', 45.00, 'Activo'),
    ('ING004', 'Cebolla', 'Verduras', 'kg', 15.00, 'Activo'),
    ('ING005', 'Limón', 'Frutas', 'kg', 20.00, 'Activo');
END
GO

-- Insertar stock inicial
IF NOT EXISTS (SELECT 1 FROM Stock)
BEGIN
    INSERT INTO Stock (id_ingrediente, cantidad_disponible, cantidad_minima, cantidad_maxima) VALUES
    (1, 50.00, 10.00, 100.00),
    (2, 200.00, 50.00, 500.00),
    (3, 30.00, 10.00, 80.00),
    (4, 40.00, 15.00, 100.00),
    (5, 25.00, 10.00, 60.00);
END
GO

-- Insertar empleados de ejemplo
IF NOT EXISTS (SELECT 1 FROM Empleados)
BEGIN
    INSERT INTO Empleados (cedula, nombre, apellido, telefono, email, cargo, departamento, estado) VALUES
    ('001-0000000-1', 'Administrador', 'Sistema', '809-555-9999', 'admin@salsiao.com', 'Administrador', 'Sistemas', 'Activo'),
    ('001-0000000-2', 'Carlos', 'Martínez', '809-555-0001', 'carlos@email.com', 'Cajero', 'Ventas', 'Activo'),
    ('001-0000000-3', 'Ana', 'Rodríguez', '809-555-0002', 'ana@email.com', 'Cocinera', 'Cocina', 'Activo');
END
GO

-- Insertar locales de ejemplo
IF NOT EXISTS (SELECT 1 FROM Locales)
BEGIN
    INSERT INTO Locales (nombre, direccion, estado) VALUES
    ('Salsiao Principal', 'Av. Winston Churchill, Santo Domingo', 'Activo'),
    ('Salsiao Naco', 'Calle Principal, Naco', 'Activo');
END
GO

-- Insertar áreas de ejemplo
IF NOT EXISTS (SELECT 1 FROM Areas)
BEGIN
    INSERT INTO Areas (nombre, tipo, tamano, estado) VALUES
    ('Cocina Principal', 'Cocina', '50m2', 'Activo'),
    ('Sala de Ventas', 'Ventas', '30m2', 'Activo'),
    ('Almacén', 'Almacen', '40m2', 'Activo');
END
GO

-- Insertar equipos de ejemplo
IF NOT EXISTS (SELECT 1 FROM Equipos)
BEGIN
    INSERT INTO Equipos (nombre, tipo, marca, estado, ubicacion) VALUES
    ('Freidora Industrial', 'Cocina', 'BakerPro', 'Operativo', 'Cocina Principal'),
    ('Refrigerador 4ptas', 'Refrigeración', 'Carrier', 'Operativo', 'Cocina Principal'),
    ('Caja Registradora', 'Ventas', 'NCR', 'Operativo', 'Sala de Ventas');
END
GO

-- Insertar técnicos de ejemplo
IF NOT EXISTS (SELECT 1 FROM Tecnicos)
BEGIN
    INSERT INTO Tecnicos (nombre, empresa, especialidad, telefono) VALUES
    ('Miguel Tech', 'ServiTec RD', 'Electrodomésticos', '809-555-2001'),
    ('Técnico HVAC', 'Frío Central', 'Refrigeración', '809-555-2002');
END
GO

PRINT '✅ Base de datos SALSIAO creada exitosamente con todas las tablas y datos iniciales.';
GO
