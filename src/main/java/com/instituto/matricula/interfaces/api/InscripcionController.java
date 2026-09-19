package com.instituto.matricula.interfaces.api;

import com.instituto.matricula.application.GestionarCursoService;
import com.instituto.matricula.application.GestionarGrupoService;
import com.instituto.matricula.application.InscribirEstudianteService;
import com.instituto.matricula.domain.grupo.Grupo;
import com.instituto.matricula.domain.horario.DiaSemana;
import com.instituto.matricula.domain.inscripcion.Inscripcion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de operaciones de estudiante: inscribir y cancelar.
 */
@RestController
@RequestMapping("/api/inscripciones")
@Tag(name = "Inscripciones", description = "Inscripción de estudiantes en grupos")
public class InscripcionController {

    private final InscribirEstudianteService inscribirEstudianteService;

    public InscripcionController(InscribirEstudianteService inscribirEstudianteService) {
        this.inscribirEstudianteService = inscribirEstudianteService;
    }

    @PostMapping
    @Operation(summary = "Inscribir un estudiante en un grupo")
    public ResponseEntity<InscripcionDto> inscribir(@RequestBody SolicitudInscripcionDto solicitud) {
        Inscripcion ins = inscribirEstudianteService.inscribir(solicitud.estudianteId(), solicitud.grupoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(ins));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar una inscripción activa")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        inscribirEstudianteService.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    private InscripcionDto toDto(Inscripcion i) {
        return new InscripcionDto(
            i.getId(),
            i.getEstudiante().getId(),
            i.getGrupo().getId(),
            i.getEstado().name(),
            i.getPosicionListaEspera(),
            i.getFechaSolicitud().toString()
        );
    }

    public record SolicitudInscripcionDto(Long estudianteId, Long grupoId) {}
    public record InscripcionDto(Long id, Long estudianteId, Long grupoId, String estado,
                                 Integer posicionListaEspera, String fechaSolicitud) {}
}
