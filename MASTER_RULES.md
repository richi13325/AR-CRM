# CRM2 — Reglas maestras

Este archivo define reglas transversales del proyecto y delega reglas específicas a cada capa.

## Delegación por capa

- `DOMAIN_RULES.md`: reglas para entidades, value objects, enums y excepciones de dominio.
- `APPLICATION_RULES.md`: reglas para casos de uso, comandos, queries, puertos y servicios de aplicación.
- `INFRASTRUCTURE_RULES.md`: reglas para adaptadores REST, persistencia, seguridad, mensajería e integraciones externas.
- `BOOT_RULES.md`: reglas para arranque, configuración y composición de módulos.

## Arquitectura

- La dirección de dependencias debe respetar Clean Architecture: `boot -> infrastructure -> application -> domain`.
- `domain` no depende de Spring, JPA, Lombok experimental, frameworks web, bases de datos ni detalles externos.
- `application` depende de `domain`, pero no de `infrastructure` ni de `boot`.
- `infrastructure` implementa puertos definidos por `application`; no define reglas de negocio.
- `boot` sólo compone, configura y arranca la aplicación.

## Reglas generales de código

- Java 25, Spring Boot 4, Maven, JUnit y Mockito son el stack base.
- Lombok puede usarse para reducir boilerplate, pero no debe ocultar invariantes ni reglas de negocio.
- Los nombres deben expresar intención de negocio, no detalles técnicos accidentales.
- Las reglas de negocio deben estar en `domain` o coordinadas desde `application`, nunca en controllers, repositories, DTOs o configuración.
- No se deben filtrar DTOs, entidades JPA ni clases de framework hacia capas internas.
- Toda frontera entre capas debe convertir modelos explícitamente mediante mappers o ensambladores.

## Testing

- Las pruebas deben validar comportamiento observable, no detalles internos irrelevantes.
- `domain` y `application` deben poder probarse sin levantar Spring.
- Mockito se usa para dependencias externas o puertos, no para reemplazar lógica de dominio.

## Seguridad

- JWT y detalles de autenticación/autorización pertenecen a infraestructura y configuración.
- Las capas internas sólo deben conocer identidad, permisos o contexto de usuario mediante abstracciones propias.
