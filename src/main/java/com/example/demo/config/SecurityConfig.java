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
                .cors(cors -> {}) // Giữ nguyên cấu hình CORS để gọi từ React sang
                .csrf(csrf -> csrf.disable()) // Tắt CSRF bảo vệ chống lỗi POST/PUT

                .authorizeHttpRequests(auth -> auth
                        // 🔥 SỬA DÒNG NÀY: Mở khóa TẤT CẢ mọi API bắt đầu bằng /api/ công khai hoàn toàn
                        .requestMatchers("/api/**").permitAll()

                        // Các tài nguyên tĩnh, ảnh ọt mở hết
                        .requestMatchers("/uploads/**", "/images/**").permitAll()

                        // Tất cả các request khác cũng cho qua để tránh lỗi 403
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}