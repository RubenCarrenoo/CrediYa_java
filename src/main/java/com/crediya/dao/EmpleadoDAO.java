package com.crediya.dao;

import com.crediya.excepciones.ErrorPersistenciaException;
import com.crediya.modelo.Empleado;
import com.crediya.persistencia.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PATRÓN DAO (Data Access Object): toda la comunicación SQL con la tabla "empleados" vive aquí.
 * El resto del programa no sabe que existe MySQL.
 * Se usa PreparedStatement con "?" para evitar SQL Injection.
 */
public class EmpleadoDAO implements Guardable<Empleado>, Consultable<Empleado>, Modificable<Empleado> {

    private static final String SQL_INSERTAR =
            "INSERT INTO empleados (nombre, documento, rol, correo, salario) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_LISTAR =
            "SELECT id, nombre, documento, rol, correo, salario FROM empleados ORDER BY id";
    private static final String SQL_POR_ID =
            "SELECT id, nombre, documento, rol, correo, salario FROM empleados WHERE id = ?";
    private static final String SQL_POR_DOCUMENTO =
            "SELECT id, nombre, documento, rol, correo, salario FROM empleados WHERE documento = ?";
    private static final String SQL_ACTUALIZAR =
            "UPDATE empleados SET nombre = ?, documento = ?, rol = ?, correo = ?, salario = ? WHERE id = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM empleados WHERE id = ?";

    @Override
    public void guardar(Empleado e) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getDocumento());
            ps.setString(3, e.getRol());
            ps.setString(4, e.getCorreo());
            ps.setDouble(5, e.getSalario());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    e.setId(claves.getInt(1));   // MySQL nos devuelve el ID autogenerado
                }
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al guardar el empleado: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Empleado> listar() throws ErrorPersistenciaException {
        List<Empleado> empleados = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                empleados.add(mapear(rs));
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al listar empleados: " + ex.getMessage(), ex);
        }
        return empleados;
    }

    @Override
    public Optional<Empleado> buscarPorId(int id) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_POR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al buscar el empleado: " + ex.getMessage(), ex);
        }
    }

    public Optional<Empleado> buscarPorDocumento(String documento) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_POR_DOCUMENTO)) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al buscar el empleado: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void actualizar(Empleado e) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getDocumento());
            ps.setString(3, e.getRol());
            ps.setString(4, e.getCorreo());
            ps.setDouble(5, e.getSalario());
            ps.setInt(6, e.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al actualizar el empleado: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void eliminar(int id) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al eliminar el empleado: " + ex.getMessage(), ex);
        }
    }

    /** Convierte la fila actual del ResultSet en un objeto Empleado. */
    private Empleado mapear(ResultSet rs) throws SQLException {
        return new Empleado(rs.getInt("id"), rs.getString("nombre"), rs.getString("documento"),
                rs.getString("correo"), rs.getString("rol"), rs.getDouble("salario"));
    }
}
