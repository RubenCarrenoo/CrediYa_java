package com.crediya.modelo;

/** Cliente de CrediYa: una Persona con teléfono. */
public class Cliente extends Persona {

    private String telefono;

    public Cliente(int id, String nombre, String documento, String correo, String telefono) {
        super(id, nombre, documento, correo);
        this.telefono = telefono;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public String getTipo() {
        return "Cliente";
    }

    @Override
    public String toString() {
        return super.toString() + " | Tel: " + telefono;
    }
}
