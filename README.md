# 🚀 Backend Authentication API (Spring Boot + JWT)

## 📌 Giới thiệu
Đây là Backend RESTful API xây dựng bằng **Spring Boot**, cung cấp chức năng:

- 🔑 Đăng ký (Register)
- 🔐 Đăng nhập (Login)
- 🔒 Xác thực bằng JWT (JSON Web Token)
- 🔑 Mã hóa password bằng BCrypt
- 👤 Quản lý User và Role

---

## 🛠️ Công nghệ sử dụng

- Java 17+
- Spring Boot
- Spring Data JPA
- Spring Security
- JWT (io.jsonwebtoken)
- MySQL
- Maven

---

## 🏗️ Cấu trúc project
src/main/java/com/example/demo
│
├── controller # Xử lý request (AuthController)
├── service # Business logic (AuthService)
├── repository # JPA Repository (UserRepository, RoleRepository)
├── model # Entity (User, Role)
├── dto # DTO (LoginRequest, RegisterRequest)
├── config # JWT, Security, CORS
