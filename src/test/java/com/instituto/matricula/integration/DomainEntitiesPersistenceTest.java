package com.instituto.matricula.integration;

import com.instituto.matricula.domain.curso.Curso;
import com.instituto.matricula.domain.estudiante.Administrador;
import com.instituto.matricula.domain.estudiante.Estudiante;
import com.instituto.matricula.domain.estudiante.HistorialAcademico;
import com.instituto.matricula.domain.grupo.Grupo;
import com.instituto.matricula.domain.horario.DiaSemana;
import com.instituto.matricula.domain.horario.Horario;
import com.instituto.matricula.domain.inscripcion.EstadoInscripcion;
import com.instituto.matricula.domain.inscripcion.Inscripcion;
import com.instituto.matricula.domain.notificacion.Notificacion;
import com.instituto.matricula.domain.notificacion.TipoNotificacion;
import com.instituto.matricula.domain.prerrequisito.Prerrequisito;
import com.instituto.matricula.domain.prerrequisito.TipoPrerrequisito;
import com.instituto.matricula.infrastructure.repositories.AdministradorRepository;
import com.instituto.matricula.infrastructure.repositories.CursoRepository;
import com.instituto.matricula.infrastructure.repositories.EstudianteRepository;
import com.instituto.matricula.infrastructure.repositories.GrupoRepository;
import com.instituto.matricula.infrastructure.repositories.HistorialAcademicoRepository;
import com.instituto.matricula.infrastructure.repositories.HorarioRepository;
import com.instituto.matricula.infrastructure.repositories.InscripcionRepository;
import com.instituto.matricula.infrastructure.repositories.NotificacionRepository;
import com.instituto.matricula.infrastructure.repositories.PrerrequisitoRepository;
import com.instituto.matricula.infrastructure.repositories.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DomainEntitiesPersistenceTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private PrerrequisitoRepository prerrequisitoRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private HistorialAcademicoRepository historialAcademicoRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Test
    @DisplayName("Debe persistir correctamente Estudiante y Administrador con herencia JOINED")
    void debePersistirEstudianteYAdministradorConHerencia() {
        Estudiante est = new Estudiante("Carlos Gomez", "carlos@instituto.edu", "EST-101");
        Estudiante estGuardado = estudianteRepository.save(est);

        assertThat(estGuardado.getId()).isNotNull();
        assertThat(estGuardado.getCodigoEstudiante()).isEqualTo("EST-101");
        assertThat(estGuardado.isMatriculaActiva()).isTrue();

        Administrador admin = new Administrador("Maria Lopez", "maria@instituto.edu", "Coordinacion", "Directora");
        Administrador adminGuardado = administradorRepository.save(admin);

        assertThat(adminGuardado.getId()).isNotNull();
        assertThat(adminGuardado.getDepartamento()).isEqualTo("Coordinacion");
        assertThat(usuarioRepository.count()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Debe persistir Curso con prerrequisito MATRICULA_INSTITUCIONAL y default de 60% aprobacion")
    void debePersistirCursoConPrerrequisitoInstitucional() {
        Curso programacionI = new Curso("PROG-101", "Programacion I", 3);
        Curso cursoGuardado = cursoRepository.save(programacionI);

        assertThat(cursoGuardado.getId()).isNotNull();
        assertThat(cursoGuardado.getPorcentajeMinimoAprobacion()).isEqualTo(60.0);

        Prerrequisito prerreq = Prerrequisito.paraMatriculaInstitucional(cursoGuardado);
        Prerrequisito prerreqGuardado = prerrequisitoRepository.save(prerreq);

        assertThat(prerreqGuardado.getId()).isNotNull();
        assertThat(prerreqGuardado.getTipo()).isEqualTo(TipoPrerrequisito.MATRICULA_INSTITUCIONAL);
        assertThat(prerreqGuardado.getCursoRequerido()).isNull();
    }

    @Test
    @DisplayName("Debe persistir Curso con prerrequisito de CURSO_APROBADO")
    void debePersistirCursoConPrerrequisitoCursoAprobado() {
        Curso prog1 = cursoRepository.save(new Curso("PROG-101", "Programacion I", 3));
        Curso prog2 = cursoRepository.save(new Curso("PROG-102", "Programacion II", 3));

        Prerrequisito prerreq = Prerrequisito.paraCursoAprobado(prog2, prog1);
        Prerrequisito prerreqGuardado = prerrequisitoRepository.save(prerreq);

        assertThat(prerreqGuardado.getTipo()).isEqualTo(TipoPrerrequisito.CURSO_APROBADO);
        assertThat(prerreqGuardado.getCursoPrincipal().getId()).isEqualTo(prog2.getId());
        assertThat(prerreqGuardado.getCursoRequerido().getId()).isEqualTo(prog1.getId());
    }

    @Test
    @DisplayName("Debe persistir Grupo con cupo default 30 y multiples Horarios")
    void debePersistirGrupoYHorarios() {
        Curso curso = cursoRepository.save(new Curso("MAT-101", "Matematicas I", 4));

        Grupo grupo = new Grupo(1, curso);
        Grupo grupoGuardado = grupoRepository.save(grupo);

        assertThat(grupoGuardado.getId()).isNotNull();
        assertThat(grupoGuardado.getCupoMaximo()).isEqualTo(30);

        Horario h1 = new Horario(DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(10, 0), grupoGuardado);
        Horario h2 = new Horario(DiaSemana.MIERCOLES, LocalTime.of(8, 0), LocalTime.of(10, 0), grupoGuardado);
        horarioRepository.save(h1);
        horarioRepository.save(h2);

        assertThat(horarioRepository.findByGrupoId(grupoGuardado.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Debe persistir Inscripcion, HistorialAcademico y Notificacion")
    void debePersistirInscripcionHistorialYNotificacion() {
        Estudiante est = estudianteRepository.save(new Estudiante("Ana Ruiz", "ana@instituto.edu", "EST-202"));
        Curso curso = cursoRepository.save(new Curso("FIS-101", "Fisica I", 3));
        Grupo grupo = grupoRepository.save(new Grupo(1, curso));

        // Inscripcion
        Inscripcion inscripcion = new Inscripcion(est, grupo, EstadoInscripcion.INSCRITO);
        Inscripcion inscripcionGuardada = inscripcionRepository.save(inscripcion);
        assertThat(inscripcionGuardada.getId()).isNotNull();
        assertThat(inscripcionGuardada.getEstado()).isEqualTo(EstadoInscripcion.INSCRITO);

        // Notificacion
        Notificacion notif = new Notificacion(est, inscripcionGuardada, "Inscripcion exitosa", TipoNotificacion.PASO_A_INSCRITO);
        Notificacion notifGuardada = notificacionRepository.save(notif);
        assertThat(notifGuardada.getId()).isNotNull();
        assertThat(notifGuardada.isLeida()).isFalse();

        // Historial Academico
        HistorialAcademico historial = new HistorialAcademico(est, curso, 85.0, true, LocalDate.now());
        HistorialAcademico histGuardado = historialAcademicoRepository.save(historial);
        assertThat(histGuardado.getId()).isNotNull();
        assertThat(histGuardado.isAprobado()).isTrue();
    }
}
