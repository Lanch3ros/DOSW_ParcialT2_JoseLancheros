# ECIXPRESS — Sistema de Pedidos de Cafetería

**Estudiantes:** Jose Luis Lancheros Ayora, Dana Valeria Leal y Juan Sebastian Murcia Yanquen
**Grupo:** 1
**Materia:** Desarrollo y Operaciones de Software
**Fecha:** Abril 2026

---

## Descripción

ECIXPRESS es un backend REST para la gestión de pedidos de la cafetería universitaria. Permite a los clientes registrarse, autenticarse, consultar productos por código QR y crear/cancelar pedidos. La señora de la cafetería puede cambiar el estado de los pedidos (CREADO → EN_PREPARACION → ENTREGADO).

**Stack:** 
- Java 21 
- Spring Boot 4.0.5
- Spring Security (JWT)
- MapStruct
- JPA/PostgreSQL
- Springdoc OpenAPI

---

## Levantar el proyecto

### 1. Base de datos (Docker)

```bash
docker compose up -d
```

### 2. Aplicación

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8081`.
Swagger UI: `http://localhost:8081/swagger-ui.html`

---

## Arquitectura y estructura

```
src/main/java/edu/dosw/parcial/
├── config/          # SecurityConfig, CorsConfig, SwaggerConfig
├── controller/
│   ├── dtos/        # Request y Response DTOs
│   ├── handlers/    # GlobalExceptionHandler
│   └── mappers/     # MapStruct: UserMapper, ProductMapper, OrderMapper, OrderItemMapper
├── core/
│   ├── models/      # Enums: OrderStatus, ProductStatus, UserRole
│   ├── services/    # AuthService, OrderService, ProductService
│   ├── utils/       # JwtUtil
│   └── validators/  # StockValidator, OrderStateValidator
└── persistence/
    ├── entities/    # UserEntity, ProductEntity, OrderEntity, OrderItemEntity
    └── repositories/
```

---

## Endpoints

| Método | Ruta                        | Rol requerido       | Descripción               |
|--------|-----------------------------|---------------------|---------------------------|
| POST   | `/api/auth/register`        | Público             | Registro de usuario       |
| POST   | `/api/auth/login`           | Público             | Login — devuelve JWT      |
| GET    | `/api/products/qr/{qrCode}` | Autenticado         | Consultar producto por QR |
| POST   | `/api/orders`               | ROLE_CLIENT         | Crear pedido              |
| PATCH  | `/api/orders/{id}/cancel`   | ROLE_CLIENT         | Cancelar pedido           |
| PATCH  | `/api/orders/{id}/status`   | ROLE_CAFETERIA_LADY | Cambiar estado del pedido |

---

## Seguridad

- Autenticación **stateless** con JWT (HS256, 24h)
- Header: `Authorization: Bearer <token>`
- Rutas públicas: `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- `@PreAuthorize` en controllers para control de roles

---

## Pruebas unitarias

45 tests · 0 fallos · cobertura de línea: **72.3%** (mínimo requerido: 70%)

| Suite                           | Tests |
|---------------------------------|-------|
| `AuthServiceTest`               | 5     |
| `OrderServiceTest`              | 8     |
| `StockValidatorTest`            | 5     |
| `OrderStateValidatorTest`       | 9     |
| `JwtUtilTest`                   | 8     |
| `MapperTest`                    | 4     |
| `GlobalExceptionHandlerTest`    | 5     |
| `DoswParcialT2ApplicationTests` | 1     |

```bash
mvn test                  # ejecutar tests
mvn verify                # tests + check de cobertura Jacoco
mvn jacoco:report         # generar reporte HTML en target/site/jacoco/index.html
```

---

## Reporte de cobertura Jacoco

> Cobertura total de líneas: **72%** supera el umbral del 70%.

![Reporte Jacoco](docs/images/JacocoReport.png)

---

## Swagger UI

> Documentación interactiva disponible en `http://localhost:8081/swagger-ui.html`.
> Autenticarse con el botón **Authorize** pegando el JWT obtenido del endpoint `/api/auth/login`.

![Swagger UI](docs/images/SwaggerUI.png)
![AuthorizeSuccess.png](docs/images/AuthorizeSuccess.png)

---

## Diagramas

### Arquitectura / Componentes


### Diseños de interfaz (Figma)


---

## Pruebas funcionales — Happy Path

### 1. Registro de usuario

**Request:** POST /api/auth/register

![RegistroUsuario.png](docs/images/UserRegister.png)

---

### 2. Login

**Request:** POST /api/auth/login

![Login.png](docs/images/Login.png)

---

### 3. Consultar producto por QR

![GetProduct.png](docs/images/GetProduct.png)

---

### 4. Crear pedido

**Request:** POST /api/orders

![CreateOrder.png](docs/images/CreateOrder.png)

---

## Flujos de error

| Escenario                     | Código           | Mensaje                                 |
|-------------------------------|------------------|-----------------------------------------|
| Email ya registrado           | 409 Conflict     | `Ya existe un usuario con ese email`    |
| Credenciales incorrectas      | 400 Bad Request  | `Credenciales inválidas`                |
| Producto no encontrado        | 400 Bad Request  | `Producto no encontrado`                |
| Sin stock suficiente          | 400 Bad Request  | `Stock insuficiente para el producto`   |
| Pedido activo existente       | 409 Conflict     | `El usuario ya tiene un pedido activo`  |
| Pedido no encontrado          | 400 Bad Request  | `Pedido no encontrado`                  |
| Transición de estado inválida | 409 Conflict     | `Transición inválida`                   |
| Token inválido / ausente      | 401 Unauthorized | —                                       |
| Rol insuficiente              | 403 Forbidden    | —                                       |
| Error de validación de campos | 400 Bad Request  | `Error de validación` + lista de campos |

---

## SonarQube (análisis estático)

```bash
# SonarQube corriendo localmente en localhost:9000
mvn sonar:sonar \
  -Dsonar.projectKey=ecixpress \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=<tu-token>
```
