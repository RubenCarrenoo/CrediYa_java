-- =====================================================
--  CrediYa S.A.S. - Datos de prueba
--  Ejecutar DESPUÉS de crediya_db.sql y sobre tablas VACÍAS
-- =====================================================
SET NAMES utf8mb4;
USE crediya_db;

INSERT INTO empleados (id, nombre, documento, rol, correo, salario) VALUES
(1, 'Juan Pérez',  '123456789', 'Asesor',  'juan@crediya.com',  2500000),
(2, 'María López', '987654321', 'Gestora', 'maria@crediya.com', 2800000);

INSERT INTO clientes (id, nombre, documento, correo, telefono) VALUES
(1, 'Carlos Gómez',    '1098765432', 'carlos@gmail.com', '3001234567'),
(2, 'Ana Rodríguez',   '1122334455', 'ana@gmail.com',    '3019876543');

-- Préstamo 1: Carlos, $1.000.000 al 10% en 10 cuotas (total 1.100.000, cuota 110.000).
--             Lleva 2 pagos -> saldo 880.000. Empezó en junio: va ATRASADO (moroso).
-- Préstamo 2: Ana, $500.000 al 10% en 5 cuotas (total 550.000). Pagado completo -> PAGADO.
-- Préstamo 3: Ana, $2.000.000 al 10% en 10 cuotas (total 2.200.000). Sin pagos, recién creado.
INSERT INTO prestamos (id, cliente_id, empleado_id, monto, interes, cuotas, fecha_inicio, estado, saldo_pendiente) VALUES
(1, 1, 1, 1000000, 10, 10, '2026-06-01', 'PENDIENTE',  880000),
(2, 2, 2,  500000, 10,  5, '2026-05-01', 'PAGADO',          0),
(3, 2, 1, 2000000, 10, 10, '2026-09-15', 'PENDIENTE', 2200000);

INSERT INTO pagos (prestamo_id, fecha_pago, monto) VALUES
(1, '2026-07-01', 110000),
(1, '2026-08-01', 110000),
(2, '2026-06-01', 110000),
(2, '2026-07-01', 110000),
(2, '2026-08-01', 110000),
(2, '2026-09-01', 110000),
(2, '2026-10-01', 110000);
