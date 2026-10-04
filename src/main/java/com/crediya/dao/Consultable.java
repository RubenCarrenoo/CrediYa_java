package com.crediya.dao;

import com.crediya.excepciones.ErrorPersistenciaException;
import java.util.List;
import java.util.Optional;

/** Interfaz pequeña (ISP): solo sabe consultar. */
public interface Consultable<T> {
    List<T> listar() throws ErrorPersistenciaException;

    Optional<T> buscarPorId(int id) throws ErrorPersistenciaException;
}
