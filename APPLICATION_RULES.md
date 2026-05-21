# Application — Reglas de capa

## Responsabilidad

- Coordina casos de uso mediante comandos, queries, servicios de aplicación y puertos.
- Orquesta el dominio, transacciones lógicas y dependencias externas mediante interfaces.
- No contiene detalles REST, JPA, JWT, frameworks web ni configuración de arranque.

## Organización por entidad

- La capa `application` debe organizarse por entidad/agregado o contexto funcional.
- Dentro de cada carpeta de entidad se deben crear las carpetas:
  - `command`: modelos de entrada para casos de uso que cambian estado.
  - `port/in`: puertos de entrada de la aplicación.
  - `service`: implementaciones de los casos de uso.
  - `port/out`: contratos de salida hacia persistencia, servicios externos o módulos internos.
- No se debe usar una carpeta global genérica para mezclar comandos, puertos o servicios de entidades distintas cuando el caso pertenece claramente a una entidad.
- Ejemplo conceptual:

```text
application
└── order
    ├── command
    ├── port
    │   └── in
    │       ├── AddItemToOrderUseCase
    │       ├── CancelOrderUseCase
    │       ├── CreateOrderUseCase
    │       └── PayOrderUseCase
    └── service
        ├── AddItemToOrderService
        ├── CancelOrderService
        ├── CreateOrderService
        └── PayOrderService
```

## Dependencias

- Puede depender de `domain`.
- No puede depender de `infrastructure` ni de `boot`.
- Toda dependencia hacia el exterior debe expresarse como puerto en `port/out`.

## Casos de uso

- Los comandos representan intención de cambio de estado.
- Las queries representan intención de lectura.
- Los puertos de entrada definen contratos de uso de la aplicación y deben ubicarse en la carpeta `port/in`.
- Cada caso de uso debe exponer un solo método público en su interfaz, siguiendo Clean Architecture.
- Las interfaces de entrada deben nombrarse con sufijo `UseCase`.
- Las implementaciones deben nombrarse con sufijo `Service`.
- Cada acción/caso de uso debe tener su propia interfaz `UseCase` y su propia implementación `Service` en archivos separados.
- No se deben agrupar varias acciones de una misma entidad en un único service con múltiples métodos.
- Cada `UseCase`, `Service`, `Command` y contrato de salida (`Port`, `Gateway` o `Service`) debe tener una única responsabilidad y un único método de uso principal.
- Los servicios de aplicación deben coordinar, no absorber reglas que pertenecen al dominio.

## Puertos de salida

- Los puertos de salida deben ubicarse en `port/out` dentro de la carpeta de la entidad correspondiente.
- El nombre del contrato debe expresar exactamente qué tipo de dependencia abstrae.

### Sufijo `*Port` — Persistencia y base de datos local

- Propósito: abstraer operaciones de lectura o escritura en la base de datos principal del sistema.
- Regla de diseño: aplicar el Principio de Segregación de Interfaces (ISP), creando interfaces granulares y específicas para cada caso de uso en lugar de repositorios genéricos.
- Ejemplos: `SaveOrderPort`, `FindOrderByIdPort`, `UpdateProductStockPort`.

### Sufijo `*Gateway` — APIs y servicios de terceros

- Propósito: abstraer comunicación con plataformas externas, SaaS o proveedores de infraestructura que no controlamos.
- Regla de diseño: definir contratos basados en las necesidades de la aplicación, aislando el dominio de cambios en APIs externas.
- Ejemplos: `PaymentGateway`, `SmsNotificationGateway`, `CloudStorageGateway`.

### Sufijo `*Service` — Ecosistema propio u otros módulos

- Propósito: comunicar el caso de uso con otros bounded contexts, módulos internos o microservicios propios.
- Regla de diseño: usar cuando se requiere consultar o delegar lógica de negocio a otra entidad del sistema que maneja su propio dominio técnico.
- Ejemplos: `InventoryService`, `BillingService`, `CustomerIdentityService`.

## Contratos

- Los modelos de entrada y salida de aplicación no deben ser DTOs REST ni entidades JPA.
- Los errores esperables deben expresarse con excepciones o resultados propios de la aplicación, no con excepciones HTTP.

## Testing

- Las pruebas deben mockear puertos de salida cuando corresponda.
- No se debe levantar Spring para probar lógica de aplicación salvo que se esté validando integración explícita.
