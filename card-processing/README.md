# Card Processing Service

A RESTful backend service for processing bank cards built with Java 17 and Spring Boot 3.

## Technologies

- Java 17
- Spring Boot 3.2.0
- PostgreSQL
- Liquibase
- Docker
- JWT Authentication
- Redis (CBU exchange rate caching)
- WebClient (CBU API integration)
- Swagger / OpenAPI 3

## Requirements

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Redis

## Getting Started

### 1. Clone the repository
```bash
git clone https://gitlab.com/your-repo/card-processing.git
cd card-processing
```

### 2. Configure application.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/card_processing
spring.datasource.username=your_username
spring.datasource.password=your_password

jwt.secret=mySecretKey12345mySecretKey12345mySecretKey12345
jwt.expiration=86400000

cbu.api.url=https://cbu.uz/uz/arkhiv-kursov-valyut/json/
```

### 3. Run the application
```bash
mvn spring-boot:run
```

### 4. Access Swagger UI
```
http://localhost:8081/swagger-ui/index.html
```

## API Endpoints

### Cards

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/cards | Create a new card |
| GET | /api/v1/cards/{cardId} | Get card by ID |
| POST | /api/v1/cards/{cardId}/block | Block a card |
| POST | /api/v1/cards/{cardId}/unblock | Unblock a card |

### Transactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/cards/{cardId}/debit | Withdraw funds |
| POST | /api/v1/cards/{cardId}/credit | Top up funds |
| GET | /api/v1/cards/{cardId}/transactions | Get transaction history |

## Authentication

All endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer <your_token>
```

## Key Features

- **Idempotency** — Duplicate requests are handled safely using idempotency keys
- **ETag** — Optimistic locking for block/unblock operations using If-Match header
- **CBU Integration** — Automatic currency conversion using Central Bank of Uzbekistan rates
- **Pagination** — Transaction history supports pagination and filtering by type

## Project Structure

```
src/main/java/
├── controller/       — REST controllers
├── service/          — Business logic
│   └── impl/
├── repository/       — JPA repositories
├── entity/           — JPA entities
├── dto/              — Data transfer objects
├── exception/        — Custom exceptions
├── security/         — JWT filter and utility
└── config/           — Application configuration
```