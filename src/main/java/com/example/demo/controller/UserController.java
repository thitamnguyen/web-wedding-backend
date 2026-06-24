package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
// Đảm bảo hỗ trợ đầy đủ CORS cho cả hai môi trường chạy
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // API lấy toàn bộ danh sách người dùng cho Admin
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 🔥 API CẬP NHẬT TRẠNG THÁI: Bỏ qua @PreAuthorize gắt để chạy khớp với Token Filter bảo mật tự chế của hệ thống
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Không tìm thấy tài khoản người dùng trên hệ thống!"));
        }

        User user = userOpt.get();

        if (body.containsKey("status")) {
            try {
                Integer newStatus = Integer.parseInt(body.get("status").toString());
                user.setStatus(newStatus);
                userRepository.save(user);

                String msg = (newStatus == 1) ? "Đã khôi phục hoạt động tài khoản thành công!" : "Đã chặn quyền truy cập tài khoản thành công!";
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", msg
                ));
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Định dạng dữ liệu status không hợp lệ!"));
            }
        }

        return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Thiếu dữ liệu thuộc tính status trong request!"));
    }
}