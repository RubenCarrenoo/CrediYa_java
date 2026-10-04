-- =====================================================
--  CrediYa S.A.S. - Script de creación de la base de datos
--  Ejecutar completo en MySQL Workbench o consola mysql
-- =====================================================
CREATE DATABASE IF NOT EXISTS crediya_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

SET NAMES utf8mb4;
USE crediya_db;

-- Descomente estas 4 líneas si quiere borrar todo y empezar de cero:
-- DROP TABLE IF EXISTS pagos;
-- DROP TABLE IF EXISTS prestamos;
-- DROP TABLE IF EXISTS clientes;
-- DROP TABLE IF EXISTS empleados;

CREATE TABLE IF NOT EXISTS empleados (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    nombre    VARCHAR(80)   NOT NULL,
    documento VARCHAR(30)   NOT NULL UNIQUE,   -- AJUSTE: documento único
    rol       VARCHAR(30)   NOT NULL,
    correo    VARCHAR(80)   NOT NULL,
    salario   DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS clientes (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    nombre    VARCHAR(80) NOT NULL,
    documento VARCHAR(30) NOT NULL UNIQUE,     -- AJUSTE: documento único
    correo    VARCHAR(80) NOT NULL,
    telefono  VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS prestamos (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id      INT           NOT NULL,
    empleado_id     INT           NOT NULL,
    monto           DECIMAL(12,2) NOT NULL,
    interes         DECIMAL(5,2)  NOT NULL,
    cuotas          INT           NOT NULL,
    fecha_inicio    DATE          NOT NULL,
    estado          VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    saldo_pendiente DECIMAL(12,2) NOT NULL,    -- AJUSTE: columna nueva (saldo que se actualiza con cada pago)
    FOREIGN KEY (cliente_id)  REFERENCES clientes(id),
    FOREIGN KEY (empleado_id) REFERENCES empleados(id)
);

CREATE TABLE IF NOT EXISTS pagos (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    prestamo_id INT           NOT NULL,
    fecha_pago  DATE          NOT NULL,
    monto       DECIMAL(12,2) NOT NULL,        -- AJUSTE: 12,2 para igualar a prestamos.monto
    FOREIGN KEY (prestamo_id) REFERENCES prestamos(id)
);
