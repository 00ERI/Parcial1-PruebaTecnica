package com.instituto.matricula.interfaces.api;

import com.instituto.matricula.application.ConsultarOcupacionGrupoService;
import com.instituto.matricula.application.ConsultarOcupacionGrupoService.OcupacionGrupoDto;
import com.instituto.matricula.application.GestionarCursoService;
import com.instituto.matricula.domain.curso.Curso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos de consulta para Cursos y ocupación de Grupos.
 */
@RestController
@RequestMapping("/api/cursos")
@Tag(name = "Cursos", description = "Consulta de cursos y ocupación de grupos")
public class CursoController {

    private final GestionarCursoService gestionarCursoService;
    private final ConsultarOcupacionGrupoService consultarOcupacionGrupoService;

    public CursoController(GestionarCursoService gestionarCursoService,
                           ConsultarOcupacionGrupoService consultarOcupacionGrupoService) {
        this.gestionarCursoService = gestionarCursoService;
        this.consultarOcupacionGrupoService = consultarOcupacionGrupoService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los cursos")
    public ResponseEntity<List<CursoDto>> listarTodos() {
        List<CursoDto> cursos = gestionarCursoService.listarTodos().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un curso por ID")
    public ResponseEntity<CursoDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toDto(gestionarCursoService.buscarPorId(id)));
    }

    @GetMapping("/{id}/grupos/ocupacion")
    @Operation(summary = "Consultar ocupación (cupos y lista de espera) de los grupos de un curso — Consulta Obligatoria")
    public ResponseEntity<List<OcupacionGrupoDto>> consultarOcupacion(@PathVariable Long id) {
        return ResponseEntity.ok(consultarOcupacionGrupoService.consultarPorCurso(id));
    }

    private CursoDto toDto(Curso c) {
        return new CursoDto(c.getId(), c.getCodigo(), c.getNombre(), c.getCreditos(), c.getPorcentajeMinimoAprobacion());
    }

    public record CursoDto(Long id, String codigo, String nombre, Integer creditos, Double porcentajeMinimoAprobacion) {}
}
