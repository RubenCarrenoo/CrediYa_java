package com.crediya.util;

import com.crediya.excepciones.DatosInvalidosException;
import java.time.LocalDate;
import java.util.regex.Pattern;

/** Validaciones reutilizables. Todas lanzan DatosInvalidosException con un mensaje claro. */
public final class Validaciones {

    private static final Pattern CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern DOCUMENTO = Pattern.compile("^\\d{5,15}$");
    private static final Pattern TELEFONO = Pattern.compile("^\\d{7,15}$");

    private Validaciones() {
    }

    public static void validarTextoObligatorio(String valor, String campo) throws DatosInvalidosException {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException("El campo '" + campo + "' es obligatorio.");
        }
    }

    public static void validarDocumento(String documento) throws DatosInvalidosException {
        validarTextoObligatorio(documento, "documento");
        if (!DOCUMENTO.matcher(documento.trim()).matches()) {
            throw new DatosInvalidosException("El documento debe tener solo números (entre 5 y 15 dígitos).");
        }
    }

    public static void validarCorreo(String correo) throws DatosInvalidosException {
        validarTextoObligatorio(correo, "correo");
        if (!CORREO.matcher(correo.trim()).matches()) {
            throw new DatosInvalidosException("El correo '" + correo + "' no tiene un formato válido.");
        }
    }

    public static void validarTelefono(String telefono) throws DatosInvalidosException {
        validarTextoObligatorio(telefono, "teléfono");
        if (!TELEFONO.matcher(telefono.trim()).matches()) {
            throw new DatosInvalidosException("El teléfono debe tener solo números (entre 7 y 15 dígitos).");
        }
    }

    public static void validarMontoPositivo(double valor, String campo) throws DatosInvalidosException {
        if (Double.isNaN(valor) || Double.isInfinite(valor) || valor <= 0) {
            throw new DatosInvalidosException("El " + campo + " debe ser mayor a cero.");
        }
    }

    public static void validarInteres(double interes) throws DatosInvalidosException {
        if (Double.isNaN(interes) || interes < 0 || interes > 100) {
            throw new DatosInvalidosException("El interés debe estar entre 0 y 100 (%).");
        }
    }

    public static void validarCuotas(int cuotas) throws DatosInvalidosException {
        if (cuotas <= 0 || cuotas > 120) {
            throw new DatosInvalidosException("El número de cuotas debe estar entre 1 y 120.");
        }
    }

    public static void validarFechaPago(LocalDate fecha) throws DatosInvalidosException {
        if (fecha == null) {
            throw new DatosInvalidosException("La fecha del pago es obligatoria.");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new DatosInvalidosException("La fecha del pago no puede ser futura.");
        }
    }
}
