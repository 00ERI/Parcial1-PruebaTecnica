package com.instituto.matricula.infrastructure.repositories;

import com.instituto.matricula.domain.prerrequisito.Prerrequisito;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrerrequisitoRepository extends JpaRepository<Prerrequisito, Long> {
    List<Prerrequisito> findByCursoPrincipalId(Long cursoPrincipalId);
}
