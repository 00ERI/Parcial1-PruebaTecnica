# BACKLOG.md — Sistema D. Matrícula académica

> Tareas iniciales de scaffolding, ordenadas para poder arrancar con Antigravity mientras se cierran
> los pendientes de negocio en `ASSUMPTIONS.md`. Cada tarea indica si está **bloqueada** por algún
> pendiente o si se puede hacer ya. Referencia de contexto: `AGENTS.md` (stack, modelo, reglas).

## Convenciones de este backlog

- `[ ]` sin empezar · `[~]` en progreso · `[x]` hecho
- **Bloqueada por**: referencia al ítem de `ASSUMPTIONS.md` → "Pendientes por confirmar" que hay que
  resolver antes de completar la tarea a fondo (no necesariamente para empezarla).

---

## Fase 0 — Infraestructura del proyecto (sin bloqueos)

- [x] Inicializar proyecto Spring Boot (Maven con Java 21)
- [x] Dependencias base: Spring Web, Spring Data JPA, driver PostgreSQL, H2, Validation, Spring Boot Test
- [x] Configurar conexión a PostgreSQL y perfiles `dev` / `test` (`application.yml`)
- [x] Crear estructura de paquetes acordada: `domain` / `application` / `infrastructure` / `interfaces`
- [x] Configurar migraciones versionadas (**Flyway** recomendado, dado que el modelo ya cambió varias
      veces y va a seguir cambiando)
- [x] Configurar OpenAPI/Swagger para documentar endpoints a medida que se crean
- [x] Configurar suite de tests (JUnit 5 + Mockito ya vienen con Spring Boot Test)

## Fase 1 — Entidades del dominio (sin bloqueos)

- [ ] `Usuario` (clase base) — **decisión provisional**: estrategia de herencia JPA `JOINED` para
      `Usuario → Estudiante / Administrador` (más flexible; se puede migrar después si hace falta).
      Marcar como decisión provisional, no definitiva, en el código (comentario o ADR corto).
- [ ] `Estudiante`
- [ ] `Administrador`
- [ ] `Curso` (incluye `porcentajeMinimoAprobacion`, default 60)
- [ ] `Grupo` (incluye `cupoMaximo`, default 30)
- [ ] `Horario`
- [ ] `Prerrequisito` + enum `TipoPrerrequisito` (`CURSO_APROBADO`, `MATRICULA_INSTITUCIONAL`)
- [ ] `Inscripcion` + enum `EstadoInscripcion` (`INSCRITO`, `LISTA_ESPERA`, `RECHAZADA`, `CANCELADA`)
- [ ] `HistorialAcademico`
- [ ] `Notificacion`
- [ ] Migraciones Flyway correspondientes a cada entidad

## Fase 2 — CRUD y validaciones de forma (sin bloqueos)

- [ ] CRUD `Curso` (crear, editar, listar, eliminar) + validación "al menos un `Prerrequisito` al
      guardar" (regla ya resuelta, incluyendo el caso `MATRICULA_INSTITUCIONAL` para cursos raíz)
- [ ] CRUD `Grupo` (incluye asignación de `Horario` — ver Fase 3 para el random inicial)
- [ ] CRUD `Horario`
- [ ] Validaciones de forma: campos obligatorios, `cupoMaximo > 0`, rangos de horario coherentes
      (`horaInicio < horaFin`)
- [ ] Endpoints protegidos para que solo `Administrador` pueda modificar horario/cupo/prerrequisitos/
      métrica de aprobación (autorización básica — sin niveles diferenciados todavía, ver Fase 5)

## Fase 3 — Lógica de negocio ya definida (sin bloqueos)

- [ ] `Horario.seCruzaCon(otro)` — comparación por día + solapamiento de rango horario (criterio ya
      definido en `AGENTS.md`)
- [ ] Servicio de validación de cruce de horarios entre todos los grupos donde el estudiante ya tiene
      inscripción activa, al momento de solicitar una nueva inscripción
- [ ] Generación de `Horario` aleatorio por defecto al crear un `Grupo`
- [ ] `Grupo.cuposDisponibles()`, `Grupo.tieneCupo()`, `Grupo.tamanoListaEspera()`
- [ ] **Consulta obligatoria**: endpoint de ocupación por grupo (cupos disponibles + tamaño de lista
      de espera) — conteo de `Inscripcion` por `estado`, agrupado por `Grupo`
- [ ] Flujo de lista de espera FIFO: al llenarse el cupo, nuevas solicitudes pasan a `LISTA_ESPERA`
      ordenadas por `fechaSolicitud`
- [ ] Promoción automática: al cancelar una `Inscripcion`, el primero de la lista de espera de ese
      grupo pasa a `INSCRITO`
- [ ] Flujo de rechazo: cambio de estado a `RECHAZADA` + registro persistido + creación de
      `Notificacion` (el envío real de la notificación puede quedar como stub/log por ahora)
- [ ] Regla "una sola inscripción activa por curso" (no por grupo) por estudiante

## Fase 4 — Stubs a implementar provisionalmente (dejar marcados como TODO)

- [ ] `puedeInscribir(curso)`: implementar **solo con prerrequisitos directos** como versión inicial
      (marcar TODO: revisar si deben ser transitivos — ver `ASSUMPTIONS.md`)
- [ ] Validación de `MATRICULA_INSTITUCIONAL`: implementar como `return true` para cualquier
      `Estudiante` registrado (marcar TODO: confirmar si requiere condiciones adicionales)
- [ ] Cálculo de "curso aprobado" contra `porcentajeMinimoAprobacion`: implementar asumiendo que el
      60% aplica sobre **nota final** (marcar TODO: confirmar unidad — nota / asistencia /
      evaluaciones)

## Fase 5 — Bloqueadas (esperar definición antes de implementar en firme)

- [ ] **Bloqueada por**: pendiente "¿prerrequisitos transitivos o solo directos?" — ajustar
      `puedeInscribir(curso)` si la respuesta es transitivos
- [ ] **Bloqueada por**: pendiente "unidad del 60% de aprobación" — ajustar el cálculo de
      `HistorialAcademico` / aprobación si no es sobre nota final
- [ ] **Bloqueada por**: pendiente "niveles de permiso entre `Administrador`" — hoy se asume que
      cualquier `Administrador` puede modificar todo; si hay roles diferenciados, hay que rediseñar
      la autorización de la Fase 2
- [ ] **Bloqueada por**: pendiente "condiciones adicionales para `MATRICULA_INSTITUCIONAL`" —
      reemplazar el stub de la Fase 4 si hace falta algo más que "estar registrado"
- [ ] **Bloqueada por**: pendiente "¿el horario aleatorio por defecto evita cruces entre grupos del
      mismo curso?" — definir si la generación aleatoria de la Fase 3 necesita esa validación extra

---

*Actualizar este backlog en cada sesión: mover tareas de Fase 5 a Fase 3/4 cuando se resuelva el
pendiente correspondiente, y registrar la sesión en `BITACORA-IA.md`.*
