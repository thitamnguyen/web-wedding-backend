package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                // 1. Cấu hình CORS chi tiết tại đây cho phép React truy cập
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Tắt CSRF để cho phép gửi dữ liệu FormData từ bên ngoài vào không bị 403
                .csrf(csrf -> csrf.disable())

                // 3. Phân quyền chi tiết các đường dẫn
                .authorizeHttpRequests(auth -> auth
                        // Cho phép các HTTP OPTIONS (Preflight request do trình duyệt gửi) đi qua tự do
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Cho phép truy cập tự do vào toàn bộ API bắt đầu bằng /api/
                        .requestMatchers("/api/**").permitAll()

                        // Cho phép truy cập tài nguyên tĩnh công khai (ảnh upload, ảnh váy cưới)
                        .requestMatchers("/uploads/**", "/images/**").permitAll()

                        // Các request hệ thống khác (nếu có) mới bắt đăng nh   ập xác thực
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    // Định nghĩa Bean CORS cấu hình chuẩn xác cho port 5174 của React
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // Origin của React
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*")); // Cho phép mọi Header truyền lên
        configuration.setExposedHeaders(List.of("X-Try-On-Mode", "X-Try-On-Notice"));
        configuration.setAllowCredentials(true); // Cho phép gửi kèm Cookie / Auth Header nếu cần

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Áp dụng cấu hình này cho mọi url
        return source;
    }
}
