# CrediYa — Código completo con la ruta de cada archivo

Cada bloque indica **dónde** va el archivo dentro del proyecto. Orden: Maven → MySQL → modelo → conexión → DAO → servicios → archivos → menú.

## `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.crediya</groupId>
    <artifactId>crediya</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    <name>Sistema de Cobros de Cartera CrediYa</name>

    <properties>
        <!-- Java 17 -->
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <maven.compiler.release>17</maven.compiler.release>
        <!-- Codificación UTF-8 (para tildes y ñ) -->
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <!-- Clase principal (la usa NetBeans y exec-maven-plugin) -->
        <exec.mainClass>com.crediya.Main</exec.mainClass>
    </properties>

    <dependencies>
        <!-- Driver JDBC de MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.4.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <release>17</release>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>

            <!-- Permite ejecutar con:  mvn compile exec:java -->
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <mainClass>com.crediya.Main</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## `src/main/resources/db.properties`

```properties
# Configuración de la conexión a MySQL (la lee la clase ConexionBD)
# IMPORTANTE: cambie la contraseña por la suya y NO suba su contraseña real a GitHub.
db.host=localhost
db.port=3306
db.name=crediya_db
db.user=root
db.password=tu_contraseña
```

## `sql/crediya_db.sql`

```sql
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
```

## `sql/datos_prueba.sql`

```sql
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
```

## `src/main/java/com/crediya/modelo/EstadoPrestamo.java`

```java
package com.crediya.modelo;

/** Estados posibles de un préstamo. */
public enum EstadoPrestamo {
    PENDIENTE,   // tiene saldo por pagar
    PAGADO,      // saldo = 0
    VENCIDO      // tiene saldo y fue marcado manualmente como vencido
}
```

## `src/main/java/com/crediya/modelo/Persona.java`

```java
package com.crediya.modelo;

/**
 * Clase base (abstracta) de las personas del sistema.
 *
 * HERENCIA: Cliente y Empleado heredan de aquí id, nombre, documento y correo.
 * ENCAPSULAMIENTO: los atributos son privados; se accede con getters y setters.
 */
public abstract class Persona {

    private int id;
    private String nombre;
    private String documento;
    private String correo;

    protected Persona(int id, String nombre, String documento, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.documento = documento;
        this.correo = correo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /** POLIMORFISMO: cada clase hija responde de forma distinta. */
    public abstract String getTipo();

    @Override
    public String toString() {
        return getTipo() + " #" + id + " | " + nombre + " | Doc: " + documento + " | " + correo;
    }
}
```

## `src/main/java/com/crediya/modelo/Cliente.java`

```java
package com.crediya.modelo;

/** Cliente de CrediYa: una Persona con teléfono. */
public class Cliente extends Persona {

    private String telefono;

    public Cliente(int id, String nombre, String documento, String correo, String telefono) {
        super(id, nombre, documento, correo);
        this.telefono = telefono;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public String getTipo() {
        return "Cliente";
    }

    @Override
    public String toString() {
        return super.toString() + " | Tel: " + telefono;
    }
}
```

## `src/main/java/com/crediya/modelo/Empleado.java`

```java
package com.crediya.modelo;

import com.crediya.util.Formato;

/** Empleado de CrediYa: una Persona con rol y salario. */
public class Empleado extends Persona {

    private String rol;
    private double salario;

    public Empleado(int id, String nombre, String documento, String correo, String rol, double salario) {
        super(id, nombre, documento, correo);
        this.rol = rol;
        this.salario = salario;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    @Override
    public String getTipo() {
        return "Empleado";
    }

    @Override
    public String toString() {
        return super.toString() + " | Rol: " + rol + " | Salario: " + Formato.moneda(salario);
    }
}
```

## `src/main/java/com/crediya/modelo/Prestamo.java`

```java
package com.crediya.modelo;

import com.crediya.util.CalculadoraPrestamo;
import com.crediya.util.Formato;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Préstamo otorgado a un cliente y gestionado por un empleado. */
public class Prestamo {

    private int id;
    private final Cliente cliente;
    private final Empleado empleado;
    private final double monto;
    private final double interes;      // porcentaje: 10 significa 10 %
    private final int cuotas;
    private final LocalDate fechaInicio;
    private EstadoPrestamo estado;
    private double saldoPendiente;

    public Prestamo(int id, Cliente cliente, Empleado empleado, double monto, double interes,
                    int cuotas, LocalDate fechaInicio, EstadoPrestamo estado, double saldoPendiente) {
        this.id = id;
        this.cliente = cliente;
        this.empleado = empleado;
        this.monto = monto;
        this.interes = interes;
        this.cuotas = cuotas;
        this.fechaInicio = fechaInicio;
        this.estado = estado;
        this.saldoPendiente = saldoPendiente;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public double getMonto() {
        return monto;
    }

    public double getInteres() {
        return interes;
    }

    public int getCuotas() {
        return cuotas;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }

    public double getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(double saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

    // ----- Valores calculados (la fórmula vive en CalculadoraPrestamo) -----

    public double getValorInteres() {
        return CalculadoraPrestamo.calcularValorInteres(monto, interes);
    }

    public double getMontoTotal() {
        return CalculadoraPrestamo.calcularMontoTotal(monto, interes);
    }

    public double getCuotaMensual() {
        return CalculadoraPrestamo.calcularCuotaMensual(monto, interes, cuotas);
    }

    public double getTotalPagado() {
        return CalculadoraPrestamo.redondear(getMontoTotal() - saldoPendiente);
    }

    // ----- Regla de mora -----
    // Cada mes transcurrido desde la fecha de inicio vence una cuota.
    // Un préstamo está en mora si lo pagado es menor a lo que ya debería haberse pagado,
    // o si fue marcado manualmente como VENCIDO.

    public int getCuotasVencidas() {
        long meses = ChronoUnit.MONTHS.between(fechaInicio, LocalDate.now());
        return (int) Math.max(0, Math.min(meses, cuotas));
    }

    public double getValorEsperadoPagado() {
        double esperado = CalculadoraPrestamo.redondear(getCuotasVencidas() * getCuotaMensual());
        return Math.min(getMontoTotal(), esperado);
    }

    public boolean estaEnMora() {
        if (estado == EstadoPrestamo.VENCIDO) {
            return true;
        }
        return estado == EstadoPrestamo.PENDIENTE && getTotalPagado() < getValorEsperadoPagado() - 0.005;
    }

    /** Resumen en varias líneas (el bloque que pide el enunciado). */
    public String resumen() {
        return String.join(System.lineSeparator(),
                "Préstamo #" + id + "  |  Cliente: " + cliente.getNombre() + "  |  Empleado: " + empleado.getNombre(),
                "Monto solicitado: " + Formato.moneda(monto),
                "Interés: " + Formato.numero(interes) + "%",
                "Valor interés: " + Formato.moneda(getValorInteres()),
                "Monto total: " + Formato.moneda(getMontoTotal()),
                "Número de cuotas: " + cuotas,
                "Cuota mensual: " + Formato.moneda(getCuotaMensual()),
                "Fecha de inicio: " + fechaInicio,
                "Saldo pendiente: " + Formato.moneda(saldoPendiente),
                "Estado: " + estado);
    }

    /** Una sola línea (para listados). */
    @Override
    public String toString() {
        return "Préstamo #" + id
                + " | Cliente: " + cliente.getNombre()
                + " | Empleado: " + empleado.getNombre()
                + " | Monto: " + Formato.moneda(monto)
                + " | Total: " + Formato.moneda(getMontoTotal())
                + " | Saldo: " + Formato.moneda(saldoPendiente)
                + " | Estado: " + estado;
    }
}
```

## `src/main/java/com/crediya/modelo/Pago.java`

```java
package com.crediya.modelo;

import com.crediya.util.Formato;
import java.time.LocalDate;

/** Pago o abono realizado a un préstamo. */
public class Pago {

    private int id;
    private final int prestamoId;
    private final LocalDate fechaPago;
    private final double monto;

    public Pago(int id, int prestamoId, LocalDate fechaPago, double monto) {
        this.id = id;
        this.prestamoId = prestamoId;
        this.fechaPago = fechaPago;
        this.monto = monto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPrestamoId() {
        return prestamoId;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public double getMonto() {
        return monto;
    }

    @Override
    public String toString() {
        return "Pago #" + id + " | Préstamo #" + prestamoId + " | Fecha: " + fechaPago + " | Monto: " + Formato.moneda(monto);
    }
}
```

## `src/main/java/com/crediya/excepciones/ClienteNoEncontradoException.java`

```java
package com.crediya.excepciones;

/** Se lanza cuando se busca un cliente que no existe. */
public class ClienteNoEncontradoException extends CrediYaException {

    public ClienteNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
```

## `src/main/java/com/crediya/excepciones/CrediYaException.java`

```java
package com.crediya.excepciones;

/**
 * Excepción base del sistema. Todas las excepciones personalizadas heredan de ella,
 * así la capa de vista puede capturar una sola clase (CrediYaException) y mostrar el mensaje.
 */
public class CrediYaException extends Exception {

    public CrediYaException(String mensaje) {
        super(mensaje);
    }

    public CrediYaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
```

## `src/main/java/com/crediya/excepciones/DatosInvalidosException.java`

```java
package com.crediya.excepciones;

/** Se lanza cuando los datos ingresados no cumplen las validaciones (campo vacío, correo inválido, monto negativo...). */
public class DatosInvalidosException extends CrediYaException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
```

## `src/main/java/com/crediya/excepciones/EmpleadoNoEncontradoException.java`

```java
package com.crediya.excepciones;

/** Se lanza cuando se busca un empleado que no existe. */
public class EmpleadoNoEncontradoException extends CrediYaException {

    public EmpleadoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
```

## `src/main/java/com/crediya/excepciones/ErrorPersistenciaException.java`

```java
package com.crediya.excepciones;

/**
 * Envuelve los errores técnicos de la persistencia (SQLException, IOException).
 * Así las capas superiores no necesitan conocer JDBC ni java.io.
 */
public class ErrorPersistenciaException extends CrediYaException {

    public ErrorPersistenciaException(String mensaje) {
        super(mensaje);
    }

    public ErrorPersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
```

## `src/main/java/com/crediya/excepciones/PagoInvalidoException.java`

```java
package com.crediya.excepciones;

/** Se lanza cuando un pago no se puede aplicar (supera el saldo, préstamo ya pagado, fecha incorrecta...). */
public class PagoInvalidoException extends CrediYaException {

    public PagoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
```

## `src/main/java/com/crediya/excepciones/PrestamoNoEncontradoException.java`

```java
package com.crediya.excepciones;

/** Se lanza cuando se busca un préstamo que no existe. */
public class PrestamoNoEncontradoException extends CrediYaException {

    public PrestamoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
```

## `src/main/java/com/crediya/util/CalculadoraPrestamo.java`

```java
package com.crediya.util;

/**
 * Lógica de cálculo del préstamo, separada de la consola y de la base de datos (SRP).
 *
 * Ejemplo: monto 1.000.000, interés 10 %, 10 cuotas
 *   valor interés = 1.000.000 x 10 / 100 = 100.000
 *   monto total   = 1.100.000
 *   cuota mensual = 1.100.000 / 10 = 110.000
 *
 * Nota para estudiantes: aquí se usa double por sencillez. En un sistema bancario real
 * se usaría BigDecimal para evitar errores de redondeo; por eso todo se redondea a 2 decimales.
 */
public final class CalculadoraPrestamo {

    private CalculadoraPrestamo() {
    }

    public static double calcularValorInteres(double monto, double interesPorcentaje) {
        return redondear(monto * interesPorcentaje / 100.0);
    }

    public static double calcularMontoTotal(double monto, double interesPorcentaje) {
        return redondear(monto + calcularValorInteres(monto, interesPorcentaje));
    }

    public static double calcularCuotaMensual(double monto, double interesPorcentaje, int cuotas) {
        return redondear(calcularMontoTotal(monto, interesPorcentaje) / cuotas);
    }

    /** Redondea a 2 decimales (evita residuos como 0.0000001 al restar pagos). */
    public static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
```

## `src/main/java/com/crediya/util/Formato.java`

```java
package com.crediya.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Utilidad para mostrar valores con formato colombiano: $1.100.000 */
public final class Formato {

    private static final DecimalFormat FORMATO_MONEDA;

    static {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.forLanguageTag("es-CO"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        FORMATO_MONEDA = new DecimalFormat("#,##0.##", simbolos);
    }

    private Formato() {
        // Clase de utilidades: no se instancia.
    }

    /** 1100000 -> "$1.100.000" */
    public static String moneda(double valor) {
        return "$" + FORMATO_MONEDA.format(valor);
    }

    /** 10.0 -> "10" ; 7.5 -> "7.5" (para mostrar el porcentaje de interés) */
    public static String numero(double valor) {
        return BigDecimal.valueOf(valor).stripTrailingZeros().toPlainString();
    }
}
```

## `src/main/java/com/crediya/util/Validaciones.java`

```java
package com.crediya.util;

import com.crediya.excepciones.DatosInvalidosException;
import java.time.LocalDate;
import java.util.regex.Pattern;

/** Validaciones reutilizables. Todas lanzan DatosInvalidosException con un mensaje claro. */
public final class Validaciones {

    private static final Pattern CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern DOCUMENTO = Pattern.compile("^\\d{5,15}$");
    private static final Pattern TELEFONO = Pattern.compile("^\\d{7,15}$");

    private Validaciones() {
    }

    public static void validarTextoObligatorio(String valor, String campo) throws DatosInvalidosException {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException("El campo '" + campo + "' es obligatorio.");
        }
    }

    public static void validarDocumento(String documento) throws DatosInvalidosException {
        validarTextoObligatorio(documento, "documento");
        if (!DOCUMENTO.matcher(documento.trim()).matches()) {
            throw new DatosInvalidosException("El documento debe tener solo números (entre 5 y 15 dígitos).");
        }
    }

    public static void validarCorreo(String correo) throws DatosInvalidosException {
        validarTextoObligatorio(correo, "correo");
        if (!CORREO.matcher(correo.trim()).matches()) {
            throw new DatosInvalidosException("El correo '" + correo + "' no tiene un formato válido.");
        }
    }

    public static void validarTelefono(String telefono) throws DatosInvalidosException {
        validarTextoObligatorio(telefono, "teléfono");
        if (!TELEFONO.matcher(telefono.trim()).matches()) {
            throw new DatosInvalidosException("El teléfono debe tener solo números (entre 7 y 15 dígitos).");
        }
    }

    public static void validarMontoPositivo(double valor, String campo) throws DatosInvalidosException {
        if (Double.isNaN(valor) || Double.isInfinite(valor) || valor <= 0) {
            throw new DatosInvalidosException("El " + campo + " debe ser mayor a cero.");
        }
    }

    public static void validarInteres(double interes) throws DatosInvalidosException {
        if (Double.isNaN(interes) || interes < 0 || interes > 100) {
            throw new DatosInvalidosException("El interés debe estar entre 0 y 100 (%).");
        }
    }

    public static void validarCuotas(int cuotas) throws DatosInvalidosException {
        if (cuotas <= 0 || cuotas > 120) {
            throw new DatosInvalidosException("El número de cuotas debe estar entre 1 y 120.");
        }
    }

    public static void validarFechaPago(LocalDate fecha) throws DatosInvalidosException {
        if (fecha == null) {
            throw new DatosInvalidosException("La fecha del pago es obligatoria.");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new DatosInvalidosException("La fecha del pago no puede ser futura.");
        }
    }
}
```

## `src/main/java/com/crediya/persistencia/ConexionBD.java`

```java
package com.crediya.persistencia;

import com.crediya.excepciones.ErrorPersistenciaException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * PATRÓN SINGLETON: existe una única instancia de esta clase en todo el programa.
 * Guarda la configuración (host, puerto, usuario, clave) leída una sola vez de db.properties.
 *
 * Cada vez que un DAO necesita hablar con MySQL llama a getConexion(), que abre una
 * Connection nueva con DriverManager.getConnection(...). El DAO la cierra automáticamente
 * con try-with-resources. (Singleton = la configuración; la Connection NO se comparte.)
 */
public class ConexionBD {

    private static ConexionBD instancia;

    private final String url;
    private final String usuario;
    private final String clave;

    // Constructor privado: nadie puede hacer "new ConexionBD()" desde fuera.
    private ConexionBD() throws ErrorPersistenciaException {
        Properties props = new Properties();
        try (InputStream entrada = ConexionBD.class.getResourceAsStream("/db.properties")) {
            if (entrada == null) {
                throw new ErrorPersistenciaException(
                        "No se encontró el archivo db.properties (debe estar en src/main/resources).");
            }
            props.load(entrada);
        } catch (IOException e) {
            throw new ErrorPersistenciaException("No se pudo leer db.properties: " + e.getMessage(), e);
        }

        String host = props.getProperty("db.host", "localhost");
        String puerto = props.getProperty("db.port", "3306");
        String baseDatos = props.getProperty("db.name", "crediya_db");

        this.url = "jdbc:mysql://" + host + ":" + puerto + "/" + baseDatos
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota";
        this.usuario = props.getProperty("db.user", "root");
        this.clave = props.getProperty("db.password", "");
    }

    /** Punto de acceso a la única instancia. */
    public static synchronized ConexionBD getInstancia() throws ErrorPersistenciaException {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    /** Abre una conexión nueva. Quien la pida debe cerrarla (try-with-resources). */
    public Connection getConexion() throws ErrorPersistenciaException {
        try {
            return DriverManager.getConnection(url, usuario, clave);
        } catch (SQLException e) {
            throw new ErrorPersistenciaException(explicarError(e), e);
        }
    }

    /** Prueba rápida: abre y cierra una conexión. Lanza excepción si algo falla. */
    public void probarConexion() throws ErrorPersistenciaException {
        try (Connection con = getConexion()) {
            // Si llegamos aquí, la conexión funcionó.
        } catch (SQLException e) {
            throw new ErrorPersistenciaException(explicarError(e), e);
        }
    }

    /** Traduce los errores típicos de JDBC a mensajes que un estudiante entiende. */
    private String explicarError(SQLException e) {
        String detalle = " (detalle técnico: " + e.getMessage() + ")";
        if (e.getErrorCode() == 1045) {
            return "Usuario o contraseña de MySQL incorrectos. Revise db.properties." + detalle;
        }
        if (e.getErrorCode() == 1049) {
            return "La base de datos no existe. Ejecute primero sql/crediya_db.sql." + detalle;
        }
        String mensaje = String.valueOf(e.getMessage());
        if (mensaje.contains("No suitable driver")) {
            return "No se encontró el driver de MySQL. Revise la dependencia mysql-connector-j en el pom.xml." + detalle;
        }
        if (mensaje.contains("Communications link failure") || mensaje.contains("Connection refused")) {
            return "No se pudo conectar con MySQL. ¿Está encendido el servicio? Revise host y puerto en db.properties." + detalle;
        }
        return "Error de conexión con MySQL." + detalle;
    }
}
```

## `src/main/java/com/crediya/dao/Guardable.java`

```java
package com.crediya.dao;

import com.crediya.excepciones.ErrorPersistenciaException;

/** Interfaz pequeña (ISP): solo sabe guardar. */
public interface Guardable<T> {
    void guardar(T entidad) throws ErrorPersistenciaException;
}
```

## `src/main/java/com/crediya/dao/Consultable.java`

```java
package com.crediya.dao;

import com.crediya.excepciones.ErrorPersistenciaException;
import java.util.List;
import java.util.Optional;

/** Interfaz pequeña (ISP): solo sabe consultar. */
public interface Consultable<T> {
    List<T> listar() throws ErrorPersistenciaException;

    Optional<T> buscarPorId(int id) throws ErrorPersistenciaException;
}
```

## `src/main/java/com/crediya/dao/Modificable.java`

```java
package com.crediya.dao;

import com.crediya.excepciones.ErrorPersistenciaException;

/** Interfaz pequeña (ISP): solo sabe actualizar y eliminar. Un DAO de pagos NO necesita implementarla. */
public interface Modificable<T> {
    void actualizar(T entidad) throws ErrorPersistenciaException;

    void eliminar(int id) throws ErrorPersistenciaException;
}
```

## `src/main/java/com/crediya/dao/EmpleadoDAO.java`

```java
package com.crediya.dao;

import com.crediya.excepciones.ErrorPersistenciaException;
import com.crediya.modelo.Empleado;
import com.crediya.persistencia.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PATRÓN DAO (Data Access Object): toda la comunicación SQL con la tabla "empleados" vive aquí.
 * El resto del programa no sabe que existe MySQL.
 * Se usa PreparedStatement con "?" para evitar SQL Injection.
 */
public class EmpleadoDAO implements Guardable<Empleado>, Consultable<Empleado>, Modificable<Empleado> {

    private static final String SQL_INSERTAR =
            "INSERT INTO empleados (nombre, documento, rol, correo, salario) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_LISTAR =
            "SELECT id, nombre, documento, rol, correo, salario FROM empleados ORDER BY id";
    private static final String SQL_POR_ID =
            "SELECT id, nombre, documento, rol, correo, salario FROM empleados WHERE id = ?";
    private static final String SQL_POR_DOCUMENTO =
            "SELECT id, nombre, documento, rol, correo, salario FROM empleados WHERE documento = ?";
    private static final String SQL_ACTUALIZAR =
            "UPDATE empleados SET nombre = ?, documento = ?, rol = ?, correo = ?, salario = ? WHERE id = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM empleados WHERE id = ?";

    @Override
    public void guardar(Empleado e) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getDocumento());
            ps.setString(3, e.getRol());
            ps.setString(4, e.getCorreo());
            ps.setDouble(5, e.getSalario());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    e.setId(claves.getInt(1));   // MySQL nos devuelve el ID autogenerado
                }
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al guardar el empleado: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Empleado> listar() throws ErrorPersistenciaException {
        List<Empleado> empleados = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                empleados.add(mapear(rs));
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al listar empleados: " + ex.getMessage(), ex);
        }
        return empleados;
    }

    @Override
    public Optional<Empleado> buscarPorId(int id) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_POR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al buscar el empleado: " + ex.getMessage(), ex);
        }
    }

    public Optional<Empleado> buscarPorDocumento(String documento) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_POR_DOCUMENTO)) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al buscar el empleado: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void actualizar(Empleado e) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getDocumento());
            ps.setString(3, e.getRol());
            ps.setString(4, e.getCorreo());
            ps.setDouble(5, e.getSalario());
            ps.setInt(6, e.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al actualizar el empleado: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void eliminar(int id) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al eliminar el empleado: " + ex.getMessage(), ex);
        }
    }

    /** Convierte la fila actual del ResultSet en un objeto Empleado. */
    private Empleado mapear(ResultSet rs) throws SQLException {
        return new Empleado(rs.getInt("id"), rs.getString("nombre"), rs.getString("documento"),
                rs.getString("correo"), rs.getString("rol"), rs.getDouble("salario"));
    }
}
```

## `src/main/java/com/crediya/dao/ClienteDAO.java`

```java
package com.crediya.dao;

import com.crediya.excepciones.ErrorPersistenciaException;
import com.crediya.modelo.Cliente;
import com.crediya.persistencia.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** PATRÓN DAO para la tabla "clientes". */
public class ClienteDAO implements Guardable<Cliente>, Consultable<Cliente>, Modificable<Cliente> {

    private static final String SQL_INSERTAR =
            "INSERT INTO clientes (nombre, documento, correo, telefono) VALUES (?, ?, ?, ?)";
    private static final String SQL_LISTAR =
            "SELECT id, nombre, documento, correo, telefono FROM clientes ORDER BY id";
    private static final String SQL_POR_ID =
            "SELECT id, nombre, documento, correo, telefono FROM clientes WHERE id = ?";
    private static final String SQL_POR_DOCUMENTO =
            "SELECT id, nombre, documento, correo, telefono FROM clientes WHERE documento = ?";
    private static final String SQL_ACTUALIZAR =
            "UPDATE clientes SET nombre = ?, documento = ?, correo = ?, telefono = ? WHERE id = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM clientes WHERE id = ?";

    @Override
    public void guardar(Cliente c) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDocumento());
            ps.setString(3, c.getCorreo());
            ps.setString(4, c.getTelefono());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    c.setId(claves.getInt(1));
                }
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al guardar el cliente: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Cliente> listar() throws ErrorPersistenciaException {
        List<Cliente> clientes = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(mapear(rs));
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al listar clientes: " + ex.getMessage(), ex);
        }
        return clientes;
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_POR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al buscar el cliente: " + ex.getMessage(), ex);
        }
    }

    public Optional<Cliente> buscarPorDocumento(String documento) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_POR_DOCUMENTO)) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al buscar el cliente: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void actualizar(Cliente c) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDocumento());
            ps.setString(3, c.getCorreo());
            ps.setString(4, c.getTelefono());
            ps.setInt(5, c.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al actualizar el cliente: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void eliminar(int id) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al eliminar el cliente: " + ex.getMessage(), ex);
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(rs.getInt("id"), rs.getString("nombre"), rs.getString("documento"),
                rs.getString("correo"), rs.getString("telefono"));
    }
}
```

## `src/main/java/com/crediya/dao/PrestamoDAO.java`

```java
package com.crediya.dao;

import com.crediya.excepciones.ErrorPersistenciaException;
import com.crediya.modelo.Cliente;
import com.crediya.modelo.Empleado;
import com.crediya.modelo.EstadoPrestamo;
import com.crediya.modelo.Prestamo;
import com.crediya.persistencia.ConexionBD;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PATRÓN DAO para la tabla "prestamos".
 * Al leer, hace JOIN con clientes y empleados para devolver el Prestamo con sus objetos completos.
 * No implementa Modificable: un préstamo no se edita ni se borra, solo cambia de estado/saldo (ISP).
 */
public class PrestamoDAO implements Guardable<Prestamo>, Consultable<Prestamo> {

    private static final String SQL_BASE =
            "SELECT p.id, p.monto, p.interes, p.cuotas, p.fecha_inicio, p.estado, p.saldo_pendiente, "
            + "c.id AS cliente_id, c.nombre AS cliente_nombre, c.documento AS cliente_documento, "
            + "c.correo AS cliente_correo, c.telefono AS cliente_telefono, "
            + "e.id AS empleado_id, e.nombre AS empleado_nombre, e.documento AS empleado_documento, "
            + "e.rol AS empleado_rol, e.correo AS empleado_correo, e.salario AS empleado_salario "
            + "FROM prestamos p "
            + "JOIN clientes c ON p.cliente_id = c.id "
            + "JOIN empleados e ON p.empleado_id = e.id ";

    private static final String SQL_INSERTAR =
            "INSERT INTO prestamos (cliente_id, empleado_id, monto, interes, cuotas, fecha_inicio, estado, saldo_pendiente) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    @Override
    public void guardar(Prestamo p) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getCliente().getId());
            ps.setInt(2, p.getEmpleado().getId());
            ps.setDouble(3, p.getMonto());
            ps.setDouble(4, p.getInteres());
            ps.setInt(5, p.getCuotas());
            ps.setDate(6, Date.valueOf(p.getFechaInicio()));
            ps.setString(7, p.getEstado().name());
            ps.setDouble(8, p.getSaldoPendiente());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    p.setId(claves.getInt(1));
                }
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al guardar el préstamo: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Prestamo> listar() throws ErrorPersistenciaException {
        return consultarLista(SQL_BASE + "ORDER BY p.id", 0, false);
    }

    @Override
    public Optional<Prestamo> buscarPorId(int id) throws ErrorPersistenciaException {
        List<Prestamo> resultado = consultarLista(SQL_BASE + "WHERE p.id = ?", id, true);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public List<Prestamo> listarPorCliente(int clienteId) throws ErrorPersistenciaException {
        return consultarLista(SQL_BASE + "WHERE p.cliente_id = ? ORDER BY p.id", clienteId, true);
    }

    public int contarPorCliente(int clienteId) throws ErrorPersistenciaException {
        return contar("SELECT COUNT(*) FROM prestamos WHERE cliente_id = ?", clienteId);
    }

    public int contarPorEmpleado(int empleadoId) throws ErrorPersistenciaException {
        return contar("SELECT COUNT(*) FROM prestamos WHERE empleado_id = ?", empleadoId);
    }

    public void actualizarEstado(int id, EstadoPrestamo estado) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement("UPDATE prestamos SET estado = ? WHERE id = ?")) {
            ps.setString(1, estado.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al actualizar el estado: " + ex.getMessage(), ex);
        }
    }

    // ------------------------------ privados ------------------------------

    private List<Prestamo> consultarLista(String sql, int parametro, boolean usaParametro)
            throws ErrorPersistenciaException {
        List<Prestamo> prestamos = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (usaParametro) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapear(rs));
                }
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al consultar préstamos: " + ex.getMessage(), ex);
        }
        return prestamos;
    }

    private int contar(String sql, int parametro) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al contar préstamos: " + ex.getMessage(), ex);
        }
    }

    private Prestamo mapear(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente(rs.getInt("cliente_id"), rs.getString("cliente_nombre"),
                rs.getString("cliente_documento"), rs.getString("cliente_correo"), rs.getString("cliente_telefono"));
        Empleado empleado = new Empleado(rs.getInt("empleado_id"), rs.getString("empleado_nombre"),
                rs.getString("empleado_documento"), rs.getString("empleado_correo"),
                rs.getString("empleado_rol"), rs.getDouble("empleado_salario"));
        return new Prestamo(rs.getInt("id"), cliente, empleado, rs.getDouble("monto"), rs.getDouble("interes"),
                rs.getInt("cuotas"), rs.getDate("fecha_inicio").toLocalDate(),
                EstadoPrestamo.valueOf(rs.getString("estado")), rs.getDouble("saldo_pendiente"));
    }
}
```

## `src/main/java/com/crediya/dao/PagoDAO.java`

```java
package com.crediya.dao;

import com.crediya.excepciones.ErrorPersistenciaException;
import com.crediya.modelo.EstadoPrestamo;
import com.crediya.modelo.Pago;
import com.crediya.persistencia.ConexionBD;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** PATRÓN DAO para la tabla "pagos". Un pago no se edita ni se borra (por eso no implementa Modificable). */
public class PagoDAO implements Guardable<Pago>, Consultable<Pago> {

    private static final String SQL_INSERTAR = "INSERT INTO pagos (prestamo_id, fecha_pago, monto) VALUES (?, ?, ?)";
    private static final String SQL_LISTAR = "SELECT id, prestamo_id, fecha_pago, monto FROM pagos ORDER BY fecha_pago, id";
    private static final String SQL_POR_ID = "SELECT id, prestamo_id, fecha_pago, monto FROM pagos WHERE id = ?";
    private static final String SQL_POR_PRESTAMO =
            "SELECT id, prestamo_id, fecha_pago, monto FROM pagos WHERE prestamo_id = ? ORDER BY fecha_pago, id";

    @Override
    public void guardar(Pago pago) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion()) {
            insertar(con, pago);
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al guardar el pago: " + ex.getMessage(), ex);
        }
    }

    /**
     * Guarda el pago Y actualiza el saldo/estado del préstamo en UNA SOLA TRANSACCIÓN:
     * o se hacen las dos cosas, o no se hace ninguna (rollback). Así nunca queda un pago
     * registrado con el saldo sin actualizar.
     */
    public void registrarPagoYActualizarPrestamo(Pago pago, double nuevoSaldo, EstadoPrestamo nuevoEstado)
            throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion()) {
            try {
                con.setAutoCommit(false);              // inicia la transacción
                insertar(con, pago);
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE prestamos SET saldo_pendiente = ?, estado = ? WHERE id = ?")) {
                    ps.setDouble(1, nuevoSaldo);
                    ps.setString(2, nuevoEstado.name());
                    ps.setInt(3, pago.getPrestamoId());
                    ps.executeUpdate();
                }
                con.commit();                          // confirma las dos operaciones
            } catch (SQLException ex) {
                con.rollback();                        // deshace todo si algo falló
                throw ex;
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al registrar el pago: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Pago> listar() throws ErrorPersistenciaException {
        return consultar(SQL_LISTAR, 0, false);
    }

    @Override
    public Optional<Pago> buscarPorId(int id) throws ErrorPersistenciaException {
        List<Pago> resultado = consultar(SQL_POR_ID, id, true);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public List<Pago> listarPorPrestamo(int prestamoId) throws ErrorPersistenciaException {
        return consultar(SQL_POR_PRESTAMO, prestamoId, true);
    }

    // ------------------------------ privados ------------------------------

    private void insertar(Connection con, Pago pago) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pago.getPrestamoId());
            ps.setDate(2, Date.valueOf(pago.getFechaPago()));
            ps.setDouble(3, pago.getMonto());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    pago.setId(claves.getInt(1));
                }
            }
        }
    }

    private List<Pago> consultar(String sql, int parametro, boolean usaParametro) throws ErrorPersistenciaException {
        List<Pago> pagos = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (usaParametro) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pagos.add(new Pago(rs.getInt("id"), rs.getInt("prestamo_id"),
                            rs.getDate("fecha_pago").toLocalDate(), rs.getDouble("monto")));
                }
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al consultar pagos: " + ex.getMessage(), ex);
        }
        return pagos;
    }
}
```

## `src/main/java/com/crediya/servicio/EmpleadoService.java`

```java
package com.crediya.servicio;

import com.crediya.dao.EmpleadoDAO;
import com.crediya.dao.PrestamoDAO;
import com.crediya.excepciones.CrediYaException;
import com.crediya.excepciones.DatosInvalidosException;
import com.crediya.excepciones.EmpleadoNoEncontradoException;
import com.crediya.excepciones.ErrorPersistenciaException;
import com.crediya.modelo.Empleado;
import com.crediya.util.Validaciones;
import java.util.List;

/** Lógica de negocio de empleados: valida, comprueba reglas y delega el guardado al DAO. */
public class EmpleadoService {

    private final EmpleadoDAO empleadoDAO;
    private final PrestamoDAO prestamoDAO;

    public EmpleadoService(EmpleadoDAO empleadoDAO, PrestamoDAO prestamoDAO) {
        this.empleadoDAO = empleadoDAO;
        this.prestamoDAO = prestamoDAO;
    }

    public Empleado registrar(String nombre, String documento, String rol, String correo, double salario)
            throws CrediYaException {
        validar(nombre, documento, rol, correo, salario);
        if (empleadoDAO.buscarPorDocumento(documento.trim()).isPresent()) {
            throw new DatosInvalidosException("Ya existe un empleado con el documento " + documento.trim() + ".");
        }
        Empleado empleado = new Empleado(0, nombre.trim(), documento.trim(), correo.trim(), rol.trim(), salario);
        empleadoDAO.guardar(empleado);
        return empleado;
    }

    public List<Empleado> listar() throws ErrorPersistenciaException {
        return empleadoDAO.listar();
    }

    public Empleado buscarPorId(int id) throws CrediYaException {
        return empleadoDAO.buscarPorId(id)
                .orElseThrow(() -> new EmpleadoNoEncontradoException("No existe un empleado con ID " + id + "."));
    }

    public Empleado buscarPorDocumento(String documento) throws CrediYaException {
        return empleadoDAO.buscarPorDocumento(documento.trim())
                .orElseThrow(() -> new EmpleadoNoEncontradoException(
                        "No existe un empleado con documento " + documento.trim() + "."));
    }

    public Empleado actualizar(int id, String nombre, String documento, String rol, String correo, double salario)
            throws CrediYaException {
        Empleado empleado = buscarPorId(id);
        validar(nombre, documento, rol, correo, salario);
        // Si cambió el documento, verificar que no lo tenga otro empleado.
        boolean documentoOcupado = empleadoDAO.buscarPorDocumento(documento.trim())
                .map(otro -> otro.getId() != id).orElse(false);
        if (documentoOcupado) {
            throw new DatosInvalidosException("Ya existe otro empleado con el documento " + documento.trim() + ".");
        }
        empleado.setNombre(nombre.trim());
        empleado.setDocumento(documento.trim());
        empleado.setRol(rol.trim());
        empleado.setCorreo(correo.trim());
        empleado.setSalario(salario);
        empleadoDAO.actualizar(empleado);
        return empleado;
    }

    /** Solo se puede eliminar un empleado que no haya gestionado préstamos. */
    public void eliminar(int id) throws CrediYaException {
        buscarPorId(id);
        if (prestamoDAO.contarPorEmpleado(id) > 0) {
            throw new DatosInvalidosException("No se puede eliminar: el empleado tiene préstamos asociados.");
        }
        empleadoDAO.eliminar(id);
    }

    private void validar(String nombre, String documento, String rol, String correo, double salario)
            throws DatosInvalidosException {
        Validaciones.validarTextoObligatorio(nombre, "nombre");
        Validaciones.validarDocumento(documento);
        Validaciones.validarTextoObligatorio(rol, "rol");
        Validaciones.validarCorreo(correo);
        Validaciones.validarMontoPositivo(salario, "salario");
    }
}
```

## `src/main/java/com/crediya/servicio/ClienteService.java`

```java
package com.crediya.servicio;

import com.crediya.dao.ClienteDAO;
import com.crediya.dao.PrestamoDAO;
import com.crediya.excepciones.ClienteNoEncontradoException;
import com.crediya.excepciones.CrediYaException;
import com.crediya.excepciones.DatosInvalidosException;
import com.crediya.excepciones.ErrorPersistenciaException;
import com.crediya.modelo.Cliente;
import com.crediya.modelo.Prestamo;
import com.crediya.util.Validaciones;
import java.util.List;

/** Lógica de negocio de clientes. */
public class ClienteService {

    private final ClienteDAO clienteDAO;
    private final PrestamoDAO prestamoDAO;

    public ClienteService(ClienteDAO clienteDAO, PrestamoDAO prestamoDAO) {
        this.clienteDAO = clienteDAO;
        this.prestamoDAO = prestamoDAO;
    }

    public Cliente registrar(String nombre, String documento, String correo, String telefono)
            throws CrediYaException {
        validar(nombre, documento, correo, telefono);
        if (clienteDAO.buscarPorDocumento(documento.trim()).isPresent()) {
            throw new DatosInvalidosException("Ya existe un cliente con el documento " + documento.trim() + ".");
        }
        Cliente cliente = new Cliente(0, nombre.trim(), documento.trim(), correo.trim(), telefono.trim());
        clienteDAO.guardar(cliente);
        return cliente;
    }

    public List<Cliente> listar() throws ErrorPersistenciaException {
        return clienteDAO.listar();
    }

    public Cliente buscarPorId(int id) throws CrediYaException {
        return clienteDAO.buscarPorId(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("No existe un cliente con ID " + id + "."));
    }

    public Cliente buscarPorDocumento(String documento) throws CrediYaException {
        return clienteDAO.buscarPorDocumento(documento.trim())
                .orElseThrow(() -> new ClienteNoEncontradoException(
                        "No existe un cliente con documento " + documento.trim() + "."));
    }

    public Cliente actualizar(int id, String nombre, String documento, String correo, String telefono)
            throws CrediYaException {
        Cliente cliente = buscarPorId(id);
        validar(nombre, documento, correo, telefono);
        boolean documentoOcupado = clienteDAO.buscarPorDocumento(documento.trim())
                .map(otro -> otro.getId() != id).orElse(false);
        if (documentoOcupado) {
            throw new DatosInvalidosException("Ya existe otro cliente con el documento " + documento.trim() + ".");
        }
        cliente.setNombre(nombre.trim());
        cliente.setDocumento(documento.trim());
        cliente.setCorreo(correo.trim());
        cliente.setTelefono(telefono.trim());
        clienteDAO.actualizar(cliente);
        return cliente;
    }

    /** Solo se puede eliminar un cliente sin préstamos relacionados. */
    public void eliminar(int id) throws CrediYaException {
        buscarPorId(id);
        if (prestamoDAO.contarPorCliente(id) > 0) {
            throw new DatosInvalidosException("No se puede eliminar: el cliente tiene préstamos asociados.");
        }
        clienteDAO.eliminar(id);
    }

    public List<Prestamo> prestamosDelCliente(int clienteId) throws CrediYaException {
        buscarPorId(clienteId);   // lanza ClienteNoEncontradoException si no existe
        return prestamoDAO.listarPorCliente(clienteId);
    }

    private void validar(String nombre, String documento, String correo, String telefono)
            throws DatosInvalidosException {
        Validaciones.validarTextoObligatorio(nombre, "nombre");
        Validaciones.validarDocumento(documento);
        Validaciones.validarCorreo(correo);
        Validaciones.validarTelefono(telefono);
    }
}
```

## `src/main/java/com/crediya/servicio/PrestamoService.java`

```java
package com.crediya.servicio;

import com.crediya.dao.ClienteDAO;
import com.crediya.dao.EmpleadoDAO;
import com.crediya.dao.PrestamoDAO;
import com.crediya.excepciones.ClienteNoEncontradoException;
import com.crediya.excepciones.CrediYaException;
import com.crediya.excepciones.DatosInvalidosException;
import com.crediya.excepciones.EmpleadoNoEncontradoException;
import com.crediya.excepciones.ErrorPersistenciaException;
import com.crediya.excepciones.PrestamoNoEncontradoException;
import com.crediya.modelo.Cliente;
import com.crediya.modelo.Empleado;
import com.crediya.modelo.EstadoPrestamo;
import com.crediya.modelo.Prestamo;
import com.crediya.util.CalculadoraPrestamo;
import com.crediya.util.Formato;
import com.crediya.util.Validaciones;
import java.time.LocalDate;
import java.util.List;

/** Lógica de negocio de préstamos. */
public class PrestamoService {

    private final PrestamoDAO prestamoDAO;
    private final ClienteDAO clienteDAO;
    private final EmpleadoDAO empleadoDAO;

    public PrestamoService(PrestamoDAO prestamoDAO, ClienteDAO clienteDAO, EmpleadoDAO empleadoDAO) {
        this.prestamoDAO = prestamoDAO;
        this.clienteDAO = clienteDAO;
        this.empleadoDAO = empleadoDAO;
    }

    public Prestamo crear(int clienteId, int empleadoId, double monto, double interes, int cuotas,
                          LocalDate fechaInicio) throws CrediYaException {
        Validaciones.validarMontoPositivo(monto, "monto");
        Validaciones.validarInteres(interes);
        Validaciones.validarCuotas(cuotas);
        if (fechaInicio == null) {
            throw new DatosInvalidosException("La fecha de inicio es obligatoria.");
        }
        Cliente cliente = clienteDAO.buscarPorId(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException("No existe un cliente con ID " + clienteId + "."));
        Empleado empleado = empleadoDAO.buscarPorId(empleadoId)
                .orElseThrow(() -> new EmpleadoNoEncontradoException("No existe un empleado con ID " + empleadoId + "."));

        // El saldo inicial es el monto total (capital + interés). El estado inicial es PENDIENTE.
        double total = CalculadoraPrestamo.calcularMontoTotal(monto, interes);
        Prestamo prestamo = new Prestamo(0, cliente, empleado, monto, interes, cuotas, fechaInicio,
                EstadoPrestamo.PENDIENTE, total);
        prestamoDAO.guardar(prestamo);
        return prestamo;
    }

    public List<Prestamo> listar() throws ErrorPersistenciaException {
        return prestamoDAO.listar();
    }

    public Prestamo buscarPorId(int id) throws CrediYaException {
        return prestamoDAO.buscarPorId(id)
                .orElseThrow(() -> new PrestamoNoEncontradoException("No existe un préstamo con ID " + id + "."));
    }

    // Lambda + Stream API: filtrar la lista por estado.
    public List<Prestamo> listarActivos() throws ErrorPersistenciaException {
        return prestamoDAO.listar().stream()
                .filter(p -> p.getEstado() == EstadoPrestamo.PENDIENTE)
                .toList();
    }

    public List<Prestamo> listarPagados() throws ErrorPersistenciaException {
        return prestamoDAO.listar().stream()
                .filter(p -> p.getEstado() == EstadoPrestamo.PAGADO)
                .toList();
    }

    /** Cambio manual de estado, respetando la regla: PAGADO si y solo si el saldo es 0. */
    public Prestamo actualizarEstado(int id, EstadoPrestamo nuevoEstado) throws CrediYaException {
        Prestamo prestamo = buscarPorId(id);
        boolean sinSaldo = prestamo.getSaldoPendiente() <= 0;
        if (nuevoEstado == EstadoPrestamo.PAGADO && !sinSaldo) {
            throw new DatosInvalidosException("No se puede marcar como PAGADO: aún hay un saldo de "
                    + Formato.moneda(prestamo.getSaldoPendiente()) + ".");
        }
        if (nuevoEstado != EstadoPrestamo.PAGADO && sinSaldo) {
            throw new DatosInvalidosException("El préstamo ya no tiene saldo, su estado debe ser PAGADO.");
        }
        prestamoDAO.actualizarEstado(id, nuevoEstado);
        prestamo.setEstado(nuevoEstado);
        return prestamo;
    }
}
```

## `src/main/java/com/crediya/servicio/PagoService.java`

```java
package com.crediya.servicio;

import com.crediya.dao.PagoDAO;
import com.crediya.dao.PrestamoDAO;
import com.crediya.excepciones.CrediYaException;
import com.crediya.excepciones.ErrorPersistenciaException;
import com.crediya.excepciones.PagoInvalidoException;
import com.crediya.excepciones.PrestamoNoEncontradoException;
import com.crediya.modelo.EstadoPrestamo;
import com.crediya.modelo.Pago;
import com.crediya.modelo.Prestamo;
import com.crediya.util.CalculadoraPrestamo;
import com.crediya.util.Formato;
import com.crediya.util.Validaciones;
import java.time.LocalDate;
import java.util.List;

/** Lógica de negocio de pagos: valida, calcula el nuevo saldo y cambia el estado a PAGADO si llega a 0. */
public class PagoService {

    private final PagoDAO pagoDAO;
    private final PrestamoDAO prestamoDAO;

    public PagoService(PagoDAO pagoDAO, PrestamoDAO prestamoDAO) {
        this.pagoDAO = pagoDAO;
        this.prestamoDAO = prestamoDAO;
    }

    /** Registra un pago o abono y devuelve el préstamo con su saldo y estado ya actualizados. */
    public Prestamo registrar(int prestamoId, double monto, LocalDate fecha) throws CrediYaException {
        Prestamo prestamo = buscarPrestamo(prestamoId);
        Validaciones.validarMontoPositivo(monto, "monto del pago");
        Validaciones.validarFechaPago(fecha);

        if (prestamo.getEstado() == EstadoPrestamo.PAGADO) {
            throw new PagoInvalidoException("El préstamo #" + prestamoId + " ya está pagado en su totalidad.");
        }
        if (fecha.isBefore(prestamo.getFechaInicio())) {
            throw new PagoInvalidoException("La fecha del pago no puede ser anterior al inicio del préstamo ("
                    + prestamo.getFechaInicio() + ").");
        }

        double montoPago = CalculadoraPrestamo.redondear(monto);
        double saldoActual = prestamo.getSaldoPendiente();
        if (montoPago > saldoActual) {
            throw new PagoInvalidoException("El pago de " + Formato.moneda(montoPago)
                    + " supera el saldo pendiente de " + Formato.moneda(saldoActual) + ". Pago rechazado.");
        }

        double nuevoSaldo = CalculadoraPrestamo.redondear(saldoActual - montoPago);
        EstadoPrestamo nuevoEstado = (nuevoSaldo <= 0) ? EstadoPrestamo.PAGADO : prestamo.getEstado();

        Pago pago = new Pago(0, prestamoId, fecha, montoPago);
        pagoDAO.registrarPagoYActualizarPrestamo(pago, nuevoSaldo, nuevoEstado);

        prestamo.setSaldoPendiente(nuevoSaldo);
        prestamo.setEstado(nuevoEstado);
        return prestamo;
    }

    public List<Pago> listar() throws ErrorPersistenciaException {
        return pagoDAO.listar();
    }

    public List<Pago> historial(int prestamoId) throws CrediYaException {
        buscarPrestamo(prestamoId);
        return pagoDAO.listarPorPrestamo(prestamoId);
    }

    public double consultarSaldo(int prestamoId) throws CrediYaException {
        return buscarPrestamo(prestamoId).getSaldoPendiente();
    }

    private Prestamo buscarPrestamo(int id) throws CrediYaException {
        return prestamoDAO.buscarPorId(id)
                .orElseThrow(() -> new PrestamoNoEncontradoException("No existe un préstamo con ID " + id + "."));
    }
}
```

## `src/main/java/com/crediya/servicio/ReporteService.java`

```java
package com.crediya.servicio;

import com.crediya.dao.PagoDAO;
import com.crediya.dao.PrestamoDAO;
import com.crediya.excepciones.ErrorPersistenciaException;
import com.crediya.modelo.Cliente;
import com.crediya.modelo.EstadoPrestamo;
import com.crediya.modelo.Pago;
import com.crediya.modelo.Prestamo;
import com.crediya.util.CalculadoraPrestamo;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Reportes de la cartera. Aquí se usan expresiones Lambda y Stream API
 * (filter, map, mapToDouble, groupingBy, ...). Esta clase NO imprime: solo calcula y devuelve datos.
 */
public class ReporteService {

    private final PrestamoDAO prestamoDAO;
    private final PagoDAO pagoDAO;

    public ReporteService(PrestamoDAO prestamoDAO, PagoDAO pagoDAO) {
        this.prestamoDAO = prestamoDAO;
        this.pagoDAO = pagoDAO;
    }

    // ---- Reportes 1 a 7 ----

    /** Reporte 1: préstamos con estado PENDIENTE. */
    public List<Prestamo> prestamosActivos() throws ErrorPersistenciaException {
        return filtrarPorEstado(EstadoPrestamo.PENDIENTE);
    }

    /** Reporte 2: préstamos con estado PAGADO. */
    public List<Prestamo> prestamosPagados() throws ErrorPersistenciaException {
        return filtrarPorEstado(EstadoPrestamo.PAGADO);
    }

    /**
     * Reporte 3: préstamos en mora. Regla: cada mes desde la fecha de inicio vence una cuota;
     * está en mora si lo pagado es menor a lo que ya debía estar pagado, o si fue marcado VENCIDO.
     */
    public List<Prestamo> prestamosEnMora() throws ErrorPersistenciaException {
        return prestamoDAO.listar().stream()
                .filter(Prestamo::estaEnMora)
                .toList();
    }

    /** Reporte 3: clientes morosos (sin repetir el mismo cliente). */
    public List<Cliente> clientesMorosos() throws ErrorPersistenciaException {
        Map<Integer, Cliente> porId = prestamosEnMora().stream()
                .map(Prestamo::getCliente)
                .collect(Collectors.toMap(Cliente::getId, c -> c, (a, b) -> a, LinkedHashMap::new));
        return new ArrayList<>(porId.values());
    }

    /** Reporte 4: suma de los saldos pendientes de todos los préstamos. */
    public double totalCartera() throws ErrorPersistenciaException {
        double total = prestamoDAO.listar().stream()
                .mapToDouble(Prestamo::getSaldoPendiente)
                .sum();
        return CalculadoraPrestamo.redondear(total);
    }

    /** Reporte 5: suma de todos los pagos registrados. */
    public double totalPagos() throws ErrorPersistenciaException {
        double total = pagoDAO.listar().stream()
                .mapToDouble(Pago::getMonto)
                .sum();
        return CalculadoraPrestamo.redondear(total);
    }

    /** Reporte 6: cantidad de préstamos gestionados por cada empleado. */
    public Map<String, Long> prestamosPorEmpleado() throws ErrorPersistenciaException {
        return prestamoDAO.listar().stream()
                .collect(Collectors.groupingBy(
                        p -> p.getEmpleado().getNombre() + " (ID " + p.getEmpleado().getId() + ")",
                        TreeMap::new,
                        Collectors.counting()));
    }

    /** Reporte 7: préstamos agrupados por cliente. */
    public Map<String, List<Prestamo>> prestamosPorCliente() throws ErrorPersistenciaException {
        return prestamoDAO.listar().stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCliente().getNombre() + " (ID " + p.getCliente().getId() + ")",
                        TreeMap::new,
                        Collectors.toList()));
    }

    // ---- Filtros ----

    public List<Prestamo> filtrarPorEstado(EstadoPrestamo estado) throws ErrorPersistenciaException {
        return prestamoDAO.listar().stream()
                .filter(p -> p.getEstado() == estado)
                .toList();
    }

    public List<Prestamo> filtrarPorCliente(int clienteId) throws ErrorPersistenciaException {
        return prestamoDAO.listar().stream()
                .filter(p -> p.getCliente().getId() == clienteId)
                .toList();
    }

    public List<Prestamo> filtrarPorEmpleado(int empleadoId) throws ErrorPersistenciaException {
        return prestamoDAO.listar().stream()
                .filter(p -> p.getEmpleado().getId() == empleadoId)
                .toList();
    }

    public List<Prestamo> filtrarPorRangoDeMonto(double minimo, double maximo) throws ErrorPersistenciaException {
        return prestamoDAO.listar().stream()
                .filter(p -> p.getMonto() >= minimo && p.getMonto() <= maximo)
                .toList();
    }

    public List<Prestamo> filtrarPorSaldoMayorA(double valor) throws ErrorPersistenciaException {
        return prestamoDAO.listar().stream()
                .filter(p -> p.getSaldoPendiente() > valor)
                .toList();
    }
}
```

## `src/main/java/com/crediya/persistencia/ArchivoService.java`

```java
package com.crediya.persistencia;

import com.crediya.excepciones.ErrorPersistenciaException;
import com.crediya.modelo.Cliente;
import com.crediya.modelo.Empleado;
import com.crediya.modelo.Pago;
import com.crediya.modelo.Prestamo;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia local en archivos de texto (.txt) dentro de la carpeta "datos".
 * Esta clase es la ÚNICA que sabe leer y escribir archivos: las entidades no saben nada de archivos.
 *
 * Formato: un registro por línea, campos separados por ";".
 * Las líneas que empiezan con "#" son comentarios (encabezado) y se ignoran al leer.
 * Guardar SIEMPRE reescribe el archivo completo con la lista recibida (así también "actualiza").
 */
public class ArchivoService {

    private static final String SEP = ";";

    private final Path carpeta;

    public ArchivoService() {
        this("datos");
    }

    public ArchivoService(String nombreCarpeta) {
        this.carpeta = Paths.get(nombreCarpeta);
    }

    public String getRutaAbsoluta() {
        return carpeta.toAbsolutePath().toString();
    }

    // ------------------------------ EMPLEADOS ------------------------------

    public void guardarEmpleados(List<Empleado> empleados) throws ErrorPersistenciaException {
        List<String> lineas = new ArrayList<>();
        lineas.add("# id;nombre;documento;rol;correo;salario");
        for (Empleado e : empleados) {
            lineas.add(String.join(SEP, String.valueOf(e.getId()), limpiar(e.getNombre()), limpiar(e.getDocumento()),
                    limpiar(e.getRol()), limpiar(e.getCorreo()), numero(e.getSalario())));
        }
        escribir("empleados.txt", lineas);
    }

    public List<Empleado> leerEmpleados() throws ErrorPersistenciaException {
        List<Empleado> empleados = new ArrayList<>();
        for (String[] c : leerCampos("empleados.txt", 6)) {
            try {
                empleados.add(new Empleado(Integer.parseInt(c[0]), c[1], c[2], c[4], c[3], Double.parseDouble(c[5])));
            } catch (NumberFormatException e) {
                throw new ErrorPersistenciaException("Línea inválida en empleados.txt: " + String.join(SEP, c), e);
            }
        }
        return empleados;
    }

    // ------------------------------ CLIENTES ------------------------------

    public void guardarClientes(List<Cliente> clientes) throws ErrorPersistenciaException {
        List<String> lineas = new ArrayList<>();
        lineas.add("# id;nombre;documento;correo;telefono");
        for (Cliente c : clientes) {
            lineas.add(String.join(SEP, String.valueOf(c.getId()), limpiar(c.getNombre()), limpiar(c.getDocumento()),
                    limpiar(c.getCorreo()), limpiar(c.getTelefono())));
        }
        escribir("clientes.txt", lineas);
    }

    public List<Cliente> leerClientes() throws ErrorPersistenciaException {
        List<Cliente> clientes = new ArrayList<>();
        for (String[] c : leerCampos("clientes.txt", 5)) {
            try {
                clientes.add(new Cliente(Integer.parseInt(c[0]), c[1], c[2], c[3], c[4]));
            } catch (NumberFormatException e) {
                throw new ErrorPersistenciaException("Línea inválida en clientes.txt: " + String.join(SEP, c), e);
            }
        }
        return clientes;
    }

    // ------------------------------ PRÉSTAMOS Y PAGOS (solo respaldo) ------------------------------

    public void guardarPrestamos(List<Prestamo> prestamos) throws ErrorPersistenciaException {
        List<String> lineas = new ArrayList<>();
        lineas.add("# id;clienteId;empleadoId;monto;interes;cuotas;fechaInicio;estado;saldoPendiente");
        for (Prestamo p : prestamos) {
            lineas.add(String.join(SEP, String.valueOf(p.getId()), String.valueOf(p.getCliente().getId()),
                    String.valueOf(p.getEmpleado().getId()), numero(p.getMonto()), numero(p.getInteres()),
                    String.valueOf(p.getCuotas()), p.getFechaInicio().toString(), p.getEstado().name(),
                    numero(p.getSaldoPendiente())));
        }
        escribir("prestamos.txt", lineas);
    }

    public void guardarPagos(List<Pago> pagos) throws ErrorPersistenciaException {
        List<String> lineas = new ArrayList<>();
        lineas.add("# id;prestamoId;fechaPago;monto");
        for (Pago p : pagos) {
            lineas.add(String.join(SEP, String.valueOf(p.getId()), String.valueOf(p.getPrestamoId()),
                    p.getFechaPago().toString(), numero(p.getMonto())));
        }
        escribir("pagos.txt", lineas);
    }

    /** Devuelve todas las líneas de un archivo (para mostrarlo en consola). */
    public List<String> leerLineas(String nombreArchivo) throws ErrorPersistenciaException {
        Path ruta = carpeta.resolve(nombreArchivo);
        if (!Files.exists(ruta)) {
            throw new ErrorPersistenciaException("El archivo " + nombreArchivo
                    + " no existe todavía. Primero use 'Exportar todo a archivos'.");
        }
        try {
            return Files.readAllLines(ruta, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ErrorPersistenciaException("No se pudo leer " + nombreArchivo + ": " + e.getMessage(), e);
        }
    }

    // ------------------------------ MÉTODOS PRIVADOS ------------------------------

    private void escribir(String nombreArchivo, List<String> lineas) throws ErrorPersistenciaException {
        try {
            Files.createDirectories(carpeta);
            Files.write(carpeta.resolve(nombreArchivo), lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ErrorPersistenciaException("No se pudo escribir " + nombreArchivo + ": " + e.getMessage(), e);
        }
    }

    /** Lee un archivo y devuelve los campos de cada línea de datos, validando la cantidad de columnas. */
    private List<String[]> leerCampos(String nombreArchivo, int columnasEsperadas) throws ErrorPersistenciaException {
        List<String[]> filas = new ArrayList<>();
        for (String linea : leerLineas(nombreArchivo)) {
            if (linea.isBlank() || linea.startsWith("#")) {
                continue;
            }
            String[] campos = linea.split(SEP, -1);
            if (campos.length != columnasEsperadas) {
                throw new ErrorPersistenciaException("Línea con formato incorrecto en " + nombreArchivo + ": " + linea);
            }
            filas.add(campos);
        }
        return filas;
    }

    /** Evita que un ';' escrito por el usuario rompa el formato del archivo. */
    private String limpiar(String texto) {
        return texto == null ? "" : texto.replace(SEP, ",");
    }

    /** 12000000.0 -> "12000000" (sin notación científica). */
    private String numero(double valor) {
        return BigDecimal.valueOf(valor).toPlainString();
    }
}
```

## `src/main/java/com/crediya/servicio/PersistenciaService.java`

```java
package com.crediya.servicio;

import com.crediya.excepciones.CrediYaException;
import com.crediya.excepciones.DatosInvalidosException;
import com.crediya.modelo.Cliente;
import com.crediya.modelo.Empleado;
import com.crediya.persistencia.ArchivoService;
import java.util.List;

/**
 * Coordina el trabajo entre MySQL y los archivos .txt:
 *  - exportar: lee de MySQL (vía servicios) y escribe archivos (vía ArchivoService)
 *  - importar: lee archivos y registra en MySQL pasando por las validaciones de los servicios
 *  - datos de prueba: registra los 2 empleados y 2 clientes del enunciado
 */
public class PersistenciaService {

    private final ArchivoService archivoService;
    private final EmpleadoService empleadoService;
    private final ClienteService clienteService;
    private final PrestamoService prestamoService;
    private final PagoService pagoService;

    public PersistenciaService(ArchivoService archivoService, EmpleadoService empleadoService,
                               ClienteService clienteService, PrestamoService prestamoService,
                               PagoService pagoService) {
        this.archivoService = archivoService;
        this.empleadoService = empleadoService;
        this.clienteService = clienteService;
        this.prestamoService = prestamoService;
        this.pagoService = pagoService;
    }

    public String exportarTodo() throws CrediYaException {
        var empleados = empleadoService.listar();
        var clientes = clienteService.listar();
        var prestamos = prestamoService.listar();
        var pagos = pagoService.listar();

        archivoService.guardarEmpleados(empleados);
        archivoService.guardarClientes(clientes);
        archivoService.guardarPrestamos(prestamos);
        archivoService.guardarPagos(pagos);

        return "Archivos generados en: " + archivoService.getRutaAbsoluta()
                + "\n  empleados.txt (" + empleados.size() + "), clientes.txt (" + clientes.size()
                + "), prestamos.txt (" + prestamos.size() + "), pagos.txt (" + pagos.size() + ")";
    }

    public String importarEmpleadosYClientes() throws CrediYaException {
        return registrarEmpleadosYClientes(archivoService.leerEmpleados(), archivoService.leerClientes());
    }

    public String cargarDatosDePrueba() throws CrediYaException {
        List<Empleado> empleados = List.of(
                new Empleado(0, "Juan Pérez", "123456789", "juan@crediya.com", "Asesor", 2500000),
                new Empleado(0, "María López", "987654321", "maria@crediya.com", "Gestora", 2800000));
        List<Cliente> clientes = List.of(
                new Cliente(0, "Carlos Gómez", "1098765432", "carlos@gmail.com", "3001234567"),
                new Cliente(0, "Ana Rodríguez", "1122334455", "ana@gmail.com", "3019876543"));
        return registrarEmpleadosYClientes(empleados, clientes);
    }

    /** Registra cada elemento; si el documento ya existe o es inválido, lo omite y sigue con el siguiente. */
    private String registrarEmpleadosYClientes(List<Empleado> empleados, List<Cliente> clientes)
            throws CrediYaException {
        int empleadosNuevos = 0;
        int clientesNuevos = 0;
        int omitidos = 0;

        for (Empleado e : empleados) {
            try {
                empleadoService.registrar(e.getNombre(), e.getDocumento(), e.getRol(), e.getCorreo(), e.getSalario());
                empleadosNuevos++;
            } catch (DatosInvalidosException ex) {
                omitidos++;
            }
        }
        for (Cliente c : clientes) {
            try {
                clienteService.registrar(c.getNombre(), c.getDocumento(), c.getCorreo(), c.getTelefono());
                clientesNuevos++;
            } catch (DatosInvalidosException ex) {
                omitidos++;
            }
        }
        return "Empleados nuevos: " + empleadosNuevos + " | Clientes nuevos: " + clientesNuevos
                + " | Omitidos (ya existían o eran inválidos): " + omitidos;
    }
}
```

## `src/main/java/com/crediya/vista/MenuConsola.java`

```java
package com.crediya.vista;

import com.crediya.excepciones.CrediYaException;
import com.crediya.modelo.Cliente;
import com.crediya.modelo.Empleado;
import com.crediya.modelo.EstadoPrestamo;
import com.crediya.modelo.Pago;
import com.crediya.modelo.Prestamo;
import com.crediya.persistencia.ArchivoService;
import com.crediya.persistencia.ConexionBD;
import com.crediya.servicio.ClienteService;
import com.crediya.servicio.EmpleadoService;
import com.crediya.servicio.PagoService;
import com.crediya.servicio.PersistenciaService;
import com.crediya.servicio.PrestamoService;
import com.crediya.servicio.ReporteService;
import com.crediya.util.Formato;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Capa de VISTA: solo pide datos, llama a los servicios e imprime resultados.
 * No contiene reglas de negocio ni SQL.
 */
public class MenuConsola {

    /** Acción de menú que puede lanzar errores controlados del sistema. */
    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws CrediYaException;
    }

    private final Scanner scanner = new Scanner(System.in);
    private final EmpleadoService empleadoService;
    private final ClienteService clienteService;
    private final PrestamoService prestamoService;
    private final PagoService pagoService;
    private final ReporteService reporteService;
    private final PersistenciaService persistenciaService;
    private final ArchivoService archivoService;

    public MenuConsola(EmpleadoService empleadoService, ClienteService clienteService,
                       PrestamoService prestamoService, PagoService pagoService,
                       ReporteService reporteService, PersistenciaService persistenciaService,
                       ArchivoService archivoService) {
        this.empleadoService = empleadoService;
        this.clienteService = clienteService;
        this.prestamoService = prestamoService;
        this.pagoService = pagoService;
        this.reporteService = reporteService;
        this.persistenciaService = persistenciaService;
        this.archivoService = archivoService;
    }

    // =====================================================================
    //  MENÚ PRINCIPAL
    // =====================================================================

    public void iniciar() {
        verificarConexion();
        int opcion;
        try {
            do {
                System.out.println();
                System.out.println("========================================");
                System.out.println("       SISTEMA CREDIYA S.A.S.");
                System.out.println("       COBROS DE CARTERA");
                System.out.println("========================================");
                System.out.println();
                System.out.println("1. Gestión de empleados");
                System.out.println("2. Gestión de clientes");
                System.out.println("3. Gestión de préstamos");
                System.out.println("4. Gestión de pagos");
                System.out.println("5. Reportes");
                System.out.println("6. Persistencia");
                System.out.println("0. Salir");
                System.out.println();
                opcion = leerEntero("Seleccione una opción: ");

                switch (opcion) {
                    case 1 -> menuEmpleados();
                    case 2 -> menuClientes();
                    case 3 -> menuPrestamos();
                    case 4 -> menuPagos();
                    case 5 -> menuReportes();
                    case 6 -> menuPersistencia();
                    case 0 -> System.out.println("\nGracias por usar CrediYa. ¡Hasta pronto!");
                    default -> System.out.println("Opción inválida. Intente de nuevo.");
                }
            } while (opcion != 0);
        } catch (NoSuchElementException e) {
            // Ocurre si se cierra la entrada de datos (Ctrl+D / Ctrl+Z).
            System.out.println("\nSe cerró la entrada de datos. Saliendo...");
        }
    }

    private void verificarConexion() {
        try {
            ConexionBD.getInstancia().probarConexion();
            System.out.println("[OK] Conexión a MySQL establecida.");
        } catch (CrediYaException e) {
            System.out.println("[ADVERTENCIA] " + e.getMessage());
            System.out.println("El menú se abrirá, pero las opciones que usan la base de datos fallarán hasta solucionarlo.");
        }
    }

    // =====================================================================
    //  EMPLEADOS
    // =====================================================================

    private void menuEmpleados() {
        int opcion;
        do {
            System.out.println();
            System.out.println("========= EMPLEADOS =========");
            System.out.println();
            System.out.println("1. Registrar empleado");
            System.out.println("2. Listar empleados");
            System.out.println("3. Buscar empleado");
            System.out.println("4. Actualizar empleado");
            System.out.println("5. Eliminar empleado");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opción: ");
            switch (opcion) {
                case 1 -> ejecutar(this::registrarEmpleado);
                case 2 -> ejecutar(() -> imprimirLista(empleadoService.listar(), "No hay empleados registrados."));
                case 3 -> ejecutar(this::buscarEmpleado);
                case 4 -> ejecutar(this::actualizarEmpleado);
                case 5 -> ejecutar(this::eliminarEmpleado);
                case 0 -> { }
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private void registrarEmpleado() throws CrediYaException {
        System.out.println("\n--- Registrar empleado ---");
        String nombre = leerTexto("Nombre: ");
        String documento = leerTexto("Documento: ");
        String rol = leerTexto("Rol: ");
        String correo = leerTexto("Correo: ");
        double salario = leerDecimal("Salario: ");
        Empleado empleado = empleadoService.registrar(nombre, documento, rol, correo, salario);
        System.out.println("\n[OK] Empleado registrado con ID " + empleado.getId());
    }

    private void buscarEmpleado() throws CrediYaException {
        System.out.println("1. Buscar por ID");
        System.out.println("2. Buscar por documento");
        int tipo = leerEntero("Seleccione: ");
        if (tipo == 1) {
            System.out.println(empleadoService.buscarPorId(leerEntero("ID: ")));
        } else if (tipo == 2) {
            System.out.println(empleadoService.buscarPorDocumento(leerTexto("Documento: ")));
        } else {
            System.out.println("Opción inválida.");
        }
    }

    private void actualizarEmpleado() throws CrediYaException {
        int id = leerEntero("ID del empleado a actualizar: ");
        Empleado actual = empleadoService.buscarPorId(id);
        System.out.println("Datos actuales: " + actual);
        System.out.println("(Presione Enter para dejar el valor actual)");
        String nombre = leerTextoOpcional("Nombre", actual.getNombre());
        String documento = leerTextoOpcional("Documento", actual.getDocumento());
        String rol = leerTextoOpcional("Rol", actual.getRol());
        String correo = leerTextoOpcional("Correo", actual.getCorreo());
        double salario = leerDecimalOpcional("Salario", actual.getSalario());
        Empleado nuevo = empleadoService.actualizar(id, nombre, documento, rol, correo, salario);
        System.out.println("\n[OK] Empleado actualizado: " + nuevo);
    }

    private void eliminarEmpleado() throws CrediYaException {
        int id = leerEntero("ID del empleado a eliminar: ");
        Empleado empleado = empleadoService.buscarPorId(id);
        if (confirmar("¿Eliminar a " + empleado.getNombre() + "? (S/N): ")) {
            empleadoService.eliminar(id);
            System.out.println("\n[OK] Empleado eliminado.");
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    // =====================================================================
    //  CLIENTES
    // =====================================================================

    private void menuClientes() {
        int opcion;
        do {
            System.out.println();
            System.out.println("========= CLIENTES =========");
            System.out.println();
            System.out.println("1. Registrar cliente");
            System.out.println("2. Listar clientes");
            System.out.println("3. Buscar cliente");
            System.out.println("4. Actualizar cliente");
            System.out.println("5. Eliminar cliente");
            System.out.println("6. Consultar préstamos del cliente");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opción: ");
            switch (opcion) {
                case 1 -> ejecutar(this::registrarCliente);
                case 2 -> ejecutar(() -> imprimirLista(clienteService.listar(), "No hay clientes registrados."));
                case 3 -> ejecutar(this::buscarCliente);
                case 4 -> ejecutar(this::actualizarCliente);
                case 5 -> ejecutar(this::eliminarCliente);
                case 6 -> ejecutar(this::consultarPrestamosDeCliente);
                case 0 -> { }
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private void registrarCliente() throws CrediYaException {
        System.out.println("\n--- Registrar cliente ---");
        String nombre = leerTexto("Nombre: ");
        String documento = leerTexto("Documento: ");
        String correo = leerTexto("Correo: ");
        String telefono = leerTexto("Teléfono: ");
        Cliente cliente = clienteService.registrar(nombre, documento, correo, telefono);
        System.out.println("\n[OK] Cliente registrado con ID " + cliente.getId());
    }

    private void buscarCliente() throws CrediYaException {
        System.out.println("1. Buscar por ID");
        System.out.println("2. Buscar por documento");
        int tipo = leerEntero("Seleccione: ");
        if (tipo == 1) {
            System.out.println(clienteService.buscarPorId(leerEntero("ID: ")));
        } else if (tipo == 2) {
            System.out.println(clienteService.buscarPorDocumento(leerTexto("Documento: ")));
        } else {
            System.out.println("Opción inválida.");
        }
    }

    private void actualizarCliente() throws CrediYaException {
        int id = leerEntero("ID del cliente a actualizar: ");
        Cliente actual = clienteService.buscarPorId(id);
        System.out.println("Datos actuales: " + actual);
        System.out.println("(Presione Enter para dejar el valor actual)");
        String nombre = leerTextoOpcional("Nombre", actual.getNombre());
        String documento = leerTextoOpcional("Documento", actual.getDocumento());
        String correo = leerTextoOpcional("Correo", actual.getCorreo());
        String telefono = leerTextoOpcional("Teléfono", actual.getTelefono());
        Cliente nuevo = clienteService.actualizar(id, nombre, documento, correo, telefono);
        System.out.println("\n[OK] Cliente actualizado: " + nuevo);
    }

    private void eliminarCliente() throws CrediYaException {
        int id = leerEntero("ID del cliente a eliminar: ");
        Cliente cliente = clienteService.buscarPorId(id);
        if (confirmar("¿Eliminar a " + cliente.getNombre() + "? (S/N): ")) {
            clienteService.eliminar(id);
            System.out.println("\n[OK] Cliente eliminado.");
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    private void consultarPrestamosDeCliente() throws CrediYaException {
        int id = leerEntero("ID del cliente: ");
        List<Prestamo> prestamos = clienteService.prestamosDelCliente(id);
        imprimirLista(prestamos, "El cliente no tiene préstamos.");
    }

    // =====================================================================
    //  PRÉSTAMOS
    // =====================================================================

    private void menuPrestamos() {
        int opcion;
        do {
            System.out.println();
            System.out.println("========= PRÉSTAMOS =========");
            System.out.println();
            System.out.println("1. Crear préstamo");
            System.out.println("2. Listar préstamos");
            System.out.println("3. Buscar préstamo");
            System.out.println("4. Consultar préstamos activos");
            System.out.println("5. Consultar préstamos pagados");
            System.out.println("6. Actualizar estado");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opción: ");
            switch (opcion) {
                case 1 -> ejecutar(this::crearPrestamo);
                case 2 -> ejecutar(() -> imprimirLista(prestamoService.listar(), "No hay préstamos registrados."));
                case 3 -> ejecutar(() -> System.out.println(prestamoService.buscarPorId(leerEntero("ID del préstamo: ")).resumen()));
                case 4 -> ejecutar(() -> imprimirLista(prestamoService.listarActivos(), "No hay préstamos activos."));
                case 5 -> ejecutar(() -> imprimirLista(prestamoService.listarPagados(), "No hay préstamos pagados."));
                case 6 -> ejecutar(this::actualizarEstadoPrestamo);
                case 0 -> { }
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private void crearPrestamo() throws CrediYaException {
        System.out.println("\n--- Crear préstamo ---");
        System.out.println("(Si no recuerda un ID, use primero 'Listar' en el menú de clientes o empleados)");
        int clienteId = leerEntero("ID del cliente: ");
        int empleadoId = leerEntero("ID del empleado responsable: ");
        double monto = leerDecimal("Monto a prestar: ");
        double interes = leerDecimal("Interés (%): ");
        int cuotas = leerEntero("Número de cuotas: ");
        LocalDate fecha = leerFecha("Fecha de inicio (aaaa-mm-dd, Enter = hoy): ");

        Prestamo prestamo = prestamoService.crear(clienteId, empleadoId, monto, interes, cuotas, fecha);
        System.out.println("\n[OK] Préstamo creado con ID " + prestamo.getId());
        System.out.println();
        System.out.println(prestamo.resumen());
    }

    private void actualizarEstadoPrestamo() throws CrediYaException {
        int id = leerEntero("ID del préstamo: ");
        Prestamo prestamo = prestamoService.buscarPorId(id);
        System.out.println("Estado actual: " + prestamo.getEstado() + " | Saldo: " + Formato.moneda(prestamo.getSaldoPendiente()));
        System.out.println("1. PENDIENTE");
        System.out.println("2. PAGADO");
        System.out.println("3. VENCIDO");
        int opcion = leerEntero("Nuevo estado: ");
        EstadoPrestamo nuevo;
        switch (opcion) {
            case 1 -> nuevo = EstadoPrestamo.PENDIENTE;
            case 2 -> nuevo = EstadoPrestamo.PAGADO;
            case 3 -> nuevo = EstadoPrestamo.VENCIDO;
            default -> {
                System.out.println("Opción inválida.");
                return;
            }
        }
        Prestamo actualizado = prestamoService.actualizarEstado(id, nuevo);
        System.out.println("\n[OK] Estado actualizado a " + actualizado.getEstado());
    }

    // =====================================================================
    //  PAGOS
    // =====================================================================

    private void menuPagos() {
        int opcion;
        do {
            System.out.println();
            System.out.println("========= PAGOS =========");
            System.out.println();
            System.out.println("1. Registrar pago");
            System.out.println("2. Consultar pagos");
            System.out.println("3. Consultar histórico de un préstamo");
            System.out.println("4. Consultar saldo pendiente");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opción: ");
            switch (opcion) {
                case 1 -> ejecutar(this::registrarPago);
                case 2 -> ejecutar(() -> imprimirLista(pagoService.listar(), "No hay pagos registrados."));
                case 3 -> ejecutar(this::consultarHistorial);
                case 4 -> ejecutar(this::consultarSaldo);
                case 0 -> { }
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private void registrarPago() throws CrediYaException {
        System.out.println("\n--- Registrar pago ---");
        int prestamoId = leerEntero("ID del préstamo: ");
        Prestamo antes = prestamoService.buscarPorId(prestamoId);
        double saldoAnterior = antes.getSaldoPendiente();
        System.out.println("Saldo pendiente: " + Formato.moneda(saldoAnterior)
                + " | Cuota mensual: " + Formato.moneda(antes.getCuotaMensual()));
        double monto = leerDecimal("Monto del pago: ");
        LocalDate fecha = leerFecha("Fecha del pago (aaaa-mm-dd, Enter = hoy): ");

        Prestamo despues = pagoService.registrar(prestamoId, monto, fecha);
        System.out.println();
        System.out.println("Saldo anterior: " + Formato.moneda(saldoAnterior));
        System.out.println("Pago: " + Formato.moneda(monto));
        System.out.println("Saldo actual: " + Formato.moneda(despues.getSaldoPendiente()));
        System.out.println("Estado: " + despues.getEstado());
        if (despues.getEstado() == EstadoPrestamo.PAGADO) {
            System.out.println("\n¡El préstamo quedó pagado en su totalidad!");
        }
    }

    private void consultarHistorial() throws CrediYaException {
        int prestamoId = leerEntero("ID del préstamo: ");
        List<Pago> pagos = pagoService.historial(prestamoId);
        imprimirLista(pagos, "Este préstamo aún no tiene pagos.");
        double total = pagos.stream().mapToDouble(Pago::getMonto).sum();
        System.out.println("Total pagado: " + Formato.moneda(total));
    }

    private void consultarSaldo() throws CrediYaException {
        int prestamoId = leerEntero("ID del préstamo: ");
        System.out.println("Saldo pendiente: " + Formato.moneda(pagoService.consultarSaldo(prestamoId)));
    }

    // =====================================================================
    //  REPORTES
    // =====================================================================

    private void menuReportes() {
        int opcion;
        do {
            System.out.println();
            System.out.println("========= REPORTES =========");
            System.out.println();
            System.out.println("1. Préstamos activos");
            System.out.println("2. Préstamos pagados");
            System.out.println("3. Clientes morosos");
            System.out.println("4. Total de cartera");
            System.out.println("5. Total de pagos");
            System.out.println("6. Préstamos por empleado");
            System.out.println("7. Préstamos por cliente");
            System.out.println("8. Filtros de préstamos");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opción: ");
            switch (opcion) {
                case 1 -> ejecutar(() -> imprimirLista(reporteService.prestamosActivos(), "No hay préstamos activos."));
                case 2 -> ejecutar(() -> imprimirLista(reporteService.prestamosPagados(), "No hay préstamos pagados."));
                case 3 -> ejecutar(this::reporteMorosos);
                case 4 -> ejecutar(() -> System.out.println("Total de cartera (saldo pendiente): "
                        + Formato.moneda(reporteService.totalCartera())));
                case 5 -> ejecutar(() -> System.out.println("Total de pagos recibidos: "
                        + Formato.moneda(reporteService.totalPagos())));
                case 6 -> ejecutar(this::reportePorEmpleado);
                case 7 -> ejecutar(this::reportePorCliente);
                case 8 -> menuFiltros();
                case 0 -> { }
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private void reporteMorosos() throws CrediYaException {
        List<Prestamo> enMora = reporteService.prestamosEnMora();
        if (enMora.isEmpty()) {
            System.out.println("No hay clientes morosos.");
            return;
        }
        System.out.println("Clientes morosos: " + reporteService.clientesMorosos().size()
                + " | Préstamos en mora: " + enMora.size());
        System.out.println();
        for (Prestamo p : enMora) {
            System.out.println(p.getCliente().getNombre() + " (Doc: " + p.getCliente().getDocumento() + ")"
                    + " -> Préstamo #" + p.getId()
                    + " | Cuotas vencidas: " + p.getCuotasVencidas() + " de " + p.getCuotas()
                    + " | Pagado: " + Formato.moneda(p.getTotalPagado())
                    + " | Debería llevar: " + Formato.moneda(p.getValorEsperadoPagado())
                    + " | Saldo: " + Formato.moneda(p.getSaldoPendiente())
                    + " | Estado: " + p.getEstado());
        }
    }

    private void reportePorEmpleado() throws CrediYaException {
        Map<String, Long> datos = reporteService.prestamosPorEmpleado();
        if (datos.isEmpty()) {
            System.out.println("No hay préstamos registrados.");
            return;
        }
        datos.forEach((empleado, cantidad) -> System.out.println(empleado + ": " + cantidad + " préstamo(s)"));
    }

    private void reportePorCliente() throws CrediYaException {
        Map<String, List<Prestamo>> datos = reporteService.prestamosPorCliente();
        if (datos.isEmpty()) {
            System.out.println("No hay préstamos registrados.");
            return;
        }
        datos.forEach((cliente, prestamos) -> {
            System.out.println(cliente + " - " + prestamos.size() + " préstamo(s):");
            prestamos.forEach(p -> System.out.println("   " + p));
        });
    }

    private void menuFiltros() {
        System.out.println();
        System.out.println("--- Filtros de préstamos ---");
        System.out.println("1. Por estado");
        System.out.println("2. Por cliente");
        System.out.println("3. Por empleado");
        System.out.println("4. Por rango de monto");
        System.out.println("5. Por saldo pendiente mayor a...");
        int opcion = leerEntero("Seleccione una opción: ");
        switch (opcion) {
            case 1 -> ejecutar(() -> {
                System.out.println("1. PENDIENTE  2. PAGADO  3. VENCIDO");
                int e = leerEntero("Estado: ");
                if (e < 1 || e > 3) {
                    System.out.println("Opción inválida.");
                    return;
                }
                imprimirLista(reporteService.filtrarPorEstado(EstadoPrestamo.values()[e - 1]), "Sin resultados.");
            });
            case 2 -> ejecutar(() -> imprimirLista(
                    reporteService.filtrarPorCliente(leerEntero("ID del cliente: ")), "Sin resultados."));
            case 3 -> ejecutar(() -> imprimirLista(
                    reporteService.filtrarPorEmpleado(leerEntero("ID del empleado: ")), "Sin resultados."));
            case 4 -> ejecutar(() -> {
                double min = leerDecimal("Monto mínimo: ");
                double max = leerDecimal("Monto máximo: ");
                imprimirLista(reporteService.filtrarPorRangoDeMonto(min, max), "Sin resultados.");
            });
            case 5 -> ejecutar(() -> imprimirLista(
                    reporteService.filtrarPorSaldoMayorA(leerDecimal("Saldo mayor a: ")), "Sin resultados."));
            default -> System.out.println("Opción inválida.");
        }
    }

    // =====================================================================
    //  PERSISTENCIA
    // =====================================================================

    private void menuPersistencia() {
        int opcion;
        do {
            System.out.println();
            System.out.println("========= PERSISTENCIA =========");
            System.out.println();
            System.out.println("1. Exportar todo a archivos .txt");
            System.out.println("2. Importar empleados y clientes desde archivos");
            System.out.println("3. Ver contenido de un archivo");
            System.out.println("4. Cargar empleados y clientes de prueba");
            System.out.println("5. Probar conexión a MySQL");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opción: ");
            switch (opcion) {
                case 1 -> ejecutar(() -> System.out.println("[OK] " + persistenciaService.exportarTodo()));
                case 2 -> ejecutar(() -> System.out.println("[OK] " + persistenciaService.importarEmpleadosYClientes()));
                case 3 -> ejecutar(this::verArchivo);
                case 4 -> ejecutar(() -> System.out.println("[OK] " + persistenciaService.cargarDatosDePrueba()));
                case 5 -> verificarConexion();
                case 0 -> { }
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private void verArchivo() throws CrediYaException {
        System.out.println("1. empleados.txt  2. clientes.txt  3. prestamos.txt  4. pagos.txt");
        int opcion = leerEntero("Archivo: ");
        String[] nombres = {"empleados.txt", "clientes.txt", "prestamos.txt", "pagos.txt"};
        if (opcion < 1 || opcion > nombres.length) {
            System.out.println("Opción inválida.");
            return;
        }
        System.out.println("--- " + nombres[opcion - 1] + " (carpeta: " + archivoService.getRutaAbsoluta() + ") ---");
        archivoService.leerLineas(nombres[opcion - 1]).forEach(System.out::println);
    }

    // =====================================================================
    //  UTILIDADES DE ENTRADA / SALIDA
    // =====================================================================

    /** Ejecuta una acción y muestra el mensaje si lanza un error controlado (el programa NO se cierra). */
    private void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (CrediYaException e) {
            System.out.println("\n[ERROR] " + e.getMessage());
        }
    }

    private <T> void imprimirLista(List<T> lista, String mensajeVacio) {
        if (lista.isEmpty()) {
            System.out.println(mensajeVacio);
        } else {
            lista.forEach(System.out::println);
        }
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    private String leerTextoOpcional(String campo, String valorActual) {
        System.out.print(campo + " [" + valorActual + "]: ");
        String texto = scanner.nextLine().trim();
        return texto.isEmpty() ? valorActual : texto;
    }

    private int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = scanner.nextLine().trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("  Debe ingresar un número entero válido.");
            }
        }
    }

    private double leerDecimal(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = scanner.nextLine().trim().replace(',', '.');
            try {
                double valor = Double.parseDouble(texto);
                if (Double.isFinite(valor)) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // cae al mensaje de abajo
            }
            System.out.println("  Debe ingresar un número válido (ejemplo: 1500000 o 7.5, sin puntos de miles).");
        }
    }

    private double leerDecimalOpcional(String campo, double valorActual) {
        while (true) {
            System.out.print(campo + " [" + Formato.numero(valorActual) + "]: ");
            String texto = scanner.nextLine().trim().replace(',', '.');
            if (texto.isEmpty()) {
                return valorActual;
            }
            try {
                double valor = Double.parseDouble(texto);
                if (Double.isFinite(valor)) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // cae al mensaje de abajo
            }
            System.out.println("  Debe ingresar un número válido.");
        }
    }

    private LocalDate leerFecha(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = scanner.nextLine().trim();
            if (texto.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(texto);
            } catch (DateTimeParseException e) {
                System.out.println("  Fecha inválida. Use el formato aaaa-mm-dd (ejemplo: 2026-10-02).");
            }
        }
    }

    private boolean confirmar(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim().equalsIgnoreCase("S");
    }
}
```

## `src/main/java/com/crediya/Main.java`

```java
package com.crediya;

import com.crediya.dao.ClienteDAO;
import com.crediya.dao.EmpleadoDAO;
import com.crediya.dao.PagoDAO;
import com.crediya.dao.PrestamoDAO;
import com.crediya.persistencia.ArchivoService;
import com.crediya.servicio.ClienteService;
import com.crediya.servicio.EmpleadoService;
import com.crediya.servicio.PagoService;
import com.crediya.servicio.PersistenciaService;
import com.crediya.servicio.PrestamoService;
import com.crediya.servicio.ReporteService;
import com.crediya.vista.MenuConsola;

/**
 * Punto de entrada. Aquí solo se "arma" el sistema (se crean los objetos y se conectan entre sí)
 * y se inicia el menú. NO hay lógica de negocio en Main.
 *
 * Inyección de dependencias manual: cada servicio recibe por el constructor los DAO que necesita.
 */
public class Main {

    public static void main(String[] args) {
        // Capa DAO
        EmpleadoDAO empleadoDAO = new EmpleadoDAO();
        ClienteDAO clienteDAO = new ClienteDAO();
        PrestamoDAO prestamoDAO = new PrestamoDAO();
        PagoDAO pagoDAO = new PagoDAO();

        // Capa de persistencia en archivos
        ArchivoService archivoService = new ArchivoService();

        // Capa de servicios
        EmpleadoService empleadoService = new EmpleadoService(empleadoDAO, prestamoDAO);
        ClienteService clienteService = new ClienteService(clienteDAO, prestamoDAO);
        PrestamoService prestamoService = new PrestamoService(prestamoDAO, clienteDAO, empleadoDAO);
        PagoService pagoService = new PagoService(pagoDAO, prestamoDAO);
        ReporteService reporteService = new ReporteService(prestamoDAO, pagoDAO);
        PersistenciaService persistenciaService = new PersistenciaService(
                archivoService, empleadoService, clienteService, prestamoService, pagoService);

        // Capa de vista
        MenuConsola menu = new MenuConsola(empleadoService, clienteService, prestamoService,
                pagoService, reporteService, persistenciaService, archivoService);
        menu.iniciar();
    }
}
```

## `datos/empleados.txt`

```text
# id;nombre;documento;rol;correo;salario
1;Juan Pérez;123456789;Asesor;juan@crediya.com;2500000.0
2;María López;987654321;Gestora;maria@crediya.com;2800000.0
```

## `datos/clientes.txt`

```text
# id;nombre;documento;correo;telefono
1;Carlos Gómez;1098765432;carlos@gmail.com;3001234567
2;Ana Rodríguez;1122334455;ana@gmail.com;3019876543
```

## `datos/prestamos.txt`

```text
# id;clienteId;empleadoId;monto;interes;cuotas;fechaInicio;estado;saldoPendiente
1;1;1;1000000.0;10.0;10;2026-06-01;PENDIENTE;880000.0
2;2;2;500000.0;10.0;5;2026-05-01;PAGADO;0.0
3;2;1;2000000.0;10.0;10;2026-09-15;PENDIENTE;2200000.0
```

## `datos/pagos.txt`

```text
# id;prestamoId;fechaPago;monto
3;2;2026-06-01;110000.0
1;1;2026-07-01;110000.0
4;2;2026-07-01;110000.0
2;1;2026-08-01;110000.0
5;2;2026-08-01;110000.0
6;2;2026-09-01;110000.0
7;2;2026-10-01;110000.0
```

