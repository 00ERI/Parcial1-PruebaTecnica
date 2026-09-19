# Registro de Pruebas Automatizadas — Sistema D. Matrícula Académica

> Este documento registra cada prueba automatizada desarrollada y ejecutada en el proyecto, detallando su propósito, clase de prueba, tipo y estado actual.
> Se actualiza al completar cada fase del desarrollo.

---

## Resumen de Ejecución

- **Herramienta de ejecución**: Maven Wrapper (`.\mvnw.cmd test`)
- **Framework de pruebas**: JUnit 5 + AssertJ + Mockito + Spring Boot Test
- **Perfil activo de pruebas**: `test` (Base de datos H2 en memoria con migraciones Flyway automáticas)
- **Total de pruebas ejecutadas**: 11
- **Pruebas exitosas**: 11 (100%)
- **Fallos / Errores**: 0
- **Tiempo promedio de ejecución**: ~18 segundos

---

## Detalle de Pruebas por Suite

### 1. Carga de Contexto e Infraestructura (`MatriculaApplicationTests`)

Ubicación: `src/test/java/com/instituto/matricula/MatriculaApplicationTests.java`  
Tipo: **Integración / Contexto Spring Boot**

| # | Método de Prueba | Propósito / Regla Validada | Estado |
|---|------------------|----------------------------|--------|
| 1 | `contextLoads()` | Verifica que el contexto completo de Spring Boot levante correctamente, que Flyway ejecute las migraciones de esquema (`V1` y `V2`) y que Hibernate valide los mapeos JPA (`ddl-auto: validate`) sin inconsistencias. | **PASÓ** |

---

### 2. Pruebas Unitarias de Dominio — Horarios (`HorarioUnitTest`)

Ubicación: `src/test/java/com/instituto/matricula/unit/HorarioUnitTest.java`  
Tipo: **Unitaria (Lógica pura de dominio)**

| # | Método de Prueba | Propósito / Regla Validada | Estado |
|---|------------------|----------------------------|--------|
| 2 | `debeDetectarCrucePorSolapamientoParcial()` | Valida la regla de cruce de horarios cuando dos sesiones en el mismo día se traslapan parcialmente en el tiempo (ej. 08:00–10:00 vs. 09:00–11:00). | **PASÓ** |
| 3 | `debeDetectarCrucePorContencionTotal()` | Valida detección de cruce cuando un bloque horario abarca por completo al otro en el mismo día (ej. 07:00–12:00 vs. 08:00–10:00). | **PASÓ** |
| 4 | `noDebeHaberCruceSiSonContiguos()` | Valida que **no** haya cruce cuando un horario termina exactamente en el mismo instante en que empieza el siguiente (ej. 08:00–10:00 y 10:00–12:00). | **PASÓ** |
| 5 | `noDebeHaberCruceSiSonDiasDistintos()` | Valida que sesiones en días distintos de la semana no generen cruce aunque compartan el mismo rango de horas. | **PASÓ** |
| 6 | `debeLanzarExcepcionPorHorarioIncoherente()` | Valida la invariante de integridad: `horaInicio` debe ser estrictamente anterior a `horaFin`, lanzando `IllegalArgumentException` si son iguales o invertidas. | **PASÓ** |

---

### 3. Pruebas de Persistencia e Integración JPA (`DomainEntitiesPersistenceTest`)

Ubicación: `src/test/java/com/instituto/matricula/integration/DomainEntitiesPersistenceTest.java`  
Tipo: **Integración con base de datos (Spring Data JPA + Flyway)**

| # | Método de Prueba | Propósito / Regla Validada | Estado |
|---|------------------|----------------------------|--------|
| 7 | `debePersistirEstudianteYAdministradorConHerencia()` | Valida la estrategia de herencia JPA `JOINED` para la tabla base `usuarios` y sus subtipos `estudiantes` y `administradores`. | **PASÓ** |
| 8 | `debePersistirCursoConPrerrequisitoInstitucional()` | Valida la creación de un `Curso` con porcentaje de aprobación por defecto del 60% y un `Prerrequisito` de tipo `MATRICULA_INSTITUCIONAL` (sin curso previo requerido, para cursos raíz). | **PASÓ** |
| 9 | `debePersistirCursoConPrerrequisitoCursoAprobado()` | Valida la asociación de prerrequisito de tipo `CURSO_APROBADO` entre un curso principal y su curso requerido previo. | **PASÓ** |
| 10 | `debePersistirGrupoYHorarios()` | Valida la persistencia de un `Grupo` con cupo máximo por defecto de 30 y la relación `1..*` con múltiples sesiones de `Horario`. | **PASÓ** |
| 11 | `debePersistirInscripcionHistorialYNotificacion()` | Valida el ciclo de vida de persistencia para `Inscripcion` (estados del enum), registro de `HistorialAcademico` y emisión de `Notificacion`. | **PASÓ** |

---

## Cómo reproducir las pruebas

Ejecutar en la terminal desde la raíz del proyecto:

```powershell
.\mvnw.cmd test
```

Para ejecutar una clase de prueba específica:

```powershell
# Solo pruebas unitarias de horario
.\mvnw.cmd test -Dtest=HorarioUnitTest

# Solo pruebas de persistencia JPA
.\mvnw.cmd test -Dtest=DomainEntitiesPersistenceTest
```
