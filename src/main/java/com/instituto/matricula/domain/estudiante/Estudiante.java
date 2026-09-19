package com.instituto.matricula.domain.estudiante;

import com.instituto.matricula.domain.inscripcion.Inscripcion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa a un estudiante en el sistema de matrícula.
 */
@Entity
@Table(name = "estudiantes")
public class Estudiante extends Usuario {

    @NotBlank(message = "El código de estudiante es obligatorio")
    @Column(name = "codigo_estudiante", nullable = false, unique = true, length = 50)
    private String codigoEstudiante;

    @Column(name = "matricula_activa", nullable = false)
    private boolean matriculaActiva = true;

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistorialAcademico> historialAcademico = new ArrayList<>();

    public Estudiante() {
        super();
    }

    public Estudiante(String nombre, String correo, String codigoEstudiante) {
        super(nombre, correo);
        this.codigoEstudiante = codigoEstudiante;
        this.matriculaActiva = true;
    }

    public String getCodigoEstudiante() {
        return codigoEstudiante;
    }

    public void setCodigoEstudiante(String codigoEstudiante) {
        this.codigoEstudiante = codigoEstudiante;
    }

    public boolean isMatriculaActiva() {
        return matriculaActiva;
    }

    public void setMatriculaActiva(boolean matriculaActiva) {
        this.matriculaActiva = matriculaActiva;
    }

    public List<Inscripcion> getInscripciones() {
        return inscripciones;
    }

    public void setInscripciones(List<Inscripcion> inscripciones) {
        this.inscripciones = inscripciones;
    }

    public List<HistorialAcademico> getHistorialAcademico() {
        return historialAcademico;
    }

    public void setHistorialAcademico(List<HistorialAcademico> historialAcademico) {
        this.historialAcademico = historialAcademico;
    }
}
