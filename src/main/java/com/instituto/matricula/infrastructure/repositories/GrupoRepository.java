package com.instituto.matricula.infrastructure.repositories;

import com.instituto.matricula.domain.grupo.Grupo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    List<Grupo> findByCursoId(Long cursoId);
}
