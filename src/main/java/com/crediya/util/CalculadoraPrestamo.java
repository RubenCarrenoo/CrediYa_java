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
