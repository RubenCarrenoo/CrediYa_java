package com.crediya.dao;

import com.crediya.excepciones.ErrorPersistenciaException;
import com.crediya.modelo.Cliente;
import com.crediya.persistencia.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** PATRÓN DAO para la tabla "clientes". */
public class ClienteDAO implements Guardable<Cliente>, Consultable<Cliente>, Modificable<Cliente> {

    private static final String SQL_INSERTAR =
            "INSERT INTO clientes (nombre, documento, correo, telefono) VALUES (?, ?, ?, ?)";
    private static final String SQL_LISTAR =
            "SELECT id, nombre, documento, correo, telefono FROM clientes ORDER BY id";
    private static final String SQL_POR_ID =
            "SELECT id, nombre, documento, correo, telefono FROM clientes WHERE id = ?";
    private static final String SQL_POR_DOCUMENTO =
            "SELECT id, nombre, documento, correo, telefono FROM clientes WHERE documento = ?";
    private static final String SQL_ACTUALIZAR =
            "UPDATE clientes SET nombre = ?, documento = ?, correo = ?, telefono = ? WHERE id = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM clientes WHERE id = ?";

    @Override
    public void guardar(Cliente c) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDocumento());
            ps.setString(3, c.getCorreo());
            ps.setString(4, c.getTelefono());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    c.setId(claves.getInt(1));
                }
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al guardar el cliente: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Cliente> listar() throws ErrorPersistenciaException {
        List<Cliente> clientes = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(mapear(rs));
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al listar clientes: " + ex.getMessage(), ex);
        }
        return clientes;
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_POR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al buscar el cliente: " + ex.getMessage(), ex);
        }
    }

    public Optional<Cliente> buscarPorDocumento(String documento) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_POR_DOCUMENTO)) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al buscar el cliente: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void actualizar(Cliente c) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDocumento());
            ps.setString(3, c.getCorreo());
            ps.setString(4, c.getTelefono());
            ps.setInt(5, c.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al actualizar el cliente: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void eliminar(int id) throws ErrorPersistenciaException {
        try (Connection con = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new ErrorPersistenciaException("Error al eliminar el cliente: " + ex.getMessage(), ex);
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(rs.getInt("id"), rs.getString("nombre"), rs.getString("documento"),
                rs.getString("correo"), rs.getString("telefono"));
    }
}
