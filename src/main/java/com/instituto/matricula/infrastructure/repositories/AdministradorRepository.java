package com.instituto.matricula.infrastructure.repositories;

import com.instituto.matricula.domain.estudiante.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
}
