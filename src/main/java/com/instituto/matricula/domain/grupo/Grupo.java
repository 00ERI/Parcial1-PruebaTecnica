package com.instituto.matricula.domain.grupo;

import com.instituto.matricula.domain.curso.Curso;
import com.instituto.matricula.domain.horario.Horario;
import com.instituto.matricula.domain.inscripcion.EstadoInscripcion;
import com.instituto.matricula.domain.inscripcion.Inscripcion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa una sección o grupo de un curso con cupo y sesiones de horario.
 */
@Entity
@Table(name = "grupos")
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El número de grupo es obligatorio")
    @Column(name = "numero_grupo", nullable = false)
    private Integer numeroGrupo;

    /**
     * Cupo máximo del grupo. Valor por defecto: 30 (según regla 3.1 / ASSUMPTIONS.md).
     */
    @NotNull(message = "El cupo máximo es obligatorio")
    @Min(value = 1, message = "El cupo máximo debe ser al menos 1")
    @Column(name = "cupo_maximo", nullable = false)
    private Integer cupoMaximo = 30;

    @NotNull(message = "El curso es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Horario> horarios = new ArrayList<>();

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    public Grupo() {
    }

    public Grupo(Integer numeroGrupo, Curso curso) {
        this.numeroGrupo = numeroGrupo;
        this.curso = curso;
        this.cupoMaximo = 30;
    }

    public Grupo(Integer numeroGrupo, Integer cupoMaximo, Curso curso) {
        this.numeroGrupo = numeroGrupo;
        this.cupoMaximo = (cupoMaximo != null) ? cupoMaximo : 30;
        this.curso = curso;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumeroGrupo() {
        return numeroGrupo;
    }

    public void setNumeroGrupo(Integer numeroGrupo) {
        this.numeroGrupo = numeroGrupo;
    }

    public Integer getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(Integer cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public List<Horario> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<Horario> horarios) {
        this.horarios = horarios;
    }

    public List<Inscripcion> getInscripciones() {
        return inscripciones;
    }

    public void setInscripciones(List<Inscripcion> inscripciones) {
        this.inscripciones = inscripciones;
    }

    public void agregarHorario(Horario horario) {
        horarios.add(horario);
        horario.setGrupo(this);
    }

    public void agregarInscripcion(Inscripcion inscripcion) {
        inscripciones.add(inscripcion);
        inscripcion.setGrupo(this);
    }

    // Métodos de cálculo de negocio de grupo
    public long numeroInscritos() {
        return inscripciones.stream()
                .filter(i -> i.getEstado() == EstadoInscripcion.INSCRITO)
                .count();
    }

    public long tamanoListaEspera() {
        return inscripciones.stream()
                .filter(i -> i.getEstado() == EstadoInscripcion.LISTA_ESPERA)
                .count();
    }

    public int cuposDisponibles() {
        int disponibles = cupoMaximo - (int) numeroInscritos();
        return Math.max(0, disponibles);
    }

    public boolean tieneCupo() {
        return cuposDisponibles() > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grupo grupo = (Grupo) o;
        return Objects.equals(id, grupo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
