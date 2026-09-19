package com.instituto.matricula.infrastructure.repositories;

import com.instituto.matricula.domain.estudiante.HistorialAcademico;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialAcademicoRepository extends JpaRepository<HistorialAcademico, Long> {
    List<HistorialAcademico> findByEstudianteId(Long estudianteId);
    Optional<HistorialAcademico> findByEstudianteIdAndCursoId(Long estudianteId, Long cursoId);
}
