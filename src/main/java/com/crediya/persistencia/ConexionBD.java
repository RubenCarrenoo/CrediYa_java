package com.crediya.persistencia;

import com.crediya.excepciones.ErrorPersistenciaException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * PATRÓN SINGLETON: existe una única instancia de esta clase en todo el programa.
 * Guarda la configuración (host, puerto, usuario, clave) leída una sola vez de db.properties.
 *
 * Cada vez que un DAO necesita hablar con MySQL llama a getConexion(), que abre una
 * Connection nueva con DriverManager.getConnection(...). El DAO la cierra automáticamente
 * con try-with-resources. (Singleton = la configuración; la Connection NO se comparte.)
 */
public class ConexionBD {

    private static ConexionBD instancia;

    private final String url;
    private final String usuario;
    private final String clave;

    // Constructor privado: nadie puede hacer "new ConexionBD()" desde fuera.
    private ConexionBD() throws ErrorPersistenciaException {
        Properties props = new Properties();
        try (InputStream entrada = ConexionBD.class.getResourceAsStream("/db.properties")) {
            if (entrada == null) {
                throw new ErrorPersistenciaException(
                        "No se encontró el archivo db.properties (debe estar en src/main/resources).");
            }
            props.load(entrada);
        } catch (IOException e) {
            throw new ErrorPersistenciaException("No se pudo leer db.properties: " + e.getMessage(), e);
        }

        String host = props.getProperty("db.host", "localhost");
        String puerto = props.getProperty("db.port", "3306");
        String baseDatos = props.getProperty("db.name", "crediya_db");

        this.url = "jdbc:mysql://" + host + ":" + puerto + "/" + baseDatos
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota";
        this.usuario = props.getProperty("db.user", "root");
        this.clave = props.getProperty("db.password", "");
    }

    /** Punto de acceso a la única instancia. */
    public static synchronized ConexionBD getInstancia() throws ErrorPersistenciaException {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    /** Abre una conexión nueva. Quien la pida debe cerrarla (try-with-resources). */
    public Connection getConexion() throws ErrorPersistenciaException {
        try {
            return DriverManager.getConnection(url, usuario, clave);
        } catch (SQLException e) {
            throw new ErrorPersistenciaException(explicarError(e), e);
        }
    }

    /** Prueba rápida: abre y cierra una conexión. Lanza excepción si algo falla. */
    public void probarConexion() throws ErrorPersistenciaException {
        try (Connection con = getConexion()) {
            // Si llegamos aquí, la conexión funcionó.
        } catch (SQLException e) {
            throw new ErrorPersistenciaException(explicarError(e), e);
        }
    }

    /** Traduce los errores típicos de JDBC a mensajes que un estudiante entiende. */
    private String explicarError(SQLException e) {
        String detalle = " (detalle técnico: " + e.getMessage() + ")";
        if (e.getErrorCode() == 1045) {
            return "Usuario o contraseña de MySQL incorrectos. Revise db.properties." + detalle;
        }
        if (e.getErrorCode() == 1049) {
            return "La base de datos no existe. Ejecute primero sql/crediya_db.sql." + detalle;
        }
        String mensaje = String.valueOf(e.getMessage());
        if (mensaje.contains("No suitable driver")) {
            return "No se encontró el driver de MySQL. Revise la dependencia mysql-connector-j en el pom.xml." + detalle;
        }
        if (mensaje.contains("Communications link failure") || mensaje.contains("Connection refused")) {
            return "No se pudo conectar con MySQL. ¿Está encendido el servicio? Revise host y puerto en db.properties." + detalle;
        }
        return "Error de conexión con MySQL." + detalle;
    }
}
