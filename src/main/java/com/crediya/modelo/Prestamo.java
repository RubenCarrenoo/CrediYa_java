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
