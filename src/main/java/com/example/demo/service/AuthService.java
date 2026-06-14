package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBcryptHash(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        return trimmed.startsWith("$2a$") || trimmed.startsWith("$2b$") || trimmed.startsWith("$2y$");
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }

        if (isBcryptHash(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword.trim());
        }

        return storedPassword.trim().equals(rawPassword);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        String password = request.getPassword() == null ? "" : request.getPassword();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        if (!matchesPassword(password, user.getPassword())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        if (!isBcryptHash(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
        }

        String roleName = user.getRole() != null ? user.getRole().getName() : "ROLE_CLIENT";
        String token = "authenticated-" + user.getId();

        return new AuthResponse(
                true,
                "Đăng nhập thành công!",
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                roleName,
                "ROLE_ADMIN".equalsIgnoreCase(roleName) ? "admin" : "user"
        );
    }

    public String register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã tồn tại");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(email);
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);

        Role role = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);

        userRepository.save(user);

        return "Đăng ký thành công!";
    }
}
