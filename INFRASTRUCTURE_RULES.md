# Infrastructure — Reglas de capa

## Responsabilidad

- Contiene adaptadores de entrada y salida: REST, persistencia, seguridad, integraciones externas y mensajería.
- Traduce entre el mundo externo y los contratos de `application`.
- No define reglas de negocio.

## Dependencias

- Puede depender de `application` y `domain`.
- No debe ser dependencia de `domain` ni `application`.

## Adaptadores de entrada

- Los controllers deben ser delgados: validan entrada superficial, convierten DTOs y llaman puertos de entrada.
- Los DTOs request/response pertenecen exclusivamente a infraestructura.
- Los códigos HTTP y detalles REST no deben filtrarse a `application` ni `domain`.
- `GlobalExceptionHandler` traduce errores internos a respuestas HTTP.

## Adaptadores de salida

- La persistencia implementa puertos de salida definidos en `application`.
- Las entidades o documentos de persistencia no deben reemplazar entidades de dominio.
- Los mappers deben dejar explícita la conversión entre modelos externos, persistencia y dominio.

## Seguridad

- JWT, filtros, providers y configuración de seguridad pertenecen a infraestructura.
- La infraestructura debe adaptar la identidad autenticada hacia abstracciones entendibles por `application`.

## Testing

- Las pruebas de infraestructura pueden usar Spring cuando validan wiring, REST, seguridad o persistencia.
- Los tests de adaptadores deben verificar la traducción correcta entre contratos externos e internos.
