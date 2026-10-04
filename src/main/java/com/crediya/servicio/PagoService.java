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
