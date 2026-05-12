# Application — Reglas de capa

## Responsabilidad

- Coordina casos de uso mediante comandos, queries, servicios de aplicación y puertos.
- Orquesta el dominio, transacciones lógicas y dependencias externas mediante interfaces.
- No contiene detalles REST, JPA, JWT, frameworks web ni configuración de arranque.

## Dependencias

- Puede depender de `domain`.
- No puede depender de `infrastructure` ni de `boot`.
- Toda dependencia hacia el exterior debe expresarse como puerto en `port/out`.

## Casos de uso

- Los comandos representan intención de cambio de estado.
- Las queries representan intención de lectura.
- Los puertos de entrada definen contratos de uso de la aplicación.
- Los servicios de aplicación deben coordinar, no absorber reglas que pertenecen al dominio.

## Contratos

- Los modelos de entrada y salida de aplicación no deben ser DTOs REST ni entidades JPA.
- Los errores esperables deben expresarse con excepciones o resultados propios de la aplicación, no con excepciones HTTP.

## Testing

- Las pruebas deben mockear puertos de salida cuando corresponda.
- No se debe levantar Spring para probar lógica de aplicación salvo que se esté validando integración explícita.
