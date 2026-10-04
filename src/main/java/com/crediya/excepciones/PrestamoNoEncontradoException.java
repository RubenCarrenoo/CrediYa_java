package com.crediya.excepciones;

/** Se lanza cuando se busca un préstamo que no existe. */
public class PrestamoNoEncontradoException extends CrediYaException {

    public PrestamoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
