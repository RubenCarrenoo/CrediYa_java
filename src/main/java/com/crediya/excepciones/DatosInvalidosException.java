package com.crediya.excepciones;

/** Se lanza cuando los datos ingresados no cumplen las validaciones (campo vacío, correo inválido, monto negativo...). */
public class DatosInvalidosException extends CrediYaException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
