package com.instituto.matricula.unit;

import com.instituto.matricula.domain.horario.DiaSemana;
import com.instituto.matricula.domain.horario.Horario;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HorarioUnitTest {

    @Test
    @DisplayName("Debe detectar cruce cuando los horarios se solapan parcialmente el mismo día")
    void debeDetectarCrucePorSolapamientoParcial() {
        Horario h1 = new Horario(DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(10, 0));
        Horario h2 = new Horario(DiaSemana.LUNES, LocalTime.of(9, 0), LocalTime.of(11, 0));

        assertThat(h1.seCruzaCon(h2)).isTrue();
        assertThat(h2.seCruzaCon(h1)).isTrue();
    }

    @Test
    @DisplayName("Debe detectar cruce cuando un horario contiene totalmente a otro el mismo día")
    void debeDetectarCrucePorContencionTotal() {
        Horario h1 = new Horario(DiaSemana.MARTES, LocalTime.of(7, 0), LocalTime.of(12, 0));
        Horario h2 = new Horario(DiaSemana.MARTES, LocalTime.of(8, 0), LocalTime.of(10, 0));

        assertThat(h1.seCruzaCon(h2)).isTrue();
        assertThat(h2.seCruzaCon(h1)).isTrue();
    }

    @Test
    @DisplayName("NO debe haber cruce si los horarios son contiguos (uno termina cuando el otro empieza)")
    void noDebeHaberCruceSiSonContiguos() {
        Horario h1 = new Horario(DiaSemana.MIERCOLES, LocalTime.of(8, 0), LocalTime.of(10, 0));
        Horario h2 = new Horario(DiaSemana.MIERCOLES, LocalTime.of(10, 0), LocalTime.of(12, 0));

        assertThat(h1.seCruzaCon(h2)).isFalse();
        assertThat(h2.seCruzaCon(h1)).isFalse();
    }

    @Test
    @DisplayName("NO debe haber cruce si son días distintos aunque coincida la misma hora")
    void noDebeHaberCruceSiSonDiasDistintos() {
        Horario h1 = new Horario(DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(10, 0));
        Horario h2 = new Horario(DiaSemana.VIERNES, LocalTime.of(8, 0), LocalTime.of(10, 0));

        assertThat(h1.seCruzaCon(h2)).isFalse();
        assertThat(h2.seCruzaCon(h1)).isFalse();
    }

    @Test
    @DisplayName("Debe lanzar excepción si horaInicio no es anterior a horaFin")
    void debeLanzarExcepcionPorHorarioIncoherente() {
        assertThatThrownBy(() -> new Horario(DiaSemana.LUNES, LocalTime.of(10, 0), LocalTime.of(8, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("anterior a la hora de fin");

        assertThatThrownBy(() -> new Horario(DiaSemana.LUNES, LocalTime.of(10, 0), LocalTime.of(10, 0)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
