# Payment Operations Simulation - Microservices Architecture With JWT and WebFlux

## Overview
This application is a simulation of payment operations and serves as an example of a microservices architecture that operates using data transmission through RabbitMQ message queues. Each individual service has its own database and adaptive models for interaction. The Eureka registration and discovery service acts as the central registry, connecting all services together. The application uses Spring Security with JWT (JSON Web Token) to verify users on the API Gateway service side, which receives user data from the USER-SERVICE service (this is the only service that also has Spring Security settings; all others are designed to prohibit direct access to their endpoints). A special point of this application is the use of Spring WebFlux and WebClient for asynchronous and non-blocking communication between services. Additionally, Flyway is used for database migrations.

## Responsibilities

### **user-service**
- Creates users and stores their data.
- Has a relationship with **wallet-service**, to which it sends data via a queue for the creation of a new wallet.
- Utilizes Flyway for database schema migrations.

### **wallet-service**
- Creates wallets and allows balance top-ups or expense transactions.
- Receives data from **user-service** and creates a wallet based on this information.
- Interacts with **expenses-service** and **journal-service**, sending relevant data via queues.
- Uses Spring WebFlux for handling asynchronous requests and WebClient for inter-service communication.

### **expenses-service**
- Manages expense records.
- Receives data from **wallet-service**, processes it, and creates a corresponding record in its database.
- Employs Flyway for database migrations.

### **journal-service**
- Monitors user account data.
- Receives data from **wallet-service** and creates records in its own database.
- Allows tracking of fund movements.
- Utilizes Spring WebFlux for non-blocking operations and Flyway for database migrations.

## **Technology Stack**
- **Spring Cloud** - Used to manage microservices communication.
- **RabbitMQ** - Message broker for data exchange between services.
- **Eureka Server** - Service registry and discovery system.
- **API Gateway** - Central entry point for managing service requests (Spring WebFlux for asynchronous requests).
- **Spring WebFlux** - Enables reactive and non-blocking programming model for improved performance.
- **WebClient** - Used for efficient, non-blocking HTTP communication between microservices.
- **Flyway** - Handles database migrations for all services.

## **Scalability & Management**
This project demonstrates the capabilities of **Spring Cloud** for applications with a moderate number of microservices (up to approximately 50 units). It includes:
- A **single entry point** via **API Gateway**.
- **Eureka service registry**, enabling easy scaling and efficient management of separate modules (services).

## **Postman Endpoints Testing**

### 1. USER-SERVICE
**registration():**
POST `http://localhost:8081/api/v1/users/register`

**Body -> raw:**
```json
{
  "username": "UserOne",
  "password": "123",
  "firstName": "User1",
  "lastName": "User1",
  "email": "user1@example.com",
  "phone": "111111"
}
```

**login():**
POST    `localhost:8081/api/v1/auth/login`

**Body:**
```json
{
  "username": "username",
  "password": "123"
}
```

**getUserById(userId):**
GET `http://localhost:8081/api/v1/users/1996395b-6e65-40a3-8d87-4e6ac3549f5e`
**Headers:**
```
Content-Type: application/json
Authorization: Bearer <your_token_here>
```

**getAll():**
GET `http://localhost:8081/api/v1/users/all`
**Headers:**
```
Content-Type: application/json
Authorization: Bearer <your_token_here>
```

### 2. WALLET-SERVICE
**getAll():**
GET `http://localhost:8082/api/v1/wallets/all`
**Headers:**
```
Content-Type: application/json
Authorization: Bearer <your_token_here>
```

**getWalletById():**
GET `http://localhost:8082/api/v1/wallets/3b15ecb4-815b-4d1b-9cdd-b63854a1fe21`
**Headers:**
Content-Type: application/json
```
Authorization: Bearer <your_token_here>
```

**getBalanceByWalletId():**
GET `http://localhost:8082/api/v1/wallets/3b15ecb4-815b-4d1b-9cdd-b63854a1fe21/balance`
**Headers:**
```
Content-Type: application/json
Authorization: Bearer <your_token_here>
```

**replenishBalance():**
POST `http://localhost:8082/api/v1/wallets/up-balance`
**Headers:**
```
Content-Type: application/json
Authorization: Bearer <your_token_here>
```
**Body -> raw:**
```json
{
  "userId": "3b15ecb4-815b-4d1b-9cdd-b63854a1fe21",
  "amount": 1004
}
```

**deductBalance():**
PATCH `http://localhost:8082/api/v1/wallets/deduct-balance`
**Headers:**
```
Content-Type: application/json
Authorization: Bearer <your_token_here>
```
**Body -> raw:**
```json
{
  "userId": "3b15ecb4-815b-4d1b-9cdd-b63854a1fe21",
  "amount": 155
}
```

### 3. EXPENSES-SERVICE
**getAllExpensesByUserId():**
GET `http://localhost:8084/api/v1/expenses/3b15ecb4-815b-4d1b-9cdd-b63854a1fe21`
**Headers:**
```
Content-Type: application/json
Authorization: Bearer <your_token_here>
```

### 4. JOURNAL-SERVICE
**getAllEntries():**
GET `http://localhost:8083/api/v1/journal/all`
**Headers:**
```
Content-Type: application/json
Authorization: Bearer <your_token_here>
```

**getAllUserJournalEntries():**
GET `http://localhost:8083/api/v1/journal/3b15ecb4-815b-4d1b-9cdd-b63854a1fe21`
**Headers:**
```
Content-Type: application/json
Authorization: Bearer <your_token_here>
```


