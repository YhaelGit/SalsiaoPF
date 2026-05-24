-- =============================================
-- SCRIPT DE CREACIÓN DE EMPLEADOS (USUARIOS)
-- Incluye contraseñas encriptadas con BCrypt
-- =============================================

USE SALSIAOREF;
GO

-- Crear tabla si no existe
IF NOT EXISTS (
    SELECT *
    FROM sys.tables
    WHERE name = 'Usuarios'
)
BEGIN
    CREATE TABLE Usuarios (
        id          INT IDENTITY(1,1) PRIMARY KEY,
        nombre      VARCHAR(100) NOT NULL,
        usuario     VARCHAR(150) NOT NULL UNIQUE,
        contrasena  VARCHAR(255) NOT NULL,
        rol         VARCHAR(50)  NOT NULL,
        estado      VARCHAR(20)  NOT NULL DEFAULT 'Activo'
    );

    PRINT 'Tabla Usuarios creada.';
END
ELSE
BEGIN
    PRINT 'La tabla Usuarios ya existe.';
END
GO

-- =============================================
-- CONTRASEÑA PARA TODOS LOS USUARIOS:
-- segura123
--
-- Hash BCrypt generado con costo 12
-- =============================================

-- Usuario: curry
IF NOT EXISTS (
    SELECT 1
    FROM Usuarios
    WHERE usuario = 'curry'
)
BEGIN
    INSERT INTO Usuarios (
        nombre,
        usuario,
        contrasena,
        rol,
        estado
    )
    VALUES (
        'Stephen Curry',
        'curry',
        '$2a$12$rqA9DdSqyE1kQC7UH6gXd.YXKFOsmbKKKaQz.9oiCT94LNeTWMCeS',
        'Cajero',
        'Activo'
    );
END
GO

-- Usuario: lebronj
IF NOT EXISTS (
    SELECT 1
    FROM Usuarios
    WHERE usuario = 'lebronj'
)
BEGIN
    INSERT INTO Usuarios (
        nombre,
        usuario,
        contrasena,
        rol,
        estado
    )
    VALUES (
        'Lebron James',
        'lebronj',
        '$2a$12$rqA9DdSqyE1kQC7UH6gXd.YXKFOsmbKKKaQz.9oiCT94LNeTWMCeS',
        'Inventario',
        'Activo'
    );
END
GO

-- Usuario: neymarjr
IF NOT EXISTS (
    SELECT 1
    FROM Usuarios
    WHERE usuario = 'neymarjr'
)
BEGIN
    INSERT INTO Usuarios (
        nombre,
        usuario,
        contrasena,
        rol,
        estado
    )
    VALUES (
        'Neymar Jr',
        'neymarjr',
        '$2a$12$rqA9DdSqyE1kQC7UH6gXd.YXKFOsmbKKKaQz.9oiCT94LNeTWMCeS',
        'Administrador',
        'Activo'
    );
END
GO

-- Usuario: crisr
IF NOT EXISTS (
    SELECT 1
    FROM Usuarios
    WHERE usuario = 'crisr'
)
BEGIN
    INSERT INTO Usuarios (
        nombre,
        usuario,
        contrasena,
        rol,
        estado
    )
    VALUES (
        'Cristiano Ronaldo',
        'crisr',
        '$2a$12$rqA9DdSqyE1kQC7UH6gXd.YXKFOsmbKKKaQz.9oiCT94LNeTWMCeS',
        'Delivery',
        'Activo'
    );
END
GO

PRINT 'Usuarios insertados correctamente.';
GO