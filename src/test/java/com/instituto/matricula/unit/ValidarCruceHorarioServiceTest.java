package com.instituto.matricula.unit;

import com.instituto.matricula.application.CruceHorarioException;
import com.instituto.matricula.application.ValidarCruceHorarioService;
import com.instituto.matricula.domain.curso.Curso;
import com.instituto.matricula.domain.estudiante.Estudiante;
import com.instituto.matricula.domain.grupo.Grupo;
import com.instituto.matricula.domain.horario.DiaSemana;
import com.instituto.matricula.domain.horario.Horario;
import com.instituto.matricula.domain.inscripcion.EstadoInscripcion;
import com.instituto.matricula.domain.inscripcion.Inscripcion;
import com.instituto.matricula.infrastructure.repositories.InscripcionRepository;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidarCruceHorarioServiceTest {

    @Mock
    private InscripcionRepository inscripcionRepository;

    @InjectMocks
    private ValidarCruceHorarioService validarCruceHorarioService;

    @Test
    @DisplayName("Debe lanzar excepción cuando hay cruce real de horario")
    void debeLanzarExcepcionConCruce() {
        Estudiante estudiante = crearEstudiante(1L);
        Curso cursoA = crearCurso(1L, "MAT-101");
        Curso cursoB = crearCurso(2L, "FIS-101");

        Grupo grupoActivo = crearGrupo(1L, cursoA);
        grupoActivo.agregarHorario(new Horario(DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(10, 0), grupoActivo));

        Inscripcion ins = new Inscripcion(estudiante, grupoActivo, EstadoInscripcion.INSCRITO);

        Grupo grupoCandidato = crearGrupo(2L, cursoB);
        grupoCandidato.agregarHorario(new Horario(DiaSemana.LUNES, LocalTime.of(9, 0), LocalTime.of(11, 0), grupoCandidato));

        when(inscripcionRepository.findByEstudianteId(1L)).thenReturn(List.of(ins));

        assertThatThrownBy(() -> validarCruceHorarioService.validar(estudiante, grupoCandidato))
                .isInstanceOf(CruceHorarioException.class)
                .hasMessageContaining("Cruce de horario");
    }

    @Test
    @DisplayName("NO debe lanzar excepción si los horarios son en días distintos")
    void noDebeLanzarExcepcionConDiasDistintos() {
        Estudiante estudiante = crearEstudiante(1L);
        Curso cursoA = crearCurso(1L, "MAT-101");
        Curso cursoB = crearCurso(2L, "FIS-101");

        Grupo grupoActivo = crearGrupo(1L, cursoA);
        grupoActivo.agregarHorario(new Horario(DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(10, 0), grupoActivo));

        Inscripcion ins = new Inscripcion(estudiante, grupoActivo, EstadoInscripcion.INSCRITO);

        Grupo grupoCandidato = crearGrupo(2L, cursoB);
        grupoCandidato.agregarHorario(new Horario(DiaSemana.MIERCOLES, LocalTime.of(8, 0), LocalTime.of(10, 0), grupoCandidato));

        when(inscripcionRepository.findByEstudianteId(1L)).thenReturn(List.of(ins));

        assertThatCode(() -> validarCruceHorarioService.validar(estudiante, grupoCandidato))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("NO debe revisar grupos en lista de espera ni cancelados")
    void noDebeRevisarGruposInactivos() {
        Estudiante estudiante = crearEstudiante(1L);
        Curso cursoA = crearCurso(1L, "MAT-101");
        Curso cursoB = crearCurso(2L, "FIS-101");

        Grupo grupoEspera = crearGrupo(1L, cursoA);
        grupoEspera.agregarHorario(new Horario(DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(10, 0), grupoEspera));

        // La inscripción está en LISTA_ESPERA, no INSCRITO
        Inscripcion ins = new Inscripcion(estudiante, grupoEspera, EstadoInscripcion.LISTA_ESPERA);

        Grupo grupoCandidato = crearGrupo(2L, cursoB);
        grupoCandidato.agregarHorario(new Horario(DiaSemana.LUNES, LocalTime.of(9, 0), LocalTime.of(11, 0), grupoCandidato));

        when(inscripcionRepository.findByEstudianteId(1L)).thenReturn(List.of(ins));

        assertThatCode(() -> validarCruceHorarioService.validar(estudiante, grupoCandidato))
                .doesNotThrowAnyException();
    }

    private Estudiante crearEstudiante(Long id) {
        Estudiante e = new Estudiante("Test", "test@test.com", "EST-00" + id);
        try {
            var f = Estudiante.class.getSuperclass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(e, id);
        } catch (Exception ex) { /* no-op en test */ }
        return e;
    }

    private Curso crearCurso(Long id, String codigo) {
        Curso c = new Curso(codigo, "Curso " + codigo, 3);
        try {
            var f = Curso.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(c, id);
        } catch (Exception ex) { /* no-op en test */ }
        return c;
    }

    private Grupo crearGrupo(Long id, Curso curso) {
        Grupo g = new Grupo(id.intValue(), curso);
        try {
            var f = Grupo.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(g, id);
        } catch (Exception ex) { /* no-op en test */ }
        return g;
    }
}
