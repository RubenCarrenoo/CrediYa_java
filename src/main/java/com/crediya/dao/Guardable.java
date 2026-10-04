package com.crediya.dao;

import com.crediya.excepciones.ErrorPersistenciaException;

/** Interfaz pequeña (ISP): solo sabe guardar. */
public interface Guardable<T> {
    void guardar(T entidad) throws ErrorPersistenciaException;
}
