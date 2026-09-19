package com.instituto.matricula.infrastructure.repositories;

import com.instituto.matricula.domain.inscripcion.EstadoInscripcion;
import com.instituto.matricula.domain.inscripcion.Inscripcion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    List<Inscripcion> findByEstudianteId(Long estudianteId);
    List<Inscripcion> findByGrupoId(Long grupoId);
    List<Inscripcion> findByGrupoIdAndEstado(Long grupoId, EstadoInscripcion estado);
    Optional<Inscripcion> findByEstudianteIdAndGrupoId(Long estudianteId, Long grupoId);
}
