# BadmintonShop

A backend REST API for managing a badminton equipment e-commerce store, built with **Spring Boot 4** and **PostgreSQL**. The system supports product catalog management with category-specific attributes (rackets, shoes, apparel, shuttlecocks, strings, and accessories), employee authentication via JWT, and a role-based access control (RBAC) system.

## Tech Stack

- **Java 21**
- **Spring Boot 4.0.1** (Web MVC, Data JPA, Security, Validation)
- **PostgreSQL 16** with JSONB for polymorphic product attributes
- **Spring Security** with stateless JWT authentication
- **Lombok** for boilerplate reduction
- **Docker Compose** for local development infrastructure
- **Maven** (with Maven Wrapper)

## Architecture

```
src/main/java/com/badmintonshop/
├── config/             # Security, JWT filter, data seeder, app config
├── controller/         # REST controllers (Auth)
├── entity/             # JPA entities
│   ├── enums/          # RoleName, PermissionName, TokenType
│   └── json/           # Polymorphic JSONB attribute classes
│       ├── ProductAttributes.java      (base class)
│       ├── RacketAttributes.java
│       ├── ShoeAttributes.java
│       ├── ApparelAttributes.java
│       ├── ShuttlecockAttributes.java
│       ├── RacketStringAttributes.java
│       └── OtherAccessoryAttributes.java
├── exception/          # Global exception handler, ResourceNotFoundException
├── payload/            # Request/Response DTOs (LoginRequest, AuthResponse, ErrorResponse)
├── repository/         # Spring Data JPA repositories
├── scheduler/          # Scheduled tasks (refresh token cleanup)
├── security/           # JWT utility, custom UserDetailsService
└── service/            # Business logic (AuthService, LogoutService)
```

### Data Model

| Entity           | Description                                                        |
|------------------|--------------------------------------------------------------------|
| **Product**      | Base product definition (name, brand, description, thumbnail)      |
| **ProductVariant** | Specific SKU with price, stock, JSONB attributes, and images     |
| **Employee**     | System user implementing `UserDetails` for Spring Security         |
| **Role**         | RBAC role (ADMIN, MANAGER, STAFF, WAREHOUSE)                       |
| **Permission**   | Granular permission (e.g., `PRODUCT_CREATE`, `ORDER_READ`)         |
| **Token**        | JWT whitelist for session management                               |
| **RefreshToken** | Long-lived refresh tokens with scheduled cleanup                   |

### RBAC Roles & Permissions

| Role        | Access Level                                                                 |
|-------------|------------------------------------------------------------------------------|
| **ADMIN**   | Full access to all resources                                                 |
| **MANAGER** | All permissions except deletions                                             |
| **STAFF**   | Orders (CRU), Customers (CRU), Products (R), Inventory (R), Dashboard       |
| **WAREHOUSE** | Inventory (R/Import/Export), Products (R), Orders (R/U)                    |

## Prerequisites

- **Java 21** (or later)
- **Docker** and **Docker Compose**
- **Maven 3.9+** (or use the included Maven Wrapper)

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/NguyenVanQThanh/BadmintonShop.git
cd BadmintonShop
```

### 2. Configure environment variables

Copy the example environment file and update the values as needed:

```bash
cp .env.example .env
```

The `.env` file contains the following variables:

| Variable            | Description                  | Default                  |
|---------------------|------------------------------|--------------------------|
| `POSTGRES_USER`     | PostgreSQL username          | `admin_production`       |
| `POSTGRES_PASSWORD` | PostgreSQL password          | *(set in .env.example)*  |
| `POSTGRES_DB`       | Database name                | `badminton_core_db`      |
| `POSTGRES_PORT`     | PostgreSQL host port         | `5432`                   |
| `PGADMIN_EMAIL`     | pgAdmin login email          | `admin@badminions.com`   |
| `PGADMIN_PASSWORD`  | pgAdmin login password       | *(set in .env.example)*  |
| `PGADMIN_PORT`      | pgAdmin host port            | `5050`                   |

You will also need to configure the following in your application environment (or a `.env` file that `spring-dotenv` can read):

| Variable           | Description                              | Required |
|--------------------|------------------------------------------|----------|
| `DB_URL`           | JDBC connection string for PostgreSQL    | Yes      |
| `SSL_KEY_PASSWORD` | Password for the PKCS12 SSL keystore     | Yes      |
| `JWT_SECRET`       | Base64-encoded secret key (≥256 bits)    | Yes      |
| `JWT_EXPIRATION`   | Token expiration in ms (default: 86400000 = 24h) | No |
| `SERVER_PORT`      | Application port (default: 8443)         | No       |
| `SHOW_SQL`         | Log SQL queries (default: true)          | No       |

### 3. Start the database

```bash
docker compose up -d
```

This starts:
- **PostgreSQL 16** on `localhost:5432` (bound to `127.0.0.1` only)
- **pgAdmin 4** on `localhost:5050`

### 4. Generate an SSL keystore (for local development)

The application requires an SSL keystore at `src/main/resources/badminton-keystore.p12`:

```bash
keytool -genkeypair \
  -alias badmintons \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore src/main/resources/badminton-keystore.p12 \
  -validity 365 \
  -storepass <your-ssl-password>
```

Set the `SSL_KEY_PASSWORD` environment variable to the password you chose above.

### 5. Build and run the application

Using the Maven Wrapper:

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

Or build and run the JAR:

```bash
./mvnw clean package -DskipTests
java -jar target/badmintonshop-0.0.1-SNAPSHOT.jar
```

The application will start on `https://localhost:8443` by default.

### 6. Seed data

On first startup, the `DataSeeder` automatically creates:

- **Permissions**: All entries defined in `PermissionName` enum
- **Roles**: ADMIN, MANAGER, STAFF, WAREHOUSE (with appropriate permission sets)
- **Default employee accounts**:

| Email                 | Role      | Default Password |
|-----------------------|-----------|------------------|
| `admin@gmail.com`     | ADMIN     | `123456`         |
| `manager@gmail.com`   | MANAGER   | `123456`         |
| `staff@gmail.com`     | STAFF     | `123456`         |
| `warehouse@gmail.com` | WAREHOUSE | `123456`         |

> **⚠️ Important:** Change default passwords immediately in a production environment.

## API Endpoints

### Authentication

| Method | Endpoint             | Description      | Auth Required |
|--------|----------------------|------------------|---------------|
| POST   | `/api/auth/login`    | Employee login   | No            |
| POST   | `/api/auth/logout`   | Employee logout  | Yes           |

#### Login Request

```bash
curl -k -X POST https://localhost:8443/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@gmail.com", "password": "123456"}'
```

#### Login Response

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "admin@gmail.com",
  "role": "ROLE_ADMIN"
}
```

Use the returned token in the `Authorization` header for protected endpoints:

```
Authorization: Bearer <token>
```

## Product Attributes

Product variants use polymorphic JSONB attributes to store category-specific specs:

| Category         | Attribute Class             | Key Fields                                            |
|------------------|-----------------------------|-------------------------------------------------------|
| Rackets          | `RacketAttributes`          | weight, gripSize, balance, tension, stiffness         |
| Shoes            | `ShoeAttributes`            | *(category-specific fields)*                          |
| Apparel          | `ApparelAttributes`         | *(category-specific fields)*                          |
| Shuttlecocks     | `ShuttlecockAttributes`     | *(category-specific fields)*                          |
| Racket Strings   | `RacketStringAttributes`    | *(category-specific fields)*                          |
| Accessories      | `OtherAccessoryAttributes`  | *(category-specific fields)*                          |

## Running Tests

```bash
./mvnw test
```

## Project Configuration

Key application properties are defined in `src/main/resources/application.properties`:

- **HTTPS** enabled by default with PKCS12 keystore
- **Hibernate DDL**: `update` mode (auto-updates schema — use `validate` in production)
- **OSIV** disabled for performance
- **JDBC batching** enabled (batch size: 30)
- **CORS** configured for frontend integration
- **Stateless sessions** — every request must include a JWT

## License

This project is for educational and development purposes.
