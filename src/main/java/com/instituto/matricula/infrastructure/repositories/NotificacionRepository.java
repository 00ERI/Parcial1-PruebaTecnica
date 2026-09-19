package com.instituto.matricula.infrastructure.repositories;

import com.instituto.matricula.domain.notificacion.Notificacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByEstudianteId(Long estudianteId);
    List<Notificacion> findByEstudianteIdAndLeidaFalse(Long estudianteId);
}
