package com.instituto.matricula.application;

import com.instituto.matricula.domain.grupo.Grupo;
import com.instituto.matricula.infrastructure.repositories.GrupoRepository;
import com.instituto.matricula.infrastructure.repositories.InscripcionRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Servicio de consulta obligatoria: ocupación por grupo.
 * Expone cupos disponibles y tamaño de lista de espera por grupo.
 */
@Service
public class ConsultarOcupacionGrupoService {

    private final GrupoRepository grupoRepository;
    private final InscripcionRepository inscripcionRepository;

    public ConsultarOcupacionGrupoService(
            GrupoRepository grupoRepository,
            InscripcionRepository inscripcionRepository) {
        this.grupoRepository = grupoRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    public OcupacionGrupoDto consultarPorGrupo(Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Grupo no encontrado: " + grupoId));
        return construirDto(grupo);
    }

    public List<OcupacionGrupoDto> consultarPorCurso(Long cursoId) {
        return grupoRepository.findByCursoId(cursoId).stream()
                .map(this::construirDto)
                .toList();
    }

    private OcupacionGrupoDto construirDto(Grupo grupo) {
        long inscritos = inscripcionRepository
                .findByGrupoIdAndEstado(grupo.getId(), com.instituto.matricula.domain.inscripcion.EstadoInscripcion.INSCRITO)
                .size();
        long listaEspera = inscripcionRepository
                .findByGrupoIdAndEstado(grupo.getId(), com.instituto.matricula.domain.inscripcion.EstadoInscripcion.LISTA_ESPERA)
                .size();
        int disponibles = Math.max(0, grupo.getCupoMaximo() - (int) inscritos);

        return new OcupacionGrupoDto(
                grupo.getId(),
                grupo.getNumeroGrupo(),
                grupo.getCurso().getNombre(),
                grupo.getCupoMaximo(),
                (int) inscritos,
                disponibles,
                (int) listaEspera
        );
    }

    /**
     * DTO de respuesta de la consulta obligatoria.
     */
    public record OcupacionGrupoDto(
            Long grupoId,
            Integer numeroGrupo,
            String nombreCurso,
            Integer cupoMaximo,
            Integer inscritos,
            Integer cuposDisponibles,
            Integer listaEspera
    ) {}
}
