package com.instituto.matricula.application;

import com.instituto.matricula.domain.grupo.Grupo;
import com.instituto.matricula.domain.inscripcion.EstadoInscripcion;
import com.instituto.matricula.domain.inscripcion.Inscripcion;
import com.instituto.matricula.domain.notificacion.Notificacion;
import com.instituto.matricula.domain.notificacion.TipoNotificacion;
import com.instituto.matricula.infrastructure.repositories.InscripcionRepository;
import com.instituto.matricula.infrastructure.repositories.NotificacionRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestiona la cola FIFO de lista de espera y la promoción automática al liberar un cupo.
 * <p>
 * Regla (AGENTS.md §3): Al cancelar una inscripción, el primero de la lista de espera (por
 * fechaSolicitud) pasa a INSCRITO automáticamente, con su Notificacion correspondiente.
 */
@Service
public class GestionarListaEsperaService {

    private final InscripcionRepository inscripcionRepository;
    private final NotificacionRepository notificacionRepository;

    public GestionarListaEsperaService(
            InscripcionRepository inscripcionRepository,
            NotificacionRepository notificacionRepository) {
        this.inscripcionRepository = inscripcionRepository;
        this.notificacionRepository = notificacionRepository;
    }

    /**
     * Intenta promover al primer estudiante de la lista de espera del grupo dado.
     * Solo promueve si hay cupo disponible.
     */
    @Transactional
    public void promoverSiguienteEnListaDeEspera(Grupo grupo) {
        // Recargar inscripciones del grupo para datos frescos
        List<Inscripcion> listaEspera = inscripcionRepository
                .findByGrupoIdAndEstado(grupo.getId(), EstadoInscripcion.LISTA_ESPERA);

        if (listaEspera.isEmpty()) return;

        // FIFO: ordenar por fechaSolicitud, el más antiguo primero
        listaEspera.sort(Comparator.comparing(Inscripcion::getFechaSolicitud));

        long inscritos = inscripcionRepository
                .findByGrupoIdAndEstado(grupo.getId(), EstadoInscripcion.INSCRITO)
                .size();

        if (inscritos >= grupo.getCupoMaximo()) return;

        Inscripcion siguiente = listaEspera.get(0);
        siguiente.setEstado(EstadoInscripcion.INSCRITO);
        siguiente.setPosicionListaEspera(null);
        inscripcionRepository.save(siguiente);

        Notificacion notif = new Notificacion(
            siguiente.getEstudiante(),
            siguiente,
            String.format("Tu solicitud de inscripción al grupo %d del curso '%s' ha sido aceptada. Ya quedaste inscrito.",
                grupo.getNumeroGrupo(), grupo.getCurso().getNombre()),
            TipoNotificacion.PASO_A_INSCRITO
        );
        notificacionRepository.save(notif);

        // Recalcular posiciones de lista de espera restantes
        List<Inscripcion> listaRestante = inscripcionRepository
                .findByGrupoIdAndEstado(grupo.getId(), EstadoInscripcion.LISTA_ESPERA);
        listaRestante.sort(Comparator.comparing(Inscripcion::getFechaSolicitud));
        for (int i = 0; i < listaRestante.size(); i++) {
            listaRestante.get(i).setPosicionListaEspera(i + 1);
        }
        inscripcionRepository.saveAll(listaRestante);
    }

    /**
     * Asigna la posición en lista de espera al agregar una nueva solicitud en cola.
     */
    public int calcularPosicionListaEspera(Grupo grupo) {
        return (int) inscripcionRepository
                .findByGrupoIdAndEstado(grupo.getId(), EstadoInscripcion.LISTA_ESPERA)
                .size() + 1;
    }
}
