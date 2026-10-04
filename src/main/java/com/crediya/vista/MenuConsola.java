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
