package com.instituto.matricula.application;

import com.instituto.matricula.domain.curso.Curso;
import com.instituto.matricula.domain.grupo.Grupo;
import com.instituto.matricula.domain.horario.DiaSemana;
import com.instituto.matricula.domain.horario.Horario;
import com.instituto.matricula.infrastructure.repositories.CursoRepository;
import com.instituto.matricula.infrastructure.repositories.GrupoRepository;
import com.instituto.matricula.infrastructure.repositories.HorarioRepository;
import java.time.LocalTime;
import java.util.Random;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de gestión de Grupos y Horarios, incluyendo la generación aleatoria inicial.
 */
@Service
public class GestionarGrupoService {

    private final GrupoRepository grupoRepository;
    private final HorarioRepository horarioRepository;
    private final CursoRepository cursoRepository;
    private final Random random = new Random();

    public GestionarGrupoService(
            GrupoRepository grupoRepository,
            HorarioRepository horarioRepository,
            CursoRepository cursoRepository) {
        this.grupoRepository = grupoRepository;
        this.horarioRepository = horarioRepository;
        this.cursoRepository = cursoRepository;
    }

    /**
     * Crea un grupo para un curso, asignando un horario aleatorio por defecto.
     * Regla 3.1 (AGENTS.md): Grupo.horario se asigna aleatorio al crear; cupoMaximo default 30.
     */
    @Transactional
    public Grupo crearConHorarioAleatorio(Long cursoId, Integer numeroGrupo, Integer cupoMaximo) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado: " + cursoId));

        int cupo = (cupoMaximo != null && cupoMaximo > 0) ? cupoMaximo : 30;
        Grupo grupo = new Grupo(numeroGrupo, cupo, curso);
        grupo = grupoRepository.save(grupo);

        Horario horarioAleatorio = generarHorarioAleatorio(grupo);
        horarioRepository.save(horarioAleatorio);

        return grupo;
    }

    /**
     * Genera un horario aleatorio por defecto para un grupo.
     * Rango: lunes a sábado, de 6:00 a 20:00, bloques de 2 horas.
     */
    public Horario generarHorarioAleatorio(Grupo grupo) {
        DiaSemana[] diasLaborables = {
            DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES,
            DiaSemana.JUEVES, DiaSemana.VIERNES, DiaSemana.SABADO
        };
        DiaSemana dia = diasLaborables[random.nextInt(diasLaborables.length)];

        // Horas de inicio posibles: 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18
        int horaInicio = 6 + random.nextInt(13);
        LocalTime inicio = LocalTime.of(horaInicio, 0);
        LocalTime fin = inicio.plusHours(2);

        return new Horario(dia, inicio, fin, grupo);
    }

    @Transactional
    public Horario agregarHorario(Long grupoId, DiaSemana dia, LocalTime inicio, LocalTime fin) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Grupo no encontrado: " + grupoId));
        Horario horario = new Horario(dia, inicio, fin, grupo);
        return horarioRepository.save(horario);
    }

    @Transactional
    public Grupo actualizarCupo(Long grupoId, int nuevoCupo) {
        if (nuevoCupo < 1) throw new IllegalArgumentException("El cupo máximo debe ser mayor a 0");
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Grupo no encontrado: " + grupoId));
        grupo.setCupoMaximo(nuevoCupo);
        return grupoRepository.save(grupo);
    }
}
