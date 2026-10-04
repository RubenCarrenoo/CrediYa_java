package com.crediya.excepciones;

/**
 * Excepción base del sistema. Todas las excepciones personalizadas heredan de ella,
 * así la capa de vista puede capturar una sola clase (CrediYaException) y mostrar el mensaje.
 */
public class CrediYaException extends Exception {

    public CrediYaException(String mensaje) {
        super(mensaje);
    }

    public CrediYaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
