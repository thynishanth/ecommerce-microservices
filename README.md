# Java Microservice Assignment

E-commerce Microservices for Inventory and Order Management

## Overview

This project implements two Spring Boot microservices for an e-commerce platform: Inventory Service and Order Service. The services communicate via REST APIs to manage product inventory with batch tracking and expiry dates, and to process customer orders while ensuring inventory consistency.

1. **Inventory Service** - Manages product inventory with batch tracking and expiry dates
2. **Order Service** - Processes orders and communicates with Inventory Service

## Architecture

```
┌─────────────────────┐         ┌─────────────────────┐
│   Order Service     │  REST   │  Inventory Service  │
│     (Port 8082)     │ ──────► │    (Port 8081)      │
│                     │         │                     │
│  - Place Orders     │         │  - Track Batches    │
│  - Track Status     │         │  - Expiry Dates     │
│  - H2 Database      │         │  - FIFO Strategy    │
│  - Liquibase        │         │  - H2 Database      │
└─────────────────────┘         └─────────────────────┘
```

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 In-Memory Database**
- **Liquibase** - Database migrations and data loading
- **Lombok** - Reduce boilerplate code
- **JUnit 5 & Mockito** - Testing
- **RestTemplate** - Inter-service communication
- **Swagger/OpenAPI** - API documentation and testing

## Project Structure

```
java-microservice-assignment/
├── inventory-service/
│   ├── src/main/java/com/ecommerce/inventory/
│   │   ├── controller/     # REST endpoints
│   │   ├── service/        # Business logic
│   │   ├── repository/     # Data access
│   │   ├── entity/         # JPA entities
│   │   ├── dto/            # Data transfer objects
│   │   └── factory/        # Factory pattern implementation
│   └── src/main/resources/
│       ├── db/changelog/   # Liquibase migrations
│       └── application.properties
├── order-service/
│   ├── src/main/java/com/ecommerce/order/
│   │   ├── controller/     # REST endpoints
│   │   ├── service/        # Business logic
│   │   ├── repository/     # Data access
│   │   ├── entity/         # JPA entities
│   │   ├── dto/            # Data transfer objects
│   │   └── client/         # HTTP client for Inventory Service
│   └── src/main/resources/
│       ├── db/changelog/   # Liquibase migrations
│       └── application.properties
└── pom.xml                 # Parent POM
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/thynishanth/ecommerce-microservices.git
cd java-microservice-assignment
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Services

**Start Inventory Service first (Port 8081):**

```bash
cd inventory-service
mvn spring-boot:run
```

**Start Order Service (Port 8082):**

```bash
cd order-service
mvn spring-boot:run
```

## API Documentation

### Swagger/OpenAPI Documentation

Both services are equipped with interactive API documentation using Swagger/OpenAPI.

**Inventory Service:**
- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8081/api-docs

**Order Service:**
- **Swagger UI:** http://localhost:8082/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8082/api-docs

The Swagger UI provides an interactive interface to explore and test all API endpoints with detailed request/response schemas.

### Inventory Service (Port 8081)

#### GET /inventory/{productId}

Returns list of inventory batches sorted by expiry date for a given product.

**Request:**
```
GET http://localhost:8081/inventory/1001
```

**Response:**
```json
{
  "productId": 1001,
  "productName": "Laptop",
  "batches": [
    {
      "batchId": 1,
      "quantity": 68,
      "expiryDate": "2026-06-25"
    }
  ]
}
```

#### POST /inventory/update

Updates inventory after an order is placed.

**Request:**
```
POST http://localhost:8081/inventory/update
Content-Type: application/json

{
  "productId": 1001,
  "quantity": 10
}
```

**Response:**
```json
{
  "success": true,
  "message": "Inventory updated successfully",
  "updatedBatchIds": [1]
}
```

#### GET /inventory/{productId}/availability

Check if inventory is available for a product.

**Request:**
```
GET http://localhost:8081/inventory/1001/availability?quantity=10
```

**Response:**
```json
true
```

### Order Service (Port 8082)

#### POST /order

Places an order and updates inventory accordingly.

**Request:**
```
POST http://localhost:8082/order
Content-Type: application/json

{
  "productId": 1002,
  "quantity":1
}
```

**Response:**
```json
{
  "orderId": 100,
  "productId": 1002,
  "productName": "Smartphone",
  "quantity": 1,
  "status": "PLACED",
  "reservedFromBatchIds": [
    9
  ],
  "message": "Order placed. Inventory reserved."
}
```

#### GET /order/{orderId}

Get order by ID.

**Request:**
```
GET http://localhost:8082/order/1
```

**Response:**
```json
{
  "orderId": 1,
  "productId": 1005,
  "productName": "Smartwatch",
  "quantity": 10,
  "status": "DELIVERED",
  "orderDate": "2025-12-04",
  "reservedBatchIds": null
}
```

#### GET /order

Get all orders.

**Request:**
```
GET http://localhost:8082/order
```

## Design Patterns

### Factory Pattern (Inventory Service)

The Inventory Service implements the Factory Design Pattern to allow future extensibility of inventory handling strategies.

```
InventoryHandlerFactory
      │
      ├── FifoInventoryHandler (default - First Expiry First Out)
      └── [Future handlers can be easily added]
```

**Key Classes:**
- `InventoryHandler` - Interface defining the contract
- `FifoInventoryHandler` - Implementation for FIFO (First Expiry First Out) strategy
- `InventoryHandlerFactory` - Factory class for creating handlers

## Database

Both services use H2 in-memory database. The database is automatically initialized with sample data at startup using Liquibase.

### H2 Console

- **Inventory Service:** http://localhost:8081/h2-console
  - JDBC URL: `jdbc:h2:mem:inventorydb`
- **Order Service:** http://localhost:8082/h2-console
  - JDBC URL: `jdbc:h2:mem:orderdb`
- Username: `admin`
- Password: (empty)

## Testing

### Run All Tests

```bash
mvn test
```

### Run Tests for Individual Service

```bash
# Inventory Service tests
cd inventory-service
mvn test

# Order Service tests
cd order-service
mvn test
```

### Test Types

1. **Unit Tests** - Test service layer logic in isolation
   - `InventoryServiceImplTest`
   - `OrderServiceImplTest`

2. **Controller Tests** - Test REST endpoints with mocked services
   - `InventoryControllerTest`
   - `OrderControllerTest`

3. **Integration Tests** - Test full application with H2 database
   - `InventoryServiceIntegrationTest`
   - `OrderServiceIntegrationTest`

## Configuration

### Inventory Service (`application.properties`)

```properties
server.port=8081
spring.datasource.url=jdbc:h2:mem:inventorydb
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
```

### Order Service (`application.properties`)

```properties
server.port=8082
spring.datasource.url=jdbc:h2:mem:orderdb
inventory.service.url=http://localhost:8081
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
```
