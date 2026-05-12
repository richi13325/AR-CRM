# Boot — Reglas de capa

## Responsabilidad

- Contiene el punto de entrada de la aplicación y la composición final del sistema.
- Centraliza arranque, configuración global y ensamblado de módulos.
- No contiene reglas de negocio ni lógica de casos de uso.

## Dependencias

- Puede depender de `infrastructure`, `application` y `domain` para componer la aplicación.
- Ninguna otra capa debe depender de `boot`.

## Configuración

- La configuración debe conectar implementaciones concretas con puertos internos sin contaminar capas internas.
- Los perfiles, propiedades y beans globales viven acá o en configuración de infraestructura cuando corresponda.
- La clase principal debe mantenerse mínima.

## Testing

- Las pruebas de `boot` deben enfocarse en arranque, contexto y wiring general.
- No se debe usar `boot` para probar reglas de negocio.
