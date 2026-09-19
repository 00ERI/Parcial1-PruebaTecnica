package com.instituto.matricula.domain.inscripcion;

import com.instituto.matricula.domain.estudiante.Estudiante;
import com.instituto.matricula.domain.grupo.Grupo;
import com.instituto.matricula.domain.notificacion.Notificacion;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa una solicitud o registro de inscripción de un estudiante en un grupo.
 */
@Entity
@Table(name = "inscripciones")
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha de solicitud es obligatoria")
    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @NotNull(message = "El estado de inscripción es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 25)
    private EstadoInscripcion estado;

    /**
     * Posición en la lista de espera (solo aplica cuando estado == LISTA_ESPERA).
     */
    @Column(name = "posicion_lista_espera")
    private Integer posicionListaEspera;

    /**
     * Registro del motivo de rechazo en caso de no cumplir prerrequisitos, cruce de horario, etc.
     */
    @Column(name = "motivo_rechazo", length = 500)
    private String motivoRechazo;

    @NotNull(message = "El estudiante es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @NotNull(message = "El grupo es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notificacion> notificaciones = new ArrayList<>();

    public Inscripcion() {
        this.fechaSolicitud = LocalDateTime.now();
    }

    public Inscripcion(Estudiante estudiante, Grupo grupo, EstadoInscripcion estado) {
        this.estudiante = estudiante;
        this.grupo = grupo;
        this.estado = estado;
        this.fechaSolicitud = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public EstadoInscripcion getEstado() {
        return estado;
    }

    public void setEstado(EstadoInscripcion estado) {
        this.estado = estado;
    }

    public Integer getPosicionListaEspera() {
        return posicionListaEspera;
    }

    public void setPosicionListaEspera(Integer posicionListaEspera) {
        this.posicionListaEspera = posicionListaEspera;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public List<Notificacion> getNotificaciones() {
        return notificaciones;
    }

    public void setNotificaciones(List<Notificacion> notificaciones) {
        this.notificaciones = notificaciones;
    }

    public void agregarNotificacion(Notificacion notificacion) {
        notificaciones.add(notificacion);
        notificacion.setInscripcion(this);
        notificacion.setEstudiante(this.estudiante);
    }

    public boolean isActiva() {
        return this.estado == EstadoInscripcion.INSCRITO || this.estado == EstadoInscripcion.LISTA_ESPERA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inscripcion that = (Inscripcion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
