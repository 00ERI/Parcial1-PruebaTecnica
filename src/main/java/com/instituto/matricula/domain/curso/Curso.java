package com.instituto.matricula.domain.curso;

import com.instituto.matricula.domain.grupo.Grupo;
import com.instituto.matricula.domain.prerrequisito.Prerrequisito;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad que representa una asignatura o curso académico.
 */
@Entity
@Table(name = "cursos")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código del curso es obligatorio")
    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @NotBlank(message = "El nombre del curso es obligatorio")
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @NotNull(message = "Los créditos son obligatorios")
    @Min(value = 1, message = "Los créditos deben ser mayores a 0")
    @Column(name = "creditos", nullable = false)
    private Integer creditos;

    /**
     * Métrica mínima de aprobación. Valor por defecto: 60.0% (según especificación AGENTS.md / ASSUMPTIONS.md).
     */
    @NotNull(message = "El porcentaje mínimo de aprobación es obligatorio")
    @DecimalMin(value = "0.0", message = "El porcentaje no puede ser negativo")
    @DecimalMax(value = "100.0", message = "El porcentaje no puede exceder 100")
    @Column(name = "porcentaje_minimo_aprobacion", nullable = false)
    private Double porcentajeMinimoAprobacion = 60.0;

    @OneToMany(mappedBy = "cursoPrincipal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prerrequisito> prerrequisitos = new ArrayList<>();

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Grupo> grupos = new ArrayList<>();

    public Curso() {
    }

    public Curso(String codigo, String nombre, Integer creditos) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.creditos = creditos;
        this.porcentajeMinimoAprobacion = 60.0;
    }

    public Curso(String codigo, String nombre, Integer creditos, Double porcentajeMinimoAprobacion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.creditos = creditos;
        this.porcentajeMinimoAprobacion = (porcentajeMinimoAprobacion != null) ? porcentajeMinimoAprobacion : 60.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getCreditos() {
        return creditos;
    }

    public void setCreditos(Integer creditos) {
        this.creditos = creditos;
    }

    public Double getPorcentajeMinimoAprobacion() {
        return porcentajeMinimoAprobacion;
    }

    public void setPorcentajeMinimoAprobacion(Double porcentajeMinimoAprobacion) {
        this.porcentajeMinimoAprobacion = porcentajeMinimoAprobacion;
    }

    public List<Prerrequisito> getPrerrequisitos() {
        return prerrequisitos;
    }

    public void setPrerrequisitos(List<Prerrequisito> prerrequisitos) {
        this.prerrequisitos = prerrequisitos;
    }

    public List<Grupo> getGrupos() {
        return grupos;
    }

    public void setGrupos(List<Grupo> grupos) {
        this.grupos = grupos;
    }

    public void agregarPrerrequisito(Prerrequisito prerrequisito) {
        prerrequisitos.add(prerrequisito);
        prerrequisito.setCursoPrincipal(this);
    }

    public void agregarGrupo(Grupo grupo) {
        grupos.add(grupo);
        grupo.setCurso(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Curso curso = (Curso) o;
        return Objects.equals(id, curso.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
