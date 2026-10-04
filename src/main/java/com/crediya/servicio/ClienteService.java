package com.crediya.servicio;

import com.crediya.dao.ClienteDAO;
import com.crediya.dao.PrestamoDAO;
import com.crediya.excepciones.ClienteNoEncontradoException;
import com.crediya.excepciones.CrediYaException;
import com.crediya.excepciones.DatosInvalidosException;
import com.crediya.excepciones.ErrorPersistenciaException;
import com.crediya.modelo.Cliente;
import com.crediya.modelo.Prestamo;
import com.crediya.util.Validaciones;
import java.util.List;

/** Lógica de negocio de clientes. */
public class ClienteService {

    private final ClienteDAO clienteDAO;
    private final PrestamoDAO prestamoDAO;

    public ClienteService(ClienteDAO clienteDAO, PrestamoDAO prestamoDAO) {
        this.clienteDAO = clienteDAO;
        this.prestamoDAO = prestamoDAO;
    }

    public Cliente registrar(String nombre, String documento, String correo, String telefono)
            throws CrediYaException {
        validar(nombre, documento, correo, telefono);
        if (clienteDAO.buscarPorDocumento(documento.trim()).isPresent()) {
            throw new DatosInvalidosException("Ya existe un cliente con el documento " + documento.trim() + ".");
        }
        Cliente cliente = new Cliente(0, nombre.trim(), documento.trim(), correo.trim(), telefono.trim());
        clienteDAO.guardar(cliente);
        return cliente;
    }

    public List<Cliente> listar() throws ErrorPersistenciaException {
        return clienteDAO.listar();
    }

    public Cliente buscarPorId(int id) throws CrediYaException {
        return clienteDAO.buscarPorId(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("No existe un cliente con ID " + id + "."));
    }

    public Cliente buscarPorDocumento(String documento) throws CrediYaException {
        return clienteDAO.buscarPorDocumento(documento.trim())
                .orElseThrow(() -> new ClienteNoEncontradoException(
                        "No existe un cliente con documento " + documento.trim() + "."));
    }

    public Cliente actualizar(int id, String nombre, String documento, String correo, String telefono)
            throws CrediYaException {
        Cliente cliente = buscarPorId(id);
        validar(nombre, documento, correo, telefono);
        boolean documentoOcupado = clienteDAO.buscarPorDocumento(documento.trim())
                .map(otro -> otro.getId() != id).orElse(false);
        if (documentoOcupado) {
            throw new DatosInvalidosException("Ya existe otro cliente con el documento " + documento.trim() + ".");
        }
        cliente.setNombre(nombre.trim());
        cliente.setDocumento(documento.trim());
        cliente.setCorreo(correo.trim());
        cliente.setTelefono(telefono.trim());
        clienteDAO.actualizar(cliente);
        return cliente;
    }

    /** Solo se puede eliminar un cliente sin préstamos relacionados. */
    public void eliminar(int id) throws CrediYaException {
        buscarPorId(id);
        if (prestamoDAO.contarPorCliente(id) > 0) {
            throw new DatosInvalidosException("No se puede eliminar: el cliente tiene préstamos asociados.");
        }
        clienteDAO.eliminar(id);
    }

    public List<Prestamo> prestamosDelCliente(int clienteId) throws CrediYaException {
        buscarPorId(clienteId);   // lanza ClienteNoEncontradoException si no existe
        return prestamoDAO.listarPorCliente(clienteId);
    }

    private void validar(String nombre, String documento, String correo, String telefono)
            throws DatosInvalidosException {
        Validaciones.validarTextoObligatorio(nombre, "nombre");
        Validaciones.validarDocumento(documento);
        Validaciones.validarCorreo(correo);
        Validaciones.validarTelefono(telefono);
    }
}
