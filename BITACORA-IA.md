BITACORA-IA.md — Sistema D. Matrícula académica

Una entrada corta por sesión de trabajo con IA: qué se pidió, qué propuso el agente, qué se aceptó o rechazó (y por qué), y qué quedó sin verificar. Herramientas: Claude (chat, para diseño y resolución de temas) y Antigravity (agente de programación, para código).

Sesión 1 — 2026-09-17 — Claude (chat)

Qué pedí: Diagrama de clases UML para el Sistema D (matrícula académica con cupos y prerrequisitos), a partir del enunciado con entidades mínimas y 4 reglas de negocio + 1 consulta obligatoria.

Qué propuso el agente:

v1: Diagrama con Estudiante, Curso, Grupo (horario embebido como atributos), Inscripcion, HistorialAcademico, EstadoInscripcion (enum). Prerrequisito modelado como relación reflexiva simple entre Curso y Curso.
Al pedir una revisión más rigurosa contra el enunciado, propuso v2: separó Horario como clase propia (Grupo 1 → 1..* Horario) y Prerrequisito como clase de asociación explícita entre dos Curso, en vez de una relación directa sin atributos.

Qué acepté / rechacé y por qué:

Rechacé la v1 en lo referente a Prerrequisito y Horario: el enunciado los lista como entidades mínimas, así que no debían quedar disueltos en atributos o relaciones sin nombre.
Acepté la v2 completa: modela mejor la regla de cruce de horarios (permite grupos con varias sesiones semanales) y deja trazabilidad explícita de qué curso exige qué prerrequisito.
Acepté mantener HistorialAcademico aunque no esté en el enunciado, porque es indispensable para poder validar la regla de prerrequisitos aprobados (ver ASSUMPTIONS.md #3).

Qué quedó sin verificar:

Si los prerrequisitos deben evaluarse de forma transitiva (cadena completa) o solo directa.
Si hace falta un estado adicional para inscripciones rechazadas por prerrequisitos no cumplidos.
Definición de "curso aprobado" (nota mínima) — no discutida aún.
No se generó todavía ningún diagrama de secuencia ni código; el agente de programación (Antigravity) todavía no participó en esta iteración.

Tokens de la sesión: ver TOKENS.md (sin dato real disponible desde esta interfaz — pendiente de registrar manualmente o desde la consola correspondiente).

Sesión 2 — 2026-09-17 — Claude (chat)

Qué pedí: Incorporar decisiones de negocio propias (dueño del producto) sobre horario editable con default aleatorio, prerrequisitos obligatorios al crear curso (sin default), cupo con default de 30, flujo de rechazo de solicitud (notificación + cambio de estado + registro), una sola inscripción activa por curso por estudiante, y métrica de aprobación configurable con default de 60%.

Qué propuso el agente:

Diagrama de clases v3: agregó Usuario como superclase de Estudiante y Administrador, estado RECHAZADA en EstadoInscripcion, clase Notificacion ligada a Inscripcion, atributo porcentajeMinimoAprobacion en Curso.
Actualizó AGENTS.md (reglas de negocio 5-7, sección de valores por defecto, historial de contexto agregado) y ASSUMPTIONS.md (tabla de decisiones confirmadas, sección de inconsistencia detectada, pendientes actualizados).
Señaló una inconsistencia: no se puede crear un curso sin prerrequisito, pero eso bloquea los cursos de primer nivel de la malla.

Qué acepté / rechacé y por qué:

Acepté el modelo v3 completo tal como se propuso.
Quedó pendiente de mi parte resolver la inconsistencia de los cursos de primer nivel (el agente no la resolvió por su cuenta, correctamente, porque no estaba especificado).

Qué quedó sin verificar:

Cómo se resuelve la creación de cursos raíz sin prerrequisito real.
Unidad exacta del 60% de aprobación (nota, asistencia, evaluaciones).
Niveles de permiso entre distintos Administrador (si los hay).
Si el horario aleatorio por defecto ya evita cruces entre grupos del mismo curso o puede generarlos.

Tokens de la sesión: ver TOKENS.md.

Sesión 3 — 2026-09-17 — Claude (chat)

Qué pedí: Resolver la inconsistencia señalada en la sesión anterior sobre los cursos de primer nivel sin prerrequisito real: para esos casos, el único prerrequisito es estar matriculado en la institución académica.

Qué propuso el agente:

Diagrama de clases v4: agregó Prerrequisito.tipo (enum TipoPrerrequisito: CURSO_APROBADO, MATRICULA_INSTITUCIONAL) y cambió la multiplicidad de Prerrequisito → Curso de 1 a 0..1, para que un prerrequisito de tipo MATRICULA_INSTITUCIONAL no necesite referenciar otro curso.
Actualizó AGENTS.md y ASSUMPTIONS.md, marcando la inconsistencia anterior como resuelta.

Qué acepté / rechacé y por qué:

Acepté el cambio completo; resuelve el bloqueo de la carga inicial de cursos sin inventar una excepción no pedida.

Qué quedó sin verificar:

Si MATRICULA_INSTITUCIONAL se valida automáticamente (cualquier Estudiante registrado la cumple) o si requiere alguna verificación adicional (ej. matrícula al día, sin deudas).
Unidad exacta del 60% de aprobación (nota, asistencia, evaluaciones) — sigue pendiente de sesiones anteriores.
Niveles de permiso entre distintos Administrador — sigue pendiente.

Tokens de la sesión: ver TOKENS.md.

Sesión 4 — 2026-09-17 — Claude (chat)

Qué pedí: Recomendaciones de lenguaje/framework y estructura de proyecto para empezar a programar, antes de cerrar los pendientes de negocio.

Qué propuso el agente:

Comparó 4 opciones: Django+PostgreSQL, Spring Boot+PostgreSQL, NestJS+TypeScript+Prisma, .NET+EF Core, con ventajas/desventajas de cada una para este proyecto en particular (peso de la interfaz de administración, fidelidad al diagrama de clases, boilerplate).
Recomendó Django por el admin gratuito, o Spring Boot si se prioriza fidelidad al modelo UML.
Propuso una estructura de proyecto en capas (domain / application / infrastructure / interfaces) aplicable a cualquiera de las opciones.

Qué acepté / rechacé y por qué:

Elegí Java + Spring Boot + PostgreSQL, por cercanía del equipo con el stack — no por las razones técnicas que priorizó el agente (admin gratuito de Django), sino por curva de aprendizaje del equipo real.
Acepté la estructura de proyecto en capas propuesta, adaptada a paquetes de Java.

Qué quedó sin verificar:

Estrategia de herencia JPA para Usuario → Estudiante/Administrador (SINGLE_TABLE, JOINED o TABLE_PER_CLASS) — no decidida.
Cómo se va a construir la interfaz de administración (no hay panel gratuito como en Django).
Siguen pendientes de sesiones anteriores: transitividad de prerrequisitos, unidad del 60%, permisos de Administrador, validación de MATRICULA_INSTITUCIONAL.