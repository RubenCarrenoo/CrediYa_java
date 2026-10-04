package com.crediya.modelo;

/** Estados posibles de un préstamo. */
public enum EstadoPrestamo {
    PENDIENTE,   // tiene saldo por pagar
    PAGADO,      // saldo = 0
    VENCIDO      // tiene saldo y fue marcado manualmente como vencido
}
