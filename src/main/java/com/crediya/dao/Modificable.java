package com.crediya.dao;

import com.crediya.excepciones.ErrorPersistenciaException;

/** Interfaz pequeña (ISP): solo sabe actualizar y eliminar. Un DAO de pagos NO necesita implementarla. */
public interface Modificable<T> {
    void actualizar(T entidad) throws ErrorPersistenciaException;

    void eliminar(int id) throws ErrorPersistenciaException;
}
