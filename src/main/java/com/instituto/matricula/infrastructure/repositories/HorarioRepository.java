package com.instituto.matricula.infrastructure.repositories;

import com.instituto.matricula.domain.horario.Horario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {
    List<Horario> findByGrupoId(Long grupoId);
}
