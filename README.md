# BFSI E-Commerce Banking API

### Spring Boot 3 · Spring Security (JWT) · Hibernate · MySQL · React · AWS Free Tier

A **production-grade full-stack BFSI (Banking, Financial Services & Insurance) application** built with a Spring Boot REST API backend and a React frontend. Covers 6 core BFSI requirements in one project.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen)
![React](https://img.shields.io/badge/React-18-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![JWT](https://img.shields.io/badge/Auth-JWT-yellow)
![License](https://img.shields.io/badge/License-MIT-green)

---

## Features

- **JWT Authentication** — Stateless login with access + refresh tokens
- **Role-Based Access Control** — `ROLE_USER`, `ROLE_BANKER`, `ROLE_ADMIN`
- **Wallet System** — Top-up, fund transfer, transaction ledger
- **Product Catalogue** — Browse, search, filter by category, admin CRUD
- **Order Management** — Place orders, cancel with auto-refund, order history
- **React Frontend** — Full UI with dashboard, wallet, products, orders pages
- **AWS Ready** — Dockerfile + docker-compose + EC2 deploy script

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.2, Spring Security, Spring Data JPA |
| Auth | JWT (JJWT 0.11.5), BCrypt |
| ORM | Hibernate 6, MySQL 8 |
| Frontend | React 18, Axios, React Router v6 |
| Testing | JUnit 5, Mockito, H2 (in-memory) |
| DevOps | Docker, Docker Compose, AWS EC2 + RDS |
| Docs | Swagger UI (SpringDoc OpenAPI 2.3) |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                React Frontend (localhost:3000)              │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP / Axios
┌──────────────────────────▼──────────────────────────────────┐
│              Spring Security Filter Chain                   │
│         AuthTokenFilter (JWT) → SecurityContextHolder       │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                    REST Controllers                         │
│  AuthController │ ProductController │ OrderController       │
│                 │ WalletController  │                       │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                    Service Layer                            │
│  AuthService │ ProductService │ OrderService │ WalletService│
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│              Repository Layer (Spring Data JPA)             │
└──────────────────────────┬──────────────────────────────────┘
                           │ Hibernate ORM
┌──────────────────────────▼──────────────────────────────────┐
│             MySQL 8 (local) / AWS RDS (production)         │
└─────────────────────────────────────────────────────────────┘
```

---

## Project Structure

```
bfsi-ecommerce/
├── src/main/java/com/bfsi/ecommerce/
│   ├── config/          # Security, Swagger, DataInitializer
│   ├── controller/      # REST controllers
│   ├── dto/             # Request/Response DTOs
│   ├── entity/          # JPA entities
│   ├── exception/       # Global exception handler
│   ├── repository/      # Spring Data JPA repositories
│   ├── security/        # JWT utils, filters, auth entry point
│   └── service/         # Business logic
├── src/test/            # JUnit + Mockito tests
├── src/main/resources/
│   ├── application.properties
│   └── application-test.properties
├── frontend/            # React application
│   └── src/
│       ├── pages/       # Login, Register, Dashboard, Products, Orders, Wallet
│       ├── components/  # Navbar
│       ├── context/     # AuthContext (JWT state)
│       └── services/    # Axios API client
├── Dockerfile
├── docker-compose.yml
└── deploy-aws.sh
```

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8
- Node.js 18+

### 1. Clone the repo
```bash
git clone https://github.com/<your-username>/bfsi-ecommerce.git
cd bfsi-ecommerce
```

### 2. Create MySQL database
```sql
CREATE DATABASE bfsi_db;
```

### 3. Run the backend
```bash
mvn spring-boot:run
```
> Default DB password is `Root@123`. Change it in `src/main/resources/application.properties` if needed.

API runs at: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

### 4. Run the frontend
```bash
cd frontend
npm install
npm start
```
Frontend runs at: `http://localhost:3000`

---

## Docker (Alternative)

```bash
mvn clean package -DskipTests
docker-compose up --build
```

---

## API Endpoints

### Auth — Public
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login → JWT + refresh token |
| POST | `/api/auth/refresh` | Refresh access token |

### Products — Mixed
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/products/public` | ❌ | List all products |
| GET | `/api/products/public/search?keyword=` | ❌ | Search products |
| POST | `/api/admin/products` | ADMIN | Create product |
| PUT | `/api/admin/products/{id}` | ADMIN | Update product |
| DELETE | `/api/admin/products/{id}` | ADMIN | Deactivate product |

### Orders — Authenticated
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders` | Place order |
| GET | `/api/orders/my-orders` | Order history |
| PATCH | `/api/orders/{id}/cancel` | Cancel + auto-refund |

### Wallet — Authenticated
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/wallet` | Balance + account number |
| POST | `/api/wallet/topup` | Add funds |
| POST | `/api/wallet/transfer` | Transfer to another account |
| GET | `/api/wallet/transactions` | Transaction history |

---

## Test Credentials

| Username | Password | Role | Wallet |
|----------|----------|------|--------|
| `admin` | `Admin@123` | ADMIN | ₹10,000 |
| `banker` | `Banker@123` | BANKER | ₹5,000 |
| `user1` | `User@123` | USER | ₹2,000 |

---

## Running Tests

```bash
# All tests (uses H2 in-memory — no MySQL needed)
mvn test

# Specific test
mvn test -Dtest=AuthControllerTest
mvn test -Dtest=ProductControllerTest
mvn test -Dtest=WalletControllerTest
mvn test -Dtest=OrderControllerTest
```

---

## AWS Deployment

See [deploy-aws.sh](deploy-aws.sh) for full EC2 deployment script.

```bash
# Build JAR
mvn clean package -DskipTests

# Copy to EC2
scp -i your-key.pem target/ecommerce-banking-1.0.0.jar ubuntu@<EC2_IP>:/tmp/

# Deploy
ssh -i your-key.pem ubuntu@<EC2_IP>
sudo bash /tmp/deploy-aws.sh
```

---

## Security Features

- Stateless JWT (no sessions)
- BCrypt password hashing (strength 10)
- Access token (24h) + Refresh token (7d)
- Method-level RBAC via `@PreAuthorize`
- Input validation via Jakarta Bean Validation
- Global exception handler — no stack traces leaked
- CORS configured for frontend origin
- Non-root Docker user

---

## License

MIT License — free to use for learning and projects.
