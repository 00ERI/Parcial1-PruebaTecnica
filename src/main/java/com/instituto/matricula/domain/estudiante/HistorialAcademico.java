package com.instituto.matricula.domain.estudiante;

import com.instituto.matricula.domain.curso.Curso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Registro permanente de materias cursadas y aprobadas por un estudiante.
 * Indispensable para la validación de prerrequisitos aprobados (ASSUMPTIONS.md #3).
 */
@Entity
@Table(name = "historial_academico")
public class HistorialAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El estudiante es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @NotNull(message = "El curso es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column(name = "nota_final")
    private Double notaFinal;

    /**
     * Porcentaje de avance/asistencia/evaluaciones obtenido en el curso.
     */
    @Column(name = "porcentaje_obtenido")
    private Double porcentajeObtenido;

    @Column(name = "aprobado", nullable = false)
    private boolean aprobado;

    @Column(name = "fecha_aprobacion")
    private LocalDate fechaAprobacion;

    public HistorialAcademico() {
    }

    public HistorialAcademico(Estudiante estudiante, Curso curso, Double notaFinal, boolean aprobado, LocalDate fechaAprobacion) {
        this.estudiante = estudiante;
        this.curso = curso;
        this.notaFinal = notaFinal;
        this.porcentajeObtenido = notaFinal;
        this.aprobado = aprobado;
        this.fechaAprobacion = fechaAprobacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Double getNotaFinal() {
        return notaFinal;
    }

    public void setNotaFinal(Double notaFinal) {
        this.notaFinal = notaFinal;
    }

    public Double getPorcentajeObtenido() {
        return porcentajeObtenido;
    }

    public void setPorcentajeObtenido(Double porcentajeObtenido) {
        this.porcentajeObtenido = porcentajeObtenido;
    }

    public boolean isAprobado() {
        return aprobado;
    }

    public void setAprobado(boolean aprobado) {
        this.aprobado = aprobado;
    }

    public LocalDate getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(LocalDate fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistorialAcademico that = (HistorialAcademico) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
