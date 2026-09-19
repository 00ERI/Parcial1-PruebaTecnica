package com.instituto.matricula.application;

import com.instituto.matricula.domain.estudiante.Estudiante;
import com.instituto.matricula.domain.grupo.Grupo;
import com.instituto.matricula.domain.horario.Horario;
import com.instituto.matricula.domain.inscripcion.EstadoInscripcion;
import com.instituto.matricula.domain.inscripcion.Inscripcion;
import com.instituto.matricula.infrastructure.repositories.InscripcionRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Servicio que valida si agregar el estudiante a un grupo generaría cruce de horario
 * con alguno de sus grupos actualmente activos (INSCRITO o LISTA_ESPERA con estado INSCRITO).
 * <p>
 * Regla (AGENTS.md §3): Un estudiante no puede inscribir dos grupos cuyos horarios se cruzan.
 * La comparación es sesión por sesión (no bloque completo).
 */
@Service
public class ValidarCruceHorarioService {

    private final InscripcionRepository inscripcionRepository;

    public ValidarCruceHorarioService(InscripcionRepository inscripcionRepository) {
        this.inscripcionRepository = inscripcionRepository;
    }

    /**
     * Lanza {@link CruceHorarioException} si alguna sesión del grupo candidato se cruza
     * con alguna sesión de los grupos donde el estudiante ya está INSCRITO.
     */
    public void validar(Estudiante estudiante, Grupo grupoCandidato) {
        List<Horario> horariosNuevos = grupoCandidato.getHorarios();

        List<Inscripcion> inscripciones = inscripcionRepository.findByEstudianteId(estudiante.getId());

        for (Inscripcion ins : inscripciones) {
            if (ins.getEstado() != EstadoInscripcion.INSCRITO) continue;

            Grupo grupoActivo = ins.getGrupo();
            for (Horario hActivo : grupoActivo.getHorarios()) {
                for (Horario hNuevo : horariosNuevos) {
                    if (hActivo.seCruzaCon(hNuevo)) {
                        throw new CruceHorarioException(
                            String.format(
                                "Cruce de horario: el grupo %d del curso '%s' se cruza con el grupo %d del curso '%s' (día %s, %s-%s vs %s-%s)",
                                grupoActivo.getNumeroGrupo(),
                                grupoActivo.getCurso().getNombre(),
                                grupoCandidato.getNumeroGrupo(),
                                grupoCandidato.getCurso().getNombre(),
                                hActivo.getDiaSemana(),
                                hActivo.getHoraInicio(), hActivo.getHoraFin(),
                                hNuevo.getHoraInicio(), hNuevo.getHoraFin()
                            )
                        );
                    }
                }
            }
        }
    }
}
