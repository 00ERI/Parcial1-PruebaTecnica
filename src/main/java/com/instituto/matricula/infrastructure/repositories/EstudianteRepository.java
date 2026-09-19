package com.instituto.matricula.infrastructure.repositories;

import com.instituto.matricula.domain.estudiante.Estudiante;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    Optional<Estudiante> findByCodigoEstudiante(String codigoEstudiante);
}
