package com.crediya.excepciones;

/** Se lanza cuando un pago no se puede aplicar (supera el saldo, préstamo ya pagado, fecha incorrecta...). */
public class PagoInvalidoException extends CrediYaException {

    public PagoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
