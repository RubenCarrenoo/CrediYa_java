package com.crediya.servicio;

import com.crediya.dao.EmpleadoDAO;
import com.crediya.dao.PrestamoDAO;
import com.crediya.excepciones.CrediYaException;
import com.crediya.excepciones.DatosInvalidosException;
import com.crediya.excepciones.EmpleadoNoEncontradoException;
import com.crediya.excepciones.ErrorPersistenciaException;
import com.crediya.modelo.Empleado;
import com.crediya.util.Validaciones;
import java.util.List;

/** Lógica de negocio de empleados: valida, comprueba reglas y delega el guardado al DAO. */
public class EmpleadoService {

    private final EmpleadoDAO empleadoDAO;
    private final PrestamoDAO prestamoDAO;

    public EmpleadoService(EmpleadoDAO empleadoDAO, PrestamoDAO prestamoDAO) {
        this.empleadoDAO = empleadoDAO;
        this.prestamoDAO = prestamoDAO;
    }

    public Empleado registrar(String nombre, String documento, String rol, String correo, double salario)
            throws CrediYaException {
        validar(nombre, documento, rol, correo, salario);
        if (empleadoDAO.buscarPorDocumento(documento.trim()).isPresent()) {
            throw new DatosInvalidosException("Ya existe un empleado con el documento " + documento.trim() + ".");
        }
        Empleado empleado = new Empleado(0, nombre.trim(), documento.trim(), correo.trim(), rol.trim(), salario);
        empleadoDAO.guardar(empleado);
        return empleado;
    }

    public List<Empleado> listar() throws ErrorPersistenciaException {
        return empleadoDAO.listar();
    }

    public Empleado buscarPorId(int id) throws CrediYaException {
        return empleadoDAO.buscarPorId(id)
                .orElseThrow(() -> new EmpleadoNoEncontradoException("No existe un empleado con ID " + id + "."));
    }

    public Empleado buscarPorDocumento(String documento) throws CrediYaException {
        return empleadoDAO.buscarPorDocumento(documento.trim())
                .orElseThrow(() -> new EmpleadoNoEncontradoException(
                        "No existe un empleado con documento " + documento.trim() + "."));
    }

    public Empleado actualizar(int id, String nombre, String documento, String rol, String correo, double salario)
            throws CrediYaException {
        Empleado empleado = buscarPorId(id);
        validar(nombre, documento, rol, correo, salario);
        // Si cambió el documento, verificar que no lo tenga otro empleado.
        boolean documentoOcupado = empleadoDAO.buscarPorDocumento(documento.trim())
                .map(otro -> otro.getId() != id).orElse(false);
        if (documentoOcupado) {
            throw new DatosInvalidosException("Ya existe otro empleado con el documento " + documento.trim() + ".");
        }
        empleado.setNombre(nombre.trim());
        empleado.setDocumento(documento.trim());
        empleado.setRol(rol.trim());
        empleado.setCorreo(correo.trim());
        empleado.setSalario(salario);
        empleadoDAO.actualizar(empleado);
        return empleado;
    }

    /** Solo se puede eliminar un empleado que no haya gestionado préstamos. */
    public void eliminar(int id) throws CrediYaException {
        buscarPorId(id);
        if (prestamoDAO.contarPorEmpleado(id) > 0) {
            throw new DatosInvalidosException("No se puede eliminar: el empleado tiene préstamos asociados.");
        }
        empleadoDAO.eliminar(id);
    }

    private void validar(String nombre, String documento, String rol, String correo, double salario)
            throws DatosInvalidosException {
        Validaciones.validarTextoObligatorio(nombre, "nombre");
        Validaciones.validarDocumento(documento);
        Validaciones.validarTextoObligatorio(rol, "rol");
        Validaciones.validarCorreo(correo);
        Validaciones.validarMontoPositivo(salario, "salario");
    }
}
