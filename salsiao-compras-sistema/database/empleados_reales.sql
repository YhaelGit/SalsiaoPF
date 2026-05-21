-- =============================================
-- SCRIPT DE CREACION DE EMPLEADOS (USUARIOS)
-- Incluye contraseñas encriptadas con BCrypt
-- =============================================

USE SALSIAOREF;
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Usuarios')
BEGIN
    CREATE TABLE Usuarios (
        id           INT IDENTITY(1,1) PRIMARY KEY,
        nombre       VARCHAR(100)  NOT NULL,
        usuario      VARCHAR(150)  NOT NULL UNIQUE,
        contrasena   VARCHAR(255)  NOT NULL,
        rol          VARCHAR(50)   NOT NULL,
        estado       VARCHAR(20)   NOT NULL DEFAULT 'Activo'
    );
    PRINT 'Tabla Usuarios creada.';
END
ELSE
BEGIN
    PRINT 'La tabla Usuarios ya existe.';
END
GO

-- Insertar empleados reales con contraseñas encriptadas con BCrypt
-- La contraseña para TODOS estos usuarios es: segura123
-- El hash generado con BCrypt (costo 12) es el siguiente:

IF NOT EXISTS (SELECT 1 FROM Usuarios WHERE usuario = 'juanp')
BEGIN
    INSERT INTO Usuarios (nombre, usuario, contrasena, rol, estado) VALUES
    ('Juan Pérez', 'juanp', '$2a$12$rqA9DdSqyE1kQC7UH6gXd.YXKFOsmbKKKaQz.9oiCT94LNeTWMCeS', 'Cajero', 'Activo');
END

IF NOT EXISTS (SELECT 1 FROM Usuarios WHERE usuario = 'mariag')
BEGIN
    INSERT INTO Usuarios (nombre, usuario, contrasena, rol, estado) VALUES
    ('María Gómez', 'mariag', '$2a$12$rqA9DdSqyE1kQC7UH6gXd.YXKFOsmbKKKaQz.9oiCT94LNeTWMCeS', 'Inventario', 'Activo');
END

IF NOT EXISTS (SELECT 1 FROM Usuarios WHERE usuario = 'carlosm')
BEGIN
    INSERT INTO Usuarios (nombre, usuario, contrasena, rol, estado) VALUES
    ('Carlos Martínez', 'carlosm', '$2a$12$rqA9DdSqyE1kQC7UH6gXd.YXKFOsmbKKKaQz.9oiCT94LNeTWMCeS', 'Administrador', 'Activo');
END

IF NOT EXISTS (SELECT 1 FROM Usuarios WHERE usuario = 'luisf')
BEGIN
    INSERT INTO Usuarios (nombre, usuario, contrasena, rol, estado) VALUES
    ('Luis Fernández', 'luisf', '$2a$12$rqA9DdSqyE1kQC7UH6gXd.YXKFOsmbKKKaQz.9oiCT94LNeTWMCeS', 'Delivery', 'Activo');
END
GO

PRINT 'Empleados reales de ejemplo insertados correctamente.';
GO
