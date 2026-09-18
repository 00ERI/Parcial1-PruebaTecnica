# ASSUMPTIONS.md — Sistema D. Matrícula académica

> Todo lo que el enunciado no dice explícitamente y que se resolvió por cuenta propia durante el
> diseño (con Claude, en chat). Se actualiza cada vez que aparece un nuevo vacío en el enunciado que
> haya que decidir, en cualquier sesión de trabajo.

## Sesión 2026-09-17 — Diseño del diagrama de clases

| # | Supuesto | Por qué fue necesario | Alternativa descartada |
|---|----------|------------------------|--------------------------|
| 1 | `Prerrequisito` se modela como clase de asociación explícita (`Curso ↔ Curso`), con atributo `obligatorio` | El enunciado la nombra como entidad mínima, no como una simple relación implícita | Relación reflexiva directa `Curso *-- * Curso` sin clase propia (descartada en la v1 del diagrama, corregida en la v2) |
| 2 | `Horario` es una clase propia, y `Grupo` puede tener **una o más** sesiones de horario (`1..*`) | El enunciado no aclara si un grupo se dicta una sola vez por semana o varias (ej. lunes/miércoles/viernes). Se asumió lo más realista para que la validación de cruce de horarios sea correcta | Horario como atributos sueltos dentro de `Grupo` (un solo bloque por grupo) — v1 del diagrama |
| 3 | Existe una clase `HistorialAcademico` (curso aprobado, nota, fecha) por estudiante | Es indispensable para poder validar la regla "no inscribir sin prerrequisitos aprobados", pero el enunciado no la menciona como entidad | Guardar el estado de aprobación directamente en `Inscripcion` (se descartó porque mezclaría el historial permanente con inscripciones puntuales por periodo) |
| 4 | El cruce de horarios se define como: mismo día de la semana **y** solapamiento de rango horario, comparado sesión por sesión entre todos los grupos donde el estudiante ya tiene una inscripción activa | El enunciado dice "cuyos horarios se cruzan" sin definir el criterio exacto | — |
| 5 | El orden de la lista de espera es estrictamente FIFO por `fechaSolicitud` (con `posicionListaEspera` como campo derivado/cacheado) | El enunciado sí pide "orden de llegada", pero no dice cómo se persiste ese orden | Usar solo el orden de inserción en la tabla sin campo explícito (se descartó por ser frágil ante ediciones o migraciones de datos) |
| 6 | `EstadoInscripcion` tiene exactamente 3 valores: `INSCRITO`, `LISTA_ESPERA`, `CANCELADA` | El enunciado no define estados; se dedujeron de las reglas de negocio descritas | Agregar un estado `RECHAZADA` para el caso "no cumple prerrequisitos" (queda abierto, ver sección de pendientes) |
| 7 | "Aprobar todos los prerrequisitos" significa el 100% de los prerrequisitos directos del curso, no un porcentaje ni prerrequisitos transitivos calculados automáticamente | El enunciado dice "todos sus prerrequisitos" en singular sin matices | Cadena transitiva completa (si A requiere B y B requiere C, ¿inscribir A exige tener C también?) — no se asumió, queda como pendiente por confirmar |
| 8 | No se modelan `Docente` ni `Aula` como entidades | No están en las "entidades mínimas" del enunciado ni son necesarios para las 4 reglas de negocio ni la consulta obligatoria | Se consideró agregarlas por completitud de un sistema real, pero se descartó para no ampliar el alcance sin pedido explícito |
| 9 | `Grupo.cupoMaximo` es un entero fijo por grupo, sin manejo de cupos especiales (ej. reservados, becas) | No mencionado en el enunciado | — |

## Sesión 2026-09-17 (continuación) — Decisiones de negocio confirmadas por el cliente

> A diferencia de la tabla anterior, esto no son supuestos míos: son decisiones tomadas explícitamente
> por el dueño del producto. Quedan igual documentadas acá porque el enunciado original no las
> contemplaba y ahora son parte del comportamiento esperado del sistema.

| # | Decisión | Resuelve el pendiente # |
|---|----------|---------------------------|
| 10 | El horario de cada grupo se asigna con un valor por defecto **aleatorio** al crear el grupo, y puede ser modificado luego por un usuario `Administrador` desde la interfaz | — |
| 11 | Un curso se crea **sin** prerrequisitos por defecto, pero el sistema **no permite guardar** un curso sin al menos un prerrequisito asignado | — (ver inconsistencia marcada abajo) |
| 12 | `Grupo.cupoMaximo` tiene un valor por defecto de **30**, editable a más o menos cupos según demanda, por un `Administrador` | — |
| 13 | Cuando una solicitud de inscripción es rechazada: el estado de la `Inscripcion` pasa a `RECHAZADA`, se genera un registro (la propia `Inscripcion` rechazada, con fecha) y se crea una `Notificacion` dirigida al estudiante | Pendiente anterior "¿qué pasa con una solicitud que no cumple prerrequisitos?" — **resuelto** |
| 14 | Un mismo estudiante solo puede tener **una inscripción activa por curso** (no por grupo) — evita quedar aceptado en dos grupos del mismo curso a la vez | Pendiente anterior "¿puede tener dos inscripciones activas al mismo curso en grupos distintos?" — **resuelto: no** |
| 15 | El porcentaje mínimo de asistencia/avance para considerar un curso aprobado (`porcentajeMinimoAprobacion`) es configurable por un `Administrador`, por curso, con valor por defecto de **60%** | Pendiente anterior "definición exacta de curso aprobado" — **resuelto en cuanto a mecanismo**; sigue sin definirse si el 60% es sobre nota, asistencia u otra métrica (ver pendientes) |

## Inconsistencia detectada — resuelta 2026-09-17

- **Decisión #11 vs. cursos de nivel inicial**: si ningún curso puede crearse sin al menos un
  prerrequisito, no había forma de dar de alta los cursos de primer nivel de una malla curricular.
- **Resolución (decisión del cliente)**: para los cursos de primer nivel, el único prerrequisito es
  **estar matriculado en la institución** (no otro curso). Esto se modela con un nuevo atributo
  `Prerrequisito.tipo` (enum `TipoPrerrequisito`: `CURSO_APROBADO` | `MATRICULA_INSTITUCIONAL`). Un
  `Prerrequisito` de tipo `MATRICULA_INSTITUCIONAL` no referencia a ningún `Curso` (la asociación
  `Prerrequisito → Curso` pasa a ser `0..1`, no `1`). Así todo curso sigue teniendo al menos un
  `Prerrequisito` registrado, sin necesitar un curso previo real. Diagrama v4.

## Pendientes por confirmar con el cliente / negocio

- [ ] ¿Los prerrequisitos son transitivos (cadena completa) o solo directos? (supuesto #7)
- [x] ~~Cómo se resuelve la inconsistencia de la decisión #11 con los cursos sin prerrequisito real~~
      — resuelto 2026-09-17: prerrequisito de tipo `MATRICULA_INSTITUCIONAL` para cursos raíz.
- [ ] La métrica de aprobación (decisión #15, default 60%) — ¿60% de qué exactamente? ¿Nota sobre 100,
      asistencia, evaluaciones aprobadas? El enunciado y la decisión del cliente hablan de "60% del
      curso aprobado" pero no precisan la unidad.
- [ ] ¿Los `Administrador` tienen distintos niveles de permiso (por ejemplo, quién puede tocar cupos
      vs. quién puede tocar prerrequisitos) o cualquier administrador puede modificar todo?
- [ ] ¿El horario aleatorio por defecto respeta ya las reglas de no-cruce entre los grupos de un mismo
      curso, o puede generar cruces que luego el administrador debe corregir a mano?

*(Actualizar esta tabla y la de pendientes en cada sesión que aporte una decisión nueva, no solo en
la de diseño del modelo.)*
