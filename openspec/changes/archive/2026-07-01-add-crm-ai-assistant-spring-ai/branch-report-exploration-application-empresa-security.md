# Exploración de rama: application empresa/security

Cambio OpenSpec: `add-crm-ai-assistant-spring-ai`  
Alcance: `application/src/main/java/com/ar/crm2/application/empresa/**`, `application/src/main/java/com/ar/crm2/application/security/ActorContext.java` y tests correspondientes bajo `application/src/test/java/com/ar/crm2/application/empresa/**`.

## Fuente revisada

- `git status --short` y `git diff HEAD` para los paths del alcance.
- Archivos nuevos/modificados en el working tree, incluyendo archivos no trackeados.
- No se ejecutaron tests.

## Inventario por área

### Empresa — puertos de entrada

- `application/src/main/java/com/ar/crm2/application/empresa/port/in/ActorEmpresaScopePort.java` — nuevo, no trackeado (`??`). Define el contrato inbound reutilizable para resolver o consultar el alcance de empresas de un actor sin acoplar consumidores al servicio concreto de Empresa.

### Empresa — puertos de salida

- `application/src/main/java/com/ar/crm2/application/empresa/port/out/FindEmpresasByCreadorPort.java` — nuevo (`A`). Expone una consulta granular para obtener empresas creadas/poseídas por un usuario.

### Empresa — casos de uso

- Sin clases de caso de uso nuevas o modificadas dentro del alcance revisado.

### Empresa — servicios

- `application/src/main/java/com/ar/crm2/application/empresa/service/ActorEmpresaScopeService.java` — nuevo (`A`). Implementa `ActorEmpresaScopePort`, coordina la lectura de empresas del actor y delega la decisión de autorización a la política de dominio `EmpresaPermitidaPolicy`.

### Seguridad — contexto de actor

- `application/src/main/java/com/ar/crm2/application/security/ActorContext.java` — modificado (`M`). Refuerza el contrato de que `ActorContext` representa identidad autenticada y no transporta autoridad tenant/empresa.

### Tests de empresa

- `application/src/test/java/com/ar/crm2/application/empresa/service/ActorEmpresaScopeServiceTest.java` — nuevo (`A`). Cubre comportamiento del servicio de alcance de empresas mediante un puerto out in-memory.

## Responsabilidades y métodos públicos

### `ActorEmpresaScopePort`

Responsabilidad: contrato inbound neutral, propiedad del bounded context Empresa, para que AI u otros módulos validen alcance de empresa sin depender del servicio concreto ni de tipos `application.ai.*`.

- `EmpresaId resolver(UUID actorUsuarioId, UUID empresaId)` — resuelve una empresa permitida para el actor. Si `empresaId` es explícito, debe pertenecer al actor; si es `null`, delega el fallback configurado por la política/servicio.
- `List<EmpresaId> empresasDelActor(UUID actorUsuarioId)` — devuelve todas las empresas poseídas por el actor para validaciones de pertenencia contra recursos que ya traen `empresaId`.

### `FindEmpresasByCreadorPort`

Responsabilidad: puerto outbound granular para leer empresas asociadas al usuario creador/propietario.

- `List<EmpresaId> findEmpresasByCreador(UsuarioId creadoPor)` — devuelve los `EmpresaId` de las empresas creadas por el usuario indicado; puede devolver lista vacía.

### `ActorEmpresaScopeService`

Responsabilidad: coordinador de aplicación de Empresa. Convierte `UUID actorUsuarioId` a `UsuarioId`, consulta empresas poseídas mediante `FindEmpresasByCreadorPort` y delega la selección/validación en `EmpresaPermitidaPolicy`. Lanza `IllegalArgumentException` para actor nulo y deja propagar `TenantScopeViolationException` como excepción neutral.

- `EmpresaId resolver(UUID actorUsuarioId, UUID empresaId)` — valida que el actor exista, carga empresas poseídas y retorna la empresa seleccionada/permitida; rechaza empresa ajena o actor sin empresas vía excepción de tenant.
- `List<EmpresaId> empresasDelActor(UUID actorUsuarioId)` — valida que el actor exista y retorna la lista completa de empresas poseídas.

### `ActorContext`

Responsabilidad: record inmutable de identidad autenticada de aplicación, sin dependencias de Spring Security/JWT/Keycloak en las capas application/domain. La modificación documenta que el tenant no se transporta en este contexto.

- Accessors del record: `subject()`, `username()`, `email()`, `usuarioId()`, `superUsuarioId()`, `roles()` — exponen claims/roles ya validados por infraestructura.
- `boolean hasRole(String role)` — verifica si el actor posee un rol de realm.
- `boolean isSuperUsuario()` — indica si el actor tiene `superUsuarioId` presente.

## Soporte al AI assistant mediante scoping de actor/empresa

Este slice aporta la pieza de aplicación que separa identidad de autorización tenant:

- `ActorContext.usuarioId` queda como ancla de identidad del actor autenticado; `ActorContext` no contiene ni autoriza `empresaId`.
- `ActorEmpresaScopePort` permite que el bounded context AI dependa de un contrato estable de Empresa, no de una clase concreta ni de un adaptador específico de AI.
- `ActorEmpresaScopeService` deriva las empresas permitidas desde `Empresa.creadoPor == ActorContext.usuarioId` a través del puerto out, y usa una política de dominio para aceptar o rechazar una empresa solicitada.
- Para flujos resource-first del asistente, `empresasDelActor(actorUsuarioId)` permite verificar que la `empresaId` del recurso cargado pertenece al actor antes de leer/escribir estado AI.
- Para flujos sin recurso ancla, `resolver(actorUsuarioId, empresaId)` soporta selección explícita de tenant, dejando que el consumidor traduzca `TenantScopeViolationException` a su excepción pública.

La dirección general es correcta para la rama AI: la identidad viaja en `ActorContext`, la autoridad de empresa se resuelve por endpoint/servicio y el módulo AI no necesita importar implementación concreta de Empresa.

## Resumen de cobertura de tests

`ActorEmpresaScopeServiceTest` cubre estos comportamientos principales:

- Resolución sin `empresaId` explícito devuelve la primera empresa poseída por el actor.
- Resolución con `empresaId` explícito válido devuelve esa empresa.
- Resolución con `empresaId` ajeno rechaza con `TenantScopeViolationException` neutral.
- Actor sin empresas rechaza al resolver tenant.
- `actorUsuarioId == null` se rechaza con `IllegalArgumentException`.
- `empresasDelActor(...)` devuelve la lista completa cuando el actor tiene empresas.
- `empresasDelActor(...)` devuelve lista vacía cuando el actor no tiene empresas.

## Riesgos y brechas

- `ActorEmpresaScopePort.java` está no trackeado. Si no se agrega al control de versiones, `ActorEmpresaScopeService` queda importando un contrato ausente en la rama revisada.
- El fallback `resolver(actorUsuarioId, null)` devuelve la primera empresa incluso cuando el actor tiene más de una empresa, y el test actual fija ese comportamiento. Esto es riesgoso para endpoints AI sin recurso ancla si se espera rechazo por ambigüedad multiempresa.
- La brecha anterior es aceptable solo si los consumidores AI usan `empresasDelActor(...)` para flujos resource-first y exigen `empresaId` explícito —o una validación de “exactamente una empresa”— para listados sin recurso ancla.
- No hay test en este slice que valide explícitamente la regla “multiempresa sin empresa explícita debe ser ambiguo”; si esa regla pertenece al consumidor AI, debe quedar cubierta en tests de AI/inbound.
- `ActorContext` solo cambió contrato/documentación; cualquier garantía efectiva depende de que los mappers/controladores no reintroduzcan `empresaId` como autoridad implícita fuera de este slice.

## Recomendación

Mantener esta separación Empresa/security, pero revisar el contrato de fallback antes de cerrar PR4: para el asistente AI resource-first, el uso seguro es validar recursos mediante `empresasDelActor(...)` y evitar `resolver(..., null)` en actores multiempresa. Si el producto requiere multiempresa real, la ambigüedad debe rechazarse en el consumidor o moverse a una política explícita.

## Estado para continuación

Listo para que el orquestador consolide el reporte de rama. No se requiere modificar código desde esta exploración; los riesgos deben alimentar la revisión de PR4 y/o la fase de verificación correspondiente.
