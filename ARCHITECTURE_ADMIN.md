# BadmintonShop — Architecture Documentation

---

## 1. Entity Layer

### 1.1 Overview

Package: `com.badmintonshop.entity`

The entity layer consists of **6 JPA entity classes**, **2 enums**, and **7 JSONB attribute classes** for product variants.

---

### 1.2 Entity Classes

#### Account — `accounts`

Represents an employee account. Implements `UserDetails` (Spring Security).

| Field | Type | Constraint |
|-------|------|-----------|
| id | Long | PK, auto-increment |
| email | String | unique, not null |
| password | String | not null (hashed) |
| enabled | boolean | default: `true` |
| role | `RoleName` | not null, ENUM |
| createdAt | LocalDateTime | auto (`@CreationTimestamp`), not updatable |
| updatedAt | LocalDateTime | auto (`@UpdateTimestamp`) |

**Enums — `RoleName`:** `ADMIN`, `CASHIER`

---

#### Token — `tokens`

Stores JWT tokens for session management and token revocation.

| Field | Type | Constraint |
|-------|------|-----------|
| id | Long | PK, auto-increment |
| token | String | unique, not null, length 2048 |
| tokenType | `TokenType` | default: `BEARER` |
| revoked | boolean | |
| expired | boolean | |
| account | `Account` | FK `account_id`, not null, LAZY |

**Enums — `TokenType`:** `BEARER`

---

#### Brand — `brands`

Represents a product brand (Yonex, Victor, Li-Ning, etc.).

| Field | Type | Constraint |
|-------|------|-----------|
| id | Long | PK, auto-increment |
| name | String | unique, not null |
| logoUrl | String | |
| products | `List<Product>` | @OneToMany (mappedBy `brand`) |

---

#### Category — `categories`

Represents a product category. Supports **recursive hierarchy** (unlimited parent → child levels).

| Field | Type | Constraint |
|-------|------|-----------|
| id | Long | PK, auto-increment |
| name | String | not null |
| slug | String | unique, not null |
| description | String | TEXT |
| imageUrl | String | |
| parent | `Category` | self-ref FK `parent_id`, LAZY |
| children | `List<Category>` | self-ref @OneToMany, CASCADE ALL |
| products | `List<Product>` | @OneToMany (mappedBy `category`) |

---

#### Product — `products`

The main product entity. A product can have multiple variants (colors, sizes, etc.).

| Field | Type | Constraint |
|-------|------|-----------|
| id | Long | PK, auto-increment |
| name | String | not null |
| description | String | TEXT |
| thumbnailUrl | String | |
| isActive | boolean | default: `true` |
| createdAt | LocalDateTime | auto (`@CreationTimestamp`), not updatable |
| updatedAt | LocalDateTime | auto (`@UpdateTimestamp`) |
| category | `Category` | FK `category_id`, LAZY |
| brand | `Brand` | FK `brand_id`, LAZY |
| variants | `List<ProductVariant>` | @OneToMany, CASCADE ALL, orphanRemoval |

---

#### ProductVariant — `product_variants`

A specific SKU of a product. Variant-specific attributes are stored as **PostgreSQL JSONB**.

| Field | Type | Constraint |
|-------|------|-----------|
| id | Long | PK, auto-increment |
| sku | String | unique, not null |
| price | BigDecimal | not null |
| stockQuantity | Integer | default: `0` |
| imageUrl | String | |
| attributes | `ProductAttributes` | JSONB (Hibernate 6+), not null |
| product | `Product` | FK `product_id`, not null, LAZY |

---

### 1.3 JSONB — ProductAttributes

The `attributes` field in `ProductVariant` is a **polymorphic JSONB object**, discriminated by the `type` field.

```
ProductAttributes  (abstract)
├── color: String
└── description: String
    │
    ├── [type=RACKET]       RacketAttributes
    │     weight, gripSize, balance, tension, stiffness
    │
    ├── [type=SHOE]         ShoeAttributes
    │     size, gender, soleType, cushionTech
    │
    ├── [type=SHUTTLECOCK]  ShuttlecockAttributes
    │     speed, materialType, quantityPerTube, grade
    │
    ├── [type=STRING]       RacketStringAttributes
    │     gauge, durability, repulsionPower, hittingSound, control, origin
    │
    ├── [type=APPAREL]      ApparelAttributes
    │     size, material, gender, type
    │
    └── [type=OTHER]        OtherAccessoryAttributes
          subCategory, specification, material
```

Jackson `@JsonTypeInfo` + `@JsonSubTypes` handle polymorphic deserialization automatically.

---

### 1.4 Entity Relationship Diagram

```
┌─────────────┐       ┌─────────────┐
│   Account   │──1:N──│    Token    │
└─────────────┘       └─────────────┘

┌─────────────┐
│  Category   │──self 1:N──► Category (children)
└──────┬──────┘
       │ 1:N
       ▼
┌─────────────┐       ┌─────────────┐
│   Product   │◄──N:1─│    Brand    │
└──────┬──────┘       └─────────────┘
       │ 1:N
       ▼
┌──────────────────┐
│  ProductVariant  │
│  (attributes:    │
│   JSONB)         │
└──────────────────┘
```

---

### 1.5 Conventions

| Convention | Details |
|-----------|---------|
| Timestamps | Use `@CreationTimestamp` / `@UpdateTimestamp` (Hibernate) instead of manual `@PrePersist` / `@PreUpdate` |
| Fetch | `FetchType.LAZY` on all relationships to avoid N+1 queries |
| Builder default | Fields with inline default values must use `@Builder.Default` when combined with Lombok `@Builder` |
| JSONB | Hibernate 6+ native JSON mapping with PostgreSQL `jsonb` column type |
| Cascade | `CascadeType.ALL` + `orphanRemoval=true` on `Product → ProductVariant` |

---

## 2. API Features

### 2.1 Overview

- Base path: `/api`
- Auth: **JWT Bearer Token** (stateless, no sessions)
- All protected endpoints require header: `Authorization: Bearer <token>`

### 2.2 Access Control

| Role | Accessible Endpoints |
|------|---------------------|
| Public (no token) | `POST /api/account/auth/login` |
| `ADMIN` or `CASHIER` | `/api/account/**` |
| `ADMIN` only | `/api/admin/accounts/**` |

---

### 2.3 Authentication — `/api/account/auth`

#### `POST /api/account/auth/login`
Login and receive a JWT token.

**Request body:**
```json
{
  "email": "admin@example.com",
  "password": "secret123"
}
```

**Response `200 OK`:**
```json
{
  "token": "<jwt>",
  "email": "admin@example.com",
  "role": "ADMIN"
}
```

---

#### `POST /api/account/auth/logout`
Revoke the current JWT token. Requires a valid Bearer token.

**Response `200 OK`** — token is marked as revoked in the database.

---

### 2.4 Account (Self) — `/api/account`

> Required role: `ADMIN` or `CASHIER`

#### `GET /api/account`
Get the currently authenticated user's profile.

**Response `200 OK`:**
```json
{
  "id": 1,
  "email": "cashier@example.com",
  "role": "CASHIER",
  "enabled": true
}
```

---

### 2.5 Admin — Account Management — `/api/admin/accounts`

> Required role: `ADMIN` only

#### `GET /api/admin/accounts`
Retrieve a list of all accounts.

**Response `200 OK`:** `AccountResponse[]`

---

#### `GET /api/admin/accounts/{id}`
Retrieve a single account by ID.

**Response `200 OK`:** `AccountResponse`

---

#### `POST /api/admin/accounts`
Create a new employee account.

**Request body:**
```json
{
  "email": "newstaff@example.com",
  "password": "password123",
  "role": "CASHIER"
}
```

**Validation:**
- `email` — required, valid email format
- `password` — required, min 6 characters
- `role` — required, must be `ADMIN` or `CASHIER`

**Response `201 Created`:** `AccountResponse`

---

#### `DELETE /api/admin/accounts/{id}`
Soft-delete (deactivate) an account. Sets `enabled = false`, does not remove the record.

**Response `200 OK`:** `"Account deactivated successfully"`

---

### 2.6 Product — `/api/products`

> Status: **Not yet implemented** (controller stub exists)

---

### 2.7 CORS Configuration

| Setting | Value |
|---------|-------|
| Allowed Origins | `http://localhost:3000`, `http://localhost:5173` |
| Allowed Methods | `GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `OPTIONS` |
| Allowed Headers | `Authorization`, `Content-Type`, `X-Requested-With`, `Accept` |
| Allow Credentials | `true` |
| Max Age (preflight cache) | 3600s (1 hour) |
