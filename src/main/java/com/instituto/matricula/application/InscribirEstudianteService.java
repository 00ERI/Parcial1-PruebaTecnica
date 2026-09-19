package com.instituto.matricula.application;

import com.instituto.matricula.domain.curso.Curso;
import com.instituto.matricula.domain.estudiante.Estudiante;
import com.instituto.matricula.domain.grupo.Grupo;
import com.instituto.matricula.domain.inscripcion.EstadoInscripcion;
import com.instituto.matricula.domain.inscripcion.Inscripcion;
import com.instituto.matricula.domain.notificacion.Notificacion;
import com.instituto.matricula.domain.notificacion.TipoNotificacion;
import com.instituto.matricula.infrastructure.repositories.EstudianteRepository;
import com.instituto.matricula.infrastructure.repositories.GrupoRepository;
import com.instituto.matricula.infrastructure.repositories.InscripcionRepository;
import com.instituto.matricula.infrastructure.repositories.NotificacionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio principal del caso de uso: Inscribir un estudiante en un grupo.
 * <p>
 * Orquesta las siguientes reglas de negocio definidas en AGENTS.md §3:
 * 1. No inscribir sin cumplir todos los prerrequisitos directos del curso.
 * 2. No inscribir si hay cruce de horario con grupos activos del estudiante.
 * 3. Un mismo estudiante solo puede tener una inscripción activa por CURSO (no por grupo).
 * 4. Si el grupo tiene cupo: inscripción INSCRITO.
 * 5. Si no hay cupo: inscripción LISTA_ESPERA (FIFO por fechaSolicitud).
 * 6. Si es rechazada: estado RECHAZADA + registro persistido + Notificacion.
 */
@Service
public class InscribirEstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final GrupoRepository grupoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final NotificacionRepository notificacionRepository;
    private final ValidarPrerrequisitosService validarPrerrequisitosService;
    private final ValidarCruceHorarioService validarCruceHorarioService;
    private final GestionarListaEsperaService gestionarListaEsperaService;

    public InscribirEstudianteService(
            EstudianteRepository estudianteRepository,
            GrupoRepository grupoRepository,
            InscripcionRepository inscripcionRepository,
            NotificacionRepository notificacionRepository,
            ValidarPrerrequisitosService validarPrerrequisitosService,
            ValidarCruceHorarioService validarCruceHorarioService,
            GestionarListaEsperaService gestionarListaEsperaService) {
        this.estudianteRepository = estudianteRepository;
        this.grupoRepository = grupoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.notificacionRepository = notificacionRepository;
        this.validarPrerrequisitosService = validarPrerrequisitosService;
        this.validarCruceHorarioService = validarCruceHorarioService;
        this.gestionarListaEsperaService = gestionarListaEsperaService;
    }

    /**
     * Intenta inscribir al estudiante en el grupo dado.
     *
     * @return la Inscripcion resultante (INSCRITO o LISTA_ESPERA).
     * @throws PrerrequisitosNoCumplidosException si no cumple prerrequisitos.
     * @throws CruceHorarioException si hay cruce con horarios activos.
     * @throws InscripcionDuplicadaException si ya tiene una inscripción activa en el mismo curso.
     */
    @Transactional
    public Inscripcion inscribir(Long estudianteId, Long grupoId) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado: " + estudianteId));
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Grupo no encontrado: " + grupoId));
        Curso curso = grupo.getCurso();

        try {
            // Regla 3: una sola inscripción activa por CURSO (no por grupo)
            validarInscripcionUnicaPorCurso(estudiante, curso);

            // Regla 1: validar prerrequisitos
            validarPrerrequisitosService.validar(estudiante, curso);

            // Regla 2: validar cruce de horario
            validarCruceHorarioService.validar(estudiante, grupo);

        } catch (PrerrequisitosNoCumplidosException | CruceHorarioException | InscripcionDuplicadaException ex) {
            // Regla 6: rechazo -> persistir inscripcion RECHAZADA + Notificacion
            Inscripcion rechazada = new Inscripcion(estudiante, grupo, EstadoInscripcion.RECHAZADA);
            rechazada.setMotivoRechazo(ex.getMessage());
            inscripcionRepository.save(rechazada);

            Notificacion notif = new Notificacion(
                    estudiante, rechazada,
                    "Tu solicitud de inscripción al grupo " + grupo.getNumeroGrupo() +
                    " del curso '" + curso.getNombre() + "' fue rechazada. Motivo: " + ex.getMessage(),
                    TipoNotificacion.RECHAZO_INSCRIPCION
            );
            notificacionRepository.save(notif);

            throw ex; // re-lanzar para que el controlador devuelva error HTTP
        }

        // Regla 4/5: cupo disponible -> INSCRITO, sin cupo -> LISTA_ESPERA
        long inscritos = inscripcionRepository
                .findByGrupoIdAndEstado(grupo.getId(), EstadoInscripcion.INSCRITO).size();
        EstadoInscripcion estado;
        Integer posicion = null;

        if (inscritos < grupo.getCupoMaximo()) {
            estado = EstadoInscripcion.INSCRITO;
        } else {
            estado = EstadoInscripcion.LISTA_ESPERA;
            posicion = gestionarListaEsperaService.calcularPosicionListaEspera(grupo);
        }

        Inscripcion inscripcion = new Inscripcion(estudiante, grupo, estado);
        inscripcion.setPosicionListaEspera(posicion);
        return inscripcionRepository.save(inscripcion);
    }

    /**
     * Cancela una inscripción activa y dispara la promoción automática del primero en lista de espera.
     */
    @Transactional
    public void cancelar(Long inscripcionId) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada: " + inscripcionId));

        if (!inscripcion.isActiva()) {
            throw new IllegalStateException("Solo se pueden cancelar inscripciones activas (INSCRITO o LISTA_ESPERA)");
        }

        boolean eraInscrito = inscripcion.getEstado() == EstadoInscripcion.INSCRITO;
        Grupo grupo = inscripcion.getGrupo();

        inscripcion.setEstado(EstadoInscripcion.CANCELADA);
        inscripcionRepository.save(inscripcion);

        Notificacion notif = new Notificacion(
                inscripcion.getEstudiante(), inscripcion,
                "Tu inscripción al grupo " + grupo.getNumeroGrupo() +
                " del curso '" + grupo.getCurso().getNombre() + "' ha sido cancelada.",
                TipoNotificacion.CANCELACION_INSCRIPCION
        );
        notificacionRepository.save(notif);

        // Si era INSCRITO, libera un cupo → promover el primero en lista de espera
        if (eraInscrito) {
            gestionarListaEsperaService.promoverSiguienteEnListaDeEspera(grupo);
        }
    }

    private void validarInscripcionUnicaPorCurso(Estudiante estudiante, Curso curso) {
        List<Inscripcion> activas = inscripcionRepository.findByEstudianteId(estudiante.getId())
                .stream()
                .filter(i -> i.isActiva() && i.getGrupo().getCurso().getId().equals(curso.getId()))
                .toList();

        if (!activas.isEmpty()) {
            throw new InscripcionDuplicadaException(
                "El estudiante ya tiene una inscripción activa en el curso '" + curso.getNombre() + "'"
            );
        }
    }
}
