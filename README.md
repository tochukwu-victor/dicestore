# 🛒 Dicestore – E-Commerce Backend

**Dicestore** is a modern, production-ready **Java Spring Boot backend** for a full-stack e-commerce application. It supports user authentication, product management, email verification, and auditing—all designed to integrate smoothly with a future React frontend.

---

## 📁 Project Structure

dicestore/
│
├── src/main/java/com/victoruk/dicestore/
├── cloudservice/ # Handles image uploads to services like Cloudinary
├── config/ # Spring configuration (security, auditing, etc.)
├── constant/ # Application-wide constants
├── controller/ # REST API endpoints (Auth, Profile, Product, etc.)
├── dto/ # Data Transfer Objects for request and response mapping
├── entity/ # JPA entities (Customer, Product, Order, etc.)
├── exception/ # Global and custom exception handling
├── filter/ # JWT filter and request filtering logic
├── repository/ # Spring Data JPA repositories
├── security/ # Security config and authentication logic
├── service/ # Business logic (ProfileService, AuthService, etc.)
├── util/ # Helper classes (JwtUtil, validators, etc.)
├── application.properties / .env
└── README.md

markdown
Copy
Edit

---

## ⚙️ Tech Stack

- **Java 17**
- **Spring Boot 3**
- **Spring Security** (JWT-based)
- **Spring Data JPA + MySQL**
- **JavaMailSender** (for email verification)
- **Cloudinary** (image upload)
- **Lombok** (boilerplate reduction)
- **Flyway** (database migration)
- **Auditing** (createdBy, updatedBy)

---

## 🚀 Features Implemented

- ✅ User Registration & Login (JWT Auth)
- ✅ Role-based access control (User/Admin)
- ✅ Email verification with HTML templates
- ✅ Product and image management via Cloudinary
- ✅ Address & profile management
- ✅ Auditing with `created_by`, `updated_by`
- ✅ Exception handling with meaningful responses
- 🚧 Frontend (React) coming soon

---

## 🧪 How to Run

### Prerequisites
- Java 17
- Maven
- MySQL (running and database created)
- Cloudinary account (optional)
- Gmail App Password (for email)

### 1. Clone the project

```bash
git clone https://github.com/yourusername/dicestore.git
cd dicestore
2. Set environment variables
Create a .env file or use application.properties. Example:

properties
Copy
Edit
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/dicestore
spring.datasource.username=root
spring.datasource.password=yourpassword

spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.protocol=smtp
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

cloudinary.cloud_name=your_cloud_name
cloudinary.api_key=your_api_key
cloudinary.api_secret=your_api_secret
3. Run the application
bash
Copy
Edit
./mvnw spring-boot:run
🔐 Authentication
Login returns a JWT token

Include it in Authorization: Bearer <token> headers for authenticated routes.

Roles: ROLE_USER, ROLE_ADMIN

📬 Email Support
Users receive an email verification link when updating emails.

Email template uses Thymeleaf but can be replaced by a frontend HTML string passed to the backend.

🔎 Auditing
Using Spring Data JPA's auditing:

created_at, updated_at

created_by, updated_by

Automatically filled using AuditorAware + SecurityContextHolder

🧩 Future Plans
Add full React frontend

Payment integration (e.g., Stripe)

Order tracking

Product reviews

Admin dashboard

🛠 Tools You May Use
Postman / cURL for API testing

MySQL Workbench for DB inspection

Lombok Plugin in your IDE

Spring Boot DevTools for hot reload

🧑‍💻 Author
Victor Ukoh
📧 ukohatochukwuvictor@gmail.com

📄 License
This project is licensed under the MIT License.

⭐️ Support
Give this project a ⭐️ if you find it helpful!

yaml
Copy
Edit

---

Let me know if you want this as a downloadable file or want to include usage examples for key endpoints (`/register`, `/login`, etc).