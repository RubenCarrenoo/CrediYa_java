package com.crediya.dao;

import com.crediya.excepciones.ErrorPersistenciaException;
import com.crediya.modelo.EstadoPrestamo;
import com.crediya.modelo.Pago;
import com.crediya.persistencia.ConexionBD;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** PATRÓN DAO para la tabla "pagos". Un pago no se edita ni se borra (por eso no implementa Modificable). */
public class PagoDAO implements Guardable<Pago>, Consultable<Pago> {

    private static final String SQL_INSERTAR = "INSERT INTO pagos (prestamo_id, fecha_pago, monto) VALUES (?, ?, ?)";
    private static final String SQL_LISTAR = "SELECT id, prestamo_id, fecha_pago, monto FROM pagos ORDER BY fecha_pago, id";
    private static final String SQL_POR_ID = "SELECT id, prestamo_id, fecha_pago, monto FROM pagos WHERE id = ?";
    private static final String SQL_POR_PRESTAMO =
            "SELECT id, prestamo_id, fecha_pago, monto FROM pagos WHERE prestamo_id = ? ORDER BY fecha_pago, id";

    @Override
    public void guardar(Pago pago) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion()) {
            insertar(con, pago);
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al guardar el pago: " + ex.getMessage(), ex);
        }
    }

    /**
     * Guarda el pago Y actualiza el saldo/estado del préstamo en UNA SOLA TRANSACCIÓN:
     * o se hacen las dos cosas, o no se hace ninguna (rollback). Así nunca queda un pago
     * registrado con el saldo sin actualizar.
     */
    public void registrarPagoYActualizarPrestamo(Pago pago, double nuevoSaldo, EstadoPrestamo nuevoEstado)
            throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion()) {
            try {
                con.setAutoCommit(false);              // inicia la transacción
                insertar(con, pago);
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE prestamos SET saldo_pendiente = ?, estado = ? WHERE id = ?")) {
                    ps.setDouble(1, nuevoSaldo);
                    ps.setString(2, nuevoEstado.name());
                    ps.setInt(3, pago.getPrestamoId());
                    ps.executeUpdate();
                }
                con.commit();                          // confirma las dos operaciones
            } catch (SQLException ex) {
                con.rollback();                        // deshace todo si algo falló
                throw ex;
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al registrar el pago: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Pago> listar() throws ErrorPersistenciaException {
        return consultar(SQL_LISTAR, 0, false);
    }

    @Override
    public Optional<Pago> buscarPorId(int id) throws ErrorPersistenciaException {
        List<Pago> resultado = consultar(SQL_POR_ID, id, true);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public List<Pago> listarPorPrestamo(int prestamoId) throws ErrorPersistenciaException {
        return consultar(SQL_POR_PRESTAMO, prestamoId, true);
    }

    // ------------------------------ privados ------------------------------

    private void insertar(Connection con, Pago pago) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pago.getPrestamoId());
            ps.setDate(2, Date.valueOf(pago.getFechaPago()));
            ps.setDouble(3, pago.getMonto());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    pago.setId(claves.getInt(1));
                }
            }
        }
    }

    private List<Pago> consultar(String sql, int parametro, boolean usaParametro) throws ErrorPersistenciaException {
        List<Pago> pagos = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (usaParametro) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pagos.add(new Pago(rs.getInt("id"), rs.getInt("prestamo_id"),
                            rs.getDate("fecha_pago").toLocalDate(), rs.getDouble("monto")));
                }
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al consultar pagos: " + ex.getMessage(), ex);
        }
        return pagos;
    }
}
