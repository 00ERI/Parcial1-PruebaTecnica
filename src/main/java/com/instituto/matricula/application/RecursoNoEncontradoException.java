package com.instituto.matricula.application;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
