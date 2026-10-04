package com.crediya.dao;

import com.crediya.excepciones.ErrorPersistenciaException;
import com.crediya.modelo.Cliente;
import com.crediya.modelo.Empleado;
import com.crediya.modelo.EstadoPrestamo;
import com.crediya.modelo.Prestamo;
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

/**
 * PATRÓN DAO para la tabla "prestamos".
 * Al leer, hace JOIN con clientes y empleados para devolver el Prestamo con sus objetos completos.
 * No implementa Modificable: un préstamo no se edita ni se borra, solo cambia de estado/saldo (ISP).
 */
public class PrestamoDAO implements Guardable<Prestamo>, Consultable<Prestamo> {

    private static final String SQL_BASE =
            "SELECT p.id, p.monto, p.interes, p.cuotas, p.fecha_inicio, p.estado, p.saldo_pendiente, "
            + "c.id AS cliente_id, c.nombre AS cliente_nombre, c.documento AS cliente_documento, "
            + "c.correo AS cliente_correo, c.telefono AS cliente_telefono, "
            + "e.id AS empleado_id, e.nombre AS empleado_nombre, e.documento AS empleado_documento, "
            + "e.rol AS empleado_rol, e.correo AS empleado_correo, e.salario AS empleado_salario "
            + "FROM prestamos p "
            + "JOIN clientes c ON p.cliente_id = c.id "
            + "JOIN empleados e ON p.empleado_id = e.id ";

    private static final String SQL_INSERTAR =
            "INSERT INTO prestamos (cliente_id, empleado_id, monto, interes, cuotas, fecha_inicio, estado, saldo_pendiente) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    @Override
    public void guardar(Prestamo p) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getCliente().getId());
            ps.setInt(2, p.getEmpleado().getId());
            ps.setDouble(3, p.getMonto());
            ps.setDouble(4, p.getInteres());
            ps.setInt(5, p.getCuotas());
            ps.setDate(6, Date.valueOf(p.getFechaInicio()));
            ps.setString(7, p.getEstado().name());
            ps.setDouble(8, p.getSaldoPendiente());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    p.setId(claves.getInt(1));
                }
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al guardar el préstamo: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Prestamo> listar() throws ErrorPersistenciaException {
        return consultarLista(SQL_BASE + "ORDER BY p.id", 0, false);
    }

    @Override
    public Optional<Prestamo> buscarPorId(int id) throws ErrorPersistenciaException {
        List<Prestamo> resultado = consultarLista(SQL_BASE + "WHERE p.id = ?", id, true);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public List<Prestamo> listarPorCliente(int clienteId) throws ErrorPersistenciaException {
        return consultarLista(SQL_BASE + "WHERE p.cliente_id = ? ORDER BY p.id", clienteId, true);
    }

    public int contarPorCliente(int clienteId) throws ErrorPersistenciaException {
        return contar("SELECT COUNT(*) FROM prestamos WHERE cliente_id = ?", clienteId);
    }

    public int contarPorEmpleado(int empleadoId) throws ErrorPersistenciaException {
        return contar("SELECT COUNT(*) FROM prestamos WHERE empleado_id = ?", empleadoId);
    }

    public void actualizarEstado(int id, EstadoPrestamo estado) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement("UPDATE prestamos SET estado = ? WHERE id = ?")) {
            ps.setString(1, estado.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al actualizar el estado: " + ex.getMessage(), ex);
        }
    }

    // ------------------------------ privados ------------------------------

    private List<Prestamo> consultarLista(String sql, int parametro, boolean usaParametro)
            throws ErrorPersistenciaException {
        List<Prestamo> prestamos = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (usaParametro) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapear(rs));
                }
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al consultar préstamos: " + ex.getMessage(), ex);
        }
        return prestamos;
    }

    private int contar(String sql, int parametro) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al contar préstamos: " + ex.getMessage(), ex);
        }
    }

    private Prestamo mapear(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente(rs.getInt("cliente_id"), rs.getString("cliente_nombre"),
                rs.getString("cliente_documento"), rs.getString("cliente_correo"), rs.getString("cliente_telefono"));
        Empleado empleado = new Empleado(rs.getInt("empleado_id"), rs.getString("empleado_nombre"),
                rs.getString("empleado_documento"), rs.getString("empleado_correo"),
                rs.getString("empleado_rol"), rs.getDouble("empleado_salario"));
        return new Prestamo(rs.getInt("id"), cliente, empleado, rs.getDouble("monto"), rs.getDouble("interes"),
                rs.getInt("cuotas"), rs.getDate("fecha_inicio").toLocalDate(),
                EstadoPrestamo.valueOf(rs.getString("estado")), rs.getDouble("saldo_pendiente"));
    }
}
