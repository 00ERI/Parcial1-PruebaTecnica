package com.instituto.matricula.application;

import com.instituto.matricula.domain.curso.Curso;
import com.instituto.matricula.domain.prerrequisito.Prerrequisito;
import com.instituto.matricula.domain.prerrequisito.TipoPrerrequisito;
import com.instituto.matricula.infrastructure.repositories.CursoRepository;
import com.instituto.matricula.infrastructure.repositories.PrerrequisitoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de gestión de Cursos: CRUD con validación de prerrequisito obligatorio al guardar.
 * Regla (BACKLOG §Fase 2): no se puede guardar un Curso sin al menos un Prerrequisito.
 */
@Service
public class GestionarCursoService {

    private final CursoRepository cursoRepository;
    private final PrerrequisitoRepository prerrequisitoRepository;

    public GestionarCursoService(CursoRepository cursoRepository, PrerrequisitoRepository prerrequisitoRepository) {
        this.cursoRepository = cursoRepository;
        this.prerrequisitoRepository = prerrequisitoRepository;
    }

    public List<Curso> listarTodos() {
        return cursoRepository.findAll();
    }

    public Curso buscarPorId(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado: " + id));
    }

    /**
     * Crea un curso con prerrequisito MATRICULA_INSTITUCIONAL por defecto (cursos de primer nivel).
     * Regla AGENTS.md §3.1: no se puede guardar un curso sin al menos un Prerrequisito.
     */
    @Transactional
    public Curso crearConPrerrequisitoInstitucional(String codigo, String nombre, Integer creditos, Double porcentaje) {
        Curso curso = new Curso(codigo, nombre, creditos, porcentaje);
        curso = cursoRepository.save(curso);
        Prerrequisito prerreq = Prerrequisito.paraMatriculaInstitucional(curso);
        prerrequisitoRepository.save(prerreq);
        return curso;
    }

    /**
     * Crea un curso con prerrequisito de CURSO_APROBADO.
     */
    @Transactional
    public Curso crearConPrerrequisitoCurso(String codigo, String nombre, Integer creditos, Double porcentaje, Long cursoRequeridoId) {
        Curso cursoRequerido = buscarPorId(cursoRequeridoId);
        Curso curso = new Curso(codigo, nombre, creditos, porcentaje);
        curso = cursoRepository.save(curso);
        Prerrequisito prerreq = Prerrequisito.paraCursoAprobado(curso, cursoRequerido);
        prerrequisitoRepository.save(prerreq);
        return curso;
    }

    @Transactional
    public Curso actualizarPorcentaje(Long id, Double nuevoPorcentaje) {
        Curso curso = buscarPorId(id);
        curso.setPorcentajeMinimoAprobacion(nuevoPorcentaje);
        return cursoRepository.save(curso);
    }

    @Transactional
    public void agrgarPrerrequisitoCurso(Long cursoId, Long cursoRequeridoId) {
        Curso curso = buscarPorId(cursoId);
        Curso cursoRequerido = buscarPorId(cursoRequeridoId);
        Prerrequisito prerreq = Prerrequisito.paraCursoAprobado(curso, cursoRequerido);
        prerrequisitoRepository.save(prerreq);
    }

    @Transactional
    public void eliminarPrerrequisito(Long prerrequisitoId, Long cursoPrincipalId) {
        List<Prerrequisito> restantes = prerrequisitoRepository.findByCursoPrincipalId(cursoPrincipalId);
        if (restantes.size() <= 1) {
            throw new IllegalStateException("No se puede eliminar el único prerrequisito de un curso");
        }
        prerrequisitoRepository.deleteById(prerrequisitoId);
    }

    @Transactional
    public void eliminar(Long id) {
        buscarPorId(id);
        cursoRepository.deleteById(id);
    }
}
