package com.instituto.matricula.interfaces.admin;

import com.instituto.matricula.application.GestionarCursoService;
import com.instituto.matricula.application.GestionarGrupoService;
import com.instituto.matricula.domain.curso.Curso;
import com.instituto.matricula.domain.grupo.Grupo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de administrador para CRUD de Cursos y Grupos.
 * NOTA: La autorización real queda pendiente de definición (BACKLOG §Fase 5).
 * Actualmente se protege por convención de ruta (/admin/) + header X-Rol-Usuario.
 */
@RestController
@RequestMapping("/admin")
@Tag(name = "Admin — Gestión", description = "Operaciones de administrador para Cursos y Grupos")
public class AdminCursoGrupoController {

    private final GestionarCursoService gestionarCursoService;
    private final GestionarGrupoService gestionarGrupoService;

    public AdminCursoGrupoController(GestionarCursoService gestionarCursoService,
                                     GestionarGrupoService gestionarGrupoService) {
        this.gestionarCursoService = gestionarCursoService;
        this.gestionarGrupoService = gestionarGrupoService;
    }

    // ─── Cursos ───────────────────────────────────────────────────────────────

    @PostMapping("/cursos/primer-nivel")
    @Operation(summary = "Crear curso de primer nivel (prerrequisito MATRICULA_INSTITUCIONAL automático)")
    public ResponseEntity<CursoRespDto> crearCursoPrimerNivel(@RequestBody CrearCursoDto req) {
        Curso c = gestionarCursoService.crearConPrerrequisitoInstitucional(
                req.codigo(), req.nombre(), req.creditos(), req.porcentaje());
        return ResponseEntity.status(HttpStatus.CREATED).body(toCursoDto(c));
    }

    @PostMapping("/cursos/con-prerrequisito")
    @Operation(summary = "Crear curso que requiere aprobación de otro curso previo")
    public ResponseEntity<CursoRespDto> crearCursoConPrerrequisito(@RequestBody CrearCursoConPrerrequisitoDto req) {
        Curso c = gestionarCursoService.crearConPrerrequisitoCurso(
                req.codigo(), req.nombre(), req.creditos(), req.porcentaje(), req.cursoRequeridoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toCursoDto(c));
    }

    @PutMapping("/cursos/{id}/porcentaje")
    @Operation(summary = "Actualizar el porcentaje mínimo de aprobación de un curso")
    public ResponseEntity<CursoRespDto> actualizarPorcentaje(
            @PathVariable Long id, @RequestBody ActualizarPorcentajeDto req) {
        Curso c = gestionarCursoService.actualizarPorcentaje(id, req.porcentaje());
        return ResponseEntity.ok(toCursoDto(c));
    }

    @PostMapping("/cursos/{id}/prerrequisitos")
    @Operation(summary = "Agregar un prerrequisito de curso aprobado a un curso existente")
    public ResponseEntity<Void> agregarPrerrequisito(
            @PathVariable Long id, @RequestBody AgregarPrerrequisitoDto req) {
        gestionarCursoService.agrgarPrerrequisitoCurso(id, req.cursoRequeridoId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/cursos/{cursoId}/prerrequisitos/{prerrequisitoId}")
    @Operation(summary = "Eliminar un prerrequisito (no el último)")
    public ResponseEntity<Void> eliminarPrerrequisito(
            @PathVariable Long cursoId, @PathVariable Long prerrequisitoId) {
        gestionarCursoService.eliminarPrerrequisito(prerrequisitoId, cursoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cursos/{id}")
    @Operation(summary = "Eliminar un curso")
    public ResponseEntity<Void> eliminarCurso(@PathVariable Long id) {
        gestionarCursoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Grupos ───────────────────────────────────────────────────────────────

    @PostMapping("/grupos")
    @Operation(summary = "Crear grupo con horario aleatorio por defecto (cupo default 30)")
    public ResponseEntity<GrupoRespDto> crearGrupo(@RequestBody CrearGrupoDto req) {
        Grupo g = gestionarGrupoService.crearConHorarioAleatorio(req.cursoId(), req.numeroGrupo(), req.cupoMaximo());
        return ResponseEntity.status(HttpStatus.CREATED).body(toGrupoDto(g));
    }

    @PutMapping("/grupos/{id}/cupo")
    @Operation(summary = "Actualizar el cupo máximo de un grupo")
    public ResponseEntity<GrupoRespDto> actualizarCupo(
            @PathVariable Long id, @RequestBody ActualizarCupoDto req) {
        Grupo g = gestionarGrupoService.actualizarCupo(id, req.cupoMaximo());
        return ResponseEntity.ok(toGrupoDto(g));
    }

    // ─── DTOs ─────────────────────────────────────────────────────────────────

    private CursoRespDto toCursoDto(Curso c) {
        return new CursoRespDto(c.getId(), c.getCodigo(), c.getNombre(), c.getCreditos(), c.getPorcentajeMinimoAprobacion());
    }

    private GrupoRespDto toGrupoDto(Grupo g) {
        return new GrupoRespDto(g.getId(), g.getNumeroGrupo(), g.getCupoMaximo(), g.getCurso().getId());
    }

    public record CrearCursoDto(String codigo, String nombre, Integer creditos, Double porcentaje) {}
    public record CrearCursoConPrerrequisitoDto(String codigo, String nombre, Integer creditos, Double porcentaje, Long cursoRequeridoId) {}
    public record ActualizarPorcentajeDto(Double porcentaje) {}
    public record AgregarPrerrequisitoDto(Long cursoRequeridoId) {}
    public record CrearGrupoDto(Long cursoId, Integer numeroGrupo, Integer cupoMaximo) {}
    public record ActualizarCupoDto(Integer cupoMaximo) {}
    public record CursoRespDto(Long id, String codigo, String nombre, Integer creditos, Double porcentajeMinimoAprobacion) {}
    public record GrupoRespDto(Long id, Integer numeroGrupo, Integer cupoMaximo, Long cursoId) {}
}
