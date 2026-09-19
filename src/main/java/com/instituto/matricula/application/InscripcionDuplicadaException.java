package com.instituto.matricula.application;

/**
 * Excepción lanzada cuando el estudiante ya tiene una inscripción activa en el mismo curso.
 */
public class InscripcionDuplicadaException extends RuntimeException {
    public InscripcionDuplicadaException(String mensaje) {
        super(mensaje);
    }
}
