package com.instituto.matricula.domain.estudiante;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/**
 * Representa a un usuario administrador con permisos de configuración académica.
 */
@Entity
@Table(name = "administradores")
public class Administrador extends Usuario {

    @NotBlank(message = "El departamento es obligatorio")
    @Column(name = "departamento", nullable = false, length = 100)
    private String departamento;

    @Column(name = "cargo", length = 100)
    private String cargo;

    public Administrador() {
        super();
    }

    public Administrador(String nombre, String correo, String departamento, String cargo) {
        super(nombre, correo);
        this.departamento = departamento;
        this.cargo = cargo;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
}
