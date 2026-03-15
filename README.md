# Dicestore — E-Commerce Backend

![Build](https://img.shields.io/badge/build-passing-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
![License](https://img.shields.io/badge/license-Apache%202.0-blue)

Dicestore is a modular, production-ready e-commerce backend built with Java Spring Boot. Features include JWT authentication,
Paystack payment processing with webhook verification, Cloudinary image storage, async email notifications, and a domain-driven architecture.

## Tech Stack

- **Java 21** + **Spring Boot 3.5**
- **MySQL** (dev) / **PostgreSQL** (prod)
- **Flyway** — database migrations
- **Spring Security** + **JWT** — HttpOnly cookie authentication
- **Paystack** — payment processing with webhook verification
- **Cloudinary** — product image storage
- **JavaMail** + **Thymeleaf** — async email notifications
- **OpenAPI 3.1 / Swagger** — auto-generated API documentation from annotations

## Features

- User registration, login and JWT authentication (HttpOnly cookie)
- Password reset via email token
- Product and category management with Cloudinary image upload
- Cart management with line totals, grand total and quantity controls
- Order creation with stock validation and optimistic locking
- Paystack payment integration with HMAC-SHA512 webhook signature verification
- Idempotent webhook handling to prevent duplicate payment processing
- Payment states: `PENDING`, `SUCCESS`, `FAILED`, `REFUNDED`
- Order states: `PENDING_PAYMENT`, `PAID`, `CONFIRMED`, `CANCELLED`, `REFUNDED`
- Cart cleared only after confirmed payment via webhook
- Async order confirmation email after successful payment
- Admin order management — confirm and cancel orders
- Customer self-cancellation for unpaid orders only
- Product discount management
- Customer contact/messaging system
- Global exception handling with dedicated exceptions per domain

## Project Structure
```
com.victoruk.dicestore
├── admin          # Admin controllers (products, orders, categories, discounts, images)
├── auth           # Registration, login, JWT
├── cart           # Cart entity, service, controller
├── common         # Shared config, exceptions, security, base entity
├── contact        # Customer contact messages
├── discount       # Product discounts
├── infrastructure # Email, Cloudinary, JWT filter
├── order          # Order entity, service, controller
├── passwordreset  # Forgot/reset password flow
├── payment        # Paystack service, webhook, payment entity
├── product        # Products, categories, images
└── user           # User profile and address
```

## Getting Started

### Prerequisites
- Java 21
- MySQL 8
- Maven 3.9+

### Setup

1. Clone the repository
```bash
git clone https://github.com/tochukwu-victor/dicestore.git
cd dicestore
```

2. Set environment variables

**Database**
```
DATABASE_PASSWORD=your_db_password
```

**Email**
```
GMAIL_USERNAME=your_email
GMAIL_APP_PASSWORD=your_app_password
```

**Payment**
```
PAYSTACK_SECRET_KEY=your_paystack_key
PAYSTACK_WEBHOOK_SECRET=your_webhook_secret
```

**Cloudinary**
```
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

3. Build and run with dev profile
```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

4. Access Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

## Payment Flow
```
1. POST /api/v1/orders                     → Create order (status: PENDING_PAYMENT)
2. POST /api/v1/payments/initialize/{id}   → Call Paystack API, receive payment URL
3. User completes payment on Paystack hosted page
4. Paystack fires webhook → POST /api/v1/payments/webhook
5. Backend verifies HMAC-SHA512 signature
6. Order → PAID, cart cleared, confirmation email sent asynchronously
```

## API Overview

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | Login (sets HttpOnly JWT cookie) |
| POST | `/api/v1/auth/logout` | Logout |
| POST | `/api/v1/auth/forgot-password` | Request password reset |
| POST | `/api/v1/auth/reset-password` | Reset password using token |

### Cart
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/cart/add` | Add item to cart |
| GET | `/api/v1/cart` | View cart with line totals and grand total |
| PATCH | `/api/v1/cart/items/{cartItemId}/quantity` | Update item quantity (+1 / -1) |
| DELETE | `/api/v1/cart/remove/{cartItemId}` | Remove item from cart |
| DELETE | `/api/v1/cart/clear` | Clear entire cart |

### Orders
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/orders` | Create order from cart |
| GET | `/api/v1/orders` | Get my orders |
| PATCH | `/api/v1/orders/{orderId}/cancel` | Cancel my order (`PENDING_PAYMENT` only) |

### Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/payments/initialize/{orderId}` | Initialize Paystack payment |
| POST | `/api/v1/payments/webhook` | Paystack webhook receiver (public) |

### Products & Categories
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/products` | Get all products |
| GET | `/api/v1/products/{id}` | Get product by ID |
| GET | `/api/v1/categories` | Get all categories |
| GET | `/api/v1/categories/{id}` | Get category by ID |

### Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/admin/products` | Create product |
| PUT | `/api/v1/admin/products/{id}` | Update product |
| DELETE | `/api/v1/admin/products/{id}` | Delete product |
| POST | `/api/v1/admin/products/with-images` | Create product with images |
| POST | `/api/v1/admin/products/{productId}/images` | Upload product image |
| DELETE | `/api/v1/admin/products/{productId}/images/{publicId}` | Delete product image |
| POST | `/api/v1/admin/categories` | Create category |
| PUT | `/api/v1/admin/categories/{id}` | Update category |
| DELETE | `/api/v1/admin/categories/{id}` | Delete category |
| POST | `/api/v1/admin/discounts` | Create discount |
| GET | `/api/v1/admin/discounts/product/{productId}` | Get discounts by product |
| GET | `/api/v1/admin/orders` | Get all pending orders |
| PATCH | `/api/v1/admin/orders/{orderId}/confirm` | Confirm order |
| PATCH | `/api/v1/admin/orders/{orderId}/cancel` | Cancel order |
| GET | `/api/v1/admin/messages` | Get all open messages |
| PATCH | `/api/v1/admin/messages/{contactId}/close` | Close a message |

### Profile
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/profile` | Get current user profile |
| PUT | `/api/v1/profile` | Update profile and address |

## Example Requests

**Register**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John","email":"john@example.com","password":"SecurePass123!"}'
```

**Add to Cart**
```bash
curl -X POST http://localhost:8080/api/v1/cart/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"productId":1,"quantity":2}'
```

**Initialize Payment**
```bash
curl -X POST http://localhost:8080/api/v1/payments/initialize/1 \
  -H "Authorization: Bearer <token>"
```

## Database Migrations

| Version | Description |
|---------|-------------|
| V1 | Initial schema — users, products, cart, orders |
| V2 | Add payments table |
| V3 | Add stock and version columns for optimistic locking |

## Testing
```bash
mvn test
```

## API Documentation

Full API documentation is available via Swagger UI once the application is running:
http://localhost:8080/swagger-ui/index.html
```

## Roadmap

- [ ] Redis caching for product catalog
- [ ] Virtual threads (`spring.threads.virtual.enabled=true`)
- [ ] HikariCP connection pool tuning
- [ ] Docker support
- [ ] Refund flow implementation

## Note

Dicestore is a production-ready e-commerce backend built for
Chukwu Dice Stores — a wholesale and retail business based in Lagos, Nigeria.

## Frontend

The Next.js frontend connects to this API using the JWT cookie for authentication and consumes all endpoints listed above:
[dicestore-frontend](https://github.com/tochukwu-victor/dicestore-frontend)

## License

This project is licensed under the [Apache 2.0 License](LICENSE).