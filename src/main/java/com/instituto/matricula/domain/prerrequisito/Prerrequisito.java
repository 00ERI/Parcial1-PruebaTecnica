package com.instituto.matricula.domain.prerrequisito;

import com.instituto.matricula.domain.curso.Curso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Representa una condición o prerrequisito exigido para cursar una materia.
 * Puede ser de tipo CURSO_APROBADO (requiere haber aprobado cursoRequerido) o
 * MATRICULA_INSTITUCIONAL (para materias iniciales donde no hay curso previo, cursoRequerido es null).
 */
@Entity
@Table(name = "prerrequisitos")
public class Prerrequisito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El tipo de prerrequisito es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoPrerrequisito tipo;

    @Column(name = "obligatorio", nullable = false)
    private boolean obligatorio = true;

    /**
     * Curso que impone este prerrequisito.
     */
    @NotNull(message = "El curso principal es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_principal_id", nullable = false)
    private Curso cursoPrincipal;

    /**
     * Curso que debe estar aprobado. Es nulo si tipo == MATRICULA_INSTITUCIONAL.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_requerido_id")
    private Curso cursoRequerido;

    public Prerrequisito() {
    }

    /**
     * Constructor para prerrequisito de tipo MATRICULA_INSTITUCIONAL.
     */
    public static Prerrequisito paraMatriculaInstitucional(Curso cursoPrincipal) {
        Prerrequisito p = new Prerrequisito();
        p.setTipo(TipoPrerrequisito.MATRICULA_INSTITUCIONAL);
        p.setCursoPrincipal(cursoPrincipal);
        p.setCursoRequerido(null);
        p.setObligatorio(true);
        return p;
    }

    /**
     * Constructor para prerrequisito de tipo CURSO_APROBADO.
     */
    public static Prerrequisito paraCursoAprobado(Curso cursoPrincipal, Curso cursoRequerido) {
        Prerrequisito p = new Prerrequisito();
        p.setTipo(TipoPrerrequisito.CURSO_APROBADO);
        p.setCursoPrincipal(cursoPrincipal);
        p.setCursoRequerido(cursoRequerido);
        p.setObligatorio(true);
        return p;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoPrerrequisito getTipo() {
        return tipo;
    }

    public void setTipo(TipoPrerrequisito tipo) {
        this.tipo = tipo;
    }

    public boolean isObligatorio() {
        return obligatorio;
    }

    public void setObligatorio(boolean obligatorio) {
        this.obligatorio = obligatorio;
    }

    public Curso getCursoPrincipal() {
        return cursoPrincipal;
    }

    public void setCursoPrincipal(Curso cursoPrincipal) {
        this.cursoPrincipal = cursoPrincipal;
    }

    public Curso getCursoRequerido() {
        return cursoRequerido;
    }

    public void setCursoRequerido(Curso cursoRequerido) {
        this.cursoRequerido = cursoRequerido;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prerrequisito that = (Prerrequisito) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
