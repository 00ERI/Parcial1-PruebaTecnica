AGENTS.md — Sistema D. Matrícula académica

Contexto que se le entrega al agente de programación (Antigravity) para trabajar sobre este proyecto. Este archivo se espera que crezca durante la semana: cada vez que el agente falle o produzca algo incorrecto por falta de contexto, la corrección se agrega aquí como una línea nueva, no se reescribe el historial. Ver la sección "Historial de contexto agregado" al final.

1. Herramientas de trabajo (decisión primaria)
Resolución de temas / diseño / dudas conceptuales: Claude (chat), en claude.ai.
Agente de programación (generación y edición de código sobre el repo): Antigravity.
Este archivo (AGENTS.md) es el que se le pasa como contexto a Antigravity. ASSUMPTIONS.md y BITACORA-IA.md documentan el trabajo hecho con Claude y las decisiones tomadas en el camino.
2. Descripción del sistema

Sistema D — Matrícula académica con cupos y prerrequisitos.

Un instituto técnico matricula estudiantes en cursos con cupo limitado y cadenas de prerrequisitos. Hoy el proceso se resuelve a mano y quedan inscripciones inválidas. El sistema debe automatizar y validar la matrícula.

Entidades mínimas (ver diagrama de clases v2 acordado en la sesión del 2026-09-17): Estudiante, Curso, Grupo (con Horario y cupo), Prerrequisito, Inscripción.

3. Reglas de negocio (invariantes que el sistema DEBE respetar)
No se puede inscribir un curso sin haber aprobado todos sus prerrequisitos.
Un estudiante no puede inscribir dos grupos cuyos horarios se cruzan (comparación por sesión de Horario, no por grupo completo — un grupo puede tener varias sesiones por semana).
Cuando un grupo llega al cupo, las solicitudes siguientes entran en lista de espera en orden de llegada (FIFO).
Si alguien cancela una inscripción, el primero de la lista de espera de ese grupo pasa a inscrito automáticamente.
Un mismo estudiante solo puede tener una inscripción activa por curso (no por grupo) — no puede quedar aceptado en dos grupos del mismo curso a la vez.
Si una solicitud de inscripción es rechazada: el estado pasa a RECHAZADA, se genera un registro (la inscripción rechazada queda persistida) y se envía una Notificacion al estudiante.
Horario, cupo, prerrequisitos y la métrica de aprobación son editables desde la interfaz por un Administrador (ver valores por defecto abajo). Un Estudiante no puede modificarlos.

Consulta obligatoria: Ocupación por grupo — cupos disponibles y tamaño de la lista de espera.

3.1 Valores por defecto (decisión del dueño del producto, 2026-09-17)
Grupo.horario: se asigna aleatorio al crear el grupo; editable luego por un Administrador.
Grupo.cupoMaximo: 30 por defecto; editable a más o menos según demanda.
Curso.prerrequisitos: ninguno por defecto — pero el sistema no permite guardar un curso sin al menos un prerrequisito asignado. Para cursos de primer nivel (sin curso previo real), el prerrequisito es de tipo MATRICULA_INSTITUCIONAL (estar matriculado en la institución), no CURSO_APROBADO. Ver Prerrequisito.tipo en el modelo (v4) y ASSUMPTIONS.md.
Curso.porcentajeMinimoAprobacion: 60% por defecto; editable por curso por un Administrador. Unidad exacta del 60% (nota / asistencia / evaluaciones) — sin definir todavía.
4. Modelo de dominio acordado (v4)

Clases: Usuario (superclase de Estudiante y Administrador), Curso, Grupo, Horario, Prerrequisito, TipoPrerrequisito (enum: CURSO_APROBADO, MATRICULA_INSTITUCIONAL), Inscripcion, EstadoInscripcion (enum: INSCRITO, LISTA_ESPERA, RECHAZADA, CANCELADA), HistorialAcademico, Notificacion.

Relaciones clave:

Usuario ← Estudiante, Usuario ← Administrador (herencia)
Curso 1 → * Grupo
Grupo 1 → 1..* Horario (un grupo puede tener varias sesiones semanales)
Estudiante 1 → * Inscripcion, Grupo 1 → * Inscripcion
Inscripcion 1 → 0..* Notificacion (notificación de rechazo, de paso de lista de espera, etc.)
Curso 1 → * Prerrequisito; Prerrequisito → 0..1 Curso (el curso requerido solo aplica cuando tipo = CURSO_APROBADO; para tipo = MATRICULA_INSTITUCIONAL no hay curso requerido — así se resuelve el caso de los cursos de primer nivel)
Estudiante 1 → * HistorialAcademico, Curso 1 → * HistorialAcademico
Administrador 1 → * Grupo, Administrador 1 → * Curso (configura horario, cupo, prerrequisitos, métrica de aprobación)

Para el detalle de atributos/métodos por clase, ver el diagrama de clases v4 generado con Claude (sesión 2026-09-17) y ASSUMPTIONS.md para las decisiones que no estaban en el enunciado original.

5. Convenciones para el agente de programación

Stack definido (2026-09-17): Java + Spring Boot + PostgreSQL. Decisión tomada por cercanía del equipo con este stack, por sobre otras opciones evaluadas (Django, NestJS, .NET) — ver BITACORA-IA.md, sesión 4, para el detalle de la comparación.

Lenguaje / framework: Java, Spring Boot.
Persistencia: PostgreSQL, acceso vía Spring Data JPA / Hibernate.
Mapeo con el diagrama de clases v4: entidades JPA (@Entity) para Usuario, Estudiante, Administrador (herencia — definir estrategia: SINGLE_TABLE, JOINED o TABLE_PER_CLASS, sin decidir aún), Curso, Grupo, Horario, Prerrequisito, Inscripcion, HistorialAcademico, Notificacion; enums de Java para EstadoInscripcion y TipoPrerrequisito.
No hay panel de administración "gratis" como en Django: la interfaz para que el Administrador edite horario, cupo, prerrequisitos y métrica de aprobación hay que construirla aparte (API + frontend, o alguna herramienta de admin para Spring si se decide usar una — sin definir todavía).
Estilo de nombres: en español, consistente con el dominio (Estudiante, Inscripcion, etc.) salvo que se decida lo contrario.
Toda regla de negocio (sección 3) debe quedar cubierta por al menos una prueba automatizada antes de darse por implementada.
Estructura de proyecto
/src/main/java/<paquete_base>
  /domain            # entidades del diagrama de clases (JPA @Entity), sin lógica de negocio pesada
    estudiante/
    curso/
    grupo/
    horario/
    prerrequisito/
    inscripcion/
    notificacion/
  /application        # casos de uso / servicios: aquí viven las reglas de negocio
    InscribirEstudianteService
    ValidarPrerrequisitosService
    ValidarCruceHorarioService
    GestionarListaEsperaService
    ConsultarOcupacionGrupoService
  /infrastructure     # repositorios Spring Data, configuración
    repositories/
    config/
  /interfaces         # controllers REST (y admin si aplica)
    api/
    admin/
/src/test/java/<paquete_base>
  unit/
  integration/

La separación domain / application busca que las reglas de negocio que siguen pendientes de definir (transitividad de prerrequisitos, unidad del 60% de aprobación, permisos de Administrador) se puedan ajustar sin tocar infrastructure ni interfaces.

6. Historial de contexto agregado

Cada entrada nueva es una línea de contexto que faltaba y que el agente necesitó para no volver a equivocarse. Formato: [FECHA] — contexto agregado — motivo (qué falló sin esto).

[2026-09-17] — Versión inicial de este archivo, a partir del diseño del diagrama de clases hecho con Claude. Sin incidentes de agente todavía (aún no arrancó la programación).
[2026-09-17] — Se agregó contexto sobre valores por defecto (horario aleatorio, cupo 30, prerrequisito obligatorio al crear curso, métrica de aprobación 60%), el rol Administrador, el flujo de rechazo con Notificacion, y la regla de una sola inscripción activa por curso. Motivo: el enunciado original no cubría quién configura estos valores ni qué pasa en un rechazo; sin este contexto el agente de programación habría tenido que inventar ese comportamiento.
[2026-09-17] — Se agregó Prerrequisito.tipo (CURSO_APROBADO / MATRICULA_INSTITUCIONAL) para los cursos de primer nivel de la malla. Motivo: la regla "todo curso necesita al menos un prerrequisito" era inconsistente con los cursos sin curso previo real; sin esta distinción el agente habría tenido que inventar una excepción no especificada, o habría bloqueado la carga inicial de cursos.
[2026-09-17] — Se definió el stack (Java + Spring Boot + PostgreSQL) y la estructura de proyecto base (domain / application / infrastructure / interfaces). Motivo: sin esto el agente de programación no tenía dónde empezar a escribir código ni cómo organizar las carpetas.