-- Verificar tablas creadas
SELECT name FROM sys.tables WHERE name IN ('tbl_CLIENTE','tbl_CONTADOR_CLIENTE','tbl_DIRECCION_CLIENTE','tbl_RESERVA_CLIENTE','tbl_COMPRA_CLIENTE');

-- Ver clientes
SELECT c.ID_cliente, c.Codigo, c.Nombre, c.Apellido, c.Cedula, c.Telefono, c.Tipo_cliente, c.Estado, c.cantidad_compras, c.total_compras, c.ultima_compra FROM tbl_CLIENTE c ORDER BY c.ID_cliente;

-- Ver direcciones
SELECT d.ID_direccion, c.Nombre+' '+c.Apellido AS Cliente, d.Direccion, ISNULL(d.Referencia,'') AS Referencia, ISNULL(d.Zona,'') AS Zona FROM tbl_DIRECCION_CLIENTE d JOIN tbl_CLIENTE c ON d.fk_ID_cliente=c.ID_cliente;

-- Ver reservas
SELECT r.Codigo, c.Nombre+' '+c.Apellido AS Cliente, r.Fecha, r.Hora, r.Cantidad_personas, r.Estado FROM tbl_RESERVA_CLIENTE r JOIN tbl_CLIENTE c ON r.fk_ID_cliente=c.ID_cliente;

-- Ver compras
SELECT c.Nombre+' '+c.Apellido AS Cliente, cc.Fecha, cc.Numero_pedido, cc.Total, cc.Metodo_pago, cc.Estado_pedido FROM tbl_COMPRA_CLIENTE cc JOIN tbl_CLIENTE c ON cc.fk_ID_cliente=c.ID_cliente ORDER BY cc.Fecha DESC;
