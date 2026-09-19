package com.instituto.matricula.domain.prerrequisito;

/**
 * Define el tipo de prerrequisito requerido para inscribir un curso.
 * - CURSO_APROBADO: Exige haber aprobado un curso previo.
 * - MATRICULA_INSTITUCIONAL: Exige estar matriculado en la institución (para cursos de primer nivel).
 */
public enum TipoPrerrequisito {
    CURSO_APROBADO,
    MATRICULA_INSTITUCIONAL
}
