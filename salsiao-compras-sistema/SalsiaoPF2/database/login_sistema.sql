-- =============================================
-- SCRIPT DE AUTENTICACION Y ROLES - SALSIAO PF
-- Base de datos: SalsiaoPF2 (SQL Server)
-- Ejecutar sobre la base de datos existente
-- =============================================

USE SalsiaoPF2;
GO

-- =============================================
-- TABLA: Usuarios (sistema de login)
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Usuarios')
CREATE TABLE Usuarios (
    id_usuario   INT IDENTITY(1,1) PRIMARY KEY,
    nombre       VARCHAR(100)  NOT NULL,
    usuario      VARCHAR(150)  NOT NULL UNIQUE,
    contrasena   VARCHAR(255)  NOT NULL,   -- hash BCrypt
    rol          VARCHAR(30)   NOT NULL    -- Administrador | Cajero | Cocinero | Repartidor
                 CHECK (rol IN ('Administrador','Cajero','Cocinero','Repartidor')),
    estado       VARCHAR(20)   NOT NULL DEFAULT 'Activo'
                 CHECK (estado IN ('Activo','Inactivo')),
    fecha_creacion DATETIME    DEFAULT GETDATE()
);
GO

-- =============================================
-- USUARIOS DE PRUEBA
-- Contraseñas en texto plano para máxima facilidad en desarrollo.
-- Clave para todos: 123
-- =============================================
IF NOT EXISTS (SELECT 1 FROM Usuarios)
BEGIN
    -- Administrador  / 123
    INSERT INTO Usuarios (nombre, usuario, contrasena, rol, estado) VALUES
    ('Administrador Principal',
     'admin',
     '123',
     'Administrador', 'Activo');

    -- Cajero / 123
    INSERT INTO Usuarios (nombre, usuario, contrasena, rol, estado) VALUES
    ('Carlos Cajero',
     'cajero',
     '123',
     'Cajero', 'Activo');

    -- Cocinero / 123
    INSERT INTO Usuarios (nombre, usuario, contrasena, rol, estado) VALUES
    ('María Cocinera',
     'cocinero',
     '123',
     'Cocinero', 'Activo');

    -- Repartidor / 123
    INSERT INTO Usuarios (nombre, usuario, contrasena, rol, estado) VALUES
    ('Luis Repartidor',
     'repartidor',
     '123',
     'Repartidor', 'Activo');
END
GO

PRINT 'Tabla Usuarios creada e inicializada correctamente.';
GO
