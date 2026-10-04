package com.crediya.excepciones;

/** Se lanza cuando se busca un empleado que no existe. */
public class EmpleadoNoEncontradoException extends CrediYaException {

    public EmpleadoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
