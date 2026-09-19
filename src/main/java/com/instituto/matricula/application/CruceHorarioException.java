package com.instituto.matricula.application;

/**
 * Excepción lanzada cuando hay cruce de horario con un grupo ya inscrito.
 */
public class CruceHorarioException extends RuntimeException {
    public CruceHorarioException(String mensaje) {
        super(mensaje);
    }
}
