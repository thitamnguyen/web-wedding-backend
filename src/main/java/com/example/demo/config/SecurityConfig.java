package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {}) // Giữ nguyên cấu hình CORS gọi từ file CorsConfig

                .csrf(csrf -> csrf.disable()) // Tắt CSRF

                .authorizeHttpRequests(auth -> auth
                        // 1. Cho phép truy cập TẤT CẢ các API bắt đầu bằng /api/ tự do để test đồ án thuận tiện hơn
                        .requestMatchers("/api/**").permitAll()

                        // 2. Nếu có tài nguyên tĩnh như thư mục chứa ảnh upload (ví dụ: /uploads/**)
                        .requestMatchers("/uploads/**", "/images/**").permitAll()

                        // Các request hệ thống khác (nếu có) mới bắt xác thực
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}