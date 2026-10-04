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
