package com.instituto.matricula.integration;

import com.instituto.matricula.application.CruceHorarioException;
import com.instituto.matricula.application.GestionarCursoService;
import com.instituto.matricula.application.GestionarGrupoService;
import com.instituto.matricula.application.InscripcionDuplicadaException;
import com.instituto.matricula.application.InscribirEstudianteService;
import com.instituto.matricula.application.PrerrequisitosNoCumplidosException;
import com.instituto.matricula.domain.curso.Curso;
import com.instituto.matricula.domain.estudiante.Estudiante;
import com.instituto.matricula.domain.grupo.Grupo;
import com.instituto.matricula.domain.horario.DiaSemana;
import com.instituto.matricula.domain.horario.Horario;
import com.instituto.matricula.domain.inscripcion.EstadoInscripcion;
import com.instituto.matricula.domain.inscripcion.Inscripcion;
import com.instituto.matricula.infrastructure.repositories.EstudianteRepository;
import com.instituto.matricula.infrastructure.repositories.GrupoRepository;
import com.instituto.matricula.infrastructure.repositories.HorarioRepository;
import com.instituto.matricula.infrastructure.repositories.InscripcionRepository;
import com.instituto.matricula.infrastructure.repositories.NotificacionRepository;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InscripcionBusinessRulesTest {

    @Autowired private InscribirEstudianteService inscribirEstudianteService;
    @Autowired private GestionarCursoService gestionarCursoService;
    @Autowired private GestionarGrupoService gestionarGrupoService;
    @Autowired private EstudianteRepository estudianteRepository;
    @Autowired private GrupoRepository grupoRepository;
    @Autowired private HorarioRepository horarioRepository;
    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private NotificacionRepository notificacionRepository;

    private Estudiante estudiante;
    private Curso cursoProg1;
    private Curso cursoProg2;

    @BeforeEach
    void setup() {
        // Datos base: un estudiante y dos cursos
        estudiante = estudianteRepository.save(new Estudiante("Test Alumno", "alumno@test.edu", "EST-TEST-01"));
        cursoProg1 = gestionarCursoService.crearConPrerrequisitoInstitucional("PROG-T01", "Prog Test I", 3, 60.0);
        cursoProg2 = gestionarCursoService.crearConPrerrequisitoCurso("PROG-T02", "Prog Test II", 3, 60.0, cursoProg1.getId());
    }

    @Test
    @DisplayName("Debe inscribir exitosamente cuando hay cupo y prerrequisitos cumplidos")
    void debeInscribirExitosamente() {
        Grupo grupo = gestionarGrupoService.crearConHorarioAleatorio(cursoProg1.getId(), 1, 30);
        Inscripcion ins = inscribirEstudianteService.inscribir(estudiante.getId(), grupo.getId());

        assertThat(ins.getId()).isNotNull();
        assertThat(ins.getEstado()).isEqualTo(EstadoInscripcion.INSCRITO);
    }

    @Test
    @DisplayName("Debe rechazar inscripción cuando no cumple prerrequisitos de CURSO_APROBADO")
    void debeRechazarPorPrerrequisitosNoCumplidos() {
        // cursoProg2 requiere haber aprobado cursoProg1, pero el estudiante no tiene historial
        Grupo grupo = gestionarGrupoService.crearConHorarioAleatorio(cursoProg2.getId(), 1, 30);

        assertThatThrownBy(() -> inscribirEstudianteService.inscribir(estudiante.getId(), grupo.getId()))
                .isInstanceOf(PrerrequisitosNoCumplidosException.class);

        // La inscripción rechazada debe persistirse
        assertThat(inscripcionRepository.findByEstudianteId(estudiante.getId()))
                .anyMatch(i -> i.getEstado() == EstadoInscripcion.RECHAZADA);

        // La notificación de rechazo debe existir
        assertThat(notificacionRepository.findByEstudianteId(estudiante.getId())).isNotEmpty();
    }

    @Test
    @DisplayName("Debe pasar a LISTA_ESPERA cuando el grupo está lleno")
    void debePasarAListaEsperaCuandoNoHayCupo() {
        // Crear grupo con cupo = 1
        Grupo grupo = gestionarGrupoService.crearConHorarioAleatorio(cursoProg1.getId(), 1, 1);

        // Primer estudiante llena el cupo
        Estudiante otro = estudianteRepository.save(new Estudiante("Otro", "otro@test.edu", "EST-OTRO-01"));
        inscribirEstudianteService.inscribir(otro.getId(), grupo.getId());

        // Segundo estudiante va a lista de espera
        Inscripcion ins = inscribirEstudianteService.inscribir(estudiante.getId(), grupo.getId());

        assertThat(ins.getEstado()).isEqualTo(EstadoInscripcion.LISTA_ESPERA);
        assertThat(ins.getPosicionListaEspera()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe promocionar de lista de espera al cancelar inscripción activa")
    void debePrimerDeListaEsperaCuandoAlguienCancela() {
        Grupo grupo = gestionarGrupoService.crearConHorarioAleatorio(cursoProg1.getId(), 1, 1);

        Estudiante primero = estudianteRepository.save(new Estudiante("Primero", "primero@test.edu", "EST-P01"));
        Estudiante segundo = estudianteRepository.save(new Estudiante("Segundo", "segundo@test.edu", "EST-P02"));

        Inscripcion insPrimero = inscribirEstudianteService.inscribir(primero.getId(), grupo.getId());
        Inscripcion insSegundo = inscribirEstudianteService.inscribir(segundo.getId(), grupo.getId());

        assertThat(insSegundo.getEstado()).isEqualTo(EstadoInscripcion.LISTA_ESPERA);

        // Al cancelar el primero, el segundo debe pasar a INSCRITO
        inscribirEstudianteService.cancelar(insPrimero.getId());

        Inscripcion insActualizada = inscripcionRepository.findById(insSegundo.getId()).orElseThrow();
        assertThat(insActualizada.getEstado()).isEqualTo(EstadoInscripcion.INSCRITO);
        assertThat(insActualizada.getPosicionListaEspera()).isNull();
    }

    @Test
    @DisplayName("Debe rechazar inscripción duplicada en el mismo curso")
    void debeRechazarInscripcionDuplicadaEnMismoCurso() {
        Grupo grupo1 = gestionarGrupoService.crearConHorarioAleatorio(cursoProg1.getId(), 1, 30);
        Grupo grupo2 = gestionarGrupoService.crearConHorarioAleatorio(cursoProg1.getId(), 2, 30);

        inscribirEstudianteService.inscribir(estudiante.getId(), grupo1.getId());

        assertThatThrownBy(() -> inscribirEstudianteService.inscribir(estudiante.getId(), grupo2.getId()))
                .isInstanceOf(InscripcionDuplicadaException.class)
                .hasMessageContaining("inscripción activa en el curso");
    }

    @Test
    @DisplayName("Debe rechazar inscripción con cruce de horario")
    void debeRechazarConCruceDeHorario() {
        Grupo grupoA = grupoRepository.save(new Grupo(1, cursoProg1));
        Grupo grupoB = grupoRepository.save(new Grupo(1, cursoProg2));

        // Mismo día y hora que se cruzan
        horarioRepository.save(new Horario(DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(10, 0), grupoA));
        horarioRepository.save(new Horario(DiaSemana.LUNES, LocalTime.of(9, 0), LocalTime.of(11, 0), grupoB));

        // Primero inscribir en grupoA (para cursoProg1, tiene prerrequisito institucional)
        inscribirEstudianteService.inscribir(estudiante.getId(), grupoA.getId());

        // Intentar inscribir en grupoB con cruce de horario debe fallar
        // cursoProg2 requiere cursoProg1 aprobado, así que el rechazo será por prerrequisitos
        // Para probar solo cruce, creamos otro curso sin prerrequisito previo
        Curso cursoExtra = gestionarCursoService.crearConPrerrequisitoInstitucional("EXTRA-01", "Extra", 2, 60.0);
        Grupo grupoExtra = grupoRepository.save(new Grupo(1, cursoExtra));
        horarioRepository.save(new Horario(DiaSemana.LUNES, LocalTime.of(9, 0), LocalTime.of(11, 0), grupoExtra));

        assertThatThrownBy(() -> inscribirEstudianteService.inscribir(estudiante.getId(), grupoExtra.getId()))
                .isInstanceOf(CruceHorarioException.class);
    }
}
