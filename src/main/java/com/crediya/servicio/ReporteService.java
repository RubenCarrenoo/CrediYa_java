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
