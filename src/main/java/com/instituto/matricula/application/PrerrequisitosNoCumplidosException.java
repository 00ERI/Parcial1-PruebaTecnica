package com.instituto.matricula.application;

/**
 * Excepción lanzada cuando un estudiante no cumple los prerrequisitos para inscribir un curso.
 */
public class PrerrequisitosNoCumplidosException extends RuntimeException {
    public PrerrequisitosNoCumplidosException(String mensaje) {
        super(mensaje);
    }
}
