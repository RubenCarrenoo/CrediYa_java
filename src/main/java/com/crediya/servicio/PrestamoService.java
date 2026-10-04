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
