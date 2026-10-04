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
