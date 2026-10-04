package com.crediya.excepciones;

/** Se lanza cuando se busca un cliente que no existe. */
public class ClienteNoEncontradoException extends CrediYaException {

    public ClienteNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
