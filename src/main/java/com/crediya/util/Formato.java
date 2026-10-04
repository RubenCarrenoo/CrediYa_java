package com.crediya.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Utilidad para mostrar valores con formato colombiano: $1.100.000 */
public final class Formato {

    private static final DecimalFormat FORMATO_MONEDA;

    static {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.forLanguageTag("es-CO"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        FORMATO_MONEDA = new DecimalFormat("#,##0.##", simbolos);
    }

    private Formato() {
        // Clase de utilidades: no se instancia.
    }

    /** 1100000 -> "$1.100.000" */
    public static String moneda(double valor) {
        return "$" + FORMATO_MONEDA.format(valor);
    }

    /** 10.0 -> "10" ; 7.5 -> "7.5" (para mostrar el porcentaje de interés) */
    public static String numero(double valor) {
        return BigDecimal.valueOf(valor).stripTrailingZeros().toPlainString();
    }
}
