package com.instituto.matricula.application;

import com.instituto.matricula.domain.curso.Curso;
import com.instituto.matricula.domain.estudiante.Estudiante;
import com.instituto.matricula.domain.prerrequisito.Prerrequisito;
import com.instituto.matricula.domain.prerrequisito.TipoPrerrequisito;
import com.instituto.matricula.infrastructure.repositories.HistorialAcademicoRepository;
import com.instituto.matricula.infrastructure.repositories.PrerrequisitoRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Servicio que valida si un estudiante cumple los prerrequisitos directos de un curso.
 * <p>
 * Implementado con prerrequisitos directos únicamente (ASSUMPTIONS.md §7, TODO transitivos).
 * MATRICULA_INSTITUCIONAL se valida como return true para cualquier estudiante registrado
 * (TODO: confirmar si requiere condiciones adicionales — ASSUMPTIONS.md §pendientes).
 */
@Service
public class ValidarPrerrequisitosService {

    private final PrerrequisitoRepository prerrequisitoRepository;
    private final HistorialAcademicoRepository historialAcademicoRepository;

    public ValidarPrerrequisitosService(
            PrerrequisitoRepository prerrequisitoRepository,
            HistorialAcademicoRepository historialAcademicoRepository) {
        this.prerrequisitoRepository = prerrequisitoRepository;
        this.historialAcademicoRepository = historialAcademicoRepository;
    }

    /**
     * Lanza {@link PrerrequisitosNoCumplidosException} si el estudiante no cumple
     * alguno de los prerrequisitos obligatorios del curso.
     */
    public void validar(Estudiante estudiante, Curso curso) {
        List<Prerrequisito> prerrequisitos = prerrequisitoRepository.findByCursoPrincipalId(curso.getId());

        for (Prerrequisito prereq : prerrequisitos) {
            if (!prereq.isObligatorio()) continue;

            if (prereq.getTipo() == TipoPrerrequisito.MATRICULA_INSTITUCIONAL) {
                // TODO: Confirmar si se requiere validar algo más allá de estar registrado
                // Por ahora: cualquier Estudiante registrado cumple este prerrequisito.
                if (!estudiante.isMatriculaActiva()) {
                    throw new PrerrequisitosNoCumplidosException(
                        "El estudiante no tiene matrícula institucional activa para cursar '" + curso.getNombre() + "'"
                    );
                }
            } else if (prereq.getTipo() == TipoPrerrequisito.CURSO_APROBADO) {
                Curso cursoRequerido = prereq.getCursoRequerido();
                boolean aprobado = historialAcademicoRepository
                        .findByEstudianteIdAndCursoId(estudiante.getId(), cursoRequerido.getId())
                        .map(h -> h.isAprobado())
                        .orElse(false);

                if (!aprobado) {
                    throw new PrerrequisitosNoCumplidosException(
                        String.format("El estudiante no ha aprobado el prerrequisito '%s' requerido para inscribir '%s'",
                            cursoRequerido.getNombre(), curso.getNombre())
                    );
                }
            }
        }
    }
}
