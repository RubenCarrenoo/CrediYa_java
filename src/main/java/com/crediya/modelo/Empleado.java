package com.crediya.modelo;

import com.crediya.util.Formato;

/** Empleado de CrediYa: una Persona con rol y salario. */
public class Empleado extends Persona {

    private String rol;
    private double salario;

    public Empleado(int id, String nombre, String documento, String correo, String rol, double salario) {
        super(id, nombre, documento, correo);
        this.rol = rol;
        this.salario = salario;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    @Override
    public String getTipo() {
        return "Empleado";
    }

    @Override
    public String toString() {
        return super.toString() + " | Rol: " + rol + " | Salario: " + Formato.moneda(salario);
    }
}
