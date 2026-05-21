-- ============================================================
-- SALSIAO · Tablas POS de ventas (SQL Server - SALSIAOREF)
-- ============================================================
USE SALSIAOREF;
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_VENTA_POS')
BEGIN
    CREATE TABLE tbl_VENTA_POS (
        id_venta       INT IDENTITY(1,1) PRIMARY KEY,
        id_factura     VARCHAR(40)    NOT NULL,
        fecha_venta    DATETIME       NOT NULL DEFAULT GETDATE(),
        total          DECIMAL(12,2)  NOT NULL,
        metodo_pago    VARCHAR(30)    NOT NULL,
        monto_recibido DECIMAL(12,2)  NULL,
        devuelta       DECIMAL(12,2)  NULL,
        email_cliente  VARCHAR(150)   NULL,
        id_cliente     INT            NULL DEFAULT 1
    );
    PRINT 'Tabla tbl_VENTA_POS creada.';
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_DETALLE_VENTA_POS')
BEGIN
    CREATE TABLE tbl_DETALLE_VENTA_POS (
        id_detalle       INT IDENTITY(1,1) PRIMARY KEY,
        id_venta         INT NOT NULL,
        producto_id      VARCHAR(50)   NOT NULL,
        nombre_producto  VARCHAR(200)  NOT NULL,
        cantidad         INT           NOT NULL,
        precio_unitario  DECIMAL(12,2) NOT NULL,
        subtotal         DECIMAL(12,2) NOT NULL,
        CONSTRAINT FK_detalle_venta_pos
            FOREIGN KEY (id_venta) REFERENCES tbl_VENTA_POS(id_venta)
            ON DELETE CASCADE
    );
    CREATE INDEX IX_detalle_venta_id ON tbl_DETALLE_VENTA_POS(id_venta);
    PRINT 'Tabla tbl_DETALLE_VENTA_POS creada.';
END
GO
