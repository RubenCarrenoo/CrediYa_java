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
