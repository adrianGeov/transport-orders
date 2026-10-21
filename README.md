# Transport Orders API

API REST para la gestión de órdenes de transporte de una empresa de movilidad: órdenes, conductores y asignación de conductores a órdenes con carga de documentos (PDF) e imágenes.

## Tecnologías

- Java 17
- Spring Boot 3.5 (Web, Data JPA, Validation, Security)
- PostgreSQL 16
- JWT (jjwt 0.12)
- Swagger / OpenAPI (springdoc)
- JUnit 5 + Mockito
- Docker y Docker Compose
- Logging estructurado en JSON (formato ECS)

## Requisitos

- Docker Desktop (con Docker Compose)
- Opcional, para ejecutar sin Docker: Java 17

## Ejecución con Docker (recomendado)

```bash
git clone https://github.com/adrianGeov/transport-orders.git
cd transport-orders
docker compose up -d --build
```

La API queda disponible en `http://localhost:8080`.

Para detenerla:

```bash
docker compose down
```

## Ejecución local (sin contenedor para la app)

1. Levantar solo la base de datos:
```bash
   docker compose up -d postgres
```
2. Ejecutar la aplicación:
```bash
   ./mvnw spring-boot:run        # Linux / macOS
   .\mvnw spring-boot:run        # Windows
```

La base de datos se expone en el puerto `15432` del host para evitar conflictos con instalaciones locales de PostgreSQL.

## Documentación (Swagger)

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Para probar desde Swagger: ejecutar `POST /api/v1/auth/login`, copiar el `accessToken`, presionar **Authorize** y pegar el token.

## Autenticación

Todos los endpoints, excepto el login y Swagger, requieren el header:

```
Authorization: Bearer <token>
```

Credenciales por defecto (configurables por variables de entorno):

| Usuario | Contraseña |
|---|---|
| `admin` | `admin123` |

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/auth/login` | Obtener token JWT |
| POST | `/api/v1/drivers` | Crear conductor |
| GET | `/api/v1/drivers/active` | Listar conductores activos |
| POST | `/api/v1/orders` | Crear orden |
| GET | `/api/v1/orders/{id}` | Consultar orden por ID |
| PATCH | `/api/v1/orders/{id}/status` | Cambiar estado de la orden |
| GET | `/api/v1/orders` | Listar órdenes con filtros |
| POST | `/api/v1/orders/{orderId}/assignment` | Asignar conductor a una orden |
| POST | `/api/v1/orders/{orderId}/assignment/document` | Agregar PDF a la asignación |
| POST | `/api/v1/orders/{orderId}/assignment/image` | Agregar imagen (.png, .jpg) a la asignación |

### Filtros de órdenes

Todos opcionales y combinables:

| Parámetro | Ejemplo | Descripción |
|---|---|---|
| `status` | `CREATED` | Estado exacto |
| `origin` | `mexico` | Coincidencia parcial, sin distinguir mayúsculas |
| `destination` | `puebla` | Coincidencia parcial, sin distinguir mayúsculas |
| `from` | `2026-10-01` | Fecha de creación desde (inclusive) |
| `to` | `2026-10-31` | Fecha de creación hasta (inclusive) |

```
GET /api/v1/orders?status=CREATED&origin=mexico&from=2026-10-01&to=2026-10-31
```

### Flujo de estados de una orden

```
CREATED ──► IN_TRANSIT ──► DELIVERED
   │                 │
   └──► CANCELLED ◄──┘
```

- `CREATED` → `IN_TRANSIT` o `CANCELLED`
- `IN_TRANSIT` → `DELIVERED` o `CANCELLED`
- `DELIVERED` y `CANCELLED` son estados finales

Una transición no permitida responde `422 Unprocessable Entity`.

### Reglas de asignación

- El conductor debe existir y estar activo.
- La orden debe existir y estar en estado `CREATED`.
- Una orden solo puede tener una asignación.
- Documento: solo PDF. Imagen: solo PNG o JPG. Tamaño máximo: 5 MB.

## Formato de errores

Todas las respuestas de error comparten la misma estructura y nunca exponen el stack trace:

```json
{
  "timestamp": "2026-10-20T21:40:12.345",
  "status": 400,
  "error": "Bad Request",
  "message": "La petición contiene campos inválidos",
  "path": "/api/v1/orders",
  "fieldErrors": [
    { "field": "origin", "message": "El origen es obligatorio" }
  ]
}
```

| Código | Cuándo |
|---|---|
| 400 | Validación fallida, JSON mal formado, parámetro inválido, archivo inválido |
| 401 | Token ausente, inválido o expirado; credenciales incorrectas |
| 404 | Orden o conductor inexistente |
| 409 | Licencia de conductor duplicada |
| 413 | Archivo mayor a 5 MB |
| 422 | Regla de negocio incumplida (transición inválida, conductor inactivo, etc.) |
| 500 | Error inesperado (mensaje genérico; el detalle queda en el log) |

## Logging

Los logs se emiten en JSON (formato ECS) para su indexación en herramientas como ELK o CloudWatch. Cada petición recibe un id de correlación que se incluye en todos sus logs y se devuelve en el header `X-Correlation-Id`. Si el cliente envía ese header, se reutiliza su valor.

## Pruebas

```bash
./mvnw test        # Linux / macOS
.\mvnw test        # Windows
```

No requieren base de datos. Cubren:

- Flujo de transiciones de estado
- Servicios de órdenes, conductores y asignaciones (JUnit 5 + Mockito)
- Validación y almacenamiento de archivos
- Generación y validación de JWT
- Capa web del controlador de órdenes (`@WebMvcTest`)

## Estructura del proyecto

```
src/main/java/com/prueba/transport_orders/
├── config/          Configuración de seguridad y OpenAPI
├── controller/      Controladores REST
├── domain/
│   ├── entity/      Entidades JPA (Order, Driver, Assignment)
│   └── enums/       OrderStatus con las reglas de transición
├── dto/             Objetos de entrada y salida con validaciones
├── exception/       Excepciones de negocio y manejador global
├── logging/         Filtro de id de correlación
├── mapper/          Mapeo manual entre entidades y DTOs
├── repository/      Repositorios Spring Data y Specifications para filtros
├── security/        Servicio JWT, filtro y punto de entrada de autenticación
└── service/         Interfaces de servicio e implementaciones
```

## Decisiones de diseño

- **Capas separadas** (controller → service → repository) y servicios definidos por interfaz, aplicando inversión de dependencias.
- **DTOs** para no exponer entidades JPA; validaciones con `jakarta.validation` (equivalente actual de `javax.validation`).
- **Reglas de transición en el enum `OrderStatus`**: la lógica del flujo vive en un solo lugar y es fácil de probar.
- **`Assignment` como entidad propia**, porque tiene datos que no pertenecen ni a la orden ni al conductor (fecha, PDF, imagen).
- **Specifications de JPA** para combinar filtros opcionales sin un método de repositorio por combinación.
- **Almacenamiento de archivos detrás de una interfaz** (`FileStorageService`): hoy en disco, reemplazable por S3 sin tocar la lógica de negocio. Se valida extensión y tipo de contenido, y el nombre en disco lo genera el sistema.
- **Tabla `orders`** en lugar de `order`, que es palabra reservada en SQL.

## Variables de entorno

| Variable | Default | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:15432/transport_db` | URL de la base de datos |
| `DB_USERNAME` | `postgres` | Usuario de la base de datos |
| `DB_PASSWORD` | `postgres` | Contraseña de la base de datos |
| `APP_USERNAME` | `admin` | Usuario de la API |
| `APP_PASSWORD` | `admin123` | Contraseña de la API |
| `JWT_SECRET` | valor de desarrollo | Secreto de firma JWT (mínimo 32 caracteres) |
| `JWT_EXPIRATION_MS` | `3600000` | Vigencia del token (1 hora) |
|