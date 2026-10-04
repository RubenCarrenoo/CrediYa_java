package com.crediya;

import com.crediya.dao.ClienteDAO;
import com.crediya.dao.EmpleadoDAO;
import com.crediya.dao.PagoDAO;
import com.crediya.dao.PrestamoDAO;
import com.crediya.persistencia.ArchivoService;
import com.crediya.servicio.ClienteService;
import com.crediya.servicio.EmpleadoService;
import com.crediya.servicio.PagoService;
import com.crediya.servicio.PersistenciaService;
import com.crediya.servicio.PrestamoService;
import com.crediya.servicio.ReporteService;
import com.crediya.vista.MenuConsola;

/**
 * Punto de entrada. Aquí solo se "arma" el sistema (se crean los objetos y se conectan entre sí)
 * y se inicia el menú. NO hay lógica de negocio en Main.
 *
 * Inyección de dependencias manual: cada servicio recibe por el constructor los DAO que necesita.
 */
public class Main {

    public static void main(String[] args) {
        // Capa DAO
        EmpleadoDAO empleadoDAO = new EmpleadoDAO();
        ClienteDAO clienteDAO = new ClienteDAO();
        PrestamoDAO prestamoDAO = new PrestamoDAO();
        PagoDAO pagoDAO = new PagoDAO();

        // Capa de persistencia en archivos
        ArchivoService archivoService = new ArchivoService();

        // Capa de servicios
        EmpleadoService empleadoService = new EmpleadoService(empleadoDAO, prestamoDAO);
        ClienteService clienteService = new ClienteService(clienteDAO, prestamoDAO);
        PrestamoService prestamoService = new PrestamoService(prestamoDAO, clienteDAO, empleadoDAO);
        PagoService pagoService = new PagoService(pagoDAO, prestamoDAO);
        ReporteService reporteService = new ReporteService(prestamoDAO, pagoDAO);
        PersistenciaService persistenciaService = new PersistenciaService(
                archivoService, empleadoService, clienteService, prestamoService, pagoService);

        // Capa de vista
        MenuConsola menu = new MenuConsola(empleadoService, clienteService, prestamoService,
                pagoService, reporteService, persistenciaService, archivoService);
        menu.iniciar();
    }
}
