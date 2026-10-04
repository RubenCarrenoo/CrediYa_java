package com.crediya.excepciones;

/**
 * Envuelve los errores técnicos de la persistencia (SQLException, IOException).
 * Así las capas superiores no necesitan conocer JDBC ni java.io.
 */
public class ErrorPersistenciaException extends CrediYaException {

    public ErrorPersistenciaException(String mensaje) {
        super(mensaje);
    }

    public ErrorPersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
