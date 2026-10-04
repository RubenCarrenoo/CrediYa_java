package com.crediya.modelo;

/**
 * Clase base (abstracta) de las personas del sistema.
 *
 * HERENCIA: Cliente y Empleado heredan de aquí id, nombre, documento y correo.
 * ENCAPSULAMIENTO: los atributos son privados; se accede con getters y setters.
 */
public abstract class Persona {

    private int id;
    private String nombre;
    private String documento;
    private String correo;

    protected Persona(int id, String nombre, String documento, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.documento = documento;
        this.correo = correo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /** POLIMORFISMO: cada clase hija responde de forma distinta. */
    public abstract String getTipo();

    @Override
    public String toString() {
        return getTipo() + " #" + id + " | " + nombre + " | Doc: " + documento + " | " + correo;
    }
}
