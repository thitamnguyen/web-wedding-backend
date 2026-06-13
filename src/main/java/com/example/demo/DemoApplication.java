package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import com.example.demo.config.SecurityConfig; // Import file cấu hình của em vào đây

@SpringBootApplication
@Import(SecurityConfig.class) // 👉 ÉP SPRING BOOT PHẢI ĐỌC FILE SECURITYCONFIG
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}